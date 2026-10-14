package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.security.MessageDigest
import org.junit.Rule
import org.junit.Test

/** Actual detail composables; mock derived results make late-source races deterministic. */
class CardStoredSignatureMathUiTest {
    @get:Rule val compose = createComposeRule()
    private fun digest(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private fun source(kind: CardIdentitySourceKind): SavedCardIdentitySource {
        val raw = when (kind) {
            CardIdentitySourceKind.IDENTIFICATION -> IdentityUiFixtures.original()
            CardIdentitySourceKind.APPLICATION -> IdentityUiFixtures.application()
            CardIdentitySourceKind.APPLICATION_V2 -> byteArrayOf(0, 8, 0, 1, 0, 1, 0, 1, 0, 0)
        }
        val file = CardIdentitySourceFile(kind, CardGeneration.GEN2V2, raw)
        return SavedCardIdentitySource("source-${kind.name}", "own", file, CardIdentitySourceParser.parse(file), 0)
    }
    private fun certificate() = CardCertificateFile(CardCertificateKind.SIGNING, CardGeneration.GEN2V2,
        InstrumentationRegistry.getInstrumentation().context.assets.open("stored-math/signing-cert.bin").use { it.readBytes() })
    private fun receipt(kind: CardIdentitySourceKind) = CardIdentitySignature(kind, CardGeneration.GEN2V2,
        CardSignatureStatus.CAPTURED_UNVERIFIED, CardSignaturePolicy.NIST_P256, CardSignaturePhase.SIGN,
        statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 64,
        original = source(kind).file, certificate = certificate(), bytes = ByteArray(64) { 7 })
    private fun event(kind: CardEventFaultKind) = CardEventFaultSignature(kind, CardGeneration.GEN1,
        CardSignatureStatus.CAPTURED_UNVERIFIED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SIGN,
        statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 128,
        original = CardEventFaultFile(kind, CardGeneration.GEN1, if (kind == CardEventFaultKind.EVENTS) 6 else 12,
            ByteArray(if (kind == CardEventFaultKind.EVENTS) 864 else 576)), bytes = ByteArray(128) { 7 })
    private fun result(ef: CardSignedEf, generation: CardGeneration, original: ByteArray, bytes: ByteArray,
        status: CardSignatureMathStatus = CardSignatureMathStatus.VALID, certificate: CardCertificateFile? = null) = CardSignatureVerification(
        CardSignatureVerificationSource("own", "first", "batch", ef, generation,
            if (generation == CardGeneration.GEN1) CardApplication.GEN1 else CardApplication.GEN2,
            "receipt", "original", certificate?.let { "certificate" }, null, digest(original), digest(bytes),
            certificate?.bytes?.let(::digest), null), status,
        publicKeyFingerprint = "test-key")

    @Test fun identityEfSessionCardAndReceiptSwitchesNeverBorrowAnEarlierVerdictAndMaskAgain() {
        val sources = CardIdentitySourceKind.entries.associateWith(::source)
        val signatures = CardIdentitySourceKind.entries.associateWith(::receipt)
        val session = mutableStateOf("first"); val card = mutableStateOf("own")
        val selected = mutableStateOf(signatures)
        val results = mutableStateOf(signatures.mapKeys { it.key.signedEf() }.mapValues { (_, sig) ->
            result(sig.kind.signedEf(), sig.generation, sig.original!!.bytes, sig.bytes!!, certificate = sig.certificate)
        })
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), sources, session.value, selected.value,
                driverCardId = card.value, verification = results.value)
        } }
        for (kind in CardIdentitySourceKind.entries) {
            compose.onNodeWithText("${kind.identitySourceTitle()} öffnen").performScrollTo().performClick()
            compose.onNodeWithText("${kind.identitySourceTitle()} · Signaturprüfung: mathematisch gültig").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("${kind.identitySourceTitle()} · Zertifikatsvertrauen: nicht geprüft").performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithText("Karteninhaber und Gültigkeit öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: synthetic-own").assertExists()
        compose.runOnIdle { session.value = "second" }
        compose.onNodeWithText("mathematisch gültig", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Kartennummer: synthetic-own").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer: verborgen").assertExists()
        compose.runOnIdle { session.value = "first"; card.value = "other" }
        compose.onNodeWithText("mathematisch gültig", substring = true).assertDoesNotExist()
        compose.runOnIdle { card.value = "own"; selected.value = emptyMap() }
        compose.onNodeWithText("mathematisch gültig", substring = true).assertDoesNotExist()
        compose.runOnIdle { selected.value = signatures; results.value = results.value.mapValues { it.value.copy(status = CardSignatureMathStatus.INVALID) } }
        compose.onNodeWithText("Karteninhaber und Gültigkeit · Signaturprüfung: mathematisch ungültig").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Karteninhaber und Gültigkeit · Zertifikatsvertrauen: nicht geprüft").assertExists()
    }

    @Test fun eventsAndFaultsKeepTheirOwnSeparateResultsAndArchiveOrUnsignedSelectionHasNoPositiveResult() {
        val receipts = CardEventFaultKind.entries.associateWith(::event)
        val session = mutableStateOf<String?>("first")
        val selected = mutableStateOf(receipts)
        val results = receipts.mapKeys { it.key.signedEf() }.mapValues { (_, sig) ->
            result(sig.kind.signedEf(), sig.generation, sig.original!!.bytes, sig.bytes!!,
                if (sig.kind == CardEventFaultKind.EVENTS) CardSignatureMathStatus.VALID else CardSignatureMathStatus.INVALID)
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardEventFaultScreen(PaddingValues(), null, null, selected.value, session.value, "own", results)
        } }
        compose.onNodeWithText("Ereignisse · Signaturprüfung: mathematisch gültig").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Störungen · Signaturprüfung: mathematisch ungültig").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Störungen · Zertifikatsvertrauen: nicht geprüft").assertExists()
        compose.runOnIdle { session.value = "old"; selected.value = emptyMap() }
        compose.onNodeWithText("mathematisch gültig", substring = true).assertDoesNotExist()
        compose.onNodeWithText("mathematisch ungültig", substring = true).assertDoesNotExist()
        compose.runOnIdle { session.value = null }
        compose.onNodeWithText("Signaturprüfung:", substring = true).assertDoesNotExist()
    }

    @Test fun gen1DoesNotShowAV2VerificationAndARealMissingReceiptHasAnExplicitReason() {
        val kind = CardIdentitySourceKind.IDENTIFICATION
        val raw = source(kind).file.bytes
        val file = CardIdentitySourceFile(kind, CardGeneration.GEN1, raw)
        val sources = mapOf(kind to SavedCardIdentitySource("source", "own", file, CardIdentitySourceParser.parse(file), 0))
        val unavailable = CardSignatureVerification(CardSignatureVerificationSource("own", "first", null,
            kind.signedEf(), CardGeneration.GEN1, CardApplication.GEN1, null, null, null, null, null, null, null, null),
            CardSignatureMathStatus.NOT_CHECKABLE, CardSignatureMathReason.NO_SIGNATURE)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), sources, "first", generation = CardGeneration.GEN1,
                driverCardId = "own", verification = mapOf(kind.signedEf() to unavailable))
        } }
        compose.onNodeWithText("Karteninhaber und Gültigkeit · Signaturprüfung: nicht prüfbar · keine Signatur in dieser Übernahme").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartenanwendung V2 öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Für diese Kartenanwendung nicht vorgesehen.").assertExists()
        compose.onNodeWithText("Signaturprüfung:", substring = true).assertDoesNotExist()
    }
}
