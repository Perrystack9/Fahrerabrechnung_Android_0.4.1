package de.fahrerabrechnung.data.card

import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.CancellationException
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner

internal open class CoreSignatureCrypto {
    fun rsa(key: Gen1RsaPublicKey, data: ByteArray, signature: ByteArray): Boolean {
        if (signature.size != 128) return false
        val s = BigInteger(1, signature)
        if (s.signum() <= 0 || s >= key.modulus) return false
        val prefix = "3021300906052b0e03021a05000414".chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val digestInfo = prefix + MessageDigest.getInstance("SHA-1").digest(data)
        val expected = byteArrayOf(0, 1) + ByteArray(128 - digestInfo.size - 3) { -1 } + byteArrayOf(0) + digestInfo
        return MessageDigest.isEqual(expected, rsaPublicBlock(key, signature))
    }

    fun ecKey(policy: CardSignaturePolicy, bytes: ByteArray): ECPublicKeyParameters? {
        val parameters = policy.oid?.let { ECNamedCurveTable.getByOID(ASN1ObjectIdentifier(hexOid(it))) } ?: return null
        val coordinateWidth = (parameters.curve.fieldSize + 7) / 8
        if (bytes.size != 1 + 2 * coordinateWidth || bytes[0] != 4.toByte()) return null
        return try {
            val x = BigInteger(1, bytes.copyOfRange(1, 1 + coordinateWidth))
            val y = BigInteger(1, bytes.copyOfRange(1 + coordinateWidth, bytes.size))
            val curve = parameters.curve
            if (!curve.isValidFieldElement(x) || !curve.isValidFieldElement(y)) return null
            val point = curve.validatePoint(x, y)
            if (point.isInfinity || !point.isValid || !point.multiply(parameters.n).isInfinity) return null
            ECPublicKeyParameters(point, ECDomainParameters(parameters))
        } catch (_: IllegalArgumentException) { null }
    }

    open fun ec(policy: CardSignaturePolicy, key: ECPublicKeyParameters, data: ByteArray, signature: ByteArray): Boolean {
        if (signature.size != policy.length) return false
        val width = policy.length / 2
        val r = BigInteger(1, signature.copyOfRange(0, width))
        val s = BigInteger(1, signature.copyOfRange(width, signature.size))
        if (r.signum() <= 0 || s.signum() <= 0 || r >= key.parameters.n || s >= key.parameters.n) return false
        val verifier = ECDSASigner()
        verifier.init(false, key)
        return verifier.verifySignature(MessageDigest.getInstance(policy.hash).digest(data), r, s)
    }

    // Only the six existing Card_Sign domains are accepted; no arbitrary parameters.
    private fun hexOid(hex: String): String = when (hex) {
        "2A8648CE3D030107" -> "1.2.840.10045.3.1.7"
        "2B2403030208010107" -> "1.3.36.3.3.2.8.1.1.7"
        "2B81040022" -> "1.3.132.0.34"
        "2B240303020801010B" -> "1.3.36.3.3.2.8.1.1.11"
        "2B240303020801010D" -> "1.3.36.3.3.2.8.1.1.13"
        "2B81040023" -> "1.3.132.0.35"
        else -> error("Unsupported Card_Sign domain")
    }
}

internal class CardCoreSignatureVerifier(
    private val recoveryKeys: Gen1RsaRecoveryKeyCatalog = Gen1RsaRecoveryKeyCatalog.DEFAULT,
    private val crypto: CoreSignatureCrypto = CoreSignatureCrypto(),
) {
    fun verify(input: SavedCoreVerificationInput): CardCoreSignatureVerification {
        val source = input.source
        fun unavailable(reason: CardSignatureMathReason) = CardCoreSignatureVerification(source, CardSignatureMathStatus.NOT_CHECKABLE, reason)
        val signature = input.signature ?: return unavailable(CardSignatureMathReason.NO_SIGNATURE)
        if (source.kind != signature.kind || source.generation != signature.generation ||
            source.application != (if (signature.generation == CardGeneration.GEN1) CardApplication.GEN1 else CardApplication.GEN2) ||
            source.signatureDigest != signature.bytes?.let(::coreMathDigest) ||
            source.originalDigest != signature.original?.bytes?.let(::coreMathDigest) ||
            source.certificateDigest != signature.certificate?.bytes?.let(::coreMathDigest) ||
            source.issuerDigest != input.issuer?.bytes?.let(::coreMathDigest)) return unavailable(CardSignatureMathReason.SOURCE_MISMATCH)
        if (signature.status != CardSignatureStatus.CAPTURED_UNVERIFIED) return unavailable(
            if (signature.status == CardSignatureStatus.FAILED) CardSignatureMathReason.CAPTURE_FAILED else CardSignatureMathReason.NOT_ATTEMPTED)
        val original = signature.original ?: return unavailable(CardSignatureMathReason.NO_ORIGINAL)
        val cert = signature.certificate ?: return unavailable(CardSignatureMathReason.NO_SIGNING_CERTIFICATE)
        val policy = signature.policy ?: return unavailable(CardSignatureMathReason.UNSUPPORTED_POLICY)
        val bytes = signature.bytes ?: return unavailable(CardSignatureMathReason.NO_SIGNATURE)
        return try {
            val valid: Boolean
            val keyFingerprint: String
            var catalogFingerprint: String? = null
            if (signature.generation == CardGeneration.GEN1) {
                if (policy != CardSignaturePolicy.RSA_SHA1 || cert.kind != CardCertificateKind.CARD) return unavailable(CardSignatureMathReason.UNSUPPORTED_POLICY)
                val issuerFile = input.issuer ?: return unavailable(CardSignatureMathReason.NO_GEN1_ISSUER)
                if (issuerFile.kind != CardCertificateKind.CA || issuerFile.generation != CardGeneration.GEN1) return unavailable(CardSignatureMathReason.SOURCE_MISMATCH)
                val root = recoveryKeys.find(issuerFile.bytes.copyOfRange(186, 194)) ?: return unavailable(CardSignatureMathReason.UNKNOWN_GEN1_RECOVERY_KEY)
                val issuer = Gen1RsaCertificateRecovery.recover(issuerFile.bytes, root, 0) ?: return unavailable(CardSignatureMathReason.CERTIFICATE_RECOVERY_FAILED)
                val key = Gen1RsaCertificateRecovery.recover(cert.bytes, issuer, 1) ?: return unavailable(CardSignatureMathReason.CERTIFICATE_RECOVERY_FAILED)
                valid = crypto.rsa(key, original.bytes, bytes)
                keyFingerprint = key.fingerprint
                catalogFingerprint = recoveryKeys.fingerprint
            } else {
                if (cert.kind != CardCertificateKind.SIGNING || policy == CardSignaturePolicy.RSA_SHA1) return unavailable(CardSignatureMathReason.UNSUPPORTED_POLICY)
                val parsed = CardCertificateParser.parse(cert) as? CardEccCertificateRecord ?: return unavailable(CardSignatureMathReason.NO_SIGNING_CERTIFICATE)
                if (parsed.equipmentTypeRaw != 17 || CardSignaturePolicy.from(signature.generation, cert) != policy) return unavailable(CardSignatureMathReason.UNSUPPORTED_POLICY)
                val point = parsed.publicPointBytes.map(Int::toByte).toByteArray()
                val key = crypto.ecKey(policy, point) ?: return unavailable(CardSignatureMathReason.INVALID_PUBLIC_KEY)
                valid = crypto.ec(policy, key, original.bytes, bytes)
                keyFingerprint = coreMathDigest(policy.name.toByteArray(Charsets.US_ASCII) + point)
            }
            CardCoreSignatureVerification(source, if (valid) CardSignatureMathStatus.VALID else CardSignatureMathStatus.INVALID,
                publicKeyFingerprint = keyFingerprint, recoveryCatalogFingerprint = catalogFingerprint)
        } catch (cancel: CancellationException) { throw cancel }
        catch (_: Exception) { unavailable(CardSignatureMathReason.ENGINE_ERROR) }
    }
}
