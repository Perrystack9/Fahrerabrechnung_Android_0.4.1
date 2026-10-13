# Kartenimport - nächster überprüfbarer Schritt

**DETAIL-AKTIV.** Einziger Fachtext im Startpfad. Gemeinsamer Import beider Anwendungen aus PR #65 und vollständige neue Originale aus PR #70 / 40123 / Schema 19 werden übernommen. Aktuelle Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Abgeschlossener Matrix-Meilenstein

Die [Pflichtdateien-/Ausnahme-/Signaturmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) ist anhand Appendix 2, 7 und 11 einschließlich der relevanten Änderungen erarbeitet: 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen, tatsächliche Quellenpfade, bedingte Ausnahmen, Befehlsfolge, Card_Sign-Hash-/Längenwahl, offene Speicher-/Zugriffs-/Vertrauensgrenzen. Kein Reader-/DB-/Hardwarestand dadurch verändert. Weitere Fachtexte nur bei konkretem Bedarf.

## Abgeschlossen: vollständige 0504-/0505-Originale

**PR #70 / 40123 / Schema 19 gemergt und im eigenen Geräteumfang bestätigt.** Produktkopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`, [volle CI Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2), [gezieltes Gerätepaket](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875), Gerätebestätigung 13.10.2026 09:04 Europe/Berlin. Produktmerge `e783ad364b1692d8633c5e4b8846112adb256be6` besitzt denselben Tree. [Genauer Gerätebeleg](DEVICE_CONFIRMATION_40123.md): GEN1 13780/6202 und GEN2V2 13780/9602 Bytes, Kernprüfungen problemfrei; optionale Zusatzfälle nicht separat bestätigt.

Rohsnapshots einschließlich Pointer/unbenutzter Bytes vor Parser, auch bei EMPTY/INVALID-Auswertung; nicht versucht/EF abwesend/tatsächlicher Lesefehler getrennt. Nullring mit Nullpointern ist belegte unbenutzte 0504, unbekannter unbenutzter Fahrzeugpointer bleibt INVALID mit Original. Exakte Karte/Anwendung/neue Child-Sitzung; MF am Parent. Keine Migration, Rekonstruktion, Backfill oder jüngerer Ersatz für alte Quellen. Nichtleere Projektionen, Normalisierung und Request-Fingerprints erhalten. Keine neuen Hash-/Signatur-/Schreib-/Vertrauensbefehle oder Navigation in diesem Paket.

## Plan abgeschlossen: jetzt getrennten Signaturaufnahme-PR umsetzen

Der [konkrete 0504-/0505-Signaturaufnahmeplan](CARD_CORE_SIGNATURE_CAPTURE_PLAN.md) ist auf live geprüftem main 632e0df ausgearbeitet. Produkt-Main-CI 38360671532 und Docs-Main-CI 38361629250 erfolgreich. Vier neue echte Signaturbelege auf der vorhandenen Dual-Anwendungskarte sind der geplante erste Geräteumfang; keine Signatur für weitere EFs behaupten.

DDP038/TCS118-131 und Appendix11 gezielt geprüft: SELECT→HASH(P2=00)→vollständiges READ→SIGN; GEN2-Hash/Länge aus frisch gelesenem C101/Card_Sign, nicht aus Card_MA/Aussteller. Zusätzliche defensive Dateiende-Prüfung, getrennte Fehlerzustände und unsigned Rohimport bei fehlender Voraussetzung festgelegt. Signaturen ausschließlich an die gemeinsam neu aufgenommenen Originale/Karte/AID/Child-Sitzung; keine historischen Ergänzungen.

Als Nächstes im getrennten Produkt-Meilenstein zuerst Modelle/Command-Factories/Sequenztests umsetzen, dann eigene Sitzungsbelege mit additiver Schema 19→20-Migration, kleine Statusanzeige und vollständige Byte-/Negativ-/Abbruch-/Transaktions-/Historien-/Compose-Prüfungen. Erst eigene volle CI und neue passende Gerätebestätigung erlauben Merge. 40124/Schema 20 sind **geplant, noch nicht gebaut**; 40123 gibt keine spätere Signatur-/Vertrauens-/DDD-Funktion frei.

## Danach weiterhin getrennte Pakete

| Paket | Voraussetzung / Grenze |
|---|---|
| Weitere EF-Signaturen | Erst begrenzte 0504/0505-Aufnahme selbst CI-/geräteprüfen; dann zusätzlichen tatsächlichen EF-Umfang mit gleicher Byte-/Sitzungsbindung planen |
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Aussteller-/Signaturkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; zwei DF-Schreibversuche nur im getrennten bewusst ausgelösten geprüften Fehler-/Hardwarepfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; Rohbytes/äußere Struktur erhalten |
| Geschützte `0540` | Legitimer SC5-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität |

Weitere Primär-/Forschungsdetails erst für das betreffende Paket öffnen. Produktbereiche ausschließlich nach CURRENT_RULES.md; keine Spesen-/Lohn-/Heimatstandort-/Abrechnungsphase.
