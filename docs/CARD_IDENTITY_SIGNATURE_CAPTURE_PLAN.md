# Weitere Kartensignaturen - begrenzter Identifikationsplan

**DETAIL-BELEG, ursprünglicher Plan vom 13.10.2026.** Planbasis main `71eac7163e53148952b09639c1fcd203a2a7b8a4`, damaliges 40124/Schema 20. Die begrenzte Umsetzung **40125 / additive 20→21** ist in PR #76 abgeschlossen, vollständig CI-geprüft, mit exaktem Paket ausgeliefert und nach neuer eigener positiver Geräte-Rückmeldung übernommen. [40125-Gerätebeleg](DEVICE_CONFIRMATION_40125.md), aktueller Status/Übergabe maßgeblich. Die folgenden Format-/Konsistenz-/Speicher-/Abnahmevorgaben dokumentieren den ursprünglichen Plan; sie eröffnen keinen neuen Implementierungsauftrag. Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Ziel und genau erster Umfang

Neue signierte Leseaufnahmen der bereits unterstützten Identifikations-EFs. Die [Primärmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) ordnet 0501/0520 beiden Anwendungs-Mindestlisten zu; 0525 ist auf V2 bedingt vorhanden. Diese kleine Gruppe nutzt bereits gespeicherte vollständige Originalquellen und vermeidet weitere variable Ringformate. Kein vollständiger Kartendownload wird daraus abgeleitet.

| EF | Anwendung | Neues Original / positive Aufnahme | Bestehender Quellkind |
|---|---|---|---|
| Application_Identification `0501` | GEN1 | 10 Bytes | `IDENTITY_SOURCE_APPLICATION` |
| Identification `0520` | GEN1 | 143 Bytes | `IDENTITY_SOURCE_IDENTIFICATION` |
| Application_Identification `0501` | GEN2/GEN2V2 | Aktuelles vollständiges Format 17 Bytes; bereits unterstützte echte 15-Byte-Variante nur bei GEN2 und strikt bewiesenem Dateiende. Keine 16-Byte- oder aufgefüllte Quelle | `IDENTITY_SOURCE_APPLICATION` |
| Identification `0520` | GEN2/GEN2V2 | 143 Bytes | `IDENTITY_SOURCE_IDENTIFICATION` |
| Application_Identification_V2 `0525` | Nur GEN2V2, wenn vorhanden | 10 Bytes; bestehendes Format mit 8 folgenden Bytes. Vollständige belegte Blankdatei bleibt von fehlend getrennt | `IDENTITY_SOURCE_APPLICATION_V2` |

Bei Tobis üblicher GEN1+GEN2V2-Karte sind das **fünf zusätzliche Belege**, zusammen mit den vier bestätigten 0504/0505-Signaturen **neun**. Reine GEN1 oder GEN2 haben keine 0525-Signatur. Bestehende historische 15-Byte-Quellen, einschließlich bereits akzeptierter Altformate, bleiben unverändert lesbar; daraus keine aktuelle V2-Signaturfreigabe ableiten. Eine neue GEN2V2-0501-Quelle mit 15 Bytes bleibt als bestehende unsigned Quelle erhalten und erhält ausdrücklich keine behauptete vollständige aktuelle 17-Byte-Signaturaufnahme.

Nicht in diesem Produkt-PR: 0521/0507, Ereignisse/Störungen, Orte/Kontrollen/Sonderbedingungen, VU/GNSS und weitere V2-Dateien, zusätzliche Zertifikats-/MF-Dateisignaturen, mathematische Prüfung, Vertrauenskette, DDD-/Originalexport, Card_Download-Schreiben, 0540/Secure Messaging oder 7F66-Decoder. Das sind verschobene Umfänge, keine pauschale Standard-Weglassfreigabe. Kein Navigations-/Onboarding-Umbau.

## Primär- und Codebasis

- Appendix 2 TCS148/150 und TCS152/154, Appendix 7 DDP035/038/040 sowie 2021/1228 Anhang Nr. 30(c)/34(c): Zuordnung, Dateilängen und bedingte V2-Präsenz aus der bestehenden Primärmatrix. Deren vollständiger Primärabgleich und PDF-Fingerprints bleiben die dokumentierte Formatbasis.
- [2018/502, amtliche JRC-Fassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf), PDF S. 29 und 43: erneut gezielt gelesene TCS121/123/124 und geänderte DDP038-Sequenz. P2=00, Hash aus Card_Sign, keine freie Hashwahl; SELECT→HASH→vollständiges READ→SIGN. Die zusätzlichen EOF- und Konsistenzprüfungen unten sind lokale Annahmegrenzen.
- [DriverCardReader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCard.kt): Vorablesungen 0501/0525/0520 vor Zertifikaten; Core-Signaturen nach Zertifikaten; spätes Einfrieren derselben Erkennungsbytes. Der bisherige `freezeComplete` ist **keine** signierte neue READ-Aufnahme.
- [CardIdentitySourceParser/Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardIdentitySourceParser.kt), [Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardIdentitySourceStore.kt): feste vollständige Längen, bestehende 15/17-Kompatibilität, eigene Kind-/Generations-/Karten-Fingerprints und konkrete Child-Sitzungsquelle. Diese Herkunft nicht ersetzen.
- [Core-Reader](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureReader.kt), [Policy/Modell](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignature.kt), [Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureStore.kt) und [DAO/FKs](../data/src/main/kotlin/de/fahrerabrechnung/data/db/CardCoreSignatureDao.kt): wiederverwendbare Befehls-/Policyregeln, aber Core-Enum, Decoder und Receipt-Fingerprint ausdrücklich nur für 0504/0505. Keine neuen Identitätskinds in alte Core-Rows einschleusen.

## Aufnahme und Konsistenz

1. Bestehende serialisierte powered Session und gemeinsame MF-Aufnahme erhalten. Pro DF zunächst Erkennung, Generation und eigene Identität durch die bisherigen zulässigen Vorablesungen bestimmen. Diese defensiv kopieren; sie sind noch **keine Signaturquellen**. Fremde/unklare Karten behandeln weiterhin die bestehenden Profilregeln.
2. Die in dieser Anwendung tatsächlich gelesenen Zertifikatsbytes vor jeder neuen Hashsequenz einfrieren. GEN2/V2 braucht ein strukturell passendes neues C101/Card_Sign derselben Anwendung/Generation für Policy und echte Länge; C100/Card_MA oder die Ausstellerkurve sind kein Ersatz. GEN1 bleibt RSA/SHA-1, 128 Bytes wie 40124; C100 nur referenzieren, wenn tatsächlich in dieser Child-Sitzung gelesen. Kein erfundener Zertifikatsbeleg und kein Vertrauen aus dessen bloßer Anwesenheit.
3. Je unterstütztem Ziel deterministisch 0501, 0520, gegebenenfalls 0525: SELECT EF→PERFORM HASH OF FILE(P2=00)→**neues READ ab Offset 0 bis zur vollständigen Länge**→ein Byte am Endoffset mit `correctLength=false`→PSO: COMPUTE DIGITAL SIGNATURE mit echter Le. Zwischen HASH und SIGN keine andere EF/DF, Zertifikatslesung, weiterer Hash, Reset oder fremde APDU. Das bestehende GET RESPONSE für die laufende Antwort ist keine neue EF-Operation; HASH/SIGN/EOF erhalten keinen automatischen Le-Retry. Der Core-Import folgt weiterhin mit eigenen vollständigen Sequenzen.
4. **Positive EOF-Grenze** unverändert streng wie beim geprüften Core-Signaturpfad: datenlose 6700 oder 6B00. Kein 6Cxx/6C00-Retry, leeres 9000 oder 6282 als positiver Signatur-Grenzbeleg. Ein zusätzliches Byte beweist einen unvollständigen Präfix; diesen nicht als neues vollständiges signiertes Original speichern. Aus früheren unsigned Grenzantworten keine neue Signaturfreigabe ableiten.
5. Lesen/Vollständigkeit unabhängig von HASH/SIGN bewerten. Eine lokale Hash-/Signierstörung darf ein nachweislich vollständiges neues, passendes unsigned Original erhalten. Eine fehlgeschlagene/neue unvollständige READ-Aufnahme bekommt keinen Original-FK im Signaturbeleg; eine unabhängig bereits vollständig bewiesene Vorablesung darf nur als bisherige unsigned Quelle erhalten bleiben. Niemals die Vorablesung mit einer späteren Signatur versehen, um einen fehlenden neuen READ zu verdecken.
6. Jede tatsächlich vollständig neu gelesene Identifikationsdatei muss **bytegenau** ihrer vollständigen Vorablesung entsprechen; 0520 zusätzlich gegen dieselbe erkannte Karten-/Inhaberidentität validieren. Neue 0501/0525-Versionen oder Kapazitäten dürfen den Bootstrap nicht still ändern. Bei Abweichung **gesamte laufende Gruppe abbrechen**, auch nach erfolgreicher erster Anwendung. Kein normal tolerierter IO/MALFORMED_RESPONSE-Pfad, der nur einen Child als fehlgeschlagen vermerkt; eigener nicht verschluckter Konsistenzabbruch und gezielter Gruppen-Test erforderlich. Keine Profilbindung oder Bestätigungsoption für widersprüchliche Bytes innerhalb derselben Aufnahme.
7. Neue vollständige Capture-Bytes sind die tatsächliche neue gespeicherte Identitätsquelle; der spätere bisherige `freezeComplete` darf sie nicht mit Erkennungsbytes ersetzen. Ein unsigned Fallback bleibt sichtbar ohne zugeordnete erfolgreiche Signatur. Eine positive neue Identitätssignatur setzt das bestehende unterstützte Originalformat voraus: unbekannte V2-followingLength oder nicht parsebare Identifikation nicht als gültige/leer signierte Quelle ausgeben; eine strukturell vollständig unterstützte Blank-V2-Quelle bleibt zulässig. Fachlich gültige Kapazitäten nur aus der zur Gruppe passenden Quelle verwenden; ungültige Werte erlauben keine erfundenen abhängigen Längen/Einträge.
8. Erst passende Originale und konkrete Receipts gemeinsam defensiv einfrieren, danach Parser/Fingerprints/Bestätigung/Persistenz. Einzelanwendungs- und Mehr-Anwendungs-Pfad müssen denselben Schutz besitzen. Buffer-Mutation oder spätere Bestätigung darf kein wartendes Capture verändern.

| Lage | Neuer Signaturbeleg / gespeicherte Herkunft |
|---|---|
| Vollständige passende neue Aufnahme, gültige Länge/Status | aufgenommen · ungeprüft; Original- und gegebenenfalls Zertifikats-FK an derselben Child-Sitzung |
| 0525 für GEN1/reine GEN2 nicht vorgesehen | kein Versuch/keine erfundene Quelle; als für diese Anwendung nicht vorgesehen erkennbar |
| Unterstützte EF fehlt, keine vollständige neue Aufnahme | ausdrücklich fehlend/fehlgeschlagen; kein Original-FK und kein positives Signaturfeld |
| C101 fehlt/defekt/unbekannte Policy oder neues nicht freigegebenes Layout | nicht versucht mit konkretem Grund; zulässige unsigned Originalquelle erhalten |
| Lokaler HASH-/SIGN-Fehler oder Antwort ohne echte SW | fehlgeschlagen, tatsächliche Phase/Grund/Failure; SW nur wenn tatsächlich beantwortet; passendes vollständiges unsigned Original erhalten |
| Vorablesung und neues Original widersprechen sich | ganze Gruppe abbrechen; keine neuen Parent-/Child-/Profil-/Quellen-/Receipt-Rows |
| Timeout, Kartenentnahme, Disconnect oder Cancellation | ganze laufende Gruppe abbrechen; keine Teilgruppe, kein alter Hashzustand bei Wiederaufnahme |

## Modelle und additive Speicherung

**In40125/Schema 21 umgesetzte Planvorgabe.** Der ursprüngliche Plan-Meilenstein selbst enthielt keinen Schemaexport. Eigenes `CardIdentitySignature`-/Capture-Modell und `identitySignatures`-Map in Read/Saved-Ergebnis; bestehende Core-Map bleibt auf zwei EFs begrenzt. Policy, Status, Phasen und zulässige Befehle aus 40124 wiederverwenden; sofern eine gemeinsame Sequenz-Hilfsfunktion extrahiert wird, Verhalten und komplette Core-Tests/Belege erhalten. Kein universeller EF-/Signatur-/Trust-Umbau.

Additive Tabelle `card_identity_signature_receipts` mit `(sessionId, kind)` als Schlüssel. `kind` entspricht exakt dem bestehenden `IDENTITY_SOURCE_*`-Quellkind. Felder wie beim bewiesenen Receipt-Vertrag: Fahrerkarte, Generation/Anwendung, optionaler tatsächlicher Original- und Zertifikatsverweis, Status/Policy/Phase/Grund, echte SWs/Failure/Länge, Signaturbytes und eigener Fingerprint. FK zu `(sessionId,driverCardId)` und zum jeweiligen `(sessionId,driverCardId,kind,sourceId)`-Original bzw. Zertifikat. Die dafür nötigen kombinierten Quellenindizes bestehen seit Schema 20.

Vor `insert` und beim historischen `load` Kind/FID, Generation, Anwendung, Karten-/Child-Sitzung, vollständige Originalbytes, tatsächlichen Zertifikatskind/Policy, Länge/Status und Receipt-Fingerprint validieren. Fremde Karten/Sitzungen oder globale „neueste Quelle“ ablehnen. Die alte `card_core_signature_receipts`-Tabelle, Core-Kindstrings und `card-core-signature-receipt-v1` bleiben unverändert. Deduplizierte Rohblobs sind zulässig; positive Signaturbelege entstehen ausschließlich für den neuen tatsächlichen Aufnahmevorgang.

Migration **20→21 rein additiv**, in den verschlüsselten Produktions- und Testpfaden registrieren. Kein Backfill, keine destructive fallback migration, kein Klartext. Room-21-JSON ausschließlich aus dem echten Compiler übernehmen; Exporte 1–20 unverändert. Alte Identitätsquellen und bestehende Core-Receipts, Profile/Bindungen, Import-Rows und Request-Fingerprints bytegenau erhalten. Leere neue Map bedeutet keinen zusätzlichen Fingerprint-Tag und keinen geänderten alten Abdeckungsbericht. Wiederverwendung alter Request-/Batch-ID mit neuen Identitätssignaturen muss abgelehnt werden; vollständige neue Gruppen bleiben atomar.

## Kleine Abnahmeanzeige

Bestehenden ausgewählten Import, GEN1/GEN2-Auswahl und „Fahrerkarten-Details“/`CardIdentitySourceScreen` nutzen. Bei „Kartenanwendung“, „Karteninhaber und Gültigkeit“, „Kartenanwendung V2“ lediglich Originalgröße sowie Signaturstatus/-länge/gegebenenfalls Grund ergänzen. Alte Imports: „Signatur in diesem Import nicht aufgenommen“. Nicht vorgesehene V2-Quelle nicht als defekter GEN1-Import anzeigen. Alte vier Core-Signaturen weiterhin sichtbar.

Maskierung/Kartennummer nach EF-/Sitzungs-/Anwendungs-/Altarchivwechsel zurücksetzen. Keine Hash-/Signier-/Speichern-Knöpfe, APDU-/Roh-/Schlüsseldumps oder Vertrauenssymbole. „Aufgenommen“ bleibt „ungeprüft“.

## Erforderliche automatisierte Abnahme

| Bereich | Konkrete Fälle |
|---|---|
| Befehle und neue Bytes | Alle fünf normalen Anwendungs-/EF-Zuordnungen; exakte frische SELECT/HASH/READ/EOF/SIGN-Reihenfolge, kein intervenierender Kartenbefehl, unterschiedliche Vorablesung/READ-Buffer statt nachträglicher Vorablesungssignatur |
| Bootstrap-Konsistenz | Differenzen in Identität, Generation, Strukturversion oder Kapazitäten; auch zweite Anwendung nach erfolgreicher erster: gesamte Gruppe verworfen, keine neue Bindung/Teilgruppe |
| Format und Grenze | G1 10/143, G2 17/143, reine G2 mit vollständig belegten 15 Bytes; 16-Byte-Präfix, V2-15-Byte-Quelle, Zusatzbyte, kurze READs, leeres 9000, 6282, 6C00/6Cxx und Antwortdaten zu Fehler-SW; kein positiver Beleg aus unbewiesener Grenze |
| V2-Bedingung | Kein 0525-Versuch auf GEN1/reiner GEN2; fehlt auf V2 vs vollständige Blankquelle vs unbekannte followingLength; keine erfundenen Kapazitäten oder Quellen |
| Policy/Signaturfehler | GEN1 128; alle sechs bestehenden G2-Kurven/Le; C101 vor Aufnahme, falsche Rolle/Generation/Curve, C100 statt C101, falsche Länge, Blank-Signatur, Hashdaten/Fehler-SW, Signierfehler, Fehler ohne SW |
| Transport/Freeze | Kartenentnahme/Timeout/Disconnect/Cancellation in jeder Sequenzphase und Wechsel zwischen DFs; beide Reader-APIs; defensive Input-/Output-/wartende Bestätigungskopien, keine concurrent powered Session |
| Herkunft/SQLCipher | Exakte gleiche Child-Original-/Zertifikats-FKs, keine MF-Signaturquelle, keine fremde Karte/Sitzung; Quelle dedupliziert aber neuer eigener Receipt; Fingerprint-/Byte-/Status-Tamper und Null-FK nach fehlgeschlagener neuer READ |
| Atomarer Rollback | Fehler beim neuen Receipt der zweiten Anwendung verwirft neue Profilbindung, Parent, beide Children, Originale/Zertifikate, alte Core- und neue Identitätssignaturen der neuen Gruppe; vorherige erfolgreiche Imports bleiben unverändert |
| Migration/Historie | Echte verschlüsselte 20→21-Datenbasis mit alten Core-Signaturen/Identitätsquellen, Reopen, kein Backfill; historische 18-/19→21-Pfade mit korrekt rekonstruierten alten Schemas erhalten, keine bloße Versions- oder Assertion-Abschwächung; alte Request-Fingerprints und ID-Wiederholungen |
| Compose | Aktuelle/alte Sitzung, Anwendungen, alle drei EF-Auswahlen und Altarchiv, Fehlstatus/ungeprüft/Länge; Kartennummer wieder verborgen; bestehende Core-Anzeige erhalten; keine Quelle aus der vorherigen Auswahl |

Danach **unveränderte volle Android-CI**: sieben Gradle-Aufrufe, Unit/Lint/APK-/Signatur-/Updatepaar-/Compiler-Schemaabgleich, gesamte SQLCipher-/Compose-Suiten auf API36 und Abschlussgate. Keine Testfilter/Quarantäne, keine leichteren Regeln für Produktcode. Lokaler Downloadblock ist kein Testnachweis; echter Compiler/CI erforderlich.

## Eigene Geräteabnahme und Abschluss des Folge-PRs

[Checkliste 40125](DEVICE_TEST_40125.md), neue positive Geräteabnahme und anschließender Merge im [Beleg](DEVICE_CONFIRMATION_40125.md) dokumentiert. Ursprüngliche Voraussetzung: Erst vollständig grüner endgültiger Produktkopf, dann exaktes Paket über bestehenden Workflow; `include_baseline=false` für vorhandene 40124-Installation. Infrastruktur- und APK-Quellkopf getrennt dokumentieren.

Erforderlich: Update/Altbestand, neuer eigener Import mit fünf neuen Identitätssignaturen plus vier weiter funktionierenden Core-Signaturen, gleiche Anwendungs-/Identitätsangaben und Originalgrößen, Reopen ohne Leser, EF-/Anwendungs-/Historien-/Altarchivwechsel, erneuter Import sowie Kartenentnahme mit erfolgreichem Neustart. Tatsächliche Quellgrößen/Längen festhalten; GEN2/V2-Länge aus realem C101, keine pauschale 64-Byte-Annahme. Neue problemfreie Rückmeldung gilt nur für tatsächlich getestete Karte/Hardware, nicht für alle Kurven/ältere Gen2-Layouts oder mathematisches Vertrauen.

**Plan und Folgeumsetzung 40125 sind abgeschlossen.** Produktkopf, vollständige CI, exaktes Paket, neue positive eigene Geräte-Rückmeldung und identischer Produktmerge sind im Beleg gesichert. Keine Trust-/DDD-/Schreib-/UI-Folgephase automatisch beginnen; nächste Arbeit nur gemäß aktuellem Status/Übergabe und ausdrücklich ausgewähltem Auftrag.
