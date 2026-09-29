package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardVehicleImportDao
import de.fahrerabrechnung.data.db.CardVehicleImportEntity
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Clock
import javax.inject.Inject

data class SavedCardVehicles(val file: CardVehiclesFile, val uses: List<CardVehicleUse>,
                             val importedAtEpochMillis: Long, val cardNumber: String)

/** Stores a complete, validated vehicle EF in SQLCipher after explicit confirmation. */
class CardVehicleImportRepository @Inject constructor(
    private val dao: CardVehicleImportDao,
    private val clock: Clock,
) {
    suspend fun save(result: CardReadResult): Boolean = dao.insert(prepare(result)) != -1L

    internal fun prepare(result: CardReadResult, savedAt: Long = clock.millis()): CardVehicleImportEntity {
        require(result.recognition == CardRecognition.DRIVER && result.card != null && result.vehicles != null)
        val file = result.vehicles
        require(file.generation == result.generation)
        val uses = CardVehiclesParser.parse(file)
        require(uses.isNotEmpty())
        val id = MessageDigest.getInstance("SHA-256").apply {
            update(result.generation.name.toByteArray(Charsets.UTF_8))
            update(0)
            update(result.card.card.number.toByteArray(Charsets.UTF_8))
            update(0)
            update(ByteBuffer.allocate(8).putInt(file.newestRecordIndex).putInt(file.recordCount).array())
            update(file.records)
        }.digest().joinToString("") { "%02x".format(it) }
        return CardVehicleImportEntity(
            id = id,
            cardNumber = result.card.card.number,
            generation = result.generation.name,
            newestRecordIndex = file.newestRecordIndex,
            recordCount = file.recordCount,
            records = file.records.copyOf(),
            useCount = uses.size,
            importedAtEpochMillis = savedAt,
        )
    }

    suspend fun latest(): SavedCardVehicles? = dao.latest()?.let(::decode)

    suspend fun all(): List<SavedCardVehicles> = dao.all().map(::decode)

    internal fun decode(entity: CardVehicleImportEntity): SavedCardVehicles {
        val file = CardVehiclesFile(entity.newestRecordIndex, entity.recordCount,
            CardGeneration.valueOf(entity.generation), entity.records)
        val uses = CardVehiclesParser.parse(file)
        check(uses.size == entity.useCount)
        return SavedCardVehicles(file, uses, entity.importedAtEpochMillis, entity.cardNumber)
    }
}
