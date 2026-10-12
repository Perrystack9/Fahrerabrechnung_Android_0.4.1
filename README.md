# FahrerTagebuch – Android 0.4.1

Lokale Android-App auf Basis des bestehenden Fahrerkarte-Imports: eigene Kartendaten lesen und speichern, Tätigkeiten und belegte Fahrzeug-/GNSS-Quellen darstellen, genaue Herkunft erhalten.

**Verbindlich seit 12.10.2026:** [neue Produktentscheidung](docs/PRODUCT_DECISION_20261012.md). Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort, Lohn/Zuschläge und gesamte Abrechnungslogik entfallen vollständig. Ebenso PDF-Abrechnungs-/Lohnexport; neutraler Bericht nicht verbindlich geplant. Tätigkeiten und neutrale zeitliche Auswertung bleiben erhalten.

## Bestätigte technische Basis

**40122 / Schema 19**, PR65 nach vollständig grüner CI und Tobis Gerätebestätigung unverändert gemergt. Beide Anwendungen derselben Karte werden unabhängig gelesen und als exakte eigene Anwendungs-/Sitzungsquellen atomar gespeichert; gemeinsame MF-Belege einmal am Parent. Gen2V2 ist kein dritter AID-Zweig. Historische Imports bleiben exakt auswählbar, ohne jüngere Ergänzungen.

Tobi: „Version 40122 installiert, alte Daten erhalten, sichtbare Gen1-/Gen2-Zustände, passende Details und Wiederöffnung ohne Leser erfolgreich“. [Originalbestätigung und genaue Grenzen](docs/DEVICE_CONFIRMATION_40122.md).

Vorhanden:

- USB-CCID/APDU, Android-USB-Freigabe, ATR-/Slot-/Readerdiagnose und Kartenerkennung;
- eigene Profil-/Kartenbindung mit Folge-/Ersatzkartenregeln;
- unabhängige Originalquellen, SQLCipher/Keystore, additive Migrationen, exakte Historie;
- Aktivitäts-/Fahrzeugquellen, Tagesprojektion und neutrale Quellzeit-Zusammenfassungen;
- Kontroll-/Ereignis-/Störungs-/Sonderbedingungsbelege und vorhandene Timeline-Marker;
- GNSS-/Orts-/Grenz-/Lade-/VU-/Metadaten-/Zertifikats-/MF-Quellen gemäß [Abdeckungsmatrix](docs/CARD_SOURCE_COVERAGE.md).

Vollständige Kartenabdeckung, Kartensignaturen, DDD-/Standarddownload und produktive Vertrauenskette sind noch nicht abgeschlossen. Öffentliche Forschungsbelege verifizieren keine Benutzerquelle.

## Zukünftige Darstellung und aktueller Altbestand

Fahren, andere Arbeit, Bereitschaft und Pause/Ruhe dürfen angezeigt, zeitlich zusammengefasst und neutral ausgewertet werden. Keine bezahlten, lohnrelevanten oder abrechnungsrelevanten Stunden.

Eine spätere GNSS-Karte darf echte sichtbare Punkte und einen klar approximierten chronologischen Verlauf zeigen. Keine tatsächlich gefahrene Straßenroute, Zwischenmesspunkte oder Kilometer aus Linien erfinden. Keine Smartphone-Routenaufzeichnung.

Oberfläche, Navigation, Einrichtung und Onboarding später grundsätzlich neu konzipieren. Abrechnung entfällt als Zielbereich; Ersatz offen. Fünf feste Tabs, sieben/acht Schritte, Heimatpunkt und Gehalts-/Spesenformulare sind keine künftigen Vorgaben.

**Jetzt ausschließlich Dokumentation.** Bestehende App-Navigation/Einrichtung samt Heimatpunkt-/Standort-/Konfigurationsartefakten bleibt technischer Altbestand. Kein großer UI-Umbau und kein Rückbau des Imports in diesem Schritt.

## Architektur und Verifikation

Module :app, :core, :data, :domain, :feature und :report bleiben unverändert. Vorbereitete Report-Schnittstelle und alte Export-Designassets sind kein geplanter PDF-Abrechnungsrenderer.

JDK 17, Android SDK Platform 37, Build Tools 36.0.0, offizieller Gradle Wrapper 9.6.1. Vollständige Android-CI:

~~~bash
./gradlew help
./gradlew build --dry-run
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleInternal
./gradlew :app:assembleDeviceTest
~~~

Dazu Schemaexport-, APK-/Signatur-/Updateprüfung und vollständige Room/SQLCipher/Compose-Instrumentierung auf API 36. [Bestätigte vollständige Main-CI](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38281475535) erfolgreich. Erfolgreiche normale CI lädt keine APK-/Reportartefakte hoch; [CI-Regeln](docs/CI_ARTIFACT_POLICY.md) bleiben gültig.

## Einstieg für Work

1. [Produktentscheidung](docs/PRODUCT_DECISION_20261012.md).
2. [Aktuelle Regeln](docs/CURRENT_RULES.md).
3. [Work-Handoff](docs/WORK_HANDOFF.md) und [Status](WORK_STATUS.md).
4. [Nächste Importpunkte](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md).
5. [Dokumentationsabgleich](docs/PRODUCT_DOCUMENTATION_AUDIT_20261012.md).

Alte Hand-offs dürfen gestrichene Ziele nicht wieder einführen. [Historischer README-Stand](docs/history/README_BEFORE_PRODUCT_DECISION_20261012.md) bleibt erhalten.
