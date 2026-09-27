# Fahrerabrechnung & Fahrertagebuch – Android-Projektstart 0.4.1

**Aktuelle Produktregeln: [docs/CURRENT_RULES.md](docs/CURRENT_RULES.md). Smartphone/App GPS ist ausgeschlossen; GNSS-Punkte aus Fahrerkarte/Tachograph bleiben möglich. Ältere widersprechende Spezifikationen gelten insoweit nicht.**

Technische Startbasis gemäß dem verbindlichen Work-Auftrag **„Android-Projektstart 0.4.1“** und dem anschließenden Zwischenschritt **„Build-Verifikation und technische Basiskorrektur“**. Der Stand bildet absichtlich **keine produktive Fachlogik** für Fahrerkarte, USB/CCID, Gen2/Gen2V2, Matching, Reisen, Spesen, Übernachtungen, Zuschläge, Compliance, PDF oder Serverbackup ab.

## Architektur und Module

- `:app` – Application, Hilt-Einstieg, Navigation, Build-Varianten.
- `:core` – Design-System, gemeinsame UI-Zustände, Security-/Basis-Hilfen.
- `:data` – Room/SQLCipher, Keystore-Passphrase, DataStore, Repositories und Zukunftsschnittstellen.
- `:domain` – fachneutrale Grundtypen/Status aus 0.4.1.
- `:feature` – fünf Hauptbereiche plus vorbereitete Detailziele.
- `:report` – reine Platzhalter-Schnittstelle für spätere Berichtserzeugung.

Die fünf Hauptbereiche sind exakt: **Start, Kalender, Auswertung, Abrechnung, Einstellungen**.

## Build-Voraussetzungen

- JDK 17
- Android SDK Platform 37
- `compileSdk=37`, `targetSdk=36`, `minSdk=26`
- Android Gradle Plugin 9.4.0
- offizieller Gradle Wrapper 9.6.1

Der vollständige offizielle Gradle Wrapper 9.6.1 ist im Repository enthalten.

## Verifikationsbefehle

```bash
./gradlew help
./gradlew build --dry-run
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleInternal
```

Instrumentierte Tests (Room/SQLCipher/UI) benötigen ein Android-Gerät oder einen Emulator:

```bash
./gradlew connectedDebugAndroidTest
```

Der SQLCipher-Instrumentationstest prüft zusätzlich:

- Wiederöffnung mit dem korrekten Schlüssel,
- CRUD nach Wiederöffnung,
- keinen Klartext-SQLite-Header,
- Nichtlesbarkeit mit einem falschen Schlüssel.

## Datenschutz / Local-first

Die technische Basis ist local-first. Die Room-Datenbank wird ausschließlich über SQLCipher geöffnet. Die zufällige Datenbank-Passphrase wird nicht im Klartext persistiert, sondern per Android Keystore (AES/GCM) geschützt und erst zur Laufzeit entschlüsselt. Es gibt **keinen unverschlüsselten Fallback** und kein `fallbackToDestructiveMigration()`.

DataStore enthält nur technische Komfortwerte wie Theme-/Onboarding-/Debug-Einstellungen, keine fachliche Abrechnungskonfiguration.

## Logging

`SafeLogger` ist die zentrale Logging-Grenze. Sensible Rohwerte (vollständige VIN, Karten-ID, Name, Positionskoordinaten aus Karte/Tachograph, APDU/Kartenbinärdaten) dürfen dort nicht ausgegeben werden.

## Aktuelle Implementierungsgrenze

Vorbereitet, aber absichtlich ohne produktive Implementierung:

- `CardReaderTransport`
- `VinDecoder`
- `BackupProvider`
- Berichts-/PDF-Gateway
- Detailnavigation für Tagesreport, Kartenimport, Fahrzeugdetail und Einstellungs-Unterseiten

Die Werteplatzhalter in den fünf Grundscreens sind **reine UI-Vorschauen** und werden nicht als reale Berechnung ausgegeben.

## Spezifikationsbezug

Quelle: „Fahrerabrechnung & Fahrertagebuch 0.4.1 – Gesamtpaket für ChatGPT Work“, Stand 22.09.2026. Bei Widersprüchen gelten die späteren finalen 0.4.1-Festlegungen. Dieser Projektstand bleibt am vorgeschriebenen Zwischenstopp; eine Fahrerkarte-/USB-/Parser-Phase ist nicht begonnen.
