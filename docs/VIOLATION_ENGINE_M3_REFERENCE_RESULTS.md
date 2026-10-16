# M3 – Zuordnung fester Referenzen und tatsächlicher Prüfungen

Stand16.10.2026, Entwicklungsstand vor der technischen Gesamtprüfung. Sollwerte aus den vorab hergeleiteten M0-Fällen; keine vom produktiven Rechner erzeugten Testorakel. Synthetische Tätigkeiten durchlaufen den vorhandenen M1-Semantik-/Intervallbuilder; frühere Reduzierungen entstehen aus tatsächlich aufgezeichneten synthetischen Tagen.

## Tatsächlich ausgeführter Domainlauf

165 JUnit-Methoden erfolgreich:84 bestehende M1/M2 und81 neue M3. Ausführung mit Kotlin2.3.21 aus der SHA256-geprüften Gradle9.6.1-Distribution und JUnit4.13.2. Dieser isolierte Nachweis ist ausdrücklich noch kein projektkonfigurierter Kotlin2.4.20-, Android-, SQLCipher-, CI- oder Gerätebeleg. Die vollständigen Projektergebnisse werden im Abnahmebericht separat eingetragen.

| Feste Referenz | Implementierte Methode/Klasse und Soll, durch Assertions geprüft |
|---|---|
| LG40/41 | `DailyRestReferenceTest.LG40…`/`LG41…`:780+660=1440;781+660=1441,659min innerhalb24h. Freie Reduzierung nutzen; nach drei belegten Reduzierungen60s fehlen. Grenze13:00/erste Minute13:01, tatsächliches Ende+24h1min. |
| LG42/43 | `LG42…`/`LG43…`:vier900+540-Tage; vierter120min Fehlmenge. Beobachtete Zähler0/1/2/3 ohne Montagsreset. |
| LG44/45 | `LG44…`, drei`LG45…`:3+9 ergibt720min;179/539 verfehlen den jeweiligen Teil.9+3 löscht eine vorher gültige9h-Ruhe nicht. |
| LG46/47 | `LG46…`/`LG47…`:960+660→480 statt540 im ersten Fenster,60min fehlen, tatsächliches Ende+27h. Drei1080+360-Fenster je180min Fehlmenge, Q07 ausdrücklich gekennzeichnet. |
| LG48/49/100 | `LG48…`/`LG49…`/`LG100…` und `RestGuardRegressionTest.PrepWork…`:Crew9h in30h;1261+540 lässt539min,60s fehlen.61min belegte Solofahrt schließt Crew aus; vorbereitende Arbeit startet nicht die optionale Fahrstunde. Unbekannt/manuelles c-Bit ist kein Crewbeweis. Frühere Crew-Beifahrerbereitschaft plus spätere Solofahrt erzeugt keine unbewiesen neue sichere Anfangsstunde (L09). |
| LG50–53/98 | `RestSpecialReferenceTest.LG50…`/`LG51…`/`LG52…`/`LG53…`/`OrdinaryOnTimeDays…`:420 Fahrt+45 Pause+375 Arbeit+660 Ruhe=1500.6-Tage-Dienst einmal,8-Tage-Dienst zweimal; nächste Ausnahme nicht verfügbar.421 Fahrt oder5 Tage scheitern. Unbekannter Sicherheitsvorbehalt bleibt bedingt. Normale rechtzeitige24h-Tage verbrauchen keinen Ausnahmeplatz. |
| LG54/101–103, Tagesanteile | `LG54…`/`LG101…` und `RestCandidatesTest.LG101And103…`:23h/1439min kein Wochenreset;1440/2699/2700 nur zeitliche Wochenkandidaten.45h bilden ihren tatsächlichen vollen Endanker. M4-Zuordnung bleibt gesperrt, vorhandene Ursprungsereignisse bleiben erreichbar. |
| LG55/56/57/99 | `LG55…`/beide`LG56…`/`LG57…`/`LG99…` und `FuturePossibleRest…`/`RecordedRestIsCredited…`:bei Datenstand12:00 noch3600s bis11h-Beginn;13:01 irreversible60s.0min tatsächlich aufgezeichnete Ruhe bleibt0,659min mögliche Zukunft sind nur Kapazität. Unknown660min erzeugt weder Ruhe noch gesicherte Fehlruhe/Entwarnung. Bekannter manueller Nachtrag zählt. |
| LG81/82/83/84 | `RestSpecialReferenceTest.LG81…`/`LG82…`/`LG83…`/`LG84…`:660min Netto bei2×30min Unterbrechung erfüllt;61min/3 Unterbrechungen nicht. G1 benötigt keinen erfundenen Endpunkt;270+270 unterbrochen sind keine reduzierte9h-Tagesruhe. Fehlender konkreter Kontext bleibt bedingt. |
| LG85/86/87/110 | `RestCandidatesTest.LG85…`/`LG86…`/`LG87And110…` und Kontextrevision:unterbrochene Wochenruhe erst ab20.08.2020;479/480min geplante Fahrt sowie Kabine separat. Unbekannt→belegte Antwort bewertet neu, Original-/Tätigkeitsdaten unverändert. |
| LG88/109 | `RestSpecialReferenceTest.LG88…`/`LG109…`, `RestCandidatesTest.LG109All…`:3+9 mit unterbrochenem zweiten Teil; Nettogrenzen659/660,1439/1440,2699/2700 statt verstrichener Zeit. Konkreter659min-Fährfall:19:00 mathematische Grenze,19:01 erste Fehlminute. |
| Eingangs-/Sicherheitsgrenzen | `RestGuardRegressionTest`, weitere Kandidaten-/Tagesmethoden:Scope/AETR/50km, vor2007, konkrete EF0530-Kontextwidersprüche ohne zeitliche Extrapolation, offene echte Enden, Datenkonflikt/Unknown, spätere bekannte Teilzeit, Kontexttransfer ohne Originaländerung, UTC/DST/Zonenwechsel, Reimport/alle Ursprünge, Abbruch und Ressourcenlimit ohne garantierte Teilresultate. |

## Vorbereitete echte Androidnachweise

`DailyRestInstrumentedTest` enthält zehn Methoden für echte SQLCipher-Datenbank, vorhandene Parser/Importpipeline und beide Kartenanwendungen:Manual/Replay/Herkunft;Unknown;drei frühere Reduzierungen;gemeinsamer M2/M3-Flow;neue Kontextschlüssel/Reopen/verschlüsselter Header/Integrität;finaler CAS;Quellenkonflikt;AETR;gleiche Minute;offenes Ruheende. **Erfolgreich im Projekt kompiliert; noch kein bestandener Emulatorlauf aus dieser Liste ableiten.**

A03/A07/A08 und RuheanteilA18/TagesankeranteilA19 sind diesem M3-Teilnachweis zugeordnet. UI-, Wochenledger-, PDF- und Releaseanteile bleiben M4–M7. Unabhängige qualifizierte Fachprüfung und Beta5–10 Berufskraftfahrer werden durch diese Autorentests nicht ersetzt.
