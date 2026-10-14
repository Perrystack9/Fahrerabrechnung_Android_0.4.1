# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

**Plan zur Prüfung veröffentlicht:** [Draft-PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88), keine Umsetzungsfreigabe/kein Merge. Erster veröffentlichter Planungsbaum `a20417ca62631d4aae973f852ccf16dfb43cbea8`, Quellkopf `f31885111c45b2cc692742c1889a3aec9bb2eb8e`; reine Docs-CI [38590326579](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38590326579), Versuch1, erfolgreich. Projektgrenzen/CI-Gates grün, Produktbuild/Instrumentierung nur nach unveränderter Docs-Klassifikation ausgelassen. Kein Engine-/Hardwareerfolg daraus ableiten. Diese PR-/CI-Notiz ist ein anschließender reiner Docs-Nachlauf; endgültigen Draftkopf und dessen CI beim Start live prüfen.

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

**Aktueller Auftrag14.10.2026:** Die vollständigen80Produktentscheidungen und GrundsätzeA–H wurden in dieser Sitzung übergeben und in [der verbindlichen Produktquelle](docs/VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md) dokumentiert. Priorität unverändert: vollständige zeitbezogene561/2006-Engine, danach Kalender, Auswertungen und neue Oberfläche. AETR und eigenständige nationale Arbeitszeitprüfungen außerhalb dieses Auftrags. Keine Rekonstruktion separater Chats.

[Planungsentwurf](docs/VIOLATION_ENGINE_V1_PLAN.md) mit Architektur, Daten-/Quellenvertrag,57Regelzeilen,98fachlichen und31Daten-/Technikfällen, vollständiger80er-Abnahmezuordnung sowie16Abnahmekriterien liegt zur Prüfung vor. **Noch keine Engine implementiert und keine Umsetzungsfreigabe.** Offene ProduktpräzisierungenP01–P05 und fachliche Primärquellen-/InterpretationspunkteL01–L09 vor betreffender Umsetzung klären. Amtliche EUR-Lex-Volltexte waren direkt teilweise nicht abrufbar; indexierte Abschnitte/Kommissionsleitlinien sind kein vollständig gepinnter und unabhängig abgenommener Gesetzeskorpus. Keine rechtliche Gesamtvollständigkeit behaupten.

Bestehende EF0504/0522/0530 sowie genaue eigene Import-/Anwendungsquellen wiederverwenden. Besonderer Planbefund: EF0504-c-Bit ist bei nicht eingesetzter Karte kein Crewbeweis; bekannte Kontrollgeräte-Nachträge von unbekannten Entnahmefolgen unterscheiden. Anzeige-Tagesende und App-Speicherzeit sind kein sicherer juristischer Datenstichtag. Keine Originale/Parser/DB/Reader/UI in dieser Planung ändern.

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
