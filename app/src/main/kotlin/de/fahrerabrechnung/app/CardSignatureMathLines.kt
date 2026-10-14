package de.fahrerabrechnung.app

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.data.card.*

/** The caller first matches the exact current card, child, EF and frozen receipt bytes. */
@Composable
internal fun CardSignatureMathLines(label: String, result: CardSignatureVerification?, error: Boolean) {
    Text("$label · Signaturprüfung: " + when {
        error -> "nicht prüfbar · gespeicherte Quellen oder Prüfung nicht verfügbar"
        result == null -> "wird geprüft"
        result.status == CardSignatureMathStatus.VALID -> "mathematisch gültig"
        result.status == CardSignatureMathStatus.INVALID -> "mathematisch ungültig"
        result.status == CardSignatureMathStatus.NOT_CHECKABLE -> "nicht prüfbar · ${result.reason.mathMessage()}"
        else -> "wird geprüft"
    })
    Text("$label · Zertifikatsvertrauen: nicht geprüft")
}
