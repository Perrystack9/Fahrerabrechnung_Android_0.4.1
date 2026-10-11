# Gebündeltes Testupdate: Zertifikate und Originaldateien

Nur die neue Update-APK über die vorhandene **FahrerTagebuch Test** installieren. Nicht deinstallieren und keine Daten löschen. Basis: dein bestätigtes PR58-Testupdate40117/Schema18. Neues kumulatives Update40119, gleiche Testkennung und feste Signatur, Schema18 unverändert.

Die Ausgabe erfolgt nach dem erneuten Arbeitsauftrag vom11.10.2026 um09:10:27 Europe/Berlin, nach dem zweistündigen Arbeitsblock bis11:10:27 und bei vollständig grüner CI. Diese vorbereitete Anleitung ist noch keine Gerätebestätigung.

1. Vorhandenes Profil und alte Übernahmen öffnen: bisherige Daten bleiben erhalten. Eine alte Übernahme darf keine Zertifikate oder Originaldateien aus einem späteren Import anzeigen.
2. Eigene Karte mit dem bisherigen USB-Leser automatisch einlesen lassen. In **Fahrerkarte → Details → Zertifikate** die vier Arten öffnen: Karten-, Signatur-, CA- und Link-Zertifikat. Gen1 unterstützt nur Karten-/CA-Zertifikat. Vorhandene Angaben bzw. leere/fehlende Dateien unterscheiden; eine leere Quelle bestätigt keinen nichtleeren Datensatz.
3. Unter **Anwendungs- und Identifikationsdateien** die Anwendungsdatei, Anwendungsdatei Version2 und Identifikationsdatei öffnen. Auf der bisherigen Gen2V2-Karte sind17Byte bzw. bei einem Legacy-Format15Byte,10Byte und143Byte vorgesehen. Die angezeigten Speicherplätze sind Kapazitäten, keine tatsächlichen Eintragszahlen. Fehlstatus im Importverlauf mit prüfen.
4. Zertifikatsreferenzen und Kartennummer zunächst verborgen bzw. maskiert. Bewusst anzeigen, Art wechseln, zurückgehen und eine andere Übernahme öffnen: wieder verborgen. Keine vollständigen Nummern, Referenzen, Kennungen oder Rohdaten in die Rückmeldung kopieren.
5. Karte entnehmen, App vollständig schließen und ohne Leser wieder öffnen. Dieselbe Übernahme auswählen: ihre Angaben bleiben erhalten. Eine ältere Übernahme behält ihren damaligen Leer-/Fehlstand. Insbesondere Dateien aus einem jüngeren Import dürfen nicht übernommen werden.
6. Eigene Karte nochmals automatisch lesen lassen. Beide Lesevorgänge getrennt im Importverlauf öffnen; die frühere Übernahme bleibt unverändert. Kein gesondertes Speichern und keine erneute Bestätigung einer bereits gebundenen eigenen Karte.

Für eine kurze Rückmeldung genügen: Updateinstallation/Datenbestand, automatisches Lesen, Zertifikatsansichten, drei Originaldateien, Wiederöffnung und alter/neuer Import getrennt. Fehlende Dateien bitte mit ihrer Bezeichnung und dem angezeigten Status nennen.

Signatur und Vertrauenskette der Zertifikate sind noch nicht geprüft. Zeitfelder sind Angaben laut Datei, kein bestätigter Gültigkeitsstatus. Kein Schreiben auf die Karte. Noch keine vollständige Kartenabdeckung und kein vollständiger signierter Standarddownload. Die neuen Import-Entwürfe bleiben bis zu deiner späteren ausdrücklichen Gerätebestätigung ungemergt.
