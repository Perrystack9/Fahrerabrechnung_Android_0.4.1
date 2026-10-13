# Fahrerkarte - begrenzter Plan für echte Signaturaufnahme

**DETAIL-AKTIV, Plan vom 13.10.2026.** Geprüfte Ausgangsbasis: main `632e0dfa020adb1d190d76b9591b046c0332f58c`, **40123 / Room-Schema 19**. Produkt- und Main-CI grün; eigene Gerätebestätigung gilt für den unveränderten Snapshot-Produktbaum. Der ursprüngliche Plan-Meilenstein PR #72 änderte ausschließlich Dokumentation. **Umsetzungsstand: PR #73 / 40124 / Schema 20 gemergt; eigene volle CI und Geräteabnahme am 13.10.2026 13:57 bestätigt.** [Gerätebeleg](DEVICE_CONFIRMATION_40124.md). Dieser Text bleibt die Abnahmegrundlage; aktuellen Stand in WORK_STATUS/WORK_HANDOFF prüfen. Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

## Ziel und erster Dateiumfang

Ein eigener Produkt-PR nimmt die von der Karte erzeugten Downloadsignaturen zu **EF0504 Driver_Activity_Data und EF0505 Vehicles_Used** auf. Jeweils GEN1 sowie GEN2/GEN2V2 im eigenen AID/Child; V2 erzeugt keinen dritten Anwendungszweig. Die beiden vollständigen Originalblobs sind seit PR #70 unabhängig von der fachlichen Projektion gespeichert. Auch vorhandene unbenutzte oder fachlich INVALID ausgewertete Dateien können signiert werden, wenn ihre vollständige Aufnahme und die Befehlsfolge gesichert sind.

Erfolg heißt ausschließlich **„Kartensignatur aufgenommen · ungeprüft“**. Protokollbindung und passende Länge beweisen weder mathematische Gültigkeit noch eine vertrauenswürdige Zertifikatskette. Keine Signaturen an historische, rekonstruierte oder nur normalisierte Quellen hängen; auch gleiche Originalbytes erlauben keinen jüngeren Signaturersatz für eine alte Sitzung.

Alle anderen signaturpflichtigen EFs, mathematische Signatur-/Zertifikatsprüfung, ERCA/MSCA-Vertrauen, Originalexport/DDD, UPDATE BINARY/LastCardDownload, Secure Messaging/0540, 7F66-Decoder und Navigation/Onboarding bleiben eigene Folgepakete. Der automatische Import verändert keine gespeicherten Karten-EFs; Hash/Signatur sind neue, ausdrücklich zu prüfende Sicherheitsoperationen mit temporärem Kartenzustand.

## Primärgrundlage und feste Protokollregeln

Links, PDF-Fingerprints und vollständige Pflichtdateien stehen in der [Primärmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md). Für diesen Plan erneut gezielt gelesen: konsolidierte Fassung 21.08.2023 Appendix 2 TCS43 und TCS118-131, Appendix 7 DDP038/040, Appendix 11 Part A CSM033-035 und Part B CSM48-50/83-87/141/233-236; Änderung 2018/502 zu TCS121/123/124 sowie BSI TR-03111 **v2.00** §5.2.1. Seiten K246, K271-274, K352-354, K443, K448-449, K454, K469, K499-500; EquipmentType/CHA zusätzlich Appendix 1 §2.67, K152-153.

| Schritt | Festgelegter Ablauf |
|---|---|
| Vorbereitung je DF | Erkannte Fahrerkarte und eigene Anwendungsidentität; Kapazität ausschließlich aus diesem Application_Identification. Zertifikate innerhalb dieses DF einmal **vor** den beiden Kernsequenzen lesen und unveränderlich übernehmen. GEN2 benötigt ein neu gelesenes, nicht blankes, strukturell decodierbares **C101/Card_Sign** mit bekanntem Kurven-OID und CHA für Fahrer-Signatur (EquipmentType 17); C100/Card_MA, CA-/Link-Kurve und deren Signaturlänge ersetzen es nicht. Das ist eine Strukturvoraussetzung, keine Rollen-/Kettenfreigabe. |
| SELECT | Genau den Ziel-EF auswählen. `6A82` bedeutet EF abwesend; sonst tatsächliche SW/Readerfehler behalten. Kein Hash ohne erfolgreiche Auswahl. |
| PERFORM HASH OF FILE | **`80 2A 90 00`**, ohne Datenfeld und ohne Le, P2=00 in beiden DFs. Nur `9000` mit leerem Antwortdatenfeld ist erfolgreich. Kein generisches PSO:HASH, keine freie Algorithmuswahl. |
| READ BINARY | Ab Offset 0 alle deklarierten EF-Bytes einschließlich Pointer/unbenutzter Bytes lesen; vorhandene begrenzte Chunk-/Offsetlogik verwenden. Fachliches Parsen erst nach abgeschlossener Rohsequenz. Kein vollständiger Snapshot aus Teilbytes. |
| Vollständigkeitsprüfung | Vor SIGN zusätzlich Dateiende am deklarierten Offset mit einem Byte und `correctLength=false` prüfen. TCS43 belegt datenlose `6700`/`6B00` als Grenzantworten; ein weiteres Byte, `6Cxx`, `6282`, andere SW oder Antwortdaten sind hier **kein akzeptierter Grenzbeleg**. Kein `6C00`-Retry zu Le 256. Bei Länge 32768 ist der Grenzoffset im vorhandenen 15-Bit-Reader nicht adressierbar: regulären Rohimport erhalten, Signatur nicht versuchen (`BOUNDARY_UNSUPPORTED`). Keine Erweiterung des Adress-/SFID-Verfahrens in diesem PR. |
| PSO: COMPUTE DIGITAL SIGNATURE | **`00 2A 9E 9A Le`**, kein Datenfeld, genau die unten festgelegte Länge. `correctLength=false`; keine automatische Wiederholung mit anderer Signaturlänge. Vorhandene begrenzte GET-RESPONSE-Behandlung darf Antwortteile sammeln; nur abschließendes `9000`, exakte Gesamtlänge und kein durchgehend `00`/`FF` belegter Blankwert ergeben einen aufgenommenen Signaturblob. |
| Gemeinsame Aufnahme | Original, tatsächliche Signaturbytes, Algorithmus-/Formatbeleg und Versuchszustand defensiv kopieren. Nach SIGN erst nächste EF/andere Leser. Zwischen Hash und Signatur keine andere EF/DF-Auswahl, Zertifikatslesung, neuer Hash, Reset oder fremde APDU-Sequenz. |

Die Dateiende-Prüfung ist eine zusätzliche defensive Voraussetzung dieser ersten Implementierung, keine neue allgemeine Standardforderung. Bei datenloser unklarer Grenze bleibt die reguläre Aufnahme der deklarierten Bytes erhalten, der Signaturbeleg wird als nicht aufgenommen gezeigt. Präzisierung bei der Umsetzung: **Ein tatsächlich zusätzlich empfangenes Byte widerspricht der Kapazität und beweist einen unvollständigen Präfix.** Diesen Präfix nicht als vollständigen Core-Snapshot speichern (READ_FAILED, keine SIGN-APDU); andere Quellen bleiben unabhängig erhalten. TCS121 nennt neuen Hash, DF-Auswahl und Reset als Hashlöschgründe; ein EF-Wechsel wird hier aus Bindungsgründen ausgeschlossen, ohne ihn als zusätzlich normativ belegten Löschgrund auszugeben.

| Anwendung / tatsächlich gelesenes Card_Sign-OID | Algorithmus / Kartenhash | Signaturformat und Le |
|---|---|---|
| GEN1 | RSA PKCS#1 v1.5 / SHA-1, implizit | 128 Bytes; Le `80` |
| `2A8648CE3D030107` / `2B2403030208010107` | ECDSA P-256 / BrainpoolP256r1; SHA-256 | Plain R\|\|S, 64 Bytes; Le `40` |
| `2B81040022` / `2B240303020801010B` | ECDSA P-384 / BrainpoolP384r1; SHA-384 | Plain R\|\|S, 96 Bytes; Le `60` |
| `2B240303020801010D` | ECDSA BrainpoolP512r1; SHA-512 | Plain R\|\|S, 128 Bytes; Le `80` |
| `2B81040023` | ECDSA P-521; SHA-512 | Plain R\|\|S, 132 Bytes; Le `84` |

Kein ASN.1-/DER-Umschreiben der aufgenommenen ECDSA-Signatur. Das CertificateParser-Kurvenwissen deckt diese sechs OIDs strukturell ab; Kurvenparameter-/Punkt-/Kettenprüfung wird damit nicht behauptet. GEN1 benötigt keinen wiederhergestellten Zertifikatsschlüssel zum Abruf der impliziten 128-Byte-Signatur; sein tatsächlich neu gelesener C100-Beleg wird, falls vorhanden, derselben Sitzung zugeordnet. Fehlendes GEN1-Zertifikat ist sichtbar und verhindert jede spätere Vertrauensbehauptung.

## Zustände, Abbruch und unveränderte Rohaufnahme

Eigenes unveränderliches Signaturergebnis je Core-EF: `CAPTURED_UNVERIFIED`, `FAILED` oder `NOT_ATTEMPTED`, dazu Grund, Ergebnis-/Fehlerphase (`SELECT/HASH/READ/BOUNDARY/SIGN`), tatsächliche SW falls empfangen und ReaderFailure falls vorhanden. Erfolgreiche Hash-/Signatur-SW getrennt festhalten. Altimporte ohne neue Ergebnismap erhalten **keine** erfundenen Versuchszeilen; Anzeige „Signatur in diesem Import nicht aufgenommen“.

| Fall | Rohdatei / Signaturentscheidung |
|---|---|
| Kapazität unbekannt oder EF abwesend | Bestehenden Core-Zustand erhalten; Signatur NOT_ATTEMPTED mit Grund. Keine Signatur-APDU. |
| GEN2 C101 fehlt, blank, beschädigt, falsches CHA oder unbekanntes OID | Reguläre vollständige Rohaufnahme fortsetzen; Signatur NOT_ATTEMPTED. Kein Ratversuch mit Le 64/128, kein historisches Zertifikat als Ersatz. |
| Hash `6985/6986/6400/6581` oder andere nicht erfolgreiche SW | Signatur FAILED mit tatsächlicher SW; nach sicherer EF-Auswahl reguläre Rohdatei weiter lesen, ohne Signaturbefehl. Andere Core-EF weiter unabhängig versuchen. |
| Vollständiges READ gescheitert oder Daten überschießen die Anfrage | Bestehendes READ_FAILED; keine Teilbytes als Original und keine SIGN-APDU. Signatur FAILED/READ. |
| Deklarierte Bytes gelesen, Grenze unklar/überschritten | Bei datenloser unklarer Grenze reguläre Originalaufnahme behalten, Signatur FAILED/BOUNDARY. Tatsächliche zusätzliche Daten: Präfix verwerfen/READ_FAILED, kein vollständiges Original behaupten. SIGN nicht versuchen. Nicht adressierbare Grenze vor Hash als NOT_ATTEMPTED. |
| Signatur `6985/6400/6581`, falsche Länge, Blankantwort, falsche End-SW oder begrenzte Antwortkette überschritten | Vollständiges Original behalten; Signatur FAILED, tatsächliche SW/Länge soweit bekannt. Keine Null-/Dummy-Signatur, kein gültiger Ersatz aus einem anderen Versuch. |
| Erfolgreiche Rohsequenz, fachlich EMPTY/INVALID | Echte Signatur unabhängig von der fachlichen Projektion speichern; kein neuer Tätigkeits-/Fahrzeuginhalt erfinden. |
| Kartenentnahme, Disconnect, Timeout, Coroutine-Abbruch | Gleiche Transport-/Abbruchgrenze wie vorhandener Gruppenreader: gesamte laufende Aufnahme verwerfen, keine Teilgruppe/Signatur speichern, kein Signieren nach Wiederverbindung mit altem Hash. Vorher gespeicherte Historie bleibt. |

Keine versteckten Wiederholungen von HASH oder SIGN. Ein bewusst neuer automatischer Import nach erneuter Verbindung beginnt mit neuer Request-/Aufnahme-ID und frischer kompletter Sequenz. `IO/MALFORMED_RESPONSE` dürfen nur gemäß bestehendem Reader-Vertrag als lokale Dateifehler behandelt werden; eine verlorene Verbindung niemals durch einen behaupteten lokalen Signaturfehler verschleiern. Rückmeldungen bleiben knapp und ohne private Roh-/APDU-Dumps.

## Speicherung und historische Bindung

**Geplant: additive Schema 19→20-Migration**, eigener DAO/Store für `card_core_signature_receipts`, genau eine unveränderliche Zeile je **Child-sessionId + Core-EF**. Keine Signaturspalte am global deduplizierten Originalblob: dadurch würde eine neue Signatur bei älteren Sitzungen erscheinen. Der vorhandene `card-core-original-v1`-Fingerprint und alle bisherigen Originaltabellen bleiben unverändert.

Die neue Zeile enthält physische driverCardId, application/AID bzw. eindeutig geprüften Anwendungswert, generation, EF/kind, Zustände/Phase/SW/ReaderFailure, erwartete und tatsächlich empfangene Signaturlänge, gewählten Policy-/Hash-/Formatwert, Card_Sign-OID, Originalquellreferenz und gegebenenfalls exakt diesen neuen Zertifikatsquellbeleg. Erfolgreiche Signaturbytes nur bei CAPTURED_UNVERIFIED und vollständigem Original. Verworfene Antwortdaten erzeugen keinen Signaturblob; Länge/Fehler reichen als Beleg. Arrays/Maps beim Reader-Ergebnis, Freeze, Prepare und Laden defensiv kopieren.

FK zu `driver_import_sources(sessionId, driverCardId)`; Original-/Zertifikatsreferenzen **zusätzlich zur gleichen Sitzungsquelle** in `driver_import_metadata_sources` binden. Dafür im tatsächlichen Entity-/Migrationsentwurf passenden zusammengesetzten Unique-Index ergänzen. Ein bloßer FK zum globalen `card_metadata_imports`-Blob reicht nicht. Nullable Quelle nur bei tatsächlich fehlender Roh-/Zertifikatsaufnahme; Repository prüft kind/generation/AID/Bytes/Fingerprints und konsistente Zustände. Sitzungsfremde Quellen werden abgewiesen, auch bei identischen Bytes. FK RESTRICT und SQLCipher/Keystore erhalten.

Der reguläre automatische ALL_AVAILABLE-Import speichert diese Belege. Bestehende begrenzte Importauswahlen erhalten ihre Bedeutung: wenn die Auswahl das Core-Original ausschließt, Signatur-Dateiergebnis als NOT_STORED erfassen und keinen Signaturblob/Receipt ohne gemeinsam gespeicherte Quelle anlegen; einen tatsächlich ausgeführten Versuch nicht in NOT_ATTEMPTED umdeuten. Auch dafür einen negativen Auswahltest vorsehen.

Neue Zeilen, Originalreferenzen, Certificates, ImportRun/Session und gegebenenfalls Parent/Children **in derselben vorhandenen Room-Transaktion** speichern. Ein FK-/Storefehler rollt die ganze neue Gruppe zurück. Loader arbeitet ausschließlich über die gewählte Session; kein latest-Fallback, keine Ergänzung alter Requests. Wiederholung derselben eingefrorenen Request muss exakt dieselben neuen Belege bestätigen oder widersprüchliche Payload ablehnen. Neue Aufnahme bekommt neue Sitzung auch bei dedupliziertem Original; ECDSA kann bei gleicher Datei andere echte Signaturbytes liefern.

Fingerprint-Erweiterung ausschließlich bei vorhandener neuer Signaturmap, längengefasst und mit eigener Domain: Core-EF, Status/Grund/Phase/SW, tatsächliche Original-/Zertifikatsbindung, Algorithmus und Signaturbytes. Fehlende Map erhält bytegenau die bisherigen Einzel-/Batch-Request-Fingerprints. Keine nachträgliche Aktualisierung/Backfill historischer Tabellen. Schema-20-JSON erst vom echten Room-Compiler exportieren, niemals handschreiben. Produktversion **40124 vorgesehen**, erst im Implementierungs-PR tatsächlich erhöhen.

## Begrenzte Codefläche für den Folge-PR

| Bereich | Konkrete geplante Änderung |
|---|---|
| Protokoll / Modelle | `data/.../card/Iso7816.kt`: nur genaue Command-Factories, bestehende APDU-Verträge erhalten. Neue `CardCoreSignature.kt` und `CardCoreSignatureReader.kt`: Policy/OIDs, defensive Aufnahme, sequenzielle Phasen und Fehler; keine Trust-Engine oder neue Kryptobibliothek. |
| Gruppen-/Einzelleser | `DriverCard.kt`: vorhandene Zertifikatsaufnahme einmal vor Core-Dateien verschieben, Core-Sequenz integrieren, Fallback-Rohaufnahme und Transportabbruch erhalten. `CardApplicationRead.kt`: Freeze/Anwendungsinvarianten um Signaturen ergänzen. Keine zweite Zertifikatsaufnahme zwischen Hash und Signatur. |
| Import / Persistenz | `DriverCardImportPipeline.kt`, `DriverCardImportRepository.kt`: eigene Signatur-Dateiergebnisse, Prepare/Fingerprint/Transaktion/Laden und historische Nullmap-Kompatibilität. Neue `CardCoreSignatureStore.kt` plus `db/CardCoreSignatureDao.kt`, `CardMetadataDao.kt`, `AppDatabase.kt`, `DatabaseMigrations.kt`, `security/EncryptedDatabaseFactory.kt` und echter Schemaexport. `CardBatchRequests.kt` nur ändern, wenn die nachgewiesene bestehende Child-Fingerprint-Einbindung nicht ausreicht. |
| Kleine Abnahmeanzeige | Bestehender `SavedCardPreviewScreen.kt` und zugehörige Modelle: pro 0504/0505 der gewählten Quelle Status, tatsächliche Signaturlänge und „ungeprüft“ bzw. Grund/kein Beleg. Kein grünes Vertrauenssiegel; keine technischen Test-/Speicherknöpfe. `AppRoot.kt` nur falls Auswahlzustand angepasst werden muss. |
| Build / Tests | Nur Versionsschritt in `app/build.gradle.kts`; neue/gezielt ergänzte Unit-, SQLCipher-/Migrations- und Compose-Tests. Keine CI-/Upload-/Budget-/Triggeränderung. |

## Vorab verbindliche Prüfungen

| Ebene | Erforderlicher Nachweis vor Produktfreigabe |
|---|---|
| Unit: Befehlsbytes / Sequenz | Beide DFs P2=00, HASH ohne Lc/Le, SIGN ohne Daten mit Le 128/64/96/128/132; alle sechs Card_Sign-OIDs. Exakt SELECT→HASH→vollständige READs→Grenzprobe→SIGN; keine dazwischenliegenden Selektions-/Zertifikatsbefehle. C101-Kurve verschieden von C100/CA und von der Zertifikatssignaturlänge. |
| Unit: Byte-/Grenzfälle | Chunkgrößen 1/255/256/kurze erfolgreiche Chunks, nichtnull Pointer/Unused-Bytes, Gen1-/Gen2(V2)-Breiten, Nullring/EMPTY/INVALID. Mutationen des Eingangs und ausgegebener Maps/Arrays auch während suspendierter Kartenbestätigung. Deklarierte Kapazität zu klein/groß, zusätzliche Bytes, Offset 32768, Grenz-SW und `6C00` ohne verdeckten Retry. |
| Unit: negative Protokolle | SELECT 6A82 und andere SW; HASH 6985/6986/6400/6581, unerwartete Hashdaten; SIGN 6985/6400/6581, zu kurz/lang/blank; DER-Antwort falscher Länge zurückweisen, Plain-Bytes niemals als DER interpretieren/umkodieren. Ein zufälliges DER-Präfix in korrekt langen Plain-Bytes beweist keinen Formatfehler. Fehlende/beschädigte/blanke/unbekannte C101 und CHA=1 statt 17. GET RESPONSE mit exakter/überlanger Gesamtantwort, Endfehler, 32-Schrittgrenze und Le-Korrektur ohne SIGN-Retry. Kein Signieren ohne neue vollständige Rohsequenz. |
| Unit: Abbruch | Transportverlust und Coroutine-Abbruch in SELECT/HASH/READ/Grenze/SIGN und zwischen DFs. Kein späterer SIGN nach Reset/DF-Wechsel/neuem Hash, keine geteilte Session mit interleavenden Befehlen, keine Teilgruppe. Nichttransport-Dateifehler lässt andere EF und unsigned Rohimport bestehen. |
| SQLCipher / Migration | Echter 19→20-Upgrade und alle bestehenden Migrationsketten, Compiler-Schema, alte Profile/Imports unverändert. Bytes/Längen/Status/C101 exakt nach Reopen; FK gegen fremde Karte, EF und Session; zusätzliche Repository-Prüfung von Generation/AID. Fehlendes Original bei CAPTURED sowie Quelle aus neuerer Sitzung zurückweisen. Atomarer Rollback bei zweitem Child/Signaturinsert, keine Leichen-/Teilzeilen. |
| SQLCipher / Historie | Alte Session ohne Map/Signatur bleibt unsigned nach neuer Aufnahme gleicher Originalbytes. Reine deduplizierte neue Aufnahme hat nur eigenen Signaturbeleg; zwei verschiedene ECDSA-Signaturen zur gleichen Datei werden nicht vermischt. Gleiches Request-Replay stabil; widersprüchliche Signatur/Status ablehnen. Vorherige Einzel-/Batch-Fingerprint-Fixtures bytegenau gleich. EMPTY/INVALID-Rohquelle behält ihren eigenen Signaturzustand. |
| Compose | GEN1/GEN2(V2), alter Import/neuer Import/Altarchiv wechseln: Status und Länge gehören immer zur sichtbaren Quelle; alte Quelle erhält kein jüngeres Zertifikat/Signatur. Fehl-/Nichtversucht-Gründe, „ungeprüft“, EMPTY/INVALID ohne erfundene Inhalte; Maskierung/Selektionsreset und bestehende Importbedienung erhalten. |
| Vollständige CI | Endgültiger Produktkopf: alle sieben Gradle-Aufrufe, Unit/Lint/APK-/Signatur-/Updatepaar-/Compiler-Schemaexport, komplette API 36-SQLCipher-/Compose-Suiten und Abschlussjob. Bestehende Klassifikation und Gates unverändert; normale erfolgreiche CI ohne Upload. |

Synthetische Fixtures mit Format-/Fehlerwerten, keine privaten Kartendumps, Schlüssel oder vollständigen Benutzer-APDU-Logs. Die Tests behaupten keine mathematische Signaturvalidierung und keine zertifizierte Kartenkette.

## Eigene Geräteabnahme des Folge-PRs

Erst nach voller grüner CI ein Paket für **genau denselben endgültigen Produktkopf** über den bestehenden gezielten Workflow erzeugen. Neue APK per Update über 40123 installieren; freigegebene Paket-/Signatur-/Updatepaarregeln erhalten. Die 40123-Bestätigung ersetzt keinen dieser neuen Tests.

1. Alte Imports/Profil unverändert öffnen: kein Signaturbeleg nachträglich vorhanden; Tätigkeiten/Fahrzeuge und gespeicherte Originale weiterhin verfügbar.
2. Eigene reale Karte erneut automatisch lesen. GEN1 und vorhandenes GEN2/V2 ausdrücklich getrennt wählen. Beide Core-EFs müssen CAPTURED_UNVERIFIED mit realer protokollgemäßer Länge zeigen; Rohgrößen und fachliche Daten plausibel. Auf der vorhandenen V2-Karte GEN1-Signaturen 128 Bytes; GEN2V2-Länge aus tatsächlich neu gelesenem C101, **keine feste Annahme 64 Bytes**.
3. App/Reader trennen und App neu öffnen: exakt diese Sitzung, zwei Originale und Signaturstatus/-längen erhalten. Alte/neue Sitzung und Anwendungen mehrfach wechseln, kein jüngerer Quellenersatz.
4. Neue erneute Aufnahme durchführen: bestehende Originaldeduplizierung/Daten erhalten; neue Sitzung besitzt nur ihre eigenen Belege, kein Rückwirkungs-/Summeneffekt.
5. Einen neuen Import während des Lesens bewusst durch Kartenentnahme unterbrechen: verständlicher Abbruch, keine neue Teilgruppe/Signaturquelle. Danach neu verbinden; vollständiger neuer Import ohne alten Hashzustand muss funktionieren. Nicht behaupten, damit jede APDU-Abbruchphase auf echter Hardware geprüft zu haben; Phasenabdeckung kommt aus Tests.

Screenshots/Rückmeldung nur Status, Quelle und Größen; keine privaten Dumps. Exakten Buildkopf, Paketlauf, Geräte-/Leserumfang, reale Generation und tatsächlich bestätigte Punkte dokumentieren. Reine GEN2-Karte ohne V2, reale leere/defekte EFs und sechs verschiedene reale Kurven nicht pauschal als gerätegeprüft ausgeben; diese Varianten sind automatisiert abzudecken und echte Hardwarebelege getrennt zu benennen. Schlägt die echte Aufnahme auf dem vorhandenen Gerät fehl, bleibt der Produkt-PR vor Merge gesperrt, bis Ursache behoben oder Umfang ausdrücklich neu entschieden ist.

## Ursprünglicher Einstieg / aktueller Abschluss

Der getrennte Produkt-Meilenstein ist mit PR #73 abgeschlossen: eigene volle CI 38381962283, exaktes Paket 38388192092, ausdrückliche Gerätebestätigung sämtlicher Checklistenpunkte und Merge des unveränderten getesteten Baums. Als nächster eigener Plan-Meilenstein Pflichtdateiumfang für weitere EF-Signaturen festlegen; keine Trust-/DDD-/Schreib-/UI-Folgephase gleichzeitig beginnen. Weitere Implementierung erst in einem getrennten eigenen Produkt-Meilenstein mit eigenen Gates.
