package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun CardCertificateKind.certificateTitle(): String = when (this) {
    CardCertificateKind.CARD -> "Kartenzertifikat"
    CardCertificateKind.SIGNING -> "Signaturzertifikat"
    CardCertificateKind.CA -> "CA-Zertifikat"
    CardCertificateKind.LINK -> "Link-Zertifikat"
}

/** Exact selected own-card receipt. References are hidden again on source/kind/view changes. */
@Composable
internal fun CardCertificateScreen(padding: PaddingValues, sources: Map<CardCertificateKind, SavedCardCertificate>) {
    var kind by remember { mutableStateOf(CardCertificateKind.CARD) }
    val source = sources[kind]
    var showReferences by remember(kind, source?.id) { mutableStateOf(false) }
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    fun reference(bytes: List<Int>) = bytes.joinToString(" ") { "%02X".format(it) }
    ScreenContent(padding) {
        Text("Zertifikate", style = MaterialTheme.typography.titleLarge)
        Text("Kartenstand der ausgewählten gespeicherten Übernahme.")
        for (option in CardCertificateKind.entries) OutlinedButton(onClick = { kind = option }) {
            Text("${option.certificateTitle()} öffnen")
        }
        Text(kind.certificateTitle(), style = MaterialTheme.typography.titleMedium)
        when {
            source == null -> Text("Für diese Übernahme ist dieses Zertifikat nicht gespeichert.")
            source.record == null -> Text("Die gespeicherte Zertifikatsdatei ist leer.")
            else -> SectionCard {
                Text("Dateigröße: ${source.file.bytes.size} Bytes")
                when (val record = source.record) {
                    is CardRsaCertificateRecord -> {
                        Text("Dateiformat: Generation 1")
                        Text(if (showReferences) "Ausstellerreferenz: ${reference(record.authorityReferenceBytes)}"
                            else "Ausstellerreferenz: verborgen")
                        Text("Die weiteren Angaben dieses Formats benötigen eine RSA-Nachrichtenrückgewinnung.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardEccCertificateRecord -> {
                        Text("Dateiformat: Generation 2")
                        Text("Kurve des enthaltenen öffentlichen Schlüssels: ${record.curveName}")
                        Text("Aufgezeichneter Geräteartcode: ${record.equipmentTypeRaw}")
                        Text(if (showReferences) "Ausstellerreferenz: ${reference(record.authorityReferenceBytes)}"
                            else "Ausstellerreferenz: verborgen")
                        Text(if (showReferences) "Inhaberreferenz: ${reference(record.holderReferenceBytes)}"
                            else "Inhaberreferenz: verborgen")
                        Text(record.effectiveTimeUtc?.atZone(zone)?.format(format)?.let { "Zertifikatszeitraum laut Datei ab: $it" }
                            ?: "Zertifikatsbeginn: nicht angegeben")
                        Text(record.expirationTimeUtc?.atZone(zone)?.format(format)?.let { "Zertifikatszeitraum laut Datei bis: $it" }
                            ?: "Zertifikatsende: nicht angegeben")
                        if (record.effectiveTimeUtc != null && record.expirationTimeUtc != null &&
                            record.expirationTimeRaw < record.effectiveTimeRaw) Text("Die aufgezeichneten Zeitangaben widersprechen sich.")
                        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
                    }
                    null -> Unit
                }
                OutlinedButton(onClick = { showReferences = !showReferences }) {
                    Text(if (showReferences) "Referenzen verbergen" else "Referenzen anzeigen")
                }
            }
        }
        Text("Signatur und Vertrauenskette sind noch nicht geprüft.", style = MaterialTheme.typography.bodySmall)
    }
}
