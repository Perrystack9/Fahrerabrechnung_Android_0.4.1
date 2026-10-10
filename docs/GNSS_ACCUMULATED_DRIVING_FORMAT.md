# GNSS-Einzelpunkte – begrenzte Decoder-Grundlage

Stand: 10.10.2026. Die nachstehenden Decodergrenzen beschreiben die Grundlage aus PR #49/#50. Die neue Leser-/Speicheranbindung ist im [separaten Integrationsschritt](GNSS_IMPORT_IMPLEMENTATION.md) dokumentiert.

Stand der ursprünglichen Decoder-Grundlage: 10.10.2026. Ausgangsbasis ist main nach PR #45 und dem Dokumentationsabschluss PR #48. Dieser Schritt ergänzt ausschließlich einen Decoder und synthetische Unit-Tests für die vollständige Generation-2-Datei **EF GNSS_Places**. Er ist noch kein neuer Kartenimport: Leser/APDU, Importrequests, Datenbank/Schema 11, Signatur/Version und Oberfläche verwenden den Decoder noch nicht.

## Geprüfte Primärquellen

- [EU 2016/799, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Anlage 1 §§2.76–80: Grad-/Minutencodierung, GNSSAccuracy und eigener Zeitpunkt der Positionsbestimmung. ASN.1 auf Amtsblattseiten L 139/117–118 visuell geprüft.
- [EU 2018/502, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf), Anlage 1 §§2.61, 2.78–79 und Anlage 2 TCS_154/155: **kumulierte** statt ununterbrochene Lenkzeit, Tacho-Quellwert, 18-Byte-Datensatz und 252–336 Slots. ASN.1 und Tabellen L 85/14, 16, 29–30 visuell geprüft. In der Layouttabelle steht bei GNSSPlaceRecord noch 14; die Einzelbreiten 4 + 1 + 6 ergeben 11, der Gesamtdatensatz 4 + 11 + 3 = 18. Auch Dateigrößen 4538–6050 bestätigen 18 Bytes pro Slot.
- [EU 2021/1228, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), TCS_155, L 273/49: Generation 2 Version 2 hat 336 GNSSAD-Slots. Zusätzlicher Authentisierungsstatus ist eine eigene Quelle, nicht automatisch Teil des ursprünglichen GNSS_Places-Datensatzes; keine Authentisierung behauptet.
- [EU 2023/980](https://eur-lex.europa.eu/eli/reg_impl/2023/980/oj/eng), Übergangsbestimmungen zu OSNMA: keine Freigabe, den fehlenden separaten Authentisierungsbeleg als bestätigt zu behandeln.

## Datei und Datensatz

| Merkmal | Generation 2 Version 1 | Generation 2 Version 2 |
| --- | --- | --- |
| EF in DF Tachograph G2 | `0524` | `0524` |
| Kapazität | 252–336 | 336 |
| Neuestzeiger | unsigned, 2 Bytes | unsigned, 2 Bytes |
| Datensatz | 18 Bytes | unverändert 18 Bytes |
| vollständige Dateigröße | 2 + 18 × Kapazität | 6050 Bytes |

Generation 1, unbekannte Generationen, falsche Kapazitäten, abgeschnittene/überlange Dateien und Zeiger außerhalb des Rings werden abgewiesen. Eingang und ausgegebene Bytearrays sind defensive Kopien. Ausgabe folgt der Ringreihenfolge ab dem Slot nach dem Neuestzeiger; physische Slots, gleiche Zeiten und widersprüchliche Zeitreihenfolge bleiben erhalten.

| Offset im Slot | Breite | Quellfeld |
| --- | --- | --- |
| 0 | 4 | Zeitpunkt des kumulierten Drei-Stunden-Lenkzeit-Eintrags, unsigned UTC-TimeReal |
| 4 | 4 | eigener Zeitpunkt der GNSS-Positionsbestimmung, unsigned UTC-TimeReal |
| 8 | 1 | GNSSAccuracy, NMEA-HDOP × 10; keine Meterangabe |
| 9 | 3 | Latitude, signed 24 Bit, ±DDMM.M × 10 |
| 12 | 3 | Longitude, signed 24 Bit, ±DDDMM.M × 10 |
| 15 | 3 | unsigned Tacho-Quellwert in km; keine gefahrene Strecke |

Beide Zeiten bleiben getrennt, einschließlich Sekunden und unsigned Werten nach 2038. 0/FFFFFFFF bedeuten keinen bekannten Zeitpunkt. Eine fehlende Positionszeit wird niemals aus dem Drei-Stunden-Eintrag ergänzt. Die Grad-/Minutencodierung wird mit Minuten/60 umgerechnet; ungültige Minuten, Grenzen oder fehlende Positionszeit ergeben keine darstellbare Position. Gültige Nullkoordinaten und negative Werte nahe dem Äquator bleiben gültig. Unbrauchbare Felder bleiben als unveränderte Rohwerte im Datensatz erhalten; kein anderer gültiger Datensatz geht dadurch verloren.

Nur vollständig mit 00 oder FF gefüllte Slots werden ausgelassen. Ein teilweise belegter Slot bleibt erhalten, auch ohne darstellbare Position. Genauigkeit außerhalb 1–100 und Tacho-Werte außerhalb 0–9.999.999 behalten den Rohwert ohne interpretierte Kennzahl. Keine Sortierung, Deduplizierung, Route, Fahrzeug-/Tätigkeitszuordnung, Heimkehr, Arbeits-/Ruhezeit, Verstoß- oder Abrechnungsberechnung.

## Separate Authentisierungsquelle (Generation 2 Version 2)

EF **GNSS_Places_Authentication** bleibt eine eigene Datei. EU 2021/1228, Anlage 1 §§2.79a/b und 2.117a sowie die Layouttabelle L 273/48, beschreibt 336 Slots mit je 5 Bytes und einem eigenen 2-Byte-Neuestzeiger: vollständig **1682 Bytes**. Generation 2 Version 1 hat diese Quelle nicht. Die Leseranbindung hat EF **0527** gesondert anhand TCS_152, L 273/45 bestätigt; siehe Integrationsdokumentation.

| Offset im Authentisierungsslot | Breite | Quellfeld |
| --- | --- | --- |
| 0 | 4 | Zeitpunkt des kumulierten Drei-Stunden-Eintrags, unsigned UTC-TimeReal |
| 4 | 1 | aufgezeichneter PositionAuthenticationStatus |

Der Zeitpunkt entspricht `GNSSAccumulatedDrivingRecord.timeStamp`, ausdrücklich **nicht** dem eigenen Zeitpunkt der Positionsbestimmung. Der Decoder führt keinen Join durch: Ringindex, Reihenfolge oder eine ähnliche Zeit sind kein Zuordnungsbeleg. Die spätere Anbindung benötigt dieselbe physische Karte, exakte Sitzung und den entsprechenden Drei-Stunden-Zeitstempel; fehlende oder mehrdeutige Gegenstücke dürfen keine bestätigte Position ergeben.

Status `00` bedeutet aufgezeichnet „nicht authentisiert“, `01` aufgezeichnet „authentisiert“; `02`–`FF` sind reserviert und bleiben als Rohwerte mit unbekannter Interpretation erhalten. Auch `01` ist hier nur ein gespeicherter Tachographenbeleg, keine vom Decoder ausgeführte kryptografische Prüfung. Kein Status wird auf die unverbundene Positionsquelle übertragen.

Vollständige Dateilänge und Generation werden vor der defensiven Kopie geprüft. Zeiger außerhalb 0–335 werden abgewiesen. Ringreihenfolge, physische Slots, Duplikate und alle unbekannten Teilfelder bleiben erhalten. Nur vollständig mit 00 oder FF gefüllte Slots werden ausgelassen. Unbekannte Zeiten ersetzen weder einen Status noch einen anderen Zeitpunkt. 12 neue synthetische Unit-Testmethoden prüfen diese Grenzen, alle reservierten Statuswerte, volle/belegte Ringe und eingefrorene Bytes.

## Prüfungen und nächster Integrationspunkt

18 neue Unit-Testmethoden prüfen Kapazitäten/Dateigrenzen, Ringzeiger/Umbruch, Herkunft/Zeiten, unbekannte Felder, unsigned Zeiten, alle Vorzeichen, Nullkoordinaten, Pole/Datumsgrenze, unzulässige Minuten, HDOP, Tacho-Rohwerte, Füller und eingefrorene Bytes. Vollständige Android-CI bleibt erforderlich; der Abschlussnachweis steht im zugehörigen PR. Kein neuer Gerätetest-Erfolg wird aus diesen synthetischen Tests abgeleitet.

Für die spätere Leser-/Persistenzanbindung zuerst die vollständige **17-Byte-Anwendungsidentifikation** und Kapazität an Offsets 11–12 prüfen; die bestehende Lesung verwendet bisher nur den für ihre unterstützten Quellen benötigten 15-Byte-Präfix. Separaten Authentisierungsstatus, unabhängige vollständige EF-Lesung, bestätigte physische Karte, eingefrorene Bytes, exakte eigene Sitzung, atomare Persistenz, historische Requests, leere/fehlende/fehlerhafte Quelle, Rollback und Migration gesondert absichern. Erst dann gezielte Update-ZIP und Karten-/Lesertest, bevor dieser Import-Schritt gemergt wird. Keine Smartphone-Ortung oder künstliche Verbindungsroute.
