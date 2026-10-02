package de.fahrerabrechnung.data.card

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.*
import java.security.MessageDigest
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

enum class CardImportSelection { ACTIVITY, VEHICLES, PAIR }

data class SavedDriverImport(
    val id: String, val profileId: String, val driverCardId: String,
    val savedAtEpochMillis: Long, val newSourceCount: Int,
    val activity: SavedCardActivity?, val vehicles: SavedCardVehicles?,
    val pair: SavedCardSourcePair?,
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
        val activity = if (selection != CardImportSelection.VEHICLES) activities.prepare(result, now) else null
        val days = activity?.let {
            CardActivityParser.parse(CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords))
        }
        val vehicle = if (selection != CardImportSelection.ACTIVITY) {
            require(!result.vehicleReadFailed)
            vehicles.prepare(result, now)
        } else null
        val uses = vehicle?.let {
            CardVehiclesParser.parse(CardVehiclesFile(it.newestRecordIndex, it.recordCount,
                CardGeneration.valueOf(it.generation), it.records))
        }
        return database.withTransaction {
            val cardId = bindInside(card, false)
            val profile = checkNotNull(dao.localProfile())
            val pairId = if (activity != null && vehicle != null) "${activity.id}:${vehicle.id}" else null
            dao.sources(requestId)?.let { existing ->
                check(existing.driverCardId == cardId && existing.activityId == activity?.id &&
                    existing.vehicleId == vehicle?.id && existing.pairId == pairId)
                return@withTransaction decode(existing, profile.id)
            }
            val newActivity = activity != null && database.cardActivityImportDao().getById(activity.id) == null
            val newVehicle = vehicle != null && database.cardVehicleImportDao().getById(vehicle.id) == null
            if (activity != null && vehicle != null) pairs.storePrepared(activity, vehicle, now)
            else {
                activity?.let { database.cardActivityImportDao().insert(it) }
                vehicle?.let { database.cardVehicleImportDao().insert(it) }
            }
            dao.insertSession(ImportSessionEntity(requestId, profile.id, "PARTIAL", now, now))
            val sources = DriverImportSourcesEntity(requestId, cardId, activity?.id, vehicle?.id,
                pairId, (if (newActivity) 1 else 0) + (if (newVehicle) 1 else 0))
            dao.insertSources(sources)
            if (activity != null) normalizedActivities.save(cardId, activity, checkNotNull(days))
            if (vehicle != null) normalizedVehicles.save(cardId, vehicle, checkNotNull(uses))
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
            sources.newSourceCount, activity, vehicle, pair)
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
