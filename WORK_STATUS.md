# FahrerTagebuch – aktueller Arbeitsstand

**Dokumentationsstand: 01.10.2026 · Produktcode-Basis: `main` nach PR #26 (`fbac9310`).** Fachliche Quelle: [CURRENT_RULES.md](docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3).

Neue Work-Sitzung: zuerst [WORK_HANDOFF.md](docs/WORK_HANDOFF.md) lesen. Der gemeinsame Speichervorgang aus PR #26 wurde am 01.10.2026 am Gerät bestätigt; es gibt keine uncommitteten Produktcode-Änderungen aus dieser Sitzung.

## Nachweislich umgesetzt

| Bereich | Stand |
|---|---|
| Heimatpunkt | Siebenstufiges Onboarding; Heimatpunkt per expliziter Vordergrund-Standortabfrage oder Karte setzen; 500-m-Radius. PR #2 korrigierte die Kartenansicht. |
| Leser und Karte | USB-CCID/APDU-Kommunikation, USB-Freigabe, Slot/ATR/Reader-Diagnose und Gen1/Gen2/Gen2V2-Erkennung sind vorhanden. Der echte Galaxy-S23-/CCID-/Gen2V2-Test bestätigte die Kommunikation; andere Hardware ist dadurch nicht pauschal verifiziert. |
| Aktivitätsdatei | PR #4 liest `EF Driver_Activity_Data` bis zur auf der Karte angegebenen Länge vollständig in den Arbeitsspeicher. Am Testgerät: 13.776 Bytes. |
| Parser | PR #5 dekodiert die zyklischen Tagesdatensätze und Tätigkeitswechsel samt Kartenstatus. Am Testgerät: 74 Datensätze. Parser-Tests prüfen Ringgrenzen und ungültige Daten. |
| Ortskalendertage | PR #6 projiziert UTC-Kartenintervalle auf lokale Tage, einschließlich Mitternacht und Sommerzeit. Fehlende Kartenabschnitte werden nicht zu Ruhe ergänzt. |
| Kartentag-UI | PR #7 zeigt eine kompakte Tagesübersicht und zoombare/verschiebbare Detailansicht mit auswählbaren Abschnitten, Start/Ende/Dauer und Zeitmarke. Die lange Abschnittsliste bleibt zuschaltbar. |
| Aktivitäts-Teilimport | PR #9 ergänzt ausdrücklich ausgelöstes Speichern der vollständigen Aktivitätsdatei in Room/SQLCipher, Wiederöffnung der jüngsten gespeicherten Vorschau und Deduplizierung identischer Snapshots. Schema-Migration 1 → 2; keine automatische Speicherung durch den Lesetest. |
| Fahrzeug-Teilimport | PR #10 liest `EF Vehicles_Used` vollständig nach der auf der Karte angegebenen Anzahl, dekodiert den zyklischen Gen1-/Gen2-Datensatz und speichert nach gesonderter Bestätigung einen verschlüsselten Snapshot. Quellvorschau mit Kennzeichen, Zeit und Kilometerständen; Migration 2 → 3. Echter Gen2V2-Test: 69 Nutzungen nach Wiederöffnung ohne Karte sichtbar. |
| Fahrzeug im Kartentag | PR #11 zeigt belegte Fahrzeug-Zeitfenster und den tatsächlichen Nutzungsbeginn über der Aktivitäts-Zeitleiste, sofern die Snapshots zur selben Karte gehören. Offene Enden werden nicht fortgeschrieben; Fahrzeug zur Zeitmarke nur bei genau einem passenden Intervall. Die Fahrzeughistorie folgte in PR #12. |
| Fahrzeughistorien-Vorschau | PR #12 gruppiert alle gespeicherten Fahrzeug-Snapshots nach gültiger VIN oder Staat/Kennzeichen, entfernt wiederholte Nutzungen und zeigt erste/letzte Belegzeit sowie einzelne Karten- und Tacho-Quellwerte. Keine berechneten Gesamtkilometer, Arbeitstage oder fahrzeugbezogene Arbeitszeiten. |
| Fahrzeughistorie bedienen | PR #13 ergänzt die Suche nach Kennzeichen/VIN und zeigt die Nutzungen in nachladbaren Zehnerschritten. Die Quellwerte werden nicht neu berechnet. |
| Quellzeiten am Kartentag | PR #14 summiert die belegten Abschnitte nach Tätigkeit bei eingelegter Karte und zeigt Karteneinträge ohne Karte sowie unbelegte Zeit separat. Keine fachliche Arbeits-, Ruhe- oder Abrechnungsberechnung. |
| Fahrzeuglink im Kartentag | PR #15 öffnet für ein eindeutig belegtes Fahrzeug zur Zeitmarke die gespeicherte Historie mit Kennzeichen-/VIN-Suche. Bei mehrdeutiger Zuordnung erscheint kein spezifischer Link. |
| Ausgewählte Kartennutzung | PR #16 hebt beim Wechsel aus dem Kartentag den exakten Quellbeleg in der gespeicherten Fahrzeughistorie hervor. Fehlt er dort, wird das ausdrücklich angezeigt. Keine Strecke oder fahrzeugbezogene Arbeitszeit berechnet. |
| Aktivitäts-Snapshot-Abfrage | PR #17 ergänzt die geordnete Abfrage aller ausdrücklich gespeicherten Aktivitätsdateien für eine spätere Teilimport-Übersicht. Keine neue Persistenzform oder abgeschlossene Importhistorie. |
| Aktivitäts-Teilimport-Übersicht | PR #18 zeigt nur ausdrücklich gespeicherte Aktivitäts-Snapshots nach Speicherzeit, Kartengeneration und Datensatzanzahl an; Kartennummern sind maskiert. Noch keine vollständige Importhistorie. |
| Fahrzeug-Teilimport-Übersicht | PR #19 zeigt die separat gespeicherten Fahrzeug-Snapshots mit Speicherzeit, Generation und Nutzungsanzahl. Keine fiktive Kopplung beider Dateien zu einem vollständigen Import. |
| Älteren Snapshot ansehen | PR #20 öffnet einen ausdrücklich gespeicherten Aktivitäts-Snapshot in der bestehenden Kartentag-Vorschau und erlaubt die Rückkehr zum jüngsten Stand. Nur read-only, kein erneuter Import. |
| Gespeicherte Dateizeiträume | PR #21–#22 zeigen die belegten Kartentage sowie erste/letzte Fahrzeugnutzung je Snapshot, ohne offene Enden zu erfinden. Kurze Kartennummern sind vollständig maskiert. |
| Einzelne Fahrzeugdatei ansehen | PR #23–#24 öffnen eine ausdrücklich ausgewählte Fahrzeugdatei in ihrer eigenen Historien- oder Kartentag-Vorschau; sie ist kein vollständiger Import. Drei Fahrzeuge aus 69 Nutzungen wurden am Gerät geprüft. |
| Startseitenstatus | PR #25 unterscheidet noch fehlende, gespeicherte und nicht prüfbare Teilimporte; die Fachauswertung bleibt offen. Am Gerät bestätigt. |
| Gemeinsam gespeicherte Quellen | PR #26: Beide validierten Dateien aus demselben Lesevorgang können ausdrücklich in einer Transaktion gespeichert werden. Die Zuordnung verweist auf die unveränderten deduplizierten Quellen. Gemeinsame Vorschau aus der Dateiübersicht; additive Migration 3 → 4. Alte Einzeldateien bleiben ohne erfundene Zuordnung erhalten. Gemeinsames Speichern und Verfügbarkeit nach Lesertrennung/App-Neustart am 01.10.2026 bestätigt. |
| Technische Basis | Fünf Haupttabs, Room/SQLCipher-/Keystore-Basis, DataStore, Debug-/Internal-Builds und CI vorhanden. |

Die echte Kartenprobe mit USB-CCID, APDU, Kartenerkennung, vollständiger Aktivitätsdatei und Vorschau **hat bereits stattgefunden**. Der früher hier angekündigte „nächste Hardwaretest“ ist erledigt. Die Probe belegt den getesteten Leser und die getestete Gen2V2-Karte, nicht jede Karten-/Leserkombination.

## Test-/Preview-Grenze

Der Button „Kartendaten testen“ liest und dekodiert zunächst im Arbeitsspeicher. Gesonderte Knöpfe persistieren die Aktivitäts- und Fahrzeugdatei gemeinsam oder einzeln als verschlüsselte Teilimporte. Bei gemeinsamer Speicherung wird die Herkunft aus einem tatsächlichen Lesevorgang festgehalten; eine identische Dateikombination erzeugt keinen zweiten Eintrag. Die Speicherzeit bezeichnet die erste Bestätigung der Kombination, keine lückenlose Liste aller Leseversuche. Die Zeitleiste kann aus passenden gespeicherten Aktivitäts- und Fahrzeugdaten auch nach einem Neustart eine Vorschau erzeugen; Fahrzeugdaten erscheinen außerdem getrennt als Quellvorschau. Es gibt noch keinen fertig integrierten Tagesbestand. „Karte nicht eingelegt“ und der dort kodierte Tätigkeitswert sind getrennte Quellinformationen; Kartenentnahme allein beweist weder Ruhe noch Heimkehr. Smartphone-Routenaufzeichnung findet nicht statt.

## Offen

1. Die Einzeldateien und gemeinsam gespeicherten Quellen zu einer vollständigen Kartenimport-Pipeline mit Profilzuordnung, Importhistorie und dauerhaftem Tagesbestand ausbauen. Die Deduplizierung gilt für identische Quell-Snapshots und identische gemeinsam bestätigte Dateipaare. Vollständige Importhistorie, Profilbindung, weitere Dateien und normalisierter Tagesbestand fehlen weiterhin.
2. Fahrzeugnutzungen über die Quell-Vorschauen hinaus belastbar mit Tätigkeiten, dauerhafter Fahrzeug-Entität und geprüften Tageskennzahlen verknüpfen; weitere Karten-/Tachographendaten einlesen: GNSS-Einzelpunkte, Grenzübertritte, Ereignisse/Störungen, Kontrollen und weitere Marker. Keine künstliche Route.
3. Tageskennzahlen und fachliche Auswertung von Heimkehr, Reise/Übernachtung, Arbeitszeit, Spesen und Arbeitgeberzuschlägen gemäß Issue #3. Keine Steuerlogik.
4. PDF-/Exportfunktionen und spätere Komfortfunktionen getrennt umsetzen. Vorbereitete Entities/Interfaces und UI-Platzhalter gelten nicht als fertige Funktion.

## Verifikation

Der [CI-Lauf auf `main` nach PR #26](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36743029047) bestand Build, Unit-Tests, Lint, Debug-/Internal-APK-Verifikation sowie Room-/SQLCipher-/Compose-Emulatortests. Bereits der [Lauf zur getesteten PR-APK](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361) war vollständig grün (13 Daten- und 22 App-Instrumentierungstests). Die zusätzliche [PR-Prüfung nach dem Schemaexport](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36742530657) scheiterte vor den App-Tests bei der Emulator-Beschleunigung; ihr Build-/Test-/Lint-/APK-Job war erfolgreich. Der spätere grüne `main`-Lauf ist der aktuelle vollständige Nachweis.

**Letzter erfolgreicher Gerätetest: 01.10.2026, PR #26.** Galaxy S23/Android 16, CCID-Leser und echte Gen2V2-Fahrerkarte. Gelesen: Aktivitätsdatei 13.776 Bytes, 74 Tagesdatensätze; Fahrzeugdatei 9.600 Bytes, 69 Nutzungen. „Beide Dateien gemeinsam speichern“ meldete Erfolg; die Übersicht zeigte genau ein Dateipaar mit diesen Anzahlen. Der Nutzer bestätigte ausdrücklich Lesertrennung und App-Neustart; im letzten Screenshot war kein Leser angeschlossen und das gespeicherte Dateipaar weiterhin sichtbar. Der Test ist damit für diese Hardware-/Kartenkombination bestanden. Automatische Rollback-/Deduplizierungs-/Migrationsprüfungen und Testgrenzen stehen in [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).

Die Berichte vom 22.09.2026 beschreiben frühere, inzwischen behobene Build-/Zugriffsblockaden und sind nur historische Nachweise.
