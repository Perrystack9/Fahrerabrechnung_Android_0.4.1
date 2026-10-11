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
import java.time.ZoneId
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
        val run = runBlocking { imports.bind(card(), true); pipeline.process(read().copy(
            specificConditions = CardSpecificConditionFile(CardGeneration.GEN2V2, 112, ByteArray(562))), "saved") }
        screen(CardImportUiState(CardImportPhase.IMPORTED, lastRun = run))
        compose.onNodeWithTag("card_import_status").assertTextEquals("Fahrerkarte aktualisiert")
        compose.onNodeWithText("1 Kartentag").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug und Out of Scope übernommen").performScrollTo().assertIsDisplayed()
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
    @Test fun activeInitialImportWaitsForNavigationAndDoesNotReopenWhenGoingBack() {
        val state = mutableStateOf(CardImportUiState(CardImportPhase.WAITING_FOR_CARD))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = state.value)
        } }
        await("Fahrerkarte einlegen")
        compose.onNodeWithTag("card_import_status").assertTextEquals("Fahrerkarte einlegen")
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("nav_start").assertIsDisplayed()
        compose.waitForIdle()
        compose.onNodeWithTag("card_import_status").assertDoesNotExist()
        compose.runOnIdle { state.value = state.value.copy(phase = CardImportPhase.USB_PERMISSION_REQUIRED) }
        await("USB-Zugriff bestätigen")
        compose.onNodeWithTag("card_import_status").assertTextEquals("USB-Zugriff bestätigen")
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

    private fun issueFile(kind: CardEventFaultKind, plate: String = "TEST EVENT", type: Int = 3,
                          begin: Int = 1790000200, end: Int = 1790000100,
                          generation: CardGeneration = CardGeneration.GEN2V2): CardEventFaultFile {
        val perType = if (kind == CardEventFaultKind.EVENTS) 12 else 24
        val bytes = ByteArray(CardEventFaultParser.fileLength(kind, generation, perType))
        ByteBuffer.allocate(24).put(type.toByte()).putInt(begin).putInt(end).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).array().copyInto(bytes)
        return CardEventFaultFile(kind, generation, perType, bytes)
    }
    private fun savedIssue(file: CardEventFaultFile, id: String = "fixture-issue") =
        SavedCardEventFault(id, "own-physical", file, CardEventFaultParser.parse(file), 0)

    @Test fun eventOverviewKeepsRecordedInvertedTimesAndNeutralHumanLabels() {
        val event = savedIssue(issueFile(CardEventFaultKind.EVENTS))
        val fault = savedIssue(issueFile(CardEventFaultKind.FAULTS, "TEST FAULT", 0x40, end = 0))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardEventFaultScreen(PaddingValues(), event, fault) } }
        compose.onNodeWithText("Zeitüberschneidung").assertIsDisplayed()
        val format = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val zone = java.time.ZoneId.systemDefault()
        val starts = compose.onAllNodesWithText("Beginn: ${Instant.ofEpochSecond(1790000200).atZone(zone).format(format)}")
        starts.assertCountEquals(2)
        starts[0].assertIsDisplayed()
        compose.onNodeWithText("Ende: ${Instant.ofEpochSecond(1790000100).atZone(zone).format(format)}").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST EVENT").assertIsDisplayed()
        compose.onNodeWithText("Kartenstörung ohne nähere Angaben").performScrollTo().assertIsDisplayed()
        starts[1].performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ende: nicht angegeben").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Dauer", substring = true).assertDoesNotExist()
    }

    @Test fun missingNewSourcesAndValidEmptyFilesHaveSeparateHonestMessages() {
        val state = mutableStateOf<SavedCardEventFault?>(null)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardEventFaultScreen(PaddingValues(), state.value, null) } }
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Ereignisdaten verfügbar.").assertIsDisplayed()
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Störungsdaten verfügbar.").assertIsDisplayed()
        compose.runOnIdle { state.value = savedIssue(CardEventFaultFile(CardEventFaultKind.EVENTS,
            CardGeneration.GEN2V2, 12, ByteArray(3168)), "empty-event") }
        compose.onNodeWithText("In dieser Kartendatei sind keine Ereignisse aufgezeichnet.").assertIsDisplayed()
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Ereignisdaten verfügbar.").assertDoesNotExist()
        compose.onNodeWithText("Für diese Übernahme sind keine gespeicherten Störungsdaten verfügbar.").assertIsDisplayed()
        compose.onNodeWithText("Zeitüberschneidung").assertDoesNotExist()
    }

    @Test fun generationSpecificTypesDoNotApplyV2LabelsToOlderCardsAndUnknownCodesStayNeutral() {
        val state = mutableStateOf(savedIssue(issueFile(CardEventFaultKind.EVENTS, type = 0x0F)))
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) { CardEventFaultScreen(PaddingValues(), state.value, null) } }
        compose.onNodeWithText("GNSS-Anomalie").assertIsDisplayed()
        compose.runOnIdle { state.value = savedIssue(issueFile(CardEventFaultKind.EVENTS, type = 0x0F,
            generation = CardGeneration.GEN2), "older-g2") }
        compose.onNodeWithText("Nicht zugeordnete Meldung (Code 0F)").assertIsDisplayed()
        compose.onNodeWithText("GNSS-Anomalie").assertDoesNotExist()
        compose.runOnIdle { state.value = savedIssue(issueFile(CardEventFaultKind.EVENTS, type = 0xF1), "unknown-code") }
        compose.onNodeWithText("Nicht zugeordnete Meldung (Code F1)").assertIsDisplayed()
        compose.onNodeWithText("Verstoß", substring = true).assertDoesNotExist()
        assertEquals("Störung des internen GNSS-Empfängers", eventFaultLabel(CardGeneration.GEN2, 0x36))
        assertEquals("Nicht zugeordnete Meldung (Code 36)", eventFaultLabel(CardGeneration.GEN1, 0x36))
        assertEquals("Störung des internen Sensors", eventFaultLabel(CardGeneration.GEN2V2, 0x3A))
        assertEquals("Nicht zugeordnete Meldung (Code 3A)", eventFaultLabel(CardGeneration.GEN2, 0x3A))
    }

    @Test fun eventNavigationOpensTheExactSelectedSessionIncludingOneBeforeTheseFilesWereImported() {
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read().copy(controls = controlFile()), "without-events")
            pipeline.process(read().copy(events = issueFile(CardEventFaultKind.EVENTS, "TEST EARLY E"),
                faults = issueFile(CardEventFaultKind.FAULTS, "TEST EARLY F", 0x40)), "early-events")
            pipeline.process(read().copy(events = issueFile(CardEventFaultKind.EVENTS, "TEST LATE E"),
                faults = issueFile(CardEventFaultKind.FAULTS, "TEST LATE F", 0x40)), "late-events")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Ereignisse und Störungen").performClick()
        }
        open("early-events"); await("Fahrzeug: TEST EARLY E")
        compose.onNodeWithText("Fahrzeug: TEST EARLY F").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("TEST LATE E", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("without-events"); await("Für diese Übernahme sind keine gespeicherten Ereignisdaten verfügbar.")
        compose.onNodeWithText("TEST EARLY E", substring = true).assertDoesNotExist()
        compose.onNodeWithText("TEST LATE E", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("late-events"); await("Fahrzeug: TEST LATE E")
        compose.onNodeWithText("TEST EARLY E", substring = true).assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    @Test fun onlyEventAndFaultSourcesProduceAPartialStartStatusWithoutShowingTechnicalRecordsOnStart() {
        val run = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read().copy(activity = null, vehicles = null, activityReadFailed = true,
                vehicleReadFailed = true, events = issueFile(CardEventFaultKind.EVENTS),
                faults = issueFile(CardEventFaultKind.FAULTS, "TEST FAULT", 0x40)), "only-issues")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(lastRun = run))
        } }
        await("Kartendaten teilweise verfügbar")
        compose.onNodeWithText("Noch keine Kartendaten gespeichert").assertDoesNotExist()
        compose.onNodeWithText("Zeitüberschneidung").assertDoesNotExist()
        compose.onNodeWithText("Ereignisse und Störungen").assertDoesNotExist()
        compose.onNodeWithTag("nav_start").assertIsDisplayed()
    }

    @Test fun borderNavigationUsesOnlyTheSelectedSessionIncludingASourceOnlyImport() {
        fun border(entered: Int): CardBorderCrossingsFile {
            val bytes = ByteArray(19042)
            ByteBuffer.wrap(bytes).putShort(0).put(13.toByte()).put(entered.toByte()).putInt(1790000030)
                .put(10.toByte()).put(byteArrayOf(0, 0, 1, 0, 0, 2)).put(1.toByte()).put(byteArrayOf(0, 0, 3))
            return CardBorderCrossingsFile(CardGeneration.GEN2V2, 1120, bytes)
        }
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-borders")
            pipeline.process(read().copy(activity = null, vehicles = null, borderCrossings = border(1)), "early-borders-only")
            pipeline.process(read().copy(borderCrossings = border(7)), "latest-borders")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Grenz- und Ländereinträge").performScrollTo().performClick()
        }
        open("early-borders-only"); await("Land eingereist / bei Karteneinlage: Kartenwert 1")
        compose.onNodeWithText("Land eingereist / bei Karteneinlage: Kartenwert 7").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-borders"); await("Für diese Übernahme ist keine gespeicherte Grenz-/Länderdatei verfügbar.")
        compose.onNodeWithText("Ländereintrag").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-borders"); await("Land eingereist / bei Karteneinlage: Kartenwert 7")
        compose.onNodeWithText("Land eingereist / bei Karteneinlage: Kartenwert 1").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    @Test fun loadUnloadNavigationUsesOnlyTheSelectedSessionIncludingASourceOnlyImport() {
        fun load(operation: Int): CardLoadUnloadFile {
            val bytes = ByteArray(32482)
            ByteBuffer.wrap(bytes).putShort(0).putInt(1790000020).put(operation.toByte()).putInt(1790000030)
                .put(10.toByte()).put(byteArrayOf(0, 0, 1, 0, 0, 2)).put(1.toByte()).put(byteArrayOf(0, 0, 3))
            return CardLoadUnloadFile(CardGeneration.GEN2V2, 1624, bytes)
        }
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-loads")
            pipeline.process(read().copy(activity = null, vehicles = null, loadUnload = load(1)), "early-loads-only")
            pipeline.process(read().copy(loadUnload = load(2)), "latest-loads")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Be- und Entladeeinträge").performScrollTo().performClick()
        }
        open("early-loads-only"); await("Art: Beladen")
        compose.onNodeWithText("Art: Entladen").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-loads"); await("Für diese Übernahme ist keine gespeicherte Be-/Entladedatei verfügbar.")
        compose.onNodeWithText("Aufgezeichneter Vorgang").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-loads"); await("Art: Entladen")
        compose.onNodeWithText("Art: Beladen").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    @Test fun loadTypeNavigationUsesOnlyTheSelectedSessionIncludingASourceOnlyImport() {
        fun load(type: Int): CardLoadTypeFile {
            val bytes = ByteArray(1682)
            ByteBuffer.wrap(bytes).putShort(0).putInt(1790000100).put(type.toByte())
            return CardLoadTypeFile(CardGeneration.GEN2V2, 336, bytes)
        }
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-types")
            pipeline.process(read().copy(activity = null, vehicles = null, loadType = load(1)), "early-types-only")
            pipeline.process(read().copy(loadType = load(2)), "latest-types")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Ladungsart-Einträge").performScrollTo().performClick()
        }
        open("early-types-only"); await("Art: Güter")
        compose.onNodeWithText("Art: Fahrgäste").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-types"); await("Für diese Übernahme ist keine gespeicherte Ladungsart-Datei verfügbar.")
        compose.onNodeWithText("Aufgezeichneter Eintrag").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-types"); await("Art: Fahrgäste")
        compose.onNodeWithText("Art: Güter").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    @Test fun vehicleUnitsNavigationUsesOnlyTheSelectedSessionIncludingASourceOnlyImport() {
        fun load(type: Int): CardVehicleUnitsFile {
            val bytes = ByteArray(2002)
            ByteBuffer.wrap(bytes).putShort(0).putInt(1790000100).put(type.toByte()).put(254.toByte()).put("A.01".toByteArray())
            return CardVehicleUnitsFile(CardGeneration.GEN2V2, 200, bytes)
        }
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-units")
            pipeline.process(read().copy(activity = null, vehicles = null, vehicleUnits = load(1)), "early-units-only")
            pipeline.process(read().copy(vehicleUnits = load(2)), "latest-units")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Fahrtenschreiber-Nutzungen").performScrollTo().performClick()
        }
        open("early-units-only"); await("Herstellercode des Fahrtenschreibers: Kartenwert 1")
        compose.onNodeWithText("Herstellercode des Fahrtenschreibers: Kartenwert 2").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-units"); await("Für diese Übernahme ist keine gespeicherte Fahrtenschreiber-Datei verfügbar.")
        compose.onNodeWithText("Aufgezeichneter Fahrtenschreiber").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-units"); await("Herstellercode des Fahrtenschreibers: Kartenwert 2")
        compose.onNodeWithText("Herstellercode des Fahrtenschreibers: Kartenwert 1").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    private fun metadataHistoryNavigation(kind: CardMetadataKind, title: String, firstText: String, lastText: String) {
        fun value(code: Int): CardMetadataRead {
            val bytes = ByteArray(kind.fileLength)
            when (kind) {
                CardMetadataKind.DRIVING_LICENCE -> {
                    bytes[0] = 1; "TEST AUTHORITY".toByteArray().copyInto(bytes, 1); bytes[36] = code.toByte()
                    "TEST-LICENCE-01".toByteArray().copyInto(bytes, 37)
                }
                CardMetadataKind.CURRENT_USAGE -> {
                    ByteBuffer.wrap(bytes).putInt(1790000100).put(code.toByte()).put(1.toByte())
                    "TEST OWN".toByteArray().copyInto(bytes, 6)
                }
                CardMetadataKind.LAST_CARD_DOWNLOAD -> ByteBuffer.wrap(bytes).putInt(if (code == 1) 0 else 1790000200)
                CardMetadataKind.ICC_IDENTIFICATION -> bytes[8] = code.toByte()
                CardMetadataKind.IC_IDENTIFICATION -> bytes[0] = code.toByte()
            }
            return CardMetadataRead(CardMetadataFile(kind, CardGeneration.GEN2V2, bytes))
        }
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-metadata")
            pipeline.process(read().copy(activity = null, vehicles = null, metadata = mapOf(kind to value(1))), "early-metadata-only")
            pipeline.process(read().copy(metadata = mapOf(kind to value(7))), "latest-metadata")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText(title).performScrollTo().performClick()
        }
        open("early-metadata-only")
        if (kind == CardMetadataKind.IC_IDENTIFICATION) {
            compose.onNodeWithText("Chipkennung: verborgen").assertIsDisplayed()
            compose.onNodeWithText("Kennung anzeigen").performScrollTo().performClick()
        }
        await(firstText)
        compose.onNodeWithText(lastText, substring = true).assertDoesNotExist()
        if (kind == CardMetadataKind.DRIVING_LICENCE) {
            compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
            compose.onNodeWithText("Nummer anzeigen").performScrollTo().performClick()
            compose.onNodeWithText("Führerscheinnummer: TEST-LICENCE-01").assertIsDisplayed()
        }
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-metadata"); await("Für diese Übernahme ist diese Kartendatei nicht gespeichert.")
        compose.onNodeWithText(firstText, substring = true).assertDoesNotExist()
        compose.onNodeWithText(lastText, substring = true).assertDoesNotExist()
        compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-metadata")
        if (kind == CardMetadataKind.IC_IDENTIFICATION) {
            compose.onNodeWithText("Chipkennung: verborgen").assertIsDisplayed()
            compose.onNodeWithText("Kennung anzeigen").performScrollTo().performClick()
        }
        compose.waitUntil(10_000) { compose.onAllNodesWithText(lastText, substring = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(lastText, substring = true).assertIsDisplayed()
        compose.onNodeWithText(firstText, substring = true).assertDoesNotExist()
        if (kind == CardMetadataKind.DRIVING_LICENCE) {
            compose.onNodeWithText("Führerscheinnummer: verborgen").assertIsDisplayed()
            compose.onNodeWithText("TEST-LICENCE-01", substring = true).assertDoesNotExist()
        }
        assertEquals(3, runBlocking { pipeline.history().size })
    }
    @Test fun originalHistoryNavigationKeepsEachSelectedSnapshotAndMissingOlderFilesExact() {
        fun application(capacity: Int) = CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION,
            card().generation, IdentityUiFixtures.application(capacity)))
        val newest = runBlocking {
            imports.bind(card(), true); pipeline.process(read(), "before-original-ui")
            pipeline.process(CardReadResult(CardRecognition.DRIVER, card().generation, card(), identitySources = mapOf(
                CardIdentitySourceKind.APPLICATION to application(2))), "early-original-only")
            pipeline.process(read().copy(identitySources = mapOf(CardIdentitySourceKind.APPLICATION to application(7),
                CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION,
                    card().generation, IdentityUiFixtures.original(card()))))), "latest-original-ui")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert"); compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick(); await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Kartenangaben").performScrollTo().performClick()
        }
        open("early-original-only")
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Speicherplätze für Fahrzeugnutzungen: 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-original-ui")
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Für diese Übernahme sind diese Originalangaben nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Speicherplätze für Fahrzeugnutzungen:", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-original-ui")
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: fixture-own-1234").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartenanwendung öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Speicherplätze für Fahrzeugnutzungen: 7").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Karteninhaber und Gültigkeit öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
        assertEquals(3, runBlocking { pipeline.history().size })
    }
    @Test fun originalIdentityOnlyLatestImportOpensThroughActualRootAndResetsNumberAfterLeaving() {
        val run = runBlocking {
            imports.bind(card(), true)
            pipeline.process(CardReadResult(CardRecognition.DRIVER, card().generation, card(), identitySources = mapOf(
                CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION,
                    card().generation, IdentityUiFixtures.original(card()))))), "latest-only-original")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = run))
        } }
        await("Gespeicherte Daten ansehen"); compose.onNodeWithText("Gespeicherte Daten ansehen").performScrollTo().performClick()
        await("Fahrerkarten-Details"); compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
        compose.onNodeWithText("Kartenangaben").performScrollTo().performClick()
        compose.onNodeWithText("Karteninhaber: TEST PERSON ALPHA").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kartennummer anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: fixture-own-1234").performScrollTo().assertIsDisplayed()
        repeat(2) { compose.onNodeWithTag("back").performClick() }
        compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
        compose.onNodeWithText("Kartenangaben").performScrollTo().performClick()
        compose.onNodeWithText("Kartennummer: verborgen").performScrollTo().assertIsDisplayed()
    }

    @Test fun certificateHistoryNavigationUsesOnlySelectedSessionAndHidesReferencesAgain() {
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-certificate-ui")
            pipeline.process(read().copy(activity = null, vehicles = null, certificates = mapOf(CardCertificateKind.CARD to
                CardCertificateRead(CardCertificateFile(CardCertificateKind.CARD, CardGeneration.GEN2V2,
                    CertificateUiFixtures.bytes(role = 17))))), "early-certificate-only")
            pipeline.process(read().copy(certificates = CardCertificateKind.entries.associateWith {
                CardCertificateRead(CardCertificateFile(it, CardGeneration.GEN2V2, CertificateUiFixtures.bytes(role = 18))) }), "latest-certificate-ui")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick(); await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Zertifikate").performScrollTo().performClick()
        }
        open("early-certificate-only")
        compose.onNodeWithText("Aufgezeichneter Geräteartcode: 17").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Referenzen anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Inhaberreferenz: 09 09 09 09 09 09 09 09").performScrollTo().assertIsDisplayed()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-certificate-ui")
        compose.onNodeWithText("Für diese Übernahme ist dieses Zertifikat nicht gespeichert.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Aufgezeichneter Geräteartcode:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Inhaberreferenz: 09", substring = true).assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-certificate-ui")
        compose.onNodeWithText("Aufgezeichneter Geräteartcode: 18").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Inhaberreferenz: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Inhaberreferenz: 09", substring = true).assertDoesNotExist()
        compose.onNodeWithText("CA-Zertifikat öffnen").performScrollTo().performClick()
        compose.onNodeWithText("Aufgezeichneter Geräteartcode: 18").performScrollTo().assertIsDisplayed()
        assertEquals(3, runBlocking { pipeline.history().size })
    }
    @Test fun certificateOnlyLatestImportReopensThroughTheActualRootAndHistory() {
        val run = runBlocking {
            imports.bind(card(), true)
            pipeline.process(CardReadResult(CardRecognition.DRIVER, card().generation, card(), certificates = mapOf(
                CardCertificateKind.CARD to CardCertificateRead(CardCertificateFile(CardCertificateKind.CARD, card().generation,
                    CertificateUiFixtures.bytes())))), "latest-only-certificate")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.INCOMPLETE, lastRun = run))
        } }
        await("Gespeicherte Daten ansehen")
        compose.onNodeWithText("Gespeicherte Daten ansehen").performScrollTo().performClick()
        await("Fahrerkarten-Details"); compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
        compose.onNodeWithText("Zertifikate").performScrollTo().performClick()
        compose.onNodeWithText("Dateigröße: 204 Bytes").performScrollTo().assertIsDisplayed()
        repeat(2) { compose.onNodeWithTag("back").performClick() }
        compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
        compose.onNodeWithText("Zertifikate").performScrollTo().performClick()
        compose.onNodeWithText("Ausstellerreferenz: verborgen").performScrollTo().assertIsDisplayed()
    }

    @Test fun licenceHistoryNavigationUsesOnlyTheSelectedSessionAndResetsTheNumber() =
        metadataHistoryNavigation(CardMetadataKind.DRIVING_LICENCE, "Führerschein",
            "Ausstellungsstaat: Kartenwert 1", "Ausstellungsstaat: Kartenwert 7")
    @Test fun currentUsageHistoryNavigationUsesOnlyTheSelectedSession() =
        metadataHistoryNavigation(CardMetadataKind.CURRENT_USAGE, "Karten-Nutzungseintrag",
            "Zulassungsstaat: Kartenwert 1", "Zulassungsstaat: Kartenwert 7")
    @Test fun downloadHistoryNavigationKeepsEmptyAndMissingAndOriginalTimesDistinct() =
        metadataHistoryNavigation(CardMetadataKind.LAST_CARD_DOWNLOAD, "Download-Zeitpunkt",
            "In dieser Kartendatei ist kein Eintrag aufgezeichnet.", "Download-Zeitpunkt auf der Karte: ")

    @Test fun iccHistoryNavigationUsesOnlyTheSelectedMasterSource() =
        metadataHistoryNavigation(CardMetadataKind.ICC_IDENTIFICATION, "Kartenkennung",
            "Herstellercode der Karte: Kartenwert 1", "Herstellercode der Karte: Kartenwert 7")
    @Test fun chipHistoryNavigationUsesOnlyTheSelectedMasterSourceAndMasksItInitially() =
        metadataHistoryNavigation(CardMetadataKind.IC_IDENTIFICATION, "Chipkennung",
            "Chipkennung: 01 00 00 00 00 00 00 00", "Chipkennung: 07 00 00 00 00 00 00 00")

    private fun conditions(type: Int) = CardSpecificConditionFile(CardGeneration.GEN2V2, 112,
        ByteArray(562).also { ByteBuffer.wrap(it).apply { position(2); putInt(1790000015); put(type.toByte()) } })

    @Test fun conditionNavigationUsesOnlyTheSelectedImportAndClearsForAnOlderSource() {
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-conditions")
            pipeline.process(read().copy(specificConditions = conditions(1)), "early-conditions")
            pipeline.process(read().copy(specificConditions = conditions(2)), "latest-conditions")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
            compose.onNodeWithText("Fahrerkarten-Details").performScrollTo().performClick()
            compose.onNodeWithText("Fähre/Zug und Out of Scope").performScrollTo().performClick()
        }
        open("early-conditions"); await("Out of Scope · Beginn")
        compose.onNodeWithText("Out of Scope · Ende").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("before-conditions"); await("Für diese Übernahme sind keine gespeicherten Sonderbedingungen verfügbar.")
        compose.onNodeWithText("Out of Scope · Beginn").assertDoesNotExist()
        compose.onNodeWithText("Out of Scope · Ende").assertDoesNotExist()
        repeat(3) { compose.onNodeWithTag("back").performClick() }
        open("latest-conditions"); await("Out of Scope · Ende")
        compose.onNodeWithText("Out of Scope · Beginn").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
        compose.onNodeWithTag("nav_vehicles").assertDoesNotExist()
    }

    @Test fun aConditionOnlyImportShowsPartialDataOnStartWithoutTechnicalRecords() {
        val run = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read().copy(activity = null, vehicles = null, activityReadFailed = true,
                vehicleReadFailed = true, specificConditions = conditions(1)), "condition-only")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(lastRun = run))
        } }
        await("Kartendaten teilweise verfügbar")
        compose.onNodeWithText("Noch keine Kartendaten gespeichert").assertDoesNotExist()
        compose.onNodeWithText("Out of Scope · Beginn").assertDoesNotExist()
        compose.onNodeWithTag("nav_start").assertIsDisplayed()
    }

    @Test fun timelineMarkersUseOnlyTheExactSelectedSessionAndClearWhenOpeningAnOlderImport() {
        val localDay = CardDayTimeline.build(CardActivityParser.parse(read().activity!!), ZoneId.systemDefault()).last()
        val point = localDay.segments.first().start.plusSeconds(30).epochSecond.toInt()
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-markers")
            pipeline.process(read().copy(events = issueFile(CardEventFaultKind.EVENTS, "TEST EARLY", 5, point, point)), "early-markers")
            pipeline.process(read().copy(events = issueFile(CardEventFaultKind.EVENTS, "TEST NEWEST", 5, point, point)), "newest-markers")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
        }
        open("early-markers")
        compose.onNodeWithTag("day_issue_list").performScrollTo().performClick()
        compose.onNodeWithText("Fahrzeug: TEST EARLY").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST NEWEST").assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        compose.onNodeWithTag("back").performClick()
        open("before-markers")
        compose.onNodeWithTag("timeline_issue_overview").assertDoesNotExist()
        compose.onNodeWithTag("day_issue_list").assertDoesNotExist()
        compose.onNodeWithTag("back").performClick()
        open("newest-markers")
        compose.onNodeWithTag("day_issue_list").performScrollTo().performClick()
        compose.onNodeWithText("Fahrzeug: TEST NEWEST").assertIsDisplayed()
        compose.onNodeWithText("Fahrzeug: TEST EARLY").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }

    @Test fun conditionTimelineUsesOnlyTheSelectedSessionAndDoesNotBackfillAnOlderImport() {
        val localDay = CardDayTimeline.build(CardActivityParser.parse(read().activity!!), ZoneId.systemDefault()).last()
        val point = localDay.segments.first().start.plusSeconds(30).epochSecond.toInt()
        fun atDay(type: Int) = CardSpecificConditionFile(CardGeneration.GEN2V2, 112,
            ByteArray(562).also { ByteBuffer.wrap(it).apply { position(2); putInt(point); put(type.toByte()) } })
        val newest = runBlocking {
            imports.bind(card(), true)
            pipeline.process(read(), "before-condition-markers")
            pipeline.process(read().copy(specificConditions = atDay(1)), "early-condition-markers")
            pipeline.process(read().copy(specificConditions = atDay(4)), "latest-condition-markers")
        }
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            AppRoot(ThemeMode.LIGHT, {}, activities, vehicles, pairs, driverCardImports = imports,
                cardImportPipeline = pipeline, importState = CardImportUiState(CardImportPhase.IMPORTED, lastRun = newest))
        } }
        await("Fahrerkarte aktualisiert")
        compose.onNodeWithText("Importverlauf").performScrollTo().performClick()
        fun open(id: String) {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("open_import_$id").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("open_import_$id").performScrollTo().performClick()
            await("Fahrerkarten-Details")
        }
        open("early-condition-markers")
        compose.onNodeWithTag("day_condition_list").performScrollTo().performClick()
        compose.onNodeWithText("Out of Scope · Beginn").assertIsDisplayed()
        compose.onNodeWithText("Fähre/Zug · Ende").assertDoesNotExist()
        compose.onNodeWithText("Schließen").performClick()
        compose.onNodeWithTag("back").performClick()
        open("before-condition-markers")
        compose.onNodeWithTag("timeline_condition_overview").assertDoesNotExist()
        compose.onNodeWithTag("day_condition_list").assertDoesNotExist()
        compose.onNodeWithTag("back").performClick()
        open("latest-condition-markers")
        compose.onNodeWithTag("day_condition_list").performScrollTo().performClick()
        compose.onNodeWithText("Fähre/Zug · Ende").assertIsDisplayed()
        compose.onNodeWithText("Out of Scope · Beginn").assertDoesNotExist()
        assertEquals(3, runBlocking { pipeline.history().size })
    }
}
