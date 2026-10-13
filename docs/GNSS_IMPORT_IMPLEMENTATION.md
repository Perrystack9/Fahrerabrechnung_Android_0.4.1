> **Produktentscheidung 12.10.2026 – GNSS-Darstellung und neutraler Umfang.** Dieser Decoder-/Import-/Detailschritt erzeugt weiterhin keine Route. Das ist seine Implementierungsgrenze, kein Verbot der späteren Karte: echte Karten-GNSS-Punkte dürfen chronologisch mit klar approximierten Linien verbunden werden, ohne gefahrene Straßenroute/erfundene Messpunkte. Heimatstandort, Heimkehr/Übernachtung, Spesen, Lohn/Zuschläge und Abrechnung entfallen. Neutrale Tätigkeitsdauern/Auswertung bleiben erlaubt. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# GNSS-Leser und exakte Importspeicherung

Stand 10.10.2026. Folgeschritt auf den offenen, vollständig CI-geprüften Decoder-PRs #49/#50. Main und diese Decoderköpfe bleiben unverändert. Integration benötigt eigene vollständige CI und den [Gerätetest](GNSS_IMPORT_DEVICE_TEST.md).

## Geprüfter Dateizugriff

Primärquelle: [EU 2021/1228, offizielles JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Tabelle TCS_152, Amtsblatt L 273/45: EF GNSS_Places **0524**, EF GNSS_Places_Authentication **0527**. Dateikennung visuell geprüft. Die vollständige Basis-Anwendungsidentifikation hat 17 Bytes (L 273/46); GNSS-Kapazität steht an Offsets 11–12. Das V2-Erweiterungs-EF 0525 bleibt separat und liefert diese Kapazität nicht.

Nach gültiger Identifikation werden beide EFs unabhängig und vollständig gelesen und validiert. Positionen: Gen2 252–336 Slots, Gen2V2 genau 336. Authentisierung: ausschließlich Gen2V2, 336 Slots. Gen1 versucht keines dieser EFs. Lesefehler, Kürzung, ungültige Kapazität/Zeiger verwerfen nur die betroffene Quelle. Cancellation propagiert, Leser wird geschlossen. Vollständig leere gültige Dateien sind speicherbare Quellen mit null Aufzeichnungen.

## Herkunft und Migration

Schema 11→12 ergänzt ausschließlich `card_gnss_imports` und `driver_import_gnss_sources`. Vollständige Rohdatei, Generation, Kapazität, physische Karte und Quellart gehen in den Fingerprint ein. Die zusammengesetzten Fremdschlüssel erzwingen Karte/Art/Quelle. Quellen und exakte Sitzungslinks werden in derselben Transaktion wie alle übrigen ausgewählten Quellen gespeichert. Wiederholte Bytes werden dedupliziert; Ersatzkarten bleiben eigene Quellen. Unbestätigte/fremde Karten erzeugen keine Quelle.

Historische Requests ohne GNSS behalten ihren bisherigen Fingerprint und ihre Dateiabdeckung; es gibt keinen Backfill. Laden verwendet ausschließlich den ausgewählten Sitzungslink und prüft den Quellfingerprint erneut. Keine Suche nach neuester GNSS-Datei als Ersatz. Das verschlüsselte Bestandsupgrade und der Schemaexport werden in der echten CI geprüft; `12.json` muss aus KSP stammen.

Die Detailansicht zeigt Punkte und Statusaufzeichnungen unabhängig. Eigene Positionszeit mit Sekunden; fehlende Position/Zeit/HDOP bleiben unbekannt. HDOP ist keine Meterangabe. Keine Ringindex-Zuordnung zwischen Dateien, Route, Tätigkeits-, Heimkehr- oder Vertrauensentscheidung.

## Verifikation

15 neue Testmethoden: 5 Reader-Unit-Tests, 7 SQLCipher-Pipeline-Tests, 1 Room-Schema-Migrationstest und 2 Compose-Tests. Prüfen Generationen/volle Basisidentifikation, unabhängige Ausfälle, ungültige Kapazität/Zeiger, Cancellation, historische Wiederholung, genaue Sitzung nach Neustart, Deduplizierung, physische Ersatzkarte, leere Quelle, unbestätigte/fremde Karte, atomaren Rollback, Upgrade ohne Backfill, falsche Fremdschlüssel und beschädigte gespeicherte Bytes. Erster Lauf 38027322333 auf f9911fd05a4915fe054e460361e7117374ce4dd2: alle sieben Gradle-Kommandos, Lint, APKs und stabiles Updatepaar erfolgreich. Ausschließlich der noch nicht eingecheckte KSP-Export 12.json ließ den unveränderten Schema-Guard scheitern. Dieser Export (SHA-256 2b54a30e7683e0c1a3a66407ebae709b976be812b677ae242214b9ac1a69403a) ist unverändert übernommen; alle 24 alten Entity-Exporte sind bytegleich und der Migrations-SQL entspricht seinen neuen Spalten/Fremdschlüsseln. Die vollständige CI einschließlich Emulator des Folgekopfs ist noch offen; Ergebnis im PR nachtragen.

Version 40110 behält Testpaket und öffentliche Testsignatur. Das CI-Updatepaar verwendet den gemergten PR-45-Produktstand 9f24a48845367502e585c87b1eb8e63f370b3dfb, Schema 11/40109, als Basis. Normale erfolgreiche CI-Läufe laden weiterhin keine Artefakte hoch. Gezielte ZIP bleibt manuell und durch vollständig grüne CI geschützt.
