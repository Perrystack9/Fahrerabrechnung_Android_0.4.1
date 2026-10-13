# FahrerTagebuch - aktuelle Übergabe

Einstieg ausschließlich über [WORK_START.md](../WORK_START.md) und aktuellen Git/GitHub-Stand. Regeln und bestätigten technischen Zustand nicht aus alten Handoffs rekonstruieren.

## Nächster Auftrag nach dem Dokumentationsabschluss

**Pflichtdateien-/Ausnahme-/Signaturmatrix für Gen1, Gen2 und Gen2V2 erarbeiten.** Der [Import-Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) legt Ausgabe und Prüfkriterien fest. Vorhandenen Import übernehmen; Reader, Profilbindung, Zwei-Anwendungs-Gruppen und Schema 19 sind bereits implementiert.

Die Matrix muss jede relevante MF-/DF-Datei ihrer Anwendung, vorhandenen Originalquelle, Pflicht/Ausnahme, erforderlichen Signatur und konkreten Primärstelle zuordnen. Getrennt dokumentieren, was noch fehlt und was ein bewusst ausgelöster Standarddownload zusätzlich verlangt. Keine Vollständigkeit oder Benutzerkarten-Vertrauensprüfung vorwegnehmen.

## Offene Sperren und Risiken

- `7F66`: inneres Primärformat ISO 7816-4:2013 §12.7.1 noch nicht vollständig belegt. Vorhandene Rohbytes/äußere Struktur erhalten; keine Puffergröße erraten.
- Signaturaufnahme, ERCA/MSCA/Kartenvertrauen, Originalquellenexport und Standarddownload sind eigene spätere Schritte. Signatur, Originalbytes, konkrete Anwendung und Sitzung unveränderlich zusammenbinden.
- Automatischer Import bleibt lesend. Download-Datum erst in einem getrennt belegten Schreib-/Fehler-/Hardwarepfad aktualisieren.
- `0540`: legitimer authentisierter SC5-/Secure-Messaging-Weg fehlt; kein blindes Lesen oder Schlüsselersatz.
- Neue relevante Produktänderung braucht eigene vollständige CI und erforderliche Gerätebestätigung. Freigabe 40122 ist keine Freigabe neuer Befehle, Migrationen oder Sicherheitsfunktionen.
- Laufende CI und offene PRs live prüfen. Bei externer CI-/Budgetblockade tatsächlichen Stand sichern, Ursache nennen und keine eigenmächtige Budgetänderung oder Retryserie starten.

## Spätere Produktarbeit

Nach geklärter Import-/Quellenbasis: neutrale Tätigkeits-/Fahrzeugauswertung und grundlegende UI-/Einrichtungs-/GNSS-Konzeption. Zielrichtung etwa Start, Kalender, Auswertung, Einstellungen; Details offen. Keine Spesen-, Lohn-, Heimkehr-, Heimatstandort- oder PDF-Abrechnungsphase.

## Sitzungsabschluss

Nur diesen aktuellen Übergabetext und Status ersetzen, Arbeit im zugehörigen Branch/PR sichern, Prüf-/Freigabestatus knapp melden und Sitzung beenden. Historische Nachweise nur gezielt öffnen. Keine neue größere Produktarbeit im Dokumentations-Cleanup beginnen.

Nächster Startsatz: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
