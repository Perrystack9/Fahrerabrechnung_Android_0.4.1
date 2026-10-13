# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für den nächsten Produkt-Schritt genau den [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) öffnen. Weitere Fachtexte erst bei konkretem Bedarf.

Das sind **fünf Startdateien einschließlich WORK_START.md**. README, Dokumentenindex, Designpakete, Geräteprotokolle, Checkpoints und Archive gehören nicht zur Pflichtlektüre. Keine pauschale Volltextlektüre aller Markdown-Dateien und keine Rekonstruktion alter Chats, wenn das Repository ausreicht.

## Aktuelle Produktgrenzen

- Bestehenden Fahrerkarte-Import vollständig erhalten; Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten dürfen neutral angezeigt und ausgewertet werden.
- Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort samt Einrichtung, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung sind vollständig gestrichen.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant.
- GNSS bleibt: spätere Karte mit echten Punkten und gekennzeichnetem grobem chronologischem Verlauf; keine behauptete gefahrene Straßenroute oder Kilometer aus Verbindungslinien.
- Oberfläche, Navigation und Onboarding später neu konzipieren. Zielrichtung: etwa vier Bereiche **Start, Kalender, Auswertung, Einstellungen**. Konkretes Layout offen; kein UI-Umbau in diesem Cleanup.

## Nachweislich fertig und noch gesperrt

**40122 / Room-Schema 19:** automatischer Vordergrund-Import, eigene Kartenbindung, getrennte Gen1-/Gen2-Anwendungen, gemeinsame MF-Belege und atomare SQLCipher-Speicherung; exakte Quellen-/Historienwahl. PR #65 vollständig geprüft und im ausdrücklich bestätigten Geräteumfang abgeschlossen. PR #67 ist gemergt und verankert den schlankeren Produktumfang.

Keine vollständige Kartenabdeckung, echte Downloadsignaturen, produktive Vertrauenskette oder DDD-/Standarddownload behaupten. Neue relevante Produktänderungen benötigen ihre eigene CI und erforderliche Gerätebestätigung. Laufende CI und offene PRs immer aktuell abfragen; frühere grüne Läufe geben spätere Änderungen nicht frei.

Basis des Snapshot-PRs: main nach gemergtem PR #69, `5b1b9669af8614ec502d027a4c71d94ab81496ad`. Das ist ein Ausgangsbeleg, kein dauerhaft aktueller HEAD; maßgeblich bleibt der live geprüfte main.

## Exakt nächster Schritt

Den **vollständig CI-geprüften Snapshot-PR #70** für die Geräteabnahme vorbereiten. Grüner Produktkopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`, [CI 38335263243, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2); 40123 / Schema 19 noch nicht am Gerät bestätigt. Gezieltes Gerätepaket mit bestehendem `device-test.yml`, Workflow-Branch main, diesem `source_sha` und `include_baseline=false` bauen; aktueller GitHub-Zugriff kann den Dispatch nicht auslösen. Danach Paket prüfen/bereitstellen und eigene Gerätebestätigung einholen. **Kein Produktmerge vor dieser Abnahme.** Konkreter Arbeitszustand und Paketparameter in [Übergabe](docs/WORK_HANDOFF.md) und [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md). Der separate CI-Dokumentationsbranch verändert den grünen Produktkopf nicht.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
