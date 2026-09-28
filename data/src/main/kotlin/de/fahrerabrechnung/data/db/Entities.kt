package de.fahrerabrechnung.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "configuration_versions",
    foreignKeys = [ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId"), Index(value = ["profileId", "validFromEpochDay", "revision"], unique = true)],
)
data class ConfigurationVersionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val validFromEpochDay: Long?,
    val revision: Int,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "driver_cards",
    foreignKeys = [ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId")],
)
data class DriverCardEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val cardIdentifier: String,
    val assignmentStatus: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "vehicles",
    foreignKeys = [ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId")],
)
data class VehicleEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val licensePlate: String?,
    val sourceIdentity: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "import_sessions",
    foreignKeys = [ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId"), Index("status")],
)
data class ImportSessionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

/** An explicitly saved, complete EF Driver_Activity_Data snapshot. No other card files are implied. */
@Entity(tableName = "card_activity_imports", indices = [Index("importedAtEpochMillis")])
data class CardActivityImportEntity(
    @PrimaryKey val id: String,
    val cardNumber: String,
    val generation: String,
    val oldestDayOffset: Int,
    val newestDayOffset: Int,
    val dailyRecords: ByteArray,
    val dayCount: Int,
    val importedAtEpochMillis: Long,
)

/** Explicitly saved EF Vehicles_Used snapshot; separate from activity and calculated trips. */
@Entity(tableName = "card_vehicle_imports", indices = [Index("importedAtEpochMillis")])
data class CardVehicleImportEntity(
    @PrimaryKey val id: String,
    val cardNumber: String,
    val generation: String,
    val newestRecordIndex: Int,
    val recordCount: Int,
    val records: ByteArray,
    val useCount: Int,
    val importedAtEpochMillis: Long,
)
