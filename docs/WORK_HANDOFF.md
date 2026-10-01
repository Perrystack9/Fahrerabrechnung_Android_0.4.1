# Work-Übergabe – FahrerTagebuch

**Stand: 01.10.2026.** Diese Datei hält den technischen Fortschritt und den zuletzt vom Nutzer bestätigten Gerätetest fest. Sie ersetzt keine Fachregeln.

## Einstieg und Ausgangsbasis

1. Zuerst [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) und [CURRENT_RULES.md](CURRENT_RULES.md) lesen. Beide sind der verbindliche Produkt-/Fachstand; historische Design-/GPS-Dokumente gelten nur ohne Widerspruch.
2. Danach diese Übergabe, [WORK_STATUS.md](../WORK_STATUS.md) und [README.md](../README.md) lesen.
3. Den aktuellen `main` aus GitHub verwenden und vor der nächsten Änderung HEAD, offene PRs und CI erneut prüfen. Alte Scratch-Verzeichnisse oder ZIPs können fehlen oder älter sein.

| Bezug | Stand |
|---|---|
| Letzter Produktcode-PR | [PR #26: Kartenquellen gemeinsam atomar speichern und exakt wieder öffnen](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/26), gemergt |
| Produktcode-Basis auf main | [`fbac9310e4c5d127d7cebdd0cda1319b6880ed6d`](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/commit/fbac9310e4c5d127d7cebdd0cda1319b6880ed6d); spätere reine Dokumentationscommits ändern diese technische Basis nicht |
| PR-Kopf einschließlich Schemaexport | `307f1d4e6ee7e8ef49ee275d6aa0e417615933c8` |
| Datenbank | Room/SQLCipher, Schema **4**, Migrationen 1 → 2 → 3 → 4, Schemaexport 4.json eingecheckt |
| Offene lokale Arbeit | Keine uncommitteten Produktcode-Änderungen oder laufenden Builds aus diesem Work zu übernehmen; PR #26 ist abgeschlossen |
| Letzter manueller Gerätetest | **01.10.2026, PR #26: bestanden**, vom Nutzer bestätigt |

## Wirklich implementiert

- Siebenstufiges Onboarding; Heimatpunkt ausdrücklich per Vordergrund-Standort oder Karte wählbar, fester 500-m-Radius.
- USB-CCID/APDU, USB-Freigabe, ATR-/Slot-Diagnose und Gen1-/Gen2-/Gen2V2-Erkennung.
- Vollständiges Lesen der Aktivitäts- und Fahrzeugdatei, zyklische Tagesdatensätze/Tätigkeitswechsel und Fahrzeugnutzungen dekodieren.
- Aktivitätsintervalle auf lokale Kalendertage projizieren; fehlende Zeit und Einträge ohne Karte gesondert kennzeichnen.
- Kompakte Tagesübersicht, zoombare/verschiebbare Detailzeitleiste, Abschnittsauswahl, Zeitmarke und belegte Fahrzeug-Zeitfenster. Offene Fahrzeugenden werden nicht fortgeschrieben.
- Verschlüsselte, ausdrücklich bestätigte Einzeldateien; identische Quell-Snapshots werden dedupliziert und nach App-Neustart wieder geöffnet.
- Fahrzeughistorien-Vorschau über gespeicherte Dateien, VIN-/Kennzeichen-Suche, nachladbare Nutzungen und gezielte Navigation aus dem Kartentag. Tacho-Werte sind Quellwerte.
- Dateiübersicht mit Generation, Datensatzanzahl, maskierter Kartennummer und Quellzeitraum; ältere Aktivitäts-/Fahrzeugdateien gezielt als Vorschau öffnen.
- Startseite unterscheidet fehlende, vorhandene und nicht prüfbare Teilimporte.

**PR #26 ergänzt:** Ein tatsächlicher Lesevorgang mit beiden validierten Dateien kann über „Beide Dateien gemeinsam speichern“ ausdrücklich gesichert werden. Eine Room-Transaktion speichert beide Quellen und ihre Zuordnung. Die gemeinsame Vorschau öffnet genau diese beiden Dateien. Bestehende Einzeldateien werden nicht rückwirkend anhand von Datum oder Kartennummer gepaart.

Die Dateien und identische Dateipaare bleiben dedupliziert. Die Zeit des Dateipaars bezeichnet seine erste gemeinsame Speicherbestätigung; sie ist keine vollständige Historie jedes erneuten Leseversuchs. Getrennt gespeicherte Quellen können weiterhin als Vorschau kombiniert werden, ohne dadurch als derselbe Lesevorgang zu gelten.

## Letzter erfolgreicher Hardware-/Gerätetest

**Datum:** 01.10.2026, etwa 10:37–10:41 Uhr Europe/Berlin.  
**Gerät:** Galaxy S23, Android 16; vorhandener USB-CCID-Leser und echte Gen2V2-Fahrerkarte. Das genaue Lesermodell ist in dieser Rückmeldung nicht neu spezifiziert.

| Prüfung | Bestätigtes Ergebnis |
|---|---|
| Aktivitätsdatei | 13.776 Bytes gelesen; 74 Tagesdatensätze dekodiert |
| Fahrzeugdatei | 9.600 Bytes gelesen; 69 Nutzungen dekodiert |
| Gemeinsame Speicherung | Erfolgsmeldung „Beide Dateien gemeinsam verschlüsselt gespeichert.“ |
| Dateiübersicht | Ein gemeinsam gespeichertes Dateipaar, 74 Tagesdatensätze / 69 Fahrzeugnutzungen; Kartennummer maskiert |
| Leser entfernen und App neu starten | Nutzer bestätigte, dies bereits durchgeführt zu haben; letzter Screenshot zeigt die gespeicherte Übersicht ohne angeschlossenen Leser |
| Ergebnis | Gemeinsame Speicherung und Verfügbarkeit des Dateipaars nach Lesertrennung/App-Neustart am Gerät bestätigt |

Die Rückmeldung bestand aus drei Screenshots und der ausdrücklichen Nutzerbestätigung. Keine echten Rohdateien, VINs oder unmaskierten Kartennummern werden für diese Übergabe übernommen.

**Testgrenze:** Der Test bestätigt die vorhandene Geräte-/Leser-/Gen2V2-Kombination. Er ist keine Hardwarefreigabe für alle Generationen/Leser und kein Beweis für einen vollständigen Kartenimport. Er ersetzt auch nicht die automatischen Fehlerfalltests. Doppelt wirkende Einträge in früheren langen Handy-Screenshots waren laut Nutzer ein Screenshot-/Handyproblem; die tatsächliche App zeigte drei Fahrzeuge.

### Getestete APK

- Ausgeliefert: **Debug-APK als ZIP**, `FahrerTagebuch-PR26-Kartenquellen-Test.zip`.
- Herkunft: [grüner CI-Lauf 36568389361](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361), Artefakt `Fahrerabrechnung-0.4.1-debug-apk`, ID `11032504544`; enthält `app-debug.apk`.
- APK-Quellstand: `7a112c09770e078e78b4bcd29ef43fbdf211723f`. Der nachfolgende Commit ergänzte ausschließlich den generierten Room-Schemaexport; der App-Code ist gleich.
- ZIP-SHA256: `1520ff9a7fa12b183a9784eed8383379a6f81e2a4b0183c057899d569fd3b0d3`.
- APK-SHA256: `2e20fc3e05c4cfd1fe12278cfafad731b21ccd334008a3a777dbf9ef6a15d6da`.

Bei Bedarf diese APK über das GitHub-Artefakt wiederherstellen. Eine ältere PR-APK nicht als neue Version ausgeben. Der Nutzer benötigt APKs **als ZIP**.

## Automatische Verifikation

- [Vollständiger main-Lauf 36743029047](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36743029047) für `fbac9310`: **grün**, einschließlich Build, Unit-Tests, Lint, Debug-/Internal-APK-Verifikation und Room-/SQLCipher-/Compose-Instrumentierung.
- [Lauf 36568389361](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361) zur ausgelieferten APK: vollständig grün; **13 Daten- und 22 App-Instrumentierungstests**, keine Fehler.
- [Zusätzlicher PR-Lauf 36742530657](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36742530657) nach dem Schemaexport: Build/Tests/Lint/APK grün, Emulatorjob scheiterte vor den App-Tests bei „Enable emulator hardware acceleration“. Der danach erfolgreiche main-Lauf liefert den vollständigen aktuellen Nachweis.

Gezielte Regressionstests sichern verschlüsselte Wiederöffnung, Deduplizierung, fehlende/defekte zweite Datei, Transaktions-Rollback mit Erhalt älterer Quellen, unveränderte Aktivität bei neuer Fahrzeugdatei und v3-Migration ohne erfundene Paarung. Der Compose-Test öffnet ein älteres Dateipaar mit dessen Fahrzeugen und wechselt zurück zu den neuesten Einzeldateien.

## Wichtige Code-Einstiege

- [DriverCard.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCard.kt): Kartenmodelle, Dateidekodierung und Leserablauf.
- [CardSourcePairRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardSourcePairRepository.kt): validieren, atomar speichern, exakt zugehörige Quellen lesen.
- [CardSourcePairDao.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/CardSourcePairDao.kt): Herkunftstabelle und Fremdschlüssel.
- [CardActivityImportRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardActivityImportRepository.kt) / [CardVehicleImportRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardVehicleImportRepository.kt): Quell-Snapshots und inhaltliche Deduplizierung.
- [AppDatabase.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/AppDatabase.kt) / [DatabaseMigrations.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DatabaseMigrations.kt): Schema 4, additive Migrationen; keine destruktive Migration.
- [CardHardwareTestScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardHardwareTestScreen.kt), [CardSnapshotHistoryScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardSnapshotHistoryScreen.kt), [AppRoot.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/AppRoot.kt): lesen/speichern, Quellenübersicht und Auswahl der Vorschau.
- [EncryptedDatabaseInstrumentedTest.kt](../data/src/androidTest/kotlin/de/fahrerabrechnung/data/EncryptedDatabaseInstrumentedTest.kt) / [CardSourcePairNavigationTest.kt](../app/src/androidTest/kotlin/de/fahrerabrechnung/app/CardSourcePairNavigationTest.kt): Persistenz-/Migrations- und Navigationsregressionen.

## Noch offen und empfohlene Fortsetzung

Der Lesetest speichert weiterhin nur nach ausdrücklicher Bestätigung. Einzeldateien und Dateipaare sind **persistente Teilimporte**, noch keine vollständige Kartenimport-Pipeline.

Offen sind Profil-/Kartenbindung, vollständige Importhistorie und Status/Fehlerbehandlung über alle Kartenquellen, dauerhafter normalisierter Tagesbestand sowie weitere Dateien für GNSS, Grenzübertritte, Ereignisse/Störungen und Kontrollen. Dauerhafte Fahrzeugzuordnung, Tageskilometer/Arbeitszeiten und fachliche Reise-/Spesen-/Zuschlagsauswertung fehlen. Grundscreens und vorbereitete Entities/Interfaces sind teilweise Preview.

Empfohlener nächster Arbeitsabschnitt: den zuverlässigen vollständigen Import und sein Datenmodell weiter ausbauen, ausgehend von den gespeicherten Quellen. Einen begrenzten Folgeschritt und seine Migration/Fehlerfälle vor Umsetzung festlegen. Die aktuelle Übergabe ist kein automatischer Auftrag, sämtliche Produktpunkte sofort zu implementieren.

### Verbindliche Grenzen bei der Fortsetzung

- Keine Smartphone-Routenaufzeichnung, Hintergrundortung oder Smartphone-Kilometer; Heimatpunkt ausdrücklich per Standort/Karte.
- Echte Tachograph-/Fahrerkarten-GNSS-Punkte später einzeln zeigen; keine erfundene Verbindungsstrecke.
- Kartenentnahme/fehlende Zeit beweist keine Ruhe oder Heimkehr. Heimkehr verlangt belastbaren Heimatstandort und mindestens 8:59 h zusammenhängende belegte Ruhe, rückwirkend auf deren Beginn.
- Übernachtung, Spesen und Arbeitgeberzuschläge gemäß Issue #3/CURRENT_RULES; keine Steuerlogik. Feiertagsgrundlage ist das Bundesland des Firmenstandorts.
- Für die spätere Fahrzeugdarstellung [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md) beachten; Modell/VIN-Zuordnung oder Fahrzeugbild nie raten.
- Rohkartendaten und vollständige APDUs nicht loggen; Testdaten synthetisch, gespeicherte Kartennummern in der Oberfläche maskieren.

### Test- und Arbeitsvereinbarungen

Manueller Gerätetest **spätestens bei jedem dritten PR**, früher sobald Hardware, Import, Parser, Persistenz oder Datenbank betroffen sind. Mehrere PRs bis zu einem späteren Test nur nach konkreter Nutzerfreigabe zusammenfassen. Die zwischenzeitliche Bitte, wegen fehlendem Gerät den Test aufzuschieben, ist durch den jetzt durchgeführten PR-#26-Test erledigt und keine dauerhafte Ausnahme.

Auf dem bestandenen Test aufbauen; ihn nicht allein wegen des Work-Wechsels wiederholen. Nach neuen Änderungen die passenden automatischen Prüfungen und den nächsten erforderlichen Gerätetest planen. Der Nutzer kann Bildduplikate durch lange Handy-Screenshots haben; vor einer Datenkorrektur echte App-Einträge prüfen.

Bei Unterbrechung den vorhandenen Branch/PR weiterführen, statt erledigte Arbeit neu zu beginnen. Zum Übergabezeitpunkt ist kein weiterer Produktcode nach PR #26 begonnen worden.
