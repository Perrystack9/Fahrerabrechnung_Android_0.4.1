package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "local_driver_profile", foreignKeys = [
    ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["profileId"], unique = true)])
data class LocalDriverProfileEntity(@PrimaryKey val id: Int = 1, val profileId: String) {
    init { require(id == 1) }
}

/** Immutable identification of each separately assigned physical card. SQLCipher protects the fields. */
@Entity(tableName = "driver_card_identities", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"], childColumns = ["cardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["cardKey"], unique = true)])
data class DriverCardIdentityEntity(
    @PrimaryKey val cardId: String,
    val cardKey: String,
    val cardNumber: String,
    val generation: String,
    val issuingState: Int,
    val issuedEpochSeconds: Long,
    val validFromEpochSeconds: Long,
    val expiresEpochSeconds: Long,
    val surname: String,
    val givenNames: String,
    val birthDateIso: String?,
)

/** Provenance for one explicitly saved partial import, not inferred for older snapshots. */
@Entity(tableName = "driver_import_sources", foreignKeys = [
    ForeignKey(entity = ImportSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"], childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardActivityImportEntity::class, parentColumns = ["id"], childColumns = ["activityId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardVehicleImportEntity::class, parentColumns = ["id"], childColumns = ["vehicleId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardSourcePairEntity::class, parentColumns = ["id"], childColumns = ["pairId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index("activityId"), Index("vehicleId"), Index("pairId"), Index(value = ["sessionId", "driverCardId"], unique = true)])
data class DriverImportSourcesEntity(
    @PrimaryKey val sessionId: String,
    val driverCardId: String,
    val activityId: String?,
    val vehicleId: String?,
    val pairId: String?,
    val newSourceCount: Int,
)

@Dao
interface DriverProfileDao {
    @Query("SELECT p.* FROM profiles p JOIN local_driver_profile l ON l.profileId = p.id WHERE l.id = 1")
    suspend fun localProfile(): ProfileEntity?

    @Query("SELECT i.* FROM driver_card_identities i JOIN driver_cards c ON c.id = i.cardId WHERE c.profileId = :profileId ORDER BY c.createdAtEpochMillis, c.id")
    suspend fun cards(profileId: String): List<DriverCardIdentityEntity>

    @Query("SELECT * FROM driver_card_identities WHERE cardKey = :key")
    suspend fun identityByKey(key: String): DriverCardIdentityEntity?

    @Query("SELECT * FROM driver_cards WHERE id = :id")
    suspend fun card(id: String): DriverCardEntity?

    @Insert suspend fun insertLocal(entity: LocalDriverProfileEntity)
    @Insert suspend fun insertCard(entity: DriverCardEntity)
    @Insert suspend fun insertIdentity(entity: DriverCardIdentityEntity)
    @Insert suspend fun insertSession(entity: ImportSessionEntity)
    @Insert suspend fun insertSources(entity: DriverImportSourcesEntity)

    @Query("SELECT * FROM import_sessions WHERE id = :id AND profileId = :profileId")
    suspend fun session(id: String, profileId: String): ImportSessionEntity?

    @Query("SELECT d.* FROM driver_import_sources d JOIN import_sessions s ON s.id = d.sessionId JOIN driver_cards c ON c.id = d.driverCardId WHERE s.profileId = :profileId AND c.profileId = :profileId ORDER BY s.createdAtEpochMillis DESC, s.id DESC")
    suspend fun imports(profileId: String): List<DriverImportSourcesEntity>

    @Query("SELECT * FROM driver_import_sources WHERE sessionId = :id")
    suspend fun sources(id: String): DriverImportSourcesEntity?
}
