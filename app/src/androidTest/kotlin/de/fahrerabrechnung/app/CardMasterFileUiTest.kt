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

class CardMasterFileUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(kind: CardMetadataKind = CardMetadataKind.ICC_IDENTIFICATION,
                       id: String = "source", empty: Boolean = false, unknownMonth: Boolean = false): SavedCardMetadata {
        val bytes = if (empty) ByteArray(kind.fileLength) else when (kind) {
            CardMetadataKind.ICC_IDENTIFICATION -> ByteBuffer.allocate(25).put(253.toByte()).putInt(0x80000001.toInt())
                .put(if (unknownMonth) byteArrayOf(250.toByte(), 255.toByte()) else byteArrayOf(0x10, 0x26))
                .put(254.toByte()).put(253.toByte()).put("AB-01234".toByteArray()).put(252.toByte())
                .put("DE".toByteArray()).put(251.toByte()).put(byteArrayOf(255.toByte(), 128.toByte(), 0, 127)).array()
            CardMetadataKind.IC_IDENTIFICATION -> byteArrayOf(0, 127, 128.toByte(), 255.toByte(), 222.toByte(), 173.toByte(), 190.toByte(), 239.toByte())
            else -> error("MF fixture only")
        }
        val file = CardMetadataFile(kind, CardGeneration.GEN2V2, bytes)
        return SavedCardMetadata(id, "own", file, CardMetadataParser.parse(file), 0)
    }
    @Test fun cardSerialIsHiddenUntilRequestedAndResetOnSourceChange() {
        val selected = mutableStateOf<SavedCardMetadata?>(source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.ICC_IDENTIFICATION, selected.value)
        } }
        compose.onNodeWithText("Kartenseriennummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("2147483649", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Kennung anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartenseriennummer: 2147483649").assertIsDisplayed()
        compose.onNodeWithText("Kennung verbergen").performClick()
        compose.onNodeWithText("2147483649", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Kennung anzeigen").performClick()
        compose.runOnIdle { selected.value = source(id = "other") }
        compose.onNodeWithText("Kartenseriennummer: verborgen").assertIsDisplayed()
        compose.onNodeWithText("2147483649", substring = true).assertDoesNotExist()
    }
    @Test fun chipIdentifierRevealsBothExactOctetStringsOnlyAfterATap() {
        val selected = mutableStateOf<SavedCardMetadata?>(source(CardMetadataKind.IC_IDENTIFICATION))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.IC_IDENTIFICATION, selected.value)
        } }
        compose.onNodeWithText("Chipkennung: verborgen").assertIsDisplayed()
        compose.onNodeWithText("00 7F 80 FF DE AD BE EF", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Kennung anzeigen").performClick()
        compose.onNodeWithText("Chipkennung: 00 7F 80 FF DE AD BE EF").assertIsDisplayed()
        compose.onNodeWithText("Die Chipkennung besteht aus Serienbytes und Herstellerreferenz.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(CardMetadataKind.IC_IDENTIFICATION, id = "other") }
        compose.onNodeWithText("Chipkennung: verborgen").assertIsDisplayed()
        compose.onNodeWithText("00 7F 80 FF DE AD BE EF", substring = true).assertDoesNotExist()
    }
    @Test fun missingAndValidEmptyMasterSourcesRemainDistinct() {
        val selected = mutableStateOf<SavedCardMetadata?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.ICC_IDENTIFICATION, selected.value)
        } }
        compose.onNodeWithText("Für diese Übernahme ist diese Kartendatei nicht gespeichert.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Kartendatei ist kein Eintrag aufgezeichnet.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Kartenseriennummer: verborgen").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Kartenseriennummer: verborgen").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme ist diese Kartendatei nicht gespeichert.").assertIsDisplayed()
    }
    @Test fun unknownMonthAndCodesRemainOriginalWithoutAFullDateOrAuthenticityClaim() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.ICC_IDENTIFICATION, source(unknownMonth = true))
        } }
        compose.onNodeWithText("Herstellercode der Karte: Kartenwert 253").assertIsDisplayed()
        compose.onNodeWithText("Kartentyp-Code: Kartenwert 254").assertIsDisplayed()
        compose.onNodeWithText("Monat/Jahr-Code: nicht lesbar (Rohwert FA FF)").assertIsDisplayed()
        compose.onNodeWithText("Bauartgenehmigung: AB-01234").assertIsDisplayed()
        compose.onNodeWithText("Diese Kartenwerte sind kein kryptografischer Echtheitsnachweis.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ortszeit", substring = true).assertDoesNotExist()
    }
    @Test fun switchingMasterKindResetsPreviouslyRevealedIdentifiers() {
        val kind = mutableStateOf(CardMetadataKind.ICC_IDENTIFICATION)
        val selected = mutableStateOf(source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), kind.value, selected.value)
        } }
        compose.onNodeWithText("Kennung anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartenseriennummer: 2147483649").assertIsDisplayed()
        compose.runOnIdle { kind.value = CardMetadataKind.IC_IDENTIFICATION; selected.value = source(CardMetadataKind.IC_IDENTIFICATION) }
        compose.onNodeWithText("Chipkennung: verborgen").assertIsDisplayed()
        compose.onNodeWithText("2147483649", substring = true).assertDoesNotExist()
        compose.runOnIdle { kind.value = CardMetadataKind.ICC_IDENTIFICATION; selected.value = source() }
        compose.onNodeWithText("Kartenseriennummer: verborgen").assertIsDisplayed()
    }
}
