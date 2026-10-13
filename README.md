# FahrerTagebuch - Android 0.4.1

Lokale Android-App für eigene Fahrerkartendaten: zuverlässig importieren, unveränderlich speichern und Tätigkeiten, belegte Fahrzeuge sowie GNSS neutral darstellen.

**Work beginnt in [WORK_START.md](WORK_START.md).** Aktuelle [Produktregeln](docs/CURRENT_RULES.md), [Implementierung](WORK_STATUS.md) und [Übergabe](docs/WORK_HANDOFF.md) haben getrennte Aufgaben. Der [Dokumentenindex](docs/DOCUMENTATION_INDEX.md) erschließt Details und Historie nur bei Bedarf.

## Produktumfang

Fahrerkarte-Import und echte Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten bleiben Grundlage. Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung entfallen vollständig. PDF-Abrechnungs-/Lohnexport entfällt; neutraler Bericht ist nicht verbindlich geplant.

Spätere GNSS-Karte: echte Punkte und klar approximierter chronologischer Verlauf, ohne behauptete gefahrene Straßenroute. Oberfläche, Navigation und Onboarding später neu konzipieren; Zielrichtung etwa Start, Kalender, Auswertung, Einstellungen. Die vorhandene App enthält noch technische Altbestandteile der früheren Einrichtung/Navigation.

## Technik

Module `:app`, `:core`, `:data`, `:domain`, `:feature`, `:report`; USB-CCID/APDU, eigene Profil-/Kartenbindung, getrennte Kartenanwendungen, atomare SQLCipher-Speicherung, Keystore und genaue Quellenhistorie. Bestätigte Testbasis **40122 / Schema 19**. Vollständige Kartenabdeckung, Downloadsignaturen und produktives Kartenvertrauen sind noch offen.

Toolchain: JDK 17, Android SDK Platform 37, Build Tools 36.0.0, offizieller Gradle Wrapper 9.6.1. Volle Android-Verifikation:

```bash
./gradlew help
./gradlew build --dry-run
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleInternal
./gradlew :app:assembleDeviceTest
```

Dazu APK-/Signatur-/Updatepaar-/Schemaexportprüfung und vollständige Room/SQLCipher-/Compose-Instrumentierung auf API 36. [CI-/Artefaktpolitik](docs/CI_ARTIFACT_POLICY.md) und [Test-App-Signierung/Updates](ci/README.md) bei Bedarf lesen. Historische Designpakete und Reports sind keine aktuelle Startlektüre.
