# FahrerTagebuch - aktuelle Übergabe

Einstieg ausschließlich über [WORK_START.md](../WORK_START.md) und aktuellen Git/GitHub-Stand. Regeln und bestätigten technischen Zustand nicht aus alten Handoffs rekonstruieren.

## Erreichter Meilenstein

**13.10.2026: Pflichtdateien-/Ausnahme-/Signaturmatrix erarbeitet.** Basis main nach PR #68, `6190efa38762749a165922820b75ef61b998135f`; dessen Main-CI [38330641632](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38330641632) grün. Beim Start keine offenen PRs, sauberer Arbeitsbaum. Arbeit gesichert im Branch `docs/card-download-matrix-20261013`; endgültigen PR-Kopf und CI/Merge beim nächsten Start live prüfen.

Die [Matrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) enthält 44 EF-Zeilen, konkrete Primärstellen, tatsächliche Reader-/Speicher-/Sitzungspfade, V2-/Kontrollausnahmen, Hash-/Signaturverfahren und getrennte Standarddownload-/Datums-/TLV-Anforderungen. Vollständige amtliche PDFs und die tatsächlich referenzierte TR-03111-Ausgabe 2.00 wurden gelesen; MF-/DF-Tabellen zusätzlich visuell geprüft. Keine historischen Chats rekonstruiert.

**Neue Erkenntnisse für die Folgearbeit:** P2=00 beim Datei-Hash beider DFs gemäß Änderung 2018, Gen2-Hash aus Card_Sign statt Aussteller-/Card_MA-Kurve. 0504/0505 haben noch keinen unabhängigen vollständigen EF-Blob; leerer Fahrzeugbestand erhält EMPTY ohne gespeichertes Original. Reader-/Parse-/Kapazitäts-/Nicht-versucht-Zustände sind noch nicht überall getrennt. Diese Dokumentation behebt keinen Produktpfad.

Produktbasis bleibt **40122 / Schema 19**, bestätigte Zwei-Anwendungs-Speicherung übernommen. Keine neuen Befehle, Migrationen, Vertrauensfunktionen, APK oder Gerätefreigabe.

## Exakt nächster Auftrag

**Begrenzter Implementierungs-PR für vollständige Originalsnapshots von 0504 und 0505**, einschließlich vorhandener unbenutzter Dateien. Details und Abnahme im [Import-Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md), konkrete Primär-/Codezuordnung in der Matrix erst für diese Facharbeit öffnen.

- Rohsnapshot unabhängig vom fachlichen Parsergebnis unveränderlich erfassen; tatsächliche Karte, Anwendung, EF und neue Child-Sitzung zusammenbinden.
- Erfolgreiche Rohaufnahme, gültig leer, Parserfehler, Leseabbruch und nachweislich nicht versucht unterscheiden. Unklare Default-/Pointerfälle gezielt am Primärformat prüfen, nicht leer erraten.
- Bestehende Importdaten, Requests, normalisierte Tätigkeiten/Fahrzeuge, beide Anwendungssätze und historische Auswahl erhalten; keine Rekonstruktion/Backfill alter Quellen.
- Passende additive Migration nur mit Compiler-Schemaexport; volle unveränderte Android-CI, eigene erforderliche Gerätebestätigung vor Merge. **Kein Produktmerge mit bloßer Freigabe 40122.**

## Offene Sperren

- Echte Dateisignaturen fehlen für alle signierten Pflichtdateien. Erst nach gesicherter Byteaufnahme getrennten Signatur-PR planen; keine Signatur nachträglich an ältere Originale hängen.
- Produktive ERCA/MSCA/Card_Sign-Vertrauenskette, Rollen/Gültigkeit/Rollover, Standardexport und LastCardDownload-Schreibpfad weiterhin offen. Automatischer Import bleibt lesend.
- `7F66`: ISO 7816-4:2013 §12.7.1 fehlt weiterhin; äußere Struktur/Rohbytes erhalten, keine Puffergröße erraten.
- `0540`: legitimer SC5-/Secure-Messaging-Weg fehlt; nicht versucht, keine Schlüssel/VU-Identität erfinden. Nicht im expliziten Mindestkatalog bedeutet keine belegte pauschale Download-Ausnahme.
- Bei externer CI-/Budgetblockade Stand sichern, Ursache nennen; keine Budgetänderung oder Retryserie. Lange Emulatorläufe sparsam prüfen.

## Spätere Produktarbeit und Abschluss

Nach geklärter Import-/Quellenbasis: neutrale Auswertung, neue UI/Einrichtung/GNSS-Konzeption mit etwa Start, Kalender, Auswertung, Einstellungen. Keine Spesen-, Lohn-, Heimkehr-, Heimatstandort- oder PDF-Abrechnungsphase.

Matrix-Meilenstein abschließen; keine größere Produktimplementierung in derselben Dokumentationssitzung beginnen. Status/Übergabe ersetzt, keine Checkpoint-Chronik angehängt. Nächster Startsatz: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
