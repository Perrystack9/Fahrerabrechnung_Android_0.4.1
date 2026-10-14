# 0504/0505 — mathematische Prüfung gespeicherter Kartensignaturen

**Freigegeben am 14.10.2026, 06:13 Europe/Berlin; Umsetzung 40127 auf Produktbranch.** Tobi beauftragt zunächst einen konkreten technischen Plan für Gen1 und Gen2/Gen2V2. Bestätigte Produktbasis: main `7bbc66ed37dc1129a1e5286319e73e0860a2f474`, 40126 / Room22; Produktmerge #79 und Abschluss #80, aktuelle CI grün. [Regeln](CURRENT_RULES.md), [Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) und bestätigte Quellen-/Gerätebelege bleiben erhalten.

## Ziel und eng begrenzter Umfang

Eine bestehende gespeicherte Übernahme liefert je EF0504 (Tätigkeiten) und EF0505 (Fahrzeuge) eine abgeleitete mathematische Aussage: passt die gespeicherte Signatur zu den exakten Originalbytes unter dem zugeordneten öffentlichen Schlüssel? Gen1 und Gen2/V2 werden separat anhand ihres Childs geprüft; V2 bleibt derselbe Gen2-AID-Zweig.

Zertifikatsvertrauen ist eine zweite, eigenständige Aussage und bleibt **NOT_EVALUATED**. Mathematisch gültig bedeutet weder amtlich vertrauenswürdige Karte/Identität noch gültige Zertifikatskette, Widerrufsprüfung, rechtliche Datenbestätigung, vollständiger Standarddownload oder Manipulationsfreiheit des gesamten Datenbestands. Der begrenzte Meilenstein erfüllt nicht den vollständigen IDE-Kettenprüfauftrag aus CSM035/234. Insbesondere ist ein strukturell lesbares Gen2-C101 noch kein authentifiziertes Zertifikat.

Nur vorhandene Core-Receipts und vollständige eigene Originalquellen verwenden. Keine neue Signaturaufnahme, Reader-/APDU-/Schreibfunktion, weitere EF-Verifikation, Identifikations-/Ereignis-/Störungs-Verifikation, Export/DDD, Internet-Schlüsselabfrage, Trust-UI oder Oberflächenneugestaltung.

## Tatsächlicher Ausgangspunkt

- [CardCoreSignatureStore](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureStore.kt) bindet bereits Karte, Child-sessionId, Anwendung, Generation, EF, Originalquelle und C100/C101-Zertifikatsquelle. `verify()` prüft heute nur identische gespeicherte Receipt-/Request-Fingerprints, **keine** mathematische Kartensignatur; Methode und Fingerprints nicht umdeuten.
- [Core-Receipts](../data/src/main/kotlin/de/fahrerabrechnung/data/db/CardCoreSignatureDao.kt) und [Core-Snapshots](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSnapshotStore.kt) sind unveränderliche verschlüsselte Belege. Aufnahmezustand CAPTURED_UNVERIFIED bleibt als ursprünglicher Aufnahmebefund erhalten.
- [CardCertificateParser](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCertificateParser.kt) liefert Gen1-Signatur/Rest/CAR, noch keinen RSA-Schlüssel. Gen2 liefert C101-OID und unkomprimierten öffentlichen Punkt, bisher ohne mathematische Punktprüfung.
- [SavedDriverImport/decode](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCardImportRepository.kt) lädt exakte gewählte Quellen; [SavedCardPreviewScreen](../app/src/main/kotlin/de/fahrerabrechnung/app/SavedCardPreviewScreen.kt) zeigt die zwei Core-Belege. Bestehende synthetische CertificateFixtures sind ausdrücklich **nicht** kryptografisch gültig und dürfen keine positiven Kryptotests begründen.

## Primärgrundlagen und Befundgrenzen

| Primärquelle | Relevanter Vertrag |
|---|---|
| [EU2016/799, Appendix11](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), TeilA CSM017–019/033–035 | Gen1-Zertifikatsrückgewinnung mit ISO9796-2-Rahmen; davon getrennte RSA-PKCS#1-v1.5/SHA-1-Dateisignatur |
| Derselbe Anhang, TeilB CSM48–50/233–236 | Sechs feste EC-Domänen, Hash nach Card_Sign-Schlüssel, ECDSA-Plain-Dateisignatur |
| [EU2018/502](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf), CSM135/141/146; vorhandene [Matrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) | DER-Zertifikatsrahmen und Rollen; Card_Sign nicht durch Card_MA oder Ausstellerkurve ersetzen |
| [BSI TR-03111 v2.00](https://www.bsi.bund.de/SharedDocs/Downloads/EN/BSI/Publications/TechGuidelines/TR03111/BSI-TR-03111_V-2-0_pdf.pdf?__blob=publicationFile&v=1), §4.2.1.2/5.2.1 | ECDSA-Bereichsprüfung und feste unsigned Big-Endian-Plain-Komponenten R/S |
| [RFC8017 §8.2.2/9.2](https://www.rfc-editor.org/rfc/rfc8017.html) | RSA-Primitiv, volle EMSA-PKCS1-v1.5-Prüfung statt bloßem Hash-/Präfixvergleich |
| [Gen1-Public-Key-Audit](CARD_GEN1_PUBLIC_CERTIFICATE_AUDIT.md), [JRC-Dokumentation](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_dt.php.html) | Bereits belegter öffentlicher ERCA-RSA-Einstieg und öffentliches MSCA-Testzertifikat; diese Forschung ist keine Benutzerkarten- oder Trust-Abnahme |

2016/799 und 2018/502 am 13.10.2026 für diesen Plan erneut direkt vom JRC geladen: SHA256 `7bc8a9f42bbac789e8aa5bcebec028d017d477a7d212db82e0b83170bc732f05` und `eb804bb2997ccb673405330efa6e7ee80fa06834387682ad6242b7cb374d6704`, entsprechend den dokumentierten Primärbelegen. TR03111-v2.00-Datei mit dokumentiertem SHA256 `f39a69a5c54b07712a76e1f31bd275e26f59d7ad67b7a54f9c3e198e6d9e2509` gelesen. Keine abweichende alte Scratch-PDF als diesen Nachweis ausgeben. JRC-IOT v1.10 §10.1.2/Tabelle20 bestätigt zusätzlich den 164-Byte-Klarinhalt von Gen1-Zertifikaten. Hieraus keine neue Vertrauens-, Geräte- oder regulatorische Vollabnahme ableiten.

## Architektur und Quellenvertrag

1. **Exakter gespeicherter Eingang:** Neuer interner Assembler lädt über profileId/sessionId/EF und gegebenenfalls den exakten batchId/Child-Bezug in einem konsistenten Read-Snapshot die vorhandene Receipt-Zeile, deren OriginalSourceId, CertificateSourceId und Metadatenverknüpfungen. Bestehende Fingerprint-/FK-/Karten-/Batch-Child-/AID-/Generationsprüfungen vor Kryptografie beibehalten. Nicht ausschließlich eine ungeprüfte UI-Map als Eingang akzeptieren.
2. **Gen1-Ausstellerquelle:** C108 nur aus derselben Child-sessionId/driverCardId/GEN1. C100 ausschließlich über die konkrete Receipt-Zertifikatsreferenz. Globale aktuelle/jüngere Zertifikate und Quellen anderer Anwendungen dürfen keine Lücke schließen. Der öffentliche RSA-Einstieg ist der separat versionierte, exakt über CAR ausgewählte Rechenschlüsselkatalog.
3. **Unveränderliche Eingaben:** Original-, Signatur-, Zertifikats- und Schlüsselbytes defensiv kopieren. Prüfen über sämtliche gespeicherten EF-Originalbytes einschließlich Ringpointer/unbenutzter Slots; keine Parserprojektion, Sortierung, Normalisierung, DDD-Tags, Längenpräfixe, APDU-Statusbytes oder vorhandenen SHA256-Speicherfingerprint signaturprüfen.
4. **Eigene Ergebnistypen:** Neue CardCoreSignatureVerification mit ausgewählter Karten-/Session-/gegebenenfalls Batch-Parent-/Anwendungs-/Generations-/EF-Bindung, Receipt-/Original-/C100- oder C101-Zertifikatsfingerprint sowie gegebenenfalls konkreten C108-Quellfingerprint, tatsächlichem Schlüssel-/Policyfingerprint, verifierVersion und gegebenenfalls Gen1-Katalogfingerprint. Originale Fingerprints/Enums/Receipts nicht verändern; Prüfaussage nicht global an einen deduplizierten Blob hängen.
5. **Nur abgeleitete Leseprojektion:** Prüfung beim Öffnen der gespeicherten Quelle im Hintergrund, ohne Leser und ohne DB-Schreibvorgang. Ergebnis nicht persistieren; bei Reopen neu berechnen. Kein Backfill, kein verändertes Replay/Dedup, keine neue Tabelle/Migration; **Room22 bleibt**. Zielversion der später freigegebenen Produktimplementierung: 40127.
6. **Auswahl und Fehler:** Ergebnis erst für denselben noch ausgewählten sessionId/EF/Quellentupel veröffentlichen; Wechsel/Altarchiv löscht es, Coroutine-Cancellation propagiert. Kryptofehler halten keine bestätigten Importdaten zurück und erzeugen keine neue Gruppen-/Quellenzuordnung. Vorhandene Herkunfts-/Integritätsablehnung nicht durch catch-and-continue abschwächen. Kein neuer Karten-/Profilnachweis aus Zertifikats-CHR ableiten.
7. **Keine langlebige globale Ergebnisablage:** Zunächst kein Cache. Neue Bibliotheks-/Verifikator-/Katalogversion darf bei Reopen eine neue explizit zugeordnete Rechenaussage liefern; ursprüngliche Aufnahme bleibt unverändert.

## Gen1: Schlüsselrückgewinnung und Dateiprüfung getrennt

**Schlüsselgewinnung:** Gen1-C108 und C100 sind je 194 Bytes: Sign128 + Rest58 + äußere CAR8. C108 mit dem über äußereCAR exakt identifizierten öffentlichen ERCA-Rechenschlüssel öffnen, anschließend C100 mit genau diesem rückgewonnenen MSCA-Schlüssel. Beide Operationen sind mathematische Zertifikatsrückgewinnung, keine Trust-Entscheidung.

Je Schritt unsigned RSA-Repräsentant mit `0 < s < n`, exakte 1024-Bit-Schlüsseldomäne und 128-Byte-RSA-Ausgabe prüfen; nur den CSM018/019-Rahmen `6A || Cr106 || H20 || BC` annehmen. Inhalt164 = Cr106 + Rest58; SHA1(Inhalt) muss H20 sein. Keine ISO9796-Alternative/n−m-Rettung, ASN.1-/X.509- oder PKCS1-Zertifikatsvermutung.

Im rückgewonnenen Inhalt CPI01, innere/äußereCAR und Ausstellerschlüsselreferenz, CHR-Verknüpfung C100.CAR == C108.CHR, richtige Gen1-AID/CHA (MSCA00, Fahrerkarte01) und gültige RSA-n/e-Domäne prüfen. Zahlen unsigned, n 1024 Bits/ungerade; e im zulässigen positiven ungeraden Bereich. Gültigkeitszeiten auslesen, aber in diesem Meilenstein **nicht** als zeitliche Trust-Policy oder mathematisches Dateiurteil bewerten.

**Öffentlicher Einstieg:** Kleiner unveränderlicher Gen1RsaRecoveryKeyCatalog, zunächst genau der vorhandene veröffentlichte ERCA-144-Byte-Einstieg (CAR8/n128/e8, Referenz `fd45432000ffff01`, Nutzlast-SHA256 `4cc1ed00c6be8f83c0d8f014282165d037427f829734402215fb59a31a622229`). Bei Umsetzung Originalartefakt erneut gegen diesen dokumentierten Digest prüfen. Kein beliebiger nachgeladener Schlüssel, keine Länder-/Zeit-/latest-Auswahl. Unbekannte Referenz oder fehlendes C108/C100 => NOT_CHECKABLE. Katalog ist Rechenmaterial, **kein** eingeführter App-Truststore; keine Rolle TRUSTED_ROOT vergeben.

**0504/0505-Dateisignatur:** Mit ausschließlich dem aus dem konkreten C100 gewonnenen öffentlichen Kartenschlüssel SHA1 der vollständigen EF-Bytes genau einmal berechnen. Signatur 128 Bytes, unsigned Repräsentant in der RSA-Domäne; gesamtes zurückgewonnenes PKCS1-EM gegen `00 01 FF…FF 00 || DigestInfo(SHA1, digest)` prüfen. Genau CSM034-DigestInfo mit SHA1-OID/NULL/20-Byte-Hash, voller 128-Byte-Rahmen, alle Paddingbytes und kein weiterer Rest. Nicht RSA-PSS, ISO9796 oder SHA256 verwenden; keine laxen BER-/fehlendes-NULL-/Suffix-/Präfix-/Trailing-Garbage-Akzeptanzen. SHA1 hier ausschließlich vorhandenes Gen1-Protokoll, keine Wahl für neue Signaturen.

## Gen2/Gen2V2: C101 und ECDSA-Plain

Nur den exakten Receipt-C101/Card_Sign-Beleg desselben Childs verwenden, CHA17 und vorhandenen strikten Zertifikatsrahmen prüfen. Keine C100/Card_MA-/C108-/C109-/Zertifikatssignaturkurve als Dateipolicy. Für die reine Dateimathematik ist keine Gen2-Ausstellerkette Voraussetzung; deren Authentizität/Gültigkeit bleibt NOT_EVALUATED.

| Card_Sign-Domäne | OID-Hex | Dateihash | R/S je | Ganze Plain-Signatur |
|---|---|---|---:|---:|
| NIST P-256 | 2A8648CE3D030107 | SHA256 | 32 Bytes | 64 Bytes |
| BrainpoolP256r1 | 2B2403030208010107 | SHA256 | 32 Bytes | 64 Bytes |
| NIST P-384 | 2B81040022 | SHA384 | 48 Bytes | 96 Bytes |
| BrainpoolP384r1 | 2B240303020801010B | SHA384 | 48 Bytes | 96 Bytes |
| BrainpoolP512r1 | 2B240303020801010D | SHA512 | 64 Bytes | 128 Bytes |
| NIST P-521 | 2B81040023 | SHA512 | 66 Bytes | 132 Bytes |

Öffentlichen Punkt auf genau dieser festen Domäne prüfen: korrekte unkomprimierte Länge/04-Rahmen, x/y im Feld, auf Kurve, nicht Punkt im Unendlichen, richtige Untergruppe (`nQ=O`). Keine frei angelieferten Kurvenparameter oder Koordinaten-Modulo-Reparatur.

Signatur exakt in feste unsigned Big-Endian-R/S zerlegen, jeweils `1 <= r,s < n`. Originalbytes unverändert; keine DER-Erkennung/-Rettung und kein Umkodieren des gespeicherten Belegs. Ein korrekt langes Plain-R, das mit 30 beginnt, ist kein DER-Nachweis. Führende Nullbytes sind normale feste I2OS-Codierung; P521-Überbreiten nicht kürzen. Kein Low-S-Zwang oder Signatur-Normalisieren: mathematisch gültiges (r,n−s) bleibt gültig.

Dateihash genau einmal bilden und der geprüften ECDSA-Primitive als Digest geben; Bibliothek muss FIPS-konforme Hash-zu-Integer-Verarbeitung übernehmen. Weder SHA256-Doppelfingerprint noch SHA512-zu-Hashlänge-Ratversuch. Unterschiedliche Aussteller-/Subject-Kurven sind keine Dateiverifikationsabweichung.

## Kryptobibliothek und geplante Eingriffspunkte

Vorschlag: explizit gepinnte Bouncy-Castle-Lightweight-API, bevorzugt `org.bouncycastle:bcprov-jdk15to18:1.86` (aktueller [Herstellerdownload](https://www.bouncycastle.org/download/bouncy-castle-java/)); Java8-kompatible Distribution. Dependency exakt hinzugefügt; Originalartefakt und Maven-SHA512 geprüft. JVM-Vektoren erfolgreich, Android-Kompatibilität durch die unveränderte volle CI nachweisen; kein ungepinnter Versionsbereich und kein stiller Versionswechsel.

Bibliotheks-EC-Domänen/ECDSASigner/RSA-Primitiv verwenden; keine eigene EC-Arithmetik oder aus anderen App-Dateien zusammengesuchte Schlüssel. RSA-Rahmenprüfung wird eng gegen die Tachograph-Spezifikation aufgebaut. Kein globales Security.addProvider/removeProvider, kein Ersatz von Androids BC/Keystore/TLS-Providern und keine Änderung an SQLCipher. JVM- und echte Android-API36-Tests müssen insbesondere Brainpool und P521 absichern.

| Geplanter Bereich | Konkrete Änderung nach Freigabe |
|---|---|
| data/card: CardCoreSignatureVerification.kt | Zustände, Gründe, unveränderliche Ergebnisse/Provenienz |
| data/card: SavedCoreVerificationInputs.kt | Konsistenter exakter Read-Snapshot aus bestehender Receipt-/Metadatenbindung |
| data/card: Gen1RsaCertificateRecovery.kt + Gen1RsaRecoveryKeyCatalog.kt | Enger CSM017–019-Rechenweg und versioniertes öffentliches Einstiegsformat |
| data/card: CardCoreSignatureVerifier.kt | Reine RSA-/ECDSA-Dateiprüfung beider Core-EFs |
| DriverCardImportRepository/SavedDriverImport | Separates abgeleitetes Ergebnis zur exakt gewählten gespeicherten Übernahme; keine Änderung der Save-/Request-Digests |
| app/SavedCardPreviewScreen.kt und AppRoot.kt | Kleine Statuszeile je Core-EF, Auswahl-/Cancellation-Schutz |
| data/build.gradle.kts, gradle/libs.versions.toml | Genau gepinnte Kryptobibliothek, keine Workflow-/SDK-/DB-Absenkung |
| Unit-, SQLCipher-/Repository- und Compose-Tests | Neue unabhängige Kryptovektoren und Provenienz-/UI-Abnahme neben allen bisherigen Prüfungen |

Dateinamen sind konkrete vorgesehene Aufteilung; kleine Zusammenlegungen bei Implementierung sind zulässig, Sicherheitsverträge nicht. Plan-PR #81 war ausschließlich Markdown und wurde vor der getrennten freigegebenen Produktimplementierung übernommen. Kein Schemaexport aus Handarbeit.

## Getrennte Ergebnisse und verständliche Anzeige

| Mathematische Aussage | Bedeutung / Anzeige |
|---|---|
| NOT_RUN | Noch nicht berechnet; während Hintergrundprüfung neutraler Ladezustand |
| VALID | „Signatur mathematisch gültig“ — nur für diesen Schlüssel und diese exakten gespeicherten Bytes |
| INVALID | „Signatur mathematisch ungültig“ — vollständige mögliche Dateiprüfung scheitert; keine Schuld-/Manipulationsbewertung |
| NOT_CHECKABLE | „Nicht prüfbar: …“ — erforderlicher Beleg/Schlüssel fehlt, Herkunft ist nicht konsistent, Schlüssel/Zertifikatsrückgewinnung/Algorithmus nicht verwendbar oder Enginefehler |

Erforderliche typisierte Gründe: NO_SIGNATURE, CAPTURE_FAILED/NOT_ATTEMPTED, NO_ORIGINAL, NO_SIGNING_CERTIFICATE, NO_GEN1_ISSUER, UNKNOWN_GEN1_RECOVERY_KEY, SOURCE_MISMATCH/INTEGRITY_ERROR, CERTIFICATE_RECOVERY_FAILED, UNSUPPORTED_POLICY, INVALID_PUBLIC_KEY, ENGINE_ERROR. Falsche Länge/r/s/PKCS1-Rahmen mit vollständig verfügbarem Schlüssel gehören zur ungültigen **Dateisignatur**, nicht zu fehlenden Voraussetzungen. Misslungene Gen1-Zertifikatsrückgewinnung erlaubt noch kein Urteil über die Dateisignatur. Cancellation ist kein gespeicherter Fehlerzustand.

Daneben immer eigene Aussage **„Zertifikatsvertrauen nicht geprüft“**, unabhängig vom mathematischen Ergebnis. Keine grüne „vertrauenswürdige Karte“, „amtlich geprüft“ oder Gesamtfreigabe. Gespeicherter Aufnahmezustand CAPTURED_UNVERIFIED bleibt unverändert, die neue Aussage wird separat berechnet. In der Ansicht bei erfolgreicher Prüfung klar trennen: „Aufnahme: aufgenommen · 128 Bytes“, „Signaturprüfung: mathematisch gültig“, „Zertifikatsvertrauen: nicht geprüft“; keine widersprüchliche unbeschriftete Doppelanzeige „ungeprüft/gültig“. Andere EF-Anzeigen bleiben beim bisherigen ungeprüften Status.

## Umsetzung nach Freigabe, in dieser Reihenfolge

1. Unabhängige Positiv-/Negativvektoren und Herkunftsvertrag vorbereiten; erste kleine Prüfung der gepinnten Bibliothek auf JVM/Android, ohne Providerregistrierung.
2. Reine Gen1-Recovery und strikte RSA-Dateiprüfung, anschließend alle sechs ECDSA-Domänen für Gen2/V2. Primitive getrennt von DB/UI prüfen.
3. Exakten Read-Assembler und abgeleitete Ergebnisse integrieren; beide Core-EFs/alle drei Generationswerte/alte gespeicherte Quellen, keine neue Migration.
4. Kleine UI-Zeilen und Auswahl-/Abbruchschutz; komplette Regression, vollständige vorgeschriebene Android-CI des endgültigen 40127-Kopfes.
5. Exaktes signiertes Geräteupdate über 40126, Anleitung verständlich im Chat, neue passende eigene Gerätebestätigung. Produktmerge erst nach eigener CI und diesem Gate; frühere Geräteabnahmen reichen nicht dafür.

## Verbindliche Abnahmekriterien

| ID | Muss nachweislich erfüllt sein |
|---|---|
| A1 — Unabhängige Wahrheit | Feste Positive für RSA/SHA1 und sämtliche sechs EC-Domänen, jeweils 0504/0505 und Gen2/Gen2V2. Testsignaturen öffentlich/synthetisch, vorab mit unabhängiger Implementierung (z.B. OpenSSL/Python-cryptography) gegengeprüft; Reproduktionsmethode/Version/Digests dokumentiert. Keine privaten Benutzerquellen; keine positiven Tests nur aus derselben zu prüfenden Library oder bisherigen struktur-only CertificateFixtures. |
| A2 — Gen1-Kette als Rechenvoraussetzung | Öffentliches ERCA→MSCA-Beispiel aus vorhandenem Audit als bekannte Antwort; zusätzlich unabhängig erzeugte synthetische MSCA→Kartenkette/Dateisignatur. Änderungen an Sign/Rest/CAR/CHR/CPI/CHA/Rahmen/n/e abweisen. Fehlender/falscher Einstieg oder C108/C100 ergibt passenden NOT_CHECKABLE-Grund, niemals erfundenen Schlüssel/positives Ergebnis. |
| A3 — Echte Negativprüfung | Einzelbit in Daten/Signatur, fremder gültiger Schlüssel, falscher Hash, Doppelhash, vertauschte Bytes, Abschneiden/Zusatzbytes, r/s=0/n/Überbreite, falsche Domäne und ungültige EC-Punkte. RSA: Sign>=n, falscher DigestInfo-OID/NULL/Hashlänge, ungültige/kurze Paddingstrecke, Restbytes/Prefix-only-Fälschung. Jede vollständige falsche Dateisignatur wird INVALID. |
| A4 — Encoding/Policy | Alle sechs Card_Sign-OIDs/Hash-/Längenkombinationen, P521-feste 66-Byte-Komponenten und kanonische führende Nullbytes. Plain mit 30-Präfix und gültiges High-S akzeptieren; DER/Doppeldeutung nicht. C101-Subject und Aussteller absichtlich auf verschiedener Kurve; Dateipolicy bleibt Subject. Card_MA, unbekanntes OID und ungeeignetes CHA werden nicht als Ersatz verwendet. |
| A5 — Unabhängiges Vertrauen | Mathematisch korrekte Gen2-Dateisignatur unter synthetischem, nicht authentifiziertem C101 bleibt VALID + NOT_EVALUATED. Veränderung nur an ungeprüfter Gen2-Zertifikatssignatur/Gültigkeitszeit macht Dateimathematik nicht automatisch INVALID. Abgelaufene/zeitlich unbekannte Zertifikate nicht als ungültige Dateisignatur melden; nie TRUSTED aus Mathematik, Herkunftslabel oder Gen1-Recovery ableiten. |
| A6 — Eindeutige Quellen | Andere Karte/Child/Anwendung/Generation/EF/Original-/Zertifikatsreferenz, anderer Parent oder jüngere gleichartige Quelle darf kein positives Ergebnis erzeugen. Gleiche Blobbytes in zwei Sessions ergeben getrennte Receipt-/Resultatbindung. Repository-Tamper wird schon durch bestehende Herkunfts-/Fingerprint-Prüfung abgewiesen; nackter Kryptotest eines Datenbitfehlers separat INVALID. |
| A7 — Nichtdestruktive Historie | Alte Quellen mit gültigen vorhandenen Core-Receipts offline prüfbar; alte Quellen ohne Beleg bleiben NOT_CHECKABLE. Fachliches EMPTY/INVALID mit vollständigen Originalbytes kann mathematisch gültig sein, ohne Parserstatus zu reparieren. Request-/Receipt-/Original-/Certificate-Fingerprints, Profile/Karten/Batchs und SQLCipher-Daten vor/nach Prüfung byte-/zeilengenau unverändert; wiederholtes Reopen stabil. Alle Migrationen erhalten; kein Schema23. |
| A8 — Auswahl/Engine/Android | Schneller Quellen-/App-/Archivwechsel und spätes Coroutineresultat mischen keine Urteile; Maskierung erhalten, Abbruch propagiert. Enginefehler nicht als mathematisch ungültig ausgeben. Alle Domänen einschließlich Brainpool und P521 auf JVM und API36 geprüft; Android-Keystore/Providerliste und SQLCipher-Reopen bleiben funktional. Keine pauschale weitere Android-/Hardwareabdeckung behaupten. |
| A9 — Bestehende vollständige Gates | Alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/echt generierter Schemaabgleich, vollständige SQLCipher-/Compose-Suiten API36 und Abschlussjob. Keine Filter/Quarantäne, Upload-/Budget-/Workflowänderung oder neue Migration. Endgültiger Produktkopf 40127 vollständig grün. |
| A10 — Neue eigene Geräteabnahme | Exaktes 40127-Paket prüfen, Update ohne Deinstallation/Datenlöschung. Bestehende signierte Übernahme 40124–40126 ohne Leser öffnen; beide angebotenen Anwendungen/0504+0505 getrennt, Mathematikstatus und Gründe notieren, Zertifikatsvertrauen bleibt ungeprüft. Wechsel/Reopen/Altimport ohne Signatur, normaler neuer Import, übrige Signaturen/Daten unverändert und bestehender Import-Abbruchfall. Reale unerwartete INVALID-Urteile bei vollständigem geeignetem Material vor Merge aufklären; nicht durch pauschale Ausnahme oder nur neuen Import verstecken. |

Positive Geräteabnahme gilt für die tatsächlich getestete Karte/Leser/Telefon-Kombination; synthetische vollständige Kurvenabdeckung ersetzt keine reale allgemeine Hardwareabnahme. Negative Datenmanipulation nur an synthetischen Testkopien, niemals an Tobis gespeichertem Originalbestand.

## Freigabe und verbleibende Produktgates

Tobis Freigabe vom 14.10.2026 betrifft genau den oben begrenzten Umfang. Plan-PR #81 ist übernommen; Produktbasis main `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0`, 40126/Room22. Umsetzung 40127 bleibt bis eigener vollständiger endgültiger CI und neuer positiver Geräteabnahme auf einem separaten Draft-PR. [Krypto- und Quellenbelege](CARD_CORE_SIGNATURE_MATH_EVIDENCE.md), [neue Gerätetestpunkte](DEVICE_TEST_40127.md). Keine weitere EF-/Trust-/DDD-/Schreibfreigabe.
