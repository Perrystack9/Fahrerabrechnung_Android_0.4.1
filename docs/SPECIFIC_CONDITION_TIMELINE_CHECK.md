> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Fähre/Zug- und Out-of-Scope-Marker aus gespeicherten Übernahmen

Implementierte Markerprojektion ausschließlich aus der gespeicherten Sonderbedingungsquelle der ausgewählten eigenen Sitzung. Leser-/Parser-/Speichergrenzen dieses Teilschritts bleiben unverändert. Aktuelle Gerätebasis im technischen Status; frühere PR-44-Ausnahme ist ein historischer Einzelbeleg.

## Darstellung und Herkunft

- Über der kompakten Tagesübersicht sowie im Zoom liegen nur tatsächlich aufgezeichnete Zeitpunkte auf derselben realen Zeitachse wie die Tätigkeiten. Separate Spur und Symbole: ↔ Fähre/Zug, ⊘ Out of Scope, ? unbekannte Sonderbedingung. Zeitgleiche Ereignisse/Störungen bleiben auf ihrer eigenen Spur erreichbar.
- Beginn und Ende bezeichnen ausschließlich den aufgezeichneten Code, keine daraus gebildeten Intervalle. Gen1-Code 03 bleibt „Fähre/Zug“ ohne erfundenen Beginn; Code 04 bleibt dort neutral. Fehlende oder zeitlich anders sortierte Gegenstücke erzeugen keine Dauer und ändern keine Tätigkeit.
- Fehlende Zeitpunkte erzeugen keine Tagesmarker und werden nicht durch Importdatum, Tätigkeit oder Tagesgrenze ersetzt. Die vorhandene Fahrerkarten-Detailübersicht bewahrt diese Belege als „Zeitpunkt: nicht angegeben“. Auch reservierte Codes mit bekanntem Zeitpunkt bleiben neutral sichtbar.
- Halboffene Ortskalendertage, 23-/25-Stunden-Tage und doppelte Ortszeiten bleiben unterscheidbar. Details zeigen Sekunden und UTC-Offset. Punkte außerhalb des Zoomfensters werden nicht an dessen Rand verschoben.
- Antippen öffnet genau den ausgewählten Beleg. Mehrere Treffer bleiben einzeln wählbar; die zusätzlich bedienbare Tagesliste erreicht auch dicht beieinanderliegende Punkte. Quellen-/Tageswechsel schließt den vorherigen Beleg.
- Die physische DriverCard-ID der ausgewählten eigenen Sitzung ist Voraussetzung. Fehlende Bindung, andere Karten und das getrennte Altarchiv erzeugen keine Marker. Alte Sitzungen erhalten keine neueren Dateien. Ohne Aktivitätsquelle werden keine Kartentage erfunden; die Sonderbedingungsübersicht bleibt separat verfügbar.

## HISTORISCH - Automatische Prüfung

Acht neue Unit-Tests prüfen Mitternacht, unbekannte Zeiten, einzelne/invertiert geordnete Codes, gleiche Zeitpunkte mit getrennten Slots, beide Sommerzeitwechsel, Gen1/reservierte Codes, physische Kartenidentität und Zoomgrenzen. Acht neue Compose-Tests prüfen Antippen, exakte Sekunden/Offset, gleichzeitige Sonderbedingungen/Ereignisse, Zoom, Quellen-/Tageswechsel, Archiv-/Kartenbindung, Gen1, doppelte Ortszeit und die echte Navigation zwischen drei unterschiedlich gespeicherten Imports. Die bisherigen 292 Tests bleiben Teil der vollständigen CI. Schemaexport und APK-/Signatur-/Versionsprüfungen laufen im bestehenden Workflow; finalen Nachweis am aktuellen PR-Kopf vor Merge prüfen.

## HISTORISCH - Optionale Softwareprüfung

Die CI-ZIP aktualisiert die bestehende **FahrerTagebuch Test** (40106, 40107 oder 40108) auf **40109**, gleicher Paketname und Testschlüssel. Aus Schema 10 führt weiterhin die bereits implementierte additive Migration auf 11; aus 40108 bleibt Schema 11 unverändert. Bei vorhandener App ausschließlich `02-FahrerTagebuch-Test-Update.apk` darüber installieren. Nicht deinstallieren oder Daten löschen; `01-FahrerTagebuch-Test-Schema10.apk` (40107) ist nur für eine neue getrennte Testinstallation.

1. Ohne Leser eine eigene gespeicherte Übernahme mit Sonderbedingungsbeleg öffnen und zum tatsächlichen Ortsdatum wechseln. Antippen zeigt den ursprünglichen Code/Zeitpunkt mit Sekunden und Offset; kein errechneter Zeitraum oder Ruhenachweis.
2. Zoomen/verschieben: Punkte folgen der Zeitachse und bleiben nur in ihrem tatsächlichen Zeitfenster sichtbar. Die Tagesliste öffnet jeden Beleg einzeln, auch bei überlagerten Punkten.
3. Einen älteren Import ohne diese Quelle, das getrennte Altarchiv bzw. eine gelesene leere Quelle öffnen: keine aus späteren Übernahmen ergänzten Marker. Profil, Kartentage und Fahrzeughistorie bleiben verfügbar.

Ohne reale Sonderbedingungsbelege kann diese Markerprüfung nicht am Gerät durchgeführt werden; die synthetischen automatischen Tests sind dann der vorhandene Nachweis. Es wird für diesen UI-Schritt keine erneute Kartenlesung verlangt. Das Überspringen bei PR #44 ist keine allgemeine dauerhafte Ausnahme für spätere Leser-/Import-/Migrationsänderungen und setzt den Hardware-Testzähler nicht zurück.

## HISTORISCH - Abschluss am 10.10.2026

[CI 37137254348, Versuch 6](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37137254348/attempts/6) am unveränderten Produktkopf `42fb5dea0fb8f18d1b2206bda5d09b59b4ebe8b1` vollständig grün: **143 Unit-, 99 Daten-/SQLCipher- und 66 App-/Compose-Tests, insgesamt 308 ohne Fehler oder übersprungene Tests**. Alle acht neuen Compose-Tests anhand der Instrumentierungsberichte als ausgeführt bestätigt. Alle sieben Gradle-Aufrufe, Lint, APK-/Signatur-/Versionsprüfung und bytegleicher Schema-11-Export bestanden.

Am 10.10.2026 um 05:52 Uhr Europe/Berlin bestätigt der Nutzer zur bereitgestellten Testversion 40109: „Daten sind nach dem schließen und wieder öffnen nach wie vor vorhanden.“ Die Bildschirmaufnahme zeigt gespeicherte Kartentage und Fahrzeughistorie ohne erneute Kartenlesung. Für die ausgewählte ältere Übernahme fehlen gespeicherte Sonderbedingungen. Das ist keine neue Leerdaten-Lesung oder Prüfung realer Fähre-/Zug-/Out-of-Scope-Marker. Installationsweg und Ausgangs-Versionscode sind nicht separat sichtbar; keine genauere Migrationsmessung behauptet. Letzte vollständige bestätigte Karten-/Leserprüfung bleibt PR #41 vom 03.10.2026, 08:06 Uhr.

Nach grüner Gesamt-CI und Nutzerbestätigung am unveränderten Kopf gemergt: `9f24a48845367502e585c87b1eb8e63f370b3dfb`, Tree `90b73aa63229055d5560d6e0f8ad63b855ab30d5`. CI-Infrastruktur aus PR #46/#47 erhalten. Die tatsächliche kurze Test-ZIP enthält nur das Update 40109; vorhandene Test-App beibehalten.
