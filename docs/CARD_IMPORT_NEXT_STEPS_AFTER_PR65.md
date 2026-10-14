# Kartenimport - nächster überprüfbarer Schritt

**DETAIL-AKTIV.** Einziger Fachtext im Startpfad. Gemeinsamer Import beider Anwendungen aus PR #65 und vollständige neue Originale aus PR #70 / 40123 / Schema 19 werden übernommen. Aktuelle Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Abgeschlossener Matrix-Meilenstein

Die [Pflichtdateien-/Ausnahme-/Signaturmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) ist anhand Appendix 2, 7 und 11 einschließlich der relevanten Änderungen erarbeitet: 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen, tatsächliche Quellenpfade, bedingte Ausnahmen, Befehlsfolge, Card_Sign-Hash-/Längenwahl, offene Speicher-/Zugriffs-/Vertrauensgrenzen. Kein Reader-/DB-/Hardwarestand dadurch verändert. Weitere Fachtexte nur bei konkretem Bedarf.

## Abgeschlossen: vollständige 0504-/0505-Originale

**PR #70 / 40123 / Schema 19 gemergt und im eigenen Geräteumfang bestätigt.** Produktkopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`, [volle CI Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2), [gezieltes Gerätepaket](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875), Gerätebestätigung 13.10.2026 09:04 Europe/Berlin. Produktmerge `e783ad364b1692d8633c5e4b8846112adb256be6` besitzt denselben Tree. [Genauer Gerätebeleg](DEVICE_CONFIRMATION_40123.md): GEN1 13780/6202 und GEN2V2 13780/9602 Bytes, Kernprüfungen problemfrei; optionale Zusatzfälle nicht separat bestätigt.

Rohsnapshots einschließlich Pointer/unbenutzter Bytes vor Parser, auch bei EMPTY/INVALID-Auswertung; nicht versucht/EF abwesend/tatsächlicher Lesefehler getrennt. Nullring mit Nullpointern ist belegte unbenutzte 0504, unbekannter unbenutzter Fahrzeugpointer bleibt INVALID mit Original. Exakte Karte/Anwendung/neue Child-Sitzung; MF am Parent. Keine Migration, Rekonstruktion, Backfill oder jüngerer Ersatz für alte Quellen. Nichtleere Projektionen, Normalisierung und Request-Fingerprints erhalten. Keine neuen Hash-/Signatur-/Schreib-/Vertrauensbefehle oder Navigation in diesem Paket.

## Abgeschlossen: eigene 0504-/0505-Kartensignaturen

Der [begrenzte Plan](CARD_CORE_SIGNATURE_CAPTURE_PLAN.md) ist mit **PR #73 / 40124 / Schema 20** umgesetzt und übernommen: Merge `3298b707525a4f33162c835ce31deaf181d2e772`, identischer Tree `3540b26b775d17c9bdc6335549dafbfbd58bcd92` zum Produktkopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`. Eigene volle [CI 38381962283](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38381962283) und exaktes [Paket 38388192092](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38388192092) erfolgreich. Tobi bestätigt sämtliche eigenen Gerätefälle am 13.10.2026 13:57 Europe/Berlin; [genauer Beleg](DEVICE_CONFIRMATION_40124.md).

SELECT→HASH(P2=00)→vollständiges READ→defensive Dateigrenze→SIGN, Policy aus frisch gelesenem C101/Card_Sign, eigene verschlüsselte Sitzung/FKs, additive 19→20 und kleine Statusanzeige. Originale bleiben bei lokalen Signaturfehlern erhalten; Transportverlust verwirft die laufende Gruppe. Alte Imports bleiben ohne neue Signaturen. Room-20-Schema aus echtem Compilerexport; eigene 19→20-Prüfung erhalten. GEN1 beide Signaturen 128 Bytes, GEN2V2 beide 64 Bytes; Aufnahme bleibt ungeprüft.

## Abgeschlossen: Identifikationssignaturen 40125

Der [Identifikationsplan](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md) ist in [PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76) als **40125 / additive 20→21** umgesetzt und nach neuer eigener positiver Geräte-Rückmeldung übernommen. Merge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, identischer Produktbaum `36e014af6cbffa9add766bdd917c6d94313f87ec` zum geprüften Kopf `20d34a0fc7ce370fb4c1d22002905a500574ee06`. Volle CI 38408152663 und exaktes Paket 38414059875 erfolgreich. Tobi, 13.10.2026 17:32 Europe/Berlin: „Wurde getestet, und funktioniert 1a“; [genauer Gerätebeleg und Grenzen](DEVICE_CONFIRMATION_40125.md).

Neu: frische 0501-/0520-/V2-0525-Captures, bytegenauer Bootstrap-Vergleich mit nicht toleriertem Gruppenabbruch, getrennte Child-Receipts, unabhängiger unsigned Fallback und kleine Anzeige. Alte Core-Belege/Originale/Fingerprints und historische 15-Byte-Quellen erhalten. Neue vollständige Reader-/SQLCipher-/Migrations-/Historien-/Compose-Prüfungen; eigenständige 19→20-Abnahme bleibt bestehen. Room21 echt, alte Exporte unverändert; keine CI-Abschwächung oder Workflowänderung.

## Abgeschlossen: Ereignis-/Störungssignaturen 40126

Der [begrenzte 0502/0503-Plan](CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md) ist mit [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79) als **40126 / additive 21→22** umgesetzt und nach neuer eigener Geräteabnahme übernommen. Merge `6334b4f67485feaee4de5d649d6788475af940ec`, identischer vollständiger Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e` zum geprüften Kopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`. Volle CI 38444430958 und exaktes Paket 38448369846 erfolgreich. Tobi am 13.10.2026 20:59 Europe/Berlin nach allen neun Chat-Testpunkten: „Hab alles getestet.“ Screenshot: Events 3168 / Faults 1152 Originalbytes und jeweils 64-Byte-Signatur, ungeprüft; keine sichtbare Anwendungsbezeichnung. [Genauer Beleg](DEVICE_CONFIRMATION_40126.md).

Separate Rohquellen/Child-Receipts, vollständige Bytes auch bei EMPTY/INVALID, frische post-HASH-READs und strikter EOF-Beweis. Lokale HASH-/SIGN-Fehler erhalten unabhängig vollständige unsigned Originale, Transportverlust verwirft die Gruppe. Exakte historische Quellen ohne Backfill, unveränderte alte Maps/Digests/Fingerprints und alle 44 Tabellen von Schema 21 erhalten. Eigene vollständige Reader-/SQLCipher-/Migrations-/Rollback-/Compose-Abnahme; eigenständige 19→20-/20→21-Prüfungen bleiben bestehen. Room22 aus echtem Compilerexport, keine CI-Abschwächung oder Workflowänderung.

## Abgeschlossen: mathematische Core-Verifikation 40127

Der [freigegebene Plan A1–A10](CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md) ist mit [PR #82](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/82) als **40127 / unverändertem Room22** umgesetzt und nach eigener neuer positiver Geräteabnahme übernommen. Merge `96370d0ef291af1ac3893333cd128c2a3f735033`, identischer vollständiger Baum `f818dcb8b1da48cdad7cdc555bcc7638c478b819` zum endgültigen CI-/Gerätekopf `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc`. Volle CI [38515830418](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38515830418) und exaktes Paket [38530132281](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38530132281), jeweils Versuch1, erfolgreich. Tobi14.10.2026 10:12 Europe/Berlin: „Das passt alles 👍“ nach den verständlichen abschließenden Chat-Testpunkten. Sichtbar: GEN1 und GEN2V2, Tätigkeiten/Fahrzeuge jeweils mathematisch gültig; Zertifikatsvertrauen nicht geprüft. [Genauer Beleg und Grenzen](DEVICE_CONFIRMATION_40127.md).

Reine abgeleitete Leseprojektion aus exakten historischen Originalen/Child-Receipts. Gen1-RSA-Recovery und getrennte strikte PKCS1/SHA1-Dateiprüfung; sechs feste Gen2/V2-ECDSA-Domänen. Quellen-/Snapshot-/Cancellation-/Auswahl-/Archivschutz, alle alten Fingerprints/Aufnahmezustände, DB-Zeilen/Room22 und bisherigen Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Andere EF-Signaturen unverifiziert, Vertrauen stets NOT_EVALUATED. Keine automatische weitere Produktfreigabe.

## Aktueller Planungsauftrag: gespeicherte Dateisignaturen vollständig prüfen

Tobi14.10.2026 10:48 Europe/Berlin: mathematische Verifikation aller bereits gespeicherten Kartensignaturen abschließen. [Kurzer Plan A1–A8](CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), **Implementierung erst nach Freigabe**. Vorhandene0501/0520/0502/0503-Receipts für GEN1/GEN2/GEN2V2 und0525 nurV2 anbinden;40127-Engine/0504/0505 und eigene Herkunft wiederverwenden, Room22/Aufnahme/Fingerprints/alle Gates erhalten. Keine neue Signaturaufnahme. Nach neuer eigener positiver Abnahme diesen mathematischen Umfang schließen; anschließend Benutzeroberfläche und vereinbarte Funktionen für1.0 planen.

## Danach weiterhin getrennte Pakete

| Paket | Voraussetzung / Grenze |
|---|---|
| Mathematische Prüfung vorhandener Identity-/Event-/Fault-Receipts | Aktueller Abschlussplan oben; Umsetzung wartet auf Freigabe. Andere nicht gespeicherte EF-Signaturen erweitern diesen Auftrag nicht. |
| Weitere EF-Signaturen | 0504/0505, der begrenzte 40125-Identifikationsumfang und 0502/0503 sind CI-/im eigenen Umfang gerätebestätigt; weitere EFs anhand der vorhandenen Matrix und eigener Originalpfade separat planen |
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Aussteller-/Signaturkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; zwei DF-Schreibversuche nur im getrennten bewusst ausgelösten geprüften Fehler-/Hardwarepfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; Rohbytes/äußere Struktur erhalten |
| Geschützte `0540` | Legitimer SC5-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität |

Weitere Primär-/Forschungsdetails erst für das betreffende Paket öffnen. Produktbereiche ausschließlich nach CURRENT_RULES.md; keine Spesen-/Lohn-/Heimatstandort-/Abrechnungsphase.
