package de.fahrerabrechnung.data.db

import androidx.room.*

/** Immutable receipts for one physical read. Old sessions are deliberately not backfilled. */
@Entity(tableName = "card_read_batches", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"], childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardReadBatchEntity(@PrimaryKey val id: String, val driverCardId: String,
    val requestFingerprint: String, val savedAtEpochMillis: Long, val primaryApplication: String)

@Entity(tableName = "card_application_attempts", primaryKeys = ["batchId", "application"], foreignKeys = [
    ForeignKey(entity = CardReadBatchEntity::class, parentColumns = ["id", "driverCardId"], childColumns = ["batchId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardImportRunEntity::class, parentColumns = ["id", "driverCardId"], childColumns = ["runId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId", "driverCardId"], childColumns = ["sessionId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["batchId", "driverCardId"]), Index(value = ["runId", "driverCardId"]), Index(value = ["runId"], unique = true), Index(value = ["sessionId", "driverCardId"])])
data class CardApplicationAttemptEntity(val batchId: String, val application: String, val driverCardId: String,
    val status: String, val failureCode: String?, val generation: String?, val runId: String?, val sessionId: String? = null)

@Entity(tableName = "card_master_sources", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"], childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId", "fileId"], unique = true)])
data class CardMasterSourceEntity(@PrimaryKey val id: String, val driverCardId: String, val fileId: Int,
    val bytes: ByteArray, val savedAtEpochMillis: Long)

@Entity(tableName = "card_batch_master_files", primaryKeys = ["batchId", "fileId"], foreignKeys = [
    ForeignKey(entity = CardReadBatchEntity::class, parentColumns = ["id", "driverCardId"], childColumns = ["batchId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardMasterSourceEntity::class, parentColumns = ["id", "driverCardId", "fileId"], childColumns = ["sourceId", "driverCardId", "fileId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["batchId", "driverCardId"]), Index(value = ["sourceId", "driverCardId", "fileId"])])
data class CardBatchMasterFileEntity(val batchId: String, val fileId: Int, val driverCardId: String,
    val sourceId: String?, val readFailed: Boolean)

@Dao
interface CardReadBatchDao {
    @Insert suspend fun insertBatch(value: CardReadBatchEntity)
    @Insert suspend fun insertAttempts(values: List<CardApplicationAttemptEntity>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertMaster(value: CardMasterSourceEntity): Long
    @Insert suspend fun insertMasterFiles(values: List<CardBatchMasterFileEntity>)
    @Query("SELECT * FROM card_read_batches WHERE id = :id") suspend fun batch(id: String): CardReadBatchEntity?
    @Query("SELECT b.* FROM card_read_batches b JOIN driver_cards c ON c.id = b.driverCardId WHERE c.profileId = :profileId ORDER BY b.savedAtEpochMillis DESC, b.id DESC")
    suspend fun batches(profileId: String): List<CardReadBatchEntity>
    @Query("SELECT * FROM card_application_attempts WHERE batchId = :id ORDER BY application") suspend fun attempts(id: String): List<CardApplicationAttemptEntity>
    @Query("SELECT * FROM card_application_attempts WHERE runId = :id") suspend fun attemptForRun(id: String): CardApplicationAttemptEntity?
    @Query("SELECT a.* FROM card_application_attempts a JOIN card_import_runs r ON r.id = a.runId WHERE r.sessionId = :id")
    suspend fun attemptForSession(id: String): CardApplicationAttemptEntity?
    @Query("SELECT * FROM card_batch_master_files WHERE batchId = :id ORDER BY fileId") suspend fun masterFiles(id: String): List<CardBatchMasterFileEntity>
    @Query("SELECT * FROM card_master_sources WHERE id = :id") suspend fun master(id: String): CardMasterSourceEntity?
}
