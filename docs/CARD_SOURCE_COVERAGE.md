# Abdeckung der Fahrerkarte nach dem Fahrtenschreiber-Entwurf

Stand10.10.2026: getesteter Main-Stand PR53, neue Be-/Entlade-, Ladungsart- und Fahrtenschreiber-Quellen in ungemergten Folge-Entwürfen. „Implementiert“ beschreibt den Codeumfang; Gerätebestätigung und vollständige CI stehen für die neuen Quellen separat aus. Vollständige Kartenabdeckung wird noch nicht behauptet.

| Quelldatei | EF | Generation | Stand |
|---|---|---|---|
| Driver_Activity_Data | `0504` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Vehicles_Used | `0505` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Control_Activity_Data | `0508` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Events_Data | `0502` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Faults_Data | `0503` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Specific_Conditions | `0522` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| GNSS_Places | `0524` | Gen2/Gen2V2 | eigene Sitzungsquelle |
| GNSS_Places_Authentication | `0527` | Gen2V2 | eigene getrennte Sitzungsquelle |
| Places | `0506` | Gen1/Gen2/Gen2V2 | eigene Sitzungsquelle |
| Places_Authentication | `0526` | Gen2V2 | eigene getrennte Sitzungsquelle |
| Border_Crossings | `0528` | Gen2V2 | PR53, allgemein am Gerät bestätigt |
| Load_Unload_Operations | `0529` | Gen2V2 | PR54, neue unabhängige Sitzungsquelle |
| Load_Type_Entries | `0530` | Gen2V2 | PR55, neue unabhängige Sitzungsquelle |
| VehicleUnits_Used | `0523` | Gen2/Gen2V2 | neuer unabhängiger Entwurf, eigener Zähler und exakte Quelle |

Application_Identification (`0501`), Application_Identification_V2 (`0525`) und Identification (`0520`) werden zur Generation, Kapazität und Profil-/Kartenbindung gelesen. Das ist nicht gleichbedeutend mit einer eigenen unveränderten Rohdatei-Archivierung sämtlicher Metadaten.

## Nächste noch offene Quellen

| Datei | EF | Vor einer Umsetzung zu klären |
|---|---|---|
| Driving_Licence_Info | `0521` | unabhängige feste Datei, Rohtext/Zeichenkodierung erhalten |
| Current_Usage | `0507` | einzelne aufgezeichnete Sitzung, keine erfundene Einsatzdauer oder Verknüpfung |
| Card_Download | `050E` | nur ursprünglichen Kartenwert lesen; ausdrücklich keine Aktualisierung schreiben |
| CardMA_Certificate/CardSignCertificate/CA_Certificate/Link_Certificate | `C100`/`C101`/`C108`/`C109` | variable Längen, eigene Quellen und separate kryptografische Prüfungsgrenzen |
| VU_Configuration | `0540` | eigenes Sicherheits-/Zugriffsverfahren, Gen2V2; nicht blind als normale EF übernehmen |

Die Tabelle ist eine Arbeitsliste relevanter noch offener Quellen im Tachograph_G2-Verzeichnis, keine abgeschlossene Bestandsaufnahme sämtlicher MF-/Gen1-Dateien. ICC-/IC-Identifikation und weitere Metadaten müssen vor einer Vollständigkeitsbehauptung zusätzlich abgeglichen werden. Fleet-/Kilometer-/Spesen- und weitere Timeline-Projektionen folgen erst nach zuverlässig vollständig übernommenen relevanten Quellen.

## Geprüfter VehicleUnits_Used-Entwurf

Der amtliche Datensatz beschreibt den Fahrtenschreiber, nicht den LKW-Hersteller: Einsatzbeginn, Hersteller-Rohcode, herstellerspezifische Geräte-ID und vier Byte Softwareversion. Er enthält weder VIN noch Kilometerstand, Endzeit oder Seriennummer. Keine Zuordnung zu einem Fahrzeugmodell oder Herstellerlogo aus diesen Feldern ableiten.

Die amtlichen ASN.1-, Byte- und Kapazitätstabellen sind vollständig visuell geprüft: zwei Byte Ringzeiger, zehn Byte je Datensatz, Gen2-Kapazität84..200, Gen2V2 genau200. Die2018-Erweiterung liefert den unabhängigen Zwei-Byte-Zähler am Ende der vollständigen17-Byte-Application_Identification, Offset15..16. Eine alte15-Byte-Anwendung liefert ihn nicht; keine Kapazität raten. Softwareversion ist vier Byte IA5String, Rohbytes bleiben vollständig erhalten.

[VEHICLE_UNITS_IMPORT.md](VEHICLE_UNITS_IMPORT.md) beschreibt die Implementierung und exakte Legacy-Grenze. Die Dateien sind angebunden; vollständige finale CI/Compilerexport und späterer Hardwaretest stehen separat aus. Diese Quelle erweitert die Abdeckung auf14 Quellen, noch keine vollständige Fahrerkarte.
