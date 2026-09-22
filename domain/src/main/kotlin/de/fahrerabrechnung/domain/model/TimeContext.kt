package de.fahrerabrechnung.domain.model

import java.time.ZoneId

object TimeContext {
    val REPORT_ZONE: ZoneId = ZoneId.of("Europe/Berlin")
}
