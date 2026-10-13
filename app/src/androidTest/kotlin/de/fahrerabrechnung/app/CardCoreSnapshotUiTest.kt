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

    @Test fun signatureSelectionNeverShowsNewerReceiptInAnUnsignedOrArchivedImport() {
        val raw = CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN1, ByteArray(68))
        val signature = CardCoreSignature(CardCoreEf.ACTIVITY, CardGeneration.GEN1,
            CardSignatureStatus.CAPTURED_UNVERIFIED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SIGN,
            statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 128,
            original = raw, bytes = ByteArray(128) { 7 })
        val selected = mutableStateOf(mapOf(CardCoreEf.ACTIVITY to signature))
        val archive = mutableStateOf(false)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), null, null, { _, _, _ -> }, archive = archive.value,
                coreSignatures = selected.value)
        } }
        compose.onNodeWithText("Tätigkeiten: 128 Bytes · aufgenommen · ungeprüft").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrzeuge: Signatur in diesem Import nicht aufgenommen").assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCoreEf.ACTIVITY to CardCoreSignature(CardCoreEf.ACTIVITY,
            CardGeneration.GEN2V2, CardSignatureStatus.NOT_ATTEMPTED, reason = CardSignatureReason.SIGNING_CERTIFICATE_UNAVAILABLE)) }
        compose.onNodeWithText("Tätigkeiten: nicht versucht · passendes Signaturzertifikat fehlt").assertIsDisplayed()
        compose.onNodeWithText("Tätigkeiten: 128 Bytes · aufgenommen · ungeprüft").assertDoesNotExist()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onNodeWithText("Tätigkeiten: Signatur in diesem Import nicht aufgenommen").assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCoreEf.ACTIVITY to signature); archive.value = true }
        compose.onNodeWithText("Kartensignaturen").assertDoesNotExist()
        compose.onNodeWithText("Tätigkeiten: 128 Bytes · aufgenommen · ungeprüft").assertDoesNotExist()
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
