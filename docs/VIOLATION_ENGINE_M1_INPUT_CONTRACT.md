# M0 – verbindlicher logischer Eingangsvertrag für M1

Stand 14.10.2026. Spezifikation auf bestehendem Produktbaum, **kein Kotlin-/SQL-Entwurf zur unmittelbaren Ausführung**. M1 ist nicht freigegeben. Geltung: PD/A–H/E/P und [Gesamtplan](VIOLATION_ENGINE_V1_PLAN.md); Nachweise/Gates im [Quellenregister](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md). Die folgenden logischen Bezeichnungen schreiben keine konkreten Klassen, Tabellen oder Migrationen vor.

## 1. Vertragsziel und Grenzen

M1 stellt einen konsistenten, eigenen, rückverfolgbaren Faktenstand bereit. Eine spätere Engine muss für jede berücksichtigte Minute den ursprünglichen Beleg, die Semantik und die Grenzen ihrer Aussage wiederfinden können. Eine fehlende Voraussetzung wird ausdrücklich getragen und nicht mit einem Standardwert ersetzt. Die legale Berechnung und die frei gewählte Darstellung benutzen getrennte Verträge.

M1 ändert nach eigener späterer Freigabe ausschließlich die dafür erforderlichen Bausteine. Vorhandene Parser, Quellen, Originale und unveränderliche Normalisierung bleiben die Grundlage. Keine neue Kryptografie, zweite Kartenparserfamilie, Signaturaufnahme, Trustentscheidung, Karten-Schreiboperation, Datenrückfüllung oder angenommene Altarchivzuordnung. Aktuelles M0 ändert überhaupt keine dieser Komponenten.

## 2. Eingefrorener Faktenstand

| Logischer Bestandteil | Pflichtinhalt / Invariante |
|---|---|
| Snapshot Identity | Fahrerprofil-ID; Menge zulässiger eigener physischer Karten; vollständig bestimmte Datenrevision; Kontextrevision; Regel-/Interpretationsrevision. Engineversion und Herkunftskatalog separat. Keine Displayzone im fachlichen Inputhash. |
| Source Identity | Profil→physische Karte→Anwendung/G1 oder G2(V2 als Version)→Parentbatch→konkreter Child/Importversuch→EF/Generation→Original-/Receipt-/Signaturbeleg. Fehlt eine Ebene tatsächlich, keine synthetische Altbindung erzeugen; begründete Unverfügbarkeit tragen. |
| Activity Observation | Quelltag UTC, Index des tatsächlichen gespeicherten Wechsels, Minute 0..1439, Tätigkeit, Slot, Kartenstatusp, Roh-c; normalisierter Tagesfingerprint und exakter Quellenlink. Ein rekonstruierbares Rohwort ist keine zusätzlich gespeicherte Originalquelle. |
| Activity Interval | Halboffenes `[startInstant,endInstant)`, Tätigkeit oder Unknown/Conflict, tatsächliche Auflösung, Anfangs-/Endbeleg, alle kompatiblen Ursprünge, semantische Ableitung/Version. Positivdauer nur mit Endbeleg. Punkt und unbewiesenes offenes Ende sind keine positive Intervallminute. |
| Special Condition Observation | EF0522-Zeitpunkt, Rohcode und bekannte Bedeutung, G1/G2/V2, Record/Ringposition und Quellenbezug. G1 Fähre/Zug ist ein Punkt, kein erfundenes geschlossenes Intervall. Unbekannte Codes/Zeiten bleiben unterscheidbar. |
| Load Type Observation | EF0530 nur GEN2 V2, Zeitpunkt/Rohcode/GOODS/PASSENGERS/UNDEFINED/unbekannt; Record/Quelle. Ereignis allein beweist keine ununterbrochene Verkehrsart über alle Lücken und keine Linien-/Gelegenheitsart. |
| Other Observation | Vorhandene Fahrzeuge, Länder-/Grenz-/Orts-/GNSS-Punkte mit jeweiliger Zeitauflösung, Provenienz und ursprünglicher Unbekanntheit. Keine Smartphone-Ortung/Heimradius. |
| Context Assertion | Kontextart/Wert, Gültigkeitsintervall, Erfassungsherkunft, Erfassungszeit, Revision, betroffener tatsächlicher Einsatz-/Ruhe-/Dienstzusammenhang. Aussage des Nutzers ist keine neue Fahrerkartenbeobachtung. |
| Scope Decision | Regel-/Artikelmenge und Zeitbereich, Rechtsordnung/Version, Anwendung/partielle belegte Ausnahme/konkrete Unklarheit/unsupported; zugehörige Tatsachen und Begründung. Keine erfundene rechtliche Entscheidung aus einem OOS-Bit. |
| Coverage | Je Quellenfamilie und Regelabhängigkeit: belegte Intervalle, konkrete Lücken/Konflikte/offene Enden, gesicherte Endgrenze und fehlende Anker. Nicht ein globaler grüner Vollständigkeitsschalter. |
| Integrity | Vorhandener Aufnahme-/Receipt-/mathematischer Signaturstatus unverändert referenziert; Zertifikatsvertrauen stets NOT_EVALUATED. Fehlend/nicht versucht/ungültig/nicht auswertbar/gültig leer getrennt. Keine neue pauschale Vertrauensskala. |

Quellen-/Regel-/Kontexthashes werden aus dokumentierter stabiler kanonischer Repräsentation gebildet. Die konkrete Serialisierung ist vor M1-Abnahme als Implementierungsdetail mit deterministischen Vektoren festzulegen. Original-Fingerprints bleiben bytegleich; ein neuer abgeleiteter Hash ersetzt sie nicht. Kein Bezug enthält maskierte Anzeigenummern als Primärschlüssel. Anzeigen und Diagnosen geben sensible Identitäten nur minimiert/maskiert aus.

## 3. Vorhandene Zugriffe und atomare Grenze

| Bereits gelesener Bestand im Produktbaum | Verbindliche M1-Nutzung |
|---|---|
| `CardActivityParser`, `NormalizedCardActivityStore`, `DriverActivityDao` | Bestehende UTC-Tage/Wechsel und Quellenlinks lesen. Tagesfingerprint enthält Karte/Datum/Tätigkeiten/Slot/c/p, nicht Ringposition; verschiedene Ringplätze allein erzeugen keine neue Tätigkeit. Kein Schreib-Backfill bei fehlender Normalisierung. |
| `CardReadBatchDao` / `DriverCardImportPipeline` | Beide eigenen Anwendungsversuche über Parent/Child in einem konsistenten Lese-Snapshot laden. Erst nach erfolgreichem äußerem Commit auslösen; ein SAVED/PARTIAL-Child mitten in der Gruppe ist kein vollständiger Gruppentrigger. MF bleibt Parentbeleg. |
| `DriverCardImportRepository.get` | Exakte gültige Sitzung/Quelle für Herkunft nutzen. `all/latest` beschränken auf Primäranwendung und beweisen daher allein keine vollständige Eingangsmenge. |
| `CardSpecificConditionParser/Store`, `CardLoadTypeParser/Store` | Vorhandene dekodierte EF0522/0530 und genaue Quellen lesen, keinen parallelen Parser entwickeln. Fehler einer zusätzlichen Familie nicht als gültig leer behandeln. |
| `CardDayTimeline` | Nur Darstellung; dessen letzte Tätigkeit bis Tagesende niemals ungeprüft an den Rechtskern geben. |
| `TimeContext.REPORT_ZONE` | Vorhandene feste Anzeige ist keine gesetzliche Zeitkonvention und keine Enginevorgabe. |

Quellenprüfung geschieht innerhalb eines eingefrorenen Read-Snapshots; Berechnung danach außerhalb langer Room-Transaktionen. Ergebnis nur veröffentlichen, wenn Daten-/Kontext-/Regelrevision noch aktuell. Quellenfehler lehnt die konkrete unzulässige Beziehung ab; Importoriginale bleiben gespeichert. Atomare Neuberechnung und Audit sind M1-Vertragsvorbereitung, vollständige Umsetzung/Anzeige folgt den Paketen M2–M6.

## 4. EF0504 – semantische Wahrheitstabelle

Quellen: Q14-G1 Appendix 1 §2.1 einschließlich S.56-Entnahmenotiz; Q04 AnnexIC Appendix 1 §2.1 einschließlich identischer Notiz, Q17-Änderungen. Das vorhandene Feld `crewDriving` bleibt unverändert und wird als **Roh-c** gelesen.

| p / card Inserted | c / vorhandenes crew Driving | Semantik in Kartenaufzeichnung | Folgerung |
|---|---|---|---|
| 0 / true | 0 / false | Karte eingesetzt, Solo | Belegte aa-Tätigkeit und Slot für den belegten zeitlichen Abschnitt; kein Crewprivileg. |
| 0 / true | 1 / true | Karte eingesetzt, Crew | Zeitlich belegte Crewinformation grundsätzlich verwenden; keine routinemäßige zweite Karte/Anwesenheitsbestätigung. |
| 1 / false | 0 / false | Karte entnommen/nicht eingesetzt; folgende Tätigkeit unbekannt | aa/Slot am Entnahmepunkt nicht als Folgetätigkeit fortschreiben. Folge Unknown, selbst bei aa=REST. Slot bezeichnet am Entnahmepunkt den Entnahmeslot. |
| 1 / false | 1 / true | Folgende Tätigkeit bekannt, manuell am Kontrollgerät eingetragen | aa als entsprechende bekannte aufgezeichnete Tätigkeit; **kein Crewbeweis**. Slot außerhalb der Entnahmenotiz rechtlich nicht wie normale Beifahrerposition verwenden. Badge exakt „Manueller Nachtrag am EG-Kontrollgerät“. |

Rohwortkontrolle für 07.09.2026, Minute 360(06:00), SlotDRIVER, aaREST: `0168`=eingesetzt Solo; `4168`=eingesetzt Crew; `2168`=nicht eingesetzt/unbekannt; `6168`=nicht eingesetzt/bekannter manueller Nachtrag. Für WORK jeweils`+1000`, AVAILABILITY`+0800`, DRIVING`+1800`, CO_DRIVER`+8000` (hex). Keine Addition verändert die Bedeutung von p/c. Bei p=1, c=0 sind sämtliche vier aa-Varianten unbekannte Folge. `0000` ist ein möglicher tatsächlicher REST-Wechsel um 00:00, nicht allein ein Leerbeleg.

Bei Entnahme schreibt das Kontrollgerät s=Entnahmeslot, c=0, p=1, aa=aktuelle Tätigkeit. Später darf das Kontrollgerät c/aa in seiner Karte durch manuelle Eingabe überschreiben. Fahrer Tagebuch bewahrt beide tatsächlichen Imports unverändert und leitet den aktuellen Sachstand mit beiden Bezügen neu ab. Keine App-Eingabefunktion für Aktivitäten.

Gleichminütige mehrere Wechsel: geordnete Beobachtungen erhalten; dazwischen null Minuten, keine künstlichen Sekunden/Minuten. Ein p=1/c=0-Entnahmepunkt im selben Minutenstempel darf nicht verloren gehen. Eine dauerhafte Folge wird erst aus dem belegten geordneten Endzustand und passendem Endbeleg gebildet. Fachlich nicht bestätigte kollidierende Reihenfolge → Konflikt, kein „höchster Index gewinnt“ über verschiedene Quellen. Unplausible Rohkombination bleibt als Quellenanomalie erkennbar, nicht still korrigiert.

## 5. Endbelege, Abdeckung und Stichtag

Eine Änderung um 06:00 und die nächste passende belegte Änderung um 08:00 tragen genau `[06:00,08:00)`. Eine Entnahme um 08:00 begründet keinen unbekannt durch Ruhe ersetzten Nachlauf. Der letzte beobachtete Wechsel selbst liefert keine Dauer bis 24:00. Ein späterer Tagesdatensatz kann ein zusammenhängendes Ende nur unter dokumentierter, geprüfter Kartenchronologie belegen; ein bloßes Datum oder ein Tageskopf genügt nicht, um eine Lücke zu schließen. Bis zum unabhängigen Endbelegabgleich bleibt dieser konservative Unknown-Pfad verbindlich.

Import-`startedAt/finishedAt/savedAt` sind im vorhandenen Ablauf Persistenz-/Verarbeitungszeiten nach dem Lesen, keine Aufzeichnungs-Endbelege. EF0507-Sitzungsbeginn ist kein aktuelles Nutzungsende. Geräteuhr, Appöffnung oder späterer Import alter Daten verschieben keinen juristischen Stichtag. Eine künftige zusätzliche Erfassungsmetadatenmessung wäre separat innerhalb freigegebener M1-Arbeit zu begründen und bräuchte unveränderte Hardware-/CI-/Gerätegates; allein damit ist fortlaufende Vergangenheit weiterhin nicht bewiesen.

Für regelbezogene Prüfung gilt:

- Bereits vor einer späteren Lücke vollständig belegte Ereignisse bleiben prüfbar.
- Darf in einer Lücke eine qualifizierende Ruhe liegen, werden getrennte Fahrblöcke nicht zu einem fiktiven Lenktag summiert.
- Bekannte Untergrenzen können ein numerisches Überschreiten beweisen, aber nur bei belegtem gemeinsamen Rechtsfenster und ohne mögliche unterbrechende Qualifikation.
- Restbudgets nur, wenn alle für genau dieses Budget entscheidenden Anker/Zähler/Anwendbarkeiten hinreichend bestimmt sind. Sonst ausblenden und konkrete Prüflücke anzeigen.
- Kein Befund nach dem belegten Stand. Bereits unumkehrbar nicht mehr erfüllbare Frist kann vorher erkennbar sein; Grenzpunkt, Feststellbarkeit und tatsächlicher Beobachtungsstichtag bleiben getrennt.

## 6. Deduplizierung und Konfliktvertrag

Ein abgeleiteter kanonischer Verlauf enthält jede tatsächliche Minute höchstens einmal. Übereinstimmende Quellen sammeln Bezüge; Quellenobjekte werden niemals zusammengelegt/gelöscht. GEN1/GEN2 derselben Karte sind zwei Quellen, keine zwei Fahrer. Ersatzkarten nur bei bereits zulässiger Profilbindung; fremde Quellen oder unzugeordnetes Altarchiv nicht anschließen.

Ein echter vorher unbekannter Zeitraum kann durch späteren bekannten Kontrollgeräte-Nachtrag belegt werden. Die frühere Unbekanntheit ist keine positive widersprechende REST/WORK-Beobachtung. Stimmen zwei tatsächliche bekannte Beobachtungen nicht überein, entsteht ein Konfliktsegment; neuer Import, bessere Generation oder neue Signatur allein bestimmt keinen automatischen Sieger. Konfliktgrenzen an allen belegten Änderungszeitpunkten aufteilen; gemeinsame Teilabschnitte behalten ihren Beleg. Stille Annahme „jüngster gewinnt“ ist verboten.

Herkunft eines Endes kann eine andere kompatible Quelle sein als die des Anfangs; beide sind mit der Ableitungsbegründung zu tragen. Keine frühere Sitzung bekommt nachträglich den jüngeren Endbeleg zugeschrieben. Identischer Replay erzeugt weder neue juristische Ereignisse noch Audit-/Neu-Duplikate. Datenänderung wird durch tatsächlichen Quellen-/Kontextunterschied begründet, nicht durch Anzeigefilter oder Uhrzeit.

## 7. Anwendbarkeits- und Kontextvertrag

| Kontext / Tatsachen | Kleinstmögliche relevante Aussage |
|---|---|
| Regulärer Güter-/Personeneinsatz | Masse inklusive Anhänger, Reise-/Rechtsraum und tatsächliche Beförderungsart soweit für konkrete Anwendung nötig; Profil wiederverwenden. Fehlendes EF0530 ist bei G1/G2 V1 kein Fehler. |
| Bus | PASSENGERS aus 0530 löst bei fehlender Angabe gezielt Linien-/Gelegenheitsfrage aus. Linienlänge ist die Linie, keine kurze Fahrt. Besondere Dienstkennung, -dauer und notwendige Ausnahmebedingungen nur bei konkreter Nutzung; keine komplette Reiseverwaltung. |
| Crew | Eigene gültige zeitliche p=0/c=1-/Slotchronik nutzen. Gesetzliche erste Stunde nicht als dauerhafte pauschale Ausnahme. Unbekannte Folge nicht aus einem Bit heilen; keine zweite Karte/routinemäßige Anwesenheitsbestätigung. |
| International zwei Reduzierungen | Niederlassungsstaat Arbeitgeber, Wohnsitzland Fahrer, Beginnland beider tatsächlicher reduzierter Wochenruhen. Fehlende entscheidende Länder gezielt; keine Adresse/Heimradius/Standortüberwachung. |
| Fähre/Zug | Bestehende 0522-Marker; gezielt nur fehlende konkrete Schlafmöglichkeit und gegebenenfalls Fahrplandauer/Kabine für die betroffene Reise. Antworten gelten für diesen Zusammenhang, kein Standarddialog je Import und keine allgemeine Unterkunftsprüfung. |
| Art.3/11/13/14/OOS | Nur konkret relevante belegte Ausnahme mit Artikel-/Gebiets-/Zeitumfang oder transparente Unklarheit/Unsupported. Kein vollständiger aktiver Spezialbetriebskatalog. OOS allein weder Anwendungsbeweis noch Freistellung. |
| Artikel 12 | Keine Anerkennungs-/Begründungs-/Heimkehr-Faktenabfrage. Numerischer Befund bleibt mit offener Bewertung; keine tatsächliche Ausgleichsschuld aus Zahlen ableiten. |
| Rechtszeit | Technischer Instant ist bestimmt. Rechtliche Wochen-/Nachtkonvention separat versioniert mit Fundstelle; bei L02-Unklarheit ausdrücklich unbestimmt statt Displayzone oder RTM-Woche zu übernehmen. |

Kontextantworten erkennen sich über Art, betroffenen Zusammenhang, Gültigkeitsintervall und Revision wieder. Unveränderter Reimport wiederholt keine Frage. Ein geändertes Standardprofil überschreibt keine Vergangenheit. Widerspruch wird konkret angezeigt. Kontext darf keine aufgezeichnete Tätigkeit entfernen oder ändern. Minimaler Scope-Guard ist bereits spezifizierbar; aktive rechtliche Sonderpfade benötigen den jeweiligen L-Nachweis vor Freigabe.

## 8. Ergebnis- und Auditübergang

Ein späterer Lauf trennt numerischen Tatsachenbefund, rechtliche Einordnung, Verpflichtung, Warnung und Prüfhindernis. Das ist kein zusätzlicher Statusfilter. Ein normaler Lenküberschuss mit möglicher Art.12-Ausnahme bleibt in Liste/PDF, aber ohne gesicherte Rechtswidrigkeitsklasse oder automatische Schuld. Quellenqualität ist kein erfundener juristischer Schweregrad.

Jeder Befund trägt Regel-ID/Tatzeitfassung/Interpretation, tatsächliche betroffene Intervalle, mathematische Grenzstelle, Mengenart (zusätzliche Fahrt/Ruhefehlmenge/Verspätung), Feststellbarkeitszeitpunkt, Quellen/Anker und den getrennten Bewertungsumfang. Grenzstelle bedeutet bei exakt ausgenutzter Grenze noch keine positive Überschreitung; erst eine belegte positive Zusatzdauer begründet den Überschuss. Volle betroffene Periode und überschießende Teilstücke nicht verwechseln.

Offene Ausgleichspflichten tragen Ursprung, erforderliche Mindest-/angerechnete Ruhe, Fehlumfang, Rechtsfrist, zusammenhängende belegte Erfüllungsintervalle und exklusive Zusatzminuten. Erfüllte Pflichten verschwinden nur aus der offenen Ansicht, nicht aus Herkunft/Audit. Stabile Episoden-ID richtet sich nach Regel/ursprünglichem Anker/Profil, nicht Displaytag/Import-ID. Eine spätere echte Ruhe heilt keine frühere Verspätung; ein späterer Import tatsächlicher damaliger Erfüllung darf die alte Bewertung mit Vorher/Nachher/Grund berichtigen.

Während Neuberechnung alte Ergebnisse ausblenden. Veröffentlichung nur atomar für aktuelle Revision. Prozessabbruch/Fehler/Cancellation bewahrt den Import und liefert weder halbe Ergebnisse noch eine positive Entwarnung. Reine Projektion (Zone/Kalender/Schicht/Filter) erzeugt keine juristische Revision, keine Schuld, kein Zählerreset und keine neue Markierung.

## 9. M1-Gate und Abnahmebelege

A02/A04/A05 sowie Eingangsanteile A08/A11/A17/A18/A23 werden gegen [M0-Datenvektoren](VIOLATION_ENGINE_M0_DATA_CASES.md) geprüft. A06 erfordert bei später tatsächlich nötiger Persistenzänderung eine echte additive Compilerexport-Migration mit SQLCipher-/Reopen-/Replay-/Rollback-Nachweisen. A15 und sämtliche bisherigen CI-/Geräte-/Mergegrenzen bleiben unverändert.

Vor Programmierungsfreigabe: Tobi erhält M0-Bericht und Restblocker; keine stillschweigende Erlaubnis aus diesem Vertragsdokument. Für vorbehaltlose M1-Abnahme sind die konkret eingesetzten technischen Fassungen/Endbelege unabhängig abzugleichen. Unbestimmte Quellen-/Scope-/Rechtszeitwerte dürfen als solche transportiert werden; daraus entsteht keine Freigabe, ungeklärte M2–M4-Regeln zu programmieren. Es gibt keinen Schema-/Parser-/Reader-/APK-Diff in M0.
