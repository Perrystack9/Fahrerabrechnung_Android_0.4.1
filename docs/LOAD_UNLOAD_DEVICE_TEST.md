# FahrerTagebuch Test – Be- und Entladeeinträge

Vorbereitete Anleitung; nicht vor11.10.2026 00:56 Europe/Berlin zur Hardwareprüfung ausgeben. Nur nach vollständiger grüner CI des exakten Produktkopfs. Update40113/Schema15 auf die vorhandene Test-App40112/Schema14 mit gleicher Signatur installieren. App nicht deinstallieren oder Daten löschen.

1. Nach dem Update müssen Profil, bisherige Imports/Kartentage/Fahrzeuge sowie GNSS-, Beginn-/Ende- und Grenz-/Länderquellen verfügbar bleiben.
2. Eigene Karte erneut lesen. Bei Gen2V2 erscheint zusätzlich „Be- und Entladeeinträge“ mit eigenem gespeichertem/leerem oder Fehlerstatus. Ältere Generationen versuchen diese Datei nicht. Eine fehlerhafte Datei darf andere gültige Kartendaten nicht blockieren.
3. Die neue Übernahme öffnen → Fahrerkarten-Details → Be- und Entladeeinträge. Eine gültig leere Datei wird ausdrücklich ohne Vorgänge angezeigt. Bei vorhandenen Vorgängen sind Art, Beginn und davon getrennte Positionszeit sichtbar; keinen zusätzlichen realen Vorgang für den Test erzeugen. Alte Übernahmen ohne diese Quelle erhalten keine späteren Vorgänge.
4. Karte/Leser abziehen, App schließen und erneut öffnen. Gespeicherte neue Angaben und bisherige Daten müssen erhalten bleiben.

Kein neuer Hardwareerfolg ist bisher bestätigt. Falls der spätere Block weitere Quellen ergänzt, diese Anleitung für das gemeinsame Update entsprechend bündeln.
