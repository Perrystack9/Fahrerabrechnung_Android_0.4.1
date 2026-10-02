# Work-Übergabe – FahrerTagebuch

**Stand: 02.10.2026.** Diese Datei hält den technischen Fortschritt und den zuletzt vom Nutzer bestätigten Gerätetest fest. Sie ersetzt keine Fachregeln.

## Einstieg und Ausgangsbasis

1. Zuerst [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) und [CURRENT_RULES.md](CURRENT_RULES.md) lesen. Beide sind der verbindliche Produkt-/Fachstand; historische Design-/GPS-Dokumente gelten nur ohne Widerspruch.
2. Danach diese Übergabe, [WORK_STATUS.md](../WORK_STATUS.md) und [README.md](../README.md) lesen.
3. Den aktuellen `main` aus GitHub verwenden und vor der nächsten Änderung HEAD, offene PRs und CI erneut prüfen. Alte Scratch-Verzeichnisse oder ZIPs können fehlen oder älter sein.

| Bezug | Stand |
|---|---|
| Letzter Produktcode-PR | [PR #32: Kartentage mit unveränderlicher Karten- und Quellenherkunft](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/32), nach bestandenem Gerätetest gemergt |
| Produktcode-Basis auf main | [`b0583a6ea8b15b7260b76c19e56f5166608bb898`](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/commit/b0583a6ea8b15b7260b76c19e56f5166608bb898); spätere reine Dokumentationscommits ändern diese technische Basis nicht |
| Getesteter PR-32-Kopf | `7194869bdc2a45ae79e1e88c823a2cdf2d7a6182`; Merge-Tree entspricht exakt diesem getesteten Stand |
| Abgeschlossener begrenzter Produktabschnitt | Normalisierte, quellenbezogene Kartentage auf main; vorhandene Profilbindung und Speicherung weiterverwenden |
| Datenbank im Produktabschnitt | Room/SQLCipher, Schema **6**, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6; keine destruktive Migration oder automatische Zuordnung alter Quellen |
| Automatischer Nachweis | [PR-32-Lauf 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2): vollständig grün, 61 Unit-Tests und 67 Emulatortests. Generierter Schema-6-Export und Migration geprüft |
| Letzter manueller Gerätetest | **02.10.2026, PR #32: echtes Schema-5→6-Update und Wiederöffnung bestanden**, Details und Grenzen unten |

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

Die Dateien und identische Dateipaare bleiben dedupliziert. Die Zeit des Dateipaars bezeichnet seine erste gemeinsame Speicherbestätigung; sie ist keine vollständige Historie jedes erneuten Leseversuchs. Neue Profil-Teilimporte protokollieren die ausdrücklich ausgelöste Speicherung eines Lesevorgangs separat. Beim gebundenen Profil öffnet die Standardvorschau die Quellen einer konkreten Teilimport-Sitzung; eine einzelne Quelle wird nicht mit einer anderen Sitzung ergänzt.

## Abgeschlossene Profilbindungs-Stufe (PR #30)

- `DriverCardImportRepository` und `DriverProfileDao`: ein ausdrücklich gebundenes lokales Profil; unabhängige Profil-/Karten-UUIDs, unveränderliche Kartenidentitäten und zusätzliche Folge-/Ersatzkarten. Die Kartennummer bezeichnet eine Quelle, nie die dauerhafte Fahreridentität.
- Match ausschließlich über exakt gleiche, vollständige Nachnamen, Vornamen und vorhandenes Geburtsdatum. Teil-/Präfix-/unscharfe Matches, fehlende Daten und möglicherweise abgeschnittene volle 35-Zeichen-Namensfelder verlangen Bestätigung. Unterschiedliche vollständige Namen und Geburtsdaten bei allen bestätigten Karten sperren eine Fremdkarte; nur ein abweichendes Merkmal bleibt unklar. Widersprüchliche Inhaberdaten zu einer bereits zugeordneten konkreten Karte werden gesperrt und überschreiben nichts.
- Erste eigene Karte und unklare neue/Ersatzkarte verlangen einen eigenen Bestätigungsdialog. Abbrechen und Lesetest speichern nichts. Zuordnung einer belastbar gleichen neuen Karte erfolgt erst beim ausdrücklich ausgelösten Speichern. Eine erkennbar fremde Karte bleibt temporäre Vorschau ohne Speicher-/Zuordnungsaktion und ohne Navigation ihrer Live-Nutzungen in die Profilhistorie.
- `driver_import_sources` verbindet jede neue `PARTIAL`-Sitzung mit ihrer konkreten Karte und ausgewählten Quellen. Quelle, Dateipaar, zusätzliche Karte und Sitzung werden gemeinsam atomar gespeichert; ein Fehler rollt die komplette neue Speicherung zurück. Derselbe Speicherauftrag ist idempotent, ein neuer Lesevorgang darf eine eigene Sitzung mit bereits vorhandenen Quelldateien erzeugen.
- Schema 5 ergänzt drei Tabellen ohne Backfill oder Umschreibung alter Karten/Snapshots/Sitzungen. Alte Dateien bleiben im getrennten Quellarchiv. Eine Archivauswahl bleibt read-only und erzeugt keine Profilbindung; bei Navigation bleibt die Fahrzeugvorschau auf diese ausgewählte Datei begrenzt.
- Die normale Fahrzeughistorie und Standard-Kartenvorschau nutzen bei gebundenem Profil ausschließlich dessen explizit verknüpfte Quellen. PR #30 enthielt noch keine normalisierten Tagesquellen; diese ergänzt der nachfolgend beschriebene PR #32. Vollständiger Kartenimport, dauerhafte Fahrzeugentität und fachliche Abrechnung bleiben offen.
- Automatische Regressionen: exakter/unklarer/fremder Match, Bestätigen/Abbrechen, Fremdkartensperre, unveränderte Folge-/Altkarten, Deduplizierung/Retry, atomarer Rollback, Einzelquellen, verschlüsselte Wiederöffnung, Migration 3 → 5 und 4 → 5 sowie Trennung von Archiv und Profilhistorie. Der geprüfte Profilbindungs-Gerätetest ist bestanden; Details und Grenzen unten.
- [Gerätetest-Anleitung](PROFILE_BINDING_DEVICE_TEST.md): Bestätigung/Abbruch, Speichern, Neustart ohne Leser, erneute eigene Karte und getrenntes Altarchiv. Identische ältere Quellen dürfen nach einem frischen Lesevorgang wiederverwendet werden; dadurch werden keine alten Sitzungen rückwirkend umgeschrieben.
- [Erster vollständig grüner PR-30-Lauf 36856005336](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36856005336), Kopf `99a29b6`: 57 Unit-Tests, 27 Daten-/Transporttests, 29 App-Tests, Lint und Debug-/Internal-APK-Prüfung bestanden; keine Fehler/übersprungenen Tests. Schemaexport und Kartenwechsel-Absicherung kamen anschließend hinzu und sind durch die oben genannte finale PR-30-CI abgedeckt. Bei Kartenwechsel werden der alte Dialog und laufende Prüfungen verworfen; verspätete Ergebnisse einer anderen Karte ändern keine aktuelle Speicherfreigabe.

## Abgeschlossene normalisierte Tagesquellen (PR #32)

- Drei additive Tabellen speichern unveränderliche UTC-Tagesinhalte, geordnete Tätigkeitswechsel und genaue Karten-/Quelldateiverweise einschließlich Ringposition. Die vorhandene Sitzung liefert die Importherkunft.
- Tagesinhalte derselben physischen Karte werden unabhängig von der Ringposition dedupliziert. Geänderte Fassungen desselben Datums bleiben zusätzlich erhalten; keine stille Auswahl einer fachlich maßgeblichen Fassung. Folge-/Ersatzkarten bleiben getrennte Karten, auch bei gleicher Nummer und gleichen Tagesinhalten.
- Normalisierung und bisherige Karten-/Datei-/Sitzungsspeicherung erfolgen atomar. Wiederholter Speicherauftrag bleibt idempotent. UTC, Kartenstatus, Fahrer-/Beifahrerslot, Teamstatus und Reihenfolge einschließlich gleicher Minuten bleiben erhalten. Entfernung der Karte und fehlende Tage werden nicht ergänzt.
- Die Profilvorschau verwendet gespeicherte normalisierte Tage. Quellen ohne Normalisierung bleiben über den bisherigen Parser lesbar. Lesen/Archivauswahl erzeugt keine Normalisierung oder Profilbindung; erst ein frischer, ausdrücklich gespeicherter eigener Lesevorgang darf passende vorhandene Quelldateien wiederverwenden.
- Noch kein vollständiger Import, konfliktbereinigter Tageskalender, berechnete Arbeitszeit, neue Fahrzeug-/GNSS-Datei oder automatische USB-Importbedienung. Letztere bleibt gemäß PR #31 verbindliches späteres Ziel.
- Vier neue Fingerprint-Unit-Tests und zehn neue SQLCipher-Instrumentierungstests prüfen Inhaltstreue, Ringverschiebung, Fassungen, Kartenherkunft, Deduplizierung, Verschlüsselung/Wiederöffnung, Fehler-Rollback und Migration 5 → 6. Die finale CI und der [Update-Gerätetest](NORMALIZED_DAYS_DEVICE_TEST.md) sind bestanden; keine erneute Implementierung oder Testwiederholung allein wegen eines Work-Wechsels.
- Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/6.json` stammt aus dem erfolgreichen Build von [Lauf 36876124448, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36876124448/attempts/2), Produktkopf `ad9e558`. Seine elf bisherigen Entities sind identisch zu Schema 5; ausschließlich die drei beschriebenen Tabellen kommen hinzu. SQLite-Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem Export; keine Fremdschlüsselverletzung. Schema-SHA256: `4e0a5e5f70db778d5f0426c3ab26839e9e1b47ce09a823178d2cc36a0b250ed0`.
- Die separate App **FahrerTagebuch Test** erhält eine gleichbleibende öffentliche Entwicklungssignatur. CI baut/prüft eine Schema-5-Ausgangs-APK und das aktuelle Update mit derselben Paketkennung. Die bisherigen Debug-/Internal-Installationen bleiben getrennt; Details zu Testschlüssel und Buildherkunft in [ci/README.md](../ci/README.md).

## Letzte Hardware-/Gerätetests

### PR #32 – letzter bestätigter Update-/Persistenztest

**02.10.2026, 08:25 Uhr Europe/Berlin:** Der Nutzer bestätigte mit **„Alle Punkte funktionieren perfekt“** sämtliche vier im Chat bereitgestellten Testschritte. Die Rückmeldung gilt im bisherigen Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext:

- Schema-5-Ausgangs-APK installieren, Onboarding durchführen, eigene Karte bestätigen und beide Dateien speichern.
- Leser entfernen, App vollständig beenden und Schema-6-APK als echtes Update ohne Deinstallation installieren.
- Ohne Leser: kein neues Onboarding; vorhandenes Profil, Kartentage und Fahrzeugvorschau bleiben verfügbar.
- Eigene Karte erneut ohne Erstbestätigung lesen und speichern; bei unverändertem Inhalt 0 neue Quelldateien. Alte und neue Vorschauen bleiben nach vollständigem Neustart ohne Leser verfügbar.

Damit ist der vorgesehene Schema-5→6-Gerätetest bestanden. Neue Byte-/Datensatzwerte oder genaue Sitzungsanzahlen wurden nicht nachgemeldet. Fremd-/Ersatzkarten, andere Generationen/Leser und Fehlerfälle sind keine zusätzlich behaupteten manuellen Erfolge. Der Test beweist weder eine Migration der früheren getrennten Debug-/Internal-Installationen noch ein Schema-4→5-Update oder den vollständigen Kartenimport.

**Herkunft:** `FahrerTagebuch-PR32-Update-Test.zip`, Artefakt `FahrerTagebuch-device-test-update`, ID `11187283572`, aus [vollständig grünem CI-Lauf 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2). Getesteter PR-32-Kopf `7194869bdc2a45ae79e1e88c823a2cdf2d7a6182`; Schema-5-Ausgangscode `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit ausschließlich Test-Buildkonfiguration gemäß [ci/README.md](../ci/README.md). Beide APKs verwenden `de.fahrerabrechnung.app.devicetest` und dieselbe Entwicklungssignatur; Versionscodes 40100 → 40101.

- ZIP-SHA256: `38a846b935eb4215a84351ffdb345f59a6a006b23242289fcf4bc233e65cb43f`.
- Ausgangs-APK-SHA256: `76fda42afb378ea97766647c397d5c08deddc8957bc27d7c5695681f0104adc5`.
- Update-APK-SHA256: `87436270c797fc82f31c158caaa6bfa3659e71e93c16e0e5353e6a800ae82884`.

PR #32 wurde nach dieser Bestätigung mit unverändertem Kopf gemergt. Dieser Test ist der neue Ausgangspunkt für weitere import-/persistenzrelevante Änderungen.

### PR #30 – vorheriger bestätigter Profilbindungs-Ablauf

**01.10.2026, etwa 15:00–15:47 Uhr Europe/Berlin:** Galaxy S23 / Android 16, vorhandener USB-CCID-Leser und eigene Gen2V2-Karte. Video, Screenshots und ausdrückliche Rückmeldungen bestätigen:

- Aktivitätsdatei 13.776 Bytes / 74 Tagesdatensätze, Fahrzeugdatei 9.600 Bytes / 69 Nutzungen.
- Eigene Karte ausdrücklich bestätigt; gemeinsame Speicherung mit zwei neuen Quelldateien und sichtbarer Profil-Teilimport-Sitzung.
- Sitzung und beide Dateien nach vollständigem App-Neustart ohne Leser verfügbar.
- Wiederholtes Lesen erkennt die eigene Karte ohne Erstbestätigung; erneutes Speichern meldet Erfolg und **0 neue Quelldateien**.

Der geprüfte Ablauf ist bestanden. Die genaue Sitzungsanzahl nach Wiederholung wurde nicht separat gezeigt. Abbruch, Fremd-/Ersatzkarten, weitere Generationen/Leser und Fehlerfälle wurden nicht als zusätzliche manuelle Erfolge behauptet; synthetische automatische Tests sichern diese Fälle ab. Der vollständige Import bleibt offen.

**Herkunft:** finaler PR-30-Kopf `09ccfdf2f270164f4ee9968dc9e022218b53360f`, vollständig grüner [CI-Lauf 36859243839](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36859243839). Ausgeliefert wurde nach dem Debug-Installationsfehler die parallel installierbare Internal-ZIP `FahrerTagebuch-PR30-Paralleltest-Internal.zip`, Artefakt `11161857220`. ZIP-SHA256 `cb9bd0b7f3f7ddfaf9e794adff24c4a46f585e66e84e5275c58526e0e8ee31c0`; APK-SHA256 `5aaa301d7215694ed2dc9f3f5fa24dead4949ddccf2a9da094770d939500abe4`.

**Historische Installations-/Testgrenze:** Die PR-28- und PR-30-Debug-Zertifikate unterscheiden sich. Die parallel bereitgestellte Internal-App hat eine andere Paketkennung; dieser PR-30-Test wies keinen Datenerhalt bei einem echten Update nach. Vorhandene Daten nicht zur Umgehung löschen. Der separate PR-32-Test bestätigt inzwischen das echte Schema-5→6-Update innerhalb der konsistent signierten Test-App; die früheren Installationen bleiben getrennt.

### PR #28 – vorherige Nutzerrückmeldung

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

- [Finaler PR-32-Lauf 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2) für `7194869`: **vollständig grün**, 61 Unit-Tests, 37 Daten-/SQLCipher- und 30 App-/Compose-Emulatortests; keine Fehler oder übersprungenen Tests. Alle zehn neuen Normalisierungs-/Migrationstests bestanden. Lint, Debug-/Internal-/Test-APKs, gleiche Testsignaturen und steigende Versionscodes geprüft. Der erste Emulatorversuch scheiterte vor Testbeginn an der Runner-Hardwarebeschleunigung; nur der fehlgeschlagene Job wurde erneut ausgeführt und bestand.
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
- [AppDatabase.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/AppDatabase.kt) / [DatabaseMigrations.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DatabaseMigrations.kt): Schema 6, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6; keine destruktive Migration.
- [CardHardwareTestScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardHardwareTestScreen.kt), [CardSnapshotHistoryScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardSnapshotHistoryScreen.kt), [AppRoot.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/AppRoot.kt): lesen/speichern, Quellenübersicht und Auswahl der Vorschau.
- [EncryptedDatabaseInstrumentedTest.kt](../data/src/androidTest/kotlin/de/fahrerabrechnung/data/EncryptedDatabaseInstrumentedTest.kt) / [CardSourcePairNavigationTest.kt](../app/src/androidTest/kotlin/de/fahrerabrechnung/app/CardSourcePairNavigationTest.kt): Persistenz-/Migrations- und Navigationsregressionen.

## Noch offen und empfohlene Fortsetzung

Der Lesetest speichert weiterhin nur nach ausdrücklicher Bestätigung. Einzeldateien und Dateipaare sind **persistente Teilimporte**, noch keine vollständige Kartenimport-Pipeline.

Offen sind vollständige Importhistorie und Status/Fehlerbehandlung über alle Kartenquellen, vollständiger quellenübergreifender Tagesbestand sowie weitere Dateien für GNSS, Grenzübertritte, Ereignisse/Störungen und Kontrollen. Die Profil-/Kartenbindung aus PR #30 und normalisierten Tagesquellen aus PR #32 sind gemergt und im beschriebenen Umfang am Gerät bestanden. Dauerhafte Fahrzeugzuordnung, Tageskilometer/Arbeitszeiten und fachliche Reise-/Spesen-/Zuschlagsauswertung fehlen. Grundscreens und vorbereitete Entities/Interfaces sind teilweise Preview.

Empfohlener nächster Arbeitsabschnitt: den zuverlässigen vollständigen Import und sein Datenmodell weiter ausbauen, ausgehend von den gespeicherten Quellen. Einen begrenzten Folgeschritt und seine Migration/Fehlerfälle vor Umsetzung festlegen. Die aktuelle Übergabe ist kein automatischer Auftrag, sämtliche Produktpunkte sofort zu implementieren.

**Profilbindung fachlich festgelegt am 01.10.2026:** Single-Driver-App; erste eigene Karte ausdrücklich bestätigen. Die dauerhafte Fahreridentität ist das lokale Profil, nicht die Kartennummer. Belastbar gleiche Inhaber-Identität erlaubt zusätzliche Folge-/Ersatzkarten; unklare Fälle verlangen Bestätigung. Erkennbar fremde Karten bleiben auf den temporären technischen Lesetest begrenzt. Alte Karten/Imports bleiben erhalten; ältere Teilimporte nicht anhand von Datum, Kartennummer oder Vermutung zuordnen. Zielmodell und vollständige Vorgabe stehen in [CURRENT_RULES.md](CURRENT_RULES.md).

Abgeschlossen: normalisierte Tagesquellen mit unveränderlicher Herkunft auf der vorhandenen Profil-/Kartenbindung, vollständige CI und echtes Update von Schema 5 auf 6. Darauf die vollständige Import-Pipeline in begrenzten Schritten weiter ausbauen. Das Onboarding speichert bisher nur Heimatpunkt und Abschlussstatus; die übrigen Profil-/Fachfelder bleiben Vorschau.

### Verbindliche Grenzen bei der Fortsetzung

- Keine Smartphone-Routenaufzeichnung, Hintergrundortung oder Smartphone-Kilometer; Heimatpunkt ausdrücklich per Standort/Karte.
- Echte Tachograph-/Fahrerkarten-GNSS-Punkte später einzeln zeigen; keine erfundene Verbindungsstrecke.
- Kartenentnahme/fehlende Zeit beweist keine Ruhe oder Heimkehr. Heimkehr verlangt belastbaren Heimatstandort und mindestens 8:59 h zusammenhängende belegte Ruhe, rückwirkend auf deren Beginn.
- Übernachtung, Spesen und Arbeitgeberzuschläge gemäß Issue #3/CURRENT_RULES; keine Steuerlogik. Feiertagsgrundlage ist das Bundesland des Firmenstandorts.
- Für die spätere Fahrzeugdarstellung [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md) beachten; Modell/VIN-Zuordnung oder Fahrzeugbild nie raten.
- Rohkartendaten und vollständige APDUs nicht loggen; Testdaten synthetisch, gespeicherte Kartennummern in der Oberfläche maskieren.

### Test- und Arbeitsvereinbarungen

Manueller Gerätetest **spätestens bei jedem dritten PR**, früher sobald Hardware, Import, Parser, Persistenz oder Datenbank betroffen sind. Mehrere PRs bis zu einem späteren Test nur nach konkreter Nutzerfreigabe zusammenfassen. Der bestandene PR-32-Test ist der aktuelle Ausgangspunkt; frühere Testaufschübe sind keine dauerhafte Ausnahme.

Auf dem bestandenen Test aufbauen; ihn nicht allein wegen des Work-Wechsels wiederholen. Nach neuen Änderungen die passenden automatischen Prüfungen und den nächsten erforderlichen Gerätetest planen. Der Nutzer kann Bildduplikate durch lange Handy-Screenshots haben; vor einer Datenkorrektur echte App-Einträge prüfen.

Bei Unterbrechung zuerst lokale unveröffentlichte Änderungen und den aktuellen GitHub-Stand prüfen und vorhandene Folgearbeit fortführen. PR #30/#31/#32 sind gemergt; Profilbindung und normalisierte Tagesquellen nicht erneut implementieren. Die vollständige Import-Pipeline ist weiterhin offen. Den grünen CI- und bestandenen Gerätetest-Stand von PR #32 als Ausgangsbasis verwenden.
