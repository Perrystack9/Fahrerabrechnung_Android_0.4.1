package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardCoreSnapshot(val id: String, val driverCardId: String,
    val snapshot: CardCoreSnapshot, val firstSavedAtEpochMillis: Long)

/** Existing encrypted schema19 has card/kind/session FKs. Raw-only sources need no parser. */
internal class CardCoreSnapshotStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardCoreSnapshot, now: Long): Boolean {
        val id = fingerprint(cardId, file)
        val row = dao.get(id)
        if (row == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.storageKind,
            file.generation.name, file.bytes, now))
        else check(row.driverCardId == cardId && row.kind == file.kind.storageKind &&
            row.generation == file.generation.name && row.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.storageKind, cardId, id))
        return row == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardCoreEf, SavedCardCoreSnapshot> {
        val result = mutableMapOf<CardCoreEf, SavedCardCoreSnapshot>()
        for (source in dao.sources(sessionId)) {
            val kind = CardCoreEf.entries.singleOrNull { it.storageKind == source.kind } ?: continue
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.storageKind)
            val file = CardCoreSnapshot(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardCoreSnapshot(row.id, cardId, file, row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardCoreSnapshot): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-core-original-v1", cardId, file.kind.storageKind, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            val bytes = file.bytes
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
