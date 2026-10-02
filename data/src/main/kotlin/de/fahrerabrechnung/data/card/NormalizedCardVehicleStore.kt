package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.CardVehicleImportEntity
import de.fahrerabrechnung.data.db.DriverVehicleUseEntity
import de.fahrerabrechnung.data.db.DriverVehicleUseSourceEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.time.Instant

/** Identity includes every decoded field and the physical card, but excludes the moving ring slot. */
internal object CardVehicleUseFingerprint {
    fun id(cardId: String, use: CardVehicleUse): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeUTF("driver-vehicle-use-v1")
            output.writeUTF(cardId)
            output.writeLong(use.firstUseUtc.epochSecond)
            output.writeBoolean(use.lastUseUtc != null)
            use.lastUseUtc?.let { output.writeLong(it.epochSecond) }
            output.writeInt(use.registrationNation)
            output.writeUTF(use.registrationNumber)
            output.writeBoolean(use.vin != null)
            use.vin?.let(output::writeUTF)
            output.writeInt(use.odometerBeginKm)
            output.writeBoolean(use.odometerEndKm != null)
            use.odometerEndKm?.let(output::writeInt)
            output.writeInt(use.vuDataBlockCounter)
        }
        return MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}

/** Invoked only inside the transaction for a fresh, explicitly saved own-card read. */
internal class NormalizedCardVehicleStore(database: AppDatabase) {
    private val dao = database.driverVehicleUseDao()

    suspend fun save(cardId: String, source: CardVehicleImportEntity, uses: List<CardVehicleUse>) {
        check(uses.isNotEmpty() && uses.size == source.useCount)
        uses.forEachIndexed { recordIndex, use ->
            val id = CardVehicleUseFingerprint.id(cardId, use)
            dao.insertUse(DriverVehicleUseEntity(id, cardId, use.firstUseUtc.epochSecond,
                use.lastUseUtc?.epochSecond, use.registrationNation, use.registrationNumber,
                use.vin, use.odometerBeginKm, use.odometerEndKm, use.vuDataBlockCounter))
            dao.insertSource(DriverVehicleUseSourceEntity(cardId, source.id, recordIndex, id, use.ringIndex))
        }
        // Existing immutable sources, including retries, must still describe the exact same file.
        check(load(cardId, source) == uses)
    }

    /** A legacy source stays a read-only parser fallback; no backfill or guessed card binding. */
    suspend fun load(cardId: String, source: CardVehicleImportEntity): List<CardVehicleUse>? {
        val rows = dao.sourceUses(cardId, source.id)
        if (rows.isEmpty()) return null
        check(rows.size == source.useCount)
        check(rows.map { it.ringIndex }.distinct().size == rows.size)
        return rows.mapIndexed { index, row ->
            check(row.recordIndex == index && row.ringIndex in 0 until source.recordCount)
            check(row.use.driverCardId == cardId)
            val stored = row.use
            val use = CardVehicleUse(row.ringIndex, Instant.ofEpochSecond(stored.firstUseUtcEpochSeconds),
                stored.lastUseUtcEpochSeconds?.let(Instant::ofEpochSecond), stored.registrationNation,
                stored.registrationNumber, stored.vin, stored.odometerBeginKm, stored.odometerEndKm,
                stored.vuDataBlockCounter)
            check(CardVehicleUseFingerprint.id(cardId, use) == stored.id)
            use
        }
    }
}
