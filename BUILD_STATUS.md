> **Produktentscheidung 12.10.2026 – überholte Produkt-/UI-/PDF-Planung.** Historische Referenz, kein aktueller Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung entfallen vollständig; neutraler Bericht nicht verbindlich geplant. Alte Tabanzahl, Navigationsplätze und sieben/acht Schritte sind keine Ziele. Ganze Oberfläche und Einrichtung später neu konzipieren, jetzt keine UI-Umsetzung aus diesem Paket. Historische Tests/Beobachtungen nicht umgeschrieben. Verbindlich ist die [neue Produktentscheidung](docs/PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

Aktueller Checkpoint 11.10.2026: Tobi bestätigt um 08:36 Europe/Berlin allgemein „Funktioniert alles“ zum ausgegebenen PR58-Testupdate 40117/Schema 18 und beauftragt zwei Stunden selbstständige Weiterarbeit bis 10:36:13. Alle vier Jobs der exakten CI-Köpfe PR54–58 erneut erfolgreich geprüft. PR58 unverändert gemergt als 1b592d627d5d18db9fc909f5c8cf2c796fd8578b; Merge-Tree 2e314a47211f06696a6b05287e40b63575b921df bytegleich zum getesteten Kopf ba9656f0e22980af94883783986367d7bcdf545b. GitHub hat die enthaltenen PR54–57 ebenfalls abgeschlossen. Keine neuen Einzelmesswerte oder nichtleeren Quellenfälle behauptet.

Neuer begrenzter Zertifikatsentwurf PR59: vier unabhängige Zertifikatsarten der gewählten Kartenanwendung (Gen1 zwei), vollständige begrenzte EF-Lesung, struktureller Decoder, verschlüsselte exakte Karten-/Sitzungsquelle und Detailansicht mit Referenzen auf Antippen. Kopf c535c01a838fb9f565b49b7a9b50030d06e77bf7, 54 zusätzliche Testmethoden. Alle vier Jobs der vollständigen CI 38119878793 erfolgreich, einschließlich SQLCipher-/Room- und Compose-Instrumentierung. Der Zertifikatskopf bleibt unverändert. [Umfang und Primärformat](docs/CARD_CERTIFICATE_IMPORT.md).

Separater Folgeentwurf ergänzt die vollständigen bereits gelesenen Originaldateien für Anwendung, Karten-/Inhaberidentifikation und V2-Anwendung, einschließlich unabhängiger Dateiendproben und der Ansicht „Kartenangaben“. 41 weitere Testmethoden vorbereitet. Die erste Folge-CI 38121099485 fand zwei falsch typisierte APDU-Testvergleiche; die Vergleiche sind auf kodierte Bytes korrigiert, neue vollständige CI am Korrekturkopf erforderlich. Aktuelle Testversion 40119, Schema 18 und stabile Testkennung/-signatur unverändert. Neue Gen2V2-Vorgänge können 26 unterstützte Quellen enthalten; ältere 19-/23-Quellen-Vorgänge behalten ihren exakten bisherigen Stand. Keine Vollständigkeits-/Kryptovertrauensbehauptung, keine neuen PSO-/Schreib-/Authentisierungsbefehle und keine Nachbefüllung. [Originalquellen und Prüfgrenzen](docs/CARD_ORIGINAL_IDENTIFICATION_IMPORT.md), [gebündelter Gerätetest](docs/CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md). Vollständige Android-/SQLCipher-/Compose-CI am endgültigen Kopf vor der Ausgabe erforderlich. Kein neuer Hardwaretest bestätigt; beide Importentwürfe bleiben bis zur Rückmeldung ungemergt.

Dieser Checkpoint ist maßgeblich; die älteren Abschnitte darunter dokumentieren ihre jeweiligen früheren Stände.

---

> **Historischer Zwischenbericht vom 22.09.2026.** Die folgenden Blockaden und Phasengrenzen beschreiben nur den damaligen Zustand, nicht den heutigen `main`-Stand. Aktuell: [README](README.md), [WORK_STATUS](WORK_STATUS.md), [Produktregeln](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3). Build, Tests, Lint, APK-Verifikation und Emulator-Tests waren im [CI-Lauf zu PR #7](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36411408668) grün.

# Build-Status – Zwischenschritt 0.4.1

Stand: 22.09.2026

## Ergebnis in dieser Ausführungsumgebung

Die technische Basiskorrektur wurde soweit ohne Android-Build ausführbar umgesetzt. Die geforderte vollständige Build-Verifikation ist **nicht erfolgreich abgeschlossen**, weil der aktuellen Sandbox drei zwingende Bestandteile fehlen:

1. JDK 17 – vorhanden ist ausschließlich OpenJDK 21.0.11.
2. Android SDK / Platform 37 – `ANDROID_HOME`/`ANDROID_SDK_ROOT` und `sdkmanager` sind nicht vorhanden.
3. Das offizielle binäre `gradle/wrapper/gradle-wrapper.jar` für Gradle 9.6.1 kann wegen fehlendem Container-Netz-/DNS-Zugriff nicht heruntergeladen oder durch `gradle wrapper` erzeugt werden; eine lokale Gradle-Installation ist ebenfalls nicht vorhanden.

Es wurde **kein** Ersatz-JAR und **kein** selbstgeschriebener Ersatz-Wrapper erzeugt.

## Wrapper-Stand

Vorhanden:

- offizielles Gradle-9.6.1-`gradlew`
- offizielles Gradle-9.6.1-`gradlew.bat`
- `gradle-wrapper.properties` mit `gradle-9.6.1-bin.zip`
- `distributionSha256Sum=9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14`

Nicht materialisierbar in dieser Sandbox:

- `gradle/wrapper/gradle-wrapper.jar`

Erwartete veröffentlichte SHA-256 für das Gradle-9.6.1-Wrapper-JAR:

`497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7`

## Ausgeführte Pflichtbefehle

Alle sechs geforderten Befehle wurden tatsächlich aufgerufen und protokolliert:

| Befehl | Ergebnis | Log |
|---|---|---|
| `./gradlew help` | BLOCKIERT, Exit 1 | `build-logs/verify-help.log` |
| `./gradlew build --dry-run` | BLOCKIERT, Exit 1 | `build-logs/verify-build-dry-run.log` |
| `./gradlew test` | BLOCKIERT, Exit 1 | `build-logs/verify-test.log` |
| `./gradlew lint` | BLOCKIERT, Exit 1 | `build-logs/verify-lint.log` |
| `./gradlew assembleDebug` | BLOCKIERT, Exit 1 | `build-logs/verify-assembleDebug.log` |
| `./gradlew assembleInternal` | BLOCKIERT, Exit 1 | `build-logs/verify-assembleInternal.log` |

Gemeinsamer unmittelbarer Fehler:

`Error: Unable to access jarfile .../gradle/wrapper/gradle-wrapper.jar`

Damit wurde Gradle selbst noch nicht gestartet; nachgelagerte Android-/Dependency-Probleme können in dieser Umgebung folglich noch nicht ausgeschlossen werden.

## Zusätzliche Prüfungen

- `scripts/verify_project_boundary.sh`: **PASS**
- Domain-Quellen mit lokalem `kotlinc 1.9.0` als zusätzlicher Syntaxcheck: **PASS** (`build-logs/domain-static-compile.log`)
- `gradlew` POSIX-Syntax (`sh -n`): **PASS**
- Dependency-Pin-Check: Hilt **2.60.1**; AGP/Kotlin/KSP/Room/SQLCipher/DataStore und übrige Versionen unverändert: **PASS**
- Wrapper-Properties auf Gradle 9.6.1 + Distribution-SHA geprüft: **PASS**

## Android-/Instrumentationstests

Keine Android-Laufzeitumgebung (Gerät/Emulator + SDK) ist verfügbar. Deshalb konnten Room-/SQLCipher- und Compose-Instrumentationstests nicht ausgeführt werden.

Der vorhandene SQLCipher-Test wurde innerhalb der freigegebenen technischen Basis erweitert und prüft beim nächsten instrumentierten Lauf:

- verschlüsselte DB mit korrektem Schlüssel erstellen,
- Datensatz schreiben,
- DB schließen und mit demselben Schlüssel wieder öffnen,
- Datensatz erneut lesen,
- Klartext-SQLite-Header ausschließen,
- Öffnung/Lesezugriff mit falschem Schlüssel muss mit SQLCipher-Fehler scheitern.

## APK

Da `assembleDebug` und `assembleInternal` nicht bis Gradle gelangen, wurden **keine APK-Dateien erzeugt**. Es werden keine Pfade erfunden oder erwartete Standardpfade als existierende Artefakte ausgegeben.
