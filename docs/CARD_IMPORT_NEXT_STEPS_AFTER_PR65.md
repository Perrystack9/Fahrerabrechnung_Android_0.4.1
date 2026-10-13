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

## Nächster begrenzter Plan fertig: Identifikationssignaturen

Der [konkrete Identifikationsplan](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md) legt den eigenen Folge-Produkt-PR **40125 / additive Migration 20→21** fest: 0501/0520 in GEN1 und GEN2(V2), 0525 auf V2 wenn vorhanden. Vorablesung und neue post-HASH-READ-Aufnahme strikt trennen, bytegenauen Bootstrap-Vergleich und Gruppenabbruch bei widersprüchlichen neuen Identitätsbytes verlangen. Neue eigene Child-Sitzungsbelege neben unveränderten Core-Receipts, keine Altquellen-/Fingerprint-Ergänzung. Kleine Statusanzeige in bestehenden Kartenangaben, vollständige negative/SQLCipher-/Migrations-/Compose-Prüfungen und [neues Gerätegate](DEVICE_TEST_40125.md).

Planbasis main 71eac71 / bestätigtes 40124. Produkt-Main-CI 38394108412 und Docs-Main-CI 38395123764 am Einstieg grün. Der Plan-Meilenstein ändert ausschließlich Dokumentation und endet nach Sicherung; die Umsetzung startet getrennt mit aktuellen main/PR/CI-Prüfungen und gezieltem Öffnen des Plans. Noch keine 40125-APKs, neue Schemaexporte oder Hardwarefreigabe. Keine Trust-/DDD-/Schreib-/UI-Folgephase gleichzeitig beginnen.

## Danach weiterhin getrennte Pakete

| Paket | Voraussetzung / Grenze |
|---|---|
| Weitere EF-Signaturen | 0504/0505 ist CI-/gerätebestätigt; 0501/0520/0525 ist separat geplant, weitere verbleibende EFs danach anhand eigener Originalpfade begrenzen |
| Originalquellenexport | Exakte Parent-/Child-Quellen; ohne erforderliche Originalsignaturen kein vollständiger Standarddownload/DDD |
| ERCA/MSCA/Kartenvertrauen | Rollen, Referenzen, Aussteller-/Signaturkurve, Gültigkeit, Pinning/Rollover und negative Fälle; öffentliche Forschung ersetzt keine Benutzerkartenkette |
| Standarddownload-Datum | Normaler Import bleibt lesend; zwei DF-Schreibversuche nur im getrennten bewusst ausgelösten geprüften Fehler-/Hardwarepfad |
| ATR/INFO `7F66` | Vollständiges ISO-7816-4:2013-Primärformat §12.7.1 fehlt; Rohbytes/äußere Struktur erhalten |
| Geschützte `0540` | Legitimer SC5-/Secure-Messaging-Weg fehlt; keine blinden Leseversuche, Schlüssel oder VU-Identität |

Weitere Primär-/Forschungsdetails erst für das betreffende Paket öffnen. Produktbereiche ausschließlich nach CURRENT_RULES.md; keine Spesen-/Lohn-/Heimatstandort-/Abrechnungsphase.
