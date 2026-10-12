> **Produktentscheidung 12.10.2026 – überholte Produkt-/UI-/PDF-Planung.** Historische Referenz, kein aktueller Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung entfallen vollständig; neutraler Bericht nicht verbindlich geplant. Alte Tabanzahl, Navigationsplätze und sieben/acht Schritte sind keine Ziele. Ganze Oberfläche und Einrichtung später neu konzipieren, jetzt keine UI-Umsetzung aus diesem Paket. Historische Tests/Beobachtungen nicht umgeschrieben. Verbindlich ist die [neue Produktentscheidung](../../PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

> **Historische Onboarding-Spezifikation; App-GPS für Routen und acht Schritte sind überholt.** Maßgeblich sind [aktuelle Produktregeln](../../CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3); den heutigen Implementierungsstand beschreibt [WORK_STATUS](../../../WORK_STATUS.md). Bei Widersprüchen haben die aktuellen Quellen Vorrang. Historische Aussagen unten bleiben als Zeitdokument erhalten.

# FahrerTagebuch – Onboarding-Spezifikation 0.4.1

Diese Datei war innerhalb des damaligen Designpakets maßgeblich. Die PNG-Datei unter `05_Onboarding/Referenzen/` dient nur als visuelle Referenz. Innerhalb des historischen Pakets hatte die Markdown-Datei Vorrang vor dem Bild; heute gelten die oben verlinkten aktuellen Quellen.

## Globale Gestaltung
- Markenname: **FahrerTagebuch**
- Claim: **Touren. Zeiten. Übersicht.**
- Branding nach Konzept C.
- Farben: Navy `#0F2D4A`, Petrol `#008B8B`, Hellgrau `#E5E7EB`, Weiß `#FFFFFF`.
- Schriftwirkung: Inter bzw. äquivalente sauber lesbare Sans-Serif entsprechend Brand-Guideline.
- Ruhig, professionell, klar, viel Weißraum, konsistente Karten und Abstände.
- Kopfbereich und Fortschrittsanzeige auf allen acht Schritten einheitlich.
- Navigation unten: `Zurück` / `Weiter`, im letzten Schritt `FahrerTagebuch starten`.

## Schritt 1 von 8 – Willkommen / Hauptprofil
- Nur **Benutzername** manuell erfassen.
- **Kein** manuelles Feld für Vorname oder Nachname.
- Vorname/Nachname werden später aus der Fahrerkarte übernommen, sobald diese eingelesen wurde.
- Kurzer Hinweis hierzu ist erlaubt.

## Schritt 2 von 8 – App-GPS
- Auswahl `GPS verwenden` oder `Ohne App-GPS fortfahren`.
- Fachlicher Hinweis: GPS ergänzt Routen/Ortsbezug; Arbeits- und Lenkzeiten stammen nicht aus dem App-GPS.
- Android-Berechtigungsdialog erst nach ausdrücklicher Auswahl von GPS anfordern.

## Schritt 3 von 8 – Datenschutz & Zuhause
- Lokale Verarbeitung klar kommunizieren.
- Zuhause über aktuelle Position oder Adresse festlegen.
- **Heimradius ist fest 500 Meter und nicht vom Nutzer veränderbar.**
- Mockup-Texte, die einen frei anpassbaren Radius suggerieren, sind zu ignorieren.

## Schritt 4 von 8 – Spesen
Felder exakt:
1. `Spesen über 8 Stunden`
2. `Spesen über 24 Stunden`
3. `Übernachtungspauschale`
4. Schalter `Doppelte Spesen verwenden`

Wichtig:
- Doppelte Spesen sind eine freiwillige Leistung des Arbeitgebers.
- **Keinen** Erklärungstext verwenden, der doppelte Spesen an mehrere Länder, Grenzübertritte oder ähnliche Bedingungen knüpft.
- Konkrete Beträge sind Profilwerte, keine fest codierten gesetzlichen Standardwerte.

## Schritt 5 von 8 – Arbeitszeit & Zuschläge
- Schalter `Bereitschaft bezahlt` und `Pause bezahlt`, standardmäßig aus.
- Stundenlohn/Basiswert konfigurierbar.
- Zuschläge für Nacht, Samstag, Sonntag, Feiertag.
- Nachtzuschläge unterstützen **mehrere frei konfigurierbare Zeitfenster**, z. B. 00:00–02:00 = 40 %, 02:00–06:00 = 25 %.
- Keine Überschneidung innerhalb der Nacht-Zeitfenster zulassen.
- Unterschiedliche Zuschlagsarten dürfen gleichzeitig gelten und sich addieren.
- Zuschläge gelten fachlich nur auf Lenkzeit + sonstige Arbeit, nicht auf Bereitschaft/Pause/Ruhe.

## Schritt 6 von 8 – Fahrerkarte
- `Jetzt Fahrerkarte einlesen` und `Später einrichten`.
- Visuell Fahrerkarte/Kartenleser zeigen.
- Noch keine Fachlogik erfinden, falls USB-CCID/Parser in separater Phase umgesetzt wird.

## Schritt 7 von 8 – Erinnerungen
- Erinnerungen optional.
- Tag 21, 25, 27 nach letztem erfolgreichen Einlesen.
- Standardzeit 18:00 Europe/Berlin, Nutzer kann Zeit ändern.
- Empfehlung für Tag 27 bleibt sichtbar, auch wenn Benachrichtigungen deaktiviert sind.

## Schritt 8 von 8 – Zusammenfassung
- Übersicht über Hauptprofil/Benutzername, GPS, Zuhause, Spesen, Arbeitszeit/Zuschläge, Fahrerkarte, Erinnerungen.
- Einzelne Bereiche können über `Ändern` angesprungen werden.
- Abschlussbutton: `FahrerTagebuch starten`.

## Wichtiger Umsetzungsgrundsatz
Das Onboarding soll das finale visuelle System der App verwenden und nicht wie ein separates Demo-Design wirken. Die visuelle Referenz ist daher an das globale Branding und die tatsächlichen Compose-Komponenten der App anzupassen.
