# FahrerTagebuch – Arbeitsstand

- Ausgangs-HEAD: `5c580698dbda5af84f6260ba44d7861f17098ff1`
- Phase: Fahrerkarte, USB-CCID, ISO7816/APDU und Identitätsdecoder.
- Implementiert: APDU-fähige CCID-Reader-Erkennung und Transport, ATR, bounded APDU-Folgen, Gen1/Gen2/Gen2V2-Erkennung anhand der Anwendungsdatei, Identitätsdatei, synthetische Tests. Keine Kartenpersistenz.
- Grenzen: Nur CCID-Geräte mit APDU-Austausch; TPDU/Character-Level, CCID-Chaining und mehrere Slots sind noch nicht unterstützt. Keine kryptografische Echtheitsprüfung oder vollständiger Kartendownload. Die freigegebene UI ist unverändert.
- Nächster Hardwaretest: USB-CCID-Leser am Galaxy S23 verbinden, USB-Freigabe erteilen, Fahrerkarte einlegen und ATR, Auswahl der Anwendung, EF 0501 und EF 0520 gegen echte Antworten prüfen. Ohne diese Probe wird keine Hardwarekompatibilität behauptet.
- Tests: siehe letzten CI-Lauf zum Commit. Der lokale Runner kann die Gradle-Distribution nicht herunterladen.
