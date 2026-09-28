> **Historischer Zwischenbericht vom 22.09.2026.** Die folgenden Blockaden und Phasengrenzen beschreiben nur den damaligen Zustand, nicht den heutigen `main`-Stand. Aktuell: [README](README.md), [WORK_STATUS](WORK_STATUS.md), [Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Build, Tests, Lint, APK-Verifikation und Emulator-Tests waren im [CI-Lauf zu PR #7](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36411408668) grün.

# GitHub Actions Handoff – Android-Basis 0.4.1

Stand: 22.09.2026

## Zweck

Dieser Stand dient ausschließlich der echten Build-Verifikation der bereits freigegebenen Android-Basis 0.4.1. Keine Fachphase darf hier beginnen.

## Bereits vorbereitet

- Hilt bleibt auf 2.60.1.
- Keine sonstige Gradle-Dependency wurde geändert.
- `.github/workflows/android-ci.yml` ist vorbereitet.
- CI verwendet JDK 17.
- CI installiert Android SDK Platform 37 und Build Tools 36.0.0.
- CI installiert Gradle 9.6.1 über `gradle/actions/setup-gradle`.
- CI erzeugt den offiziellen Gradle-9.6.1-Wrapper in einer isolierten Bootstrap-Gradle-Umgebung, kopiert `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` und `gradle-wrapper.properties` in das Projekt und verifiziert den Wrapper-JAR-SHA-256.
- CI trägt den veröffentlichten SHA-256 der Gradle-9.6.1-Binärdistribution in `gradle-wrapper.properties` ein.
- CI committet den offiziellen Wrapper zurück in den Projektbranch, wenn er sich geändert hat.
- Danach werden alle Pflichtbefehle ausgeführt: `help`, `build --dry-run`, `test`, `lint`, `assembleDebug`, `assembleInternal`.
- Debug- und Internal-APK werden als Actions-Artefakte hochgeladen, sobald sie existieren.
- Nach grünem Build läuft ein Emulator-Job für die vorbereiteten Room-/SQLCipher- und Compose-Instrumentationstests.
- Der SQLCipher-Test prüft Wiederöffnung mit richtigem Schlüssel, Scheitern mit falschem Schlüssel und fehlenden Klartext-SQLite-Header.

## Verifizierte Toolchain-Vorgaben

- AGP 9.4.0
- Gradle 9.6.1
- JDK 17
- compileSdk 37
- targetSdk 36
- Build Tools 36.0.0
- Gradle 9.6.1 Distribution SHA-256: `9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14`
- Gradle 9.6.1 Wrapper-JAR SHA-256: `497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`

## Damaliger Plattform-Blocker (22.09.2026)

Der verbundene GitHub-Connector kann bestehende Repositories beschreiben und verändern, stellt in der aktuellen Sitzung aber keine Repository-Erstellungsfunktion bereit. Für den verbundenen Nutzer `Perrystack9` ist derzeit kein Repository und keine GitHub-App-Installation verfügbar. Ein Schreibversuch gegen `Perrystack9/Fahrerabrechnung_Android_0.4.1` liefert deshalb 404.

Sobald ein leeres privates Repository mit diesem Namen existiert und für den GitHub-Connector zugänglich ist, kann der komplette Projektstand dorthin übertragen und der CI-Zyklus ohne weitere fachliche Freigabe gestartet werden.

## Damalige Phasengrenze

Nicht beginnen mit Fahrerkarte/Tachographenparser, USB-CCID, produktivem GPS, Gen2/Gen2V2, Matching, Reise/Heimkehr, Spesen, Übernachtungen, Zuschlagsberechnung oder Compliance.
