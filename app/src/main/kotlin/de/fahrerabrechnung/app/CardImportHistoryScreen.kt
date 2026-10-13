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
    var batches by remember { mutableStateOf<Map<String, SavedCardReadBatch>>(emptyMap()) }
    var earlier by remember { mutableStateOf<List<EarlierDriverImportRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var previewError by remember { mutableStateOf<String?>(null) }
    var opening by remember { mutableStateOf<String?>(null) }
    var visibleCount by remember { mutableStateOf(20) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pipeline) {
        try {
            runs = pipeline.history(); earlier = pipeline.earlierImports()
            batches = runs.mapNotNull { it.batchId }.distinct().mapNotNull { id ->
                try { pipeline.applicationBatch(id)?.let { id to it } }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { null }
            }.toMap()
        }
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
                if (entry.run?.batchId != null && entry.run.batchId !in batches)
                    Text("Die Anwendungsgruppe dieses Vorgangs konnte nicht geöffnet werden. Andere Einträge bleiben verfügbar.")
                entry.run?.batchId?.let { batchId -> batches[batchId]?.let { batch ->
                    Text("Gemeinsamer Lesevorgang · getrennte Anwendungen")
                    Text("Kartentage werden je ausgewählter Anwendung angezeigt.", style = MaterialTheme.typography.bodySmall)
                    batch.applications.forEach { application ->
                        Text("${application.application.name}: " + when (application.status) {
                            CardApplicationReadStatus.ABSENT -> "nicht vorhanden"
                            CardApplicationReadStatus.READ_FAILED -> "Lesen fehlgeschlagen"
                            CardApplicationReadStatus.READ -> application.run?.status?.label().orEmpty()
                        })
                        application.run?.sessionId?.let { session ->
                            OutlinedButton(enabled = opening == null,
                                modifier = Modifier.testTag("open_application_${batch.id}_${application.application.name}"),
                                onClick = {
                                    opening = session; previewError = null
                                    scope.launch {
                                        try { onPreview(checkNotNull(imports.get(session))) }
                                        catch (cancelled: CancellationException) { throw cancelled }
                                        catch (_: Exception) { previewError = entry.id }
                                        finally { opening = null }
                                    }
                                }) { Text("${application.application.name}-Daten ansehen") }
                        }
                    }
                    batch.masterFiles.forEach { file ->
                        val name = when (file.fileId) {
                            0x0002 -> "Kartenkennung"; 0x0005 -> "Chipkennung"; 0x2F00 -> "Anwendungsverzeichnis"
                            0x2F01 -> "Pufferinformation"; 0x0006 -> "Protokollunterstützung"; else -> "Kartenweite Datei"
                        }
                        Text("$name: " + if (file.readFailed) "Lesen fehlgeschlagen" else "${file.bytes?.size} Bytes gespeichert")
                    }
                } }
                entry.run?.let { run ->
                    Text(run.status.label())
                    run.files.forEach { file -> Text(file.description()) }
                    run.files.firstOrNull { it.kind == CardImportFileKind.ACTIVITIES && it.status == CardImportFileStatus.STORED }?.let { file ->
                        val first = file.firstUtcEpochSeconds?.let { Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC).toLocalDate() }
                        val last = file.lastUtcEpochSeconds?.let { Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC).toLocalDate() }
                        if (first != null && last != null) Text("Kartentage: $first – $last")
                    }
                    if (run.sessionId != null && run.newSourceCount == 0 && run.batchId == null) Text("Die Daten waren bereits vorhanden.")
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
        CardImportFileKind.GNSS_POSITIONS -> "GNSS-Punkte"
        CardImportFileKind.GNSS_AUTHENTICATION -> "GNSS-Authentisierungsstatus"
        CardImportFileKind.BORDER_CROSSINGS -> "Grenz- und Ländereinträge"
        CardImportFileKind.LOAD_UNLOAD_OPERATIONS -> "Be- und Entladeeinträge"
        CardImportFileKind.LOAD_TYPE_ENTRIES -> "Ladungsart-Einträge"
        CardImportFileKind.VEHICLE_UNITS_USED -> "Fahrtenschreiber-Nutzungen"
        CardImportFileKind.DRIVING_LICENCE_INFO -> "Führerscheininformation"
        CardImportFileKind.CURRENT_USAGE -> "Karten-Nutzungseintrag"
        CardImportFileKind.LAST_CARD_DOWNLOAD -> "Download-Zeitpunkt"
        CardImportFileKind.ICC_IDENTIFICATION -> "Kartenkennung"
        CardImportFileKind.IC_IDENTIFICATION -> "Chipkennung"
        CardImportFileKind.CARD_CERTIFICATE -> "Kartenzertifikat"
        CardImportFileKind.CARD_SIGNING_CERTIFICATE -> "Signaturzertifikat"
        CardImportFileKind.CA_CERTIFICATE -> "CA-Zertifikat"
        CardImportFileKind.LINK_CERTIFICATE -> "Link-Zertifikat"
        CardImportFileKind.APPLICATION_IDENTIFICATION -> "Kartenanwendung"
        CardImportFileKind.DRIVER_IDENTIFICATION -> "Originale Kartenangaben"
        CardImportFileKind.APPLICATION_IDENTIFICATION_V2 -> "Kartenanwendung V2"
        CardImportFileKind.MF_DIRECTORY -> "Anwendungsverzeichnis"
        CardImportFileKind.MF_ATR_INFO -> "Pufferinformation"
        CardImportFileKind.MF_EXTENDED_LENGTH -> "Protokollunterstützung"
        CardImportFileKind.APPLICATION_SIGNATURE -> "Kartensignatur der Kartenanwendung"
        CardImportFileKind.IDENTIFICATION_SIGNATURE -> "Kartensignatur der Kartenangaben"
        CardImportFileKind.APPLICATION_V2_SIGNATURE -> "Kartensignatur der Kartenanwendung V2"
        CardImportFileKind.ACTIVITY_SIGNATURE -> "Kartensignatur der Tätigkeiten"
        CardImportFileKind.VEHICLES_SIGNATURE -> "Kartensignatur der Fahrzeuge"
        CardImportFileKind.ACTIVITY_ORIGINAL -> "Originaldatei der Tätigkeiten"
        CardImportFileKind.VEHICLES_ORIGINAL -> "Originaldatei der Fahrzeuge"
        CardImportFileKind.WORK_PERIOD_PLACES -> "Beginn-/Ende-Orte"
        CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION -> "Orts-Authentisierungsstatus"
        CardImportFileKind.SPECIFIC_CONDITIONS -> "Fähre/Zug und Out of Scope"
    }
    return when (status) {
        CardImportFileStatus.STORED -> if (kind in setOf(CardImportFileKind.ACTIVITY_SIGNATURE, CardImportFileKind.VEHICLES_SIGNATURE,
            CardImportFileKind.APPLICATION_SIGNATURE, CardImportFileKind.IDENTIFICATION_SIGNATURE, CardImportFileKind.APPLICATION_V2_SIGNATURE))
            "$label: aufgenommen · ungeprüft" else if (kind in setOf(CardImportFileKind.ACTIVITY_ORIGINAL, CardImportFileKind.VEHICLES_ORIGINAL))
            "$label: vollständig gespeichert" else if (kind in setOf(CardImportFileKind.CARD_CERTIFICATE, CardImportFileKind.CARD_SIGNING_CERTIFICATE,
            CardImportFileKind.CA_CERTIFICATE, CardImportFileKind.LINK_CERTIFICATE,
            CardImportFileKind.APPLICATION_IDENTIFICATION_V2, CardImportFileKind.MF_DIRECTORY,
            CardImportFileKind.MF_ATR_INFO, CardImportFileKind.MF_EXTENDED_LENGTH) && decodedCount == 0)
            "$label: übernommen, Datei leer"
            else if (kind == CardImportFileKind.CONTROLS && decodedCount == 0)
            "Kontrolldaten: übernommen, keine Kontrolle aufgezeichnet"
            else if (kind in setOf(CardImportFileKind.EVENTS, CardImportFileKind.FAULTS, CardImportFileKind.SPECIFIC_CONDITIONS, CardImportFileKind.GNSS_POSITIONS, CardImportFileKind.GNSS_AUTHENTICATION, CardImportFileKind.WORK_PERIOD_PLACES, CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION, CardImportFileKind.BORDER_CROSSINGS, CardImportFileKind.LOAD_UNLOAD_OPERATIONS, CardImportFileKind.LOAD_TYPE_ENTRIES, CardImportFileKind.VEHICLE_UNITS_USED, CardImportFileKind.DRIVING_LICENCE_INFO, CardImportFileKind.CURRENT_USAGE, CardImportFileKind.LAST_CARD_DOWNLOAD, CardImportFileKind.ICC_IDENTIFICATION, CardImportFileKind.IC_IDENTIFICATION) && decodedCount == 0)
                "$label: übernommen, keine Aufzeichnung" else "$label: $decodedCount gespeichert"
        CardImportFileStatus.READY -> "$label: Übernahme läuft"
        CardImportFileStatus.MISSING -> "$label: nicht verfügbar"
        CardImportFileStatus.READ_FAILED -> "$label: Lesen fehlgeschlagen"
        CardImportFileStatus.INVALID -> "$label: Daten nicht auswertbar"
        CardImportFileStatus.EMPTY -> "$label: keine Aufzeichnung"
        CardImportFileStatus.NOT_STORED -> "$label: nicht gespeichert"
        CardImportFileStatus.NOT_ATTEMPTED -> "$label: nicht gelesen, Dateigröße unbekannt"
    }
}
