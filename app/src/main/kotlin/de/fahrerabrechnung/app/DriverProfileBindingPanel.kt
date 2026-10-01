package de.fahrerabrechnung.app

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.fahrerabrechnung.data.card.*
import kotlinx.coroutines.launch

/** Confirmation is specific to this freshly read card; closing the dialog writes nothing. */
@Composable
internal fun DriverProfileBindingPanel(card: DriverCard, repository: DriverCardImportRepository,
                                       onReview: (CardBindingReview?) -> Unit) {
    key(card, repository) { BindingPanelForCard(card, repository, onReview) }
}

@Composable
private fun BindingPanelForCard(card: DriverCard, repository: DriverCardImportRepository,
                                onReview: (CardBindingReview?) -> Unit) {
    var review by remember(card) { mutableStateOf<CardBindingReview?>(null) }
    var showConfirmation by remember(card) { mutableStateOf(false) }
    var busy by remember(card) { mutableStateOf(false) }
    var error by remember(card) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val latestOnReview by rememberUpdatedState(onReview)
    LaunchedEffect(card, repository) {
        latestOnReview(null)
        try {
            review = repository.review(card)
            latestOnReview(review)
        } catch (_: Exception) { error = "Profilzuordnung konnte nicht geprüft werden. Speichern bleibt gesperrt." }
    }
    review?.let { state ->
        state.profileName?.let { Text("Fahrerprofil: $it") }
        when (state.kind) {
            CardBindingKind.FIRST_CARD -> Text("Diese Karte ist noch keinem lokalen Fahrerprofil zugeordnet.")
            CardBindingKind.KNOWN_CARD -> Text("Diese Karte ist deinem Fahrerprofil zugeordnet.")
            CardBindingKind.SAME_HOLDER -> Text("Die vollständigen Inhaberdaten stimmen überein. Beim Speichern wird diese Karte als zusätzliche Folgekarte zugeordnet.")
            CardBindingKind.CONFIRM_REPLACEMENT -> Text("Die Inhaberdaten sind nicht eindeutig zuzuordnen. Bestätige ausdrücklich, wenn dies deine eigene neue oder Ersatzkarte ist.")
            CardBindingKind.FOREIGN_CARD -> Text("Diese Karte gehört nicht zum gebundenen Fahrerprofil. Nur temporäre Vorschau; Speichern und Zuordnen sind gesperrt.")
        }
        if (state.kind == CardBindingKind.FIRST_CARD || state.kind == CardBindingKind.CONFIRM_REPLACEMENT) {
            Button(enabled = !busy, onClick = { showConfirmation = true },
                modifier = Modifier.testTag("request_card_binding")) {
                Text(if (state.kind == CardBindingKind.FIRST_CARD) "Eigene Fahrerkarte zuordnen"
                    else "Eigene Folge-/Ersatzkarte bestätigen")
            }
        }
    }
    error?.let { Text(it) }
    if (showConfirmation) AlertDialog(
        onDismissRequest = { if (!busy) showConfirmation = false },
        title = { Text("Eigene Fahrerkarte bestätigen") },
        text = {
            Text("Karteninhaber: ${card.holder.givenNames} ${card.holder.surname}\n" +
                "Geburtsdatum: ${card.holder.birthDate?.toString() ?: "nicht angegeben"}\n\n" +
                "Bestätige nur, wenn diese Karte dir gehört. Alte Karten und Teilimporte bleiben erhalten.")
        },
        confirmButton = {
            TextButton(enabled = !busy, modifier = Modifier.testTag("confirm_own_card"), onClick = {
                busy = true
                scope.launch {
                    try {
                        repository.bind(card, confirmedOwnCard = true)
                        review = repository.review(card)
                        latestOnReview(review)
                        showConfirmation = false
                        error = null
                    } catch (_: Exception) { error = "Karte konnte nicht zugeordnet werden. Bitte erneut versuchen." }
                    finally { busy = false }
                }
            }) { Text("Das ist meine Karte") }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = { showConfirmation = false }) { Text("Abbrechen") }
        },
    )
}
