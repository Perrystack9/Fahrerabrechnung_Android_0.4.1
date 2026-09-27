# FahrerTagebuch · aktuelle Produktregeln

Stand: 27.09.2026. Diese Regeln ersetzen widersprechende ältere Handoffs und Spezifikationen.

- **Smartphone/App GPS: nicht Bestandteil des Produkts.** Keine Standortberechtigung, dauerhafte Erfassung, Smartphone-Strecke, GPS-Kilometer oder GPS-Einstellung vorbereiten.
- **Keine Karten-/Routenansicht.** Keine Routenlinie, Abdeckung oder GPS/Tachograph-Vergleiche.
- **GNSS nur aus Fahrerkarte/Tachograph als punktuelle Quelldaten möglich.** Solche Nachweise niemals zu einer künstlichen kontinuierlichen Route verbinden.
- Die freigegebene UI samt siebenstufigem Onboarding, fünf Tabs, Scrollzustand und Dark Mode bleibt eingefroren.
- Die aktuelle Fachphase umfasst ausschließlich USB-CCID, ISO7816/APDU, Fahrerkarte, Kartengeneration und Parser-Basis. Reise-, Spesen-, Zuschlags-, Compliance-, PDF-, VIN- und Cloud-Fachlogik folgen in getrennten Aufträgen.
- Keine personenbezogenen Kartendaten, vollständigen APDUs oder Rohdumps in Logs oder Test-Fixtures.
