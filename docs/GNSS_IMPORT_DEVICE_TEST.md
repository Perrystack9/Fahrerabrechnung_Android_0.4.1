# GNSS-Import: vorgesehener Gerätetest

Stand 10.10.2026: neuer Leser-/Persistenzschritt, Version **40110**, Schema **12**. CI und Gerätetest dieses Schritts sind noch offen. Diese Anleitung ist vorbereitet; sie belegt keinen Testerfolg. Test-ZIP erst nach vollständig grüner Android-CI für den exakten Quellcommit bereitstellen. PR erst nach ausdrücklicher Testbestätigung mergen.

Auf der vorhandenen **FahrerTagebuch Test** (PR #45, Version 40109/Schema 11) nur die Update-APK installieren. App und Daten behalten. Ausgangs-APK nur bei einer ausdrücklich gewünschten frischen Migrationseinrichtung verwenden; sie ist für das vorhandene Gerät nicht nötig.

1. Update installieren, App öffnen: vorhandener Fahrer, Importverlauf und Kartendaten sind erhalten.
2. Eigene Fahrerkarte mit dem bisherigen USB-Leser lesen. Aktivitäten, Fahrzeuge und bisherige Detailquellen bleiben verfügbar; GNSS-Dateien erscheinen im Übernahmebeleg jeweils separat. Generation 1 versucht keine GNSS-Dateien, Generation 2 keine Authentisierungsdatei.
3. Kartendaten → Details → GNSS-Aufzeichnungen öffnen. Punkte und aufgezeichneter Authentisierungsstatus sind getrennte Quellen. Gültig leer heißt keine Aufzeichnung; fehlende/fehlerhafte Quellen bleiben erkennbar. Keine Strecke und kein aus einem anderen Zeitpunkt ergänzter Punkt. Anschließend einen alten Import auswählen: dort dürfen keine neuen GNSS-Punkte erscheinen.
4. App schließen und erneut öffnen: alte Daten und der neue Beleg samt exakt zugehörigen GNSS-Quellen sind vorhanden. Erneutes unverändertes Lesen erzeugt einen neuen Beleg, aber keine doppelte Rohquelle. Ergebnis und gegebenenfalls Screenshot melden.

Status 01 ist der vom Fahrtenschreiber gespeicherte Status, keine von der App ausgeführte kryptografische Prüfung. Ein Status wird keinem Punkt zugeordnet. Einzelpunkte sind kein vollständiger Fahrtverlauf.
