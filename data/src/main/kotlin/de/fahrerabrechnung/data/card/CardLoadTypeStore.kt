package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardLoadTypeDao
import de.fahrerabrechnung.data.db.CardLoadTypeImportEntity
import de.fahrerabrechnung.data.db.DriverImportLoadTypeSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardLoadType(val id: String, val driverCardId: String,
    val file: CardLoadTypeFile, val records: List<CardLoadTypeRecord>,
    val firstSavedAtEpochMillis: Long)

/** Called only inside the enclosing accepted own-card source/session transaction. */
internal class CardLoadTypeStore(private val dao: CardLoadTypeDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardLoadTypeFile, now: Long): Boolean {
        CardLoadTypeParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardLoadTypeImportEntity(id, cardId,
            file.generation.name, file.recordCount, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.generation == file.generation.name &&
            existing.recordCount == file.recordCount && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportLoadTypeSourceEntity(sessionId, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): SavedCardLoadType? {
        val source = dao.source(sessionId) ?: return null
        check(source.driverCardId == cardId)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId)
        val file = CardLoadTypeFile(CardGeneration.valueOf(row.generation), row.recordCount, row.bytes)
        check(fingerprint(cardId, file) == row.id)
        return SavedCardLoadType(row.id, cardId, file, CardLoadTypeParser.parse(file), row.importedAtEpochMillis)
    }

    companion object {
        fun fingerprint(cardId: String, file: CardLoadTypeFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-load-type-file-v1", cardId, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(8).putInt(file.recordCount).putInt(file.bytes.size).array())
            digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
