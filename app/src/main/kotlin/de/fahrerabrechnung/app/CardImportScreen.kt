package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun CardImportScreen(padding: PaddingValues, state: CardImportUiState,
                              onConfirm: (String) -> Unit, onDecline: (String) -> Unit,
                              onRetry: () -> Unit, onOpenHistory: () -> Unit,
                              onOpenLatest: () -> Unit, onOpenVehicles: () -> Unit) {
    ScreenContent(padding) {
        Text("Fahrerkarte", style = MaterialTheme.typography.titleLarge)
        SectionCard {
            Text(state.phase.title(), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("card_import_status"))
            Text(state.phase.description(state.readerFailure))
            if (state.phase == CardImportPhase.READING || state.phase == CardImportPhase.SAVING)
                LinearProgressIndicator()
            if (state.phase == CardImportPhase.ERROR || state.phase == CardImportPhase.CANCELLED)
                Button(onClick = onRetry, modifier = Modifier.testTag("retry_card_import")) { Text("Erneut versuchen") }
            if (state.phase == CardImportPhase.USB_PERMISSION_REQUIRED)
                OutlinedButton(onClick = onRetry) { Text("USB-Zugriff freigeben") }
        }
        state.lastRun?.takeUnless { state.phase in listOf(CardImportPhase.FOREIGN_CARD,
            CardImportPhase.CONFIRM_FIRST_CARD, CardImportPhase.CONFIRM_REPLACEMENT,
            CardImportPhase.READING, CardImportPhase.SAVING) ||
            (state.phase == CardImportPhase.ERROR && state.readerFailure != null) }?.let { run ->
            SectionCard {
                val date = Instant.ofEpochMilli(run.startedAtEpochMillis).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                Text("Letzter Lesevorgang · $date", style = MaterialTheme.typography.titleMedium)
                if (run.batchId != null) Text("Gemeinsamer Lesevorgang · Ansicht ${run.application?.name}. Beide Anwendungen findest du im Importverlauf.")
                Text(run.status.label())
                run.files.filter { it.status == CardImportFileStatus.STORED }.forEach {
                    when (it.kind) {
                        CardImportFileKind.ACTIVITIES -> Text("${it.decodedCount} ${if (it.decodedCount == 1) "Kartentag" else "Kartentage"}")
                        CardImportFileKind.VEHICLES -> Text("${it.decodedCount} ${if (it.decodedCount == 1) "Fahrzeugnutzung" else "Fahrzeugnutzungen"}")
                        CardImportFileKind.CONTROLS -> Text("Kontrolldaten übernommen")
                        CardImportFileKind.EVENTS -> Text("${it.decodedCount} Ereignisse übernommen")
                        CardImportFileKind.FAULTS -> Text("${it.decodedCount} Störungen übernommen")
                        CardImportFileKind.GNSS_POSITIONS -> Text("GNSS-Punkte übernommen")
                        CardImportFileKind.GNSS_AUTHENTICATION -> Text("GNSS-Authentisierungsstatus übernommen")
                        CardImportFileKind.BORDER_CROSSINGS -> Text("Grenz- und Ländereinträge übernommen")
                        CardImportFileKind.LOAD_UNLOAD_OPERATIONS -> Text("Be- und Entladeeinträge übernommen")
                        CardImportFileKind.LOAD_TYPE_ENTRIES -> Text("Ladungsart-Einträge übernommen")
                        CardImportFileKind.VEHICLE_UNITS_USED -> Text("Fahrtenschreiber-Nutzungen übernommen")
                        CardImportFileKind.DRIVING_LICENCE_INFO -> Text("Führerscheininformation übernommen")
                        CardImportFileKind.CURRENT_USAGE -> Text("Karten-Nutzungseintrag übernommen")
                        CardImportFileKind.LAST_CARD_DOWNLOAD -> Text("Download-Zeitpunkt übernommen")
                        CardImportFileKind.ICC_IDENTIFICATION -> Text("Kartenkennung übernommen")
                        CardImportFileKind.IC_IDENTIFICATION -> Text("Chipkennung übernommen")
                        CardImportFileKind.CARD_CERTIFICATE -> Text("Kartenzertifikat übernommen")
                        CardImportFileKind.CARD_SIGNING_CERTIFICATE -> Text("Signaturzertifikat übernommen")
                        CardImportFileKind.CA_CERTIFICATE -> Text("CA-Zertifikat übernommen")
                        CardImportFileKind.LINK_CERTIFICATE -> Text("Link-Zertifikat übernommen")
                        CardImportFileKind.APPLICATION_IDENTIFICATION -> Text("Kartenanwendung übernommen")
                        CardImportFileKind.DRIVER_IDENTIFICATION -> Text("Originale Kartenangaben übernommen")
                        CardImportFileKind.APPLICATION_IDENTIFICATION_V2 -> Text("Kartenanwendung V2 übernommen")
                        CardImportFileKind.MF_DIRECTORY -> Text("Anwendungsverzeichnis übernommen")
                        CardImportFileKind.MF_ATR_INFO -> Text("Pufferinformation übernommen")
                        CardImportFileKind.MF_EXTENDED_LENGTH -> Text("Protokollunterstützung übernommen")
                        CardImportFileKind.ACTIVITY_SIGNATURE -> Text("Kartensignatur der Tätigkeiten aufgenommen · ungeprüft")
                        CardImportFileKind.VEHICLES_SIGNATURE -> Text("Kartensignatur der Fahrzeuge aufgenommen · ungeprüft")
                        CardImportFileKind.ACTIVITY_ORIGINAL -> Text("Originaldatei der Tätigkeiten gespeichert")
                        CardImportFileKind.VEHICLES_ORIGINAL -> Text("Originaldatei der Fahrzeuge gespeichert")
                        CardImportFileKind.WORK_PERIOD_PLACES -> Text("Beginn-/Ende-Orte übernommen")
                        CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION -> Text("Orts-Authentisierungsstatus übernommen")
                        CardImportFileKind.SPECIFIC_CONDITIONS -> Text("Fähre/Zug und Out of Scope übernommen")
                    }
                }
                if (run.sessionId != null) {
                    if (run.newSourceCount == 0 && run.batchId == null) Text("Die gelesenen Daten waren bereits vorhanden.")
                    OutlinedButton(onClick = onOpenLatest) { Text("Gespeicherte Daten ansehen") }
                    if (run.files.any { it.kind == CardImportFileKind.VEHICLES && it.status == CardImportFileStatus.STORED })
                        OutlinedButton(onClick = onOpenVehicles) { Text("Fahrzeughistorie") }
                }
            }
        }
        OutlinedButton(onClick = onOpenHistory) { Text("Importverlauf") }
        Text("Die App übernimmt die verfügbaren Kartendaten automatisch. Weitere Kartendaten folgen; die vollständige Auswertung ist noch in Entwicklung.",
            style = MaterialTheme.typography.bodySmall)
    }
    val token = state.confirmationToken
    val card = state.confirmationCard
    if (token != null && card != null) AlertDialog(
        onDismissRequest = { onDecline(token) },
        title = { Text(if (state.phase == CardImportPhase.CONFIRM_FIRST_CARD) "Deine Fahrerkarte?" else "Deine neue Fahrerkarte?") },
        text = { Text("${card.holder.givenNames} ${card.holder.surname}\n" +
            "Geburtsdatum: ${card.holder.birthDate?.toString() ?: "nicht angegeben"}\n" +
            "Kartennummer: ${maskCardNumber(card.card.number)}\n\n" +
            "Bestätige nur deine eigene Karte. Danach übernimmt die App die verfügbaren Daten automatisch. Alte Karten und Daten bleiben erhalten.") },
        confirmButton = { TextButton(onClick = { onConfirm(token) }, modifier = Modifier.testTag("confirm_automatic_own_card")) { Text("Das ist meine Karte") } },
        dismissButton = { TextButton(onClick = { onDecline(token) }) { Text("Abbrechen") } },
    )
}

internal fun CardImportRunStatus.label() = when (this) {
    CardImportRunStatus.IN_PROGRESS -> "Übernahme läuft"
    CardImportRunStatus.SAVED -> "Verfügbare Kartendaten gespeichert"
    CardImportRunStatus.PARTIAL -> "Verfügbare Daten gespeichert – einige Daten fehlen"
    CardImportRunStatus.FAILED -> "Keine neuen Daten übernommen"
    CardImportRunStatus.INTERRUPTED -> "Übernahme unterbrochen – keine neuen Daten übernommen"
}

private fun CardImportPhase.title() = when (this) {
    CardImportPhase.WAITING_FOR_READER -> "Kartenleser anschließen"
    CardImportPhase.USB_PERMISSION_REQUIRED -> "USB-Zugriff bestätigen"
    CardImportPhase.WAITING_FOR_CARD -> "Fahrerkarte einlegen"
    CardImportPhase.READING -> "Fahrerkarte wird gelesen …"
    CardImportPhase.CONFIRM_FIRST_CARD -> "Eigene Fahrerkarte bestätigen"
    CardImportPhase.CONFIRM_REPLACEMENT -> "Neue Fahrerkarte bestätigen"
    CardImportPhase.SAVING -> "Daten werden übernommen …"
    CardImportPhase.IMPORTED -> "Fahrerkarte aktualisiert"
    CardImportPhase.INCOMPLETE -> "Einige Daten fehlen"
    CardImportPhase.FOREIGN_CARD -> "Andere Fahrerkarte erkannt"
    CardImportPhase.CANCELLED -> "Übernahme abgebrochen"
    CardImportPhase.ERROR -> "Übernahme nicht möglich"
}

private fun CardImportPhase.description(failure: ReaderFailure?) = when (this) {
    CardImportPhase.WAITING_FOR_READER -> "Die App erkennt einen kompatiblen Leser und liest die Karte automatisch, solange sie geöffnet ist."
    CardImportPhase.USB_PERMISSION_REQUIRED -> "Erlaube Android den Zugriff auf deinen Kartenleser."
    CardImportPhase.WAITING_FOR_CARD -> "Der Leser ist bereit. Lege deine Fahrerkarte ein."
    CardImportPhase.READING -> "Lass die Karte im Leser, bis das Lesen abgeschlossen ist."
    CardImportPhase.CONFIRM_FIRST_CARD -> "Deine erste Karte wird erst nach deiner Bestätigung dem lokalen Fahrerprofil zugeordnet."
    CardImportPhase.CONFIRM_REPLACEMENT -> "Die Inhaberdaten sind nicht eindeutig. Bestätige nur deine eigene neue oder Ersatzkarte."
    CardImportPhase.SAVING -> "Die verfügbaren Daten werden verschlüsselt gespeichert."
    CardImportPhase.IMPORTED -> "Die verfügbaren Kartendaten sind gespeichert. Du kannst den Leser entfernen."
    CardImportPhase.INCOMPLETE -> "Die lesbaren Daten sind gespeichert. Andere Daten konnten nicht übernommen werden. Details stehen im Importverlauf."
    CardImportPhase.FOREIGN_CARD -> "Diese Karte gehört nicht zum gebundenen Fahrerprofil. Ihre Daten werden nicht gespeichert."
    CardImportPhase.CANCELLED -> "Es wurden keine Daten übernommen. Entferne die Karte oder versuche es erneut."
    CardImportPhase.ERROR -> when (failure) {
        ReaderFailure.PERMISSION -> "Der USB-Zugriff wurde nicht freigegeben. Du kannst die Freigabe erneut anfordern."
        ReaderFailure.CARD_ABSENT, ReaderFailure.CARD_REMOVED -> "Die Fahrerkarte fehlt oder wurde beim Lesen entfernt. Lege sie erneut ein."
        ReaderFailure.DISCONNECTED, ReaderFailure.NO_READER -> "Der Kartenleser wurde getrennt. Schließe ihn erneut an."
        ReaderFailure.UNSUPPORTED_READER -> "Dieser Kartenleser wird derzeit nicht unterstützt."
        else -> "Die Karte konnte nicht gelesen oder die Daten konnten nicht gespeichert werden. Vorhandene Daten bleiben erhalten."
    }
}
