package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.nio.ByteBuffer
import org.junit.Rule
import org.junit.Test

class CardMetadataUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(kind: CardMetadataKind = CardMetadataKind.DRIVING_LICENCE, id: String = "source",
                       empty: Boolean = false, unknown: Boolean = false): SavedCardMetadata {
        val bytes = ByteArray(kind.fileLength)
        if (!empty) when (kind) {
            CardMetadataKind.DRIVING_LICENCE -> {
                bytes[0] = if (unknown) 255.toByte() else 1
                "TEST AUTHORITY".toByteArray().copyInto(bytes, 1); bytes[36] = 253.toByte()
                if (unknown) byteArrayOf(0, 127, 128.toByte(), 255.toByte()).copyInto(bytes, 37)
                else "TEST-LICENCE-01".toByteArray().copyInto(bytes, 37)
            }
            CardMetadataKind.CURRENT_USAGE -> {
                ByteBuffer.wrap(bytes).putInt(if (unknown) 0 else 1790000100).put(253.toByte()).put((if (unknown) 255 else 1).toByte())
                "TEST OWN".toByteArray().copyInto(bytes, 6)
            }
            CardMetadataKind.LAST_CARD_DOWNLOAD -> ByteBuffer.wrap(bytes).putInt(1790000200)
        }
        val file = CardMetadataFile(kind, CardGeneration.GEN2V2, bytes)
        return SavedCardMetadata(id, "own", file, CardMetadataParser.parse(file), 0)
    }
    @Test fun licenceNumberRequiresExplicitRevealAndResetsForTheNextSource() {
        val selected = mutableStateOf<SavedCardMetadata?>(source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.DRIVING_LICENCE, selected.value)
        } }
        compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Nummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Führerscheinnummer: TEST-LICENCE-01").assertIsDisplayed()
        compose.onNodeWithText("Nummer verbergen").performClick()
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Nummer anzeigen").performClick()
        compose.runOnIdle { selected.value = source(id = "other") }
        compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
    }
    @Test fun unreadableLicenceBytesRemainHiddenUntilExplicitReveal() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.DRIVING_LICENCE, source(unknown = true))
        } }
        compose.onNodeWithText("Ausstellende Stelle: Text nicht lesbar (Zeichensatzcode 255)").assertIsDisplayed()
        compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("00 7F 80 FF", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Nummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Führerscheinnummer: nicht lesbar (Rohwert 00 7F 80 FF", substring = true).assertIsDisplayed()
    }
    @Test fun missingAndValidEmptyMetadataStayDistinctAcrossSelections() {
        val selected = mutableStateOf<SavedCardMetadata?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.DRIVING_LICENCE, selected.value)
        } }
        compose.onNodeWithText("Für diese Übernahme ist diese Kartendatei nicht gespeichert.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Kartendatei ist kein Eintrag aufgezeichnet.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Ausstellende Stelle: TEST AUTHORITY").assertDoesNotExist()
    }
    @Test fun unknownCurrentUsageRetainsItsNationWithoutPretendingToBeLive() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.CURRENT_USAGE, source(CardMetadataKind.CURRENT_USAGE, unknown = true))
        } }
        compose.onNodeWithText("Aufgezeichneter Beginn: Zeitpunkt nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeugkennzeichen: Text nicht lesbar (Zeichensatzcode 255)").assertIsDisplayed()
        compose.onNodeWithText("Zulassungsstaat: Kartenwert 253").assertIsDisplayed()
        compose.onNodeWithText("Dieser gespeicherte Eintrag zeigt keinen Live-Fahrzustand und enthält keinen Nutzungsabschluss.").assertIsDisplayed()
    }
    @Test fun downloadShowsOriginalCardTimeAndClearedSourceWithoutAnAppTimestamp() {
        val selected = mutableStateOf<SavedCardMetadata?>(source(CardMetadataKind.LAST_CARD_DOWNLOAD))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.LAST_CARD_DOWNLOAD, selected.value)
        } }
        compose.onNodeWithText("Download-Zeitpunkt auf der Karte: ", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Der Kartenwert ist kein Nachweis eines Downloads durch diese App.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(CardMetadataKind.LAST_CARD_DOWNLOAD, id = "cleared", empty = true) }
        compose.onNodeWithText("In dieser Kartendatei ist kein Eintrag aufgezeichnet.").assertIsDisplayed()
        compose.onNodeWithText("Download-Zeitpunkt auf der Karte: ", substring = true).assertDoesNotExist()
    }
    @Test fun changingMetadataKindAndReturningToLicenceResetsTheReveal() {
        val kind = mutableStateOf(CardMetadataKind.DRIVING_LICENCE)
        val selected = mutableStateOf(source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), kind.value, selected.value)
        } }
        compose.onNodeWithText("Nummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Führerscheinnummer: TEST-LICENCE-01").assertIsDisplayed()
        compose.runOnIdle { kind.value = CardMetadataKind.CURRENT_USAGE; selected.value = source(CardMetadataKind.CURRENT_USAGE) }
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
        compose.runOnIdle { kind.value = CardMetadataKind.DRIVING_LICENCE; selected.value = source() }
        compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
    }
}
