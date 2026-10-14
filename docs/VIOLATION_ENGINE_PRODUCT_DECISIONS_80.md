# FahrerTagebuch 1.0 – 80 verbindliche Produktentscheidungen zur Verstoß-Engine

**Stand:** 14.10.2026
**Projekt:** FahrerTagebuch
**Repository:** `Perrystack9/Fahrerabrechnung_Android_0.4.1`

Diese 80 Entscheidungen wurden gemeinsam im Produktberatungschat getroffen und sind für die Planung von FahrerTagebuch 1.0 verbindlich.

**Auftrag:** Bitte die Entscheidungen dokumentieren und bei der Erstellung der Umsetzungsspezifikation berücksichtigen. Vor einer Implementierung möchte ich zunächst einen fachlich vollständigen Plan mit gesetzlicher Regelmatrix, Architektur, Teststrategie und Abnahmekriterien prüfen und ausdrücklich freigeben.

Die geltenden gesetzlichen Anforderungen dürfen nicht durch Produktentscheidungen verändert werden.

## Runde 1 – Grundlagen (1–5)

1. **Einsatzprofil:** Bei der Einrichtung ein Einsatzprofil erfassen und Ausnahmen beziehungsweise abweichende Einsätze berücksichtigen.
2. **Automatische Prüfung:** Nach jedem erfolgreichen Fahrerkartenimport die Verstoß-Engine automatisch ausführen.
3. **Datenlücken:** Fehlende oder unzureichende Daten deutlich in der Hauptübersicht kennzeichnen.
4. **Artikel 12:** Mögliche Sonderfälle nach Artikel 12 lediglich kennzeichnen. Keine automatische Rechtfertigung und keine verpflichtende Eingabe einer Begründung.
5. **Zeitbudgets:** Neben Verstößen verbleibende gesetzliche Zeitbudgets anzeigen, bezogen auf den letzten Importdatenstand.

## Runde 2 – Berechnung und Kontext (6–10)

6. **Offene Abschnitte:** Auch noch nicht abgeschlossene Abschnitte bis zum letzten belegten Datenstand prüfen. Eindeutig festgestellte Überschreitungen anzeigen.
7. **Keine manuellen Tätigkeitsnachträge:** Fehlende Tätigkeiten oder Ruhezeiten nicht manuell ergänzen. Datenlücken bleiben entsprechend ungeklärt.
8. **Besondere Einsatzarten:** Ergänzende Kontextangaben für Mehrfahrerbetrieb und besondere Einsatzarten ermöglichen. Nicht belegte Voraussetzungen transparent kennzeichnen.
9. **Wochenruhe-Ausgleich:** Offene Ausgleichsverpflichtungen einschließlich ihrer Fristen bereits vor Fristablauf anzeigen.
10. **Verstoßdetails:** Exakte Zeiträume, Rechtsgrundlagen, Rechenwege und verwendete Quellen in einer Detailansicht darstellen.

## Runde 3 – Tägliche und wöchentliche Ruhezeiten (11–15)

11. **Tägliche Ruhezeitreduzierungen:** Verbrauchte und verbleibende zulässige Reduzierungen direkt in der Übersicht anzeigen.
12. **Frühwarnungen:** Auf Grundlage des letzten Imports vor einer drohenden täglichen Ruhezeitverletzung warnen. Einen Verstoß erst bei eindeutiger Feststellung melden.
13. **Geteilte Ruhezeiten:** Zulässige 3+9-Stunden-Ruhezeiten automatisch erkennen und ihre Anrechnung erklären.
14. **Unbekannte Zeitabschnitte:** Datenlücken im Zeitstrahl markieren und ihre Auswirkungen auf die Prüfung erläutern.
15. **Wochenruhebudget:** Gesetzliche Frist für die nächste Wochenruhezeit und verbleibendes Zeitbudget in der Übersicht anzeigen, sobald die Anzeige gemäß Punkt 16 relevant wird.

## Runde 4 – Wochenruhezeiten und Ausgleich (16–20)

16. **Wochenruhefrist:** Die Frist zur nächsten Wochenruhezeit auf der Hauptübersicht erst anzeigen, wenn sie näher rückt.
17. **Reduzierte Wochenruhe:** Verkürzung, Ausgleichsdauer und gesetzliche Ausgleichsfrist ausführlich in den Details erklären.
18. **Automatische Ausgleichserkennung:** Offene Ausgleichspflichten automatisch aus belegten Ruhezeiten zuordnen und die Anrechnung nachvollziehbar dokumentieren.
19. **Internationale Sonderregeln:** Bei möglicherweise einschlägigen Sonderregelungen des internationalen Güterverkehrs gezielte optionale Kontextangaben abfragen.
20. **Ruheort/Unterkunft:** Keine Abfrage und keine Verstoßbewertung dazu, ob eine Wochenruhezeit im Lkw, Hotel oder zu Hause verbracht wurde. Die zeitlichen Wochenruhevorgaben werden weiterhin vollständig geprüft. Die gesetzliche Unterkunftsregel selbst bleibt außerhalb des App-Prüfumfangs.

## Runde 5 – Lenkzeiten und Fahrtunterbrechungen (21–25)

21. **Geteilte Fahrtunterbrechungen:** Gesetzlich zulässige Aufteilungen automatisch berücksichtigen und standardmäßig nur das Ergebnis anzeigen.
22. **Restlenkzeit bis zur Pause:** Diese Information ausschließlich in den Details anzeigen, nicht direkt auf der Startseite.
23. **10-Stunden-Verlängerungen:** Bereits verbrauchte und noch verfügbare tägliche Lenkzeitverlängerungen direkt in der Übersicht darstellen.
24. **Wochen- und Doppelwochenlenkzeit:** Die 56-Stunden-Wochengrenze und 90-Stunden-Doppelwochengrenze getrennt einschließlich verbleibender Budgets anzeigen.
25. **Mehrfahrerbetrieb:** Anwendbare Mehrfahrer-Sonderregelungen automatisch berücksichtigen; ihre Berechnung und Voraussetzungen in den Details erklären.

## Runde 6 – Verstoßdarstellung (26–30)

26. **Startseite:** Kompakte Zusammenfassung festgestellter Verstöße mit direktem Zugang zur vollständigen Übersicht.
27. **Kalenderzuordnung:** Verstoßmarkierungen an die gewählte Kalender- oder Schichttagansicht anpassen. Der tatsächliche Verstoßzeitpunkt bleibt unverändert.
28. **Gruppierung:** Gleichartige Verstöße gruppieren. Einzelereignisse und ihre Berechnungen bleiben aufklappbar.
29. **Warnungen:** Bevorstehende Risiken deutlich von festgestellten Verstößen unterscheiden und optisch zurückhaltender darstellen.
30. **Historie:** Standardmäßig Verstöße der jüngsten 28 Tage anzeigen. Ältere Ereignisse bleiben über das Archiv abrufbar. Dieser Anzeigefilter begrenzt niemals den gesetzlichen Berechnungshorizont.

## Runde 7 – Datenqualität und Nachvollziehbarkeit (31–35)

31. **Neue Verstöße:** Nach einem Import erstmals erkannte Verstöße vorübergehend als „Neu“ kennzeichnen.
32. **Nachträgliche Neubewertungen:** Änderungen an früheren Bewertungen ausschließlich im Prüfprotokoll dokumentieren. Die Übersicht zeigt das aktuelle Ergebnis, jedoch keinen zusätzlichen prominent hervorgehobenen Änderungshinweis.
33. **Widersprüchliche Daten:** Konflikte zwischen Aufzeichnungen direkt im Zeitstrahl markieren und in den Datenqualitätsdetails erklären.
34. **Mehrere Verstöße im selben Zeitraum:** Zeitlich zusammenhängende Ereignisse als Gruppe mit sämtlichen rechtlich eigenständigen Verstößen darstellen.
35. **Technische Nachweise:** Technische Herkunfts- und Prüfbelege in einem zusätzlichen aufklappbaren Bereich der Verstoßdetails anzeigen.

## Runde 8 – Bedienkomfort und Klassifizierung (36–40)

36. **Schweregradklassifizierung:** Zusätzliche rechtliche Schweregrade berücksichtigen, soweit sie eindeutig aus einschlägigen Vorschriften ableitbar sind. Keine erfundenen Kategorien oder Bußgeldschätzungen.
37. **Filter:** Die Verstoßhistorie ausschließlich nach Zeitraum und Verstoßart filtern. Kein Statusfilter.
38. **Neu-Kennzeichnung:** Die Markierung „Neu“ verschwindet, sobald der Fahrer den entsprechenden Verstoß angesehen hat.
39. **Direkte Zeitstrahlnavigation:** Aus einem Verstoßdetail unmittelbar zum betroffenen Abschnitt der Tätigkeitschronik springen.
40. **Anzeigezeitzone:** Eine feste, einstellbare Anzeigezeitzone verwenden. Bei der Einrichtung wird eine passende Standardeinstellung ermittelt. Gesetzliche Berechnungen sind davon unabhängig.

## Runde 9 – Qualitätssicherung und Funktionen (41–60)

41. **Unabhängige Fachprüfung:** Die Verstoß-Engine vor dem Release unabhängig fachkundig prüfen lassen.
42. **Betatestgröße:** Die erste geschlossene Testphase umfasst ungefähr 5–10 Fahrer.
43. **Zusammensetzung:** Zunächst Lkw-Fahrer als Betatester einsetzen.
44. **Fehlerberichte:** Betatester sollen Fehler über ein Formular innerhalb der App melden können.
45. **Testabdeckung:** Informationen zur fachlichen Testabdeckung nur intern dokumentieren.
46. **Rechtsänderungen:** Änderungen gesetzlicher Berechnungsregeln im Änderungsprotokoll erläutern.
47. **Neubewertungsprotokoll:** Frühere und neue Bewertung sowie den Grund der Änderung dokumentieren.
48. **Verwendete Gesetzesfassung:** Den für einen Verstoß angewendeten Rechtsstand in den Verstoßdetails anzeigen.
49. **Datenabdeckung:** Eine zeitliche Übersicht der vorhandenen und ausreichend prüfbaren Daten bereitstellen.
50. **Doppelte Importe:** Bereits vorhandene Aufzeichnungen still zusammenführen. Doppelte Daten dürfen keine doppelten Verstöße erzeugen.
51. **Unberechenbare Zeitbudgets:** Nicht zuverlässig berechenbare Restbudgets ausblenden. Die Hinweise zur unvollständigen Datenlage bleiben sichtbar.
52. **Sortierung:** Die neuesten Verstöße zuerst anzeigen.
53. **Gruppensortierung:** Gruppen gleicher Verstoßarten nach ihrem jeweils jüngsten Ereignis sortieren.
54. **Schweregradkennzeichnung:** Rechtliche Schweregrade durch Bezeichnung und Symbol darstellen.
55. **Gesetzesverweise:** In den Verstoßdetails den konkreten Gesetzesverweis angeben. Kein direkter externer Link zur Gesetzesquelle erforderlich.
56. **Zeitstrahlmarkierung:** Beim Sprung aus den Verstoßdetails den gesamten betroffenen Abschnitt hervorheben.
57. **Hinweise nach dem Import:** Neue Verstöße direkt in der App anzeigen. Keine zusätzliche Smartphone-Push-Mitteilung als Anforderung.
58. **Keine festgestellten Verstöße:** Eine kurze positive Meldung anzeigen, sofern die Daten für den betreffenden Zeitraum ausreichend vollständig sind. Datenlücken dürfen keine falsche Entwarnung erzeugen.
59. **PDF-Bericht:** Einen lokalen PDF-Verstoßbericht bereits in Version 1.0 ermöglichen. Dieser ist ausdrücklich kein DDD-Export und keine Abrechnung.
60. **Zeitraumwahl:** Die Verstoßhistorie erhält eine freie Zeitraumwahl. Keine zusätzlichen verpflichtenden Schnellfilter für 7, 28 oder 90 Tage. Der voreingestellte 28-Tage-Zeitraum bleibt bestehen.

## Runde 10 – Bereinigte Entscheidungen nach GitHub-Bestandsprüfung (61–80)

61. **Fähre/Zug:** Angewendete Fähre-/Zug-Regeln in den Ruhezeitdetails erklären. Die bereits implementierte EF 0522 wiederverwenden; keine doppelte Standardeingabe. Fehlende gesetzliche Voraussetzungen dürfen nicht unterstellt werden.
62. **Artikel-12-Sonderfälle:** Mögliche Artikel-12-Konstellationen unmittelbar am betroffenen Ereignis kennzeichnen.
63. **Out of Scope:** Die rechtliche Einordnung aufgezeichneter Out-of-Scope-Bedingungen direkt in der Chronik erklären. EF 0522 ist bereits vorhanden; die Kennzeichnung allein beweist nicht den rechtlichen Geltungsbereich.
64. **Fehlendes EF 0530:** Das vorhandene Einsatzprofil nutzen und nur bei entscheidenden Unklarheiten nachfragen. GEN1- und GEN2-V1-Karten besitzen keine EF 0530.
65. **Personenverkehr:** Wenn über die bereits implementierte EF 0530 Personenbeförderung erkannt wird, nach dem Import gezielt fragen, ob Linien- oder Gelegenheitsverkehr vorliegt, sofern die Angabe noch fehlt. Keine unnötigen Wiederholungsfragen. Rechtlich relevante weitere Voraussetzungen sind bei Bedarf ebenfalls zu prüfen.
66. **Datenqualität:** Auswirkungen unvollständiger Aufzeichnungen in einer allgemeinen Datenqualitätsübersicht erklären.
67. **Beginn der Überschreitung:** Den mathematisch exakten Beginn einer Überschreitung und den vollständigen betroffenen Zeitraum im Zeitstrahl hervorheben.
68. **Geänderte Einsatzangaben:** Nach Änderungen rechtlich relevanter Kontextangaben automatisch neu berechnen und die Neubewertung im Prüfprotokoll dokumentieren.
69. **Während der Neuberechnung:** Bisherige Prüfergebnisse vorübergehend ausblenden, bis die neue Berechnung abgeschlossen ist.
70. **Sommer-/Winterzeit:** Zeitumstellungen direkt im Zeitstrahl sichtbar markieren.
71. **PDF-Zeitraum:** Vor jeder PDF-Erstellung den gewünschten Auswertungszeitraum neu auswählen lassen, statt automatisch die aktuelle Auswahl zu übernehmen.
72. **Fahrername im PDF:** Optional zuschaltbar, standardmäßig deaktiviert.
73. **Datenabdeckung im PDF:** Datenlücken und Einschränkungen der Prüfbarkeit auf der ersten Seite des Berichts ausweisen.
74. **Vollständige PDF-Details:** Jeden Verstoß mit Zeitraum, Berechnung, Überschreitung und Rechtsgrundlage ausführlich dokumentieren.
75. **PDF-Speicherung und Weitergabe:** PDF lokal speichern und optional über das Android-Teilen-Menü weitergeben.
76. **Aktualisierung gesetzlicher Regeln:** Regeländerungen für Version 1.0 über reguläre App-Updates ausliefern. Die Berechnung soll offline funktionieren.
77. **Diagnosedaten bei Fehlerberichten:** Datensparsame technische Diagnosedaten optional anhängen, aber ausschließlich nach Vorschau und ausdrücklicher Zustimmung des Testers.
78. **Gesetzesfassung im PDF:** Die tatsächlich angewendete Gesetzesfassung bei jedem Verstoß separat aufführen.
79. **Beta-Diagnoseansicht:** Für den Betatest eine lokale technische Diagnoseansicht der Verstoß-Engine bereitstellen.
80. **Sprache:** FahrerTagebuch 1.0 zunächst vollständig auf Deutsch veröffentlichen.

---

## Zusätzliche verbindliche Grundsätze

Diese Grundsätze wurden außerhalb der nummerierten Entscheidungen ausdrücklich besprochen und sind ebenso zu beachten.

### A. Automatische Berücksichtigung gesetzlicher Ausnahmen

Sämtliche anwendbaren gesetzlichen Verlängerungen, Reduzierungen, Anrechnungen, Sonderregeln und Ausgleichsmöglichkeiten müssen korrekt berücksichtigt werden.

Beispiel: 9 Stunden und 25 Minuten tägliche Lenkzeit sind kein Verstoß, wenn eine zulässige 10-Stunden-Verlängerung noch verfügbar ist und keine andere Vorschrift verletzt wird.

### B. Keine künstlichen Verstoßstatus

Die Verstoßübersicht enthält eindeutig belegbare Rechtsverstöße. Datenlücken, ungeklärte Voraussetzungen und mögliche Artikel-12-Fälle werden getrennt als Prüfhinweise behandelt.

Es gibt keinen zusätzlichen Statusfilter.

### C. Rechtlicher Geltungsbereich

Das bloße Stecken einer Fahrerkarte beweist rechtlich nicht automatisch die Anwendbarkeit der VO 561/2006. Die Engine muss Einsatzprofil, tatsächliche Verkehrsart, maßgeblichen Geltungsbereich und anwendbare Ausnahmen berücksichtigen.

### D. Bereits implementierte Kartenquellen wiederverwenden

Insbesondere sind bereits vorhanden:

- EF 0504: Tätigkeiten, Steckplatz- und Mehrfahrerkennzeichen.
- EF 0522: Fähre/Zug und Out of Scope.
- EF 0530: Güter- beziehungsweise Personenbeförderung bei GEN2 V2.
- Weitere Quellen wie Fahrzeuge, Länderwechsel, GNSS und Ortsangaben.

Vorhandene Parser, Originaldaten und Speicherstrukturen wiederverwenden. Keine unnötige Neuimplementierung.

### E. Kalender- und Schichttagansicht

Der Fahrer kann bei der Einrichtung und später in den Einstellungen zwischen Kalendertag und Schichttag wählen.

Im Schichtmodus gehören Tätigkeiten zum Datum des Schichtbeginns, auch wenn die Schicht über Mitternacht reicht. 00:00 Uhr wird im Zeitstrahl sichtbar markiert.

Die gesetzliche Verstoßberechnung bleibt vollständig unabhängig von dieser Darstellung.

### F. Unveränderliche Fahrerkartendaten

Keine manuellen Nachträge oder Änderungen der aufgezeichneten Tätigkeiten. Zusätzliche Einsatzkontextinformationen werden getrennt erfasst und nachvollziehbar gespeichert.

### G. Grenzen von Version 1.0

AETR, eigenständige nationale Arbeitszeitgesetze, die automatische Bewertung des tatsächlichen Ruheortes und der DDD-Standardexport gehören nicht zum aktuellen Auftrag.

Die übrigen anwendbaren zeitbezogenen Bestimmungen der VO 561/2006 einschließlich ihrer gesetzlichen Sonderregelungen bleiben zu berücksichtigen.

### H. Qualitätssicherung

Vor dem Release benötigen wir eine vollständige fachliche Regelmatrix, reproduzierbare positive und negative Testfälle, Grenzfalltests, eine unabhängige rechtliche Fachprüfung und die vorgesehenen technischen CI-/Geräteabnahmen.

Die 80 Entscheidungen bilden eine verbindliche Produktgrundlage, ersetzen aber noch keine vollständige gesetzliche Implementierungsspezifikation.

---

## Nächster Arbeitsauftrag

1. Den tatsächlichen Projektstand und die abgeschlossene Übergabe aus der vorherigen Work-Sitzung prüfen.
2. Alle 80 Entscheidungen einschließlich der übergreifenden Grundsätze als verbindliche Produktanforderungen dokumentieren.
3. Bestehende technische Implementierungen und die nachgewiesene Kartenquellen-Abdeckung berücksichtigen.
4. Eine vollständige gesetzliche Regelmatrix zur VO 561/2006 erarbeiten.
5. Architektur, Datenbedarf, Teststrategie und verbindliche Abnahmekriterien ausarbeiten.

**Wichtig: Noch keine Implementierung der Verstoß-Engine beginnen. Ich möchte zuerst den vollständigen Plan prüfen und ausdrücklich freigeben.**

## Quellenvermerk zur Übernahme

Maßgeblich ist Tobis vollständige Nachricht in dieser Work-Sitzung vom 14.10.2026, übergeben zusammen mit der Markdown-Anlage. Die Entscheidungen 1–80 und Grundsätze A–H wurden inhaltlich unverändert übernommen; nur Leerzeilen und Überschriftenformat vereinheitlicht. Die beigefügte Anlage bleibt unverändert erhalten, SHA256 `83cdc61ca31cc8271b40122d67ee1a64fa63a9111cbe95b5e72f0b91c67989fe`. Sie enthält eine frühere Formulierung derselben Entscheidungen; bei Formulierungsunterschieden entscheidet die vollständig ausgeschriebene neue Nachricht. Kein Rückgriff auf den separaten Beratungschat.
