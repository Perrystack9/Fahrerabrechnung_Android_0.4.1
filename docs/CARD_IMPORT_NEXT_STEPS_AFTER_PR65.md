# Kartenimport - nächster überprüfbarer Schritt

**DETAIL-AKTIV.** Einziger Fachtext im Startpfad. PR #65 / 40122 / Schema 19 und der gemeinsame Import beider Anwendungen werden übernommen. Aktuelle Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Abgeschlossener Matrix-Meilenstein

Die [Pflichtdateien-/Ausnahme-/Signaturmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) ist anhand Appendix 2, 7 und 11 einschließlich der relevanten Änderungen erarbeitet: 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen, tatsächliche Quellenpfade, bedingte Ausnahmen, Befehlsfolge, Card_Sign-Hash-/Längenwahl, offene Speicher-/Zugriffs-/Vertrauensgrenzen. Kein Reader-/DB-/Hardwarestand dadurch verändert. Weitere Fachtexte nur bei konkretem Bedarf.

## Zuerst: vollständige 0504-/0505-Originalsnapshots

Der nächste begrenzte Produkt-PR soll **Driver_Activity_Data 0504 und Vehicles_Used 0505 vollständig und unabhängig vom fachlichen Parsergebnis** erfassen und unveränderlich speichern. Das schafft die notwendige Bytebasis vor echter Signaturaufnahme.

| Befund in 40122 | Erforderlicher begrenzter Schritt |
|---|---|
| Pointerwerte plus Ring-/Recordbytes gespeichert, kein eigener vollständiger EF-Blob | Originalbytes einschließlich Pointerbytes erhalten; vorhandene fachliche Projektionen weiter nutzen |
| Vehicles_Used: gültig leer erhält EMPTY, `usableRead` entfernt Datei aus der Speicherung | Vollständigen vorhandenen Rohbeleg auch ohne Nutzungsrecord an der neuen Sitzung erhalten |
| Driver_Activity_Data: Parser erwartet gültige Tageskette; unbenutzter Ring nicht als gültig leer abgesichert | Default-/Pointerformat gezielt prüfen; Rohaufnahme erhalten, „gültig leer“ nur bei belegtem Format |
| Einige Parse-/Lese-/Kapazitätsfälle werden in null/readFailed zusammengefasst | Für diese beiden EFs Rohaufnahme, Parsergebnis und tatsächlichen Leseversuch getrennt belegen |

Abnahme:

- Eigene Karte, EF, konkrete G1-/G2-Anwendung und neue Child-Sitzung binden; Gen2V2 bleibt im G2-AID, MF nur am Parent.
- Vollständige Originalbytes, Leer-/Fehler-/Abbruchfälle und exakte spätere Quellenauswahl prüfen. Normalisierte Tätigkeiten/Fahrzeuge bleiben fachliche Darstellung, kein Ersatz für Originale.
- Bestehende Daten, Requests/Fingerprints und Historie erhalten; keine rückwirkende Rekonstruktion/Backfill oder jüngerer Quellenersatz.
- Rohbeleg mit Parsefehler nicht als fachlich vollständig/gültig präsentieren; unbekannt und nicht versucht nicht erraten. Atomare Persistenz erhalten.
- Nötige additive Migration mit echtem Compiler-Schemaexport; vollständige unveränderte Android-CI und **eigene erforderliche Gerätebestätigung vor Merge**. Keine Produktfreigabe aus dem Dokumentations-PR.

Keine Hash-/Signatur-/Schreibbefehle, kein Vertrauensmodul, Standardexport oder UI-Umbau in diesem Paket. Weitere EF-Zustände erst in einem separat begrenzten Folgepaket behandeln.

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
