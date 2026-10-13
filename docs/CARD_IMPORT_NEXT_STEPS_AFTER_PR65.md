# Kartenimport - nächster überprüfbarer Schritt

**DETAIL-AKTIV.** Einziger Fachtext im Startpfad für die nächste Importarbeit. PR #65 ist abgeschlossen; 40122/Schema 19 und der gemeinsame Import beider Anwendungen werden übernommen. Aktuelle Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Zuerst: Pflichtdateien-/Ausnahme-/Signaturmatrix

Eine kompakte Matrix für **Gen1, Gen2 und Gen2V2** anhand Appendix 7 DDP035, Appendix 2 §3.5.13/3.5.14 und Appendix 11 einschließlich der anwendbaren Änderungen erarbeiten. Keine bloße Liste der bisher implementierten Quellen als vollständigen Downloadumfang ausgeben.

| Spalte je Datei/Vorgang | Erforderliche Aussage |
|---|---|
| MF/DF, AID, Generation, EF | Gemeinsame MF-Belege und die beiden konkreten Child-Anwendungen eindeutig zuordnen |
| Pflicht / bedingte Ausnahme | Primärstelle und Bedingung nennen; ungeprüfte Fälle ausdrücklich offen lassen |
| Vorhandene Originalquelle | Tatsächlichen Reader-/Speicherpfad und genaue Sitzung referenzieren |
| Originalsignatur / Hashverfahren | Benötigte Befehlsfolge, Bytebindung und belegte Hash-/Längenwahl benennen |
| Fehlend / unlesbar / nicht versucht | Unterschiedliche Zustände erhalten; kein Backfill oder jüngerer Quellenersatz |
| Standarddownload zusätzlich | LastCardDownload in beiden DFs und Teilfehler als getrennten bewusst ausgelösten Downloadpfad berücksichtigen |

Abnahme: Jede Aussage hat eine verifizierte Primärstelle und den Abgleich zur tatsächlichen Implementierung; fehlende Inhalte und Verfahren sind sichtbar. Die Matrix selbst verändert keinen Reader, keine DB und keine Hardwarefreigabe. Erst daraus einen begrenzten nächsten Implementierungs-PR ableiten.

## Details erst bei Bedarf

- [Vorhandene Quellenabdeckung](CARD_SOURCE_COVERAGE.md): Ausgangsinventar, keine vollständige Pflichtdateienliste.
- [Signatur-/Vertrauens- und Downloadgrenzen](CARD_IMPORT_SECURITY_RESEARCH.md): bisheriger Primärabgleich; benötigte Originaltexte erneut verfügbar machen und konkret prüfen.
- [Gen1-Forschungsbeleg](CARD_GEN1_PUBLIC_CERTIFICATE_AUDIT.md): echte öffentliche Mathematik, keine App-Kartenverifikation.

## Nachgelagerte getrennte Pakete und Sperren

| Paket | Voraussetzung / Grenze |
|---|---|
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| Echte Signaturaufnahme | Reset-/AID-/EF-Wechsel, Fehler/Abbruch und Appendix-11-Regeln belegen; Originalbytes, Signatur, Anwendung und Sitzung unveränderlich zusammenbinden; eigene CI/Geräteprüfung |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Ausstellerkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; Schreiben erst in getrenntem geprüften Downloadpfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; bis dahin äußere Struktur und Originalbytes, keine geratenen Puffergrößen oder Transportänderung |
| Geschützte `0540` | Legitimer SC5-/Authentisierungs-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität erfinden |

Relevante Produkt-/Reader-/Parser-/Persistenz-/Schema-/Sicherheitsänderungen benötigen den eigenen vollständigen Prüf- und erforderlichen Gerätestand. Keine neue Produktarbeit im Dokumentations-Cleanup.
