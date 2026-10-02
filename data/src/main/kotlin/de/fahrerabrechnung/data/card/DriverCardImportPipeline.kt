package de.fahrerabrechnung.data.card

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class CardImportRunStatus { IN_PROGRESS, SAVED, PARTIAL, FAILED, INTERRUPTED }
enum class CardImportFileKind { ACTIVITIES, VEHICLES, CONTROLS }
enum class CardImportFileStatus { READY, STORED, MISSING, READ_FAILED, INVALID, EMPTY, NOT_STORED }

data class CardImportFileResult(
    val kind: CardImportFileKind, val status: CardImportFileStatus, val decodedCount: Int,
    val firstUtcEpochSeconds: Long?, val lastUtcEpochSeconds: Long?, val failureCode: String?,
)

data class CardImportRun(
    val id: String, val driverCardId: String, val cardNumber: String, val generation: CardGeneration,
    val status: CardImportRunStatus, val startedAtEpochMillis: Long, val finishedAtEpochMillis: Long?,
    val sessionId: String?, val newSourceCount: Int, val failureCode: String?,
    val files: List<CardImportFileResult>,
)

/** Supported-source pipeline, not a claim that every driver-card EF is imported. */
@Singleton
class DriverCardImportPipeline @Inject constructor(
    private val database: AppDatabase,
    private val imports: DriverCardImportRepository,
    private val activities: CardActivityImportRepository,
    private val vehicles: CardVehicleImportRepository,
    private val clock: Clock,
) {
    private val dao get() = database.cardImportRunDao()
    private val initialization = Mutex()
    private var initialized = false

    // Singleton lifetime is the app process. Recover once, before any new process-owned run starts.
    private suspend fun initialize() = initialization.withLock {
        if (!initialized) {
            database.withTransaction {
                dao.unfinished().forEach { finishUnstored(it, CardImportRunStatus.INTERRUPTED, "INTERRUPTED") }
            }
            initialized = true
        }
    }

    suspend fun process(result: CardReadResult, requestId: String): CardImportRun {
        require(requestId.isNotBlank() && requestId.length <= 128)
        // Freeze mutable raw buffers before parsing, fingerprinting or any persistent mutation.
        val plan = prepare(result)
        initialize()
        val start = database.withTransaction {
            // Reuse existing explicit/strong matching rules. Foreign/unconfirmed reads write nothing.
            val cardId = imports.bind(checkNotNull(plan.read.card))
            val fingerprint = plan.fingerprint(cardId)
            dao.run(requestId)?.also {
                require(it.driverCardId == cardId && it.requestFingerprint == fingerprint)
            } ?: CardImportRunEntity(requestId, cardId, fingerprint,
                CardImportRunStatus.IN_PROGRESS.name, clock.millis(), null, null, 0, null).also {
                dao.insertRun(it)
                dao.insertFiles(plan.files.map { file -> file.entity(requestId) })
            }
        }
        if (start.status != CardImportRunStatus.IN_PROGRESS.name) return database.withTransaction { decode(start) }
        return try {
            database.withTransaction {
                val current = checkNotNull(dao.run(requestId))
                if (current.status != CardImportRunStatus.IN_PROGRESS.name) return@withTransaction decode(current)
                val selection = plan.selection
                if (selection == null) {
                    finishUnstored(current, CardImportRunStatus.FAILED, "NO_USABLE_DATA")
                } else {
                    val saved = imports.save(plan.usableRead, requestId, selection)
                    check(saved.driverCardId == current.driverCardId)
                    dao.updateFiles(dao.files(requestId).map {
                        if (it.status == CardImportFileStatus.READY.name) it.copy(status = CardImportFileStatus.STORED.name) else it
                    })
                    dao.updateRun(current.copy(status = if (plan.files.all { it.status == CardImportFileStatus.READY })
                        CardImportRunStatus.SAVED.name else CardImportRunStatus.PARTIAL.name,
                        finishedAtEpochMillis = clock.millis(), sessionId = saved.id, newSourceCount = saved.newSourceCount))
                }
                decode(checkNotNull(dao.run(requestId)))
            }
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { database.withTransaction {
                dao.run(requestId)?.takeIf { it.status == CardImportRunStatus.IN_PROGRESS.name }?.let {
                    finishUnstored(it, CardImportRunStatus.INTERRUPTED, "INTERRUPTED")
                }
            } }
            throw cancelled
        } catch (_: Exception) {
            // Source/session/normalization writes have rolled back together. The failed attempt remains.
            database.withTransaction {
                val current = checkNotNull(dao.run(requestId))
                if (current.status == CardImportRunStatus.IN_PROGRESS.name)
                    finishUnstored(current, CardImportRunStatus.FAILED, "STORAGE_FAILED")
                decode(checkNotNull(dao.run(requestId)))
            }
        }
    }

    suspend fun history(): List<CardImportRun> {
        initialize()
        return database.withTransaction {
            val profile = imports.profile() ?: return@withTransaction emptyList()
            dao.runs(profile.id).map { decode(it) }
        }
    }

    suspend fun earlierImports(): List<EarlierDriverImportRow> {
        initialize()
        return database.withTransaction {
            val profile = imports.profile() ?: return@withTransaction emptyList()
            dao.earlierImports(profile.id)
        }
    }

    private suspend fun finishUnstored(run: CardImportRunEntity, status: CardImportRunStatus, failure: String) {
        dao.updateFiles(dao.files(run.id).map {
            if (it.status == CardImportFileStatus.READY.name) it.copy(status = CardImportFileStatus.NOT_STORED.name, failureCode = failure) else it
        })
        dao.updateRun(run.copy(status = status.name, finishedAtEpochMillis = clock.millis(), failureCode = failure))
    }

    private suspend fun decode(run: CardImportRunEntity): CardImportRun {
        val profile = checkNotNull(imports.profile())
        check(checkNotNull(database.driverProfileDao().card(run.driverCardId)).profileId == profile.id)
        val identity = database.driverProfileDao().cards(profile.id).single { it.cardId == run.driverCardId }
        val files = dao.files(run.id)
        val kinds = files.map { it.kind }.toSet()
        // Historic runs attempted only the two original EFs. Do not invent new outcomes for them.
        check(kinds == setOf(CardImportFileKind.ACTIVITIES.name, CardImportFileKind.VEHICLES.name) ||
            kinds == CardImportFileKind.entries.map { it.name }.toSet())
        run.sessionId?.let { check(database.driverProfileDao().sources(it)?.driverCardId == run.driverCardId) }
        return CardImportRun(run.id, run.driverCardId, identity.cardNumber, CardGeneration.valueOf(identity.generation),
            CardImportRunStatus.valueOf(run.status), run.startedAtEpochMillis, run.finishedAtEpochMillis,
            run.sessionId, run.newSourceCount, run.failureCode, files.map {
                CardImportFileResult(CardImportFileKind.valueOf(it.kind), CardImportFileStatus.valueOf(it.status),
                    it.decodedCount, it.firstUtcEpochSeconds, it.lastUtcEpochSeconds, it.failureCode)
            })
    }

    private fun prepare(result: CardReadResult): Plan {
        require(result.recognition == CardRecognition.DRIVER && result.card != null && result.card.generation == result.generation)
        val read = result.copy(activity = result.activity?.let { CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords.copyOf()) },
            vehicles = result.vehicles?.let { CardVehiclesFile(it.newestRecordIndex, it.recordCount, it.generation, it.records.copyOf()) },
            controls = result.controls?.let { CardControlFile(it.generation, it.bytes) })
        val activity = inspect(CardImportFileKind.ACTIVITIES, read.activity != null, read.activityReadFailed) {
            val days = CardActivityParser.parse(checkNotNull(read.activity))
            val prepared = activities.prepare(read)
            CardImportFileResult(CardImportFileKind.ACTIVITIES, CardImportFileStatus.READY, prepared.dayCount,
                days.minOf { it.recordDateUtc.epochSecond }, days.maxOf { it.recordDateUtc.epochSecond }, null)
        }
        val vehicle = inspect(CardImportFileKind.VEHICLES, read.vehicles != null, read.vehicleReadFailed) {
            val uses = CardVehiclesParser.parse(checkNotNull(read.vehicles))
            if (uses.isEmpty()) CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.EMPTY,
                0, null, null, "EMPTY")
            else CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.READY, vehicles.prepare(read).useCount,
                uses.minOf { it.firstUseUtc.epochSecond },
                // An open end remains unknown; do not turn its first-use instant into an end.
                if (uses.any { it.lastUseUtc == null }) null else uses.maxOf { checkNotNull(it.lastUseUtc).epochSecond }, null)
        }
        // A missing field on an older technical result means not attempted, not a failed control read.
        val control = if (read.controls != null || read.controlReadFailed)
            inspect(CardImportFileKind.CONTROLS, read.controls != null, read.controlReadFailed) {
                val file = checkNotNull(read.controls)
                require(file.generation == read.generation)
                val record = CardControlParser.parse(file)
                // A valid unused EF is still a complete source, with zero recorded controls.
                CardImportFileResult(CardImportFileKind.CONTROLS, CardImportFileStatus.READY,
                    if (record == null) 0 else 1, record?.timeUtc?.epochSecond, record?.timeUtc?.epochSecond, null)
            } else null
        return Plan(read, listOfNotNull(activity, vehicle, control))
    }

    private inline fun inspect(kind: CardImportFileKind, present: Boolean, failed: Boolean,
                               validate: () -> CardImportFileResult): CardImportFileResult {
        val status = when {
            failed -> CardImportFileStatus.READ_FAILED
            !present -> CardImportFileStatus.MISSING
            else -> try { return validate() } catch (_: ParseError) { CardImportFileStatus.INVALID }
                catch (_: IllegalArgumentException) { CardImportFileStatus.INVALID }
        }
        return CardImportFileResult(kind, status, 0, null, null, status.name)
    }

    private data class Plan(val read: CardReadResult, val files: List<CardImportFileResult>) {
        val selection: CardImportSelection? get() {
            if (files.any { it.kind == CardImportFileKind.CONTROLS && it.status == CardImportFileStatus.READY })
                return CardImportSelection.ALL_AVAILABLE
            val activity = files[0].status == CardImportFileStatus.READY
            val vehicle = files[1].status == CardImportFileStatus.READY
            return when { activity && vehicle -> CardImportSelection.PAIR
                activity -> CardImportSelection.ACTIVITY; vehicle -> CardImportSelection.VEHICLES; else -> null }
        }

        val usableRead: CardReadResult get() = read.copy(
            activity = read.activity.takeIf { files[0].status == CardImportFileStatus.READY },
            vehicles = read.vehicles.takeIf { files[1].status == CardImportFileStatus.READY },
            controls = read.controls.takeIf { files.any { it.kind == CardImportFileKind.CONTROLS && it.status == CardImportFileStatus.READY } },
        )

        fun fingerprint(cardId: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            fun text(value: String) { val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes) }
            text("card-import-run-v1"); text(cardId); text(read.generation.name)
            files.forEach { text(it.kind.name); text(it.status.name) }
            read.activity?.let { digest.update(1.toByte()); digest.update(ByteBuffer.allocate(8)
                .putInt(it.oldestDayOffset).putInt(it.newestDayOffset).array());
                digest.update(ByteBuffer.allocate(4).putInt(it.dailyRecords.size).array()); digest.update(it.dailyRecords) }
                ?: digest.update(0.toByte())
            read.vehicles?.let { digest.update(1.toByte()); text(it.generation.name); digest.update(ByteBuffer.allocate(8)
                .putInt(it.newestRecordIndex).putInt(it.recordCount).array());
                digest.update(ByteBuffer.allocate(4).putInt(it.records.size).array()); digest.update(it.records) }
                ?: digest.update(0.toByte())
            digest.update(if (read.vehicleReadFailed) 1.toByte() else 0.toByte())
            // Preserve the exact legacy fingerprint when no control EF was attempted.
            if (read.controls != null || read.controlReadFailed) {
                read.controls?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.controlReadFailed) 1.toByte() else 0.toByte())
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

private fun CardImportFileResult.entity(runId: String) = CardImportFileResultEntity(runId, kind.name, status.name,
    decodedCount, firstUtcEpochSeconds, lastUtcEpochSeconds, failureCode)
