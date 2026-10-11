# Feste Kartenquellen als unabhängige Übernahme

Gen1, Gen2 und Gen2V2: `Driving_Licence_Info` (`0521`,53Byte), `Current_Usage` (`0507`,19Byte), `Card_Download` (`050E`,4Byte). Alle Dateien werden ausschließlich mit SELECT/READ gelesen. Keine Aktualisierung von Card_Download; kein Karten-Schreibbefehl. Feste Längen brauchen keinen Ringzähler und bleiben unabhängig von Application_Identification-Kapazitäten und V2-Erweiterung.

## Amtlich geprüfter Aufbau

Primärquelle: [EU2016/799 bei JRC](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), AnhangIC, Anlage1 Abschnitte2.16,2.18,2.89,2.99,2.166/167 und Kapitel4 sowie Anlage2 TCS150/154 und Dateitabellen L139/218–223. Relevante ASN.1-/Byte-/Zeichensatztabellen visuell geprüft; Gen2V2-Dateiliste auch mit [EU2021/1228](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), L273/45, abgeglichen.

| Datei | Originalfelder | Grenze der Aussage |
|---|---|---|
| Driving_Licence_Info | Name36Byte: Zeichensatzcode+35Textbytes; Staat1Byte; Führerscheinnummer16IA5-Bytes | Keine Klassen, Ablauf-/Prüfdaten oder Aussage zur Fahrerlaubnisgültigkeit |
| Current_Usage | ursprünglicher Beginn4Byte; Zulassungsstaat1Byte; Zeichensatzcode+13Kennzeichenbytes | Gespeicherter Kartenstand; kein Live-Fahrzustand, Abschluss oder erfundene Einsatzdauer |
| Card_Download | ursprünglicher Download-Zeitwert4Byte | Keine App-Importzeit, Downloadbestätigung, Frist-/Erinnerungsberechnung oder Kartenaktualisierung |

Unbekannte Zeichensätze werden nicht geraten. Definierte Codes1,2,3,5,7,9,13,15,16,80,85 werden bei unterstütztem und fehlerfreiem Decoder ausgewertet; andernfalls bleiben Code und sämtliche Bytes unverändert verfügbar. Kontrollzeichen werden nicht als lesbarer Text dargestellt. IA5-Nummer wird nicht numerisch umgerechnet; unbekannte Bytes bleiben erhalten. Die Führerscheinnummer wird nur nach ausdrücklichem Antippen angezeigt; der Anzeigenzustand wird bei Quellen-/Ansichtswechsel zurückgesetzt.

Vollständig00/FF-Dateien und der amtliche gelöschte Stand ohne Inhaltsfelder sind gültige leere Quellen. Teilweise unbekannte Werte bleiben Datensätze. Unbekannte Zeit0/FFFFFFFF wird nicht aus einer anderen Datei oder Importzeit ersetzt. Positive unsigned32-Bit-Zeiten einschließlich nach2038 bleiben exakte UTC-Zeitpunkte. Es gibt keine Verknüpfung mit einer Fahrzeugnutzung, Aktivität oder Arbeits-/Ruhezeit.

## Speicherung und Herkunft

Zwei additive Tabellen in Schema18: `card_metadata_imports` und `driver_import_metadata_sources`. Jede Datei bleibt eine eigene unveränderte, verschlüsselte Quelle, mit Fingerprint aus physischer eigener Karte, Art, Generation und vollständigen Bytes. Der Sitzungsschlüssel enthält die Quellenart; zusammengesetzter Fremdschlüssel bindet Quelle, Karte und Art. Laden prüft zusätzlich tatsächliche Sitzungskarte, Art, Generation, Länge und Fingerprint. Alle Quellen einer angenommenen Übernahme werden atomar gespeichert; ein Speicherfehler rollt sämtliche Quellen-/Sitzungsschreibvorgänge zurück.

Fehlend, Lesefehler, ungültig und gültig leer sind getrennte Status. Ein Fehler einer Art verwirft die anderen gültigen Quellen nicht. Gleiches Material erzeugt keine doppelte Quelle, aber einen eigenen Vorgang; abweichende Wiederverwendung derselben Vorgangs-ID wird abgelehnt. Fremde oder noch unbestätigte Karten schreiben nichts. Eine physische Ersatzkarte behält eigene Herkunft, auch bei gleicher Nummer und gleichen Bytes.

Migration17→18 verändert keine der36 bisherigen Tabellen und füllt keine alten Quellen/Quellstatus nach. Alte technische Resultate mit leerer Metadatenmap behalten Fingerprint und Abdeckung. Neue vollständige Resultate können17 unabhängige Quellen enthalten. Das ist weiterhin keine Behauptung vollständiger Fahrerkarte: Zertifikate, ICC-/IC-Information und VU_Configuration benötigen eigene geprüfte Implementierung.

## Verifikation dieses Entwurfs

14 Parser-,8 Leser-,10 SQLCipher-,1 Room- und9 Compose-/Navigationstests hinzugefügt. Nur synthetische Karten. Unter anderem drei Generationen, unsigned Zeiten, komplette Dateien, rohe Codes und Textbytes, unabhängige Fehler, Abbruch, ausschließlich Lesebefehle, Wiederöffnung, Wiederholungen, Rückrollen, Migration, physische Kartenprovenienz, Fremdkarten sowie Verbergen/Zurücksetzen der Nummer und genaue ältere Sitzung.

Statische Projekt-/Diff-/Workflowprüfungen und echte Android-CI werden vor Freigabe geprüft. Der unveränderte KSP-Compilerexport18 aus CI38086827094/Artifact11682788686 ist übernommen: SHA25670340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b. Alle36 bisherigen Entitäten sind identisch; SQLite17→18 stimmt für alle38 Tabellen/Schlüssel/Indizes mit dem Export überein. Kein handgefertigter Export. Die vier zusätzlichen15/17-Byte-Grenztests aus PR56 sind ebenfalls übernommen; finale vollständige CI folgt. Dieser Implementierungsstand ist noch keine Hardwarebestätigung. Kumulativer Geräteplan: [CARD_METADATA_DEVICE_TEST.md](CARD_METADATA_DEVICE_TEST.md).
