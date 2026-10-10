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

class CardLoadUnloadUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(id: String = "source", empty: Boolean = false, count: Int = 1): SavedCardLoadUnload {
        val bytes = ByteArray(32482)
        if (!empty) {
            ByteBuffer.wrap(bytes).putShort((count-1).toShort())
            repeat(count) { slot ->
                ByteBuffer.wrap(bytes, 2 + slot * 20, 20).putInt(0).put(254.toByte()).putInt(0)
                    .put(255.toByte()).put(ByteArray(6)).put(254.toByte()).put(byteArrayOf(0, 0, 3))
            }
        }
        val file = CardLoadUnloadFile(CardGeneration.GEN2V2, 1624, bytes)
        return SavedCardLoadUnload(id, "own", file, CardLoadUnloadParser.parse(file), 0)
    }
    @Test fun selectedMissingAndValidEmptySourcesRemainDistinctAcrossSelectionChanges() {
        val selected = mutableStateOf<SavedCardLoadUnload?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadUnloadScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Be-/Entladedatei verfügbar.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Be-/Entladedatei sind keine Vorgänge aufgezeichnet.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Be-/Entladedatei verfügbar.").assertIsDisplayed()
    }
    @Test fun reservedOperationAndUnknownClocksRemainNeutralWithoutInventedPosition() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadUnloadScreen(PaddingValues(), source()) } }
        compose.onNodeWithText("Art: unbekannt (Kartenwert 254)").assertIsDisplayed()
        compose.onNodeWithText("Beginn des Vorgangs: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Positionszeit: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Position: nicht verfügbar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: unbekannt (254)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kilometerstand (Kartenwert): 3 km").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: authentisiert").assertDoesNotExist()
    }
    @Test fun batchesResetWhenTheExactSourceChanges() {
        val selected = mutableStateOf<SavedCardLoadUnload?>(source(count = 21))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardLoadUnloadScreen(PaddingValues(), selected.value) } }
        compose.onAllNodesWithText("Aufgezeichneter Vorgang").assertCountEquals(20)
        compose.onNodeWithText("Weitere Vorgänge anzeigen").performScrollTo().performClick()
        compose.onAllNodesWithText("Aufgezeichneter Vorgang").assertCountEquals(21)
        compose.runOnIdle { selected.value = source(id = "another", count = 21) }
        compose.onAllNodesWithText("Aufgezeichneter Vorgang").assertCountEquals(20)
    }
}
