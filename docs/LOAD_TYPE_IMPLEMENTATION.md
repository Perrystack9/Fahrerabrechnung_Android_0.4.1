> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Ladungsart als unabhängige Kartenquelle

Implementierte unabhängige Lesung, verschlüsselte Speicherung und neutrale Anzeige der Ladungsart. Be-/Entladevorgänge bleiben eine getrennte Quelle.

## Geprüfte Primärspezifikation

[Amtliches JRC-PDF EU2021/1228](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Appendix1 2.24a/b (L273/28–29), 2.61a (31), 2.90a (35) und Appendix2 TCS_152/TCS_154/TCS_155 (45,48–49). ASN.1-Struktur, Wertetabelle, EF-Kennung und feste Kapazität sind im PDF visuell geprüft.

EF `Load_Type_Entries` hat die Kennung `0530`, ausschließlich Gen2V2. Fahrerkarte: zwei Byte Ringzeiger plus336×5Byte =1682Byte. Jeder Datensatz enthält einen vier Byte großen unsigned `TimeReal` und einen Byte großen `LoadType`. Die Kapazität steht an Offset6..7 der vollständigen zehn Byte großen EF `Application_Identification_V2` (`0525`, Folgelänge8). Die Nachbarzähler für Grenzübertritte und Be-/Entladevorgänge werden nicht als Kapazität dieser Quelle verwendet oder validiert.

| Rohcode | Aufgezeichnete Bedeutung |
|---|---|
| `00` | Ladungsart nicht festgelegt |
| `01` | Güter |
| `02` | Fahrgäste |
| `03`..`FF` | Reservierter Rohwert, keine Ersatzklassifikation |

Ein bekanntes Datum zusammen mit Code00 ist ein gültiger Eintrag. Unbekannte Zeitfelder behalten den Ladungscode, erhalten jedoch keine Ersatzzeit. Nur vollständig00-/FF-gefüllte Datensätze werden als leere Slots ausgelassen. Ringfolge, physische Slots, Wiederholungen und nicht chronologische Zeitangaben bleiben erhalten. Diese Einträge dürfen nicht in Transportintervalle, Fahrzeugklassen, Be-/Entladevorgänge oder Spesenregeln umgedeutet werden.

## Implementierung

Die Lesung benötigt sieben READ-BINARY-Portionen, zuletzt1536+146Byte. Jede eigene Kapazität wird unabhängig validiert. Fehlende/teilweise Dateien, unbrauchbare Zeiger und Reader-Fehler blockieren andere Quellen nicht; Cancellation propagiert und schließt den Reader. Ältere Generationen versuchen EF0530 nicht. EF Card_Download wird nicht geschrieben.

Die gemeinsame Dateilesung begrenzt jede Antwort auf die tatsächlich angeforderte Portion. Eine formal erfolgreiche Antwort mit mehr Bytes als angefordert wird als fehlerhafte Quelle abgelehnt; kürzere nichtleere Portionen werden weiterhin bis zur vollständigen Dateilänge gelesen. Der Ladungsart-Reader-Test enthält dafür eine257Byte-Antwort auf einen256Byte-Request und prüft den Erhalt der anderen bereits gelesenen Quellen.

Additive Migration15→16 mit `card_load_type_imports` und `driver_import_load_type_sources`, unveränderten32 alten Entitäten und zusammengesetzten Fremdschlüsseln. Der Fingerprint umfasst physische eigene Karte, Generation, Kapazität und vollständige Bytes. Sitzungen verknüpfen atomar die genaue Quelle; neue Fassungen deduplizieren keine andere Karte. Historische Requests ohne diese Quelle behalten Fingerprint und Abdeckung. Fremde/unbestätigte Karten bleiben temporär; keine späteren Quellen in alte Sitzungen einsetzen.

Die zusammengesetzten Fremdschlüssel sichern das Quelldatei-/Kartenpaar; das Laden prüft zusätzlich die Karte der referenzierten Sitzung. Auch eine künstlich manipulierte Verknüpfung zu einer gültigen Quelle einer anderen physischen eigenen Ersatzkarte muss deshalb beim Laden abbrechen, bevor Datensätze angezeigt werden. Der SQLCipher-Test prüft sowohl ungültige FK-Paare als auch diese gültige, aber zur Sitzung unpassende andere Kartenquelle und veränderte gespeicherte Bytes.

Die Pipeline und der Coordinator frieren die Bytes ein und erhalten einen unabhängigen Status. Die Detailansicht nutzt ausschließlich die ausgewählte Sitzung, zeigt gültig leere und fehlende Dateien getrennt und lädt Einträge in20er-Portionen. Quellwechsel setzt die Portion zurück. Coverage-Zeiten bleiben bei unbekanntem Eintragszeitpunkt offen; keine Ersatzzeit einer anderen Quelle.

## HISTORISCH - Entstehungs-/Prüfstand

Stand 10.10.2026, erster Arbeitsblock. Dieser eigene Zweig baut auf PR54 `6d46ebd6fe77a4c424aee45dc700f0bd186f5d5c` auf und ergänzt die unabhängige Lesung, verschlüsselte Speicherung, Quellensitzung, Pipeline-Abdeckung und neutrale Anzeige der Ladungsart. PR54 bleibt der vollständige separate Be-/Entladeimport. Beide neuen Import-/Schemaänderungen bleiben bis späterer Gerätebestätigung ungemergt.

PR54-Compilerexport15 stammt unverändert aus CI38074866406 Versuch1, Artefakt11678109394, Archiv-SHA256 `4ab167987baf82a36a820a72466efa04056f12d0cf2f8fa933c0dbe8cdb1e04e`. Schema-SHA256 `fde634e57a39aa25fbf43dc71f979750f4b534b1a4d2f08a2ac2ecb974ca5122`. Alle30 vorherigen Entitäten sind identisch, exakt zwei kommen hinzu. Die sieben Gradle-Kommandos und die APK-Paarprüfung bestanden; lediglich der fehlende eingecheckte Export blockierte diesen ersten Lauf. Der vollständige Folgelauf38075694297 am PR54-Kopf einschließlich Instrumentation ist separat zu prüfen.

39 neue Testmethoden: 14 Parser, elf Reader, acht SQLCipher, eine Room-Migration und fünf Compose einschließlich echter AppRoot-Navigation. Die bestehende SQLCipher-Schema14-Prüfung entfernt jetzt beide neuen Quellenpaare und prüft so zusätzlich die zusammenhängende14→15→16-Migration; die neue Prüfung deckt15→16 mit vorhandenen Be-/Entladequellen ab.

Testversion40114; feste Paketkennung und Signatur unverändert. Gerätepaar bleibt vom zuletzt getesteten PR53-Schema14/40112 auf den kumulativen neuen Stand. [Gemeinsame Geräteanleitung](LOAD_TYPE_DEVICE_TEST.md) erst nach dem zweiten Arbeitsblock ausgeben.

Der unveränderte16.json-Compilerexport stammt aus CI38075938370 Versuch1 am Produktkopf `8ae5aa7b19fc1016d8dd05f82e23848e8227f61e`, Artefakt11679200555. Archiv-SHA256 `53b68c39106153ad08f0ce7814ffcee59790233be9059ef92fa292a584708910`; Schema-SHA256 `d821ad258e3b8bcaf3b5a0b384f50c50b4e7d6f7a08cdf75b04d592cdd2e0210`. Alle32 vorherigen Entitäten sind vollständig identisch, genau zwei neue kommen hinzu. Die sieben erforderlichen Gradle-Kommandos, APK-Prüfung und Updatepaar bestanden; nur der zunächst nicht eingecheckte Export blockierte diesen ersten Lauf.

Lokale Struktur-/Whitespace-/SQLite-Prüfungen ersetzen die vollständige Android-CI nicht. Der finale Kopf mit16.json, zusätzlicher Reader-Antwortgrenze und den erweiterten Manipulationsprüfungen muss sämtliche Gradle-, APK-Paar-, Schema-, SQLCipher-, Room- und Compose-Prüfungen bestehen. [Abdeckung](CARD_SOURCE_COVERAGE.md), [Fortsetzung und spätere Ausgabe](DEFERRED_TEST_HANDOFF.md). Frühestens11.10.2026 um00:56:06 Europe/Berlin ein Hardware-Testpaket bereitstellen. Zuvor endet der erste Block20:56:06, Pause bis22:56:06 und zweiter zweistündiger Block bis00:56:06. Kein neuer Hardwareerfolg ist bisher bestätigt.
