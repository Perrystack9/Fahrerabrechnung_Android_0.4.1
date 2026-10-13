package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun CardIdentitySourceKind.identitySourceTitle(): String = when (this) {
    CardIdentitySourceKind.IDENTIFICATION -> "Karteninhaber und Gültigkeit"
    CardIdentitySourceKind.APPLICATION -> "Kartenanwendung"
    CardIdentitySourceKind.APPLICATION_V2 -> "Kartenanwendung V2"
}

/** Always the selected original source. The normalized profile is no substitute for a missing EF. */
@Composable
internal fun CardIdentitySourceScreen(padding: PaddingValues, sources: Map<CardIdentitySourceKind, SavedCardIdentitySource>, receiptId: String? = null,
    signatures: Map<CardIdentitySourceKind, CardIdentitySignature> = emptyMap(),
    generation: CardGeneration? = sources.values.firstOrNull()?.file?.generation ?: signatures.values.firstOrNull()?.generation) {
    var kind by remember { mutableStateOf(CardIdentitySourceKind.IDENTIFICATION) }
    val source = sources[kind]
    var showNumber by remember(kind, source?.id, receiptId) { mutableStateOf(false) }
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    fun time(value: Instant) = value.takeUnless { it.epochSecond in setOf(0L, 0xFFFF_FFFFL) }
        ?.atZone(zone)?.format(format) ?: "nicht angegeben"
    ScreenContent(padding) {
        Text("Kartenangaben", style = MaterialTheme.typography.titleLarge)
        Text("Originalangaben aus der ausgewählten gespeicherten Übernahme.")
        for (option in CardIdentitySourceKind.entries) OutlinedButton(onClick = { kind = option }) {
            Text("${option.identitySourceTitle()} öffnen")
        }
        Text(kind.identitySourceTitle(), style = MaterialTheme.typography.titleMedium)
        val signature = signatures[kind]
        if (kind == CardIdentitySourceKind.APPLICATION_V2 && generation != null && generation != CardGeneration.GEN2V2)
            Text("Für diese Kartenanwendung nicht vorgesehen.")
        else Text(when (signature?.status) {
            CardSignatureStatus.CAPTURED_UNVERIFIED -> "Kartensignatur: ${signature?.actualLength} Bytes · aufgenommen · ungeprüft"
            CardSignatureStatus.FAILED -> "Kartensignatur: fehlgeschlagen · ${signature?.reason.signatureMessage()}"
            CardSignatureStatus.NOT_ATTEMPTED -> "Kartensignatur: nicht aufgenommen · ${signature?.reason.signatureMessage()}"
            null -> "Signatur in diesem Import nicht aufgenommen"
        })
        if (source == null) Text("Für diese Übernahme sind diese Originalangaben nicht gespeichert.")
        else SectionCard {
            Text("Dateigröße: ${source.file.bytes.size} Bytes")
            when (val record = source.record) {
                null -> Text("Diese gespeicherte Originaldatei ist leer.")
                is CardOriginalIdentityRecord -> {
                    val card = record.card
                    Text("Karteninhaber: ${card.holder.givenNames} ${card.holder.surname}")
                    Text("Geburtsdatum: ${card.holder.birthDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "nicht angegeben"}")
                    Text(if (showNumber) "Kartennummer: ${card.card.number}" else "Kartennummer: verborgen")
                    Text("Ausstellende Stelle: ${card.card.issuingAuthority}")
                    Text("Aufgezeichneter Aussteller-Ländercode: ${card.card.issuingState}")
                    Text("Ausgestellt laut Karte: ${time(card.card.issued)}")
                    Text("Gültig laut Karte ab: ${time(card.card.validFrom)}")
                    Text("Gültig laut Karte bis: ${time(card.card.expires)}")
                    OutlinedButton(onClick = { showNumber = !showNumber }) {
                        Text(if (showNumber) "Kartennummer verbergen" else "Kartennummer anzeigen")
                    }
                    Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
                }
                is CardApplicationIdentityRecord -> {
                    Text("Aufgezeichnete Strukturversion: ${record.structureVersionBytes.joinToString(" ") { "%02X".format(it) }}")
                    Text("Speicher für Ereignisse je Typ: ${record.eventsPerTypeRaw}")
                    Text("Speicher für Störungen je Typ: ${record.faultsPerTypeRaw}")
                    Text("Speicher für Aktivitätsaufzeichnungen: ${record.activityStructureLengthRaw} Bytes")
                    Text("Speicherplätze für Fahrzeugnutzungen: ${record.vehicleCapacityRaw}")
                    Text("Speicherplätze für Beginn-/Ende-Orte: ${record.placeCapacityRaw}")
                    record.gnssCapacityRaw?.let { Text("Speicherplätze für GNSS-Aufzeichnungen: $it") }
                    record.specificConditionCapacityRaw?.let { Text("Speicherplätze für besondere Bedingungen: $it") }
                    record.vehicleUnitCapacityRaw?.let { Text("Speicherplätze für Fahrtenschreiber-Nutzungen: $it") }
                    Text("Diese Angaben beschreiben die aufgezeichnete Speicherkapazität, nicht die Anzahl übernommener Einträge.",
                        style = MaterialTheme.typography.bodySmall)
                }
                is CardApplicationV2IdentityRecord -> {
                    Text("Speicherplätze für Grenz- und Ländereinträge: ${record.borderCapacityRaw}")
                    Text("Speicherplätze für Be- und Entladeeinträge: ${record.loadUnloadCapacityRaw}")
                    Text("Speicherplätze für Ladungsart-Einträge: ${record.loadTypeCapacityRaw}")
                    Text("Speicher für Fahrtenschreiberkonfiguration: ${record.vuConfigurationLengthRangeRaw} Bytes")
                    Text("Diese Angaben beschreiben die aufgezeichnete Speicherkapazität, nicht die Anzahl übernommener Einträge.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
