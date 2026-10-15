# FahrerTagebuch 1.0 – Änderungen am Planungsentwurf in PR #88

**Fortschreibung15.10.2026:** Abschnitte1–5 dokumentieren den historischen P03–P05-Finalisierungsstand amAusgangskopf6800d796. Deren frühere Aussage „M0 noch nicht freigegeben“ ist durch Tobis anschließende M0-Freigabe überholt. **Aktueller Nachweis-/Freigabestand: Abschnitt6 und [M0-Bericht](VIOLATION_ENGINE_M0_REPORT.md).** Keine historische Produktgrundlage kommentarlos ersetzt.

**Stand: 14.10.2026, finale P03–P05-Produktbeschlüsse. Ausschließlich Planung; noch keine Freigabe der anschließenden M0-Facharbeit oder Programmierung.** Ursprung des E01–E12-Abgleichs `235839c152936d3e13a97cbc7f3c7e76de2ee1b6` (Docs-CI38590591817 grün); Ausgangskopf dieses finalen Auftrags `e21d297699b1304ee53f949dcd82ca3e80e2f9c7` (Docs-CI38601301433 grün). Ausgangs-main unverändert `df7061659b621d67ff39ac2f02f44e13d7ac4e86`. [PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt Draft; kein Merge, keine Implementierungs- oder Releasefreigabe.

Lesepfad: [Gesamtplan](VIOLATION_ENGINE_V1_PLAN.md) → [Regelmatrix](VIOLATION_ENGINE_LEGAL_MATRIX.md) → [Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md). Verbindliche Quelle: [Ergänzungen E01–E12 und finaler P03–P05-Auftrag](VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md), zusammen mit den unverändert erhaltenen [80 Entscheidungen/A–H](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md).

## 1. Was sich gegenüber dem ersten Entwurf ändert

Die folgenden alten Formulierungen sind ausdrücklich überholt beziehungsweise präzisiert. Ihre vollständigen früheren Fassungen bleiben im genannten Git-Stand erhalten. Die ursprüngliche80er-Quelle wurde oberhalb ihres neuen Ergänzungsvermerks nicht umgeschrieben.

| Beschluss | Betroffene frühere Grundlage / Planstelle | Aktueller verbindlicher Plan | Regel-/Test-/Abnahmebezug |
|---|---|---|---|
| E01 | Planabschnitt1,16; Entwurf ohne neue Quelle | Spätere Beschlüsse ausdrücklich vorrangig, Änderungsnachweis und reine Dokumentationsfreigabe | A01; alle E-Zuordnungen |
| E02 | Plan4.2: zusätzliche Nachtragskennzeichnung noch nicht genehmigt; PD07/35/F | Dezent „Manueller Nachtrag am EG-Kontrollgerät“, bekannte Tätigkeit normal rechnen, unbekannt nicht Ruhe/Crew; Originale erhalten | S13; LG99/LG100, DT29–DT33/DT43; A17 |
| E03 | Plan5/6, S11/B04/R05; LG15/LG37/LG49 teils zusätzliche Bestätigung/Beweislast | Eigener zeitbezogener Crewaufzeichnung grundsätzlich vertrauen,30-h-Ruhe/Beifahrerpause; keine Routine-Anwesenheitsfrage/Zweitkarte/allgemeine Bestätigung | S11/B04/R05; LG100, DT34; A18 |
| E04 | P01 offene Frage zu Fähre-Schlafvoraussetzung; F01/LG87 | P01 geschlossen: nur konkret entscheidende fehlende Voraussetzung gezielt, gültige Antwort wiederverwenden; allgemeine Unterkunft bleibt ausgeschlossen | F01–F05; LG109/LG110, DT35; A03/A07/A14 |
| E05 | W07/W08, Plan5/6; PD19 | Internationale Doppelreduzierung einschließlich rollierender Vierwochenfenster und beiden Staaten;21+20 vor45, volle Fristen, keine automatische Schuld zusätzlicher Rast | W07/W08; LG106–LG108, DT36/DT38; A09 |
| E06 | Plan6/7, W03–W09; bislang kein expliziter23-h-Fall | Gemeinsame alternative Zuordnung, individuelle zusammenhängende Ausgleiche ohne Doppelminute,23 h nur geeignete Tagesruhe, stabile fortbestehende Wochenepisode, erfüllte Pflichten auditiert | **W14 neu**; LG101–LG107/LG114/LG115, DT37/DT38; A19 |
| E07 | Entscheidungen4/62 und ausschließliche Hinweisbehandlung B; Plan6/7/10/11, X01–X05/H01, P02; LG89/LG90/PD04/62 | **P02 geschlossen.** Numerische Normalgrenzenüberschreitung bleibt Liste/PDF mit klar offener Rechtsbewertung und1-/2-h-Hinweis. Keine automatische Rechtfertigung, definitive Rechtswidrigkeitsklasse oder Artikel-12-Schuld | X01–X06/H01; LG111–LG113, DT40, PD31/36/54/58/74 angepasst; A10/A12/A13/A20 |
| E08 | S03/B03/R06/W10–W13, Plan5/13 | Bestätigte Bus-Zeitsonderregeln/Historie vollständig, EF0530 nur nach Aussagekraft, gezielter minimaler Kontext; kein Reiseverwaltungsmodul als Voraussetzung | LG116, vorhandene Busfälle, DT36; A03/A07 |
| E09 | S05–S08 und alte vollständige Art.-3-/13-Tabellen mit Vollkatalogtestpflicht; L05/M0 | Aktiver Umfang auf reguläre Lkw-/Bus-Einsätze begrenzt; konkrete gesetzliche Nichtanwendbarkeit/unsupported Sonderfälle korrekt begrenzen. Keine aktive Implementierung sämtlicher Spezial-/Notfallkataloge | Rechtsmatrix4/5 ersetzt, LG09–LG12/LG97 angepasst, DT41; A03/A10/A14 |
| E10 | Plan6/10, S12, A08/DT09 | Eindeutige UTC-Dauern; keine zusätzliche DST-Dauerformel. Gesetzliche Wochen/Nachtfenster gesondert, Anzeigezone nur Darstellung | DT39/DT42; A08 |
| E11 | Ursprüngliche98 LG/31 DT/16 A | E-Abgleich ergänzte auf116 LG/43 DT/20 A. Finaler P-Abgleich erweitert zusätzlich um7 PF/8 DT/3 A auf116 LG+7PF/51DT/23A; ursprüngliche Fälle erhalten, alle80 PD/12 E/5 P zugeordnet | Gesamte Testmatrix; A01–A23, kein geplanter Fall als bestanden ausgegeben |
| E12 | Alte M0–M7-Reihenfolge und offene P01–P05 | E-Abgleich schloss P01/P02; der frühere Zwischenstand „P03–P05 offen“ ist jetzt überholt. Alle fünf geschlossen, M0-Fachfreigabe/Programmierungsfreigabe/Releaseentscheidung getrennt; Schutz bestehender Arbeit unverändert | Plan13–16; WORK_START/STATUS/HANDOFF/CURRENT_RULES |

Unverändert:9:25 h mit verfügbarer10-h-Verlängerung legal; alle normalen zulässigen Reduzierungen/Splits/Sonderregeln anwenden; keine App-Tätigkeitsnachträge, keine Herkunfts-/Originaländerung, kein28-Tage-Berechnungslimit, kein Statusfilter, lokaler deutscher PDF-Bericht und unabhängige Fachprüfung/Beta.

## 2. Letzte Produktbeschlüsse und explizit überholte Vorschläge

**P01–P05 vollständig geschlossen. Für diesen Engine-Plan keine weitere konkrete Produktentscheidung nötig.** Ältere Vorschläge aus dem Ausgangskopf bleiben in Git nachlesbar; sie sind keine aktuelle Implementierungsvorgabe.

| Punkt | Alter Vorschlag / Status | Vorrangiger Beschluss vom14.10.2026 | Nachweis / Paket |
|---|---|---|---|
| P03 | **Überholt:**24 h vor Wochenruhe und2 h vor letztem täglichen Ruhebeginn als feste UI-Auslöser; Status offen | Keine festen Stunden-/Prozentgrenzen. Frühwarnung/relevante Ruhefristen/offene Ausgleichsfristen bleiben, am letzten Datenstand, keine Liveüberwachung. UI innerhalb Vorgaben später technisch ausarbeiten | PF01/PF02/DT44/DT45, PD09/12/15/16/51, A21/M5 |
| P04 | **Status überholt:** lokaler Formular-/Teilenpfad nur Vorschlag, Zustellweg offen | Lokales Offline-Formular/Versionsdaten, vollständige Vorschau, freiwillige optionale Diagnose, Datei speichern und bewusst Android-Teilen. Kein Backend/Konto/automatischer Versand oder Rohkarten-/APDU-Bericht | PF03/PF04/DT46–DT49, PD44/77/79, A14/A22/M6 |
| P05 | **Präzisiert:** ungeklärter Displayanker ohne verbindlichen Hinweis; Status offen | Nur zuverlässig belegter Schichtbeginn; sonst exakt „Schichtzuordnung unklar“. Vorhandene Tätigkeiten vollständig im Kalender, Zeiten/Dauern/Originale unverändert, keine künstliche Schicht-/Rechtsgrenze. Belegte Folgeimporte können Anzeige korrigieren | PF05–PF07/DT50/DT51, PD14/27/39/40/56, A08/A23/M5 |

P01 durch E04 und P02 durch E07 bleiben geschlossen. Keine Routine-Crewbeweisabfrage, neue starre Warnschwelle, neues Backend oder zusätzliche Standort-/Schichtfunktion aus diesen Präzisierungen ableiten. Die80er-Quelle und E01–E12 bleiben erhalten und bestätigte Grundlage.

## 3. Noch erforderliche juristische Nachweise

Diese Punkte sind fachliche Arbeiten und Freigabegates; sie bedeuten keine offene Produktwahl. Weder die Planung noch die Docs-CI erledigt eine unabhängige Rechtsprüfung. Die Rechtsmatrix enthält Quellen Q01–Q14 und die vollständigen L01–L09.

| Punkt | Noch zu verifizierende Quellen / Ergebnis | Unabhängige Referenzfälle | Erforderlich vor |
|---|---|---|---|
| L01 | Q01 Grund-/Tatzeitfassung, Q02/Q03 Änderungen und relevante historische Acts/Berichtigungen vollständig pinnen; genaue Fundstelle/Wirksamkeit/Übergang, Quellenhash. Direkte Abrufgrenze bleibt bis tatsächlichem Vollbeleg offen | LG02/LG35/LG76/LG85, DT21 mit alten/neuen getrennten Erwartungen | Jeder betroffene M1–M6-Regel-/Datenvertrag; empfohlen vollständig in M0, A03 |
| L02 | Q01/Q04/Q06: rechtliche Woche, Tagesverlängerungszuordnung und Bus-Nachtfenster; technische UTC-Woche nicht ungeprüft zu juristischer Auslegung machen | LG16/LG22/LG26/LG79, DT39/DT42 | M2–M4 Rechtszeit, M5 Invarianz, A08 |
| L03 | Q13 Original C(2011)3759 und Q07 Guidance7 vollständig lesen/pinnen; getrennte Tageslenk-/Ruhefenster mit fachlicher Erwartung | LG28/LG46/LG47, ausreichende versus unzureichende Ruhe | M2/D06 und M3/R04, A03/A07 |
| L04 | Q01/Q02/Q03/Q06/Q08: Wochen-/Ausgleichsalternativen,23 h/fortbestehende Episode, internationale Reihenfolge/Fristen, Bus-Nachruhe und historische Übergänge fachlich bestätigen | LG58–LG80/LG101–LG108/LG114/LG115, DT37/DT38: ganze Kalenderabläufe, genaue Frist/+1 min, keine Doppelminute | M3 Tagesanteile/M4 Solver/Ledger, A09/A19 |
| L05 | Q01; Q09/Q10 als Wegweiser für konkret relevante nationale/temporäre Primärakte im unterstützten Umfang, keine Vollkatalogpflicht. Artikel-/Zeit-/Territorialabgrenzung und unsupported Guard dokumentieren | LG01–LG14/LG97, DT41: belegte Ausnahme/unklar/partieller Artikelumfang | M1 Anwendbarkeit/M2 normale Grenzen, A03/A10 |
| L06 | Q01/Q02/Q08: P02/E07-Hinweise,1-/2-h-Regeln/Absatzgrenzen/offene Rechtsbewertung, Normalgrenze und bedingter Ausgleich; keine Schuld aus Kandidat | LG89–LG94/LG111–LG113, DT40 für identische Übersicht/PDF-Aussage | M5 Ergebnis/Details, M6 PDF; Normalgrenzen in M2, A20 |
| L07 | Q11/Q12: passende Tatzeit-Schweregradtabellen, besondere Voraussetzungen, Geltungs-/Umsetzungsstatus und Sonderfälle vollständig verifizieren | LG95/LG96/LG98 und Grenzen jeder tatsächlich verwendeten Zeile | M5 Klassifizierung, M6 gleiche PDF-Aussage, A12 |
| L08 | Q04/Q14: vollständige technische Appendix-Fassungen G1/G2/V2, Entnahmenotiz/c-Bit/manuelle Nachträge, belegter Stichtag/Crewzeiten. Bestehende Quellenbindungen schriftlich ohne Codeänderung belegen | LG99/LG100, DT01–DT08/DT14/DT29–DT34/DT43 als feste Karten-/Semantikinputvektoren | M1 lesender Input-/Herkunftsvertrag, A04/A17/A18 |
| L09 | Q01/Q02/Q03/Q08: beschlossene Ausschlüsse tatsächlicher Heimkehr/Unterkunft fachlich korrekt erklären und von Art.9/W07-Kontext trennen; keine neue Produktfrage | PD20/61, LG87/LG108/LG110, keine allgemeine Unterkunfts-/Heimkehrbehauptung | M0 Umfangsspezifikation, M1 Kontextgrenzen, M5 Erklärungen, A03/A14 |

Alle L-Punkte sind weiterhin **offen**, bis das tatsächliche verlinkte Beleg-/Auslegungsergebnis vorliegt. Die Tabelle ist der Auftrag an die spätere freigegebene M0-Facharbeit und erklärt keine Referenzfälle für bereits fachkundig abgenommen. Geplante Vektoren dürfen nicht aus derselben späteren Engine errechnet werden.

Vor Release zusätzlich tatsächliche unabhängige fachkundige Gesamtprüfung mit dokumentierten Feststellungen/Behebungen sowie geschlossene Beta mit ungefähr5–10 Berufskraftfahrern, zunächst Lkw. Busregeln benötigen trotzdem vollständige fachliche/technische Abnahme. Eine neue unabhängige Engine-Gesamtabnahme und Beta-Ergebnisse sind im Repository nicht dokumentiert; externe Beauftragungen werden nicht aus dem Chat vorausgesetzt. Dieser Dokumentationsauftrag ermächtigt nicht zur Kontaktaufnahme oder Datenweitergabe.

## 4. Reihenfolge und Freigabegrenzen

Dieser Auftrag finalisiert ausschließlich den Plan. **Erste gesonderte Entscheidung durch Tobi: M0-Facharbeit freigeben.** Danach M0-Quellenregister, M1-Daten-/Scope-Vertrag, Recheninterpretationen und vollständige unabhängige Referenzfälle vorbereiten; L01–L09 tatsächlich belegen oder den abhängigen Umfang ausdrücklich blockieren. Empfohlen: vollständige M0-Spezifikation abschließen und Ergebnis zur Prüfung vorlegen.

**Zweite gesonderte Entscheidung: Programmierung ab M1 freigeben.** Mindestens sämtliche M1-relevanten L01/L05/L08/L09-Anteile und Eingangs-/Herkunftsreferenzen müssen geschlossen sein. Danach M1 Quellen/Semantik/Scope/Kontext/Persistenz → M2 Lenkzeit/Pausen → M3 Tagesruhe/Fähre/Crew/Bus → M4 Wochenruhe/Ausgleich → M5 Ergebnisse/Audit/UI → M6 PDF/Beta-Werkzeuge → M7 Fachgesamtprüfung/Beta und separate Releaseentscheidung. [Gesamtplan13.1](VIOLATION_ENGINE_V1_PLAN.md) konkretisiert den Start,13/14 ordnen Pakete und sämtliche23 Kriterien vollständig zu. Keine nochmals offenen Produktfragen vor M5/M6.

Paketübergreifende funktionale A-Kriterien erhalten Teilnachweise: Herkunft M1, Rechtsrechnung M2–M4, UI M5, PDF/Betaformular M6. Spätere Anzeige-/PDF-Tests blockieren nicht rückwärts die Quellenarbeit. **Zum Abschluss M7** müssen A01–A23 insgesamt erfüllt sein, einschließlich Fachgesamtprüfung/Beta A16. Alle produktbezogenen technischen Gates gelten trotzdem bei jedem betreffenden PR vollständig.

Alle alten technischen Gates bleiben: sieben Gradle-Aufrufe, echte Compiler-Schemavergleiche, SQLCipher-/Keystore-/Herkunftsschutz, ungekürzte API36-SQLCipher-/Compose-Instrumentierung, APK-/Signatur-/Updatepaarnachweise, Abschlussgate und eigene Gerätebestätigung bei relevanten Änderungen beziehungsweise spätestens jedem dritten Produkt-PR. Reine Docs-CI ersetzt keinen dieser späteren Produktnachweise.

**Aktuell keine Engine implementiert und kein neuer LG/PF/DT/PD/E/P-/Gerätefall ausgeführt.** Sicherheits-/Herkunftsabschnitte ab „Kartenidentität und unveränderliche Herkunft“ in CURRENT_RULES bleiben bytegleich zum Ausgangskopf. Room22, versionCode40128, Kartenquellen, Signaturmathematik und erwartetes Vertrauen `NOT_EVALUATED` sind unverändert. Frühere Branches/Worktrees, offene Settings-Arbeit und hashgleiche Sicherung bleiben erhalten.

Nach Abschluss werden der endgültige PR-Kopf und dessen echte Docs-Checks in PR #88 und im Abschluss berichtet. Keine Behauptung einer fachlichen Abnahme oder Veröffentlichungsfreigabe aus grünen Dokumentationschecks.


## 5. Tatsächlich ausgeführte lokale Dokumentationsprüfungen

Für diesen P03–P05-Finalisierungsauftrag tatsächlich erfolgreich geprüft:

- `git diff --check` ohne Fehler.
- `bash scripts/verify_project_boundary.sh`: Projektgrenzen bestanden.
- `python3 -m unittest discover -s scripts/tests -v`: alle10 bestehenden CI-Routing-/Schema-Guard-Prüfungen bestanden. Keine neuen Engine-/Androidtests daraus ableiten.
- Lokale Links und Tabellenbreiten;116 LG-/7 PF-/51 DT-/80 PD-/12 E-/5 P-/23 A-Kennungen sowie vollständige23er-Paketzuordnung.
- Die116 bisherigen LG- und43 bisherigen DT-Fälle unverändert; gesetzliche Matrix und80er-Quelle bytegleich zum Ausgangskopf. E01–E12 bleiben als unverändertes Bytepräfix im ergänzten Addendum.
- CURRENT_RULES ab Herkunfts-/Sicherheitskapitel bytegleich; offene Settings-Datei und Backup mit unverändertem SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0` erhalten.
- Nur neun bereits erlaubte Markdown-Dateien betroffen; keine Produktcode-/Parser-/Schema-/Hardware-/APK-/Workflowänderung und keine Abschwächung bestehender Prüfskripte.

Vor Abschluss zusätzlich konservative CI-Klassifikation, genaue veröffentlichte Blob-/Tree-Identität, Draftzustand/mergefähiger Kopf und echte GitHub-Docs-CI prüfen. Endgültiger SHA/CI-Link stehen im aktualisierten PR-Abschluss. Alle neuen LG/PF/DT/PD/E/P-/A-Nachweise bleiben geplante spätere Engine-/Produktabnahmen; **L01–L09 weiterhin offen, M0 noch nicht freigegeben.**


## 6. M0 – tatsächliche Quellen-/Spezifikationsfortschreibung15.10.2026

Tobi hat M0 ausdrücklich nur dokumentarisch freigegeben; nach Pause fortgesetzt. Keine M1-, Merge- oder Releasefreigabe.80/A–H/E01–E12/P01–P05 und alle58Regel-IDs/23A-Kriterien erhalten. Die frühere reine Quellen-Beschaffungsplanung ist jetzt durch belegte Teilresultate ersetzt, nicht durch eine behauptete Fachabnahme.

| Betroffene Planstelle | Vorheriger Stand | Tatsächliche Fortschreibung / Nachweis |
|---|---|---|
| Rechtsmatrix1/Q01–Q14 | Teils Indexbeleg/JavaScript-Prüfseite, kein vollständiger Primärpin |35 verifizierte amtliche Repräsentationen, historische Original-/Änderungsakte, aktuelleDE/EN-Texte und benannte Berichtigungen; [Register](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md), [Manifest](m0/SOURCE_RETRIEVAL_MANIFEST.md),14Auszüge. |
| S12/D02/L02 | Technische UTC-Woche/TRACE-Zuordnung als vorgesehene Umsetzung | Juristische Wochen-/Nachtzeitzone und Erweiterungszuordnung ausdrücklich noch offen. RTM-Anhang gilt nur dort; Referenzparameter keine stille Rechtsvorgabe. |
| D06/R04/L03 | Q13-Originalbeschaffung noch offen | C(2011)3759 vollständig gelesen/gepinnt: empfohlener Tageslenkansatz, keine zulässige7h-Ruhe; unabhängige Anwendungs-/Fensterbestätigung bleibt. |
| S13/Plan4/L08 | Semantik vorgesehen, technische Vollbelege offen | G1/G2/V2-p/c-/Entnahmenotiz und aktuelleG2/V2-Fassung gepinnt, Wahrheitstabelle/Rohwortreferenzen; [M1-Vertrag](VIOLATION_ENGINE_M1_INPUT_CONTRACT.md). End-/Herkunfts-/Fassungsabgleich vorM1-Gate offen. |
| W03/W11/L04 | Verbot doppelter Wochenzuordnung, Buspaket noch abstrakt | Ein45h-Block nicht doppelt; normative Bus-Nachruhe≥90/≥69h gesondert. Physisch angrenzendeR-Blöcke nie künstlich trennen; globale Auslegung noch fachlich zu bestätigen. |
| H01/L07 | Aktuelle indexierte Tabelle, frühere Familien nicht vollständig gepinnt | Q11-PDF mit visuellen Klassenspalten; historische2006/22/2009/5 und2016/403/2022/694 gepinnt. E4–E8-Grenzzeichen inRF-H. Nationale Umsetzung/Historie/Konkurrenz weiterhin offen. |
| Testvertrag LG/DT/PF | Kurzfälle ohne vollständige feste Parameter |116LG-/51DT-/7PF-Zuordnungen mit festen Bausteinen, vier ganze Mehrregelreferenzen, offene Übergangsurteile gekennzeichnet. Keine tatsächlichen Engine-/UI-/PDF-Tests. |
| Exakte Zeitzuordnung | Ein allgemeiner Grenzpunkt | Grenzerreichung und spätere erste überschüssige Fahrt getrennt; Pausen zählen nicht als zusätzlicheFahrt.9+3-/25h-Alternativpfade ohne falsche Befunde präzisiert. |
| Plan13/16 und Start/Status/Handoff/Regeln | M0 erst freigeben lassen | M0-Freigabe dokumentiert, Bericht/Restblocker vorgelegt; M1 und auch Dokumentationsmerge weiterhin gesondert genehmigen lassen. Sicherheitsabschnitte bytegleich. |

Tatsächlich ausgeführt:74 reine Referenzarithmetik-/Bit-/Zeitzonenprüfungen,10 bestehende CI-Scope-/Schema-Guard-Tests, Projektgrenzen undDiff-Whitespaceprüfung. Links/Tabellen/IDs/Originalgleichheit/Settings-Hashes und die reine16er-Markdown-Dateimenge zusätzlich erfolgreich geprüft. Konservative CI-Einstufung und endgültigen Veröffentlichungsbaum/CI im PR nachweisen. Unabhängige Fachabnahme und Produkt-/Gerätenachweise daraus nicht ableiten. [M0-Bericht](VIOLATION_ENGINE_M0_REPORT.md) benennt die verbleibenden M1-Blocker und weitere paketbezogene L01–L09-Nachweise.
