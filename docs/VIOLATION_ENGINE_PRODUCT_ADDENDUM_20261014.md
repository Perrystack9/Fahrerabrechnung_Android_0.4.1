# FahrerTagebuch 1.0 – verbindliche Ergänzungen vom 14.10.2026

**Quelle:** Tobis Nachricht „FahrerTagebuch 1.0 – Verbindliche Ergänzungen und Abschlussauftrag zur Verstoß-Engine“, vollständig in dieser Work-Sitzung übergeben. **Auftrag:** bestehenden Draft-[PR #88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88) fachlich fertigstellen; ausschließlich Planungsdokumentation, keine Implementierungs- oder Releasefreigabe.

Die folgenden E01–E12 entsprechen den zwölf Abschnitten dieser neuen Nachricht. Sie dokumentieren sämtliche darin beschlossenen Anforderungen in geordneter Form; sie sind keine Umnummerierung oder Ersetzung der [historischen 80 Entscheidungen und A–H](VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md). Bei Widersprüchen haben diese neueren ausdrücklichen Beschlüsse Vorrang vor älteren Produktformulierungen. Gesetzliche Vorgaben bleiben maßgeblich. Betroffene alte Planstellen und Freigabepunkte stehen in der [Änderungsübersicht](VIOLATION_ENGINE_PLAN_CHANGES_20261014.md).

## E01 – Ausgangssituation und Auftrag

Die im separaten Produktberatungschat abgeschlossenen fachlichen Themen werden aus dieser tatsächlich übergebenen Quelle übernommen. PR #88 erhält eine konsistente Gesamtplanung, korrigierte gesetzliche Regelmatrix, Architektur und Umsetzungsspezifikation, fachliche und technische Testmatrix, Abnahmekriterien, ergänzte Produktdokumentation sowie aktualisierte offene Fragen und Freigabevoraussetzungen. Historische Entscheidungen werden nachvollziehbar ergänzt oder als überholt gekennzeichnet, nicht kommentarlos umgeschrieben.

## E02 – Manuelle Nachträge am EG-Kontrollgerät

Manuell am digitalen Fahrtenschreiber nachgetragene Tätigkeiten werden erkannt und im Zeitstrahl dezent gekennzeichnet. Der verbindliche Wortlaut lautet:

> Manueller Nachtrag am EG-Kontrollgerät

Die Kennzeichnung ist klein und unaufdringlich oberhalb der jeweiligen Tätigkeit beziehungsweise im entsprechenden Tätigkeitsdetail. Die bereits vorgesehene semantische Auswertung erhält diese UI-Anforderung und passende Tests.

- Informationen aus EF 0504 wiederverwenden; Kartenstatus und c-Bit gemeinsam interpretieren.
- Das bestehende Feld `crewDriving` nicht unabhängig vom Kartenstatus als Mehrfahrerkennzeichen verwenden.
- Bei entnommener Karte bekannte, manuell am Kontrollgerät eingetragene Aktivitäten von unbekannten Zeiträumen unterscheiden.
- Ordnungsgemäß aufgezeichnete Nachträge rechtlich wie entsprechende andere aufgezeichnete Tätigkeiten berücksichtigen.
- Unbekannte Zeiträume nicht als Ruhe annehmen.
- Originaldaten unverändert lassen; FahrerTagebuch bietet weiterhin keine eigene manuelle Tätigkeits-Nachtragsfunktion.

## E03 – Mehrfahrerbetrieb anhand vorhandener Aufzeichnungen

Korrekt aufgezeichneter Mehrfahrerbetrieb wird grundsätzlich für die betreffenden Zeiträume verwendet. Die Tachographendaten sollen grundsätzlich ausreichen; eine übermäßig aufwendige Nachweisführung ist nicht gewünscht.

- Keine routinemäßige Nachfrage nach der tatsächlichen Anwesenheit eines zweiten Fahrers.
- Keine verpflichtende zweite Fahrerkarte und keine allgemeine Nutzerbestätigung des Mehrfahrerbetriebs.
- Gesetzliche tägliche Ruhe innerhalb von 30 Stunden und besondere Fahrtunterbrechungsmöglichkeit beim Beifahrer berücksichtigen.
- Mehrfahrerkennzeichen zeitlich korrekt auswerten; ein einzelnes Kennzeichen nicht auf einen vollständig unbekannten Zeitraum übertragen.
- Unterschiedliche c-Bit-Bedeutung bei gesteckter und entnommener Karte beachten.

Gesetzliche Voraussetzungen bleiben Teil der Regelbeschreibung. Diese Entscheidung genehmigt keine abweichende Rechtsregel, sondern die Verwendung der vorhandenen Aufzeichnungen ohne zusätzliche routinemäßige Beweisabfragen.

## E04 – Fähre und Zug

Die bisherige fachliche Planung ist bestätigt. EF 0522 wiederverwenden und die zulässigen Unterbrechungen berücksichtigen:

- höchstens zwei Unterbrechungen, zusammen höchstens 60 Minuten;
- erforderliche tatsächliche Ruhezeit ohne die Unterbrechungen;
- Unterschiede zwischen täglicher Ruhe, reduzierter und regulärer Wochenruhe;
- zusätzliche gesetzliche Voraussetzungen der jeweiligen Ausnahme.

Die Kennzeichnung allein beweist nicht alle Voraussetzungen. Bei entscheidenden fehlenden Angaben keine gesicherte Bewertung erfinden. Zusätzliche Angaben nur gezielt für konkret betroffene Bewertungen erfragen, keine wiederkehrenden Standardfragen nach jedem Import. Bei unverändertem Sachverhalt bereits vorhandene Antworten wiederverwenden. Die allgemeine Prüfung regulärer Wochenruhe im Lkw, Hotel oder zu Hause bleibt ausgeschlossen. Der frühere Produktpunkt P01 ist damit entschieden.

## E05 – Internationaler Güterverkehr und zwei reduzierte Wochenruhen

Die Sonderregel gehört zu 1.0. Zu prüfen sind Voraussetzungen zweier aufeinanderfolgender Reduzierungen, alle relevanten rollierenden Vierwochenzeiträume, Arbeitgeber-Niederlassungsstaat und Fahrer-Wohnsitzland, beide Ausgleichspflichten, vorgeschriebene Reihenfolge der Ausgleichsblöcke und anschließenden regulären Wochenruhe sowie Fristen.

Beispiel: 24 und 25 Stunden angerechnete reduzierte Wochenruhe erzeugen grundsätzlich 21 beziehungsweise 20 Stunden Ausgleich. Im gesetzlichen Sonderfall liegen diese unmittelbar vor der anschließenden regulären Wochenruhe.

Entscheidende fehlende Länderangaben dürfen gezielt erfragt werden. Bei unverändertem Sachverhalt Fragen nicht wiederholen. Zusätzliche Ruhe von 24 bis unter 45 Stunden nicht automatisch als neue ausgleichspflichtige Reduzierung behandeln.

## E06 – Automatische Ruhezeitzuordnung und Ausgleich

1. Tatsächliche Tätigkeiten und Ruheblöcke chronologisch auswerten.
2. Für alle möglichen Zuordnungen gesetzliche Fristen und Voraussetzungen beachten.
3. Eine zwei Kalenderwochen überschneidende Wochenruhe nur einer Woche zuordnen.
4. Jede offene Ausgleichspflicht mit eigenem Ursprung, Umfang und Frist verwalten.
5. Dieselbe zusätzliche Ruhestunde nicht doppelt für zwei Ausgleiche verwenden.
6. Einen vorgeschriebenen zusammenhängenden Ausgleich nicht in kleine Abschnitte zerlegen.
7. Rechtlich zulässige Alternativzuordnungen gemeinsam prüfen und nachvollziehbar auflösen.
8. Bereits belegte Verstöße durch spätere Ruhe nicht rückwirkend ungeschehen machen.

**23 Stunden Ruhe:** erfüllen keine reduzierte Wochenruhe, können aber tägliche Ruhe sein. Tageslenkzeit- und Tagesruheberechnung korrekt fortsetzen, ohne eine Wochenruhe zu buchen. Ein fortbestehender Wochenruhemangel erzeugt nicht jeden Folgetag denselben Verstoß neu. Rechtlich eigenständige Pflichtverletzungen bleiben prüfbar.

Offene Pflichten mit Fristen anzeigen. Erfüllte Pflichten verschwinden aus der offenen Übersicht, bleiben nachvollziehbar. Vollständiger Folgeimport oder relevanter Kontextwechsel löst erforderliche Neubewertung aus.

## E07 – Artikel 12: rechnerische Überschreitung mit offener rechtlicher Bewertung

Diese Entscheidung ersetzt beziehungsweise präzisiert insbesondere Nr. 4 und Nr. 62 sowie die damit widersprechende ausschließliche Hinweisbehandlung aus Grundsatz B und dem bisherigen Planungspunkt P02.

Außergewöhnliche Abweichungen werden nicht automatisch als gerechtfertigt anerkannt. **Rechnerische Überschreitungen normaler gesetzlicher Lenkzeitgrenzen bleiben in der Verstoßübersicht.** Bei zahlenmäßig passenden Überschreitungen im Bereich der besonderen Ein-/Zwei-Stunden-Heimkehrregel folgt ein verständlicher Zusatzhinweis. Das übergebene Beispiel lautet:

> Lenkzeitüberschreitung: +1 Stunde 30 Minuten

> Unter bestimmten außergewöhnlichen Umständen kann eine Abweichung nach Artikel 12 der VO (EG) Nr. 561/2006 zulässig sein. Ob die gesetzlichen Voraussetzungen erfüllt sind, wurde von FahrerTagebuch nicht geprüft.

Die Details erläutern korrekt: Rückkehr zum Wohnort/Betriebszentrum für die vorgeschriebene Wochenruhe, Ein-/Zwei-Stunden-Unterschied, zusätzliche Fahrtunterbrechung bei der Zwei-Stunden-Regel, Verkehrssicherheit und außergewöhnliche Umstände, gesetzliche Dokumentation, gegebenenfalls Ausgleich zusätzlicher Lenkzeit sowie nicht aufgehobene Grenzen.

Eine passende Zahl beweist weder tatsächliche Nutzung noch Rechtfertigung. **Rechnerische Feststellung und abschließend rechtlich bestätigter Verstoß sind klar zu unterscheiden**, auch in Übersicht und PDF. Ohne ausreichende Tatsachen keine Klassifizierung als gesichert rechtswidrig und keine automatisch angenommene tatsächliche Artikel-12-Ausgleichsschuld.

Nicht vorgesehen: Pflichtbegründung, manuelle Artikel-12-Anerkennung, automatische Prüfung tatsächlicher Heimkehr, zusätzliche Standortüberwachung oder automatische Rechtfertigung bei höchstens zwei Stunden. Kein zusätzlicher Statusfilter. P02 ist produktseitig geschlossen; Quellen-/Auslegungsprüfung der gesetzlichen Hinweise bleibt erforderlich.

## E08 – Busfahrer: bestätigter Umfang

Relevante Unterschiede zwischen Linien- und Gelegenheitsverkehr bleiben enthalten:

- Linienverkehr bis einschließlich 50 km;
- besondere Pausenaufteilung im Gelegenheitsverkehr, etwa 30 + 15 Minuten;
- gesetzlich begrenzte 25-Stunden-Regel;
- Zwölftageregel mit sämtlichen weiteren Voraussetzungen;
- historisch richtige Anwendung der VO (EU) 2024/1258.

EF 0530 bei GEN2V2 nach seiner tatsächlichen Aussagekraft verwenden. Linien-/Gelegenheitsart bei Bedarf gezielt erfragen. Keine umfangreiche Reiseverwaltungsfunktion als Voraussetzung entwickeln. Lkw-Fahrer bleiben erste, Busfahrer zweite Zielgruppe.

## E09 – Schlanker Funktionsumfang und rechtliche Anwendbarkeit

Primär regulärer professioneller Lkw-Güterverkehr, sekundär Busfahrer. Keine vollständige Benutzerführung und aktive Regelimplementierung für sämtliche seltenen Art.-3-/13-/14-Spezialbetriebe oder umfassende nationale/befristete Ausnahmekataloge. Alte umfangreiche Katalogpflichten sind entsprechend überholt.

Gesetzliche Geltungsbereiche ändern sich dadurch nicht. Bei nachweislich nicht anwendbaren oder nicht zuverlässig einzuordnenden Einsätzen keine unberechtigten VO-561/2006-Verstöße behaupten. Vorhandenes Out of Scope korrekt berücksichtigen, ohne den Modus pauschal als Rechtsnachweis zu behandeln. Nicht unterstützte Sonderkonstellationen und die Grenzen ihrer Prüfbarkeit transparent abgrenzen. Keine neuen umfangreichen Einrichtungsdialoge für seltene Einsatzprofile.

## E10 – UTC und Sommer-/Winterzeit

Tatsächliche Zeitdauern konsequent mit UTC-Zeitstempeln beziehungsweise eindeutig bestimmten Intervallen rechnen. Keine zusätzliche Sommer-/Winterzeit-Sonderberechnung realer Lenk-/Ruhedauern. Einstellbare Anzeigezone ausschließlich für Darstellung. Rechtliche Wochen und andere kalendarische Tatbestände unabhängig davon nach ihren gesetzlichen Definitionen behandeln. Zeitumstellungen im Zeitstrahl weiterhin markieren; ein technischer Grenztest muss fehlerhafte lokale Uhrzeit-Subtraktion ausschließen.

## E11 – Verbindliche Teststrategie und Releasequalität

Die ursprünglichen 98 fachlichen Mindestfälle, 31 technischen Fälle und 16 Abnahmekriterien bleiben Ausgangsbasis und werden um alle Ergänzungen erweitert. Besonders nachzuweisen:

- bekannter Kontrollgeräte-Nachtrag gegenüber unbekannter Kartenentnahmefolge; c-Bit mit beiden Kartenstatus; dezente sichtbare UI-Kennzeichnung;
- Mehrfahrer anhand vorhandener Karte ohne unnötige Pflichtabfragen;
- 23 Stunden Ruhe mit korrekter Fortsetzung; Wochenruhe Sonntag/Montag;
- Ausgleich genau zum Fristende/eine Minute danach; mehrere Schulden; keine doppelte Nutzung;
- internationale zwei reduzierte Wochenruhen; Fähre/Zug einschließlich entscheidender fehlender Voraussetzungen;
- Bus-Sonderregeln und historische Rechtswechsel;
- Artikel-12-Zahlenfall mit korrekter Übersicht und PDF;
- Mehrfachimporte, Quellenkonflikte und Neubewertungen;
- fehlende Daten und unveränderte Rechtsberechnung nach Anzeigezonenwechsel.

Freigabesperren: falsche Verstöße, falsche Entwarnungen, falsche Budgets, falsche Ruhe-/Ausgleichszuordnung, Herkunftsverlust/-verwechslung oder fehlende Unterscheidung zwischen Zahlenüberschreitung und rechtlich offener Ausnahme. Unabhängige Fachprüfung und geschlossene Beta mit ungefähr 5–10 Berufskraftfahrern bleiben Releaseanforderungen.

## E12 – Ergebnis, Schutz bestehender Arbeit und Freigabegrenze

Vorlegen: konsistente Gesamtplanung, gesetzliche Matrix im beschlossenen Umfang, aktualisierte Tests und Abnahmekriterien, nachvollziehbare Änderungsübersicht, kurze Liste wirklich entscheidungsbedürftiger Fragen, sämtliche offenen juristischen Quellen-/Auslegungs-/Fachprüfungen sowie spätere Paketreihenfolge mit Abhängigkeiten und Prüfgates.

Vorhandene Implementierungen, Sicherheitsregeln, Signaturprüfung, SQLCipher/Keystore, Kartenoriginale und Herkunft bleiben erhalten. Technische Prüf-/Abnahmevorgaben nicht abschwächen. **Keine Produktcode-, Datenbankschema-, Parser-, Hardware- oder APK-Änderung. PR #88 bleibt reiner Planungsentwurf.**

Dieser Auftrag genehmigt ausschließlich Aktualisierung und Fertigstellung der Planung. Am Abschluss Änderungen und verbliebene Freigabepunkte zusammenfassen und den vollständigen Plan zur Prüfung vorlegen. **Erst Tobis gesonderte ausdrückliche Zustimmung erlaubt die Implementierung; Releasefreigabe ist nochmals getrennt.**
