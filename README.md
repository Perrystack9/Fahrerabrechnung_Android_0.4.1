# FahrerTagebuch – Android 0.4.1

**Dokumentationsstand: 01.10.2026; begrenzte Profilbindungs-Stufe auf Basis von `main` nach PR #28/#29. Gerätetest dieser Stufe noch offen.** Verbindlicher Fachstand: [aktuelle Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Ältere Design- und GPS-Dokumente sind historische Referenzen, soweit sie dem nicht widersprechen.

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
- Profilbindungs-Stufe: Erste eigene Karte ausdrücklich bestätigen; zusätzliche Folge-/Ersatzkarten gehören zum selben stabilen lokalen Profil. Vollständige identische Inhaberdaten erlauben die Zuordnung beim ausdrücklich ausgelösten Speichern; unklare Fälle verlangen eine eigene Bestätigung. Erkennbar fremde Karten bleiben temporäre Vorschau. Neue Teilimport-Sitzungen verknüpfen Profil, konkrete Karte und tatsächlich gespeicherte Quellen; ältere Dateien bleiben unzugeordnet im Quellarchiv. Room/SQLCipher-Schema 5 mit additiver Migration 4 → 5. Gerätetest noch offen.
- Fünf Haupttabs: **Start, Kalender, Auswertung, Abrechnung, Einstellungen**. Room/SQLCipher-/Keystore-Basis, DataStore, Designsystem und CI.

**Wichtig:** Lesen und Dekodieren erfolgen zunächst im Arbeitsspeicher. Erst zusätzliche Knöpfe speichern die Aktivitäts- und Fahrzeugdatei gemeinsam oder einzeln lokal. Die Kartentag-Zeitleiste bleibt eine Vorschau aus diesen Daten, kein abgeschlossener Kartenimport oder berechneter Tagesbericht. Andere Onboarding-Fachfelder und Werte in Grundscreens sind weiterhin Vorschau beziehungsweise Platzhalter.

## Noch nicht produktiv

- Noch keine vollständige persistente Kartenimport-Pipeline oder vollständige Kartenabdeckung. Profil-/Kartenbindung und eine Übersicht der neu gespeicherten Teilimport-Sitzungen sind vorhanden; dauerhafter normalisierter Tagesbestand, weitere Kartenquellen und Fehlerhistorie aller Leseversuche fehlen.
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

Room-/SQLCipher- und Compose-Instrumentierung erfordern Emulator/Gerät (`./gradlew connectedDebugAndroidTest`). Der [vollständige CI-Lauf auf `main` nach PR #26](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36743029047) bestand Build, Unit-Tests, Lint, Debug-/Internal-APK-Verifikation sowie Room-/SQLCipher-/Compose-Emulatortests. Am **01.10.2026** bestätigte der Nutzer auf dem Galaxy S23 mit CCID-Leser und echter Gen2V2-Karte die gemeinsame Speicherung von **74 Tagesdatensätzen und 69 Fahrzeugnutzungen** sowie die weitere Verfügbarkeit des Dateipaars nach Abziehen des Lesers und App-Neustart. Details, Testgrenzen und getestete APK stehen in [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).

Der [vollständige PR-28-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966) bestand ebenfalls alle Prüfungen einschließlich 13 Daten- und 22 App-Instrumentierungstests. Nach der PR-28-Debug-ZIP und der Testanleitung meldete der Nutzer am 01.10.2026 „Funktioniert soweit“. Neue Einzelmesswerte wurden nicht nachgemeldet; Details stehen in der Übergabe. Die Fachvorgabe zur Profil-/Kartenbindung wurde am 01.10.2026 bestätigt und ist in CURRENT_RULES dokumentiert. Der bestehende Hardwaretest ersetzt nicht den noch offenen Gerätetest der neuen Profilbindungs-Stufe.

Die Room-Datenbank wird über SQLCipher geöffnet. Die zufällige Passphrase ist per Android Keystore/AES-GCM geschützt; kein unverschlüsselter Fallback und kein `fallbackToDestructiveMigration()`. `SafeLogger` darf keine sensiblen Kartendaten, Rohdumps oder vollständigen APDUs ausgeben.

## Dokumente lesen

- [docs/WORK_HANDOFF.md](docs/WORK_HANDOFF.md): Einstieg für ein neues Work, technische Ausgangsbasis und letzter erfolgreicher Gerätetest.
- [WORK_STATUS.md](WORK_STATUS.md): aktueller Implementierungs- und Teststand.
- [docs/CURRENT_RULES.md](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3): maßgebliche Produkt-/Fachregeln.
- [BUILD_STATUS.md](BUILD_STATUS.md), [BUILD_VERIFICATION_REPORT_0.4.1.md](BUILD_VERIFICATION_REPORT_0.4.1.md), [PROJECT_START_REPORT.md](PROJECT_START_REPORT.md) und [GITHUB_ACTIONS_HANDOFF.md](GITHUB_ACTIONS_HANDOFF.md): historische Berichte vom 22.09.2026.
- `docs/design-handoff-v2/` und `docs/UI_REFINEMENT_0.4.1_V2.md`: historische Design-/Zwischenstände; enthalten teils überholte Smartphone-GPS- und Acht-Schritte-Vorgaben.
