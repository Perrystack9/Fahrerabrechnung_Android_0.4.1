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

class CardPlacesUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun selectedMissingAndEmptySourcesAreDistinctAndNoNewSourceLeaksToOldImport() {
        val source = mutableStateOf<SavedCardPlaces?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardPlacesScreen(PaddingValues(), source.value, null) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte Ortsdatei verfügbar.").assertIsDisplayed()
        val file = CardPlacesFile(CardGeneration.GEN2V2, 112, ByteArray(2354))
        compose.runOnIdle { source.value = SavedCardPlaces("empty", "own", file, CardPlacesParser.parse(file), 0) }
        compose.onNodeWithText("In dieser Ortsdatei sind keine Aufzeichnungen enthalten.").assertIsDisplayed()
        compose.runOnIdle { source.value = null }
        compose.onNodeWithText("In dieser Ortsdatei sind keine Aufzeichnungen enthalten.").assertDoesNotExist()
    }
    @Test fun unknownTimeAndTypeAreNeutralAndSeparateAuthenticationDoesNotFillPosition() {
        val bytes = ByteArray(2354)
        ByteBuffer.wrap(bytes).putShort(0).putInt(0).put(254.toByte()).put(13.toByte()).put(0.toByte())
            .put(byteArrayOf(0, 0, 3)).putInt(0).put(255.toByte()).put(ByteArray(6))
        val file = CardPlacesFile(CardGeneration.GEN2V2, 112, bytes)
        val places = SavedCardPlaces("places", "own", file, CardPlacesParser.parse(file), 0)
        val authBytes = ByteArray(562)
        ByteBuffer.wrap(authBytes).putShort(0).putInt(1790000015).put(1.toByte())
        val authFile = CardPlacesAuthenticationFile(CardGeneration.GEN2V2, authBytes)
        val auth = SavedCardPlacesAuthentication("auth", "own", authFile, CardPlacesAuthenticationParser.parse(authFile), 0)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardPlacesScreen(PaddingValues(), places, auth) } }
        compose.onNodeWithText("Eintragsart: unbekannt (254)").assertIsDisplayed()
        compose.onNodeWithText("Eintragszeit: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Position: nicht verfügbar").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: authentisiert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Beginn der täglichen Arbeitsperiode").assertDoesNotExist()
    }
}
