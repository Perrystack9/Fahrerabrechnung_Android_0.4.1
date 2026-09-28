> **Historischer Zwischenbericht vom 22.09.2026.** Die folgenden Blockaden und Phasengrenzen beschreiben nur den damaligen Zustand, nicht den heutigen `main`-Stand. Aktuell: [README](README.md), [WORK_STATUS](WORK_STATUS.md), [Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Build, Tests, Lint, APK-Verifikation und Emulator-Tests waren im [CI-Lauf zu PR #7](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36411408668) grün.

# Zwischenbericht – Android-Projektstart 0.4.1

Stand: 22.09.2026

## Status

Der **Quell-/Projektstand des technischen Projektstarts** ist umgesetzt und an der im Auftrag festgelegten Fachgrenze gestoppt. Die formalen Abnahmekriterien „kompiliert“, „Debug-/Internal-APK erzeugt“ und „Tests grün“ können in der aktuellen isolierten Ausführungsumgebung **nicht als erfüllt behauptet werden**, weil hier Android SDK, Gradle-Binär-/Dependency-Cache und direkter Maven-/Netzzugang fehlen. Insbesondere konnte `gradle-wrapper.jar` nicht bezogen werden.

Daher wurde **keine APK erfunden oder als erfolgreich ausgegeben**. Die verpflichtenden Buildbefehle wurden angestoßen; sie stoppen bereits vor Gradle-Start am fehlenden Wrapper-Binary. Die Logs liegen unter `build-logs/`.

## 1. Umgesetzte Projektstruktur

- `:app` – Android Application, Hilt Entry, Navigation, `debug/internal/release`
- `:core` – Material-3-Designsystem, gemeinsame UI-Zustände, SafeLogger
- `:data` – Room, SQLCipher, Keystore-Passphrase, DataStore, Repositories, NoOp-Schnittstellen
- `:domain` – Status-/Grundtypen, Money, Zeitkontext
- `:feature` – Start, Kalender, Auswertung, Abrechnung, Einstellungen sowie vorbereitete Detailziele
- `:report` – nur Report-Gateway-Platzhalter

Genutzte Packages folgen dem Auftrag (`de.fahrerabrechnung.*`).

## 2. Verwendete Dependency-/Tool-Versionen

| Bereich | Version |
|---|---:|
| Android Gradle Plugin | 9.4.0 |
| Gradle Distribution | 9.6.1 |
| Kotlin | 2.4.20 |
| KSP | 2.3.10 |
| Compose UI | 1.12.1 |
| Material 3 | 1.4.0 |
| Activity Compose | 1.13.0 |
| Lifecycle | 2.11.0 |
| Navigation Compose | 2.10.1 |
| Hilt | 2.60.1 |
| Room | 2.8.5 |
| SQLCipher Android | 4.18.0 |
| DataStore | 1.2.1 |
| kotlinx Serialization | 1.9.0 |
| Coroutines | 1.10.2 |
| JDK Ziel | 17 |
| min/compile/target SDK | 26 / 37 / 36 |

AGP 9.x hat standardmäßig Built-in Kotlin. Weil die 0.4.1-Stackvorgabe Kotlin 2.4.20 explizit festlegt, ist für diesen Projektstand `android.builtInKotlin=false` + `android.newDsl=false` gesetzt und der Kotlin-Pluginstand explizit gepinnt. Das ist eine dokumentierte technische Integrationsentscheidung, keine Fachänderung.

## 3. Datenbank- und Verschlüsselungsaufbau

- Room-Schema `version = 1`, `exportSchema = true`.
- Room Gradle Plugin schreibt nach `data/schemas` (Schema-Datei entsteht beim ersten erfolgreichen Build).
- Basistabellen: `ProfileEntity`, `ConfigurationVersionEntity`, `DriverCardEntity`, `VehicleEntity`, `ImportSessionEntity`.
- Foreign Keys/Indices für Profilbezüge.
- Kein `fallbackToDestructiveMigration()`.
- SQLCipher über `net.zetetic.database.sqlcipher.SupportOpenHelperFactory`.
- `System.loadLibrary("sqlcipher")` vor DB-Erstellung.
- Zufällige 32-Byte-DB-Passphrase.
- Passphrase wird AES/GCM-verschlüsselt persistiert; AES-Schlüssel liegt im Android Keystore.
- Kein unverschlüsselter DB-Fallback.
- Instrumentierter Test prüft CRUD, Wiederöffnung mit korrektem Schlüssel, dass der DB-Dateikopf **nicht** mit `SQLite format 3` beginnt und dass ein falscher Schlüssel keinen lesbaren Zugriff ermöglicht.

## 4. Tests und Build-Ergebnisse

### Ausführbar in dieser Umgebung

- Statischer Scope-/Boundary-Check: **PASS** (`scripts/verify_project_boundary.sh`).
- Reiner Domain-Kotlin-Syntaxcompile mit dem lokal verfügbaren `kotlinc 1.9.0`: **PASS**. Das ist nur ein zusätzlicher Syntaxcheck und ersetzt den Ziel-Build mit Kotlin 2.4.20 nicht.

### Vorbereitet

- Unit: Status/Enums, Money, Zeitformatierung.
- Room instrumentiert: verschlüsselte DB öffnet, CRUD, Plaintext-Header ausgeschlossen.
- Room MigrationTestHelper-Basis (v1→v2 absichtlich `@Ignore`, bis v2 existiert).
- Compose UI: fünf Navigationseinträge, Top-Level-Wechsel, Dark Theme ohne Crash.

### Verbindliche Befehle – in dieser Umgebung blockiert

- `./gradlew help` – **BLOCKIERT**
- `./gradlew build --dry-run` – **BLOCKIERT**
- `./gradlew test` – **BLOCKIERT**
- `./gradlew lint` – **BLOCKIERT**
- `./gradlew assembleDebug` – **BLOCKIERT**
- `./gradlew assembleInternal` – **BLOCKIERT**

Grund: `gradle-wrapper.jar` konnte wegen fehlendem direkten Binär-/Mavenzugang nicht heruntergeladen werden; Android SDK ist ebenfalls nicht installiert. Siehe `build-logs/*.log` und `BUILD_STATUS.md`.

## 5. Bekannte offene Punkte / Abweichungen

1. Das frühere Ersatz-`gradlew` wurde durch die offiziellen Gradle-9.6.1-Skripte ersetzt; `gradle-wrapper.jar` fehlt im Archiv weiterhin. `gradle-wrapper.properties` inklusive 9.6.1-Distribution und SHA-256 ist vorhanden. Auf einer Buildmaschine mit Gradle 9.6.1 den offiziellen Wrapper vollständig erzeugen beziehungsweise das offizielle JAR ergänzen und dessen SHA-256 verifizieren.
2. Android SDK Platform 37 / Build Tools müssen lokal installiert sein.
3. Erst danach können KSP/Room die reale Schema-JSON exportieren und die vollständigen Gradle-Tests laufen.
4. Instrumentierte Room-/UI-Tests benötigen Emulator oder Gerät.
5. Keine echten App-Screenshots vorhanden, da kein Android-Runtime/Emulator verfügbar war. Es werden keine Screenshots simuliert und als echte App-Ausgabe ausgegeben.
6. Debug-/Internal-APK wurden daher **nicht erzeugt**.

## 6. APK-Pfad

**Kein APK-Pfad vorhanden.** APK-Erzeugung ist in dieser Umgebung nicht verifiziert und wird nicht vorgetäuscht.

Nach erfolgreichem Build wären die üblichen Gradle-Ausgaben unter `app/build/outputs/apk/debug/` und `app/build/outputs/apk/internal/` zu erwarten; dies ist hier ausdrücklich **kein Nachweis**, dass diese Dateien bereits existieren.

## 7. Relevante Architekturentscheidungen

- Local-first; kein Cloudzwang, Firebase Analytics oder automatisches Crash-Daten-SDK.
- Genau fünf Bottom-Navigation-Ziele: Start, Kalender, Auswertung, Abrechnung, Einstellungen.
- Tagesreport nur Detailnavigation, kein sechster Tab.
- Light/Dark Material-3-Tokens aus 0.4.1, Dynamic Color v1 aus.
- `ViewModel + StateFlow` wird für Theme-Modus genutzt; DataStore speichert nur technische Komfortwerte.
- `ResultValue` trennt `VOLLSTAENDIG / TEILWEISE / UNKLAR / KEINE_DATEN / NICHT_ANWENDBAR`, statt „unbekannt“ als fachliches `null` oder 0 zu behandeln.
- Money nutzt `BigDecimal` und expliziten Währungscode; keine `Double`-Geldwerte.
- Berichtskontext `Europe/Berlin` als Grundtyp vorbereitet.
- Internal-Build ist nicht debuggable; er nutzt Release-Fallbacks der Library-Module und eine lokale Debug-Signatur nur für interne Installierbarkeit. Release-Signierung ist optional über nicht eingecheckte `keystore.properties` vorbereitet.

## 8. Bewusst nur Interface / Placeholder

- `CardReaderTransport` + `NoOpCardReaderTransport`
- `LocationSource` + `NoOpLocationSource`
- `VinDecoder` + `NoOpVinDecoder`
- `BackupProvider` + `NoOpBackupProvider`
- `ReportGateway` + `NoOpReportGateway`
- Fahrerkarte-einlesen-Detailscreen: nur Hinweis/Navigation
- Fahrzeugdetail: nur Hinweis/Navigation
- Einstellungs-Unterseiten: nur Navigation/Placeholder
- Tagesreport: UI-Struktur ohne produktive Fachberechnung
- Start/Analytics/Payroll: Mock-/Leerzustände, keine scheinbar echten Ergebnisse

**Nicht begonnen** wurden Fahrerkarte-/Tachographenparser, USB-CCID, produktives GPS, Gen2/Gen2V2-Matching, Reise/Heimkehr, Spesen, Übernachtungen, Zuschlagsberechnung, Compliance, PDF-Erzeugung und Serverbackup.

## Zwischenstopp

Hier wird die Implementierung gestoppt. Es wird **keine nächste Fachphase** begonnen. Formal kann der Auftrag erst nach erfolgreicher Build-/APK-/Testverifikation auf einer Android-Buildumgebung als vollständig abgeschlossen markiert werden.
