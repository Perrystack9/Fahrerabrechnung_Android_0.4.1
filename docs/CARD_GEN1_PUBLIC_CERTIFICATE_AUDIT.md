> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Gen1: öffentliches Wurzelformat und echte Zertifikatsprüfung

Forschungscheckpoint12.10.2026 nach dem bestätigten PR65/40122. Keine neue App-/Reader-/Vertrauensfunktion. Verwendet wurden ausschließlich veröffentlichte öffentliche ERCA-/Mitgliedstaatdaten, keine Benutzerkarte, privaten Schlüssel oder Kartenrohdumps.

## Primärbelege und tatsächliche Prüfung

Die [offizielle ERCA-Dokumentationsseite](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_dt.php.html) verlinkt [EC_PK.zip](https://dtc.jrc.ec.europa.eu/erca_of_doc/EC_PK.zip). Erneut direkt über HTTPS geladen; Archiv-SHA256 `b520d74950773622cf38bcc88cb4cec35489f2025a72c68d84837d4d7d966ed5`. BIN, TXT-Hex und Base64 im herstellerspezifisch gerahmten `EC_PK.pem` ergeben exakt dieselben144Bytes, SHA256 `4cc1ed00c6be8f83c0d8f014282165d037427f829734402215fb59a31a622229`. Das PEM enthält die Grenze `BEGIN ERCA PK`; es ist kein gewöhnliches SubjectPublicKeyInfo-PEM.

| Feld im veröffentlichten144Byte-Schlüssel | Byteoffset, einschließlich | Länge / tatsächlicher Wert |
|---|---|---|
| Autoritäts-/Schlüsselreferenz | 0–7 |8Bytes, `fd45432000ffff01` |
| RSA-Modulus n | 8–135 |128Bytes, tatsächlich1024Bits, ungerade |
| RSA-Exponent e | 136–143 |8Bytes, tatsächlich65537 |

Aus n/e ein RSA-PublicKey-Objekt mit Python-cryptography erzeugt; Konstruktion erfolgreich. Dieser Schlüssel enthält keine Selbstsignatur, Zertifikatsrolle oder Gültigkeitszeit. Herkunft und späteres App-Pinning bleiben eigene Vertrauensentscheidungen.

Als echte Signaturprobe wurde das auf der [offiziellen Gen1-Liste](https://dtc.jrc.ec.europa.eu/dtc_public_key_certificates_dt.php.html) veröffentlichte Mitgliedstaat-Kartenzertifikat Nordmazedonien `244D4B2008FFFF01` verwendet: [MK_TCC2.bin](https://dtc.jrc.ec.europa.eu/BIN_DT/MK_TCC2.bin),194Bytes. SHA1 `c1f55da30fd6c47994971f460d9fb25e86585c12` stimmt mit der JRC-Liste überein; zusätzlich berechnete SHA256 `5b2e8f8a0d8cca84c8c9518c33706fefc9b34ab857a448b5c4632b9f2ee97f6a`.

Primärformat: [AnhangIC Appendix11 TeilA CSM003/004,014,017–019](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), amtliche Seiten L139/344–349. RSA-Nachrichtenrückgewinnung und SHA1 wurden tatsächlich gerechnet. Die veröffentlichte Datei wurde nicht als ASN.1/X.509- oder gewöhnliche PKCS1-Signatur interpretiert.

## Reproduzierbare Bytebindung

| Teil der194Byte-Zertifikatsdatei | Offset, einschließlich | Länge |
|---|---|---|
| RSA-Signatur | 0–127 |128Bytes |
| Nicht rückgewinnbarer Inhaltsrest | 128–185 |58Bytes |
| Äußere Ausstellerreferenz | 186–193 |8Bytes |

`s = unsignedBigEndian(cert[0:128])`; Bereich `0 < s < n` verlangen. `recovered = (s^e mod n)` als genau128Big-Endian-Bytes. Tatsächliche Rückgewinnung hat Header `6A`,106Inhaltsbytes,20Hashbytes und Trailer `BC`. Klarinhalt = `recovered[1:107] + cert[128:186]` (164Bytes); sein SHA1 ist gleich `recovered[107:127]`.

Zusätzlich geprüft: CPI `01`; innere CAR `content[1:9]` und äußere CAR stimmen mit dem veröffentlichten Wurzelschlüssel überein. CHR `content[20:28]` ist genau `244d4b2008ffff01`. CHA `content[9:16]` ist `ff544143484f00`, also die Gen1-Tachograph-Anwendung und Mitgliedstaattyp00. EOV ergibt `2033-09-24T00:00:00Z`, passend zur JRC-Liste. Der zertifizierte öffentliche Schlüssel aus `content[28:156]` / `content[156:164]` hat ebenfalls1024Bits / Exponent65537; PublicKey-Konstruktion erfolgreich.

**Ergebnis: tatsächliche mathematische Prüfung dieses öffentlichen ERCA→Mitgliedstaat-Zertifikats bestanden.** Die sechs folgenden Veränderungen derselben öffentlichen Datei wurden tatsächlich abgewiesen; es wurde keine Signatur mit erfundenen Schlüsseln erzeugt.

| Veränderung | Beobachtete Ablehnung |
|---|---|
| Datei auf193Bytes gekürzt | Länge/Ausstellerzuordnung |
| Zusätzliches Byte,195Bytes | Länge/Ausstellerzuordnung |
| Signaturrepräsentant128Bytes `FF`, außerhalb0…n−1 | RSA-Bereich |
| Signaturbyte0 um ein Bit verändert | Rückgewinnungsrahmen |
| Inhaltsrestbyte128 um ein Bit verändert | Inhalts-Hash |
| Äußere Ausstellerreferenz, Byte193 um ein Bit verändert | Ausstellerzuordnung |

## Übertragbare Vorbereitung und verbleibende Grenze

Ein späterer begrenzter Gen1-Verifikator braucht genau diese Längen-/Bereichs-/Rahmen-/Hash-/Referenzprüfungen, anschließend Rollen- und Gültigkeitsprüfung des rückgewonnenen Inhalts. Ein strukturell vollständiger194Byte-Beleg allein erlaubt kein Vertrauen. Generationsabhängige Ausstellerwahl und die Verbindung zur konkreten gespeicherten Anwendung/Sitzung bleiben verpflichtend.

Diese Forschungsprüfung umfasst einen veröffentlichten Mitgliedstaatbeleg. Keine vollständige ERCA/MSCA/Kartenkette, aktuelle Benutzerquelle, Kartendownload-Signatur, Widerrufspolitik, App-Vertrauensspeicher oder geschützten0540-Zugriff implementiert. [Aktueller Primär-/Sicherheitsabgleich](CARD_IMPORT_SECURITY_RESEARCH.md) · [nächste Importpunkte](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md). Produktcode, Schema19, Befehle und bestätigte40122-APK unverändert.
