# Abdeckung der Fahrerkarte nach dem Ladungsart-Entwurf

Stand10.10.2026: getesteter Main-Stand PR53, neue Be-/Entlade- und Ladungsart-Quellen in den ungemergten PR54/55. „Implementiert“ beschreibt den Codeumfang; Gerätebestätigung und vollständige CI stehen für die neuen Quellen separat aus. Vollständige Kartenabdeckung wird noch nicht behauptet.

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

Application_Identification (`0501`), Application_Identification_V2 (`0525`) und Identification (`0520`) werden zur Generation, Kapazität und Profil-/Kartenbindung gelesen. Das ist nicht gleichbedeutend mit einer eigenen unveränderten Rohdatei-Archivierung sämtlicher Metadaten.

## Nächste noch offene Quellen

| Datei | EF | Vor einer Umsetzung zu klären |
|---|---|---|
| VehicleUnits_Used | `0523` | Gen2/Gen2V2, vollständiger eigener Kapazitätszähler, Version der Kartenspezifikation |
| Driving_Licence_Info | `0521` | unabhängige feste Datei, Rohtext/Zeichenkodierung erhalten |
| Current_Usage | `0507` | einzelne aufgezeichnete Sitzung, keine erfundene Einsatzdauer oder Verknüpfung |
| Card_Download | `050E` | nur ursprünglichen Kartenwert lesen; ausdrücklich keine Aktualisierung schreiben |
| CardMA_Certificate/CardSignCertificate/CA_Certificate/Link_Certificate | `C100`/`C101`/`C108`/`C109` | variable Längen, eigene Quellen und separate kryptografische Prüfungsgrenzen |
| VU_Configuration | `0540` | eigenes Sicherheits-/Zugriffsverfahren, Gen2V2; nicht blind als normale EF übernehmen |

Die Tabelle ist eine Arbeitsliste relevanter noch offener Quellen im Tachograph_G2-Verzeichnis, keine abgeschlossene Bestandsaufnahme sämtlicher MF-/Gen1-Dateien. ICC-/IC-Identifikation und weitere Metadaten müssen vor einer Vollständigkeitsbehauptung zusätzlich abgeglichen werden. Fleet-/Kilometer-/Spesen- und weitere Timeline-Projektionen folgen erst nach zuverlässig vollständig übernommenen relevanten Quellen.

## Vorbereitung VehicleUnits_Used

Der amtliche Datensatz beschreibt den Fahrtenschreiber, nicht den LKW-Hersteller: Einsatzbeginn, Hersteller-Rohcode, herstellerspezifische Geräte-ID und vier Byte Softwareversion. Er enthält weder VIN noch Kilometerstand, Endzeit oder Seriennummer. Keine Zuordnung zu einem Fahrzeugmodell oder Herstellerlogo aus diesen Feldern ableiten.

Die Grundspezifikation nennt zehn Byte je Datensatz und zwei Byte Ringzeiger, im ursprünglichen Gen2-Treiberformat84..200 Datensätze; Gen2V2 hat nach der2021-Erweiterung fest200. Die2018-Erweiterung ergänzt den eigenen Zwei-Byte-Zähler am Ende der vollständigen17Byte-Application_Identification. Vor Codeänderung dessen Offset15..16, Kapazitätsregeln und ASN.1-Rohtexttyp gegen die amtlichen Tabellen visuell prüfen. Eine alte15Byte-Erweiterung liefert diesen Zähler nicht; keine Kapazität aus einem Nachbarfeld oder einer anderen Quelle erfinden. Reservierte Hersteller-/Geräte-/Softwarewerte als Rohwerte erhalten.

Primärquellen: [EU2016/799 JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf), Appendix1 2.39/2.40/2.61/2.106/2.226 und Appendix2 TCS152/154/155; [EU2018/502 EUR-Lex](https://eur-lex.europa.eu/legal-content/EN/ALL/?uri=uriserv%3AOJ.L_.2018.085.01.0001.01.ENG), Änderung2.61; [EU2021/1228 JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Appendix2 TCS152/154/155 (L273/45,48–49). EF-Kennungen und Gen2V2-Kapazitäten visuell im2021-PDF geprüft. Der2016-PDF-Webabruf war wegen Dateigröße blockiert; ASN.1-Details dieser nächsten Quelle noch nicht vollständig visuell geprüft, daher hier nur Recherchecheckpoint und keine Implementierungsfreigabe.
