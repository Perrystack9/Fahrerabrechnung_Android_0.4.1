# MF-Anwendungsverzeichnis je Übernahme

Der kumulative PR60 ergänzt nach Zertifikaten und originalen Kartenangaben eine weitere unabhängige Quelle: das vollständige MF-Anwendungsverzeichnis `DIR` einer erkannten Gen2-/Gen2V2-Fahrerkarte. Testversion40120, Schema18 und stabile Testkennung/-signatur; Updatebasis bleibt der bestätigte PR58-Stand40117. Keine neue Datenbanktabelle und keine Migration.

## Amtliches Format und Grenze

[EU2016/799, Anhang IC, Anhang2, TCS140 und TCS142..145](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf) beschreibt das transparente MF-EF `2F00`, READ ALW/UPDATE NEV und exakt20Bytes. Die zwei vorgeschriebenen, geordneten `61 08 4F 06`-Objekte enthalten `FF 54 41 43 48 4F` und `FF 53 4D 52 44 54`. Diese Werte bezeichnen die Generation1-/Generation2-Anwendungen; sie beweisen keine V2-Struktur und keine erfolgreiche Lesung dieser Anwendungen. Andere Tags/Längen/AIDs/Reihenfolgen werden nicht als unterstütztes Verzeichnis dekodiert. Vollständige00-/FF-Füllung bleibt als leere Originalquelle erhalten.

Der Leser selektiert `DIR` nach den bereits unterstützten ICC-/IC-MF-Dateien und vor der ersten AID-Auswahl. Er liest exakt20Bytes in höchstens20 nichtleeren Teilantworten und prüft anschließend einmal unkorrektiert `READ BINARY(offset20, Le1)`. Nur leere6700/6B00/6282-Antworten beweisen das Dateiende. Zusätzliche Daten, leere9000,6C00/6C01, Fehlerdaten und unbekannte Status werden verworfen. SELECT/READ sind die einzigen ergänzten Befehle. Diese20Bytes werden bis zur unabhängig validierten Fahreridentifikation nur temporär gehalten. Gen1, unbekannte Karten oder fehlgeschlagene Identifikation erhalten in diesem Schritt keine DIR-Quelle und keinen neuen historischen Status. Für Gen1 wird keine Unterstützung seines anderen DIR-Formats behauptet.

Ein DIR-Fehler lässt die anderen Kartenquellen unabhängig lesbar. Der Verzeichniseintrag ersetzt weder die erfolgreiche AID-/Strukturerkennung noch die vorhandene eigene Karten-/Inhaberbindung. Abbruch propagiert und schließt den Transport.

## Speicherung und Ansicht

`MASTER_DIRECTORY` ist ein eigener bekannter Namensraum in den vorhandenen verschlüsselten Schema18-Metadatentabellen. Fingerprints enthalten physische Karte, Generation, Länge und sämtliche Bytes. Die exakte Quellenvereinigung eines Wiederholungsauftrags, kombinierte Karte-/Art-Fremdschlüssel, Deduplizierung und atomare Speicherung gelten auch für DIR. Die anderen drei Decoder überspringen nur diesen bekannten Namensraum; unbekannte Arten bleiben Fehler. Eine vollständige leere Datei zählt als Quelle mit0 dekodierten Einträgen. Ein normales DIR hat2 Einträge ohne zeitliche Abdeckung.

Neue Gen2V2-Vorgänge können27 unterstützte Quellen enthalten. Frühere19-/23-/26-Quellen-Vorgänge behalten ihre Status, Fingerprints und fehlenden Quellen. Es gibt keine Nachbefüllung aus späteren Übernahmen. Auch DIR allein kann gespeichert und über die normale letzte Übernahme geöffnet werden.

`Fahrerkarte → gespeicherte Daten → Fahrerkarten-Details → Anwendungsverzeichnis` zeigt genau die ausgewählte Sitzung. Fehlend und vollständig leer bleiben unterscheidbar. Historische Auswahl, Archiv-/Quellenwechsel und erneutes Öffnen setzen den ausgewählten Stand passend. Das Verzeichnis liefert keine Aktivitätszeitspanne, Kilometer, Arbeits-/Ruhezeit oder kryptografische Vertrauensaussage.

## Prüfung

31 zusätzliche Methoden:5 Parser-,5 begrenzte APDU-Leser-,5 tatsächliche Fahrerkarte-Leser-,11 SQLCipher-/Room-/Pipeline- und5 Compose-/AppRoot-Tests. Alle20 Teilgrößen und drei erlaubten EOF-Antworten, jede einzeln veränderte Formatposition, alle kürzeren Dateien, ungültige Endantworten, Abbruch, unabhängige Fehler, alte19/23/26-Sitzungen, genaue Wiederholung, Deduplizierung, Fremd-/Ersatzkarten, Rollback, verschlüsselte Wiederöffnung, Fremdschlüssel, beschädigte Originalbytes und tatsächliche Navigation werden geprüft. Bisherige Testmethoden bleiben erhalten.

CI38122321988 fand außerdem einen widersprüchlichen alten Originaldateien-Testdatensatz:0501 meldete200 Fahrzeugplätze, der Transport lieferte zwei. Der Datensatz meldet jetzt die tatsächlich gelieferten zwei Plätze und den16-Byte-Aktivitätsring; die unveränderten positiven Quellenprüfungen und die18-Byte-Endprüfung bleiben erhalten. Keine Produktionsgrenze wird dafür gelockert.

Lokale Projektgrenzen,10 CI-Regressionsprüfungen, Shellsyntax und Diff-Prüfung bestanden. Vollständige Android-/Emulator-CI am endgültigen gemeinsamen Kopf ist vor der APK-Ausgabe erforderlich. Hardwaretest noch offen; PR59/60 bleiben bis zur nächsten ausdrücklichen Gerätebestätigung ungemergt. [Gebündelte Geräteanleitung](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md).
