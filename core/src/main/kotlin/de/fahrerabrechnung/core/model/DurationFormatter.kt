package de.fahrerabrechnung.core.model

import java.time.Duration

object DurationFormatter {
    fun hoursMinutes(duration: Duration): String {
        require(!duration.isNegative) { "Duration must be non-negative" }
        val totalMinutes = duration.toMinutes()
        return "%d:%02d h".format(totalMinutes / 60, totalMinutes % 60)
    }
}
