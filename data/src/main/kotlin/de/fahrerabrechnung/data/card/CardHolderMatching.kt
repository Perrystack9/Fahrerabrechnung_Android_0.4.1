package de.fahrerabrechnung.data.card

import java.time.LocalDate

enum class CardHolderMatch { SAME, UNCLEAR, DIFFERENT }

/** Exact, complete source fields only. No fuzzy names, prefixes or card-number identity. */
object CardHolderMatching {
    fun compare(candidate: DriverIdentity, known: List<DriverIdentity>): CardHolderMatch {
        if (complete(candidate) && known.any { complete(it) && same(candidate, it) })
            return CardHolderMatch.SAME
        if (complete(candidate) && known.isNotEmpty() && known.all {
                complete(it) && candidate.birthDate != it.birthDate &&
                    (candidate.surname != it.surname || candidate.givenNames != it.givenNames)
            }) return CardHolderMatch.DIFFERENT
        return CardHolderMatch.UNCLEAR
    }

    private fun complete(holder: DriverIdentity): Boolean =
        fullName(holder.surname) && fullName(holder.givenNames) && holder.birthDate != null

    // A full 35-character card field may be truncated; never use it for an automatic match.
    private fun fullName(value: String) = value.isNotBlank() && value.length < 35 &&
        value.none { it == '\uFFFD' || it.isISOControl() }

    private fun same(a: DriverIdentity, b: DriverIdentity) =
        a.surname == b.surname && a.givenNames == b.givenNames && a.birthDate == b.birthDate
}

enum class CardBindingKind { FIRST_CARD, KNOWN_CARD, SAME_HOLDER, CONFIRM_REPLACEMENT, FOREIGN_CARD }

data class CardBindingReview(val kind: CardBindingKind, val profileName: String?) {
    val canSave get() = kind == CardBindingKind.KNOWN_CARD || kind == CardBindingKind.SAME_HOLDER
}

class CardBindingRequired : IllegalStateException("Explicit own-card confirmation required")
class ForeignCardRejected : IllegalStateException("Card cannot be assigned to the local driver")

internal fun holder(surname: String, givenNames: String, birthDateIso: String?) =
    DriverIdentity(surname, givenNames, birthDateIso?.let(LocalDate::parse), "")
