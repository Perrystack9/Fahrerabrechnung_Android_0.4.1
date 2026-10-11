# Kartenabdeckung nach PR59/60 · Quellensichtung

Stand 11.10.2026, Produktkopf `df88742ca57b4e2ca018fe474863ee8ff390ac5c`. Dieser Forschungscheckpoint ändert keine App-Datei und belegt keinen neuen Gerätetest. Prüfnachweise und endgültiger Produktstatus gehören zur Beschreibung von PR60. Die Produktregeln aus Issue #3 und CURRENT_RULES bleiben maßgeblich.

## Was der aktuelle Leser versucht

Die Tabelle beschreibt Codeabdeckung der ausgewählten Anwendung. „Unterstützt“ bedeutet einen vorhandenen eigenständigen Lese-/Speicherpfad; es behauptet weder eine erfolgreiche konkrete Kartenlesung noch einen vollständigen signierten Download. MF-Dateien sind einmalige gemeinsame Kartenquellen, keine Kopie je Anwendung.

| Bereich / EF | Kennung | Gen1 | Gen2 | Gen2V2 |
|---|---|---|---|---|
| MF ICC | 0002 | unterstützt | unterstützt | unterstützt |
| MF IC | 0005 | unterstützt | unterstützt | unterstützt |
| Application_Identification | 0501 | unterstützt | unterstützt | unterstützt |
| Karten-/MA-Zertifikat | C100 | unterstützt | unterstützt | unterstützt |
| Signaturzertifikat | C101 | entfällt | unterstützt | unterstützt |
| CA-Zertifikat | C108 | unterstützt | unterstützt | unterstützt |
| Link-Zertifikat | C109 | entfällt | unterstützt | unterstützt |
| Identification | 0520 | unterstützt | unterstützt | unterstützt |
| Card_Download, nur vorhandener Zeitwert | 050E | unterstützt | unterstützt | unterstützt |
| Driving_Licence_Info | 0521 | unterstützt | unterstützt | unterstützt |
| Events_Data | 0502 | unterstützt | unterstützt | unterstützt |
| Faults_Data | 0503 | unterstützt | unterstützt | unterstützt |
| Driver_Activity_Data | 0504 | unterstützt | unterstützt | unterstützt |
| Vehicles_Used | 0505 | unterstützt | unterstützt | unterstützt |
| Places | 0506 | unterstützt | unterstützt | unterstützt |
| Current_Usage | 0507 | unterstützt | unterstützt | unterstützt |
| Control_Activity_Data | 0508 | unterstützt | unterstützt | unterstützt |
| Specific_Conditions | 0522 | unterstützt | unterstützt | unterstützt |
| VehicleUnits_Used | 0523 | entfällt | unterstützt | unterstützt |
| GNSS_Places | 0524 | entfällt | unterstützt | unterstützt |
| Application_Identification_V2 | 0525 | entfällt | entfällt | unterstützt |
| Places_Authentication | 0526 | entfällt | entfällt | unterstützt |
| GNSS_Places_Authentication | 0527 | entfällt | entfällt | unterstützt |
| Border_Crossings | 0528 | entfällt | entfällt | unterstützt |
| Load_Unload_Operations | 0529 | entfällt | entfällt | unterstützt |
| Load_Type_Entries | 0530 | entfällt | entfällt | unterstützt |
| VU_Configuration | 0540 | entfällt | entfällt | offen |

Für die ausgewählte Anwendung einschließlich beider MF-Dateien sind damit 16 Gen1-, 20 Gen2- bzw. 26 Gen2V2-Quellenpfade vorhanden. Die Gen2V2-Dateitabelle enthält zusätzlich EF0540. Diese Zählung beschreibt angebotene Pfade; eine konkrete Übernahme behält ihren eigenen Status und ihre tatsächlich gespeicherten Quellen. Vor PR59/60 gespeicherte 19-/23-Quellen-Vorgänge erhalten keine neuen Originaldateien.

## Nächste begrenzte Schritte

1. **EF0540 getrennt sichern.** EU2021/1228 nennt eine 3072-Byte-Datei und einen eigenständigen unsigned 16-Bit-Wert `vuConfigurationLengthRange` in EF0525. Ein späterer Pfad muss den Originalwert validieren, die vollständige Dateigrenze belegen und seine Bytes eigenständig speichern. Keine Kapazität aus einem anderen Ring übernehmen. Keine Einstellungen aus unbeschriebenen Bytes erfinden und keine Konfiguration auf die Karte oder den Fahrtenschreiber schreiben. Der aktuelle Code speichert den Kapazitätswert in der Originalanwendung, versucht die Konfigurationsdatei aber noch nicht.
2. **Gen1-Spiegelanwendung neuer Karten separat erfassen.** Der heutige Leser wählt bei vorhandener Gen2-Anwendung diese aus; er liest die zusätzlich vorhandene Gen1-Anwendung noch nicht. Physische Kartengeneration und Herkunftsanwendung müssen getrennte Merkmale bleiben. Gleiche EF-IDs aus zwei Anwendungen brauchen getrennte Quellen; Gen1-Kapazitäten dürfen nicht aus Gen2 übernommen werden. Keine Ersatzkarte aus einem Wechsel der Anwendung erzeugen, keine doppelten Kalenderaktivitäten und keine historische Nachbefüllung.
3. **Signatur- und Downloadprotokoll gesondert umsetzen.** DDP035 verlangt für einen vollständigen Download beide Anwendungen einer Gen2-Karte sowie die zugehörigen Signaturen. Ein späterer Export benötigt die vollständigen Datei-/Signaturblöcke und eine nachvollziehbare Prüfung nach Appendix11. Der aktuelle reine SELECT/READ-Import ist kein signierter Export. Das normative Aktualisieren von LastCardDownload wird hier nicht ausgeführt; ein angezeigter Kartenzeitwert bleibt der vorgefundene Wert. Die gegenwärtige Phase ergänzt keine PSO-, Schreib- oder Authentisierungsbefehle.

## Primärnachweise

- [EU2016/799](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): Appendix2 TCS150/152/154 und Appendix7 3.3/3.4; Gen1-Datei- und Zugriffstabellen visuell geprüft.
- [EU2018/502](https://eur-lex.europa.eu/eli/reg_impl/2018/502/oj/eng): vollständige 17-Byte-Gen2-Anwendungsstruktur und separate Fahrtenschreiber-Kapazität.
- [EU2021/1228, JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf): gedruckte Seiten31,39,45,48/49 und80/81. ASN.1, Kennung0540, 3072-Byte-Grenze und DDP035 getrennt geprüft; lokale PDF SHA256 `58811d8069e3d019d8293c8c0bcbd5f470c89a9dca9608e59be5f51639fdf870`.

Zertifikats-Forschungsoracle erneut erfolgreich: 115 öffentliche JRC-v1.4-Zertifikate strukturell geprüft, 352 beschädigte Rahmungen abgewiesen, 115 Beispielsignaturen mathematisch geprüft und Änderung eines Ablaufzeitbytes korrekt abgewiesen. Die Android-App führt diese mathematische Vertrauensprüfung nicht aus. Der reine Grenzalgorithmus erhält 4968 synthetische Dateien über alle 138 zulässigen Längen und benötigt höchstens neun Ein-Byte-Proben. Die tatsächlichen Android-Tests sind separat in PR59/60 nachgewiesen. Es wurden nur `.cert`-Archivmitglieder gelesen, keine privaten Beispielschlüssel. Archiv SHA256 `b8ed453eb99f2a889b888c02b17eed33ee1e5c9c8dd0a6c182ced74a39b0ae5c`.
