# FahrerTagebuch - aktuelle Übergabe

Einstieg über [WORK_START.md](../WORK_START.md) und aktuellen Git/GitHub-Stand; keine alten Handoffs oder Chats rekonstruieren.

## Aktueller Meilenstein

**13.10.2026: vollständige 0504-/0505-Originalaufnahme als begrenzter Produktkandidat 40123 implementiert.** Basis: main nach gemergtem PR #69, `5b1b9669af8614ec502d027a4c71d94ab81496ad`; beim Start keine offenen PRs. Draft-[PR #70](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/70), Branch `feat/card-ef-snapshots-20261013`. Endgültigen Kopf, PR und CI live prüfen.

Rohdateien einschließlich Pointer/unbenutzter Bytes werden vor dem fachlichen Parser eingefroren. Bestehende verschlüsselte Schema-19-Tabellen mit neuen Original-Quellarten/Fingerprints; genaue Karten-/EF-/Generations-/Child-Sitzungsbindung. Keine Migration, Altquellenrekonstruktion oder Backfill. Leere und ungültige Auswertungen erhalten vollständige Rohaufnahme. Fehlende Kapazität/nicht versucht, EF abwesend und tatsächlicher Readerfehler bleiben getrennt; Transportabbruch verwirft die Mehranwendungsgruppe.

Unbenutzte Aktivitätsdatei: nur Nullpointer plus vollständig nullgefüllter Ring (Primärtabellen TCS150/154, Konsolidierung S. 281/284). Keine Tageskette oder Ruhe erfinden. Normalisierte nichtleere Tätigkeiten/Fahrzeuge bleiben erhalten. Alte Requests behalten ihre Abdeckung/Fingerprints; alte Auswahlen erhalten keine neueren Originale. Kleine Vorschau-/Importanzeigen für Originalgrößen und Herkunft, keine neue Navigation.

## Prüfung

- Lokal bestanden: Projektgrenzen, alle zehn CI-/Schema-Guard-Tests, Diff-Prüfung.
- Lokaler Gradle-Download blockiert (Netzwerk eingeschränkt). Keine lokale Android-/APK-Freigabe behaupten.
- Unit-Fälle: exakte Pointer/Bytes, defensive Kopien, strenge Leer-Erkennung, unabhängige Fingerprints, kurze Chunks, fehlende Kapazität/EF/abgebrochener Leseversuch.
- SQLCipher-Fälle: Reopen, raw-only EMPTY/INVALID, Request-/Historienerhalt, Byteänderungen, FK-/Korruptionsabwehr, atomarer Einzel-/Mehr-Child-Rollback und getrennte G1/G2V2-Originale bei MF am Parent.
- Compose: ausgewählte Originalgrößen/Generation, Wechsel zu historisch fehlender Quelle, raw-only Anzeige ohne erfundene fachliche Daten.
- Vollständige unveränderte Android-CI, endgültiger Kopf und tatsächliche Befunde im PR abfragen. **Noch keine eigene Geräteabnahme, kein Produktmerge.**

## Exakt nächste Arbeit

1. Offenen Snapshot-PR und dessen endgültigen Kopf live prüfen. Fehlgeschlagene CI gezielt anhand erster konkreter Ursache reparieren; keine Budgetänderung, Filter/Quarantäne oder Retryserie. Bei externer Blockade Ursache und Stand sichern.
2. Nach voller grüner CI gezielten bestehenden Workflow `device-test.yml` für den exakten Produktkopf mit `include_baseline=false` auslösen und 40123 bereitstellen. Normaler CI-Lauf erzeugt keine erfolgreichen Artefaktuploads; Uploadregeln unverändert lassen. Falls kein Workflow-Dispatch-Zugriff vorhanden ist, diese Grenze offen benennen, keinen Browser-/Budget-/CI-Umbau daraus machen.
3. [Geräteabnahme 40123](CARD_CORE_SNAPSHOT_DEVICE_TEST.md): Update/Altbestand, neue G1-/G2-Originalgrößen, exakte historische Auswahl und Wiederöffnung ohne Leser; weitere tatsächlich ausgeführte Fälle getrennt bestätigen.
4. Erst bei exaktem grünem Kopf, erforderlicher Gerätebestätigung und geprüfter Mergefähigkeit Produktmerge erwägen. **Gerätebasis 40122 gilt nicht für 40123.** Danach Status/Übergabe auf bestätigten Meilenstein ersetzen und getrennt den Signatur-PR planen.

## Weitere Sperren

Echte Dateisignaturen erst nach akzeptierter vollständiger Bytebasis; keine Signaturen rückwirkend an Altquellen hängen. Produktive Vertrauenskette, Standardexport und Download-Datum-Schreibpfad getrennte spätere Pakete. Automatischer Import bleibt lesend. `7F66`-Inhaltsdecoder ohne ISO-Primärformat und geschützte `0540` ohne legitimen SC5-Weg weiterhin gesperrt.

Spätere Produktbereiche nur nach CURRENT_RULES.md: neutrale Auswertung, UI/Einrichtung/GNSS. Keine Spesen-, Lohn-, Heimkehr-, Heimatstandort- oder PDF-Abrechnungsphase. Nach dem PR-/CI-Meilenstein Arbeit sichern und Sitzung abschließen; keine größeren Folgepakete im selben Zug beginnen.
