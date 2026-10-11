package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardOriginalFile(val id: String, val driverCardId: String, val file: CardOriginalFile,
    val record: CardOriginalFileRecord, val firstSavedAtEpochMillis: Long)

/** Independent kinds use schema18's existing exact card/session foreign keys. */
internal class CardOriginalFileStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardOriginalFile, now: Long): Boolean {
        CardOriginalFileParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.storageKind,
            file.generation.name, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == file.kind.storageKind &&
            existing.generation == file.generation.name && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.storageKind, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardOriginalFileKind, SavedCardOriginalFile> {
        val result = mutableMapOf<CardOriginalFileKind, SavedCardOriginalFile>()
        for (source in dao.sources(sessionId)) {
            if (source.kind in CardMetadataKind.entries.map { it.name } ||
                source.kind in CardCertificateKind.entries.map { it.storageKind }) continue
            val kind = CardOriginalFileKind.entries.singleOrNull { it.storageKind == source.kind }
                ?: error("Unsupported saved card source kind")
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.storageKind)
            val file = CardOriginalFile(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardOriginalFile(row.id, cardId, file,
                CardOriginalFileParser.parse(file), row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardOriginalFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-original-identification-file-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(4).putInt(file.bytes.size).array()); digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
