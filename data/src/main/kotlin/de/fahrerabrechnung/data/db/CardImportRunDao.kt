package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/** Only accepted own cards enter this log. No raw bytes or arbitrary exception messages. */
@Entity(tableName = "card_import_runs", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"], childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = ImportSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["sessionId"], unique = true), Index("startedAtEpochMillis")])
data class CardImportRunEntity(
    @androidx.room.PrimaryKey val id: String,
    val driverCardId: String,
    val requestFingerprint: String,
    val status: String,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val sessionId: String?,
    val newSourceCount: Int,
    val failureCode: String?,
)

@Entity(tableName = "card_import_file_results", primaryKeys = ["runId", "kind"], foreignKeys = [
    ForeignKey(entity = CardImportRunEntity::class, parentColumns = ["id"], childColumns = ["runId"], onDelete = ForeignKey.RESTRICT),
])
data class CardImportFileResultEntity(
    val runId: String,
    val kind: String,
    val status: String,
    val decodedCount: Int,
    val firstUtcEpochSeconds: Long?,
    val lastUtcEpochSeconds: Long?,
    val failureCode: String?,
)

/** Metadata-only overview: a damaged raw source must not hide the whole import history. */
data class EarlierDriverImportRow(
    val id: String,
    val driverCardId: String,
    val cardNumber: String,
    val generation: String,
    val savedAtEpochMillis: Long,
    val newSourceCount: Int,
    val dayCount: Int?,
    val useCount: Int?,
)

@Dao
interface CardImportRunDao {
    @Insert suspend fun insertRun(run: CardImportRunEntity)
    @Insert suspend fun insertFiles(files: List<CardImportFileResultEntity>)
    @Update suspend fun updateRun(run: CardImportRunEntity)
    @Update suspend fun updateFiles(files: List<CardImportFileResultEntity>)

    @Query("SELECT * FROM card_import_runs WHERE id = :id")
    suspend fun run(id: String): CardImportRunEntity?

    @Query("SELECT * FROM card_import_file_results WHERE runId = :id ORDER BY kind")
    suspend fun files(id: String): List<CardImportFileResultEntity>

    @Query("SELECT r.* FROM card_import_runs r JOIN driver_cards c ON c.id = r.driverCardId WHERE c.profileId = :profileId ORDER BY r.startedAtEpochMillis DESC, r.id DESC")
    suspend fun runs(profileId: String): List<CardImportRunEntity>

    @Query("SELECT * FROM card_import_runs WHERE status = 'IN_PROGRESS'")
    suspend fun unfinished(): List<CardImportRunEntity>

    @Query("""SELECT s.id, d.driverCardId, i.cardNumber, i.generation,
        s.createdAtEpochMillis AS savedAtEpochMillis, d.newSourceCount,
        a.dayCount, v.useCount FROM driver_import_sources d
        JOIN import_sessions s ON s.id = d.sessionId
        JOIN driver_cards c ON c.id = d.driverCardId
        JOIN driver_card_identities i ON i.cardId = c.id
        LEFT JOIN card_activity_imports a ON a.id = d.activityId
        LEFT JOIN card_vehicle_imports v ON v.id = d.vehicleId
        LEFT JOIN card_import_runs r ON r.sessionId = s.id
        WHERE s.profileId = :profileId AND c.profileId = :profileId AND r.id IS NULL
        ORDER BY s.createdAtEpochMillis DESC, s.id DESC""")
    suspend fun earlierImports(profileId: String): List<EarlierDriverImportRow>
}
