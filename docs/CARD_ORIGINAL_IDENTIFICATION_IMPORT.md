> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Originale Identifikationsdateien je Übernahme

Originalbytes von Application_Identification, Identification und der V2-Erweiterung sind eigene verschlüsselte Quellen. Normalisierte Karte und Fahrerprofil ersetzen diese Originaldateien nicht.

## Primärformat und Lesung

- [EU 2016/799](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Anhang IC, Anhang 1, 2.61/2.62 und Anhang 2: EF0501 ist für Gen1 10 Bytes lang, für die ursprüngliche Gen2-Struktur 15 Bytes; EF0520 enthält 65 Bytes Kartenidentifikation und 78 Bytes Karteninhaberidentifikation.
- [EU 2018/502](https://eur-lex.europa.eu/eli/reg_impl/2018/502/oj/eng) ergänzt die eigenständige Fahrtenschreiber-Kapazität der vollständigen 17-Byte-Gen2-Anwendungsdatei. Eine vollständige ältere 15-Byte-Datei erhält dafür keinen geratenen Wert.
- [EU 2021/1228](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), 2.61a: EF0525 enthält fünf unabhängige unsigned 16-Bit-Felder, insgesamt 10 Bytes: folgende Länge, Grenz-/Ländereinträge, Be-/Entladeeinträge, Ladungsart-Einträge und VU-Konfigurationslängenbereich. Die bekannte folgende Länge ist 8. Eine vollständig mit 00 oder FF belegte V2-Datei bleibt als leere Originalquelle mit null dekodierten Datensätzen erhalten.

Der Leser verwendet die bereits vollständig gelesenen Bytes, kopiert sie vor jeder weiteren suspendierenden Operation und wählt die betreffende Datei für eine einzelne Endprobe erneut aus. Nur eine Antwort **ohne Daten** auf `READ BINARY(offset = Länge, Le = 1)` mit 6700, 6B00 oder 6282 beweist die Grenze. Eine erfolgreiche Antwort, ein zusätzlicher Bytewert, eine Antwort mit Fehlerdaten, 6C00/6C01, Timeout oder ein unbekannter Status beweist kein Dateiende. Für diese Probe bleibt die allgemeine 6C-Le-Korrektur ausgeschaltet. Gen2 kann dadurch keine 18-Byte-Datei als vollständige 17-Byte-Quelle speichern; dasselbe gilt für Gen1 und die Identifikationsdatei.

Kartenerkennung und andere Quellen bleiben bei einer fehlgeschlagenen Endprobe erhalten. Unbekannte Karten und fehlgeschlagene Identifikation erzeugen keine Originalquellen. Die Originaldatei `Identification` muss exakt dieselbe dekodierte Karte einschließlich Karteninhaber und Gültigkeitsangaben beschreiben wie das validierte Leseergebnis. Kapazitätswerte werden dagegen unverändert aufbewahrt: Ein Wert außerhalb der unterstützten Grenzen verwirft weiterhin die abhängige Aktivitäts-/Ringdatei, aber nicht die strukturell gültige Originalangabe.

## Speicherung und Ansicht

Die drei neuen Arten verwenden den eigenen Namensraum `IDENTITY_SOURCE_*` in den vorhandenen Schema-18-Metadatentabellen. Der Quellenfingerprint enthält physische Karte, Art, Generation, Länge und alle Originalbytes. Die kombinierten Fremdschlüssel und die gesamte Übernahmetransaktion gelten unverändert. Ein Wiederholungsauftrag prüft die genaue Vereinigung aller Quellen; eine unveränderte neue Übernahme teilt Quellenbytes, erhält aber ihre eigene Sitzung. Fremde oder unbestätigte Karten schreiben weiterhin keine Quellen.

Historische Übernahmen mit 19 oder 23 Quellen behalten ihre bisherigen Statuszeilen und Fingerprints. Sie erhalten weder Originaldateien aus der normalisierten Karte noch aus einer späteren Übernahme. Neue Gen2V2-Vorgänge können 26 unterstützte Quellen enthalten. Das ist keine Behauptung vollständiger Kartenabdeckung: Gen1-Spiegelanwendung, Downloadsignaturen, kryptografische Vertrauensprüfung, VU-Konfigurationsdateien und ein vollständiger signierter Export sind gesonderte Arbeit.

`Fahrerkarte → gespeicherte Daten → Fahrerkarten-Details → Kartenangaben` zeigt die exakt ausgewählte Originalquelle. Karteninhaber, Aussteller und aufgezeichnete Gültigkeit stammen aus dieser Datei. Die Kartennummer ist zunächst verborgen; Quelle-, Art-, Sitzungs- und Ansichtswechsel verbergen sie wieder, auch bei identischer deduplizierter Quelldatei. Fehlende Originaldaten werden ausdrücklich als fehlend angezeigt. Anwendungsangaben heißen Speicherkapazitäten; sie sind keine Anzahl übernommener Einträge. Kartenvalidität und Kapazitäten ergeben keine Aktivitätszeitspanne, keine Arbeits-/Ruhezeit und keine Importzeit.

## Prüfumfang und Ausgabestatus

44 zusätzliche Testmethoden: 8 Parser-, 5 Endproben-, 6 tatsächliche Leser-, 14 SQLCipher-/Room- und 11 Compose-/AppRoot-Tests. Sie prüfen Originalbytes, alle Generationen, die ältere 15-Byte-Struktur, unabhängige Kapazitäten, 00/FF-V2-Dateien, zusätzliche Bytes, fehlerhafte Endantworten, Abbruch, Fremdkarten, Ersatzkarten, gleichzeitige Importe, exakte Wiederholungen, Rollback, verschlüsselte Wiederöffnung, beschädigte Quellen sowie historischen und aktuellen Ansichtswechsel. Alle bisherigen Tests bleiben erhalten.

Lokale Projektgrenzen, CI-Regressionsprüfungen und Shellsyntax sind geprüft. Die vollständige Android-/Emulator-CI am endgültigen Produktkopf ist vor jeder APK-Ausgabe erforderlich. Ein neuer Gerätetest ist noch nicht bestätigt; beide Importentwürfe bleiben bis zur Rückmeldung ungemergt. [Gebündelter Gerätetest](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md).

Der gemeinsame Folgestand ergänzt zusätzlich das unabhängige [MF-Anwendungsverzeichnis](CARD_DIRECTORY_IMPORT.md). Damit können neue Gen2V2-Vorgänge27 Quellen enthalten; die bisherigen26-Quellen-Vorgänge behalten ihren exakten Stand. Die oben beschriebenen Originaldateien und ihre44 Tests bleiben erhalten.

## HISTORISCH - Entstehungsbezug

Begrenzter Folgeschritt zum Zertifikatsimport: Die bereits zur Kartenerkennung gelesenen Originalbytes von `Application_Identification` und `Identification` sowie die unabhängige V2-Erweiterung werden als eigene verschlüsselte Quellen gespeichert. Die normalisierte Karte und das Fahrerprofil ersetzen diese Dateien nicht. Schema 18 bleibt unverändert; Testversion 40120 hat dieselbe Testkennung und Signatur wie der bestätigte Stand 40117.
