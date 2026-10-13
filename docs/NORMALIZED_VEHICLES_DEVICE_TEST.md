> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Gerätetest: gespeicherte Fahrzeugnutzungen und Update 6 → 7

Die ZIP enthält das Schema-7-Update derselben App **FahrerTagebuch Test**, die bereits für PR #32 eingerichtet wurde. Die Testsignatur und Paketkennung bleiben gleich. Bestehende Apps oder Daten nicht löschen. Die technische Oberfläche bleibt für diesen Entwicklungsschritt bestehen; die spätere automatische, einfache Bedienung folgt gesondert gemäß CURRENT_RULES.

## Mit vorhandener PR-32-Test-App

1. Vor dem Update einen bekannten gespeicherten Kartentag, Fahrzeugnutzungen und eine ältere Sitzung ansehen und merken. Leser abziehen und die Test-App vollständig beenden.
2. Nur `02-FahrerTagebuch-Test-Update.apk` installieren. Android muss ein **Update von FahrerTagebuch Test** anbieten. Nicht deinstallieren; `01-FahrerTagebuch-Test-Schema6.apk` wird bei der bereits eingerichteten App nicht benötigt.
3. Ohne Leser öffnen: kein neues Onboarding, gleiches Fahrerprofil. Den gemerkten Kartentag, die Fahrzeugvorschau und ältere Sitzung öffnen. Kennzeichen, Nutzungszeiten und Tacho-Quellwerte müssen zum bisherigen Stand passen; offene Nutzungsenden bleiben offen.
4. Eigene Karte erneut lesen: keine neue Erstbestätigung. Beide Dateien gemeinsam speichern. Bei unveränderten Kartendaten sind **0 neue Quelldateien** korrekt. Fahrzeugnutzungen werden intern zusätzlich dauerhaft gespeichert; keine weitere Bedienaktion dafür.
5. Leser entfernen, App vollständig beenden und neu öffnen. Die alte und neue Sitzung samt Kartentagen und Fahrzeugnutzungen müssen verfügbar sein. Fahrzeughistorie öffnen und einen bekannten Kennzeichen-/VIN-Eintrag vergleichen. Erneut unverändert lesen/speichern: keine zusätzlichen erfundenen Nutzungen, Kilometer oder Tätigkeiten.

Rückmeldung: Update akzeptiert? Vorhandenes Profil und alte Daten erhalten? Neue und alte Fahrzeugvorschauen nach Neustart korrekt? Keine unmaskierten Kartennummern, Rohdateien oder APDUs senden.

## Nur wenn die Test-App noch nicht eingerichtet ist

Zuerst `01-FahrerTagebuch-Test-Schema6.apk` installieren, Onboarding durchführen, eigene Karte bestätigen und beide Dateien gemeinsam speichern. Danach oben mit Schritt 1 fortfahren. Die Ausgangsversion enthält den geprüften PR-32-Produktcode. Nach Installation des Updates die Ausgangsversion nicht erneut installieren.

Dieser Test bestätigt nur den vorhandenen Leser und die eigene Karte. Andere Generationen, Ringverschiebungen, geänderte Nutzungsfassungen, Ersatz-/Fremdkarten, SQLCipher-Migration und Speicherfehler werden zusätzlich synthetisch automatisch geprüft. Fahrzeugnutzungen bleiben Quellbelege: noch keine dauerhafte aufgelöste Fahrzeugentität, berechnete Tages-/Monatskilometer oder vollständige Import-Pipeline.
