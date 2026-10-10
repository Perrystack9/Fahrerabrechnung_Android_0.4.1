package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.*
import java.nio.ByteBuffer
import java.security.MessageDigest

enum class CardGnssFileKind { POSITIONS, AUTHENTICATION }
data class SavedCardGnssPositions(val id: String, val driverCardId: String,
    val file: CardGnssAccumulatedDrivingFile, val records: List<CardGnssAccumulatedDrivingRecord>,
    val firstSavedAtEpochMillis: Long)
data class SavedCardGnssAuthentication(val id: String, val driverCardId: String,
    val file: CardGnssAuthenticationFile, val records: List<CardGnssAuthenticationRecord>,
    val firstSavedAtEpochMillis: Long)

/** Used inside the accepted own-card source/session transaction. No cross-file position join. */
internal class CardGnssStore(private val dao: CardGnssDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardGnssAccumulatedDrivingFile, now: Long): Boolean {
        CardGnssAccumulatedDrivingParser.parse(file)
        return save(cardId, sessionId, CardGnssFileKind.POSITIONS, file.generation, file.recordCount, file.bytes, now)
    }
    suspend fun save(cardId: String, sessionId: String, file: CardGnssAuthenticationFile, now: Long): Boolean {
        CardGnssAuthenticationParser.parse(file)
        return save(cardId, sessionId, CardGnssFileKind.AUTHENTICATION, file.generation,
            CardGnssAuthenticationParser.RECORD_COUNT, file.bytes, now)
    }
    private suspend fun save(cardId: String, sessionId: String, kind: CardGnssFileKind,
                             generation: CardGeneration, count: Int, bytes: ByteArray, now: Long): Boolean {
        val id = fingerprint(cardId, kind, generation, count, bytes)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardGnssImportEntity(id, cardId, kind.name, generation.name, count, bytes, now))
        else check(existing.driverCardId == cardId && existing.kind == kind.name &&
            existing.generation == generation.name && existing.recordCount == count && existing.bytes.contentEquals(bytes))
        dao.insertSource(DriverImportGnssSourceEntity(sessionId, kind.name, cardId, id))
        return existing == null
    }
    private suspend fun load(cardId: String, sessionId: String, kind: CardGnssFileKind): CardGnssImportEntity? {
        val source = dao.source(sessionId, kind.name) ?: return null
        check(source.driverCardId == cardId && source.kind == kind.name)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId && row.kind == kind.name && row.id == source.sourceId)
        check(fingerprint(cardId, kind, CardGeneration.valueOf(row.generation), row.recordCount, row.bytes) == row.id)
        return row
    }
    suspend fun positions(cardId: String, sessionId: String): SavedCardGnssPositions? {
        val row = load(cardId, sessionId, CardGnssFileKind.POSITIONS) ?: return null
        val file = CardGnssAccumulatedDrivingFile(CardGeneration.valueOf(row.generation), row.recordCount, row.bytes)
        return SavedCardGnssPositions(row.id, cardId, file, CardGnssAccumulatedDrivingParser.parse(file), row.importedAtEpochMillis)
    }
    suspend fun authentication(cardId: String, sessionId: String): SavedCardGnssAuthentication? {
        val row = load(cardId, sessionId, CardGnssFileKind.AUTHENTICATION) ?: return null
        check(row.recordCount == CardGnssAuthenticationParser.RECORD_COUNT)
        val file = CardGnssAuthenticationFile(CardGeneration.valueOf(row.generation), row.bytes)
        return SavedCardGnssAuthentication(row.id, cardId, file, CardGnssAuthenticationParser.parse(file), row.importedAtEpochMillis)
    }
    companion object {
        fun fingerprint(cardId: String, file: CardGnssAccumulatedDrivingFile): String =
            fingerprint(cardId, CardGnssFileKind.POSITIONS, file.generation, file.recordCount, file.bytes)
        fun fingerprint(cardId: String, file: CardGnssAuthenticationFile): String =
            fingerprint(cardId, CardGnssFileKind.AUTHENTICATION, file.generation, CardGnssAuthenticationParser.RECORD_COUNT, file.bytes)
        private fun fingerprint(cardId: String, kind: CardGnssFileKind, generation: CardGeneration,
                                count: Int, bytes: ByteArray): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-gnss-file-v1", cardId, kind.name, generation.name)) {
                val encoded = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(encoded.size).array()); digest.update(encoded)
            }
            digest.update(ByteBuffer.allocate(8).putInt(count).putInt(bytes.size).array()); digest.update(bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
