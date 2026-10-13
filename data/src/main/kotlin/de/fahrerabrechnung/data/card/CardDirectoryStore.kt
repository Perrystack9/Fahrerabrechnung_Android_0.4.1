package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardDirectory(val id: String, val driverCardId: String, val file: CardDirectoryFile,
    val record: CardDirectoryRecord?, val firstSavedAtEpochMillis: Long)

/** Schema18 combined card/kind FK; exactly the selected session, with no historical fallback. */
internal class CardDirectoryStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardDirectoryFile, now: Long): Boolean {
        CardDirectoryParser.parse(file)
        val id = fingerprint(cardId, file)
        val row = dao.get(id)
        if (row == null) dao.insert(CardMetadataImportEntity(id, cardId, STORAGE_KIND,
            file.generation.name, file.bytes, now))
        else check(row.driverCardId == cardId && row.kind == STORAGE_KIND &&
            row.generation == file.generation.name && row.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, STORAGE_KIND, cardId, id))
        return row == null
    }

    suspend fun load(cardId: String, sessionId: String): SavedCardDirectory? {
        var result: SavedCardDirectory? = null
        for (source in dao.sources(sessionId)) {
            if (source.kind in CardCoreEf.entries.map { it.storageKind }) continue
            if (source.kind in CardCommunicationKind.entries.map { it.storageKind } ||
                source.kind in CardMetadataKind.entries.map { it.name } ||
                source.kind in CardCertificateKind.entries.map { it.storageKind } ||
                source.kind in CardIdentitySourceKind.entries.map { it.storageKind }) continue
            check(source.kind == STORAGE_KIND && source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == STORAGE_KIND)
            val file = CardDirectoryFile(CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id && result == null)
            result = SavedCardDirectory(row.id, cardId, file, CardDirectoryParser.parse(file), row.importedAtEpochMillis)
        }
        return result
    }

    companion object {
        const val STORAGE_KIND = "MASTER_DIRECTORY"
        fun fingerprint(cardId: String, file: CardDirectoryFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-mf-directory-v1", cardId, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            val bytes = file.bytes
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
