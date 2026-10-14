package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.security.MessageDigest
import org.junit.Rule
import org.junit.Test

class CardCoreMathUiTest {
    @get:Rule val compose = createComposeRule()
    private fun receipt() = CardCoreSignature(CardCoreEf.ACTIVITY, CardGeneration.GEN1,
        CardSignatureStatus.CAPTURED_UNVERIFIED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SIGN,
        statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 128,
        original = CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN1, ByteArray(68)), bytes = ByteArray(128) { 7 })
    private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun result(receipt: CardCoreSignature) = CardCoreSignatureVerification(
        CardCoreVerificationSource("own", "first", "batch", receipt.kind, receipt.generation, CardApplication.GEN1,
            "exact-receipt", "exact-original", null, null, digest(receipt.original!!.bytes), digest(receipt.bytes!!), null, null),
        CardSignatureMathStatus.VALID, publicKeyFingerprint = "test-key")

    @Test fun resultIsRemovedImmediatelyWhenSessionCardOrReceiptChangesAndTrustStaysSeparate() {
        val signature = receipt()
        val session = mutableStateOf("first"); val card = mutableStateOf("own")
        val selected = mutableStateOf(mapOf(CardCoreEf.ACTIVITY to signature))
        val results = mutableStateOf(mapOf(CardCoreEf.ACTIVITY to result(signature)))
        val archive = mutableStateOf(false)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), null, null, { _, _, _ -> }, archive = archive.value,
                receiptId = session.value, driverCardId = card.value, coreSignatures = selected.value, coreVerification = results.value)
        } }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch gültig").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Tätigkeiten · Zertifikatsvertrauen: nicht geprüft").assertIsDisplayed()
        compose.runOnIdle { session.value = "second" }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch gültig").assertDoesNotExist()
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: wird geprüft").assertIsDisplayed()
        compose.runOnIdle { session.value = "first"; card.value = "other" }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch gültig").assertDoesNotExist()
        compose.runOnIdle { card.value = "own"; selected.value = emptyMap() }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch gültig").assertDoesNotExist()
        compose.runOnIdle {
            results.value = mapOf(CardCoreEf.ACTIVITY to CardCoreSignatureVerification(result(signature).source.copy(
                receiptFingerprint = null, signatureDigest = null, certificateDigest = null), CardSignatureMathStatus.NOT_CHECKABLE,
                CardSignatureMathReason.NO_SIGNATURE))
        }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: nicht prüfbar · keine Signatur in dieser Übernahme").assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardCoreEf.ACTIVITY to signature); results.value = mapOf(CardCoreEf.ACTIVITY to
            result(signature).copy(status = CardSignatureMathStatus.INVALID)) }
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch ungültig").assertIsDisplayed()
        compose.onNodeWithText("Tätigkeiten · Zertifikatsvertrauen: nicht geprüft").assertIsDisplayed()
        compose.runOnIdle { archive.value = true }
        compose.onNodeWithText("Kartensignaturen").assertDoesNotExist()
        compose.onNodeWithText("Tätigkeiten · Signaturprüfung: mathematisch ungültig").assertDoesNotExist()
    }
}
