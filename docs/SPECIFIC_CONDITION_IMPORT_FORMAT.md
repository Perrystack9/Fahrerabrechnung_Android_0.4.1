# Fähre/Zug und Out of Scope – begrenzte Kartenquelle

Ausgangsbasis ist PR #43 auf main (`10eefb6d8b33f119948349d3ffea63f0c55c25cf`): Schema 10 und vollständig grüne CI mit 262 Tests. Letzte bestätigte Hardwarebasis bleibt PR #41 vom 03.10.2026, 08:06 Uhr. Der Folgeschritt ergänzt ausschließlich EF Specific_Conditions, die verschlüsselte eigene Quelle, deren exakten Sitzungsverweis und eine read-only Ansicht in den bestehenden Fahrerkarten-Details. Timeline-Marker dieser neuen Quelle, Zeitintervalle und Fachberechnungen gehören nicht zu diesem Schritt.

## Primärquellen und Formatprüfung

- [EU 2016/799, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Anhang IC Anlage 1 §§2.112, 2.152–2.154; Anlage 2 TCS_150/154/155. ASN.1 und Layouttabellen der Seiten 136–137, 220, 222 und 224 wurden visuell geprüft.
- [EU 2018/502, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf), Korrektur der Generation-2-Anwendungsidentifikation: der Kapazitätswert bezeichnet weiterhin die Anzahl speicherbarer Sonderbedingungen.
- [EU 2021/1228, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Anlage 2 TCS_152/154/155: Generation 2 Version 2 mit 112 Slots und 562 Bytes. Tabellen auf Seiten 45, 47 und 49 geprüft.

| Merkmal | Generation 1 | Generation 2 Version 1 | Generation 2 Version 2 |
| --- | --- | --- | --- |
| EF in der ausgewählten Anwendung | `0522` | `0522` | `0522` |
| Kapazität | fest 56 | 56–112 | fest 112 |
| Kapazitätsfeld | keines | Application_Identification, Byteoffset 13–14 | dasselbe Feld |
| Header | keiner | 2-Byte-Neuestzeiger | 2-Byte-Neuestzeiger |
| vollständige Dateilänge | 280 | 2 + 5 × Kapazität | 562 |

Jeder Slot enthält vier Bytes unsigned UTC-TimeReal und ein unverändertes Typbyte. Physische Slotpositionen bleiben erhalten. Generation 1 hat keinen Ringzeiger; Generation 2 wird nach dem belegten Neuestzeiger zyklisch geöffnet. Die Werte 0 und FFFFFFFF werden als unbekannter Zeitpunkt behandelt, niemals durch Importdatum oder Tätigkeit ersetzt. Vollständig ungenutzte Zero-/Unknown-Slots erfinden keine Meldung. Nichtleere Datensätze behalten unbekannte Zeiten oder reservierte Typen.

Codes 01/02 bedeuten den aufgezeichneten Beginn bzw. das Ende von Out of Scope. Code 03 bezeichnet bei Generation 1 eine Fähre-/Zugmeldung ohne codiertes Ende; bei Generation 2 den Beginn. Nur Generation 2 gibt Code 04 die Bedeutung eines Fähre-/Zugendes. Andere Codes bleiben neutral als unbekannte Sonderbedingung sichtbar. Keine Paarung zu Intervallen, Ruhefeststellung, Heimkehr, Verstoßbewertung oder Abrechnung.

## Import, Persistenz und Grenzen

Die neue Datei wird nach Identifikation unabhängig von den bisherigen fünf Dateien vollständig gelesen. Kapazitäts-/Zeiger-/Längenfehler oder Lesefehler betreffen nur diese Quelle. Cancellation wird weitergereicht; der Leser wird stets geschlossen. Eine vollständige leere Datei ist eine gültige Quelle mit null Meldungen.

Schema 11 ergänzt `card_specific_condition_imports` und `driver_import_specific_condition_sources`. Die bisherigen 22 Entities bleiben unverändert. Additive Migration 10→11 ohne Backfill oder destruktive Migration. Die Rohquelle wird anhand physischer DriverCard-ID, Generation, Kapazität und vollständiger Bytes dedupliziert. Quelle, konkrete Sitzung, normalisierte bisherige Daten und Importergebnis bleiben in derselben bestehenden Transaktion. Änderungen werden als neue Quellen erhalten, nicht rückwirkend ersetzt.

Ältere Zwei-/Drei-/Fünf-Datei-Requests behalten Status, Umfang und Fingerprint. Ohne tatsächlichen damaligen Leseversuch wird kein neuer Dateistatus ergänzt. Erkennbar fremde und unbestätigte Karten schreiben nichts; Folge-/Ersatzkarten behalten getrennte physische Kartenidentitäten. Altarchive werden nicht zugeordnet. Das Öffnen zeigt nur die Quelle der ausgewählten eigenen Sitzung; ältere Imports erhalten keine neueren Dateien.

16 neue Unit-, neun SQLCipher- und fünf Compose-Testmethoden prüfen Parser/Generation/Slots, vollständige unabhängige Lesung und Abbruch, eingefrorene Quellbytes/Bestätigung, Deduplizierung, Teilfehler, historische Requests, physische Bindung, atomaren Rollback, Migration, Korruption und echte Navigation. Die vollständige CI muss am endgültigen Kopf einschließlich echtem Schemaexport grün sein, bevor eine Test-APK ausgegeben wird. Danach ist wegen neuer EF-Lesung und Migration der [Gerätetest](SPECIFIC_CONDITION_DEVICE_TEST.md) erforderlich. Bis zu dessen Bestätigung bleibt der PR ungemergt.
