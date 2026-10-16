# M4 – Wochenruhe, Ausgleich und Sonderregeln

**Begonnene Arbeit, noch keine M4-Abnahme.** Ausdrückliche Freigabe im vierseitigen PDF-Auftrag vom16.10.2026, SHA256 `3d582e399156a9d41db3ca02cafbe384c397674670357642c8957570a0a76b35`. [M3-Integration und erfülltes Eingangsgate](M3_INTEGRATION_20261016.md): Merge `ba0cf1933f8d01590d8fdef32526f1715ec8d571`, volle Main-CI312/Versuch1 mit vier erfolgreichen Jobs.

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

**Entwicklungszwischenstand:** Quellenabgleich und konkrete L04-Grenzen im [M4-Fachbeleg](VIOLATION_ENGINE_M4_LEGAL_EVIDENCE.md). Domainmotor mit getrennten Wochenruhe-/Schuld-/Reservierungsobjekten, gemeinsamer chronologischer Alternativsuche, UTC-Fristen, internationalen Vierwochenfällen und datierten Bus-Voraussetzungen/Nachruheprojektionen. Konkrete0506-/0528-Länderpunkte über amtliche NationNumeric-Semantik, mit Konflikten und vollständiger Herkunft; keine Parseränderung. Bestehender M3-Rechner verarbeitet getrennte Anrechnungsreservierungen, keine veränderten physischen Quellen. M2/M3/M4 werden gemeinsam hinter demselben abschließenden Revisions-CAS veröffentlicht.40132/Room23 in Vorbereitung.

Letzte tatsächlich konfigurierte lokale Prüfung `:domain:testDebugUnitTest :data:compileDebugAndroidTestKotlin --stacktrace --no-build-cache` erfolgreich:270 Domain-Tests,0 Fehler/Skip; vollständige Android-Testkompilierung. Eine vorherige App-Kompilierung war ebenfalls erfolgreich. Auch Langruhe-/Ledger-/Länder- und Iteratorreferenzen sind erfolgreich;98 neue Domainmethoden und13 neue Androidmethoden. Der erste komplette Root-Unitlauf enthält789 Tests,0 Fehler/Skip; Lint und Debug-Build sind erfolgreich. Die restlichen lokalen Gates laufen noch. Die Schlussdurchsicht ergänzt noch die Absicherung eines nicht belegten tatsächlichen Ruhebeginns; **dies ist kein eingefrorener Endprüfkopf**. Ein lokaler Gradle-JDK-Transformcache wurde vom unveränderten Werkzeug als verändert abgewiesen. Genau dieser generierte Cache wurde außerhalb des aktiven Caches erhalten und anschließend vom Werkzeug neu erzeugt; kein Produkt-/CI-/Sicherheitscheck geändert. Der folgende Lauf war erfolgreich.

Neue Fachfälle: LG58–LG80/LG101–LG108/LG114–LG116 in Domainreferenzen, gesonderte Guards für Scope/Herkunft/Lücken/Mehrfahrer/Länder/Fähre sowie neue SQLCipher-/Reopen-/CAS-/Abbruchprüfungen vorbereitet. DT37-Episode und DT38-Ledger mit mehreren Verpflichtungen sind getrennt. Aktuelle Zuordnung der vollständigen technischen Gates und Ist-Ergebnisse folgt nach dem lokalen Abschluss.

**Noch unvollständig:** Absicherung unklarer Ruhebeginne, Abschluss der lokalen Endgates und GitHub-CI/Emulatorlauf. [Referenz-/Testzuordnung](VIOLATION_ENGINE_M4_TEST_EVIDENCE.md). Kein M4-Abnahmebeleg, PR oder Gerätepaket. L02/L03/L04/L09 und übrige Grenzen bleiben offen; insbesondere allgemeine Langruheteilung, kombinierte unterbrochene Ausgleichsanrechnung und definitive juristische Auswahl konkurrierender Pflichtzuordnungen nicht als erledigt ausgeben. Kein M4-Merge/M5–M7/Release. Unabhängige Fachprüfung/Beta bleiben Releasevoraussetzungen.
