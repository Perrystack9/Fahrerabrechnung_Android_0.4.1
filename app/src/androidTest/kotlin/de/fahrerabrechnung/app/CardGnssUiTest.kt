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

class CardGnssUiTest {
    @get:Rule val compose = createComposeRule()
    private fun empty(): SavedCardGnssPositions {
        val file = CardGnssAccumulatedDrivingFile(CardGeneration.GEN2V2, 336, ByteArray(6050))
        return SavedCardGnssPositions("empty", "own", file, CardGnssAccumulatedDrivingParser.parse(file), 0)
    }
    @Test fun missingAndValidEmptyAreDifferentAndSelectingOldSourceClearsNewPoints() {
        val source = mutableStateOf<SavedCardGnssPositions?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardGnssScreen(PaddingValues(), source.value, null) } }
        compose.onNodeWithText("Für diese Übernahme ist keine gespeicherte GNSS-Punktdatei verfügbar.").assertIsDisplayed()
        compose.runOnIdle { source.value = empty() }
        compose.onNodeWithText("In dieser GNSS-Punktdatei sind keine Aufzeichnungen enthalten.").assertIsDisplayed()
        compose.runOnIdle { source.value = null }
        compose.onNodeWithText("In dieser GNSS-Punktdatei sind keine Aufzeichnungen enthalten.").assertDoesNotExist()
    }
    @Test fun unknownPositionAndReservedStatusRemainHonestIndependentRecords() {
        val bytes = ByteArray(6050)
        ByteBuffer.wrap(bytes).putShort(0).putInt(1790000015).putInt(0).put(255.toByte())
            .put(byteArrayOf(0x7f, 0xff.toByte(), 0xff.toByte(), 0, 0, 2, 0, 0, 3))
        val file = CardGnssAccumulatedDrivingFile(CardGeneration.GEN2V2, 336, bytes)
        val positions = SavedCardGnssPositions("position", "own", file, CardGnssAccumulatedDrivingParser.parse(file), 0)
        val authBytes = ByteArray(1682)
        ByteBuffer.wrap(authBytes).putShort(0).putInt(0).put(254.toByte())
        val authFile = CardGnssAuthenticationFile(CardGeneration.GEN2V2, authBytes)
        val auth = SavedCardGnssAuthentication("auth", "own", authFile, CardGnssAuthenticationParser.parse(authFile), 0)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardGnssScreen(PaddingValues(), positions, auth) } }
        compose.onNodeWithText("Positionszeit: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Position: nicht verfügbar").assertIsDisplayed()
        compose.onNodeWithText("HDOP: nicht angegeben").assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberstatus: unbekannt (254)").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Breite", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Fahrtenschreiberstatus: authentisiert").assertDoesNotExist()
    }
}
