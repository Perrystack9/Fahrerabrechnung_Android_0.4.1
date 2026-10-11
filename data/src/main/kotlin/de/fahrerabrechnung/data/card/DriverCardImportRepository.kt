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
    val places: SavedCardPlaces? = null,
    val placesAuthentication: SavedCardPlacesAuthentication? = null,
    val borderCrossings: SavedCardBorderCrossings? = null,
    val loadUnload: SavedCardLoadUnload? = null,
    val loadType: SavedCardLoadType? = null,
    val vehicleUnits: SavedCardVehicleUnits? = null,
    val metadata: Map<CardMetadataKind, SavedCardMetadata> = emptyMap(),
    val certificates: Map<CardCertificateKind, SavedCardCertificate> = emptyMap(),
    val identitySources: Map<CardIdentitySourceKind, SavedCardIdentitySource> = emptyMap(),
    val directory: SavedCardDirectory? = null,
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
    private val places = CardPlacesStore(database.cardPlacesDao())
    private val borderCrossings = CardBorderCrossingsStore(database.cardBorderCrossingsDao())
    private val loadUnload = CardLoadUnloadStore(database.cardLoadUnloadDao())
    private val loadType = CardLoadTypeStore(database.cardLoadTypeDao())
    private val vehicleUnits = CardVehicleUnitsStore(database.cardVehicleUnitsDao())
    private val metadata = CardMetadataStore(database.cardMetadataDao())
    private val certificates = CardCertificateStore(database.cardMetadataDao())
    private val identitySources = CardIdentitySourceStore(database.cardMetadataDao())
    private val directories = CardDirectoryStore(database.cardMetadataDao())

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
        val placeFile = if (selection == CardImportSelection.ALL_AVAILABLE) result.places?.let {
            require(!result.placeReadFailed && it.generation == result.generation)
            CardPlacesFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardPlacesParser.parse(frozen) }
        } else null
        val placeAuth = if (selection == CardImportSelection.ALL_AVAILABLE) result.placesAuthentication?.let {
            require(!result.placesAuthenticationReadFailed && it.generation == result.generation)
            CardPlacesAuthenticationFile(it.generation, it.bytes).also { frozen -> CardPlacesAuthenticationParser.parse(frozen) }
        } else null
        val borders = if (selection == CardImportSelection.ALL_AVAILABLE) result.borderCrossings?.let {
            require(!result.borderCrossingsReadFailed && it.generation == result.generation)
            CardBorderCrossingsFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardBorderCrossingsParser.parse(frozen) }
        } else null
        val loads = if (selection == CardImportSelection.ALL_AVAILABLE) result.loadUnload?.let {
            require(!result.loadUnloadReadFailed && it.generation == result.generation)
            CardLoadUnloadFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardLoadUnloadParser.parse(frozen) }
        } else null
        val types = if (selection == CardImportSelection.ALL_AVAILABLE) result.loadType?.let {
            require(!result.loadTypeReadFailed && it.generation == result.generation)
            CardLoadTypeFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardLoadTypeParser.parse(frozen) }
        } else null
        val units = if (selection == CardImportSelection.ALL_AVAILABLE) result.vehicleUnits?.let {
            require(!result.vehicleUnitsReadFailed && it.generation == result.generation)
            CardVehicleUnitsFile(it.generation, it.recordCount, it.bytes).also { frozen -> CardVehicleUnitsParser.parse(frozen) }
        } else null
        val metadataFiles = if (selection == CardImportSelection.ALL_AVAILABLE) result.metadata.mapNotNull { (kind, read) ->
            read.file?.let {
                require(!read.readFailed && it.kind == kind && it.generation == result.generation)
                CardMetadataFile(kind, it.generation, it.bytes).also { frozen -> CardMetadataParser.parse(frozen) }
            }
        } else emptyList()
        val certificateFiles = if (selection == CardImportSelection.ALL_AVAILABLE) result.certificates.mapNotNull { (kind, read) ->
            read.file?.let {
                require(!read.readFailed && it.kind == kind && it.generation == result.generation && kind.supports(result.generation))
                CardCertificateFile(kind, it.generation, it.bytes).also { frozen -> CardCertificateParser.parse(frozen) }
            }
        } else emptyList()
        val identityFiles = if (selection == CardImportSelection.ALL_AVAILABLE) result.identitySources.mapNotNull { (kind, read) ->
            read.file?.let {
                require(!read.readFailed && it.kind == kind && kind.supports(result.generation))
                CardIdentitySourceFile(kind, it.generation, it.bytes).also { frozen -> CardIdentitySourceParser.validate(frozen, card) }
            }
        } else emptyList()
        val directory = if (selection == CardImportSelection.ALL_AVAILABLE) result.directory?.let {
            require(!result.directoryReadFailed && it.generation == result.generation)
            CardDirectoryFile(it.generation, it.bytes).also { frozen -> CardDirectoryParser.parse(frozen) }
        } else null
        val extraFiles = listOfNotNull(events, faults)
        require(activity != null || vehicle != null || control != null || extraFiles.isNotEmpty() || conditions != null || positions != null || authentication != null || placeFile != null || placeAuth != null || borders != null || loads != null || types != null || units != null || metadataFiles.isNotEmpty() || certificateFiles.isNotEmpty() || identityFiles.isNotEmpty() || directory != null)
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
                check(database.cardPlacesDao().source(requestId, CardPlacesFileKind.PLACES.name)?.sourceId ==
                    placeFile?.let { CardPlacesStore.fingerprint(cardId, it) })
                check(database.cardPlacesDao().source(requestId, CardPlacesFileKind.AUTHENTICATION.name)?.sourceId ==
                    placeAuth?.let { CardPlacesStore.fingerprint(cardId, it) })
                check(database.cardBorderCrossingsDao().source(requestId)?.sourceId ==
                    borders?.let { CardBorderCrossingsStore.fingerprint(cardId, it) })
                check(database.cardLoadUnloadDao().source(requestId)?.sourceId ==
                    loads?.let { CardLoadUnloadStore.fingerprint(cardId, it) })
                check(database.cardLoadTypeDao().source(requestId)?.sourceId ==
                    types?.let { CardLoadTypeStore.fingerprint(cardId, it) })
                check(database.cardVehicleUnitsDao().source(requestId)?.sourceId ==
                    units?.let { CardVehicleUnitsStore.fingerprint(cardId, it) })
                check(database.cardMetadataDao().sources(requestId).associate { it.kind to it.sourceId } ==
                    metadataFiles.associate { it.kind.name to CardMetadataStore.fingerprint(cardId, it) } +
                        certificateFiles.associate { it.kind.storageKind to CardCertificateStore.fingerprint(cardId, it) } +
                        identityFiles.associate { it.kind.storageKind to CardIdentitySourceStore.fingerprint(cardId, it) } +
                        directory?.let { mapOf(CardDirectoryStore.STORAGE_KIND to CardDirectoryStore.fingerprint(cardId, it)) }.orEmpty())
                return@withTransaction decode(existing, profile.id)
            }
            val newActivity = activity != null && database.cardActivityImportDao().getById(activity.id) == null
            val newVehicle = vehicle != null && database.cardVehicleImportDao().getById(vehicle.id) == null
            val newControl = control != null && database.cardControlDao().get(CardControlStore.fingerprint(cardId, control)) == null
            val newExtras = extraFiles.count { database.cardEventFaultDao().get(CardEventFaultStore.fingerprint(cardId, it)) == null }
            val newConditions = conditions != null && database.cardSpecificConditionDao().get(CardSpecificConditionStore.fingerprint(cardId, conditions)) == null
            val newPositions = positions != null && database.cardGnssDao().get(CardGnssStore.fingerprint(cardId, positions)) == null
            val newAuthentication = authentication != null && database.cardGnssDao().get(CardGnssStore.fingerprint(cardId, authentication)) == null
            val newPlaces = placeFile != null && database.cardPlacesDao().get(CardPlacesStore.fingerprint(cardId, placeFile)) == null
            val newPlaceAuth = placeAuth != null && database.cardPlacesDao().get(CardPlacesStore.fingerprint(cardId, placeAuth)) == null
            val newBorders = borders != null && database.cardBorderCrossingsDao().get(CardBorderCrossingsStore.fingerprint(cardId, borders)) == null
            val newLoads = loads != null && database.cardLoadUnloadDao().get(CardLoadUnloadStore.fingerprint(cardId, loads)) == null
            val newTypes = types != null && database.cardLoadTypeDao().get(CardLoadTypeStore.fingerprint(cardId, types)) == null
            val newUnits = units != null && database.cardVehicleUnitsDao().get(CardVehicleUnitsStore.fingerprint(cardId, units)) == null
            val newMetadata = metadataFiles.count { database.cardMetadataDao().get(CardMetadataStore.fingerprint(cardId, it)) == null }
            val newCertificates = certificateFiles.count { database.cardMetadataDao().get(CardCertificateStore.fingerprint(cardId, it)) == null }
            val newIdentities = identityFiles.count { database.cardMetadataDao().get(CardIdentitySourceStore.fingerprint(cardId, it)) == null }
            val newDirectory = directory != null && database.cardMetadataDao().get(CardDirectoryStore.fingerprint(cardId, directory)) == null
            if (activity != null && vehicle != null) pairs.storePrepared(activity, vehicle, now)
            else {
                activity?.let { database.cardActivityImportDao().insert(it) }
                vehicle?.let { database.cardVehicleImportDao().insert(it) }
            }
            dao.insertSession(ImportSessionEntity(requestId, profile.id, "PARTIAL", now, now))
            val sources = DriverImportSourcesEntity(requestId, cardId, activity?.id, vehicle?.id,
                pairId, (if (newActivity) 1 else 0) + (if (newVehicle) 1 else 0) + (if (newControl) 1 else 0) + newExtras + (if (newConditions) 1 else 0) + (if (newPositions) 1 else 0) + (if (newAuthentication) 1 else 0) + (if (newPlaces) 1 else 0) + (if (newPlaceAuth) 1 else 0) + (if (newBorders) 1 else 0) + (if (newLoads) 1 else 0) + (if (newTypes) 1 else 0) + (if (newUnits) 1 else 0) + newMetadata + newCertificates + newIdentities + (if (newDirectory) 1 else 0))
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
            if (placeFile != null) check(places.save(cardId, requestId, placeFile, now) == newPlaces)
            if (placeAuth != null) check(places.save(cardId, requestId, placeAuth, now) == newPlaceAuth)
            if (borders != null) check(borderCrossings.save(cardId, requestId, borders, now) == newBorders)
            if (loads != null) check(loadUnload.save(cardId, requestId, loads, now) == newLoads)
            if (types != null) check(loadType.save(cardId, requestId, types, now) == newTypes)
            if (units != null) check(vehicleUnits.save(cardId, requestId, units, now) == newUnits)
            var storedMetadata = 0
            for (file in metadataFiles) if (metadata.save(cardId, requestId, file, now)) storedMetadata++
            check(storedMetadata == newMetadata)
            var storedCertificates = 0
            for (file in certificateFiles) if (certificates.save(cardId, requestId, file, now)) storedCertificates++
            check(storedCertificates == newCertificates)
            var storedIdentities = 0
            for (file in identityFiles) if (identitySources.save(cardId, requestId, file, now)) storedIdentities++
            check(storedIdentities == newIdentities)
            if (directory != null) check(directories.save(cardId, requestId, directory, now) == newDirectory)
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
            gnss.positions(sources.driverCardId, session.id), gnss.authentication(sources.driverCardId, session.id),
            places.places(sources.driverCardId, session.id), places.authentication(sources.driverCardId, session.id), borderCrossings.load(sources.driverCardId, session.id), loadUnload.load(sources.driverCardId, session.id), loadType.load(sources.driverCardId, session.id), vehicleUnits.load(sources.driverCardId, session.id), metadata.load(sources.driverCardId, session.id), certificates.load(sources.driverCardId, session.id), identitySources.load(sources.driverCardId, session.id), directories.load(sources.driverCardId, session.id))
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
