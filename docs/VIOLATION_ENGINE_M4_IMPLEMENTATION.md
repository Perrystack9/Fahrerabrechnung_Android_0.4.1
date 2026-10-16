# M4 – Wochenruhe, Ausgleich und Sonderregeln

**Technischer M4-Prüfkandidat vorbereitet; lokale Endgates erfolgreich, noch keine M4-Abnahme.** Ausdrückliche Freigabe im vierseitigen PDF-Auftrag vom16.10.2026, SHA256 `3d582e399156a9d41db3ca02cafbe384c397674670357642c8957570a0a76b35`. [M3-Integration und erfülltes Eingangsgate](M3_INTEGRATION_20261016.md): Merge `ba0cf1933f8d01590d8fdef32526f1715ec8d571`, volle Main-CI312/Versuch1 mit vier erfolgreichen Jobs.

Branch `feature/v1-m4-weekly-rest-20261016`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m4`. PD01–PD80/A–H/E01–E12/P01–P05 und spätere ausdrücklich dokumentierte Präzisierungen bleiben verbindlich. Kein M4-Merge, M5–M7 oder Release genehmigt.

## Freigegebener Umfang und Eingang

W01–W14 aus der bestehenden [gesetzlichen Matrix](VIOLATION_ENGINE_LEGAL_MATRIX.md):45-/24-h-Wochenruhe,6×24-h-Beginnfrist, überlappende Zweiwochenpflichten, eindeutige Wochenzuordnung, einzelne Ausgleichsverpflichtungen und zusammenhängende Einmalanrechnung, internationale Doppelreduzierung/Vierwochenpflicht sowie datierte Bus-Zwölftageregel mit Nachruhe und Nachtbedingungen.23h bleiben gegebenenfalls Tagesruhe und erzeugen weder Wochenreset noch automatische Ausgleichsschuld.

M4 konsumiert denselben gefrorenen M1-Snapshot und bestehende M2-/M3-Ergebnisse einschließlich der schon vorhandenen Ruhe-/Fährkandidaten und echten Endbelege. Keine zweite Parserarchitektur, keine Änderung der Originale, Signaturen oder Herkunft. Keine Systemuhr, Live-Daten oder Anzeigezone als Rechtsquelle.

## Gemeinsame Zuordnung und Datenvertrag

1. Physische Ruheintervalle, rechtliche Grundruhe, zugeordnete Kalenderwoche und zusätzlicher Ausgleich sind getrennte belegte Objekte. Ein Ursprung bleibt über Karte/Anwendung/Import/Original/Receipt erreichbar.
2. Zusätzliche24–<45-h-Rast ist nicht automatisch eine Pflichtreduzierung. Alternative Wochen-/Ruhe-/Ausgleichszuordnungen sind gemeinsam anhand der Normvoraussetzungen zu prüfen. Keine reine FIFO-/Greedyregel oder Minimierung von Verstoßzahlen als unbelegte Rechtsgrundlage.
3. Jeder Ausgleich behält Ursprung, Umfang, Frist, zusammenhängende Reservierung und Erfüllungsbelege. Dieselbe Zusatzminute darf nicht zwei Verpflichtungen erfüllen. Nicht fristgerechte spätere Erfüllung beseitigt keine frühere belegte Fristüberschreitung.
4. Stabile W02-Episode pro tatsächlichem alten Wochenanker; Fortschreibung nach belegtem Datenstand statt Tages-/Importduplikaten. Neue belegte Wochenruhe kann einen neuen Folgeanker ermöglichen, ohne ungeklärte Vergangenheit nachträglich zu heilen.
5. M3-Tagesruhereduzierungen und M2-Lenkverlängerungen bleiben getrennt. Wechselwirkungen der Grundruhe-/Ausgleichsanrechnung werden ausdrücklich geprüft; unbestätigte L04-Zuordnungen dürfen keine gesicherten abhängigen Tagesruhebudgets erzeugen.
6. Gemeinsame Veröffentlichung hinter dem bestehenden abschließenden M1-Revisions-CAS. Kontextwechsel, Reimport, Abbruch und Ressourcenbegrenzung dürfen keine veralteten oder willkürlich ausgewählten gesicherten Teilergebnisse veröffentlichen.

Abgeleitete Resultate und deterministische Revisionierung verwenden die bestehende Architektur. Keine neue Datenbankstruktur ist derzeit vorgesehen; eine tatsächliche Notwendigkeit wäre gesondert zu begründen und vollständig zu migrieren/prüfen. Keine neuen Produktdialoge, UI-Verstoßliste, Schweregrade oder PDF-Ausgabe.

## Fachnachweise und feste Referenzen

Primärquellen und Wirksamkeit werden vor den betroffenen Regeln gegen das bestehende M0-Register abgeglichen. L01/L02/L03/L04/L05/L09 bleiben konkret nachweisabhängig; insbesondere ist das technische UTC-Wochenraster keine juristisch bestätigte Rechtswoche. Q08 ist eine Kommissionsauslegungshilfe, kein unabhängiges Rechtsgutachten. Grenzfälle und nicht zuverlässig belegte Auslegungen bleiben ausdrücklich offen/bedingt.

Verbindlich: LG58–LG80, LG101–LG108 und LG114–LG116 sowie DT37/DT38 und A07/A09/A19. Erwartungswerte stammen aus den vorab hergeleiteten M0-Referenzen; neue Frist-/Solverfälle werden unabhängig von der produktiven Berechnung gerechnet. Volle und fehlende Historie, offene Enden, manuelle Nachträge/Unknown, Mehrfachimporte, Herkunftskonflikte, UTC/DST/Anzeigezone, verschlüsseltes Reopen, Revision/CAS und getrennte Zähler sind zu unterscheiden.

## Entwicklungs- und Prüfablauf

Zuerst Fach-/Quellenabgleich und Referenzen, dann kleine Implementierungsschritte und gezielte lokale Prüfungen. Bedeutende Zwischenstände werden regelmäßig auf dem Entwicklungsbranch gesichert und als unvollständig gekennzeichnet. Der eigene PR wird zur abschließenden vollständigen Prüfung vorbereitet; bis dahin keine unnötigen PR-CI-Abbruchschleifen.

Vor der finalen CI: sämtliche geplanten Änderungen abschließen, exakten Kopf/Baum festlegen und einfrieren. Alle sieben vorgeschriebenen Gradle-Gates, Unit/Lint/APK/Signatur/Updatepaar/Schema/Migration und vollständige API36-SQLCipher-/Compose-Suiten unverändert ausführen. Keine Filter oder Quarantäne in den Abnahmegates. Bei tatsächlichem Fehler nur die belegte Ursache korrigieren, dann neuen festen Kopf vollständig prüfen. Das endgültige CI-/Paket-/Geräteprotokoll gehört in den M4-PR; keine nachgeschobenen Quellcommits während eines laufenden finalen Prüflaufs.

## Implementierte Komponenten und Grenzen

- `weekly/WeeklyRestCalculator` konsumiert denselben M1-Snapshot und vorhandene M2-/M3-Ergebnisse. `WeeklyAssignments` trennt physische Ruhe, Grundruhe, Wochenrolle und zusätzlich reservierten Ausgleich. Die chronologische Suche verwendet begrenzte Iteratorrahmen; kein JVM-Rekursionsabbruch bei langen Tätigkeitsfolgen.
- `CompensationAllocator` erhält zulässige vollständige Schuldteilmengen, Reihenfolgen sowie Grundruhe vor/zwischen/nach ganzen Ausgleichsblöcken. Jede Minute genau einmal, andere Grundruhe mindestens9h. Fristgerechte und verspätete Erfüllung behalten Ursprung und Belege. Zusätzliche Rast erzeugt nicht automatisch eine Schuld.
- `WeeklyRules` prüft jedes überlappende Zweiwochenpaar und einschlägige rollierende Vierwochenfenster. Wochenrollen, Ausgleichslage, Fristen und determinierbare M3-Tagesbedingungen werden gemeinsam gelöst. Keine pauschale Priorität oder kleinste Verstoßzahl. Bereits in allen Alternativen feststehende alte Nichterfüllung löscht spätere unabhängige Pflichten nicht.
- Internationale Doppelreduzierung verwendet konkrete vorhandene0506-/0528-Punkte und minimale Kontextangaben, zwei ganze Ausgleiche unmittelbar vor dem regelmäßigen Grundblock und gesonderte Kalenderfristen. Keine automatische Länderextrapolation oder doppelte Frage. Bus-Zwölftageprojektion besitzt historische2010-/2014-/2024-Voraussetzungen,90-/69h-Nachruhe und an vorhandene M2-Perioden gebundene Nachtbedingungen.
- M3s bestehender Rechner wird mit separaten `DailyRestAttribution`-Reservierungen wiederverwendet. Physische Quellintervalle und Endbelege bleiben unverändert. Der gemeinsame Ergebnisverbund enthält sämtliche korrelierten Varianten; ein isolierter Ledger ist kein gesichertes Gesamturteil.
- Der bestehende `DrivingCheckRepository` publiziert M2/M3/M4 gemeinsam hinter dem abschließenden M1-Revisions-CAS. Reimport/Kontextwechsel/Abbruch bleiben abgesichert. Keine abgeleitete Datenbankpersistenz, keine Migration; generischer vorhandener verschlüsselter Kontextcodec speichert zwei minimale SERVICE-Flags für bestätigte Busvoraussetzungen.

**Unklare Ruhegrenzen:** Ein sichtbarer24–<45h-Abschnitt ohne belegten tatsächlichen Beginn ist nur eine bedingte Möglichkeit und erzeugt keine neue Ausgleichsschuld. Bekannte Nicht-Ruhe unmittelbar davor oder ein konfliktfrei belegtes Ende eines gesonderten Transferkontexts kann den Beginn belegen. Unbekannte Vergangenheit und wiederholte Ruhe-/Mitternachtsmarker beweisen weder Beginn noch tatsächliches Ende. Die eigentlichen Originaldaten werden niemals ergänzt oder geändert.

**Mengen und Zähler:** 23h sind keine Wochenruhe. Drei Tagesreduzierungen bleiben vom Wochenzähler der zwei10h-Lenkverlängerungen getrennt. Eine lediglich nach Dauer24h mögliche Wochenruhe erzeugt bei noch unbestätigter Art.8(6)-Zuordnung keinen unbedingten Tageszählerreset. Kalender-Mengenuntergrenzen rechnen mit tatsächlichen Grundruhelängen, nicht pauschal mit21h pro Reduzierung; sie sind keine zusätzlichen Ausgleichsschulden und keine nachträgliche Verlängerung der Aufzeichnung.

**Explizite fachliche Grenzen:** L02-Wochen-/Nachtzeit, L03, L04-Gesamtzuordnung und L09 bleiben offen. Allgemeine Teilung sehr langer physischer Ruhe, kombinierter Ausgleich über Fährenunterbrechungen sowie die fachlich endgültige Auswahl konkurrierender Pflichtzuordnungen sind nicht als abgeschlossen auszugeben. Suchgrenzen liefern einen ausdrücklich unvollständigen Befund ohne gesicherte Teilaussagen. Kein Gesamt-OK, keine automatische Art.12-Schuld oder Schuld-/Schweregradentscheidung. [Konkrete erforderliche Fachprüfung](VIOLATION_ENGINE_M4_LEGAL_EVIDENCE.md).

## Technische Abnahme und Testpaket

Version **40132 / Room23**, unveränderter Compilerexport23 SHA256 `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`. Keine Änderungen an Fahrerkartenparsern, Originaldaten, SQLCipher/Keystore/Signaturfunktionen, Hardware, CI/Sicherheitsgates oder Schemaexporten1–23. 801 lokale Unit-Tests ohne Fehler/Skip und alle sieben Gradle-Endgates sowie zusätzliche Signatur-/Updatepaar-/Schema-/Android-Testkompilierprüfungen erfolgreich. Die Ist-Ergebnisse stehen im [M4-Testnachweis](VIOLATION_ENGINE_M4_TEST_EVIDENCE.md).

Nach allen lokalen Korrekturen wird ein fester Entwicklungsstand im eigenen M4-PR vollständig geprüft. Dessen Beschreibung dokumentiert den exakten Produktkopf/Baum, CI-Lauf/Versuch und vier Jobresultate, die Paket-/APK-Prüfsummen und verbleibende Geräte-/Fachgates. Nach Beginn dieser Prüfung keine neuen Quellcommits. Erfolgreiche normale CI bleibt ohne Artefaktupload; ein anschließend separat gebautes Testpaket muss denselben unveränderten Quellbaum verwenden und Signatur/Version nachweisen.

Die Test-APK wird ausschließlich als Update der vorhandenen **FahrerTagebuch Test**-Installation bereitgestellt; keine Deinstallation, kein Datenlöschen und keine erneute Installation der40121-Basis verlangen. Die automatisierte40121/Schema18-Updatepaarprüfung bleibt davon getrennt erhalten. Eine eigene40132-Gerätebestätigung steht aus;40131 deckt diesen Kandidaten nicht ab. Keine sichtbare M4-Verstoßoberfläche erwarten: M5/M6 sind nicht begonnen.

**Freigabeempfehlung:** Nach vollständiger grüner technischer CI und eigener Gerätebestätigung kann Tobi die technische M4-Abnahme im tatsächlich dokumentierten Umfang prüfen. Die benannten Rechts-/Zuordnungsgrenzen verhindern eine uneingeschränkte juristische Gesamtfreigabe. Kein M4-Merge, M5–M7 oder Release ohne neue ausdrückliche Freigabe; unabhängige qualifizierte Fachgesamtprüfung und Beta5–10 Fahrer bleiben verbindlich.
