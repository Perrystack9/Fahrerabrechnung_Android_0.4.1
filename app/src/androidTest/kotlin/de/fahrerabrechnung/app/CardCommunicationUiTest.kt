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

internal object CommunicationUiFixtures {
    fun sources(empty: Boolean = false, code: Int = 0x11): Map<CardCommunicationKind, SavedCardCommunication> =
        CardCommunicationKind.entries.associateWith { kind ->
            val raw = if (empty) ByteArray(kind.minimumLength) else when (kind) {
                CardCommunicationKind.ATR_INFO -> byteArrayOf(0x7F, 0x66, 4, 2, 2, 1, 0)
                CardCommunicationKind.EXTENDED_LENGTH -> byteArrayOf(2, 1, code.toByte())
            }
            val file = CardCommunicationFile(kind, CardGeneration.GEN2V2, raw)
            SavedCardCommunication("synthetic-${kind.name}-$empty-$code", "own", file, CardCommunicationParser.parse(file), 0)
        }
    fun read() = sources().mapValues { (_, source) -> CardCommunicationRead(source.file) }
}

class CardCommunicationUiTest {
    @get:Rule val compose = createComposeRule()
    @Test fun bothStoredFilesShowOriginalLengthsAndProtocolClaimsWithoutNegotiatedBufferSizes() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardCommunicationScreen(PaddingValues(), CommunicationUiFixtures.sources()) } }
        compose.onNodeWithText("Dateigröße: 7 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Die Karte hat Pufferinformationen aufgezeichnet.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Erweiterte Länge für T=0: aufgezeichnet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Erweiterte Länge für T=1: aufgezeichnet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Dateigröße: 3 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("4096", substring = true).assertDoesNotExist()
    }
    @Test fun changingSelectedSourceSeparatesStoredEmptyDataFromMissingHistory() {
        val selected = mutableStateOf(CommunicationUiFixtures.sources())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardCommunicationScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Erweiterte Länge für T=1: aufgezeichnet").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = CommunicationUiFixtures.sources(empty = true) }
        compose.onAllNodesWithText("Diese gespeicherte Kartendatei ist leer.").assertCountEquals(2)
        compose.onNodeWithText("Erweiterte Länge für T=1: aufgezeichnet").assertDoesNotExist()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onAllNodesWithText("Für diese Übernahme ist diese bedingte Kartenangabe nicht gespeichert.").assertCountEquals(2)
        compose.onAllNodesWithText("Diese gespeicherte Kartendatei ist leer.").assertCountEquals(0)
        compose.onAllNodesWithText("Dateigröße:", substring = true).assertCountEquals(0)
    }
    @Test fun oneMissingFileDoesNotHideTheOtherStoredSource() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardCommunicationScreen(PaddingValues(), CommunicationUiFixtures.sources() - CardCommunicationKind.ATR_INFO)
        } }
        compose.onNodeWithText("Für diese Übernahme ist diese bedingte Kartenangabe nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Erweiterte Länge für T=0: aufgezeichnet").performScrollTo().assertIsDisplayed()
    }
    @Test fun unknownProtocolCodeHasNoInventedT0OrT1Support() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardCommunicationScreen(PaddingValues(), CommunicationUiFixtures.sources(code = 0xFF)) } }
        compose.onNodeWithText("Erweiterte Länge für T=0: nicht zugeordnet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Erweiterte Länge für T=1: nicht zugeordnet").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Unbekannter Protokollcode: Kartenwert 255").performScrollTo().assertIsDisplayed()
    }
    @Test fun communicationOnlyImportDisplaysIndependentStoredSourcesInNormalImportUi() {
        val run = CardImportRun("comm-only", "own", "synthetic-own", CardGeneration.GEN2V2,
            CardImportRunStatus.PARTIAL, 0, 0, "comm-only", 2, null, listOf(
                CardImportFileResult(CardImportFileKind.MF_ATR_INFO, CardImportFileStatus.STORED, 1, null, null, null),
                CardImportFileResult(CardImportFileKind.MF_EXTENDED_LENGTH, CardImportFileStatus.STORED, 1, null, null, null)))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = run), {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Pufferinformation übernommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Protokollunterstützung übernommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Gespeicherte Daten ansehen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 Kartentag").assertDoesNotExist()
    }
}
