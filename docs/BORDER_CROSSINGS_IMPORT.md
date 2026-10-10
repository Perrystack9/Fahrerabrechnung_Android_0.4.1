# Grenz-/Ländereinträge: unabhängige Gen2V2-Kartenquelle

Stand 10.10.2026. Ausgangsbasis: der nach Tobis Gerätetest unverändert gemergte PR #52, main `4d5b101eda225e6b2437f184bff3dc6f25b544ff`, Schema 13/Testversion 40111. Quellkopf `3a1c32d371d748cea0167b235dfe2470b5916583`, Tree `511f84f4c68b50b24f5016d68e5c78ae3f1074f6`, CI 38052970906 Versuch 1 und Paket 38054791654 erfolgreich. Die bestätigte Anzeige von Beginn-/Ende-Orten auch nach Kartenentnahme gilt PR52, nicht dieser Erweiterung.

## Primärspezifikation und begrenzter Umfang

[EU 2021/1228, amtliches JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Appendix 1 2.11a/2.11b (L 273/27–28), DriverCardApplicationIdentificationV2 2.61a (31), GNSSPlaceAuthRecord 2.79c (34–35), Appendix 2 TCS_152 und TCS_154/155 (45, 48–49). Die relevanten Tabellen wurden vor Anbindung visuell mit dem vorbereiteten PDF geprüft. EF Border_Crossings ist 0528 und nur für Gen2V2-Fahrerkarten unterstützt; keine Werkstattkarten.

| Feld | Bytes | Offset im Datensatz |
|---|---:|---:|
| countryLeft | 1 | 0 |
| countryEntered | 1 | 1 |
| GNSS-Positionszeit | 4 | 2 |
| HDOP | 1 | 6 |
| signierte Breite / Länge, Grad-Minuten | 3 + 3 | 7 / 10 |
| aufgezeichneter GNSS-Authentisierungsstatus | 1 | 13 |
| Kilometerstand als Kartenwert | 3 | 14 |

Vollständige Datei: 2-Byte-Zeiger plus 1120 × 17 Bytes = 19042 Bytes. Kapazität aus der bereits gelesenen 10-Byte-EF Application_Identification_V2 (0525): 2-Byte-Folgelänge 8, danach 2-Byte-Kapazität an Offset 2–3. Eine ungültige neue Kapazität betrifft nur Border_Crossings. Ringreihenfolge beginnt nach dem neuesten physischen Slot; alle vollständigen Einträge einschließlich Duplikaten und ungeordneten Zeiten bleiben erhalten. Nur vollständig mit 00 oder FF gefüllte Slots werden ausgelassen. Unbekannte Einzelwerte bleiben unverändert im Rohdatensatz, ohne erfundene Zeit/Position. Gen1/Gen2 versuchen diese Datei nicht.

Die Datei enthält ausdrücklich auch Ländereinträge beim Einlegen der Karte. `countryLeft=00` bedeutet keine Information; `countryEntered=FF` bedeutet Rest der Welt, nicht fehlendes Land. Deshalb heißt die Ansicht „Grenz- und Ländereinträge“ und weist Karteneinlage als mögliche Herkunft aus. Keine automatische Behauptung, jeder Datensatz beweise einen Grenzübertritt. Der GNSS-Status ist im Datensatz enthalten, wird nicht mit einer anderen Statusdatei verbunden und ist keine kryptografische Prüfung dieser App.

## Herkunft, Migration und Anzeige

Unabhängige EF-Lesung nach validierter eigener Kartenidentifikation, vollständige Bytes vor Freigabe, getrennte Lesefehler/ungültige/leere Quelle. Cancellation wird weitergegeben und der Leser geschlossen. Verschlüsselte vollständige Rohdatei und Sitzung werden atomar gespeichert. Fingerprint enthält konkrete physische DriverCard-ID, Generation, Kapazität und vollständige Bytes. Zusammengesetzte Fremdschlüssel verhindern einen Link zu einer anderen Karte. Idempotenter Request prüft dieselben Quellen; ein neuer Request darf gleiche Quellen dedupliziert verwenden. Veränderte Fassungen und Ersatzkarten bleiben getrennt.

Schema 13→14 ergänzt ausschließlich `card_border_crossing_imports` und `driver_import_border_crossing_sources`. Keine Änderung alter Tabellen, Umschreibung oder Rückfüllung. Alte Requests ohne versuchte Grenzdatei behalten ihren Fingerprint und ihre Dateiabdeckung. Keine spätere Quelle als Ersatz in älteren Sitzungen oder im Archiv. Fremde/unbestätigte Karten bleiben temporär.

Die Detailansicht nutzt ausschließlich die ausgewählte eigene Sitzung. Positionszeit, Länderwerte, einzelne Position, HDOP, aufgezeichneter Status und Kilometerstand bleiben Quellwerte. In Portionen von 20 Einträgen nachladen; Quellenwechsel setzt die Portion zurück. Die unselektierte Vorschau darf den letzten eigenen Import laden; eine explizit ausgewählte Sitzung darf nicht durch spätere Daten ersetzt werden. Keine Timeline-Marker, Route, Kilometerdifferenz, Arbeits-/Ruhe-/Heimkehr-, Reise-/Spesen- oder Vertrauensentscheidung. Kein Smartphone-Standort und kein Card_Download-Schreiben.

## Prüfung und notwendiger Gerätetest

16 vorbereitete Parser-Tests weitergeführt, fünf Reader-Tests, sieben echte SQLCipher-Quellen-/Rollback-/Upgrade-Tests, ein Room-Migrationstest und drei Compose-Tests ergänzt. Vollständige CI einschließlich SQLCipher/Room/Compose am finalen Kopf und echter KSP-Export 14.json sind vor Ausgabe nachzuweisen. Kein handgeschriebener Schemaexport. Aktueller Nachweis im Draft-PR und WORK_HANDOFF.

Testversion 40112, unverändertes Paket `de.fahrerabrechnung.app.devicetest` und festes öffentliches Testzertifikat. CI prüft das Schema-13/40111→Schema-14/40112-Paar; ausgegebene ZIP enthält ausschließlich das Update sowie Anleitung, Buildherkunft und Prüfsummen. [Gerätetest](BORDER_CROSSINGS_DEVICE_TEST.md) vor Merge erforderlich. Keine erfolgreiche Hardwareprüfung dieser neuen Quelle behaupten.
