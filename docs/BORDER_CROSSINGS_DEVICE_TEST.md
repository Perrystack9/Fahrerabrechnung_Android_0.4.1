# FahrerTagebuch Test – Grenz-/Ländereinträge

Update 40112, Schema 14, ausgehend vom bestätigten PR52-Teststand 40111/Schema 13. Dieselbe Paketkennung `de.fahrerabrechnung.app.devicetest` und feste Testsignatur. Nur die Update-APK über deine vorhandene **FahrerTagebuch Test** installieren. App nicht deinstallieren, keine Daten löschen und keine alte Ausgangs-APK installieren.

1. Vorhandene Kartentage, Fahrzeuge, GNSS- und Beginn-/Ende-Orte müssen nach dem Update weiter vorhanden sein.
2. Eigene Fahrerkarte erneut lesen. Bei einer Gen2V2-Karte zeigt der Import zusätzlich „Grenz- und Ländereinträge“ mit gespeichertem, leerem oder eigenem Fehlerstatus. Gen1/Gen2 unterstützen diese Datei nicht und versuchen sie nicht.
3. Die neue Übernahme öffnen → Fahrerkarte · Details → Grenz- und Ländereinträge. Vorhandene Länder-/Positionsangaben ansehen. Eine gültig leere Quelle muss als gelesen ohne Aufzeichnung erkennbar sein. Karteneinlage-Einträge sind möglich; ein echter Auslandseinsatz ist für diesen Test nicht erforderlich. Alte Übernahmen ohne neue Quelle dürfen keine späteren Einträge erhalten.
4. Karte/Leser abziehen, App schließen und wieder öffnen. Gespeicherte Angaben dieser Übernahme und die bisherigen Daten müssen erhalten sein.

Eine fehlende/fehlerhafte Grenzdatei darf die Speicherung anderer gültiger Dateien nicht verhindern. Falls Lesen, Anzeigen oder Wiederöffnen scheitert, den sichtbaren Fehler mitteilen. Sensible Kartennummern bleiben maskiert. Diese neue Leser-/Persistenz-/Updateprüfung ist noch nicht bestätigt; der PR bleibt bis zu Tobis ausdrücklicher Rückmeldung offen.
