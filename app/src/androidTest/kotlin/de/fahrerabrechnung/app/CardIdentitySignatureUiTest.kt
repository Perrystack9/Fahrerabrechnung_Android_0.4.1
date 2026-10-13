package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import org.junit.Rule
import org.junit.Test

class CardIdentitySignatureUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(kind: CardIdentitySourceKind, generation: CardGeneration): SavedCardIdentitySource {
        val raw = when (kind) {
            CardIdentitySourceKind.IDENTIFICATION -> IdentityUiFixtures.original()
            CardIdentitySourceKind.APPLICATION -> if (generation == CardGeneration.GEN1)
                byteArrayOf(1, 1, 0, 1, 1, 0, 16, 0, 1, 1) else IdentityUiFixtures.application()
            CardIdentitySourceKind.APPLICATION_V2 -> byteArrayOf(0, 8, 0, 1, 0, 1, 0, 1, 0, 0)
        }
        val file = CardIdentitySourceFile(kind, generation, raw)
        return SavedCardIdentitySource("source-${kind.name}-${generation.name}", "own", file, CardIdentitySourceParser.parse(file), 0)
    }
    private fun certificate(): CardCertificateFile {
        fun tlv(tag: Int, bytes: ByteArray): ByteArray {
            val prefix = if (tag > 255) byteArrayOf((tag ushr 8).toByte(), tag.toByte()) else byteArrayOf(tag.toByte())
            val size = if (bytes.size < 128) byteArrayOf(bytes.size.toByte()) else byteArrayOf(0x81.toByte(), bytes.size.toByte())
            return prefix + size + bytes
        }
        val body = tlv(0x5F29, byteArrayOf(0)) + tlv(0x42, ByteArray(8) { 1 }) +
            tlv(0x5F4C, DriverCardFiles.gen2Aid + byteArrayOf(17)) +
            tlv(0x7F49, tlv(6, byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 3, 1, 7)) + tlv(0x86, ByteArray(65) { if (it == 0) 4 else 7 })) +
            tlv(0x5F20, ByteArray(8) { 9 }) + tlv(0x5F25, ByteArray(4)) + tlv(0x5F24, ByteArray(4))
        return CardCertificateFile(CardCertificateKind.SIGNING, CardGeneration.GEN2V2,
            tlv(0x7F21, tlv(0x7F4E, body) + tlv(0x5F37, ByteArray(64) { 3 })))
    }
    private fun signatures(sources: Map<CardIdentitySourceKind, SavedCardIdentitySource>) = sources.mapValues { (kind, source) ->
        val g1 = source.file.generation == CardGeneration.GEN1
        val policy = if (g1) CardSignaturePolicy.RSA_SHA1 else CardSignaturePolicy.NIST_P256
        CardIdentitySignature(kind, source.file.generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, CardSignaturePhase.SIGN,
            statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = policy.length,
            original = source.file, certificate = if (g1) null else certificate(), bytes = ByteArray(policy.length) { 9 })
    }
    @Test fun allFiveReceiptsShowExactSizesAndUnverifiedLengthsAcrossApplicationAndEfChanges() {
        val generation = mutableStateOf(CardGeneration.GEN2V2)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            val sources = CardIdentitySourceKind.entries.filter { it.supports(generation.value) }.associateWith { source(it, generation.value) }
            CardIdentitySourceScreen(PaddingValues(), sources, generation.value.name, signatures(sources))
        } }
        for (gen in listOf(CardGeneration.GEN2V2, CardGeneration.GEN1)) {
            compose.runOnIdle { generation.value = gen }
            for (kind in CardIdentitySourceKind.entries.filter { it.supports(gen) }) {
                compose.onNodeWithText("${kind.identitySourceTitle()} öffnen").performScrollTo().performClick()
                compose.onNodeWithText("Dateigröße: ${source(kind, gen).file.bytes.size} Bytes").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText("Kartensignatur: ${if (gen == CardGeneration.GEN1) 128 else 64} Bytes · aufgenommen · ungeprüft").performScrollTo().assertIsDisplayed()
            }
        }
        compose.onNodeWithText("Kartenanwendung V2 öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Für diese Kartenanwendung nicht vorgesehen.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartensignatur:", substring = true).assertDoesNotExist()
    }
    @Test fun sessionAndArchiveChangesClearReceiptsAndMaskDeduplicatedCardNumber() {
        val selected = mapOf(CardIdentitySourceKind.IDENTIFICATION to source(CardIdentitySourceKind.IDENTIFICATION, CardGeneration.GEN1))
        val receipt = mutableStateOf("new")
        val archive = mutableStateOf(false)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), if (archive.value) emptyMap() else selected, receipt.value,
                if (receipt.value == "new" && !archive.value) signatures(selected) else emptyMap())
        } }
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: synthetic-own").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { receipt.value = "old" }
        compose.onNodeWithText("Kartennummer: synthetic-own").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Signatur in diesem Import nicht aufgenommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("aufgenommen · ungeprüft", substring = true).assertDoesNotExist()
        compose.runOnIdle { archive.value = true; receipt.value = "archive" }
        compose.onNodeWithText("Karteninhaber:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
    }
    @Test fun failedNewReadShowsReasonAlongsideIndependentUnsignedSource() {
        val kind = CardIdentitySourceKind.IDENTIFICATION
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), mapOf(kind to source(kind, CardGeneration.GEN1)), "failed",
                mapOf(kind to CardIdentitySignature(kind, CardGeneration.GEN1, CardSignatureStatus.FAILED,
                    CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.READ, CardSignatureReason.INCOMPLETE_READ)))
        } }
        compose.onNodeWithText("Kartensignatur: fehlgeschlagen · Datei nicht vollständig gelesen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dateigröße: 143 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("aufgenommen · ungeprüft", substring = true).assertDoesNotExist()
    }
}
