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

class CardEventFaultSignatureUiTest {
    @get:Rule val compose = createComposeRule()
    private fun signature(kind: CardEventFaultKind): CardEventFaultSignature {
        val count = if (kind == CardEventFaultKind.EVENTS) 12 else 24
        val raw = CardEventFaultFile(kind, CardGeneration.GEN1, count,
            ByteArray(CardEventFaultParser.fileLength(kind, CardGeneration.GEN1, count)))
        return CardEventFaultSignature(kind, CardGeneration.GEN1, CardSignatureStatus.CAPTURED_UNVERIFIED,
            CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SIGN, statusWord = 0x9000, hashStatusWord = 0x9000,
            signStatusWord = 0x9000, actualLength = 128, original = raw, bytes = ByteArray(128) { 7 })
    }
    @Test fun selectedReceiptChangesRemoveOldSignatureAndRawSizeFromBothViews() {
        val current = mutableStateOf(true)
        val signatures = CardEventFaultKind.entries.associateWith(::signature)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardEventFaultScreen(PaddingValues(), null, null, if (current.value) signatures else emptyMap())
        } }
        compose.onNodeWithText("Ereignisse · Original: 1728 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ereignisse · Kartensignatur: 128 Bytes · aufgenommen · ungeprüft").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Störungen · Original: 1152 Bytes").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { current.value = false }
        compose.onNodeWithText("Ereignisse · Signatur in diesem Import nicht aufgenommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Störungen · Signatur in diesem Import nicht aufgenommen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("aufgenommen · ungeprüft", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Original:", substring = true).assertDoesNotExist()
    }
    @Test fun failedReadAndAbsentFileShowSeparateReasonsWithoutInventedRawBytes() {
        val receipts = mapOf(
            CardEventFaultKind.EVENTS to CardEventFaultSignature(CardEventFaultKind.EVENTS, CardGeneration.GEN1,
                CardSignatureStatus.FAILED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.READ, CardSignatureReason.INCOMPLETE_READ),
            CardEventFaultKind.FAULTS to CardEventFaultSignature(CardEventFaultKind.FAULTS, CardGeneration.GEN1,
                CardSignatureStatus.NOT_ATTEMPTED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SELECT, CardSignatureReason.EF_ABSENT))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardEventFaultScreen(PaddingValues(), null, null, receipts) } }
        compose.onNodeWithText("Ereignisse · Aufnahme fehlgeschlagen · Datei nicht vollständig gelesen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Störungen · nicht versucht · Datei nicht vorhanden").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Original:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("aufgenommen · ungeprüft", substring = true).assertDoesNotExist()
    }
}
