package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.*
import java.nio.ByteBuffer
import java.security.MessageDigest

internal class CardEventFaultOriginalStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardEventFaultFile, now: Long): Boolean {
        require(file.bytes.size == CardEventFaultParser.fileLength(file.kind, file.generation, file.recordsPerType))
        val id = fingerprint(cardId, file)
        val row = dao.get(id)
        if (row == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.originalStorageKind,
            file.generation.name, file.bytes, now))
        else check(row.driverCardId == cardId && row.kind == file.kind.originalStorageKind &&
            row.generation == file.generation.name && row.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.originalStorageKind, cardId, id))
        return row == null
    }
    companion object {
        fun decode(kind: CardEventFaultKind, generation: CardGeneration, bytes: ByteArray): CardEventFaultFile {
            val slotGroupSize = CardEventFaultParser.groupCount(kind, generation) * CardEventFaultParser.RECORD_LENGTH
            require(bytes.size % slotGroupSize == 0)
            val count = bytes.size / slotGroupSize
            require(bytes.size == CardEventFaultParser.fileLength(kind, generation, count))
            return CardEventFaultFile(kind, generation, count, bytes)
        }
        fun fingerprint(cardId: String, file: CardEventFaultFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-event-fault-original-v1", cardId, file.kind.originalStorageKind, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            val raw = file.bytes
            digest.update(ByteBuffer.allocate(8).putInt(file.recordsPerType).putInt(raw.size).array()); digest.update(raw)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
