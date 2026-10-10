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

class CardBorderCrossingsUiTest {
    @get:Rule val compose = createComposeRule()
    private fun source(id: String = "source", empty: Boolean = false, count: Int = 1): SavedCardBorderCrossings {
        val bytes = ByteArray(19042)
        if (!empty) {
            ByteBuffer.wrap(bytes).putShort((count-1).toShort())
            repeat(count) { slot ->
                ByteBuffer.wrap(bytes, 2 + slot * 17, 17).put(0.toByte()).put(255.toByte()).putInt(0)
                    .put(255.toByte()).put(ByteArray(6)).put(254.toByte()).put(byteArrayOf(0, 0, 3))
            }
        }
        val file = CardBorderCrossingsFile(CardGeneration.GEN2V2, 1120, bytes)
        return SavedCardBorderCrossings(id, "own", file, CardBorderCrossingsParser.parse(file), 0)
    }
    @Test fun selectedMissingAndValidEmptySourcesRemainDistinctAcrossSelectionChanges() {
        val selected = mutableStateOf<SavedCardBorderCrossings?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardBorderCrossingsScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Grenz-/Länderdatei verfügbar.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source(empty = true) }
        compose.onNodeWithText("In dieser Grenz-/Länderdatei sind keine Aufzeichnungen enthalten.").assertIsDisplayed()
        compose.runOnIdle { selected.value = source() }
        compose.onNodeWithText("Land verlassen: nicht angegeben").assertIsDisplayed()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Land verlassen: nicht angegeben").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Grenz-/Länderdatei verfügbar.").assertIsDisplayed()
    }
    @Test fun insertionCountryRestOfWorldAndUnknownInlineStatusStayNeutralWithoutInventedPosition() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardBorderCrossingsScreen(PaddingValues(), source()) } }
        compose.onNodeWithText("Land verlassen: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Land eingereist / bei Karteneinlage: Rest der Welt (Kartenwert 255)").assertIsDisplayed()
        compose.onNodeWithText("Positionszeit: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Position: nicht verfügbar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: unbekannt (254)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kilometerstand (Kartenwert): 3 km").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: authentisiert").assertDoesNotExist()
    }
    @Test fun batchesResetWhenTheExactSourceChanges() {
        val selected = mutableStateOf<SavedCardBorderCrossings?>(source(count = 21))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardBorderCrossingsScreen(PaddingValues(), selected.value) } }
        compose.onAllNodesWithText("Ländereintrag").assertCountEquals(20)
        compose.onNodeWithText("Weitere Ländereinträge anzeigen").performScrollTo().performClick()
        compose.onAllNodesWithText("Ländereintrag").assertCountEquals(21)
        compose.runOnIdle { selected.value = source(id = "another", count = 21) }
        compose.onAllNodesWithText("Ländereintrag").assertCountEquals(20)
    }
}
