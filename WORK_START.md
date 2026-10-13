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

Basis des Matrix-Meilensteins: main nach PR #68, `6190efa38762749a165922820b75ef61b998135f`. Das ist ein Ausgangsbeleg, kein dauerhaft aktueller HEAD; maßgeblich bleibt der live geprüfte main.

## Exakt nächster Schritt

Die **vollständigen unveränderlichen EF-Snapshots für 0504 und 0505 einschließlich unbenutzter Dateien** als begrenzten Implementierungs-PR vorbereiten. Die Pflichtdateien-/Ausnahme-/Signaturmatrix ist erarbeitet; der [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) beschreibt die konkreten Byte-/Leer-/Fehlerlücken und Abnahme. Keine Rekonstruktion alter Quellen, neuen Signatur-/Schreib-/Vertrauensbefehle oder UI-Umbau in diesem Paket. Eigene volle CI und erforderliche Gerätebestätigung vor Produktmerge.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
