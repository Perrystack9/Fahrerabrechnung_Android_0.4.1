# PR65 – gemeinsamer Kartenimport 40122: gesicherter Übergabestand

Stand 12.10.2026. **Aktueller Produktkopf gesichert; Build/JVM/Signatur/Schema bestanden. Die Emulatorprüfung wird von GitHub vor der Runner-Zuweisung abgewiesen. Keine vollständige CI-Freigabe und keine Hardwarefreigabe für 40122.**

## Aktuelle Quelle und unveränderte Sperren

| Beleg | Wert |
|---|---|
| PR | [PR65](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/65), offen / Entwurf |
| Produktbranch | `feature/card-dual-application-reader-20261012` |
| Produktcommit | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Quellbaum | `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` |
| Einziger GitHub-Parent | `faf47521529b8d3c42bec56ab8b96daa73ff4938` |
| Produktmanifest | 302 Blobs; SHA256 `72f9a96d655651389ef993efd80c49e86e07bc57bc4e32a01b126d979560afbb` |
| Kandidat / Schema | 40122 / 19 |
| Main / letzte Gerätebasis | `ace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b`, 40121 / 18 |

Manifestformat: rekursive Git-Tree-Reihenfolge, `<mode> blob <sha>\t<path>\n`, ohne `docs/`, `README.md` und `WORK_STATUS.md`. Vollständige lokale und veröffentlichte Trees abgeglichen; Ref-Updates mit erwarteter SHA und ohne Force. Keine Dateien gelöscht oder Implementierung neu begonnen.

Der ursprüngliche Arbeitsstand wurde zuerst vollständig auf `backup/pr65-storage-local-20261012` erhalten: `7e906ec`, anschließend Prüfungsdokumentation `ef886d8`. Testkorrekturen bleiben zusätzlich auf `backup/pr65-schema-assertions-local-20261012` (`78be454`) und `backup/pr65-instrumentation-fixes-local-20261012` (`90b9170`) erhalten. Lokale Snapshot-Abstammung wurde nicht nach GitHub gepusht. Die lokale GitHub-Historiengrenze am Reader-Parent f1f92d6 ist ausdrücklich als shallow markiert; die älteren lokalen Sicherungsbranches bleiben erhalten.

## Implementierter Umfang

Der normale Vordergrund-Import liest beide festen AIDs in einem gemeinsamen Karten-Lesevorgang. Gen1 und Gen2/Gen2V2 behalten eigene Identitäts-, Kapazitäts-, Zertifikats-, Quellen-, Run- und Sitzungsbelege; MF-Originale liegen am gemeinsamen Parent. Beide Anwendungsschaltflächen öffnen genau ihre gespeicherte Sitzung. Übersicht und normale Quellenlisten wählen die Primäranwendung ausdrücklich. Keine Addition beider Zeitsätze, kein jüngerer Quellenfallback und kein Backfill alter Imports.

Migration 18→19 ergänzt vier Tabellen und zwei eindeutige FK-Ziele. Eine äußere SQLCipher-Transaktion umfasst Bindung, Parent, beide Child-Sitzungen und alle Quellen. Ein Speicherfehler nach dem ersten Child rollt die gesamte neue Gruppe zurück. Zusammengesetzte FKs binden Run, Sitzung und MF-Quelle an dieselbe physische Karte. Alte Requests und historische Quellen bleiben unverändert. Erste/unklare Ersatzkarten brauchen weiterhin frische eigene Bestätigung; eindeutig fremde oder widersprüchliche Gruppen werden nicht übernommen. Gemeinsame sensible MF-Angaben werden nach Sitzungswechsel erneut verborgen.

[Integration und Grenzen](CARD_MULTI_APPLICATION_IMPORT.md) · [additive Modellentscheidung](CARD_APPLICATION_STORAGE_DESIGN.md) · [Geräteanleitung](CARD_MULTI_APPLICATION_DEVICE_TEST.md). Die technische Speicher-/UI-Integration ist kein behaupteter vollständiger Kartenimport oder Standarddownload.

## Lokale und Build-Nachweise

Lokal bestehen 452 Daten-Unit-Tests ohne Fehler/Skip. App-Kompilierung, beide Instrumentierungssuiten und DeviceTest-APK sind erfolgreich. Gegenüber Main: 27 JVM-, neun SQLCipher- und drei Compose-Testmethoden. Nach den letzten reinen Testkorrekturen wurden beide Instrumentierungssuiten erneut kompiliert; `javap` bestätigt alle drei neuen Compose-Methoden als JVM `void`. Die bestehenden Produkt-/Bindungsregeln wurden nicht verändert. Projektgrenzen, zehn CI-Guard-Tests, Shellsyntax und Diff-/Schema-Guards bestehen.

Echter Room/KSP-Export 19 vorhanden; Migration gegen alle 42 Tabellen einschließlich Spalten, PK, Indizes und FKs abgeglichen. Schema 19 SHA256 `01cdfdeee311e13efdcebd74d66783103f5bd0a8423ad8fedc8b9c12f897892e`, Room-Identität `055b3aad563fd07d68ec0e56bf533124`. Schema 18 bleibt bytegleich: SHA256 `70340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b`.

[Aktuelle CI 38243540172](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38243540172) gehört exakt zu 62fc0d2. Der frisch gelesene Build-Log aus Versuch 1 belegt alle sieben Pflichtkommandos mit Exit 0: `help`, `build --dry-run`, `test`, `lint`, `assembleDebug`, `assembleInternal`, `:app:assembleDeviceTest`. Signiertes Updatepaar 40121→40122 und generierte Room-Exporte sind bestanden.

| Tatsächlich ausgeführte Prüfung, Versuch 1 | Job-ID | Ergebnis |
|---|---|---|
| Project boundaries and CI scope | 114791737264 | success |
| Build, test, lint and APK verification | 114791800813 | success |

## Konkreter CI-Blocker

Im aktuellen Lauf wurden Instrumentierung und Gesamtprüfung **vor jeder Runner-Ausführung** als failure beendet: `runner_id=0`, leerer Runnername, keine Schritte, keine Joblogs. Ein gezieltes `rerun-failed-jobs` änderte keine Quelle und wiederholte nicht den erfolgreichen Build. Auch Versuch 2 wurde vor dem Runnerstart abgewiesen.

| Noch nicht ausgeführte Prüfung | Versuch 1 | Versuch 2 |
|---|---|---|
| SQLCipher / Compose | 114795006085 | 114795922434 |
| Android verification | 114795027663 | 114795943318 |

Der Workflow endet in Versuch 2 mit failure, nicht mit vollständigem Erfolg. Die erfolgreichen Prüfungen bleiben erhalten; die aktuellen SQLCipher-/Compose-Fälle wurden nach den letzten Korrekturen noch nicht ausgeführt. GitHub liefert Check-Annotations, deren konkreter Text über die verfügbare Connector-Route nicht auslesbar ist; Joblogs existieren wegen des fehlenden Runnerstarts nicht. **Keine unbelegte Behauptung, dass das 2-USD-Budget ausgeschöpft ist.** Budget und Stop-usage wurden nicht geändert. Kein Browserfallback und keine weiteren blinden CI-Wiederholungen.

Zur Fortsetzung zuerst die Startfehlermeldung in GitHub beim fehlgeschlagenen Emulatorjob prüfen und Runner-Ausführung wieder ermöglichen. Wenn dafür eine Änderung des 2-USD-Limits oder der Kontoeinstellungen erforderlich ist, entscheidet Tobi darüber. Danach am unveränderten Kopf 62fc0d2 ausschließlich die fehlgeschlagenen Instrumentierungs-/Gesamtjobs erneut ausführen. Kein neuer vollständiger Build oder Paketworkflow, solange die Quelle unverändert bleibt.

## Vorherige Prüfläufe und behobene Testfehler

1. Lauf 38235270100 an 134464f bestand Build, Signatur, Updatepaar und Schema; er wurde beim Ersetzen zweier alter fester Schema-18-Assertions automatisch abgebrochen. Er ist kein vollständiger Nachweis.
2. Lauf 38238378646 an faf4752 bestand ebenfalls den vollständigen Build. Die archivierten Berichte enthalten 228 Daten-Tests mit einer fehlgeschlagenen neuen Fremdkarten-Fixture und 137 App-Einträge mit zwei Fehlern. Acht der neun neuen SQLCipher-Fälle sind dort tatsächlich bestanden, einschließlich Gruppenrückrolle, FKs, Deduplizierung, genauer Wiederöffnung und Schema-18-Update.
3. Die neue Fremdkarten-Fixture hatte zuvor dasselbe Geburtsdatum wie die eigene Karte. Nach den unveränderten Regeln ist sie daher eine unklare Ersatzkarte, die mit ausdrücklicher eigener Bestätigung übernommen werden kann. Der synthetische Fremdfall hat jetzt ein anderes gültiges Geburtsdatum; sein `FOREIGN_CARD`-Status wird vor dem Speicher-/Ablehnungscheck geprüft.
4. Zwei neue Compose-Methoden hatten durch ihren letzten Ausdruck einen nicht-void-Rückgabewert. JUnit meldete einen Klassen-Initialisierungsfehler; die drei neuen Compose-Fälle sind dadurch noch nicht gelaufen. Explizites Kotlin `Unit` ist jetzt durch tatsächliche JVM-Methodensignaturen geprüft.
5. Zusätzlich scheiterte der bestehende `DriverProfileBindingTest.confirmationForPreviousCardCannotCarryOverToAForeignCard` beim Dispose mit einem Compose-SlotWriter-Arrayfehler. Seine Ursache ist nicht nachgewiesen; keine ungeprüfte Produktkorrektur oder behauptete Behebung. Dieser Fall muss im wieder freigegebenen vollständigen Emulatorlauf erneut bestehen.

Fehlerartefakt 11744020337, SHA256 `e2b64642e0144b5c526ae83389a84ed80648e78f6fd0c504f3fcacfc47b506a4`, direkt geladen und XML-Berichte geprüft. Die gesamte lokale Diagnose-ZIP bleibt unverändert erhalten; keine Benutzerkartenrohdaten oder privaten Screenshots veröffentlicht.

## Paket- und Hardwaregrenze

Die lokale 40122-APK ist vorbereitet und unabhängig mit offiziellem `apksigner` inklusive Inhaltssignatur sowie Manifest geprüft; `arm64-v8a` ist enthalten. Beide Testkorrekturcommits ändern ausschließlich Instrumentierungstests. Nach erneuter Assemble-Prüfung ist die APK bytegleich.

| APK-Beleg | Wert |
|---|---|
| Paket | `de.fahrerabrechnung.app.devicetest` |
| Version / Schema | 40122 / 19 |
| Stabiles Zertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| APK SHA256 | `1ee96d453becc036026de3cf9acee1d6787ac69e06877e6e8620eca6e13cba29` |

**Keine neue Test-ZIP erzeugt, gespeichert oder ausgegeben.** Die Freigabe bleibt an vollständige erfolgreiche aktuelle CI gebunden. Danach nur Update 40122 über vorhandener 40121 installieren, ohne Deinstallation oder Datenlöschung. Gerätetest: Erhalt alter Imports, Gen1-/Gen2-Auswahl desselben Vorgangs, gemeinsame MF-Details, erneutes Einstecken und genaue Wiederöffnung ohne Leser nach App-Neustart. Die ZIP muss Anleitung, Quell-/CI-Nachweis und geprüfte Prüfsummen enthalten.

Die 40121-Rückmeldung bestätigt nur den beobachteten Wiederlese-/Anzeigeumfang der 11-/3-Byte-Kommunikationsdateien. Sie gilt nicht für 40122. PR65 bleibt bis eigener ausdrücklicher Hardwarebestätigung des unveränderten getesteten Kopfs offen/Entwurf, ohne Merge oder Auto-Merge. Main bleibt unverändert.

## Nächste Importarbeiten

Nach vollständiger CI und eigenem Gerätetest PR65 am exakt getesteten Kopf abschließen. Anschließend verifiziertes 7F66-Primärformat, echte Byte-/Signaturbindung beim Standarddownload/DDD sowie ERCA-/MSCA-/Kartenvertrauen getrennt weiterführen. Für geschützte VU_Configuration 0540 fehlt weiterhin ein belegter legitimer Authentisierungs-/Secure-Messaging-Weg. [Vorhandener Primärabgleich](CARD_IMPORT_SECURITY_RESEARCH.md). Keine vollständige Kartenabdeckung oder Kryptovertrauensbehauptung.

Keine Browser, Unteragenten, Bereinigung oder Fahrzeug-/Spesen-/Abrechnungsarbeit. Actions-Budget 2 USD und Stop-usage unverändert. Bestehende Webhook-Auslöser unverändert. Der reine Dokumentationsabschluss verändert gegenüber 62fc0d2 keine Produktblobs. Diese Dokumentationsbranch enthält den unbestätigten PR65-Code als Vorfahren und darf vor dessen Gerätefreigabe nicht separat als angebliches Docs-Update nach Main gemergt werden.
