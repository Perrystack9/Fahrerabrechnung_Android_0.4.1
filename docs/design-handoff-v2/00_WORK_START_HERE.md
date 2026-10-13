> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: ÜBERHOLT.** Frühere Produkt-/UI-/Exportplanung; kein aktueller Umsetzungsauftrag. Aktueller Einstieg: [WORK_START.md](../../WORK_START.md); verbindlich sind [CURRENT_RULES.md](../CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – überholte Produkt-/UI-/PDF-Planung.** Historische Referenz, kein aktueller Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung entfallen vollständig; neutraler Bericht nicht verbindlich geplant. Alte Tabanzahl, Navigationsplätze und sieben/acht Schritte sind keine Ziele. Ganze Oberfläche und Einrichtung später neu konzipieren, jetzt keine UI-Umsetzung aus diesem Paket. Historische Tests/Beobachtungen nicht umgeschrieben. Verbindlich ist die [neue Produktentscheidung](../PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

> **Historisches Design-Handoff; die interne Reihenfolge der Verbindlichkeit gilt nur innerhalb dieses alten Pakets.** Maßgeblich sind [aktuelle Produktregeln](../CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3); den heutigen Implementierungsstand beschreibt [WORK_STATUS](../../WORK_STATUS.md). Bei Widersprüchen haben die aktuellen Quellen Vorrang. Historische Aussagen unten bleiben als Zeitdokument erhalten.

# Historischer Nachtrag – UI-Refinement v2

Für die nächste UI-/Designrunde zuerst **`WORK_UI_REFINEMENT_0.4.1.md`** lesen. Diese Datei enthält das aktuelle Galaxy-S23-Testfeedback sowie die neuesten verbindlichen Onboarding-Korrekturen. Bei Widersprüchen hat sie Vorrang.

---

# FahrerTagebuch – Design-Handoff für Work

**Arbeitsstand:** 0.4.1  
**Zweck:** Verbindliche Übergabe der freigegebenen Designrichtung, Logos, PDF-Exportlayouts und Darstellungsregeln an ChatGPT Work.

## WICHTIG: Reihenfolge der Verbindlichkeit

Bei Widersprüchen gilt immer diese Priorität:

1. **Diese Markdown-Dateien** sind fachlich und sprachlich verbindlich.
2. Danach gilt die Datei `04_Spezifikationen/BRAND_GUIDELINE.md`.
3. Danach gilt `04_Spezifikationen/PDF_EXPORT_SPEZIFIKATION.md`.
4. Die PNG-Dateien dienen **nur als visuelle Referenz für Stil, Proportionen und Layout**.

Die Beispiel-PNGs wurden als Design-Mockups erzeugt. Einzelne darin sichtbare Beispieltexte, Zahlen, Farben oder Begriffe dürfen daher **nicht ungeprüft als fachliche Vorgabe übernommen werden**. Die Markdown-Spezifikation überschreibt die Mockups.

---

# 1. Auftrag an Work

Wenn die Designphase technisch umgesetzt wird, soll Work:

- die visuelle Richtung exakt anhand dieser Unterlagen übernehmen,
- Logos und Symbole nach Möglichkeit **sauber als Vektor/SVG bzw. Android-Vector-Drawable** nachbauen,
- die bereitgestellten PNG-Crops nicht als endgültige Master-Dateien behandeln,
- alle Layouts responsiv und ohne Verzerrung umsetzen,
- die vorhandene technische Architektur des Projekts respektieren,
- **keine neue Fachlogik erfinden**, nur weil in einem Mockup Beispielwerte sichtbar sind,
- die hier definierten Begriffe exakt verwenden,
- die PDF-Ausgaben als eigenständige Dokumentlayouts und nicht als Screenshots der App umsetzen.

## Noch nicht automatisch ausführen

Dieses Paket ist eine **Design- und Übergabespezifikation**. Es ist kein Auftrag, während einer laufenden technischen Basis-/CI-Phase ungefragt umfangreiche UI- oder Exportlogik einzubauen. Die Umsetzung erfolgt erst, wenn der entsprechende Work-Auftrag ausdrücklich gestartet wird.

---

# 2. Markenname und Sprache

**Markenname:** `FahrerTagebuch`  
**Claim:** `Touren. Zeiten. Übersicht.`

Im Monats-/Tagesexport den Begriff **„Abrechnung“ vollständig vermeiden**. Die App stellt Übersichten, Auswertungen und Nachweise bereit; sie behauptet nicht, selbst eine Lohn- oder Gehaltsabrechnung zu erstellen.

Bevorzugte Begriffe:

- Monatsübersicht
- Tagesübersicht
- Arbeitszeit & Zuschläge
- Spesen & Abwesenheitstage
- Kilometer & Fahrzeuge
- Übersicht
- Auswertung
- Nachweis

Nicht als Exporttitel verwenden:

- Monatsabrechnung
- Abrechnungsbericht
- Lohnabrechnung

---

# 3. Zwingende Designregeln

- Primärfarben: Navy, Petrol, Hellgrau, Weiß.
- Primärschrift: **Inter**; Fontdateien sind nicht Bestandteil dieses Pakets und dürfen nicht aus dem Paket erwartet werden.
- Ruhiges, professionelles, sachliches Erscheinungsbild.
- Viel Weißraum, klare Tabellen, geringe Schattierung.
- Keine verspielten 3D- oder Glanzeffekte.
- Kreisförmige Elemente müssen **immer echtes 1:1-Seitenverhältnis** haben; niemals ovale Kreise.
- Icons müssen sauber und fehlerfrei sein. Keine generativ fehlerhaften Symbole in der finalen App/PDF.
- Logo oder App-Icon niemals strecken oder stauchen.

---

# 4. PDF-Monatsübersicht – verbindlicher Aufbau

Der Standardexport besteht aus fünf Seiten:

1. `Monatsübersicht`
2. `Tagesübersicht`
3. `Arbeitszeit & Zuschläge`
4. `Spesen & Abwesenheitstage`
5. `Kilometer & Fahrzeuge`

Zusätzlich existiert ein **separater** Export:

- `Verstöße & Hinweise`

Verstöße oder Compliance-Hinweise gehören **nicht** in den normalen Monatsübersicht-Export.

---

# 5. Wichtige fachliche Darstellungsentscheidungen für den Export

## Aus dem Monats-/Tagesexport entfernen

- Datenstand
- GPS-Kilometer
- GPS-Abdeckung
- festgestellte Verstöße
- offene Prüfungen / „keine offenen Prüfungen“
- Status-Spalten

## Begriffe

- Im Export `Arbeitszeit` statt `Bezahlte Zeit` verwenden.
- Keine Abkürzung `ÜN`; stattdessen `Abwesenheitstage`.
- Tagesübersicht: vor dem Datum eine separate Wochentagsspalte mit `Mo`, `Di`, `Mi`, `Do`, `Fr`, `Sa`, `So`.
- In der Tagesübersicht Zeiten gut lesbar darstellen, z. B. `9 Stunden 24 Minuten`, nicht `9:24`.

## Nachtzuschläge

Die App muss mehrere konfigurierbare Nachtzuschlags-Zeitfenster unterstützen und im Export getrennt ausweisen können.

Beispiel:

- 00:00–02:00 → 40 %
- 02:00–06:00 → 25 %

Jedes Fenster wird separat mit Zeitraum, Zeitmenge, Prozentsatz und Betrag dargestellt. Verschiedene Zuschlagsarten dürfen sich weiterhin addieren, z. B. Nacht + Sonntag.

---

# 6. Verstoß-/Hinweisexport

Der Verstoßexport ist bewusst separat vom Monatsübersicht-Export zu behandeln.

Visuelle Richtung:

- gleiche Marken-DNA wie die übrigen PDFs,
- Rot für erkannte Verstöße,
- Orange für Hinweise/Prüfbedarf,
- Grau/Blau für Informationen,
- sachliche Tabellen,
- echte Kreise statt ovaler Formen,
- fehlerfreie, konsistente Icons.

Der Bericht bleibt informativ und darf nicht als automatische rechtliche Prüfung formuliert werden.

---

# 7. Produktionshinweis zu den Logo-Dateien

Die Dateien unter `01_Branding/Crops_Referenz/` sind **aus dem freigegebenen Markenboard ausgeschnittene Referenzen**. Sie dienen Work dazu, Form, Proportionen und Bildsprache eindeutig zu erkennen.

Für die App soll Work daraus nach Möglichkeit technisch saubere Master-Assets erstellen:

- SVG oder Android Vector Drawable für Logos/Symbole, soweit geeignet,
- Adaptive Icon für Android mit getrenntem Foreground/Background,
- monochrome Notification-Icon-Variante,
- keine Raster-Skalierung, wenn eine Vektorversion sinnvoll ist.

---

# 8. Abschlusskriterium der späteren Designumsetzung

Work soll vor Abschluss prüfen:

- Markenname und Claim korrekt,
- Farbwerte korrekt,
- keine Verwendung von „Abrechnung“ in den hier beschriebenen Exporten,
- keine GPS-Kilometer oder GPS-Abdeckung im Standardexport,
- keine Verstöße im Standardexport,
- Wochentage in der Tagesübersicht vorhanden,
- `Arbeitszeit` statt `Bezahlte Zeit`,
- Nachtzuschläge nach Zeitfenstern getrennt,
- Kreisformen 1:1,
- keine verzerrten oder fehlerhaften Icons,
- PDF-Layout in A4 Hochformat sauber druckbar,
- keine Beispielwerte aus den Mockups als echte Programmlogik fest einbauen.
