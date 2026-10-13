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

**40125 / Room-Schema 21 übernommen und im eigenen tatsächlich getesteten Umfang bestätigt.** [PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76), Merge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`; identischer vollständiger Produktbaum `36e014af6cbffa9add766bdd917c6d94313f87ec` zum CI-/Gerätekopf `20d34a0fc7ce370fb4c1d22002905a500574ee06`. Volle CI 38408152663 und exaktes Paket 38414059875 erfolgreich. Tobi bestätigt am 13.10.2026 17:32 Europe/Berlin: „Wurde getestet, und funktioniert 1a“. Einzelmesswerte/Modellnamen wurden diesmal nicht separat protokolliert. [Gerätebeleg](docs/DEVICE_CONFIRMATION_40125.md) nur bei Bedarf.

Neue 0501/0520 in beiden Anwendungen und 0525 nur V2 neben erhaltenen vier Core-Belegen: frische vollständige post-HASH-READs, strikte Dateigrenze, Bootstrap-Konsistenz mit Gruppenabbruch, eigene Child-Receipts und additive 20→21. Alte Originale/Fingerprints/Imports und 19→20-Abnahme erhalten; kein Backfill oder jüngerer Ersatz. Kleine maskierte Detailanzeige, weiterhin ungeprüft. Kein Trust/DDD/Schreib-/UI-Folgeumfang dadurch freigegeben.

## Exakt nächster Schritt

Dieser Produkt-Meilenstein ist abgeschlossen. Aktuellen main/PR/CI live prüfen; zusätzliche Produkt-Main-CI 38423233063 und Dokumentationschecks anhand aktueller Ergebnisse bewerten. Umsetzung nicht wiederholen. Im ursprünglichen Checkout liegt eine lokale angefangene Änderung an `SettingsScreen.kt`, die erhalten und nicht übernommen wurde; vorhandene Arbeit vor weiterer Integration prüfen/sichern. [Übergabe](docs/WORK_HANDOFF.md) nennt Checkout und Grenzen. Nur einen ausdrücklich ausgewählten Folgeauftrag planen; keine automatische weitere EF-/Trust-/DDD-/Schreib-/UI-Phase beginnen. **Übergabe vorbereiten und Sitzung abschließen.**

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
