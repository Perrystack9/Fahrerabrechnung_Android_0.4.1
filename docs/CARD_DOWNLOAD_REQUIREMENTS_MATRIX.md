# Fahrerkarte - Pflichtdateien, Ausnahmen und Signaturen

**DETAIL-AKTIV, Primärabgleich 13.10.2026; Codeabgleich auf main `632e0dfa020adb1d190d76b9591b046c0332f58c`, Produktbasis 40123 / Schema 19.** Die 0504-/0505-Snapshotlücke ist durch PR #70 mit eigener CI-/Gerätebestätigung geschlossen; Dateisignaturen bleiben noch unimplementiert. Diese Matrix-/Planaktualisierung ändert keine Kartenbefehle, Datenbank oder Hardwarefreigabe. Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Geprüfte Formatbasis

Die vollständigen PDFs wurden erneut abgerufen; bildbasierte MF-/DF-Tabellen und Download-Tags zusätzlich als Seitenbilder gelesen. Seitenangaben **K** beziehen sich auf die PDF-Seiten der konsolidierten englischen Fassung `02016R0799`, **21** auf die JRC-Fassung 2021/1228. Die Konsolidierung ist ein Dokumentationswerkzeug; die verknüpften Amtsblatttexte bleiben die authentischen Änderungsbelege. Entwürfe sind keine geltenden Formatänderungen.

| Beleg | Geprüfte Stellen / Bedeutung |
|---|---|
| [2016/799, JRC-Amtsblattfassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf) | Appendix 2, 7, 11; Ausgangsformat, spätere Änderungen beachten |
| [2018/502, JRC-Amtsblattfassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf) | Änderungen von TCS121/123/124, DDP037/038/046 und Appendix 11; insbesondere implizites Hashverfahren |
| [2021/1228, JRC-Amtsblattfassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf) | Anhang Nr. 30(c): TCS152/154, V2-Dateien und SC5, 21 S. 45-49; Nr. 34(c): DDP035, 21 S. 80-81 |
| [Konsolidierung 21.08.2023, amtlicher RSA-Spiegel](https://www.rsa.ie/docs/default-source/services/commercial-vehicle-legislation/commission-implementing-regulation-%28eu%29-2016799-implementing-regulation-%28eu%29-no-165-2014.pdf?sfvrsn=bf7dd79c_1) / [EUR-Lex-Kennung](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02016R0799-20230821) | Einschließlich 2018/502, 2020/158, 2021/1228, 2023/980 und Berichtigung; konkret geprüfte Stellen unten |
| [BSI TR-03111 v2.00, 28.06.2012](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03111/BSI-TR-03111_V-2-0_pdf.pdf?__blob=publicationFile&v=1) | Genau die in Appendix 11 Part B §7.1 referenzierte Ausgabe; §5.2.1: feste Plain-Signatur `R || S` |

Abruf-Fingerprints (SHA-256 der vollständigen PDFs, keine privaten Kartenquellen):

| PDF | SHA-256 |
|---|---|
| 2016/799 | `7bc8a9f42bbac789e8aa5bcebec028d017d477a7d212db82e0b83170bc732f05` |
| 2018/502 | `eb804bb2997ccb673405330efa6e7ee80fa06834387682ad6242b7cb374d6704` |
| 2021/1228 | `58811d8069e3d019d8293c8c0bcbd5f470c89a9dca9608e59be5f51639fdf870` |
| Konsolidierung / RSA | `3986fb1c3420743a8921733418d9380e3f7f7f7bb08ee86d4fc349cb60504576` |
| TR-03111 v2.00 | `f39a69a5c54b07712a76e1f31bd275e26f59d7ad67b7a54f9c3e198e6d9e2509` |

## Anwendung und Herkunft

| Kürzel | Anwendung / Primärstelle | Exakte vorhandene Bindung |
|---|---|---|
| MF | Gemeinsamer Master File; Appendix 2 TCS142-147, K S. 278-279 | `readMaster` vor AID; `processApplications` speichert `CardMasterSourceEntity` und `CardBatchMasterFileEntity` am Parent |
| G1 | DF Tachograph, AID `FF544143484F`, DF-FID `0500`; TCS37, TCS148/150, K S. 244, 279-281 | Eigener GEN1-Child, dessen `runId` und `sessionId`; auf Gen2-Karten weiterhin separat |
| G2 | DF Tachograph_G2, AID `FF534D524454`; TCS37, TCS140/152/154, K S. 244, 277, 282-285 | Eigener GEN2-Child mit Generation GEN2 oder GEN2V2; **kein dritter V2-AID** |

Die Matrix prüft **Codepfade**, keine private Datenbank. „Vorhanden“ bedeutet einen implementierten erfolgreichen Aufnahme-/Speicherpfad, nicht eine Zusage für jede Datei jeder historischen Benutzersitzung. Deduplizierte Originale bleiben über die konkrete Sitzung referenziert; jüngere Quellen dürfen fehlende alte Quellen nicht ersetzen.

Reader **R**: [DriverCardReader.readApplications/readSelected](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCard.kt). Bindung **B**: [DriverCardImportPipeline.processApplications/decodeBatch](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCardImportPipeline.kt), [DriverCardImportRepository.save](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCardImportRepository.kt), [CardBatchRequests](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardBatchRequests.kt). Jede DF-Quelle unten gehört zum **jeweiligen Child-sessionId**, MF zum Parent-batchId.

| Speicherkürzel | Tatsächlicher Originalpfad hinter R/B |
|---|---|
| I | [CardIdentitySourceReader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardIdentitySourceReader.kt) -> [CardIdentitySourceStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardIdentitySourceStore.kt), vollständige EF-Bytes und `DriverImportMetadataSourceEntity` |
| C | [CardCertificateReader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCertificateReader.kt) -> [CardCertificateStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCertificateStore.kt), vollständige EF-Bytes, eigener Zertifikatskind und Sitzungsbeleg |
| M | [CardMetadataStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardMetadataStore.kt), vollständige EF-Bytes und Sitzungsbeleg |
| A | [CardActivityImportRepository](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardActivityImportRepository.kt), fachliche Ringprojektion; historisch keine eigenständige vollständige EF-Quelle. Neue Rohquelle separat über O |
| V | [CardVehicleImportRepository](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardVehicleImportRepository.kt), fachliche Recordprojektion; neue Rohquelle unabhängig über O |
| O | [CardCoreSnapshotStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSnapshotStore.kt), vollständiger unveränderlicher EF0504-/EF0505-Blob einschließlich Pointer/unbenutzter Bytes, eigener Kind-/Fingerprint-/Child-Sitzungsbeleg; auch bei fachlichem EMPTY/INVALID |
| F | Im jeweiligen unten benannten `Card...Store`, vollständige EF-Bytes und eigene Sitzungsreferenz; Speicherung über B |

**P** = ausdrücklich je Sitzung / Fahrerkarte vorgeschrieben nach DDP035. **D** = vom allgemeinen Satz über andere Anwendungsdaten erfasst, nicht zusätzlich im expliziten Mindestkatalog genannt; daraus folgt keine belegte pauschale Weglassfreigabe. **S1/S2** = zusätzliche echte Dateisignatur nach dem Protokoll unten, in der App für **alle** so markierten Zeilen noch fehlend. „Ohne“ bedeutet keine zusätzliche Download-Dateisignatur, nicht dass ein Zertifikat selbst unsigniert wäre.

## Gemeinsame MF-Dateien

Die Dateipflicht im Kartenformat ist von der Pflicht im Downloadpaket zu unterscheiden. DDP035 nennt hier nur ICC/IC als optional; MF kann nicht mit dem DF-Dateihashverfahren signiert werden (TCS118-119).

| MF-EF / FID | Generation / Formatpflicht | Standarddownload / Signatur | Vorhandene Originalquelle / Lücke | Primärstelle |
|---|---|---|---|---|
| ICC `0002` | Gen1/Gen2/V2, 25 Bytes | Optional / ohne | R.readMaster -> B Parent, vollständige Bytes | TCS142/144, DDP035/037; K S. 278, 350, 352 |
| IC `0005` | Gen1/Gen2/V2, 8 Bytes | Optional / ohne | R.readMaster -> B Parent, vollständige Bytes | TCS142/144, DDP035/037; K S. 278, 350, 352 |
| DIR `2F00` | Gen2/V2 vorgeschrieben, 20 Bytes; keine entsprechende Gen1-Pflicht aus dieser Gen2-MF-Tabelle ableiten | Kein ausdrücklicher DDP035-Paketbestandteil / keine DF-Signatur | [CardDirectoryReader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardDirectory.kt) -> B Parent; Versuch auch bei Gen1, Fehlschlag beweist keine Gen1-Pflicht | TCS140/142-145; K S. 277-278 |
| ATR/INFO `2F01` | Gen2/V2, **falls ATR extended length anzeigt**, 7-128 Bytes | Kein ausdrücklicher DDP035-Paketbestandteil / keine DF-Signatur | [CardCommunicationReader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCommunication.kt) -> B Parent; nur bei erkannter Anzeige versucht; inneres `7F66`-Format offen | TCS146, ISO 7816-4:2013 §12.7.1; K S. 278 |
| Extended_Length `0006` | Gen2/V2, gleiche ATR-Bedingung, 3 Bytes | Kein ausdrücklicher DDP035-Paketbestandteil / keine DF-Signatur | CardCommunicationReader -> B Parent; `02 01 xx`, unbekannter Code bleibt unbekannt | TCS147; K S. 278-279 |

## DF Tachograph / G1

Alle folgenden EFs gehören zur G1-Anwendung sowohl einer Gen1- als auch einer Gen2-/V2-Karte. FIDs und Format: **TCS148/150, K S. 279-281**; Downloadpflicht: **DDP035, K S. 350 / 21 S. 80**. Die Spalte „Quelle“ nennt R/B plus den konkreten Speicherpfad; die beiden gemeinsamen MF-Quellen werden hier nicht dupliziert.

| G1-EF / FID | Pflicht / Ausnahme | Dateisignatur | Vorhandene Quelle / zusätzliche Lücke |
|---|---|---|---|
| Application_Identification `0501` | P | S1 | I, vollständige 10 Bytes |
| Card_Certificate `C100` | P | Ohne | C, 194 Bytes; keine geprüfte Kartenkette |
| CA_Certificate `C108` | P | Ohne | C, 194 Bytes; keine geprüfte Kartenkette |
| Identification `0520` | P | S1 | I, vollständige 143 Bytes; eigene Identitätsbindung |
| Card_Download `050E` | Vom signierten Download-Dateisatz ausdrücklich ausgenommen; LastCardDownload zusätzlich aktualisieren | Keine vorgeschriebene Download-Signatur | M, 4 Bytes, **nur gelesen**; bewusst ausgelöster Schreibpfad fehlt |
| Driving_Licence_Info `0521` | D | S1 bei Download | M, vollständige 53 Bytes |
| Events_Data `0502` | P | S1 | F: [CardEventFaultStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardEventFaultStore.kt), EVENTS |
| Faults_Data `0503` | P | S1 | F: CardEventFaultStore, FAULTS |
| Driver_Activity_Data `0504` | P | S1 | O + A; vollständiger unabhängiger neuer Snapshot, Nullring mit Nullpointern als gültig unbenutzt; historische Quellen nicht ergänzt |
| Vehicles_Used `0505` | P | S1 | O + V; neuer vollständiger Snapshot unabhängig von EMPTY/INVALID-Projektion; unbekannter unbenutzter Pointer bleibt INVALID |
| Places `0506` | P | S1 | F: [CardPlacesStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardPlacesStore.kt) |
| Current_Usage `0507` | D | S1 bei Download | M, vollständige 19 Bytes; offenes Nutzungsende nicht ergänzen |
| Control_Activity_Data `0508` | P | S1 | F: [CardControlStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardControlStore.kt) |
| Specific_Conditions `0522` | P | S1 | F: [CardSpecificConditionStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardSpecificConditionStore.kt) |

## DF Tachograph_G2 / G2 und Gen2V2

FIDs/Format: **TCS152/154, K S. 282-285 / 21 S. 45-48**. Downloadpflicht: **DDP035, K S. 350-351 / 21 S. 80-81**. G2 bedeutet hier Gen2 **und** Gen2V2; die sieben V2-Zeilen existieren nur im V2-Format. Für die gesamte G2-Downloadgruppe nennt DDP035 genau eine Ausnahme: Fahrerkarte **in einer VU**, Kontrolle durch **Nicht-EU-Kontrollbehörde**, **Gen1-Kontrollkarte**. Diese Kombination gilt nicht für den direkten eigenen USB-Import; keine allgemeine Gen2-Weglassfreigabe.

| G2-EF / FID | Generation | Pflicht / Ausnahme | Dateisignatur | Vorhandene Quelle / zusätzliche Lücke |
|---|---|---|---|---|
| Application_Identification `0501` | G2 | P | S2 | I, vollständiges aktuelles 17-Byte-Format; alte kürzere Belege nicht ergänzen |
| CardMAcertificate `C100` | G2 | Authentisierungszertifikat im Format; kein expliziter DDP035-Mindestbestandteil | Keine zusätzliche Dateisignatur hier belegt | C; **ersetzt nicht CardSignCertificate** |
| CardSignCertificate `C101` | G2 | P | Ohne | C; dessen Card_Sign-Kurve bestimmt Download-Hash und Signaturlänge |
| CA_Certificate `C108` | G2 | P | Ohne | C; Aussteller-/Rollen-/Kettenprüfung noch offen |
| Link_Certificate `C109` | G2 | P im DDP035-Downloadkatalog | Ohne | C; Rollover/gegebenenfalls unpersonalisierter Inhalt separat prüfen, kein Blankwert als Vertrauen |
| Identification `0520` | G2 | P | S2 | I, vollständige 143 Bytes |
| Card_Download `050E` | G2 | Aus signiertem Dateisatz ausgenommen; LastCardDownload in beiden DFs zusätzlich aktualisieren | Keine vorgeschriebene Download-Signatur | M, 4 Bytes, nur gelesen; Schreib-/Teilfehlervorgang fehlt |
| Driving_Licence_Info `0521` | G2 | D | S2 bei Download | M, vollständige 53 Bytes |
| Events_Data `0502` | G2 | P | S2 | F: CardEventFaultStore, EVENTS |
| Faults_Data `0503` | G2 | P | S2 | F: CardEventFaultStore, FAULTS |
| Driver_Activity_Data `0504` | G2 | P | S2 | O + A; neuer unabhängiger vollständiger Snapshot / belegter unbenutzter Fall; exakter GEN2-/GEN2V2-Child |
| Vehicles_Used `0505` | G2 | P | S2 | O + V; neuer vollständiger Snapshot auch bei EMPTY/INVALID; exakte Generation/Child-Sitzung |
| Places `0506` | G2 | P | S2 | F: CardPlacesStore |
| Current_Usage `0507` | G2 | D | S2 bei Download | M, vollständige 19 Bytes |
| Control_Activity_Data `0508` | G2 | P | S2 | F: CardControlStore |
| Specific_Conditions `0522` | G2 | P | S2 | F: CardSpecificConditionStore |
| VehicleUnits_Used `0523` | G2 | P | S2 | F: [CardVehicleUnitsStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardVehicleUnitsStore.kt) |
| GNSS_Places `0524` | G2 | P | S2 | F: [CardGnssStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardGnssStore.kt) |
| Application_Identification_V2 `0525` | Nur V2 | P **wenn vorhanden** | S2 | I, vollständige 10 Bytes; fehlender Kapazitätsbeleg verhindert abhängige Leseversuche |
| Places_Authentication `0526` | Nur V2 | P **wenn vorhanden** | S2 | F: [CardPlacesStore.save](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardPlacesStore.kt), AUTHENTICATION / eigener Sitzungsbeleg |
| GNSS_Places_Authentication `0527` | Nur V2 | P **wenn vorhanden** | S2 | F: [CardGnssStore.save](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardGnssStore.kt), AUTHENTICATION / eigener Sitzungsbeleg |
| Border_Crossings `0528` | Nur V2 | P **wenn vorhanden** | S2 | F: [CardBorderCrossingsStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardBorderCrossingsStore.kt) |
| Load_Unload_Operations `0529` | Nur V2 | P **wenn vorhanden** | S2 | F: [CardLoadUnloadStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardLoadUnloadStore.kt) |
| Load_Type_Entries `0530` | Nur V2 | P **wenn vorhanden** | S2 | F: [CardLoadTypeStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardLoadTypeStore.kt) |
| VU_Configuration `0540` | Nur V2 | D, nicht im expliziten Mindestkatalog; keine ausdrückliche DDP035-Ausnahme wie für Card_Download | S2-Regel für Anwendungsdaten; konkreter zulässiger Downloadumfang/-weg noch offen | **Nicht implementiert / nicht versucht**; Lesen benötigt SC5, Select SC1; legitimer Authentisierungs-/Secure-Messaging-Weg fehlt |

SC5 bedeutet für gerades READ BINARY `SM-C-MAC-G2 AND SM-R-ENC-MAC-G2`; ungerades READ ist NEV (TCS152). Das Format allein erlaubt keine VU-Identität, Schlüssel oder geschützten Leseversuche. Die Sensor_Installation_Data-Ausnahme betrifft Werkstattkarten, keine hier erfundene Fahrer-EF.

## Originalsignatur und genaue Bytebindung

DDP038 (K S. 352-353, in 2018 geändert) verlangt je signierter EF: **SELECT EF -> PERFORM HASH OF FILE -> vollständiges READ BINARY -> PSO: COMPUTE DIGITAL SIGNATURE -> Daten und Signatur zusammen speichern**. Die dokumentierte SFID-Variante darf vor dem Hash selektieren/lesen; sie rechtfertigt keine Vermischung verschiedener Snapshots.

| Vorgang | Verifiziertes Format / Fehler | Stand 40123 |
|---|---|---|
| Datei-Hash | Appendix 2 §3.5.13, TCS118-125, K S. 271-273: `80 2A 90 00`, kein Datenfeld; **P2=00 für beide DFs**, Algorithmus implizit | Kein Reader-/Transportbefehl für dieses Verfahren |
| Signaturabruf | §3.5.14, TCS126-131, K S. 273-274: `00 2A 9E 9A Le`; privater Schlüssel in Karte implizit bekannt | Keine echte Aufnahme, kein unveränderlicher Signaturbeleg |
| Hashlebensdauer | TCS121: neuer Datei-Hash, DF-Auswahl oder Reset löschen temporären Hash | Kein bewiesener Hash-/Read-/Signaturzustandsautomat; EF-Wechsel nicht als zusätzlich belegten Löschgrund behaupten, dennoch Fehlbindung ausschließen |
| G1 / S1 | Appendix 11 Part A CSM004/033-035, K S. 430, 443: RSA, PKCS#1 v1.5 über SHA-1; **128 Bytes**; Appendix 1 §2.149, K S. 182 | Aufnahme/Prüfung für Benutzerkarte fehlt; nicht mit RSA-Nachrichtenrückgewinnung der Gen1-Zertifikate verwechseln |
| G2/V2 / S2 | Part B CSM48-50/233/235, K S. 448-449, 499-500: ECDSA Plain, Hash aus **Card_Sign**, nicht aus Ausstellerkurve oder Card_MA | Aufnahme/Prüfung fehlt; weder generisches PSO:HASH noch freie Hashwahl verwenden |
| Fehler | Hash: `6985` unzulässige EF, `6400/6581` Korruption, `6986` keine transparente aktuelle EF; Signatur: `6400/6581` Schlüsselkorruption, `6985` kein temporärer Datei-Hash | SW, Transportabbruch und „nicht versucht“ unverwechselbar erfassen; keine erfundene Signatur bei Teilfehler |
| Bytebindung | DDP040, K S. 354: vollständige Datei-/Bitreihenfolge erhalten; Karte, Anwendung/AID, EF, Sitzung, Originalbytes, Signatur und tatsächlicher Algorithmus zusammenbinden | Neue 0504-/0505-Originalbytes vorhanden; Signaturbeleg fehlt. Lokale SHA-256-Fingerprints sind **keine** Karten-Dateisignaturen; keine Signatur nachträglich an ältere Importbytes hängen |

Die folgenden Längen sind aus CSM48/50/233 und TR-03111 v2.00 §5.2.1 abgeleitet: `R || S`, je Integer `ceil(log256(n))` Bytes für die Ordnung n. Appendix 1 §2.149 gibt für G2 den Bereich 64-132 Bytes vor. Eine Zertifikatssignatur richtet sich dagegen nach dem **Ausstellerschlüssel** (CSM150); beide Fälle unterscheiden.

| Card_Sign-Kurve | Download-Hash | Plain-Signatur / Le |
|---|---|---|
| NIST P-256 / BrainpoolP256r1 | SHA-256 | 64 Bytes / `40` |
| NIST P-384 / BrainpoolP384r1 | SHA-384 | 96 Bytes / `60` |
| BrainpoolP512r1 | SHA-512 | 128 Bytes / `80` |
| NIST P-521 | SHA-512 | 132 Bytes / `84` |

Eine passende Länge oder strukturell gelesene Kurve beweist keine Echtheit. CSM231-236 verlangen getrennte Zertifikats-/Rollen-/Gültigkeits-/Wurzelprüfung; die öffentliche ERCA-Forschung autorisiert keine Benutzerquelle.

## Standarddownload zusätzlich

| Vorgang | Beleg / Bedingung | Stand und erforderliche Grenze |
|---|---|---|
| Vollständige Sitzung | DDP036, K S. 351: Reset bis Deaktivierung; DDP035 beide Anwendungssätze auf Gen2/V2 mit enger Kontrollausnahme | Gemeinsamer Leseimport vorhanden, kein vollständig belegter Standarddownload |
| LastCardDownload | DDP035, K S. 351 / 21 S. 81; TCS148/150 und TCS152/154: EF050E in G1 und gegebenenfalls G2 aktualisieren, 4-Byte-TimeReal | Automatischer Import bleibt lesend. Erst getrennt bewusst ausgelöster UPDATE-BINARY-/Fehler-/Hardwarepfad; keine atomare Kartenänderung über zwei DFs behaupten |
| Teilfehler beim Datum | Zwei unabhängige DF-Schreibvorgänge, mögliche Kartenentnahme / Transportfehler | Jeden tatsächlichen Versuch und Erfolg getrennt belegen; ein erfolgreiches Datum ist kein Nachweis aller Dateien/Signaturen. Kein stiller Retry und keine pauschale „Download erfolgreich“-Anzeige |
| ESM-/DDD-Paket | DDP040-046, K S. 354-355: ein Sitzungsfile, TLV mit 2-Byte-Länge, G1/MF Daten `FID||00`, G1 Signatur `FID||01`, G2 Daten `FID||02`, G2 Signatur `FID||03`; Signatur direkt hinter Daten | Kein Standardexport implementiert. Nicht heruntergeladene Dateien ohne Tag/Null-Längenobjekt; ein vorhandener gültig leerer EF ist nicht „nicht heruntergeladen“ |
| Extra Originalquellen | MF DIR/ATR/Extended_Length, G2 CardMAcertificate und vorhandene read-only EF050E-Snapshots | Exakten Originalquellenexport separat benennen; nicht als vollständiges DDD-Paket ausgeben oder automatisch neue TLV-Konventionen erfinden |

## Geschlossene Snapshotlücke und begrenzter Folge-PR

1. **Alle S1/S2-Dateisignaturen fehlen weiterhin.** Zertifikatsbytes sind vorhanden, produktive Vertrauenskette und Originalquellen-/Standardexport fehlen. Der [begrenzte Signaturaufnahmeplan](CARD_CORE_SIGNATURE_CAPTURE_PLAN.md) ist jetzt dokumentiert; noch keine Befehle implementiert.
2. **Neue 0504/0505 sind als vollständige Originale abgesichert.** PR #70 / 40123 speichert unabhängige defensive READ-BINARY-Blobs vor fachlichem Parsen. 0504 mit Nullpointern und ganzem Nullring ist gültig unbenutzt; 0505-Originale bleiben auch bei EMPTY oder unklarem Pointer/INVALID erhalten. Historische Projektionen sind keine nachträglich ergänzten Originalbelege. Eigene CI und Geräteumfang stehen im [40123-Beleg](DEVICE_CONFIRMATION_40123.md).
3. **Fehlend / unlesbar / nicht versucht nicht überall vollständig differenziert.** Für neue Core-EFs bestehen ausdrückliche Zustände inklusive Kapazitätsausfall, EF-Abwesenheit und Readerfehler. Andere `readSelected`-Quellen fassen weiterhin teilweise Parse-/Lese-/Kapazitätsfälle zusammen; bedingte MF-/V2-Versuche bleiben begrenzt. Kein vollständiger universeller Statusautomat behauptet.
4. **0540 und inneres 7F66 bleiben gesperrt.** Weder ein zusätzlicher Dateiname noch die äußere TLV-Struktur liefert den fehlenden legitimen Sicherheitsweg bzw. ISO-Primärabschnitt.

**Nächster begrenzter Implementierungs-PR:** echte Signaturaufnahme nur für **neu gemeinsam gelesene 0504 und 0505** in beiden Anwendungen. Reihenfolge, Card_Sign-Policy, defensive Vollständigkeitsprüfung, eigene Sitzungsbelege/Schema 19→20, Fehler-/Abbruch-/Historienprüfungen und eigenes CI-/Gerätegate sind im Plan konkret festgelegt. Passende Länge und Aufnahme sind kein mathematischer oder vertrauenswürdiger Signaturnachweis. Keine weiteren EFs, Vertrauenskette, DDD-/Export-/Card_Download-Schreib-/UI-Umbauphase gleichzeitig beginnen.

**Matrix-Abnahme:** 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen gegen TCS142/148/152 und DDP035 abgeglichen; G2-Mindestliste, V2-Bedingungen, Signatur-/Schreib-/Exportvorgänge und tatsächliche Speicherlücken getrennt. Die explizit offenen Punkte sind keine Vollständigkeitsbehauptung und blockieren einen vollständig behaupteten Standarddownload.
