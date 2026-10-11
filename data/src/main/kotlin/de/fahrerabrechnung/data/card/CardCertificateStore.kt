package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardCertificate(val id: String, val driverCardId: String, val file: CardCertificateFile,
    val record: CardCertificateRecord?, val firstSavedAtEpochMillis: Long)

/** Existing schema18 has exact card/kind/session foreign keys; no migration or historical backfill. */
internal class CardCertificateStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardCertificateFile, now: Long): Boolean {
        CardCertificateParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.storageKind,
            file.generation.name, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == file.kind.storageKind &&
            existing.generation == file.generation.name && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.storageKind, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardCertificateKind, SavedCardCertificate> {
        val result = mutableMapOf<CardCertificateKind, SavedCardCertificate>()
        for (source in dao.sources(sessionId)) {
            if (source.kind in CardMetadataKind.entries.map { it.name } ||
                source.kind in CardIdentitySourceKind.entries.map { it.storageKind }) continue
            val kind = CardCertificateKind.entries.singleOrNull { it.storageKind == source.kind }
                ?: error("Unsupported saved card source kind")
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.storageKind)
            val file = CardCertificateFile(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardCertificate(row.id, cardId, file,
                CardCertificateParser.parse(file), row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardCertificateFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-certificate-file-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(4).putInt(file.bytes.size).array()); digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
