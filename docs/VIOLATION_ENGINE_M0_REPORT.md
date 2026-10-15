# FahrerTagebuch1.0 – M0-Bericht zur Prüfung

Stand15.10.2026. **M0-Spezifikation vorgelegt, fachliche Abnahme noch nicht vollständig erreicht. Keine Implementierungs-, Merge- oder Releasefreigabe.** PR[#88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) bleibt offener Planungsentwurf.

## 1. Auftrag, tatsächliche Basis und Ergebnis

Tobi hat am14.10.2026 ausdrücklich ausschließlich M0 für fachliche Spezifikation/Dokumentation freigegeben. Nach der Pause wurde die Arbeit am15.10. fortgesetzt. Alle80Entscheidungen/A–H/E01–E12/P01–P05 bleiben verbindlich; keine konkrete neue Produktentscheidung ist erforderlich. Frühere Formulierungen „M0 noch nicht freigegeben“ sind historisch überholt. **Ein Dokumentationsmerge muss separat vorher genehmigt werden; Programmierung abM1 benötigt weiterhin eigene ausdrückliche Zustimmung.**

Bei Wiederaufnahme live bestätigt: main `df7061659b621d67ff39ac2f02f44e13d7ac4e86`; einziger offener PR88, Draft/unmerged, Kopf `6800d79653928e258a77d1737215a960ed685fb0`; letzte Docs-CI[38607504273](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38607504273), erfolgreich, keine aktiven Läufe unter den geprüften aktuellen Runs. Dieser Ausgangskopf ist nicht der später veröffentlichte M0-Kopf; dessen tatsächlicher SHA/CI-Nachweis steht im aktualisierten PR.

40128 ist abgeschlossen und im tatsächlichen bisherigen Umfang abgenommen. Produktbaum, Room22, versionCode40128, Kartenoriginale, genaue Herkunftsbeziehungen, Signaturmathematik/TrustNOT_EVALUATED, SQLCipher/Keystore und sämtliche bestehenden CI-/Gerätevorgaben wurden nicht geändert. Keine neue APK und kein neuer Gerätetest für diese Dokumentationsarbeit. Frühere Checkouts/Branches und offene Settings-Arbeit samt Sicherung bleiben erhalten.

**Ergebnis:** Der M1-Eingangsvertrag und das fachliche Nachweispaket sind konkret prüfbar. Die Beschaffungslücke der wichtigsten amtlichen Originaltexte ist geschlossen. Eine vollständige juristische Auslegungs-/Gesamtabnahme ist nicht erreicht. Quellen-, Autorenrechen- und unabhängige Fachnachweise werden getrennt geführt; eine grüne Docs-CI ersetzt keinen davon.

## 2. Vorgelegte M0-Unterlagen

| Dokument | Konkreter Inhalt |
|---|---|
| [Quellenregister](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md) | Norm/technisches Format/Leitfaden getrennt, Originale und historische Anwendbarkeit, tatsächlicher L01–L09-Fortschritt mit Restnachweisen und Paketgates. |
| [Abrufmanifest](m0/SOURCE_RETRIEVAL_MANIFEST.md) |35 tatsächlich nachgehashte amtliche Downloadrepräsentationen mit URL/Format/Bytes/SHA256 und Extraktionshash; keine leeren202-/Fehlerantworten als Beleg. Das sind nicht35 verschiedene Gesetze. |
| [Primärquellenauszüge](m0/PRIMARY_SOURCE_EXTRACTS.md) |14 lesbare Auszüge einschließlich aktueller DE/EN-Zeitregeln, aller im benutzten DE/EN-Kopf genannten Berichtigungen, alter Busregel, technischer p/c-Definition und RTM-Abgrenzung. Keine vollständige Sammlung sämtlicher Anhänge behauptet. |
| [M1-Daten-/Herkunfts-/Anwendbarkeitsvertrag](VIOLATION_ENGINE_M1_INPUT_CONTRACT.md) | Logischer Snapshot, Quellenidentität/Endbelege, beide Anwendungen, c/p-Wahrheitstabelle, Deduplizierung/Konflikte, regelbezogene Abdeckung, minimaler versionierter Kontext, atomare Revision/Audit. Keine SQL-/Parser-/Hardwareänderung. |
| [Gesetzliche Referenzen](VIOLATION_ENGINE_M0_REFERENCE_CASES.md) |116LG-Zuordnungen mit festen Zeit-/Dauerbausteinen, historische/gegenwärtige Bedingungen und offene Urteile, aktuelle Grenztabellen, vier vollständige Mehrregelabläufe. Sollwerte aus Quellen und expliziter Arithmetik, nicht aus Engine. |
| [Daten-/Produktreferenzen](VIOLATION_ENGINE_M0_DATA_CASES.md) |51DT- und7PF-Abläufe mit synthetischen Quellen, Rohwortvarianten, bekannten/unklaren manuellen Nachträgen, Quellenkonflikten, Recompute-/Anzeige-/Diagnose-/PDF-Vertrag. Ausführung steht aus. |
| [Regelmatrix](VIOLATION_ENGINE_LEGAL_MATRIX.md), [Plan](VIOLATION_ENGINE_V1_PLAN.md), [Testmatrix](VIOLATION_ENGINE_TEST_MATRIX.md) |58 stabile Regel-IDs und23 A-Kriterien beibehalten; tatsächlichen Quellenstand, offene juristische Zeitkonvention, Paketgates und Referenzverknüpfung konsistent fortgeschrieben. |

Autorenseitige Unabhängigkeit der Sollherleitung ist nicht mit einer zweiten qualifizierten externen Prüfung gleichzusetzen. Die noch offene Fachprüfung hat keine erfundene Unterschrift, Prüferidentität oder Abnahmezeit. Die vier Gesamtfolgen prüfen zusätzlich die Abwesenheit falscher Befunde; sie ersetzen noch kein vollständiges unabhängig bestätigtes Orakel aller Solveralternativen.

## 3. Sachliche Ergebnisse und gegenüber dem Plan präzisierte Stellen

- **Historie:** Original561,1073/2009,165/2014,2020/1054 und2024/1258 mit tatsächlich unterschiedlichen Geltungsdaten; benannte DE/EN-Berichtigungen einzeln gelesen. Allgemeiner Anwendungstermin von165/2014 und dessen frühere561-Änderung nicht vermischt. Reiseblatt56Tage ist kein Engine-Berechnungslimit. Übergänge über Rechtsstandsgrenzen bleiben dort offen, wo keine sichere Auslegung vorliegt.
- **Zeitkonvention:** Reale Dauern aus UTC-Instants sind bestimmt. Die technische UTC-Woche insbesondere des RTM-Anhangs beweist allein keine endgültige juristische Wochen-/Nachtzeitzone. Referenzprojektionen weisen ihren Zeitparameter ausdrücklich aus; keine Displayzone als Rechtsregel.
- **Manuelle Nachträge:** G1/G2-Kartenstatus und c-Bit einschließlich vollständiger Entnahmenotiz nachgewiesen. Bekannte manuelle Tätigkeit ist weder Crewbeweis noch App-Nachtrag. Unbekannte Folge bleibt unbekannt. Gleichminütige Beobachtungen dürfen keine künstliche Minute erzeugen.
- **RTM/Gerätewarnung:** Vereinfachungen für Fernkommunikation/Geräteanzeigen sind kein allgemeines Rechtsorakel. Insbesondere unbekannt=Ruhe, angenommene Anfangsruhe oder vereinfachte Fähr-/OOS-Zähler werden nicht in unsere Engine übernommen.
- **Tägliche Ruhe:**7h-Konvention ausC(2011)3759 betrifft empfohlenen Tageslenkansatz bei unzureichender Ruhe, keine legale7h-Tagesruhe. Bei9+3 kann der erste9h-Teil selbst einen Tagesanker begründen.25h-Busfall erhält keine pauschale24h-Verletzung, solange ein zulässiger reduzierter Standardpfad möglich ist.
- **Wochenruhe/Ausgleich:** Physisch angrenzende Ruhe bleibt ein zusammenhängender Block.23h kann Tagesruhe, nicht Wochenruhe sein; kein22h-Ausgleich. Pflichtreduzierung und zusätzliche Rast getrennt. Beide internationalen Ausgleiche vor folgendem45h-Grundblock; keine doppelte Ausgleichsminute. Bus-Nachruhepaket mit zwei Grundperioden ist von doppelter Wochenzuordnung desselben45h-Blocks zu unterscheiden. Globale Alternativen bedürfen weiterhin Fachbestätigung.
- **Exakte Zuordnung:** Zeitpunkt des Erreichens einer Fahrgrenze, erste spätere zusätzliche Fahrt, kalendarischer Gesamtzeitraum und zusätzliche Fahrminuten sind getrennte Angaben. Eine dazwischenliegende Pause ist keine zusätzliche Lenkzeit.
- **Artikel12/Schweregrade:** Normalwert zuerst, numerischer Befund und offene Ausnahmebewertung getrennt. Keine automatische tatsächliche Schuld oder bestätigte Rechtswidrigkeitsklasse. Heutige und frühere Tabellenfamilien einschließlich Unternehmerkontext/Umsetzung nicht mischen; spezielle Bus-Nachruhe-Grenzzeichen sind ausgeschrieben.

Diese Punkte sind fachliche Konkretisierungen und Quellenkorrekturen innerhalb der beschlossenen Produkteigenschaften, keine neuen Funktionen oder Ersatzentscheidungen.

## 4. L01–L09 und verbleibende Blocker

| Punkt | Tatsächlich erreicht | Noch offen / abhängiges Gate |
|---|---|---|
| L01 | Amtliche Grund-/Änderungsakte, aktuelle DE/EN-Texte und benannte Berichtigungen gepinnt; Geltungsdaten hergeleitet. | Vollständiger Aktualitäts-/änderungsaktbezogener Abgleich und Übergangsrecht über Versionsgrenzen; historische Regelpfade vorM2–M4 nicht freigegeben. |
| L02 | UTC-Dauern, getrennte Darstellungs-/Rechtszeit und gezielte Grenzreferenzen spezifiziert. | Juristische Wochen-/Nachtzeitzone, Erweiterungszuordnung beim Wochenwechsel, Mischrechtsraum; vorD02–D04/W12. |
| L03 | Q13-Original undQ07-Fensteransatz gelesen; unabhängige Beispielarithmetik getrennt. | Fachbestätigte Konventions-/Fensteranwendung einschließlich Historie; vorD06/R04. |
| L04 | Wochen-/Schuldmengen, Fristenden/+1min, vierwöchige Pflichten,23h, Einmaligkeit und Buspakete konkretisiert. | Vollständige rechtliche Solveralternativen, Randfenster, lange Ruheblöcke und historische Übergänge unabhängig bestätigen; vorM4/teilweiseM3. |
| L05 | Minimaler Scope-Guard mit positiv belegtem reguliertem Einsatz/partieller Ausnahme/konkreter Unklarheit/unsupported beschrieben. | Tatsächlich freigabefähiges Rechtsraum-/Zeitfassungsmanifest samt relevanten Abweichungsbelegen; **M1-Anteil vor Freigabe fachlich bestätigen**, aktive Sonderregeln erst nach eigenem Beleg. Keine Vollkatalogpflicht. |
| L06 | Art.12-Zahlen-/Hinweis-/Schuldgrenzen mit amtlichem Text und Q&A abgeglichen. | Qualifizierter Abgleich der rechtlich offenen Aussage und Absatzgrenzen; vorM5/M6, NormalwertanteileM2. |
| L07 | Heutiger AnhangIII und historische Originale beschafft, heutige Grenzwerte separat ausgeschrieben. | Historische Grenz-/Umsetzungs-/MSI-Anwendbarkeit und Konkurrenz von Fensterbefunden; vorM5 rechtlicher Klasse. Kein persönlicher Bußgeldtarif. |
| L08 | Technische Grund-/aktuelle G2/V2-Fassung, Entnahmenotiz, Wahrheitstabelle,32Rohwortvarianten und konservativer Endbelegvertrag. | **M1-Blocker:** unabhängiger technischer Fassungs-/Quellen-/Endbelegabgleich der konkret benutzten Adapterableitungen einschließlich gleichminütiger Wechsel. Kein unbewiesener Tagesendabschluss als Ersatz; Unknown-Pfad bleibt verbindlich. |
| L09 | Zeitpflichten gegenüber ausgeschlossener Unterkunft/Rückkehrorganisation/Heimkehr abgegrenzt; konkrete Fähr-/Länderbedingungen separat. | M1-Kontextgrenzen bestätigen; Fähr-/Bus-/Wochenausnahmeauslegung vorM3/M4. Kein neuer Produktdialog zu Unterkunft/Heimstandort. |

**Prioritär vor M1:** L08-Adapter-/Endbelegnachweis und die eingangsrelevanten L01/L05/L09-Anteile gegen diesen Vertrag bestätigen. Das erfordert keine erneute Produktentscheidung. Die übrigen L-Punkte blockieren ihre jeweiligen Rechen-/Darstellungspakete; ein Datenträger für unbestimmte Rechtszeit darf keinen unbestätigten Rechtsdefault enthalten. Noch offene Rechtsfragen werden nicht durch Testsollwerte oder gut klingende Leitfadensätze geschlossen.

Unabhängige qualifizierte rechtliche Gesamtprüfung, dokumentierte Beta ungefähr5–10Berufskraftfahrer (zunächstLkw), Fehlerbehebung und sämtliche technischen CI-/Geräteabnahmen bleiben zusätzliche verbindliche Releasevoraussetzungen inM7. Keine solche neue Gesamtprüfung oder Beta durchgeführt; keine externe Kontaktaufnahme/Datenweitergabe.

## 5. Tatsächlich durchgeführte Prüfungen

- `git diff --check` ohne Befund; `bash scripts/verify_project_boundary.sh` bestanden.
- `python3 -m unittest discover -s scripts/tests -v`: alle10 bestehenden CI-Scope-/Schema-Guard-Prüfungen bestanden, unveränderte Skripte.
-74 getrennte reine Referenzarithmetik-/Bit-/Zeitzonenprüfungen erfolgreich: DAY-/Wochen-/Paketmengen, konkrete UTC-Endpunkte/Fristen, beide Zeitumstellungen,32p/c/Slot/aa-Rohwortkombinationen, elementare Wirksamkeitsdatum-Additionen. Hilfsrechnung außerhalb des Repositorys, **keine implementierte Verstoßregel**. Keine rechtliche Wochenzeitzone oder Fachabnahme daraus abgeleitet.
-35 Downloadrepräsentationen gegen unveränderte Bytes nachgehasht;14 Auszüge mit eigenem Hash gebunden. Q11-Seiten3–5 visuell mit Text-/Klassenspalten abgeglichen. Nicht alle umfangreichen Anhänge pauschal fachlich geprüft.

Zusätzlich erfolgreich geprüft: lokale Links/Tabellen und vollständige116LG/51DT/7PF-/80PD-/58Regel-/23Kriterien-/23Paketzuordnungen; vollständige80er-Quelldatei bytegleich und gesamter alter E/P-Addendumtext als unverändertes Präfix erhalten. CURRENT_RULES ab Herkunfts-/Sicherheitskapitel bytegleich. Beide Settings-/Backup-Dateien haben weiterhin SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`. Diese M0-Revision umfasst ausschließlich16Markdown-Dateien; keine Prüfskripte verändert. Endgültiger Veröffentlichungsbaum, konservative Docs-Klassifikation und neue GitHub-CI werden zusätzlich im PR mit tatsächlichem Kopf nachgewiesen. **Keine116LG/51DT/7PF-Engine-/UI-/PDF-Fälle ausgeführt, keine neue Produktabnahme.**

## 6. Freigabeempfehlung und konkrete Reihenfolge

**Keine vorbehaltlose M1-Programmierungsfreigabe empfehlen, solange die in Abschnitt4 genannten M1-Nachweise offen sind.** Der logische Vertrag ist vollständig formuliert und reviewbar; die verbleibende fachliche Belegabnahme darf nicht durch Programmierung vorweggenommen werden. Das ist keine neue Produktfrage. M0 liegt als konkretes Spezifikations-/Nachweispaket mit transparenten Restblockern zur Prüfung vor, nicht als vollständig juristisch abgenommenes Regelwerk.

1. M0-Bericht, M1-Vertrag und zugehörige Datenfälle prüfen; die konkret M1-relevanten L01/L05/L08/L09-Nachweise mit belegtem Ergebnis ergänzen. L02/L03/L04/L06/L07 und noch offene Sonderauslegungen für die jeweiligen Folgepakete gesperrt lassen.
2. Wenn diese Eingangs-Gates geschlossen sind, Tobi um **gesonderte ausdrückliche Freigabe des konkret begrenzten M1-Umfangs** bitten. Ein gegebenenfalls erforderlicher Dokumentationsmerge wird davor separat vorgelegt; PR88 wird jetzt nicht gemergt.
3. Nach späterer Zustimmung M1 zuerst als lesenden eingefrorenen Quellen-/Semantikadapter aus bestehenden Parent-/Child-/EF-Strukturen beginnen; anschließend Abdeckung/Deduplizierung/Konflikte/Scope-Kontext und minimale getrennte abgeleitete Laufpersistenz. Keine unbestätigten Wochen-/Ruhealgorithmen, kein zweiter Parser oder Kryptokern.
4. M1 mit tatsächlichen Adapter-/Herkunfts-/Persistenztests, unveränderter voller CI und erforderlicher eigener Geräteabnahme bestätigen; erst dann freigegebene M2-Regeln. DanachM3→M4→M5→M6→M7 mit bestehenden Abhängigkeiten und allen23A-Kriterien.

Es wurde kein Android-Produktcode, Fahrerkartenparser, Datenbankschema, Kryptografie-/Hardwarepfad, APK, Produktversion oder CI-/Sicherheitsgate verändert. Für die Entscheidung ist das fachliche Ergebnis maßgeblich, nicht eine grüne Dokumentationsprüfung.
