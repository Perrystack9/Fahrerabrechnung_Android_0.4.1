# FahrerTagebuch 1.0 – gesetzliche Regelmatrix

**Entwurf zur ausdrücklichen Freigabe, 14.10.2026. Keine Implementierung und keine unabhängige Rechtsabnahme.**

Verbindliche Produktgrundlage: [Entscheidungen 1–80 und Grundsätze A–H](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md). Architektur und Gates: [Umsetzungsplan](VIOLATION_ENGINE_V1_PLAN.md). Reproduzierbare Prüffälle: [Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md).

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

**Offene Quellenarbeit ist Bestandteil Meilenstein M0 und Gate A03.** Vor Freigabe der betreffenden Rechenregeln: amtliche Originalfassungen einschließlich Änderungen/Berichtigungen, echte Wirksamkeitsdaten und genaue Fundstellen beschaffen, unabhängigen Abgleich dokumentieren. Direkte Abrufprobleme werden nicht durch erfundene Volltextbelege ersetzt. Der Plan beschreibt den vollständigen vorgesehenen Prüfumfang; er erklärt die Quellenprüfung noch nicht für abgeschlossen.

## 2. Zeitsystem, Rechtsstände und Geltungsbereich

Intervalle sind halboffen `[Beginn, Ende)`. Dauern sind reale verstrichene Zeit, keine lokale Uhrzeit-Subtraktion. Tätigkeitswechsel aus EF 0504 sind minutengenau; sekundengenaue Zusatzmarker erhöhen ihre Auflösung nicht. Ein rechnerischer Grenzbeginn ist innerhalb der belegten Auflösung exakt, ohne erfundene Sekunden.

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

**Eingangskürzel:** T = belegte Tätigkeit mit vollständiger Provenienz; K = getrennt gespeicherter, zeitlich begrenzter Einsatzkontext; C = EF 0522; L = EF 0530, nur GEN2V2; G = belegte Länder-/Grenz-/GNSS-/Ortsquellen. K ersetzt niemals T. „Prüfhinweis“ ist kein Verstoßstatus.

Jede Zeile benötigt vor fachlicher Abnahme Positiv-, Negativ-, Grenz-, Datenlücken- und Kontextwechseltests. Konkrete Beispiele stehen in der Testmatrix; ohne sie ist keine Zeile abgenommen.

### 3.1 Anwendbarkeit und Zeitbedeutung

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Daten und Ergebnis |
|---|---|---|---|
| S01 | Art. 2 Abs. 1 a | Güterbeförderung, zulässige Gesamtmasse einschließlich Anhänger/Sattelanhänger >3,5 t. | T+K; Masse nicht aus Fahrzeugmodell oder Fahrerkarte erraten. Einschließlich relevanter Leerfahrten nach Art. 4 a. |
| S02 | Art. 2 Abs. 1 aa; Art. 3 ha | Ab 01.07.2026 grenzüberschreitender Güterverkehr/Kabotage >2,5 t; Ausnahme >2,5–3,5 t bei Eigenbeförderung ohne Haupttätigkeit Fahren und ohne Beförderung gegen Entgelt. | T+K; genaue Schwellen, Datum und sämtliche Ausnahmebedingungen prüfen. Keine generelle nationale 2,5-t-Regel daraus ableiten. |
| S03 | Art. 2 Abs. 1 b; Art. 3 a | Personenfahrzeug für >9 Personen einschließlich Fahrer; Linienverkehr bis einschließlich 50 km ausgenommen. | L+K; EF 0530 beweist weder Sitzplatzzahl noch Linienlänge/Gelegenheitsdienst. |
| S04 | Art. 2 Abs. 2–3 | EU/EEA/Schweiz gegenüber AETR-Reise abgrenzen, bei EU-/AETR-zugelassenen Fahrzeugen gegebenenfalls gesamte Reise. | K+G; kein Wechsel der Rechtsordnung bloß an jedem Grenzmarker. AETR/TCA-Fälle werden als außerhalb der unterstützten Berechnung gekennzeichnet, nicht mit EU-Budgets als entwarnt. |
| S05 | Art. 3 a–i einschließlich aa/ha | Alle unmittelbar geltenden Ausnahmegruppen aus Tabelle 4 prüfen. | K, ergänzend G; unklare Voraussetzungen als Prüfhinweise, keine pauschale Ausnahme durch Out-of-Scope. |
| S06 | Art. 13 Abs. 1–3 | Ausnahmen nur, wenn der betreffende Staat sie tatsächlich gewährt und die zusätzlichen Bedingungen erfüllt sind; Tabelle 5. | Versionierter amtlicher Länderkatalog+K+G. Teilweise nur Art. 7 ausgenommen; niemals automatisch alle Regeln abschalten. |
| S07 | Art. 11 | Strengere nationale Lenk-/Pausen-/Ruhevorschriften bei vollständig innerstaatlicher Beförderung, sofern tatsächlich erlassen. | Amtlicher Katalog+K; gehört als 561/2006-Abweichung zum Plan, trotz Ausschluss eigenständiger nationaler Arbeitszeitgesetze. |
| S08 | Art. 14 | Behördlich zugelassene Ausnahmen beziehungsweise dringende, begründete befristete Ausnahmen bis 30 Tage mit konkretem Einsatz-/Gebiets-/Zeitumfang. | Amtlicher Katalog+K+G. Eine Notlage oder Listeneintrag mit unklaren Daten genügt nicht. Ausnahmeablauf automatisch berücksichtigen; gleiche Tätigkeit je Tatdatum unterschiedlich bewertet. |
| S09 | Art. 4 d–f; Art. 6 Abs. 5 | Andere Arbeit und kommerzielles Fahren außerhalb des EU-Regelbereichs bleiben andere Arbeit; Bereitschaft ist keine tägliche Ruhe. | Original-T erhalten, abgeleitete rechtliche Bedeutung separat. Kein Out-of-Scope-Fahren als Ruhe, kein technischer Ereigniscode als Verschulden. |
| S10 | Art. 9 Abs. 2–3 | An-/Abreise zur Übernahme eines Fahrzeugs außerhalb Wohnort/üblicher Betriebsstätte ist keine Pause/Ruhe, außer gesetzliche Fähre-/Zug-Schlafvoraussetzungen; eigene Fahrt mit ausgenommenem Fahrzeug ist andere Arbeit. | Bereits vorhandene T+optionales K; keine manuell erfundenen Fahrten. Fehlendes K oder fehlende T verhindert definitive Beurteilung dieses Tatbestands. |
| S11 | Art. 4 o | Mehrfahrerbetrieb: mindestens zwei Fahrer während der relevanten Lenkabschnitte; in der ersten Stunde zweiter Fahrer optional, anschließend erforderlich. | Steckplatz-/Crewbits aus T plus notwendiges K. Ein einzelnes gesetztes Crewbit beweist nicht die gesamte Konstellation. |
| S12 | Art. 4 i; technische Annex-Definition Q04 | Feste Woche Montag 00:00 bis nächste Montag 00:00; Tageslenkzeit ist kein Kalendertag. | UTC-Intervalle und getrennte Rechts-/Anzeigezeit. Keine Schichtdatumszählung; Fachbestätigung der Zeitkonvention als A03-Unterpunkt. |
| S13 | Technische Karten-Aufzeichnungsdefinition Q14; Art. 4 d–f | Kartenstatus und c-Bit gemeinsam auswerten: bei p=0 Solo/Crew; bei p=1 unbekannter Folgestatus beziehungsweise bekannter Kontrollgeräte-Nachtrag. | T; bestehendes Feld `crewDriving` enthält das rohe c-Bit. Keine Crewannahme bei gezogener Karte; bekannte Nachträge sind aufgezeichnete Tätigkeit, unbekannte Entnahmefolgen keine Ruhe. Alte Originale/Fingerprints unverändert. |

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
| B04 | Art. 7 letzter Absatz, seit 20.08.2020 | 45-min-Pause im von anderem Fahrer bewegten Fahrzeug möglich, wenn der pausierende Fahrer nicht unterstützt. | Crew/Beifahrerquelle+gezieltes K. Bereitschaft nur unter belegter besonderer Voraussetzung als Pause; niemals dadurch Tagesruhe im bewegten Fahrzeug. |
| B05 | Art. 4 d–f; Art. 7 | Unterbrechungszweck und fehlende andere Arbeit beachten; Reisen S10 und unklare Abschnitte nicht als Pause anrechnen. | Prüfhinweis statt angenommener 45-min-Unterbrechung. Zähler nur bei rechtlich tragfähiger Anrechnung zurücksetzen. |

### 3.4 Tägliche Ruhe

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| R01 | Art. 4 g; Art. 8 Abs. 2 | Regelmäßige tägliche Ruhe ≥11 h innerhalb 24 h nach Ende der vorherigen Tages-/Wochenruhe. | Nur Teil der Ruhe innerhalb des Fensters zählt für rechtzeitige Erfüllung; späterer Gesamtumfang separat. Keine allgemeine „maximale Schichtdauer“ anstelle der Ruheprüfung. |
| R02 | Art. 4 g; Art. 8 Abs. 2, 4 | Reduziert ≥9 und <11 h; höchstens drei zwischen zwei Wochenruhen. | Kein Zählerreset am Montag. Zulässige Reduzierung automatisch, keine Ausgleichsschuld für tägliche Reduzierung. Viertes Ereignis benötigt regelmäßige Ruhe. |
| R03 | Art. 4 g | Regelmäßige geteilte Ruhe: zuerst ≥3 h, dann ≥9 h, beide im rechtlich relevanten Ablauf. | 3+9 gilt regelmäßig und verbraucht keine Reduzierung. 9+3 oder 3+8 genügt nicht als regelmäßige geteilte Ruhe; separat prüfen, ob eine 9-h-Reduzierung zulässig ist. |
| R04 | Art. 8 Abs. 2; Q07 | Bei fehlender ausreichender Ruhe sukzessive Referenzfenster und tatsächliches Ende einer später ausreichenden Ruhe verwenden. | Kein unendlicher künstlicher „Tag“, kein Reset nach beliebiger kurzer Pause. 24-h-Verletzung und späterer Restbeginn/Restende getrennt. |
| R05 | Art. 8 Abs. 5; Art. 4 o | Qualifizierter Mehrfahrerbetrieb: mindestens 9 h neue tägliche Ruhe innerhalb 30 h. | Vollständige Voraussetzungen S11; keine pauschale 30-h-Freigabe durch Crewbit. Wechsel zwischen Solo-/Crewbetrieb fachlich konsistent segmentieren. |
| R06 | Art. 8 Abs. 2a, seit 22.05.2024 | Einzelner Gelegenheits-Personendienst ≥6×24 h: einmal Ruhe innerhalb ≤25 h, bei ≥8×24 h zweimal; tägliche Lenkzeit ≤7 h, Sicherheits-/Arbeitsbedingungen erhalten. | Tour-ID, tatsächliche Dauer und Nutzung je Dienst aus K; kein eigener neuer 9-/11-h-Ruhegrundwert, keine 10-h-Lenkerleichterung. Nationaler Arbeitszeitvorbehalt als ungeprüft kennzeichnen, keine entsprechende Vollständigkeitsbehauptung. |
| R07 | Art. 8 Abs. 3 | Tägliche Ruhe kann zur Wochenruhe verlängert werden. | Keine Addition einer separat geforderten 9-/11-h-Ruhe vor derselben Wochenruhe; tatsächliches vollständiges Ende ist neuer Anker. |

### 3.5 Wochenruhe, Zuordnung und Ausgleich

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| W01 | Art. 4 h; Art. 8 Abs. 6 | In jedem Paar aufeinanderfolgender fester Wochen mindestens zwei regelmäßige Wochenruhen oder eine regelmäßige ≥45 h und eine reduzierte ≥24 h. | Alle überlappenden Paare, getrennt von 6×24-Frist prüfen. Offenes, noch erfüllbares Fenster ist keine abgeschlossene Verletzung. |
| W02 | Art. 8 Abs. 6 Unterabs. 2 | Nächste Wochenruhe beginnt spätestens 6×24 h =144 reale Stunden nach Ende der vorherigen Wochenruhe. | Beginn statt Ende; vorheriges tatsächliches Ende statt Sonntag/Montag. Standardbudgets ab letztem belegtem Stichtag. |
| W03 | Art. 8 Abs. 9 | Über zwei feste Wochen reichende Wochenruhe darf einer von beiden, niemals beiden, zugerechnet werden. | Globale gesetzeskonforme Zuordnung über alle überlappenden Fenster, begründete deterministische Wahl; nicht zwei getrennte 45-h-Ruhen aus einem durchgängigen Block erfinden. |
| W04 | Art. 8 Abs. 6b Unterabs. 1 | Reduzierung mit äquivalenter Ruhe ausgleichen; grundsätzlich `45 h - angerechnete reduzierte Ruhe`, am Stück bis Ende der dritten folgenden Woche. | Schuldursprung, Wochenzuordnung, Menge und Frist speichern; Frist nicht einfach „21 Tage nach Ruheende“. Bereits vorher offene Pflicht anzeigen. |
| W05 | Art. 8 Abs. 7 | Ausgleich an andere Ruhe ≥9 h anschließen. | Kein Ausgleich in isolierten kleinen Pausen, keine doppelte Verwendung derselben Ausgleichsminute; tägliche Mindestdauer und Wochenruhegrundblock weiterhin erfüllen. |
| W06 | Art. 8 Abs. 6, 6b; Q08 Frage 7 | Verpflichtende und zusätzliche Ruhe korrekt unterscheiden. | Keine automatische 21-h-Schuld für jeden beliebigen zusätzlichen 24-h-Block. Insbesondere internationale Zusatzruhe nach Q08, Nähe zur 6×24-Frist und gewählte Pflichtzuordnung nachvollziehen. |
| W07 | Art. 8 Abs. 6 Unterabs. 3–4, seit 20.08.2020 | Internationale Güterbeförderung: zwei aufeinanderfolgende reduzierte Wochenruhen möglich, wenn beide außerhalb Arbeitgeber-Niederlassungsstaat und Fahrer-Wohnsitzland beginnen; in jedem relevanten Vierwochenfenster mindestens vier Wochenruhen, davon zwei regelmäßige. | L+K+G; weder bloß „Fernverkehr“ noch beliebige zwei Länder genügen. Wohnsitz-/Niederlassungsland sind Kontext, kein Heimstandort/Heimradius. |
| W08 | Art. 8 Abs. 6b Unterabs. 2; Q08 Frage 4 | Nach W07 beide Ausgleiche vor der anschließenden regelmäßigen Wochenruhe; deren Beginn weiterhin spätestens innerhalb 6×24 h. | Bei 24+25 h Reduzierung: 21+20 h Ausgleich unmittelbar vor ≥45 h. Deshalb muss Gesamtblock spätestens `Frist -41 h` beginnen. Pflichtbudget entsprechend vorverlegen; nicht nur 45 h am Ende von Tag 6 starten lassen. |
| W09 | Art. 8 Abs. 6/6b/9 | Pflichtzuordnung und Ausgleich gemeinsam lösen. | Kein lokales FIFO, das eine andere Pflicht rechtswidrig macht; jede Schuld am Stück erfüllen, zulässige Kombinationen deterministisch prüfen. Tagesruhe darf Teil des gesetzlichen Gesamtblocks sein; Zusatz-Ausgleichsminuten nicht mehrfach verteilen. |
| W10 | Art. 8 Abs. 6a, seit 22.05.2024 auch nationaler einzelner Gelegenheitsdienst | Nach vorheriger regelmäßiger Wochenruhe Beginn nächster Wochenruhe bis 12×24 h =288 h möglich, alle gesetzlichen Voraussetzungen erfüllen. | Einzelner Dienst, korrekte historische Geltung, technische Nachtbedingungen, anschließende Pflichten. Kein allgemeines 12-Tage-Budget für Busfahrer. |
| W11 | Art. 8 Abs. 6a b | Nach W10 zwei regelmäßige Wochenruhen oder eine regelmäßige plus eine reduzierte ≥24 h; deren Reduzierung am Stück vor Ende der dritten Woche nach Ende des Ausnahmezeitraums ausgleichen. | Nachruhepaket und Schuldfristen eigenständig prüfen; keine einzige 45-h-Ruhe als vollständigen Nachweis akzeptieren. Konkrete Abgrenzung direkt anschließender Wochenruhen im Fachgoldenfall festlegen. |
| W12 | Art. 8 Abs. 6a c–d | Seit 01.01.2014 vorgeschriebener digitaler Fahrtenschreiber; bei Fahrt 22:00–06:00 Mehrfahrerbetrieb oder Verkürzung des Art.-7-Lenkabschnitts auf 3 h. | Gerätetyp/Generation und lokale rechtliche Nachtzeit mit K belegen. Tageslenkgrenzen bleiben erhalten; Anzeigezeitzone definiert dieses Nachtfenster nicht. |
| W13 | Art. 8 Abs. 6a in älteren Fassungen | Vor 22.05.2024 nur damaliger internationaler Gelegenheitsdienst und mindestens 24 h im anderen einschlägigen Staat. | Keine aktuelle nationale Erleichterung auf Altimporte anwenden. |

### 3.6 Fähre/Zug

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| F01 | Art. 9 Abs. 1 | Fahrer begleitet Fahrzeug auf Fähre/Zug; geeigneter gesetzlicher Schlafplatz verfügbar. | C+K; Marker sind Tatsachen, kein vollständiger Voraussetzungenbeleg. Gezielte optionale Schlafplatzangabe bedarf Produktklärung P01; keine allgemeine Hotel-/Lkw-Abfrage. |
| F02 | Art. 9 Abs. 1; Art. 4 g | Regelmäßige tägliche Ruhe, auch zulässige 3+9-Aufteilung: höchstens zwei Unterbrechungen, zusammen ≤1 h. | Ruhesumme ohne Unterbrechungszeit und 24-/gegebenenfalls besonderes Fenster prüfen. Gesetzliche Unterbrechung ist kein zusätzlicher Ruheanteil. |
| F03 | Art. 9 Abs. 1 seit 20.08.2020 | Auch reduzierte Wochenruhe unter den gleichen Unterbrechungsgrenzen und Schlafplatzbedingungen. | ≥24 h tatsächliche Ruhe; reduzierter täglicher 9-h-Ruheblock ist dadurch nicht unterbrechbar. |
| F04 | Art. 9 Abs. 1 seit 20.08.2020 | Regelmäßige Wochenruhe nur bei planmäßiger Fähre-/Zugreise ≥8 h und Schlafkabine, zusätzlich höchstens zwei/≤1 h Unterbrechungen. | Fahrplanlänge ist nicht aus Kartenpause zu schätzen; Unterkunftsprüfung Art. 8 Abs. 8 bleibt ausgeschlossen. Bedingung unbekannt: Anrechnung nicht als bewiesen darstellen. |
| F05 | Art. 9 Abs. 1 in älterer Fassung; Q06 | Vor 20.08.2020 keine neuen Wochenruhe-Erleichterungen. | GEN1 kann nur Fähre-/Zugmarker ohne Endmarker liefern. Kein erfundenes Reiseende, keine pauschale Paarung um Kartendatenlücken. |

### 3.7 Artikel 12 und rechtliche Klassifizierung

| ID | Rechtsgrundlage | Regel / Voraussetzungen | Ergebnis und besondere Grenzen |
|---|---|---|---|
| X01 | Art. 12 Abs. 1 | Unvorhergesehene, notwendige Abweichung von Art. 6–9 zum geeigneten Halteplatz ohne Gefährdung. | Kein allgemeiner Zahlenfreibrief; Sonderumstände werden nicht aus Kartenwerten bewiesen. Nach Entscheidung 4/62 und Grundsatz B nur möglicher Fall als Prüfhinweis; keine automatische Rechtfertigung. |
| X02 | Art. 12 Abs. 2 seit 20.08.2020 | Ausnahmsweise bis 1 h zusätzliche tägliche/wochentliche Lenkzeit zum Wohnort/Betriebszentrum für Wochenruhe. | Zweck und Ausnahmesituation nicht inferieren; keine neue Heimkehrfunktion. Anwendbare Art. 8 Abs. 2-Abweichung und normale Mindest-Ruhedauer getrennt. |
| X03 | Art. 12 Abs. 3 seit 20.08.2020 | Bis 2 h für regelmäßige Wochenruhe, unmittelbar zuvor ununterbrochene ≥30-min-Pause. | Normale Art.-7-Pause bleibt erforderlich; 30 min ersetzen bei erfüllten 4:30 h nicht pauschal 45 min. Bereits unmittelbar vorher ausreichende 45 min nicht mit weiterer 30-min-Pflicht verdoppeln. |
| X04 | Art. 12 Abs. 4 | Gesetzliche manuelle Begründung auf vorgeschriebenem Träger spätestens bei Ankunft. | Keine verpflichtende Begründung in dieser App und keine Prüfung der externen Notiz. App-Hinweis muss diese bestehende rechtliche Pflicht korrekt erklären. |
| X05 | Art. 12 Abs. 5; Q08 | Zusätzliche Lenkzeit der besonderen Verlängerung am Stück mit anderer Ruhe bis Ende der dritten folgenden Woche ausgleichen. | Mangels fachlich bestätigter Art.-12-Nutzung lediglich mögliche Ausgleichspflicht als Prüfhinweis, keine erfundene feste Schuld. Beweis-/Kontextvertrag im Fachgate klären. |
| X06 | Art. 12 Abs. 2–3; Q08 | Diese spezielle Heimkehrverlängerung ändert nicht 90-h-Doppelwochengrenze, Art.-7-Pflichten oder 6×24-Wochenruhebeginn; sie kürzt keine tägliche Mindestruhe. | Nicht mit dem anderen Anwendungsbereich des allgemeinen Abs. 1 vermischen. Eine abstrakt denkbare Ausnahme rechtfertigt nicht alle festgestellten Überschreitungen. |
| H01 | RL 2006/22 Anhang III; Q11/Q12 | Nur eindeutig zuordenbare gesetzliche Schweregradgruppe; Rechtsquelle, Tatzeitfassung und Tabellenzeile separat speichern. | Deutsch: geringfügig (MI), schwerwiegend (SI), sehr schwerwiegend (VSI), schwerster Verstoß (MSI). Keine Bußgelder, Farbenpflicht oder erfundener Ersatzgrad. |
| H02 | Art. 19; Q11 | Rechtlich eigenständige Regeln separat beurteilen, denselben Tatbestand nicht mehrfach aus identischen Quellen erzeugen. | Darstellung gruppiert Ereignisse; App verhängt keine Sanktion und entscheidet keine nationalen Konkurrenzen. Wiederholte Importquellen schaffen keinen neuen Verstoß. |

## 4. Vollständige unmittelbar geltende Ausnahmegruppen des Artikels 3

Jede Zeile benötigt K, Geltungsdatum, erforderliche technische Parameter und einen positiven/negativen Test. Die Beschreibung ist ein Planungsindex; genaue Tatbestandsfassung wird in M0 abgeglichen.

| Buchstabe | Gruppe | Zwingend gesondert zu klärende Bedingungen |
|---|---|---|
| a | Linien-Personenverkehr | Linienstrecke ≤50 km, nicht bloß gerade gefahrene Distanz. Nationale Schutzregeln Art. 15 bleiben außerhalb der EU-Rechenbehauptung. |
| aa | Berufsmaterial/Ausrüstung/Maschinen oder handwerklich hergestellte Güter | ≤7,5 t, Radius ≤100 km, Fahren nicht Haupttätigkeit, keine Beförderung gegen Entgelt; historisch passende Variante. |
| b | Langsame Fahrzeuge | Zulässige Höchstgeschwindigkeit ≤40 km/h, keine gemessene Durchschnittsgeschwindigkeit. |
| c | Streitkräfte/Zivilschutz/Feuerwehr/öffentliche Ordnung | Entsprechendes Eigentum oder fahrerlose Anmietung, Aufgabenbezug und eigene Kontrolle. |
| d | Notfall/Rettung einschließlich bestimmter nichtkommerzieller humanitärer Hilfe | Tatsächlicher Einsatz, keine generelle Ausnahme für jeden Hilfstransport. |
| e | Spezialisierte medizinische Fahrzeuge | Zweck und Spezialisierung. |
| f | Spezialisierte Pannenhilfe | Radius ≤100 km von Basis, nicht jedes Abschleppen. |
| g | Straßenversuche/Entwicklung/Reparatur/Wartung; noch nicht eingesetzte neue/umgebaute Fahrzeuge | Tatsächlicher Test-/Noch-nicht-in-Betrieb-Zweck. |
| h | Nichtkommerzielle Güterbeförderung | ≤7,5 t einschließlich Kombination; Rechtsdefinition, keine Gleichsetzung mit beliebiger Eigenbeförderung. |
| ha | Eigenbeförderung >2,5–3,5 t | Nicht gegen Entgelt und Fahren nicht Haupttätigkeit; relevant für S02. |
| i | Historische gewerbliche Fahrzeuge | Anerkannter historischer Status nach nationalem Recht und nichtkommerzielle Personen-/Güterbeförderung. |

## 5. Nationale Ausnahmegruppen des Artikels 13

Art. 13 ermöglicht Ausnahmen, gewährt sie nicht selbst. Mit Ausnahme des theoretischen Katalogs wird in diesem Plan **keine vollständige historische Länderabdeckung als bereits geprüft behauptet**. M0 beschafft nationale Primärverfügungen einschließlich zwischenstaatlicher Vereinbarungen. Fehlende Einträge oder eine Datenbankabfrage mit leerer Rückgabe beweisen keine Nichtanwendbarkeit einer Ausnahme.

| Buchstabe | Möglicher Einsatz | Grundbedingungen aus EU-Katalog; nationale Zusatzbedingungen möglich |
|---|---|---|
| a | Öffentliche Stellen | Ohne Wettbewerb mit privaten Unternehmen. |
| b | Landwirtschaft/Gartenbau/Forst/Fischerei | Eigene Unternehmensaktivität, Radius ≤100 km. |
| c | Land-/forstwirtschaftliche Zugmaschinen | Entsprechender Einsatz, Radius ≤100 km. |
| d | Universaldienst-Post | ≤7,5 t, Universaldienstzustellung, Radius ≤100 km, Fahren nicht Haupttätigkeit. |
| e | Bestimmte Inseln / abgelegene Gebiete | Fläche und Verbindungen beziehungsweise Gebietsvoraussetzungen gemäß jeweils geltender nationaler Variante. |
| f | Güterfahrzeuge mit Gas-/Elektroantrieb | ≤7,5 t, Radius ≤100 km; nationale Masse-/Radiusgrenzen können enger sein. |
| g | Fahrausbildung/Fahrprüfung/Berufsqualifikation | Keine kommerzielle Personen-/Güterbeförderung; Ausnahmen können nur einzelne Personen/Artikel betreffen. |
| h | Ver-/Entsorgung, Straßenunterhaltung, bestimmte öffentliche Dienste | Einzelne Einsatzarten genau abgrenzen; nicht jeder kommunale Auftrag ausgenommen. |
| i | Nichtkommerzielle Personenbeförderung | Fahrzeuge mit 10–17 Sitzen und einschlägigem Zweck. |
| j | Zirkus/Jahrmarkt | Spezialisierte Fahrzeuge. |
| k | Mobile Bildungsprojekte | Spezielle Ausstattung, primärer stationärer Bildungszweck. |
| l | Milchsammlung/Rücklieferungen | Vorgeschriebener Agrarbezug. |
| m | Geld-/Werttransport | Spezialisierter Einsatz. |
| n | Tierische Abfälle/Kadaver | Nicht für menschlichen Verzehr bestimmte Güter. |
| o | Innerhalb bestimmter Umschlaganlagen | Ausschließlicher Anlagenbetrieb; kein beliebiger Nahverkehr. |
| p | Lebendtiere Hof/örtlicher Markt/örtlicher Schlachthof | Vorgeschriebene Relation, Radius ≤100 km. |
| q | Baumaschinentransport | Bauunternehmen, Radius ≤100 km, Fahren nicht Haupttätigkeit. |
| r | Lieferungen von Transportbeton | Tatsächlicher Einsatz; einzelstaatlich möglicherweise nur Art.-7-Ausnahme. |

Art. 13 Abs. 3 (genehmigte geringe Ausnahmen in dünn besiedelten Gebieten) ist ein eigener Katalogtyp, nicht automatisch durch Abs.-1-Buchstaben abgedeckt. Rechtsraum-/Zeitgrenzen und betroffene Artikel dürfen nicht von einer nationalen Ausnahme auf EU-Auslandsabschnitte übertragen werden.

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

Die materielle Verletzung entsteht ohne Schweregradzuordnung. Die Tabellenzeile muss zum konkreten zulässigen Grundwert passen, insbesondere bei befristeten Änderungen, Bus-3-h-Pausen, 25-h-Tagesruhe und normalen versus besonderen Ausgleichspflichten. Fehlende eindeutige Tabellenzuordnung heißt „keine rechtlich gesicherte Zusatzklassifizierung“, nicht eigener fünfter Schweregrad. 12-Tage-Nachruhe (E4–E7) mit teilweise speziellen Grenzzeichen wird vor ihrer Umsetzung vollständig separat gepinnt und nicht aus einer beliebigen 69-h-Summe interpoliert. Unterkunfts-/Arbeitgeberverstöße und Zuverlässigkeitsfolgen werden nicht als persönliche Zeitverstöße in diese App übernommen.

## 7. Zeitzuordnung eines Befunds

Ein Ergebnis enthält mindestens:
- betrachtetes Rechtsfenster mit Start-/Endanker und Tatzeitfassung;
- mathematischen Grenzpunkt, ersten belegten Abschnitt über der Grenze und sämtliche betroffenen Segmente;
- tatsächlichen Gesamtwert, zulässigen Wert sowie Lenkzeitüberschuss, Ruhefehlmenge oder Verspätung als jeweils verschiedene Größen;
- gesetzlich anwendbare Erleichterungen samt verwendetem Zähler und Voraussetzungen;
- genaue Quellen-IDs, Karte/Anwendung/Parent/Child/Importbelege, Kontextversion und Rechtsregel-ID;
- den Stichtag und die zur Aussage erforderliche Datenabdeckung.

**Beispiel Lenkzeit:** Bei aufgeteilten Fahrblöcken werden 25 min Überschuss als 25 min zusätzliche Lenkzeit erklärt; darin liegende Arbeit/Pausen verlängern den kalendarischen betroffenen Zeitraum, aber nicht die Überschreitungsdauer.

**Beispiel Ruhe:** 8:30 h bei erforderlichen 9 h bedeutet 30 min Ruhefehlmenge. Zusätzlich können Beginn-/Erfüllungsfrist und tatsächlicher Restzeitraum angegeben werden; eine fehlende Ruhezeit ist nicht automatisch ein lenkzeitlicher Überschuss.

Bei R01/R02 ist der späteste noch rechtzeitig mögliche Ruhebeginn ein Warn-/Grenzpunkt. Solange ein rechtlich zulässiger anderer Ruhepfad noch offen ist, wird kein endgültiger Verstoß erzeugt. Der feststehende Fehlbestand im abgeschlossenen Referenzfenster bleibt unabhängig von einem späteren ausreichend langen Ruheblock dokumentiert.

## 8. Bewusst ausgeschlossene beziehungsweise nicht automatisch beweisbare Pflichten

| Artikel / Bereich | Behandlung |
|---|---|
| Art. 5 Mindestalter des Fahrpersonals/Schaffner | Kein zeitbezogener Lenk-/Ruhetatbestand; keine neue Alters-/Schaffnerprüfung. |
| Art. 6 Abs. 2 Verweis auf Arbeitszeit-RL; Art. 8 Abs. 2a Arbeitszeitvorbehalt | Eigenständige Arbeitszeitberechnung ausgeschlossen; Vorbehalt transparent lassen. Keine Behauptung vollständiger arbeitsrechtlicher Compliance. |
| Art. 8 Abs. 8 Unterkunft/Kosten | Ausdrücklich durch Entscheidung 20 ausgeschlossen; zeitliche Wochenruhe weiter prüfen. |
| Art. 8 Abs. 8a Rückkehrorganisation durch Arbeitgeber | Bestehender Ausschluss Heimkehr/Heimstandort erhalten; keine Fahrer-Schuld aus fehlendem Heimkehrbeleg. Notwendige Staatenangaben für W07 sind davon getrennt. Die Abgrenzung dieser zeitbezogenen organisatorischen Pflicht wird vor behaupteter Gesamtvollständigkeit fachlich und mit Tobi bestätigt. |
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
| L04 | Globale Wochenzuordnung einschließlich zusätzlicher Ruhen, Ausgleichseinmaligkeit, W07/W08-Fristen, Nachruhepaket W11 und historischer Übergänge anhand unabhängiger Goldenfälle bestätigen. |
| L05 | Nationale Art.-11/13/14-Abweichungen mit Primärrecht, Wirksamkeit und tatsächlich unterstützt geprüften Gebieten katalogisieren. Kein leerer Katalog als „keine Ausnahmen“ interpretieren. |
| L06 | Umgang mit Artikel-12-Hinweisen und potenziellen Ausgleichsfolgen ohne automatische Rechtfertigung festlegen; Entscheidungen 4/62 und Grundsatz B bleiben maßgeblich. |
| L07 | Vollständige rechtlich passende Schweregradzuordnung, MSI-Voraussetzungen und Sonderregel-/Altfallgrenzen, inklusive Richtlinien-/Umsetzungsstatus. |
| L08 | Zeitlicher Aufzeichnungsstichtag, Entnahme-/manuelle Kartennachträge und Vertrauensgrenze der aufgezeichneten Ruhe: Mindestnachweise für belastbare Befunde und Budgets bestätigen. |
| L09 | Ausschluss organisatorischer Rückkehrpflicht versus vollständiger zeitbezogener Fahrerdatenprüfung explizit bestätigen; keine neue Heimkehr-/Heimradiusfunktion. |

Diese Punkte sind keine Ermächtigung, Entscheidungen zu ersetzen. Der Plan kann als vollständiger Arbeits- und Abnahmeumfang geprüft werden; die betreffenden Rechtsalgorithmen erhalten erst nach Nachweis und expliziter Umsetzungsfreigabe ihren verbindlichen implementierbaren Stand.
