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

**Aktiver nächster Auftrag:** Tobi hat 0502/0503 am 13.10.2026 freigegeben. [Begrenzter Plan](CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md), Draft [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79), 40126 / Schema 22; Implementierung in eigener CI, kein neuer bestätigter Produktmerge. Aktuellen PR-Kopf und CI live prüfen, echter Room-Export, eigenes Paket und neue Geräteabnahme erforderlich. Andere Optionen unten bleiben getrennte, nicht automatisch freigegebene Pakete.

## Danach weiterhin getrennte Pakete

| Paket | Voraussetzung / Grenze |
|---|---|
| Weitere EF-Signaturen | 0504/0505 und der begrenzte 40125-Identifikationsumfang sind CI-/im eigenen Umfang gerätebestätigt; weitere EFs erst anhand eigener Originalpfade separat planen |
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Aussteller-/Signaturkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; zwei DF-Schreibversuche nur im getrennten bewusst ausgelösten geprüften Fehler-/Hardwarepfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; Rohbytes/äußere Struktur erhalten |
| Geschützte `0540` | Legitimer SC5-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität |

Weitere Primär-/Forschungsdetails erst für das betreffende Paket öffnen. Produktbereiche ausschließlich nach CURRENT_RULES.md; keine Spesen-/Lohn-/Heimatstandort-/Abrechnungsphase.
