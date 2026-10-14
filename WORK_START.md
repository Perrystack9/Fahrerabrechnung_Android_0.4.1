# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für den nächsten Produkt-Schritt genau den [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) öffnen. Weitere Fachtexte erst bei konkretem Bedarf.

Das sind **fünf Startdateien einschließlich WORK_START.md**. Keine pauschale Markdown-Volltextlektüre oder Rekonstruktion alter Chats, wenn das Repository ausreicht.

## Aktuelle Produktgrenzen

- Bestehenden Fahrerkarte-Import vollständig erhalten; Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten dürfen neutral angezeigt und ausgewertet werden.
- Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort samt Einrichtung, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung sind vollständig gestrichen.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant.
- GNSS bleibt: echte Punkte und später gekennzeichneter grober chronologischer Verlauf; keine behauptete gefahrene Straßenroute oder Kilometer aus Verbindungslinien.
- Oberfläche, Navigation und Onboarding als nächster Produktschwerpunkt für 1.0 neu konzipieren. Zielrichtung etwa vier Bereiche **Start, Kalender, Auswertung, Einstellungen**; konkrete Struktur und Umsetzung separat planen.

## Nachweislich fertig und offene Grenzen

**40128 / unverändertes Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`; vollständiger identischer Baum `066dc0532fd2de486def695323b74f8e9d1150bd` zum endgültigen CI-/Gerätekopf `7f5f770aa71ec70bb8f02d51cae3403a6f904637`. Volle CI [38547729369](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38547729369) und exaktes Gerätepaket [38553890752](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38553890752), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach der vollständigen neunteiligen Chat-Testanleitung am **14.10.2026 13:47 Europe/Berlin: „Funktioniert alles wie es soll 👍“**. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40128.md).

**Die mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen ist nach A1–A8 abgeschlossen.** 0504/0505 und 0501/0520/0502/0503 für GEN1/GEN2/GEN2V2; 0525 ausschließlich GEN2V2 bei vorhandenem Beleg. Gemeinsame geprüfte RSA-/ECDSA-Engine, konsistenter gefrorener Read-Snapshot, exakte Karte/Child/EF/Original/Receipt/Zertifikatsbindung und getrennte Mathematik-/Vertrauensanzeige. Fehlende Voraussetzungen bleiben begründet nicht prüfbar; Zertifikatsvertrauen stets NOT_EVALUATED.

Keine DB-Schreiboperation, Migration, Cache, Backfill, neue Signaturaufnahme oder jüngerer Quellenersatz. Alte Aufnahmestatus/Fingerprints, Room22 und sämtliche Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Bestätigte Vorgänger 40124–40127 nicht erneut bearbeiten. [Abgeschlossener Plan](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), [technische Nachweise](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md).

Zusätzlicher voller Produkt-Main-Lauf [38559255131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38559255131) für Produktmerge beim Abschluss noch laufend; kein Erfolgsclaim daraus. Aktuellen Lauf und getrennten Dokumentationsabschluss beim nächsten Start live prüfen. Finale Produkt-PR-CI und eigene Geräteabnahme sind erfolgreich; der Produktmerge besitzt den identischen vollständigen Baum.

## Nächste Arbeit

**Benutzeroberfläche und vereinbarte Funktionen für Version 1.0 planen.** Bestehenden tatsächlich bestätigten Import-/Datenstand übernehmen und konkrete Navigation, Ansichten und Bedienabläufe mit Tobi abstimmen. Noch keine bestimmte UI- oder Funktionsimplementierung daraus ableiten. Keine automatische weitere Kryptografie-, Vertrauenskette-, DDD-/Export-, Signaturaufnahme- oder Kartenschreibphase.

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`; Abschlussbranch `docs/stored-signature-math-40128-closure`. Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` und Sicherung `/workspace/scratch/4c3cf966d792/session-backups` erhalten; alte Produkt-/Docs-/Sicherungsbranches nicht löschen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
