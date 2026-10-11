package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao
import de.fahrerabrechnung.data.db.CardMetadataImportEntity
import de.fahrerabrechnung.data.db.DriverImportMetadataSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardIdentitySource(val id: String, val driverCardId: String, val file: CardIdentitySourceFile,
    val record: CardIdentitySourceRecord?, val firstSavedAtEpochMillis: Long)

/** Same schema18 combined card/kind FK and exact session source; no historical reconstruction. */
internal class CardIdentitySourceStore(private val dao: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardIdentitySourceFile, now: Long): Boolean {
        CardIdentitySourceParser.parse(file)
        val id = fingerprint(cardId, file)
        val row = dao.get(id)
        if (row == null) dao.insert(CardMetadataImportEntity(id, cardId, file.kind.storageKind,
            file.generation.name, file.bytes, now))
        else check(row.driverCardId == cardId && row.kind == file.kind.storageKind &&
            row.generation == file.generation.name && row.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportMetadataSourceEntity(sessionId, file.kind.storageKind, cardId, id))
        return row == null
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardIdentitySourceKind, SavedCardIdentitySource> {
        val result = mutableMapOf<CardIdentitySourceKind, SavedCardIdentitySource>()
        for (source in dao.sources(sessionId)) {
            if (source.kind in CardMetadataKind.entries.map { it.name } ||
                source.kind in CardCertificateKind.entries.map { it.storageKind }) continue
            val kind = CardIdentitySourceKind.entries.singleOrNull { it.storageKind == source.kind }
                ?: error("Unsupported saved card source kind")
            check(source.driverCardId == cardId)
            val row = checkNotNull(dao.get(source.sourceId))
            check(row.driverCardId == cardId && row.kind == kind.storageKind)
            val file = CardIdentitySourceFile(kind, CardGeneration.valueOf(row.generation), row.bytes)
            check(fingerprint(cardId, file) == row.id)
            check(result.put(kind, SavedCardIdentitySource(row.id, cardId, file,
                CardIdentitySourceParser.parse(file), row.importedAtEpochMillis)) == null)
        }
        return result.toMap()
    }

    companion object {
        fun fingerprint(cardId: String, file: CardIdentitySourceFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-original-identification-v1", cardId, file.kind.name, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            val bytes = file.bytes
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
