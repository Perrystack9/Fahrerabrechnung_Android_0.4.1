# Gerätetest der begrenzten Profilbindung

Ausgangspunkt: bisher getestetes Galaxy S23 / Android 16, vorhandener USB-CCID-Leser und eigene Gen2V2-Fahrerkarte. Dieser Test prüft die neue Profil-/Importzuordnung; die alten Lesefunktionen werden nicht neu implementiert. Weitere Karten/Leser bleiben durch diesen Test unbestätigt.

## Installation und bestehende Daten

Die Debug-APK wird als ZIP aus dem vollständig grünen PR-CI-Lauf bereitgestellt. Nach einem zulässigen Update müssen ältere gespeicherte Dateien im **Quellarchiv und frühere Teilimporte** erhalten bleiben; die Migration erzeugt daraus keine Profilbindung oder neuen Profil-Teilimport.

Falls Android das Update wegen einer abweichenden Debug-Signatur ablehnt: nicht zur Umgehung deinstallieren oder App-Daten löschen. So lässt sich der Erhalt vorhandener Daten nicht prüfen. Die Migration ist zusätzlich mit SQLCipher automatisch geprüft; ein Gerätetest nach Neuinstallation ersetzt keinen echten Update-/Migrationstest. Der Installationsweg ist mit der Rückmeldung anzugeben.

## Eigene Karte ausdrücklich bestätigen

1. **Fahrerkarte ansehen → Kartendaten testen** mit der eigenen Karte ausführen. Aktivitäten und Fahrzeuge müssen weiter lesbar sein. Ohne Profilzuordnung darf noch kein Speichern im Fahrerprofil möglich sein.
2. **Eigene Fahrerkarte zuordnen** öffnen. Angezeigten Inhaber und Geburtsdatum mit der eigenen Karte vergleichen. Erst **Abbrechen** wählen: keine Zuordnung und kein neuer Profil-Teilimport.
3. Erneut öffnen und **Das ist meine Karte** bestätigen. Danach muss **Diese Karte ist deinem Fahrerprofil zugeordnet** erscheinen. Die Bestätigung allein legt noch keine Teilimport-Sitzung an.
4. **Beide Dateien gemeinsam speichern** wählen. Erwartet: Erfolgsmeldung zum Fahrerprofil-Teilimport. Sind die Dateien schon im alten Archiv identisch vorhanden, sind **0 neue Quelldateien** korrekt; trotzdem ist genau eine neue Teilimport-Sitzung vorhanden. Alte Quellen bleiben unverändert.

## Wiederöffnung und erneutes Lesen

5. Unter **Gespeicherte Teilimporte ansehen → Teilimporte im Fahrerprofil** die neue Sitzung öffnen. Tages-/Nutzungsanzahlen und ausgewählte Dateien müssen dem gerade gespeicherten Lesevorgang entsprechen. Es bleibt ein Teilimport, kein vollständiger Kartenimport.
6. Leser entfernen, App vollständig beenden und neu starten. Die Profil-Teilimport-Sitzung und beide gespeicherten Quellen müssen ohne Leser verfügbar bleiben; dieselben Tages-/Nutzungsanzahlen erscheinen wieder.
7. Dieselbe eigene Karte erneut lesen. Keine erneute Erstkartenbestätigung. Erneut gemeinsam speichern: eine weitere Sitzung, identische Quelldateien nicht nochmals speichern. Neue Quelldateien können 0 sein; bei tatsächlich geänderten Daten auch 1 oder 2.

## Altes Archiv bleibt getrennt

8. Eine ältere Datei oder ein älteres Dateipaar aus dem **Quellarchiv und frühere Teilimporte** öffnen. Es darf dadurch keine zusätzliche Profil-Teilimport-Sitzung entstehen. Die Fahrzeugvorschau dieser Datei darf keine Quellen aus einer anderen Sitzung ergänzen.

Rückmeldung: Installationsweg (Update/Neuinstallation), Ergebnis der Bestätigung und des Abbruchs, gemeinsame Speicherung, Wiederöffnung ohne Leser sowie Auffälligkeiten im alten Archiv. Keine vollständigen Kartennummern oder Rohkartendaten senden. Eine zweite/fremde echte Karte ist für diesen Test nicht erforderlich; Match-, Fremdkarten- und Rollbackfälle sind zusätzlich durch synthetische automatische Tests abgedeckt.
