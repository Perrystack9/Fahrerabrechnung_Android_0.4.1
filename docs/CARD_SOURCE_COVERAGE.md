Aktueller Gerätecheckpoint12.10.2026: Tobi bestätigt den neuen40121-Wiederlese-/Anzeigefall nach erneutem Einstecken positiv. Beide neuen MF-Quellen real vorhanden: ATR/INFO11Bytes, Extended_Length3Bytes, T=1 aufgezeichnet/T=0 nicht aufgezeichnet. PR62-Produktkopfefbf49cdf92bd5e5ddb2e042b3e14e9372169756 unverändert und vollständige CI38172596871 sowie Paketlauf38201601458 erneut grün. [Genaue40121-Gerätebestätigung und Grenzen](DEVICE_CONFIRMATION_40121.md). Keine zusätzlichen40121-Neustart-/Altdaten-/Historienfälle behaupten. Nach unverändertem PR62-Abschluss selbstständige Produktfortsetzung durch die eingerichtete Aufgabe; spätere Änderungen weiterhin bis eigener Freigabe ungemergt. Keine Browser/Unteragenten/Bereinigung, Budget2USD unverändert. Die folgenden Angaben sind historische Checkpoints.

---

Aktueller bestätigter Stand11.10.2026: Gerätetest PR60/40120 ausdrücklich bestanden: Update und alte Daten, neue Kartenlesung, Zertifikate/Kartenangaben/Anwendungsverzeichnis, Bestand nach Kartenentnahme und App-Neustart sowie unveränderter alter Import. PR60 am unveränderten geprüften Kopf85d0614 gemergt als9e7670f05b7c35a585244aeca539be9d0fe7c6fb, Tree78155b21cb76f9c27231360bd78dad0aa5472a22 exakt gleich dem Teststand. PR59 ist enthalten und von GitHub ebenfalls als gemergt abgeschlossen. Letzte Gerätebasis jetzt40120/Schema18. PR62/40121 und spätere Änderungen sind ausdrücklich noch nicht hardwarebestätigt und bleiben ungemergt. [Verbindliche Gerätebestätigung und Merge-Nachweise](DEVICE_CONFIRMATION_40120.md). Selbstständige Importfortsetzung beauftragt; Projektregeln, kein Browser/Unteragenten/Bereinigung und Actions-Budget2USD bleiben bestehen. Ältere Checkpoints darunter sind historische Stände.

---

Aktueller geprüfter Importfolgeentwurf PR62/40121/Schema18: bedingte MF-Kommunikationsquellen, exakte eigene Karten-/Sitzungsspeicherung und Quellenmap-Snapshots; bis29 Gen2V2-Quellenarten bei eindeutiger ATR-Ankündigung. 38 zusätzliche Testmethoden. Integrierter Produktkopf efbf49cdf92bd5e5ddb2e042b3e14e9372169756, Tree11e79e492b381f9d515bee796ffea30da3099592; vollständige CI38172596871 mit allen vier Jobs erfolgreich, einschließlich Build/Unit/Lint/APKs, Updatepaar40120→40121, unveränderten Room-Exporten und SQLCipher/Compose. Dokumentierter Main f2984027643a27ea55dc44c10fae9cdf6d115938 als zweiter Parent enthalten; PR62 ohne Konflikt mergebar, aber ausdrücklich hardwareunbestätigt und ungemergt. Gerätefreigabe40120 gilt nur für abgeschlossene PR59/60. [Vollständiger Folgecheckpoint und offene Grenzen](CARD_IMPORT_FINALIZATION_HANDOFF.md). Abschließende Dokumentation liegt auf docs/card-import-finalization-checkpoint-20261011, Produktkopf unverändert. Ältere Checkpoints sind historische Stände.

---

Aktueller gemeinsamer FolgestandPR60/40120: Originaldateien plus MF-DIR für erkannte Gen2/Gen2V2-Karten, bis27 Quellen. Lokale Prüfungen bestanden; neue vollständige CI vor Ausgabe erforderlich. Keine neue Hardwarebestätigung. [DIR-Umfang](CARD_DIRECTORY_IMPORT.md). Ältere Checkpoints dokumentieren ihre damaligen Stände.

# Abdeckung der Fahrerkarte

Stand11.10.2026: Tobi hat PR58/40117 um08:36 Europe/Berlin allgemein bestätigt („Funktioniert alles“). PR54–58 sind unverändert kumulativ gemergt; Main1b592d6 hat den getesteten Tree2e314a4. Diese Bestätigung enthält keine zusätzlichen Einzelmesswerte oder nichtleeren Quellenfälle.

PR59/40118 mit Zertifikatsquellen hat die vollständige Android-CI38119878793 an c535c01 bestanden, einschließlich Room/SQLCipher/Compose. PR60/40120 hat CI38145473572, Paketlauf38148088141 und den ausdrücklich bestätigten Gerätetest bestanden; Zertifikate, Kartenangaben und DIR bleiben nach Kartenentnahme/App-Neustart sichtbar, alter Import unverändert. PR62/40121 ergänzt die beiden bedingten MF-Kommunikationsdateien. PR59/60 sind nach bestätigtem Gerätetest40120 unverändert kumulativ gemergt; PR62 bleibt ohne eigene Hardwarebestätigung ungemergt. Vollständige Kartenabdeckung wird noch nicht behauptet.

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
| MF ATR/INFO | `2F01` | Gen2/Gen2V2, bedingt | PR62; vollständige7–128Originalbytes und äußere `7F66`-Struktur, keine Puffergrößeninterpretation |
| MF Extended_Length | `0006` | Gen2/Gen2V2, bedingt | PR62; exakt3Bytes, T=0/T=1-Angaben aus der Datei, unbekannte Codes bleiben unbekannt |

## Herkunft und offene Quellen

Neue Gen2V2-Lesevorgänge enthalten bis27 bisherige und zwei zusätzlich bedingte Quellenarten, insgesamt bis29. Die neuen Dateien werden nur bei eindeutiger ATR-Ankündigung versucht. Gen1 hat16 Arten, Gen2 bis21 beziehungsweise23 mit beiden bedingten Dateien. Das zählt keine Zeitabdeckung und keine Datensätze. Historische19-/23-/26-/27-Quellen-Übernahmen behalten ihre damalige Abdeckung; keine Nachbefüllung und kein Rückgriff auf jüngere Sitzungen. Fehlende/ungültige Daten bleiben separat sichtbar. Schema18 unverändert.

[Annex IC §4.5.2.2–4.5.2.4](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02016R0799-20160526) sieht DIR und beide Kommunikationsdateien nur auf Karten der zweiten Generation vor. Eine Standard-DIR-Datei auf reinen Gen1-Karten fehlt somit nicht im Importumfang. Die getrennte Gen1-Anwendung auf einer Gen2-Karte wird dagegen noch nicht zusätzlich zur ausgewählten Gen2-Anwendung importiert.

| Noch offene Datei/Funktion | EF | Klärung vor Umsetzung |
|---|---|---|
| VU_Configuration | `0540` | Gen2V2, eigenes Sicherheits-/Zugriffsverfahren SC5; nicht blind als gewöhnliche EF lesen |
| Getrennter Import beider Anwendungen auf Gen2-Karte | zwei DFs | Eigenständige generationsgetrennte Quellen-/Sitzungsmodellierung statt Überlagerung |
| ATR/INFO-Inhalt | `2F01` | Originalbytes sind importiert; vollständigen ISO7816-4:2013-Decoder für den `7F66`-Inhalt anhand belegten Primärformats ergänzen |
| Unbekanntes ATR-Historienformat | ATR | Bedingte Dateien nicht versucht; unbekannt nicht als fehlend oder vollständig behaupten |
| Vollständiger signierter Standarddownload | mehrere | Download-/Authentisierungsprotokoll und Original-Signaturen separat implementieren |
| Kryptografische Zertifikats-/Signaturprüfung | mehrere | RSA-Nachrichtenrückgewinnung bzw. EC-Prüfung und überprüfbare Vertrauenskette |

Arbeitsliste relevanter offener Quellen; vor einer Vollständigkeitsbehauptung sind sämtliche MF-/Gen1-/Gen2-Dateien zusätzlich abzugleichen. Fahrzeug-/Kilometer-/Spesen- und weitere Timeline-Auswertung folgen erst nach zuverlässig vollständig übernommenen relevanten Kartenquellen.

[Zertifikatsformat und Grenze](CARD_CERTIFICATE_IMPORT.md) · [Originaldateien](CARD_ORIGINAL_IDENTIFICATION_IMPORT.md) · [Kommunikationsquellen](CARD_COMMUNICATION_IMPORT.md) · [Übergabe](CARD_IMPORT_FINALIZATION_HANDOFF.md)
