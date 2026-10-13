> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Karten- und Chipkennung als eigene Originalquellen

`EF ICC` (`0002`,25Byte) und `EF IC` (`0005`,8Byte) gehören zum Karten-Hauptverzeichnis MF. Gen1, Gen2 und Gen2V2 werden unterstützt. Ausschließlich SELECT/READ; kein zusätzlicher Reset, kein Schreibbefehl, keine Änderung der Kartenbindung oder Profil-/Ersatzkartenregeln.

## Amtlich geprüfte Reihenfolge und Daten

Primärquelle [EU2016/799 bei JRC](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Anlage1 Abschnitte2.11,2.13,2.23,2.65,2.72 und Anlage2 TCS18,39,142–144, Seiten L139/93,94,97,112–113,116,179,187,216–217. ASN.1-, Byte-, Zugriffs- und Reset-/Auswahltabellen visuell geprüft.

Nach ATR ist MF das aktuelle Verzeichnis (TCS18). Beide EFs werden mit dem bereits vorhandenen SELECT-EF-Befehl unter diesem Verzeichnis gelesen, **vor** der ersten AID-Anwendungsauswahl. Ein SELECT auf `3F00` über den EF-Auswahlbefehl wäre nicht die spezifizierte Zugriffsfolge und wird nicht eingeführt. Daten bleiben zunächst nur im temporären Lesergebnis; erst nach erfolgreicher Fahreridentifikation werden sie in die Quellenmap der erkannten Generation übernommen. Unbekannte/andere Karten oder inkonsistente Identifikation geben diese Quellen nicht zurück. Fehler einer MF-Datei verhindern die andere MF-Datei oder die Anwendung nicht. Abbruch propagiert und schließt den Leser.

| Quelle | Exakte Originalfelder | Aussagegrenze |
|---|---|---|
| EF ICC,25Byte | Taktstopp1; erweiterte Serienkennung8 = unsigned Serienwert4, Monat/Jahr2BCD, Typ1, Hersteller1; Genehmigung8IA5; Personalisierer1; Einbetter5 = Land2IA5, Modulcode1, Herstellerinfo2; IC-Kennung2 | Kein vollständiges Herstellungsdatum, keine Jahrhundertannahme, keine neue Profilbindung und kein Echtheitsnachweis |
| EF IC,8Byte | Serienkennung4OCTET-Bytes und Herstellerreferenz4OCTET-Bytes | Alle acht Bytes bleiben unverändert, keine numerische/endianabhängige Umdeutung; Serienbytes allein sind keine vollständige Chipkennung |

Unbekannte Rohcodes, BCD oder Textbytes verwerfen keine anderen Felder. Der zweistellige Monat/Jahr-Code kann Herstellung **oder** Serienvergabe bezeichnen; keine Umrechnung in einen Timestamp. Vollständig00/FF-Dateien sowie der amtliche gelöschte ICC-Stand mit leerer Genehmigung sind gültig leer. Teilweise gefüllte Dateien bleiben eigene Originaldatensätze. Serienwerte/Chipkennung sind zunächst verborgen und erscheinen nur nach ausdrücklichem Antippen; der Zustand wird beim Quellen-/Ansichtswechsel zurückgesetzt. Keine Kennungen oder Roh-APDUs in Protokollen.

## Herkunft und alte Übernahmen

Wiederverwendet die beiden generischen Tabellen der Metadatenquellen: Schema18 unverändert, **keine neue Migration oder Tabelle**. Jede MF-Datei hat einen eigenen Fingerprint aus bestätigter physischer Fahrerkarte, Art, erkannter Anwendungsgeneration und vollständigen Bytes. Atomare Speicherung im selben Vorgang, zusammengesetzte Art-/Karten-/Quellen-Fremdschlüssel und zusätzliche Prüfungen bei Laden. Ein MF-Serienwert ersetzt niemals die bestehenden Karten-/Fahrermerkmale und bestätigt keine fremde Karte.

Alte technische Resultate ohne MF-Quellen behalten ihre Fingerprints, Status, Daten und Quellenabdeckung. Alte17-Quellen-Vorgänge erhalten keine neue MF-Quelle als Ersatz oder Nachbefüllung. Neue vollständige Vorgänge können19 Quellen enthalten. Der echte KSP-Export18 aus PR57 bleibt unverändert: SHA25670340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b. Releasezähler40117, stabile deviceTest-Kennung/Signatur; Updatebasis PR53/40112/Schema14.

## HISTORISCH - Prüfung

37 zusätzliche synthetische Tests:13 Parser,8 Leser,9 SQLCipher,7 Compose einschließlich zwei weiterer echter AppRoot-/Verlaufswege. Vor-AID-Reihenfolge wird durch einen Leser mit getrennten Verzeichnissen geprüft. Abgedeckt sind unter anderem Rohbytes, unsigned Serienwert, unbekannte BCD-/Typcodes, kurze Antworten, unabhängige Fehler, Abbruch, Fremd-/unbestätigte Karten, vollständiges Rückrollen, gleiche Nummer bei physischer Ersatzkarte, historische genaue Sitzung, verschlüsselte Wiederöffnung und Verbergen/Zurücksetzen.

Finale Android-CI inklusive APK-/Updatepaar-, Schema-, Room/SQLCipher/Compose-Prüfung steht separat aus; keine Hardwarebestätigung. Es wird noch keine vollständige Karte behauptet. Zertifikate, bedingte MF-Info-/Längendateien und VU_Configuration müssen separat amtlich geprüft und implementiert werden. Kumulative Anleitung [CARD_MASTER_FILE_DEVICE_TEST.md](CARD_MASTER_FILE_DEVICE_TEST.md).
