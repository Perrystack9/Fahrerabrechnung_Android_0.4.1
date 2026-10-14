package de.fahrerabrechnung.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.data.card.*
import java.security.MessageDigest
import java.security.Security
import java.util.zip.ZipInputStream
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardStoredSignatureMathAndroidTest {
    @Test fun everyStoredEfUsesTheSameRsaAndSixEcDomainsOnAndroidWithoutProviderChanges() {
        val providers = Security.getProviders().map { it.name }
        val archive = InstrumentationRegistry.getInstrumentation().context.assets.open("stored-math/vectors.zip").use { it.readBytes() }
        assertEquals("ecf080dd616b8b3d32c4d58796816dfea17bde83d243e36f6c15df2c8812b4ea",
            MessageDigest.getInstance("SHA-256").digest(archive).joinToString("") { "%02x".format(it) })
        val files = ZipInputStream(archive.inputStream()).use { zip ->
            buildMap { while (true) { val entry = zip.nextEntry ?: break; put(entry.name, zip.readBytes()) } }
        }
        val verifier = CardSignatureVerifier(Gen1RsaRecoveryKeyCatalog(listOf(files.getValue("test-root.bin"))))
        val curves = mapOf("nist256" to CardSignaturePolicy.NIST_P256, "bp256" to CardSignaturePolicy.BRAINPOOL_P256,
            "nist384" to CardSignaturePolicy.NIST_P384, "bp384" to CardSignaturePolicy.BRAINPOOL_P384,
            "bp512" to CardSignaturePolicy.BRAINPOOL_P512, "nist521" to CardSignaturePolicy.NIST_P521)
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val tag = if (generation == CardGeneration.GEN1) "g1" else if (generation == CardGeneration.GEN2) "g2" else "g2v2"
            for (ef in CardSignedEf.entries.filter { it.supports(generation) })
                for (name in if (generation == CardGeneration.GEN1) listOf("rsa") else curves.keys) {
                    val policy = if (name == "rsa") CardSignaturePolicy.RSA_SHA1 else curves.getValue(name)
                    val cert = CardCertificateFile(if (name == "rsa") CardCertificateKind.CARD else CardCertificateKind.SIGNING,
                        generation, files.getValue(if (name == "rsa") "test-card.bin" else "$name.cert"))
                    val issuer = if (name == "rsa") CardCertificateFile(CardCertificateKind.CA, generation, files.getValue("test-ca.bin")) else null
                    val prefix = "$name-$tag-${"%04x".format(ef.fileId)}"
                    val raw = files.getValue("$prefix.raw"); val bytes = files.getValue("$prefix.sig")
                    fun input(original: ByteArray) = SavedSignatureVerificationInput(CardSignatureVerificationSource("own", "child", "batch",
                        ef, generation, if (name == "rsa") CardApplication.GEN1 else CardApplication.GEN2,
                        "receipt", "original", "certificate", issuer?.let { "issuer" }, coreMathDigest(original),
                        coreMathDigest(bytes), coreMathDigest(cert.bytes), issuer?.bytes?.let(::coreMathDigest)),
                        CardMathReceipt(ef, generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, original, cert, bytes), issuer)
                    val good = verifier.verify(input(raw))
                    assertEquals(CardSignatureMathStatus.VALID, good.status)
                    assertEquals(CardCertificateTrustStatus.NOT_EVALUATED, good.certificateTrust)
                    val altered = raw.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
                    assertEquals(CardSignatureMathStatus.INVALID, verifier.verify(input(altered)).status)
                }
        }
        assertEquals(providers, Security.getProviders().map { it.name })
    }
}
