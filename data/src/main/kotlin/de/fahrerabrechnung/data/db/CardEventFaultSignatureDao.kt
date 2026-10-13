package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_event_fault_signature_receipts", primaryKeys = ["sessionId", "kind"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId", "driverCardId"],
        childColumns = ["sessionId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = DriverImportMetadataSourceEntity::class,
        parentColumns = ["sessionId", "driverCardId", "kind", "sourceId"],
        childColumns = ["sessionId", "driverCardId", "kind", "originalSourceId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = DriverImportMetadataSourceEntity::class,
        parentColumns = ["sessionId", "driverCardId", "kind", "sourceId"],
        childColumns = ["sessionId", "driverCardId", "certificateKind", "certificateSourceId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sessionId", "driverCardId"]),
    Index(value = ["sessionId", "driverCardId", "kind", "originalSourceId"]),
    Index(value = ["sessionId", "driverCardId", "certificateKind", "certificateSourceId"])])
data class CardEventFaultSignatureEntity(
    val sessionId: String, val driverCardId: String, val kind: String,
    val generation: String, val application: String, val originalSourceId: String?,
    val certificateKind: String?, val certificateSourceId: String?,
    val status: String, val policy: String?, val phase: String?, val reason: String?,
    val statusWord: Int?, val hashStatusWord: Int?, val signStatusWord: Int?,
    val failure: String?, val actualLength: Int?, val bytes: ByteArray?, val fingerprint: String,
)

@Dao
interface CardEventFaultSignatureDao {
    @Query("SELECT * FROM card_event_fault_signature_receipts WHERE sessionId = :sessionId")
    suspend fun receipts(sessionId: String): List<CardEventFaultSignatureEntity>
    @Insert suspend fun insert(entity: CardEventFaultSignatureEntity)
}
