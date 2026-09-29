# FahrerTagebuch – Android 0.4.1

**Dokumentationsstand: 29.09.2026; technischer Bezugsstand: PR #25.** Verbindlicher Fachstand: [aktuelle Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Ältere Design- und GPS-Dokumente sind historische Referenzen, soweit sie dem nicht widersprechen.

Die App ist eine lokale Android-Basis mit echtem Fahrerkarte-Lesetest und **explizit speicherbaren Aktivitäts- und Fahrzeug-Teilimporten**. Ein vollständig nutzbarer Kartenimport und die Arbeitgeberabrechnung sind noch nicht fertig. Der genaue Fortschritt steht in [WORK_STATUS.md](WORK_STATUS.md).

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
- Fünf Haupttabs: **Start, Kalender, Auswertung, Abrechnung, Einstellungen**. Room/SQLCipher-/Keystore-Basis, DataStore, Designsystem und CI.

**Wichtig:** Lesen und Dekodieren erfolgen zunächst im Arbeitsspeicher. Erst die jeweiligen zusätzlichen Knöpfe speichern Aktivitäts- oder Fahrzeugdatei lokal. Die Kartentag-Zeitleiste bleibt eine Vorschau aus diesen Daten, kein abgeschlossener Kartenimport oder berechneter Tagesbericht. Andere Onboarding-Fachfelder und Werte in Grundscreens sind weiterhin Vorschau beziehungsweise Platzhalter.

## Noch nicht produktiv

- Noch keine vollständige persistente Kartenimport-Pipeline mit Profilzuordnung, Importhistorie in der Oberfläche und vollständiger Kartenabdeckung; bislang sind Aktivitäts- und Fahrzeug-Snapshots separat gespeichert.
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
```

Room-/SQLCipher- und Compose-Instrumentierung erfordern Emulator/Gerät (`./gradlew connectedDebugAndroidTest`). Der [CI-Lauf zu PR #24](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36528989882) bestand Build, Tests, Lint, APK-Verifikation und Room-/SQLCipher-/Compose-Instrumentierung im Emulator (nach Runner-Neustart wegen fehlendem `/dev/kvm`). Aktivitäts- und Fahrzeug-Teilimporte sowie die beiden Fahrzeugvorschauen wurden auf dem Galaxy S23 ohne eingelegte Karte geprüft. Der Startseitenstatus aus PR #25 erfordert einen gesonderten Gerätetest.

Die Room-Datenbank wird über SQLCipher geöffnet. Die zufällige Passphrase ist per Android Keystore/AES-GCM geschützt; kein unverschlüsselter Fallback und kein `fallbackToDestructiveMigration()`. `SafeLogger` darf keine sensiblen Kartendaten, Rohdumps oder vollständigen APDUs ausgeben.

## Dokumente lesen

- [WORK_STATUS.md](WORK_STATUS.md): aktueller Implementierungs- und Teststand.
- [docs/CURRENT_RULES.md](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3): maßgebliche Produkt-/Fachregeln.
- [BUILD_STATUS.md](BUILD_STATUS.md), [BUILD_VERIFICATION_REPORT_0.4.1.md](BUILD_VERIFICATION_REPORT_0.4.1.md), [PROJECT_START_REPORT.md](PROJECT_START_REPORT.md) und [GITHUB_ACTIONS_HANDOFF.md](GITHUB_ACTIONS_HANDOFF.md): historische Berichte vom 22.09.2026.
- `docs/design-handoff-v2/` und `docs/UI_REFINEMENT_0.4.1_V2.md`: historische Design-/Zwischenstände; enthalten teils überholte Smartphone-GPS- und Acht-Schritte-Vorgaben.
