# FahrerTagebuch · aktuelle Produktregeln

Stand: 27.09.2026. Diese Regeln ersetzen widersprechende ältere Handoffs und Spezifikationen.

- **Smartphone-Standort nur im Onboarding:** Auf ausdrücklichen Tipp darf der Heimatpunkt einmalig per Vordergrund-GPS bestimmt und auf einer kleinen Karte mit verschiebbarer Stecknadel korrigiert werden. Manuelles Setzen funktioniert ohne Berechtigung. Danach keine Standortabfrage, Aufzeichnung, Hintergrundberechtigung, Smartphone-Strecke oder GPS-Kilometer. Der gespeicherte Heimatpunkt ist eine Koordinate mit 500-m-Radius.
- **Karte nur zur Heimatpunktwahl.** Keine sonstige Karten-/Routenansicht. Keine Routenlinie, Abdeckung oder GPS/Tachograph-Vergleiche.
- **GNSS nur aus Fahrerkarte/Tachograph als punktuelle Quelldaten möglich.** Solche Nachweise niemals zu einer künstlichen kontinuierlichen Route verbinden.
- Das siebenstufige Onboarding startet beim ersten Öffnen automatisch; erst sein Abschluss öffnet die fünf Tabs. Die Einrichtung des Heimatpunkts ergänzt Schritt 2. Die übrige UI, Scrollzustand und Dark Mode bleiben erhalten.
- Die aktuelle Fachphase umfasst ausschließlich USB-CCID, ISO7816/APDU, Fahrerkarte, Kartengeneration und Parser-Basis. Reise-, Spesen-, Zuschlags-, Compliance-, PDF-, VIN- und Cloud-Fachlogik folgen in getrennten Aufträgen.
- Keine personenbezogenen Kartendaten, vollständigen APDUs oder Rohdumps in Logs oder Test-Fixtures.
