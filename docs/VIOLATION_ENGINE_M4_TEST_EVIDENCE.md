# M4 – Referenzen und technische Abnahmen

Entwicklungsstand16.10.2026. [Implementierung](VIOLATION_ENGINE_M4_IMPLEMENTATION.md), [Quellen und offene Fachgrenzen](VIOLATION_ENGINE_M4_LEGAL_EVIDENCE.md). Kein M4-Merge, keine Release-/juristische Gesamtfreigabe. Zahlenfälle stammen aus den vorab veröffentlichten [M0-Referenzen](VIOLATION_ENGINE_M0_REFERENCE_CASES.md); ihre unabhängige qualifizierte Fachabnahme bleibt offen.

## Tatsächlich implementierte Referenztests

Alle Tests konsumieren synthetische Aufzeichnungen über die vorhandene M1-Semantik. Exogene Schulden in isolierten Ausgleichstests sind ausdrücklich fest bezeichnete M0-Ledger-Eingänge, keine vom geprüften Motor selbst erzeugten Sollwerte. Intervalle UTC/halboffen; Kalenderzahlen bleiben L02-Projektionen.

| Referenz / Abnahmepunkt | Konkrete Klasse / Prüfung | Feste Sollwerte und Grenze |
|---|---|---|
| LG58–LG59, A07/A09 | `WeeklyRestReferenceTest` |45/45 und45/24, benötigte21h-Schuld; jedes überlappende Paar; fehlende Internationalvoraussetzungen bleiben offen. |
| LG60–LG61, LG115, A07 | `WeeklyRestReferenceTest` |144h ab tatsächlichem Ende bis neuem Beginn;0/+1Minute; spätere Ruhe löscht damalige Verspätung nicht. |
| LG62–LG63, A09 | `WeeklyRestReferenceTest` | Eine physische45h-Ruhe nur einmal zählen; unabhängige Acht-Kombinationen-Wahrheitstabelle für drei Wochenüberlappungen, genau drei zulässige Projektionszuordnungen. Zwei verschiedene Ruhen dürfen derselben Woche zugerechnet werden. |
| LG64, LG103–LG104 | `CompensationReferenceTest`, `WeeklyRestReferenceTest` |24/30/44:59→21/15/0:01h Ausgleich;23:59 keine Wochenruhe;45h keine Schuld. W37/W38-Ursprung→05./12.10.00:00UTC als getrennte Fristvarianten. |
| LG65–LG68, LG105, A09 | `CompensationReferenceTest` | Vollständiger En-bloc-Ausgleich, Frist exakt/+1Minute;9+21h zusammenhängend,10+11h getrennt nicht;32h können nicht zwei21h-Schulden zahlen;46h mit11h-Grundruhe bieten35h für14+21h.30−21=9h, keine pauschale11h-Grundruhe/erfundene vierte Tagesreduzierung. |
| LG69–LG75, LG106–LG108 | `InternationalWeeklyReferenceTest` | Extra24h nicht dritte Schuld;21+20 vor45=86h, Gesamtbeginn103h/45h-Beginn144h; falscher Gesamtbeginn144h→geordneter45h-Körper41h=2460min verspätet. Beiderseitige Länderprüfung, alle Vierwochenfenster, offene Zukunft nicht verletzt. |
| LG76–LG80, LG116 | `BusWeeklyReferenceTest` |288h−1/288h/288h+1min;90/69/45h-Nachruhe;179/180/181min Art.7-Nachtprojektion; historische2010/2014/2024-Voraussetzungen, laufender Dienst über Änderungsrand bedingt. Crew ersetzt keinen Digitalnachweis. |
| LG101–LG103, A19 | `WeeklyRestReferenceTest` |23h geeignet tägliche, keine wöchentliche Ruhe; keine22h-Schuld; eine55h-Wochenepisode statt Tagesduplikaten. |
| LG114, A09 | `WeeklyRestReferenceTest` | Zusätzliche24h neben ausreichenden45h-Ruhen erzeugen keine in allen legalen Alternativen feststehende weitere21h-Schuld. |
| DT37, A19 | `WeeklyRestReferenceTest`, `WeeklyRestInstrumentedTest` | Stabile alte Anker-/Episoden-ID bei Mehrfachimport/G1+G2; kein Reset durch reine Darstellung. |
| DT38, A09 | `WeeklyGuardRegressionTest`, `WeeklyRestInstrumentedTest` | Mehrere eigene Schulden, ein vollständiger Zahlungszeuge und eine andere offene Schuld; disjunkte Listen, Ursprung/Frist/Zahlung nach verschlüsseltem Reopen, Replay und Kontextrevision. Alternative Ledger nicht miteinander vermischen. |
| Daten-/Scope-/Fährguards, A07 | `WeeklyGuardRegressionTest` |0506/0528 am konkreten Beginn, keine Zeitextrapolation; widersprüchliche/fehlende/Sammelcodes offen; Wochen- und Tagesartikel getrennt. Fähre netto,≤60/>60min, historische2020-Grenze und fehlender Fahrplan. Mehrwöchige Ruhe ausdrücklich L04-bedingt. |
| UTC/DST, Ressourcen und Abbruch | `WeeklyRestReferenceTest`, `WeeklyGuardRegressionTest` | Gleiche Revision/Mengen bei UTC/Berlin/NewYork über Zeitumstellung; explizite Suchlimits ohne gesicherte Teilergebnisse;1.200 Ruheabschnitte ohne JVM-Rekursionsgrenze; Cancellation weiterwerfen. |
| M2/M3/M4-Integration, Herkunft/Sicherheit | `WeeklyRestInstrumentedTest` | Derselbe eingefrorene Snapshot, finale Revisions-CAS, keine veraltete Kontext-/Importpublikation; Originale und beide Anwendungen erhalten; Unknown/Konflikt/Gleichminute/offenes Ende/AETR keine sichere Entwarnung. |
| Tatsächliche Ruhegrenzen / MW11–MW12 | `WeeklyGuardRegressionTest` | Links angeschnittene24h und Unknown davor erzeugen keine Schuld; bekannte Arbeit davor erhält die21h-Variante. Erneuter Ruhemarker nach24h ist kein Endbeleg. Gesonderter belegter Transfer kann den Beginn bestimmen, widersprüchlicher Kontext nicht. RED-/Zusatzruhe-L04 darf weder Tageszähler noch6×24h-Budget als gesichert zurücksetzen. |
| Tatsächliche Mengenuntergrenzen / MW13 | `InternationalWeeklyReferenceTest`, `WeeklyRestReferenceTest` |44:59/44:58/30/24h→3min;45/30/40h→29h; leeres Vierwochenfenster138h; zwei vorhandene45h-Grundblöcke erfordern noch48h reduzierte Grundmenge. Ein44h-Grundblock im Zweiwochenfenster→25h; leer69h. Keine pauschale21h je beliebiger Reduzierung, keine Ausgleichsschuld daraus. |

Die bisherigen Domain-/Daten-/Krypto-/Parser-/Migrations-/Compose-Suiten bleiben unverändert Teil der vollständigen Abnahme. Zwei bestehende synthetische Fixturehelfer nehmen lediglich den bereits vorhandenen `reviewed`-Scopeparameter entgegen; keine bestehenden Sollassertionen oder CI-Filter geändert.

## Ausgeführte lokale Entwicklungsprüfungen

Abschließender lokaler Lauf auf dem unveränderten Produktquellstand: **801 tatsächliche XML-Testausführungen /801 verschiedene Methoden,0 Fehler/0 Fehlerereignisse/0 Skip**. Domain282, Daten513, Feature5, Core1. Darin110 neue M4-Domainmethoden:29 Wochenruhe-,18 Ausgleichs-,16 internationale-,20 Bus- und27 Guardmethoden. Gegenüber dem historischen98-M4-/789-Gesamtstand wurden zwölf gezielte Referenzen ergänzt.

| Tatsächlich ausgeführtes lokales Gate | Ergebnis | Laufzeit Sekunden |
|---|---|---:|
| `./gradlew help --stacktrace` | exit0 |24.42|
| `./gradlew build --dry-run --stacktrace` | exit0 |25.79|
| `./gradlew test --stacktrace` | exit0,801/801 ohne Fehler/Skip |79.85|
| `./gradlew lint --stacktrace` | exit0 |324.05|
| `./gradlew assembleDebug --stacktrace` | exit0 |147.08|
| `./gradlew assembleInternal --stacktrace` | exit0 |359.90|
| `./gradlew :app:assembleDeviceTest --stacktrace` | exit0 |90.75|
| `bash scripts/build_device_test_update.sh` | exit0,40121/Schema18→40132/Room23 |225.00|
| `bash scripts/verify_room_schemas.sh` | exit0,Exporte1–23 unverändert |6.79|
| `./gradlew :data:compileDebugAndroidTestKotlin :app:compileDebugAndroidTestKotlin --stacktrace` | exit0,beide vollständigen Testsätze kompiliert |30.61|

APK-Paketkennung `de.fahrerabrechnung.app.devicetest`, Version40132, minSdk26/targetSdk36/compileSdk37. Bestehendes öffentliches Testzertifikat SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`, APK-Signatur tatsächlich gültig. Die sieben Pflichtaufrufe und die zusätzlichen Gates besitzen keine Testfilter, Quarantäne oder geänderten Sicherheitsanforderungen. Neuer unabhängiger Routing-/Schemaguardlauf mit allen zehn Python-Regressionsprüfungen und Shellsyntaxprüfung erfolgreich.42 relative Markdown-Dateilinks geprüft, kein fehlendes Ziel. Der Vergleich mit M3 bestätigt unveränderte CI/Skripte/Wrapper, Parser, Kryptografie und Schemaexporte; Produktentscheidungen und gesetzliche Regelmatrix unverändert.

13 neue SQLCipher-/M4-Androidmethoden sind tatsächlich vollständig kompiliert. Daraus **keine bereits ausgeführte Emulatorprüfung ableiten**: deren autoritativer Ist-Nachweis gehört zum nachfolgenden vollständigen GitHub-CI-Lauf am eingefrorenen PR-Kopf. Exakter SHA/Baum, Lauf/Versuch, vier Jobresultate und endgültiges Paket werden in der M4-PR-Beschreibung dokumentiert. Während dieses Laufs keine Quellcommits nachschieben.

Die früheren270 Domain-/789 Root-Tests sind historische Zwischenstände am gesicherten GitHub-Kopf46132ea. Der damalige Workspace einschließlich Buildumgebung/Prüflogs war bei Wiederaufnahme nicht mehr vorhanden. Die letzte nur lokale Ruhebeginnkorrektur wurde neu ausgearbeitet und geprüft, nicht als Original rekonstruiert. Alle oben genannten Endprüfungen wurden neu durchgeführt.

Konkrete lokale Einrichtungsfehler nach Wiederherstellung: Ein erster Neuaufbau endete ohne Testresultat; die Ursache ist im verbliebenen Log nicht belegt. Der nächste Versuch scheiterte vor den Tests an noch nicht akzeptierten offiziellen SDK-Lizenzen. Diese wurden mit dem offiziellen SDK-Manager angenommen und Plattform37.0/Build-Tools36.0.0 ordnungsgemäß installiert. Der erste lokale Endgate-Anlauf scheiterte bei Build-Abhängigkeiten, weil das vorhandene System-Java17 lediglich eine Laufzeit ohne `javac` war. Ein vollständiges Temurin17.0.20.1+1 wurde gegen die amtliche SHA256 geprüft und eingerichtet; der anschließende Domainlauf ist erfolgreich. Keine Produkt-/CI-/Sicherheitsvorgabe wurde dafür geändert; noch kein M4-GitHub-CI-Lauf abgebrochen.

Entwicklungsfehler wurden konkret behoben: neue Fixture-Endzeit nach84h korrigiert; variantenbezogene2460min nicht aus einem ungekennzeichneten Gesamtminimum ablesen; Bus-Anrechnungsgrenze nicht als physisches Ende/künstliche45h-RED-Ruhe modellieren; erforderliche Wochenartikel statt Tagesartikel prüfen; keine doppelte Länder-Kontextfrage. Eine neue nullable-Long-Erwartung wurde auf den korrekten Long-Literaltyp berichtigt, die Zahl blieb518400. Ein Gradle-JDK-Transformcache wurde mit Werkzeugdiagnose erhalten/quarantänisiert und regulär neu erzeugt. Keine Gate-/Sicherheitsabschwächung.

## Verbindliche Abnahmegates und Nachweisorte

- Exakter fester M4-PR-Kopf mit allen vier erfolgreichen CI-Jobs einschließlich ungekürzter API36-SQLCipher-/Compose-Suiten; keine neue Quelländerung während dieses finalen Laufs.
- Lokale Vorprüfung aller sieben Gradle-Aufrufe, APK-/Signatur-/Updatepaarprüfung, unveränderte Compiler-Schemaexporte1–23 und Android-Testkompilierung oben erfolgreich nachgewiesen. Die GitHub-CI führt dieselben vorgeschriebenen Gates erneut aus.
- Eigenes40132/Room23-Gerätepaket und Tobis gesonderte tatsächliche Antwort. Die40131-Bestätigung gilt ausschließlich für M3.
- L02/L03/L04/L09 und weitere fallbezogene Fachpunkte gemäß M4-Fachbeleg; insbesondere Fachfreigabe der endgültigen gemeinsamen Pflicht-/Ausgleichszuordnung. Numerische Quellen-/Testarbeit ersetzt keine unabhängige Rechtsprüfung.
- A09-Anzeigen/Audit und endgültige Oberfläche gehören zu den gesonderten Folgemeilensteinen; hier werden die vollständigen korrelierten Ergebnis-/Herkunftsobjekte und deterministische Neubewertung geprüft. Keine M5-/M6-Oberfläche vorweggenommen. Fachgesamtprüfung und Beta5–10 Fahrer bleiben Releasegates.
