> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../../WORK_START.md); verbindlich sind [CURRENT_RULES.md](../CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historischer Berater-Handoff.** Originalfassung von Issue #3 vor dieser Entscheidung. Gesamte Punkte 8/9 aufgehoben; frühere Heimat-/PDF-/Navigations- und pauschale GNSS-Verbindungsverbote überholt, kein Auftrag. Text als Entscheidungshistorie erhalten. Verbindlich ist die [neue Produktentscheidung](../PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Berater-Handoff – FahrerTagebuch Produktentscheidungen (28.09.2026)

Dieses Issue dient **nur als verbindliche Produkt-/Fachkontext-Übergabe an Work**. Bitte nicht automatisch alles sofort implementieren. Bei der Umsetzung gilt weiterhin: aktuelle Produktregeln vor älteren historischen Design-/GPS-Dokumenten.

## Rollen
- ChatGPT in diesem Chat = Produkt-/Fachberater.
- Work = ausschließlicher Programmierer/Implementierer.

## 1. Tageszeitleiste – verbindliche Designrichtung
Die Tageszeitleiste soll eines der Kernmerkmale der App werden.

**Grundprinzip**
- 24h-Übersicht als kompakter horizontaler Zeitstrahl.
- Darunter zoombare Detailansicht eines Zeitbereichs.
- Pinch-to-Zoom bis zur sinnvollen Minutenebene.
- Horizontal verschiebbar.
- Antippen eines Abschnitts zeigt exakte Start-/Endzeit und Dauer.
- Verschiebbare Zeitmarke/Fadenkreuz mit exaktem Zeitpunkt.

**Tätigkeiten**
Feste visuelle Identität je Tätigkeitsart, immer Farbe **und** Symbol:
- Lenken
- Andere Arbeit
- Bereitschaft
- Pause/Ruhe

Farben dürfen nicht die einzige Information sein.

**Zusatzmarker auf/bei der Timeline**
- Fahrzeugwechsel
- GNSS-Punkt
- Grenzübertritt
- Ereignis/Störung
- Fähre/Zug
- Out of Scope
- Karte eingelegt/entnommen, sofern fachlich sinnvoll darstellbar

**Fahrzeugbezug**
- Oberhalb/bei der Timeline soll sichtbar sein, welches Fahrzeug in welchem Abschnitt verwendet wurde.
- Fahrzeugwechsel direkt in der Tageschronik markieren.
- Tipp auf Fahrzeug/Kennzeichen kann in Fahrzeughistorie führen.

**Ausgewählter Abschnitt**
Beispielinhalt:
- Tätigkeit
- Start/Ende
- Dauer
- Fahrzeug
- ggf. Einzel-/Mehrfahrerstatus, sofern aus Daten belastbar ableitbar

**Tageszusammenfassung**
Kompakte Kennzahlen, u. a.:
- Arbeitszeit
- Lenkzeit
- Andere Arbeit
- Bereitschaft
- Pause/Ruhe
- Nachtarbeit
- Kilometer
- Fahrzeuge
- ggf. Grenzübertritte

## 2. GNSS-Detailansicht
GNSS-Marker in der Tageszeitleiste sind antippbar.

Bei Tipp:
- kleines Bottom Sheet / Overlay
- Zeitpunkt
- Position
- Punktart
- kleine Karte mit Marker
- V1 normale Kartenansicht
- später optional Satellitenansicht
- optional „In Karten-App öffnen“

Wichtig:
- Nur echte gespeicherte GNSS-Punkte zeigen.
- **Keine künstliche Route zwischen Punkten erzeugen.**
- Tachograph-/Fahrerkarten-GNSS ist Punktinformation, keine erfundene kontinuierliche Strecke.

## 3. Ereignisse/Störungen und Kontrollen
**Ereignisse & Störungen**
- Eigene Übersicht.
- Zusätzlich Marker an der richtigen Position in der Tageszeitleiste.
- Technisches Ereignis niemals automatisch als „Verstoß“ oder Fahrerverschulden darstellen.
- Menschenlesbare Bezeichnung statt kryptischer Codes, soweit belastbar.

**Letzte Kontrolle**
- Fahrerkarte → Details → Kontrollen.
- Nicht prominent auf der Startseite.
- Datum, Art und verfügbare Kontrolldaten anzeigen.

## 4. Fahrerkarte – Detailansicht
Optional, nicht als Hauptnavigation:
- Personal/Inhaber
- Führerschein
- Fahrerkarte
- Gültigkeit
- Aktivität
- Kontrollen
- technische/importbezogene Informationen

Sensible Nummern standardmäßig maskieren, bei Bedarf einblendbar.

## 5. Kartenablauf-Erinnerung
- Standardmäßig **aktiv**, in Einstellungen deaktivierbar.
- Funktioniert ohne eingesteckte Karte, da Gültigkeitsdatum lokal gespeichert wird.
- bisherige Zielrichtung: 90 Tage in-app, 30 Tage Benachrichtigung, 7 Tage stärkerer Hinweis; Feinschliff später.

## 6. Import / Diagnose / Historie
Geplante Produktlogik:
- kompatiblen USB-C/CCID-Leser erkennen und Import anbieten
- optional später Auto-Öffnen/Auto-Import
- Importhistorie mit Datum, Kartengeneration, Zeitraum/Status, Anzahl neuer Daten
- Kartenleser-Diagnose: Leser erkannt, Hersteller, CCID, Slot, ATR, Generation, letzter erfolgreicher Import
- keine sensiblen Kartendaten / vollständigen APDUs in Logs
- Importerinnerungen anhand **letztem erfolgreichen Import**, nicht fiktivem Datum
- Startseite später: letzter Import + nächster empfohlener Import

## 7. Fahrzeughistorie
Zentrales Kernfeature, nicht nur Zusatz:
- Kennzeichen
- Zulassungsland
- VIN, wenn vorhanden
- erste/letzte Nutzung
- Arbeitstage
- Lenk-/Arbeitszeit mit diesem Fahrzeug
- Kilometer, soweit zuverlässig aus Quelldaten ableitbar
- zugehörige Einsätze/Tage
- gleiche Fahrzeuge über mehrere Kartenimporte zusammenführen

## 8. Spesen / Reise / Arbeitgeberabrechnung – Produktgrenze
**Keine Steuerberater-App.**
FahrerTagebuch soll Fahrer-/Arbeitgeberabrechnung nachvollziehbar machen.
Steuerliche Einordnung, Steuerfreiheit, Mahlzeitenkürzungen, spezielle 9-€-Berufskraftfahrerpauschale etc. sind **nicht Bestandteil der Fachlogik**.

### Spesenbasis
Beibehalten:
- eintägige Reise > 8h → kleine Pauschale
- exakt 8h oder weniger → keine Pauschale
- mehrtägig: Anreisetag kleine Pauschale
- voller Abwesenheitstag große Pauschale
- Abreisetag kleine Pauschale
- zwei Reisen an einem Tag ohne Doppelzählung zusammenführen
- doppelte Spesen verdoppeln nur Spesenposition, **nicht** Übernachtungspauschale
- unklare Heimkehr → abhängige Spesenposition bleibt UNKLAR

### Über-Nacht-Fahrt ohne Übernachtung
Als Testfall ergänzen:
- Reise über Mitternacht ohne echte mehrtägige Übernachtung entsprechend bisheriger gewünschter Arbeitgeber-/Spesenlogik behandeln; nicht aus altem GPS-Konzept ableiten.
- genaue Testfallformulierung vor Implementierung mit aktuellem Fachstand abgleichen.

### Übernachtungserkennung
Für FahrerTagebuch gilt pragmatisch:
- laufende Reise
- Fahrer ist auswärts
- zusammenhängende qualifizierende Pause/Ruhe
- **ab 9h**, technische Toleranz: **8:59h**
→ Übernachtung erkannt.

Produktannahme:
**LKW-Fahrer ist auswärts = Übernachtung im LKW.**
Keine Unterscheidung Hotel/Pension/Bekannte/LKW.

Tagschlaf eines Nachtfahrers zählt genauso.
Mehrtagige auswärtige Wochenruhe kann mehrere Übernachtungen ergeben.
Unklarer Aufenthaltsort → UNKLAR.
Kurze Pausen/Bereitschaft → keine Übernachtung.

### Heimkehr-Erkennung
**Karte ziehen allein = niemals Heimkehr.**
Grund: Fahrzeugwechsel o. ä. möglich.

Heimkehr gilt als erkannt, wenn:
- Heimatstandort belastbar erkannt,
- anschließend mindestens **8:59h zusammenhängende Pause/Ruhe** vorliegt.

Dabei:
- Karte darf eingesteckt bleiben oder gezogen worden sein.
- Wenn Karte gezogen ist, darf fehlende Zeit **nicht einfach als Ruhe erfunden** werden; Ruhe muss durch belastbare Karten-/Tachographendaten bzw. zulässigen Nachtrag belegt sein.
- Die Heimkehrzeit wird rückwirkend auf den **Beginn der qualifizierenden Ruhephase** gesetzt.
- 8:59h ist Produkt-/Toleranzschwelle, keine Aussage über gesetzliche Ruhezeit.

500m Heimradius bleibt als Produktregel bestehen.

## 9. Arbeitszeit & Zuschläge
Beibehalten:
- „Bereitschaft bezahlt“ konfigurierbar
- „Pause bezahlt“ konfigurierbar
- Zuschlagsbasis grundsätzlich Lenken + sonstige Arbeit
- Bereitschaft/Pause bleiben auch bei Bezahlung nicht automatisch zuschlagsfähig
- mehrere Nachtfenster möglich
- Nachtfenster dürfen sich gegenseitig nicht überschneiden
- verschiedene Zuschlagsarten dürfen gleichzeitig gelten
- Prozentwerte addieren sich, nicht multiplizieren
- Rundung wie im Testkatalog auf Monats-/Zuschlagsartenebene, nicht als Tagesrechengrundlage

**Samstag, Sonntag, Feiertag**
- bleiben als konfigurierbare Zuschläge enthalten.
- Nutzer kann pro Zuschlagsart zwischen
  - Prozent
  - fixer Pauschale
  wählen.
- Pauschale soll grundsätzlich einmal pro betroffenem Kalendertag auslösen, sobald mindestens eine zuschlagsfähige Tätigkeit an diesem Kalendertag vorhanden ist.
- Samstag bleibt bewusst erhalten, auch wenn steuerliche SFN-Logik hier nicht relevant ist.

**Feiertage**
- Grundlage ist das im Profil gewählte Bundesland am **Firmen-/Arbeitgeberstandort**.
- Kein Versuch, Feiertage anhand laufender LKW-Positionen/Bundesländer zu wechseln.

## 10. Konfigurationshistorie
Unbedingt beibehalten:
- gültig-ab-Datum
- alte Fassungen nachvollziehbar
- Änderungen nicht still rückwirkend anwenden
- historische Korrektur als neue Revision
- bereits erzeugte PDF-/Ergebnis-Snapshots nicht still überschreiben

## 11. Altes Smartphone-GPS-/Routenkonzept entfernen
Historische Tests/Dokumente enthalten noch:
- App-GPS während Fahrt
- Routen-Matching
- GPS-Kilometer
- GPS-vs-Tacho-Vergleich
- GPS-Rohpuffer/Routenbackup

Diese Logik ist **nicht mehr aktuell**.

Aktuelle Regel:
- Smartphone-Standort nur explizit im Onboarding/Heimatpunkt-Setzen.
- danach kein Smartphone-Tracking / keine Hintergrundroute / keine Smartphone-km.
- Karten/Tachograph-GNSS darf als **diskrete Punkte** angezeigt werden.
- keine künstlichen Verbindungsrouten.

## 12. Spätere / nicht jetzt priorisierte Punkte
Später prüfen/einführen:
- Google-Anmeldung
- serverseitige interne UUID
- Heimatland & Sprache
- Download-Zeitstempel auf Fahrerkarte schreiben
- Auslandsspesen
- Satellitenkarte
- Cloud/Backup/Sync

Google-Anmeldung später bevorzugt optional und sinnvoll gekoppelt an Cloud/Sync/Gerätewechsel; lokale Kernnutzung soll nicht unnötig blockiert werden.

## 13. Entwicklungspriorität
Weiterhin:
1. zuverlässiger vollständiger Fahrerkarte-Import
2. Persistenz / Datenmodell
3. Parser / Auswertung
4. Tageszeitleiste / Tageschronik
5. Fahrzeughistorie / km
6. Ereignisse/Störungen / Kontrollen
7. Importhistorie / Reminder
8. Spesen / Arbeitgeberabrechnung / Zuschläge
9. spätere Komfort-/Cloud-Funktionen

## Hinweis
Der vorhandene Testkatalog 0.4.1 ist eine starke Basis, enthält aber alte Smartphone-GPS-/Routenlogik. Bei Widersprüchen gilt dieser Handoff zusammen mit den aktuellen Produktregeln vor historischen Designreferenzen.
