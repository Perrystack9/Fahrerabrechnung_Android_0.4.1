package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

/** Append-only derived context. No card activity, original or signature row is updated here. */
@Entity(tableName = "violation_context_revisions", foreignKeys = [
    ForeignKey(entity = ProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = ViolationContextRevisionEntity::class, parentColumns = ["id", "profileId"],
        childColumns = ["previousId", "profileId"], onDelete = ForeignKey.RESTRICT),
], indices = [
    Index(value = ["profileId", "sequence"], unique = true),
    Index(value = ["id", "profileId"], unique = true),
    Index(value = ["previousId", "profileId"]),
])
data class ViolationContextRevisionEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val previousId: String?,
    val sequence: Long,
    val payloadVersion: Int,
    val payload: String,
    val payloadDigest: String,
    val semanticRevision: String,
    val recordedAtEpochMillis: Long,
)

@Dao
interface ViolationContextDao {
    @Insert suspend fun insert(value: ViolationContextRevisionEntity)

    @Query("SELECT * FROM violation_context_revisions WHERE profileId = :profileId ORDER BY sequence")
    suspend fun history(profileId: String): List<ViolationContextRevisionEntity>
}
