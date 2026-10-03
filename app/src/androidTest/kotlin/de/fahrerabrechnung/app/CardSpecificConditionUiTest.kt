package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.junit.Rule
import org.junit.Test

class CardSpecificConditionUiTest {
    @get:Rule val compose = createComposeRule()
    private fun saved(generation: CardGeneration = CardGeneration.GEN2V2,
                      entries: List<Pair<Long, Int>> = emptyList()): SavedCardSpecificConditions {
        val count = if (generation == CardGeneration.GEN1) 56 else 112
        val bytes = ByteArray(CardSpecificConditionParser.fileLength(generation, count))
        val b = ByteBuffer.wrap(bytes)
        if (generation != CardGeneration.GEN1) b.putShort((entries.size - 1).coerceAtLeast(0).toShort())
        entries.forEach { (time, type) -> b.putInt(time.toInt()).put(type.toByte()) }
        val file = CardSpecificConditionFile(generation, count, bytes)
        return SavedCardSpecificConditions("fixture-${generation.name}-${entries.size}", "physical-own", file,
            CardSpecificConditionParser.parse(file), 0)
    }

    @Test fun missingAndValidEmptySourcesHaveDifferentHonestMessages() {
        val source = mutableStateOf<SavedCardSpecificConditions?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardSpecificConditionScreen(PaddingValues(), source.value) } }
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Sonderbedingungen verfügbar.").assertIsDisplayed()
        compose.runOnIdle { source.value = saved() }
        compose.onNodeWithText("In dieser Kartendatei sind keine Sonderbedingungen aufgezeichnet.").assertIsDisplayed()
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Sonderbedingungen verfügbar.").assertDoesNotExist()
    }

    @Test fun exactSecondsAndRecordedTypesAppearWithoutFillingUnknownTimeOrPairingDurations() {
        val time = 1790000015L
        val source = saved(entries = listOf(time to 1, time to 2, time to 3, time to 4, 0L to 254))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardSpecificConditionScreen(PaddingValues(), source) } }
        for (label in listOf("Out of Scope · Beginn", "Out of Scope · Ende", "Fähre/Zug · Beginn", "Fähre/Zug · Ende"))
            compose.onNodeWithText(label).performScrollTo().assertIsDisplayed()
        val expected = Instant.ofEpochSecond(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX"))
        compose.onAllNodesWithText(expected).assertCountEquals(4)
        compose.onNodeWithText("Unbekannte Sonderbedingung (Code FE)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Zeitpunkt: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Dauer:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
    }

    @Test fun gen1FerryHasNoInventedEndAndAReservedTypeGetsNoGen2Meaning() {
        val source = saved(CardGeneration.GEN1, listOf(1790000015L to 3, 1790000016L to 4))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardSpecificConditionScreen(PaddingValues(), source) } }
        compose.onNodeWithText("Fähre/Zug").assertIsDisplayed()
        compose.onNodeWithText("Unbekannte Sonderbedingung (Code 04)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Beginn").assertDoesNotExist()
        compose.onNodeWithText("Fähre/Zug · Ende").assertDoesNotExist()
    }
}
