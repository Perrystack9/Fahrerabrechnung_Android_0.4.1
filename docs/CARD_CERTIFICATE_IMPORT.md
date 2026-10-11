# Zertifikatsdateien als unabhängige Kartenquellen

Stand: 11.10.2026. Begrenzter Folgeschritt auf dem von Tobi allgemein bestätigten PR58-Stand: vollständige Zertifikats-EFs lesen, strukturell dekodieren, verschlüsselt zur exakten eigenen Karte und Übernahme speichern und in Fahrerkarte → Details → Zertifikate anzeigen. Keine kryptografische Echtheits- oder Vertrauensbehauptung, keine neuen Kartenschreib-/PSO-/Authentisierungsbefehle. Der endgültige CI-Kopf und die Prüfnachweise stehen in der PR-Beschreibung.

## Amtliches Format

- [EU2016/799, amtlicher JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): Appendix1 §2.41/2.42, Appendix2 TCS43/150/152–155, Appendix11 TeilA CSM018 und TeilB CSM134–150. Zugriffstabellen und Format-/Größentabellen auf den gedruckten Seiten 218/219/221/222/376/377 visuell geprüft.
- [EU2018/502](https://eur-lex.europa.eu/eli/reg_impl/2018/502/oj/eng): CSM135-Klarstellung zu den vollständigen Tag-/Längenbytes. Die vollständige EF-Hülle 7F21 bleibt erhalten; der PSO-Kommandopayload ist kein EF-Format.
- [EU2021/1228](https://eur-lex.europa.eu/eli/reg_impl/2021/1228/oj/eng): DDP035/MIG019; Gen2V2 verwendet die bestehenden Gen2-Zertifikatsarten.
- [JRC-Dokumentation und öffentliches Beispielarchiv v1.4](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_st.php.html). Vorbereitete Forschung: [CARD_CERTIFICATE_RESEARCH.md auf dem separaten Forschungsbranch](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/blob/research/card-certificate-format/docs/CARD_CERTIFICATE_RESEARCH.md).

| Generation | Kartenquelle | EF | vollständige Länge |
|---|---|---|---|
| Gen1 | Card_Certificate | C100 |194 Byte|
| Gen1 | CA_Certificate | C108 |194 Byte|
| Gen2/Gen2V2 | CardMA_Certificate | C100 |204–341 Byte|
| Gen2/Gen2V2 | CardSignCertificate | C101 |204–341 Byte|
| Gen2/Gen2V2 | CA_Certificate | C108 |204–341 Byte|
| Gen2/Gen2V2 | Link_Certificate | C109 |204–341 Byte|

Lesung ausschließlich nach Auswahl der konkreten erkannten Kartenanwendung und gültiger Fahreridentifikation. Gen1 versucht C101/C109 nicht. Ein Fehler einer Zertifikatsdatei blockiert keine andere Datei. Abbruch wird weitergegeben, Leser im bestehenden finally geschlossen. Es werden ausschließlich SELECT/READ und die vorhandene begrenzte Antwortbehandlung verwendet.

## Vollständige Dateigrenze

Gen1 liest genau194 Bytes und prüft den zusätzlichen Byteoffset194. Gen2 liest zunächst204 Bytes, beweist die Maximalgrenze mit einer Ein-Byte-Probe an341 und bestimmt die vollständige EF-Grenze durch eine begrenzte binäre Suche innerhalb204..341. Höchstens neun Ein-Byte-Grenzproben; anschließend alle restlichen Bytes exakt lesen. Dadurch gilt derselbe vollständige Lesepfad auch für null-/FF-belegte Dateien, ohne einer DER-Länge zu vertrauen oder Padding zu verwerfen.

Kurze nichtleere erfolgreiche Antworten werden zusammengesetzt. Nur datenlose6700/6B00/6282-Antworten auf die Ein-Byte-Grenzprobe sind Grenzbelege. Leere9000-Antworten, zusätzliche Antwortdaten, Daten mit Fehlerstatus, unbekannte Statuswörter, Timeout oder6C00/6C01 an der Grenzprobe bleiben Fehler. Die neue optionale Deaktivierung der6C-Längenkorrektur gilt für Grenzproben; normale Datenlesungen behalten die vorhandene Korrektur. 6C-Fehlerantworten mit Daten werden vor einem Retry strikt abgewiesen.

## Strukturelle Dekodierung und Grenzen

Gen1 erhält die128 Signaturbytes,58 Restbytes und acht offenen Ausstellerreferenzbytes. Die verdeckten Zertifikatsfelder benötigen RSA-Nachrichtenrückgewinnung und werden nicht als Klartext erfunden.

Gen2 validiert die komplette CVC-Hülle, minimale definite DER-Längen, genaue Feldreihenfolge, feste Feldgrößen, Profil00, Anwendungskennung, die sechs spezifizierten NIST-/Brainpool-Kurven-OIDs und die Kodierungsgröße eines unkomprimierten Punktes. Der originale Körper einschließlich7F4E-/Längenbytes und die Plain-r‖s-Signatur bleiben erhalten. Die erlaubte Signaturlänge stammt vom Aussteller und wird nicht aus der Subjektkurve abgeleitet. Unbekannte Geräteartcodes und unbekannte/widersprüchliche unsigned Zeitwerte bleiben original. Diese Dekodierung prüft weder die elliptische Kurvengleichung noch Signaturen, Fristgültigkeit oder eine vertrauenswürdige ERCA-Kette. Die Oberfläche sagt ausdrücklich, dass Signatur und Vertrauenskette noch nicht geprüft sind.

Zertifikatszeiträume sind keine Fahr-/Arbeits-/Import-Zeitabdeckung. Aussteller- und Inhaberreferenzen bleiben zunächst verborgen, auf Antippen einblendbar und bei Kartenquellen-/Zertifikatsarten-/Ansichtswechsel erneut verborgen. Keine Profilbindung aus Referenzen, kein Vergleich mit Karten-/Chipkennung als Echtheitsnachweis.

## Speicherung und Historie

Die vorhandenen Schema18-Tabellen card_metadata_imports und driver_import_metadata_sources tragen alle Originalbytes. Zertifikate haben den eigenen Namensraum CERTIFICATE_<Art> und einen separaten Fingerprint mit physischer Karten-ID, Art, Generation und vollständigen Bytes. Die Metadaten- und Zertifikatsdecoder bleiben getrennt; die gemeinsame Sitzung prüft die exakte Vereinigung beider Quellarten. Die bestehenden zusammengesetzten Fremdschlüssel sichern Karten-/Art-/Sitzungsherkunft.

Keine Datenbank-/Migrations-/Schemaexportänderung, keine historische Nachbefüllung oder spätere Ersatzquelle für eine alte Übernahme. Leere vollständige Dateien bleiben gespeicherte Quellen mit null dekodierten Einträgen. Fremde/unbestätigte Karten schreiben keine Quellen. Transaktion und Ergebnisbeleg bleiben atomar; deduplizierte Dateien teilen nur denselben exakten Ursprung derselben physischen Karte. Alte19-Quellen-Vorgänge behalten unveränderte Fingerprints und Status. Neue Gen2V2-Lesevorgänge können23 unterstützte Quellen enthalten; das ist weiterhin kein vollständiger Karten-/Signaturdownload.

## Prüfung und Update

54 neue Testmethoden:14 Parser,10 begrenzte Dateileser/APDU, acht echte DriverCardReader-Wege,13 SQLCipher-/Pipelinefälle und neun Compose-/AppRoot-Wege. Keine alten Testmethoden entfernt. Darunter sämtliche138 möglichen Gen2-EF-Längen mit Null-/FF-Belegung, vier Antwortstückgrößen und drei EOF-Varianten (3312 Varianten),115 öffentliche JRC-Dateien zusätzlich in beiden Gen2-Versionen, unbekannte/fehlerhafte Kodierung, gemischte Aussteller-/Subjektgrößen, neue Karten, genaue Historie, Fremd-/unbestätigte Karten, Wiederöffnung, Rückrolle und Referenzmaskierung. Vollständige Android-CI einschließlich unverändertem KSP-Schemaexport18 ist am finalen Kopf erforderlich.

Testversion40118 auf bestätigtem PR58/40117/Schema18, unveränderte feste Testsignatur und Paketkennung. Das CI-Updatepaar baut die bytegleiche PR58-Produktbasis aus Merge1b592d627d5d18db9fc909f5c8cf2c796fd8578b als40117 und den neuen Kopf als40118. Nutzer-ZIP später nur mit Update-APK, Anleitung, Herkunft und Prüfsumme. [Gebündelter Gerätetest](CARD_CERTIFICATE_DEVICE_TEST.md), nach dem beauftragten zweistündigen Arbeitsblock und vollständiger grüner CI; neue Importänderung bis zur späteren Gerätebestätigung ungemergt.

Öffentliche Testressource: ausschließlich115 .cert-Mitglieder aus dem JRC-Archiv, keine privaten Beispielschlüssel gelesen oder extrahiert. Archiv-SHA256 b8ed453eb99f2a889b888c02b17eed33ee1e5c9c8dd0a6c182ced74a39b0ae5c; Hex-Ressource5632e31224be5da4d97fc45650e9d39be1b7c06aee98ce35799941a5b962933f. Lokale strukturelle-/Shell-/CI-Scope-Prüfungen sind keine Android- oder Hardwarebestätigung.
