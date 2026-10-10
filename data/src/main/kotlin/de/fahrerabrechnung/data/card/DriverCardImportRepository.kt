package de.fahrerabrechnung.data.card

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.*
import java.security.MessageDigest
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

enum class CardImportSelection { ACTIVITY, VEHICLES, PAIR, ALL_AVAILABLE }

data class SavedDriverImport(
    val id: String, val profileId: String, val driverCardId: String,
    val savedAtEpochMillis: Long, val newSourceCount: Int,
    val activity: SavedCardActivity?, val vehicles: SavedCardVehicles?,
    val pair: SavedCardSourcePair?,
    val controls: SavedCardControl? = null,
    val events: SavedCardEventFault? = null,
    val faults: SavedCardEventFault? = null,
    val specificConditions: SavedCardSpecificConditions? = null,
    val gnssPositions: SavedCardGnssPositions? = null,
    val gnssAuthentication: SavedCardGnssAuthentication? = null,
)

/** Single-driver binding plus explicitly saved partial imports of the supported sources. */
class DriverCardImportRepository @Inject constructor(
    private val database: AppDatabase,
    private val activities: CardActivityImportRepository,
    private val vehicles: CardVehicleImportRepository,
    private val pairs: CardSourcePairRepository,
    private val clock: Clock,
) {
    private val dao get() = database.driverProfileDao()
    private val normalizedActivities = NormalizedCardActivityStore(database)
    private val normalizedVehicles = NormalizedCardVehicleStore(database)
    private val controls = CardControlStore(database.cardControlDao())
    private val eventFaults = CardEventFaultStore(database.cardEventFaultDao())
    private val specificConditions = CardSpecificConditionStore(database.cardSpecificConditionDao())

    private val gnss = CardGnssStore(database.cardGnssDao())

    suspend fun profile(): ProfileEntity? = dao.localProfile()

    suspend fun review(card: DriverCard): CardBindingReview = database.withTransaction {
        validate(card)
        reviewInside(card)
    }

    suspend fun bind(card: DriverCard, confirmedOwnCard: Boolean = false): String = database.withTransaction {
        validate(card)
        bindInside(card, confirmedOwnCard)
    }

    suspend fun save(result: CardReadResult, requestId: String, selection: CardImportSelection): SavedDriverImport {
        require(requestId.isNotBlank() && requestId.length <= 128)
        require(result.recognition == CardRecognition.DRIVER && result.card != null &&
            result.card.generation == result.generation)
        val card = result.card
        validate(card)
        val now = clock.millis()
        // Validate/copy selected files before any profile, card or source mutation.
        val activity = if (selection in setOf(CardImportSelection.ACTIVITY, CardImportSelection.PAIR) ||
            (selection == CardImportSelection.ALL_AVAILABLE && result.activity != null)) {
            require(!result.activityReadFailed)
            activities.prepare(result, now)
        } else null
        val days = activity?.let {
            CardActivityParser.parse(CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords))
        }
        val vehicle = if (selection in setOf(CardImportSelection.VEHICLES, CardImportSelection.PAIR) ||
            (selection == CardImportSelection.ALL_AVAILABLE && result.vehicles != null)) {
            require(!result.vehicleReadFailed)
            vehicles.prepare(result, now)
        } else null
        val uses = vehicle?.let {
            CardVehiclesParser.parse(CardVehiclesFile(it.newestRecordIndex, it.recordCount,
                CardGeneration.valueOf(it.generation), it.records))
        }
        val control = if (selection == CardImportSelection.ALL_AVAILABLE) result.controls?.let {
            require(!result.controlReadFailed && it.generation == result.generation)
            CardControlFile(it.generation, it.bytes).also { file -> CardControlParser.parse(file) }
        } else null
        fun prepareEventFault(file: CardEventFaultFile?, kind: CardEventFaultKind, failed: Boolean): CardEventFaultFile? =
            if (selection == CardImportSelection.ALL_AVAILABLE) file?.let {
                require(!failed && it.kind == kind && it.generation == result.generation)
                CardEventFaultFile(kind, it.generation, it.recordsPerType, it.bytes).also { frozen -> CardEventFaultParser.parse(frozen) }
            } else null
        val events = prepareEventFault(result.events, CardEventFaultKind.EVENTS, result.eventReadFailed)
        val faults = prepareEventFault(result.faults, CardEventFaultKind.FAULTS, result.faultReadFailed)
        val conditions = if (selection == CardImportSelection.ALL_AVAILABLE) result.specificConditions?.let {
            require(!result.specificConditionReadFailed && it.generation == result.generation)
            CardSpecificConditionFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardSpecificConditionParser.parse(frozen) }
        } else null
        val positions = if (selection == CardImportSelection.ALL_AVAILABLE) result.gnssPositions?.let {
            require(!result.gnssPositionReadFailed && it.generation == result.generation)
            CardGnssAccumulatedDrivingFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardGnssAccumulatedDrivingParser.parse(frozen) }
        } else null
        val authentication = if (selection == CardImportSelection.ALL_AVAILABLE) result.gnssAuthentication?.let {
            require(!result.gnssAuthenticationReadFailed && it.generation == result.generation)
            CardGnssAuthenticationFile(it.generation, it.bytes).also { frozen -> CardGnssAuthenticationParser.parse(frozen) }
        } else null
        val extraFiles = listOfNotNull(events, faults)
        require(activity != null || vehicle != null || control != null || extraFiles.isNotEmpty() || conditions != null || positions != null || authentication != null)
        return database.withTransaction {
            val cardId = bindInside(card, false)
            val profile = checkNotNull(dao.localProfile())
            val pairId = if (activity != null && vehicle != null) "${activity.id}:${vehicle.id}" else null
            dao.sources(requestId)?.let { existing ->
                check(existing.driverCardId == cardId && existing.activityId == activity?.id &&
                    existing.vehicleId == vehicle?.id && existing.pairId == pairId)
                check(database.cardControlDao().source(requestId)?.controlId ==
                    control?.let { CardControlStore.fingerprint(cardId, it) })
                for ((kind, file) in listOf(CardEventFaultKind.EVENTS to events, CardEventFaultKind.FAULTS to faults))
                    check(database.cardEventFaultDao().source(requestId, kind.name)?.sourceId ==
                        file?.let { CardEventFaultStore.fingerprint(cardId, it) })
                check(database.cardSpecificConditionDao().source(requestId)?.sourceId ==
                    conditions?.let { CardSpecificConditionStore.fingerprint(cardId, it) })
                check(database.cardGnssDao().source(requestId, CardGnssFileKind.POSITIONS.name)?.sourceId ==
                    positions?.let { CardGnssStore.fingerprint(cardId, it) })
                check(database.cardGnssDao().source(requestId, CardGnssFileKind.AUTHENTICATION.name)?.sourceId ==
                    authentication?.let { CardGnssStore.fingerprint(cardId, it) })
                return@withTransaction decode(existing, profile.id)
            }
            val newActivity = activity != null && database.cardActivityImportDao().getById(activity.id) == null
            val newVehicle = vehicle != null && database.cardVehicleImportDao().getById(vehicle.id) == null
            val newControl = control != null && database.cardControlDao().get(CardControlStore.fingerprint(cardId, control)) == null
            val newExtras = extraFiles.count { database.cardEventFaultDao().get(CardEventFaultStore.fingerprint(cardId, it)) == null }
            val newConditions = conditions != null && database.cardSpecificConditionDao().get(CardSpecificConditionStore.fingerprint(cardId, conditions)) == null
            val newPositions = positions != null && database.cardGnssDao().get(CardGnssStore.fingerprint(cardId, positions)) == null
            val newAuthentication = authentication != null && database.cardGnssDao().get(CardGnssStore.fingerprint(cardId, authentication)) == null
            if (activity != null && vehicle != null) pairs.storePrepared(activity, vehicle, now)
            else {
                activity?.let { database.cardActivityImportDao().insert(it) }
                vehicle?.let { database.cardVehicleImportDao().insert(it) }
            }
            dao.insertSession(ImportSessionEntity(requestId, profile.id, "PARTIAL", now, now))
            val sources = DriverImportSourcesEntity(requestId, cardId, activity?.id, vehicle?.id,
                pairId, (if (newActivity) 1 else 0) + (if (newVehicle) 1 else 0) + (if (newControl) 1 else 0) + newExtras + (if (newConditions) 1 else 0) + (if (newPositions) 1 else 0) + (if (newAuthentication) 1 else 0))
            dao.insertSources(sources)
            if (activity != null) normalizedActivities.save(cardId, activity, checkNotNull(days))
            if (vehicle != null) normalizedVehicles.save(cardId, vehicle, checkNotNull(uses))
            if (control != null) check(controls.save(cardId, requestId, control, now) == newControl)
            var storedExtras = 0
            for (file in extraFiles) if (eventFaults.save(cardId, requestId, file, now)) storedExtras++
            check(storedExtras == newExtras)
            if (conditions != null) check(specificConditions.save(cardId, requestId, conditions, now) == newConditions)
            if (positions != null) check(gnss.save(cardId, requestId, positions, now) == newPositions)
            if (authentication != null) check(gnss.save(cardId, requestId, authentication, now) == newAuthentication)
            decode(sources, profile.id)
        }
    }

    suspend fun all(): List<SavedDriverImport> = database.withTransaction {
        val profile = dao.localProfile() ?: return@withTransaction emptyList()
        dao.imports(profile.id).map { decode(it, profile.id) }
    }

    suspend fun latest(): SavedDriverImport? = database.withTransaction {
        val profile = dao.localProfile() ?: return@withTransaction null
        dao.imports(profile.id).firstOrNull()?.let { decode(it, profile.id) }
    }

    suspend fun get(id: String): SavedDriverImport? = database.withTransaction {
        val profile = dao.localProfile() ?: return@withTransaction null
        val sources = dao.sources(id) ?: return@withTransaction null
        if (dao.session(id, profile.id) == null || dao.card(sources.driverCardId)?.profileId != profile.id)
            return@withTransaction null
        decode(sources, profile.id)
    }

    suspend fun vehicleSources(): List<SavedCardVehicles> = database.withTransaction {
        val profile = dao.localProfile() ?: return@withTransaction emptyList()
        dao.imports(profile.id).filter { it.vehicleId != null }
            .distinctBy { it.driverCardId to it.vehicleId }.map {
                decodeVehicles(it.driverCardId, checkNotNull(it.vehicleId))
            }
    }

    private suspend fun reviewInside(card: DriverCard): CardBindingReview {
        val profile = dao.localProfile()
        val identity = dao.identityByKey(key(card))
        if (identity != null) {
            val assigned = checkNotNull(dao.card(identity.cardId))
            if (profile?.id != assigned.profileId || identity.surname != card.holder.surname ||
                identity.givenNames != card.holder.givenNames || identity.birthDateIso != card.holder.birthDate?.toString())
                return CardBindingReview(CardBindingKind.FOREIGN_CARD, profile?.name)
            return CardBindingReview(CardBindingKind.KNOWN_CARD, checkNotNull(profile).name)
        }
        if (profile == null) return CardBindingReview(CardBindingKind.FIRST_CARD, null)
        val known = dao.cards(profile.id).map { holder(it.surname, it.givenNames, it.birthDateIso) }
        val kind = when (CardHolderMatching.compare(card.holder, known)) {
            CardHolderMatch.SAME -> CardBindingKind.SAME_HOLDER
            CardHolderMatch.UNCLEAR -> CardBindingKind.CONFIRM_REPLACEMENT
            CardHolderMatch.DIFFERENT -> CardBindingKind.FOREIGN_CARD
        }
        return CardBindingReview(kind, profile.name)
    }

    private suspend fun bindInside(card: DriverCard, confirmed: Boolean): String {
        val review = reviewInside(card)
        when (review.kind) {
            CardBindingKind.FOREIGN_CARD -> throw ForeignCardRejected()
            CardBindingKind.FIRST_CARD, CardBindingKind.CONFIRM_REPLACEMENT -> if (!confirmed) throw CardBindingRequired()
            CardBindingKind.KNOWN_CARD -> return checkNotNull(dao.identityByKey(key(card))).cardId
            CardBindingKind.SAME_HOLDER -> Unit
        }
        val now = clock.millis()
        val profile = dao.localProfile() ?: ProfileEntity(UUID.randomUUID().toString(),
            "${card.holder.givenNames} ${card.holder.surname}".trim(), now, now).also {
            database.profileDao().insert(it)
            dao.insertLocal(LocalDriverProfileEntity(profileId = it.id))
        }
        val id = UUID.randomUUID().toString()
        dao.insertCard(DriverCardEntity(id, profile.id, key(card),
            if (review.kind == CardBindingKind.SAME_HOLDER) "MATCHED_HOLDER" else "CONFIRMED_OWN_CARD", now, now))
        dao.insertIdentity(DriverCardIdentityEntity(id, key(card), card.card.number, card.generation.name,
            card.card.issuingState, card.card.issued.epochSecond, card.card.validFrom.epochSecond,
            card.card.expires.epochSecond, card.holder.surname, card.holder.givenNames, card.holder.birthDate?.toString()))
        return id
    }

    private suspend fun decode(sources: DriverImportSourcesEntity, profileId: String): SavedDriverImport {
        val session = checkNotNull(dao.session(sources.sessionId, profileId))
        check(checkNotNull(dao.card(sources.driverCardId)).profileId == profileId)
        val activity = sources.activityId?.let {
            val row = checkNotNull(database.cardActivityImportDao().getById(it))
            activities.decode(row, normalizedActivities.load(sources.driverCardId, row))
        }
        val vehicle = sources.vehicleId?.let { decodeVehicles(sources.driverCardId, it) }
        val pair = sources.pairId?.let { id ->
            val row = checkNotNull(database.cardSourcePairDao().getById(id))
            check(row.activityId == sources.activityId && row.vehicleId == sources.vehicleId)
            SavedCardSourcePair(id, checkNotNull(activity), checkNotNull(vehicle), row.savedAtEpochMillis)
        }
        return SavedDriverImport(session.id, profileId, sources.driverCardId, session.createdAtEpochMillis,
            sources.newSourceCount, activity, vehicle, pair, controls.load(sources.driverCardId, session.id),
            eventFaults.load(sources.driverCardId, session.id, CardEventFaultKind.EVENTS),
            eventFaults.load(sources.driverCardId, session.id, CardEventFaultKind.FAULTS),
            specificConditions.load(sources.driverCardId, session.id),
            gnss.positions(sources.driverCardId, session.id), gnss.authentication(sources.driverCardId, session.id))
    }

    private suspend fun decodeVehicles(cardId: String, sourceId: String): SavedCardVehicles {
        val source = checkNotNull(database.cardVehicleImportDao().getById(sourceId))
        return vehicles.decode(source, normalizedVehicles.load(cardId, source))
    }

    private fun validate(card: DriverCard) {
        require(card.generation in setOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2))
        require(card.card.number.isNotBlank() && card.holder.surname.isNotBlank() && card.card.expires > card.card.validFrom)
    }

    private fun key(card: DriverCard): String {
        // This identifies a card instance, never the driver. Profile/card IDs are independent UUIDs.
        val parts = listOf(card.card.issuingState.toString(), card.card.number, card.card.issued.epochSecond.toString(),
            card.card.validFrom.epochSecond.toString(), card.card.expires.epochSecond.toString())
        val digest = MessageDigest.getInstance("SHA-256")
        parts.forEach { digest.update(it.toByteArray(Charsets.UTF_8)); digest.update(0) }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
