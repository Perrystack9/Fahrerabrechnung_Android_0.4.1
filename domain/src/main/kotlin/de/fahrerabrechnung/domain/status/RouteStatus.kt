package de.fahrerabrechnung.domain.status

enum class RouteStatus {
    ROUTE_VOLLSTAENDIG,
    ROUTE_TEILWEISE,
    NUR_GEN2_ORTSDATEN,
    KEINE_ORTSDATEN,
    ORTSDATEN_UNKLAR,
}
