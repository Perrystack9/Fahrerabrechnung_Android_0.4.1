> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../../../WORK_START.md); verbindlich sind [CURRENT_RULES.md](../../CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – überholte Produkt-/UI-/PDF-Planung.** Historische Referenz, kein aktueller Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung entfallen vollständig; neutraler Bericht nicht verbindlich geplant. Alte Tabanzahl, Navigationsplätze und sieben/acht Schritte sind keine Ziele. Ganze Oberfläche und Einrichtung später neu konzipieren, jetzt keine UI-Umsetzung aus diesem Paket. Historische Tests/Beobachtungen nicht umgeschrieben. Verbindlich ist die [neue Produktentscheidung](../../PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

> **Historisches Galaxy-S23-Feedback zu einem früheren UI-Stand.** Maßgeblich sind [aktuelle Produktregeln](../../CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3); den heutigen Implementierungsstand beschreibt [WORK_STATUS](../../../WORK_STATUS.md). Bei Widersprüchen haben die aktuellen Quellen Vorrang. Historische Aussagen unten bleiben als Zeitdokument erhalten.

# Gerätetest – UI-Feedback 0.4.1

**Testgerät:** Samsung Galaxy S23  
**Android:** 16  
**One UI:** 8.5

## Positiv
- App startet ohne Probleme.
- Grundaufbau und Darstellung sind sauber.

## 1. Wechsel zwischen Hauptseiten wirkt zu träge
Beobachtung: Beim Wechsel über die untere Navigation wirken die Seiten so, als würden sie optisch ineinanderlaufen.

### Ziel
- Top-Level-Navigation soll unmittelbar, klar und responsiv wirken.
- Kein langes Crossfade und keine sichtbare Überlagerung zweier kompletter Seiten.
- Bestehenden Screen-State nach Möglichkeit erhalten.

### Technische Leitlinie
- Vorhandene `Crossfade`-/`AnimatedContent`-/NavHost-Transitions prüfen.
- Für die fünf Haupttabs entweder keine Fullscreen-Transition oder eine sehr kurze, konsistente Transition verwenden.
- Richtwert: ca. 100–150 ms statt langer Überblendung.
- Enter- und Exit-Animation dürfen kein länger sichtbares Doppelbild erzeugen.
- Falls die Trägheit nicht nur Animation ist: Main-Thread-Arbeit/Recomposition beim Tabwechsel prüfen und unnötige Arbeit aus dem Navigationswechsel entfernen.
- Nach Änderung erneut auf Galaxy S23 testen.

## 2. Untere Navigation: `Einstellungen`
Beobachtung: Der Text `Einstellungen` passt nicht sauber in den verfügbaren Bereich.

### Ziel
- **Alle fünf Labels immer einzeilig.**
- Kein Zeilenumbruch.
- Kein Abschneiden auf üblichen Smartphone-Breiten.
- Schriftgröße responsiv an den verfügbaren Platz anpassen, ohne Inkonsistenzen zwischen den Tabs.

### Technische Leitlinie
- `maxLines = 1`, `softWrap = false`.
- Responsive/Auto-Size-Typografie für Labels verwenden.
- Einheitliche Min-/Max-Schriftgröße definieren; nicht nur `Einstellungen` separat kleiner machen.
- Auf mindestens ca. 360 dp und 412 dp Breite testen.
- Icons und Labels müssen weiterhin sauber zentriert sein.
