# Work-Übergabe – FahrerTagebuch

**Stand: 02.10.2026.** Diese Datei hält den technischen Fortschritt und den zuletzt vom Nutzer bestätigten Gerätetest fest. Sie ersetzt keine Fachregeln.

## Einstieg und Ausgangsbasis

1. Zuerst [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) und [CURRENT_RULES.md](CURRENT_RULES.md) lesen. Beide sind der verbindliche Produkt-/Fachstand; historische Design-/GPS-Dokumente gelten nur ohne Widerspruch.
2. Danach diese Übergabe, [WORK_STATUS.md](../WORK_STATUS.md) und [README.md](../README.md) lesen.
3. Den aktuellen `main` aus GitHub verwenden und vor der nächsten Änderung HEAD, offene PRs und CI erneut prüfen. Alte Scratch-Verzeichnisse oder ZIPs können fehlen oder älter sein.

| Bezug | Stand |
|---|---|
| Letzter Produktcode-PR | [PR #37: Aktivitäts- und Fahrzeugdatei unabhängig lesen](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/37), nach bestandenem Gerätetest gemergt |
| Produktcode-Basis auf main | [`c4c4a7ef9a49ec003476731518cabadaa55ebe97`](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/commit/c4c4a7ef9a49ec003476731518cabadaa55ebe97); spätere reine Dokumentationscommits ändern diese technische Basis nicht |
| Getesteter PR-37-Kopf | `af612b87f5266c79cf70201c4b37ef176ed69bdc`; Merge-Tree `f7fdd8bdd3c807ecb33b1e1941de330a5e2f83d5` entspricht exakt diesem getesteten Stand |
| Abgeschlossener begrenzter Produktabschnitt | Automatische Vordergrund-Lesung und Übernahme eigener Aktivitäten/Fahrzeugnutzungen samt dauerhafter Importhistorie; vorhandene Profilbindung, Quellen und Normalisierung weiterverwenden |
| Datenbank auf main | Room/SQLCipher, Schema **8**, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8; keine destruktive Migration oder automatische Zuordnung alter Quellen |
| Automatischer Nachweis | [PR-37-Lauf 37003289077](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37003289077): vollständig grün, 84 Unit-Tests und 103 Emulatortests (67 Daten/SQLCipher, 36 App/Compose). Schema-8-Export byte-identisch zur bestandenen PR-35-Basis |
| Letzter manueller Gerätetest | **02.10.2026, PR #37: Update innerhalb Schema 8, automatischer USB-Ablauf und Wiederöffnung bestanden**, Details und Grenzen unten |

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
- Die separate App **FahrerTagebuch Test** erhält eine gleichbleibende öffentliche Entwicklungssignatur. Für PR #32 baute/prüfte CI eine Schema-5-Ausgangs-APK und das Schema-6-Update mit derselben Paketkennung. Die bisherigen Debug-/Internal-Installationen bleiben getrennt; aktuelle Folgetests, Testschlüssel und Buildherkunft in [ci/README.md](../ci/README.md).

## Abgeschlossene normalisierte Fahrzeugnutzungen (PR #34)

[PR #34](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/34), Branch `feature/normalized-vehicle-sources`, baut auf dem bestandenen PR-32-Stand auf. Zwei additive Tabellen in Schema 7 speichern jeden dekodierten Fahrzeugnutzungsinhalt sowie dessen genaue Herkunft aus physischer Karte, Quelldatei, Datensatzreihenfolge und Ringslot. Identische Inhalte derselben Karte werden unabhängig vom Ringslot dedupliziert; geänderte Nutzungsfassungen und Ersatzkarten bleiben getrennt und schreiben frühere Imports nicht um.

Normalisierung, Kartentage, Rohquellen, Dateipaar, konkrete Karte und Sitzung werden gemeinsam atomar gespeichert. Profilvorschau und Fahrzeughistorie lesen die gespeicherten Nutzungen. Ältere unnormalisierte Quellen bleiben read-only über den bisherigen Parser verfügbar; weder Migration noch Öffnen oder Wiederholen eines alten Speicherauftrags erzeugt eine neue Zuordnung. Erst ein frischer eigener Lese-/Speichervorgang darf vorhandene identische Rohdateien wiederverwenden und zusätzlich normalisieren.

Keine aufgelöste dauerhafte Fahrzeugentität, neue Fahrzeugdarstellung, Kilometerberechnung, Tätigkeitszuordnung oder automatische USB-Bedienung in diesem Schritt. Fünf neue Fingerprint-Unit-Tests und zwölf SQLCipher-Instrumentierungstests decken exakte Quellwerte/offene Enden, Gen1, Ringverschiebung, Fassungen, Ersatz-/Fremdkarten, Wiederöffnung, Deduplizierung, Rollback und Migration 6 → 7 ab. Vollständigen automatischen Nachweis am veröffentlichten Kopf über die [PR-34-Prüfungen](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/34/checks) heranziehen. Der [Update-Gerätetest](NORMALIZED_VEHICLES_DEVICE_TEST.md) wurde am 02.10.2026 um 10:53 Uhr bestätigt; PR #34 danach mit unverändertem Kopf gemergt. Finale CI 36978402626: 66 Unit-, 49 Daten-/SQLCipher- und 30 App-/Compose-Tests, keine Fehler/übersprungenen Tests.

Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/7.json` stammt aus dem erfolgreichen Build von [Lauf 36976965131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36976965131), Produktkopf `50327a1`. Alle 14 bisherigen Entities sind exakt identisch zu Schema 6; nur die beiden beschriebenen Tabellen kommen hinzu. Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem generierten Export; keine Fremdschlüsselverletzung. Schema-SHA256: `6d7a2837b83e9ccf549e45d0154abc4c3b17fdc94c86e82d90608e2bd8ff7cf0`. Der Build bestand 66 Unit-Tests, Lint und die APK-/Signatur-/Versionsprüfung; die vollständige Emulatorverifikation ist über den jeweils aktuellen PR-Kopf nachzuweisen.

Das neue Testpaar verwendet Schema 6 aus dem unveränderten PR-32-Merge `b0583a6ea8b15b7260b76c19e56f5166608bb898` als Ausgangscode (40101) und Schema 7 als Update (40102). Nutzer mit eingerichteter PR-32-Test-App installieren nur die Update-APK; kein erneutes Onboarding oder Löschen vorhandener Daten.

## Abgeschlossener automatischer Vordergrund-Import (PR #35)

`feature/card-import-lifecycle` baut auf dem unveränderten PR-34-Merge auf. Der normale Ablauf erkennt vorhandene/angeschlossene Leser und eingelegte Karten in der geöffneten App. Android-USB-Freigabe bleibt notwendig. Einmal pro beobachteter Einlage lesen; keine automatische Leseschleife bei unveränderter eingesteckter Karte. Karte herausnehmen und erneut einlegen löst einen frischen Lesevorgang aus. Nach Rückkehr in den Vordergrund wird die Karte frisch geprüft. Kein Hintergrunddienst, kein Lesen bei geschlossener App, keine Smartphone-Ortung.

Bekannte/reliabel eigene Karten werden ohne Speichern-Knopf übernommen. Erste eigene/unklare Ersatzkarte verlangt Bestätigung; davor wird die tatsächlich eingesteckte Karte nochmals vollständig gelesen. Stimmen Karte/Inhaber nicht mit dem Dialog überein, wird die alte Bestätigung verworfen und die aktuelle Karte erneut geprüft. Fremde/unbestätigte Karten erzeugen weder Profilbindung noch dauerhaften Importlauf oder Datensitzung. Der zuletzt eigene Import wird während fremder/noch ungeklärter Live-Karten nicht als deren Daten angezeigt.

Schema 8 ergänzt ausschließlich `card_import_runs` und `card_import_file_results`. Eigene akzeptierte Übernahmen erhalten exakte unterstützte Datenstatus (gespeichert/fehlend/Lesefehler/ungültig/leer/nicht gespeichert), Kartengeneration und Quellzählungen/Zeitraum. Rohquellen, normalisierte Inhalte, Sitzung und erfolgreicher Ergebnisbeleg werden gemeinsam atomar gespeichert. Ein Speicherfehler rollt alle neuen Datenschreibvorgänge zurück und bleibt als eigener Fehlversuch ohne Datensitzung sichtbar. Prozessabbruch wird beim nächsten Prozessstart einmal als unterbrochen beendet; es werden keine gelesenen Daten rekonstruiert. Neue requestId ist ein neuer Versuch, derselbe Auftrag bleibt inhaltlich geprüft idempotent. Unveränderte Daten werden weiterhin dedupliziert.

Importverlauf verwendet Metadaten und öffnet nur die ausgewählte Sitzung; defekte Rohdaten verhindern nicht die ganze Metadatenübersicht. Frühere explizite eigene Sitzungen bleiben sichtbar, ohne erfundene neue Fehler-/Zeitdetails. Unzugeordnete Dateien bleiben im getrennten read-only Archiv. Die normale gespeicherte Vorschau führt keine Leser-/Speicheraktion aus. Fünf Haupttabs, APDUs, Dateiparser, Tages-/Fahrzeugquellen, Fachlogik und Produktgrenzen bleiben erhalten. Weitere Kartenfiles, kanonischer Kalender, Fahrzeugentität, Kilometer-/Arbeitszeit-/Spesenberechnung und vollständiger Import fehlen.

Zwölf neue Unit-Tests des Koordinators, fünfzehn neue reale SQLCipher- und sechs neue Compose-Tests sind vorhanden. Der [USB-/Update-Gerätetest](AUTOMATIC_IMPORT_DEVICE_TEST.md) war erforderlich, weil Anschluss, Slotzustand, Freigabe und Lesen seit PR #35 automatisch zusammenarbeiten. Test-APK: Schema-7-Ausgangscode `7eb2c6fa8aaba38a09e89fa292a6bf9265248684`, 40102, Update 40103, gleicher öffentlicher Testschlüssel/Paketkennung. Nutzer mit PR-34-Test-App nur das Update installieren. [PR #35](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/35) ist nach dem am 02.10.2026 um 13:30 Uhr bestätigten Gerätetest am unveränderten Kopf gemergt.

Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/8.json` stammt aus dem erfolgreichen Build von [Lauf 36993377935](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36993377935), Produktkopf `31cb01eb2720ec2ed4af3df5ca370028ee280b46`. Schema-SHA256 `5614d0a4b4146e08397be0740dff5e6dc04822ff3dd53e332510d983b54d3c8d`, Room-Identität `3ccf9792f9859e1e03e16faa5afe016d`. Alle 16 bisherigen Entities exakt identisch zu Schema 7; nur die beiden Tabellen für Import-/Datenstatus kommen hinzu. Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem Export, keine FK-Verletzung.

[Finaler Lauf 36996161264](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36996161264) am unveränderten getesteten Kopf `47ea64cbea72c24c6231af26d451ee59d611b6dd`: 78 Unit-, 64 Daten-/SQLCipher- und 36 App-/Compose-Tests bestanden, keine Fehler oder übersprungenen Tests. Lint, Debug-/Internal-/DeviceTest-Builds sowie Signatur-/Paket-/Versionsprüfungen erfolgreich; generierter Schemaexport im finalen Build byte-identisch. Die Sitzungsauswahl erfolgt auf dem UI-Hauptthread; Navigation bei USB-Freigabe und leerem Leser ist regressionsgeprüft.

## Abgeschlossene unabhängige Dateilesung (PR #37)

Branch `fix/independent-card-source-reads` baut auf dem unveränderten bestandenen PR-35-Stand und dessen aktualisierter Übergabe auf. Nach gültiger Kartenidentifikation werden die unterstützten Aktivitäts- und Fahrzeugdateien unabhängig gelesen: ein Aktivitäts-Lesefehler verhindert eine vollständige Fahrzeuglesung nicht mehr. Unvollständige Bytes werden verworfen; der Status unterscheidet echten Lesefehler von fehlendem Eingang. Ungültige Identität bleibt gesperrt, Cancellation wird weitergereicht. Alte Daten werden nicht als Ersatz in die neue Sitzung gepaart. Wenn beide Dateien fehlschlagen, bleibt ein eigener Fehlversuch ohne neue Datensitzung; frühere Daten bleiben erhalten.

Sechs zusätzliche APDU-Unit-Tests für Gen1/Gen2/Gen2V2 und drei zusätzliche SQLCipher-Tests prüfen fehlende/abgeschnittene/ungültige Aktivitäten, unabhängige Fahrzeuge, Abbruch, zwei Fehler, Statusunterschied, Deduplizierung und exakte Wiederöffnung. Der bisherige Trunkierungstest erwartet jetzt vollständige verfügbare Fahrzeuge statt Verlust aller Daten. Schema 8, bestehende Parser, Profilregeln und Tabellen bleiben unverändert; vollständige Kartenabdeckung bleibt offen. [Finale CI 37003289077](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37003289077) am Kopf `af612b87f5266c79cf70201c4b37ef176ed69bdc`: 84 Unit-, 67 Daten-/SQLCipher- und 36 App-/Compose-Tests, keine Fehler oder übersprungenen Tests. Lint, APK-/Signatur-/Versionsprüfung und Schemaexport bestanden. Test-App 40103→40104, gleiche Testsignatur. Der [Gerätetest](INDEPENDENT_READ_DEVICE_TEST.md) ist seit 02.10.2026 um 14:24 Uhr bestanden, PR danach unverändert gemergt.

## Letzte Hardware-/Gerätetests

### PR #37 – letzter bestätigter Leser-/Update-Test

**02.10.2026, 14:24 Uhr Europe/Berlin:** Nutzer bestätigte **„Passt alles.“** zur unmittelbar zuvor im Chat bereitgestellten [Anleitung](INDEPENDENT_READ_DEVICE_TEST.md). Update 40103→40104 ohne Deinstallation/Datenlöschen, Profil und ältere Daten erhalten; eigene Karte automatisch ohne Erstbestätigung übernehmen, Aktivitäten und Fahrzeuge verfügbar, keine Leseschleife; Wiederöffnung alter/neuer Imports nach vollständigem Neustart ohne Leser. Lesertrennung während der Übernahme war zusätzlich vorgesehen, soweit herstellbar; dazu liegt kein separates Einzelprotokoll vor. Vorhandener Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext, keine neuen Einzelmesswerte oder zusätzlichen Karten-/Leserkombinationen. Isolierte Fehler einer einzelnen Kartendatei sind durch die automatischen Tests belegt, nicht als manuell erzeugter Hardwarefall bestätigt.

**Herkunft:** `FahrerTagebuch-PR37-Lesefehler-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11225246786`, [Lauf 37003289077](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37003289077), Kopf `af612b87f5266c79cf70201c4b37ef176ed69bdc`. Schema 8 in beiden APKs; Ausgangs-APK 40103 byte-identisch zur bestandenen PR-35-Update-APK, Update 40104. Gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und öffentliche Entwicklungssignatur.

- ZIP-SHA256: `87e6b09a3d4f12386f11edb71dad3f301ce9e6e0e29a3a6d029a1dd196d50bb9` (50.246.717 Bytes).
- Ausgangs-APK-SHA256: `0444438a09aa3c9f495b8385fdcb19fb87c1a6cbf88d0f2e55d724160f3aef0e`.
- Update-APK-SHA256: `821eba8989256aa66026af7a8e56bde5d8ac505e00f5ceab58fb2c6b52b38220`.

PR #37 am unveränderten Kopf gemergt: `c4c4a7ef9a49ec003476731518cabadaa55ebe97`, Tree `f7fdd8bdd3c807ecb33b1e1941de330a5e2f83d5`, exakt der getestete Stand. Dieser bestandene Ablauf ist die aktuelle Hardwarebasis. Die Freigabe zum gebündelten Weiterarbeiten bleibt bestehen; erfolgreiche Tests nicht allein wegen einer Work-Unterbrechung wiederholen.

### PR #35 – vorheriger bestätigter automatischer USB-/Update-Test

**02.10.2026, 13:30 Uhr Europe/Berlin:** Nutzer bestätigte **„Hab alles abgearbeitet, und es funktioniert alles wunderbar“** zur unmittelbar zuvor im Chat bereitgestellten [Anleitung](AUTOMATIC_IMPORT_DEVICE_TEST.md). Rückmeldung umfasst echtes Schema-7→8-Update ohne Deinstallation und Erhalt von Profil/älteren Daten; automatisches Öffnen/Lesen/Speichern bei Leseranschluss und Karteneinlage; genau ein Lesevorgang je beobachteter Einlage, keine Leseschleife/identischen Zusatznutzungen; erneutes Einlegen ohne Lesertrennung; Wiederöffnung alter/neuer Daten nach vollständigem Neustart ohne Leser sowie Erkennen des bereits angeschlossenen Lesers beim App-Start. Die Anleitung sah zusätzlich Freigabe-/Abbruchfälle vor, soweit am Gerät herstellbar; dazu keine getrennten Einzelprotokolle. Vorhandener Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext; keine neuen Einzelmesswerte, weiteren Karten-/Leserkombinationen oder manuell geprüften Fremd-/Ersatzkarten behaupten.

**Herkunft:** `FahrerTagebuch-PR35-Autoimport-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11222550163`, [Lauf 36996161264](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36996161264), Kopf `47ea64cbea72c24c6231af26d451ee59d611b6dd`. Schema-7-Ausgangs-APK (40102) byte-identisch zur bestandenen PR-34-Update-APK; Schema-8-Update (40103), gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und öffentliche Entwicklungssignatur.

- ZIP-SHA256: `0e6c514daab2417731e9467e7018669167edb639ae13cb099d18bbc00d30a845` (50.145.087 Bytes).
- Ausgangs-APK-SHA256: `592435fd14f5a1c238d3bb8b12162dcc727debe65caa3de3d957bde4fa7e44cd`.
- Update-APK-SHA256: `0444438a09aa3c9f495b8385fdcb19fb87c1a6cbf88d0f2e55d724160f3aef0e`.

PR #35 mit unverändertem Kopf gemergt: `909dd6652d029cab842ee986c4756fac0fd2ff93`, Tree `62541078580465bed35c79175594c8744e45b72b`, exakt derselbe Produktstand wie in der geprüften APK. Dieser bestandene Ablauf war bis PR #37 die Hardwarebasis und wird allein wegen eines Work-Wechsels nicht wiederholt. Die am 02.10.2026 erlaubte gebündelte Fortsetzung bleibt bestehen.

### PR #34 – vorheriger bestätigter Update-/Persistenztest

**02.10.2026, 10:53 Uhr Europe/Berlin:** Nutzer bestätigte **„Hat alles geklappt“** zu den unmittelbar zuvor im Chat bereitgestellten PR-34-Testschritten: echtes Update Schema 6→7 ohne Deinstallation, bestehendes Profil/ältere Kartentage und Fahrzeugnutzungen erhalten; eigene Karte erneut ohne Erstbestätigung lesen und gemeinsam speichern; alte und neue Sitzungen/Daten nach vollständigem Neustart ohne Leser öffnen; unveränderte Wiederholung erzeugt keine zusätzlichen identischen Fahrzeugnutzungen. Dies gilt im vorhandenen Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext. Keine neuen Einzelmesswerte, zusätzliche Karten-/Leserkombinationen oder Fremd-/Ersatzkarten manuell behauptet.

**Herkunft:** `FahrerTagebuch-PR34-Update-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11214921515`, [Lauf 36978402626](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36978402626), getesteter Kopf `c4392761a5db904ec9ad5b4ea9a3d999be68053c`. Schema-6-Ausgangscode `b0583a6ea8b15b7260b76c19e56f5166608bb898`, Version 40101; Schema-7-Update 40102. Paket `de.fahrerabrechnung.app.devicetest` und Entwicklungssignatur gleich.

- ZIP-SHA256: `917bf5c61eabd221dfcb62b55fc62ef923701ebcc055375f53b4f703c6637c32`.
- Ausgangs-APK-SHA256: `87436270c797fc82f31c158caaa6bfa3659e71e93c16e0e5353e6a800ae82884`.
- Update-APK-SHA256: `592435fd14f5a1c238d3bb8b12162dcc727debe65caa3de3d957bde4fa7e44cd`.

PR #34 wurde mit unverändertem Kopf gemergt: `7eb2c6fa8aaba38a09e89fa292a6bf9265248684`, Tree `be841b982792b02b8ae07e1f3ad2b1f3f134242e`, exakt der getestete Kopf. Dieser Test ersetzte PR #32 als damalige Hardwarebasis; aktuell ist PR #37 maßgeblich.

In derselben Nachricht erlaubte der Nutzer: **„mach bitte erstmal ein gutes Stück alleine weiter, und melde dich bitte erst wenn ein Hardware test wirklich erforderlich ist“**. Das autorisiert gebündelte begrenzte Folgeschritte mit vollständiger automatischer Verifikation bis zum sachlich notwendigen Hardware-Prüfpunkt; kein erneutes Nachfragen nach jedem PR. Die inzwischen bestandene Leserprobe von PR #35 ist oben dokumentiert; bisherige erfolgreiche Tests werden nicht allein wegen einer Work-Unterbrechung wiederholt.

### PR #32 – vorheriger bestätigter Update-/Persistenztest

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

PR #32 wurde nach dieser Bestätigung mit unverändertem Kopf gemergt. Dieser Test war bis zum bestandenen PR-34-Test der Ausgangspunkt für weitere import-/persistenzrelevante Änderungen.

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
- [AppDatabase.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/AppDatabase.kt) / [DatabaseMigrations.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DatabaseMigrations.kt): main Schema 8, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8; keine destruktive Migration.
- [NormalizedCardVehicleStore.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/NormalizedCardVehicleStore.kt) / [DriverVehicleUseDao.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DriverVehicleUseDao.kt): Folgeschritt mit unveränderlichen Fahrzeugnutzungen und additiver Migration 6 → 7; bisherige Tabellen unverändert.
- [CardHardwareTestScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardHardwareTestScreen.kt), [CardSnapshotHistoryScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardSnapshotHistoryScreen.kt), [AppRoot.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/AppRoot.kt): lesen/speichern, Quellenübersicht und Auswahl der Vorschau.
- [EncryptedDatabaseInstrumentedTest.kt](../data/src/androidTest/kotlin/de/fahrerabrechnung/data/EncryptedDatabaseInstrumentedTest.kt) / [CardSourcePairNavigationTest.kt](../app/src/androidTest/kotlin/de/fahrerabrechnung/app/CardSourcePairNavigationTest.kt): Persistenz-/Migrations- und Navigationsregressionen.

## Noch offen und empfohlene Fortsetzung

Die normale Vordergrund-Lesung übernimmt unterstützte eigene Daten automatisch. Erste eigene/unklare Ersatzkarten verlangen weiterhin Bestätigung. Aktivitäts-/Fahrzeugquellen sind **persistente, profilgebundene Quellen**, noch keine vollständige Kartenabdeckung oder fachliche Tageszusammenführung.

Offen sind vollständige Importhistorie und Status/Fehlerbehandlung über alle Kartenquellen, vollständiger quellenübergreifender Tagesbestand sowie weitere Dateien für GNSS, Grenzübertritte, Ereignisse/Störungen und Kontrollen. Die Profil-/Kartenbindung aus PR #30 und normalisierten Tagesquellen aus PR #32 sind gemergt und im beschriebenen Umfang am Gerät bestanden. Dauerhafte Fahrzeugzuordnung, Tageskilometer/Arbeitszeiten und fachliche Reise-/Spesen-/Zuschlagsauswertung fehlen. Grundscreens und vorbereitete Entities/Interfaces sind teilweise Preview.

Empfohlener nächster Arbeitsabschnitt: den zuverlässigen vollständigen Import und sein Datenmodell weiter ausbauen, ausgehend von den gespeicherten Quellen. Einen begrenzten Folgeschritt und seine Migration/Fehlerfälle vor Umsetzung festlegen. Die aktuelle Übergabe ist kein automatischer Auftrag, sämtliche Produktpunkte sofort zu implementieren.

**Profilbindung fachlich festgelegt am 01.10.2026:** Single-Driver-App; erste eigene Karte ausdrücklich bestätigen. Die dauerhafte Fahreridentität ist das lokale Profil, nicht die Kartennummer. Belastbar gleiche Inhaber-Identität erlaubt zusätzliche Folge-/Ersatzkarten; unklare Fälle verlangen Bestätigung. Erkennbar fremde Karten bleiben auf den temporären technischen Lesetest begrenzt. Alte Karten/Imports bleiben erhalten; ältere Teilimporte nicht anhand von Datum, Kartennummer oder Vermutung zuordnen. Zielmodell und vollständige Vorgabe stehen in [CURRENT_RULES.md](CURRENT_RULES.md).

Abgeschlossen: normalisierte Tagesquellen mit unveränderlicher Herkunft auf der vorhandenen Profil-/Kartenbindung, vollständige CI und echtes Update von Schema 5 auf 6. Normalisierte Fahrzeugnutzungsquellen aus PR #34 sind ebenfalls abgeschlossen. Die automatische Vordergrund-Übernahme mit nachvollziehbaren Import-/Quellenstatus aus PR #35 ist ebenfalls abgeschlossen. Weitere Kartenquellen in begrenzten Stufen auf dieser Basis ergänzen; keine Fachwerte oder Identitäten raten. Das Onboarding speichert bisher nur Heimatpunkt und Abschlussstatus; die übrigen Profil-/Fachfelder bleiben Vorschau.

### Verbindliche Grenzen bei der Fortsetzung

- Keine Smartphone-Routenaufzeichnung, Hintergrundortung oder Smartphone-Kilometer; Heimatpunkt ausdrücklich per Standort/Karte.
- Echte Tachograph-/Fahrerkarten-GNSS-Punkte später einzeln zeigen; keine erfundene Verbindungsstrecke.
- Kartenentnahme/fehlende Zeit beweist keine Ruhe oder Heimkehr. Heimkehr verlangt belastbaren Heimatstandort und mindestens 8:59 h zusammenhängende belegte Ruhe, rückwirkend auf deren Beginn.
- Übernachtung, Spesen und Arbeitgeberzuschläge gemäß Issue #3/CURRENT_RULES; keine Steuerlogik. Feiertagsgrundlage ist das Bundesland des Firmenstandorts.
- Für die spätere Fahrzeugdarstellung [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md) beachten; Modell/VIN-Zuordnung oder Fahrzeugbild nie raten.
- Rohkartendaten und vollständige APDUs nicht loggen; Testdaten synthetisch, gespeicherte Kartennummern in der Oberfläche maskieren.

### Test- und Arbeitsvereinbarungen

Manueller Gerätetest **spätestens bei jedem dritten PR**, früher sobald Hardware, Import, Parser, Persistenz oder Datenbank betroffen sind. Mehrere PRs bis zu einem späteren Test nur nach konkreter Nutzerfreigabe zusammenfassen. Der bestandene PR-37-Test ist der aktuelle Ausgangspunkt. Die ausdrücklich am 02.10.2026 erlaubte Bündelung ist oben festgehalten; kein schematischer Testaufruf nach jedem Folge-PR. Frühere Aufschübe sind keine allgemeine dauerhafte Ausnahme.

Auf dem bestandenen Test aufbauen; ihn nicht allein wegen des Work-Wechsels wiederholen. Nach neuen Änderungen die passenden automatischen Prüfungen und den nächsten erforderlichen Gerätetest planen. Der Nutzer kann Bildduplikate durch lange Handy-Screenshots haben; vor einer Datenkorrektur echte App-Einträge prüfen.

Bei Unterbrechung zuerst lokale unveröffentlichte Änderungen und den aktuellen GitHub-Stand prüfen und vorhandene Folgearbeit fortführen. PR #30/#31/#32 sind gemergt; Profilbindung und normalisierte Tagesquellen nicht erneut implementieren. PR #34 und PR #35 sind nach bestandenen Tests gemergt. Automatischen Import, normalisierte Quellen und Schema 8 nicht nochmals implementieren. Lokale/unveröffentlichte Folgeänderungen zuerst prüfen und exakt dort fortsetzen. Vollständige Kartenabdeckung und Fachauswertung bleiben offen. Der bestandene PR-35-Gerätetest bleibt bis zum nächsten bestätigten Test die Ausgangsbasis.

## Laufender Folgeschritt: Kontrolldaten

`feature/card-control-source` erweitert die bestehende Pipeline ausschließlich um EF Control_Activity_Data (0508h, 46 Bytes in allen drei unterstützten Generationen). [Technischer Umfang, Feldbelegung und offizielle Quellen](CONTROL_IMPORT_FORMAT.md). Zwei additive Tabellen in Schema 9 erhalten vollständige verschlüsselte Quellen je physischer DriverCard und ihre exakte Sitzung; alle 18 bisherigen Entities bleiben unverändert, kein Backfill. Neue Quellenstatus unterscheiden fehlgeschlagene/ungültige Kontrolldaten von einer vollständig gelesenen Datei ohne Kontrollbeleg. Ältere Läufe behalten ihre tatsächliche Zweiquellen-Abdeckung und ihren Request-Fingerprint. Profile, Karten, Normalisierung und atomare Speicherung weiterverwenden.

Kontrollen stehen ausschließlich in gespeicherte Fahrerkarte → Details → Kontrollen, mit standardmäßig maskierter Kontrollkartennummer. Exakt ausgewählte Quelle, keine spätere Kontrollangabe in ältere Imports übernehmen, keine Verstoßbewertung, kein weiterer Haupttab. Acht Parser-/Fingerprint-, vier Leser-, elf SQLCipher- und fünf Compose-Testmethoden ergänzt. Generierter Schema-9-Export und vollständige CI noch abzuschließen; kein manueller Test bestätigt. [Kurzer Gerätetest](CONTROL_IMPORT_DEVICE_TEST.md) erforderlich, erst nach vollständiger grüner CI Test-ZIP aus exakt finalem Kopf liefern. Ausgangscode getesteter PR-37-Merge, 40104, Schema 8; Update 40105, Schema 9. Vorhandene App direkt aktualisieren und bis bestätigtem Test ungemergt lassen.
