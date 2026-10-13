# Fahrzeuge - aktuelle Daten- und Assetregeln

**DETAIL-AKTIV - bei Fahrzeugarbeit lesen.** [CURRENT_RULES.md](CURRENT_RULES.md) bestimmt den Umfang. Diese Regeln planen keine feste Bildschirmposition und behaupten keine neue Implementierung.

## Belegte Daten

Physische Fahrzeugidentität, Kennzeichen/Staat, VIN, Nutzungszeiten und Tacho-Quellwerte unverändert erhalten. Marke, Modell, Baureihe und Antrieb nur bei belastbarer Zuordnung anzeigen; unbekannte Angaben bleiben unbekannt. Fahrtenschreiber-Hersteller aus VehicleUnits_Used ist kein LKW-Hersteller. Keine erfundenen Kilometer, offenen Endzeiten oder Modellzuordnungen.

Fahrzeugbelege gehören zur exakt ausgewählten Karte/Anwendung/Sitzung. Keine jüngere Quelle als Ersatz, keine Addition beider Anwendungsbestände. Gleiche belastbar erkannte Identität überall konsistent darstellen. Technische Fehler/fehlende Quellwerte nicht verdecken.

## Bildauswahl

| Belastbare Zuordnung | Darstellung |
|---|---|
| Exaktes Modell sicher | Passendes echtes, rechtlich nutzbares Modellbild |
| Hersteller sicher, Modell offen | Herstellerlogo |
| Hersteller ebenfalls offen | Neutrales LKW-Symbol |

Keine KI-Modellbilder, geratenen Modellbilder, Laufzeit-Bildersuche oder Hotlinks. Kuratierte Assets lokal bündeln oder kontrolliert laden/cachen, Lizenzen vor Auslieferung klären. Bilder passend optimieren, Perspektive/Größe vereinheitlichen und ohne Verzerrung darstellen.

## Spätere Gestaltung

Bestehendes Branding/Designsystem als Grundlage verwenden. Fahrzeuge können in Kalender, neutraler Auswertung und Historie erscheinen; endgültige Navigation, Kartenproportionen, Antriebs-Badges und Layout erst mit der neuen Oberfläche festlegen.

Die frühere vollständige Layout-/Beispielvorgabe ist in Git nachvollziehbar, kein aktueller Umsetzungsauftrag. Ein Fahrzeugbild beweist keine Strecke; für spätere GNSS-Verbindungen gelten die aktuellen Punkt-/Verlaufsregeln.
