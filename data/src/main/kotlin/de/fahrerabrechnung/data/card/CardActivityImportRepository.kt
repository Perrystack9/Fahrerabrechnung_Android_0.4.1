package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardActivityImportDao
import de.fahrerabrechnung.data.db.CardActivityImportEntity
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Clock
import javax.inject.Inject

data class SavedCardActivity(val file: CardActivityFile, val generation: CardGeneration,
                             val dayCount: Int, val importedAtEpochMillis: Long,
                             val cardNumber: String,
                             val days: List<CardActivityDay> = CardActivityParser.parse(file))

/** Stores only the explicitly confirmed activity snapshot in the encrypted Room database. */
class CardActivityImportRepository @Inject constructor(
    private val dao: CardActivityImportDao,
    private val clock: Clock,
) {
    suspend fun save(result: CardReadResult): Boolean = dao.insert(prepare(result)) != -1L

    internal fun prepare(result: CardReadResult, savedAt: Long = clock.millis()): CardActivityImportEntity {
        require(result.recognition == CardRecognition.DRIVER && result.card != null && result.activity != null)
        val file = result.activity
        val days = CardActivityParser.parse(file)
        require(days.isNotEmpty())
        val id = MessageDigest.getInstance("SHA-256").apply {
            update(result.generation.name.toByteArray(Charsets.UTF_8))
            update(0)
            update(result.card.card.number.toByteArray(Charsets.UTF_8))
            update(0)
            update(ByteBuffer.allocate(8).putInt(file.oldestDayOffset).putInt(file.newestDayOffset).array())
            update(file.dailyRecords)
        }.digest().joinToString("") { "%02x".format(it) }
        return CardActivityImportEntity(
            id = id,
            cardNumber = result.card.card.number,
            generation = result.generation.name,
            oldestDayOffset = file.oldestDayOffset,
            newestDayOffset = file.newestDayOffset,
            dailyRecords = file.dailyRecords.copyOf(),
            dayCount = days.size,
            importedAtEpochMillis = savedAt,
        )
    }

    suspend fun latest(): SavedCardActivity? = dao.latest()?.let { decode(it) }

    suspend fun all(): List<SavedCardActivity> = dao.all().map { decode(it) }

    internal fun decode(entity: CardActivityImportEntity, normalizedDays: List<CardActivityDay>? = null): SavedCardActivity {
        val generation = CardGeneration.valueOf(entity.generation)
        val file = CardActivityFile(entity.oldestDayOffset, entity.newestDayOffset, entity.dailyRecords)
        val days = normalizedDays ?: CardActivityParser.parse(file)
        check(days.size == entity.dayCount)
        return SavedCardActivity(file, generation, days.size, entity.importedAtEpochMillis,
            entity.cardNumber, days)
    }
}
