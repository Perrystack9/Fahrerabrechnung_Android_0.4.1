# 40128 — vollständige mathematische Prüfung vorhandener Dateisignaturen

**Umsetzung freigegeben14.10.2026 11:20 Europe/Berlin, noch nicht neu gerätebestätigt/übernommen.** [Plan-PR #84](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/84), [A1–A8](CARD_STORED_SIGNATURE_COMPLETION_PLAN.md). Planmerge `32d66febb1f5c89970f0ae4c8400cf188e978afc`; Produktbasis bleibt bestätigtes40127/Room22. Endgültigen Produkt-PR, Kopf und eigene volle CI live prüfen.

## Gemeinsame Prüfung und Herkunft

Die bestehende40127-RSA-/ECDSA-Primitive und Gen1-Recovery sind unverändert. `CardSignatureVerifier` enthält den bisher ausschließlich für Core verwendeten Kontrollablauf; `CardCoreSignatureVerifier` bleibt kompatibler Adapter. `verifySavedCoreSignatures` und sämtliche bestehenden Tests bleiben erhalten. `verifySavedSignatures` prüft alle vorhandenen Core-/Identity-/Event/Fault-Belegfamilien aus einem konsistenten validierten Read-Snapshot; Rechnen außerhalb der Transaktion auf Default-Dispatcher, Cancellation propagiert.

Unpersistierte `CardSignedEf`/Familie unterscheiden0504/0505,0501/0520/bedingteV2-0525 und0502/0503. Keine Erweiterung alter persistierter Core-Kinds oder Fingerprintformate. Die bisherigen Stores/load/decode-, Profil-/Karten-/Parent-/Child-/FK-/Digest-/Bootstrap-/Generationsprüfungen bleiben bestehen; neue Leseadapter übernehmen ausschließlich die tatsächlich vom jeweiligen Receipt referenzierten Rohbytes/Zertifikate. Null-Original-FK eines fehlgeschlagenen Captures erhält keinen unsigned Ersatz. Gen1-C108 nur aus demselben Child; öffentlicher Produktionskatalog unverändert.

Ergebnis an Karte, Child/Batch, Anwendung/Generation, EF/Familie, Receipt-Fingerprint, Quellen-IDs und Original-/Signatur-/Zertifikats-/Ausstellerdigests gebunden. Alle Eingaben defensiv eingefroren. Mathematisch gültig/ungültig/nicht prüfbar mit Grund; Zertifikatsvertrauen stets NOT_EVALUATED. Unterschiedliche Aufnahme- und Mathematikbefunde bleiben getrennt. Kleine vorhandene Detailanzeigen, Auswahl-/EF-/Sitzungs-/Archiv-/late-result-Schutz und erneute Maskierung; keine neue Navigation.

Room22, Compilerexporte1–22, Tabellen, persistierte Aufnahmestatus und Original-/Request-/Certificate-/Receipt-Fingerprints unverändert. Keine Migration, DB-Schreiboperation, Cache/Backfill, neue Reader-Befehle oder Signaturaufnahme. BC1.86 Lightweight/Provider und bestehende Sicherheits-/Migrations-/CI-/Gerätegates unverändert.

## Unabhängige feste Vektoren

[Neue Testressource](../data/src/test/resources/stored-math/vectors.zip) und identische Android-Testressource; SHA256 **`ecf080dd616b8b3d32c4d58796816dfea17bde83d243e36f6c15df2c8812b4ea`**.298 öffentliche/synthetische Binärdateien plus SHA256-Manifest. Alte40127-Vektoren samt Pin unverändert. Öffentlicher UI-Test-C101 entspricht exakt dem NIST-P256-Zertifikat dieses Archivs. Keine Testressource im Produkt-APK.

[Generator](../scripts/generate-stored-signature-vectors.py): Python cryptography50.0.2/OpenSSL4.0.3; alle Datensignaturen vor Speicherung unabhängig mit OpenSSL verifiziert, RSA-Recovery-Rahmen durch Python-Public-Exponentiation. Root/MSCA/Card und sechs EC-Schlüssel nur im RAM, niemals private Schlüssel serialisiert. Keine echten Benutzerkartendaten. Alle unterstützten EF-/Generationszuordnungen einschließlich vollständiger leerer Ereignis-/Störungsslots; echte15-Byte-Gen2-0501, vollständige Blank-V2-0525, Doppelhash- und gültige falsche-Schlüssel-Fälle.

Reproduktion: `python3 scripts/generate-stored-signature-vectors.py NEUES_LEERES_VERZEICHNIS`. Methode mit neuen Zufallsschlüsseln reproduzierbar, keine byteidentische Neugenerierung; archivierte feste Antworten mit manifestierten Einzelhashes sind Abnahmebasis. Alte Vektoren nicht überschreiben.

## Tests und Nachweisgrenzen

- **18 isolierte lokale JVM-Tests erfolgreich:** elf bestehende40127-Tests unverändert plus sieben neue Tests; Kotlin2.4.20/Java17/BC1.86. Tatsächliche mathematische Modelle/Parser/Primitiven/Verifier und feste Vektoren kompiliert. Lediglich äußerer Reader-/Import-Kontext und nicht benötigte Android-Typen lokal ersetzt; kein lokaler vollständiger Android-/Repository-/Compose-Nachweis.
- Neue Unit-Matrix prüft jeden EF/unterstützte Generation, RSA und alle sechs EC-Domänen, neue Layoutvarianten, Bit-/Tail-/Signatur-/Schlüsseltausch/Doppelhash, echte typed-Receipt-Adapter und UI-Quellenmatching, missing/source mismatch, unverändertes Trust und defensive Bytes/Enginefehler/Cancellation. Bestehende strikte RSA-/Recovery-/EC-/Providerprüfungen unverändert.
- Neue echte Android-Mathematikmatrix für alle unterstützten EFs/Generationen, Manipulation und Providerliste. Gen1-Positivvektoren mit ausdrücklich synthetischem Testkatalog; kein Hinzufügen dieses Katalogs zur Produktion.
- Neue SQLCipher-Abnahme im bisherigen ungekürzten Pipeline-Test: alle drei Belegfamilien, deduplizierte eigene Originale mit unterschiedlichen Child-Receipts, nur ein ungültiger EF neben gültigen anderen, unsigned Altbestand, echte15-Byte-Gen2, Gen1-unbekannter öffentlicher Einstieg, vollständige tatsächliche DB-Zeilengleichheit/Replay/Reopen und Ablehnung korrupter Identity-/Event-Receipts ohne unsigned/neuen Ersatz.
- Neue Compose-Abnahme in den tatsächlichen Detailansichten: sämtliche Identifikations-EFs, getrennte Ereignis-/Störungsbefunde, EF-/Sitzungs-/Karten-/Receiptwechsel, späte Ergebnisse, Maskierung, fehlender Beleg und nicht vorgeseheneV2/Archiv-Kontexte. Bestehende UI-Tests unverändert.

Die neuen Android-/SQLCipher-/Compose-Abnahmen müssen in der eigenen endgültigen **vollen unveränderten CI** grün sein: alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemavergleich und vollständige API36-Suiten samt Abschlussgate. Keine Filter/Quarantäne. Danach exaktes40128-Paket, tatsächliche Integritätsprüfung nach Download und [neue eigene Geräteabnahme](DEVICE_TEST_40128.md) vor Merge. Frühere40127-Bestätigung ersetzt das neue Gerätegate nicht.

Nach erfüllten A1–A8 und Produktmerge diesen mathematischen Umfang abschließen und den Schwerpunkt auf Benutzeroberfläche/Funktionen für1.0 legen. Keine Zertifikatsvertrauens-/DDD-/Export-/Karten-Schreibfreigabe aus dieser Mathematik ableiten.
