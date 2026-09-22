package de.fahrerabrechnung.data.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Boundary only. It intentionally exposes no productive GPS recording in this project-start phase. */
interface LocationSource {
    fun observeAvailability(): Flow<LocationAvailability>
}

enum class LocationAvailability { AVAILABLE, UNAVAILABLE, UNKNOWN }

class NoOpLocationSource @javax.inject.Inject constructor() : LocationSource {
    override fun observeAvailability(): Flow<LocationAvailability> = flowOf(LocationAvailability.UNKNOWN)
}
