# CI-Prüfungen und gezielte Testdateien

Stand: 10.10.2026. Infrastrukturvorgabe des Nutzers; keine Änderung an Produktregeln, App-Code, Kartenimport, Signatur oder Datenbankschema.

## Normale Pull-Request- und main-Prüfung

`.github/workflows/android-ci.yml` prüft bei Code-, Build-, Schema- und CI-Änderungen weiterhin sämtliche bisherigen Gradle-Aufrufe: `help`, `build --dry-run`, `test`, `lint`, `assembleDebug`, `assembleInternal` und `:app:assembleDeviceTest`. Debug/Internal-APKs und das feste Gerätetest-Update-Paar werden auf dem kurzlebigen Runner gebaut und geprüft. Room-/SQLCipher- und Compose-Instrumentierung laufen vollständig auf dem bisherigen API-36-Emulator. Generierte Room-Exporte müssen dem eingecheckten Stand entsprechen; neue nicht eingecheckte Exporte sind ein Fehler.

**Ein erfolgreicher normaler Lauf lädt keine Actions-Artefakte hoch.** Keine Debug-/Internal-APKs, kein Projekt-ZIP, kein Gerätetest-Paar, keine erfolgreichen Instrumentierungsberichte. Console-Logs bleiben im Actions-Lauf verfügbar; Quellcode liegt in Git.

Bei Fehlschlag werden vorhandene relevante Build-/Testberichte und Logs für **3 Tage** hochgeladen. Ein fehlgeschlagener Diagnose-Upload verdeckt den eigentlichen Prüfungsfehler nicht. Erfolgreiche Prüfungen haben keine Upload-Abhängigkeit mehr.

Nur Änderungen an ausdrücklich zugelassenen Dokumentationspfaden erhalten leichte Prüfungen: Markdown im Projektwurzelverzeichnis, dokumentarische Markdown-/Bild-/PDF-Dateien unter `docs/` und `ci/README.md`. Eingebettete App-Assets, Skripte, Workflows, Schemaexporte und gemischte Änderungen laufen vollständig. Löschungen und beide Seiten von Umbenennungen werden berücksichtigt; unbekannte/fehlende Vergleichsbasis oder manuelles Starten führen zur vollständigen Prüfung.

Der Abschlussjob **Android verification** bestätigt entweder die vollständige Android-CI oder die erfolgreiche leichte Dokumentationsprüfung. Ein ausgefallener/abgebrochener benötigter Job führt niemals zu einem grünen Abschluss. Neue Commits im gleichen PR brechen überholte PR-Läufe ab. Jeder main- und manuelle Lauf hat eine eigene Concurrency-Gruppe, damit kein autoritativer Lauf durch PR-Commits oder eine gemeinsame Warteschlange verdrängt wird.

## Manueller Hardware-/Update-Test

`.github/workflows/device-test.yml` wird ausschließlich gezielt über `workflow_dispatch` gestartet:

- `source_sha`: vollständiger SHA des gewünschten Produktstands. Für genau diesen Stand müssen Build- und Emulatorjob eines vollständigen Android-CI-Laufs erfolgreich sein; eine Dokumentationsprüfung allein reicht nicht.
- `include_baseline`: standardmäßig `false`. Für eine bereits eingerichtete FahrerTagebuch-Test-App genügt die Update-APK. Nur für eine tatsächlich benötigte frische Migrationstest-Ausgangsinstallation `true` wählen.

Der Workflow verwendet die Infrastruktur seines ausgewählten Workflow-Refs und checkt den exakten Produktstand getrennt aus. Er verändert weder dessen Produktdateien noch dessen Version. Das vorhandene Skript prüft Paketkennung, feste öffentliche Testsignatur und steigende Versionscodes des Update-Paars. Zusätzlich wird der bekannte Zertifikats-SHA-256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` geprüft.

Ausgabe: eine ZIP mit benötigter Update-APK, passender Anleitung aus demselben Produktstand, Prüfsummen und Buildherkunft; die Ausgangs-APK nur bei ausdrücklicher Auswahl. Aufbewahrung **7 Tage**. Kein vollständiges Projekt-ZIP. Fehlerdiagnosen: **3 Tage**.

CLI-Beispiel nach Übernahme des Workflows auf main:

```bash
gh workflow run device-test.yml --ref main -f source_sha=<vollständiger-geprüfter-Produkt-SHA> -f include_baseline=false
```

Gleichwertig ist die GitHub-REST-API für Workflow-Dispatch. Work öffnet dafür keine GitHub-Weboberfläche. Ohne verfügbare API-/CLI-Startmöglichkeit den fehlenden konkreten Start melden; nicht eigenmächtig auf den Browser ausweichen.

## Übergabe zu PR #45 und Speicherkontingent

Die Infrastrukturarbeit basiert auf main `4b2c0b875d1912a91013bb23f0dbfe1596d0b3a2` und ändert PR #45 nicht. Infrastruktur-PR #46 wurde am unveränderten Kopf `aa27d6a3b0a2ffec68ce80c13d350f06312d41a4` vollständig geprüft: [Lauf 37838912358](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37838912358), alle vier Jobs grün einschließlich vollständiger Android-/Emulatorprüfung und beider Schemaexport-Prüfungen. Der erfolgreiche Lauf erzeugte **0 Actions-Artefakte**. Merge auf main: `c2132388d1c0b49b17e19d3516367712ff892a4d`, Tree bytegleich zum geprüften Kopf. App-/Produktcode, Leser/Parser, Schema 11 und Testsignatur blieben unverändert. Der autoritative main-Lauf prüft diese Infrastruktur zusätzlich; keine PR-45-Freigabe daraus ableiten.

**Historie vom 08.10.2026 (überholt durch den Abschluss unten):** PR-45-Produktkopf bleibt `42fb5dea0fb8f18d1b2206bda5d09b59b4ebe8b1`; Schema 11. Am 08.10.2026 wurde nach der Prüfung des unveränderten Kopfs **genau der freigegebene fehlgeschlagene Job `111385798418`** des bestehenden Laufs `37137254348` wiederholt. [Versuch 4](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37137254348/attempts/4) bestand alle sieben Gradle-Prüfungen, APK-Verifikation und das stabile Testupdate-Paar. Der neue Build-Job **`113524605169`** scheiterte um 22:31:55 Europe/Berlin ausschließlich bei `Upload build logs and reports`: `Artifact storage quota has been hit`. Abhängiger Emulatorjob **`113527170916`** wurde übersprungen; die neuen Compose-Fälle sind nicht ausgeführt. Keine Artefakte vorhanden. **An der externen Blockade gestoppt; keine weitere Wiederholung und kein zusätzlicher Cleanup.** Ein erneuter Jobversuch benutzt weiter den ursprünglichen Workflow; diese neue Speicherpolitik ändert den historischen Lauf nicht rückwirkend.

**Abschluss am 10.10.2026:** Der Nutzer erhöhte das Actions-Budget auf **2,00 USD**, Stop usage bleibt **Yes**. Nach kurzer Propagationszeit und erneuter Prüfung des unveränderten Kopfs wurde **genau ein gezielter Retry** des damals aktuellen fehlgeschlagenen Build-Jobs `113762376670` gestartet. [Versuch 6](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37137254348/attempts/6) vollständig grün: Build `114119290705`, Emulator `114120535610`, insgesamt 308 Tests ohne Fehler/übersprungene Tests. Die ursprüngliche Uploadstrategie dieses historischen Laufs blieb unverändert. Das bestätigte kurze Geräte-/Bestandstest-Ergebnis vom 10.10.2026 um 05:52 Europe/Berlin und die genauen Grenzen stehen in [WORK_HANDOFF](WORK_HANDOFF.md). PR #45 am unveränderten Produktkopf gemergt als `9f24a48845367502e585c87b1eb8e63f370b3dfb`; keine weitere PR-45-Wiederholung erforderlich. Für künftige Läufe gilt die oben beschriebene speicherschonende CI; benötigte neue Test-ZIPs ausschließlich gezielt.

Am 08.10.2026 wurden die acht ausdrücklich behaltenen Artefakte einzeln bestätigt: insgesamt **169.898.027 Bytes**. Der bereits abgeschlossene Cleanup löschte 153 freigegebene Artefakte mit 67.164.219 Bytes. Keine weitere Löschfreigabe. Die GitHub-Quota ist nicht aus diesen Bytezahlen ableitbar; Neuberechnung kann verzögert erfolgen. Keine Artefakte, Releases, Branches, Tags oder andere GitHub-Ressourcen endgültig löschen.
