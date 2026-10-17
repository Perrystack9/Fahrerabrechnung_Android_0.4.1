# Unabhängige Referenzzeitachsen – Rechtsnachprüfung 17.10.2026

**Keine Änderung und kein neuer Lauf von Produkt-/Unit-/Instrumentierungstests.** LR01–LR20 sind neue dokumentierte Rechen-/Prüfvektoren. Sie ändern keine LG-/MI-Nummer, bestehende Sollwerte oder Testzahlen. Rechnung aus Kalender und Normmengen, nicht aus dem Algorithmus abgeleitet. Die rechnerischen Summen/Datumsoperationen wurden separat in einer temporären Standardbibliotheksrechnung kontrolliert.

Zeiten sind UTC, Intervalle links eingeschlossen/rechts ausgeschlossen. **Festes technisches Wochenraster Montag00:00UTC bis Montag00:00UTC, erneut verbindlich bestätigt17.10.2026.** Die Berlin-Gegenrechnung in LR01 dokumentiert ausschließlich die juristische Quellenfrage und schlägt keine alternative Produktberechnung vor. D=Lenken, W=andere belegte Nicht-Ruhe, R=belegte Ruhe, U=unbekannt, A=automatisch aufgezeichnete Crew-Beifahrerbereitschaft. Positive Ruheanfangs/-endbelege und konfliktfreie Herkunft werden nur dort vorausgesetzt, wo ausdrücklich angegeben. Außerhalb ausgeschriebener Episoden wird keine zusätzliche Tätigkeit/Ruhe angenommen. Angegebene Voranker/Schuldledger sind **Eingabevoraussetzungen**, kein aus fehlenden Daten erfundenes Ergebnis.

**Soll** trennt reine Menge, Auslegungsvariante und endgültiges Rechtsurteil. **Ist** bedeutet hier ausschließlich lesender Abgleich von Code und vorhandenen Testaussagen auf M4-HEAD424bd300, nicht neu ausgeführtes Laufzeitergebnis. CI313/Device40132 sind historische technische Belege, keine juristischen Referenzorakel. So bleibt ein ausdrücklich offenes Soll offen, statt einen Testwert zum Gesetz zu erklären.

## LR01 – Wochenkante und erste positive Zusatzfahrt (L02)

Norm: Art.4(i)/(k),6(1), technische UTC-Woche R06; R10 PDF34/37. Beziehung: LG22, bestehende M2-Tests zu Montag und Zwischenwochenruhe.

Eingang: vollständig bekannter Tageslenkabschnitt mit D13.09.2026 10:00–14:30, R14:30–15:15, D15:15–19:45 = genau9h; R19:45 bis zum jeweils genannten Neustart. Keine weitere Fahrt davor/dazwischen. Neubeginn D für genau1min:

| Variante | Erste positive Zusatzfahrt | Berliner historische Anzeige (CEST) | UTC-Woche / Berliner Wochenhypothese |
|---|---|---|---|
| A |13.09.21:59–22:00Z |13.09.23:59–14.09.00:00 | beide noch erste Woche beim Beginn |
| B |13.09.22:00–22:01Z |14.09.00:00–00:01 | UTC erste, Berlin zweite Woche |
| C |13.09.22:01–22:02Z |14.09.00:01–00:02 | UTC erste, Berlin zweite Woche |
| D |14.09.00:00–00:01Z |14.09.02:00–02:01 | beide zweite Woche |

Soll: alle Varianten9h01, genau ein verlängerter Tageslenkvorgang. Erreichen9h um19:45 verbraucht allein keinen Vorgang. Der vorläufige TRACE-Ansatz ordnet die erste positive Zusatzfahrt zu; welche gesetzliche Uhr gilt, bleibt offen. VarianteB beweist reale Abweichung zweier Wochenprojektionen. Eine tatsächliche Zwischenwochenruhe darf die zwei Verlängerungen derselben Rechtswoche nicht freigeben.

Ist: DrivingCalculator/LG22 verwendet erste positive Zusatzfahrt, trägt L02-Wochen-/Verlängerungsrestriktionen. Die obige Berlin-Gegenrechnung ist ein unabhängiger Dokumentationsvektor, kein neu ausgeführter Test. **Kein endgültiges juristisches Wochen-Soll.**

## LR02 – Winterzeitwechsel, gleiche tatsächliche Ruhe (L02)

Episode: W25.10.2026 00:00–00:30Z; R00:30–11:30Z; W11:30–12:00Z. Tatsächliche Grenzen belegt. Berlin: Ruhebeginn02:30CEST, Ende12:30CET.

Soll: UTC-Differenz660min=11h; bloße Subtraktion der lokalen Ziffern ergäbe fälschlich10h. Dauer und rechnerischer11h-Status bleiben bei Anzeige UTC/Berlin identisch; Sommer-/Wintermarkierung ist Darstellung. Keine Aussage zu einem nicht mitgelieferten früheren24h-Anker.

Ist: M1-Intervalle und M2–M4 verwenden Instants; bestehender WeeklyRestReferenceTest „DisplayZoneAndDstDoNotChangeUtcDurationsOrWeekProjections“ hält Anzeigeinvarianz fest. Genau diese neue Zeitachse nicht erneut ausgeführt. Gesetzliche Wochen-/Nachtuhr dadurch weiterhin nicht entschieden.

## LR03 – 8+7+8 mit regelgerechten Zwischenpausen (L03)

Voranker: qualifizierende Wochenruhe endet07.09.2026 00:00Z. Episode:

| Zeitraum | Tätigkeit | Fahrtmenge |
|---|---|---:|
|07.09.00:00–04:00 | D |240min |
|04:00–04:45 | R |0 |
|04:45–08:45 | D |240min |
|08:45–15:45 | R |0 |
|15:45–19:45 | D |240min |
|19:45–20:30 | R |0 |
|20:30–08.09.00:30 | D |240min |

Soll: faktisch960min=16h Fahrt in24h30; Konvention C(2011)3759 trennt zwei480min-Abschnitte bei Beginn/Ende des420min-Ruheblocks. Kein automatisch rechtlicher16h-Lenktag. Die längste kontinuierliche Ruhe im ersten24h-Fenster ist nur7h; bei noch zulässiger Reduzierung fehlen2h zum9h-Mindestwert. Die beiden45min-Pausen vermeiden einen unbeabsichtigten Art.7-Nebenfehler im Referenzaufbau. Keine9-/11h-Tagesruhe durch Konvention erfüllt.

Ist: M2 hat eigenen Konventionsanker; DailyRestReferenceTest „SevenHourDrivingConventionNeverBecomesAM3DailyRestAnchor“ trennt M3. LG28 ist die zugehörige vorhandene Grenzfamilie. Nationale/historische Konventionsanerkennung und das unabhängige vollständige Vollzugs-Soll bleiben L03.

## LR04 – 419/420/539 Minuten sind unterschiedliche Belege (L03)

Vor LR03 zweiter Fahrtteil wird der mittlere R-Block ab07.09.08:45 ersetzt: Ende15:44=419min,15:45=420min,17:44=539min. Danach jeweils D4h, R45min, D4h; Enden08.09.00:29/00:30/02:29. Andere vorherige Abschnitte identisch.

Soll:419min kein7h-Konventionsanker;420/539min Konventionsanker. Alle drei unter540min und damit keine reduzierte tägliche Ruhe allein aus diesem Block. Eine gedachte Minute am Import-/Mitternachtsende wird nicht ergänzt. Bei419min wird nicht automatisch ein endgültiger gesetzlicher16h-Tag behauptet; gerade hierfür ist der nationale Auslegungspfad zu bestätigen.

Ist: bestehende LG28-Tests prüfen419/420/539 getrennt; M2-Konvention und M3-Ruhegrenzen getrennt modelliert. Neue Zeitstempelvarianten nicht ausgeführt.

## LR05 – verspätete vollständige tägliche Ruhe (L03)

Voranker07.09.2026 00:00Z. W00:00–16:00; R16:00–08.09.03:00; W03:00–04:00. Keine Lücke. Reduzierung wäre noch verfügbar.

Soll: im ersten24h-Fenster bis08.09.00:00 liegen8h Ruhe; Defizit1h gegenüber9h. Insgesamt11h R enden tatsächlich erst nach27h; Guidance7 setzt folgenden Bezug auf08.09.03:00. Alte Nichterfüllung wird nicht rückwirkend geheilt. Falls keine Reduzierung mehr zulässig war, anderes Mindest-Soll gesondert11h/3h Defizit.

Ist: LG46 belegt diese27h-/1h-Trennung; LG47 getrennte unzureichende Folgefenster. Qualifizierter/historischer Vollzugsnachweis bleibt offen.

## LR06 – drei übergreifende Ruheblöcke, acht Rollenpfade (L04)

Bekannte physische R-Blöcke, jeweils45h: A06.09.2026 23:00–08.09.20:00; B13.09.23:00–15.09.20:00; C20.09.23:00–22.09.20:00. Andere Zeiten sind nicht Teil dieses Rollentests. Bedingtes UTC-Kalenderraster: A kann W36/37, B W37/38, C W38/39 zugeordnet werden.

Soll unter allein diesen drei Grundblöcken:2×2×2=8 Rollenpfade. Für die zwei geschlossenen Prüffenster W37+38 und W38+39 genügen genau die drei Pfade (36,38,38), (37,38,38), (37,38,39). Eine einzelne Ruhe zählt je Pfad nur einer Woche. Zwei Ruheperioden derselben Woche können im relevanten Zweiwochenpaar zusammen beitragen. Für andere Randpaare fehlen hier Daten; daraus keine globale Entwarnung.

Ist: LG63 sucht gemeinsame Fensterzeugen, statt einen lokalen ersten Pfad festzuschreiben; L02/L04-Einschränkungen bleiben. Ob sämtliche technisch erwogenen Rollen normativ zulässig sind, benötigt weiter externes Gesamt-Orakel.

## LR07 – Paketende eine Minute spät ist noch kein Gesamtbeweis (L04)

Eingangsschuld: belegte erforderliche24h-Reduzierung in bedingter WocheW37,21h Ausgleich fällig spätestens05.10.2026 00:00Z. Physische R-Episode03.10.16:01–05.10.00:01Z=32h, links/rechts angrenzende Nicht-Ruhe belegt.

| Arithmetische Zuordnung | Grundruhe11h | Ausgleich21h | Fristbefund dieses Pfads |
|---|---|---|---|
| Suffix |03.10.16:01–04.10.03:01 |04.10.03:01–05.10.00:01 |1min nach Frist |
| Präfix |04.10.13:01–05.10.00:01 |03.10.16:01–04.10.13:01 |Ausgleich10h59 vor Frist abgeschlossen |

Soll: beide Intervalldifferenzen sind exakt; keine gemeinsame Minute. Der Suffixbefund ist nicht Beweis, dass jede rechtlich zulässige Anrechnung zu spät ist. Die Präfixvariante ist **noch kein bestätigter Gesamtzeuge**: Lage der Tagesruhe im vorherigen24h-Fenster und rechtliche Anrechenbarkeit müssen gemeinsam geklärt werden. Das fehlende Gesamt-Orakel wird nicht durch eine passende Teilrechnung ersetzt. Exakter Fristfall: identischer32h-Block eine Minute früher, Ende05.10.00:00, Suffix rechtzeitig.

Ist: CompensationAllocator prüft unterschiedliche Lagen und hält L04; vorhandene LG65-Familie trennt Pfadmenge von globalem Rechtsurteil. Keine neue Ausführung; rechtliche Lage-/Tageskreditfrage offen.

## LR08 – zwei Schulden, jede Minute nur einmal (L04)

Vorab belegtes gewöhnliches Ledger:31h reduzierte Wochenruhe27.08.2026 00:00–28.08.07:00 (W35,14h Schuld, Frist21.09.00:00Z); reguläre45h04.09.00:00–05.09.21:00 dazwischen;24h10.09.00:00–11.09.00:00 (W37,21h Schuld, Frist05.10.00:00Z). Die beiden Reduzierungen sind nicht die internationale Doppelreduzierung unmittelbar aufeinanderfolgender Wochen. Rollen und weitere Mindestpflichten für diesen Rechenvektor extern als gegeben vorausgesetzt.

Rückzahlungsruhe A19.09.00:00–20.09.08:00=32h. Mit11h Grundruhe bleiben21h:14h oder21h passen,35h zusammen nicht. Beispiel A1: Grund00:00–11:00 am19.; Schuld21h19.11:00–20.08:00. Beispiel A2: Grund19.00:00–11:00; Schuld14h19.11:00–20.01:00, danach7h unreserviert.

Variante B R19.09.00:00–20.09.22:00=46h.11h Grund +14h+21h=46h. Zwei disjunkte Reihenfolgen nach Grundende19.11:00:14h bis20.01:00,21h bis20.22:00; oder21h bis20.08:00,14h bis20.22:00. Beide vor beiden oben genannten Fristen. Es entsteht keine gesetzliche FIFO-Pflicht allein aus der Chronologie der Schuld.

Soll: keine Doppelverwendung, kein Aufteilen einer Schuld in Stücke.30h Gesamt minus21h Ausgleich sind nur9h, nicht pauschal11h täglicher Grundrest. Rechtliche Tageskredit-/Wochenrollen bleiben gemeinsame L04-Frage.

Ist: getrennte Reservierungen/Alternativen im CompensationAllocator; LG66–LG68/LG105 als verwandte Bestandsfälle. Kein neues Laufzeitergebnis oder universelles rechtliches Orakel.

## LR09 – zusätzliche reduzierte Rast im internationalen Vierwochenfall (L04)

Bedingtes Wochenraster, historisch aktuelle Ausnahme und Länderqualifikation DE-Niederlassung/DE-Wohnsitz gegeben. Erforderliche Ruhebeginne im Ausland; Grenzen jeweils belegt:

| Physische Ruhe | UTC-Intervall | Menge / angenommene Wochenrolle |
|---|---|---|
| A |05.09.2026 11:00–07.09.08:00 |45h, W36 |
| B |13.09.08:00–14.09.08:00, FR |24h, W37 |
| X |16.09.08:00–17.09.08:00 |24h zusätzliche Rast |
| C |20.09.05:00–21.09.08:00, ES |27h, W38 |
| D |25.09.17:00–29.09.05:00 |84h=21+18+45, regulärer Grundblock27.09.08:00–29.09.05:00, RolleW39 |

Sollmenge:21h ausB und18h ausC; keine automatische zusätzliche21h ausX. Das isolierte Mengen-/Rollenszenario konkretisiert den amtlichen Frage7-Hinweis, es bestätigt nicht alle ausgelassenen Tagesruhe-/Fahrtfenster. Der Auslegungshinweis zur Fristnähe muss bei der Auswahl zwischenX undC beantwortet werden. **Offenes Gesamt-Soll:** Welche technischen Alternativen des Solvers sind im genauen Q&A-Sachverhalt noch rechtlich zulässig? Nicht durch generische Schuldminimierung ersetzen.

Ist: LG69/LG114 behandeln Zusatzruhen und mögliche Pflichtrollen; L04-Rollenrestriktion schützt vor endgültiger Behauptung. Ein fallgenauer unabhängiger Vergleich mit diesem vollständigen Rollen-/Länderinput steht aus. Kein nachgewiesener Runtime-Bug aus bloßer Suchraumweite.

## LR10 – 23h und ein unbekannter linker24h-Rand (L04/L08)

Variante A: W06.09.2026 23:00–07.09.00:00; R07.09.00:00–23:00; W23:00–08.09.00:00. Grenzen belegt. Soll:23h kann tägliche Ruhe sein, ist keine≥24h-Wochenruhe und erzeugt keinen22h-Ausgleich. Wochenruhefrist/Zähler nicht automatisch zurücksetzen; fortbestehende Fristverletzung nicht täglich identisch neu erzeugen.

Variante B: U bis07.09.00:00, sichtbarer R07.09.00:00–08.09.00:00, danach W00:00–01:00. Soll: mindestens24h sichtbar, tatsächlicher Beginn unbekannt, tatsächliche Gesamtruhe könnte≥45h sein. **Keine sichere21h-Schuld**, keine unbedingte reduzierte Wochenrolle.

Ist: LG101–LG103 sowie WeeklyGuardRegressionTest zum offenen linken Rand enthalten entsprechende Guards. Reines erneut auftretendes Ruhesymbol/Mitternacht ist kein Endbeleg. Keine neue Testausführung.

## LR11 – 21+20+45 vor144h, geordnete Spätvariante (L04)

Vorherige reguläre Ruhe05.09.11:00–07.09.08:00. Zwei belegte RED:12.09.08:00–13.09.08:00=24h und19.09.07:00–20.09.08:00=25h; beide Ausland gegenüber Arbeitgeber-/Wohnsitzland. RollenW37/W38, übrige rollierende Vierwochenvoraussetzungen sind eigens zu bestätigen.

Letztes tatsächliches Wochenruheende20.09.08:00.144h-Frist26.09.08:00. Pflichtpaket pünktlich:

- Ausgleich21h24.09.15:00–25.09.12:00.
- Ausgleich20h25.09.12:00–26.09.08:00.
- Regelmäßige Wochenruhe45h26.09.08:00–28.09.05:00.

Soll:86h insgesamt; Start103h nach Vorende;45h-Grundbeginn exakt144h. Keine weitere11h pauschal vor das gesamte Paket addieren, Tagesfenster trotzdem prüfen.

Spätvariante desselben **geordneten** Pfads: Paketbeginn26.09.08:00;21h bis27.09.05:00,20h bis28.09.01:00,45h bis29.09.22:00. Grundbeginn185h nach Vorende, Differenz41h=2460min. Dadurch kann sich auch die gesetzliche Wochenrolle ändern. Nicht als einzig mögliche gesicherte Gesamtbewertung ausgeben.

Ist: LG70–LG75/LG106–LG108; M4 hält alternative Ledger/Restriktionen und bezeichnet LG73s2460min als Variantenbefund. Quelle für Mengen/Reihenfolge Art.8(6)/(6b); gemeinsame gesetzliche Pfad-/Fristabnahme weiter L04.

## LR12 – Bus-Nachruhe und Fristursprung (L04/L09)

Belegte regelmäßige Vorruhe05.09.2026 03:00–07.09.00:00. Qualifizierter einzelner Gelegenheitsdienst und alle historischen/technischen Voraussetzungen als Kontext gegeben.12×24h endet19.09.00:00.

Variante A Nachruhe19.09.00:00–22.09.18:00=90h. Arithmetisch zwei45h-Grundperioden. Variante B19.09.00:00–21.09.21:00=69h:45h bis20.09.21:00 plus24h bis21.09.21:00,21h Kompensation verbleiben. Im bedingten UTC-Raster Ende Ausnahme W38, Ende dritter Folgewoche12.10.00:00; fälschliche Anknüpfung ans Ende Nachruhe W39 ergäbe19.10.00:00.

Soll: Fristursprung ist Art.8(6a)(b) folgend das Ende des Ausnahmezeitraums. Ob exakt diese interne Teilung eines ununterbrochenen physischen69-/90h-Blocks alle rechtlichen Rollen trägt, bleibt L04. Nicht künstlich zwei aufgezeichnete physische Enden behaupten.

Nachtvariante: belegte Fahrt18.09.22:00–19.09.01:00=180min, danach volle45min Pause. Bei einschlägiger rechtlicher Nachtzeit und ohne Crew genau3h noch keine Überschreitung; Fahrt bis01:01 ergibt1min. Die Annahme, dass UTC-Uhr zugleich die gesetzliche Nachtuhr sei, ist gerade **nicht** bestätigt. Tageslenk-/90h-/Wochenrollen zusätzlich prüfen.

Ist: BusWeeklyReferenceTest LG76–LG80; Nachtbezug L02, Langruhenteilung L04, Crewpausen L09 bleiben getrennt. Kein neu ausgeführtes Ergebnis.

## LR13 – tatsächliche15+30 gegenüber kurzer automatischer Bereitschaft (L09)

Episode07.09.2026: D00:00–02:00=120min; A02:00–02:15=15; D02:15–04:45=150; A04:45–05:15=30; D05:15–05:16=1. Crew/Beifahrerslot nur für die betreffenden A-Intervalle auf eigener Karte belegt; keine Hilfsarbeit ist ein gesondert erforderlicher rechtlicher Umstand, keine neue Routinefrage.

Soll: mit qualifizierender15+30-Unterbrechung hat letzte Fahrt1min im neuen Abschnitt; ohne qualifizierende A-Anrechnung kumuliert271min, rechnerisch1min über270. Automatische kurze A-Abschnitte beweisen die qualifizierende Pause noch nicht abschließend. Kein gesichertes Budget oder Verstoß nur durch Wahl der ungünstigen/günstigen Auslegung.

Gegenprobe: identische Zeitachse, aber ausdrücklich aufgezeichnete tatsächliche R stattA; gewöhnliche15+30-Teilung erfüllt. Eine qualifizierende ununterbrochene45min-Crewpause ist ein anderer, amtlich dokumentierter Belegfall.

Ist: DrivingReferenceTest „partialCrewAvailabilitySplitIsConditionalAndAProvenFullBreakRestoresCertainty“ und „actualRecordedRestSplitInCrewNeedsNoAdditionalLegalOrUserConfirmation“. Keine zweite Karte/Nutzerbestätigung implementiert. Neues externes Vollzugs-Soll für kurze Automatik bleibt L09.

## LR14 – vier9h-Crewruhen, Grenze zum gesicherten Budget (L09)

Qualifizierender Wochenvoranker07.09.2026 00:00; folgende30h-Episoden sind lückenlos und tatsächlich Crew-belegt. Nicht-Ruheblöcke sind ausdrücklich nicht als21h eigene Fahrt zu lesen:

| Folge | belegte Nicht-Ruhe | tatsächliche R9h |
|---|---|---|
|1 |07.09.00:00–21:00 |07.09.21:00–08.09.06:00 |
|2 |08.09.06:00–09.09.03:00 |09.09.03:00–12:00 |
|3 |09.09.12:00–10.09.09:00 |10.09.09:00–18:00 |
|4 |10.09.18:00–11.09.15:00 |11.09.15:00–12.09.00:00 |

Soll: jede einzelne9h-Ruhe liegt im zugehörigen30h-Fenster. **Offen:** Behandlung aller9h-Crewruhen unter Art.8(4)/(5), insbesondere viertes Ereignis und folgender Solobetrieb. R22 ist konkrete britische Gegenposition zum unbegrenzten Verbrauch, keine EU-Gesamtklärung.

Variante4-regulär: letzter Nicht-Ruheblock endet bereits11.09.13:00, dann R11h bis12.09.00:00. Vollständige11h im30h-Fenster benötigt die umstrittene9h-Reduzierungsoption nicht.

Ist: RestGuardRegressionTest „AnUnresolvedFourthCrewReductionCannotGrantAnAssuredNineHourWindowOrBudget“; Ober-/Untergrenze bleiben, kein sicheres viertes9h-Budget. Kein neuer Test und keine juristische Schließung.

## LR15 – Fähre: Netto24/45h, zwei Unterbrechungen60/61min (L09/L04)

Belegter Beginn/Ende, Fahrtkontext und zulässiger Schlafplatz vorausgesetzt. Episode:

| Zeit | Aktivität |
|---|---|
|07.09.2026 00:00–12:00 | R12h |
|12:00–12:30 | W30min, Unterbrechung1 |
|12:30–18:30 | R6h |
|18:30–19:00 | W30min, Unterbrechung2 |
|19:00–08.09.01:00 | R6h |

Soll: physisch25h, Netto-Ruhe24h, zwei Unterbrechungen gesamt60min. Nicht25h Ruhe buchen. Grenzvariante Unterbrechung2 bis19:01, letzter R-Block19:01–08.09.01:01: weiterhin24h netto, aber61min Unterbrechung, Art.9-Summenvoraussetzung nicht erfüllt.

45h-Netto-Variante: mittlerer R-Block12:30–08.09.15:30=27h, Unterbrechung2 am08.09.15:30–16:00, Schluss-R16:00–22:00=6h; physisch46h, netto45h. Für regelmäßige Wochenruhe zusätzlich **planmäßige** Reise≥8h und Schlafkabine belegen. Eine tatsächliche Karte mit EF0522 allein ersetzt diese Fakten nicht. Vor20.08.2020 keine heutige Wochenruhe-Ausnahme anwenden.

Ist: RestCandidatesTest LG81/LG85/LG87/LG109 und M4-Weiterverwendung prüfen Netto-/Voraussetzungsgrenzen; fehlende Fakten bedingt. Keine sichere Ausgleichsreservierung über die Unterbrechung. Gemeinsame Ausgleichs-/Wochen-/Tagesrolle weiterhin L04, kein pauschales Gesamt-OK.

## LR16 – Bus25h und30+15 ohne falschen Ruheverstoß (L09/L01)

Qualifizierter einzelner6×24h-Dienst, eine25h-Nutzung noch verfügbar, aktueller Rechtsstand und übrige Sicherheitsvoraussetzungen gegeben; Vorruheende07.09.2026 00:00.

D00:00–03:30; R03:30–04:15; D04:15–07:45; W07:45–14:00; R14:00–08.09.01:00. Fahrt7h, Tagesruhe11h bis25h-Grenze. Im gewöhnlichen24h-Fenster liegen10h Ruhe.

Soll: bei belegter25h-Ausnahme11h regelmäßig. Ohne diese Ausnahme sind10h im24h-Fenster **noch eine mögliche zulässige Reduzierung**, nicht automatisch1h Verstoß. Falls bereits drei Reduzierungen verbraucht, anderes gewöhnliches Mindest-Soll. Variante7h01: D zusätzlich07:45–07:46, W erst07:46–14:00;25h-Privileg fällt weg, gewöhnliche Ruheprüfung weiterhin separat. Bei8×24h qualifizierten Diensten maximal zwei Nutzungen, keine Wochenzählung daraus.

Pausenvariante am07.09.: D00–02; R02–02:30; D02:30–05:00; R05:00–05:15; D05:15–05:16. Fahrtmenge270min vor letztem D. Unter belegtem Gelegenheitsbusrecht ab22.05.2024 qualifiziert30+15; unter gewöhnlicher15+30-Regel allein nicht. Identische Variante mit Datum21.05.2024 nicht durch späteren Import privilegieren.

Ist: M2 LG30–LG36 und M3 RestSpecialReferenceTest LG50/LG51 sowie historische Phasengrenze; Rechtszeit und tatsächliche Dienstdauer/Nutzung bleiben Voraussetzung. Keine neue Dienstverwaltungsfunktion oder Laufzeitprüfung.

## LR17 – Entnahme, Nachtrag, Gleichminute und Endbeleg (L08)

EpisodeA: Karte07.09.2026 08:00 entnommen; Rohwort p=1/c=0/aa=Ruhe, Wiederstecken09:00. Soll:08–09 U, nicht automatisch R. EpisodeB als getrenntes späteres Quellenoriginal: p=1/c=1/aa=Ruhe08:00 und nächster belegter Tätigkeitspunkt09:00. Soll: bekannter am Kontrollgerät eingetragener Ruhe-Nachtrag, nicht Crew. Keine eigene manuelle App-Ergänzung; beide Importoriginale bleiben unverändert. Die zusammengeführte Quelle benötigt den bestehenden Konflikt-/Herkunftsvertrag, keine freie Überschreibung.

Gleichminute: zwei widersprechende Zustände um09:00 ohne eindeutig belegbaren Folgezustand; positiver Folgezustand um09:15. Keine doppelte Minute oder willkürliche Reihenfolge als Tätigkeit; Unklarheit ausdrücklich tragen. Letzter R-Punkt10:00 ohne tatsächlichen Nicht-Ruhe-Folgebeginn: kein erfundenes physisches Ruheende10:00/Importzeit/Mitternacht. Ein weiterer R-Marker um11:00 ist kein Endbeleg.

Ist: InputSnapshotBuilder/ActivityIntervals, MI-Familien und die M4-Randguards bleiben lesend nachvollziehbar; G1/G2-Deduplizierung behält Herkunft. Keine neue Aussage, jeder reale Kartenkonflikt lasse sich automatisch rechtlich lösen. L08-Gesamt-/Sicherheitsprüfung unverändert.

## LR18 – minimale Anwendbarkeit statt Kartenschluss (L05)

Explizite Faktenepisode07.09.2026 00:00–01:00Z: gewerblicher EU-Gütertransport, Straße,4t zulässige Masse, keine konkret einschlägige Ausnahme. Soll: gewöhnlicher EU-Grundpfad. Vergleichsparameter: internationale Masse2500kg gegenüber2501kg und3500kg; für den neuen aa-Pfad gilt Datum ab01.07.2026 und Eigenverkehr/Haupttätigkeit beachten. Bei fehlender rechtlicher Kalenderreferenz Übergang ungeklärt.

Linie50km gegenüber50,1km bei geeigneter Personenkapazität: Art.3(a) bis einschließlich50km. Nichtanwendbarkeit561 ist keine Zusage, dass keine FPersV-/sonstigen Pflichten gelten. AETR-Mischreise bzw. ungeklärter nationaler/temporärer Ausnahmepfad bleibt gesondert Unsupported/Unknown.

Ist: ScopeGuard/ScopeContextAcceptanceTest erhalten diese Minimalgrenzen. Keine umfassenden neuen Sonderdialoge; vorhandene Out-of-Scope-Kennung allein kein Rechtsnachweis.

## LR19 – +90min und möglicher Art.12-Fall (L06)

Vorab nur **rechnerisch zulässige normale10h-Grenze** für diesen Vektor angenommen; deren Wochenzuordnung bleibt separat L02.07.09.2026: D00–04:30; R04:30–05:15; D05:15–09:45; R09:45–10:30; D10:30–11:30; R11:30–12:00; D12:00–13:30. Summe690min,90min über600.30min unmittelbar vor Zusatzfahrt vorhanden, übrige4h30-Pause nicht verletzt.

Soll: zahlenmäßig passende besondere2h-Konstellation; tatsächliche außergewöhnliche Umstände, Sicherheitslage, Rückkehrziel, dortige regelmäßige Wochenruhe und Dokumentation nicht belegt. Deshalb weder automatische Rechtfertigung noch abschließend gesicherte Rechtswidrigkeit, weder tatsächliche90min-Art.12-Schuld noch automatische Standort-/Begründungsfrage. Fehlende30min würde eine Voraussetzung dieser besonderen2h-Variante ausschließen, aber nicht jeden allgemeinen Art.12-Sachverhalt abschließend entscheiden.90h/144h werden durch besondere Rückkehrabsätze nicht erweitert.

Ist: bestehende M2-Art.12-Kandidaten und LegalAssessment.NOT_DETERMINED; M4 kann daraus keine neue echte Art.12-Schuld erzeugen. Spätere Übersicht/PDF noch nicht freigegeben; E07 bleibt.

## LR20 – Klassifikationsgrenzen, keine Geldbeträge (L07)

Eigenständige Zahlenvektoren nach R11 AnhangIII, **nur unter bestätigtem Grundtatbestand**, passende aktuelle Umsetzung nicht unterstellt:

- Keine10h-Option: Tagesfahrt539/540/541/599/600/659/660min → keine/keine/MI/MI/SI/SI/VSI; MSI-Zelle ab810min gesondert prüfen.
- Fahrt ohne notwendige Pause270/271/299/300/359/360min → keine/MI/MI/SI/SI/VSI.
- Bus-Nachruhe nach69h-Mindestalternative:4079/4080/4140min (67h59/68h/69h) → MI/MI/keine;4020min (67h) SI,3900min (65h) VSI. Gesetzliche Rollen-/Langruhenteilung aus L04 zuvor bestätigen.

Ist: keine Schweregrad-/Bußgeldfunktion im freigegebenen M4-Auftrag. Dies ist Quellen-/Grenzdokumentation, keine neue Testklasse, keine nationale Umsetzungsbescheinigung. Historische MSI-Zusatzbedingungen, Unternehmer-Anhang und heutige Tabelle nicht vermischen.

## Offene Soll-/Ist-Abnahmen

Rechenmengen in diesen Vektoren sind unabhängig kontrollierbar. Für LR01/LR06–LR09/LR11–LR15 fehlt teilweise das **qualifizierte normative Gesamt-Orakel**; genau deshalb werden keine erfundenen abschließenden Sollurteile eingetragen. Für neue hier beschriebene Varianten fehlt eine neu ausgeführte Runtime-Ist-Spalte, weil Code-/Teständerungen und Testläufe nicht Teil dieses Auftrags sind. Bestehende Testquellen wurden lesend verglichen, frühere Test-/CI-Nachweise nicht verändert. Eine spätere explizite Freigabe muss sowohl fachliches Orakel als auch anschließend passende technische Regressionen abdecken.
