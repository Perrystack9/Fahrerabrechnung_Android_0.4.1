package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardEventFaultDao
import de.fahrerabrechnung.data.db.CardEventFaultImportEntity
import de.fahrerabrechnung.data.db.DriverImportEventFaultSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardEventFault(val id: String, val driverCardId: String, val file: CardEventFaultFile,
    val records: List<CardEventFaultRecord>, val firstSavedAtEpochMillis: Long)

/** Always inside the enclosing accepted-card source/session transaction. */
internal class CardEventFaultStore(private val dao: CardEventFaultDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardEventFaultFile, now: Long): Boolean {
        CardEventFaultParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardEventFaultImportEntity(id, cardId, file.kind.name,
            file.generation.name, file.recordsPerType, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == file.kind.name &&
            existing.generation == file.generation.name && existing.recordsPerType == file.recordsPerType &&
            existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportEventFaultSourceEntity(sessionId, file.kind.name, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String, kind: CardEventFaultKind): SavedCardEventFault? {
        val source = dao.source(sessionId, kind.name) ?: return null
        check(source.driverCardId == cardId && source.kind == kind.name)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId && row.kind == kind.name)
        val file = CardEventFaultFile(kind, CardGeneration.valueOf(row.generation), row.recordsPerType, row.bytes)
        check(fingerprint(cardId, file) == row.id)
        return SavedCardEventFault(row.id, cardId, file, CardEventFaultParser.parse(file), row.importedAtEpochMillis)
    }

    companion object {
        fun fingerprint(cardId: String, file: CardEventFaultFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-event-fault-file-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(8).putInt(file.recordsPerType).putInt(file.bytes.size).array())
            digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
