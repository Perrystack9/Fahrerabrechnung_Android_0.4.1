# M0 – unabhängig hergeleitete gesetzliche Referenzfälle

Stand 15.10.2026. Autorenseitige Referenzrechnung, keine ausgeführten Engine-Tests und **noch keine unabhängige qualifizierte Fachabnahme**. „Unabhängig“ bedeutet: Sollwerte aus Gesetzestext, angegebenen Eingangsdaten und einfacher Zeit-/Mengenrechnung hergeleitet, nicht aus einer zu prüfenden Engine. Kein Rechtsalgorithmus oder Produktcode wurde implementiert. Verantwortlicher Fachprüfer/Prüfdatum noch offen. [Quellenregister](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md), [Datenvertrag](VIOLATION_ENGINE_M1_INPUT_CONTRACT.md), [Daten-/Produktszenarien](VIOLATION_ENGINE_M0_DATA_CASES.md), [ursprüngliche Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md).

## 1. Präziser Fixturevertrag

Alle Intervalle halboffen, alle hier ausgeschriebenen Zeitpunkte UTC. Standard `t=2026-09-07T00:00:00Z`, Montag. Dauerangaben in Minuten; `h:mm` ist eine Dauer, keine Uhrzeit. `D/W/A/R/U` bedeuten Fahren/Arbeit/Bereitschaft/aufgezeichnete Ruhe/unbekannt. `SEQ(t;...)` legt Segmente lückenlos nacheinander ab t; Endpunkte entstehen ausschließlich durch Addition der angegebenen Dauern. U bleibt U. Kein implizites Auffüllen mit Ruhe; außerhalb des ausdrücklich angegebenen Eingangs steht keine Aussage.

Standardquelle jeder Beobachtung: synthetisches Profil `P1`, eigene physische Karte `C1`, GEN2-Anwendung V2, Parent `B1`, Child/Import `I1`, EF0504, Quelltag UTC, fortlaufender Record-/Wechselindex, unveränderte synthetische Originalreferenz `O1`, Receiptbezug `E1`. Eingesetzte Karte p=0, c=0, SlotDRIVER; Abweichungen ausdrücklich genannt. Ein letzter Endpunkt wird durch eine eigene passende Folgebeobachtung getragen; der Endmarker trägt selbst keine erfundene Dauer. EF0522/0530 und Kontextbelege erhalten eigene Familienreferenzen, nie EF0504-Identität. Kein realer Name, keine echte Kartennummer/Rohkarte/APDU.

Standardkontext `K1`: regulierte entgeltliche EU-Güterbeförderung,40.000 kg inklusive Anhänger, kein bekannter konkreter Ausnahmefall, keine Fähre, Solo. Die Zeitkonvention der **Referenzprojektion** ist Montag 00:00 Z bis Montag 00:00 Z. Dies ist ein expliziter Prüfparameter, **keine Erledigung von L02 oder heimliche Produktfestlegung**. Solange L02 nicht bestätigt ist, sind kalenderabhängige rechtliche Erwartungen bedingt; die Minutenarithmetik ist bestimmt. Historische Fälle ersetzen ausdrücklich Datum/Regelfassung, nicht den Importzeitpunkt.

### 1.1 Reproduzierbare Sequenzbausteine

| Baustein | Vollständig bestimmte Auflösung |
|---|---|
| `DL(n)` | Für n≤270: D(n). Für 270<n≤540: D270, R45, D(n−270). Für 540<n≤810: D270, R45, D270, R45, D(n−540). Entsprechend weitere Blöcke von höchstens 270 mit dazwischen R45; nie Pause nach dem letzten D erfinden. Nur die angegebenen n werden benutzt. |
| `DAY(d,n)` | Start am UTC-Datum d um 06:00. Zuerst W(780−n−45·k), dann DL(n), wobei k die tatsächlich nötigen inneren DL-Pausen ist; Ende 19:00, danach R660 bis nächster 06:00. Für n=480/540/600: W255/195/90, Pausen 45/45/90. Keine Arbeit nach genau 4:30 Schlussfahrt. |
| `WEEK(d;[n1..n7])` | Sieben feste UTC-Daten ab Montag d. An Tagen mit n>0 DAY(d, n). Aufeinanderfolgende 0-Tage bilden ununterbrochene R ab dem vorausgehenden 19:00 bis nächstem positivem 06:00. Ein führender Nulltag setzt explizit vorhandene R aus der Vorgeschichte fort; Quelle/Ende sind belegt. |
| `WORKREST(t,a,r)` | SEQ(t; W(a), R(r)); Stichtag exakt t+a+r. Nur Einzelregelprojektion für Tagesruhe; W kann andere, hier nicht betrachtete Arbeitszeitfragen auslösen. Keine nationale Arbeitszeitabnahme. |
| Wochenruheprojektion | Alle ausdrücklich gelisteten Ruheblöcke sind geschlossen mit Herkunft. Im ausdrücklich begrenzten Beobachtungsintervall sind alle nicht gelisteten Minuten W. **Nur benannte W-Regeln beurteilen**; zusätzliche Tagesruheverstöße sind dadurch nicht als abwesend behauptet. |
| Schuldenprojektion | Schulden sind exakt bezeichnete, aus angegebenen Ursprungsruhen bereits hergeleitete Ledger-Eingänge. Testet ausschließlich Zuordnung/Frist, keine ungenannte Wochenhistorie. Keine zirkuläre Erzeugung durch die künftige Engine. |

Belegte Vorzähler sind reine Zustandsprojektionen. Bei `EXTk` mit k>0 liegt der Prüfstart am 09.09.2026 06:00 Z. EXT1 enthält DAY07.09. mit 600 Fahrminuten; EXT2 zusätzlich DAY08.09. mit 600. EXT0 enthält keine frühere Verlängerung. Jeder dieser früheren Tage und sein anschließender 660-Minuten-Ruheblock sind mit eigenen Quellen belegt; keine bloße unbewiesene Zählerzahl. Andere tagesbezogene Referenzen mit EXT0 brauchen keine ungenannten Verlängerungsereignisse.

Bei `REDk` endet die vorherige 45-h-Wochenruhe genau k·1440 Minuten vor dem jeweiligen Prüfstart; danach folgen kmal vollständig belegte SEQ(W900, R540). Damit liegen vor dem Test exakt k reduzierte tägliche Ruhezeiten seit der letzten Wochenruhe. Diese relativen Daten sind für jeden fest bezeichneten Prüfstart eindeutig. RED3 vor Montag 07.09.00:00 enthält also die drei Folgen ab 04./05./06.09.00:00, keinen erfundenen Montagreset. Die Zustandsprojektionen behaupten keine Vollabnahme anderer Regeln. Vollständige Mehrregel-Orakel stehen in Abschnitt 4.

Jeder LG-Fall verknüpft die gleichnamige Test-ID unverändert. Gemessener Überschuss ist zusätzliche Fahrt, Ruhefehlmenge oder Verspätung, niemals deren ungekennzeichnete Summe. Für numerische Lenküberschüsse gilt E07 auch dann, wenn die Kurzrechnung „überschritten“ sagt: keine automatisch gesicherte Rechtswidrigkeit/Schweregrad/Artikel 12-Schuld. „Kein Befund“ meint nur die benannte Regelprojektion. Unklare Voraussetzungen haben ein ausdrücklich offenes Ergebnis, kein erratenes Goldenurteil.

## 2. Feste Einzelreferenzen LG01–LG116

### 2.1 Geltungsbereich und Zeit

| ID | Feste Eingabe / vollständige Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG01 | SEQ(t; D60), Masse 3500/3501 kg, DE-Inland | Art.2(1)a strikt>3500:3500 nicht erfasst,3501 erfasst. Keine nationale Entwarnung. |
| LG02 | SEQ(30.06.2026 12:00 Z; D60) bzw.01.07.2026 12:00 Z,2501 kg, DE→FR | Vor neuer 2,5-t-Anwendbarkeit kein S02-Ergebnis; danach grundsätzlich S02. Nur klare datierte Einsätze, Übergangsminute separat L01/L02. |
| LG03 | SEQ(t; D60), Massen 2500/2501/3500, je DE-Inland/DE→FR/FR-Kabotage | >2500-Grenze nur international/Kabotage;3500 genau nicht S01,3501 schon. Kein Test entscheidet bloß anhand Fahrermasse. |
| LG04 | SEQ(t; D60),3000 kg, DE→FR; Eigenbeförderung=true, Entgelt=false, Haupttätigkeit Fahren=false | Alle drei zusammen Art.3 ha; jeweils einzeln umkehren→keine belegte Ausnahme; jeweils null/unbekannt→Prüfhinweis. |
| LG05 | SEQ(t; D60), PASSENGERS,9/10 Sitze einschließlich Fahrer | 9≤9 nicht S03;10>9 S03. |
| LG06 | Gleiche Fahrt 60 min, Linie 49999/50000/50001 m,10 Sitze | Bis 50000 einschließlich Art.3 a;50001 nicht ausgenommen. Gefahrene Teilstrecke 10 km ändert Linienlänge nicht. |
| LG07 | SEQ(t; D60), FahrzeugDE; Reise DE→CH bzw.DE→NO bzw.DE→Serbien | Erste zwei Art.2(2)-Raum, letzte AETR für maßgebliche Gesamtfahrt: unsupported EU-Prüfung, keine EU-Restbudgetfreigabe. Mischfallauslegung L02 offen. |
| LG08 | SEQ(t; D60),0522 OOS-BEGIN bei t/OOS-END t+60; Anwendung reguliert/belegt ausgenommen/unklar | Marker ist nur Modus. Reguliert nicht pauschal abschalten; belegte ausgenommene kommerzielle D60 ist andere Arbeit; unklar→betroffene Scope-Lücke. |
| LG09 | SEQ(t; D300), synthetischer ausdrücklich belegter nur-Art.7-Ausnahmeumfang `[t,t+360)` | Art.7 ausnehmen, D300 für andere anwendbare Regeln erhalten. Ohne passenden amtlichen Einzelbeleg kein aktiv behaupteter nationaler Sonderpfad. |
| LG10 | SEQ(t; DL(570)), belegter hypothetischer Art.11-Inlandumfang 8 h, DE-Inland/DE→FR | Bei unterstützter Inlandregel 570−480=90 min, grenzüberschreitend nicht automatisch übernehmen. Unsupported→konkrete Begrenzung. Der synthetische 8 h-Wert behauptet keine existierende deutsche Vorschrift. |
| LG11 | Ausnahme `[07.09.00:00,08.09.00:00)`, D jeweils 07.09.23:59–08.09.00:01 | Vor Ende gilt belegter Artikelumfang; ab Ende nicht. Aufteilung 1+1 min, kein ganztägiger Weiterlauf. |
| LG12 | Dieselbe Ausnahme mit missing End bzw.Ende 06.09 vor Beginn 07.09 | Kein gültiger Ausnahmegegenstand; Rechtsbewertung offen statt erfundener Standardbefund. |
| LG13 | SEQ(t; D60), konkret ausgenommene kommerzielle Fahrt; Variante W60 Anreise zum entfernten Übernahmeort | Art.6(5)/9(3): andere Arbeit, keine Ruhe. Original-D bleibt Original-D; rechtliche Bedeutung separat. |
| LG14 | SEQ(t; R660), Anreise Zug zum entfernten Übernahmeort; Schlafgelegenheit=true/unbekannt | Art.9(2): bei belegter gesetzlicher Voraussetzung Anrechnung möglich; unbekannt→kein sicherer positiver/negativer Ruheabschluss. |
| LG15 | SEQ(t; D59, D61), c=0 dann c=1; Variante D61, D59; beide sonst eingesetzte eigene Karte | Crew ab Minute 59 innerhalb erster 60; ab Minute 61 eine belegte Solo-Minute nach erster Stunde. Kein Crewbeleg über spätere U60. Keine Zusatz-Anwesenheitsfrage. Fachliche ganze Crewqualifikation L08. |
| LG16 | Gleicher Input SEQ(t; DL(565)), EXT0, Anzeige UTC/Berlin/New York, Kalender/Schicht | 565 Fahrt, freie 10 h-Erweiterung→0 Überschuss,35 min bis 10 h; alle legalen Mengen/IDs gleich. Nur Zeitlabels/Gruppierung anders. |

### 2.2 Lenkzeit und Pause

| ID | Feste Eingabe / Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG17 | SEQ(t; DL540), EXT0, Stichtag 09:45 | 540=9 h,0 Erweiterungen,0 Tagesüberschuss. |
| LG18 | SEQ(09.09.06:00 Z; DL565), EXT0/EXT1 | Pause 90, Ende 16:55;565>540 nutzt 1 Erweiterung,600−565=35 min. 0 Tagesüberschuss. |
| LG19 | Wie LG18, EXT2 | Normalgrenze 540,9 h erreicht 15:45; erste überschießende Fahrt ab 16:30 nach zweiter Pause, Fahrtüberschuss `[16:30,16:55)`=25 min. E07 offener≤1 h-Hinweis. |
| LG20 | SEQ(t; DL600/601), EXT0 | Ende 11:30/11:31; Grenzpunkt 11:30,0/1 min Überschuss. |
| LG21 | SEQ(t; DL541, R660), erneut 08.09./09.09.um 00:00 gleiche Folge | Dritte 541-Minuten-Periode in gleicher Woche hat 1 min über 540; erste zwei jeweils zulässige Erweiterung. Nur Tageslenkprojektion, kein kompletter 24-h-Ruheplan. |
| LG22 | DL565 mit Beginn der ersten Fahrt über 540 min am 13.09.23:59/14.09.00:00/00:01 | Die 540 min sind jeweils 45 min davor erreicht; Start der Sequenz jeweils 540+90 min früher, also 13.09.13:29/13:30/13:31. Erste positive Zusatzminute endet 14.09.00:00/00:01/00:02. **Zuordnung des Erweiterungsereignisses nach TRACE-Vorschlag fachlich offen L02**; kein Lenktagreset. |
| LG23 | Woche 07.09.:DAY Mon 600, Tue 600, Wed 540, Thu 540, Fri 540, Sat 539/540/541; Sonntag 0 | Summe 3359/3360/3361 min;0/0/1 min Wochenüberschuss. Grenzpunkt im letzten Fahrblock Samstag; bei 541 wird Minute 19:00–19:01 nicht benutzt, DAY verschiebt W entsprechend, Endpunkt 19:00. Exakter Grenzpunkt 18:59. |
| LG24 | WEEK07.09.[540,540,540,540,540,0,0]; WEEK14.09.gleich; WEEK21.09.[540,540,540,540,541,0,0] | 2700+2700=5400;2700+2701=5401. Zweites Paar+1 min, keine frei ausgewählten nichtüberlappenden Paare. Ordnungswidrigkeitenrechtliche Konkurrenz getrennt L07/H02. |
| LG25 | WEEK31.08.[600,600,600,600,600,0,0]; laufend 07.09.[600,600,600,599/600/601,0,0,0] | Vorwoche 3000, laufend 2399/2400/2401;5399/5400/5401→0/0/1 min. Offenes Paar kann numerisch bereits überschritten sein. |
| LG26 | D13.09.23:30–14.09.00:30, Vorwochenfahrt 3300 min, neue Woche 0 | 30 min alte,30 neue Woche; alte 3330 nicht 3390. Keine Minute doppelt/verloren. |
| LG27 | SEQ(t; D120), Länderpunkt FR bei+60, sonst EU-Reise | D120, kein Reset am Punkt. Tatsächlicher anderer Gesamt-Rechtsraum nach LG07 gesondert. |
| LG28 | SEQ(t; DL600, R419/420/539, DL600), keine Änderung des Ruhebedarfs | Bei 419 kein≥420-Konventionsanker, gesamt 1200 Tagesfahrt; bei 420/539 nach Q13 zwei 600-Blöcke. Alle Ruheblöcke<540 weiterhin keine zulässige 9 h-Ruhe. Konventionsgeltung L03 offen. |
| LG29 | SEQ(t; D270/271) | Grenze 04:30; bei 270 kein Überlauf, bei 271 Fahrt `[04:30,04:31)`=1 min. |
| LG30 | SEQ(t; D270, R44/45, D1) | 44 nicht Reset, Fahrtgesamt 271→1 min;45 Reset, neue Fahrt 1→0. |
| LG31 | SEQ(t; D120, R15, D150, R30, D1) | Erste 270 Fahrt mit 15+30, Reset am 05:15; anschließend 1 min ohne Befund. |
| LG32 | SEQ(t; D120, R30, D150, R15, D1) | Kein Standard 15+30-Ersatz, letzte Minute 05:15–05:16 überschüssig. |
| LG33 | SEQ(t; D90, R10, D90, R10, D90, R25, D1) | R10+10+25=45, aber kein gültiger Standardabschluss; letzte Fahrt 1 min über 270. |
| LG34 | SEQ(t; D150, W60/Dauer A60, D121) | 271 Fahrtinnerhalb 331 verstrichener Minuten; letzte Minute 05:30–05:31 überschüssig. Keine Addition W60 als Fahrt. |
| LG35 | LG32 bzw.SEQ(t; D120, R20, D150, R25, D1), t=21.05./22.05.2024 00:00 Z, Gelegenheitsbus | Vorher 30+15/20+25 kein Standardabschluss→1 min; nachher 2 Teile≥15, Summe 45→0. Datumsrand L01/L02, gewählte Folgen liegen klar innerhalb Tag. |
| LG36 | SEQ(t; D120, R15, D150, R15/30, D1), Gelegenheitsbus 2026 | Summe 30→1 min Überlauf; Summe 45→0. |
| LG37 | SEQ(t; D270, A45, D1), gesamte Folge p=0/c=1; A45 mit SlotCO_DRIVER undbewegtem Fahrzeug; Variante W45/U45 | Zulässige Crewpause grundsätzlich aus eigener Karte; A45 geeignet→Reset, W/Ukeinbelegter Reset. Keine Routine-Zweitkarte/Bestätigung. Nicht-Unterstützung als Rechtsvoraussetzung. |
| LG38 | SEQ(t; D271, R45, D1) | Frühere 1 min bleibt; späterer Resetlöscht Vergangenheitnicht. |
| LG39 | SEQ(t; D601), EXT0 | Tagesüberschuss 1 min `[10:00,10:01)`; Pausenprojektion 331 minüber 270 ohne Unterbrechung. Zwei Regelbefunde, ein Sachzusammenhang; E07 nurpassenden Tagesbefund rechtlich offen, keine Additionzu 332„Gesamtstrafe“. |

### 2.3 Tagesruhe

| ID | Feste Eingabe / Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG40 | WORKREST(t,780,660), RED0 | Ende 08.09.00:00,660 innerhalb 1440→regelmäßig,0 Reduzierung. |
| LG41 | WORKREST(t,781,660), RED0/RED3 | Bei 24 h 659 Ruhe; RED0→zulässige Reduzierung 1, RED3→1 min Fehlmenge zu 660. Tatsächliches Ende 08.09.00:01, kein rückwirkender 11 h-Fensterabschluss. |
| LG42 | WORKREST ab 07./08./09./10.09.00:00 je W900, R540, letzte Wochenruhe 07.09.00:00 | Erste 3 Reduzierungen,4.Ruhe 540 statt 660→120 min Fehlmenge, keinetägliche Ausgleichsschuld. |
| LG43 | WORKREST11./13./14.09.00:00 je W900, R540; ZwischenräumeW, Wochenanker 07.09.00:00 | Dritteam Montag bleibt 3., nicht 1.; Montagkein Reset. Nur Zählerprojektion, zusätzlichefehlende Ruhe/144 h separat. |
| LG44 | SEQ(t; W240, R180, W480, R540), RED3 | 1440 gesamt,3+9 regelmäßig,0 Reduzierung; Anker 08.09.00:00. |
| LG45 | SEQ(t; W240, R179, W481, R540) /W240, R180, W481, R539 /W240, R540, W480, R180 | 2:59+9→kein Split,9 hggf.Reduzierung;3+8:59→1 min Fehlmenge zu 9;9+3 nicht Split, aberfrühe 9 h kann selbst qualifizierende Tagesruheundneues Fenstersein. Keinpauschaler Verstoßausbloßer Reihenfolge. |
| LG46 | WORKREST(t,960,660), RED0 | Bei 24 h 480 Ruhe→60 min Fehlmenge zu 9; gesamte Ruheendet 08.09.03:00, danachneuer Anker; alter Befundbleibt. Q07-/Q13-Ankerfachlich L03. |
| LG47 | SEQ(t; W1080, R360, W1080, R360, W1080, R360) | Je 1440-Fenster 360 stattmind 540→je 180 min Fehlmenge, keineerfüllte Tagesruhe;3 aufeinanderfolgende Fenster, nicht unbegrenzt wachsender einziger„Tag“. Q07-Konventionfachlich L03. |
| LG48 | SEQ(t; W675/676, D270, A45, D270, R540), p=0/c=1; A45 mit SlotCO_DRIVER, D mit SlotDRIVER | Ende+1800 rechtzeitig; bei 1261 innerhalb 1800 nur 539→1 min Fehlmenge.30 h-Regelnicht 24 h anwenden. |
| LG49 | LG48 mit vollständigerp 0/c 1-Chronik; Variante Minute 61–120 Solo/U | Vollständige Crewquelle genügt ohne Zusatzdialog.Soloentkräftet Qualifikation; Ubelegtweder Solo noch Crew→konkrete Prüflücke. |
| LG50 | Gelegenheitsdienst 07.09.00:00–13.09.00:00(8640 min), Tagesfall 09.09.00:00:DL420, W375, R660 | DL mit Pause 45 dauert 465;465+375+660=1500. Fahrt 7 h, Ende 10.09.01:00,1.Nutzung→bei übrigen Voraussetzungen 25 h regelmäßig. |
| LG51 | LG50:DL421, W374, R660 /Dienst nur 7200 min /vorherige 25 h-Nutzung am 08.09. | Fahrt 421>420 oder Dauer<8640 oder zweite Nutzung bei 6 Tagen→keine belegte Erleichterung. Im 24 h-Fenster 600 Ruhe:ggf.reduziert, sonst 60 min Fehlmenge, kein pauschaler 1 h-Verstoß bei freier Reduzierung. |
| LG52 | Dienst 07.09.–15.09.00:00(11520 min), je LG50-Nutzung 09./11./13.09. | Nutzungen 1/2 zulässig,3 nicht; gleicher Dienstzähler, nicht Kalenderwoche. Standard 24 h mit Reduzierungsalternativen gesondert prüfen. |
| LG53 | LG50, aber Dienstdauer oder Sicherheitsvorbehalt unbekannt | Zahlen 1500/420 bestimmt, Ausnahmebeurteilung offen; keine vorbehaltlose 25 h-Compliance. Keine nationale Arbeitszeitvollprüfung behaupten. |
| LG54 | SEQ(t; W780, R2700) | Rbeginnt 07.09.13:00, bis 08.09.00:00 schon 660 Tagesruhe; Ende 09.09.10:00=45 h, zugleich Wochenruhe. Keineextra 11 hvorab. |
| LG55 | Wbis 07.09.12:00, belegtes Ende 12:00, RED3 | Letzter 11 h-Ruhebeginn 13:00 noch möglich, Restbudgetbis Beginn 60 min; kein Befundnach Systemuhr.Darstellung P03 ohne Starrauslöser. |
| LG56 | Belegtes Wvon 07.09.00:00 bis 13:01, RED3 | 659 minbis Fensterende<660,1 min bereits unumkehrbar fehlend, sofern kein anderer belegter Ruhepfad. Grenzpunkt 13:00, Feststellbarkeit 13:01, keine erdachte Fahrt. Bei RED0 noch 9 h Pfadmöglich, keinvorzeitiger Verstoß. |
| LG57 | SEQ(t; W780, U660) | Lückekann 11 h Ruhetragen; kein bestätigter Ruheverstoß, keine Ruheannahme und kein sicheres Budget. |

### 2.4 Wochenruhe und Ausgleich

| ID | Feste Eingabe / Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG58 | Wochenprojektion 07.–21.09.:R12.09.03:00 mit 2700/1440 min, R19.09.03:00 mit 2700/1440 min | 45+45 bzw 45+24 erfüllen Basis-Paar,24+24 nur bei qualifizierter Sonderregel. Benötigte 24 erzeugt 1260 min Schuld.144 h-Beginnprüfung separat. |
| LG59 | Vier Wochenruhenje Samstag 03:00 am 12./19./26.09./03.10., Dauern 2700,1440,1440,2700; kein Internationalkontext | Mittleres Paar 24+24 nicht Standard, Woche 1+2 allein als korrekt genügt nicht. Internationale Alternativeoder Lückeoffenhalten. |
| LG60 | Wochenanker 07.09.08:00, Rbeg 13.09.08:00/08:01, Dauer 2700 | 6·1440=8640,0/1 min Verspätung; Grenze Beginn, nicht Ende. |
| LG61 | LG60 pünktlich, Ende 15.09.05:00 | 45 h Ruheendenach Fristzulässig; Verspätung 0. |
| LG62 | R13.09.23:00–15.09.20:00=45 h | Nur W37 oder W38, nichtbeide. Ein Blockistkeinzweiter Wochenruhebeleg. |
| LG63 | R06.09.23:00–08.09.20:00, R13.09.23:00–15.09.20:00, R20.09.23:00–22.09.20:00 | Alternativen je überlappender Wochegemeinsam; z.B.Zuordnung W37/W38/W39 erfülltdiese Paare, andererlegaler Pfadnichtals Verstoßunterdrücken. Außenfenster bedürfen weiterer Historie. |
| LG64 | Benötigte R12.09.00:00 mit 1440/1800/2699 min, Ursprungswoche W37 | 2700−r=1260/900/1 min Schuld; Frist Ende W40=05.10.00:00.Rechtszeitparameter L02. |
| LG65 | Schuld 1260 aus W37, Grundruhe 660+Ausgleich 1260:Start 03.10.16:00/16:01, Ende 05.10.00:00/00:01 | 32 h Paket, genau Fristendeerfüllt;1 min später keine fristgerechte vollständige En-bloc-Erfüllung. Bei Fristnoch 1 minfehlendund Erfüllung 1 min verspätet getrennt. |
| LG66 | Schuld 1260; R19.09.00:00–20.09.06:00(9+21)versus R19.09.00:00–10:00 und R20.09.00:00–11:00 | Zusammenhängend 1260 anmind 540 erfüllbar; zweigeteilte restliche 21 hnicht En-bloc.9 h-Grundruhe benötigt eigene zulässige Tageszuordnung. |
| LG67 | Zwei Schuldenje 1260 ausbenötigten R24 W36/W37; R19.09.00:00–20.09.08:00=32 h, Grundruhe 660 | Nur 1260 Zusatzminuten, genau ein Schuldblock erfüllbar, nicht 2520.Zweite 1260 offen; Tie-Breakerkein Rechtsbeweis. |
| LG68 | R19.09.00:00–20.09.06:00=1800, Schuld 1260, RED0/RED3 | Nach Ausgleich 540 Grundminuten. Bei RED0 zulässiger 9 h-Pfad; bei RED3 keinepauschale 11 h-Grundruheaus Gesamtsumme 30 h. Globalealternative Zuordnung gegebenenfalls L04. |
| LG69 | R05.09.00:00=2700; R12.09.00:00=1440; Extra R16.09.00:00=1440; R20.09.00:00=1620; Folgepaket 25.09.00:00=5040(21+18+45) | Gewählte Pflichtreduzierungen 24/27→1260/1080, Extra 24 keine weitere 1260.Sonderkontextund 144 h-Folgebeginn zusätzlich belegen; reine Mengenreferenznach Q08. |
| LG70 | Vier Wochenblöcke 05.09.R45,12.09.R24,19.09.R24,24.09.06:00 R87; letzter 45-Teilab 26.09.00:00; DE-Arbeitgeber/PL-Wohnsitz, ReduzierungenFR/BE | Vier Ruhen,2 regelmäßig;21+21 vor dem 45-Teil. Vollständige Randfenster und 144 h gesondert; kein automatisches Gesamt-OK nur aus Liste 45,24,24,45. |
| LG71 | LG70, ein Reduzierungsbeginn PL/DE/unbekannt | PL/DE verletzt Landvoraussetzung, unbekannt belegt sie weder positiv noch negativ.Sonderregel offen/nicht belegt, Standardalternativen prüfen. |
| LG72 | Letzte R25 endet 20.09.08:00; Paketbeginnt 24.09.15:00,21+20+45 | 103 hbis Paketstart,21+20=41 h;45-Teilab 26.09.08:00=144 h, Ende 28.09.05:00;86 hgesamt.Nettoschulden 1260/1200,0 Doppelminute. |
| LG73 | Gleicher Anker, Paketbeginnt 26.09.08:00 |45-Teilab 28.09.01:00,185 hseit Anker→41 h=2460 minzu spät.Nichtbloß Beginn 86-h-Blockprüfen. |
| LG74 | Vier Wochenblöcke 05./12./19./26.09.je R45/24/24/24 | Nur 1 regelmäßigeunter 4→keine Vierwochen-Sondererfüllung; an Rand noch mögliche gültige Zuordnungen verhindern vorzeitiges Urteil. |
| LG75 | LG70 nur belegt bis 21.09.00:00, späteres Ruhepaket noch nicht beobachtet | Frist/Pflichtanzeigen, kein Zukunftsverstoß vor letzter gesetzlicher Erfüllungsmöglichkeit. |
| LG76 | Nationaler Gelegenheitsbus, Anker 13.05.2024 00:00 bzw 22.05.00:00,12-Tage-Antrag | Vor 22.05.keine nationale Erleichterung; nachdiesem Datumbeiweiteren Bedingungen.Prüffensterüber Änderungsrandbleibt L01 offen, nicht automatisch Altstart neu qualifizieren. |
| LG77 | Reguläre Vorruheendet 07.09.00:00, Bustour/Rückruhebeginnt 18.09.23:59/19.09.00:00/00:01 | 287:59/288:00/288:01→0/0/1 min Fristüberlauf, nurqualifizierter Dienst.Nachruhegesondert. |
| LG78 | Nach LG77, Rab 19.09.00:00 von 5400/4140/2700 min |90=45+45 möglich;69=45+24 mit 21 h Schuld;45 alleinnicht Nachruhepflicht. Normative Paketaufteilungund Fristendeab Endeder Ausnahme, nichtbeliebiger Schuldwoche, fachlich L04. |
| LG79 |12-Tage-Fall, Fahrbeginn 09.09.22:00 imexpliziten Rechtsnachtkalender, Fahrt 179/180/181 min, Solo |3 h-Pausenperiode:0/0/1 minüber 180; außerhalb qualifizierter Ausnahme keine allgemeine 3 h-Nachtregel.Displayzone irrelevant, L02 Nachtbezugoffen. |
| LG80 | LG79, technischer Gerätenachweis unbekannt; Variante Crewbelegt | Digitalvoraussetzung nicht erraten; Crewkann Nachtalternative tragen, nicht fehlendes Digitalgerätbelegen. Keinezweite Karte Pflicht. |

### 2.5 Fähre/Zug und Artikel 12

| ID | Feste Eingabe / Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG81 | SEQ(t; W720, R240, W30, R180, W30, R240),0522-Beginn/Endegeeignet, Schlafplatzbelegt | Gesamt 1440, R660, WUnterbrechung 60 in 2 Blöcken→11 h Nettoim 24 h-Fenster, keine 12 h Ruhebehaupten. |
| LG82 | LG81 mit W31 zweite Unterbrechung(dann W719 vorher) /3 Unterbrechungen je 20, Ruheteile 180/180/180/120 | Summe 61 bzw Anzahl 3 verletzt Art.9-Ausnahme, obgleich Netto 660.Einzelne ununterbrochene Ruhealternativen prüfen, keinautomatischer Gesamtfreispruch. |
| LG83 | LG81 mit GEN1 nur 0522-Punktbei 12:00, Transport-/Schlafkontextbelegt/unklar | Keinvirtuelles Endeaus G1; vollbelegteandere Voraussetzungen können tragen, fehltentscheidender Beleg→offen. |
| LG84 | SEQ(t; W890, R270, W10, R270), nurreduzierte Tagesruhegeplant | Netto 540, aberkeine 9 hununterbrochen; Art.9 giltfürregelmäßige Tagesruhen, nichtreduzierte 9 h. Kein Reparieren allein durch Fährflag. |
| LG85 | SEQ(18.08./21.08.2020 00:00 Z; R720, W30, R720), Schlafplatz/Fährebelegt |24 h Nettowochenruhemit 30 Unterbrechung:neue Regelab 20.08., vorherkeine Wochenruhe-Erleichterung. Fristund Gesamtzuordnung separat. |
| LG86 | R1350, W60, R1350 ab 19.09.00:00, Fähre/Kabinebelegt, Fahrplan 479/480 min | Netto 2700,46 hverstrichen;479<480 keine belegte Art.9-Regelwochenruhe-Ausnahme,480 beiweiteren Bedingungenmöglich. Nichtaus Ruhesumme Fahrplandauer ableiten. |
| LG87 | LG86, entweder Kabineoder Fahrplandauer unbekannt; gleicher Reimport/Antwortvorhanden | Betroffene Ausnahmebewertung offen, gezielteoptionale Frage; nichtbeiunverändert gültiger Antwortwiederfragen.Keineallgemeine Unterkunftsbewertung. |
| LG88 | SEQ(t; W180, R180, W300, R240, W30, R300), Fährbeginn nach erstem R240; Schlafplatzbelegt |3 h Erstteil+4 h+5 h Zweitteilmit 30 Unterbrechung=3+9 Netto,1230 gesamt.Fährunterbrechung im zweiten Teil; allgemeine Zusammenführung/Erstteilunterbrechungen fachlich L03/L09 offenstattfrei 12 haddieren. |
| LG89 | SEQ(t; DL660), EXT0, Art.12-Tatsachenunbekannt |660−600=60 min Normalüberschuss, Grenzpunkt 11:30, Ende 12:30; Liste/PDFmitoffenem 1 h-Hinweis, kein bestätigter Grad/Schuld. |
| LG90 | SEQ(t; DL660/690/720), EXT0 | Überschuss 60/90/120 min; Grenzpunkt 11:30, Ende 12:30/13:00/13:30.Normalgrenze 10 h, kein neues 11/12 h-Budget.60→1 h-Hinweis;90/120→2 h-Erläuterungoffen. |
| LG91 | Nach 540 belegten Fahrminuten:R29/30/45, D120; vorher letzte geeignete Art.7-Pausebei 270 | Sonder 30 minnurbei 30/45 belegt; normale 45 minnach 270 weiterseparat. R30 genügt allein nicht diesem Art.7-Abschluss. R45 nichtzusätzliche R30 verlangen. Keine Artikel 12-Anerkennung. |
| LG92 | Normale Doppelwochenfahrt 5401 bzw Wochenruhebeginn Anker+8641, gleichzeitig passender≤2 h Tagesüberschuss |1 min 90 h-/144 h-Überschuss bleibt durch spezielle Heimkehrregel nicht aufgehoben.Allgemeiner Abs.1 separat; keinepauschale Legalität. |
| LG93 | LG90 mit App-Begründung=null | Keine Pflichteingabe, kein„App-Dokumentationsverstoß“; externeschriftliche Darlegung spätestens an geeignetem Halt/Zielgesetzlicher Hinweis. |
| LG94 | LG90 mit Zusatzfahrt 90 undkeinemgeprüften Ausnahmegrund | Gesetzlichebedingte Kompensation 90 erklären,**0 tatsächliche Art.12-Schulden**im Ledgerausdieser Zahlenlage. |
| LG95 | Siehe TabelleRF-H unten:jedeexakte Grenzeund±1 min, je Tatzeittabelle | Inklusivität bestimmt; Anwendbarkeit/Nationalumsetzung/Historie L07 nicht automatisch bestanden. |
| LG96 | DL810 am 09.09.06:00 mit EXT2 bzw.DL900 mit Verfügbarkeit; alte Q12/historische Q11-Familie und heutige Q11 getrennt | Aktuelle B4 ab 810, B8 ab 900 MSI bei gesichertem Tatbestand; alte besondere Zusatzbedingungen nicht in heutige Tabelle importieren. Historische Klassenvollabnahme offen. |
| LG97 | Wochenfahrt 3420 min, konkreteamtliche Ausnahmebis 3600 behauptet, Originalverfügung fehlt | Keinegesicherte 56 h-Verletzung/Standardschwere behaupten; Anwendbarkeitder Ausnahmeoffen. Mit passendem voll belegtem Ausnahmeumfang 3420≤3600,0 gegenjene Grenze. Synthetischer Testistkeine Behauptung einer aktuellen Ausnahme. |
| LG98 | LG50 mit voll belegter 25 h-Ausnahme und keiner verbrauchten Reduzierung | R660 rechtzeitig im 25 h-Fenster, nicht wegen 600 min im 24 h-Fenster als 11 h-Fehlruhe klassifizieren. |

### 2.6 Ergänzungsfälle

| ID | Feste Eingabe / Variation | Herleitung und Sollprojektion |
|---|---|---|
| LG99 | WORKREST(t,900,540)bzw(t,780,660), Rmitp 1/c 1 gegenp 1/c 0 | Bekanntermanueller R540/R660 normalanrechnen; unbekannte Folgegenügtwederals Ruhebeweisnoch Crew. Badge nurbekannt. |
| LG100 | LG48 p 0/c 1 inklusive A45 als Beifahrer im ausgeschriebenen 1260-Minuten-Vorlauf; gegenp 1/c 1 | Echte Crew 9 in 30+geeignete Pauseaus Aufzeichnung; manueller Nachtragkein Crewprivileg. Keine Bestätigungs-/Zweitkartenpflicht. |
| LG101 | Wochenanker 07.09.00:00; SEQ(t; W60, R1380, DL540) |23 h Rvon 07.09.01:00 bis 08.09.00:00 genügt Tagesruhe, neuer Tagesankerdort; Wochenanker bleibt 07.09.,144 h Frist 13.09.00:00; keine 22 h Schuld. |
| LG102 | Wochenprojektion 07.–16.09., tägliche R23 von 13.09.07:00 bis 14.09.06:00,14.09.07:00 bis 15.09.06:00; W60 am 15.09.06:00, dann erste R24 ab 15.09.07:00 | Kein 24 h Wochenanker; eine fortbestehende 144 h-Episode ab 13.09.00:00 bis 15.09.07:00=55 h belegte Verspätung, kein täglicher Duplikatbefund.Wochenpaarpflicht separat. |
| LG103 | Benötigte R12.09.00:00 mit 1439/1440/2699/2700 min |1439 keine Wochenruhe/keine Schuld;1440→1260,2699→1,2700→0 Schuldbei Pflichtzuordnung. Tagesruhebei 1439 möglich. |
| LG104 | R13.09.23:00–14.09.23:00=24 h | Zuordnung W37→Frist 05.10.00:00, W38→12.10.00:00; beides nicht gleichzeitig erlaubt. Vor-/Folgefenster bestimmen gültigen Gesamtpfad; keine automatisch späteste Fristwahl. |
| LG105 | Schulden 840 aus W36(Frist 28.09.),1260 aus W37(Frist 05.10.); R19.09.00:00 von 1920/2760 min, Grund 660 |32 hbietet 1260 Zusatz:höchstens 21 h-oder 14 h-Schuld vollständig;46 hbietet 2100=840+1260, beideje En-blocreservierbar, keine Doppelminute. Keinfestes Greedygebot; Fristvarianten LG65 je Schuld. |
| LG106 | LG72 gegen Paket 45+21+20 | Richtig:1260+1200 vor 2700. Falsch:45 ersterfüllt Vorgabe„vor Folgewochenruhe“nicht. Reihenfolge der beiden Schulden kein ungeprüfter zusätzlicher Tatbestand, L04. |
| LG107 | Sechs Ruhenam 05./12./19./26.09./03./10.10., Dauern 45/24/24/45/24/24 h | Erste Vier 45/24/24/45 haben 2 regelmäßige; zweites Fenster 24/24/45/24 nureine.Alleüberlappenden Vierfenster berücksichtigen, grenzwochenübergreifende Alternativenprüfen. Keinabschließendes Urteilausnurerstem Fenster. |
| LG108 | LG72, ArbeitgeberDE, WohnsitzPL, BeginnländerFR/null; Reimportgleich, dann Belgienfürzweiten Beginnbelegt | Nurfehlendes Land des konkreten zweiten Beginnsfragen; gleiche Antwort wiederverwenden, neue Kontextrevision, geänderten Lauf/Audit, nicht Heimortableiten. |
| LG109 | FährmusterR(n 1), W30, R(n 2), W30, R(n 3), Netto 659/660,1439/1440,2699/2700 | Grenzwerte 11/24/45 hnetto; verstrichen Netto+60.Je Schlaf-/Fahrplan-/Ruheartbedingungen getrennt; dritte Unterbrechung verletzt Ausnahmebeiallen Nettozahlen. Verteilungn 1=180, n 2=180, n 3=Netto−360, außer 45 h:900/900/Rest. |
| LG110 | LG86 Kabine/Fahrplanunbekannt→belegte Antwort 480/Kabine=true | Erstoffen, dannauskonkretem Kontextneuprüfenmit Audit; keine pauschale allgemeine Unterkunftsfrage. |
| LG111 | DL(Grund+e) am 09.09.06:00, Grund 540 bei EXT2 oder 600 bei EXT0; e=0/1/60/61/120/121; analog Wochen 3360+e | e 0 kein Befund;1..60 möglicher 1 h-,61..120 möglicher 2 h-Hinweis.121 kein bloß numerisch passender Spezialfall, allgemeiner Abs.1 nicht automatisch ausgeschlossen. Nieautomatische Schuld/Rechtfertigung. |
| LG112 | Laufender Tag 690, Tagesnormal 600, Wochenstand 3450, Vorwoche 2040 | Tages+90, Woche+90, Doppelpaar 5490−5400=90;3 rechtlichgetrennte Zahlenregeln, keine 270 min Gesamtfahrt/Schuld. Spezieller Hinweishebt 90 h-Grenzenichtauf. |
| LG113 | LG91 mit Gesamt 720, App-Grund=null; UIundPDFgleicher Snapshot | Zahlen 120 offen,30 minundnormale 45 minunterscheiden; keine Anerkennung/Begründungspflicht/tatsächliche Schuld. Wortlaut/Details Abschnitt 3. |
| LG114 | Pflichtblöcke R45 am 05./12./19.09., zusätzlich R24 am 16.09.00:00 | Zusätzlicher 24 h Block nicht automatisch 21 h Schuld; legale Pflicht-/Ankerzuordnungen L04 globalprüfen. Keine Regel„jede 24 h Rastist Reduzierungsschuld“. |
| LG115 | LG60 mit 1 min Verspätung; spätere R45 ab 20.09.; Variante Folgeimportbelegt R45 schon 13.09.08:00 | Spätere Tatsacheheilt 1 minnicht; neuer Belegüberdamalige Tatsache darf numerische Bewertungmit Auditkorrigieren. Herkunftbeider Sitzungenbleibt. |
| LG116 | LG35/LG50/LG76 als datierte Varianten mit gleicher Bus-Dienstkennung und PASSENGERS | Änderung 22.05.2024 nur für betroffene Normen; EF0530 allein belegt weder Linien-/Gelegenheitsart noch vollständige Sondervoraussetzungen. Keine Reiseverwaltung als Ersatzbeweis. |

## 3. Grenztabellen und offene Auslegungen

### RF-H – heutige Schweregradgrenzen als getrennte Referenz

Q11 AnhangIII, PDF-Seiten 3–5. x=gemessene Fahr-/Ruhemenge, d=Verspätung; alle Werte Minuten. Jede enthaltene Grenze b erhält die drei fest definierten Einzelwerte b−1, b, b+1. Klassen sind Tabellenablesungen **nur bei belegtem Tatbestand und passender Tatzeit-/Umsetzungsfassung**, kein Befund über Art.12- oder andere offene Ausnahmen. Keine Bußgeldschätzung.

| Tabellenbereich | MI | SI | VSI | MSI / zusätzliche Bedingung |
|---|---|---|---|---|
| B1–B4,9 h erforderlich |540<x<600|600≤x<660|660≤x<810|x≥810 |
| B5–B8,10 h zulässig |600<x<660|660≤x<720|720≤x<900|x≥900 |
| B9–B12, Woche |3360<x<3600|3600≤x<3900|3900≤x<4200|x≥4200 |
| B13–B16, Doppelwoche |5400<x<6000|6000≤x<6300|6300≤x<6750|x≥6750 |
| C1–C3, Standardpause |270<x<300|300≤x<360|x≥360|keinMSI |
| D1–D3,11 h erforderlich |600≤x<660|510≤x<600|x<510|keinMSI |
| D4–D6,9 h zulässig |480≤x<540|420≤x<480|x<420|keinMSI |
| D7–D9,3 h+Rest |180+480≤x<180+540|180+420≤x<180+480|zweiter Teil<420|Erstteil≥180 separatbelegt; keinMSI |
| D10–D12, Crew 9 h |480≤x<540|420≤x<480|x<420|keinMSI |
| D13–D15, reduzierte Wochenruhe |1320≤x<1440|1200≤x<1320|x<1200|keinMSI |
| D16–D18,45 h erforderlich |2520≤x<2700|2160≤x<2520|x<2160|keinMSI |
| D19–D21,144 h-Frist |0<d<180|180≤d<720|d≥720|keinMSI |
| D22, zwei Reduzierungenohne Ausgleich |—|—|Tatbestandbelegt|keinautomatischer Tatbestand aus offenem Ledger |
| E1–E3,288 h-Frist |0<d<180|180≤d<720|d≥720|nurqualifizierter 12-Tage-Fall |
| E4–E6, Nachruhe 45+24 |4020<x<4140|3900<x≤4020|x≤3900|besondere Grenzzeichen, nicht austauschbar; Ruhearten ebenso erforderlich |
| E7–E8, Nachtpause |—|180<x<270|x≥270|nur Solo/Nacht/12-Tage-Pfad |

Beispiel E4–E6:4021→MI,4020→SI,3901→SI,3900→VSI;4140 keine Fehlmenge. Eine beliebige 4140-Minuten-Summe ohne gesetzliche Nachruhearten ist kein positiver Test. Für ältere Zeiträume werden **nicht** diese Klassen rückwirkend benutzt. Die Vollständigkeit der historischen Tabellen-/nationalen Umsetzung bleibt L07; LG95/LG96 sind insoweit noch nicht fachlich freigabefähig.

### RF-X – Artikel 12-Erklärung und rechtliche Offenheit

Normaler zulässiger 9-/10 h-Wert zuerst, ebenso 56 h und 90 h. Für +90 min bei freier 10 h-Erweiterung lautet der numerische Wert 11:30−10:00=1:30. Die Aussage bleibt auch in Start/Liste/PDF erkennbar offen:

> Unter bestimmten außergewöhnlichen Umständen kann eine Abweichung nach Artikel 12 der VO(EG)Nr.561/2006 zulässig sein. Ob die gesetzlichen Voraussetzungen erfüllt sind, wurde von Fahrer Tagebuch nicht geprüft.

Gesetzliche Erläuterung: besondere 1 h-Fahrt zur Wochenruhe; besondere 2 h-Fahrt zur **regelmäßigen** Wochenruhe mit vorheriger ununterbrochener 30-min-Pause; Wohnort/Betriebszentrum, Verkehrssicherheit, außergewöhnlicher Anlass, gesetzlicher schriftlicher Vermerk. Zusätzliche tatsächliche Verlängerung wäre bei rechtlicher Inanspruchnahme en bloc bis Ende der dritten Folgewoche auszugleichen. Zahlen allein belegen weder Inanspruchnahme noch Ausgleichsschuld. Spezielle 1/2 h heben 90 h/Art.7/144 h nicht auf; allgemeiner Abs.1 hat anderen Tatbestand. Vor 20.08.2020 kein heutiger besonderer 1/2 h-Pfad. Qualifizierte Prüfung L06 bleibt offen.

### RF-HIST – absichtlich nicht durch Sollwerte erfundene Übergänge

Offene Referenzeingänge: Wochenruhe beginnt 19.08.2020 23:00 Z und endet 21.08.2020 23:00 Z; frühere Pflichtreduzierung W33/2020 mit Ausgleich nach 20.08.; Gelegenheitsdienst 20.–28.05.2024 mit Nutzung 25 h am 23.05.; EU-Anwendbarkeitsfahrt 30.06.2026 23:30 Z–01.07.00:30 Z in unterschiedlich maßgeblicher Rechtszeit. Tatsächliche Dauern 2880/anderegenannte Minuten bestimmt, Übergangsrecht/Zuordnung vor L01/L02 **offen**. Tests müssen später die bestätigte Interpretation-ID tragen; ein fehlendes Urteil ist kein leerer „bestanden“-Goldenfall.

## 4. Vollständige Mehrregel-Referenzen

### RF-G1 –56+34 ohne falschen Lenkzeitverstoß

Beobachtungshorizont 28.08.2026 19:00 Z bis 21.09.2026 06:00 Z, Beurteilungszeitraum 07.–20.09. Quelle/Kontext wie Abschnitt 1. Alle Minuten sind angegeben:

- R28.08.19:00–07.09.06:00=227 h. Vorwoche 31.08.–07.09. ohne Fahrt; Wochenruhezuordnung zu W36. Keine Aussage über frühere externe Fenster.
- WEEK07.09.[600,600,540,540,540,540,0]. Tägliche innere Ruhe jeweils 660 min; nach Samstag 19:00 durchgehend R bis Dienstag 15.09.06:00=59 h.
- WEEK14.09.[0,540,540,480,480,0,0]. Letzte Ruhe 18.09.19:00–21.09.06:00=59 h.

Handrechnung: W37=3360 min, W38=2040 min; Paare W36/W37=3360 und W37/W38=5400 min. Verlängerungen W37=2, W38=0. Jeder Fahrtag≤600 min; jede innere Fahrtunterbrechung 45 min, kein Fahrabschnitt>270 min. Tagesruhe 11 h, keine verbrauchten Reduzierungen. Wochenruhe jeweils 59 h; Beginn nach dem vorherigen tatsächlichen Ende nach 133 h beziehungsweise 85 h, jeweils≤144 h. Zuordnung W36/W37/W38 ohne Doppelbelegung. Keine Ausgleichsschuld; Fähre/Crew/Bus/Artikel 12 nicht betroffen.

**Erwartete Befunde im beurteilten Zeitraum:0; Prüflücken für diese Rechenfenster:0.** Kalender-/Schicht-/Anzeigezone ändern kein Zahlenergebnis. Interpretation der UTC-Woche bleibt L02 vorbehalten; keine amtliche Gesamtabnahme behauptet.

Am Datenende 21.09.06:00 neuer Tages-/Wochenruheanker. Neuer Wochenrahmen W39, vorherige Woche W38=2040 min: Wochenbudget 3360, Doppelwochenbudget 5400−2040=3360 min; zwei Tagesverlängerungen und drei Tagesreduzierungen verfügbar. Nächste 144 h-Grenze 27.09.06:00. Budgets gelten am angegebenen Datenstand, ohne Livefortschreibung oder P03-Auslöseschwelle.

### RF-G2 – offene 1:30 h-Abweichung mit sonst gültiger Tagesruhe

Prefix R28.08.19:00–07.09.06:00 wie G1; dann SEQ(07.09.06:00; D270, R45, D270, R45, D150, R660). Ende 08.09.06:00. Fahrt 690 min, innere Pausen 90 min, Ruhe 660 min; Gesamt 1440 min. Ende der 9 h-Fahrt 15:45, zweite Pause bis 16:30;10 h-Punkt 17:30, überschüssige Fahrt 17:30–19:00=90 min. Eine Tagesverlängerung verbraucht; Woche 690 min, Doppelpaar 690 min. Kein Pausenverstoß, keine Tagesruhefehlmenge, Wochenfrist noch offen und keine Wochenreduzierungsschuld.

**Ein numerischer Tagesbefund+90 min mit offener Artikel 12-Ausnahmebewertung; kein bestätigter Rechtswidrigkeitsgrad und 0 tatsächliche Artikel 12-Schulden.** Kein weiterer Pausen-/Wochen-/Ruheverstoß aus dieser Folge. „Neu“ bis angesehen; identischer Replay erzeugt kein zweites Ereignis. Liste, Detail und PDF tragen dieselbe offene Aussage und exakte Quellen. Eine positive Gesamtmeldung darf den offenen Zahlenbefund nicht verschweigen.

### RF-G3 – bekannte manuelle Ruhe gegenüber unbekanntem Abschnitt

Prefix R05.09.03:00–07.09.00:00=45 h; danach W900 bis 07.09.15:00; danach Beobachtung p=1/c=1/REST bis 08.09.00:00, durch einen bekannten Folgewechsel geschlossen. Keine Fahrt. Tagesfenster 1440 min, belegte Ruhe 540 min: erste zulässige Reduzierung, keine Schuld. Wochenfrist ab 07.09.00:00 bleibt 13.09.00:00. Manueller Badge; Roh-c=1 begründet weder Crew noch 30 h-Budget. Alle übrigen Zeitgrenzen im beurteilten Tag unverletzt.

Variante p=1/c=0 mit denselben aaREST-Bits:540 min unbekannt. Weder bestätigter Ruheabschluss noch bestätigter Ruheverstoß, weil die Lücke das Ruhefenster tragen könnte. Kein Tagesruhe-Restbudget und keine Entwarnung. Folgeimport I2 belegt p=1/c=1 zum damaligen Zeitpunkt: aktuelle Ableitung wie im bekannten Fall; Audit U→bekannte Ruhe mit beiden Importbezügen. Original I1 bleibt bytegleich.

### RF-G4 –23 h Tagesruhe ohne Wochenruhefiktion

Prefix 45 h endet 07.09.00:00; SEQ(t; W60, R1380, DL540, R660). R23 endet 08.09.00:00, DL540 endet 08.09.09:45, R660 endet 20:45. Beide Tagesruhen rechtzeitig, keine Fahrt-/Pausenüberschreitung, keine Reduzierung oder Ausgleichsschuld. Letzter Wochenanker bleibt 07.09.00:00, Frist 13.09.00:00;23 h setzen ihn nicht zurück. Am Datenende ist diese Frist noch nicht abgelaufen.

**0 festgestellte Zeitverstöße und 0 Wochenruhe-Schulden im vollständig belegten beurteilten Ablauf; kein 22 h-Ausgleich.** Die anschließende Verspätungsepisode wird gesondert in LG102 geprüft und nicht aus der Systemuhr erfunden.

## 5. Noch erforderliche Referenzabnahme

Diese Datei macht die Kernarithmetik und sämtliche 116 LG-Zuordnungen prüfbar. Sie ersetzt keine qualifizierte Zweitprüfung. Noch offen: L01/L02-Historien-/Rechtszeit- und Verlängerungsgrenzen; L03-Fenster/Vollzugskonvention; L04 vollständige globale Solver-Alternativorakel einschließlich Außenfenstern, Bus-Nachruhe und Teilung langer Ruhe; L06 rechtliche Anzeigeabgrenzung; L07 historische/nationale Schweregrade; L08 End-/Bit-/Quellenabgleich; L09 Sondervoraussetzungen. Bewusst offene Urteile sind **keine freigegebenen Goldenwerte**. Deren Bestätigung und ausführbare Tests erfolgen erst im jeweils gesondert freigegebenen Paket. Kein Rechtsrechenkern wurde zum Erzeugen der Erwartungen gebaut.
