package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

internal object OriginalUiFixtures {
    fun application(generation: CardGeneration = CardGeneration.GEN2V2, capacity: Int = 13780, legacy: Boolean = false) =
        ByteArray(if (generation == CardGeneration.GEN1) 10 else if (legacy) 15 else 17).also {
            it[0] = 1; it[1] = 1; it[2] = if (generation == CardGeneration.GEN2V2) 1 else 0
            ByteBuffer.wrap(it).putShort(5, capacity.toShort())
        }
    fun identity(): ByteArray = ByteArrayOutputStream().apply {
        fun name(value: String) { write(1); write(value.padEnd(35).toByteArray()) }
        write(13); write("TESTCARD12345678".toByteArray()); name("Fixture")
        write(ByteBuffer.allocate(12).putInt(0).putInt(0).putInt(1893456000).array())
        name("ALPHA"); name("TEST PERSON"); write(byteArrayOf(0x20, 0, 2, 0x29)); write("de".toByteArray())
    }.toByteArray()
    fun source(kind: CardOriginalFileKind = CardOriginalFileKind.APPLICATION_IDENTIFICATION,
        generation: CardGeneration = CardGeneration.GEN2V2, id: String = "original-ui", bytes: ByteArray = application(generation)): SavedCardOriginalFile {
        val file = CardOriginalFile(kind, generation, bytes)
        return SavedCardOriginalFile(id, "own", file, CardOriginalFileParser.parse(file), 0)
    }
}

class CardOriginalFilesUiTest {
    @get:Rule val compose = createComposeRule()
    private fun screen(sources: Map<CardOriginalFileKind, SavedCardOriginalFile>) {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardOriginalFilesScreen(PaddingValues(), sources) } }
    }
    @Test fun applicationCapacitiesAreLabelledAsRecordedStorageAndNotActualActivityCounts() {
        screen(mapOf(CardOriginalFileKind.APPLICATION_IDENTIFICATION to OriginalUiFixtures.source(bytes = OriginalUiFixtures.application(capacity = 65535))))
        compose.onNodeWithText("Dateigröße: 17 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Aktivitätsspeicher: 65535 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherkapazitäten laut Datei; keine Anzahl tatsächlicher Einträge.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartengeneration: Generation 2, Version 2").performScrollTo().assertIsDisplayed()
    }
    @Test fun versionTwoConfigurationCapacityRemainsARecordedSizeIncludingZeroValues() {
        screen(mapOf(CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2 to OriginalUiFixtures.source(
            CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2, bytes = byteArrayOf(0, 8, 0, 0, 0, 0, 0, 0, 255.toByte(), 255.toByte()))))
        compose.onNodeWithText("Anwendungsdatei Version 2 öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Grenzübertrittsplätze: 0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicher für Fahrtenschreiberkonfiguration: 65535 Bytes").performScrollTo().assertIsDisplayed()
    }
    @Test fun legacyApplicationDoesNotBorrowAbsentVehicleUnitCapacity() {
        screen(mapOf(CardOriginalFileKind.APPLICATION_IDENTIFICATION to OriginalUiFixtures.source(generation = CardGeneration.GEN2,
            bytes = OriginalUiFixtures.application(CardGeneration.GEN2, legacy = true))))
        compose.onNodeWithText("Dateigröße: 15 Bytes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Fahrtenschreiberplätze: in diesem Dateiformat nicht angegeben").performScrollTo().assertIsDisplayed()
    }
    @Test fun missingOlderReceiptClearsOriginalInformationInsteadOfUsingNewerFiles() {
        val selected = mutableStateOf(mapOf(CardOriginalFileKind.APPLICATION_IDENTIFICATION to OriginalUiFixtures.source()))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardOriginalFilesScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Aktivitätsspeicher: 13780 Bytes").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = emptyMap() }
        compose.onNodeWithText("Für diese Übernahme ist diese Originaldatei nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Aktivitätsspeicher: 13780 Bytes").assertDoesNotExist()
    }
    @Test fun identificationNumberIsInitiallyMaskedAndHiddenAgainOnSourceOrKindChange() {
        val selected = mutableStateOf(mapOf(CardOriginalFileKind.IDENTIFICATION to OriginalUiFixtures.source(
            CardOriginalFileKind.IDENTIFICATION, bytes = OriginalUiFixtures.identity())))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardOriginalFilesScreen(PaddingValues(), selected.value) } }
        compose.onNodeWithText("Identifikationsdatei öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { selected.value = mapOf(CardOriginalFileKind.IDENTIFICATION to OriginalUiFixtures.source(
            CardOriginalFileKind.IDENTIFICATION, id = "next", bytes = OriginalUiFixtures.identity())) }
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Anwendungsdatei öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Identifikationsdatei öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").assertDoesNotExist()
    }
    @Test fun changingReceiptHidesNumberEvenWhenItsDeduplicatedSourceIdIsUnchanged() {
        val receipt = mutableStateOf("first")
        val sources = mapOf(CardOriginalFileKind.IDENTIFICATION to OriginalUiFixtures.source(
            CardOriginalFileKind.IDENTIFICATION, bytes = OriginalUiFixtures.identity()))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardOriginalFilesScreen(PaddingValues(), sources, receipt.value) } }
        compose.onNodeWithText("Identifikationsdatei öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { receipt.value = "repeat" }
        compose.onNodeWithText("Kartennummer: TESTCARD12345678").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().assertIsDisplayed()
    }
    @Test fun detailsExposeTheOriginalFilesThroughOneReadOnlyNavigationAction() {
        var opened = 0
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardDetailsScreen(PaddingValues(), {}, {}, onOriginalFiles = { opened++ })
        } }
        compose.onNodeWithText("Anwendungs- und Identifikationsdateien").performScrollTo().performClick()
        assertEquals(1, opened)
    }
}
