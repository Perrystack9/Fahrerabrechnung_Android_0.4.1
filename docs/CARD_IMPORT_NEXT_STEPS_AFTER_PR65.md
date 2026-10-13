# Kartenimport - nächster überprüfbarer Schritt

**DETAIL-AKTIV.** Einziger Fachtext im Startpfad. PR #65 / 40122 / Schema 19 und der gemeinsame Import beider Anwendungen werden übernommen. Aktuelle Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Abgeschlossener Matrix-Meilenstein

Die [Pflichtdateien-/Ausnahme-/Signaturmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) ist anhand Appendix 2, 7 und 11 einschließlich der relevanten Änderungen erarbeitet: 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen, tatsächliche Quellenpfade, bedingte Ausnahmen, Befehlsfolge, Card_Sign-Hash-/Längenwahl, offene Speicher-/Zugriffs-/Vertrauensgrenzen. Kein Reader-/DB-/Hardwarestand dadurch verändert. Weitere Fachtexte nur bei konkretem Bedarf.

## Jetzt: Snapshot-PR 40123 prüfen und am Gerät abnehmen

Im Draft-[PR #70](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/70), Branch `feat/card-ef-snapshots-20261013` sind **vollständige unabhängige EF-Originale für Driver_Activity_Data 0504 und Vehicles_Used 0505** implementiert. Grundlage ist main nach PR #69, `5b1b9669af8614ec502d027a4c71d94ab81496ad`. Die frühere Matrix beschreibt den Ausgangsbefund 40122; sie ist kein neuer Produktnachweis.

| Ausgangslücke 40122 | Implementierter Kandidat 40123 |
|---|---|
| Nur Pointerwerte und Ring-/Recordbytes | `CardCoreSnapshot` erhält READ-BINARY-Originale einschließlich Pointer und unbenutzter Bytes, mit defensiven Kopien |
| Leere Fahrzeugdatei ohne Original | Eigener Rohbeleg STORED; fachlicher Bestand EMPTY; kein erfundener Record |
| Unbenutzter Aktivitätsring nicht abgesichert | Belegtes Defaultformat: beide Pointer null und gesamter Ring null (Appendix 2 TCS150/154, Konsolidierung S. 281/284); fachlich EMPTY, Rohbeleg gespeichert |
| Rohaufnahme und Parse-/Lesefehler vermischt | Eigene Original-Dateiergebnisse plus getrennte fachliche Ergebnisse; NOT_ATTEMPTED bei fehlender Kapazität, MISSING bei EF-Abwesenheit, READ_FAILED mit tatsächlicher Fehlerart, INVALID bei Parserfehler trotz vollständigem Original |
| Gefahr nachträglicher Quellenzuordnung | Exakte Quellreferenz an neuer Child-Sitzung, getrennte Generation; MF bleibt Parent. Kein Backfill, kein neuerer Ersatz für historische Quellen |

Bestehende SQLCipher-/Schema-19-Tabellen reichen; keine Migration oder selbst geschriebenen Schemaexporte. Nichtleere Projektionen, Normalisierung, alte Requests/Fingerprints und Altarchiv bleiben erhalten. Geänderte unbenutzte Bytes schaffen einen neuen Originalbeleg, nicht neue normalisierte Tätigkeiten. Transportabbruch/Entfernen/Timeout im regulären Mehranwendungsimport liefert keine speicherbare Teilgruppe. Normale neue Importe können weiterhin partiell sein, weil dieses Paket nur zwei EFs verbessert.

**Abnahme noch offen:** vollständige unveränderte Android-CI für endgültigen Kopf, anschließend [eigene Geräteabnahme 40123](CARD_CORE_SNAPSHOT_DEVICE_TEST.md). Lokal nur Projektgrenzen/Guard-/Diff-Prüfung möglich; Gradle-Distribution wegen eingeschränktem Netzwerk unerreichbar. CI/PR live prüfen. Bestehenden gezielten Geräte-Workflow erst nach vollständigem CI-Nachweis benutzen; erfolgreiche Normal-CI-Uploads bleiben aus. Gerätebasis 40122 gilt nicht für 40123; bis zur eigenen Bestätigung kein Produktmerge.

Neue automatisierte Prüfungen decken genaue Originalbytes, unbenutzte/ungültige/fehlende/abgebrochene Fälle, getrennte G1-/G2V2-Sitzungen, FK/Korruption, atomare Rollbacks, Reopen sowie historische Auswahl ab. Keine neuen Hash-/Signatur-/Schreib-/Vertrauensbefehle, Standardexporte oder UI-/Onboarding-Neukonzeption. Weitere EF-Zustände separat behandeln.

## Danach weiterhin getrennte Pakete

| Paket | Voraussetzung / Grenze |
|---|---|
| Echte Signaturaufnahme | Nach vollständiger Bytebasis: DDP038, TCS118-131 und Appendix 11 umsetzen; P2=00, Card_Sign-Hash und echte Länge; eigene Byte-/Abbruch-/CI-/Geräteprüfung |
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Aussteller-/Signaturkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; zwei DF-Schreibversuche nur im getrennten bewusst ausgelösten geprüften Fehler-/Hardwarepfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; Rohbytes/äußere Struktur erhalten |
| Geschützte `0540` | Legitimer SC5-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität |

Weitere Primär-/Forschungsdetails erst für das betreffende Paket öffnen. Produktbereiche ausschließlich nach CURRENT_RULES.md; keine Spesen-/Lohn-/Heimatstandort-/Abrechnungsphase.
