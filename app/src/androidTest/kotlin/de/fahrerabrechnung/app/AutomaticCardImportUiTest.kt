package de.fahrerabrechnung.app

import android.os.Looper
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import java.nio.ByteBuffer
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** The normal UI has one import, explicit own-card confirmation and read-only history. */
class AutomaticCardImportUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var db: AppDatabase
    private lateinit var activities: CardActivityImportRepository
    private lateinit var vehicles: CardVehicleImportRepository
    private lateinit var pairs: CardSourcePairRepository
    private lateinit var imports: DriverCardImportRepository
    private lateinit var pipeline: DriverCardImportPipeline

    @Before fun prepare() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        val clock = Clock.fixed(Instant.parse("2026-10-02T09:00:00Z"), ZoneOffset.UTC)
        activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        pairs = CardSourcePairRepository(db, activities, vehicles, clock)
        imports = DriverCardImportRepository(db, activities, vehicles, pairs, clock)
        pipeline = DriverCardImportPipeline(db, imports, activities, vehicles, clock)
    }
    @After fun close() { db.close() }
    private fun card(number: String = "fixture-own-1234") = DriverCard(CardGeneration.GEN2V2,
        CardIdentity(number, 13, "Fixture", Instant.EPOCH, Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")),
        DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de"))
    private fun read(card: DriverCard = card(), plate: String = "TEST OWN"): CardReadResult {
        val epoch = Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch).putShort(1).putShort(0).putShort(0).array()
        val record = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2)).putInt(epoch + 3600).putInt(epoch + 7200)
            .put(13.toByte()).put(1.toByte()).put(plate.padEnd(13).toByteArray()).putShort(1).put("WDB12345678901234".toByteArray()).array()
        return CardReadResult(CardRecognition.DRIVER, card.generation, card, CardActivityFile(0, 0, day),
            CardVehiclesFile(0, 1, card.generation, record))
    }
    private fun await(text: String) { compose.waitUntil(10_000) {
        compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    } }
    private fun screen(state: CardImportUiState, confirm: (String) -> Unit = {}, decline: (String) -> Unit = {}, retry: () -> Unit = {}) {
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), state, confirm, decline, retry, {}, {}, {})
        } }
    }

    @Test fun normalSuccessfulImportHasNoManualReadOrSaveControls() {
        val run = runBlocking { imports.bind(card(), true); pipeline.process(read(), "saved") }
        screen(CardImportUiState(CardImportPhase.IMPORTED, lastRun = run))
        compose.onNodeWithTag("card_import_status").assertTextEquals("Fahrerkarte aktualisiert")
        compose.onNodeWithText("1 Kartentag").assertIsDisplayed()
        listOf("Kartenleser prüfen", "Kartendaten testen", "Beide Dateien gemeinsam speichern",
            "Aktivitätsdaten lokal speichern", "Fahrzeugdaten lokal speichern").forEach {
            compose.onNodeWithText(it).assertDoesNotExist()
        }
        compose.onNodeWithText("Teilimporte", substring = true).assertDoesNotExist()
    }

    @Test fun confirmationShowsMaskedNumberAndEmitsOnlyTheCurrentToken() {
        var confirmed: String? = null; var declined: String? = null
        val state = mutableStateOf(CardImportUiState(CardImportPhase.CONFIRM_FIRST_CARD, "nonce", card()))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), state.value, { confirmed = it }, { declined = it }, {}, {}, {}, {})
        } }
        compose.onNodeWithText("••••1234", substring = true).assertIsDisplayed()
        compose.onNodeWithText("fixture-own-1234", substring = true).assertDoesNotExist()
        compose.onNodeWithTag("confirm_automatic_own_card").performClick()
        assertEquals("nonce", confirmed); assertNull(runBlocking { imports.profile() })
        compose.onNodeWithText("Abbrechen").performClick(); assertEquals("nonce", declined)
        compose.runOnIdle { state.value = CardImportUiState(CardImportPhase.IMPORTED) }
        compose.onNodeWithTag("confirm_automatic_own_card").assertDoesNotExist()
    }

    @Test fun foreignReadDoesNotShowOrMixAnEarlierOwnRun() {
        val own = runBlocking { imports.bind(card(), true); pipeline.process(read(), "own") }
        screen(CardImportUiState(CardImportPhase.FOREIGN_CARD, lastRun = own))
        compose.onNodeWithText("Andere Fahrerkarte erkannt").assertIsDisplayed()
        compose.onNodeWithText("1 Kartentag").assertDoesNotExist()
        compose.onNodeWithText("Gespeicherte Daten ansehen").assertDoesNotExist()
        compose.onNodeWithTag("confirm_automatic_own_card").assertDoesNotExist()
        assertEquals(1, runBlocking { pipeline.history().size })
    }

    @Test fun partialImportAndDeniedPermissionOfferClearRecovery() {
        val partial = runBlocking { imports.bind(card(), true)
            pipeline.process(read().copy(vehicles = null, vehicleReadFailed = true), "partial") }
        val state = mutableStateOf(CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = partial))
        var retried = false
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportScreen(PaddingValues(), state.value, {}, {}, { retried = true }, {}, {}, {})
        } }
        compose.onNodeWithTag("card_import_status").assertTextEquals("Einige Daten fehlen")
        compose.onNodeWithText("Aktivitäten und Fahrzeugnutzungen gespeichert").assertDoesNotExist()
        compose.runOnIdle { state.value = CardImportUiState(CardImportPhase.ERROR, readerFailure = ReaderFailure.PERMISSION) }
        compose.onNodeWithText("Der USB-Zugriff wurde nicht freigegeben.", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("retry_card_import").performClick(); assertTrue(retried)
    }

    @Test fun historyKeepsEarlierOwnSessionsAndNeverAddsUnassignedArchiveData() {
        runBlocking {
            pairs.save(read(card("fixture-archive"), "TEST ARCHIVE"))
            imports.bind(card(), true)
            imports.save(read(), "old-own", CardImportSelection.PAIR)
            pipeline.process(read().copy(vehicles = null, vehicleReadFailed = true), "new-partial")
        }
        var archiveOpened = false
        val opened = AtomicReference<Pair<String, Boolean>?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportHistoryScreen(PaddingValues(), pipeline, imports,
                { opened.set(it.id to (Looper.myLooper() == Looper.getMainLooper())) }, { archiveOpened = true })
        } }
        await("Früher gespeicherter Lesevorgang")
        compose.onNodeWithText("Fahrzeugnutzungen: Lesen fehlgeschlagen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("fixture-own-1234", substring = true).assertDoesNotExist()
        compose.onNodeWithText("fixture-archive", substring = true).assertDoesNotExist()
        compose.onNodeWithTag("open_import_new-partial").performScrollTo().performClick()
        compose.waitUntil(10_000) { opened.get() != null }
        assertEquals("new-partial" to true, opened.get())
        compose.onNodeWithText("Früheres Dateiarchiv").performScrollTo().performClick()
        assertTrue(archiveOpened)
        assertEquals(1, runBlocking { db.driverProfileDao().cards(imports.profile()!!.id).size })
    }

    @Test fun normalNavigationOpensExactSessionAndRetainsFiveMainTabs() {
        val run = runBlocking { imports.bind(card(), true); pipeline.process(read(), "saved") }
        val state = mutableStateOf(CardImportUiState(lastRun = run))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = state.value)
        } }
        compose.onNodeWithTag("nav_start").assertIsDisplayed()
        compose.runOnIdle { state.value = state.value.copy(phase = CardImportPhase.USB_PERMISSION_REQUIRED) }
        await("USB-Zugriff bestätigen")
        compose.onNodeWithTag("card_import_status").assertTextEquals("USB-Zugriff bestätigen")
        compose.runOnIdle { state.value = state.value.copy(phase = CardImportPhase.WAITING_FOR_CARD) }
        await("Fahrerkarte einlegen")
        compose.onNodeWithTag("card_import_status").assertTextEquals("Fahrerkarte einlegen")
        compose.runOnIdle { state.value = state.value.copy(phase = CardImportPhase.IMPORTED) }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        await("Kartentage: 1 gespeichert")
        compose.onNodeWithTag("open_import_saved").performScrollTo().performClick()
        await("TEST OWN · Staat 13")
        compose.onNodeWithText("Kartenleser prüfen").assertDoesNotExist()
        compose.onNodeWithText("Fahrzeughistorie").performScrollTo().performClick()
        await("TEST OWN")
        compose.onNodeWithText("TEST OWN", substring = true).performScrollTo().assertIsDisplayed()
        assertEquals(listOf("saved"), runBlocking { imports.all().map { it.id } })
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("back").performClick()
        listOf("start", "calendar", "analytics", "payroll", "settings").forEach {
            compose.onNodeWithTag("nav_$it").assertExists()
        }
        compose.onNodeWithTag("nav_vehicles").assertDoesNotExist()
    }
    private fun controlFile(plate: String = "TEST FIRST", number: String = "TEST CONTROL0001", time: Int = 1790000000) =
        CardControlFile(CardGeneration.GEN2V2, ByteBuffer.allocate(46).put(0xF8.toByte()).putInt(time)
            .put(3.toByte()).put(13.toByte()).put(number.padEnd(16).toByteArray())
            .put(13.toByte()).put(1.toByte()).put(plate.padEnd(13).toByteArray())
            .putInt(time - 200).putInt(time - 100).array())

    @Test fun controlsShowRecordedFactsAndMaskTheControlCardNumberUntilRequested() {
        val file = controlFile()
        val saved = SavedCardControl("fixture-source", "physical-own", file, CardControlParser.parse(file), 0)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardControlScreen(PaddingValues(), saved) } }
        compose.onNodeWithText("Fahrerkarte heruntergeladen").assertIsDisplayed()
        compose.onNodeWithText("Kalibrierungsparameter geprüft").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST CONTROL0001", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Kontrollkarte: ••••0001").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kontrollkarte: TEST CONTROL0001").assertIsDisplayed()
        compose.onNodeWithText("Kartennummer verbergen").performScrollTo().performClick()
        compose.onNodeWithText("TEST CONTROL0001", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
    }

    @Test fun missingControlSourceAndAReadUnusedSourceHaveDifferentHonestMessages() {
        val state = mutableStateOf<SavedCardControl?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardControlScreen(PaddingValues(), state.value) } }
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Kontrolldaten verfügbar.").assertIsDisplayed()
        compose.runOnIdle { state.value = SavedCardControl("unused", "own", CardControlFile(CardGeneration.GEN2V2, ByteArray(46)), null, 0) }
        compose.onNodeWithText("In dieser Kartendatei ist keine Kontrolle aufgezeichnet.").assertIsDisplayed()
        compose.onNodeWithText("Letzte aufgezeichnete Kontrolle").assertDoesNotExist()
        compose.onNodeWithText("Kartennummer anzeigen").assertDoesNotExist()
    }

    @Test fun controlNavigationOpensOnlyTheSelectedSessionAndKeepsItsOldSource() {
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read().copy(controls = controlFile()), "first-control")
            pipeline.process(read().copy(controls = controlFile("TEST LATEST", time = 1790000100)), "latest-control")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_first-control").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("open_import_first-control").performScrollTo().performClick()
        await("Fahrerkarten-Details")
        compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
        compose.onNodeWithText("Kontrollen").performClick()
        await("Fahrzeug: TEST FIRST · Staat 13")
        compose.onNodeWithText("TEST LATEST", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        compose.onNodeWithTag("open_import_latest-control").performScrollTo().performClick()
        await("Fahrerkarten-Details")
        compose.onNodeWithText("Fahrerkarten-Details").performClick()
        compose.onNodeWithText("Kontrollen").performClick()
        await("Fahrzeug: TEST LATEST · Staat 13")
        compose.onNodeWithText("TEST FIRST", substring = true).assertDoesNotExist()
        assertEquals(2, runBlocking { pipeline.history().size })
    }

    @Test fun archivePreviewDoesNotOfferProfileCardDetailsOrControls() {
        var opened = false
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            SavedCardPreviewScreen(PaddingValues(), null, null, { _, _, _ -> }, archive = true, onOpenDetails = { opened = true })
        } }
        compose.onNodeWithText("Fahrerkarten-Details").assertDoesNotExist()
        compose.onNodeWithText("Kontrollen").assertDoesNotExist()
        compose.onNodeWithText("Diese Dateivorschau ordnet ältere Daten keinem Fahrerprofil zu.").assertIsDisplayed()
        assertFalse(opened)
    }

    @Test fun controlOnlySavedDataShowsAPartialStartStatusWithoutProminentControlDetails() {
        val run = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read().copy(activity = null, vehicles = null, activityReadFailed = true,
                vehicleReadFailed = true, controls = controlFile()), "only-control")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(lastRun = run))
        } }
        await("Kartendaten teilweise verfügbar")
        compose.onNodeWithText("Noch keine Kartendaten gespeichert").assertDoesNotExist()
        compose.onNodeWithText("Letzte aufgezeichnete Kontrolle").assertDoesNotExist()
        compose.onNodeWithText("Kontrollen").assertDoesNotExist()
        compose.onNodeWithTag("nav_start").assertIsDisplayed()
    }

}
