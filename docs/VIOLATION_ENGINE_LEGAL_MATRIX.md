# FahrerTagebuch 1.0 – gesetzliche Regelmatrix

**Entwurf zur ausdrücklichen Freigabe, 14.10.2026. Keine Implementierung und keine unabhängige Rechtsabnahme.**

Verbindliche Produktgrundlage: [historische Entscheidungen 1–80 und A–H](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md) mit vorrangigen [Ergänzungen E01–E12](VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md). **58 Regelzeilen; aktive Zeitregeln für regulären Güterverkehr und unterstützten Linien-/Gelegenheitsbusverkehr, begrenzte Anwendbarkeitsprüfung für weitere Einsätze.** [Änderungen gegenüber dem ersten PR-Entwurf](VIOLATION_ENGINE_PLAN_CHANGES_20261014.md). Architektur und Gates: [Umsetzungsplan](VIOLATION_ENGINE_V1_PLAN.md). Reproduzierbare Prüffälle: [Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md).

## 1. Rechtliche Quellen und Nachweisgrenzen

Gesetzlicher Tatbestand, technische Aufzeichnungsdefinition und Vollzugsempfehlung sind getrennte Quellenarten. Eine Kommissionsleitlinie ersetzt weder den Gesetzestext noch eine gerichtliche Auslegung. Eine spätere Rechtsfassung wird nicht rückwirkend auf alte Daten angewendet. Die folgenden Quellen wurden am 14.10.2026 recherchiert.

| ID | Primärquelle | Verwendung und tatsächlicher Zugriff |
|---|---|---|
| Q01 | [VO 561/2006, konsolidiert 31.12.2024](https://eur-lex.europa.eu/legal-content/DE/TXT/?uri=CELEX:02006R0561-20241231), CELEX 02006R0561-20241231 | Aktueller bei EUR-Lex ausgewiesener Konsolidierungsstand; Artikelabschnitte über die indexierten EUR-Lex-Ergebnisse gelesen. Direkte Volltext-/PDF-Aufrufe lieferten eine JavaScript-Prüfseite. Kein vollständig heruntergeladener oder gepinnter Gesetzesvolltext behauptet. |
| Q02 | [VO 2020/1054](https://eur-lex.europa.eu/eli/reg/2020/1054/oj), CELEX 32020R1054 | Änderungen ab 20.08.2020; internationale Güter-Wochenruhe, Fähre/Zug, Mehrfahrerpause, Artikel 12; prospektive 2,5-t-Grenze ab 01.07.2026. Indexierte Gesetzesabschnitte und Kommissionsnachweise; Volltextpin noch offen. |
| Q03 | [VO 2024/1258](https://eur-lex.europa.eu/eli/reg/2024/1258/oj), CELEX 32024R1258 | Gelegenheits-Personenverkehr ab 22.05.2024; flexible Pausen, 25-Stunden-Fenster, erweiterte 12-Tage-Regel und Nachweispflichten. Indexierte Artikelabschnitte; Volltextpin noch offen. |
| Q04 | [VO 2016/799, konsolidiert](https://eur-lex.europa.eu/eli/reg_impl/2016/799), und [VO 2021/1228](https://eur-lex.europa.eu/eli/reg_impl/2021/1228/oj) | Technische UTC-Zeit-/Wochendefinition, Kartenformate. Indexierter UTC-Wochenbeleg aus 2021/1228; vorhandene Parser und bereits belegte Formatdokumentation bleiben Grundlage. Vor Freigabe genaue Fundstelle je verwendeter Annex-Fassung pinnen. |
| Q05 | [Kommission: Driving time and rest periods](https://transport.ec.europa.eu/transport-modes/road/social-provisions/driving-time-and-rest-periods_en) | Übersichtsseite und Verweise tatsächlich geöffnet. Ihre vereinfachten Aussagen „dreimal pro Woche“ und ältere internationale Busbeschreibung werden nicht anstelle von Art. 8 Abs. 4 beziehungsweise Q03 verwendet. |
| Q06 | [Kommission: TRACE II, 2023](https://transport.ec.europa.eu/system/files/2023-12/TRACE_II_Driving-Resting_Times_Enforcement_Guidance.pdf) | Volltext zugänglich; gezielt tägliche Lenkzeit, Verlängerungswochen, Ruhe-Anker, Wochenzuordnung, Ausgleich, Mehrfahrer, Fähre und Artikel 12 gelesen. Ältere Busregeln und Schweregrade nur zeitlich korrekt verwenden. |
| Q07 | [Kommission: Guidance Note 7](https://transport.ec.europa.eu/system/files/2016-09/guidance_7_en.pdf) | Volltext mit fünf Beispielen geöffnet; sukzessive 24-/30-Stunden-Referenzfenster bei unzureichender Ruhe. |
| Q08 | [Kommission: Q&A Part II](https://transport.ec.europa.eu/system/files/2021-09/qa_mobipack_part_ii_en.pdf) | Tatsächlich geöffnete Fassung enthält auch neuere gerichtliche Verweise; Fragen 4, 6, 7, 9–13 sind für Ausgleich und Artikel 12 relevant. Insbesondere Ausgleich vor Beginn der nächsten regelmäßigen Wochenruhe innerhalb der 6×24-Stunden-Frist. Nicht allein nach URL-Jahr datieren. |
| Q09 | [Kommission: nationale Art.-13-Ausnahmen](https://transport.ec.europa.eu/document/download/bb92ed48-ffde-40e5-af09-20799a86a76b_en?filename=national_exceptions_regulation_2006_561.pdf) | Tatsächlich geöffnete Tabelle, Dokumentstand 12.06.2026, Übersichtsseite veröffentlicht 29.07.2026. Tabelle ist Wegweiser; konkrete nationale Rechtsakte, Artikelumfang und historische Geltung sind pro Eintrag nachzuweisen. |
| Q10 | [Kommission: befristete Ausnahmen](https://transport.ec.europa.eu/document/download/c768680f-c26e-460e-8f0a-58ccbb1a34c4_en?filename=temporary-relaxation-drivers.pdf) | Nach zunächst gescheitertem Aufruf PDF tatsächlich geöffnet; Übersicht verlinkt am 02.09.2026. Enthält teilweise widersprüchlich extrahierte Datumsbereiche. Solche Einträge bis zur Prüfung der Originalverfügung nicht automatisch anwenden. |
| Q11 | [RL 2006/22/EG, Anhang III in Fassung RL 2024/846](https://eur-lex.europa.eu/eli/dir_del/2024/846/oj), CELEX 32024L0846 | EU-Schweregradgruppen B/C/D/E aus indexierten amtlichen Tabellen. Richtlinienwirkung und nationale Umsetzung getrennt von materieller Verstoßregel behandeln. |
| Q12 | [VO 2016/403](https://eur-lex.europa.eu/eli/reg/2016/403/oj), geändert durch [VO 2022/694](https://eur-lex.europa.eu/eli/reg_impl/2022/694/oj) | Weitere EU-Klassifizierung im Zuverlässigkeitskontext des Unternehmers. Nicht als Fahrer-Bußgeldtarif oder pauschalen persönlichen Status verwenden. |
| Q13 | [Kommissionsentscheidung C(2011) 3759](https://transport.ec.europa.eu/system/files/2016-09/2011_3759_en.pdf) | In Q06 ausdrücklich referenzierte Behandlung einer mindestens siebenstündigen, dennoch unzureichenden Ruhe bei täglicher Lenkzeit. Originalentscheidung vor Umsetzung dieser Vollzugskonvention separat lesen/pinnen; hier keine erteilte fachliche Freigabe behaupten. |
| Q14 | [VO 2016/799, Appendix 1 §2.1 ActivityChangeInfo](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02016R0799-20160526), für GEN1 zusätzlich [Annex IB §2.1](https://eur-lex.europa.eu/eli/reg/1985/3821/2011-10-01/eng) | Amtlich indexierte Definition gelesen: c-Bit bei eingesetzter Karte Solo/Crew, bei entnommener Karte unbekannte/bekannte manuell eingetragene folgende Tätigkeit; eigene Entnahmenotiz. Kein Beweis für Crew aus c=1 bei p=1. Exakte anwendbare G1/G2/V2-Fassungen und vollständige Entnahmenotiz vor Adapterfreigabe pinnen. |

**Offene Quellenarbeit ist Bestandteil Meilenstein M0 und Gate A03.** Vor Freigabe der betreffenden Rechenregeln: amtliche Originalfassungen einschließlich Änderungen/Berichtigungen, echte Wirksamkeitsdaten und genaue Fundstellen beschaffen, unabhängigen Abgleich dokumentieren. Direkte Abrufprobleme werden nicht durch erfundene Volltextbelege ersetzt. Der Plan beschreibt den vorgesehenen Prüfumfang innerhalb E09; er erklärt die Quellenprüfung noch nicht für abgeschlossen. Am 14.10.2026 wurden die indexierten amtlichen Artikel-12-Abschnitte von Q01 und die zugänglichen Kommissionsantworten zu 21+20+45 Stunden erneut abgeglichen. Dies ist kein Ersatz für Volltextpin, Auslegungsnachweis oder unabhängige Prüfung.

## 2. Zeitsystem, Rechtsstände und Geltungsbereich

Intervalle sind halboffen `[Beginn, Ende)`. Dauern sind reale verstrichene Zeit aus UTC-Zeitstempeln beziehungsweise eindeutig bestimmten Intervallen, keine lokale Uhrzeit-Subtraktion. E10 verlangt keine zusätzliche Sommer-/Winterzeit-Dauerformel; DST gehört zur Anzeige und zum Invarianztest. Tätigkeitswechsel aus EF 0504 sind minutengenau; sekundengenaue Zusatzmarker erhöhen ihre Auflösung nicht. Ein rechnerischer Grenzbeginn ist innerhalb der belegten Auflösung exakt, ohne erfundene Sekunden.

Für die feste Rechtswoche ist UTC als Karten-/Tachograph-Konvention vorgesehen, getrennt von der frei einstellbaren Anzeigezone. Q04 definiert die technische Woche in UTC. Die Übertragung auf rechtliche Wochenprüfung sowie lokale Nachtfenster/nationale Abweichungen wird ausdrücklich durch die Fachprüfung bestätigt; eine reine Displayänderung verändert kein Ergebnis.

Historische Regeln sind nach Wirksamkeit zu segmentieren:

| Zeitraum / Grenze | Notwendige Behandlung |
|---|---|
| Vor Anwendbarkeit der Art. 6–9 am 11.04.2007 | Kein rückwirkendes 561/2006-Ergebnis; vorheriges Recht außerhalb dieses Prüfpakets kennzeichnen. |
| 04.06.2010 | Einführung der damaligen internationalen 12-Tage-Regel durch VO 1073/2009; ursprüngliche Bedingungen erhalten. |
| 01.01.2014 | Zusätzliche technische und Nachtfahrbedingungen der 12-Tage-Regel wirksam. |
| 02.03.2015 | Einschlägige Änderungen aus VO 165/2014, unter anderem Ausnahmetatbestände, zeitlich korrekt laden. |
| 20.08.2020 | Q02-Regeln verwenden; ältere Fähre-/Wochenruhe-/Artikel-12-Regeln nicht überschreiben. |
| 22.05.2024 | Q03-Regeln für Gelegenheitsverkehr verwenden; vorher keine 25-Stunden-Regel oder flexible Zweiteilung. |
| 31.12.2024 | Neuer Konsolidierungs-/Nachweisstand, unter anderem 56 Tage mitzuführende Reiseblätter; kein pauschal neuer 56-Tage-Berechnungshorizont. |
| 01.07.2026 | Art. 2 Abs. 1 aa für grenzüberschreitenden Güterverkehr/Kabotage >2,5 t aktiv. |
| Nationale / befristete Ausnahme | Eigenes Wirksamkeitsintervall, Territorium, Einsatzvoraussetzungen und betroffene Artikel. Import- oder App-Updatedatum ist kein Geltungsdatum. |

Ein Fenster, eine Wochenruhe oder eine Ausgleichsschuld über einem Versionswechsel benötigt eine fachlich bestätigte Übergangsregel. Alter Ursprung und damals wirksame Pflicht bleiben referenziert; eine aktuelle Erleichterung heilt frühere Befunde nicht automatisch.

## 3. Regelmatrix

**Eingangskürzel:** T = belegte Tätigkeit mit vollständiger Provenienz; K = getrennt gespeicherter, zeitlich begrenzter Einsatzkontext; C = EF 0522; L = EF 0530, nur GEN2V2; G = belegte Länder-/Grenz-/GNSS-/Ortsquellen. K ersetzt niemals T. „Prüfhinweis“ ist kein Verstoßstatus. E07 verlangt zusätzlich in der Verstoßübersicht sichtbare **rechnerische Lenküberschreitungen mit offener Artikel-12-Bewertung**; diese sind nicht als gesichert rechtswidrig zu klassifizieren. Datenlücken bleiben getrennte Prüfhindernisse.

Jede Zeile benötigt vor fachlicher Abnahme Positiv-, Negativ-, Grenz-, Datenlücken- und Kontextwechseltests. Konkrete Beispiele stehen in der Testmatrix; ohne sie ist keine Zeile abgenommen.

### 3.1 Anwendbarkeit und Zeitbedeutung

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Daten und Ergebnis |
|---|---|---|---|
| S01 | Art. 2 Abs. 1 a | Güterbeförderung, zulässige Gesamtmasse einschließlich Anhänger/Sattelanhänger >3,5 t. | T+K; Masse nicht aus Fahrzeugmodell oder Fahrerkarte erraten. Einschließlich relevanter Leerfahrten nach Art. 4 a. |
| S02 | Art. 2 Abs. 1 aa; Art. 3 ha | Ab 01.07.2026 grenzüberschreitender Güterverkehr/Kabotage >2,5 t; Ausnahme >2,5–3,5 t bei Eigenbeförderung ohne Haupttätigkeit Fahren und ohne Beförderung gegen Entgelt. | T+K; genaue Schwellen, Datum und sämtliche Ausnahmebedingungen prüfen. Keine generelle nationale 2,5-t-Regel daraus ableiten. |
| S03 | Art. 2 Abs. 1 b; Art. 3 a | Personenfahrzeug für >9 Personen einschließlich Fahrer; Linienverkehr bis einschließlich 50 km ausgenommen. | L+K; EF 0530 beweist weder Sitzplatzzahl noch Linienlänge/Gelegenheitsdienst. |
| S04 | Art. 2 Abs. 2–3 | EU/EEA/Schweiz gegenüber AETR-Reise abgrenzen, bei EU-/AETR-zugelassenen Fahrzeugen gegebenenfalls gesamte Reise. | K+G; kein Wechsel der Rechtsordnung bloß an jedem Grenzmarker. AETR/TCA-Fälle werden als außerhalb der unterstützten Berechnung gekennzeichnet, nicht mit EU-Budgets als entwarnt. |
| S05 | Art. 3 a–i einschließlich aa/ha | Unmittelbare gesetzliche Ausnahmen respektieren; aktiv benötigte Linien-≤50-km- und >2,5–3,5-t-Abgrenzung aus S02/S03. Seltene Spezialbetriebe nach E09 außerhalb aktiver Vollauswertung. | K, ergänzend G; bei belegter Ausnahme betroffene EU-Prüfung ausnehmen, bei konkreter unklarer/unsupported Konstellation betroffene Regeln als nicht zuverlässig prüfbar. OOS allein ist kein Rechtsbeweis; keine Vollbefragung aller Gruppen. |
| S06 | Art. 13 Abs. 1–3 | Nationale Ausnahme wirkt nur bei tatsächlicher staatlicher Gewährung und erfüllten Bedingungen. Keine aktive Vollimplementierung sämtlicher nationaler Spezialkataloge in1.0, E09. | Belegter konkreter sachlicher/territorialer/zeitlicher Artikelumfang getrennt behandeln, etwa nur Art.7. Nicht unterstützt/unklar: betroffene Prüfung begrenzen statt erfundener Standardverstöße; keine unbegründete Abschaltung aller übrigen Regeln. |
| S07 | Art. 11 | Tatsächlich erlassene strengere Regeln für vollständig innerstaatliche Beförderung ändern sich durch Produktgrenzen nicht. | Unterstützten Rechtsraum und vorhandene Abweichungsbelege prüfen. Nicht unterstützte Abweichung transparent ausweisen; keine Behauptung vollständiger nationaler Prüfung und kein automatischer umfangreicher Länderregel-Dialog. |
| S08 | Art. 14 | Behördliche/befristete Ausnahme nur in ihrem belegten Einsatz-, Artikel-, Gebiets- und Zeitumfang. Kein vollständiger aktiver Notfallkatalog in1.0, E09. | Konkrete vorhandene Ausnahmeinformation am Tatdatum berücksichtigen oder betroffene Prüfung begrenzen. Fehlende Verfügung/unklare Geltung nicht als Ausnahme oder sichere Standardrechtswidrigkeit erfinden; Ausnahme nicht nach Ablauf fortschreiben. |
| S09 | Art. 4 d–f; Art. 6 Abs. 5 | Andere Arbeit und kommerzielles Fahren außerhalb des EU-Regelbereichs bleiben andere Arbeit; Bereitschaft ist keine tägliche Ruhe. | Original-T erhalten, abgeleitete rechtliche Bedeutung separat. Kein Out-of-Scope-Fahren als Ruhe, kein technischer Ereigniscode als Verschulden. |
| S10 | Art. 9 Abs. 2–3 | An-/Abreise zur Übernahme eines Fahrzeugs außerhalb Wohnort/üblicher Betriebsstätte ist keine Pause/Ruhe, außer gesetzliche Fähre-/Zug-Schlafvoraussetzungen; eigene Fahrt mit ausgenommenem Fahrzeug ist andere Arbeit. | Bereits vorhandene T+optionales K; keine manuell erfundenen Fahrten. Fehlendes K oder fehlende T verhindert definitive Beurteilung dieses Tatbestands. |
| S11 | Art. 4 o | Mindestens zwei Fahrer während relevanter Lenkabschnitte; in erster Stunde zweiter Fahrer optional, anschließend erforderlich. | Korrekt interpretierte eingesetzte Karten-/Crew-/Slotaufzeichnungen grundsätzlich zeitbezogen verwenden, E03. Keine zweite Karte, Routine-Anwesenheitsfrage oder allgemeine Nutzerbestätigung. Unbekannte Folgeperioden nicht aus einzelnem Bit extrapolieren; tatsächliche Quellenwidersprüche getrennt erklären. |
| S12 | Art. 4 i; technische Annex-Definition Q04 | Feste Woche Montag 00:00 bis nächste Montag 00:00; Tageslenkzeit ist kein Kalendertag. | UTC-Intervalle und getrennte Rechts-/Anzeigezeit. Keine Schichtdatumszählung; Fachbestätigung der Zeitkonvention als A03-Unterpunkt. |
| S13 | Technische Karten-Aufzeichnungsdefinition Q14; Art. 4 d–f | Kartenstatus/c-Bit gemeinsam: p=0 Solo/Crew; p=1 unbekannte Folge oder bekannter manueller Kontrollgeräte-Nachtrag; Entnahmepunkt gesondert. | Lesende Semantik auf unverändertem Parserfeld `crewDriving`; p=1/c=1 kein Crewbeweis. Bekannte Nachträge normal anrechnen und dezent „Manueller Nachtrag am EG-Kontrollgerät“ kennzeichnen, E02. Unbekannt keine Ruhe; Originale/Fingerprints erhalten. |

### 3.2 Lenkzeiten

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| D01 | Art. 4 k; Art. 6 Abs. 1 | Tägliche Lenkzeit zwischen täglicher/wochentlicher Ruhe: grundsätzlich ≤9 h. | Nur Lenkzeit summieren, andere Arbeit nicht addieren; mehrere Fahrblöcke und Mitternacht verbinden. |
| D02 | Art. 6 Abs. 1 | Bis ≤10 h höchstens zweimal je fester Woche. >9 h nutzt einen Verlängerungsvorgang, genau 9 h keinen. | Zähler plus verbleibende Vorgänge; verlängerten Tag bei Wochenwechsel anhand Grenzübertritt >9 h zuordnen, Q06. Keine pauschale 9-h-Verletzung bei 9:25 und freier Verlängerung. |
| D03 | Art. 6 Abs. 2 | Wochenlenkzeit ≤56 h innerhalb jeder festen Woche. | Fahrblock am Rechtswochenwechsel aufteilen; Überschreitung als zusätzliche Lenkzeit, nicht als vergangene Kalenderzeit. |
| D04 | Art. 6 Abs. 3 | Summe jeder Kombination zweier aufeinanderfolgender fester Wochen ≤90 h. | Überlappende Paare prüfen, keine fest ausgewählten Zweiwochenblöcke. Laufende Woche mit belegtem Teilstand und vorheriger Woche. |
| D05 | Art. 6 Abs. 4–5 | Relevante Lenkzeit im EU-/Drittstaatsgebiet korrekt einbeziehen, tatsächliche ausgenommene kommerzielle Fahrt nach S09 einordnen. | Länderwechsel setzt Zähler nicht zurück. Reise-Rechtsordnung und Mischbetrieb separat beurteilen. |
| D06 | Art. 6 Abs. 1; Q06/Q13 | Bei unzureichender Ruhe von mindestens 7 h Vollzugskonvention zur täglichen Lenkzeit getrennt von legal ausreichender Ruhe prüfen. | Eine 7-h-Ruhe darf niemals als zulässige 9-/11-h-Ruhe, Reduzierung oder Ruhefrist-Erfüllung gelten. Originalentscheidung und Fachabnahme vor Umsetzung; getrennte Anker für tägliche Lenkzeit und Art. 8. |

### 3.3 Fahrtunterbrechungen

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| B01 | Art. 7 Abs. 1; Art. 4 d | Nach kumuliert 4:30 h Fahrt ≥45 min ununterbrochene Pause oder qualifizierende Ruhe. | Nicht nur ununterbrochene Straßenfahrt zählen. Arbeit/Bereitschaft oder 44 min Pause setzen nicht zurück. Genau 4:30 ohne weitere Fahrt noch keine Überschreitung. |
| B02 | Art. 7 Abs. 2 | Erst ≥15 min, später ≥30 min, zusammen legaler Ersatz für 45 min. | Reihenfolge beachten; 30+15 genügt im Standardfall nicht. Kein beliebiges Sammeln kleiner Pausen. Nach >4:30 bereits entstandenen Befund nicht rückwirkend löschen. |
| B03 | Art. 7 Abs. 3, seit 22.05.2024 | Gelegenheits-Personenverkehr: zwei Teile jeweils ≥15 min, insgesamt weiterhin ≥45 min. | 30+15 oder 20+25 zulässig; 15+15 unzureichend. Einsatzqualifikation und Tatdatum belegen. |
| B04 | Art. 7 letzter Absatz, seit 20.08.2020 | 45-min-Pause im von anderem Fahrer bewegten Fahrzeug möglich, sofern pausierender Fahrer nicht unterstützt. | Zeitbezogene vorhandene Crew-/Beifahrer-/Bereitschaftsaufzeichnung grundsätzlich nutzen, E03; keine Routinebestätigung oder Pflicht zur zweiten Karte. Gesetzliche Nicht-Unterstützung in Details erklären, konkrete entgegenstehende Arbeits-/Quellenbelege beachten. Keine Tagesruhe im bewegten Fahrzeug; ungeklärte Zeit nicht als Pause ergänzen. |
| B05 | Art. 4 d–f; Art. 7 | Unterbrechungszweck und fehlende andere Arbeit beachten; Reisen S10 und unklare Abschnitte nicht als Pause anrechnen. | Prüfhinweis statt angenommener 45-min-Unterbrechung. Zähler nur bei rechtlich tragfähiger Anrechnung zurücksetzen. |

### 3.4 Tägliche Ruhe

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| R01 | Art. 4 g; Art. 8 Abs. 2 | Regelmäßige tägliche Ruhe ≥11 h innerhalb 24 h nach Ende der vorherigen Tages-/Wochenruhe. | Nur Teil der Ruhe innerhalb des Fensters zählt für rechtzeitige Erfüllung; späterer Gesamtumfang separat. Keine allgemeine „maximale Schichtdauer“ anstelle der Ruheprüfung. |
| R02 | Art. 4 g; Art. 8 Abs. 2, 4 | Reduziert ≥9 und <11 h; höchstens drei zwischen zwei Wochenruhen. | Kein Zählerreset am Montag. Zulässige Reduzierung automatisch, keine Ausgleichsschuld für tägliche Reduzierung. Viertes Ereignis benötigt regelmäßige Ruhe. |
| R03 | Art. 4 g | Regelmäßige geteilte Ruhe: zuerst ≥3 h, dann ≥9 h, beide im rechtlich relevanten Ablauf. | 3+9 gilt regelmäßig und verbraucht keine Reduzierung. 9+3 oder 3+8 genügt nicht als regelmäßige geteilte Ruhe; separat prüfen, ob eine 9-h-Reduzierung zulässig ist. |
| R04 | Art. 8 Abs. 2; Q07 | Bei fehlender ausreichender Ruhe sukzessive Referenzfenster und tatsächliches Ende einer später ausreichenden Ruhe verwenden. | Kein unendlicher künstlicher „Tag“, kein Reset nach beliebiger kurzer Pause. 24-h-Verletzung und späterer Restbeginn/Restende getrennt. |
| R05 | Art. 8 Abs. 5; Art. 4 o | Aufgezeichneter qualifizierter Mehrfahrerbetrieb: mindestens 9 h neue tägliche Ruhe innerhalb 30 h. | Zeitverlauf S11 grundsätzlich aus vorhandener eigener Karte, keine zusätzliche pauschale Beweisabfrage. Solo-/Crewwechsel und Lücken korrekt segmentieren; ein einzelnes Bit qualifiziert keine unbekannte gesamte 30-h-Periode. |
| R06 | Art. 8 Abs. 2a, seit 22.05.2024 | Einzelner Gelegenheits-Personendienst ≥6×24 h: einmal Ruhe innerhalb ≤25 h, bei ≥8×24 h zweimal; tägliche Lenkzeit ≤7 h, Sicherheits-/Arbeitsbedingungen erhalten. | Tour-ID, tatsächliche Dauer und Nutzung je Dienst aus K; kein eigener neuer 9-/11-h-Ruhegrundwert, keine 10-h-Lenkerleichterung. Nationaler Arbeitszeitvorbehalt als ungeprüft kennzeichnen, keine entsprechende Vollständigkeitsbehauptung. |
| R07 | Art. 8 Abs. 3 | Tägliche Ruhe kann zur Wochenruhe verlängert werden. | Keine Addition einer separat geforderten 9-/11-h-Ruhe vor derselben Wochenruhe; tatsächliches vollständiges Ende ist neuer Anker. |

### 3.5 Wochenruhe, Zuordnung und Ausgleich

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| W01 | Art. 4 h; Art. 8 Abs. 6 | In jedem Paar aufeinanderfolgender fester Wochen mindestens zwei regelmäßige Wochenruhen oder eine regelmäßige ≥45 h und eine reduzierte ≥24 h. | Alle überlappenden Paare, getrennt von 6×24-Frist prüfen. Offenes, noch erfüllbares Fenster ist keine abgeschlossene Verletzung. |
| W02 | Art. 8 Abs. 6 Unterabs. 2 | Nächste Wochenruhe beginnt spätestens 6×24 h =144 reale Stunden nach Ende der vorherigen Wochenruhe. | Beginn statt Ende; vorheriges tatsächliches Ende statt Sonntag/Montag. Standardbudgets ab letztem belegtem Stichtag. |
| W03 | Art. 8 Abs. 9 | Über zwei feste Wochen reichende Wochenruhe darf einer von beiden, niemals beiden, zugerechnet werden. | Globale gesetzeskonforme Zuordnung über alle überlappenden Fenster, begründete deterministische Wahl; nicht zwei getrennte 45-h-Ruhen aus einem durchgängigen Block erfinden. |
| W04 | Art. 8 Abs. 6b Unterabs. 1 | Pflichtreduzierung äquivalent ausgleichen: grundsätzlich `45 h - angerechnete reduzierte Ruhe`, am Stück bis Ende der dritten folgenden Woche. | Jede Schuld mit Ursprung, zugeordneter Woche, Betrag und exakter Frist; nicht einfach 21 Tage ab Ende. Mehrere Pflichten getrennt; vor Fälligkeit offen anzeigen, erfüllt aus offener Liste entfernen und mit Zuordnung/Audit erhalten. Zusätzliche Rast oder 23 h nicht automatisch als Reduzierungsschuld buchen. |
| W05 | Art. 8 Abs. 7 | Ausgleich an andere Ruhe ≥9 h anschließen. | Kein Ausgleich in isolierten kleinen Pausen, keine doppelte Verwendung derselben Ausgleichsminute; tägliche Mindestdauer und Wochenruhegrundblock weiterhin erfüllen. |
| W06 | Art. 8 Abs. 6, 6b; Q08 Frage 7 | Verpflichtende und zusätzliche Ruhe korrekt unterscheiden. | Keine automatische 21-h-Schuld für jeden beliebigen zusätzlichen 24-h-Block. Insbesondere internationale Zusatzruhe nach Q08, Nähe zur 6×24-Frist und gewählte Pflichtzuordnung nachvollziehen. |
| W07 | Art. 8 Abs. 6 Unterabs. 3–4, seit 20.08.2020 | Internationaler Güterverkehr: zwei aufeinanderfolgende reduzierte Wochenruhen, beide beginnen außerhalb Arbeitgeber-Niederlassungsstaat und Fahrer-Wohnsitzland; jedes relevante rollierende Vierwochenfenster mindestens vier Wochenruhen, davon zwei regelmäßige. | L+K+G; fehlende konkrete Länderangabe gezielt erfragen, vorhandene gültige Antwort wiederverwenden. Keine Adresse/Heimortung. W08 und beide ursprünglichen Ausgleichsfristen beachten; bloß Fernverkehr oder zwei verschiedene Länder genügen nicht. |
| W08 | Art. 8 Abs. 6b Unterabs. 2; Q08 Frage 4 | Nach W07 beide ungeteilten Ausgleiche unmittelbar vor anschließender regelmäßiger Wochenruhe; deren Beginn spätestens 6×24 h nach vorherigem tatsächlichem Wochenruheende. | 24+25 h erzeugen 21+20 h vor ≥45 h: Gesamtblock spätestens `144 h -41 h =103 h` nach letzter Wochenruhe beginnen. Ausgleichsblöcke vor dem regelmäßigen Grundblock, jede Minute einmal. Keine unbelegte zusätzliche gesetzliche Reihenfolge zwischen den beiden Schulden erfinden; konkrete Zuordnungs-/Reihenfolgegoldenfälle L04. |
| W09 | Art. 8 Abs. 6/6b/9 | Pflichtzuordnung und Ausgleich gemeinsam lösen. | Kein lokales FIFO, das eine andere Pflicht rechtswidrig macht; jede Schuld am Stück erfüllen, zulässige Kombinationen deterministisch prüfen. Tagesruhe darf Teil des gesetzlichen Gesamtblocks sein; Zusatz-Ausgleichsminuten nicht mehrfach verteilen. |
| W10 | Art. 8 Abs. 6a, seit 22.05.2024 auch nationaler einzelner Gelegenheitsdienst | Nach vorheriger regelmäßiger Wochenruhe Beginn nächster Wochenruhe bis 12×24 h =288 h möglich, alle gesetzlichen Voraussetzungen erfüllen. | Einzelner Dienst, korrekte historische Geltung, technische Nachtbedingungen, anschließende Pflichten. Kein allgemeines 12-Tage-Budget für Busfahrer. |
| W11 | Art. 8 Abs. 6a b | Nach W10 zwei regelmäßige Wochenruhen oder eine regelmäßige plus eine reduzierte ≥24 h; deren Reduzierung am Stück vor Ende der dritten Woche nach Ende des Ausnahmezeitraums ausgleichen. | Nachruhepaket und Schuldfristen eigenständig prüfen; keine einzige 45-h-Ruhe als vollständigen Nachweis akzeptieren. Konkrete Abgrenzung direkt anschließender Wochenruhen im Fachgoldenfall festlegen. |
| W12 | Art. 8 Abs. 6a c–d | Seit 01.01.2014 vorgeschriebener digitaler Fahrtenschreiber; bei Fahrt 22:00–06:00 Mehrfahrerbetrieb oder Verkürzung des Art.-7-Lenkabschnitts auf 3 h. | Gerätetyp/Generation und lokale rechtliche Nachtzeit mit K belegen. Tageslenkgrenzen bleiben erhalten; Anzeigezeitzone definiert dieses Nachtfenster nicht. |
| W13 | Art. 8 Abs. 6a in älteren Fassungen | Vor 22.05.2024 nur damaliger internationaler Gelegenheitsdienst und mindestens 24 h im anderen einschlägigen Staat. | Keine aktuelle nationale Erleichterung auf Altimporte anwenden. |
| W14 | Art. 4 g/h; Art. 8 Abs. 2/6/9 | 23 h zusammenhängende Ruhe ist keine reduzierte Wochenruhe (Mindestdauer24 h), kann tägliche Ruhe sein. | Tageslenk-/Tagesruheanker am tatsächlich geeigneten Ende fortsetzen; Wochenruheanker, Reduzierungszählung zwischen Wochenruhen und offene Wochenfrist nicht als erfüllt zurücksetzen. Kein automatischer 22-h-Ausgleich. Fortbestehende W02-Verletzung als eine Episode, keine neue identische Meldung täglich; unabhängige W01-/andere Pflichten separat. |

### 3.6 Fähre/Zug

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| F01 | Art. 9 Abs. 1 | Fahrer begleitet Fahrzeug auf Fähre/Zug; gesetzlich erforderlicher Schlafplatz verfügbar. | EF0522 wiederverwenden; Marker kein Vollbeweis. E04 schließt P01: nur bei konkret entscheidender Lücke optional erforderlichen Schlafplatz/Fahrplan erfragen, gültige vorhandene Antwort wiederverwenden; keine Standardfrage bei jedem Import und keine allgemeine Hotel-/Lkw-/Zuhause-Prüfung. |
| F02 | Art. 9 Abs. 1; Art. 4 g | Regelmäßige tägliche Ruhe, auch zulässige 3+9-Aufteilung: höchstens zwei Unterbrechungen, zusammen ≤1 h. | Ruhesumme ohne Unterbrechungszeit und 24-/gegebenenfalls besonderes Fenster prüfen. Gesetzliche Unterbrechung ist kein zusätzlicher Ruheanteil. |
| F03 | Art. 9 Abs. 1 seit 20.08.2020 | Auch reduzierte Wochenruhe unter den gleichen Unterbrechungsgrenzen und Schlafplatzbedingungen. | ≥24 h tatsächliche Ruhe; reduzierter täglicher 9-h-Ruheblock ist dadurch nicht unterbrechbar. |
| F04 | Art. 9 Abs. 1 seit 20.08.2020 | Regelmäßige Wochenruhe nur bei planmäßiger Fähre-/Zugreise ≥8 h und Schlafkabine, zusätzlich höchstens zwei/≤1 h Unterbrechungen. | Fahrplanlänge ist nicht aus Kartenpause zu schätzen; Unterkunftsprüfung Art. 8 Abs. 8 bleibt ausgeschlossen. Bedingung unbekannt: Anrechnung nicht als bewiesen darstellen. |
| F05 | Art. 9 Abs. 1 in älterer Fassung; Q06 | Vor 20.08.2020 keine neuen Wochenruhe-Erleichterungen. | GEN1 kann nur Fähre-/Zugmarker ohne Endmarker liefern. Kein erfundenes Reiseende, keine pauschale Paarung um Kartendatenlücken. |

### 3.7 Artikel 12 und rechtliche Klassifizierung

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| X01 | Art. 12 Abs. 1 | Unvorhergesehene notwendige Abweichung von Art.6–9 zum geeigneten Halteplatz ohne Gefährdung. | Keine pauschale Zahlengenehmigung. E07: normale rechnerische Lenküberschreitung bleibt Übersicht/PDF; unbekannte außergewöhnliche Tatsachen nicht als Rechtfertigung oder bestätigte Rechtswidrigkeit ausgeben. Allgemeiner Absatz1 getrennt von speziellen1-/2-h-Absätzen behandeln. |
| X02 | Art. 12 Abs. 2 seit 20.08.2020 | Unter außergewöhnlichen Umständen sicherheitsverträglich bis1 h zusätzliche Tages-/Wochenlenkzeit zum Wohnort/Betriebszentrum für Wochenruhe. | Numerische Abweichung von tatsächlich zulässigem Normalwert sichtbar; bei `0 < e ≤60 min` Hinweis auf mögliche1-h-Regel, keine Prüfung/Anerkennung tatsächlicher Heimkehr. Zweck/Tatsachen nicht inferieren, Tagesmindestruhe unverändert; Art.8 Abs.2 gesondert beachten. |
| X03 | Art. 12 Abs. 3 seit 20.08.2020 | Unter denselben Bedingungen bis2 h für regelmäßige Wochenruhe; unmittelbar zuvor ununterbrochene Pause≥30 min. | Bei `60 < e ≤120 min` an einschlägiger Tages-/Wochenlenkgrenze Zahlenhinweis mit offener Bewertung; keine Ausnahme als bewiesen. Normale Art.7-Pflicht bleibt:30 min ersetzen fällige45 min nicht, vorhandene passende45 min nicht um weitere30 min verdoppeln. |
| X04 | Art. 12 Abs. 1 und 4 | Gesetzliche manuelle Begründung auf vorgeschriebenem Träger spätestens bei Ankunft am geeigneten Halteplatz/Ziel. | Details erläutern gesetzlichen Zeitpunkt/Träger nach anwendbarer Fassung. Kein App-Pflichttext, keine manuelle Artikel-12-Anerkennung oder Prüfung der externen Notiz. |
| X05 | Art. 12 Abs. 5; Q08 | Zusätzliche Lenkzeit der besonderen Verlängerung am Stück mit anderer Ruhe bis Ende der dritten folgenden Woche ausgleichen. | Nur bedingte Erläuterung, keine automatische tatsächlich entstandene Artikel-12-Schuld aus numerischem Kandidaten. Kein Nutzeranerkennungsdialog/zweiter Schuldbuchungspfad. Quellen-/Auslegungsgate L06 bleibt; Wochenruheschulden W04 unverändert separat. |
| X06 | Art. 12 Abs. 2–3; Q08 | Diese spezielle Heimkehrverlängerung ändert nicht 90-h-Doppelwochengrenze, Art.-7-Pflichten oder 6×24-Wochenruhebeginn; sie kürzt keine tägliche Mindestruhe. | Nicht mit dem anderen Anwendungsbereich des allgemeinen Abs. 1 vermischen. Eine abstrakt denkbare Ausnahme rechtfertigt nicht alle festgestellten Überschreitungen. |
| H01 | RL 2006/22 Anhang III; Q11/Q12 | Zusatzschweregrad nur für rechtlich eindeutig gesicherte passende Verletzung, Tatzeitfassung und Tabellenzeile getrennt speichern. | MI/SI/VSI/MSI mit deutscher Bezeichnung/Symbol, keine Bußgelder oder Ersatzklasse. Rechnerischer Artikel-12-Kandidat mit offener Bewertung erhält keinen bestätigten rechtlichen Verstoß-Schweregrad. Zahlenbereich allein beweist keine Rechtswidrigkeit. |
| H02 | Art. 19; Q11 | Rechtlich eigenständige Regeln separat beurteilen, denselben Tatbestand nicht mehrfach aus identischen Quellen erzeugen. | Darstellung gruppiert Ereignisse; App verhängt keine Sanktion und entscheidet keine nationalen Konkurrenzen. Wiederholte Importquellen schaffen keinen neuen Verstoß. |

## 4. Unterstützte Anwendbarkeit statt vollständiger Spezialbetriebskataloge

**E09 ersetzt die frühere aktive Vollkatalogpflicht dieses Entwurfs.** Die ursprünglichen Art.-3-/13-Tabellen sind im ersten PR-Stand `235839c152936d3e13a97cbc7f3c7e76de2ee1b6` weiterhin nachlesbar; sie sind keine Implementierungs- oder Vollbefragungsliste für1.0. Alle gesetzlichen Ausnahmegruppen bleiben rechtlich wirksam.

| Konstellation | Aktive Behandlung in1.0 | Grenze / nötiger Hinweis |
|---|---|---|
| Regulärer professioneller Güterverkehr S01/S02 | Lenk-/Pausen-/Tages-/Wochenruhe inklusive sämtlicher hier einschlägiger Erleichterungen | Masse, Reise-Rechtsraum und relevante tatsächliche Einsatzabweichung nur soweit notwendig aus vorhandenen Quellen/gezieltem Profil. |
| Unterstützter Linien-/Gelegenheitsbus S03/R06/W10–W13 | ≤50-km-Abgrenzung und sämtliche bestätigten Bus-Zeitsonderregeln | EF0530 kein Linien-/Gelegenheitsbeweis; gezielte Angaben, keine umfassende Reiseverwaltung. |
| Seltene unmittelbar ausgenommene Spezialbetriebe Art.3 | Keine vollständige Spezialbedienung; belegte Ausnahme und deren Zeitraum respektieren | Unklare/unsupported Einordnung als Prüfgrenze; keine unberechtigten EU-Befunde/Budgets oder Entwarnung. |
| Nationale Spezialausnahmen Art.13 / genehmigte Gebietsregel Abs.3 | Keine aktive Vollimplementierung sämtlicher Gruppen/Staaten | Konkreten Ausnahmebeleg mit Artikelumfang respektieren oder betroffene Prüfung begrenzen; Art.13 gewährt die nationale Ausnahme nicht selbst. |
| Strengere innerstaatliche Art.11-Regel oder temporäre Art.14-Abweichung | Unterstützte reguläre Rechtsräume fachlich prüfen; relevante konkrete Abweichung nicht ignorieren | Vollkatalog/automatische Aktualität nicht behaupten; fehlende verlässliche Unterstützung konkret erklären. |
| EF0522 Out of Scope | Originalmodus anzeigen und rechtliche Bedeutung getrennt erläutern | Kein allgemeiner Ausschalter und keine Umdeutung ausgenommener Fahrt zu Ruhe. |

## 5. Vertrag für begrenzte Regelabdeckung

Das mitgelieferte Manifest benennt **tatsächlich unterstützte reguläre Einsätze, Rechtsräume, Zeitfassungen und ausdrücklich nicht unterstützte Sonderkonstellationen**. Kein leerer Spezialkatalog beweist „keine Ausnahme“. Die allgemeine bekannte regulierte Einsatzart darf jedoch nicht wegen jeder bloß theoretisch denkbaren Spezialausnahme global unprüfbar werden.

Bei einem konkreten Signal für eine möglicherweise relevante nicht unterstützte Ausnahme wird nur der betroffene Rechts-/Zeitumfang begrenzt. Ist die Nichtanwendbarkeit belegt, wird kein EU-Zeitverstoß dort behauptet. Ist Artikelumfang/Anwendbarkeit unklar, entsteht ein konkret erklärter Prüfhinweis; abhängige Budgets/Entwarnungen bleiben aus. Sicher unabhängig beurteilbare andere Zeiträume/Regeln bleiben prüfbar. Teilweise Ausnahmen dürfen nicht alle Artikel pauschal abschalten.

Keine umfangreichen neuen Einrichtungsdialoge für Handwerk, Landwirtschaft, kommunale Einsätze oder sämtliche Notfallvarianten. Eine spätere aktive Erweiterung erfordert gesonderten Auftrag und amtliche Quellen-/Test-/Fachabnahme. L05 prüft nun diese Abgrenzung und den tatsächlich unterstützten Umfang, keinen vollständigen historischen Spezialkatalog.

## 6. Schweregradmatrix als getrennte, versionierte Tabelle

Aktuelle Planungsbasis Q11; `x` ist Gesamtlenkzeit beziehungsweise tatsächlich qualifizierende Ruhe, `d` die zeitliche Verspätung. Bereiche müssen an jeder Unter-/Obergrenze getestet werden. Folgende Werte sind der amtlichen indexierten EU-Tabelle zugeordnet; genaue historische Einträge und verbleibende Sonderkonstellationen werden in M0 verifiziert.

| Tatbestand | MI | SI | VSI | MSI |
|---|---|---|---|---|
| Tageslenkzeit, nur 9 h zulässig | 9 h <x<10 h | 10 h ≤x<11 h | 11 h ≤x<13:30 h | x≥13:30 h nach einschlägiger B4-Tabellenfassung und ihren Voraussetzungen |
| Tageslenkzeit, 10 h zulässig | 10 h <x<11 h | 11 h ≤x<12 h | 12 h ≤x<15 h | x≥15 h nach einschlägiger B8-Tabellenfassung und ihren Voraussetzungen |
| Wochenlenkzeit | 56 h <x<60 h | 60 h ≤x<65 h | 65 h ≤x<70 h | x≥70 h |
| Doppelwochenlenkzeit | 90 h <x<100 h | 100 h ≤x<105 h | 105 h ≤x<112:30 h | x≥112:30 h |
| Lenkabschnitt ohne ausreichende Pause, Standard | 4:30 h <x<5 h | 5 h ≤x<6 h | x≥6 h | Kein erfundener eigener Eintrag |
| Tagesruhe, 11 h erforderlich | 10 h ≤x<11 h | 8:30 h ≤x<10 h | x<8:30 h | Kein eigener Eintrag |
| Tagesruhe, 9 h zulässig / Mehrfahrer | 8 h ≤x<9 h | 7 h ≤x<8 h | x<7 h | Kein eigener Eintrag |
| 3+9 geteilt, erster Teil ≥3 h | Zweiter Teil 8 h ≤x<9 h | Zweiter Teil 7 h ≤x<8 h | Zweiter Teil <7 h | Kein eigener Eintrag |
| Wochenruhe, 24 h zulässig | 22 h ≤x<24 h | 20 h ≤x<22 h | x<20 h | Kein eigener Eintrag |
| Wochenruhe, 45 h erforderlich | 42 h ≤x<45 h | 36 h ≤x<42 h | x<36 h | Kein eigener Eintrag |
| 6×24 h verspätet | 0<d<3 h | 3 h ≤d<12 h | d≥12 h | Kein eigener Eintrag |
| 12×24 h verspätet, Ausnahme tatsächlich zulässig | 0<d<3 h | 3 h ≤d<12 h | d≥12 h | Kein eigener Eintrag |
| Kein Ausgleich für zwei aufeinanderfolgende Reduzierungen | — | — | D22 | — |

Eine rechtlich ausreichend belegte materielle Verletzung benötigt keine Schweregradzuordnung. Ein noch offener Artikel-12-Fall wird nicht allein anhand dieser Zahlentabelle als rechtswidrig klassifiziert. Die Tabellenzeile muss zum konkreten zulässigen Grundwert passen, insbesondere bei befristeten Änderungen, Bus-3-h-Pausen, 25-h-Tagesruhe und normalen versus besonderen Ausgleichspflichten. Fehlende eindeutige Tabellenzuordnung heißt „keine rechtlich gesicherte Zusatzklassifizierung“, nicht eigener fünfter Schweregrad. 12-Tage-Nachruhe (E4–E7) mit teilweise speziellen Grenzzeichen wird vor ihrer Umsetzung vollständig separat gepinnt und nicht aus einer beliebigen 69-h-Summe interpoliert. Unterkunfts-/Arbeitgeberverstöße und Zuverlässigkeitsfolgen werden nicht als persönliche Zeitverstöße in diese App übernommen.

## 7. Zeitzuordnung eines Befunds

Ein Ergebnis enthält mindestens:
- betrachtetes Rechtsfenster mit Start-/Endanker und Tatzeitfassung;
- mathematischen Grenzpunkt, ersten belegten Abschnitt über der Grenze und sämtliche betroffenen Segmente;
- tatsächlichen Gesamtwert, zulässigen Wert sowie Lenkzeitüberschuss, Ruhefehlmenge oder Verspätung als jeweils verschiedene Größen;
- gesetzlich anwendbare Erleichterungen samt verwendetem Zähler und Voraussetzungen;
- genaue Quellen-IDs, Karte/Anwendung/Parent/Child/Importbelege, Kontextversion und Rechtsregel-ID;
- den Stichtag und die zur Aussage erforderliche Datenabdeckung;
- bei E07 die getrennte sichere Zahlenfeststellung und noch offene rechtliche Ausnahmebewertung, ohne bestätigten Rechtsverstoß-Schweregrad.

**Beispiel Lenkzeit:** Bei aufgeteilten Fahrblöcken werden 25 min Überschuss als 25 min zusätzliche Lenkzeit erklärt; darin liegende Arbeit/Pausen verlängern den kalendarischen betroffenen Zeitraum, aber nicht die Überschreitungsdauer.

**Beispiel Ruhe:** 8:30 h bei erforderlichen 9 h bedeutet 30 min Ruhefehlmenge. Zusätzlich können Beginn-/Erfüllungsfrist und tatsächlicher Restzeitraum angegeben werden; eine fehlende Ruhezeit ist nicht automatisch ein lenkzeitlicher Überschuss.

Bei R01/R02 ist der späteste noch rechtzeitig mögliche Ruhebeginn ein Warn-/Grenzpunkt. Solange ein rechtlich zulässiger anderer Ruhepfad noch offen ist, wird kein endgültiger Verstoß erzeugt. Der feststehende Fehlbestand im abgeschlossenen Referenzfenster bleibt unabhängig von einem späteren ausreichend langen Ruheblock dokumentiert.

## 8. Bewusst ausgeschlossene beziehungsweise nicht automatisch beweisbare Pflichten

| Artikel / Bereich | Behandlung |
|---|---|
| Art. 5 Mindestalter des Fahrpersonals/Schaffner | Kein zeitbezogener Lenk-/Ruhetatbestand; keine neue Alters-/Schaffnerprüfung. |
| Art. 6 Abs. 2 Verweis auf Arbeitszeit-RL; Art. 8 Abs. 2a Arbeitszeitvorbehalt | Eigenständige Arbeitszeitberechnung ausgeschlossen; Vorbehalt transparent lassen. Keine Behauptung vollständiger arbeitsrechtlicher Compliance. |
| Art. 8 Abs. 8 Unterkunft/Kosten | Ausdrücklich durch Entscheidung 20 ausgeschlossen; zeitliche Wochenruhe weiter prüfen. |
| Art. 8 Abs. 8a Rückkehrorganisation durch Arbeitgeber | Bestehender Ausschluss Heimkehr/Heimstandort erhalten; keine Fahrer-Schuld aus fehlendem Heimkehrbeleg. Notwendige Staatenangaben für W07 sind davon getrennt. E07/E09 bestätigen den Produktausschluss tatsächlicher Heimkehrprüfung; die fachliche Prüfung bestätigt die korrekte Erklärung der Grenze, ohne erneut eine Heimkehrfunktion als offene Produktwahl einzuführen. |
| Art. 8a Parkflächen; Art. 9a autonomes Fahren | Kein individueller Zeitprüfungstatbestand / keine autonome Fahrerstatusfiktion. |
| Art. 10 Unternehmensorganisation/Entlohnung | Keine Lohn-, Spesen-, Arbeitgeberhaftungs- oder organisatorische Compliance-Funktion; zeitliche Fahrerdatenprüfung bleibt. |
| Art. 15 nationale Schutzregeln für ausgenommenen Linienverkehr | Außerhalb eigenständiger EU-Engine; klarer Hinweis auf ausgeschlossene andere Rechtsordnung. |
| Art. 16 Dienstpläne/Reiseblätter/Nachweise | Vorhandensein gesetzlich relevanter Voraussetzungen für Bus-Ausnahmen als Kontext beachten; keine App-Tätigkeitsnachträge oder dokumentenrechtliche Verstoßmaschine. |
| Art. 17–29 Kontroll-, Sanktions-, Änderungs- und Übergangsvorschriften | Soweit notwendig für Geltungsdaten/Schweregrade berücksichtigen, keine eigene Bußgeld-/Verfahrensprüfung. |
| AETR, TCA, DDD und nationale Arbeitszeitgesetze | Außerhalb dieses Implementierungsauftrags, in Rechtsraum-/Prüfbarkeitsanzeige erkennbar. |

## 9. Fachlich zu schließende Freigabepunkte

| ID | Entscheidung / Nachweis vor Umsetzung beziehungsweise Release |
|---|---|
| L01 | Vollständige amtliche Volltexte, historische Wirksamkeits-/Übergangsregeln und Berichtigungen beschaffen; heutiger Indexstand allein ist keine vollendete unabhängige Rechtsprüfung. |
| L02 | Rechtliche UTC-Wochengrenze, Verlängerungszuordnung, lokale Bus-Nachtfenster und Mischrechtsräume gemeinsam bestätigen. Keine Anzeigezone als juristische Konfiguration. |
| L03 | Q13/7-h-Vollzugskonvention und Q07-Fensterverfahren separat fachlich bestätigen; zu kurze Ruhe darf keine zulässige Ruhe werden. |
| L04 | Unabhängige Goldenfälle für globale Wochen-/Ausgleichszuordnung, mehrere Schulden und Einmaligkeit, W07/W08-Reihenfolge/Fristen, 23-h-Tagesanker/Wochenepisode W14, Bus-Nachruhe W11 und historische Übergänge bestätigen. Spätere tatsächliche Ruhe heilt vergangene Verletzung nicht. |
| L05 | Begrenzten E09-Unterstützungsumfang und unterstützte reguläre Rechtsräume gegen aktuelle/historisch relevante Primärquellen prüfen; konkrete Art.3/11/13/14-Belege, partielle Ausnahmen und Scope-Guard-Goldenfälle bestätigen. Kein Vollkatalog seltenster Spezialbetriebe als Releasepflicht. |
| L06 | Produktentscheidung E07/P02 ist geschlossen. Offen bleibt unabhängige Rechts-/Quellenprüfung der1-/2-h-Erläuterungen, Normalgrenzenauswahl, rechtlichen Offenheit in Übersicht/PDF, Absatzgrenzen und bedingten Ausgleichshinweise. Keine automatische Schuld oder bestätigte Rechtswidrigkeit aus Zahlen. |
| L07 | Vollständige rechtlich passende Schweregradzuordnung, MSI-Voraussetzungen und Sonderregel-/Altfallgrenzen, inklusive Richtlinien-/Umsetzungsstatus. |
| L08 | Q14 je G1/G2/V2 vollständig pinnen; Entnahme-/Nachtragssemantik, zeitlicher Datenstichtag und Verwendung korrekter aufgezeichneter Crewzeiten ohne Routine-Zweitnachweis bestätigen. Bekannter Nachtrag trägt Tätigkeit, unbekannte Folge keine Ruhe/Crewannahme. |
| L09 | Bereits beschlossenen Ausschluss Unterkunft/organisatorischer Rückkehr-/Heimkehrprüfung fachlich korrekt abgrenzen; gesetzliche Fähre-Schlafplatzbedingung und Länderangaben W07 bleiben davon getrennt. Keine neue Produktfrage oder Standortfunktion daraus ableiten. |

Diese Punkte sind keine Ermächtigung, Entscheidungen zu ersetzen. Der Plan kann als vollständiger Arbeits- und Abnahmeumfang geprüft werden; die betreffenden Rechtsalgorithmen erhalten erst nach Nachweis und expliziter Umsetzungsfreigabe ihren verbindlichen implementierbaren Stand.
