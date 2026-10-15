package de.fahrerabrechnung.data.violation

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.ViolationContextRevisionEntity
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Clock
import javax.inject.Inject

class ContextRevisionConflict : IllegalStateException("Context revision changed")

/** Full replacements of the separate context, with optimistic concurrency and immutable history. */
class ViolationContextRepository @Inject constructor(private val db: AppDatabase, private val clock: Clock) {
    suspend fun current(profileId: String): InputContext = db.withTransaction { currentInside(profileId) }

    suspend fun replace(profileId: String, expectedRevision: String, assertions: List<ContextAssertion>): InputContext =
        db.withTransaction {
            requireOwnProfile(profileId)
            val previous = currentInside(profileId)
            if (previous.revisionId != expectedRevision) throw ContextRevisionConflict()
            val candidate = InputContext(profileId, "candidate", assertions).freeze()
            // A repeated answer or changed submission clock is not a factual revision.
            if (candidate.semanticRevision == previous.semanticRevision) return@withTransaction previous
            val history = db.violationContextDao().history(profileId)
            val sequence = Math.addExact(history.lastOrNull()?.sequence ?: 0L, 1L)
            val payload = InputContextCodec.encode(candidate)
            val digest = InputRevision.digest(payload.toByteArray(Charsets.UTF_8))
            val previousId = previous.revisionId.takeUnless { it == "none" }
            val id = revisionId(profileId, previousId, sequence, digest, candidate.semanticRevision)
            db.violationContextDao().insert(ViolationContextRevisionEntity(id, profileId, previousId, sequence,
                InputContextCodec.VERSION, payload, digest, candidate.semanticRevision, clock.millis()))
            candidate.copy(revisionId = id)
        }

    /** The caller already holds the same Room snapshot as the card sources. */
    internal suspend fun currentInside(profileId: String): InputContext {
        requireOwnProfile(profileId)
        var previous: ViolationContextRevisionEntity? = null
        var current = InputContext.empty(profileId)
        for (row in db.violationContextDao().history(profileId)) {
            check(row.profileId == profileId && row.sequence == (previous?.sequence ?: 0L) + 1L)
            check(row.previousId == previous?.id && row.payloadVersion == InputContextCodec.VERSION)
            check(row.payloadDigest == InputRevision.digest(row.payload.toByteArray(Charsets.UTF_8)))
            current = InputContextCodec.decode(row.payload, row.id)
            check(current.profileId == profileId && current.semanticRevision == row.semanticRevision)
            check(row.id == revisionId(profileId, row.previousId, row.sequence, row.payloadDigest, row.semanticRevision))
            previous = row
        }
        return current
    }

    private suspend fun requireOwnProfile(profileId: String) {
        require(db.driverProfileDao().localProfile()?.id == profileId)
    }

    companion object {
        internal fun revisionId(profileId: String, previousId: String?, sequence: Long,
            payloadDigest: String, semanticRevision: String) = InputRevision.hash("stored-context-revision") {
                text(profileId); nullable(previousId); long(sequence); int(InputContextCodec.VERSION)
                text(payloadDigest); text(semanticRevision)
            }
    }
}
