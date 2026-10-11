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

class CardLoadTypeUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(id: String = "source", empty: Boolean = false, count: Int = 1,
                       type: Int = 254, time: Int = 0): SavedCardLoadType {
        val bytes = ByteArray(1682)
        if (!empty) {
            ByteBuffer.wrap(bytes).putShort((count-1).toShort())
            repeat(count) { slot ->
                ByteBuffer.wrap(bytes, 2 + slot * 5, 5).putInt(time).put(type.toByte())
            }
        }
        val file = CardLoadTypeFile(CardGeneration.GEN2V2, 336, bytes)
        return SavedCardLoadType(id, "own", file, CardLoadTypeParser.parse(file), 0)
    }
    @Test fun selectedMissingAndValidEmptySourcesRemainDistinctAcrossSelectionChanges() {
        val selected = mutableStateOf<SavedCardLoadType?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadTypeScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Ladungsart-Datei verfügbar.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Ladungsart-Datei sind keine Einträge aufgezeichnet.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Ladungsart-Datei verfügbar.").assertIsDisplayed()
    }
    @Test fun reservedTypeAndUnknownTimeRemainNeutralWithoutAnInventedInstant() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadTypeScreen(PaddingValues(), source()) } }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertIsDisplayed()
        compose.onNodeWithText("Eingetragen: Zeitpunkt nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Art: Güter").assertDoesNotExist()
        compose.onNodeWithText("Art: Fahrgäste").assertDoesNotExist()
    }
    @Test fun allDefinedCodesHaveTheirOwnLabelsAndUndefinedIsARecordedEntry() {
        val selected = mutableStateOf(source(type = 0, time = 1790000000))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadTypeScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Art: nicht festgelegt").assertIsDisplayed()
        compose.onNodeWithText("In dieser Ladungsart-Datei sind keine Einträge aufgezeichnet.").assertDoesNotExist()
        compose.onNodeWithText("Eingetragen: Zeitpunkt nicht angegeben").assertDoesNotExist()
        compose.runOnIdle { selected.value = source(id = "goods", type = 1) }
        compose.onNodeWithText("Art: Güter").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(id = "passengers", type = 2) }
        compose.onNodeWithText("Art: Fahrgäste").assertIsDisplayed()
        compose.onNodeWithText("Art: Güter").assertDoesNotExist()
    }
    @Test fun batchesResetWhenTheExactSourceChanges() {
        val selected = mutableStateOf<SavedCardLoadType?>(source(count = 21))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadTypeScreen(PaddingValues(), selected.value) } }
        compose.onAllNodesWithText("Aufgezeichneter Eintrag").assertCountEquals(20)
        compose.onNodeWithText("Weitere Einträge anzeigen").performScrollTo().performClick()
        compose.onAllNodesWithText("Aufgezeichneter Eintrag").assertCountEquals(21)
        compose.runOnIdle { selected.value = source(id = "another", count = 21) }
        compose.onAllNodesWithText("Aufgezeichneter Eintrag").assertCountEquals(20)
    }
}
