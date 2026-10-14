package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest

/** Receipts belong to the exact session. Shared raw blobs never acquire a signature globally. */
internal class CardCoreSignatureStore(private val dao: CardCoreSignatureDao, private val sources: CardMetadataDao) {
    suspend fun save(cardId: String, sessionId: String, signature: CardCoreSignature) {
        val sourceId = signature.original?.let { CardCoreSnapshotStore.fingerprint(cardId, it) }
        val certificateId = signature.certificate?.let { CardCertificateStore.fingerprint(cardId, it) }
        val row = CardCoreSignatureEntity(sessionId, cardId, signature.kind.storageKind, signature.generation.name,
            application(signature.generation).name, sourceId, signature.certificate?.kind?.storageKind, certificateId,
            signature.status.name, signature.policy?.name, signature.phase?.name, signature.reason?.name,
            signature.statusWord, signature.hashStatusWord, signature.signStatusWord, signature.failure?.name,
            signature.actualLength, signature.bytes, "")
        // Validate the same-session references even before the SQL foreign keys enforce them.
        decode(row)
        dao.insert(row.copy(fingerprint = fingerprint(row)))
    }

    suspend fun load(cardId: String, sessionId: String): Map<CardCoreEf, CardCoreSignature> =
        dao.receipts(sessionId).associate { row ->
            check(row.driverCardId == cardId && row.sessionId == sessionId && row.fingerprint == fingerprint(row))
            val signature = decode(row)
            signature.kind to signature
        }

    suspend fun verify(cardId: String, sessionId: String, expected: Map<CardCoreEf, CardCoreSignature>) {
        val actual = load(cardId, sessionId)
        check(actual.keys == expected.keys)
        for ((kind, signature) in expected) {
            check(requestFingerprint(cardId, signature) == requestFingerprint(cardId, actual.getValue(kind)))
        }
    }

    /** Caller holds the existing read transaction and profile/session/batch validation. No writes. */
    suspend fun verificationInputs(saved: SavedDriverImport): List<SavedCoreVerificationInput> {
        val cardId = saved.driverCardId
        val sessionId = saved.id
        val signatures = load(cardId, sessionId)
        val rows = dao.receipts(sessionId).associateBy { it.kind }
        val refs = sources.sources(sessionId)
        return CardCoreEf.entries.map { kind ->
            val signature = signatures[kind]
            val row = rows[kind.storageKind]
            val original = saved.coreSnapshots[kind]
            check(row == null || row.originalSourceId == null || row.originalSourceId == original?.id)
            val generation = signature?.generation ?: original?.snapshot?.generation
            val application = generation?.let(::application)
            check(saved.application == null || application == null || saved.application == application)
            var issuerId: String? = null
            val issuer = if (signature?.generation == CardGeneration.GEN1) {
                refs.singleOrNull { it.kind == CardCertificateKind.CA.storageKind }?.let { ref ->
                    check(ref.driverCardId == cardId && ref.sessionId == sessionId)
                    val raw = checkNotNull(sources.get(ref.sourceId))
                    check(raw.driverCardId == cardId && raw.kind == ref.kind && raw.generation == CardGeneration.GEN1.name)
                    CardCertificateFile(CardCertificateKind.CA, CardGeneration.GEN1, raw.bytes).also {
                        check(CardCertificateStore.fingerprint(cardId, it) == ref.sourceId)
                        issuerId = ref.sourceId
                    }
                }
            } else null
            SavedCoreVerificationInput(CardCoreVerificationSource(cardId, sessionId, saved.batchId, kind,
                generation, application, row?.fingerprint, if (row == null) original?.id else row.originalSourceId,
                row?.certificateSourceId, issuerId, (if (signature == null) original?.snapshot?.bytes else signature.original?.bytes)?.let(::coreMathDigest),
                signature?.bytes?.let(::coreMathDigest), signature?.certificate?.bytes?.let(::coreMathDigest),
                issuer?.bytes?.let(::coreMathDigest)), signature, issuer)
        }
    }

    private suspend fun decode(row: CardCoreSignatureEntity): CardCoreSignature {
        val kind = CardCoreEf.entries.single { it.storageKind == row.kind }
        val generation = CardGeneration.valueOf(row.generation)
        check(application(generation).name == row.application)
        val refs = sources.sources(row.sessionId)
        suspend fun original(id: String, sourceKind: String): CardMetadataImportEntity {
            check(refs.any { it.kind == sourceKind && it.sourceId == id && it.driverCardId == row.driverCardId })
            val source = checkNotNull(sources.get(id))
            check(source.driverCardId == row.driverCardId && source.kind == sourceKind && source.generation == row.generation)
            return source
        }
        val raw = row.originalSourceId?.let { id ->
            CardCoreSnapshot(kind, generation, original(id, kind.storageKind).bytes).also {
                check(CardCoreSnapshotStore.fingerprint(row.driverCardId, it) == id)
            }
        }
        check((row.certificateSourceId == null) == (row.certificateKind == null))
        val certificate = row.certificateSourceId?.let { id ->
            val certificateKind = CardCertificateKind.entries.single { it.storageKind == row.certificateKind }
            CardCertificateFile(certificateKind, generation, original(id, certificateKind.storageKind).bytes).also {
                check(CardCertificateStore.fingerprint(row.driverCardId, it) == id)
            }
        }
        return CardCoreSignature(kind, generation, CardSignatureStatus.valueOf(row.status),
            row.policy?.let(CardSignaturePolicy::valueOf), row.phase?.let(CardSignaturePhase::valueOf),
            row.reason?.let(CardSignatureReason::valueOf), row.statusWord, row.hashStatusWord, row.signStatusWord,
            row.failure?.let(ReaderFailure::valueOf), row.actualLength, raw, certificate, row.bytes)
    }

    companion object {
        private fun application(generation: CardGeneration): CardApplication = when (generation) {
            CardGeneration.GEN1 -> CardApplication.GEN1
            CardGeneration.GEN2, CardGeneration.GEN2V2 -> CardApplication.GEN2
            else -> error("Unsupported signature application")
        }
        fun requestFingerprint(cardId: String, signature: CardCoreSignature): String = fingerprint(
            CardCoreSignatureEntity("", cardId, signature.kind.storageKind, signature.generation.name,
                application(signature.generation).name,
                signature.original?.let { CardCoreSnapshotStore.fingerprint(cardId, it) },
                signature.certificate?.kind?.storageKind,
                signature.certificate?.let { CardCertificateStore.fingerprint(cardId, it) },
                signature.status.name, signature.policy?.name, signature.phase?.name, signature.reason?.name,
                signature.statusWord, signature.hashStatusWord, signature.signStatusWord, signature.failure?.name,
                signature.actualLength, signature.bytes, ""))

        private fun fingerprint(row: CardCoreSignatureEntity): String {
            val buffer = ByteArrayOutputStream()
            DataOutputStream(buffer).use { output ->
                for (value in listOf("card-core-signature-receipt-v1", row.sessionId, row.driverCardId, row.kind,
                    row.generation, row.application, row.originalSourceId, row.certificateKind, row.certificateSourceId,
                    row.status, row.policy, row.phase, row.reason, row.statusWord?.toString(), row.hashStatusWord?.toString(),
                    row.signStatusWord?.toString(), row.failure, row.actualLength?.toString())) {
                    val bytes = value?.toByteArray(Charsets.UTF_8)
                    output.writeInt(bytes?.size ?: -1); if (bytes != null) output.write(bytes)
                }
                output.writeInt(row.bytes?.size ?: -1); row.bytes?.let(output::write)
            }
            return MessageDigest.getInstance("SHA-256").digest(buffer.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }
}
