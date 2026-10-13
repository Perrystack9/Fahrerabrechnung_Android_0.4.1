package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardMetadata(val id: String, val driverCardId: String, val file: CardMetadataFile,
    val record: CardMetadataRecord?, val firstSavedAtEpochMillis: Long)

/** Each kind remains an independent source within the accepted own-card transaction. */
internal class CardMetadataStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardMetadataFile, now: Long): Boolean {
        CardMetadataParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.name,
            file.generation.name, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == file.kind.name &&
            existing.generation == file.generation.name && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.name, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardMetadataKind, SavedCardMetadata> {
        val result = mutableMapOf<CardMetadataKind, SavedCardMetadata>()
        for (source in dao.sources(sessionId)) {
            if (source.kind in CardCoreEf.entries.map { it.storageKind }) continue
            if (source.kind in CardEventFaultKind.entries.map { it.originalStorageKind }) continue
            // Certificates use their own namespace and decoder in these same schema18 tables.
            if (source.kind in CardCommunicationKind.entries.map { it.storageKind } ||
                source.kind == CardDirectoryStore.STORAGE_KIND || source.kind in CardCertificateKind.entries.map { it.storageKind } ||
                source.kind in CardIdentitySourceKind.entries.map { it.storageKind }) continue
            val kind = CardMetadataKind.valueOf(source.kind)
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.name)
            val file = CardMetadataFile(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardMetadata(row.id, cardId, file,
                CardMetadataParser.parse(file), row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardMetadataFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-fixed-metadata-file-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(4).putInt(file.bytes.size).array()); digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
