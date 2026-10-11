package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardCommunication(val id: String, val driverCardId: String, val file: CardCommunicationFile,
    val record: CardCommunicationRecord?, val firstSavedAtEpochMillis: Long)

/** Existing schema18, exact physical card/kind/session; never fills a historical missing source. */
internal class CardCommunicationStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardCommunicationFile, now: Long): Boolean {
        CardCommunicationParser.parse(file)
        val id = fingerprint(cardId, file)
        val row = dao.get(id)
        if (row == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.storageKind,
            file.generation.name, file.bytes, now))
        else check(row.driverCardId == cardId && row.kind == file.kind.storageKind &&
            row.generation == file.generation.name && row.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.storageKind, cardId, id))
        return row == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardCommunicationKind, SavedCardCommunication> {
        val result = mutableMapOf<CardCommunicationKind, SavedCardCommunication>()
        for (source in dao.sources(sessionId)) {
            if (source.kind == CardDirectoryStore.STORAGE_KIND || source.kind in CardMetadataKind.entries.map { it.name } ||
                source.kind in CardCertificateKind.entries.map { it.storageKind } ||
                source.kind in CardIdentitySourceKind.entries.map { it.storageKind }) continue
            val kind = CardCommunicationKind.entries.singleOrNull { it.storageKind == source.kind }
                ?: error("Unsupported saved card source kind")
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.storageKind)
            val file = CardCommunicationFile(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardCommunication(row.id, cardId, file,
                CardCommunicationParser.parse(file), row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardCommunicationFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-mf-communication-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            val bytes = file.bytes
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
