package de.fahrerabrechnung.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.SectionCard

private val stepTitles = listOf(
    "Willkommen bei FahrerTagebuch", "Datenschutz & Zuhause", "Spesen",
    "Arbeitszeit & Zuschläge", "Fahrerkarte", "Erinnerungen", "Deine Übersicht",
)

@Composable
fun OnboardingScreen(
    padding: PaddingValues,
    draft: OnboardingDraft,
    onDraftChange: (OnboardingDraft) -> Unit,
    onFinish: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var editingSummary by rememberSaveable { mutableStateOf(false) }
    val validation = when (step) {
        2 -> if (listOf(draft.expenses8, draft.expenses24, draft.overnight).any { !validAmount(it) }) "Bitte Beträge mit höchstens zwei Nachkommastellen eingeben." else null
        3 -> nightWindowError(draft.nightWindows) ?: if (listOf(draft.hourlyRate, draft.saturday, draft.sunday, draft.holiday).any { !validAmount(it) }) "Bitte gültige Beträge bzw. Prozentsätze eingeben." else null
        5 -> if (clockMinutes(draft.reminderTime) == null) "Bitte eine Uhrzeit im Format HH:mm eingeben." else null
        6 -> nightWindowError(draft.nightWindows)
            ?: if (listOf(draft.expenses8, draft.expenses24, draft.overnight, draft.hourlyRate, draft.saturday, draft.sunday, draft.holiday).any { !validAmount(it) }) "Bitte die Beträge und Prozentsätze prüfen."
            else if (clockMinutes(draft.reminderTime) == null) "Bitte die Erinnerungszeit prüfen." else null
        else -> null
    }
    Column(Modifier.fillMaxSize().padding(padding).imePadding().testTag("onboarding")) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { (step + 1) / 7f }, modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.secondaryContainer,
            )
            Text("Schritt ${step + 1} von 7", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.testTag("onboarding_step"))
            Text("Der Heimatpunkt wird beim Abschluss gespeichert; übrige Profilfelder sind derzeit Vorschau.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        key(step) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).testTag("onboarding_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stepTitles[step], style = MaterialTheme.typography.headlineSmall)
                when (step) {
                    0 -> {
                        Text("Richte dein Hauptprofil ein. Du kannst deine Angaben später ändern.")
                        SectionCard {
                            Entry("Benutzername", draft.username, { onDraftChange(draft.copy(username = it)) }, tag = "onboarding_username")
                        }
                        Note("Vorname und Nachname werden später aus deiner Fahrerkarte übernommen.")
                    }
                    1 -> {
                        HomePointPicker(draft.homeLatitude, draft.homeLongitude) { lat, lon ->
                            onDraftChange(draft.copy(homeLatitude = lat.toString(), homeLongitude = lon.toString()))
                        }
                        Note("Deine Daten werden lokal auf deinem Gerät verarbeitet.")
                        SectionCard(title = "Zuhause festlegen") {
                            Entry("Adresse", draft.address, { onDraftChange(draft.copy(address = it)) })
                            Text("Die Adresse ist eine optionale Notiz. Für den Heimatbereich zählt der Punkt, den du oben festlegst.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(48.dp).padding(12.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Column { Text("Heimbereich: 500 Meter", style = MaterialTheme.typography.titleSmall); Text("Fest vorgegeben", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                    2 -> {
                        Text("Trage die mit deinem Arbeitgeber vereinbarten Profilwerte ein.")
                        SectionCard {
                            Amount("Spesen über 8 Stunden", draft.expenses8) { onDraftChange(draft.copy(expenses8 = it)) }
                            Amount("Spesen über 24 Stunden", draft.expenses24) { onDraftChange(draft.copy(expenses24 = it)) }
                            Amount("Übernachtungspauschale", draft.overnight) { onDraftChange(draft.copy(overnight = it)) }
                            Toggle("Doppelte Spesen verwenden", draft.doubleExpenses) { onDraftChange(draft.copy(doubleExpenses = it)) }
                            Text("Eine freiwillige Leistung des Arbeitgebers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    3 -> {
                        SectionCard(title = "Arbeitszeit") {
                            Toggle("Bereitschaft bezahlt", draft.standbyPaid) { onDraftChange(draft.copy(standbyPaid = it)) }
                            Toggle("Pause bezahlt", draft.breakPaid) { onDraftChange(draft.copy(breakPaid = it)) }
                            Amount("Stundenlohn / Basiswert", draft.hourlyRate) { onDraftChange(draft.copy(hourlyRate = it)) }
                        }
                        Text("Nachtzuschläge", style = MaterialTheme.typography.titleMedium)
                        Text("Mehrere Zeitfenster sind möglich. Sie dürfen sich nicht überschneiden.", style = MaterialTheme.typography.bodyMedium)
                        draft.nightWindows.forEachIndexed { index, window ->
                            SectionCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Nachtfenster ${index + 1}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                    IconButton(onClick = { onDraftChange(draft.copy(nightWindows = draft.nightWindows.filterIndexed { i, _ -> i != index })) }) {
                                        Icon(Icons.Rounded.DeleteOutline, "Nachtfenster ${index + 1} entfernen")
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Entry("Von (HH:mm)", window.start, { onDraftChange(draft.withNight(index, window.copy(start = it))) }, Modifier.weight(1f), KeyboardType.Text, "night_${index}_start")
                                    Entry("Bis (HH:mm)", window.end, { onDraftChange(draft.withNight(index, window.copy(end = it))) }, Modifier.weight(1f), KeyboardType.Text, "night_${index}_end")
                                }
                                Amount("Zuschlag", window.percent, unit = "%", tag = "night_${index}_percent") { onDraftChange(draft.withNight(index, window.copy(percent = it))) }
                            }
                        }
                        OutlinedButton(onClick = { onDraftChange(draft.copy(nightWindows = draft.nightWindows + NightWindowDraft())) }, modifier = Modifier.fillMaxWidth().testTag("add_night_window")) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("Zeitfenster hinzufügen", modifier = Modifier.padding(start = 8.dp))
                        }
                        SectionCard(title = "Weitere Zuschläge") {
                            Amount("Samstag", draft.saturday, "%") { onDraftChange(draft.copy(saturday = it)) }
                            Amount("Sonntag", draft.sunday, "%") { onDraftChange(draft.copy(sunday = it)) }
                            Amount("Feiertag", draft.holiday, "%") { onDraftChange(draft.copy(holiday = it)) }
                        }
                        Note("Verschiedene Zuschlagsarten können sich addieren. Sie gelten auf Lenkzeit und sonstige Arbeit, nicht auf Bereitschaft, Pause oder Ruhe.")
                    }
                    4 -> {
                        SectionCard(title = "Fahrerkarte verbinden") {
                            Icon(Icons.Rounded.CreditCard, contentDescription = "Fahrerkarte", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text("Das Einlesen mit einem Kartenleser steht noch nicht zur Verfügung.")
                            Text("Einlesen jetzt: folgt mit der Kartenleser-Unterstützung.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { step = if (editingSummary) 6 else 5; editingSummary = false }, modifier = Modifier.fillMaxWidth()) { Text("Später einrichten") }
                        }
                    }
                    5 -> {
                        Text("Erinnere dich an das nächste Einlesen deiner Fahrerkarte.")
                        SectionCard {
                            Toggle("Erinnerungen aktivieren", draft.remindersEnabled) { onDraftChange(draft.copy(remindersEnabled = it)) }
                            Check("Tag 21", draft.reminder21, draft.remindersEnabled) { onDraftChange(draft.copy(reminder21 = it)) }
                            Check("Tag 25", draft.reminder25, draft.remindersEnabled) { onDraftChange(draft.copy(reminder25 = it)) }
                            Check("Tag 27 · Empfehlung", draft.reminder27, draft.remindersEnabled) { onDraftChange(draft.copy(reminder27 = it)) }
                            Entry("Uhrzeit (HH:mm)", draft.reminderTime, { onDraftChange(draft.copy(reminderTime = it)) })
                            Text("Europe/Berlin · nach dem letzten erfolgreichen Einlesen", style = MaterialTheme.typography.bodySmall)
                        }
                        Note("Empfehlung: Fahrerkarte an Tag 27 erneut einlesen. Diese Empfehlung bleibt auch ohne Benachrichtigungen sichtbar.")
                        Text("Es werden in der Vorschau noch keine Benachrichtigungen eingerichtet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    6 -> {
                        Text("Prüfe deinen Entwurf. Über „Ändern“ kommst du direkt zum passenden Schritt.")
                        val summaries = listOf(
                            "Hauptprofil" to draft.username.ifBlank { "Noch nicht eingegeben" },
                            "Zuhause" to if (draft.homeLatitude.isBlank()) "Noch nicht festgelegt · 500 Meter" else "Heimatpunkt gesetzt · 500 Meter${if (draft.address.isNotBlank()) " · ${draft.address}" else ""}",
                            "Spesen" to "Über 8 Stunden: ${draft.expenses8.money()}\nÜber 24 Stunden: ${draft.expenses24.money()}\nÜbernachtungspauschale: ${draft.overnight.money()}\nDoppelte Spesen: ${if (draft.doubleExpenses) "Ja" else "Nein"}",
                            "Arbeitszeit & Zuschläge" to "Basiswert: ${draft.hourlyRate.money()}\nBereitschaft bezahlt: ${draft.standbyPaid.yesNo()}\nPause bezahlt: ${draft.breakPaid.yesNo()}\nNachtfenster: ${draft.nightWindows.size}\nSamstag: ${draft.saturday.percent()} · Sonntag: ${draft.sunday.percent()} · Feiertag: ${draft.holiday.percent()}",
                            "Fahrerkarte" to "Noch nicht eingelesen",
                            "Erinnerungen" to if (draft.remindersEnabled) "Gewünscht: ${listOf(21 to draft.reminder21, 25 to draft.reminder25, 27 to draft.reminder27).filter { it.second }.joinToString { "Tag ${it.first}" }}\n${draft.reminderTime} · Europe/Berlin\nNoch nicht eingerichtet" else "Deaktiviert · Empfehlung für Tag 27 bleibt",
                        )
                        summaries.forEachIndexed { index, (title, body) ->
                            SectionCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { step = index; editingSummary = true }, modifier = Modifier.testTag("edit_step_${index + 1}")) { Text("Ändern") }
                                }
                                Text(body, style = MaterialTheme.typography.bodyMedium)
                                if (index == 3) draft.nightWindows.forEach { Text("${it.start}–${it.end} · ${it.percent} %", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
                if (validation != null) Text(validation, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.testTag("onboarding_error"))
            }
        }
        Surface(shadowElevation = 0.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (editingSummary) { step = 6; editingSummary = false } else step-- }, enabled = step > 0 || editingSummary, modifier = Modifier.testTag("onboarding_back")) { Text("Zurück") }
                Button(
                    onClick = { if (step == 6) onFinish() else { step = if (editingSummary) 6 else step + 1; editingSummary = false } },
                    enabled = validation == null,
                    modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp).testTag("onboarding_next"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                ) { Text(if (step == 6) "FahrerTagebuch starten" else if (editingSummary) "Zur Übersicht" else "Weiter") }
            }
        }
    }
}

private fun OnboardingDraft.withNight(index: Int, value: NightWindowDraft) = copy(nightWindows = nightWindows.mapIndexed { i, old -> if (i == index) value else old })
private fun String.money() = ifBlank { "—" } + if (isNotBlank()) " €" else ""
private fun String.percent() = ifBlank { "—" } + if (isNotBlank()) " %" else ""
private fun Boolean.yesNo() = if (this) "Ja" else "Nein"

@Composable
private fun Entry(label: String, value: String, change: (String) -> Unit, modifier: Modifier = Modifier, keyboard: KeyboardType = KeyboardType.Text, tag: String = label) {
    OutlinedTextField(value = value, onValueChange = change, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard), modifier = modifier.fillMaxWidth().testTag(tag), shape = MaterialTheme.shapes.small)
}

@Composable
private fun Amount(label: String, value: String, unit: String = "€", tag: String = label, change: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = change, label = { Text(label) }, suffix = { Text(unit) },
        singleLine = true, isError = !validAmount(value), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(tag), shape = MaterialTheme.shapes.small)
}

@Composable
private fun Note(text: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun Toggle(label: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp).toggleable(checked, role = Role.Switch, onValueChange = change), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked, onCheckedChange = null)
    }
}

@Composable
private fun Check(label: String, checked: Boolean, enabled: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp).toggleable(checked, enabled = enabled, role = Role.Checkbox, onValueChange = change), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Checkbox(checked, onCheckedChange = null, enabled = enabled)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
