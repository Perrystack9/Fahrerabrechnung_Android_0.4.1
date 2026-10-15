# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für den aktuellen M2-Schritt den [M2-Implementierungsbericht](docs/VIOLATION_ENGINE_M2_IMPLEMENTATION.md) öffnen. M2-Rechtsnachweise/Abnahme und ältere M0/M1-Belege nur bei konkretem Bedarf.

Das sind **fünf Startdateien einschließlich WORK_START.md**. Keine pauschale Markdown-Volltextlektüre oder Rekonstruktion alter Chats, wenn das Repository ausreicht.

## Aktuelle Produktgrenzen

- Bestehenden Fahrerkarte-Import vollständig erhalten; Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten dürfen neutral angezeigt und ausgewertet werden.
- Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort samt Einrichtung, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung sind vollständig gestrichen.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant.
- GNSS bleibt: echte Punkte und später gekennzeichneter grober chronologischer Verlauf; keine behauptete gefahrene Straßenroute oder Kilometer aus Verbindungslinien.
- Priorität für1.0: vollständige zeitbezogene Verstoß-Engine nach Verordnung (EG) Nr.561/2006; danach Kalender, Auswertungen und neue Oberfläche/Navigation/Onboarding. Zielrichtung etwa vier Bereiche **Start, Kalender, Auswertung, Einstellungen** bleibt offen; konkrete Anforderungen/Umsetzung separat planen.

## Nachweislich fertig und offene Grenzen

**40128 / unverändertes Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`; vollständiger identischer Baum `066dc0532fd2de486def695323b74f8e9d1150bd` zum endgültigen CI-/Gerätekopf `7f5f770aa71ec70bb8f02d51cae3403a6f904637`. Volle CI [38547729369](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38547729369) und exaktes Gerätepaket [38553890752](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38553890752), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach der vollständigen neunteiligen Chat-Testanleitung am **14.10.2026 13:47 Europe/Berlin: „Funktioniert alles wie es soll 👍“**. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40128.md).

**Die mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen ist nach A1–A8 abgeschlossen.** 0504/0505 und 0501/0520/0502/0503 für GEN1/GEN2/GEN2V2; 0525 ausschließlich GEN2V2 bei vorhandenem Beleg. Gemeinsame geprüfte RSA-/ECDSA-Engine, konsistenter gefrorener Read-Snapshot, exakte Karte/Child/EF/Original/Receipt/Zertifikatsbindung und getrennte Mathematik-/Vertrauensanzeige. Fehlende Voraussetzungen bleiben begründet nicht prüfbar; Zertifikatsvertrauen stets NOT_EVALUATED.

Keine DB-Schreiboperation, Migration, Cache, Backfill, neue Signaturaufnahme oder jüngerer Quellenersatz. Alte Aufnahmestatus/Fingerprints, Room22 und sämtliche Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Bestätigte Vorgänger 40124–40127 nicht erneut bearbeiten. [Abgeschlossener Plan](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), [technische Nachweise](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md).

Zusätzliche volle Produkt-Main-CI [38559255131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38559255131), Versuch1, für Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405` jetzt vollständig erfolgreich; alle vier Jobs einschließlich ungekürzter API36-SQLCipher-/Compose-Instrumentierung und Abschlussgate. Am14.10.2026 nach Übergabeauftrag live bestätigt. Keine offenen40128-Prüfungen; Produktmerge besitzt exakt den endgültigen CI-/Gerätebaum.

## Nächste Arbeit und Übergabe

**M0/M1 kontrolliert integriert; M2-Implementierung ausdrücklich genehmigt.** Tobi hat M1/40129/Room23 nach neuer eigener Geräteprüfung am15.10.2026 abgenommen und PR88 zuerst/PR89 danach freigegeben. M0-Planungsmerge `9ff0a525d8394031148aada37593c9fed8f84148`, M1-Produktmerge/neue Basis **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**. Neue volle Integrations-CI38744945937/Versuch1 erfolgreich, alle Produktdateien gegenüber dem bestätigten40129-Kopf212a140 bytegleich. [Nachweise](docs/M0_M1_INTEGRATION_20261015.md). Zusätzliche volle main-CI38749285258/Versuch1 ebenfalls erfolgreich.

M2-Branch `feature/v1-m2-driving-breaks-20261015`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m2`; Entwicklungsstand40130/Room23. [Implementierung](docs/VIOLATION_ENGINE_M2_IMPLEMENTATION.md), [Rechtsnachweise/Entscheidungsvorlage](docs/VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md), [Abnahme](docs/VIOLATION_ENGINE_M2_ACCEPTANCE.md). D01–D06/B01–B05, keine zweite Parserarchitektur, keine DB-/Krypto-/Hardware-/CI-Änderung. Alle80/A–H/E01–E12/P01–P05 bleiben maßgeblich.

**L02 bleibt offen:** UTC ist Speicher-/Dauerbasis und technisches Wochenraster. Technische UTC-Norm und lokale TRACE-Auslegung sind nicht hinreichend verbindlich aufgelöst. Wochen-/Verlängerungsprojektionen tragen feste Einschränkungen, sichere davon abhängige Budgets bleiben null. Keine Testannahme oder Nutzerpräferenz als Rechtsnachweis.7h ist nur benannte Tageslenk-Vollzugskonvention, keine erfüllte Tagesruhe. Wochenruhe setzt zwei10-h-Verlängerungen innerhalb derselben Woche nicht zurück; M3-Ruhereduzierungen bleiben getrennter Anker/Zähler.

M2 benötigt seine eigene vollständige unveränderte CI und erforderliche neue Geräteprüfung. Nicht aus frühen isolierten Rechentests oder40129-Antwort ableiten. **M2-Merge und M3–M7/Release bleiben gesperrt**, bis Tobi ausdrücklich zustimmt. Neue Oberfläche/Badges M5, PDF M6, keine vorgezogene Ruhe-/Ausgleichsqualifikation. Historische Ortszeit ist ab15.10.2026 bevorzugte spätere Anzeige; Standardzone als Rückfall, Unklarheit kennzeichnen, nie Rechtsberechnung verändern.

Frühere automatische Workspace-Wartung entfernte die alte uncommittete Settings-Arbeit. M1 ist aus GitHub wiederhergestellt; der alte unvollständige Entwurf nicht. [Verlustgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Nicht rekonstruieren oder als vorhanden melden. Alle vorhandenen Dateien/Branches/Backups erhalten, kein Reset/Clean.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
