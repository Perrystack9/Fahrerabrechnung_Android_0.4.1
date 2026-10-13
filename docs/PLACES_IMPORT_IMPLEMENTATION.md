> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Beginn-/Ende-Orte: unabhängige Kartenquelle

Implementierte unabhängige Beginn-/Ende-Ortsquellen mit genauer eigener Karte/Anwendung/Sitzung. Getrennte Authentisierungsdaten bleiben eigenständige Belege.

## Primärquellen und Format

[EU 2016/799, amtliches PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2016_799.pdf): Appendix 1, EntryTypeDailyWorkPeriod (Amtsblatt L 139/113), NoOfCardPlaceRecords (123), PlaceRecord (126), CardPlaceDailyWorkPeriod (99); Appendix 2 TCS_150/152/154/155 (218–224). Die Tabellen wurden visuell geprüft. [EU 2021/1228](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), TCS_152 (L 273/45), vollständige Basis-Anwendungsidentifikation (46), unverändertes Gen2V2-EF Places (47); authentisierungsbezogene Definitionen zusätzlich im [amtlichen BOE-Text](https://www.boe.es/buscar/doc.php?id=DOUE-L-2021-81052&lang=es) geprüft.

| Quelle | Gen1 | Gen2 | Gen2V2 |
|---|---|---|---|
| EF Places 0506 | 1-Byte-Zeiger + 84–112 × 10 Byte | 2-Byte-Zeiger + 84–112 × 21 Byte | 2-Byte-Zeiger + 112 × 21 Byte |
| EF Places_Authentication 0526 | nicht versucht | nicht versucht | 2-Byte-Zeiger + 112 × 5 Byte |

Places: Eintragszeit (4), Eintragsart (1), Land (1), Region (1), Kilometerstand (3); Gen2 zusätzlich Positionszeit (4), HDOP (1), signierte Grad-/Minutenkoordinaten (3+3). Kapazität aus Application_Identification: Gen1 Offset 9, Gen2 Offsets 9–10, nicht aus dem separaten V2-Erweiterungs-EF. Authentisierung: eigene Eintragszeit (4) und aufgezeichneter Status (1). 00 = nicht authentisiert, 01 = authentisiert, andere Codes bleiben unbekannt. Das ist keine eigene kryptografische Prüfung.

Gen1 erlaubt Eintragsarten 0–5, Gen2 zusätzlich 6/7 (Zeit auf GNSS-Basis). Auch diese Codes beweisen keine berechnete Schicht oder Ruhe. Ringreihenfolge bleibt die physische Reihenfolge nach dem neuesten Zeiger; doppelte oder zeitlich ungeordnete Einträge bleiben erhalten. Nur vollständig mit 00 oder FF gefüllte Slots werden ausgelassen. Unbekannte Zeit, reservierte Codes und ungültige einzelne Positionsfelder bleiben im Rohdatensatz erhalten. Positionszeit wird nie aus Eintragszeit ergänzt.

## Import, Herkunft und Grenzen

Beide Dateien werden nach gültiger Identifikation unabhängig gelesen; Lesefehler, Kürzung, ungültige Kapazität oder Zeiger betreffen nur diese Quelle. Cancellation wird weitergegeben und der Leser geschlossen. Auch eine gültig leere Quelle wird gespeichert. Ältere Ergebnisse ohne neue Felder bleiben unverändert.

Schema 12→13 ergänzt ausschließlich `card_place_imports` und `driver_import_place_sources`. Fingerprint über physische Karte, Quellart, Generation, Kapazität und vollständige Rohdatei; zusammengesetzte Fremdschlüssel erzwingen Karte/Art/Quelle. Bestehende Tabellen und Quellbytes bleiben unverändert. Import, Quellen und Sitzungslinks werden atomar gespeichert. Kein Backfill, keine neueste Datei als Ersatz für eine historische Übernahme. Fremde/unbestätigte Karten erzeugen keine Quelle. Echte KSP-Datei `13.json` aus Buildlauf 38052207004 übernommen (SHA-256 `fd9878a0be989af955ad51f2937412a701314dd227c5fa31432b53f130b0cf0e`). Alle 26 bisherigen Entity-Exporte unverändert; neue Tabellen, Spalten, Fremdschlüssel und Indizes entsprechen dem Migrations-SQL.

Die Detailansicht zeigt die exakt ausgewählte Ortsquelle und separat den aufgezeichneten Authentisierungsstatus. Land/Region zunächst als Kartenwerte, unbekannte Werte neutral. Keine Zuordnung der Authentisierungsdatei per Ringindex, keine Route, Arbeitszeit-, Heimkehr-, Kilometerdifferenz- oder Vertrauensentscheidung. Vorhandene GNSS-, Aktivitäts- und Fahrzeugdarstellung bleibt erhalten.

## HISTORISCH - Verifikation und Gerätetest

Neue Parser-/Reader-Unit-Tests, echte SQLCipher-Tests für Wiederöffnung, Sitzungsgenauigkeit, Deduplizierung, Ersatzkarte, Teilfehler, leere Quelle, Fremdkartenschutz, atomaren Rollback, Upgrade ohne Backfill und beschädigte Links/Bytes; Room-Migrationstest und Compose-Tests für Quellenwechsel/Unbekanntwerte. Erster Lauf 38052207004 am Kopf 6a4ffb1: alle Gradle-Prüfungen, Lint, APKs und stabiles Updatepaar erfolgreich. Ausschließlich der noch nicht eingecheckte KSP-Export ließ den Schema-Guard scheitern; Emulator wurde deshalb übersprungen. Vollständige Android-CI des Folgekopfs und Gerätetest sind noch offen; aktuellen Nachweis im PR ergänzen.

Testversion 40111 mit unverändertem Testpaket und öffentlichem Testzertifikat. CI-Updatepaar verwendet den bestätigten PR-51-Stand Schema 12/40110. Normale erfolgreiche CI lädt keine Artefakte hoch; die gezielte ZIP erst nach voller grüner CI des exakten Quellcommits. [Geräteanleitung](PLACES_IMPORT_DEVICE_TEST.md).

## HISTORISCH - Entstehungsbezug

Stand 10.10.2026. Ausgangsbasis ist der unverändert gemergte, CI-geprüfte PR #51 (main `1c73cb4d2fa4f656e069362a3e159cee12b0d884`, Schema 12/Testversion 40110). Nutzerbestätigung: GNSS-Anzeige passt und bleibt nach Wiederöffnung vorhanden. Diese Rückmeldung bestätigt keine ungenannten Fehler- oder Fremdkartentests.
