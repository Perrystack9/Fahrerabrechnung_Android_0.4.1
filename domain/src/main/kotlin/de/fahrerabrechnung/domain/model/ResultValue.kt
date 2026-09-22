package de.fahrerabrechnung.domain.model

import de.fahrerabrechnung.domain.status.DataStatus

sealed interface ResultValue<out T> {
    val status: DataStatus

    data class Complete<T>(val value: T) : ResultValue<T> {
        override val status = DataStatus.VOLLSTAENDIG
    }

    data class Partial<T>(val knownPart: T, val reason: String) : ResultValue<T> {
        override val status = DataStatus.TEILWEISE
    }

    data class Unclear(val reason: String) : ResultValue<Nothing> {
        override val status = DataStatus.UNKLAR
    }

    data object NoData : ResultValue<Nothing> { override val status = DataStatus.KEINE_DATEN }
    data object NotApplicable : ResultValue<Nothing> { override val status = DataStatus.NICHT_ANWENDBAR }
}
