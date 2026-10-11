package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardVehicleUnitsDao
import de.fahrerabrechnung.data.db.CardVehicleUnitsImportEntity
import de.fahrerabrechnung.data.db.DriverImportVehicleUnitsSourceEntity
import java.nio.ByteBuffer
import java.security.MessageDigest

data class SavedCardVehicleUnits(val id: String, val driverCardId: String,
    val file: CardVehicleUnitsFile, val records: List<CardVehicleUnitRecord>,
    val firstSavedAtEpochMillis: Long)

/** Called only inside the enclosing accepted own-card source/session transaction. */
internal class CardVehicleUnitsStore(private val dao: CardVehicleUnitsDao) {
    suspend fun save(cardId: String, sessionId: String, file: CardVehicleUnitsFile, now: Long): Boolean {
        CardVehicleUnitsParser.parse(file)
        val id = fingerprint(cardId, file)
        val existing = dao.get(id)
        if (existing == null) dao.insert(CardVehicleUnitsImportEntity(id, cardId,
            file.generation.name, file.recordCount, file.bytes, now))
        else check(existing.driverCardId == cardId && existing.generation == file.generation.name &&
            existing.recordCount == file.recordCount && existing.bytes.contentEquals(file.bytes))
        dao.insertSource(DriverImportVehicleUnitsSourceEntity(sessionId, cardId, id))
        return existing == null
    }

    suspend fun load(cardId: String, sessionId: String): SavedCardVehicleUnits? {
        val source = dao.source(sessionId) ?: return null
        check(source.driverCardId == cardId)
        val row = checkNotNull(dao.get(source.sourceId))
        check(row.driverCardId == cardId)
        val file = CardVehicleUnitsFile(CardGeneration.valueOf(row.generation), row.recordCount, row.bytes)
        check(fingerprint(cardId, file) == row.id)
        return SavedCardVehicleUnits(row.id, cardId, file, CardVehicleUnitsParser.parse(file), row.importedAtEpochMillis)
    }

    companion object {
        fun fingerprint(cardId: String, file: CardVehicleUnitsFile): String {
            val digest = MessageDigest.getInstance("SHA-256")
            for (value in listOf("card-vehicle-units-file-v1", cardId, file.generation.name)) {
                val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
            }
            digest.update(ByteBuffer.allocate(8).putInt(file.recordCount).putInt(file.bytes.size).array())
            digest.update(file.bytes)
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
