# FahrerTagebuch – Arbeitsstand

- Basis-HEAD vor dem Hardwaretest-Einstieg: `598ab11f1857fdae84a0e08c20232511f225111c`
- Phase: Fahrerkarte, USB-CCID, ISO7816/APDU und Identitätsdecoder.
- Implementiert: APDU-fähige CCID-Reader-Erkennung und Transport einschließlich begrenzter CCID-Befehls-/Antwortketten und Slot-Auswahl, ATR, APDU-Folgen, Gen1/Gen2/Gen2V2-Erkennung anhand der Anwendungsdatei, Identitätsdatei und synthetische Tests. Minimaler Hardwaretest-Einstieg über Start → Fahrerkarte mit USB-Freigabe und datensparsamer Diagnose. Keine Kartenpersistenz.
- Grenzen: Nur CCID-Geräte mit APDU-Austausch; TPDU-/Character-Level nicht unterstützt. Keine kryptografische Echtheitsprüfung oder vollständiger Kartendownload. Nur die bisherige Fahrerkarte-Platzhalterseite erhielt den technischen Testknopf; übrige UI unverändert.
- Nächster Hardwaretest: USB-CCID-Leser am Galaxy S23 verbinden, USB-Freigabe erteilen, Fahrerkarte einlegen und ATR, Auswahl der Anwendung, EF 0501 und EF 0520 gegen echte Antworten prüfen. Ohne diese Probe wird keine Hardwarekompatibilität behauptet.
- Hardwarebefund 27.09.2026: Alcor Link AK9563 (`2ce3:9563`), Slot 0 und Karte erkannt; erster Power-On endete vor ATR mit `TIMEOUT`. USB-Bulk-Nulltransfers werden jetzt begrenzt weitergelesen und Send-/Empfangsphase angezeigt; erneuter Test mit demselben Leser erforderlich.
- Tests: siehe letzten CI-Lauf zum Commit. Der lokale Runner kann die Gradle-Distribution nicht herunterladen.
