package de.fahrerabrechnung.app

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import de.fahrerabrechnung.core.design.FahrerTagebuchTheme
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

class CardReadBatchHistoryUiTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var imports: DriverCardImportRepository
    private lateinit var pipeline: DriverCardImportPipeline
    @Before fun prepare() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(AppDatabase.DATABASE_NAME)
        db = EncryptedDatabaseFactory.create(context, ByteArray(32) { (it + 1).toByte() })
        val clock = Clock.fixed(Instant.parse("2026-10-12T07:00:00Z"), ZoneOffset.UTC)
        val activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        val vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        imports = DriverCardImportRepository(db, activities, vehicles, CardSourcePairRepository(db, activities, vehicles, clock), clock)
        pipeline = DriverCardImportPipeline(db, imports, activities, vehicles, clock)
    }
    @After fun close() { db.close(); context.deleteDatabase(AppDatabase.DATABASE_NAME) }
    private fun read(generation: CardGeneration): CardReadResult {
        val raw = ByteArrayOutputStream().apply {
            fun name(value: String) { write(1); write(value.padEnd(35).toByteArray()) }
            write(1); write("TESTCARD12345678".toByteArray()); name("TEST AUTHORITY")
            write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0)); write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0))
            write(byteArrayOf(0x75, 0x53, 0xF1.toByte(), 0)); name("MUSTER"); name("ERIKA")
            write(byteArrayOf(0x20, 0, 0x02, 0x29)); write("DE".toByteArray())
        }.toByteArray()
        return CardReadResult(CardRecognition.DRIVER, generation, DriverCardDecoder.identity(raw, generation),
            identitySources = mapOf(CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(
                CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION, generation, raw))))
    }
    private fun batch(second: CardApplicationAttempt = CardApplicationAttempt.read(CardApplication.GEN2, read(CardGeneration.GEN2))) =
        CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1, read(CardGeneration.GEN1)), second),
            listOf(CardMasterFileCapture(0x0005, ByteArray(8), false)))
    @Test fun historyOffersBothExactChildSessionsWithoutDuplicatingTheParent() = runBlocking {
        val group = pipeline.processApplications(batch(), "ui-parent", true)
        var selected: SavedDriverImport? = null
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportHistoryScreen(PaddingValues(), pipeline, imports, { selected = it }, {})
        } }
        compose.waitUntil(10000) { compose.onAllNodesWithTag("open_application_ui-parent_GEN1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("open_application_ui-parent_GEN1").performScrollTo().performClick()
        compose.waitUntil(10000) { selected?.application == CardApplication.GEN1 }
        assertEquals(group.applications.single { it.application == CardApplication.GEN1 }.run!!.sessionId, selected!!.id)
        compose.onNodeWithTag("open_application_ui-parent_GEN2").performScrollTo().performClick()
        compose.waitUntil(10000) { selected?.application == CardApplication.GEN2 }
        assertEquals(group.primaryRun.sessionId, selected!!.id)
        compose.onAllNodesWithText("Gemeinsamer Lesevorgang · getrennte Anwendungen").assertCountEquals(1)
        compose.onNodeWithText("Chipkennung: 8 Bytes gespeichert").performScrollTo().assertIsDisplayed()
    }
    @Test fun failedApplicationOffersNoBorrowedOrNewerPreview() = runBlocking {
        pipeline.processApplications(batch(CardApplicationAttempt.failed(CardApplication.GEN2, ReaderFailure.IO)), "ui-failed", true)
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardImportHistoryScreen(PaddingValues(), pipeline, imports, {}, {})
        } }
        compose.waitUntil(10000) { compose.onAllNodesWithText("GEN2: Lesen fehlgeschlagen").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("GEN2: Lesen fehlgeschlagen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("open_application_ui-failed_GEN2").assertDoesNotExist()
        compose.onNodeWithTag("open_application_ui-failed_GEN1").performScrollTo().assertIsDisplayed()
    }
    @Test fun sharedMasterSourceIsMaskedAgainWhenTheChildReceiptChanges() {
        val file = CardMetadataFile(CardMetadataKind.IC_IDENTIFICATION, CardGeneration.GEN2, ByteArray(8) { 1 })
        val saved = SavedCardMetadata("shared-parent-mf", "own", file, CardMetadataParser.parse(file), 0)
        val receipt = mutableStateOf("gen1-child")
        compose.setContent { FahrerTagebuchTheme(ThemeMode.LIGHT) {
            CardMetadataScreen(PaddingValues(), CardMetadataKind.IC_IDENTIFICATION, saved, receipt.value)
        } }
        compose.onNodeWithText("Kennung anzeigen").performScrollTo().performClick()
        compose.onNodeWithText("Chipkennung: verborgen").assertDoesNotExist()
        compose.runOnIdle { receipt.value = "gen2-child" }
        compose.onNodeWithText("Chipkennung: verborgen").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Kennung anzeigen").assertExists()
    }
}
