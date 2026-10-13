> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Ereignis- und Störungsquellen – begrenzter Importschritt PR #41

Implementierte unabhängige Ereignis-/Störungsquellen mit genauer eigener Sitzung. Die neutrale Detailübersicht bewertet weder Verstöße noch Verschulden. Die getrennte Markerprojektion ist in [EVENT_FAULT_TIMELINE_CHECK.md](EVENT_FAULT_TIMELINE_CHECK.md) dokumentiert.

## Verbindliche technische Quellen

- [EU 2016/799, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): Anhang IC, Anlage 1 §§2.19–2.22, 2.70, 2.109–2.110; Anlage 2 TCS_150/151/152/154/155. Layouttabellen wurden zusätzlich visuell gelesen.
- [EU 2018/502, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf): Anlage 1 §§2.19 und 2.70 korrigiert die Generation-2-Gruppenstruktur und Typbezeichnungen. Insbesondere gelten nicht mehr die ursprünglichen GNSS-Störungscodes `50`–`56` aus dem ungeänderten 2016-PDF.
- [EU 2021/1228, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf): Anlage 1 §2.70 ergänzt Generation 2 Version 2; Anlage 2 TCS_154/155 legt deren feste Gruppenkapazitäten fest. Unbekannte/nicht anwendbare Felder sind standardmäßig `FF`, sofern kein anderer Default spezifiziert ist.

## Dateien und Struktur

| Merkmal | Events_Data | Faults_Data |
| --- | --- | --- |
| EF-Kennung in der ausgewählten Anwendung | `0502` | `0503` |
| Kapazität je Gruppe aus Application_Identification | Byteoffset 3 | Byteoffset 4 |
| Gruppen Generation 1 | 6 | 2 |
| Gruppen Generation 2 / Generation 2 Version 2 | 11 | 2 |
| Kapazität Generation 1 / Generation 2 Version 1 | 6–12 | 12–24 |
| Kapazität Generation 2 Version 2 | 12 | 24 |
| Vollständige Dateilänge | Gruppen × Kapazität × 24 | Gruppen × Kapazität × 24 |

Das bestehende Lesen der ersten 15 Bytes der Generation-2-Anwendungsidentifikation reicht für diese beiden Kapazitätsfelder aus; sie stehen nicht in Application_Identification_V2. Keine neue Anwendungserkennung. Dateien werden unabhängig nach erfolgreicher Fahreridentifikation gelesen. Ungültige Kapazitäten, fehlende/abgeschnittene Daten oder Parserfehler betreffen nur die jeweilige Datei; andere lesbare Quellen bleiben verfügbar. Abbruch wird weitergereicht, Leser stets geschlossen.

| Byteoffset pro Datensatz | Länge | Feld |
| --- | --- | --- |
| 0 | 1 | EventFaultType, unverändert erhalten |
| 1 | 4 | Beginn als unsigned TimeReal |
| 5 | 4 | Ende als unsigned TimeReal |
| 9 | 1 | Zulassungsstaat |
| 10 | 1 | Codepage des Kennzeichens |
| 11 | 13 | Kennzeichen |

Gruppen- und Slotposition bleiben erhalten; es gibt keinen Ringzeiger. Vollständig ungenutzte Standard-/Unknown-Slots erzeugen keine Meldung. Ein tatsächlich belegter Datensatz behält auch nicht zugeordnete Typcodes oder fehlende Einzelzeitpunkte. Zero-/`FFFFFFFF`-Zeiten sind unbekannt. Nichtleere Kennzeichen erfordern eine unterstützte Codepage; ein unbekanntes/ungefülltes Kennzeichen bleibt leer, ohne eine fremde Codepage zu erfinden.

Bei Zeitüberschneidung bezeichnet Beginn die Entnahme im vorherigen Fahrzeug und Ende das Einlegen im aktuellen Fahrzeug. Die Reihenfolge darf invertiert sein. Bei nicht korrekt abgeschlossener Kartensitzung bezeichnen die beiden Zeitpunkte die frühere und aktuelle Einlegung. Keine Vertauschung, Lückenfüllung, künstliche Dauer oder Ruhe-/Heimkehrableitung. Die Oberfläche zeigt generationstreue, menschenlesbare Bezeichnungen, bei unbekanntem Code einen neutralen Hinweis; die Slots werden nicht als belastbare Gesamthistorie ausgegeben.

## Persistenz, Importstatus und Grenzen

Schema 10 ergänzt ausschließlich `card_event_fault_imports` und `driver_import_event_fault_sources`. Rohdatei-ID enthält physische DriverCard, Dateiart, Generation, Gruppenkapazität und vollständige Bytes. Deduplizierung erfolgt pro physischer Karte/Dateiart; Folge-/Ersatzkarten bleiben getrennte DriverCard(s) desselben Profils. Die Sitzung verweist exakt auf die eigenen Quellen, ohne neuesten Stand aus anderen Sitzungen zu ergänzen. SQLCipher und die vorhandene Transaktion für Quellen, Normalisierung, Sitzung und Ergebnis bleiben maßgeblich. Die Migration 9→10 ist additiv ohne Backfill oder destruktive Migration.

Eine vollständig gelesene Datei ohne Meldungen ist weiterhin eine gespeicherte Quelle mit null dekodierten Meldungen. Neue Lesungen können fünf Dateiergebnisse enthalten. Ältere Zwei-/Drei-Datei-Imports behalten ihren damaligen Umfang und ihren Request-Fingerprint. Nicht versuchte Dateien werden nicht nachträglich als Fehler ergänzt. Fremde/unbestätigte Karten schreiben keine Ereignis-/Störungsquelle; unzugeordnete Archive bleiben getrennt. Eine beschädigte Rohquelle darf die Importübersicht nicht unbenutzbar machen; nur das Öffnen ihres betroffenen Vorgangs meldet den Fehler.

## HISTORISCH - damaliger Folge-/Geräteplan

Keine zusätzlichen Produkt-/Fachregeln. Vollständiger Kartenimport, kanonischer Tagesbestand, permanente Fahrzeugzuordnung, GNSS-/Grenzdateien und Timeline-Marker bleiben offen. Der nächste Gerätetest prüft das Update 40105→40106 sowie die beiden neuen echten EFs, erst nach vollständig grüner CI. PR #41 bleibt bis zur Nutzerbestätigung ungemergt.
