# M0 – feste Daten-, Herkunfts- und Produktreferenzen

Stand 15.10.2026. Spezifikation, keine ausgeführten Adapter-/UI-/PDF-Tests. Verbindlich sind [M1-Eingangsvertrag](VIOLATION_ENGINE_M1_INPUT_CONTRACT.md), [gesetzliche Referenzen](VIOLATION_ENGINE_M0_REFERENCE_CASES.md) und die unveränderten Sicherheits-/CI-/Gerätevorgaben. IDs DT01–DT51/PF01–PF07 bleiben erhalten. Erwartungen sind aus Quellenformat, explizitem Ablauf und Produktbeschlüssen hergeleitet; technische Implementierung und unabhängiger Abgleich stehen noch aus.

## 1. Geschlossene synthetische Datenbausteine

Alle Zeitpunkte des Basistags `07.09.2026` in UTC; Minuten ab 00:00. Nur synthetische Quellen, keine echten Kartendaten. Profil P1→eigene Karte C1→Parent B1→Child I1→GEN2 V2/G2→EF0504→Original O1/Receipt E1; Roh-c und p im gespeicherten Parsermodell, keine neue Parserdefinition. Tages-/Quellenfingerprints sind als vorhandene unveränderte Identitäten zu referenzieren, nicht in M0 neu zu berechnen. G1-Parallele I2 gehört zur selben Karte/Parent und hat eigene Original-/Receipt-/Childidentität.

| Beobachtung in F1 | Minute / Zeit | Rohwort hex / Bedeutung |
|---|---|---|
| F1.0 |0 /00:00 |`0000`: eingesetzt, Solo, REST |
| F1.1 |360 /06:00 |`1968`: eingesetzt, Solo, DRIVING |
| F1.2 |480 /08:00 |`11e0`: eingesetzt, Solo, WORK |
| F1.3 |540 /09:00 |`021c`: eingesetzt, Solo, REST |
| F1.4 |600 /10:00 |`1258`: eingesetzt, Solo, WORK; **kein belegtes folgendes Ende** |

Daraus exakt: R360, D120, W60, R60; alle mit eigenem Anfangs- und jeweiligem Folgebeleg. Ab 10:00 nur punktuelle neue W-Beobachtung, keine behauptete positive Dauer. Letzter sicher geschlossener Intervallendpunkt 10:00, nicht Importuhrzeit 14:00 oder 24:00. F1 ist ein Datenvertragsfixture, kein ganzer positiver gesetzlicher Tag.

Weitere Bausteine:

- **F2**: I2/G1 hat dieselben Beobachtungen wie F1, eigene Quelle O2/E2. Gleiche tatsächliche Intervalle zählen einmal, beide Ursprünge erhalten.
- **F3**: I3/G2 hat statt F1.1 den Wechsel `1168`=WORK um 06:00, Rest wie F1. I1 und I3 widersprechen sich auf 06:00–08:00. Keine automatische Siegerquelle.
- **F4-known**: F1.3 ist `621c`, p=1/c=1/REST; I4. **F4-unknown**: F1.3 ist `221c`, p=1/c=0; I5. Folgebeobachtung 10:00 bleibt vorhanden. Known trägt R60 mit manuellem Badge; Unknown trägt U60, selbst bei aaREST.
- **F5**: Nach Entnahme um 10:00 (`3258`=p 1/c 0/WORK) fehlt jeder spätere Beleg. Ein späteres Importdatum 08.09.14:00 ist nur App-Persistenzmetadatum. Keine Folge-W/R bis dorthin.
- **F6**: Dritte Quelle I6 bestätigt den bekannten manuellen Ruheabschnitt der F4-known anstelle der früheren F4-unknown. Kein positiver Tatsachenkonflikt allein aus vorherigem U. Ändert I6 dagegen eine vorher bekannte Fahrt in Ruhe, entsteht ein Konflikt.
- **F7**: Bekannte Ruhe 25.10.2026 00:30–09:30 Z, jeweils geschlossener Endbeleg; Anzeige Berlin beginnt 02:30 MESZ und endet 10:30 MEZ. Frühjahrsvariante 29.03.2026 00:30–09:30 Z:01:30 MEZ bis 11:30 MESZ. Beide 540 reale Minuten.
- **F8**: EF0522/G1 nur Fährpunkt 07.09.12:00; G2-Beginn 12:00/Ende 20:00 als andere Quellen. Schlafplatz/Fahrplanstatus jeweils ausdrücklich true/unknown, niemals aus Markerspanne beweisen. EOF/unknown/raw codes bleiben eigene Beobachtungszustände.
- **F9**: EF0530/V2, PASSENGERS07.09.06:00; kein Linien-/Gelegenheitskontext. G1/G2 V1-Variante: Datei technisch nicht vorgesehen, kein Fehler und kein Güterdefault. Unbekannter Code 3 bleibt unbekannt,0 UNDEFINED getrennt.
- **F10**: Datensnapshot D1/K1/R1; neuer relevanter Kontext K2 für 07.09.06:00–08.09.06:00. LäufeA(D1/K1/R1), B(D1/K2/R1), B beendet vorA. NurB aktuell. Displayzone ist keine K-Revision.
- **F11**: Ledger aus LG105: Schuld 840 aus W36, Frist 28.09.00:00; Schuld 1260 aus W37, Frist 05.10.00:00. R19.09.00:00–20.09.08:00, Grund 660;1260 exklusive Zusatzminuten, keine doppelte Nutzung.
- **F12**: Lokaler Fehlerbericht: Beschreibung„Anzeige springt“, App 40128/Engineversion„noch nicht implementiert“, Android API36, Regelpaket-ID und maskierte technische Fehlerklasse optional. Synthetische verbotene Sentinelwerte `RAW_CARD_SENTINEL`, `APDU_SENTINEL`, `CARD_NUMBER_SENTINEL` dürfen weder in Datei noch Vorschau/Log ungefiltert erscheinen. Kein tatsächlicher APDU-Rohdump als Testmaterial.

## 2. DT01–DT51 – exakte Änderungen und Erwartungen

| ID | Fester Eingang / Ablauf | Erwarteter Nachweis |
|---|---|---|
| DT01 | F1 als gleicher Inhalt in I1 und I7 erneut importieren | Eine R360/D120/W60/R60-Projektion, zwei Importbezüge; kein doppelter numerischer Befund oder „Neu“-Replay. |
| DT02 | F1+F2 | Fahrt 120, nicht 240; Quellenmenge{I1, I2}. MF einmal Parent, kein dritter V2-Anwendungszweig. |
| DT03 | F1+F3 | Konflikt genau 06:00–08:00; gemeinsame übrige Intervalle normal. Kein jüngster-gewinnt. Abhängiges Fahrbudget verborgen. |
| DT04 | I8 bestätigt F1 bis 10:00 und liefert zusätzlich passende W-Beobachtung 10:00 sowie R um 11:00 | Neue belegte W-Dauer 10:00–11:00, alte Originale/Endbeziehungen unverändert; jüngere Quelle nicht als damals in I1 vorhanden ausgeben. Widersprechender Zusatz→Konflikt. |
| DT05 | F1 auf C1, F2 zeitgleich auf bereits bestätigter eigener Ersatzkarte C2; fremde C3 an P2 | Profilweite eigene Minuten einmal; C3 zurückweisen. Keine Namensähnlichkeit als neue Kartenbindung. |
| DT06 | F1 und gleicher geschlossener Datensatz 09.09., kein 08.09. | Fehlender Tag bleibt U, keine fortgesetzte Tätigkeit oder Ruhe aus 01. oder 09.Datensatz. |
| DT07 | F5 mit offenem Ende | Kein Ende 24:00/Importzeit/jetzt; bekannte frühere Intervalle nutzbar. |
| DT08 | F5 erst 20.09.14:00 importieren | Gleiche historische Abdeckung wie zuvor, kein zusätzlicher juristischer Lauf bis 20.09. aufgrund bloßer Appzeit. |
| DT09 | F7, beide Umstellungen; Anzeige UTC/Berlin/New York | Je 540 min; fehlende/doppelte lokale Stunde mit Offset/DST-Marker. Keine lokale 8/10-h-Subtraktion. |
| DT10 | RF-G1, Anzeigezone/Schicht/Filter/Sortrichtung ändern | Identische Inputrevision, Befund-/Schuld-IDs und Mengen; nur Projektion. Keine neue „Neu“-Markierung. |
| DT11 | F11 mit Bericht 01.–05.10.; Ursprung W36 liegt außerhalb 28 Tagen | Ursprung/Frist/Restpflicht trotzdem laden. Anzeigefilter schneidet den Abhängigkeitshorizont nicht ab. |
| DT12 | RF-G2 vollständig, zusätzliche U-Lücke in einer anderen Woche ohne Abhängigkeit | +90 min Tageszahlenbefund bleibt; keine pauschale Löschung. Gesamtentwarnung für lückenhaften anderen Zeitraum unterbleibt. |
| DT13 | SEQ(07.09.00:00; D300, U660, D300) | Keine gesicherte 600-Minuten-Tagesfahrt, weil U tägliche Ruhe enthalten kann. Lokaler erster Pausenüberlauf 30 min dagegen bei belegten Voraussetzungen prüfbar. |
| DT14 | F1 mit falschem profile Id/Child/card Id/Receipt-O-Digest jeweils einzeln | Betroffene Quelle ablehnen, maskierter Grund, kein Fallback auf ähnliche jüngere Quelle. Andere gültige Quellen bleiben erhalten. |
| DT15 | F10 mit Kontextwechsel K1→K2, D1/R1 gleich | Neuer fachlicher Lauf mit alter/neuer Bewertung und Grund„Kontextrevision“. Keine Originaländerung. |
| DT16 | F10, B publiziert, dann A fertig | NurB wird sichtbar/aktuell; späteA verworfen, keine vermischten Details/Budgets. |
| DT17 | F10, B-Prozessabbruch vor/während Ergebniskommit | Entweder atomarer bestätigterB oder sichtbar ausstehend; niemals halbe Listen. Kein Importverlust. |
| DT18 | F1 erfolgreich importiert, Rechenlauf Fehler/Cancellation | Import erhalten; Fehler ist kein INVALID-Signaturstatus/Rechtsverstoß und keine positive Entwarnung. |
| DT19 | Exakt F1/K1/R1 zweimal ohne Änderung auswerten | Gleiche fachliche IDs/Ergebnisse, kein zusätzliches Änderungs-/Neu-Ereignis allein aus Replay. Technische Laufhistorie darf keinen fachlichen Unterschied vortäuschen. |
| DT20 | F1 mit bestehendem SignaturstatusVALID/INVALID/NOT_CHECKABLE; TrustNOT_EVALUATED | Bestehende Mathematik/Begründung separat. Kein neuer Kryptokern/Trust; Quellenproblem regelbezogen, fehlende Signatur nicht pauschal jede Tätigkeit verwerfen. |
| DT21 | RF-HIST mit vor 20.08.2020 entstandener Schuld | Ursprungsversion erhalten; Übergangsurteil bis L01-Fachnachweis ausdrücklich offen. Kein stiller Schuldreset. |
| DT22 | LG107 plus alle Zuordnungsalternativen; gleiches Ressourcenlimit zweimal | Deterministischer Abschluss oder konkreter Hinweis„nicht zuverlässig berechenbar“; Limit erzeugt keinen geratenen Verstoß/Budgetwert. Konkretes Laufzeitbudget erst technische Umsetzung, keine neue Produktgrenze. |
| DT23 | F9 mit G1/G2 V1, V2 UNDEFINED, unbekannt 3 | Technisch nicht vorgesehen/unbestimmt/unbekannt getrennt; vorhandenes Profil, nur entscheidende fehlende Angabe fragen. |
| DT24 | F1-Inhalt im Altarchiv ohne zulässige Profilbindung | Kein automatischer Join/Backfill; getrennt erhalten, auch bei identischen Bytes. |
| DT25 | F1-Stichtag 10:00; Appuhr 20.09./offline/Neustart | Gesicherte Abdeckung bleibt 10:00; keine künstliche Fortschreibung/Fristverletzung. |
| DT26 | F10 läuftB, PDF noch ausA angefordert | Erstellung erst nach konsistentem aktuellen Snapshot; kein gemischter Bericht. Zeitraum jedes Mal neu wählen. |
| DT27 | F1-Abdeckungbis 10:00, F8 bis 20:00, F9 nur Punkt 06:00 | Keine pauschale 20:00-Aktivitätsabdeckung. Jede Regel nutzt nur hinreichend bestimmte gemeinsame Tatsachen. |
| DT28 | K2 gilt nur 07.09.06:00–08.09.06:00 | Nicht auf früheren/späteren Einsatz ausweiten; historische Revisionen bleiben, übergreifende Rechtsfenster richtig segmentieren. |
| DT29 | F4-known | R60 bekannt, manuell markiert, c=1 kein Crewbeweis; Rohwerte/Fingerprint/Quelle unverändert. |
| DT30 | F4-unknown mit aa=00/01/10/11 | Alle Folgeabschnitte U60; Punkt-aa an Entnahme nicht als Folgetätigkeit. Kein manueller Badge. |
| DT31 | F4-unknown I5, später F6 I6 | U60→belegte R60 mit beiden Bezügen/Audit; I5 nicht überschreiben. Gegen bekannte Fahrt entsteht Konflikt. |
| DT32 | F4-known in Kalender/Schicht/Zoom/Detail; Vergleich Unknown | Exakter Text„Manueller Nachtrag am EG-Kontrollgerät“, klein am richtigen Segment/Detail; verständliche deutsche Barrierefreiheit. Keine Warnungs-/Verstoßmarke aus Herkunft. |
| DT33 | Rohwörter 0168/4168/2168/6168 bei Minute 360; je Slot+8000 undaa+0000/+0800/+1000/+1800 hex; zusätzlich Entnahme+gleichminütiger Folgewechsel | Alle 32 Kombinationen p/c×Slot×aa mechanisch dekodierbar;32 semantische Erwartungen gemäß Wahrheitstabelle, nicht 32 pauschal plausible Tätigkeiten. Zeit 360, kein 1 min-Zuwachs zwischen gleichminütigen Punkten. G1/G2/V2-Fassungsabgleich L08. |
| DT34 | Crewfolge LG48 mit eigenerp 0/c 1-Karte, keine Zweitkarte/Bestätigung |30 h-Prüfung und Pause ohne Routine-Pflichtdialog; U-Folge bleibt begrenzt unklar. |
| DT35 | F8, Reise J1 mit gültigem Schlaf-/Fahrplankontext KJ1, reimportiert; neue J2 ohne Antwort | J1 keine Wiederholungsfrage; J2 nur konkret entscheidende Angabe optional gezielt, kein globaler Schlafplatzdefault. |
| DT36 | LG108/F9, unveränderte Antwort erneut; danach tatsächliche Kontextänderung | Gültige Antwort wiederverwenden; nur neue entscheidende Unklarheit fragen, K-Revision/Audit, keine pauschale Rückwirkung. |
| DT37 | LG102-Datenstände 14.09.06:00/15.09.07:00; Ansicht/Import wechseln | Dieselbe W02-Episode ab 13.09.00:00, spätere Dauer entsprechend Beleg; keine neue Episode je Tag/Import. Andere eigenständige Regelpflichten bleiben. |
| DT38 | F11, erst 1260-Schuld erfüllt, Neustart/Reimport | Erfüllte Schuld aus offener Ansicht, Ursprung/Frist/Erfüllung erhalten;840-Schuld nicht mitverschwinden. Keine doppelte Minute. |
| DT39 | F7 mit Berlin→UTC→New York |540 min zu allen Zonen, legale Werte identisch; korrekte Offsetlabels. |
| DT40 | RF-G2, kompakte Startkarte/Liste/Detail/Chronik/PDF mit Seitenumbruch | +90 min und offene Rechtsbewertung überall; kein bestätigter Grad/automatische Schuld/irreführende bestätigte Verstoßanzahl. Vollständiger Hinweis auch im PDF. |
| DT41 | LG09/12/97 konkret unsupported, daneben vollständiger RF-G2-Tag | Nur betroffene Scope-Regeln begrenzen, keine unberechtigten Befunde/Budgets dort; unabhängig belegter Tag bleibt auswertbar. Kein Spezialkatalogdialog. |
| DT42 | F11/RF-G2, Displayzone/DST/Schicht ändern | Rechtsanker/IDs/Mengen/Audit unverändert; reine Projection erzeugt keinen juristischen Lauf. |
| DT43 | F4-unknown→F6; Variante F1-D120 gegenneue R120 | Erste schließt belegbar Lücke, zweite Konflikt; atomare aktuelle Revision, beide Original-/Receipt-/Request-Fingerprints bytegleich. |
| DT44 | RF-G1/RF-G4 mit bestehenden Ruhefristen; Darstellungssituationen verschieden | Keine feste 24 h-/2 h-/Prozentauslöseschwelle. Frist-/Frühwarnfunktionen bleiben. Konkrete zurückhaltende UI-Ausarbeitung M5, kein erfundener exakter Sichtbarkeitszeitpunkt als M0-Sollwert. |
| DT45 | RF-G4-Stichtag 08.09.20:45; Appuhrändert sich, später echte neue Quelle | Ohne Quelle gleiche Warn-/Frist-/Budgetwerte, kein Livecountdown. Neue Quelle erlaubt tatsächliche Neubewertung mit Grund. |
| DT46 | F12, Netzwerk aus, Formular öffnen, Beschreibung eingeben, Vorschau, lokal speichern | Versionsdaten/Formular/Vorschau/Datei offline; Inhalt exakt Vorschau. Kein Konto/Backend/Netzwerkzwang. |
| DT47 | F12, optionale Diagnose abgelehnt/zugestimmt; danach Inhalt ändern | Ohne freiwillige Zustimmung keine optionalen Diagnosen. Geänderter Inhalt benötigt neue passende Vorschau/Zustimmung. Bericht ohne Anhang möglich. |
| DT48 | F12-Sentinelwerte in sensibler Fehlerquelle | Keine vollständigen Rohkarten/APDU; Nummer/Exceptiondaten minimiert/maskiert. Weder Zustimmung noch Fehlersituation hebt Ausschluss auf. |
| DT49 | F12-Datei speichern, Teilen bewusst auswählen, Androiddialog abbrechen, Netz kehrt zurück | Nur bewusster Share mit Content-URI/begrenztem Leserecht; Abbruch kein Versand/keine Queue. Kein automatischer Empfänger/Nachsenden. Versand durch Zielprogramm nicht als Offlinezusage. |
| DT50 | Schichtmodus, F5/Vorgeschichte U, Mitternacht/DST/Detailnavigation | Exakt„Schichtzuordnung unklar“; bekannte Aktivitäten vollständig im Kalender mit echten Zeiten/Dauern; keine erfundene Grenze. |
| DT51 | Anzeigeankerverbesserung ohne Datenänderung; danach F6 mit neuem realem Beleg | Reine Projektion: keine Rechts-/Schuld-/Zähler-/Neuänderung. Echter Faktenwechsel darf atomar neu bewerten und Audit tragen. |

## 3. PF01–PF07 – Produktfachliche Schrittfolgen

| ID | Feste Schrittfolge | Soll / Grenzen |
|---|---|---|
| PF01 | RF-G4 aufrufen; Tages-/Wochenruhefristen bei seinem Datenende zeigen; Appuhr einen Tag vorstellen | Hinweise zurückhaltend, letzte belegte Quelle unverändert. Keine festen 2 h/24 h-Stunden-/Prozentgrenzen. Kein Zukunftsverstoß aus Uhr. Die konkrete visuelle Relevanzentscheidung ist bewusst noch keine neue starre technische Konstante. |
| PF02 | F11 am 20.09.08:00; später belegter Stand 28.09.00:01 mit 840 Schuld unvollständig; Variante entscheidende Lücke | Offene Pflichten mit Ursprung/Frist schon vorher sichtbar; belegte Verspätung von bloßer Warnung getrennt. Bei Lücke unberechenbare Budgets ausblenden, keine Entwarnung. |
| PF03 | F12 offline, Beschreibung„Anzeige springt“, Diagnose abwählen, Vorschau, Datei speichern | Exakte Beschreibung/Versionsdaten; kein optionaler Anhang/Konto/Upload/Rohkarte/APDU. |
| PF04 | F12 minimierten Anhang vorschlagen, Vorschau, ausdrücklich zustimmen, speichern; später Share/Abbruch | Datei entspricht freigegebener Vorschau; kein automatischer Empfänger. Sentinelprüfung weiterhin zwingend. |
| PF05 | F5 mit unklarer Vorgeschichte im Schichtmodus öffnen | Exakter Unklarheitstext, gesamte bekannte F1-Zeitachse im Kalender; unbekannt nicht ausgefüllt. |
| PF06 | RF-G2 mit belegtem Beginn 06:00 als Kalender/Schichtansicht; zusätzlich alle RF-G2-Zeitpunkte um 17 h verschoben (Beginn 23:00, gleiche reale Dauern) | Tätigkeiten zum Datum des belegten Schichtbeginns,00:00 sichtbar; tatsächliche Zeit/Dauer/Befunde unverändert. Keine neue juristische Tagesgrenze. |
| PF07 | PF05 danach F6 und echten Beginnbeleg laden; Vergleich: nur Displayankeränderung | Anzeige kann belegbar werden; echte Faktenrevision getrennt von reiner Darstellung. Keine künstliche Schuld/Verletzung/Reset. |

## 4. Abnahmeumfang und noch offene Nachweise

Die Datenbausteine und Schritte sind in M0 festgelegt; ausführbare Fixtures, DB-/Compose-/PDF-/Gerätetests entstehen erst nach eigener Umsetzungsfreigabe. Ergebnis-ID-/Hashserialisierung und UI-Ausgestaltung sind technische Details innerhalb des Vertrags, keine offenen Produktentscheidungen. Eine grüne Docs-CI beweist keine DT/PF-Ausführung.

Vor M1 ist insbesondere L08 zu Endbelegen, gleichminütigen Wechselfolgen und exakten Quellenbindungen unabhängig abzugleichen. Vor vollständiger M1-Abnahme alle passenden DT01–DT08/DT14/DT20/DT23/DT24/DT27/DT29–DT31/DT33/DT35/DT36/DT43 sowie atomare Vertragsanteile prüfen. Spätere UI-/PDF-Anteile bleiben M5/M6-Gates; alle A01–A23 abschließend M7, keine Abschwächung bestehender CI-/Gerätevorgaben.
