# CI-Prüfungen und gezielte Testdateien

Stand: 08.10.2026. Infrastrukturvorgabe des Nutzers; keine Änderung an Produktregeln, App-Code, Kartenimport, Signatur oder Datenbankschema.

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

Die Infrastrukturarbeit basiert auf main `4b2c0b875d1912a91013bb23f0dbfe1596d0b3a2` und ändert PR #45 nicht. Produktkopf bleibt `42fb5dea0fb8f18d1b2206bda5d09b59b4ebe8b1`; Schema 11. Der bestehende Lauf `37137254348`, Versuch 3, scheiterte ausschließlich am Artefakt-Upload im Build-Job `111385798418`. Der abhängige Emulatorjob wurde übersprungen. Ein erneuter Jobversuch benutzt weiter den ursprünglichen Workflow; diese neue Speicherpolitik ändert den historischen Lauf nicht rückwirkend.

Nach verfügbarer Upload-Quota nur diesen Build-Job samt abhängigen Emulatorprüfungen wiederholen. Bei erneutem ausschließlich externem Quota-Fehler stoppen. PR #45 braucht vor Merge vollständige grüne CI und den kurzen vom Nutzer bestätigten Geräte-Update-/Bestandstest. Ohne reale Sonderbedingungen keine Prüfung solcher Belege behaupten; letzte bestätigte Hardwarebasis bleibt PR #41. Der Gerätetest von PR #44 wurde ausdrücklich übersprungen.

Am 08.10.2026 wurden die acht ausdrücklich behaltenen Artefakte einzeln bestätigt: insgesamt **169.898.027 Bytes**. Der bereits abgeschlossene Cleanup löschte 153 freigegebene Artefakte mit 67.164.219 Bytes. Keine weitere Löschfreigabe. Die GitHub-Quota ist nicht aus diesen Bytezahlen ableitbar; Neuberechnung kann verzögert erfolgen. Keine Artefakte, Releases, Branches, Tags oder andere GitHub-Ressourcen endgültig löschen.
