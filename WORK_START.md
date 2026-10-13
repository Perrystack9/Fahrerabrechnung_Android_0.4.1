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

**40123 / Room-Schema 19:** automatischer eigener Mehr-Anwendungs-Import einschließlich vollständiger unveränderlicher 0504-/0505-Originale. PR #70 gemergt (`e783ad364b1692d8633c5e4b8846112adb256be6`), Produktkopf `659f31c510752e5a06bf5f45bf2c93f4aff37031` mit voller CI und eigener Gerätebestätigung vom 13.10.2026, 09:04 Europe/Berlin. GEN1 13780/6202 Bytes, GEN2V2 13780/9602 Bytes; Kernfälle bestätigt, optionale Wiederholungs-/Abbruchfälle nicht separat bestätigt. Exakte Historie, Normalisierung, gemeinsame MF-Belege und atomare SQLCipher-Speicherung erhalten. [Geräteumfang](docs/DEVICE_CONFIRMATION_40123.md) nur bei konkretem Bedarf öffnen.

Keine vollständige Kartenabdeckung, echte Downloadsignaturen, produktive Vertrauenskette oder DDD-/Standarddownload behaupten. Neue relevante Produktänderungen benötigen ihre eigene CI und erforderliche Gerätebestätigung. Laufende CI und offene PRs immer aktuell abfragen; frühere grüne Läufe geben spätere Änderungen nicht frei.

Geprüfte Planbasis: main `632e0dfa020adb1d190d76b9591b046c0332f58c` nach Dokumentations-PR #71. Produkt-Main-CI 38360671532 und Docs-Main-CI 38361629250 erfolgreich. Das sind Ausgangsbelege, kein dauerhaft aktueller HEAD; maßgeblich bleibt der live geprüfte main.

## Exakt nächster Schritt

**40124 / Schema 20 ist im Produkt-PR [#73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73) umgesetzt, noch nicht freigegeben.** Aktuellen PR-Kopf, volle CI und offenen Geräteumfang live prüfen; Umsetzung nicht nochmals beginnen. Echter Room-20-Compilerexport übernommen. Erst endgültigen vollen Lauf inklusive API36 abschließen, dann gezieltes Paket desselben Kopfs und [eigene Geräteabnahme](docs/DEVICE_TEST_40124.md). Vor bestätigtem Gerätegate kein Merge. Signaturen heißen aufgenommen/ungeprüft; keine Vertrauens-/DDD-/Schreib-/UI-Folgephase beginnen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
