> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Ereignis-/Störungsmarker aus gespeicherten Übernahmen

Implementierte Markerprojektion ausschließlich aus den gespeicherten Quellen der ausgewählten eigenen Sitzung. Aktuelle Gerätebasis im technischen Status; dieser Projektionsschritt verändert keine Leser-/Parser-/Speicherlogik.

## Darstellung und Herkunft

- Über der kompakten Tageszeitleiste sowie der zoombaren Detailansicht liegen die aufgezeichneten Ereignis-/Störungszeitpunkte auf derselben realen Zeitachse wie die Tätigkeiten. Ereignis und Störung haben unterschiedliche Symbole und Farben.
- Jeder vorhandene Beginn und jedes vorhandene Ende ist ein eigener, entsprechend benannter Punkt. Gleiche Beginn-/Endzeit im selben Beleg ergibt einen gemeinsamen Punkt. Verschiedene Belege an derselben Position bleiben getrennt wählbar.
- Fehlende Zeitpunkte erzeugen keine Marker. Kein Ersatz durch Importdatum, Tagesgrenzen, Tätigkeitszeiten oder einen anderen Import. Auch invertierte Zeitpunkte bleiben unverändert; die Darstellung erzeugt weder Ereignisdauern noch eine Bewertung als Verstoß.
- Ortskalendertage werden durch ihre echten UTC-Grenzen begrenzt; 23-/25-Stunden-Tage und doppelte Ortszeiten bleiben unterscheidbar. Details zeigen Sekunden und UTC-Offset. Außerhalb des Zoomfensters liegende Punkte werden nicht an dessen Rand verschoben.
- Antippen öffnet den exakten Beleg. Bei mehreren Treffern erscheint eine Auswahl; die zusätzlich bedienbare Tagesliste erreicht auch dicht beieinanderliegende Punkte. Die bekannte Ereignisbezeichnung und vorhandenen Fahrzeug-/Zeitangaben stammen aus derselben Quelle wie die bereits getestete Übersicht.
- Die unveränderliche physische DriverCard-ID der ausgewählten eigenen Sitzung ist Voraussetzung. Fehlende Bindung, andere Karten und das getrennte Altarchiv erzeugen keine Marker. Ältere Sitzungen erhalten keine Dateien aus neueren Übernahmen. Ohne Aktivitätsquelle werden keine neuen Kartentage erfunden; die Ereignis-/Störungsübersicht bleibt separat verfügbar.

## HISTORISCH - Automatische Prüfung

Acht neue Unit-Tests prüfen fehlende/offene/invertierte/gleiche Zeitpunkte, getrennte Quellbelege, Mitternacht, beide Sommerzeitwechsel, physische Kartenidentität und Zoomgrenzen. Sechs neue Compose-Tests prüfen Antippen, exakte Sekunden/Offset, Überlagerungen, Zoom, Tageswechsel, Archiv-/Kartenbindung und die echte Navigation zwischen drei unterschiedlich gespeicherten Imports. Die bisherigen 248 Tests bleiben Teil der vollständigen CI. Schemaexport und APK-/Signatur-/Versionsprüfungen laufen weiterhin im bestehenden Workflow.

## HISTORISCH - Optionale Prüfung ohne Kartenleser

Die CI-ZIP aktualisiert die bereits bestandene **FahrerTagebuch Test** von **40106 auf 40107**, jeweils Schema 10, gleicher Paketname und gleicher Testschlüssel. Bei vorhandener App ausschließlich `02-FahrerTagebuch-Test-Update.apk` darüber installieren. Nicht deinstallieren oder Daten löschen; die Schema-10-Ausgangs-APK ist nur für eine neue getrennte Installation.

1. Ohne Leser eine eigene gespeicherte Übernahme mit Ereignisbeleg öffnen und zum tatsächlichen Ortsdatum wechseln. Auf der Tagesübersicht erscheint der belegte Zeitpunkt; Antippen zeigt den ursprünglichen Beleg mit Sekunden und Offset.
2. Zoomen/verschieben: Punkte folgen der Zeitachse und bleiben im Detail nur in ihrem tatsächlichen Zeitfenster sichtbar. Die Tagesliste öffnet jeden vorhandenen Beleg einzeln, auch bei überlagerten Punkten.
3. Eine ältere Übernahme vor PR #41 bzw. das getrennte Altarchiv öffnen: keine aus späteren Übernahmen ergänzten Marker. Profil, Kartentage und Fahrzeughistorie bleiben verfügbar.

Ein neuer USB-/Karten-/Migrationstest ist für diesen reinen Projektions-/UI-Schritt nicht erforderlich. Die vorhandene PR-41-Gerätefreigabe gilt weiter nur für die dokumentierte Kombination; neue Kartenquellen erhalten später ihren eigenen Hardwareprüfpunkt.

## HISTORISCH - Entstehungsbezug

Dieser begrenzte UI-Schritt verwendet ausschließlich die bereits gespeicherten Quellen der ausgewählten eigenen ImportSession. Leser, APDU, Parser, Speicherung, Schema 10 und Migrationen bleiben unverändert. Der letzte bestätigte Hardware-/Update-Test ist PR #41 vom 03.10.2026, 08:06 Uhr; dafür ist keine Wiederholung erforderlich.
