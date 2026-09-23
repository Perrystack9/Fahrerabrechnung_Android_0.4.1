package de.fahrerabrechnung.feature.onboarding

import androidx.compose.runtime.saveable.listSaver

/** UI-only draft. No repository, permissions, scheduling or calculation is invoked. */
data class OnboardingDraft(
    val username: String = "",
    val gpsChoice: String = "unset",
    val address: String = "",
    val expenses8: String = "",
    val expenses24: String = "",
    val overnight: String = "",
    val doubleExpenses: Boolean = false,
    val standbyPaid: Boolean = false,
    val breakPaid: Boolean = false,
    val hourlyRate: String = "",
    val saturday: String = "",
    val sunday: String = "",
    val holiday: String = "",
    val remindersEnabled: Boolean = false,
    val reminder21: Boolean = true,
    val reminder25: Boolean = true,
    val reminder27: Boolean = true,
    val reminderTime: String = "18:00",
    val nightWindows: List<NightWindowDraft> = emptyList(),
) {
    companion object {
        val Saver = listSaver<OnboardingDraft, String>(
            save = { d ->
                listOf(
                    d.username, d.gpsChoice, d.address, d.expenses8, d.expenses24, d.overnight,
                    d.doubleExpenses.toString(), d.standbyPaid.toString(), d.breakPaid.toString(),
                    d.hourlyRate, d.saturday, d.sunday, d.holiday, d.remindersEnabled.toString(),
                    d.reminder21.toString(), d.reminder25.toString(), d.reminder27.toString(), d.reminderTime,
                ) + d.nightWindows.flatMap { listOf(it.start, it.end, it.percent) }
            },
            restore = { s ->
                OnboardingDraft(
                    s[0], s[1], s[2], s[3], s[4], s[5], s[6].toBoolean(), s[7].toBoolean(), s[8].toBoolean(),
                    s[9], s[10], s[11], s[12], s[13].toBoolean(), s[14].toBoolean(), s[15].toBoolean(), s[16].toBoolean(), s[17],
                    s.drop(18).chunked(3).map { NightWindowDraft(it[0], it[1], it[2]) },
                )
            },
        )
    }
}

data class NightWindowDraft(val start: String = "", val end: String = "", val percent: String = "")

/** Form validation only, including windows crossing midnight; no paid time is computed. */
internal fun nightWindowError(windows: List<NightWindowDraft>): String? {
    val intervals = mutableListOf<Pair<Int, Int>>()
    for (window in windows) {
        val start = clockMinutes(window.start)
        val end = clockMinutes(window.end, allowEndOfDay = true)
        if (start == null || end == null || start == end) {
            return "Bitte unterschiedliche Start- und Endzeiten im Format HH:mm eingeben."
        }
        if (window.percent.isBlank() || !validAmount(window.percent)) return "Bitte den Prozentsatz für jedes Nachtfenster eingeben."
        val parts = if (end > start) listOf(start to end) else listOf(start to 1440, 0 to end)
        for (part in parts.filter { it.first < it.second }) {
            if (intervals.any { part.first < it.second && it.first < part.second }) {
                return "Nachtfenster dürfen sich nicht überschneiden. Bitte die Zeiten anpassen."
            }
        }
        intervals.addAll(parts)
    }
    return null
}

internal fun clockMinutes(value: String, allowEndOfDay: Boolean = false): Int? {
    if (allowEndOfDay && value == "24:00") return 1440
    if (!Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(value)) return null
    return value.take(2).toInt() * 60 + value.takeLast(2).toInt()
}

internal fun validAmount(value: String): Boolean = value.isBlank() || Regex("[0-9]+(?:[,.][0-9]{1,2})?").matches(value)
