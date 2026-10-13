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

class CardCoreSnapshotUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun changingSelectionRemovesNewerOriginalClaimsAndKeepsGenerationAndLengthExact() {
        val first = SavedCardCoreSnapshot("synthetic-first", "own",
            CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN1, ByteArray(68)), 0)
        val second = SavedCardCoreSnapshot("synthetic-second", "own",
            CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN2V2, ByteArray(132)), 1)
        val selected = mutableStateOf(mapOf(CardCoreEf.ACTIVITY to first))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), null, null, { _, _, _ -> }, coreSnapshots = selected.value)
        } }
        compose.onNodeWithText("Tätigkeiten: 68 Bytes · GEN1").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrzeuge: keine Originaldatei in dieser Übernahme").assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCoreEf.ACTIVITY to second) }
        compose.onNodeWithText("Tätigkeiten: 132 Bytes · GEN2V2").assertIsDisplayed()
        compose.onNodeWithText("Tätigkeiten: 68 Bytes · GEN1").assertDoesNotExist()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onNodeWithText("Tätigkeiten: keine Originaldatei in dieser Übernahme").assertIsDisplayed()
        compose.onNodeWithText("Tätigkeiten: 132 Bytes · GEN2V2").assertDoesNotExist()
        compose.onNodeWithText("Für diese Auswahl sind keine auswertbaren Kartendaten verfügbar.").assertIsDisplayed()
    }

    @Test fun rawOnlyImportShowsOriginalsWithoutInventingAnyParsedDayOrVehicle() {
        val run = CardImportRun("raw-only", "own", "synthetic-own", CardGeneration.GEN2V2,
            CardImportRunStatus.PARTIAL, 0, 0, "raw-only", 2, null, listOf(
                CardImportFileResult(CardImportFileKind.ACTIVITY_ORIGINAL, CardImportFileStatus.STORED, 0, null, null, null),
                CardImportFileResult(CardImportFileKind.VEHICLES_ORIGINAL, CardImportFileStatus.STORED, 0, null, null, null)))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = run), {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Originaldatei der Tätigkeiten gespeichert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Originaldatei der Fahrzeuge gespeichert").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 Kartentag").assertDoesNotExist()
        compose.onNodeWithText("Fahrzeughistorie").assertDoesNotExist()
    }
}
