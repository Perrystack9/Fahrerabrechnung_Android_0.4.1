# FahrerTagebuch 1.0 – Test- und Nachweismatrix

**Geplante Tests, 14.10.2026. Noch keine Engine implementiert, keine der neuen fachlichen Testgruppen ausgeführt.** Bestehende 40128-CI und Geräteabnahme sind davon getrennt.

Bezüge: [Plan](VIOLATION_ENGINE_V1_PLAN.md), [gesetzliche Matrix](VIOLATION_ENGINE_LEGAL_MATRIX.md), [historische80 Entscheidungen](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md), vorrangige [E01–E12 und abschließende P03–P05](VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md) und [Änderungsübersicht](VIOLATION_ENGINE_PLAN_CHANGES_20261014.md). **116 LG-Fälle,7 produktfachliche PF-Fälle,51 DT-Fälle (43+8),80 PD-/12 E-/5 P-Zuordnungen;23 Abnahmekriterien im Plan (20+3).** Die116/43/20 des vorherigen Entwurfs bleiben die Ausgangsbasis; P03–P05 sind jetzt beschlossen. Ursprüngliche IDs bleiben erhalten; ihre widersprechenden Erwartungen sind ausdrücklich aktualisiert, keine erfolgreiche Durchführung behauptet.

## 1. Reproduzierbarer Testvertrag

Jeder spätere Testvektor enthält feste UTC-Zeitstempel, Aktivitätssegmente, Quellreferenzen, Generation/Anwendung/Karte, Kontextrevision, Rechtsstand, dokumentierten Datenstichtag und vollständige erwartete Ergebnisse. Erwartete Werte werden fachlich unabhängig festgelegt; sie dürfen nicht durch dieselbe Engine erzeugt werden, die geprüft wird.

Als Standardursprung dient **Montag 07.09.2026 00:00 UTC**. Standardkontext: regulierte Güterbeförderung >3,5 t, EU-Reise ohne anwendbare Ausnahme, Solo, zulässige persönliche Karte, vollständige Eingangsdaten und eine belegte vorherige regelmäßige Wochenruhe. Alle Abweichungen stehen im einzelnen Vektor. Für historische Fälle wird das Datum ausdrücklich ersetzt.

Kurze Testnotation: `D(h:mm)` Fahren, `W(h:mm)` andere Arbeit, `A(h:mm)` Bereitschaft, `R(h:mm)` aufgezeichnete Ruhe/Pause, `?(h:mm)` unbekannt. `DL(h:mm)` bezeichnet eine Tageslenksequenz mit normgerechten 45-min-Pausen nach höchstens jeweils 4:30 Fahrt. Jeder Kurzfall wird vor Implementierung als feste vollständige Eingabe ausgeschrieben; keine Aktivität darf zum Bestehen des Tests hinzuerfunden werden.

Einzelregeltests betrachten gezielt die benannte Regel; daraus folgt keine Aussage „alle anderen Regeln bestanden“. Zusätzlich gibt es vollständige Goldenfälle mit sämtlichen erwarteten Regelbefunden, Verpflichtungen, Budgets, Hinweisen und Quellen. Nach E07 ist bei Zahlenüberschreitung rechtliche Einordnung getrennt zu erwarten: passende1-/2-h-Fälle bleiben Übersicht/PDF mit offener Bewertung, ohne bestätigten Rechtswidrigkeits-Schweregrad oder automatische Schuld. Ein kurzer LG-Eintrag „Überschuss/Verletzung“ erlaubt keine abweichende definitive UI-Beschriftung. Positive Tests prüfen gerade auch Abwesenheit falscher Verstöße, nicht bloß positive Zählerwerte.

Für jede Grenzprüfung: `Grenze -1 Aufzeichnungseinheit`, `Grenze`, `Grenze +1 Aufzeichnungseinheit`; für EF-0504-minutengenaue Daten bedeutet das eine Minute. Sekundengrenzen werden nur für tatsächlich sekundengenaue Quellen/Fenstergrenzen separat geprüft.

## 2. Konkrete fachliche Mindestfälle

### 2.1 Anwendbarkeit und Zeitbasis

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG01 | S01 | Güterfahrzeug 3.500 kg / 3.501 kg, gleicher EU-Inlandseinsatz | Nur >3,5 t löst diese EU-Anwendung aus; keine pauschale Entwarnung für nationale Vorschriften. |
| LG02 | S02 | 2.501 kg, international, 30.06./01.07.2026 | EU-Grenze erst ab 01.07.; andere Tatzeit nicht rückwirkend ändern. |
| LG03 | S02 | 2.500 /2.501/3.500 kg; national/international/Kabotage | Inklusive Anhänger-Masse, Operatoren strikt > und ≤; keine allgemeine nationale Ausweitung. |
| LG04 | S02/S05 | 3.000 kg, Eigenbeförderung und Fahren nicht Haupttätigkeit; jeweils eine Bedingung fehlt | Art.-3-ha-Ausnahme nur bei allen Bedingungen; unklare Bedingung als Hinweis. |
| LG05 | S03 | Personenfahrzeug 9 /10 Plätze einschließlich Fahrer | >9 anwenden; Fahrer nicht doppelt zählen. |
| LG06 | S03/S05 | Linienlänge 49,999/50/50,001 km; dieselbe kurze Teilfahrt | ≤50 ausgenommen; gefahrene Teilstrecke ersetzt Linienlänge nicht. |
| LG07 | S04 | EU/CH/EEA-Reise versus Gesamtfahrt EU–AETR-Drittstaat | Reise-Rechtsordnung korrekt; im ausgeschlossenen AETR-Fall keine EU-Budgetfreigabe. |
| LG08 | S05/S09 | EF-0522-OOS-Marker, sonst EU-Güterprofil; Geltungsbereich unbekannt /belegt | Marker allein schaltet nicht alle Regeln aus; kommerzielle ausgenommene Fahrt wird nicht Ruhe. |
| LG09 | S06 | Konkreter belegter Art.-13-Ausnahmeumfang nur Art.7; kein aktiver Spezialkatalog | Keine pauschale Abschaltung anderer Regeln. Betroffene Pause entweder belegbar ausgenommen oder als unsupported begrenzt; keine ungeprüfte nationale Vollauswertung behaupten. |
| LG10 | S07 | Konkreter Beleg strengerer Art.-11-Regel, Inland/Grenzreise, aktive Unterstützung vorhanden/fehlend | Richtigen Artikel-/Gebiets-/Zeitumfang respektieren. Ohne verlässliche Unterstützung konkret eingeschränkte Prüfung, keine Behauptung nationaler Vollständigkeit. |
| LG11 | S08 | Konkrete amtliche Ausnahme `[t0,t1)`, belegte Aktivität genau vor/bei/nach t1 | Ausnahme nur in Geltung verwenden oder betroffene Prüfung begrenzen; nach Ende nicht weiter als Rechtfertigung. Keine Vollkatalog-Implementierung nötig. |
| LG12 | S08 | Verfügungsdatum fehlt /Liste enthält Ende vor Beginn | Ausnahmeobjekt nicht als gültig anwenden; betroffene Bewertung ausdrücklich unklar, kein stiller Standardbefund. |
| LG13 | S09/S10 | Belegte kommerzielle OOS-Fahrt /Dienstanreise ohne Sondervoraussetzungen | Andere Arbeit, keine tägliche Ruhe und keine normale Pause. Original-Kartenaktivität bleibt unverändert. |
| LG14 | S10 | Dienstanreise auf Zug mit gesetzlich belegtem Schlafplatz /unbekannt | Anrechnung nur bei nachgewiesener Sondervoraussetzung; unbekannt kein Beweis für Pause/Ruhe. |
| LG15 | S11 | Kartenchronik Solo0:59/Crew ab0:59 versus Solo1:01/Crew ab1:01; später konkret Solo/unklar | Gesetzliche erste Stunde und folgende zeitliche Qualifikation beachten. Keine tatsächliche Zweitfahrer-Anwesenheitsabfrage; Karte als Quelle nutzen, unbekannte Perioden nicht durch ein Bit heilen. |
| LG16 | S12 | Identische Quellen bei Anzeige UTC/Berlin/New York, Kalender/Schicht | Befund-IDs, Werte, Rechtswochen und Pflichten identisch; nur Darstellung ändert sich. |

**E09 ersetzt die frühere Pflicht zu aktiven Positiv-/Negativtests sämtlicher Spezialkatalogeinträge.** Verpflichtend bleiben vollständige Grenz-/Kontexttests tatsächlich unterstützter regulärer Einsatzregeln sowie Scope-Guard-Fälle für belegte Nichtanwendbarkeit, konkrete unklare Sonderausnahme, partiellen Artikelumfang und fehlende Unterstützung. Keine seltene Art.-3-/13-/14-Variante allein als aktive Implementierungsforderung. Die frühere Formulierung ist in Git und Änderungsübersicht erhalten.

### 2.2 Lenkzeit und Pausen

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG17 | D01/D02 | DL(9:00), freie Verlängerung | Kein Tagesverstoß, kein verbrauchter Verlängerungsvorgang. |
| LG18 | D02 | DL(9:25), bisher 0/1 Verlängerungen | Kein Tagesverstoß; genau ein weiterer Vorgang verbraucht, 35 min bis zulässige 10 h. |
| LG19 | D01/D02 | DL(9:25), bereits zwei Vorgänge in derselben Rechtswoche | 25 min Tageslenküberschuss, Grenze beim kumulierten 9-h-Punkt, Quellen präzise. |
| LG20 | D02 | DL(10:00)/DL(10:01), zulässiger Vorgang | Grenze selbst zulässig; +1 min genau 1 min Überschuss. |
| LG21 | D02 | Drei Tagesperioden mit jeweils DL(9:01) in einer Woche | Dritte Periodenverlängerung unzulässig; kein vierter abstrakter Verstoßstatus. |
| LG22 | D02/S12 | Sonntag/Montag überschreitender Tagesblock; >9-h-Punkt vor/bei/nach UTC-Montag | Vorgang dem fachlich bestätigten Grenzübertritt zuordnen, kein Mitternachtsreset der Tageslenkzeit. |
| LG23 | D03 | Rechtlich isolierter Wochenplan mit 55:59/56:00/56:01 Fahrt | 56:00 zulässig, bei 56:01 genau 1 min Wochenüberschuss. |
| LG24 | D04 | Wochen 45:00,45:00,45:01 in Folge | Paar 1+2 zulässig; Paar 2+3 überschreitet um 1 min. |
| LG25 | D04 | Abgeschlossene Vorwoche 50:00, laufende Woche 39:59/40:00/40:01 | Eindeutige Überschreitung schon im offenen Pair bei >90 h; sonst noch erfüllbares Zeitfenster nicht vorweg verurteilen. |
| LG26 | D03/D04 | Fahrblock über Montag; Quellen je Minute vorhanden | Fahrminuten exakt auf beide Wochen; keine Doppelzählung/Verluste. |
| LG27 | D05 | Gleiche regulierte Fahrt mit belegtem EU-/Drittstaatswechsel | Keine künstliche Zählerlöschung an Ländergrenze. Reise-Rechtsordnung bleibt maßgeblich. |
| LG28 | D06/R04 | Zwei Fahrblöcke durch R(6:59)/R(7:00)/R(8:59) getrennt | Separate Auswirkung der bestätigten Q13-Lenktagkonvention; 7 h weiterhin unzureichende tägliche Ruhe. |
| LG29 | B01 | D(4:30), Datenstichtag genau danach /D(4:31) | Grenze selbst ohne weitere Fahrt noch keine Überschreitung; +1 min 1 min zusätzliche Fahrt. |
| LG30 | B01 | D(4:30), R(0:44)/R(0:45), D(0:01) | 44 min setzen nicht zurück; 45 min setzen zurück. |
| LG31 | B02 | D(2:00),R(0:15),D(2:30),R(0:30),D(0:01) | Zulässige 15+30, kein Pausenverstoß. |
| LG32 | B02 | D(2:00),R(0:30),D(2:30),R(0:15),D(0:01) | Im Standardfall keine gültige Aufteilung; überschießende Fahrt bleibt erkannt. |
| LG33 | B02 | Kleine R(0:10)+R(0:10)+R(0:25) zwischen Fahrten | Nicht allein wegen Summe 45 als gesetzliche Teilung werten. |
| LG34 | B01/B05 | D(2:30),W(1:00),D(2:01); Variante Bereitschaft | Arbeits-/Bereitschaftszeit verlängert kalenderlichen Rahmen, setzt Fahrzähler nicht zurück. |
| LG35 | B03 | Gelegenheitsbus am 21./22.05.2024, 30+15 beziehungsweise 20+25 | Neue flexiblere Zweiteilung erst ab Wirksamkeit; vorher Standardregel. |
| LG36 | B03 | Gelegenheitsbus 15+15 versus 15+30, gleiche Fahrzeit | Summe weiterhin ≥45; 15+15 genügt nicht. |
| LG37 | B04/S11 | Zeitbezogene eigene Karte: Crew,45 min geeignete Beifahrer-Bereitschaft; Varianten belegte andere Arbeit oder unbekannte Folge | Grundsätzlich besondere Pause anhand eigener Aufzeichnung ohne Routinebestätigung/Zweitkarte verwenden. Konkrete Arbeit/Unklarheit nicht als erfüllte Pause behaupten; Nicht-Unterstützung als Rechtsvoraussetzung erläutern, keine Tagesruhe im bewegten Fahrzeug. |
| LG38 | B01/B02 | D(4:31) vor abschließend ausreichender Pause | Bereits entstandene 1-min-Verletzung nicht rückwirkend heilen. |
| LG39 | D01/B01/H02 | Überlappende tägliche Lenkzeit- und Pausenverletzung | Beide Rechtsverstöße erhalten, gemeinsamer Sachverhalt gruppiert; keine Duplikate pro Import. |

### 2.3 Tägliche Ruhe und offene Fenster

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG40 | R01 | 13 h belegte Aktivität, R(11:00) bis t0+24 h | Regelmäßige tägliche Ruhe rechtzeitig erfüllt. |
| LG41 | R01/R02 | 13:01 h Aktivität, anschließend R(11:00), Reduzierung verfügbar /nicht verfügbar | Innerhalb 24 h nur 10:59 h: gegebenenfalls reduzierte Ruhe, sonst 1 min Fehlmenge; späteres Gesamtende macht sie nicht rückwirkend regelmäßig im Fenster. |
| LG42 | R02 | Drei R(9:00) zwischen Wochenruhen, vierte R(9:00) | Erste drei zulässig; vierte unzulässige Reduzierung; keine tägliche Ausgleichsschuld. |
| LG43 | R02 | Reduzierungen Freitag/Sonntag/Montag ohne dazwischen belegte Wochenruhe | Montag setzt Zähler nicht auf null. |
| LG44 | R03 | R(3:00), legale Aktivität, R(9:00) rechtzeitig | Regelmäßige geteilte Ruhe, keine verbrauchte Reduzierung. |
| LG45 | R03 | R(2:59)+R(9:00); R(3:00)+R(8:59); R(9:00)+R(3:00) | Nicht als 3+9-Regelruhe akzeptieren. Gesonderte 9-h-Reduzierung nur, wenn tatsächlich zulässig. |
| LG46 | R01/R04 | Unzureichende Ruhe läuft über 24-h-Grenze und erreicht anschließend 9/11 h | Ursprüngliche Fehlmenge erhalten; folgenden Anker nach Q07 am tatsächlichen ausreichenden Ruheende setzen. |
| LG47 | R04 | Mehrere Tage nur R(6:00), dazwischen Arbeit/Fahrt | Sukzessive Referenzfenster; kein grenzenlos kumulierter fiktiver Rechtstag und keine dauerhafte Entwarnung. |
| LG48 | R05 | Qualifizierte Crew, R(9:00) endet bei t0+30 h /+1 min | Genau 30 h rechtzeitig; danach passender Fehlbestand im Fenster. |
| LG49 | S11/R05 | Eigene Karte vollständig Crew nach erster Stunde; Variante danach Solo/Quellenlücke | Vollständige korrekte Crewchronik genügt ohne Zusatznachweis für30-h-Regel. Solo/unklar zeitbezogen behandeln; ein früheres Crewbit nicht über Lücke übertragen. |
| LG50 | R06 | Gelegenheitsdienst 6×24 h, Tagesfahrt 7:00, Ruheende bei +25 h, erste Nutzung | Neue zeitbezogene Ausnahme bei vollständigen Voraussetzungen zulässig. |
| LG51 | R06 | Gleich, Tagesfahrt 7:01 /Tour nur 5×24 h /zweite Nutzung bei 6 Tagen | Ausnahme nicht nachgewiesen beziehungsweise unzulässig; entsprechendes Standardfenster prüfen. |
| LG52 | R06 | Tour 8×24 h, erste/zwei/drei Nutzungen | Zwei zulässig, dritte nicht; Tourwechsel setzt nur rechtlich tourbezogenen Zähler zurück. |
| LG53 | R06 | Tourdauer oder Sicherheits-/Arbeitszeitvorbehalt unklar | Kein vorbehaltloses Ausnahme-/Compliance-Ergebnis; zeitlicher Normal-/Alternativpfad transparent. |
| LG54 | R07 | R(45:00) beginnt rechtzeitig im täglichen Fenster | Erfüllt zugleich tägliche und Wochenruhe ohne zusätzliche davor verlangte tägliche Ruhe. |
| LG55 | R01/R02 | Noch offene Periode, Datenende vor spätestem möglichen Ruhebeginn | Nur Warnung/Budget; kein Zukunftsverstoß anhand Geräte-Uhr. |
| LG56 | R01/R02 | Nach letztem möglichen Ruhebeginn durchgehend belegte Arbeit, Fenster endgültig nicht mehr erfüllbar | Eindeutige Pflichtverletzung im belegten Umfang; Warn-/Grenzpunkt, Feststellbarkeit und Fehlmenge getrennt ausgeben. |
| LG57 | R01 | Datenlücke könnte ausreichend Ruhe enthalten | Nur Hinweis für die abhängige Ruheprüfung; keine erfundene Ruhe und kein sicherer Ruheverstoß. |

### 2.4 Wochenruhe und Ausgleich

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG58 | W01 | Zwei feste Wochen: 45+45 /45+24 /24+24, normale Güterregel | Erste zwei Konstellationen zulässig; letzte nur bei gesondert belegter Ausnahme. |
| LG59 | W01 | Ruhefolge über drei/vier Wochen, erste Zweierkombination zulässig, folgende nicht | Jedes überlappende Paar prüfen; kein fester Zweiwochenblockfilter. |
| LG60 | W02 | Vorheriges Wochenruheende Montag 08:00 UTC; nächste beginnt Sonntag 08:00 /08:01 | Grenze beim Beginn; +1 min genau 1 min Verspätung. |
| LG61 | W02 | Wochenruhebeginn rechtzeitig, Ende 45 h später | Kein Verspätungsverstoß nur, weil Ende nach 144-h-Grenze liegt. |
| LG62 | W03 | Ein 45-h-Ruheblock Sonntag–Montag, Bedarf beider Wochen | Nur einer Woche zuordnen; keine doppelte Anrechnung. |
| LG63 | W03/W09 | Mehrere Grenzwochenruhen mit zwei legalen Zuordnungen | Deterministische global zulässige Zuordnung ohne künstlich erzeugten Verstoß; Zuordnungsbeleg ausgegeben. |
| LG64 | W04 | Angerechnete reduzierte Ruhe 24:00 /30:00 /44:59 | Schuld 21:00 /15:00 /0:01; passende feste Wochenfrist. |
| LG65 | W04 | Schuld aus Woche W, Ausgleich endet genau Ende W+3 /1 min danach | Rechtzeitig nur bis legaler Frist; Verspätung, Fehlmenge und später erfüllter Ausgleich getrennt. |
| LG66 | W05 | 21-h-Schuld: R(9:00+21:00), versus R(10:00)+später R(11:00) | Zusammenhängender Zusatzblock an ≥9 h erfüllt; zerstückelte 21 h erfüllen nicht automatisch. |
| LG67 | W05/W09 | Derselbe Überschuss von 21 h, zwei Schulden zu je 21 h | Keine doppelte Verrechnung; eine zweite Schuld bleibt offen. |
| LG68 | W05/R02 | R(30:00) mit vorgeschlagener 9+21-Anrechnung, Reduzierungszähler unterschiedlich | Auch tägliche Anrechnung und Reduzierungsbedarf korrekt; Kompensation macht den 9-h-Grundblock nicht 11 h. |
| LG69 | W06 | Zusätzliche 24-h-Ruhe neben notwendigen internationalen Ruhen nach Q08-Beispiel | Kein blindes Erzeugen einer weiteren 21-h-Schuld; notwendige Quelle/Zuordnung erhalten. |
| LG70 | W07 | Vierwochenfenster 45,24,24,45; beide reduzierten Ruhebeginne außerhalb beider relevanter Staaten | Ausnahme nur mit allen zusätzlichen Bedingungen, vier tatsächlichen Ruheperioden und passendem Ausgleich. |
| LG71 | W07 | Derselbe Verlauf, einer der Beginne im Wohnsitzland /Niederlassungsstaat /Land unbekannt | Sondervoraussetzung fehlt oder unklar; keine automatische Fernverkehrsfreigabe. |
| LG72 | W08 | Reduzierungen 24+25; nächster Gesamtblock 21+20+45; regelmäßiger 45-h-Teil beginnt genau 144 h nach letzter Wochenruhe | Zeitlich korrekt; Gesamtruhe muss schon bei +103 h begonnen haben. |
| LG73 | W08 | Derselbe Gesamtblock beginnt erst bei +144 h | Regelmäßiger Wochenruheteil 41 h zu spät; nicht als rechtzeitig werten, nur weil Gesamtblock schon begann. |
| LG74 | W07/W09 | Vierwochenregel mit mindestens vier Ruhen, aber nur einer regelmäßigen | Sonderregel nicht erfüllt; eindeutige Fensterverletzung nur bei ausreichender Abdeckung. |
| LG75 | W01/W07 | Noch offene Zweier-/Vierwochenkonstellation, spätere rechtzeitige Ruhe noch möglich | Verpflichtung/Warnung, kein endgültiger Zukunftsverstoß. |
| LG76 | W10/W13 | Einzelner nationaler Gelegenheitsdienst vor/nach 22.05.2024 | 12-Tage-Erleichterung nicht rückwirkend national zulassen. |
| LG77 | W10 | Qualifizierter 12-Tage-Fall: nächste Ruhe beginnt nach 287:59/288:00/288:01 h | ±1-min-Grenze korrekt; zusätzliche Busbedingungen und Nachruhe separat prüfen. |
| LG78 | W11 | Nachruhe 45+45 versus 45+24; passende Ausgleichsschuld | Zwei regelmäßige oder regelmäßige/reduzierte mit passender Schuld; eine einzelne 45-h-Ruhe reicht nicht. |
| LG79 | W12 | Solo während gesetzlicher Nachtzeit, Fahrabschnitt 2:59/3:00/3:01 h | Unter qualifizierter 12-Tage-Regel 3-h-Grenze, ohne diese kein frei erfundener genereller Nachtlenkgrenzwert. |
| LG80 | W12 | Fehlender technischer Gerätenachweis /Crew während Nacht | Bedingung nicht aus ruhender Karte erraten; erlaubte Alternative mit Crew gesondert nachweisen. |

### 2.5 Fähre/Zug und Artikel 12

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG81 | F01/F02 | Reguläre Tagesruhe mit zwei Unterbrechungen 0:30+0:30, Ruhesumme ≥11 h, Schlafplatz und Transport belegt | Unterbrechungen nicht als Ruhe zählen; bei rechtzeitiger Erfüllung zulässige Anrechnung. |
| LG82 | F02 | Drei Unterbrechungen /zwei mit Summe 1:01 h | Art.-9-Unterbrechungserleichterung nicht erfüllt. |
| LG83 | F02 | GEN1-Fährmarker ohne Ende, Tagesruhe aus T vollständig /unvollständig | Kein künstliches Reiseende; nur belegte Voraussetzungen, sonst Hinweis. |
| LG84 | F03 | Reduzierte tägliche 9-h-Ruhe, Unterbrechung 0:10 | Nicht durch Art. 9 automatisch reparieren. |
| LG85 | F03 | Reduzierte Wochenruhe ≥24 h, zulässige Unterbrechungen, vor/nach 20.08.2020 | Erleichterung nur im richtigen Rechtsstand. |
| LG86 | F04 | Regelmäßige Wochenruhe, planmäßige Reise 7:59 /8:00 h, Schlafkabine verfügbar | 8-h-Voraussetzung strikt; nicht aus Summe aufgezeichneter Ruhe schätzen. |
| LG87 | F01/F04 | EF0522 und Zeitwerte vorhanden; entscheidender Schlafplatz/Fahrplan fehlt, erneuter gleicher Import | Keine gesicherte Anrechnung/Rechtswidrigkeit erfinden; gezielte optionale Frage nur für konkrete Bewertung. Unveränderte vorhandene Antwort wiederverwenden, kein Import-Standarddialog oder allgemeine Unterkunftsprüfung. |
| LG88 | F02/R03 | 3+9-Ruhe mit Fährunterbrechung in gesetzlich passenden Teilblöcken | Split- und Unterbrechungsregeln gemeinsam, nicht beliebige 12-h-Summe akzeptieren. |
| LG89 | X01/X02 | Numerisch belegte Tages-/Wochenlenküberschreitung≤1 h ohne belegte Ausnahmesituation, Tatdatum2026 | Rechnerischer Befund bleibt Liste/PDF, exakter Zusatzhinweis und offene rechtliche Bewertung. Keine automatische Anerkennung, Rechtswidrigkeitsklasse oder tatsächliche Schuld. E07/P02 beschlossen. |
| LG90 | X02/X03 | Überschuss1:00/1:30/2:00 über zulässiger Normalgrenze, tatsächlicher Heimkehrzweck unbekannt | Zahlenbefund plus passende1-/2-h-Erläuterung bleibt Übersicht; nicht als abschließend rechtswidrig/automatisch legal. Kein Zusatzbudget11/12 h und keine tatsächliche Heimkehrprüfung. |
| LG91 | X03 | +2-h-Konstellation, vorher 29/30/45 min geeignete Pause | 30-min-Sondervoraussetzung und normale 45-min-Artikel-7-Pflicht separat. 45 min nicht noch einmal durch weitere 30 min verdoppeln. |
| LG92 | X06 | Spezielle Heimkehrverlängerung bei überschrittenen 90 h oder 6×24 h | Diese spezielle Ausnahme hebt Grenzen nicht auf; allgemeiner Abs. 1 wird nicht mit pauschalem +2 h gleichgesetzt. |
| LG93 | X04 | Kein in der App eingetragener Begründungstext | Kein erzwungenes Formular und kein App-Begründungsverstoß; gesetzlicher externer Nachweishinweis korrekt. |
| LG94 | X05 | Mögliche Art.-12-Nutzung ohne geprüfte außergewöhnliche Tatsachen | Nur bedingten gesetzlichen Ausgleich erklären; weder feste noch offene tatsächlich entstandene Artikel-12-Schuld im Ledger erzeugen. Keine Nutzeranerkennungsfunktion. |
| LG95 | H01 | Jede Klassifizierungsgrenze der aktuellen/historischen Schweregradtabellen | Exakte Inklusivität, Rechtsversion und richtige Tabellenfamilie; unpassende Kategorie auslassen, keine Bußgeldzahl. |
| LG96 | H01 | MSI-Tagesfall mit/ohne qualifizierende besondere Voraussetzung der jeweils geltenden Tabelle | Historische und heutige Tabellenbedingungen nicht vermischen; Fachgoldenfall erforderlich. |
| LG97 | S08/H01 | Konkreter amtlicher Hinweis temporär60-h-Wochengrenze; Fahrt57 h, Unterstützung ungeklärt | Kein unbedingter56-h-Rechtsverstoß oder Standard-Schweregrad. Betroffene Prüfung klar begrenzen oder belegte verfügbare Ausnahme korrekt verwenden; kein vollständiger aktiver Notfallkatalog gefordert. |
| LG98 | R06/H01 | Bus-25-h-Ausnahme mit rechtzeitiger Ruhe | Keine Klassifizierung als unzureichende 24-h-Ruhe aus altem Standardalgorithmus. |

### 2.6 Zusätzliche fachliche Fälle aus E02–E11

Die18 Ergänzungsfälle erweitern die98 ursprünglichen Fälle, ohne sie zu ersetzen. Jede Variante wird als vollständiger unabhängiger Goldenvektor ausgeschrieben.

| Test-ID | Regel | Feste Eingabe / Varianten | Erwartung |
|---|---|---|---|
| LG99 | S13/R01 | Identische9-/11-h-Tätigkeit mit p=1/c=1 bekannter manueller Ruhe gegenüber p=1/c=0 unbekannter Entnahmefolge | Bekannter aufgezeichneter Nachtrag normal anrechnen und Herkunft behalten. Unbekannt nicht als Ruhe, kein sicherer abhängiger Budgetwert/Entwarnung; kein Crewbeweis aus manuellem c-Bit. |
| LG100 | S11/R05/B04 | Vollständige p=0/c=1-Chronik mit passender9 h in30 h und geeigneter45-min-Beifahrerphase; Variante nur p=1/c=1 | Korrekte Crewaufzeichnung ohne Zweitkarte/Bestätigung nutzen; p=1/c=1 dagegen manueller Nachtrag, keine30-h- oder Beifahrer-Pauschalanrechnung. |
| LG101 | W14/R01/D01 | Belegtes früheres Wochenruheende, rechtzeitige R(23:00), danach neuer Fahrblock |23 h geeignete Tagesruhe, neuer Tageslenk-/Tagesruheanker; kein neuer Wochenruheanker/144-h-Reset, Reduzierungszähler zwischen Wochenruhen nicht auf null, keine22-h-Schuld. |
| LG102 | W14/W02/W01 |144-h-Wochenfrist verstrichen; danach weitere Tagesruhen/Importstände, aber keine≥24-h-Wochenruhe | Eine fortbestehende W02-Episode mit aktualisierter belegter Dauer/ID, keine identische Tagesduplizierung. Rechtlich eigenständige abgeschlossene Wochenpaarpflicht separat; Grenzen der Feststellbarkeit bei Lücken beachten. |
| LG103 | W14/W04 | Vergleich zusammenhängender Ruhe23:59/24:00/44:59/45:00 im passenden Pflichtzusammenhang | Unter24 keine Wochenruhe/Reduzierungsschuld;24 bis<45 mögliche reduzierte Pflicht mit exakt45 minus Dauer;45 regelmäßig. Zusätzliche Rast nur nach rechtlich gewählter Pflichtzuordnung. |
| LG104 | W03/W04/W09 | Ruhe startet Sonntag23:00 und endet Montag/ Dienstag; zwei legale Wochenzuordnungen mit unterschiedlicher Ausgleichsfrist | Eine Woche wählen, vollständige Folgefenster/Pflichten gemeinsam prüfen; Frist von gewählter Ursprungswoche ableiten und Auswahl erklären, nicht doppelt buchen. |
| LG105 | W04/W05/W09 | Zwei Schulden14 h und21 h, Fristen verschieden; R(32:00) versus R(46:00) mit11-h-Grundruhe |32 h bietet nur21 Zusatzstunden, nicht beide35 h.46 h kann beide getrennt zusammenhängend ohne doppelte Minute erfüllen, soweit alle Fenster/Anbindungen passen. Exaktes Fristende/+1 min je Schuld separat. |
| LG106 | W07/W08 | Internationale reduzierte24+25; Paket21+20+45 vor144-h-Grenze versus45+21+20 | Beide Schulden vor folgendem regulären Grundblock; erstes Paket bei Beginn+103 h/45-h-Beginn+144 h rechtzeitig, falsche Platzierung nicht. Reihenfolge zwischen Schulden nur nach bestätigtem rechtlichem Zuordnungsbefund. |
| LG107 | W07/W09 | Sechs feste Wochen mit zwei Grenzruhezuordnungen: ein Vierwochenfenster erfüllt, benachbartes nur eine regelmäßige Ruhe | Jedes rollierende Vierwochenfenster und tatsächliche vier Ruhen prüfen; keine Auswahl nur eines günstigen Fensters, kein Verstoß solange zulässige Alternative/Fristerfüllung noch offen. |
| LG108 | W07 | Arbeitgeberland/ Wohnsitzland bekannt, Ruhebeginnland fehlt; erneut gleicher Import, später geänderte entscheidende Angabe | Nur konkrete fehlende Länderfrage; unveränderte Antwort nicht wiederholen. Änderung zeitgebunden neu bewerten/auditieren, keine automatische Heimatstandortprüfung. |
| LG109 | F01–F04 | Zwei Unterbrechungen30+30 min, Netto-Ruhe10:59/11:00 bzw.23:59/24:00 bzw.44:59/45:00; dritte Unterbrechung |60 min selbst zulässig, Unterbrechungen keine Ruhe. Unterschiedliche Grunddauer und regelmäßige Wochenreise≥8 h/Schlafkabine beachten; dritte Unterbrechung nicht reparieren. |
| LG110 | F01/F04 | Reguläre Wochenruhe mit passend langen Zeiten/EF0522, Kabine oder Fahrplan unbekannt /gezielt belegt | Ohne entscheidende Bedingung keine gesicherte Ausnahme oder gesicherte Verletzung erfinden. Bei belegter Bedingung normal neu bewerten; keine Frage zu allgemeinem Lkw/Hotel/Zuhause. |
| LG111 | X02/X03/D01–D03 | Normalgrenze9 oder10 h je Verlängerungszähler beziehungsweise56 h/Woche; Überschuss0/1/60/61/120/121 min |0 kein Befund; positive passende1-/2-h-Fälle numerisch sichtbar/offen. Normalverlängerung zuerst;121 kein Kandidat allein für spezielle≤2 h, kein pauschaler Ausschluss Art.12 Abs.1. |
| LG112 | X06/D01/D03/D04 | Derselbe Zeitraum überschreitet Tages- und Wochenlimit je1:30 h und Doppelwochenlimit | Rechtlich getrennte numerische Befunde, gemeinsame Darstellung ohne doppelte Schuld. Spezieller Art.-12-Hinweis rechtfertigt90-h-Grenze nicht; keine unqualifizierte Summe gesicherter Verstöße. |
| LG113 | X03/X04/X05 | Passender2-h-Zahlenfall, Pause29/30/45 min, kein App-Grundtext, tatsächliche Voraussetzungen nicht geprüft | Zahlenbefund bleibt sichtbar mit offener Bewertung;30-min- und normale45-min-Pflicht korrekt erklären, keine Anerkennung/Begründungspflicht/automatische Schuld. PDF identisch in Aussage. |
| LG114 | W06/W09 | Zusätzliche24–<45-h-Ruhe zwischen bereits erfüllten Wochenpflichten, mindestens zwei alternative Zuordnungen | Nicht jede Rast neue Reduzierung/Schuld. Nur tatsächlich angerechnete Pflichtreduzierungen erzeugen Ledger; globale Lösung mit Quelle und Alternativen erklären. |
| LG115 | W02/W04 | Tatsächlich verspätete Pflicht, spätere ausreichende Ruhe; Variante später importierte damalige rechtzeitige Quelle | Später tatsächlich genommene Ruhe heilt alte Fristverletzung nicht; später belegte damalige Erfüllung darf alte Bewertung mit Audit korrigieren. Keine Unterdrückung unabhängiger Verletzung. |
| LG116 | B03/R06/W10–W13 | Passender Gelegenheitsdienst vor/nach22.05.2024:30+15,25-h-Nutzung,12-Tage-Pfad und minimale Kontextkennung | Historische Voraussetzungen vollständig, keine Rückwirkung. PASSENGERS allein kein Nachweis; keine vollständige Reiseverwaltung/Zweitfahrerabfrage als Voraussetzung. Regeln auch außerhalb Lkw-Beta fachlich testen. |

### 2.7 Datenqualität, Stichtag, Zeitdarstellung und Historie

| Test-ID | Eingabe / Ablauf | Erwartung |
|---|---|---|
| DT01 | Gleiche Aktivitätsquelle zweimal importiert | Ein kanonischer Zeitverlauf, ein Befund; beide Importbelege erhalten. |
| DT02 | GEN1 und GEN2V2 derselben Karte, übereinstimmende Zeiten | Nicht addieren; eine zeitliche Rechenprojektion mit allen Quellen. |
| DT03 | Abweichende GEN1-/GEN2-Aufzeichnung für denselben Zeitraum | Konfliktsegment, kein stiller „jüngster gewinnt“-Ersatz. Unabhängig eindeutig belegte Befunde bleiben möglich. |
| DT04 | Teilweise alter/erneuter Karten-Tagesdatensatz, neuerer enthält echte zusätzliche aufgezeichnete Änderungen | Nur bewiesene kompatible Erweiterung verbinden; alte Quellen/Referenzen bleiben unverändert, widersprüchliche Teile kein Zeitgewinn. |
| DT05 | Eigene Ersatzkarte plus überlappende Historie; fremde Karte abgelehnt | Profilweite eigene Zeitachse ohne Doppelzählung, keine Vermischung fremder Fahrer. |
| DT06 | Fehlender UTC-Kartentag zwischen vorhandenen Tagen | Unbekanntes Intervall; keine Tätigkeit über Lücke fortschreiben, keine Ruhefiktion. |
| DT07 | Neuester Kartentag nach Entnahme, letzte Änderung ohne Endnachweis | Kein Fortsetzen bis 24:00 oder bis Import-Uhrzeit; offener Rest bleibt unklar. |
| DT08 | Alter unveränderter Kartentag lange nach damaliger Entnahme importiert | Importdatum beweist keine neue Tätigkeit oder laufende Tagesdauer; Stichtag quellbezogen. |
| DT09 | Sommerzeit 29.03.2026 /Winterzeit 25.10.2026, Anzeige Berlin | 23-/25-h-Anzeigetag und doppelte/fehlende lokale Stunden korrekt; reale 24-h-Frist unverändert. |
| DT10 | Displayzone/Schichtmodus/Sortierung/28-Tage-Filter ändern | Gleicher Rechtslauf und gleiche Befund-/Schuldwerte; reine Projection. |
| DT11 | Ausgleichsursprung älter als 28 Tage, ausgewählter Bericht jünger | Abhängigkeitshorizont vollständig einbeziehen, nicht auf Anzeigezeitraum kürzen. |
| DT12 | Positiver Tagesbefund bei Lücke in anderer Woche | Datenqualitätsblockade regelbezogen; nicht pauschal alle Ergebnisse löschen oder globale Entwarnung. |
| DT13 | Bekannte Lenkzeit-Untergrenze >Grenze, unklare zwischenliegende mögliche qualifizierende Ruhe | Nur eindeutige Aussage über korrekt begrenzte Regel; kein unzulässiges Addieren über mögliche Ruhe hinweg. |
| DT14 | Korrupte Quellen-/Profil-/Sitzungsreferenz oder Receipt | Ablehnung mit maskierter Diagnose, keine erfundene Ersatzquelle. |
| DT15 | Zeit-/Kontextrevision verändert, Datenrevision/Regelhash gleich | Neuer Lauf plus Vorher/Nachher-Grund im Audit; kein unverbundenes Update. |
| DT16 | Zwei Neuberechnungen, älterer Lauf kommt später zurück | Nur aktuelle Snapshotrevision publizieren; keine veralteten Details/Budgets. |
| DT17 | App-Prozess beendet zwischen Berechnung und Ergebnispublikation | Atomarer bestätigter Lauf oder sichtbar ausstehende Neuberechnung; keine Teil-Ergebnisliste. |
| DT18 | Fehler/Cancellation im Engine-Lauf nach erfolgreichem Import | Import bleibt erhalten, kein Verstoßbefund aus technischem Fehler, keine falsche positive Meldung. |
| DT19 | Erneuter vollständiger Lauf mit exakt gleichem Input | Deterministische Inhalte/IDs, keine Audit-/Neu-Duplikate wegen bloßem Replay. |
| DT20 | Signatur fehlt /gültig /ungültig, Vertrauen NOT_EVALUATED | Mathematischen Status separat übernehmen; keine neue Kryptografie. Ungültigkeit als Quellenproblem; fehlende Signatur nicht pauschal mit unbrauchbarer Tätigkeit gleichsetzen. |
| DT21 | Wochenruhe-Ausgleich über Rechtsversionsgrenze | Ursprungsvorschrift/Frist erhalten und dokumentierte Übergangsregel; kein rückwirkendes Löschen. |
| DT22 | Mehrere Wochen möglicher legaler Wochenruhezuordnung | Solver beendet deterministisch; Ressourcenlimit erzeugt Hinweis, keinen geratenen Verstoß. |
| DT23 | Unbekannte EF-0530-Werte /GEN1 /GEN2-V1 | Kein Güter-Default allein aus fehlendem EF; passendes Einsatzprofil und gezielte Rückfrage. |
| DT24 | Vorhandenes Altarchiv ohne zulässige Profil-/Quellenbindung | Weiter getrennt; keine automatische Zuordnung oder DB-Backfill. |
| DT25 | App-Uhr manipuliert, Offlinebetrieb, Wiederöffnen Tage später | Bewertungsstichtag nicht auf heutiges Datum fortschreiben, keine künstlichen Zukunftsverpflichtungsverstöße. |
| DT26 | Export während Neuberechnung oder mit veraltetem Lauf | Erzeugung gesperrt bis konsistenter aktueller Snapshot, kein widersprüchlicher PDF. |
| DT27 | Stichtag unterscheidet sich je Quellenfamilie | Nur für jeweilige Regel ausreichende gemeinsame Belegabdeckung; globale Frische nicht unterstellen. |
| DT28 | Kontextänderung innerhalb historischer Tages-/Wochenperiode | Rechtsfenster und Einsatzwechsel korrekt, Kontext nicht pauschal auf gesamten alten Import anwenden. |
| DT29 | EF0504 mit p=1/c=1, bekannte manuell am Kontrollgerät aufgezeichnete Arbeit/Ruhe | Tätigkeit als aufgezeichnete manuelle Quelle berücksichtigen; niemals c=1 als Crewbeweis werten. Kein App-Nachtrag und keine Änderung der Originalbytes. |
| DT30 | EF0504 mit p=1/c=0, Aktivitätsbits 00/01/10/11 nach Entnahme | Punktbedeutung nach Appendix beachten; Folgeabschnitt unbekannt, unabhängig vom zufälligen ausgewählten Aktivitätscode keine fortgesetzte Ruhe/Fahrt erfinden. |
| DT31 | Gleich datierter früherer unbekannter Abschnitt und spätere bekannte Kontrollgeräte-Nachtragsquelle | Abgeleitete Neubewertung mit exakten beiden Quellbezügen und Audit; historische erste Sitzung bleibt unverändert, keine falsche Crew-/Ruheanrechnung. |
| DT32 | Bekannte manuelle Tätigkeit im Zeitstrahl/Detail, Kalender-/Schichtwechsel und Zoom | Exakt „Manueller Nachtrag am EG-Kontrollgerät“, klein/unaufdringlich oberhalb Segment/beim Detail. Quelle nicht Warnung/Verstoß; unbekannte Folge erhält keinen falschen Nachtragsbadge. Barrierefreiheitsbeschreibung Deutsch. |
| DT33 | Alle Kombinationen p/c=0/0,0/1,1/0,1/1, Slot-/Aktivitätsvarianten und Entnahmepunkt | Semantik und Grenzen richtig, keine Rohfeldänderung/Fingerprint-Neuberechnung. G1/G2/V2-Vektoren mit gepinnter Appendix-Fassung; p=1/c=1 niemals Crew. |
| DT34 | Automatischer Import mit vollständiger eigener Crewchronik, keine zweite Karte/Nutzerbestätigung | Prüfung/30-h- und Beifahrerregeln ohne neuen Pflichtdialog; offene unbekannte Folge konkret begrenzt, ohne globalen Routine-Zweitnachweis. |
| DT35 | Dieselbe Fähr-/Zugreise erneut importiert, entscheidende Antwort vorhanden; andere neue Reise ohne Antwort | Gleicher gültiger Zusammenhang keine erneute Frage. Neue konkret betroffene Bewertung kann nur fehlende entscheidende Angabe gezielt fragen; kein universeller Importdialog/Schlafplatzdefault. |
| DT36 | Länder-/Buskontext wiederholt unverändert /geändert für konkreten Gültigkeitszeitraum | Antworten wiederverwenden, nur tatsächlich neue Unklarheit fragen. Revision/Audit erhalten; keine pauschale Rückwirkung auf alte Einsätze. |
| DT37 | Fortbestehende Wochenruheepisode über mehrere Tage, Filter/Schichtwechsel und Mehrfachimport | Eine fachliche ID mit exaktem alten Wochenanker, kein Ereignis je Displaytag/Import. Eigenständige andere Regelereignisse nicht verschlucken. |
| DT38 | Mehrere Schulden, Zuordnung erfüllt eine, Neustart/Reimport/neuer Kontext | Erfüllte aus offener Übersicht, historische Herkunft/Frist/Erfüllungsquelle erhalten; andere Schuld offen. Kein Löschen/Verwechseln/doppelte Zusatzruhe; Neubewertung auditierbar. |
| DT39 | R von25.10.2026 00:30 bis09:30 UTC (Berlin02:30 CEST bis10:30 CET); Frühjahrsvariante29.03.2026 gleiche UTC-Zeiten | Reale Dauer jeweils9 h, nicht lokale8/10 h; Anzeige korrekter Offset/DST-Marker. Keine eigene DST-Dauerformel, identische Rechtswerte bei UTC/Berlin/New York. |
| DT40 |1:30-h-Artikel-12-Kandidat in Start/Liste/Detail/Zeitstrahl/PDF mit langem Seitenumbruch | Exakter Überschuss und offen/nicht geprüft gut sichtbar. Kein bestätigter Verstoßgrad, keine pauschale Rechtswidrigkeitsanzahl/Entwarnung; Hinweis bleibt Einzelbefund, PDF vollständiger Text. Kein Statusfilter. |
| DT41 | Konkrete belegte ausgenommene/unsupported/unklare Sonderkonstellation mit OOS; anderer regulierter Zeitraum vollständig | Betroffene Scope-Grenze erklären, keine unberechtigten Befunde/Budgets/Entwarnung. Anderer ausreichend belegter Zeitraum bleibt prüfbar; keine aktive Vollkatalog-/Dialogpflicht. |
| DT42 | Anzeigezone/DST/Schichtansicht ändern bei offenen Pflichten und E07-Befunden | Input-/Regelrevision, IDs, Mengen, Rechtsfenster und Audit identisch; nur Darstellung anders, kein neuer juristischer Lauf/Neu-Status. |
| DT43 | Vollständiger Folgeimport belegt damalige manuelle Ruhe; Variante Quellenkonflikt zur alten Fahrt | Atomare Neubewertung mit alten/neuen exakten Belegen. Nur kompatibler damaliger Nachtrag schließt Lücke; Konflikt sichtbar, Originale/Receipt/Request-/Quellenfingerprint unverändert. |
| DT44 | P03-Präsentationskonfiguration/Übergänge bei bestehender täglicher und Wochenruhe-Frist | Keine verbliebenen24-/2-h-Auslöser oder ersatzweise starren Stunden-/Prozentgrenzen. Technische UI-Ausarbeitung behält Frist-/Frühwarnanzeigen; gesetzliche24-/30-h-Fenster und1-/2-h-Artikel12-Regeln nicht mit gestrichenen UI-Schwellen verwechseln. |
| DT45 | Gleicher Quellenstichtag, App-Uhr/Reopen/Offline/Warten ändern sich, später echter vollständiger Import | Ohne neue Quelle bleiben Warn-/Frist-/Budgetwerte am belegten Stand, kein Live-Countdown/Zukunftsverstoß. Neue belegte Daten dürfen sauber neu bewerten; unveränderte Sicherheits-/Audit-/Recompute-Regeln. |
| DT46 | Beta-Fehlerformular und Vorschau bei ausgeschaltetem Netzwerk, lokales Dateispeichern | Problembeschreibung/Versionsdaten/Formular/Vorschau/Datei offline nutzbar; kein Konto/Backend-/Netzwerkzwang oder Versandauftrag. Dateiinhalt entspricht Vorschau. |
| DT47 | Diagnosedaten vorgeschlagen, Zustimmung abgelehnt/erteilt, Daten nach Vorschau verändert | Ohne ausdrückliche freiwillige Zustimmung keine optionalen Diagnosen. Vorschau exakt tatsächlicher Inhalt; Zustimmung zu geändertem Inhalt nicht aus alter Vorschau ableiten. Bericht ohne optionalen Anhang bleibt möglich. |
| DT48 | Fehlerquelle mit sensiblen Nummern, vollständigen EF-Rohbytes/APDU-Protokollen und ungefiltertem Exceptiontext | Minimierte/maskierte Feldliste; vollständige Rohkarten-/APDU-Inhalte nie im Bericht, auch nach Zustimmung nicht. Keine echte Karte in Fixtures, synthetische Sentinelwerte für Leckprüfung; Schutz-/Herkunftsdaten unverändert. |
| DT49 | Bericht speichern, später bewusst Android-Teilen, Dialog abbrechen, Netz kehrt zurück | Kein automatischer Entwickler-/Drittversand oder Nachsenden. Android-Teilen nur Nutzeraktion mit Content-URI/begrenztem Leserecht; Abbruch respektieren, keine ungefragte Weitergabe. Zielprogrammversand nicht als Offlinefunktion zusichern. |
| DT50 | Schichtmodus mit fehlender/mehrdeutiger Vorgeschichte, Mitternacht/DST und Detailnavigation | Exakt „Schichtzuordnung unklar“; alle vorhandenen Aktivitäten vollständig mit tatsächlichen Zeiten/Dauern in Kalenderansicht. Kein gefälschter Beginn/Intervall/Schichtgrenze oder verlorener Quellenbezug. |
| DT51 | Reine Schichtprojektionskorrektur gegenüber Folgeimport mit wirklich neuen zeitlichen Belegen | Nur Darstellung geändert: gleiche Rechtswerte/IDs/Schulden/Zähler/Neu-Zustände, kein neuer Rechtslauf aus Displayanker. Echte Inputänderung darf atomare gesetzliche Neubewertung mit tatsächlichem Grund auslösen; niemals künstlicher Zählerreset durch neue Gruppierung. |


### 2.8 Produktfachliche Abnahmefälle P03–P05

Diese sieben Fälle ergänzen die116 LG-Regel-/Fachfälle. Es sind festgelegte spätere Prüffälle, keine neue Produktentscheidung oder bereits bestandene UI-Funktion. Sie enthalten positive, negative und unklare Datenlagen sowie die beschriebenen Zustimmungs-/Importwechsel. Vollständige Fixture-/UI-Schrittfolgen und unabhängige Erwartungen werden in M0 vorbereitet; konkrete P03-Darstellung darf M5 innerhalb des beschlossenen Rahmens ausarbeiten, ohne neue starre Auslösegrenzen.

| Fall-ID | Beschluss / Bezüge | Fachliche Eingabe / Ablauf | Verbindliche Erwartung |
|---|---|---|---|
| PF01 | P03, PD12/15/16 | Bevorstehende noch rechtzeitig erfüllbare Tages-/Wochenruhefrist, ausreichend belegter Stichtag; ausgewählte nahe/ferne Darstellungssituationen | Beschlossene Frühwarn-/Fristfunktion bleibt. Zurückhaltender Hinweis klar vor tatsächlicher Überschreitung; keine künstliche Rechtsverletzung, kein vorgeschriebener24-/2-h- oder Prozent-Cutoff. Konkrete UI-Relevanzbeispiele in M5 fachlich prüfen statt neue harte Schwelle voraussetzen. |
| PF02 | P03, PD09/12/51 | Zwei offene Ausgleichspflichten mit Ursprung/Frist vor Fälligkeit; Vergleich belegter Fristüberschreitung und entscheidender Datenlücke | Offene Pflichten/gesetzliche Fristen erhalten und getrennt von festgestellter Verletzung. Bei Lücke keine unsicheren Restbudgets oder falsche Entwarnung; alle Aussagen letzter belegter Datenstand, nicht Smartphone-Uhr. |
| PF03 | P04, PD44/77 | Nutzer beschreibt Fehler, Versionsdaten vorhanden, optionale Diagnosedaten abgelehnt; Netzwerk fehlt | Lokales Formular/Vorschau/Dateispeichern funktionieren. Kein Konto/Backend; Bericht ohne optionale Diagnosen zulässig, keine Rohkarte/APDU oder automatische Übermittlung. |
| PF04 | P04, PD77/79 | Nutzer sieht vollständige Vorschau, stimmt freiwillig optionalen minimierten Diagnosen zu, speichert und wählt später Android-Teilen/Abbruch | Genau dargestellte freigegebene Daten; sensible Inhalte minimiert/maskiert. Bewusste Nutzeraktionen, Abbruch ohne Versand. Zustimmung legitimiert keine verbotenen Rohdaten und keinen automatischen Empfänger. |
| PF05 | P05, PD14/27, GrundsatzE | Schichtbeginn wegen Lücke oder mehreren möglichen Zuordnungen nicht zuverlässig belegbar | Sichtbar exakt „Schichtzuordnung unklar“. Keine erfundenen Tätigkeiten/Schichtgrenzen; tatsächliche vorhandene Aktivitäten vollständig mit unveränderten Zeiten/Dauern im Kalender. Gesetzliche Prüfung und Prüflücken unabhängig. |
| PF06 | P05, PD27/39/56 | Tatsächliche Tätigkeits-/Ruhefolge bestimmt Beginn zuverlässig; gleicher Verlauf als Kalender-/Schichttag über Mitternacht | Belegte Zuordnung zum Schichtbeginn, vollständige Tätigkeiten/Navigation/00:00-Marker; tatsächliche Zeitpunkte, Dauern und Rechtsrechnung unverändert. Kein künstlicher rechtlicher Tagesreset. |
| PF07 | P05, PD47/68, DT43 | Vollständiger Folgeimport behebt früheren Displayanker-Mangel; Vergleich reine Anzeigeänderung und wirklich neue rechtsrelevante Tätigkeit | Belegte Schichtanzeige automatisch neu bestimmbar. Nur Gruppierung korrigiert: keine neue Verletzung/Schuld/Zählerreset. Reale neue Inputdaten dürfen nach bestehendem Auditvertrag tatsächlich neu bewerten, klar von Projektion getrennt. |

## 3. Alle 80 Produktentscheidungen mit prüfbarer Abnahme

Die folgenden PD-Prüfungen sind die vollständige Rückverfolgbarkeit der Produktquelle; sie kommen zu den gesetzlichen Fällen hinzu. „Test/Abnahme“ bedeutet geplanten späteren Nachweis, nicht bereits implementierte Funktion.

| Entscheidung | Test-ID | Geplante überprüfbare Abnahme |
|---|---|---|
| 1 | PD01 | Einsatzprofil mit gültigem Zeitraum, Abweichung und Ausnahme erfassbar; Kontext eindeutig vom Original getrennt. |
| 2 | PD02 | Genau nach atomar erfolgreichem eigenen Import neuer Prüflauf; fehlgeschlagene Gruppe erzeugt keinen Datenlauf. |
| 3 | PD03 | Entscheidende Datenlücke in Hauptübersicht sichtbar, auch bei leerer Verstoßliste. |
| 4 | PD04 | Durch E07 präzisiert: normale rechnerische Überschreitung bleibt Liste/PDF mit Hinweis/offener Rechtsbewertung; keine automatische Rechtfertigung/Pflichttext. P02 geschlossen. |
| 5 | PD05 | Jedes Restbudget mit belegtem Stichtag; Anzeige nicht als Live-Fahrerassistenz. |
| 6 | PD06 | Offene Perioden bis belegter Grenze geprüft, DT07/DT08 keine Zukunftsfortschreibung. |
| 7 | PD07 | Keine App-Tätigkeits-/Ruhe-Nachtragsfunktion; Originale bytegleich. E02: vorhandene bekannte Kontrollgeräte-Nachträge normal rechnen und dezent kennzeichnen. |
| 8 | PD08 | E03 nutzt zeitlich korrekte eigene Crewaufzeichnung ohne Routinebestätigung/Zweitkarte; konkrete unbekannte Sondervoraussetzung getrennt sichtbar. |
| 9 | PD09 | Offene Ausgleichspflichten vor Fälligkeit mit Menge, Ursprung und genauer Frist. |
| 10 | PD10 | Detail enthält Zeitraum, Rechenweg, Rechtsregel, Quellen und passende Überschreitungsgröße. |
| 11 | PD11 | Zulässige täglichen Reduzierungen zwischen Wochenruhen verbraucht/verfügbar korrekt auf Start. |
| 12 | PD12 | P03 bestätigt tägliche Frühwarnung ohne feste2-h-Auslöseschwelle; Warnung klar von belegter Ruheverletzung getrennt. Keine Echtzeitfortschreibung, PF01/PF02/DT44/DT45. |
| 13 | PD13 | 3+9 erkannt und erklärt, LG44/LG45; keine verbrauchte Reduzierung bei zulässiger Regelruhe. |
| 14 | PD14 | Unbekannte Abschnitte und Auswirkungen im Zeitstrahl/detail; DT06. |
| 15 | PD15 | Bevorstehende relevante Wochenruhefrist/-budget am belegten Datenstand; P03 geschlossen, keine feste24-h-Auslöseschwelle. PF01/DT44; gesetzliche Frist unverändert. |
| 16 | PD16 | P03 präzisiert Relevanzdarstellung: alltagstauglich/zurückhaltend, keine starren Stunden-/Prozentgrenzen. Frühwarn-/Fristfunktion erhalten, konkrete technische UI später M5. |
| 17 | PD17 | Reduzierungsdetails mit Fehlbetrag, Frist und zugrunde liegender Ruhe. |
| 18 | PD18 | Automatische Ausgleichszuordnung mit Belegen; keine Mehrfachanrechnung, LG66–LG73. |
| 19 | PD19 | Gezielt nur fehlende entscheidende internationale Voraussetzungen fragen; keine Adresse/Heimradius. |
| 20 | PD20 | Keine allgemeine Hotel-/Lkw-/Zuhause-Wochenruhebewertung; E04/P01 entschieden: nur konkret relevante gesetzliche Fähr-Schlafplatzvoraussetzung gezielt. |
| 21 | PD21 | Pausensplits im Standard-/Busfall automatisch; Hauptansicht zeigt nur Ergebnis. |
| 22 | PD22 | Restfahrt bis Pause nur Detail; nicht Start. |
| 23 | PD23 | Zulässige Tagesverlängerungen verbraucht/verfügbar auf Start; LG17–LG22. |
| 24 | PD24 | Woche und Doppelwoche getrennt, keine einzige irreführende Restzahl; LG23–LG26. |
| 25 | PD25 | 30-h-/Beifahrer-Sonderregeln aus korrekt aufgezeichneter Crewchronik ohne Routine-Nachweisdialog, Voraussetzungen/konkrete Lücken im Detail erklärt. |
| 26 | PD26 | Kompakte Startzusammenfassung mit Zugang zur ganzen Liste. |
| 27 | PD27 | Kalender-/Schichtwechsel verschiebt Gruppierung, nicht Zeit/Ergebnis. P05: belegter Beginn oder „Schichtzuordnung unklar“, vorhandene Tätigkeiten vollständig im Kalender. |
| 28 | PD28 | Typgruppen mit einzeln aufklappbaren Ereignissen und Berechnungen. |
| 29 | PD29 | Warnung optisch zurückhaltender und sprachlich deutlich vom Verstoß getrennt. |
| 30 | PD30 | Standardliste 28 Tage; Archive zugänglich und DT11 voller Berechnungshorizont. |
| 31 | PD31 | Erstmalig importierter zahlenmäßig gesicherter Befund „Neu“, auch E07 mit klar offener Bewertung; keine neue Markierung für bloß geänderten Wert oder reinen Lückenhinweis. |
| 32 | PD32 | Frühere Neubewertung nur Audit; aktuelle Übersicht ohne zusätzlichen prominenten Änderungsbanner. |
| 33 | PD33 | Konfliktsegment sichtbar und erläutert, DT03. |
| 34 | PD34 | Sachverhaltsgruppe erhält alle unterschiedlichen Rechtsverstöße, LG39. |
| 35 | PD35 | Aufklappbare technische Belege; sensible Nummern zunächst maskiert und nach Auswahlwechsel erneut verborgen. |
| 36 | PD36 | Nur rechtlich sichere Zusatzklasse; E07-Fall mit offener Bewertung kein bestätigter rechtlicher Verstoßgrad, fehlende Tabelle keine Ersatzklasse. |
| 37 | PD37 | Genau Zeitraum- und Artfilter, kein Statusfilter. |
| 38 | PD38 | Lesen einzelner Verstoßdetails löscht dessen „Neu“, nicht unbeachtete Nachbarbefunde. |
| 39 | PD39 | Detailnavigation auf exakte Quell-/Zeitspanne, nicht bloß pauschales Tagesdatum. |
| 40 | PD40 | Einstellbare feste Anzeigezone mit Einrichtungsvorschlag, DT09/DT10 Rechtslauf identisch. |
| 41 | PD41 | Unabhängiger namentlich und fachlich qualifizierter Prüfer bestätigt Regelmatrix/Goldenfälle vor Release. |
| 42 | PD42 | Geschlossene Beta mit ungefähr 5–10 Fahrern dokumentiert; kein größerer Testumfang behauptet. |
| 43 | PD43 | Erste Beta Lkw-Fahrer; Busregeln dennoch technisch/fachlich vollständig in Tests, keine behauptete Bus-Hardwarebeta. |
| 44 | PD44 | P04 geschlossen: lokales Offline-Fehlerformular, Problembeschreibung/Versionsdaten, vollständige Vorschau, Datei speichern und bewusst Android-Teilen. Kein Backend/Konto/automatischer Empfänger. |
| 45 | PD45 | Fachliche Testabdeckung bleibt intern; keine öffentliche „alles rechtssicher“-Anzeige. |
| 46 | PD46 | Gesetzesregeländerung im Änderungsprotokoll, nicht nur unspezifische Versionsnummer. |
| 47 | PD47 | Audit enthält Vorher/Nachher sowie Import-/Kontext-/Regeländerungsgrund. |
| 48 | PD48 | Tatsächlich angewendete Tatzeit-Gesetzesfassung im Detail, nicht stets heutige. |
| 49 | PD49 | Datenabdeckung nach Zeitraum und Regelabhängigkeit erkennbar. |
| 50 | PD50 | DT01/DT02/DT04 still zusammengeführt, Quellen erhalten und Ereignisse nicht verdoppelt. |
| 51 | PD51 | Nicht sicher berechenbares Budget verborgen; Hinweis auf fehlende Daten sichtbar. |
| 52 | PD52 | Neueste Ereignisse zuerst, stabile Tie-Breaker für gleiche Zeiten. |
| 53 | PD53 | Typgruppen nach jüngstem tatsächlichem Ereignis, nicht Importzeit sortiert. |
| 54 | PD54 | Gesicherter rechtlicher Grad mit deutschem Begriff/Symbol; für offenes E07-Ereignis keine als Rechtswidrigkeit behauptete Gradkennzeichnung. |
| 55 | PD55 | Konkreter Artikel/Absatz/Fassung; externer Link ist keine Produktpflicht. |
| 56 | PD56 | Gesamte betroffene Zeitspanne hervorgehoben einschließlich relevanter Untersegmente. |
| 57 | PD57 | Neue Befunde im App-Ergebnis, keine neue Push-/Hintergrundalarmberechtigung. |
| 58 | PD58 | Positive Kurzmeldung nur bei ausreichender Abdeckung und ohne relevante offene Bewertung/Lücke; E07-Zahlenbefund nie durch Entwarnung verdecken. |
| 59 | PD59 | Lokaler Verstoß-PDF in 1.0, eigene Berichtsart ohne DDD/Abrechnung/Spesen. |
| 60 | PD60 | Freie Zeitraumwahl, 28-Tage-Default, keine Pflicht-Schnellbuttons. |
| 61 | PD61 | E04: vorhandene EF0522 wiederverwenden, Ruhe netto/Unterbrechungen/Voraussetzungen erklären; gezielte Frage nur bei konkreter Lücke, Antwort wiederverwenden. |
| 62 | PD62 | E07 ersetzt ausschließlich ausgelagerten Hinweis: direkt am Zahlenbefund in Übersicht/PDF/Chronik klar offene Ausnahmebewertung, keine tatsächliche Schuld/Heimkehrprüfung. |
| 63 | PD63 | OOS-Originalmarker plus getrennte rechtliche Einordnung direkt Chronik, LG08. |
| 64 | PD64 | GEN1/GEN2-V1 ohne EF 0530 nutzen Profil; entscheidende Unklarheit gezielt, keine ständig neue Frage. |
| 65 | PD65 | Neue Personenquelle löst einmal passende Linien-/Gelegenheitsfrage aus; fehlende Linienlänge zusätzlich nur wenn relevant. |
| 66 | PD66 | Gemeinsame Datenqualitätsseite mit konkreten Auswirkungen, keine zerstreuten künstlichen Verstoßstatus. |
| 67 | PD67 | Grenzpunkt und ganze Ereignisspanne getrennt exakt markiert; keine überhöhte Sekundenpräzision. |
| 68 | PD68 | Relevanter Kontextedit löst Neubewertung mit nachvollziehbarem Audit aus, DT15/DT28. |
| 69 | PD69 | Während Berechnung alle bisherigen Verstoß-/Budgetansichten und Export ausgeblendet/gesperrt, DT16/DT26. |
| 70 | PD70 | DST-Übergang sichtbar mit korrekten Offsets bei doppelter Stunde, DT09. |
| 71 | PD71 | PDF erfordert neue Zeitraumwahl bei jedem Aufruf, alte Historienwahl nicht still übernehmen. |
| 72 | PD72 | Fahrername standardmäßig aus; nur nach bewusster Aktivierung im Bericht. |
| 73 | PD73 | Datenlücken, Prüfbarkeitsgrenzen und Stichtag auf erster PDF-Seite. |
| 74 | PD74 | Jeder Befund vollständig mit Zeit/Berechnung/Überschuss/Rechtsgrundlage; E07 bleibt Einzelbefund mit sichtbarer offener Bewertung, auch über Seitenwechsel. |
| 75 | PD75 | Lokales Speichern über bewusste Android-Dateiwahl und optionales Teilen mit temporärem Leserecht; kein automatischer Upload. |
| 76 | PD76 | Offlineberechnung aus versioniertem mitgeliefertem Regelpaket; Gesetzesänderung nur reguläres Update. |
| 77 | PD77 | P04: optionaler Diagnoseinhalt nur nach Vorschau/freiwilliger Zustimmung, ohne Zustimmung Bericht ohne Anhang. Keine vollständigen Rohkartendaten/APDU-Protokolle auch mit Zustimmung; DT47/DT48. |
| 78 | PD78 | Je PDF-Befund richtige Gesetzesfassung, auch bei gemischter Historie. |
| 79 | PD79 | Lokale Beta-Diagnoseansicht maskiert; Fehlermeldung erhält nur erlaubte minimierte Diagnosedaten. P04-Vorschau/Offline/Speichern/Teilen, kein Rohdaten-/APDU-Bericht. |
| 80 | PD80 | Alle neuen 1.0-Texte, Fehler, Berichte und Barrierefreiheitsbeschreibungen Deutsch; keine ungeprüften englischen Fallbacks. |

Zusatzgrundsätze A–H erhalten eigene Sammelabnahmen: A→LG17–LG116; B→PD04/29/37/58/62 **mit vorrangigem E07**; C→LG01–LG16; D→DT01–DT08/DT14/DT20/DT23; E→DT09/DT10/PD27/39/40/56/67/70; F→PD07/08/47/68; G→LG07/PD20/59 und Legal-Matrix Abschnitt 8; H→PD41–PD45 und alle Gates des Plans.

### 3.1 Vollständige Zuordnung der verbindlichen Ergänzungen

| Ergänzung | Geplanter Nachweis / betroffene Abnahme |
|---|---|
| E01 | Änderungsübersicht mit allen alten/neuen Planstellen, A01; historischer80er-Quelltext erhalten. |
| E02 | S13, LG99/LG100, DT29–DT33/DT43, PD07/14/35; A04/A17. |
| E03 | S11/B04/R05, LG15/LG37/LG48/LG49/LG100, DT33/DT34; A18. |
| E04 | F01–F05, LG81–LG88/LG109/LG110, DT35, PD20/61; A03/A07/A14. |
| E05 | W07/W08, LG70–LG74/LG106–LG108, DT36/DT38; A09. |
| E06 | W01–W09/W14, LG62–LG69/LG101–LG107/LG114/LG115, DT37/DT38/DT43; A09/A19. |
| E07 | X01–X06/H01, LG89–LG94/LG111–LG113, DT40, PD04/31/36/54/58/62/74; A10/A12/A13/A20. |
| E08 | B03/R06/W10–W13, LG35/LG36/LG50–LG53/LG76–LG80/LG98/LG116, DT23/DT36; A03/A07. |
| E09 | S01–S10, LG01–LG14/LG97, DT41, Anwendbarkeits-/Supportmanifest; A03/A10/A14. |
| E10 | S12, LG16/LG22/LG26, DT09/DT10/DT39/DT42, PD40/70; A08. |
| E11 | Alle116 LG/7 PF/51 DT/80 PD/12 E/5 P und A01–A23; unveränderte technische Gates und unabhängige Prüfung/Beta. |
| E12 | Ausschließlich dokumentierter Draft-PR, unveränderte Code-/Schema-/Parser-/Hardware-/APK-Basis; P-Finalauftrag trennt M0-Fachfreigabe, spätere Programmierungsfreigabe und Releasefreigabe. |

### 3.2 P01–P05 vollständig geschlossen und zugeordnet

| Punkt | Bestätigte Quelle | Fachlicher/technischer Nachweis | Abnahme / Paket |
|---|---|---|---|
| P01 | E04 | LG81–LG88/LG109/LG110, DT35, PD20/61 | A03/A07/A14, M3/M5 |
| P02 | E07 | LG89–LG94/LG111–LG113, DT40, PD04/62 | A10/A12/A20, M2/M5/M6 |
| P03 | Abschließendes AddendumP03 | PF01/PF02, DT44/DT45, PD09/12/15/16/51 | A21, M5; normative Fristen M2–M4 |
| P04 | Abschließendes AddendumP04 | PF03/PF04, DT46–DT49, PD44/77/79 | A14/A22, M6 |
| P05 | Abschließendes AddendumP05 | PF05–PF07, DT50/DT51, PD14/27/39/40/56 | A08/A23, M5; belegter Input M1 |

Alle Prüfungen sind geplant. Keine dieser Produktbestätigungen schließt L01–L09 oder technische Produkt-/Gerätegates; M0 startet erst nach Freigabe und darf keine Programmierung vorwegnehmen.

## 4. Technische Prüfungen und Hardware

Alle bestehenden Tests bleiben aktiv. Neue Tests werden den vorhandenen Gradle-/Instrumentierungsaufrufen hinzugefügt, ohne Filter, Quarantäne oder eigenes leichteres Produktgate.

| Ebene | Pflichtnachweis |
|---|---|
| Domain-Unit | Feste unabhängige Goldenvektoren für alle Regeln, ganze Wochen-/Tourabläufe, historische Fassungen, Datenlücken, Beweismengen und Grenzen. |
| Eigenschaften | Invarianz gegen Importreihenfolge, Duplikate, gleichwertige Segmentaufteilung, reine Anzeigezone/Schichtmodus; stetige belegte Verlängerung verändert nur zulässige abhängige Ergebnisse. |
| Unabhängiger Vergleich | Fachkundig berechnete Fälle und dokumentierter Vergleich mit anerkannter Referenzauswertung, wo verfügbar; Referenzsoftware ist kein Ersatz für Gesetzes-/Fachprüfung. |
| Reader-/Parser-Regressionssuite | Unveränderte 0504-/0522-/0530-Dekodierung, GEN1/GEN2/GEN2V2, Bootstrap-/FK-/Gruppen-/EOF-/Cancellationregeln; keine neuen Kartenschreibbefehle. |
| SQLCipher/Room | Echte additive Migration von Schema 22 auf ersten benötigten Compilerstand, Historie/Originale/Fingerprints identisch, fremde Profilzugriffe abgelehnt, keine Teilresultate, Ausgleich/Audit/Readstate/Reopen/Replay. |
| Compose | Läufe/Neuberechnung/Quellenwechsel/Details/Nachtragsbadge/Crew/E07/Navigation/Zone/Maskierung, P03-Fristhinweise ohne feste Cutoffs, P05-Hinweis und vollständiger Kalender, P04-Vorschau/Zustimmung/Speichern/Teilen; keine falsche Entwarnung oder alte Recompute-Ergebnisanzeige. |
| PDF | Feste Berichtsfälle rendern und visuell/inhaltlich prüfen: leere ausreichende Daten, große Verstoßliste, gemischte Rechtsstände, E07-Kandidaten mit offener Bewertung und sicheren Summen, lange Quellen-/Regeltexte, DST, Datenlücken; erste Seite korrekt, alle Einzelbefunde enthalten. |
| Sicherheit/Diagnose | P04: vollständige Vorschau/freiwillige Diagnosezustimmung, minimierte maskierte Feldliste, keine vollständigen Rohkartendaten/APDU-Protokolle/private Schlüssel im Fehlerbericht auch mit Zustimmung. Keine echten Kartennummern in Fixtures; alle bestehenden Log-/Sicherheitsregeln erhalten. |
| Geräteupdate | Exakter freigegebener Produktkopf, grüne vollständige CI, stabiles signiertes Updatepaar, Update über 40128 ohne Deinstallation/Datenlöschung, neue eigene Bestätigung für betreffenden Baum. |

Geplante eigene Geräteanleitung spätestens mit erstem Import-/Persistenz-/Migrationsproduktpaket: Update prüfen; alte Imports und mathematische Signaturen kontrollieren; automatisch berechneten Datenstand und Regelbefunde prüfen; doppelten Import auslösen; Quellen/Anwendungen/Karte wechseln; Kontext ändern und Neuberechnung beobachten; Lücken-/OOS-/Fährhinweise prüfen, soweit tatsächlich vorhanden; Neustart ohne Leser; Abbruch/Entnahme; Kalender-/Schicht-/Zeitzonenwechsel; PDF mit und ohne Name speichern/teilen. Jeder Punkt wird verständlich direkt im Chat gegeben, nicht nur in einer ZIP.

Keine dieser geplanten Hardwarefällen bestätigt bislang Busbetrieb, fehlende Kartenmodelle oder bestimmte kryptografische Kurven. Beta-Rückmeldungen, Quellenabdeckung und Rechtsprüfung werden getrennt dokumentiert.

## 5. Durchführung und Ergebnisprotokoll

Jeder Testnachweis enthält Eingabehash, fachlichen Erwartungshash, Regelpaket-/Engineversion, endgültigen Git-SHA, Toolchain, Prüfer und echtes Resultat. Nicht gelaufene Fälle bleiben „geplant“ im internen Nachweis, werden nie in eine erfolgreiche Gesamtzahl eingerechnet. Entscheidungen dürfen nicht über Teständerung passend gemacht werden. Jeder fachliche Fehler blockiert den betroffenen Meilenstein; falsche Verstoßmeldung/Entwarnung/Budgets, falsche Ruhe-/Ausgleichszuordnung, Herkunftsverlust/-verwechslung oder fehlende E07-Zahlen-/Rechtstrennung blockieren den Release.
