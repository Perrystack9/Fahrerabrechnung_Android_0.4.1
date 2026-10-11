# Gemeinsames Testupdate nach dem Arbeitsblock

Nur das neue Update über die bestehende FahrerTagebuch-Test-App installieren. Nicht deinstallieren oder Daten löschen. Paketkennung und feste Testsignatur bleiben gleich; Ausgangspunkt ist der allgemein bestätigte PR53-Stand40112/Schema14. Das kumulative Update40115/Schema17 enthält Be-/Entladen, Ladungsart und Fahrtenschreiber-Nutzungen. Ausgabe/Hardwaretest frühestens11.10.2026 00:56:06 Europe/Berlin und erst bei vollständiger grüner CI.

1. Update installieren und vorhandenes Profil sowie alte Übernahmen öffnen. Alte Dateien bleiben erhalten; Übernahmen vor den neuen Quellen dürfen keine späteren Einträge übernehmen.
2. Eigene Karte am vorhandenen USB-Leser automatisch einlesen lassen. Importverlauf und Fahrerkarte→Details öffnen: **Be- und Entladeeinträge**, **Ladungsart-Einträge**, **Fahrtenschreiber-Nutzungen**. Vorhandene Originalangaben prüfen; eine gültige leere Datei soll ausdrücklich leer erscheinen, fehlend/Lesefehler separat. Ein fehlender Quellbeleg gilt nicht als erfolgreich getesteter nichtleerer Datensatz.
3. Karte entnehmen, App vollständig schließen und ohne Leser wieder öffnen. Genau diese Übernahme erneut anzeigen; vorhandene neue Angaben bzw. deren Leer-/Fehlstatus müssen erhalten bleiben. Eine ältere Übernahme wählen: keine späteren Belege als Ersatz anzeigen.
4. Karte erneut einlesen: neue Übernahme getrennt öffnen, frühere Übernahme unverändert erhalten. Bei Fahrtenschreiber-Nutzungen ist der Herstellercode der des Fahrtenschreibers; Software ggf. als Rohbytes. Keine LKW-Identität oder Nutzungsdauer aus diesen Einträgen prüfen.

Diese Anleitung ist vorbereitet und noch keine Gerätebestätigung. Erst Tobis spätere Rückmeldung dokumentiert tatsächlich geprüfte Schritte; neue Entwürfe vorher nicht mergen. Automatische synthetische Tests decken Generationen, Kapazitäten, Fehler, Fremd-/Ersatzkarten und Manipulationen separat ab.
