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

**Freigabe für 40127 erteilt**, 14.10.2026, 06:13 Europe/Berlin. [Verbindlicher Plan und Abnahmekriterien](docs/CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md). Plan-PR #81 übernommen, Basis main `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0`; dessen Docs-CI 38510387252 erfolgreich. 40126 bleibt bestätigte Produktbasis; 40127 liegt bis neuer vollständiger CI und eigener Gerätebestätigung auf getrenntem Produktbranch.

Implementiert: abgeleitete Offline-Prüfung vorhandener 0504/0505-Receipts; Gen1-RSA-Recovery/striktes PKCS1-SHA1, sechs Gen2/V2-ECDSA-Domänen, exakte Child-/Karten-/Quellenbindung und kleine getrennte Mathematik-/Vertrauensanzeige. Aufnahmezustände/alle Fingerprints unverändert, Room22 ohne Migration. Zertifikatsvertrauen immer NOT_EVALUATED. [Belege und offene Gates](docs/CARD_CORE_SIGNATURE_MATH_EVIDENCE.md); [40127-Gerätetest](docs/DEVICE_TEST_40127.md). Keine neue weitere EF-, Export-, Trust-, Schreib- oder UI-Phase.

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`, Implementierungsbranch `feat/core-signature-math-40127`. Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` und zusätzliche Sicherung `/workspace/scratch/4c3cf966d792/session-backups` erhalten; alte Produkt-/Docs-/Sicherungsbranches nicht löschen. Aktuellen main/Produkt-PR/CI live prüfen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat geben; ZIP-/Repository-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
