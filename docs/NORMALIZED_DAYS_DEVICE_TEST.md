> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); verbindlich sind [CURRENT_RULES.md](CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Gerätetest: normalisierte Kartentage und Datenbank-Update 5 → 6

Die ZIP enthält zwei APKs derselben dauerhaft aktualisierbaren App **FahrerTagebuch Test**. Sie wird zusätzlich zur bisherigen Debug-/Internal-App installiert. Bestehende Apps und deren Daten nicht löschen. Die sichtbare technische Oberfläche bleibt für diesen Entwicklungsschritt bestehen; die spätere vereinfachte automatische Bedienung steht in CURRENT_RULES.

## 1. Einmalig eine Schema-5-Ausgangsdatenbank anlegen

Nur durchführen, wenn **FahrerTagebuch Test** noch nicht eingerichtet ist. Bei einer bereits eingerichteten Test-App direkt mit der Update-APK aus Schritt 2 fortfahren; die Ausgangsversion nicht erneut installieren.

1. `01-FahrerTagebuch-Test-Schema5.apk` installieren und im Installer **Öffnen** wählen. Das ist der bereits geprüfte PR-30-Produktcode mit eigener Test-Paketkennung und unveränderlicher Testsignatur.
2. Onboarding durchführen, eigene Karte lesen und ausdrücklich als eigene Karte bestätigen.
3. **Beide Dateien gemeinsam speichern**. Anzahl der Tagesdatensätze, Fahrzeugnutzungen und der gespeicherten Sitzung merken. Im bisherigen Kartenstand waren es 74 Tagesdatensätze und 69 Fahrzeugnutzungen; später hinzugekommene echte Kartendaten dürfen die Anzahlen verändern.
4. Leser abziehen und die App vollständig beenden.

## 2. Tatsächliches Update mit vorhandenen Daten

5. `02-FahrerTagebuch-Test-Update.apk` installieren. Android muss ein **Update von FahrerTagebuch Test** anbieten. Nicht deinstallieren und keine App-Daten löschen.
6. Ohne Leser öffnen: kein neues Onboarding. Die vorhandene Sitzung und ihre Aktivitäts-/Fahrzeugdaten müssen erhalten und zugänglich sein. Einen bekannten Kartentag und die Fahrzeugvorschau öffnen und mit dem Ausgangsstand vergleichen.
7. Erst durch Öffnen alter Dateien dürfen weder neue Sitzungen noch eine andere Karten-/Profilzuordnung entstehen. Ältere Daten bleiben ohne nachträgliche Vermutungszuordnung verfügbar.

## 3. Frischer Import auf Schema 6

8. Dieselbe eigene Karte erneut lesen: keine neue Erstkartenbestätigung. Gemeinsam speichern. Bei unveränderten Kartendaten sind **0 neue Quelldateien** korrekt; es entsteht eine weitere Sitzung und die Tagesdaten werden zusätzlich normalisiert gespeichert.
9. Leser entfernen, App vollständig beenden und erneut öffnen. Die neue Sitzung, Tageszeitleiste und Fahrzeugdaten müssen weiter verfügbar sein. Sowohl die vorherige als auch die neue Sitzung öffnen; Tätigkeitswechsel und gewählter Kartentag müssen zum jeweiligen Stand passen.
10. Bei einer weiteren Wiederholung bleiben unveränderte Quelldateien und Tagesinhalte dedupliziert. Keine zusätzlichen Tagesabschnitte, erfundene Ruhezeiten oder vermischte Quellen.

UTC-Tagesdatensätze der Karte und angezeigte lokale Kalendertage können wegen der Ortszeit unterschiedliche Anzahlen haben. Verglichen werden dieselben Quellen und Kalendertage vor/nach dem Update; die Projektion wird nicht fachlich neu berechnet.

Rückmeldung: Hat Android das Update akzeptiert? Waren vorhandene Daten danach ohne Leser verfügbar? Wurde die eigene Karte wiedererkannt? Stimmen die neuen und älteren Vorschauen nach Speicherung/Neustart? Keine unmaskierten Kartennummern, echten Rohdateien oder APDUs senden.

Dieser Test prüft den vorhandenen Leser und die eigene Gen2V2-Karte. Ringverschiebungen, andere Karten/Generation, Tagesfassungen, SQLCipher-Migration, Profiltrennung und erzwungene Speicherfehler werden zusätzlich mit synthetischen automatischen Tests geprüft. Die Stufe ist noch kein vollständiger Kartenimport oder fachlich zusammengeführter Kalender.
