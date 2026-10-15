# FahrerTagebuch 1.0 – aktualisierter M0-Bericht

Stand 15.10.2026. **M1-Eingangsnachweise für den abgegrenzten Umfang erbracht. M1 bleibt bis zu Tobis erneuter ausdrücklicher Zustimmung gesperrt.** Keine Implementierungs-, Merge- oder Releasefreigabe. [PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt Draft.

## 1. Ergebnis und Freigabeempfehlung

**Die fachlichen Eingangsvoraussetzungen für den abgegrenzten M1-Fakten-, Herkunfts-, Abdeckungs-, Kontext- und Guardumfang sind erfüllt. Dafür kann eine gesonderte ausdrückliche Umsetzungsfreigabe empfohlen werden.** Die zuvor generisch offenen Eingangsbelege aus L08 sowie L01/L05/L09 sind jetzt in [N01–N19](VIOLATION_ENGINE_M0_M1_EVIDENCE.md) mit Quelle, Prüfung, Erwartung und Ergebnis nachgewiesen. Das ersetzt die frühere Empfehlung, M1 zunächst wegen dieser unbelegten Eingangsannahmen zurückzustellen.

M0 als vollständiges juristisches Gesamtregelwerk ist weiterhin nicht unabhängig abgenommen. Konkrete Rechtszeit-, Fenster-, Solver-, Übergangs-, Länder-, Ausnahme- und Klassifikationsnachweise bleiben vor den jeweils abhängigen Paketen offen. Eine qualifizierte externe Fachgesamtprüfung und die Beta mit ungefähr fünf bis zehn Berufskraftfahrern bleiben verbindliche Releasevoraussetzungen. Keine neue Produktentscheidung nötig; die 80 Entscheidungen/A–H/E01–E12/P01–P05 bleiben geschlossen und unverändert.

Diese Empfehlung erteilt selbst keine Genehmigung. **Kein Beginn von M1, kein Merge von PR #88.** Ein gegebenenfalls nötiger Dokumentationsmerge wird vorher separat zur Entscheidung vorgelegt.

## 2. Tatsächlicher Ausgangsstand

Bei Wiederaufnahme für diesen Folgeauftrag geprüft: main `df7061659b621d67ff39ac2f02f44e13d7ac4e86`; einziger offener PR #88, Draft/unmerged, Kopf `192c0ea3750208cba6ec18560facbd183f28b515`, Docs-CI [38679874481](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38679874481) erfolgreich; keine laufenden oder wartenden Workflows. Die untersuchten Produktdateien stimmen zwischen main und diesem PR-Kopf überein. Dies sind Ausgangswerte; endgültigen neuen Dokumentationskopf und echte Kopf-CI weist der aktualisierte PR aus.

40128 ist bereits abgeschlossen und im dokumentierten Umfang von Tobi abgenommen. Kein erneuter Kryptografieauftrag. Room22, versionCode40128, Originale, Quellenbindungen, Signaturmathematik und Trust `NOT_EVALUATED`, SQLCipher/Keystore, Produktcode/Parser/Hardware sowie sämtliche bisherigen Test-/Geräte-/Sicherheitsregeln bleiben unverändert. Frühere Branches/Worktrees, offene Settings-Arbeit und Sicherungen bleiben erhalten.

## 3. Jetzt konkret nachgewiesen

| Gegenstand | Ergebnis / Fundstelle |
|---|---|
| Technische Fassungen/Kartensemantik | G1-Original, späterer G1-Text und G2/V2-Format einschließlich relevanter Änderungs-/Berichtigungsanweisungen gegen Bestand geprüft. p/c gemeinsam auslegen; `crewDriving` bleibt Roh-c, kein Crewbeweis bei p=1. N01–N04. |
| Entnahme/manuelle Nachträge | Unbekannte Entnahmefolge keine Ruhe. Zulässige aufgezeichnete WORK/AVAILABILITY/REST-Nachträge normal berücksichtigen, Originalherkunft und exaktes dezentes Badge erhalten. p=1/c=1/DRIVING als auffälliger Rohbeleg, kein automatisch zulässiger manueller Fahrtbeleg. Keine App-Nachtragsfunktion. |
| Gleichminütige Wechsel | Parser/Speicher erhalten Reihenfolge/Status. Keine zusätzliche Minute; identische Zustände kompatibel. Unterschiedliche semantische Folgezustände ohne gesicherten Zusatzbeleg bleiben unklar; kein Indexgewinner. N05/MI05/MI06. |
| Kalenderchronologie | Tatsächlich gezeigt: Parser akzeptiert Nichtmitternachtsdatum und gegenläufige Tagesdaten. M1 prüft dies zusätzlich lesend, ohne Rohdatenkorrektur oder kaschierende Sortierung. N06. |
| Endbelege | Fünf konkrete Bedingungen für historischen Abschluss durch tatsächlichen Folge-00:00-Record; 660-Minuten-Markerfolge und fehlender Tag am echten Parser geprüft. Neuester offener Abschnitt, Kopf allein, Importzeit, EF0507 und UI-Tagesende bleiben kein Endbeleg. N07–N09/Abschnitt4. |
| Herkunft/Speicher | Echter Fingerprinter gegen unabhängig serialisierten Referenzhash geprüft; Ringplatz identitätsneutral, Karte/c/p/Reihenfolge relevant. Room22-FKs/Originalabfragen erhalten gleichminütige Punkte und exakte Quellenfilter. Beide Anwendungen über Parent/Child statt nur Primär-`all/latest`; zulässiger bestehender Raw-Fallback lesend, keine Altbindung/Backfill. N10–N14. |
| Anwendbarkeit/Kontext | Datierter Minimalvertrag zu Masse/Verkehrs-/Rechtsraum, Art.2-Erweiterung ab01.07.2026, Linienweg≤50km, AETR-Ganzreise, OOS/Teilumfang/anderer Arbeit und getrennten konkreten Fähr-/Länderbedingungen. Keine allgemeine Unterkunfts-/Heimkehrprüfung oder Routine-Crewbestätigung. N15–N19/Abschnitt5. |

Die historische Endschlussregel ist ausdrücklich eine Herleitung aus vollständiger Tages-/Statuswechselaufzeichnung und Recordstruktur, kein erfundener zusätzlicher Normsatz. Sie schließt keine fehlenden Tage oder tatsächlich offenen jüngsten Abschnitte. Die fachliche Eingangsprüfung ist von einer zweiten qualifizierten externen Gesamtprüfung getrennt.

## 4. Nachweise und tatsächlich ausgeführte Prüfungen

Vorgelegt: [Quellenregister](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md), [M1-Eingangsvertrag](VIOLATION_ENGINE_M1_INPUT_CONTRACT.md), [Nachweisprotokoll](VIOLATION_ENGINE_M0_M1_EVIDENCE.md), [reproduzierbare Bestandsproben/MI01–MI24](m0/M1_INPUT_PROBE_20261015.md). Bestehende [116 gesetzliche Referenzfälle](VIOLATION_ENGINE_M0_REFERENCE_CASES.md), [51 Daten-/7 Produktfälle](VIOLATION_ENGINE_M0_DATA_CASES.md), 58 Regel-IDs und alle 23 Abnahmekriterien/Paketzuordnungen bleiben erhalten.

- **15 JUnit-Bestandsproben bestanden:** drei bestehende unveränderte Parser-Tests plus zwölf synthetische Quellen-/Fingerprint-/Endpunkt-/UI-Abgrenzungsproben. Unveränderte Quellen außerhalb des Repositorys kompiliert; alle32 Rohwortkombinationen, Entnahme/Nachtrag, gleiche Minute, Kalenderdaten, Folgetagsmarker und offene UI-Enden geprüft. Kein neuer Engineadapter.
- **12 Speicherstrukturprüfungen bestanden:** unveränderter Room22-Compilerexport und exakte DAO-SELECTs in frischer SQLite-memory-Datenbank; Reihenfolge, Quellenfilter, FK-/Löschschutz und Schemahash geprüft. Kein Room-/SQLCipher-/Migrations-/Gerätelauf.
- **43 amtliche Downloadrepräsentationen gepinnt**, acht mehr als zuvor; sechs direkte HTTPS-Wiederabrufe mit identischen Hashes. Zwei zusätzliche G1-Texte und elf zusätzliche exakte Primärauszüge, insgesamt25. Keine pauschale Fachprüfung sämtlicher Anhänge oder globale Aktualitätsgarantie. [Abrufmanifest](m0/SOURCE_RETRIEVAL_MANIFEST.md), [neue Primärstellen](m0/M1_SOURCE_EXTRACTS_20261015.md).
- Die bisher berichteten74 reinen Referenzarithmetik-/Bit-/Zeitzonenprüfungen bleiben frühere Autorennachweise, keine zusätzlichen Engine-Tests oder rechtliche Wochen-/Nachtabnahme.

Dokumentationsprüfungen dieser Fortschreibung: `git diff --check`, Projektgrenze, zehn bestehende Python-CI-Scope-/Schema-Guard-Tests; lokale Links/Tabellen/IDs, Quellen-/Auszughashes, unveränderte80er-Quelle, vollständiges Addendumpräfix, Sicherheits-/Herkunftsteil und Sicherungen. Konkreter erfolgreicher Abschluss, veröffentlichter Baum und Kopf-CI werden im PR belegt. Keine Produktprüfskripte/CI-Vorgaben geändert.

**Nicht ausgeführt:** neuer M1-Adapter, echte neue Room-/SQLCipher-/Reopen-/Migrations-/Hardware-/APK-Abnahme, LG-/DT-/PF-/MI-Engine-/UI-/PDF-Tests oder externe Fachprüfung. MI01–MI24 sind feste spätere Adapterabnahmevektoren. Bestandsproben ersetzen keine solche Produktabnahme.

## 5. Verbleibende fachliche Nachweise und echte Blocker

| Nachweis | Abschluss für M1 | Konkret noch offen / Gate |
|---|---|---|
| L01 | Benutzte technische Fassungen, Versions-/datierte Scope-Eingangsfakten autorenseitig belegt | Rechtsakt-Aktualität, historische Übergänge laufender Pflichten und konkrete Länderübernahmen vor aktiver M2–M4-Rechtsableitung. |
| L05 | Minimaler E09-Scope-/Kontextguard abgeschlossen | Amtliche EEA-/CH-/historische und nationale/temporäre Übernahme-/Abweichungsakte samt Artikel-/Zeit-/Gebietsumfang vor aktiver Anwendung. Unbelegte Konstellation konkret ungeklärt tragen, kein Ausnahmenvollkatalog. |
| L08 | Begrenzter konservativer Eingangsvertrag nachgewiesen | Nach Implementierung echte Adapter-/Persistenz-/Gerätetests vor M1-Abnahme. Stärkere Gleichminutenauflösung bräuchte gerade deren technische Reihenfolge-/Folgezustandsbestätigung; für ein offenes jüngstes Ende fehlt ein tatsächlicher Endzeitbeleg. Beides kein erlaubter Default. |
| L09 | Grenzen des konkreten Tatsachenkontexts bestätigt | Fähr-/Bus-/Wochenanrechnung vor M3/M4, Art.12-Ergebnisaussagen vor M5/M6 qualifiziert prüfen. |
| L02 | Unbestimmte Rechtszeit darf M1 unverfälscht tragen | Geografische Rechtswoche/Nacht, Verlängerungszuordnung beim Wochenwechsel und Mischrechtsraum vor betroffenen M2–M4-Regeln fachlich bestätigen. Technische UTC-Woche ist kein Ersatz. |
| L03 | Kein Reset-/Ruhealgorithmus in M1 | Anwendung der7-h-Vollzugskonvention und täglichen Fenster-/Ankerreferenzen vor D06/R04 in M2/M3 fachlich bestätigen. |
| L04 | Kein Wochen-/Ausgleichssolver in M1 | Wochenalternativen, Randfenster, zusammenhängende/exklusive Ausgleichsminuten, sehr lange Ruhe,23h und Bus-Nachruhepakete mit unabhängigen Referenzen vor M3/M4 bestätigen. |
| L06/L07 | Keine Art.12-Anerkennung oder rechtliche Klasse in M1 | Absatz-/Tatzeitgrenzen, rechtlich offene Aussagen und tatsächlich umgesetzte Schweregradanwendbarkeit vor M5/M6; Normalgrenzenanteil vor M2. |

[Nachweisabschnitt6](VIOLATION_ENGINE_M0_M1_EVIDENCE.md) benennt erforderliche Art der externen Prüfung und abhängige Gates. Kein externer Prüfer wurde erfunden, beauftragt oder kontaktiert. Datenmangel blockiert abhängige Budgets/Entwarnungen; er hindert M1 nicht, gerade diesen Mangel korrekt und quellengenau darzustellen. Offene spätere Rechenfragen sind keine neuen Produktfragen. Unabhängige qualifizierte Gesamtprüfung/Beta sowie alle technischen Releasegates bleiben M7.

## 6. Empfohlene Reihenfolge nach gesonderter Zustimmung

1. Tobi prüft Bericht/N01–N19/M1-Vertrag und entscheidet gesondert über genau den abgegrenzten M1-Umfang. PR #88 bleibt bis zu seiner Entscheidung Draft; nötigen Dokumentationsmerge separat vorlegen.
2. Danach M1 mit eingefrorenem lesendem Snapshot beider eigenen Anwendungen samt genauer Parent-/Child-/Original-/Receiptidentität beginnen; vorhandene Parser/Stores/Fallbacks verwenden.
3. Semantik, Datum/Chronologie, belegte Enden/Abdeckung und kompatible Deduplizierung/Konflikte umsetzen; danach getrennten versionierten Kontext/Scope-Guard. MI01–MI24 und passende bestehende DT-Fälle tatsächlich ausführen.
4. Nur benötigte getrennte abgeleitete Persistenz ergänzen; nötige additive Migration aus echtem Compilerexport mit allen bestehenden SQLCipher-/CI-/Gerätegates abnehmen. Keine vorgezogene ungeklärte M2–M4-Regel.
5. M1 erst nach seinen echten technischen Abnahmen schließen. Weitere M0-Fachnachweise vor ihren abhängigen Paketen erbringen; unabhängige Gesamtprüfung/Beta und separate Releaseentscheidung bleiben M7.

Ausschließlich Planungs-/Nachweisdokumentation geändert. Keine Implementierung, kein Merge, keine Produkt-, Parser-, Schema-, Kryptografie-, Hardware-, APK-, Versions- oder Workflowänderung.
