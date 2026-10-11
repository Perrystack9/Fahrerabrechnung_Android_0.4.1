# Kartenimport – Fortsetzung und Übergabe am 11.10.2026

## Verbindliche Ausgangslage

Repository `Perrystack9/Fahrerabrechnung_Android_0.4.1`. Main bleibt `1b592d627d5d18db9fc909f5c8cf2c796fd8578b`; letzte bestätigte Testversion PR58/40117/Schema 18. PR59 und PR60 sind offene Entwürfe. PR60-Produktkopf `85d0614235e4d9b5384d96d9ee124aab1f9fbc3a`, Tree `78155b21cb76f9c27231360bd78dad0aa5472a22`; vollständige CI `38145473572` und gezielter Paketlauf `38148088141` erfolgreich. Diese vorhandenen Nachweise sind laut GitHub-PR-Checkpoint bestätigt; kein erneuter Lauf für PR60 ausgelöst.

Bereitgestelltes Paket `FahrerTagebuch-PR60-Test-Update.zip`: Version 40120, SHA256 `77a66af59fb994892e852084f44bdedd857ba981aa4beef24c65765352e2b23a`; APK SHA256 `e287466c84f4bc38ecc1dc88971920eab30f11ff761cf8a9d9d179b10be81d82`. Paket, Anleitung, Produktkopf und beide bestehenden Entwürfe bleiben unverändert. **40120 noch nicht am Gerät bestätigt; kein Merge, kein Auto-Merge.**

Tobi hat selbstständige Weiterarbeit mit dem verbleibenden Kontingent beauftragt und am 11.10. um 19:47 Europe/Berlin erneut bestätigt. Kein neuer fester Stundenauftrag daraus abgeleitet. Keine Browsernutzung, Unteragenten, Bereinigung oder Löschung. GitHub-Actions-Budget unverändert 2 USD; keine Budgeteinstellungen verändert. Aktuelle Arbeit wird in einem getrennten kumulativen Folgeentwurf auf PR60 gesichert.

## Erledigt im neuen Folgeentwurf

- Bedingte MF-Dateien ATR/INFO `2F01` und Extended_Length `0006`: unabhängige vollständige begrenzte SELECT/READ-Lesung vor AID, eindeutige ATR-Ankündigung, Gen2/Gen2V2, keine neuen Transportbefehle.
- Exakte verschlüsselte Speicherung je eigener physischer Karte und ausgewählter ImportSession in bestehenden Schema-18-Tabellen; Deduplizierung, Request-Vereinigung, Art-/Karten-FKs, Rückrollen und kein historischer Fallback.
- Kommunikationsangaben in Fahrerkarten-Details, normaler Importübersicht und Verlauf; gespeicherte leere und fehlende Quellen bleiben unterscheidbar.
- Koordinator friert alle neuen Quellenmaps vor wartender Kartenprüfung ein; ausdrückliche Bestätigung verwendet weiter frische Lesung.
- 38 neue Testmethoden: 20 JVM, elf SQLCipher, sieben Compose/AppRoot. Alte Tests nicht entfernt oder gelockert. Amtlicher MF-/Fahrerkarten-Abgleich und genaue Grenzen in [CARD_COMMUNICATION_IMPORT.md](CARD_COMMUNICATION_IMPORT.md).

## Prüfstand

Lokale Projektgrenzen, zehn CI-Scope-/Schema-Guard-Regressionsprüfungen, Shellsyntax, eindeutige neue Testmethoden, Navigations-/Reset-Abgleich und Diffprüfung bestanden. Datenbank, Entities, Migrationen und gespeicherte Schema-18-Exporte unverändert. Lokal ist keine Android-/Gradle-Toolchain verfügbar; Android-, Unit-, Lint-, APK-/Signatur-/Updatepaar-, KSP- und Emulatornachweise müssen aus der vollständigen CI kommen. Nicht als bereits bestanden behaupten.

Neuer Entwurfszähler 40121, unveränderte feste Testkennung/-signatur; keine neue Test-ZIP ausgegeben. Neue vollständige CI wird erst nach gebündelter Code-/Testprüfung ausgelöst und hier dokumentiert. Ein Erfolg am alten PR60-Kopf gilt nicht als Prüfung der neuen Änderungen.

## Präzise verbleibende Grenzen

| Thema | Aktueller Stand | Nächster konkret erforderlicher Schritt |
|---|---|---|
| Geräte-/Merge-Freigabe | PR60/40120 und neuer Folgeentwurf unbestätigt | Ausdrückliche Gerätebestätigung mit genauer getesteter Version; erst danach unveränderten freigegebenen Kopf behandeln |
| MF ATR/INFO | Originalbytes und äußere `7F66`-Struktur vorhanden | Vollständigen ISO7816-4:2013-Inhaltsdecoder auf überprüfbarem Primärformat ergänzen, ohne ungesicherte Puffer-/Readerannahmen |
| Unbekannte ATR-Historie | Optionaldateien nicht versucht | Keine Vollständigkeitsbehauptung; ggf. getrennte herstellerspezifische bzw. amtliche ATR-Formatklärung |
| Gen1-MF DIR | Bisher nur Gen2-20-Byte-Format | Gen1-Format separat primär klären; keine Gen2-AIDs oder Template-Längen übernehmen |
| Beide Anwendungen auf Gen2-Karte | Ausgewählte erkannte Anwendung importiert | Separater generationsgetrennter Import beider DFs, eigene Quellen-/Sitzungsmodellierung; vorhandene Gen1-Inhalte nicht als bereits gelesen behaupten |
| VU_Configuration `0540` | Nicht gelesen, SC5-Zugriff | Autorisiertes Authentisierungs-/Secure-Messaging-Konzept und echte geprüfte Zugriffsfolge; keine blinde normale EF-Lesung |
| Signierter Standarddownload/DDD | Kein vollständiger Standarddownload | Amtliches Download-/Signaturprotokoll, echte Originalsignaturen und interoperablen Export separat implementieren |
| Zertifikats-/Signaturvertrauen | Nur strukturell dekodiert | Überprüfbare ERCA-/MSCA-Kette, RSA-/EC-Prüfung, Verfahren und Fehlerstatus implementieren |

Die nutzbaren ungesichert lesbaren Quellen der **ausgewählten** Gen1-Anwendung umfassen 16 Arten, Gen2 21 (23 mit beiden angekündigten Kommunikationsquellen), Gen2V2 27 (29 mit beiden bedingten Quellen). Das zählt Quellenarten, keine Zeitabdeckung oder aufgezeichneten Datensätze. Es ist **keine** Behauptung, alle Anwendungen, Signaturen und geschützten Kartenbereiche zu importieren. Fahrzeug-/Spesen-/Abrechnungslogik bleibt außerhalb dieses Schritts.

## Weiterarbeiten

Zuerst diesen Checkpoint und den aktuellen GitHub-Kopf/CI-Stand prüfen. Keines der bestehenden Pakete ersetzen, keinen Entwurf als gerätebestätigt markieren und keine neue APK ohne vollständigen Nachweis ausgeben. Bei Fortsetzung an klar abgegrenzten offenen Importpunkten arbeiten; [Produktregeln](CURRENT_RULES.md), [Abdeckung](CARD_SOURCE_COVERAGE.md) und [spätere Geräteanleitung](CARD_COMMUNICATION_DEVICE_TEST.md) beachten.
