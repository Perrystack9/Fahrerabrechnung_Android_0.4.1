package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardControlDao
import de.fahrerabrechnung.data.db.CardControlImportEntity
import de.fahrerabrechnung.data.db.DriverImportControlSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardControl(val id: String, val driverCardId: String, val file: CardControlFile,
    val record: CardControlRecord?, val firstSavedAtEpochMillis: Long)

/** Used inside the enclosing source/session transaction; no independent profile assignment. */
internal class CardControlStore(private val dao: CardControlDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardControlFile, now: Long): Boolean {
        CardControlParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardControlImportEntity(id, cardId, file.generation.name, file.bytes.copyOf(), now))
        else check(existing.driverCardId == cardId && existing.generation == file.generation.name &&
            existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportControlSourceEntity(sessionId, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): SavedCardControl? {
        val source = dao.source(sessionId) ?: return null
        check(source.driverCardId == cardId)
        val row = checkNotNull(dao.get(source.controlId))
        check(row.driverCardId == cardId)
        val file = CardControlFile(CardGeneration.valueOf(row.generation), row.bytes)
        check(fingerprint(cardId, file) == row.id)
        return SavedCardControl(row.id, cardId, file, CardControlParser.parse(file), row.importedAtEpochMillis)
    }

    companion object {
        fun fingerprint(cardId: String, file: CardControlFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-control-file-v1", cardId, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(4).putInt(file.bytes.size).array()); digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
