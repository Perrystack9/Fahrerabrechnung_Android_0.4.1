package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import org.junit.Rule
import org.junit.Test

internal object DirectoryUiFixtures {
    fun bytes() = byteArrayOf(0x61, 8, 0x4F, 6, 0xFF.toByte(), 0x54, 0x41, 0x43, 0x48, 0x4F,
        0x61, 8, 0x4F, 6, 0xFF.toByte(), 0x53, 0x4D, 0x52, 0x44, 0x54)
    fun source(empty: Boolean = false): SavedCardDirectory {
        val file = CardDirectoryFile(CardGeneration.GEN2V2, if (empty) ByteArray(20) else bytes())
        return SavedCardDirectory(if (empty) "synthetic-empty-dir" else "synthetic-dir", "own", file,
            CardDirectoryParser.parse(file), 0)
    }
}

class CardDirectoryUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun storedDirectoryShowsBothRecordedApplicationsAndItsExactByteLength() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardDirectoryScreen(PaddingValues(), DirectoryUiFixtures.source())
        } }
        compose.onNodeWithText("Dateigröße: 20 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiber Generation 1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiber Generation 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Generation 2 V2", substring = true).assertDoesNotExist()
    }
    @Test fun changingSelectedSourceKeepsACompleteEmptyDirectoryDistinctFromMissingHistory() {
        val selected = mutableStateOf<SavedCardDirectory?>(DirectoryUiFixtures.source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardDirectoryScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Fahrtenschreiber Generation 2").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = DirectoryUiFixtures.source(empty = true) }
        compose.onNodeWithText("Dieses gespeicherte Anwendungsverzeichnis ist leer.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiber Generation 2").assertDoesNotExist()
        compose.runOnIdle { selected.value = null }
        compose.onNodeWithText("Für diese Übernahme ist kein Anwendungsverzeichnis gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dieses gespeicherte Anwendungsverzeichnis ist leer.").assertDoesNotExist()
        compose.onNodeWithText("Dateigröße:", substring = true).assertDoesNotExist()
    }
    @Test fun directoryOnlyImportIsListedAsAnIndependentStoredSourceInTheNormalImportUi() {
        val run = CardImportRun("dir-only", "own", "synthetic-own", CardGeneration.GEN2V2,
            CardImportRunStatus.PARTIAL, 0, 0, "dir-only", 1, null, listOf(
                CardImportFileResult(CardImportFileKind.MF_DIRECTORY, CardImportFileStatus.STORED, 2, null, null, null)))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = run), {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Anwendungsverzeichnis übernommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Gespeicherte Daten ansehen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("2 Kartentage").assertDoesNotExist()
        compose.onNodeWithText("2 Fahrzeugnutzungen").assertDoesNotExist()
    }
}
