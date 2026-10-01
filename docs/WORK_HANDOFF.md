# Work-Übergabe – FahrerTagebuch

**Stand: 01.10.2026.** Diese Datei hält den technischen Fortschritt und den zuletzt vom Nutzer bestätigten Gerätetest fest. Sie ersetzt keine Fachregeln.

## Einstieg und Ausgangsbasis

1. Zuerst [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) und [CURRENT_RULES.md](CURRENT_RULES.md) lesen. Beide sind der verbindliche Produkt-/Fachstand; historische Design-/GPS-Dokumente gelten nur ohne Widerspruch.
2. Danach diese Übergabe, [WORK_STATUS.md](../WORK_STATUS.md) und [README.md](../README.md) lesen.
3. Den aktuellen `main` aus GitHub verwenden und vor der nächsten Änderung HEAD, offene PRs und CI erneut prüfen. Alte Scratch-Verzeichnisse oder ZIPs können fehlen oder älter sein.

| Bezug | Stand |
|---|---|
| Letzter Produktcode-PR | [PR #28: Fahrerkarten-Geburtsdatum korrekt als BCD-Kalenderdatum dekodieren](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/28), gemergt |
| Produktcode-Basis auf main | [`a0ce87675779aa7130473fa847cea76db2a12f82`](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/commit/a0ce87675779aa7130473fa847cea76db2a12f82); spätere reine Dokumentationscommits ändern diese technische Basis nicht |
| Getesteter PR-28-Kopf | `969310b68446437e2214ea07e3e3155a2a72bb86` |
| Datenbank | Room/SQLCipher, Schema **4**, Migrationen 1 → 2 → 3 → 4, Schemaexport 4.json eingecheckt |
| Offene lokale Arbeit | Keine uncommitteten Produktcode-Änderungen oder laufenden Builds aus diesem Work zu übernehmen; PR #26 ist abgeschlossen |
| Letzter manueller Gerätetest | **01.10.2026, PR #28: vom Nutzer als funktionierend zurückgemeldet**, Details und Grenzen unten |

## Wirklich implementiert

- Siebenstufiges Onboarding; Heimatpunkt ausdrücklich per Vordergrund-Standort oder Karte wählbar, fester 500-m-Radius.
- USB-CCID/APDU, USB-Freigabe, ATR-/Slot-Diagnose und Gen1-/Gen2-/Gen2V2-Erkennung.
- PR #28: Das Geburtsdatum in EF Identification wird als vier Byte BCD-Kalenderdatum (`LocalDate`) gelesen; alle Nullbytes bedeuten ein fehlendes Datum. TimeReal-Zeitstempel bleiben unverändert.
- Vollständiges Lesen der Aktivitäts- und Fahrzeugdatei, zyklische Tagesdatensätze/Tätigkeitswechsel und Fahrzeugnutzungen dekodieren.
- Aktivitätsintervalle auf lokale Kalendertage projizieren; fehlende Zeit und Einträge ohne Karte gesondert kennzeichnen.
- Kompakte Tagesübersicht, zoombare/verschiebbare Detailzeitleiste, Abschnittsauswahl, Zeitmarke und belegte Fahrzeug-Zeitfenster. Offene Fahrzeugenden werden nicht fortgeschrieben.
- Verschlüsselte, ausdrücklich bestätigte Einzeldateien; identische Quell-Snapshots werden dedupliziert und nach App-Neustart wieder geöffnet.
- Fahrzeughistorien-Vorschau über gespeicherte Dateien, VIN-/Kennzeichen-Suche, nachladbare Nutzungen und gezielte Navigation aus dem Kartentag. Tacho-Werte sind Quellwerte.
- Dateiübersicht mit Generation, Datensatzanzahl, maskierter Kartennummer und Quellzeitraum; ältere Aktivitäts-/Fahrzeugdateien gezielt als Vorschau öffnen.
- Startseite unterscheidet fehlende, vorhandene und nicht prüfbare Teilimporte.

**PR #26 ergänzt:** Ein tatsächlicher Lesevorgang mit beiden validierten Dateien kann über „Beide Dateien gemeinsam speichern“ ausdrücklich gesichert werden. Eine Room-Transaktion speichert beide Quellen und ihre Zuordnung. Die gemeinsame Vorschau öffnet genau diese beiden Dateien. Bestehende Einzeldateien werden nicht rückwirkend anhand von Datum oder Kartennummer gepaart.

Die Dateien und identische Dateipaare bleiben dedupliziert. Die Zeit des Dateipaars bezeichnet seine erste gemeinsame Speicherbestätigung; sie ist keine vollständige Historie jedes erneuten Leseversuchs. Getrennt gespeicherte Quellen können weiterhin als Vorschau kombiniert werden, ohne dadurch als derselbe Lesevorgang zu gelten.

## Letzte Hardware-/Gerätetests

### PR #28 – aktuelle Nutzerrückmeldung

**01.10.2026, etwa 12:20 Uhr Europe/Berlin:** Nach Bereitstellung der PR-28-Debug-ZIP und einer Anleitung für Installation, „Kartendaten testen“, gemeinsame Speicherung, Lesertrennung, vollständiges Beenden und Wiederöffnen der App meldete der Nutzer: **„Funktioniert soweit“**. Diese Rückmeldung ist als bestandener Gerätetest im bisherigen Galaxy-S23-/Android-16-/CCID-/Gen2V2-Kontext übernommen.

Neue Einzelwerte, Screenshots und Angaben zum Installationsweg wurden nicht nachgemeldet. Die nachstehenden 74 Tagesdatensätze und 69 Fahrzeugnutzungen sind weiterhin die Messwerte aus PR #26. Das Geburtsdatum wird nicht in der Oberfläche angezeigt; seine genaue Dekodierung ist durch automatische Tests abgesichert. Diese Rückmeldung ist keine allgemeine Freigabe anderer Karten oder Leser.

- Ausgeliefert: `FahrerTagebuch-PR28-Datumsparser-Test.zip`, enthält `app-debug.apk`.
- Herkunft: [vollständig grüner PR-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966), Debug-Artefakt ID `11153391471`, PR-Kopf `969310b68446437e2214ea07e3e3155a2a72bb86`.
- ZIP-SHA256: `8d563f345abd90205daa12727a4d758e6170b44d3c4fe3b31462b96ed6362728`.
- APK-SHA256: `c729cc1a8f35bf78f0a080a28ba8e99546e9579094503193ad0f7207777a5b7e`.
- Der Zertifikatsvergleich ergab eine andere Debug-Signatur als bei der PR-26-CI-APK. Ein direktes Update über diese PR-26-APK ist damit blockiert; zur Umgehung nicht deinstallieren und gespeicherte Daten verlieren. Die Nutzerrückmeldung erklärt den Installationsweg nicht. PR #28 ändert keine Signierkonfiguration.

### PR #26 – vorheriger Test mit Einzelmesswerten

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

### Getestete PR-26-APK

- Ausgeliefert: **Debug-APK als ZIP**, `FahrerTagebuch-PR26-Kartenquellen-Test.zip`.
- Herkunft: [grüner CI-Lauf 36568389361](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361), Artefakt `Fahrerabrechnung-0.4.1-debug-apk`, ID `11032504544`; enthält `app-debug.apk`.
- APK-Quellstand: `7a112c09770e078e78b4bcd29ef43fbdf211723f`. Der nachfolgende Commit ergänzte ausschließlich den generierten Room-Schemaexport; der App-Code ist gleich.
- ZIP-SHA256: `1520ff9a7fa12b183a9784eed8383379a6f81e2a4b0183c057899d569fd3b0d3`.
- APK-SHA256: `2e20fc3e05c4cfd1fe12278cfafad731b21ccd334008a3a777dbf9ef6a15d6da`.

Bei Bedarf diese APK über das GitHub-Artefakt wiederherstellen. Eine ältere PR-APK nicht als neue Version ausgeben. Der Nutzer benötigt APKs **als ZIP**.

## Automatische Verifikation

- [Vollständiger PR-28-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966) für `969310b`: **grün**, einschließlich Unit-Tests, Lint, Debug-/Internal-APK-Verifikation und Room-/SQLCipher-/Compose-Instrumentierung. **13 Daten- und 22 App-Instrumentierungstests**, keine Fehler oder übersprungenen Tests. Zehn neue Regressionstestmethoden prüfen Datef-Dekodierung, Schaltjahre, Nullwert, ungültige BCD-/Kalenderdaten, Trunkierung und Leserablauf für Gen1/Gen2/Gen2V2.
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

**Profilbindung fachlich festgelegt am 01.10.2026:** Single-Driver-App; erste eigene Karte ausdrücklich bestätigen. Die dauerhafte Fahreridentität ist das lokale Profil, nicht die Kartennummer. Belastbar gleiche Inhaber-Identität erlaubt zusätzliche Folge-/Ersatzkarten; unklare Fälle verlangen Bestätigung. Erkennbar fremde Karten bleiben auf den temporären technischen Lesetest begrenzt. Alte Karten/Imports bleiben erhalten; ältere Teilimporte nicht anhand von Datum, Kartennummer oder Vermutung zuordnen. Zielmodell und vollständige Vorgabe stehen in [CURRENT_RULES.md](CURRENT_RULES.md).

Geplanter begrenzter nächster Schritt: persistente Profil-/Kartenbindung und daran gebundene Teilimport-Sitzungen für die bereits unterstützten Quellen. Additive Migration, explizite Bestätigung und Fremdkartenschutz automatisch prüfen. Das Onboarding speichert bisher nur Heimatpunkt und Abschlussstatus; die übrigen Profil-/Fachfelder bleiben Vorschau.

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

Bei Unterbrechung den vorhandenen Branch/PR weiterführen, statt erledigte Arbeit neu zu beginnen. PR #28 ist gemergt; die vollständige Import-Pipeline ist weiterhin offen. Die Profil-/Kartenbindung ist fachlich freigegeben und der nächste begrenzte Implementierungsschritt.
