# Bedingte Kommunikationsquellen unter MF

Folgeentwurf auf dem unveränderten PR60-Produktkopf `85d0614235e4d9b5384d96d9ee124aab1f9fbc3a` (Tree `78155b21cb76f9c27231360bd78dad0aa5472a22`). Schema 18 bleibt unverändert. Die Quellen gehören jeweils zur ausgewählten eigenen physischen Fahrerkarte und ImportSession, nicht zum Smartphone oder zur aktuellen Verbindung.

## Primärformat und Grenze

- [EU2016/799, Anlage IC, Anlage 2](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): TCS33 und TCS142–147, insbesondere MF-Tabellen und bedingtes Vorhandensein auf L139/216–217.
- [JRC Interoperability Test Specification 1.10, April 2022](https://dtc.jrc.ec.europa.eu/iot_doc/2022-04-08%20Smart%20Tachograph%20Interoperability%20Test%20Specification%20Version%201.10.pdf), Kapitel 8.1–8.3, Dokumentseiten 27–29: unabhängiger Abgleich der Dateikennungen und Fahrer-/Werkstatt-/Kontroll-/Unternehmenskartenquellen. Fahrerkarte enthält keine Calibration-, Sensor_Installation-, Controller_Activity- oder Company_Activity-Dateien.
- [USB-IF CCID 1.1](https://www.usb.org/sites/default/files/DWG_Smart-Card_CCID_Rev110.pdf), Begriffsdefinition „Convention“ und §6.1/6.2: Übertragungskonvention und vom Leser gelieferte Zeichenwerte.
- [pcsc-tools ATR_analysis](https://github.com/LudovicRousseau/pcsc-tools-devel/blob/master/ATR_analysis.in), Blob `7a40f67cd7832716e6ea26c9ff7783c558a282e2`, `analyse_historical_bytes`/`cc`: Compact-TLV Tag 7, dritte Funktionstabelle; Bit `0x40` gilt für die dekodierten Zeichen bei TS `3B` und `3F`. Die UICC-spezifische AOSP-Maskenwahl wird nicht auf den CCID-Pfad übertragen.

| Quelle | EF | Vollständige Länge | Auswertung |
|---|---|---|---|
| ATR/INFO | `2F01` | 7–128 Bytes | Originalbytes und begrenzte äußere BER-TLV-Struktur mit genau einem nichtleeren `7F66`-Template; dessen Inhalt bleibt unverändert und ohne Größeninterpretation |
| Extended_Length | `0006` | 3 Bytes | `02 01 xx`; `01` = T=1, `10` = T=0, `11` = beide; unbekanntes `xx` bleibt unbekannt |

Die genaue ISO7816-4:2013-Auswertung des `7F66`-Inhalts ist **noch nicht implementiert**. Es werden keine ausgehandelten Puffergrößen, Reader-Fähigkeiten oder beschleunigten Übertragungen daraus behauptet. Ein äußerlich gültiges Template ist keine vollständige semantische ISO-Prüfung. Null-/FF-Dateien mit vollständiger amtlicher Länge bleiben gespeicherte leere Quellen.

## Leser und Fehlertrennung

Der ATR muss die erweiterte Länge über eine eindeutig lesbare Compact-TLV-Kartenfähigkeit ankündigen. Fehlendes, proprietäres, abgeschnittenes oder mehrfaches Fähigkeitsobjekt bleibt unbekannt; die neuen EFs werden dann nicht versucht. Ein eindeutig verneinter Hinweis versucht sie ebenfalls nicht. Unbekannt ist keine Bestätigung, dass die Dateien fehlen oder die gesamte Karte vollständig gelesen wurde.

Die Compact-TLV-Kategorien `80` und `00` werden begrenzt gelesen; bei `00` bleiben die letzten drei Statusbytes außerhalb der Datenobjekte. Das Fähigkeitsbit ist stets `0x40` im dritten Wertbyte von `73`. Die physische inverse Konvention ändert die Bedeutung der bereits vom CCID gelieferten Zeichen nicht. Die Regression deckt alle 256 Flagwerte unter beiden Konventionen und beiden Statuslayouts sowie den vollständigen Reader-Pfad ab.

Die Lesung erfolgt nach ATR unter dem aktuellen MF **vor der ersten AID-Auswahl**, mit kurzen SELECT/READ-Befehlen. Die bestehenden ICC-/IC-/DIR-Reihenfolgen bleiben erhalten. Jeder Versuch hat seine eigene Fehleranzeige. Eine angekündigte, aber fehlende Datei ist ein Lesefehler und macht den Lesevorgang teilweise. Nicht angekündigte Dateien ergänzen keine Statuszeile und verändern keine alten Request-Fingerprints. Temporäre MF-Bytes werden erst nach unabhängiger Fahreridentifikation für Gen2/Gen2V2 freigegeben; Gen1, unbekannte und inkonsistente Karten erhalten diese Quellen nicht.

ATR/INFO wird nicht anhand einer möglicherweise beschädigten TLV-Länge zugeschnitten. Mindestlänge wird vollständig gelesen, Maximalgrenze mit einer unkorrektierten Ein-Byte-Probe geprüft und das Ende in höchstens sieben weiteren Proben bestimmt. Kurzantworten müssen in jedem Schritt Fortschritt machen. Extended_Length bekommt ebenfalls eine unabhängige Endprobe. EOF nur bei leerer Antwort mit `6700`, `6B00` oder `6282`; `6C00`, leere Erfolgsmeldungen und Antwortdaten am behaupteten Ende reichen nicht. Abbruch propagiert und schließt den Leser.

Keine erweiterten APDUs, PSO-, Schreib-, Authentisierungs- oder Secure-Messaging-Befehle. EF `0540` wird nicht versucht.

## Speicherung und Bedienung

Zwei getrennte `MASTER_COMMUNICATION_*`-Namensräume in den bestehenden Schema-18-Metadatentabellen. Fingerprints enthalten eigene physische Karte, Art, Generation, Länge und sämtliche Originalbytes. Quellenvereinigung bei Request-Wiederholung, Art-/Karten-Fremdschlüssel und atomare Speicherung bleiben verbindlich. Vorbereitete eigene Imports können bei Gen2V2 **29 unterstützte Quellen** enthalten, nur wenn die beiden bedingten Dateien tatsächlich angekündigt und erfolgreich gelesen sind. Frühere 19-/23-/26-/27-Quellen-Übernahmen werden nicht nachbefüllt.

Unter Fahrerkarte → Details → Kommunikationsangaben erscheinen ausschließlich die Quellen der ausgewählten Übernahme. Fehlend, vollständig leer und unbekannter Protokollcode bleiben unterscheidbar. Keine Zeitabdeckung, Fahrertätigkeit, Echtheit oder erfolgreiche Verbindung wird daraus abgeleitet. Verlaufswechsel, Archivöffnung und Rückkehr setzen die Auswahl korrekt zurück; kein jüngerer Fallback.

Der Vordergrund-Importkoordinator kopiert nun auch Zertifikats-, Originalidentitäts- und Kommunikationsmaps vor einer möglicherweise wartenden Kartenprüfung. Eine externe Mutation der ursprünglichen Maps verändert den geprüften Snapshot nicht. Ausdrückliche Erst-/Ersatzkartenbestätigung liest weiterhin frisch und übernimmt nur den bestätigten frischen Vorgang.

## Prüfung und Freigabe

38 zusätzliche synthetische Testmethoden gegenüber PR60: 12 Parser/Dateigrenzen/Fingerprints, sechs vollständige Reader-Integration, zwei Koordinator, elf reale SQLCipher-/Sitzungstests, fünf Compose-Details und zwei tatsächliche AppRoot-/Verlaufswege. Alle bestehenden Tests bleiben erhalten. Die Dateigrenzenmatrix prüft alle 122 ATR/INFO-Längen mit vier Antwortstückgrößen und drei EOF-Varianten (1464 Fälle).

Lokale Projektgrenzen, zehn CI-Scope-/Schema-Guard-Prüfungen, Shellsyntax und Diffprüfung bestanden. Vollständige CI38172596871 am integrierten Produktkopf efbf49cdf92bd5e5ddb2e042b3e14e9372169756, Tree11e79e492b381f9d515bee796ffea30da3099592: alle vier Jobs erfolgreich, einschließlich Build/Unit/Lint/APKs, Updatepaar40120→40121, unverändertem compiler-generierten Schemaexport und Room/SQLCipher/Compose. [Genaue Prüf-/Geräte-/Merge-Nachweise](CARD_IMPORT_FINALIZATION_HANDOFF.md). PR62 bleibt am exakt geprüften Produktkopf; abschließende Dokumentation auf docs/card-import-finalization-checkpoint-20261011.

Keine Hardwarebestätigung für die neuen Kommunikationsquellen. PR59/PR60 sind nach dem bestandenen Gerätetest40120 unverändert kumulativ gemergt; dieser neue Importentwurf bleibt bis zu seiner eigenen Gerätebestätigung ungemergt. PR60/40120 ist weiterhin das unveränderte bereits bereitgestellte Testpaket; Version40121 ist der neue Entwurfszähler ohne eigene Gerätefreigabe. Die letzte bestätigte Basis ist jetzt40120; das CI-Updatepaar wird entsprechend aktualisiert.
