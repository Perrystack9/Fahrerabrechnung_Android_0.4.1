# FahrerTagebuch Test – Be-/Entlade- und Ladungsart-Einträge

Vorbereitete gemeinsame Anleitung; nicht vor11.10.2026 00:56 Europe/Berlin zur Hardwareprüfung ausgeben. Nur nach vollständiger grüner CI des exakten Produktkopfs. Update40114/Schema16 auf die vorhandene Test-App40112/Schema14 mit gleicher Signatur installieren. App nicht deinstallieren oder Daten löschen.

1. Nach dem Update müssen Profil, bisherige Imports/Kartentage/Fahrzeuge sowie GNSS-, Beginn-/Ende- und Grenz-/Länderquellen verfügbar bleiben.
2. Eigene Karte erneut lesen. Gen2V2 übernimmt zusätzlich Be-/Entladeeinträge und Ladungsart-Einträge mit jeweils eigenem gespeichertem/leerem oder Fehlerstatus. Ältere Generationen versuchen diese beiden Dateien nicht. Eine fehlende oder fehlerhafte Quelle darf andere gültige Kartendaten nicht blockieren.
3. Die neue Übernahme öffnen → Fahrerkarten-Details → Be- und Entladeeinträge. Eine gültig leere Datei wird ausdrücklich ohne Vorgänge angezeigt. Bei vorhandenen Vorgängen sind Art, Beginn und davon getrennte Positionszeit sichtbar. Keine zusätzlichen realen Vorgänge für den Test erzeugen.
4. Zurück zu Fahrerkarten-Details → Ladungsart-Einträge. Bei vorhandenen Einträgen sind die aufgezeichnete Ladungsart und Eintragszeit sichtbar; „nicht festgelegt“ ist ein Kartenwert. Eine gültig leere Datei hat eine eigene Meldung. Keine Ladungsart am Fahrtenschreiber für diesen Test ändern.
5. Eine ältere Übernahme ohne die jeweilige Quelle öffnen. Sie darf keine Einträge aus der neuen Übernahme anzeigen. Die Quellen bleiben getrennt, auch wenn nur eine der beiden Dateien verfügbar ist.
6. Karte/Leser abziehen, App schließen und erneut öffnen. Gespeicherte neue Angaben und bisherige Daten müssen erhalten bleiben.

Kein neuer Hardwareerfolg ist bisher bestätigt. Weitere Quellen des zweiten Arbeitsblocks gegebenenfalls in dieses gemeinsame Update und dessen Anleitung aufnehmen.
