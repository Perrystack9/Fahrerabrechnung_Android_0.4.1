package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.nio.ByteBuffer
import java.time.Instant
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

internal object IdentityUiFixtures {
    fun card(number: String = "synthetic-own") = DriverCard(CardGeneration.GEN2V2,
        CardIdentity(number, 13, "Fixture", Instant.EPOCH, Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")),
        DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de"))
    fun original(own: DriverCard = card()): ByteArray {
        val data = ByteBuffer.allocate(143).put(own.card.issuingState.toByte()).put(own.card.number.padEnd(16).toByteArray())
        fun name(value: String) { data.put(1); data.put(value.padEnd(35).toByteArray()) }
        name(own.card.issuingAuthority); data.putInt(own.card.issued.epochSecond.toInt())
            .putInt(own.card.validFrom.epochSecond.toInt()).putInt(own.card.expires.epochSecond.toInt())
        name(own.holder.surname); name(own.holder.givenNames)
        data.put(own.holder.birthDate?.toString()?.replace("-", "")?.chunked(2)?.map { it.toInt(16).toByte() }?.toByteArray() ?: ByteArray(4))
        return data.put(own.holder.language.toByteArray()).array()
    }
    fun application(capacity: Int = 2, legacy: Boolean = false): ByteArray = ByteBuffer.allocate(if (legacy) 15 else 17)
        .put(1).put(1).put(1).put(12).put(24).putShort(16).putShort(capacity.toShort())
        .putShort(112).putShort(336).putShort(112).apply { if (!legacy) putShort(200) }.array()
    fun source(kind: CardIdentitySourceKind = CardIdentitySourceKind.IDENTIFICATION, id: String = "synthetic-original",
        raw: ByteArray = original()): SavedCardIdentitySource {
        val file = CardIdentitySourceFile(kind, CardGeneration.GEN2V2, raw)
        return SavedCardIdentitySource(id, "own", file, CardIdentitySourceParser.parse(file), 0)
    }
}

class CardIdentitySourceUiTest {
    @get:Rule val compose = createComposeRule()
    private val full = "Kartennummer: synthetic-own"
    @Test fun originalHolderAndValidityShowWhileTheCardNumberStartsHidden() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), mapOf(CardIdentitySourceKind.IDENTIFICATION to IdentityUiFixtures.source()))
        } }
        compose.onNodeWithText("Dateigröße: 143 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Karteninhaber: TEST PERSON ALPHA").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Geburtsdatum: 29.02.2000").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(full).assertDoesNotExist()
        compose.onNodeWithText("Ausgestellt laut Karte: nicht angegeben").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(full).performScrollTo().assertIsDisplayed()
    }
    @Test fun choosingANewOriginalSnapshotMasksTheNumberAgain() {
        val selected = mutableStateOf(mapOf(CardIdentitySourceKind.IDENTIFICATION to IdentityUiFixtures.source()))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardIdentitySourceScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(full).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardIdentitySourceKind.IDENTIFICATION to IdentityUiFixtures.source(id = "next-original")) }
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(full).assertDoesNotExist()
    }
    @Test fun changingKindOrLeavingAndReopeningMasksTheNumberAgain() {
        val visible = mutableStateOf(true)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            if (visible.value) CardIdentitySourceScreen(PaddingValues(), mapOf(CardIdentitySourceKind.IDENTIFICATION to IdentityUiFixtures.source()))
            else Text("Other view")
        } }
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText(full).assertDoesNotExist()
        compose.onNodeWithText("Karteninhaber und Gültigkeit öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.runOnIdle { visible.value = false }; compose.runOnIdle { visible.value = true }
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(full).assertDoesNotExist()
    }
    @Test fun missingOriginalIdentityNeverUsesTheProfileOrLaterSnapshotAndBlankV2IsDistinct() {
        val selected = mutableStateOf<Map<CardIdentitySourceKind, SavedCardIdentitySource>>(emptyMap())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardIdentitySourceScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Karteninhaber:", substring = true).assertDoesNotExist()
        compose.runOnIdle { selected.value = mapOf(CardIdentitySourceKind.APPLICATION_V2 to IdentityUiFixtures.source(
            CardIdentitySourceKind.APPLICATION_V2, raw = ByteArray(10))) }
        compose.onNodeWithText("Kartenanwendung V2 öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Diese gespeicherte Originaldatei ist leer.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").assertDoesNotExist()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onNodeWithText("Diese gespeicherte Originaldatei ist leer.").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
    }
    @Test fun changingReceiptMasksTheNumberEvenWhenOriginalSourceIsDeduplicated() {
        val receipt = mutableStateOf("first")
        val source = mapOf(CardIdentitySourceKind.IDENTIFICATION to IdentityUiFixtures.source())
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), source, receipt.value)
        } }
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText(full).performScrollTo().assertIsDisplayed()
        compose.runOnIdle { receipt.value = "second" }
        compose.onNodeWithText(full).assertDoesNotExist()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
    }
    @Test fun originalApplicationDisplaysAllRecordedCapacitiesIncludingUnsignedMaximum() {
        val raw = IdentityUiFixtures.application().also {
            it[3] = 255.toByte(); it[4] = 128.toByte(); it[13] = 255.toByte(); it[14] = 255.toByte()
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), mapOf(CardIdentitySourceKind.APPLICATION to IdentityUiFixtures.source(
                CardIdentitySourceKind.APPLICATION, raw = raw)))
        } }
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Speicher für Ereignisse je Typ: 255").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicher für Störungen je Typ: 128").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für besondere Bedingungen: 65535").performScrollTo().assertIsDisplayed()
    }
    @Test fun capacitiesAreLabeledAsStorageAndLegacyApplicationHasNoInventedVehicleUnitField() {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), mapOf(CardIdentitySourceKind.APPLICATION to IdentityUiFixtures.source(
                CardIdentitySourceKind.APPLICATION, raw = IdentityUiFixtures.application(legacy = true))))
        } }
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Dateigröße: 15 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für Fahrzeugnutzungen: 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für Fahrtenschreiber-Nutzungen:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Diese Angaben beschreiben die aufgezeichnete Speicherkapazität, nicht die Anzahl übernommener Einträge.").performScrollTo().assertIsDisplayed()
    }
    @Test fun v2DisplaysEachIndependentCapacityWithoutDrivingOrWorkingTimeClaims() {
        val raw = ByteBuffer.allocate(10).putShort(8).putShort(112).putShort(336).putShort(84).putShort(0).array()
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardIdentitySourceScreen(PaddingValues(), mapOf(CardIdentitySourceKind.APPLICATION_V2 to IdentityUiFixtures.source(
                CardIdentitySourceKind.APPLICATION_V2, raw = raw)))
        } }
        compose.onNodeWithText("Kartenanwendung V2 öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Speicherplätze für Grenz- und Ländereinträge: 112").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für Be- und Entladeeinträge: 336").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für Ladungsart-Einträge: 84").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicher für Fahrtenschreiberkonfiguration: 0 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").assertDoesNotExist()
    }
}
