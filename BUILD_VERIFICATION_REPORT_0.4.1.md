> **Historischer Zwischenbericht vom 22.09.2026.** Die folgenden Blockaden und Phasengrenzen beschreiben nur den damaligen Zustand, nicht den heutigen `main`-Stand. Aktuell: [README](README.md), [WORK_STATUS](WORK_STATUS.md), [Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Build, Tests, Lint, APK-Verifikation und Emulator-Tests waren im [CI-Lauf zu PR #7](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36411408668) grün.

# Zwischenbericht – Build-Verifikation und technische Basiskorrektur 0.4.1

Stand: 22.09.2026

## 1. Umfang und Stoppgrenze

Bearbeitet wurde ausschließlich der bereits freigegebene Android-Projektstart 0.4.1. Es wurde keine neue Fachfunktion begonnen. Fahrerkarte/Tachograph, USB-CCID, produktives GPS, Gen2/Gen2V2, Matching, Reise/Heimkehr, Spesen, Übernachtungen, Zuschläge und Compliance bleiben außerhalb dieses Schritts.

## 2. Änderungen

### Dependency

Einzige beabsichtigte Dependency-Änderung:

- Dagger/Hilt: **2.57.2 → 2.60.1**

Keine andere Dependency-Version wurde geändert.

### Gradle Wrapper

- Das vorherige selbstgeschriebene Minimal-`gradlew` wurde entfernt.
- `gradlew` wurde durch das offizielle Gradle-9.6.1-POSIX-Wrapper-Skript ersetzt.
- `gradlew.bat` wurde durch das offizielle Gradle-9.6.1-Windows-Wrapper-Skript ersetzt.
- `gradle-wrapper.properties` zeigt auf `gradle-9.6.1-bin.zip` und enthält die veröffentlichte Distribution-SHA-256.
- Das offizielle `gradle-wrapper.jar` konnte in der Sandbox nicht materialisiert werden. Es wurde bewusst **kein** selbst gebautes oder nachgeahmtes JAR als Ersatz erzeugt.

Damit ist die Forderung „Wrapper vollständig einschließlich JAR“ in dieser Ausführungsumgebung noch **offen** und nicht als erledigt markiert.

### SQLCipher-Verifikationstest

`EncryptedDatabaseInstrumentedTest` wurde erweitert. Er deckt nun explizit ab:

1. verschlüsselte DB mit zufälliger 32-Byte-Passphrase,
2. Basis-CRUD,
3. Schließen und Wiederöffnen mit korrekter Passphrase,
4. erneutes Lesen nach Wiederöffnung,
5. kein Klartext-Header `SQLite format 3`,
6. falsche zufällige Passphrase darf die Datenbank nicht lesbar machen und muss einen SQLCipher-Fehler auslösen.

Das ist ausschließlich Test-/Basiskorrektur; es wurde keine Fachlogik ergänzt.

### Dokumentation

README und Build-Status wurden auf diesen Verifikationsschritt aktualisiert.

## 3. Geforderte Toolchain

| Vorgabe | In Sandbox verfügbar | Ergebnis |
|---|---|---|
| JDK 17 | Nein; nur OpenJDK 21.0.11 | BLOCKIERT |
| Android SDK Platform 37 | Nein | BLOCKIERT |
| AGP 9.4.0 im Projekt | Ja, gepinnt | statisch bestätigt |
| Gradle 9.6.1 | Properties/Skripte vorhanden; Distribution/Wrapper-JAR lokal nicht verfügbar | BLOCKIERT |

Der Container besitzt keinen nutzbaren Gradle-/Android-Cache und keinen direkten DNS-/Binärdownloadzugriff. Deshalb können die fehlenden Toolchain-Bestandteile hier nicht nachinstalliert werden.

## 4. Pflicht-Builds und Tests

Tatsächlich ausgeführt:

| Kommando | Exit | Ergebnis |
|---|---:|---|
| `./gradlew help` | 1 | Wrapper-JAR fehlt |
| `./gradlew build --dry-run` | 1 | Wrapper-JAR fehlt |
| `./gradlew test` | 1 | Wrapper-JAR fehlt |
| `./gradlew lint` | 1 | Wrapper-JAR fehlt |
| `./gradlew assembleDebug` | 1 | Wrapper-JAR fehlt |
| `./gradlew assembleInternal` | 1 | Wrapper-JAR fehlt |

Logs: `build-logs/verify-*.log`.

Zusätzlich erfolgreich:

- technische Scope-/Boundary-Prüfung: PASS,
- Domain-Kotlin-Syntaxcompile: PASS,
- POSIX-Syntaxprüfung des offiziellen `gradlew`: PASS,
- statischer Versions-/Wrapper-Properties-Check: PASS.

Nicht ausführbar mangels Android Runtime/SDK:

- Room-/SQLCipher-Instrumentationstests,
- Compose-Instrumentationstests.

## 5. Datenbank / Verschlüsselung

Unverändert bleibt der freigegebene Aufbau:

- Room mit expliziter Schema-Version und Schemaexport,
- SQLCipher als einziger DB-OpenHelper,
- kein unverschlüsselter Fallback,
- zufällige DB-Passphrase,
- AES/GCM-Schutz der Passphrase,
- AES-Schlüssel im Android Keystore,
- kein `fallbackToDestructiveMigration()`.

Der erweiterte Instrumentationstest bildet die vom Nutzer geforderte richtige-/falsche-Schlüssel-Prüfung ab; die reale Laufzeitverifikation bleibt bis zu einer Android-Laufzeitumgebung offen.

## 6. APK-Ergebnis

**Keine Debug-APK und keine Internal-APK erzeugt.**

Da beide Assemble-Aufrufe bereits vor Gradle-Start am fehlenden Wrapper-JAR enden, existieren keine verifizierten APK-Pfade. Erwartete Standardpfade werden bewusst nicht als Ergebnis ausgegeben.

## 7. Verbleibende Warnungen / offene Punkte

1. Offizielles Gradle-9.6.1-`gradle-wrapper.jar` fehlt physisch noch und muss auf einer Umgebung mit Gradle 9.6.1 oder zugänglicher offizieller Distribution erzeugt/bezogen und gegen die veröffentlichte SHA-256 verifiziert werden.
2. Build muss mit **JDK 17** wiederholt werden; JDK 21 dieser Sandbox wurde nicht als Ersatz akzeptiert.
3. Android SDK Platform 37 muss verfügbar sein.
4. Erst danach können die sechs Gradle-Kommandos fachgerecht verifiziert werden.
5. Anschließend die vorhandenen SQLCipher-/Room- und Compose-Instrumentationstests auf Emulator/Gerät ausführen.
6. Erst bei grünen Builds die tatsächlichen Debug-/Internal-APK-Pfade dokumentieren.

## 8. Architektur-/Scope-Kontrolle

Die technische Basiskorrektur hat die Fachgrenze nicht überschritten. Die Zukunftsschnittstellen bleiben NoOp/Interfaces; es wurde keine Fahrerkarte-/USB-/GPS-/Matching-/Reise-/Spesen-/Übernachtungs-/Zuschlags-/Compliance-Implementierung begonnen.

## STOPP

Die weitere Implementierung bleibt gestoppt. Die Fahrerkarte-/USB-/Parser-Phase wird erst nach erneuter Prüfung/Freigabe durch ChatGPT/Tobi begonnen.
