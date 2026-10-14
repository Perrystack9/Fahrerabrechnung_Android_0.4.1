# FahrerTagebuch 1.0 – Änderungen am Planungsentwurf in PR #88

**Stand: 14.10.2026, Ergänzungsauftrag. Ausschließlich Planung.** Ausgangs-PR-Kopf `235839c152936d3e13a97cbc7f3c7e76de2ee1b6`, dessen reine Docs-CI38590591817 erfolgreich. Ausgangs-main unverändert `df7061659b621d67ff39ac2f02f44e13d7ac4e86`. [PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt Draft; kein Merge, keine Implementierungs- oder Releasefreigabe.

Lesepfad: [Gesamtplan](VIOLATION_ENGINE_V1_PLAN.md) → [Regelmatrix](VIOLATION_ENGINE_LEGAL_MATRIX.md) → [Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md). Verbindliche Quelle: [Ergänzungen E01–E12](VIOLATION_ENGINE_PRODUCT_ADDENDUM_20261014.md), zusammen mit den unverändert erhaltenen [80 Entscheidungen/A–H](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md).

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
| E11 | Ursprüngliche98 LG/31 DT/16 A | **116 LG/43 DT/20 A**:18 fachliche,12 technische Fälle und4 Gates ergänzt;80 PD plus12 E vollständig zugeordnet. Keine geplanten Fälle als bestanden ausgeben | Gesamte Testmatrix; A01–A20 |
| E12 | Alte M0–M7-Reihenfolge und offene P01–P05 | Pakete mit Voraussetzungen und Prüfgates; nur P03–P05 produktseitig offen. Bestehende Sicherheit/CI/Geräte/Lokalsicherungen unverändert | Plan13–16; WORK_START/STATUS/HANDOFF/CURRENT_RULES |

Unverändert:9:25 h mit verfügbarer10-h-Verlängerung legal; alle normalen zulässigen Reduzierungen/Splits/Sonderregeln anwenden; keine App-Tätigkeitsnachträge, keine Herkunfts-/Originaländerung, kein28-Tage-Berechnungslimit, kein Statusfilter, lokaler deutscher PDF-Bericht und unabhängige Fachprüfung/Beta.

## 2. Tatsächlich offene Produktfragen

| ID | Zur Prüfung | Vorgeschlagene Lösung, noch nicht beschlossen | Benötigt vor |
|---|---|---|---|
| P03 | Anzeige-/Frühwarnschwellen | Wochenruhe≤24 h vor erforderlichem Ruhebeginn; tägliche Warnung≤2 h vor letztem legalem Ruhebeginn, jeweils Datenstichtag | M5 |
| P04 | Bewusster Zustellweg des Beta-Fehlerformulars | Lokaler Berichtsexport und Android-Teilen; kein neues Backend ohne Auftrag | M6 |
| P05 | Schichtdarstellung bei unbelegbarem Schichtbeginn | Belegten geeigneten Ruhe-Endanker verwenden; andernfalls ungeklärte Zuordnung zeigen, kein erfundenes Datum | M5 |

**Geschlossen:** P01 durch E04, P02 durch E07. Kein erneuter Grundsatzbeschluss zu Crew-Beweislast, manuellem Badge, aktivem Spezialkatalog, Busumfang oder Heimkehrüberwachung nötig. Hinweise/Schwellen ändern keine gesetzlichen Grenzen.

## 3. Noch erforderliche juristische Nachweise

Diese Punkte sind fachliche Arbeiten und Freigabegates; sie bedeuten keine offene Produktwahl. Weder die Planung noch die Docs-CI erledigt eine unabhängige Rechtsprüfung. Die Rechtsmatrix enthält Quellen Q01–Q14 und die vollständigen L01–L09.

| Punkt | Noch vorzulegender Nachweis | Abhängige Umsetzung / Gate |
|---|---|---|
| L01 | Amtliche Originalfassungen/Änderungen/Berichtigungen mit genauen Fundstellen, Wirksamkeit und belegten historischen Übergängen pinnen; bisher nur teils indexierter EUR-Lex-Volltext | Alle Rechtsgruppen, A03 |
| L02 | Rechtliche Wochen-/Verlängerungszuordnung und Bus-Nachtfenster fachlich bestätigen; technische UTC-Woche nicht ungeprüft mit juristischer Auslegung gleichsetzen | M2–M4, A08 |
| L03 | Original C(2011)3759 und Guidance7 für getrennte Tageslenk-/Ruheanker vollständig bestätigen | M2/M3, D06/R04 |
| L04 | Unabhängig berechnete gesamte Wochenabläufe: alternative Zuordnung,23 h/fortbestehende Episode, mehrere Schulden, exakte Fristen und internationale/Bus-Pakete | M3/M4, A09/A19 |
| L05 | Unterstützte reguläre Rechtsräume und konkrete Art.3/11/13/14-Abgrenzung gegen Primärrecht prüfen; transparente Guard-Erwartungen | M1/M2, A03/A10; kein Vollkatalog seltener Betriebe |
| L06 | E07-Texte/Zahlenauslöser/Absatzgrenzen und offene rechtliche Bewertung inklusive PDF unabhängig rechtlich prüfen; keine Schuld aus Kandidaten | M5/M6, A20; P02 bleibt entschieden |
| L07 | Tatzeitgenaue passende Schweregradtabellen, deren besondere Voraussetzungen und Geltungs-/Umsetzungsstatus | M5, A12 |
| L08 | Technische Appendix-Fassungen G1/G2/V2, Entnahmenotiz/c-Bit, bekannte Nachträge und belastbare Datenstichtage/aufgezeichnete Crewzeiten vollständig bestätigen | M1, A04/A17/A18 |
| L09 | Beschlossene Ausschlüsse organisatorischer Heimkehr/Unterkunft juristisch korrekt erklären; gezielte Art.9- und W07-Voraussetzungen davon abgrenzen | M0/M5, keine neue Standort-/Heimkehrfunktion |

Vor Release zusätzlich tatsächliche unabhängige fachkundige Gesamtprüfung mit dokumentierten Feststellungen/Behebungen sowie geschlossene Beta mit ungefähr5–10 Berufskraftfahrern, zunächst Lkw. Busregeln benötigen trotzdem vollständige fachliche/technische Abnahme. Eine neue unabhängige Engine-Gesamtabnahme und Beta-Ergebnisse sind im Repository nicht dokumentiert; externe Beauftragungen werden nicht aus dem Chat vorausgesetzt. Dieser Dokumentationsauftrag ermächtigt nicht zur Kontaktaufnahme oder Datenweitergabe.

## 4. Reihenfolge und Freigabegrenzen

M0 prüft Quellen/Auslegung und schließt Goldenfälle. Erst nach gesonderter ausdrücklicher Implementierungsfreigabe: M1 Eingang/Herkunft/Scope → M2 Lenkzeit/Pausen → M3 Tagesruhe/Fähre/Crew/Bus → M4 Wochenruhe/Ausgleich → M5 Ergebnis/Audit/Ansichten → M6 PDF/Beta-Werkzeuge → M7 Gesamtprüfung/Beta und separate Releaseentscheidung. Die [Paketmatrix im Gesamtplan](VIOLATION_ENGINE_V1_PLAN.md) nennt die jeweiligen Regel-/Test-/Abnahmeabhängigkeiten; betroffene M0-Nachweise müssen vor ihrem Rechenkern geschlossen sein. P03/P05 vor M5, P04 vor M6.

Paketübergreifende funktionale A-Kriterien werden mit ausdrücklich benannten Teilnachweisen erfüllt: Herkunft in M1, Rechtsberechnung in M2–M4, UI in M5, PDF in M6. Spätere Anzeige-/PDF-Tests blockieren nicht rückwärts die Eingangsdatenarbeit; vor M7 sind alle Kriterien insgesamt vollständig erforderlich.

Alle alten technischen Gates bleiben: sieben Gradle-Aufrufe, echte Compiler-Schemavergleiche, SQLCipher-/Keystore-/Herkunftsschutz, ungekürzte API36-SQLCipher-/Compose-Instrumentierung, APK-/Signatur-/Updatepaarnachweise, Abschlussgate und eigene Gerätebestätigung bei relevanten Änderungen beziehungsweise spätestens jedem dritten Produkt-PR. Reine Docs-CI ersetzt keinen dieser späteren Produktnachweise.

**Aktuell keine Engine implementiert und kein neuer LG/DT/PD/E-/Gerätefall ausgeführt.** Sicherheits-/Herkunftsabschnitte ab „Kartenidentität und unveränderliche Herkunft“ in CURRENT_RULES bleiben bytegleich zum Ausgangskopf. Room22, versionCode40128, Kartenquellen, Signaturmathematik und erwartetes Vertrauen `NOT_EVALUATED` sind unverändert. Frühere Branches/Worktrees, offene Settings-Arbeit und hashgleiche Sicherung bleiben erhalten.

Nach Abschluss werden der endgültige PR-Kopf und dessen echte Docs-Checks in PR #88 und im Abschluss berichtet. Keine Behauptung einer fachlichen Abnahme oder Veröffentlichungsfreigabe aus grünen Dokumentationschecks.
