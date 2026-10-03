package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardSpecificConditionDao
import de.fahrerabrechnung.data.db.CardSpecificConditionImportEntity
import de.fahrerabrechnung.data.db.DriverImportSpecificConditionSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardSpecificConditions(val id: String, val driverCardId: String,
    val file: CardSpecificConditionFile, val records: List<CardSpecificConditionRecord>,
    val firstSavedAtEpochMillis: Long)

/** Called only inside the enclosing accepted own-card source/session transaction. */
internal class CardSpecificConditionStore(private val dao: CardSpecificConditionDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardSpecificConditionFile, now: Long): Boolean {
        CardSpecificConditionParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardSpecificConditionImportEntity(id, cardId,
            file.generation.name, file.recordCount, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.generation == file.generation.name &&
            existing.recordCount == file.recordCount && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportSpecificConditionSourceEntity(sessionId, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): SavedCardSpecificConditions? {
        val source = dao.source(sessionId) ?: return null
        check(source.driverCardId == cardId)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId)
        val file = CardSpecificConditionFile(CardGeneration.valueOf(row.generation), row.recordCount, row.bytes)
        check(fingerprint(cardId, file) == row.id)
        return SavedCardSpecificConditions(row.id, cardId, file, CardSpecificConditionParser.parse(file), row.importedAtEpochMillis)
    }

    companion object {
        fun fingerprint(cardId: String, file: CardSpecificConditionFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-specific-condition-file-v1", cardId, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(8).putInt(file.recordCount).putInt(file.bytes.size).array())
            digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
