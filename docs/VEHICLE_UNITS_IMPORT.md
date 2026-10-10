# Unabhängige Fahrtenschreiber-Nutzungen

EF `VehicleUnits_Used` (`0523`) wird für Fahrerkarte Gen2/Gen2V2 unabhängig gelesen und verschlüsselt als vollständige Originaldatei exakt zur eigenen physischen Karte und Übernahme gespeichert. Gen1 versucht diese Datei nicht. Fehlende/ungültige Dateien und Leserfehler beeinträchtigen andere gültige Quellen nicht; Abbruch wird weitergereicht und der Leser geschlossen.

## Amtlich geprüftes Format

Primärbasis: [EU2016/799 JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Appendix1 2.39/2.40/2.106/2.226 (L139/102–103,124,163) und Appendix2 TCS152/154/155 (L139/221–224); [EU2018/502 JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202018-502.pdf), Änderungen2.61 und TCS154 (L85/14,29–30); [EU2021/1228 JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), TCS152/154/155 (L273/45,48–49). ASN.1, EF-Kennung, Bytegrößen, eigener Kapazitätszähler und Generationstabellen am10.10.2026 visuell im amtlichen PDF geprüft.

Die vollständige17-Byte-`Application_Identification` liefert `NoOfCardVehicleUnitRecords` unabhängig an Offset15..16. Gen2 erlaubt84..200 Datensätze, Gen2V2 genau200. Eine alte15-Byte-Anwendung liefert diesen Zähler nicht: Quelle als Lesefehler behandeln, ohne Kapazität aus Nachbarfeldern oder einer anderen Generation zu raten. V2-Erweiterung0525 ist für diese Quelle nicht erforderlich.

Dateigröße: zwei Byte `vehicleUnitPointerNewestRecord`, dann `count × 10` Byte;842..2002 Byte. Bei200 Slots acht READ_BINARY-Anfragen, letzte1792+210; kurze gültige Antworten werden weitergelesen, übergroße/fehlende Antworten verworfen. Nur vollständige Dateien werden weitergegeben.

| Offset im Datensatz | Größe | Originalfeld |
|---|---|---|
|0|4|`timeStamp`: aufgezeichneter Nutzungsbeginn, unsigned TimeReal|
|4|1|`manufacturerCode`: Hersteller des Fahrtenschreibers, Rohcode|
|5|1|`deviceID`: herstellerspezifischer Geräte-Typcode|
|6|4|`vuSoftwareVersion`: vier IA5-Bytes, vollständig erhalten|

Ringreihenfolge ab newest+1; physischer Slot, Duplikate und unsortierte Zeitwerte bleiben erhalten. Nur vollständig00/FF gefüllte Slots gelten als leer. Zeit00/FFFFFFFF bleibt unbekannt, post2038 bleibt unsigned exakt. Software wird nur bei vier druckbaren IA5-Zeichen mit mindestens einem Nicht-Leerzeichen als Text angezeigt; sonst genaue Hexbytes. Keine Byteersetzung, Unicode-Ersatzzeichen oder numerische Umdeutung.

Diese Quelle enthält keinen Kilometerstand, VIN, Endzeit oder eine eindeutige Geräte-Seriennummer. Der Hersteller ist kein LKW-Hersteller. Keine Fahrzeugmodell-/Logo-/Intervall-/Arbeitszeit-/Ruhe-/Kilometer-/Abrechnungsableitung und kein Zusammenführen mit den anderen Fahrzeug-/Vorgangsquellen.

## Speicherung und Bedienung

Additive Migration16→17 ergänzt ausschließlich `card_vehicle_units_imports` und `driver_import_vehicle_units_sources`. Die34 bisherigen Entitäten bleiben erhalten, kein Backfill. Fingerprint enthält eigene physische Karte, Generation, Kapazität und vollständige Bytes; neue Quelle und exakte Sitzungsverknüpfung gehören in dieselbe Gesamttransaktion. Gültige identische Quellen deduplizieren, neue Requests haben eigene Belege, Ersatzkarten behalten eigene Herkunft. Fremde/unbestätigte Karten bleiben temporär. Fingerprint-, Karten- und Sitzungsprüfungen beim Laden verhindern manipulierte Herkunft; der FK allein garantiert nicht die Karte der referenzierten Sitzung.

Historische Requests ohne versuchte neue Quelle behalten Fingerprint und Abdeckung. Historische Auswahl zeigt ausschließlich die verknüpfte Quelle; kein Fallback zu späteren Dateien. Gültige leere Quelle und fehlende Quelle bleiben unterscheidbar. Fahrerkarte→Details→Fahrtenschreiber-Nutzungen zeigt Originalbeginn, Rohcodes und Software in20er-Portionen mit Reset bei Quellenwechsel. Auch eine gültige reine Fahrtenschreiber-Übernahme kann geöffnet werden.

39 neue Testmethoden:13 Parser,11 Reader,9 SQLCipher,1 Room und5 Compose einschließlich echter AppRoot-Navigation. Lokal Projektgrenzen, Actionlint, Shellsyntax,10 CI-Skriptregressionen und SQLite-Migration mit34 erhaltenen Tabellen erfolgreich geprüft.

Schema17/Testversion40115. Echter Compilerexport und vollständige Android-CI einschließlich SQLCipher/Room/Compose am finalen Kopf erforderlich; CI-Herkunft und Abschlussstatus stehen im PR. Updatepaar bleibt vom allgemein bestätigten PR53/Schema14/40112 mit derselben Testsignatur. Neue Import-/Schema-Entwürfe bis neuer Hardwarebestätigung ungemergt; früheste Ausgabe11.10.00:56:06 Europe/Berlin.

Compilerexport17.json unverändert aus CI38085254090 Versuch1, Artefakt11682376429, Archiv-SHA256156225d096bf207aa705ceee666d55fce83ce3c84a3edbccec0c311a65b0287d. Alle34 alten Entitäten identisch, zwei neue. Erster Lauf: Produkt-/Lint-/APK-Builds erfolgreich, Unit-Test-Kompilierung scheiterte am fehlenden ByteBuffer-Testimport; Import korrigiert. Vollständiger Folgelauf am korrigierten Kopf erforderlich, kein erster Full-green- oder Hardwareerfolg behauptet.
Schema17-SHA256: `b95db604827e6ffc726933236962d1931f431bee74d368d3ef09e28adccfb340`.

## Exakte Legacy-Dateigrenze

Der Leser erkennt den alten15-Byte-Anwendungsstand jetzt über den vollständigen Präfix und ein ausdrückliches EOF6282/6B00. Der17-Byte-Stand verlangt beide zusätzlichen Zählerbytes;16Byte, leere erfolgreiche Antworten oder Timeouts sind fehlerhaft. Vier weitere Lesertests sichern diese Grenze ab. Die alte Datei liefert weiterhin keinen VehicleUnits-Zähler.
