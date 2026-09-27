> Historische Designreferenz. Bei Widersprüchen gelten die [aktuellen Produktregeln](../CURRENT_RULES.md): kein Smartphone-GPS, keine Karten-/Routenansicht, sieben Einrichtungsschritte.

# Work-Auftrag – UI-Feinschliff & verbindliche Designübernahme 0.4.1

## Ausgangslage
Die technische Basis ist gebaut und auf einem Samsung Galaxy S23 (Android 16, One UI 8.5) erfolgreich gestartet. Die App öffnet stabil und der Grundaufbau ist sauber.

Dieser Auftrag ist ein **UI-/Design-Handoff**. Bestehende funktionierende Basis nicht unnötig umbauen und noch keine Fahrerkarte-/GPS-/Spesen-/Compliance-Fachlogik vorziehen.

## Verbindliche Quellen im Paket
Priorität bei Widersprüchen:
1. `WORK_UI_REFINEMENT_0.4.1.md`
2. `00_WORK_START_HERE.md`
3. Markdown-Spezifikationen unter `04_Spezifikationen/`
4. Gerätetest unter `06_Geraetetest/`
5. PNGs nur als visuelle Referenz

## A. Sofortiger UI-Feinschliff
### A1 – Tabwechsel
Auf Galaxy S23 wirken die Hauptseiten beim Wechsel träge und laufen optisch ineinander.
- Navigation der fünf Haupttabs responsiver machen.
- Lange Crossfades/Fullscreen-Überblendungen entfernen oder deutlich verkürzen.
- Ziel: sofortiger, klarer Wechsel; Richtwert für optionale Animation ca. 100–150 ms.
- Keine sichtbare längere Überlagerung zweier Screens.
- Prüfen, ob zusätzliche Recomposition/Main-Thread-Arbeit die Trägheit verursacht.

### A2 – Bottom Navigation
`Einstellungen` passt aktuell nicht sauber.
- Alle Navigationslabels **immer einzeilig**.
- Kein Umbruch.
- Responsive/Auto-Size-Schrift für alle Labels konsistent.
- Auf üblichen Smartphone-Breiten testen, insbesondere ca. 360 dp und 412 dp.
- Icon/Label-Ausrichtung muss sauber bleiben.

## B. Globale Designrichtung übernehmen
Die App soll schrittweise vollständig das freigegebene FahrerTagebuch-Branding übernehmen:
- Markenname `FahrerTagebuch`
- Claim `Touren. Zeiten. Übersicht.`
- Konzept C als Logo-/Icon-Richtung
- Navy `#0F2D4A`
- Petrol `#008B8B`
- Hellgrau `#E5E7EB`
- Weiß `#FFFFFF`
- ruhige professionelle Material-3-Oberfläche
- konsistente Abstände, Karten, Icons und Typografie
- Kreise immer echtes 1:1-Seitenverhältnis
- keine fehlerhaften generativen Icons als Produktionsassets

Bestehende Referenz-PNGs nicht einfach als Rasterbild in die UI legen. Benötigte Produktionsassets sauber als Vector Drawable/SVG/Adaptive Icon nachbauen.

## C. Einrichtung / Onboarding
Die 8-Schritt-Struktur aus `04_Spezifikationen/ONBOARDING_SPEZIFIKATION.md` gilt verbindlich.

Besonders wichtig:
- Schritt 1: nur `Benutzername`; Vor-/Nachname später aus Fahrerkarte.
- Zuhause: fest 500 m, nicht anpassbar.
- Spesenfelder: `Spesen über 8 Stunden`, `Spesen über 24 Stunden`, `Übernachtungspauschale`.
- `Doppelte Spesen verwenden` ohne Info-Text zu Ländern/Grenzübertritten; freiwillige Arbeitgeberleistung.
- mehrere Nachtzuschlags-Zeitfenster in der Oberfläche vorsehen.

Wenn die Fachlogik hinter einem Screen noch nicht existiert, UI-State/Scaffolding sauber vorbereiten, aber **keine Fachlogik erfinden**.

## D. PDF-/Exportdesign
Die freigegebenen Exportlayouts im Paket sind für die spätere Exportfunktion verbindlich:
1. Monatsübersicht
2. Tagesübersicht
3. Arbeitszeit & Zuschläge
4. Spesen & Abwesenheitstage
5. Kilometer & Fahrzeuge
6. separater Export `Verstöße & Hinweise`

Wichtig:
- Begriff `Abrechnung` in diesen Exporten vermeiden.
- Standardexport ohne Verstöße/Compliance, ohne GPS-km/GPS-Abdeckung und ohne Statusspalten.
- `Arbeitszeit` statt `Bezahlte Zeit` im Export.
- Tageszeiten ausgeschrieben; Wochentagskürzel vor Datum.
- Nachtzuschlags-Zeitfenster separat ausweisen.

Falls die eigentliche Exportfunktion noch nicht Teil dieser Phase ist, **keine Dummy-Fachlogik bauen**. Layout-/Designsystem und Spezifikation im Projekt sauber vorbereiten und die Renderer-Implementierung bis zur zuständigen Fachphase zurückstellen.

## E. Nicht tun
- Keine neue fachliche Fahrerkarte-/Tachographenlogik beginnen.
- Keine GPS-Matching-/Reise-/Spesenberechnungslogik vorziehen.
- Keine Compliance-Regeln erfinden.
- Keine funktionierende technische Basis grundlos ersetzen.
- Keine Mockup-Beispielzahlen als echte Programmlogik verwenden.

## F. Prüfung vor Abschluss
Mindestens prüfen:
- App startet weiterhin auf Android 16.
- fünf Haupttabs funktionieren.
- Tabwechsel fühlt sich klar und direkt an.
- Bottom-Navigation bleibt auf Galaxy-S23-Breite einzeilig, insbesondere `Einstellungen`.
- Theme/Branding konsistent.
- keine Regression bei `test`, `lint`, `assembleDebug`, `assembleInternal`.

Danach STOPP und Änderungen/Testergebnis kurz dokumentieren.
