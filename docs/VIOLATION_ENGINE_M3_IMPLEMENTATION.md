# M3 – Tägliche Ruhezeiten und Sonderfälle

**Arbeitsstand, noch keine Abnahme.** Ausdrücklicher Auftrag15.10.2026 nach technischer M2-Abnahme. Basis und Integrationsgate: [M2-Abschluss](M2_INTEGRATION_20261015.md). Keine Freigabe für M3-Merge, M4–M7 oder Release. PD01–PD80/A–H/E01–E12/P01–P05 unverändert.

## Technischer Vertrag

1. Derselbe gefrorene M1-Snapshot mit Originalen, beiden Anwendungen, Karten-/Import-/Receiptbeziehungen und dem abschließenden Revisions-CAS bleibt Eingang. Kein neuer Parser und kein Verändern aufgezeichneter Tätigkeiten.
2. R01–R07/F01–F05 liefern getrennte Ruheblöcke, Anker, Ruhezeitfenster, alternative Zuordnungen, Tagesreduzierungen und nachvollziehbare Zahlenbefunde. UTC-Instants und echte geschlossene Intervalle bestimmen Dauern. Ein offener Ruheblock darf eine belegte Mindestdauer erfüllen, besitzt jedoch keinen erfundenen tatsächlichen Endanker.
3. Drei Tagesreduzierungen zählen zwischen qualifizierenden Wochenruhen. M2-Verlängerungen bleiben davon unabhängige Vorgänge je technischer Woche. Ein23-h-Block ist ein möglicher Tagesanker, niemals ein24-h-Wochenanker; hier entstehen keine Wochen-Ausgleichsschulden.
4. Die Ruheprüfung konsumiert vorhandene M1-/M2-Fakten. Veröffentlichung erfolgt gemeinsam mit M2 erst bei unverändertem Eingangstoken. Keine zusätzliche Import-/Uhrzeit-/Anzeigezonenquelle. Keine neuen UI-Dialoge, PDF-Ausgabe oder rechtliche Schweregrade.
5. Nur notwendiger fallbezogener Kontext ergänzt den bestehenden versionierten Kontext: konkrete Fähre/Schlafmöglichkeit/Fahrplan sowie einzelner Gelegenheitsdienst und dessen gesetzliche Vorbehalte. Keine zweite Kontextpersistenz und keine Reiseverwaltung.
6. Unbekannt, widersprüchlich, außerhalb des belegten Geltungsbereichs und fehlende Ausnahmevoraussetzungen bleiben sichtbar eingeschränkt. Mehrere rechtlich mögliche Pfade werden erhalten; ein Ressourcenlimit darf niemals einen beliebigen Gewinner oder ungesicherten Befund erzeugen.

## Abgrenzung

M3 erzeugt zeitliche Wochenruhe-Kandidaten nur soweit für Art.8(3)/(4)-Tagesanker erforderlich. Wochenzuordnung,6×24-h-Fristen, Schuld-/Ausgleichsledger, internationales Vierwochenpaket und Zwölftageregel bleiben M4. Zusätzliche24–45-h-Blöcke werden insbesondere nicht automatisch zu Ausgleichsschulden. Abhängige noch ungeklärte Zuordnungen bleiben bedingt.

M2-Sicherungen L02/L03/L09 werden nicht entfernt. Sieben Stunden Ruhe sind kein erfülltes Art.8-Fenster. Die Kommissionsleitlinie7 zur Folge unzureichender Ruhezeiten ist ein eigener, ausdrücklich benannter Auslegungspfad. Fehlende Daten dürfen weder Ruhe noch Rechtswidrigkeit noch Entwarnung vortäuschen. Artikel12 bleibt ohne automatische Anerkennung oder Schuld.

## Geplante Nachweise dieser Umsetzung

Tatsächlich implementierte Komponenten, Referenzfälle LG40–LG57/LG81–LG88/LG98–LG100/LG109/LG110 und Tagesanteile LG101–LG103 werden mit konkreten Soll-/Ist-Ergebnissen ergänzt. Zusätzlich: Herkunft/Reimport/Konflikte, offene Enden, historische Grenzen, UTC-/DST-Invarianz, getrennte Zähler, Kontextrevision/Reopen/CAS und Abbruch. Alle bestehenden technischen Gates bleiben unverändert. Keine abgeschlossene Prüfung wird aus diesem Arbeitsdokument abgeleitet.
