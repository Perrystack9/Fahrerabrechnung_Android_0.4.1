# Abdeckung des vorhandenen Fahrerkarte-Imports

**DETAIL-AKTIV - bei Quellenarbeit lesen.** Implementiertes Inventar auf 40122/Schema 19, keine vollständige Download-Pflichtdateienliste. Geräteumfang nur gemäß [WORK_STATUS.md](../WORK_STATUS.md); eine gespeicherte Quelle beweist keine kryptografische Echtheit.

| Quelldatei | EF | Generation | Stand |
|---|---|---|---|
| Driver_Activity_Data | `0504` | Gen1/Gen2/Gen2V2 | eigene nichtleere Sitzungsquelle: Pointerwerte/Ringbytes; unabhängiger vollständiger EF-Snapshot und unbenutzter Fall offen |
| Vehicles_Used | `0505` | Gen1/Gen2/Gen2V2 | eigene nichtleere Sitzungsquelle: Pointerwert/Recordbytes; EMPTY derzeit ohne gespeicherten Originalsnapshot |
| Control_Activity_Data | `0508` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Events_Data | `0502` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Faults_Data | `0503` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Specific_Conditions | `0522` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| GNSS_Places | `0524` | Gen2/Gen2V2 | eigene Sitzungsquelle |
| GNSS_Places_Authentication | `0527` | Gen2V2 | eigene getrennte Sitzungsquelle |
| Places | `0506` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Places_Authentication | `0526` | Gen2V2 | eigene getrennte Sitzungsquelle |
| Border_Crossings | `0528` | Gen2V2 | eigene Sitzungsquelle |
| Load_Unload_Operations | `0529` | Gen2V2 | eigene Sitzungsquelle |
| Load_Type_Entries | `0530` | Gen2V2 | eigene Sitzungsquelle |
| VehicleUnits_Used | `0523` | Gen2/Gen2V2 | eigene Sitzungsquelle; unabhängiger Zähler |
| Driving_Licence_Info | `0521` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle,53Byte; Nummer zunächst verborgen |
| Current_Usage | `0507` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle,19Byte; historischer Kartenstand |
| Card_Download | `050E` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle,4Byte; ausschließlich lesen |
| ICC-Identifikation | `0002` unter MF | Gen1/Gen2/Gen2V2 | eigene MF-Quelle,25Byte; vor AID, Serienwert verborgen |
| IC-Identifikation | `0005` unter MF | Gen1/Gen2/Gen2V2 | eigene MF-Quelle,8Originalbytes; vor AID, Kennung verborgen |
| Karten-/CA-Zertifikat | `C100`/`C108` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle, vollständige unabhängige Originalquelle; struktureller Decoder |
| Signatur-/Link-Zertifikat | `C101`/`C109` | Gen2/Gen2V2 | eigene Sitzungsquelle, eigene Originalquellen; keine Kryptoprüfung |
| Application_Identification | `0501` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle; vollständige Originalbytes10/15/17 |
| Application_Identification_V2 | `0525` | Gen2V2 | eigene Sitzungsquelle; vollständige Originalbytes10 |
| Identification | `0520` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle; vollständige143Bytes, exakte Identitätsprüfung |
| MF DIR | `2F00` | Gen2/Gen2V2 | gemeinsame MF-Quelle; exakt20Bytes mit unabhängiger Dateiendprüfung, genaue Karte/Sitzung |
| MF ATR/INFO | `2F01` | Gen2/Gen2V2, bedingt | gemeinsame MF-Quelle; vollständige7–128Originalbytes und äußere `7F66`-Struktur, keine Puffergrößeninterpretation |
| MF Extended_Length | `0006` | Gen2/Gen2V2, bedingt | gemeinsame MF-Quelle; exakt3Bytes, T=0/T=1-Angaben aus der Datei, unbekannte Codes bleiben unbekannt |

## Herkunft

Gen1-/Gen2-DFs werden derselben physischen Karte zugeordnet, als getrennte Anwendungs-/Sitzungsquellen atomar gespeichert und exakt ausgewählt. Gen2V2 ist ein Gen2-Format. Gemeinsame MF-Belege einmal am Parent. Alte Quellen/Requests/Imports nicht ergänzen; beide Zeitsätze nicht addieren. Fehlend, unlesbar, nicht versucht und gültig leer unterscheiden. Einzelne EF-Fehler sind keine Speicher-Rollback-Freigabe.

## Noch offen

| Bereich | Grenze |
|---|---|
| Download-Pflichtdateien-/Signaturmatrix | [Primärmatrix erarbeitet](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md); dieses Inventar allein beweist keine Downloadvollständigkeit |
| Originalbytes / Dateizustände 0504 und 0505 | Vollständige unabhängige EF-Snapshots einschließlich unbenutzter Dateien als nächsten begrenzten Produkt-PR vorbereiten; historische Daten nicht rekonstruieren |
| ATR/INFO-Inhalt `7F66` | Originalbytes und äußere Struktur vorhanden, vollständiges ISO-7816-4:2013-Inhaltsformat nicht belegt |
| Unbekanntes ATR-Historienformat | Bedingte Dateien nicht versucht; unbekannt bleibt unbekannt |
| Echte Signaturen / Standarddownload | Signatur-/Bytebindung und getrennten Download-/Schreibpfad implementieren und prüfen |
| Zertifikats-/Kartenvertrauen | Vollständige überprüfbare Kette, Rollen, Referenzen und Gültigkeit noch offen |
| `VU_Configuration 0540` | Legitimer SC5-Sicherheits-/Zugriffsweg fehlt |

Nächster Schritt: [Import-Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md). Pflicht-/Ausnahme-/Signaturumfang und nachgewiesene Speichergrenzen: [Primärmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md). Weitere Implementierungsdetails nur bei Bedarf. Gestrichene Produktbereiche werden nicht nach dem Kartenimport umgesetzt.
