# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

**Aktualisierter Plan zur Prüfung:** [Draft-PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt ausschließlich Dokumentation, keine Implementierungs-/Releasefreigabe oder Merge. Ergänzungen E01–E12 veröffentlicht auf `5bc0d1ebbf63412e6cac65d7532e09c09141f42f`, vollständiger geprüfter Baum `adf538027e8475a97a1ee3dee62d60cf041f305d`; reine Docs-CI [38600513431](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38600513431), Versuch1, erfolgreich. Der folgende reine Docs-Nachlauf präzisiert paketübergreifende Teilnachweise und diese Übergabenotiz. Endgültigen neuen Draftkopf und dessen Checks live prüfen; Docs-CI ist keine Engine-/Produkt-/Geräteabnahme.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für den aktuellen 1.0-Planungsschritt genau den [Verstoß-Engine-Plan](docs/VIOLATION_ENGINE_V1_PLAN.md) öffnen. Er verlinkt die vollständig übergebenen80Entscheidungen, Regel- und Testmatrix; diese nur beim entsprechenden Planungs-/Freigabeabgleich lesen. Der [abgeschlossene Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) bleibt Detailquelle für konkrete Importfragen.

Das sind **fünf Startdateien einschließlich WORK_START.md**. Keine pauschale Markdown-Volltextlektüre oder Rekonstruktion alter Chats, wenn das Repository ausreicht.

## Aktuelle Produktgrenzen

- Bestehenden Fahrerkarte-Import vollständig erhalten; Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten dürfen neutral angezeigt und ausgewertet werden.
- Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort samt Einrichtung, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung sind vollständig gestrichen.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant. Nach neuer Entscheidung59/71–75/78 gehört ein eigener lokaler **PDF-Verstoßbericht** zu1.0, ausdrücklich ohne DDD-/Abrechnung.
- GNSS bleibt: echte Punkte und später gekennzeichneter grober chronologischer Verlauf; keine behauptete gefahrene Straßenroute oder Kilometer aus Verbindungslinien.
- Priorität für1.0: vollständige zeitbezogene Verstoß-Engine nach Verordnung (EG) Nr.561/2006; danach Kalender, Auswertungen und neue Oberfläche/Navigation/Onboarding. Zielrichtung etwa vier Bereiche **Start, Kalender, Auswertung, Einstellungen** bleibt offen; konkrete Anforderungen/Umsetzung separat planen.

## Nachweislich fertig und offene Grenzen

**40128 / unverändertes Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`; vollständiger identischer Baum `066dc0532fd2de486def695323b74f8e9d1150bd` zum endgültigen CI-/Gerätekopf `7f5f770aa71ec70bb8f02d51cae3403a6f904637`. Volle CI [38547729369](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38547729369) und exaktes Gerätepaket [38553890752](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38553890752), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach der vollständigen neunteiligen Chat-Testanleitung am **14.10.2026 13:47 Europe/Berlin: „Funktioniert alles wie es soll 👍“**. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40128.md).

**Die mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen ist nach A1–A8 abgeschlossen.** 0504/0505 und 0501/0520/0502/0503 für GEN1/GEN2/GEN2V2; 0525 ausschließlich GEN2V2 bei vorhandenem Beleg. Gemeinsame geprüfte RSA-/ECDSA-Engine, konsistenter gefrorener Read-Snapshot, exakte Karte/Child/EF/Original/Receipt/Zertifikatsbindung und getrennte Mathematik-/Vertrauensanzeige. Fehlende Voraussetzungen bleiben begründet nicht prüfbar; Zertifikatsvertrauen stets NOT_EVALUATED.

Keine DB-Schreiboperation, Migration, Cache, Backfill, neue Signaturaufnahme oder jüngerer Quellenersatz. Alte Aufnahmestatus/Fingerprints, Room22 und sämtliche Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Bestätigte Vorgänger 40124–40127 nicht erneut bearbeiten. [Abgeschlossener Plan](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), [technische Nachweise](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md).

Zusätzliche volle Produkt-Main-CI [38559255131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38559255131), Versuch1, für Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405` jetzt vollständig erfolgreich; alle vier Jobs einschließlich ungekürzter API36-SQLCipher-/Compose-Instrumentierung und Abschlussgate. Am14.10.2026 nach Übergabeauftrag live bestätigt. Keine offenen40128-Prüfungen; Produktmerge besitzt exakt den endgültigen CI-/Gerätebaum.

## Nächste Arbeit und Übergabe

**Aktueller Auftrag14.10.2026:** [Historische80 Produktentscheidungen/A–H](docs/VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md) bleiben unverändert; [neuere verbindliche Ergänzungen E01–E12](docs/VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md) haben bei Widersprüchen Vorrang. [Änderungsübersicht](docs/VIOLATION_ENGINE_PLAN_CHANGES_20261014.md) nennt überholte Planstellen, geschlossene und offene Punkte. Keine Rekonstruktion separater Chats.

[Gesamtplan](docs/VIOLATION_ENGINE_V1_PLAN.md), Regel- und Testmatrix:58 Regelzeilen,116 fachliche Fälle (98+18),43 Daten-/Technikfälle (31+12),80 PD- und12 E-Zuordnungen,20 Abnahmekriterien (16+4) sowie Pakete M0–M7 mit Abhängigkeiten. **Keine Engine implementiert, keine neuen fachlichen Fälle ausgeführt, keine Umsetzungsfreigabe.** P01/P02 durch Fähre-/Artikel-12-Beschlüsse geschlossen. Produktseitig noch P03 Warnschwellen, P04 Fehlerbericht-Zustellung, P05 ungeklärter Display-Schichtbeginn; juristische Quellen-/Auslegungs-/Fachnachweise separat L01–L09. Kein vollständiger Volltextpin oder unabhängige Fachabnahme behauptet.

EF0504/0522/0530 und genaue eigene Quellen wiederverwenden. Neuer Plan: bekannter Kontrollgeräte-Nachtrag dezent „Manueller Nachtrag am EG-Kontrollgerät“; Kartenstatus/c-Bit gemeinsam, unbekannte Entnahmefolge keine Ruhe/Crew. Aufgezeichnetem Mehrfahrerbetrieb grundsätzlich ohne Routine-Zweitnachweis vertrauen.23 h kann Tagesruhe sein, erfüllt keine Wochenruhe. Artikel-12-Zahlenfall bleibt Übersicht/PDF mit offener Rechtsbewertung, keine automatische Rechtfertigung/Schuld. Aktive Spezialbetriebskataloge begrenzt; gesetzliche Nichtanwendbarkeit und Prüflücken respektieren. Keine Original-/Parser-/DB-/Reader-/UI-/APK-Änderung in dieser Planung.

Die mathematische Signaturprüfung ist mit40128 vorerst beendet; keine automatische Trust-/DDD-/Export-/Signaturaufnahme-/Kartenschreibphase. **„Zertifikatsvertrauen: nicht geprüft“ bleibt erwartetes Verhalten**, auch nach erneutem Einlesen.

Übergabe-PR [#87](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/87) übernommen, aktueller Ausgangs-main `df7061659b621d67ff39ac2f02f44e13d7ac4e86`. Docs-main-CI38562576031 und volle Produkt-main-CI38559255131 erfolgreich. Vor Planveröffentlichung keine offenen PRs. Aktuellen main und den späteren Draft-Plan-PR weiterhin live prüfen; keine bereits abgenommenen Produktprüfungen wiederholen.

Isolierter Planungscheckout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-v1-engine-plan`, Branch `docs/v1-violation-engine-plan-20261014`. Bestätigter Vorgängercheckout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures` auf `work/v1-handoff-confirmed-main-20261014` erhalten. Ursprüngliche offene Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` und Sicherungen `/workspace/scratch/4c3cf966d792/session-backups` unverändert erhalten. Keine Dateien/Änderungen/Branches/Sicherungen verwerfen oder zurücksetzen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
