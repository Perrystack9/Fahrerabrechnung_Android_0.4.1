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

## Nachweislich fertig und offene Grenzen

**40126 / Room-Schema 22 übernommen und im eigenen tatsächlich getesteten Umfang bestätigt.** [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79), Merge `6334b4f67485feaee4de5d649d6788475af940ec`; identischer vollständiger Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e` zum CI-/Gerätekopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`. Volle CI 38444430958 und exaktes Paket 38448369846 erfolgreich. Tobi meldet nach sämtlichen neun Chat-Testpunkten am 13.10.2026 20:59 Europe/Berlin: „Hab alles getestet.“ Keine Auffälligkeit gemeldet. Screenshot: Ereignisse 3168 / Störungen 1152 Originalbytes, jeweils 64-Byte-Signatur, aufgenommen · ungeprüft; sichtbare Anwendung nicht bezeichnet. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40126.md) nur bei Bedarf.

Neue eigene 0502/0503-Rohquellen und Child-Receipts in beiden Anwendungen; frische vollständige post-HASH-READs, strikte Dateigrenze und reale Card_Sign-Policy. Lokale HASH-/SIGN-Fehler erhalten unabhängig vollständige unsigned Originale; Transportverlust verwirft die Gruppe. Additive 21→22, echte Compilerexporte und eigene Reader-/SQLCipher-/Historien-/Rollback-/Compose-Abnahme. Vier Core- und fünf Identifikationsbelege, alte Originale/Fingerprints/Imports sowie eigenständige 19→20-/20→21-Prüfungen erhalten. Kein Backfill oder jüngerer Ersatz. Aufnahme bleibt ungeprüft.

Die bestätigten Vorgänger 40124/40125 bleiben erhalten; [40125-Beleg](docs/DEVICE_CONFIRMATION_40125.md). Zusätzlicher Produkt-Main-Lauf [38451502155](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38451502155) vollständig erfolgreich, alle vier Jobs. Reiner Dokumentationsabschluss: [PR #80](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/80), aktuelles CI-Ergebnis dort live prüfen. Keinen Trust-/DDD-/Schreib-/UI-Folgeumfang aus der Abnahme ableiten.

## Nächste Arbeit

Kein weiterer Produktauftrag freigegeben. 0502/0503-Meilenstein und Geräteabnahme nicht wiederholen. Für ein neues begrenztes Paket den vorhandenen Import-Folgeplan und die bereits erarbeitete Matrix verwenden; neue Umsetzung erst nach eigenständiger Umfangsentscheidung. Keine automatische weitere EF-, Export-, Vertrauens-, Schreib- oder Oberflächenphase.

Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` erhalten; zusätzliche Sicherung in `/workspace/scratch/4c3cf966d792/session-backups`. Isolierter Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`; geprüfter Produktbranch `work/40126-event-fault-remote`, Docs-Abschlussbranch `docs/close-40126-session-20261013`. Aktuellen main/PR/CI live prüfen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat geben; ZIP-/Repository-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
