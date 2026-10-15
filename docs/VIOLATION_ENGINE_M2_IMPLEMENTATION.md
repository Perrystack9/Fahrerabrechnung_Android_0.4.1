# M2 – Lenkzeiten und Fahrtunterbrechungen

Stand15.10.2026. Ausdrücklich freigegebener M2-Entwicklungsstand **40130/Room23**, eigene Implementierung auf Basis des kontrolliert integrierten M1 `3c99b7db397f307ccbe91b33fa9862e2b410a144`. **Kein M2-Merge, keine M2-Geräteabnahme oder Releasefreigabe.** Aktueller vollständiger technischer Nachweis wird commitgebunden im M2-PR-Protokoll festgehalten. [M0/M1-Integration](M0_M1_INTEGRATION_20261015.md), [Rechtsnachweise/offene Gates](VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md), [Abnahmematrix](VIOLATION_ENGINE_M2_ACCEPTANCE.md).

## Tatsächlich implementierte Komponenten

| Komponente | Vertrag |
|---|---|
| `domain/…/violation/driving/DrivingCalculator.kt` | Reiner M1-Snapshot-Konsument: Tageslenkabschnitte, normaler9-/10-h-Grenzwert, wöchentliche Verlängerungsvorgänge,56-/90-h-UTC-Projektionen und270-/45-/15+30-Pausen einschließlich datierter Bus-/Crewvarianten. Kein Kartenbyteparser und keine neue Quellenarchitektur. |
| `DrivingModel.kt` | Unveränderliche Zahlenprojektionen mit Rechts-/Versions-/Quellenkennung, tatsächlich gezählten halboffenen Fahrteilstücken und sämtlichen M1-Anfangs-/Endbelegen. Fachliche Einschränkungen, untere Mengen-/späteste Zeitgrenze und offener Rechtsstatus explizit; kein erfundener Schweregrad oder Artikel12-Schuld. |
| `data/…/violation/DrivingCheckRepository.kt` | Abonniert den M1-Cold-Flow, rechnet auf Default-Dispatcher, meldet Preparing und veröffentlicht nur die noch aktuelle Quellen-/Kontextrevision. Cancellation propagiert; Fehler werden datensparsam als fehlgeschlagene Berechnung geführt, niemals als Entwarnung. |
| `DriverCardInputAdapter.publishIfCurrent(snapshot)` | Kleine lesende Erweiterung zur Wiederverwendung desselben bestehenden manifest-/profil-/kontextgebundenen M1-Tokens. Die Kodierung des bisherigen Tokens ist unverändert. Letzte Aktualitätsprüfung und Veröffentlichung innerhalb derselben Room-Transaktion; kein alter Befund nach neuerem Commit. |
| `MainViewModel` | Abonniert M2 während der App-Nutzung über einen eager StateFlow. Erfolgreicher eigener Import, Replay oder relevanter Kontextcommit löst über die vorhandene M1-Invalidierung automatisch eine neue Berechnung aus. Keine neue Hintergrundortung oder versteckter Live-Countdown. |

**Persistenz unverändert:** keine neue Tabelle, Migration, abgeleiteter Cache oder Ergebnis-DB. Room23 und alle Exporte1–23 bytegleich zur abgenommenen M1-Basis. Ergebnisse werden deterministisch aus den unveränderten verschlüsselten Originalquellen und getrennten Kontextrevisionen erzeugt. SQLCipher, Keystore, Signaturaufnahme/-mathematik, Zertifikatsvertrauen NOT_EVALUATED, Hardware und CI sind unverändert. Version40130 identifiziert ausschließlich das neue M2-Testupdate; keine neue Produkt-/Releaseversion1.0.

## Regelabdeckung und fachliche Sperren

| Regel | Implementierung / Einschränkung |
|---|---|
| D01/D02 | Tägliche Fahrt zwischen belegten Tageslenkankern; normale9h, bei verfügbarem Vorgang höchstens10h. Genau9h verbraucht keinen Vorgang; erste positive Zusatzfahrt kennzeichnet ein Verlängerungsereignis. Nach zwei belegten vorherigen Vorgängen kein dritter Slot, auch bei zusätzlicher Lücke. **L02-Zuordnung bleibt vorläufig**, gesicherte Tages-/Verlängerungsbudgets werden nicht daraus erfunden. |
| D03 | Fahrt am technischen UTC-Montag aufteilen und jede Woche gegen56h prüfen. Fehlender Wochenanfang macht die bekannte Menge zur Untergrenze. L02-Qualifier immer vorhanden, kein rechtlich gesichertes Wochenrestbudget. |
| D04 | Alle überlappenden Paare aufeinanderfolgender UTC-Wochen gegen90h, einschließlich laufender Paare. Getrennte Befunde je Fenster; keine Straf-/Konkurrenzsumme. L02-Qualifier und fehlende Historie sichtbar. |
| D05 | Vorhandenen artikelbezogenen M1-Scope und Other-work-Hinweis übernehmen. Belegter Länderpunkt setzt nichts zurück; AETR/ungeprüfter Rechtsraum/Scope-Konflikt erzeugt keine sichere EU-Bewertung. Echte Artikel7-Teilausnahme entfernt nicht automatisch Artikel6-Fahrminuten. |
| D06 | Ununterbrochen≥7h nach benannter C(2011)3759-Empfehlung trennt Tageslenkabschnitte.≥9h hat eigenen beobachteten Ankertyp. **Beide sind keine M3-Tagesruhequalifikation**; kein Reduzierungs-/Wochenruhe-/Ausgleichsreset. Empfehlung und historische Anwendung separat gekennzeichnet. |
| B01/B02 | Kumulierte270min, nicht verstrichene Kalenderzeit;45min oder zuerst≥15/später≥30.44min allein,30+15 im Standardfall und beliebige kleine Summen setzen nicht zurück. Arbeit/gewöhnliche Bereitschaft zählen nicht als Fahrt oder passende Pause. |
| B03 | Gelegenheitsbus seit22.05.2024 zwei aufgezeichnete Ruheteile jeweils≥15 und Summe≥45, z.B.30+15/20+25. Konkrete zeitbezogene Dienstangaben verwenden, kein Reiseverwaltungssystem. Historische Grenzstunden bleiben bedingt, solange ihr Rechtszeitbezug nicht bestätigt ist. |
| B04 | Zeitlich belegte eigene Crew-/Beifahrer-Bereitschaft als45-min-Unterbrechung seit2020 berücksichtigen. Keine Zweitkarte oder Routinebestätigung. Manuelles c1 ist kein Crewbeweis; konkrete Arbeits-/Crew-/Slotkonflikte erhalten ihre Auswirkungen. Vor2020 keine vermeintlich bereits fachlich bestätigte Rückwirkung. |
| B05 | Unbekannte/konfliktbehaftete Aktivität oder entscheidender Scope unterbricht die sichere Beweiskette, ohne als Pause zu gelten. Konkret als andere Arbeit angegebene Reise wird nicht automatisch als Ruhe angerechnet. Andere ausreichend belegte Teilabschnitte werden separat geprüft. |

## Zahlen-/Zeit-/Ergebnisvertrag

- Alle Dauern und Intervallgrenzen aus UTC-Instants. Keine Anzeigezone, Gerätestandardzone, Systemuhr, Importuhr oder Kalender-/Schichtansicht als Rechenparameter.
- `boundaryReachedAt` ist der Zeitpunkt der verbrauchten Fahrgrenze; `firstExcessAt` die erste tatsächlich folgende überschießende Fahrt. Eine dazwischenliegende45-min-Pause wird nicht in die Überschreitungsdauer addiert. Mit vollständiger Beweiskette exakte Zeit; mit fehlender Vorgeschichte ausdrücklich nur spätestens belegter Beginn und untere Mengenprojektion (`onsetIsExact=false`). Kein erratener früherer Beginn.
- Ein späterer Reset löscht keinen früheren belegten Überschuss. Derselbe Importinhalt zählt einmal, zusätzliche kompatible Herkunft bleibt erreichbar. Ereignis-IDs hängen nicht von neuer identischer Importherkunft, Anzeigezone oder Öffnungszeit ab; Berechnungsrevision bindet dagegen die gesamte aktuelle M1-Herkunfts-/Kontextrevision und Interpretations-/Policyfassung.
- Aktuelles belegbares Pausenrestbudget ist ausschließlich `provenCurrentBreakHeadroomSeconds` am `observedThrough`. Nach einer belegten abgeschlossenen45-min-Pause kann es270min sein, ohne eine folgende Minute zu erfinden. Bei neuester Lücke/ungeklärtem Scope/Bus-Nachtabhängigkeit null. Historische Projektionen sind keine aktuellen Budgets. Wochen-/Verlängerungsbudgets bleiben unter L02 gesperrt; arithmetische `conditionalHeadroomSeconds` ist kein freigegebener UI-Budgetwert.
- Zahlenüberschreitungen tragen immer `LegalAssessment.NOT_DETERMINED`. Exakte passende Tages-/Wochenmengen können einen besonderen Artikel12-Hinweis tragen; weder automatische Rechtfertigung noch bestätigte Rechtswidrigkeit, Begründungspflicht, Heimkehrkontrolle oder entstandene Schuld. Vor2020 und bei bloßer Mengenuntergrenze keine scheinbar genau passende neue Heimkehrregel.90h werden nicht durch diesen speziellen Hinweis gerechtfertigt; allgemeiner Art.12 bleibt getrennt offen.
- Die internen Preparing/Ready/Failed-Zustände sind Berechnungsphasen, keine zusätzlichen Verstoßstatus/Statusfilter. Alte Ergebnisse werden bei Neubau zurückgenommen; nur aktuelle vollständige Revisionen veröffentlicht.

## Zähler-, Sonderfall- und Freigabegrenzen

`WeeklyDrivingExtensions` hat ausschließlich einen UTC-Kalenderwochenschlüssel und **keine Wochenruhe-Resetfunktion**. Die beiden Verlängerungen werden innerhalb einer Woche nicht durch45h oder eine andere Zwischenruhe neu verfügbar. Tageslenkanker und Wochenraster sind verschiedene Typen. Ein M3-Zähler für maximal drei tägliche Ruhezeitreduzierungen zwischen rechtlich qualifizierenden Wochenruhen ist **noch nicht implementiert** und darf diesen Kalenderzähler nicht wiederverwenden. Dessen vollständiger Resetnachweis folgt zwingend in M3; die neue M2-Regression sichert bereits den Nichtreset durch Wochenruhe.

Fährabhängige Tagesruhezuordnungen werden nicht erfunden: einschlägige0522-Punkte erzeugen konkrete M3-Prüfhindernisse. Zeitlich vorher bereits belegte Befunde bleiben. Während nicht qualifizierter Fährabschnitte kein scheinbar sicherer Tageslenkbefund; ein GEN1-Beginn ist kein belegtes Fährende. Wochenfahrmengen und unabhängige Pausen bleiben separat prüfbar. Fehlende End-/Anrechnungsvoraussetzungen werden nicht mit Routinefragen ersetzt.

Das besondere3-h-Nachtfenster nach tatsächlicher12-Tage-Busqualifikation gehört zu W12/M4; M2 behauptet dessen Abschluss nicht und unterdrückt dafür ungesicherte Gelegenheitsbus-Budgets.25-h-/Tagesruhe-/Wochenruhe-/Ausgleichsrechnung M3/M4, neue Übersicht/UI/Badges M5, PDF/Beta-Werkzeuge M6 und Release M7 bleiben außerhalb. Die neue historische Ortszeitanzeige wird nur als spätere Darstellungsanforderung dokumentiert; keine Ortszeitbestimmung oder Standortüberwachung implementiert.

**Empfehlung:** gesonderte M2-Technikabnahme nach allen vollständigen Gates und neuer erforderlicher Geräteprüfung. Uneingeschränkte fachliche M2-Abnahme bleibt insbesondere wegen L02 und benannter historischer/Fachnachweise offen. Die unabhängige qualifizierte Gesamtprüfung und Beta5–10 Fahrer bleiben Releasepflichten. Kein eigenständiger M2-Merge.
