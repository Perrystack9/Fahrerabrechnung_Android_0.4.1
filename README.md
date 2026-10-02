# FahrerTagebuch – Android 0.4.1

**Dokumentationsstand: 02.10.2026; PR #34 mit normalisierten Fahrzeugnutzungsquellen auf main. Vollständige CI und echtes Schema-6→7-Update am Gerät bestanden. Folgebranch ergänzt den automatischen Vordergrund-Import; dessen CI/Gerätetest noch offen.** Verbindlicher Fachstand: [aktuelle Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Ältere Design- und GPS-Dokumente sind historische Referenzen, soweit sie dem nicht widersprechen.

Die App ist eine lokale Android-Basis mit echtem Fahrerkarte-Lesetest und **explizit speicherbaren Aktivitäts- und Fahrzeug-Teilimporten**. Ein vollständig nutzbarer Kartenimport und die Arbeitgeberabrechnung sind noch nicht fertig. Der genaue Fortschritt steht in [WORK_STATUS.md](WORK_STATUS.md). Für einen neuen Work-Einstieg: [aktuelle Übergabe und letzter Gerätetest](docs/WORK_HANDOFF.md).

## Tatsächlich vorhanden

- Siebenstufiges Onboarding mit expliziter Heimatpunktwahl per Vordergrund-Standort oder Karte; gespeicherter 500-m-Heimbereich. Kein Smartphone-Routentracking.
- USB-CCID-Leserdiagnose und ISO7816/APDU-Austausch mit USB-Freigabe, ATR und Kartengenerationserkennung für Gen1/Gen2/Gen2V2. Eine echte Gen2V2-Karte wurde am Galaxy S23 mit einem CCID-Leser getestet; das ist keine Zusage für alle Leser/Karten.
- Vollständiges Lesen der `EF Driver_Activity_Data` entsprechend der auf der Karte angegebenen Dateilänge. Der Gerätetest las 13.776 Bytes und dekodierte 74 zyklische Tagesdatensätze.
- Dekodierung der Tätigkeitswechsel und Kartenstatus-Bits, Projektion der UTC-Quelldaten auf lokale Kalendertage einschließlich Tagesgrenzen/Zeitumstellung.
- Vorschau des Kartentags mit kompakter Tagesübersicht, zoombarem und verschiebbarem Detail, Abschnittsauswahl mit Start/Ende/Dauer und verstellbarer Zeitmarke. Tätigkeiten und Einträge ohne eingelegte Karte sind getrennt gekennzeichnet. Fehlende Kartenzeit wird nicht als Ruhe oder Heimkehr erfunden.
- PR #9: Nach erfolgreichem Test kann die vollständige Aktivitätsdatei ausdrücklich als Snapshot in Room/SQLCipher gespeichert werden. Ein identischer Snapshot wird nicht doppelt gespeichert. Der zuletzt gespeicherte Snapshot wird beim Öffnen der Kartenansicht erneut dekodiert und als Vorschau angezeigt; die Migration von Schema 1 auf 2 und der echte Gerätetest mit App-Neustart sind bestanden.
- PR #10: `EF Vehicles_Used` wird vollständig gelesen und in Gen1-/Gen2-Fahrzeugnutzungen dekodiert. Ein eigener Knopf speichert einen verschlüsselten, dublettenfreien Fahrzeug-Snapshot; die Quellvorschau zeigt Kennzeichen, Zeitpunkte und Kilometerstände. Migration 2 → 3. Der Gerätetest bestätigte 69 gespeicherte Nutzungen nach Wiederöffnung ohne Karte.
- PR #11: Passende Aktivitäts- und Fahrzeug-Snapshots derselben Fahrerkarte erscheinen gemeinsam im Kartentag als belegte Zeitfenster und Nutzungsbeginne. Ein offenes Ende bleibt ein Zeitpunkt.
- PR #12: Eine eigene Fahrzeughistorien-Vorschau zeigt alle ausdrücklich gespeicherten Fahrzeug-Snapshots gruppiert nach gültiger VIN, sonst nach Staat/Kennzeichen. Wiederholte Nutzungen werden zusammengeführt; Einzelbelege mit Tacho-Quellwerten bleiben sichtbar.
- PR #13: Die Fahrzeughistorie lässt sich nach Kennzeichen oder VIN durchsuchen; weitere Nutzungen werden in Zehnerschritten angezeigt.
- PR #14: Der Kartentag zeigt die Dauern belegter Tätigkeitsabschnitte mit eingelegter Karte, Einträge ohne Karte und unbelegte Zeit getrennt. Das ist weder eine Arbeitszeit- noch eine Ruhe- oder Abrechnungsberechnung.
- PR #15: Ein eindeutig belegtes Fahrzeug zur Zeitmarke lässt sich gezielt in der gespeicherten Fahrzeughistorie suchen.
- PR #16: Bei Navigation aus dem Kartentag zeigt die Fahrzeughistorie die dort ausgewählte Kartennutzung mit Zeit und Tacho-Quellwerten zuerst; sie bleibt zusätzlich in der vollständigen Liste. Eine nicht gespeicherte Nutzung wird nicht als Historie ausgegeben.
- PR #17: Gespeicherte Aktivitäts-Snapshots sind für eine spätere read-only Teilimport-Übersicht nach Speicherzeit abrufbar. Noch keine neue Oberfläche oder vollständige Importhistorie.
- PR #18: Eine read-only Ansicht listet ausdrücklich gespeicherte Aktivitäts-Snapshots mit Speicherzeit, Kartengeneration und Datensatzanzahl; Kartennummern sind maskiert. Der Lesetest allein erzeugt keinen Eintrag.
- PR #19: Dieselbe Übersicht zeigt Fahrzeug-Snapshots separat mit Speicherzeit, Kartengeneration, Nutzungsanzahl und maskierter Kartennummer. Aktivitäts- und Fahrzeugdateien werden nicht künstlich zu einem vollständigen Import zusammengezogen.
- PR #20: Ein ausdrücklich gespeicherter älterer Aktivitäts-Snapshot kann in der bestehenden Kartentag-Zeitleiste als Vorschau geöffnet werden; der Wechsel zurück zum zuletzt gespeicherten Stand ist möglich. Es entsteht dadurch kein neuer Import.
- PR #21–#22: Gespeicherte Dateien zeigen den Zeitraum der Kartentage beziehungsweise der belegten Fahrzeugnutzungen; offene Nutzungsenden bleiben kenntlich. Kurze Kartennummern werden vollständig maskiert.
- PR #23–#24: Eine ausgewählte Fahrzeugdatei lässt sich auf sie begrenzt in der Fahrzeughistorie und im Kartentag als Vorschau öffnen; die Rückkehr zur neuesten gespeicherten Datei ist möglich. Es wird weiterhin kein vollständiger Import behauptet.
- PR #25: Die Startseite benennt vorhandene Aktivitäts- und Fahrzeug-Teilimporte, statt bei gespeicherten Dateien pauschal „Noch keine Fahrerkarte eingelesen“ zu zeigen.
- PR #26: Die beiden validierten Dateien können aus einem tatsächlichen Lesevorgang ausdrücklich gemeinsam gespeichert werden. Eine SQLCipher-Transaktion sichert beide Dateien und ihre Herkunftszuordnung; identische Quellen bleiben dedupliziert. Die gespeicherte gemeinsame Vorschau öffnet genau dieses Dateipaar. Alte Einzeldateien werden nicht automatisch zugeordnet (Migration 3 → 4).
- PR #28: Das Geburtsdatum in EF Identification wird korrekt als vier Byte BCD-Kalenderdatum gelesen; alle Nullbytes bleiben ein fehlendes Datum. Zehn neue Regressionstestmethoden decken den Parser und den Leserablauf für Gen1/Gen2/Gen2V2 ab. Keine Änderung an Room-Schema oder UI.
- Profilbindungs-Stufe: Erste eigene Karte ausdrücklich bestätigen; zusätzliche Folge-/Ersatzkarten gehören zum selben stabilen lokalen Profil. Vollständige identische Inhaberdaten erlauben die Zuordnung beim ausdrücklich ausgelösten Speichern; unklare Fälle verlangen eine eigene Bestätigung. Erkennbar fremde Karten bleiben temporäre Vorschau. Neue Teilimport-Sitzungen verknüpfen Profil, konkrete Karte und tatsächlich gespeicherte Quellen; ältere Dateien bleiben unzugeordnet im Quellarchiv. Room/SQLCipher-Schema 5 mit additiver Migration 4 → 5. Der geprüfte Profilbindungs-Ablauf ist am Gerät bestanden; sein Paralleltest war kein Update-/Migrationstest.
- PR #32: neue eigene Aktivitätsimporte speichern UTC-Tagesinhalte und geordnete Tätigkeitswechsel dauerhaft mit Karten-/Quellherkunft. Identische Inhalte derselben Karte werden unabhängig von Ringpositionen wiederverwendet; abweichende Fassungen und Ersatzkarten bleiben getrennt. Die Profil-Zeitleiste nutzt die gespeicherten Tagesdaten. Additive Migration 5 → 6 ohne Umschreibung/Backfill; vollständige CI und Update-Gerätetest bestanden, PR gemergt.
- [PR #34](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/34), `feature/normalized-vehicle-sources`: Fahrzeugnutzungen analog mit physischer Karten-/Quelldateiherkunft dauerhaft speichern und daraus Profilvorschau/Fahrzeughistorie lesen. Additive Migration 6 → 7 mit generiertem Export und unveränderten bisherigen Entities; keine automatische Zuordnung alter Dateien, aufgelöste Fahrzeugidentität oder Kilometerberechnung. Finale CI mit 66 Unit- und 79 Emulatortests grün; Schema-6→7-Gerätetest am 02.10.2026 um 10:53 Uhr bestanden, PR unverändert gemergt.
- Aktueller Folgebranch `feature/card-import-lifecycle`: automatisches Lesen bei eingelegter Karte im Vordergrund, Android-USB-Freigabe, bestehende erste/unklare Kartenbestätigung und automatische Übernahme unterstützter Daten. Ein dauerhafter Importverlauf nennt Status, Kartengeneration, belegte Kartentage und fehlende/fehlerhafte Daten. Neue Daten und Ergebnisbeleg sind gemeinsam atomar; Speicherfehler/unterbrochene Übernahmen bleiben nachvollziehbar. Schema 8 ergänzt zwei Tabellen ohne Backfill. Code vorhanden, vollständige CI/Schemaexport und [neuer USB-Gerätetest](docs/AUTOMATIC_IMPORT_DEVICE_TEST.md) noch abzuschließen.
- Fünf Haupttabs: **Start, Kalender, Auswertung, Abrechnung, Einstellungen**. Room/SQLCipher-/Keystore-Basis, DataStore, Designsystem und CI.

**Wichtig:** Auf main ist der bestätigte technische Lese-/Speicherablauf aus PR #34 vorhanden. Der Folgebranch ersetzt im normalen Vordergrund-Ablauf die manuellen Test-/Speichern-Knöpfe durch automatische Übernahme zulässiger Daten. Einmalige eigene bzw. unklare Ersatzkartenbestätigung und Android-USB-Freigabe bleiben notwendig. Das getrennte Altarchiv bleibt read-only. Kartentage/Fahrzeughistorie sind weiterhin Quellvorschauen, kein fertiger Fahrerbericht oder vollständiger Kartenimport. Weitere Kartendaten und fachliche Auswertung fehlen.

## Noch nicht produktiv

- Noch keine vollständige persistente Kartenimport-Pipeline oder vollständige Kartenabdeckung. Profil-/Kartenbindung, eine Übersicht der neu gespeicherten Teilimport-Sitzungen und normalisierte Tagesquellen sind vorhanden. Die fachliche Zusammenführung aller Tagesfassungen, weitere Kartenquellen und vollständige Kartenabdeckung und Fehlerhistorie nicht identifizierter bzw. nicht bestätigter Leseversuche fehlen. Der Folgebranch protokolliert ausschließlich zulässige eigene Übernahmen; unbestätigte und fremde Karten bleiben temporär.
- Fahrzeugnutzungen erscheinen als Quellintervalle bei der Zeitleiste, haben eine read-only Fahrzeughistorien-Vorschau; eine dauerhafte Fahrzeug-Entität, belastbare Tätigkeit/Fahrzeug-Zuordnung und Kilometeraggregation fehlen. GNSS-Einzelpunkte, Grenzübertritte, Ereignisse/Störungen, Kontrollen und weitere Kartendateien fehlen.
- Keine belastbare Heimkehr-/Übernachtungs-/Reiseauswertung, Spesen, Arbeitszeit- und Arbeitgeberzuschlagsberechnung, vollständige PDF-Ausgabe oder Cloud-Synchronisation. Keine Steuerlogik.
- Die in [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) beschriebene Tageszeitleiste bleibt eine Vorschau; Fahrzeug-Zeitfenster sind der erste Zusatzmarker. GNSS, Grenzübertritte, Ereignisse und Tageskennzahlen fehlen.

## Architektur

- `:app` – Android-Anwendung, Navigation, Hardwaretest und Kartentag-Vorschau.
- `:core` – Designsystem, gemeinsame UI- und Sicherheitsbasis.
- `:data` – CCID/Kartenleser, Aktivitätsparser, lokale Tagesprojektion, Room/SQLCipher, Keystore und DataStore.
- `:domain` – Grundtypen und Status.
- `:feature` – Hauptbereiche und vorbereitete Detailseiten.
- `:report` – vorbereitete Berichtsschnittstelle; kein fertiger PDF-Renderer.

## Build und Verifikation

JDK 17, Android SDK Platform 37, Build Tools 36.0.0, AGP 9.4.0 und der offizielle Gradle Wrapper 9.6.1 werden verwendet.

```bash
./gradlew help
./gradlew build --dry-run
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleInternal
./gradlew :app:assembleDeviceTest
```

Room-/SQLCipher- und Compose-Instrumentierung erfordern Emulator/Gerät (`./gradlew connectedDebugAndroidTest`). Der [vollständige CI-Lauf auf `main` nach PR #26](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36743029047) bestand Build, Unit-Tests, Lint, Debug-/Internal-APK-Verifikation sowie Room-/SQLCipher-/Compose-Emulatortests. Am **01.10.2026** bestätigte der Nutzer auf dem Galaxy S23 mit CCID-Leser und echter Gen2V2-Karte die gemeinsame Speicherung von **74 Tagesdatensätzen und 69 Fahrzeugnutzungen** sowie die weitere Verfügbarkeit des Dateipaars nach Abziehen des Lesers und App-Neustart. Details, Testgrenzen und getestete APK stehen in [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).

Der [vollständige PR-28-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966) bestand ebenfalls alle Prüfungen einschließlich 13 Daten- und 22 App-Instrumentierungstests. Nach der PR-28-Debug-ZIP und der Testanleitung meldete der Nutzer am 01.10.2026 „Funktioniert soweit“. Neue Einzelmesswerte wurden nicht nachgemeldet; Details stehen in der Übergabe. Die Fachvorgabe zur Profil-/Kartenbindung wurde am 01.10.2026 bestätigt und ist in CURRENT_RULES dokumentiert. Der Profilbindungs-Ablauf wurde anschließend mit der PR-30-Internal-APK erfolgreich geprüft; die Installation erfolgte parallel und prüfte damit keinen Datenerhalt beim Update.

Die Room-Datenbank wird über SQLCipher geöffnet. Die zufällige Passphrase ist per Android Keystore/AES-GCM geschützt; kein unverschlüsselter Fallback und kein `fallbackToDestructiveMigration()`. `SafeLogger` darf keine sensiblen Kartendaten, Rohdumps oder vollständigen APDUs ausgeben.

Die erste vollständige [PR-30-CI 36856005336](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36856005336) bestand 57 Unit-Tests, Lint, APK-Prüfungen sowie 27 Daten-/Transport- und 29 App-Emulatortests. Schemaexport und Kartenwechsel-Absicherung wurden danach ergänzt. Die finale [PR-30-CI 36859243839](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36859243839) zum Kopf `09ccfdf` ist vollständig grün. Am 01.10.2026 bestätigte der Nutzer Erstbindung, Speicherung, Neustart ohne Leser, Wiedererkennung ohne Erstbestätigung und 0 neue Quelldateien beim erneuten Speichern.

PR #32 enthält den generierten Room-Schema-6-Export. Die [finale CI 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2) zum Kopf `7194869` ist vollständig grün: 61 Unit-Tests und 67 Room-/SQLCipher-/Compose-Emulatortests, keine Fehler oder übersprungenen Tests; außerdem Lint und APK-Prüfungen einschließlich gleicher Testsignatur und aufsteigender Versionscodes. Am 02.10.2026 bestätigte der Nutzer sämtliche vorgesehenen Gerätetestschritte: echtes Schema-5→6-Update ohne Deinstallation, Profil-/Datenerhalt ohne neues Onboarding, erneutes Lesen ohne Erstbestätigung und Verfügbarkeit nach Neustart ohne Leser. PR #32 wurde danach unverändert gemergt. Testherkunft und Grenzen stehen in der Übergabe; vollständiger Kartenimport bleibt offen; der Folgebranch setzt die vereinfachte Vordergrund-Bedienung um.

## Dokumente lesen

- [docs/WORK_HANDOFF.md](docs/WORK_HANDOFF.md): Einstieg für ein neues Work, technische Ausgangsbasis und letzter erfolgreicher Gerätetest.
- [WORK_STATUS.md](WORK_STATUS.md): aktueller Implementierungs- und Teststand.
- [docs/PROFILE_BINDING_DEVICE_TEST.md](docs/PROFILE_BINDING_DEVICE_TEST.md): Anleitung des geprüften Profilbindungs-Ablaufs in [PR #30](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/30).
- [docs/NORMALIZED_DAYS_DEVICE_TEST.md](docs/NORMALIZED_DAYS_DEVICE_TEST.md): Anleitung des bestandenen Update-/Gerätetests der normalisierten Tagesquellen. Die separate **FahrerTagebuch Test**-App erhält eine gleichbleibende öffentliche Entwicklungssignatur; [Buildherkunft und Grenzen](ci/README.md).
- [docs/NORMALIZED_VEHICLES_DEVICE_TEST.md](docs/NORMALIZED_VEHICLES_DEVICE_TEST.md): Update-/Gerätetest des Folgeschritts mit gespeicherten Fahrzeugnutzungen. Vorhandene PR-32-Test-App direkt aktualisieren; Ausgangs-APK nicht erneut installieren.
- [docs/CURRENT_RULES.md](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3): maßgebliche Produkt-/Fachregeln.
- [BUILD_STATUS.md](BUILD_STATUS.md), [BUILD_VERIFICATION_REPORT_0.4.1.md](BUILD_VERIFICATION_REPORT_0.4.1.md), [PROJECT_START_REPORT.md](PROJECT_START_REPORT.md) und [GITHUB_ACTIONS_HANDOFF.md](GITHUB_ACTIONS_HANDOFF.md): historische Berichte vom 22.09.2026.
- `docs/design-handoff-v2/` und `docs/UI_REFINEMENT_0.4.1_V2.md`: historische Design-/Zwischenstände; enthalten teils überholte Smartphone-GPS- und Acht-Schritte-Vorgaben.

Der letzte bestätigte PR-34-Gerätetest und die ausdrückliche Freigabe zum gebündelten Weiterarbeiten stehen in [WORK_HANDOFF.md](docs/WORK_HANDOFF.md). Der nächste notwendige Hardwaretest betrifft den neuen automatischen USB-Ablauf; [Anleitung](docs/AUTOMATIC_IMPORT_DEVICE_TEST.md).
