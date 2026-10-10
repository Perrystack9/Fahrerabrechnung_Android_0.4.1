# Beginn-/Ende-Orte: vorgesehener Gerätetest

Stand 10.10.2026: Version **40111**, Schema **13**. CI und Gerätetest dieses Schritts sind noch offen. Diese Anleitung ist vorbereitet; sie belegt keinen Testerfolg. Test-ZIP erst nach vollständig grüner Android-CI für den exakten Quellcommit bereitstellen. PR erst nach ausdrücklicher Testbestätigung mergen.

Auf der vorhandenen **FahrerTagebuch Test** (PR #51, Version 40110/Schema 12) nur die Update-APK installieren. App und Daten behalten. Die Ausgangs-APK wird auf dem vorhandenen Gerät nicht benötigt.

1. Update installieren, App öffnen: vorhandener Fahrer, Importverlauf, Kartentage und GNSS-Daten sind erhalten.
2. Eigene Fahrerkarte mit dem bisherigen USB-Leser lesen. Im Importverlauf erscheinen „Beginn-/Ende-Orte“ und bei Gen2V2 der „Orts-Authentisierungsstatus“ als getrennte Quellen. Bisherige Aktivitäts-, Fahrzeug- und GNSS-Daten bleiben verfügbar.
3. Gespeicherte Kartendaten → Details → Beginn-/Ende-Orte öffnen. Eintragsart, Zeit, Land/Region und vorhandene Einzelpositionen werden angezeigt. Eine gültige leere Datei ist ausdrücklich leer; fehlende oder nicht lesbare Quellen sind im Importverlauf erkennbar. Einen älteren Import auswählen: dort dürfen keine neuen Ortsdaten erscheinen.
4. App schließen und erneut öffnen: alte Daten und neuer Beleg mit seinen Ortsdaten bleiben vorhanden. Unverändertes erneutes Lesen darf keinen doppelten Rohbestand erzeugen. Ergebnis und gegebenenfalls Screenshot melden.

Die Einträge dienen als Kartenbelege. Daraus werden keine Arbeitszeiten, Heimkehr oder Fahrstrecken abgeleitet. Authentisierungsstatus ist separat aufgezeichnet und wird von der App keinem Ort zugeordnet.
