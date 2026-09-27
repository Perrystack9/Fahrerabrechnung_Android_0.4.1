# FahrerTagebuch – Arbeitsstand

- Letzter freigegebener HEAD vor dieser Fortsetzung: `6653cb09386cd8d6129570ba2cb47542b4d86723`
- Phase: Fahrerkarte, USB-CCID, ISO7816/APDU und Identitätsdecoder.
- Implementiert: APDU-fähige CCID-Reader-Erkennung und Transport einschließlich begrenzter CCID-Befehls-/Antwortketten und Slot-Auswahl, ATR, APDU-Folgen, Gen1/Gen2/Gen2V2-Erkennung anhand der Anwendungsdatei, Identitätsdatei, datensparsame Hardwarediagnose und synthetische Tests. Keine Kartenpersistenz.
- Grenzen: Nur CCID-Geräte mit APDU-Austausch; TPDU-/Character-Level nicht unterstützt. Keine kryptografische Echtheitsprüfung oder vollständiger Kartendownload. Die freigegebene UI ist unverändert.
- Nächster Hardwaretest: USB-CCID-Leser am Galaxy S23 verbinden, USB-Freigabe erteilen, Fahrerkarte einlegen und ATR, Auswahl der Anwendung, EF 0501 und EF 0520 gegen echte Antworten prüfen. Ohne diese Probe wird keine Hardwarekompatibilität behauptet.
- Tests: siehe letzten CI-Lauf zum Commit. Der lokale Runner kann die Gradle-Distribution nicht herunterladen.
