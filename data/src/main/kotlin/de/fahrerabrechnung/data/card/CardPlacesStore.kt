package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.*
import java.nio.ByteBuffer
import java.security.MessageDigest

enum class CardPlacesFileKind { PLACES, AUTHENTICATION }
data class SavedCardPlaces(val id: String, val driverCardId: String,
    val file: CardPlacesFile, val records: List<CardPlaceRecord>,
    val firstSavedAtEpochMillis: Long)
data class SavedCardPlacesAuthentication(val id: String, val driverCardId: String,
    val file: CardPlacesAuthenticationFile, val records: List<CardPlacesAuthenticationRecord>,
    val firstSavedAtEpochMillis: Long)

/** Used inside the accepted own-card source/session transaction. No place/authentication join or inferred working period. */
internal class CardPlacesStore(private val dao: CardPlacesDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardPlacesFile, now: Long): Boolean {
        CardPlacesParser.parse(file)
        return save(cardId, sessionId, CardPlacesFileKind.PLACES, file.generation, file.recordCount, file.bytes, now)
    }
    suspend fun save(cardId: String, sessionId: String, file: CardPlacesAuthenticationFile, now: Long): Boolean {
        CardPlacesAuthenticationParser.parse(file)
        return save(cardId, sessionId, CardPlacesFileKind.AUTHENTICATION, file.generation,
            CardPlacesAuthenticationParser.RECORD_COUNT, file.bytes, now)
    }
    private suspend fun save(cardId: String, sessionId: String, kind: CardPlacesFileKind,
                             generation: CardGeneration, count: Int, bytes: ByteArray, now: Long): Boolean {
        val id = fingerprint(cardId, kind, generation, count, bytes)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardPlacesImportEntity(id, cardId, kind.name, generation.name, count, bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == kind.name &&
            existing.generation == generation.name && existing.recordCount == count && existing.bytes.contentEquals(bytes))
        dao.insertSource(DriverImportPlacesSourceEntity(sessionId, kind.name, cardId, id))
        return existing == null
    }
    private suspend fun load(cardId: String, sessionId: String, kind: CardPlacesFileKind): CardPlacesImportEntity? {
        val source = dao.source(sessionId, kind.name) ?: return null
        check(source.driverCardId == cardId && source.kind == kind.name)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId && row.kind == kind.name && row.id == source.sourceId)
        check(fingerprint(cardId, kind, CardGeneration.valueOf(row.generation), row.recordCount, row.bytes) == row.id)
        return row
    }
    suspend fun places(cardId: String, sessionId: String): SavedCardPlaces? {
        val row = load(cardId, sessionId, CardPlacesFileKind.PLACES) ?: return null
        val file = CardPlacesFile(CardGeneration.valueOf(row.generation), row.recordCount, row.bytes)
        return SavedCardPlaces(row.id, cardId, file, CardPlacesParser.parse(file), row.importedAtEpochMillis)
    }
    suspend fun authentication(cardId: String, sessionId: String): SavedCardPlacesAuthentication? {
        val row = load(cardId, sessionId, CardPlacesFileKind.AUTHENTICATION) ?: return null
        check(row.recordCount == CardPlacesAuthenticationParser.RECORD_COUNT)
        val file = CardPlacesAuthenticationFile(CardGeneration.valueOf(row.generation), row.bytes)
        return SavedCardPlacesAuthentication(row.id, cardId, file, CardPlacesAuthenticationParser.parse(file), row.importedAtEpochMillis)
    }
    companion object {
        fun fingerprint(cardId: String, file: CardPlacesFile): String =
            fingerprint(cardId, CardPlacesFileKind.PLACES, file.generation, file.recordCount, file.bytes)
        fun fingerprint(cardId: String, file: CardPlacesAuthenticationFile): String =
            fingerprint(cardId, CardPlacesFileKind.AUTHENTICATION, file.generation, CardPlacesAuthenticationParser.RECORD_COUNT, file.bytes)
        private fun fingerprint(cardId: String, kind: CardPlacesFileKind, generation: CardGeneration,
                                count: Int, bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-work-period-places-v1", cardId, kind.name, generation.name)) {
                val encoded = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(encoded.size).array()); digest.update(encoded)
            }
            digest.update(ByteBuffer.allocate(8).putInt(count).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
