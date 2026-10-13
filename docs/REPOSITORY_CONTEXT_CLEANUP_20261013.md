# Repository-/Kontextbereinigung - Prüfnachweis 13.10.2026

> **HISTORISCH - nicht als aktuelle Anforderung verwenden.** Nachweis dieses Dokumentations-Cleanups. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); Regeln/Status/Übergabe ersetzen frühere Checkpoints.

## Grundlage und aktiver Startpfad

Ausgangs-main nach bereits abgeschlossenem PR #67: `11301b4078db7094a7b6d234cf9f58e612e6b203`, Tree `f53073889c2bc3c6c04520485466438ff4cc4192`. PR #67 nicht erneut aufgebaut. Eigener Cleanup-Branch: `docs/work-context-cleanup-20261013`. Der exakte Remote-Kopf und seine endgültige CI werden im zugehörigen PR festgehalten; dieser Bericht ist kein vorweggenommener CI-/Mergebeweis.

Startliste exakt: WORK_START.md -> Git/GitHub prüfen -> CURRENT_RULES.md, WORK_STATUS.md, WORK_HANDOFF.md -> CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md. Fünf Dateien; der dreizeilige AGENTS.md-Wegweiser lenkt neue Worker dorthin. README und Dokumentenindex sind kein zusätzliches Pflichtpaket. Alle weiteren Specs/Belege nur bedarfsgesteuert.

Die frühere explizite README-Startliste nannte sieben Dateien einschließlich Produktentscheidung und Audit; konkrete weitere Implementierungs-/Gerätebelege kamen bei Bedarf hinzu. Ein allgemeines „alle Markdown lesen“ ist nun ausdrücklich ausgeschlossen. Die Zahl aller Repository-Markdown-Dateien ist keine Pflichtlesezahl.

## Größenvergleich

Explizite Startliste: **7 -> 5 Dateien**, UTF-8-Text **46,115 -> 20,451 Bytes** (55.7 % weniger). Messung am Ausgangs-main und diesem Arbeitsbaum; Bytezahl ist ein reproduzierbarer Näherungswert für Kontextumfang, keine behauptete exakte Tokenzahl.

| Datei | Zeilen vorher -> nachher | Bytes vorher -> nachher |
|---|---|---|
| README.md | 60 -> 29 | 4398 -> 2182 |
| WORK_STATUS.md | 56 -> 31 | 5043 -> 2939 |
| docs/CURRENT_RULES.md | 69 -> 46 | 7622 -> 7450 |
| docs/WORK_HANDOFF.md | 63 -> 28 | 5838 -> 2647 |
| docs/CARD_SOURCE_COVERAGE.md | 78 -> 50 | 11683 -> 4487 |
| ci/README.md | 50 -> 19 | 10522 -> 2287 |
| docs/VEHICLE_UI_SPEC.md | 224 -> 25 | 8080 -> 1944 |
| WORK_START.md | neu -> 39 | neu -> 3979 |

## Dokumente und Widersprüche

Alle 80 bisherigen Markdown-Pfade sind erhalten und eingeordnet. Mit den fünf neuen Markdown-Dateien sind 85 Pfade im [vollständigen Index](DOCUMENTATION_INDEX.md) erfasst: 7 AKTUELL, 26 DETAIL-AKTIV, 45 HISTORISCH, 6 ÜBERHOLT, 1 DUPLIKAT.

- Alte Root-Build-/Startberichte, Geräteanleitungen/-bestätigungen, PR-/Paket-/CI-Checkpoints und doppelte Handoffs bleiben am bisherigen Pfad und sind ausdrücklich historisch/depriorisiert.
- Frühere UI-/Onboarding-/PDF-Layouts sind überholt; Brand-/Gerätefeedback bleiben historische Referenzen. Die bereits gekennzeichnete PDF-Spezifikation im Report-Assetpfad wurde bytegleich belassen.
- Abgeschlossener Speicherentwurf ist als DUPLIKAT/HISTORISCH gekennzeichnet; die aktuelle Zwei-Anwendungs-Implementierung führt keinen zweiten offenen PR-65-Auftrag mehr.
- Historische PR-45-/Quota-/CI-Nachweise sind aus der aktiven CI-Politik in `docs/history/CI_ARTIFACT_POLICY_PR45_20261010.md` getrennt; die alte Handoff-Referenz wurde auf ihren erhaltenen historischen Beleg repariert.
- Veraltete aktive „PR noch offen“, „keine Gerätefreigabe“, alte Ausgabezeiten und 401xx-/Schema-Checkpointketten sind aus aktuellen Status-/Einleitungsabschnitten entfernt oder unter HISTORISCH getrennt.
- Aktuelle Regeln bündeln den gestrichenen Spesen-/Lohn-/Abrechnungs-/Heimat-/Heimkehr-/Übernachtungs-/PDF-Umfang. Neutrale Tätigkeiten/GNSS bleiben; etwa vier neue Hauptbereiche sind Zielrichtung, kein jetzt umgesetzter UI-Umbau.

Keine früheren Kerndokumente als zusätzliche komplette Snapshotkopien angelegt, soweit Git ihren damaligen Stand verlässlich erhält. Originale aller schon historischen/überholten/duplizierten Dateien bleiben vollständig unter einer zusätzlichen Kopfzeile erhalten; technische Belege wurden nicht verfälscht.

## Verifikation und Erhalt

- Alle **297 Nicht-Markdown-Dateien** des Ausgangsbaums anhand ihrer Git-Blob-SHAs bytegleich: Produktcode, Reader/Parser, SQLCipher, DB/Migrationen, sämtliche Room-Schemaexporte, Tests, Build/CI, Schlüssel und funktionale Assets.
- Keine ursprüngliche Datei, Branch, History oder Artefakt gelöscht; keine neue Produktfunktion, Version, Migration, Geräteausgabe oder Hardwarefreigabe.
- Bestehende Projektgrenzen, alle zehn unveränderten CI-/Schema-Guard-Regressionsprüfungen und Shellsyntax bestanden.
- Markdown-Datei-/Bild-/Dokumentziele einschließlich lokaler Fragmente vollständig geprüft; ursprüngliche relative Pfade bleiben erhalten. Diff-/Whitespaceprüfung und Dokumentationsklassifikation am endgültigen lokalen Kopf prüfen.
- Aktive Kerntexte und Detailabschnitte gezielt auf gestrichene Anforderungen, alte offene PR-/Hardwarezustände, falsche GNSS-Routenbehauptung und feste alte Navigations-/Einrichtungsvorgaben abgeglichen. Historische Aussagen sind kein neuer Auftrag.
- Kein CI-/Hardware-/Merge-Gate geändert. Dieser Diff liegt vollständig in den bereits erlaubten Markdown-Dokumentationspfaden; die unveränderte konservative Klassifikation entscheidet über die leichte CI.

## Nächster Schritt und Session-Routine

Nach diesem Dokumentationsabschluss: Pflichtdateien-/Ausnahme-/Signaturmatrix für Gen1, Gen2 und Gen2V2 gegen verifizierte Primärformate erarbeiten. Keine neue Reader-/Schreib-/Vertrauensfunktion in diesem Cleanup.

Abschluss: „Übergabe vorbereiten und Sitzung abschließen.“ Neuer Work-Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“** Kurzlebige Sitzungen; Repository als Langzeitgedächtnis, aktuelle Checkpoints ersetzen, historische Texte gezielt und lange CI sparsam prüfen.
