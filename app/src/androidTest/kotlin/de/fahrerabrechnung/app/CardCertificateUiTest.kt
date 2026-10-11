package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.nio.ByteBuffer
import org.junit.Rule
import org.junit.Test

/** Synthetic framing fixture. No test implies a mathematically valid or trusted certificate. */
internal object CertificateUiFixtures {
    fun bytes(role: Int = 17, effective: Long = 1790000000L, expires: Long = 1890000000L): ByteArray {
        fun tlv(tag: Int, data: ByteArray): ByteArray {
            val prefix = if (tag > 255) byteArrayOf((tag ushr 8).toByte(), tag.toByte()) else byteArrayOf(tag.toByte())
            val size = if (data.size < 128) byteArrayOf(data.size.toByte()) else byteArrayOf(0x81.toByte(), data.size.toByte())
            return prefix + size + data
        }
        val oid = byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 3, 1, 7)
        val body = tlv(0x5F29, byteArrayOf(0)) + tlv(0x42, ByteArray(8) { 1 }) +
            tlv(0x5F4C, DriverCardFiles.gen2Aid + role.toByte()) +
            tlv(0x7F49, tlv(6, oid) + tlv(0x86, ByteArray(65) { if (it == 0) 4 else 7 })) +
            tlv(0x5F20, ByteArray(8) { 9 }) + tlv(0x5F25, ByteBuffer.allocate(4).putInt(effective.toInt()).array()) +
            tlv(0x5F24, ByteBuffer.allocate(4).putInt(expires.toInt()).array())
        return tlv(0x7F21, tlv(0x7F4E, body) + tlv(0x5F37, ByteArray(64) { 3 }))
    }
    fun source(kind: CardCertificateKind = CardCertificateKind.CARD, id: String = "synthetic-cert",
        generation: CardGeneration = CardGeneration.GEN2V2, raw: ByteArray = bytes()): SavedCardCertificate {
        val file = CardCertificateFile(kind, generation, raw)
        return SavedCardCertificate(id, "own", file, CardCertificateParser.parse(file), 0)
    }
}

class CardCertificateUiTest {
    @get:Rule val compose = createComposeRule()
    private val authority = "Ausstellerreferenz: 01 01 01 01 01 01 01 01"
    private val holder = "Inhaberreferenz: 09 09 09 09 09 09 09 09"

    @Test fun gen2FieldsAreNeutralAndBothReferencesRemainHiddenUntilTapped() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCertificateScreen(PaddingValues(), mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source()))
        } }
        compose.onNodeWithText("Dateigröße: 204 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kurve des enthaltenen öffentlichen Schlüssels: NIST P-256").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ausstellerreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Inhaberreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(authority).assertDoesNotExist(); compose.onNodeWithText(holder).assertDoesNotExist()
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(authority).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(holder).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Signatur und Vertrauenskette sind noch nicht geprüft.").performScrollTo().assertIsDisplayed()
    }
    @Test fun selectingAnotherSourceSnapshotResetsRevealedReferences() {
        val selected = mutableStateOf(mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source()))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCertificateScreen(PaddingValues(), selected.value)
        } }
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(holder).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source(id = "next")) }
        compose.onNodeWithText("Inhaberreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(holder).assertDoesNotExist(); compose.onNodeWithText(authority).assertDoesNotExist()
    }
    @Test fun changingCertificateKindAndReturningResetsTheReveal() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCertificateScreen(PaddingValues(), mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source(),
                CardCertificateKind.CA to CertificateUiFixtures.source(CardCertificateKind.CA, id = "ca")))
        } }
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(holder).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("CA-Zertifikat öffnen").performScrollTo().performClick()
        compose.onNodeWithText(holder).assertDoesNotExist()
        compose.onNodeWithText("Kartenzertifikat öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Inhaberreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(holder).assertDoesNotExist()
    }
    @Test fun missingCertificateAndStoredBlankEfAreDistinctWithNoLaterFallback() {
        val selected = mutableStateOf<Map<CardCertificateKind, SavedCardCertificate>>(emptyMap())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardCertificateScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme ist dieses Zertifikat nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source(raw = ByteArray(341))) }
        compose.onNodeWithText("Die gespeicherte Zertifikatsdatei ist leer.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Referenzen anzeigen").assertDoesNotExist()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onNodeWithText("Die gespeicherte Zertifikatsdatei ist leer.").assertDoesNotExist()
    }
    @Test fun gen1ShowsOnlyTheOpenReferenceAndNoInventedEccFields() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCertificateScreen(PaddingValues(), mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source(
                generation = CardGeneration.GEN1, raw = ByteArray(194) { if (it >= 186) 1 else 7 })))
        } }
        compose.onNodeWithText("Dateiformat: Generation 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Die weiteren Angaben dieses Formats benötigen eine RSA-Nachrichtenrückgewinnung.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(authority).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Inhaberreferenz:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Zertifikatszeitraum laut Datei", substring = true).assertDoesNotExist()
    }
    @Test fun unknownRoleAndSentinelTimesShowOriginalValuesWithoutAValidityVerdict() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCertificateScreen(PaddingValues(), mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source(
                raw = CertificateUiFixtures.bytes(role = 253, effective = 0xFFFF_FFFFL, expires = 0))))
        } }
        compose.onNodeWithText("Aufgezeichneter Geräteartcode: 253").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Zertifikatsbeginn: nicht angegeben").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Zertifikatsende: nicht angegeben").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Signatur und Vertrauenskette sind noch nicht geprüft.").performScrollTo().assertIsDisplayed()
    }
    @Test fun leavingAndReopeningTheCertificateViewResetsTheReveal() {
        val visible = mutableStateOf(true)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            if (visible.value) CardCertificateScreen(PaddingValues(), mapOf(CardCertificateKind.CARD to CertificateUiFixtures.source()))
            else Text("Other view")
        } }
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(holder).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { visible.value = false }; compose.onNodeWithText(holder).assertDoesNotExist()
        compose.runOnIdle { visible.value = true }
        compose.onNodeWithText("Inhaberreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(holder).assertDoesNotExist()
    }
}
