Aktueller Fortsetzungsstand 11.10.2026: PR60/40120 bleibt unverändert an `85d0614235e4d9b5384d96d9ee124aab1f9fbc3a`, gemeinsame ZIP bereitgestellt und CI bestanden, Gerätebestätigung weiterhin ausstehend. Neuer getrennt zu prüfender kumulativer Folgeentwurf 40121/Schema18 ergänzt bedingte MF-Kommunikationsquellen und Quellenmap-Snapshots; bis29 unterstützte Gen2V2-Quellen nur bei eindeutig angekündigter Erweiterung. 38 zusätzliche Testmethoden, lokale Grenzen/CI-Guards/Shellsyntax/Diff bestanden; neue vollständige Android-CI erforderlich. Alle Entwürfe ungemergt. [Verbindliche Fortsetzung und genaue offene Grenzen](CARD_IMPORT_FINALIZATION_HANDOFF.md). Keine Browser, Unteragenten oder Bereinigung; Actions-Budget unverändert 2 USD. Ältere Checkpoints bleiben historische Stände.

---

Aktueller gemeinsamer FolgestandPR60/40120: Originaldateien plus MF-DIR für erkannte Gen2/Gen2V2-Karten, bis27 Quellen. Lokale Prüfungen bestanden; neue vollständige CI vor Ausgabe erforderlich. Keine neue Hardwarebestätigung. [DIR-Umfang](CARD_DIRECTORY_IMPORT.md). Ältere Checkpoints dokumentieren ihre damaligen Stände.

# Abdeckung der Fahrerkarte

Stand11.10.2026: Tobi hat PR58/40117 um08:36 Europe/Berlin allgemein bestätigt („Funktioniert alles“). PR54–58 sind unverändert kumulativ gemergt; Main1b592d6 hat den getesteten Tree2e314a4. Diese Bestätigung enthält keine zusätzlichen Einzelmesswerte oder nichtleeren Quellenfälle.

PR59/40118 mit Zertifikatsquellen hat die vollständige Android-CI38119878793 an c535c01 bestanden, einschließlich Room/SQLCipher/Compose. Kumulativer PR60-Originaldateien-/DIR-Entwurf40120: lokale Prüfungen bestanden, vollständige CI und späterer Gerätetest erforderlich. Neue Entwürfe bleiben ungemergt. Vollständige Kartenabdeckung wird noch nicht behauptet.

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
| Load_Unload_Operations | `0529` | Gen2V2 | PR54–58 kumulativ bestätigt und gemergt |
| Load_Type_Entries | `0530` | Gen2V2 | PR54–58 kumulativ bestätigt und gemergt |
| VehicleUnits_Used | `0523` | Gen2/Gen2V2 | PR56 bestätigt/gemergt; unabhängiger Zähler |
| Driving_Licence_Info | `0521` | Gen1/Gen2/Gen2V2 | PR57 bestätigt/gemergt,53Byte; Nummer zunächst verborgen |
| Current_Usage | `0507` | Gen1/Gen2/Gen2V2 | PR57 bestätigt/gemergt,19Byte; historischer Kartenstand |
| Card_Download | `050E` | Gen1/Gen2/Gen2V2 | PR57 bestätigt/gemergt,4Byte; ausschließlich lesen |
| ICC-Identifikation | `0002` unter MF | Gen1/Gen2/Gen2V2 | PR58 bestätigt/gemergt,25Byte; vor AID, Serienwert verborgen |
| IC-Identifikation | `0005` unter MF | Gen1/Gen2/Gen2V2 | PR58 bestätigt/gemergt,8Originalbytes; vor AID, Kennung verborgen |

| Karten-/CA-Zertifikat | `C100`/`C108` | Gen1/Gen2/Gen2V2 | PR59, vollständige unabhängige Originalquelle; struktureller Decoder |
| Signatur-/Link-Zertifikat | `C101`/`C109` | Gen2/Gen2V2 | PR59, eigene Originalquellen; keine Kryptoprüfung |
| Application_Identification | `0501` | Gen1/Gen2/Gen2V2 | PR60 auf PR59; vollständige Originalbytes10/15/17 |
| Application_Identification_V2 | `0525` | Gen2V2 | PR60 auf PR59; vollständige Originalbytes10 |
| Identification | `0520` | Gen1/Gen2/Gen2V2 | PR60 auf PR59; vollständige143Bytes, exakte Identitätsprüfung |
| MF DIR | `2F00` | Gen2/Gen2V2 | PR60 kumulativ; exakt20Bytes mit unabhängiger Dateiendprüfung, genaue Karte/Sitzung |

## Herkunft und offene Quellen

Neue Gen2V2-Lesevorgänge können27 unterstützte Quellen enthalten. Historische19-/23-/26-Quellen-Übernahmen behalten ihre damalige Abdeckung; keine Nachbefüllung und kein Rückgriff auf jüngere Sitzungen. Fehlende/ungültige Daten bleiben separat sichtbar. Schema18 unverändert.

| Noch offene Datei/Funktion | EF | Klärung vor Umsetzung |
|---|---|---|
| VU_Configuration | `0540` | Gen2V2, eigenes Sicherheits-/Zugriffsverfahren SC5; nicht blind als gewöhnliche EF lesen |
| DIR außerhalb des Gen2-Formats | `2F00` | Gen1-/andere Verzeichnisformate getrennt am Primärformat klären |
| Bedingte ATR/INFO- und Extended_Length-EFs | MF | Vorhandensein, Längen und Zugriff am amtlichen Format prüfen |
| Vollständiger signierter Standarddownload | mehrere | Download-/Authentisierungsprotokoll und Original-Signaturen separat implementieren |
| Kryptografische Zertifikats-/Signaturprüfung | mehrere | RSA-Nachrichtenrückgewinnung bzw. EC-Prüfung und überprüfbare Vertrauenskette |

Arbeitsliste relevanter offener Quellen; vor einer Vollständigkeitsbehauptung sind sämtliche MF-/Gen1-/Gen2-Dateien zusätzlich abzugleichen. Fahrzeug-/Kilometer-/Spesen- und weitere Timeline-Auswertung folgen erst nach zuverlässig vollständig übernommenen relevanten Kartenquellen.

[Zertifikatsformat und Grenze](CARD_CERTIFICATE_IMPORT.md) · [Originaldateien](CARD_ORIGINAL_IDENTIFICATION_IMPORT.md) · [Gebündelter späterer Gerätetest](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md)
