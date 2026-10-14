package de.fahrerabrechnung.data.card

import de.fahrerabrechnung.data.db.CardMetadataDao

/** Called only within the existing profile/child-validated read transaction. */
internal suspend fun signatureSnapshot(sources: CardMetadataDao, saved: SavedDriverImport,
    ef: CardSignedEf, receipt: CardMathReceipt?, fingerprint: String?, originalId: String?,
    certificateId: String?, unsignedOriginal: ByteArray? = null): SavedSignatureVerificationInput {
    val generation = receipt?.generation ?: saved.identitySources[CardIdentitySourceKind.APPLICATION]?.file?.generation
        ?: saved.identitySources[CardIdentitySourceKind.IDENTIFICATION]?.file?.generation
        ?: saved.coreSnapshots.values.firstOrNull()?.snapshot?.generation
    val application = generation?.let { if (it == CardGeneration.GEN1) CardApplication.GEN1 else CardApplication.GEN2 }
    check(saved.application == null || application == null || saved.application == application)
    check(generation == null || ef.supports(generation))
    var issuerId: String? = null
    val issuer = if (receipt?.generation == CardGeneration.GEN1) {
        sources.sources(saved.id).singleOrNull { it.kind == CardCertificateKind.CA.storageKind }?.let { ref ->
            check(ref.driverCardId == saved.driverCardId && ref.sessionId == saved.id)
            val raw = checkNotNull(sources.get(ref.sourceId))
            check(raw.driverCardId == saved.driverCardId && raw.kind == ref.kind && raw.generation == CardGeneration.GEN1.name)
            CardCertificateFile(CardCertificateKind.CA, CardGeneration.GEN1, raw.bytes).also {
                check(CardCertificateStore.fingerprint(saved.driverCardId, it) == ref.sourceId)
                issuerId = ref.sourceId
            }
        }
    } else null
    return SavedSignatureVerificationInput(CardSignatureVerificationSource(saved.driverCardId, saved.id,
        saved.batchId, ef, generation, application, fingerprint, originalId, certificateId, issuerId,
        (if (receipt == null) unsignedOriginal else receipt.original)?.let(::coreMathDigest),
        receipt?.bytes?.let(::coreMathDigest), receipt?.certificate?.bytes?.let(::coreMathDigest),
        issuer?.bytes?.let(::coreMathDigest)), receipt, issuer)
}
