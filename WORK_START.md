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

**40124 / Room-Schema 20:** automatischer eigener Mehr-Anwendungs-Import mit vollständigen 0504-/0505-Originalen und echten neuen Kartensignaturen in GEN1/GEN2(V2). PR #73 gemergt (`3298b707525a4f33162c835ce31deaf181d2e772`), Produktkopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`, identischer vollständiger Tree `3540b26b775d17c9bdc6335549dafbfbd58bcd92`. Eigene volle CI 38381962283 und exaktes Gerätepaket 38388192092 erfolgreich. Tobi bestätigt 13.10.2026, 13:57 Europe/Berlin sämtliche Checklistenpunkte einschließlich Wiederholung und Kartenentnahme/Wiederaufnahme. GEN1-Originale 13780/6202 und Signaturen 128/128 Bytes; GEN2V2-Originale 13780/9602 und Signaturen 64/64 Bytes, alle ungeprüft. [Genauer Gerätebeleg](docs/DEVICE_CONFIRMATION_40124.md) nur bei Bedarf öffnen.

Exakte Historie/Fingerprints, Normalisierung, gemeinsame MF-Belege und atomare SQLCipher-Speicherung erhalten. Additive 19→20, keine nachträglichen Signaturen für alte Imports. Keine vollständige Kartenabdeckung, mathematische Signaturprüfung, produktive Vertrauenskette oder DDD-/Standarddownload behaupten. Neue relevante Produktänderungen brauchen eigene CI/Gerätebestätigung.

Zusätzliche Produkt-Main-CI [38394108412](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38394108412) für Merge 3298b70 ist vollständig erfolgreich; Docs-Main-CI 38395123764 für main nach PR #74 ebenfalls erfolgreich. Laufende CI/offene PRs immer aktuell abfragen; vergangene grüne Läufe geben neue Änderungen nicht frei.

## Exakt nächster Schritt

**40124 ist abgeschlossen; der nächste begrenzte Plan ist fertig.** Aktuellen main/PR/CI live prüfen; danach den [Identifikations-Signaturplan](docs/CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md) öffnen und als eigenen Produkt-Meilenstein **40125 / Schema 21** umsetzen: 0501/0520 in beiden Anwendungen und 0525 nur auf V2, neue vollständige Leseaufnahme nach HASH, Bootstrap-Konsistenz und additive 20→21. Zuerst Modelle/Befehls-/Konsistenztests, danach atomare Speicherung/Migration/kleine Abnahmeanzeige. Eigene volle CI und [neue Geräteabnahme](docs/DEVICE_TEST_40125.md) vor Merge. Keine alte Gerätebestätigung übertragen; keine Trust-/DDD-/Schreib-/UI-Folgephase beginnen. Dieser Dokumentations-Meilenstein endet nach Sicherung; Umsetzung bleibt die nächste getrennte Arbeit.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
