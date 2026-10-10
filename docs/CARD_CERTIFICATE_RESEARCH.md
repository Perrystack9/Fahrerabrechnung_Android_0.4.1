# Zertifikatsquellen: geprüfte Arbeitsgrundlage

Stand: 10.10.2026. Diese Forschungsbranch erweitert weder Android-App noch Datenbank oder Kartenleser. Der spätere Geräte-Kandidat bleibt Draft PR58, Produktkopf `ba9656f0e22980af94883783986367d7bcdf545b`, Schema18/Version40117 und19 mögliche Kartenquellen. Zertifikate sind darin noch nicht als eigene Quellen importiert. Ein Hardwaretest oder neues Paket bleibt frühestens11.10.2026 um00:56:06 Europe/Berlin zulässig.

## Primärquellen

- [EU2016/799, amtlicher JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): Appendix1 §2.41/2.42; Appendix2 TCS43/152–155; Appendix11 TeilA CSM018 sowie TeilB CSM48/134–150. Byte-, Zugriffs- und Profiltabellen visuell geprüft.
- [EU2018/502](https://eur-lex.europa.eu/eli/reg_impl/2018/502/oj/eng): Klarstellung CSM135, vollständige Tag-/Längenbytes; CSM141/146/148; EquipmentType für MA/Sign; DDP037.
- [EU2021/1228](https://eur-lex.europa.eu/eli/reg_impl/2021/1228/oj/eng): DDP035 und MIG019. Gen2V2 verwendet dieselben Schlüssel-/Zertifikatsarten wie Gen2V1; kein erfundenes V2-Zertifikatsformat.
- [JRC-Dokumentation und öffentliche Beispiele](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_st.php.html), [Sample-Dokument v1.2](https://dtc.jrc.ec.europa.eu/iot_doc/Smart%20Tachograph%20sample%20set%20of%20keys%20and%20certificates%20v1.2.pdf), [Beispielarchiv v1.4](https://dtc.jrc.ec.europa.eu/Samples_v1_4.zip).
- [RFC5480](https://www.rfc-editor.org/rfc/rfc5480) und [RFC5639](https://www.rfc-editor.org/rfc/rfc5639): die in CSM48 referenzierten NIST-/Brainpool-OIDs. Deren X.509-Hülle wird nicht auf die CVC-Dateien übertragen.

## Dateien und Generationen

| Generation | Quelle | EF | vollständige Dateigröße |
|---|---|---|---|
| Gen1 | Card_Certificate | C100 |194Byte|
| Gen1 | CA_Certificate | C108 |194Byte|
| Gen2/Gen2V2 | CardMA_Certificate | C100 |204..341Byte|
| Gen2/Gen2V2 | CardSignCertificate | C101 |204..341Byte|
| Gen2/Gen2V2 | CA_Certificate | C108 |204..341Byte|
| Gen2/Gen2V2 | Link_Certificate | C109 |204..341Byte|

Gen1-Dateien enthalten128 Signaturbytes,58 Restbytes und8 offene CA-Referenzbytes. Die übrigen Zertifikatsfelder sind erst durch die vorgeschriebene RSA-Nachrichtenrückgewinnung zugänglich. Den Inhalt nicht als Klartextfelder oder Gen2-TLV interpretieren. Der Gen1-Profilwert01 im zurückgewonnenen Inhalt unterscheidet sich vom Gen2-Wert00.

Gen2-Dateien tragen die vollständige CVC-Hülle. Die PSO-VERIFY-CERTIFICATE-Kommandodaten sind dagegen Körper plus Signatur ohne äußeres7F21: Dieser Kommandopayload beschreibt nicht das vollständige EF. Für das eigenständige Lesen sind SELECT/READ vorgesehen; keine neue PSO-, Authentisierungs- oder Schreiboperation hinzufügen.

## Gen2-Feldstruktur

| Feld | Tag | Wertlänge |
|---|---|---|
| vollständiges Zertifikat |7F21|variabel|
| Zertifikatskörper |7F4E|variabel|
| Profilkennung |5F29|1, Wert00|
| Ausstellerreferenz CAR |42|8|
| Berechtigungs-/Geräteart CHA |5F4C|7|
| öffentlicher Schlüssel |7F49|verschachtelt|
| Kurven-OID / öffentlicher Punkt |06 /86|variabel|
| Inhaberreferenz CHR |5F20|8|
| Beginn / Ende Zertifikatszeitraum |5F25 /5F24|je4, TimeReal|
| Zertifikats-Signatur |5F37|variabel, plain r‖s|

Ein oder zwei Tagbytes, minimale definite DER-Längen; keine indefinite Länge, führenden Null-Längenbytes oder unbemerkten Restbytes. Die Signatur bezieht sich auf die original kodierten Körperbytes einschließlich7F4E und dessen Längenbytes. Nicht nach Dekodierung neu serialisieren. Eine Kryptobibliothek kann eine DER-r/s-Signatur erwarten; die Karte liefert das Plain-Format.

| Kurve | DER-OID-Wertbytes | unkomprimierter Punkt |
|---|---|---|
| NIST P-256 |2A8648CE3D030107|65Byte|
| Brainpool P256r1 |2B2403030208010107|65Byte|
| NIST P-384 |2B81040022|97Byte|
| Brainpool P384r1 |2B240303020801010B|97Byte|
| Brainpool P512r1 |2B240303020801010D|129Byte|
| NIST P-521 |2B81040023|133Byte|

Die Signaturlänge richtet sich nach dem Aussteller-Schlüssel, nicht nach der Kurve des Schlüssels im Zertifikat. Die JRC-Beispiele enthalten tatsächlich einen97-Byte-Punkt mit64-Byte-Signatur, einen129-Byte-Punkt mit96-Byte-Signatur sowie133-Byte-Punkte mit128-Byte-Signatur. Eine reine Ableitung vom Subjekt-OID wäre falsch.

## Reproduzierbare Prüfung der öffentlichen Beispiele

`scripts/research/audit_public_certificates.py` liest ausschließlich `.cert`-Mitglieder des identifizierten JRC-Archivs. Beispiel-Privatschlüssel werden weder gelesen noch extrahiert; Zertifikatsinhalte und Kennungen werden nicht protokolliert. Das Script ist eine lokale Forschungsprüfung und wird von der Android-App nicht verwendet.

```sh
python3 scripts/research/audit_public_certificates.py /pfad/JRC-Samples-v1.4.zip --verify-signatures
```

Für die optionale mathematische Prüfung war lokal cryptography50.0.1 verfügbar. Keine neue Android-/Produktabhängigkeit. Der Lauf hat115 vollständige öffentliche Beispiele über alle sechs Kurven geprüft,352 beschädigte Tag-/Längen-/Dateivarianten abgewiesen und115 Signaturen mit dem jeweils passenden öffentlichen Aussteller-Schlüssel rechnerisch bestätigt. Eine Änderung eines Ablaufzeit-Bytes blieb strukturell lesbar und ließ die Signaturprüfung scheitern. Ergebnis: `scripts/research/public_certificate_audit.json`.

| Dateilänge | Anzahl öffentlicher Beispiele |
|---|---|
|204|22|
|205|23|
|237|1|
|266|22|
|270|23|
|304|1|
|337|14|
|341|9|

Archiv:141741Byte, SHA256 `b8ed453eb99f2a889b888c02b17eed33ee1e5c9c8dd0a6c182ced74a39b0ae5c`. Separat gelesene öffentliche ERCA-Gen2-Root-Datei:205Byte, SHA256 `9e4a98f7d28cff4535b9b2358d380161a09b627dd78814fa1530411c2699f706`, Profil00. Diese Prüfungen bestätigen weder heutige Zertifikatsgültigkeit noch eine produktive Vertrauenskette oder Tobis physische Karte.

## Vorschlag für vollständige, begrenzte EF-Lesung

Die folgende Vorgehensweise ist noch keine Android-Implementierung oder Hardwarebestätigung:

1. Generation und jeweilige Quelle explizit wählen. Gen1 fest194Byte vollständig lesen. Gen2 zunächst einen vollständigen kurzen Präfix lesen und die äußere DER-Länge nur als Kandidat für die vollständige Dateigröße verwenden.
2. Alle Bytes der Kandidatenlänge lesen, kurze nichtleere Antworten bis zum Sollende fortsetzen und die Grenze mit genau einem zusätzlichen Byte prüfen. Nur datenlose, begründete EOF-/Längenstatus gelten als Grenzbeleg; erfolgreiche leere Antworten, Daten mit Fehlerstatus und Timeout nicht umdeuten. Ein weiterer vorhandener Bytewert darf nicht als Padding verworfen werden.
3. Für vollständig nullbelegte/default-leere Dateien gibt es keine DER-Länge. Ein separat geprüfter, begrenzter Suchweg innerhalb204..341 kann ihre tatsächliche EF-Grenze bestimmen. Dafür zunächst die erlaubten Grenzen beweisen; höchstens neun Ein-Byte-Grenzproben wären in diesem Bereich nötig. Anschließend die ermittelte komplette Datei lesen und unverändert erhalten. Mehr als341Byte, abgeschnittene Dateien oder unbekannte Grenzstatus bleiben ein unabhängiger Quellenfehler.

TCS43 nennt6700 oder6Cxx bei Offset+Le>EF-Größe und6B00 bei unpassendem Offset. Der bestehende ApduSession-Helper korrigiert6C00 zu Le256. Ein datenloses6C00 unmittelbar am Dateiende ist deshalb ausdrücklich noch als eigener Prüfpunkt offen; nicht ohne Test als Erfolg oder feste Dateilänge behandeln. Die aktuellen Legacy15/17-Lesertests sichern6700 bereits mittels Ein-Byte-Nachprüfung ab.

Der isolierte synthetische Ablauf `scripts/research/probe_certificate_boundaries.py` prüft jede der138 Dateilängen204..341 mit aktiven, null- und FF-belegten Bytes, vier Antwortstückgrößen und drei EOF-Varianten.4968 vollständige Dateivarianten bleiben bytegleich, höchstens neun Ein-Byte-Grenzproben; neun zu kurze/zu lange oder fehlerhafte Varianten werden abgewiesen. Ein fehlerhaftes CA-EF verhindert die anderen drei Quellen nicht.6C00 bleibt bewusst fehlerhaft. Ergebnis: `scripts/research/certificate_boundary_probe.json`. Der Ablauf verwendet keinerlei Android-, USB-, Netzwerk- oder Karten-I/O und bestätigt keine reale Karten-Grenzantwort.

## Umsetzung und Prüffälle vor einem Zertifikats-PR

- Rohdatei, Art, Generation und vollständige ursprüngliche Länge als unabhängige Quelle speichern; konkrete eigene physische Karte und exakte Importsitzung bleiben zwingend. Keine Quelle aus einer anderen Sitzung oder Gen1/Gen2 als Ersatz übernehmen.
- Bestehende Generationsfilter und historische19-Quellen-Fingerprints beibehalten. Neue Kategorien dürfen alte Übernahmen nicht nachbefüllen. Die generischen Schema18-Metadatentabellen zuerst auf ausreichende Einschränkungen prüfen; keine unnötige neue Schema-Version.
- Dateistruktur, mathematische Signatur und produktives Vertrauen getrennt darstellen. Kartenreferenzen/Serienangaben zunächst verbergen. CHR kann nach CSM146 zur ICC-Serienstruktur passen; dies ersetzt weder eigene Kartenbestätigung noch eine geprüfte Vertrauenskette.
- Zeitfelder eines Zertifikats werden nicht zu Fahr-/Arbeitszeiten, Live-Status oder einem Downloadbeleg. Keine Frist-/Echtheits-/Vollständigkeitsbehauptung allein aus dekodierten Feldern.
- Synthetische Reader-Fälle:204/341Byte, jede zulässige DER-Längenform, kurze Antworten,6700/6C/EOF, Nullbelegung, vorhandene Restbytes, Timeout und Abbruch; jede Datei unabhängig und alle anderen gültigen Quellen erhalten.
- Parser-/Kryptofälle: unbekanntes Profil/OID, Reihenfolge/Duplikate, abgeschnittene Verschachtelung, unzulässige Punktkodierung, gemischte Aussteller-/Subjektgrößen, manipulierter Körper, unbekannter Aussteller, abgelaufene Testkette und fremder selbstsignierter Root. Kein positives Produktvertrauen allein aus einem mitgelieferten Root.
- SQLCipher/Compose-Fälle: genaue alte/neue Sitzung, Fremd-/unbestätigte und physische Ersatzkarte, deduplizierte Originalbytes, atomare Rückrolle, verschlüsselte Wiederöffnung, Kennung auf Antippen und erneutes Verbergen. Erst danach eigener vollständiger CI-Lauf und spätere Gerätebestätigung.

Produktive ERCA-Vertrauensanker, deren Pflege/Verfalls- und Wechselregeln sowie tatsächliche Karten-/Dateisignaturprüfung bleiben weitere Implementierungsschritte. Die vorbereitete Forschung verändert den gegenwärtigen Testkandidaten nicht.
