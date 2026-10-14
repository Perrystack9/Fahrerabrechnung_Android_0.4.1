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

**40127 / Room-Schema 22 übernommen und im eigenen tatsächlich getesteten Umfang bestätigt.** [PR #82](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/82), Merge `96370d0ef291af1ac3893333cd128c2a3f735033`; identischer vollständiger Baum `f818dcb8b1da48cdad7cdc555bcc7638c478b819` zum CI-/Gerätekopf `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc`. Volle CI [38515830418](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38515830418) und exaktes Paket [38530132281](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38530132281), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach den erklärten abschließenden Chat-Testpunkten am 14.10.2026 10:12 Europe/Berlin: „Das passt alles 👍“. GEN1 und GEN2V2: Tätigkeiten und Fahrzeuge mathematisch gültig; Zertifikatsvertrauen jeweils nicht geprüft. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40127.md) nur bei Bedarf. Zusätzlichen Produkt-Main-Lauf [38533736352](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38533736352) und aktuellen Dokumentationsabschluss live prüfen.

Abgeleitete Offline-Prüfung bereits gespeicherter 0504/0505-Receipts: strikte Gen1-RSA-Recovery/PKCS1-SHA1 und sechs feste Gen2/V2-ECDSA-Domänen. Exakte Karten-/Child-/Original-/Signatur-/Zertifikatsbindung; Rechnen außerhalb der Lesetransaktion. Keine DB-Schreiboperation, Migration, Backfill oder jüngerer Ersatz. Ursprüngliche Aufnahmestatus/Fingerprints unverändert, Room22 und alle Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Kleine getrennte Mathematik-/Vertrauensanzeige; Zertifikatsvertrauen immer NOT_EVALUATED.

Bestätigte Aufnahme-Meilensteine 40124/40125/40126 samt Core-, Identifikations-, Ereignis-/Störungsbelegen, vollständigen Originalen und historischer Herkunft bleiben erhalten. Andere EF-Signaturen bleiben aufgenommen · ungeprüft. [40126-Beleg](docs/DEVICE_CONFIRMATION_40126.md), [40125-Beleg](docs/DEVICE_CONFIRMATION_40125.md) nur bei konkretem Bedarf. Keine weitere EF-, Vertrauens-, DDD-/Export-, Schreib- oder UI-Freigabe aus 40127 ableiten.

## Nächste Arbeit

Der [freigegebene 40127-Plan A1–A10](docs/CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md) ist abgeschlossen; [technische Belege](docs/CARD_CORE_SIGNATURE_MATH_EVIDENCE.md). Auch der zusätzliche Produkt-Main-Lauf 38533736352 ist jetzt vollständig grün, alle vier Jobs.

**Neuer Planungsauftrag, 14.10.2026 10:48 Europe/Berlin:** mathematische Prüfung aller bereits gespeicherten Kartensignaturen durch Erweiterung um 0501/0520/0502/0503 und bedingte V2-0525 abschließen. [Kurzer technischer Plan mit A1–A8](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md). **Vollständige Umsetzung nach A1–A8 freigegeben14.10.2026 11:20 Europe/Berlin.** 40128 implementiert die gemeinsame Prüfung aller vorhandenen Belegfamilien; [Belege und noch offene CI-/Gerätegates](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md), [40128-Gerätetest](docs/DEVICE_TEST_40128.md). Bestehende Aufnahme/Herkunft/Room22 und sämtliche Gates erhalten. Nach positiver Abnahme diesen mathematischen Umfang schließen und anschließend Benutzeroberfläche/Funktionen für1.0 planen; konkrete UI-Implementierung daraus nicht vorwegnehmen.

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`, Produktbranch `feat/stored-signature-math-40128`. Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` und zusätzliche Sicherung `/workspace/scratch/4c3cf966d792/session-backups` erhalten; alte Produkt-/Docs-/Sicherungsbranches nicht löschen. Aktuellen main, offene PRs und CI live prüfen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen. Jeweils möglichst unter 100-200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat geben; ZIP-/Repository-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10-15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Erforderliche Gates bleiben bestehen.
- Nach einem Meilenstein/PR oder bei träger Sitzung Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
