# FahrerTagebuch – aktuelle verbindliche Produktregeln

**Stand: 12.10.2026, nach Tobis Entscheidung um 20:53 Europe/Berlin.**

Zuerst [PRODUCT_DECISION_20261012.md](PRODUCT_DECISION_20261012.md) lesen. Sie ersetzt vollständig die bisherigen Handoff-Punkte 8 und9 sowie widersprechende Design-/GPS-/PDF-/Navigationsvorgaben. Dieses Dokument beschreibt aktuelle Regeln; [WORK_STATUS.md](../WORK_STATUS.md) beschreibt die Implementierung. Das [aktualisierte Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) ist ergänzender Kontext, kein Auftrag zur sofortigen Umsetzung aller Ziele.

## Produktumfang

FahrerTagebuch bleibt zunächst eine lokale Single-Driver-App auf Basis des bestehenden Fahrerkarte-Imports. Eigene Kartenquellen, Tätigkeiten, Tageschronik, GNSS-Belege, Importhistorie und belegte Fahrzeugnutzungen bleiben erhalten und dürfen neutral ausgewertet werden.

**Vollständig entfallen:** Spesen/Reisekosten, Reise-/Abwesenheitsberechnung, Heimkehr/Übernachtung, Heimatstandort/Heimradius samt Einrichtung/Standortlogik, Lohn/Gehalt, Arbeitgeberzuschläge, bezahlte oder lohnrelevante Arbeitszeit und sonstige Gehalts-/Abrechnungslogik. Diese Ziele sind nicht zurückgestellt.

PDF-Abrechnungs-/Lohnexport entfällt vollständig. Neutraler Tätigkeits-/Fahrtenbericht ist aktuell nicht verbindlich geplant. Vorbereitete Report-/Spesen-/Lohnschnittstellen und alte Mockups begründen keinen Umsetzungsauftrag.

## Fahrerprofil, Fahrerkarte und Herkunft

- Erste eigene Karte ausdrücklich bestätigen. Dauerhafte Fahreridentität ist das lokale Profil, nicht die Kartennummer.
- DriverProfile → DriverCard(s) → ImportSession(s), ergänzt durch den bestätigten PR65-Gruppen-/Anwendungsbeleg.
- Belastbar gleiche Inhaberidentität erlaubt Folge-/Ersatzkarten; unklare Fälle brauchen Bestätigung. Erkennbar fremde Karten nicht dauerhaft mit dem eigenen Profil/Datenbestand vermischen.
- Alte Karten, Imports und getrenntes Altarchiv unverändert erhalten. Keine vermutete Zuordnung, Nachbefüllung oder Ergänzung historischer Imports aus jüngeren Sitzungen.
- Gen1/Gen2 derselben physischen Karte bleiben eigenständige Anwendungsquellen. Gemeinsame MF-Belege einmal am Parent; Zeiten nicht addieren.
- Sensible Nummern maskieren, bei Auswahlwechsel erneut verbergen. Keine sensiblen Rohdumps oder vollständigen APDUs in Logs/Test-Fixtures.
- Room/SQLCipher und Keystore-Schutz erhalten; kein Klartext-Fallback und keine destruktive Migration.

## Regulärer Import

Kompatiblen Leser und Karte im Vordergrund erkennen, unterstützte zulässige eigene Daten automatisch lesen und atomar speichern. Android-USB-Freigabe sowie erste/unklare Kartenbestätigung bleiben erforderlich. Technische Test-/Speichern-Knöpfe sind keine Zielbedienung des regulären Imports.

Fortschritt/Ergebnis verständlich anzeigen. Fehlende, fehlerhafte, nicht versuchte und gültig leere Quellen unterscheiden. Keine Vollständigkeit bei unvollständigen Daten behaupten. Exakte Sitzungs-/Anwendungswahl auch bei identischen Bytes. Den funktionierenden Import nicht neu aufbauen.

Signaturaufnahme, Vertrauenskette, Originalquellenexport und Standarddownload separat prüfen. Ohne erforderliche Originalsignaturen und Standardvorgänge keinen vollständigen DDD-/Standarddownload behaupten. Automatischer Import bleibt lesend; Card_Download-Schreiben erst in einem separat belegten Downloadpfad.

## Tätigkeiten und neutrale Auswertung

Fahren/Lenken, andere Arbeit, Bereitschaft und Pause/Ruhe behalten ihre Quellbedeutung. Anzeige, Dauern, zeitliche Zusammenfassungen und neutrale Auswertung bleiben erlaubt. Tätigkeitsstatistiken sind keine bezahlten, lohnrelevanten oder abrechnungsrelevanten Stundenzahlen.

Tageszeitleiste mit Farbe und Symbol je Tätigkeit, Übersicht, Zoom, Abschnittsauswahl und exakten Zeitangaben bleibt fachliche Grundlage. Konkrete spätere Oberfläche neu konzipieren. Fehlende Zeit, unbekannte Zeiten und offene Fahrzeugenden nicht ergänzen; Kartenentnahme allein beweist keine Ruhe.

Ereignisse, Störungen, Sonderbedingungen, Grenz-/Ländereinträge und weitere Marker nur aus belegten Quellen. Technische Ereignisse nicht automatisch als Verstoß/Verschulden darstellen. Bestehende Karten-/Kontrolldetails bleiben nutzbar; künftige Navigationsplätze sind offen.

## GNSS und Standort

GNSS aus der Fahrerkarte bleibt erhalten. Spätere Karte: echte Punkte mit Zeit, Position und Punktart plus grober chronologischer **approximierter Verlauf**. Echte Punkte und Verbindungslinien unterscheidbar; Linien niemals als tatsächlich gefahrene Straßenroute ausgeben.

Keine erfundenen Messpunkte oder gefahrenen Kilometer aus Verbindungslinien. Lücken/Unbekanntwerte sichtbar lassen, exakte ausgewählte Quellen verwenden. Keine Smartphone-Routenaufzeichnung oder Hintergrundortung.

Heimatstandort, Heimatpunktwahl, Heimradius und bisherige Smartphone-Standortausnahme für Zuhause entfallen als Produktanforderung. HomePointPicker/Berechtigungen bleiben technischer Altbestand bis zur späteren UI-/Einrichtungsumsetzung; jetzt kein Codeumbau. Smartphone-Standort ist keine Voraussetzung für gelesene Karten-GNSS-Punkte.

## Fahrzeuge

Belegte Identität, Kennzeichen, Staat, VIN, Nutzungszeiten und Tacho-Quellwerte bleiben nutzbar. Marke, Modell, Antrieb und Baureihe nur bei verlässlicher Zuordnung. Kein geratenes Modell oder erfundene Kilometer.

Weiterhin gelten die Daten-/Assetregeln aus [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md): echtes rechtlich nutzbares Modellbild bei sicherem Modell, Herstellerlogo bei sicherem Hersteller, sonst neutrales Symbol; keine KI-Modellbilder oder Laufzeit-Hotlinks. Frühere Layouts und Plätze in Kalender/Auswertung sind Referenzen, keine feste zukünftige Navigation.

## Oberfläche, Navigation, Einrichtung, Onboarding

Alle vier Bereiche später grundsätzlich auf dem schlankeren Konzept neu konzipieren. Hauptbereich Abrechnung gehört nicht mehr zum Zielprodukt: entfernen oder ersetzen. Ersatz, Tabanzahl, Labels, Reihenfolge und Schrittzahl sind noch offen.

Keine künftigen Zuhause-/Spesen-/Gehalts-/Zuschlagsfelder. Vorhandenes Designsystem und bestätigte Importfunktionen bleiben Grundlagen; fünf Tabs und sieben/acht Schritte sind keine verbindlichen Ziele. Jetzt keine großflächige UI-Neuentwicklung. Profile und Importdaten bei späterer Umsetzung erhalten.

## Fortsetzung und Prüfgrenzen

Priorität: vollständiger zuverlässiger Kartenimport, Dateiabdeckung, echte Signaturen, Vertrauensverfahren und begrenzter Quellenexport anhand verifizierter Primärformate. Danach neutrale Auswertung und spätere UI-/GNSS-/Fahrzeugkonzeption. Keine spätere Spesen-/Lohn-/PDF-Abrechnungsphase.

Bisherige separate Komfort-/Backup-/Cloud-/Sync-Planungen sowie optionale Google-Anmeldung, Sprache und Heimatland werden durch diese Entscheidung nicht gestrichen. Heimatland als Profil-/Sprachangabe ist kein Heimatstandort. Diese späteren Punkte bleiben außerhalb des aktuellen Auftrags; lokale Kernnutzung nicht unnötig blockieren.

CI-, Merge-/Hardware-Regeln, kein Browser/Unteragenten und Erhalt vorhandener Git-/Datenstände bleiben bestehen. Budget-/Stop-usage-Einstellungen nicht verändern. 40122/Schema 19 bestätigt; Folgeänderungen brauchen eigene passende Prüfungen. Die bestehende CI-Boundary verlangt aktuell noch Home-Picker-Berechtigungen für den unveränderten Code; daraus keine neue Produktvorgabe ableiten.

[Historischer Regelstand und damalige Checkpoints](history/CURRENT_RULES_BEFORE_PRODUCT_DECISION_20261012.md) bleiben erhalten, sind kein aktueller Auftrag.
