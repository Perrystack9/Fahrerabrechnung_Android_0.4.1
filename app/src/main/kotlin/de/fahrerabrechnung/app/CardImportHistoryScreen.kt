package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.EarlierDriverImportRow
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun CardImportHistoryScreen(padding: PaddingValues, pipeline: DriverCardImportPipeline,
                                     imports: DriverCardImportRepository,
                                     onPreview: (SavedDriverImport) -> Unit, onOpenArchive: () -> Unit) {
    var runs by remember { mutableStateOf<List<CardImportRun>>(emptyList()) }
    var earlier by remember { mutableStateOf<List<EarlierDriverImportRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var previewError by remember { mutableStateOf<String?>(null) }
    var opening by remember { mutableStateOf<String?>(null) }
    var visibleCount by remember { mutableStateOf(20) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pipeline) {
        try { runs = pipeline.history(); earlier = pipeline.earlierImports() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error = "Der Importverlauf konnte nicht geladen werden." }
        finally { loading = false }
    }
    val entries = runs.map { HistoryEntry(it.id, it.startedAtEpochMillis, it.cardNumber, it.generation,
        it.sessionId, it, null) } + earlier.map { HistoryEntry(it.id, it.savedAtEpochMillis, it.cardNumber,
        CardGeneration.valueOf(it.generation), it.id, null, it) }
    val sorted = entries.sortedWith(compareByDescending<HistoryEntry> { it.time }.thenByDescending { it.id })
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    ScreenContent(padding) {
        Text("Importverlauf", style = MaterialTheme.typography.titleLarge)
        Text("Lesevorgänge deiner zugeordneten Fahrerkarten mit den jeweils verfügbaren Daten.")
        if (loading) Text("Importverlauf wird geladen …")
        error?.let { Text(it) }
        if (!loading && error == null && sorted.isEmpty()) Text("Noch keine Daten im Fahrerprofil gespeichert.")
        sorted.take(visibleCount).forEach { entry ->
            SectionCard {
                Text(Instant.ofEpochMilli(entry.time).atZone(ZoneId.systemDefault()).format(formatter),
                    style = MaterialTheme.typography.titleMedium)
                Text("${entry.generation.name} · Fahrerkarte ${maskCardNumber(entry.cardNumber)}")
                entry.run?.let { run ->
                    Text(run.status.label())
                    run.files.forEach { file -> Text(file.description()) }
                    run.files.firstOrNull { it.kind == CardImportFileKind.ACTIVITIES && it.status == CardImportFileStatus.STORED }?.let { file ->
                        val first = file.firstUtcEpochSeconds?.let { Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC).toLocalDate() }
                        val last = file.lastUtcEpochSeconds?.let { Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC).toLocalDate() }
                        if (first != null && last != null) Text("Kartentage: $first – $last")
                    }
                    if (run.sessionId != null && run.newSourceCount == 0) Text("Die Daten waren bereits vorhanden.")
                }
                entry.earlier?.let { old ->
                    Text("Früher gespeicherter Lesevorgang")
                    old.dayCount?.let { Text("$it Kartentage") }
                    old.useCount?.let { Text("$it Fahrzeugnutzungen") }
                    Text("Für diesen älteren Vorgang wurde noch kein detaillierter Importstatus erfasst.", style = MaterialTheme.typography.bodySmall)
                }
                entry.sessionId?.let { id ->
                    OutlinedButton(enabled = opening == null, modifier = Modifier.testTag("open_import_${entry.id}"), onClick = {
                        opening = id; previewError = null
                        scope.launch(Dispatchers.Main.immediate) {
                            try { onPreview(checkNotNull(imports.get(id))) }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { previewError = entry.id }
                            finally { opening = null }
                        }
                    }) { Text("Gespeicherte Daten ansehen") }
                }
                if (previewError == entry.id) Text("Dieser gespeicherte Vorgang konnte nicht geöffnet werden. Andere Einträge bleiben verfügbar.")
            }
        }
        if (visibleCount < sorted.size) OutlinedButton(onClick = { visibleCount += 20 }) { Text("Ältere Lesevorgänge anzeigen") }
        OutlinedButton(onClick = onOpenArchive) { Text("Früheres Dateiarchiv") }
        Text("Unzugeordnete ältere Dateien bleiben im getrennten Archiv. Das Öffnen ordnet sie keinem Fahrerprofil zu.",
            style = MaterialTheme.typography.bodySmall)
    }
}

private data class HistoryEntry(val id: String, val time: Long, val cardNumber: String,
    val generation: CardGeneration, val sessionId: String?, val run: CardImportRun?, val earlier: EarlierDriverImportRow?)

private fun CardImportFileResult.description(): String {
    val label = when (kind) {
        CardImportFileKind.ACTIVITIES -> "Kartentage"
        CardImportFileKind.VEHICLES -> "Fahrzeugnutzungen"
        CardImportFileKind.CONTROLS -> "Kontrolldaten"
        CardImportFileKind.EVENTS -> "Ereignisse"
        CardImportFileKind.FAULTS -> "Störungen"
        CardImportFileKind.SPECIFIC_CONDITIONS -> "Fähre/Zug und Out of Scope"
    }
    return when (status) {
        CardImportFileStatus.STORED -> if (kind == CardImportFileKind.CONTROLS && decodedCount == 0)
            "Kontrolldaten: übernommen, keine Kontrolle aufgezeichnet"
            else if (kind in setOf(CardImportFileKind.EVENTS, CardImportFileKind.FAULTS, CardImportFileKind.SPECIFIC_CONDITIONS) && decodedCount == 0)
                "$label: übernommen, keine Aufzeichnung" else "$label: $decodedCount gespeichert"
        CardImportFileStatus.READY -> "$label: Übernahme läuft"
        CardImportFileStatus.MISSING -> "$label: nicht verfügbar"
        CardImportFileStatus.READ_FAILED -> "$label: Lesen fehlgeschlagen"
        CardImportFileStatus.INVALID -> "$label: Daten nicht auswertbar"
        CardImportFileStatus.EMPTY -> "$label: keine auswertbaren Daten"
        CardImportFileStatus.NOT_STORED -> "$label: nicht gespeichert"
    }
}
