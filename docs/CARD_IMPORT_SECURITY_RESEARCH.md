# Nachgelagerte Download-/Vertrauensgrenzen – Primärabgleich12.10.2026

Forschung neben dem PR65-Reader-Fundament. **Keine dieser nachgelagerten Funktionen ist dadurch in der App implementiert oder hardwarebestätigt.** Die Speicherung beider Anwendungen hat weiterhin Vorrang. Keine privaten Kartendaten/Schlüssel oder Benutzer-Screenshots wurden für diesen Abgleich in GitHub veröffentlicht.

## 7F66

Der [amtliche Anhang IC, Appendix2 TCS146](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf) verweist für die innere Kodierung ausdrücklich auf ISO7816-4:2013 §12.7.1. Die [offizielle ISO-Seite der Ausgabe2013](https://www.iso.org/standard/54550.html) bestätigt die Ausgabe, stellt den benötigten Detailabschnitt aber im gelesenen Inhalt nicht bereit. Die Nachfolgeausgabe2020 ist kein belegter Ersatz dieser konkret referenzierten Felder. Eine11Byte-Datei oder eine T=1-Angabe liefert keine bestimmten Puffergrößen. Vollständiger Inhaltsdecoder bleibt bis überprüftem Primärformat offen; der gespeicherte Rohbeleg und die äußere Struktur bleiben erhalten. Keine Änderung des APDU-Transports daraus ableiten.

## Signierte Quellen und Standarddownload

Primärbasis: Appendix2 §3.5.13/3.5.14 und Appendix7 DDP035; V2-Ergänzung im [amtlichen Änderungsformat2021/1228](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf). Die vollständigen Texte liegen aus der früheren JRC-Recherche lokal vor; öffentliche Web-Textabfrage dieser großen PDFs scheiterte an der Größenbegrenzung. Die folgenden Schritte sind Modell-/Protokollklärung, keine implementierten Befehle.

Die Karte kann selbst einen Dateihash und anschließend eine Signatur mit ihrem bereits vorhandenen impliziten privaten Schlüssel erzeugen. Ein Host muss dafür keinen privaten Schlüssel erfinden. Gen1 und Gen2 verwenden unterschiedliche Signaturmechanismen. Die Hash-/Signaturaufnahme gehört zum konkreten ausgewählten EF/DF; ein AID-Wechsel oder Reset löscht den temporären Hash. Originalbytes, echte Signatur, konkrete Anwendung und Sitzung müssen deshalb als gemeinsamer unveränderlicher Beleg gespeichert werden. Strukturelles Zertifikatlesen ist kein Ersatz dafür.

DDP035 verlangt beide Anwendungssätze auf Gen2-Karten und bestimmte Dateien/Signaturen. Es nennt außerdem die Aktualisierung von LastCardDownload in beiden betroffenen DFs. Der aktuelle automatische Import ist ein Leseverfahren und schreibt dieses Datum nicht. Ein zukünftiger bewusst ausgelöster Standarddownload braucht dafür einen getrennten belegten Schreib-/Fehler-/Hardwarepfad; bis dahin keinen vollständigen Standarddownload/DDD oder ein aktuelles Download-Datum behaupten. Ein optionaler Export vorhandener Originale wäre entsprechend als Originalquellenexport zu benennen.

Vor dem ersten Signaturpatch: vollständige aktuelle Pflichtdateien-/Ausnahme-Matrix, Hashwahl und echte Signaturlänge je ausgewählter Signatur- bzw. Ausstellerkurve, Fehlercodes, Abbruch und Signatur-/Bytebindung anhand Appendix11 prüfen. Keine generische PSO-HASH-Nachricht statt des standardisierten Datei-Hashverfahrens verwenden. Keine Statusbits als Vertrauensbeweis behandeln.

## Veröffentlichte öffentliche ERCA-Quellen überprüft

Am12.10.2026 direkt über HTTPS von den auf den offiziellen [Smart-Tachograph-](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_st.php.html) und [Digital-Tachograph-Seiten](https://dtc.jrc.ec.europa.eu/dtc_erca_official_documentation_dt.php.html) verlinkten URLs abgerufen:

| Öffentliche Quelle | Archiv-SHA256 | Relevanter Originalbeleg |
|---|---|---|
| [ERCA_Gen2_Root_Certificate.zip](https://dtc.jrc.ec.europa.eu/ERCA_Gen2_Root_Certificate.zip) | `0e409efb053bf05ba4d8327895b65a37631c1dbc9e5c3be956c77cb6e6f65fbd` |205Byte BIN, SHA256 `9e4a98f7d28cff4535b9b2358d380161a09b627dd78814fa1530411c2699f706` |
| [EC_PK.zip](https://dtc.jrc.ec.europa.eu/erca_of_doc/EC_PK.zip) | `b520d74950773622cf38bcc88cb4cec35489f2025a72c68d84837d4d7d966ed5` |144Byte BIN, SHA256 `4cc1ed00c6be8f83c0d8f014282165d037427f829734402215fb59a31a622229` |

Das Gen2-BIN wurde unabhängig lokal als7F21 mit vollständigem kodierten7F4E-Body, öffentlichem Punkt und64Byte-r/s-Signatur geprüft. Die Selbstsignatur über den **vollständigen kodierten7F4E-TLV** besteht mit brainpoolP256r1 (`1.3.36.3.3.2.8.1.1.7`), SHA256 und Python-cryptography50.0.1. CAR und CHR sind jeweils `fd45432001ffff01`. Das ist eine echte mathematische Prüfung dieses veröffentlichten öffentlichen Zertifikats, keine erfundene Testsignatur.

**Grenze:** kein Android-Vertrauensmodul, keine vollständig geprüfte ERCA-/MSCA-/Kartenkette und keine Verifikation irgendeiner Benutzerquelle. Keine Installation der Archive als App-Vertrauensspeicher; die Binärdateien blieben temporäre Forschungsdaten. Das Gen1-Archiv wurde nur bytegenau abgerufen, noch nicht kryptografisch/formatspezifisch verifiziert. Eine Selbstsignatur allein autorisiert kein beliebiges selbstsigniertes Zertifikat als Wurzel. Herkunft/Pinning, Rollen, Referenzen, Signatur-/Ausstellerkurve, Gültigkeit, Link-/Rollover-Fälle und vollständige Kettenregeln bleiben eigene Implementierungs-/Testaufgaben. JRC-Testschlüssel/-Zertifikate sind strikt von veröffentlichten Wurzeln und produktivem Vertrauen zu trennen.

## Geschützter Bereich0540

Das V2-Primärformat ordnetVU_Configuration der ZugriffsbedingungSC5 zu: authentisierte und geschützte Kommunikation; normales ungeschütztes READ reicht nicht. Im Projekt steht kein belegter autorisierter VU-/Authentisierungs-/Secure-Messaging-Pfad mit den dazugehörigen legitimen Schlüsseln bereit. Die eigene Fahrerkarte oder ein öffentliches ERCA-Zertifikat liefert diese Berechtigung nicht. Deshalb weiterhin kein blindes0540-Lesen, keine vorgetäuschte VU-Identität oder erfundene Schlüssel. Ein zulässiger eigener Hardware-/Sicherheitsweg ist eine tatsächliche Voraussetzung für diesen Importbereich.

Fahrzeug-/Spesen-/Abrechnungslogik, künstliche Routen, erfundene Zeitabdeckung oder Vertrauen bleiben außerhalb. Kein Browser/Unteragenten/Bereinigung. GitHub-Actions-Budget2USD und Stop-usage unverändert.
