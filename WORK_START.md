# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

**Aktueller M0-Stand15.10.2026:** [Draft-PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt ausschließlich Dokumentation. Tobi hat M0 am14.10. ausdrücklich für fachliche Spezifikation/Dokumentation freigegeben, nach Pause am15.10. fortgesetzt. Ausgangskopf `6800d79653928e258a77d1737215a960ed685fb0`, dessen Docs-CI38607504273 erfolgreich; main `df7061659b621d67ff39ac2f02f44e13d7ac4e86` bei Wiederaufnahme unverändert. **Keine M1-Programmierungs-, Merge- oder Releasefreigabe.** [M0-Bericht](docs/VIOLATION_ENGINE_M0_REPORT.md) mit Quellen/Referenzen/Restblockern und M1-Empfehlung liegt zur Prüfung vor; endgültigen neuen PR-Kopf/CI live prüfen. P01–P05 bleiben geschlossen.

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

**Aktueller Auftrag14.10.2026:** [Historische80 Produktentscheidungen/A–H](docs/VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md) bleiben unverändert; [verbindliche E01–E12 und finale P03–P05](docs/VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md) haben bei Widersprüchen Vorrang. [Änderungsübersicht](docs/VIOLATION_ENGINE_PLAN_CHANGES_20261014.md) nennt überholte Planstellen, geschlossene und offene Punkte. Keine Rekonstruktion separater Chats.

[Gesamtplan](docs/VIOLATION_ENGINE_V1_PLAN.md), Rechts-/Testmatrix:58 unveränderte Regelzeilen,116 LG-Regel-/Fachfälle,7 neue PF-Produktfälle,51 Daten-/Technikfälle (43+8),80 PD-/12 E-/5 P-Zuordnungen und23 Abnahmekriterien (20+3), vollständig M0–M7 zugeordnet. **Keine Engine implementiert; alle neuen Fallgruppen geplant, nicht ausgeführt. P01–P05 geschlossen, keine konkrete offene Produktfrage für den Engine-Plan.** P03: Frühwarnfunktionen erhalten, feste24-/2-h-Auslöser gestrichen; P04: lokales Offline-Fehlerformular/Vorschau/freiwillige Diagnose/Speichern/bewusstes Teilen ohne Backend; P05: belegter Schichtbeginn oder exakt „Schichtzuordnung unklar“, vollständiger Kalender und unveränderte Rechtsrechnung.

**M0 ausdrücklich freigegeben und dokumentarisch bearbeitet:** [Quellen/L01–L09](docs/VIOLATION_ENGINE_M0_SOURCE_REGISTER.md),35 amtliche Downloadpins/14Auszüge, [M1-Datenvertrag](docs/VIOLATION_ENGINE_M1_INPUT_CONTRACT.md), [116LG-Referenzen/vier Gesamtfolgen](docs/VIOLATION_ENGINE_M0_REFERENCE_CASES.md) sowie [51DT/7PF-Referenzen](docs/VIOLATION_ENGINE_M0_DATA_CASES.md).74 reine Referenzarithmetik-/Bit-/Zeitzonenprüfungen bestanden; keine Engine-/UI-/PDF-/Gerätefälle gelaufen. Autorenspezifikation ist keine unabhängige Rechtsabnahme. M1-relevante End-/Quellen-/Scope-/Kontextnachweise vor Programmierungsfreigabe schließen; weitere L-Punkte vor jeweiligem Paket. M0-Bericht prüfen, kein automatischer Programmierstart oder Merge. Auch Dokumentationsmerge separat vorher genehmigen lassen.

EF0504/0522/0530 und genaue eigene Quellen wiederverwenden. Neuer Plan: bekannter Kontrollgeräte-Nachtrag dezent „Manueller Nachtrag am EG-Kontrollgerät“; Kartenstatus/c-Bit gemeinsam, unbekannte Entnahmefolge keine Ruhe/Crew. Aufgezeichnetem Mehrfahrerbetrieb grundsätzlich ohne Routine-Zweitnachweis vertrauen.23 h kann Tagesruhe sein, erfüllt keine Wochenruhe. Artikel-12-Zahlenfall bleibt Übersicht/PDF mit offener Rechtsbewertung, keine automatische Rechtfertigung/Schuld. Aktive Spezialbetriebskataloge begrenzt; gesetzliche Nichtanwendbarkeit und Prüflücken respektieren. Keine Original-/Parser-/DB-/Reader-/UI-/APK-Änderung in dieser Planung.

Die mathematische Signaturprüfung ist mit40128 vorerst beendet; keine automatische Trust-/DDD-/Export-/Signaturaufnahme-/Kartenschreibphase. **„Zertifikatsvertrauen: nicht geprüft“ bleibt erwartetes Verhalten**, auch nach erneutem Einlesen.

Übergabe-PR [#87](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/87) übernommen, aktueller Ausgangs-main `df7061659b621d67ff39ac2f02f44e13d7ac4e86`. Docs-main-CI38562576031 und volle Produkt-main-CI38559255131 erfolgreich. Vor Planveröffentlichung keine offenen PRs. Aktuellen main und den späteren Draft-Plan-PR weiterhin live prüfen; keine bereits abgenommenen Produktprüfungen wiederholen.

Isolierter Planungscheckout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-v1-engine-plan`, lokaler M0-Branch `docs/v1-m0-evidence-20261014-2034` (vorheriger Finalisierungsbranch erhalten), Remote-PR-Branch `docs/v1-violation-engine-plan-20261014`. Frühere Planungsbranches erhalten. Bestätigter Vorgängercheckout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures` auf `work/v1-handoff-confirmed-main-20261014` erhalten. Ursprüngliche offene Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` und Sicherungen `/workspace/scratch/4c3cf966d792/session-backups` unverändert erhalten. Keine Dateien/Änderungen/Branches/Sicherungen verwerfen oder zurücksetzen.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
