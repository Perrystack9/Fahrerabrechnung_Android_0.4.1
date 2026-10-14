package de.fahrerabrechnung.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.data.card.*
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.Security
import java.util.zip.ZipInputStream
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** The exact fixed independent vectors also run on Android, without JCA provider mutation. */
@RunWith(AndroidJUnit4::class)
class CardCoreMathAndroidTest {
    @Test fun rsaRecoveryAndAllSixEcDomainsWorkOnAndroidWithStrictPlainFraming() {
        val providers = Security.getProviders().map { it.name }
        val archive = InstrumentationRegistry.getInstrumentation().context.assets.open("core-math/vectors.zip").use { it.readBytes() }
        assertEquals("ea71e929665bfa5f34ec12d9a70534bdfc4761e62a204ffd37ff776158d93326",
            MessageDigest.getInstance("SHA-256").digest(archive).joinToString("") { "%02x".format(it) })
        val files = ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            buildMap { while (true) { val entry = zip.nextEntry ?: break; put(entry.name, zip.readBytes()) } }
        }
        val crypto = CoreSignatureCrypto()
        val root = checkNotNull(Gen1RsaRecoveryKeyCatalog(listOf(files.getValue("test-root.bin"))).find("ROOTTEST".toByteArray()))
        val ca = checkNotNull(Gen1RsaCertificateRecovery.recover(files.getValue("test-ca.bin"), root, 0))
        val key = checkNotNull(Gen1RsaCertificateRecovery.recover(files.getValue("test-card.bin"), ca, 1))
        val publicCa = files.getValue("public-msca.bin")
        assertNotNull(Gen1RsaCertificateRecovery.recover(publicCa,
            checkNotNull(Gen1RsaRecoveryKeyCatalog.DEFAULT.find(publicCa.copyOfRange(186, 194))), 0))
        for (kind in listOf("activity", "vehicles")) {
            assertTrue(crypto.rsa(key, files.getValue("rsa-$kind.raw"), files.getValue("rsa-$kind.sig")))
            for (bad in listOf("bad-padding", "bad-oid", "bad-suffix", "missing-null"))
                assertFalse(crypto.rsa(key, files.getValue("rsa-$kind.raw"), files.getValue("rsa-$kind-$bad.sig")))
        }
        val curves = mapOf("nist256" to CardSignaturePolicy.NIST_P256, "bp256" to CardSignaturePolicy.BRAINPOOL_P256,
            "nist384" to CardSignaturePolicy.NIST_P384, "bp384" to CardSignaturePolicy.BRAINPOOL_P384,
            "bp512" to CardSignaturePolicy.BRAINPOOL_P512, "nist521" to CardSignaturePolicy.NIST_P521)
        for ((name, policy) in curves) {
            val ec = checkNotNull(crypto.ecKey(policy, files.getValue("$name.point")))
            for (kind in listOf("activity", "vehicles")) {
                val raw = files.getValue("$name-$kind.raw"); val sig = files.getValue("$name-$kind.sig")
                assertTrue("$name/$kind", crypto.ec(policy, ec, raw, sig))
                assertFalse(crypto.ec(policy, ec, raw, files.getValue("$name-$kind-double.sig")))
                assertFalse(crypto.ec(policy, ec, raw.copyOf().also { it[it.lastIndex] = 1 }, sig))
                val width = sig.size / 2
                assertFalse(crypto.ec(policy, ec, raw, fixedUnsigned(ec.parameters.n, width) + sig.copyOfRange(width, sig.size)))
                val r = BigInteger(1, sig.copyOfRange(0, width)); val s = BigInteger(1, sig.copyOfRange(width, sig.size))
                assertTrue(crypto.ec(policy, ec, raw, fixedUnsigned(r, width) + fixedUnsigned(s.max(ec.parameters.n - s), width)))
            }
            assertNull(crypto.ecKey(policy, files.getValue("$name.point").copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }))
        }
        assertTrue(crypto.ec(CardSignaturePolicy.NIST_P256, checkNotNull(crypto.ecKey(CardSignaturePolicy.NIST_P256,
            files.getValue("nist256.point"))), files.getValue("nist256-activity.raw"), files.getValue("nist256-plain30.sig")))
        val mixed = CardCertificateFile(CardCertificateKind.SIGNING, CardGeneration.GEN2V2, files.getValue("mixed.cert"))
        val record = CardCertificateParser.parse(mixed) as CardEccCertificateRecord
        val subjectPolicy = checkNotNull(CardSignaturePolicy.from(CardGeneration.GEN2V2, mixed))
        assertEquals(CardSignaturePolicy.NIST_P256, subjectPolicy)
        assertTrue(crypto.ec(CardSignaturePolicy.BRAINPOOL_P384, checkNotNull(crypto.ecKey(CardSignaturePolicy.BRAINPOOL_P384,
            files.getValue("mixed-issuer.point"))), record.originalBodyBytes.map(Int::toByte).toByteArray(), record.signatureBytes.map(Int::toByte).toByteArray()))
        for (kind in listOf("activity", "vehicles")) assertTrue(crypto.ec(subjectPolicy,
            checkNotNull(crypto.ecKey(subjectPolicy, record.publicPointBytes.map(Int::toByte).toByteArray())),
            files.getValue("mixed-$kind.raw"), files.getValue("mixed-$kind.sig")))
        assertEquals(providers, Security.getProviders().map { it.name })
    }
}
