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
