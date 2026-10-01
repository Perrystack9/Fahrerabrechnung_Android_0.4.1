package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.CardActivityImportEntity
import de.fahrerabrechnung.data.db.DriverActivityChangeEntity
import de.fahrerabrechnung.data.db.DriverActivityDayEntity
import de.fahrerabrechnung.data.db.DriverActivityDaySourceEntity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.time.Instant

/** Content identity excludes the changing ring position, but includes the physical card. */
internal object CardActivityDayFingerprint {
    fun id(cardId: String, day: CardActivityDay): String {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeUTF("driver-activity-day-v1")
            output.writeUTF(cardId)
            output.writeLong(day.recordDateUtc.epochSecond)
            output.writeUTF(day.dailyPresenceCounter)
            output.writeInt(day.distanceKm)
            output.writeInt(day.changes.size)
            day.changes.forEach { change ->
                output.writeInt(change.minuteOfDay)
                output.writeUTF(change.activity.name)
                output.writeUTF(change.slot.name)
                output.writeBoolean(change.crewDriving)
                output.writeBoolean(change.cardInserted)
            }
        }
        return MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}

/** Called inside the same transaction as the explicit, profile-bound source import. */
internal class NormalizedCardActivityStore(database: AppDatabase) {
    private val dao = database.driverActivityDao()

    suspend fun save(cardId: String, source: CardActivityImportEntity, days: List<CardActivityDay>) {
        check(days.isNotEmpty() && days.size == source.dayCount)
        days.forEachIndexed { recordIndex, day ->
            val id = CardActivityDayFingerprint.id(cardId, day)
            val added = dao.insertDay(DriverActivityDayEntity(id, cardId, day.recordDateUtc.epochSecond,
                day.dailyPresenceCounter, day.distanceKm, day.changes.size)) != -1L
            if (added) dao.insertChanges(day.changes.mapIndexed { index, change ->
                DriverActivityChangeEntity(id, index, change.minuteOfDay, change.activity.name,
                    change.slot.name, change.crewDriving, change.cardInserted)
            })
            dao.insertSource(DriverActivityDaySourceEntity(cardId, source.id, recordIndex, id, day.ringOffset))
        }
        // Existing immutable rows must describe precisely the same source, including retries.
        check(load(cardId, source) == days)
    }

    /** Null means a legacy source without normalization. Reading never backfills or assigns it. */
    suspend fun load(cardId: String, source: CardActivityImportEntity): List<CardActivityDay>? {
        val rows = dao.sourceDays(cardId, source.id)
        if (rows.isEmpty()) return null
        check(rows.size == source.dayCount)
        val changes = dao.sourceChanges(cardId, source.id).groupBy { it.dayId }
        return rows.mapIndexed { index, row ->
            check(row.recordIndex == index && row.ringOffset in source.dailyRecords.indices)
            check(row.day.driverCardId == cardId)
            val entries = changes[row.day.id].orEmpty()
            check(entries.isNotEmpty() && entries.size == row.day.changeCount)
            val day = CardActivityDay(row.ringOffset, Instant.ofEpochSecond(row.day.recordDateUtcEpochSeconds),
                row.day.dailyPresenceCounter, row.day.distanceKm, entries.mapIndexed { changeIndex, change ->
                    check(change.changeIndex == changeIndex)
                    ActivityChange(change.minuteOfDay, DriverActivity.valueOf(change.activity),
                        CardSlot.valueOf(change.slot), change.crewDriving, change.cardInserted)
                })
            check(CardActivityDayFingerprint.id(cardId, day) == row.day.id)
            day
        }
    }
}
