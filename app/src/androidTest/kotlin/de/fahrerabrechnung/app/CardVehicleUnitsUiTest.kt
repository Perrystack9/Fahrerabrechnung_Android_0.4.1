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

class CardVehicleUnitsUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(id: String = "source", empty: Boolean = false, count: Int = 1,
                       software: ByteArray = byteArrayOf(0, 255.toByte(), 127, 128.toByte()),
                       time: Int = 0): SavedCardVehicleUnits {
        val bytes = ByteArray(2002)
        if (!empty) {
            ByteBuffer.wrap(bytes).putShort((count-1).toShort())
            repeat(count) { slot ->
                ByteBuffer.wrap(bytes, 2 + slot * 10, 10).putInt(time).put(253.toByte()).put(254.toByte()).put(software)
            }
        }
        val file = CardVehicleUnitsFile(CardGeneration.GEN2V2, 200, bytes)
        return SavedCardVehicleUnits(id, "own", file, CardVehicleUnitsParser.parse(file), 0)
    }
    @Test fun missingAndValidEmptySourcesRemainDistinctAcrossSelectionChanges() {
        val selected = mutableStateOf<SavedCardVehicleUnits?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardVehicleUnitsScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Fahrtenschreiber-Datei verfügbar.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Fahrtenschreiber-Datei sind keine Nutzungen aufgezeichnet.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Herstellercode des Fahrtenschreibers: Kartenwert 253").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Herstellercode des Fahrtenschreibers: Kartenwert 253").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Fahrtenschreiber-Datei verfügbar.").assertIsDisplayed()
    }
    @Test fun unknownTimeAndSoftwareRetainExactRawValuesWithoutInventedLabels() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardVehicleUnitsScreen(PaddingValues(), source()) } }
        compose.onNodeWithText("Nutzungsbeginn: Zeitpunkt nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Geräte-Typcode: Kartenwert 254").assertIsDisplayed()
        compose.onNodeWithText("Softwareversion: nicht lesbar (Rohwert 00 FF 7F 80)").assertIsDisplayed()
    }
    @Test fun printableSoftwareVersionIsPreservedAndClearedOnSourceChange() {
        val selected = mutableStateOf(source(software = "0123".toByteArray(), time = 1790000000))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardVehicleUnitsScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Softwareversion: 0123").assertIsDisplayed()
        compose.onNodeWithText("Nutzungsbeginn: Zeitpunkt nicht angegeben").assertDoesNotExist()
        compose.runOnIdle { selected.value = source(id = "other") }
        compose.onNodeWithText("Softwareversion: 0123").assertDoesNotExist()
        compose.onNodeWithText("Softwareversion: nicht lesbar (Rohwert 00 FF 7F 80)").assertIsDisplayed()
    }
    @Test fun twentyRecordBatchesResetForEachSelectedSource() {
        val selected = mutableStateOf<SavedCardVehicleUnits?>(source(count = 21))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardVehicleUnitsScreen(PaddingValues(), selected.value) } }
        compose.onAllNodesWithText("Aufgezeichneter Fahrtenschreiber").assertCountEquals(20)
        compose.onNodeWithText("Weitere Einträge anzeigen").performScrollTo().performClick()
        compose.onAllNodesWithText("Aufgezeichneter Fahrtenschreiber").assertCountEquals(21)
        compose.runOnIdle { selected.value = source(id = "other", count = 21) }
        compose.onAllNodesWithText("Aufgezeichneter Fahrtenschreiber").assertCountEquals(20)
    }
}
