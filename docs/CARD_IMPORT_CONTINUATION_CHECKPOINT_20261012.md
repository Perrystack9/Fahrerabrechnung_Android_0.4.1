> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); verbindlich sind [CURRENT_RULES.md](CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Kartenimport-Fortsetzung – Checkpoint 12.10.2026

## Aktuelle Speicherintegration – CI noch ausstehend

Der vorhandene lokale PR65-Arbeitsstand ist am 12.10.2026 zuerst vollständig in einem separaten lokalen Sicherungscommit erhalten worden. Keine implementierte Datei wurde verworfen oder neu aufgebaut. Die Veröffentlichung folgt dem tatsächlichen GitHub-Parent `f1f92d69fff37eeaec5740b399d7df452b436098`; die lokale Snapshot-Abstammung wird nicht gepusht. Der vollständige Dateiabgleich enthält keine Löschungen. Die folgenden Reader-Abschnitte dokumentieren den früheren, bereits geprüften Zwischenstand.

Kandidat **40122 / Schema 19** integriert jetzt den normalen Beide-Anwendungen-Reader, atomare SQLCipher-Gruppenspeicherung, gemeinsame MF-Belege und genaue Gen1-/Gen2-Sitzungswahl im Verlauf. Übersicht und normale Quellenlisten wählen die Primäranwendung ausdrücklich. Alte Sitzungen, Quellen und Request-Fingerprints bleiben unverändert. [Umfang und Grenzen](CARD_MULTI_APPLICATION_IMPORT.md), [konkrete Geräteanleitung](CARD_MULTI_APPLICATION_DEVICE_TEST.md).

Echter Room/KSP-Export 19 vorhanden, SHA256 `01cdfdeee311e13efdcebd74d66783103f5bd0a8423ad8fedc8b9c12f897892e`, Identität `055b3aad563fd07d68ec0e56bf533124`. Migration 18→19 in SQLite gegen alle 42 compiler-generierten Tabellen einschließlich Spalten, Primärschlüsseln, Indizes und Fremdschlüsseln abgeglichen. Frühere Exporte bleiben unverändert.

Aktueller lokaler Gradle-Lauf erfolgreich: Daten-Unit-Tests, App-Kompilierung, Kompilierung beider neuen Instrumentierungssuiten und `:app:assembleDeviceTest`. Der vorherige lokale inkrementelle Lauf meldete doppelte Kotlin-Overloads; der unveränderte Quellstand kompiliert im anschließenden Lauf erfolgreich. Keine Produktionskorrektur daraus abgeleitet. Projektgrenzen, zehn CI-Guard-Tests, Shellsyntax, Diff- und Schema-Guards bestehen.

**Vollständige neue CI mit SQLCipher-/Compose-Emulatorprüfung und Prüfung des Updatepaars 40121→40122 steht noch aus.** Neun neue SQLCipher- und drei Compose-Testmethoden sind kompiliert, ihr Ausführungsergebnis ist noch nicht behauptet. Keine Gerätefreigabe für 40122. PR65 bleibt offen/Entwurf; kein Merge oder Auto-Merge. Kein zusätzlicher Paketworkflow, keine Budgetänderung, kein Browser, keine Unteragenten oder Bereinigung.

---

## Historischer Reader-Checkpoint

## Verbindlicher Ausgang und Eigentümerschaft

PR62 ist tatsächlich gemergt als `26a9f14c34523fa4ba884d8dd9891c24e1736ab4`; Tree `11e79e492b381f9d515bee796ffea30da3099592` exakt gleich dem getesteten Produktkopf `efbf49cdf92bd5e5ddb2e042b3e14e9372169756`. Tobis40121-Gerätefreigabe wurde zusätzlich am Dokumentationscommit21b164d und anschließend auf Main gelesen. Sie gilt nur für den beobachteten Wiederlese-/Anzeigeumfang11/3Bytes und aufgezeichnetesT=1/nicht aufgezeichnetesT=0. Keine zusätzlichen Update-/Altimport-/Neustartprüfungen oder erweiterte APDU-Nutzung behaupten.

Vor der neuen Produktbranch lag die reine Abschlussdokumentation auf Main vor: `ace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b`, Tree `a0101ba452bcebd75eaf34b44cd9518a2ac4f1d9`, Eltern26a9f14 und6d9b7fc. Keine andere offene Produktfortsetzung vorhanden. Diese Fortsetzung besitzt ausschließlich PR65; keine zweite Implementierung beginnen.

## Gesicherter erster Folgeentwurf

[PR65](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/65), Branch `feature/card-dual-application-reader-20261012`, offen/Entwurf, kein Merge oder Auto-Merge. Produktkopf **`f1f92d69fff37eeaec5740b399d7df452b436098`**, Tree **`79b3cedcb595f3baad076ac0c2c01142e332f919`**, einziger GitHub-Parentace6b7b. Vier Dateien gegenüber diesem Main: neue unveränderliche Anwendungsmodelle, gemeinsam extrahierte Reader-Auswertung/interne Beide-DFs-API,20 neue JVM-Testmethoden und die Modell-/Umfangsdokumentation. GitHub-Tree rekursiv vollständig gelesen; genau diese vier Blobs verändert/neu, keine weiteren Quelldeltas.

Das neue Reader-Fundament liest beide festen AIDs mit getrennten Kapazitäten, Identitäten und Quellen in einer eingeschalteten Kartensitzung; MF-Erfassung einmal davor. Kein Gen2V2-AID erfunden. Widerspruch und fehlender vollständiger0520-Beleg sind gesperrt/unverifiziert; Abbruch/Transportverlust ergibt keinen erfolgreichen Batch. Snapshots sind auf Ein- und Ausgabe eingefroren. Keine Persistenz oder Profilbindung im Reader.

**Noch kein regulärer Zwei-Anwendungs-Import.** Die normale App bleibt bei der bisherigen Ein-Anwendungs-API mit Gen2-Präferenz. Atomare Batch-/Child-Sitzungsspeicherung, additive Folgemigration, Anwendungs-/Verlaufsnavigation und deren eigene Hardwareprüfung stehen aus. Die vollständige Kartenimport-Phase ist nicht abgeschlossen. [Reader-Implementierung](CARD_MULTI_APPLICATION_IMPORT.md), [konkretes additives Speichermodell/Pflichtnachweise](CARD_APPLICATION_STORAGE_DESIGN.md), [Primärabgleich und tatsächliche Sicherheitsgrenzen](CARD_IMPORT_SECURITY_RESEARCH.md).

## Vollständige CI am eingefrorenen Produktkopf

[CI38204376573](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38204376573), Ereignis `pull_request`, `head_sha=f1f92d69fff37eeaec5740b399d7df452b436098`, abgeschlossen/success. Alle vier Jobs direkt frisch über GitHub geprüft:

| Job | ID | Ergebnis |
|---|---|---|
| Project boundaries and CI scope |114664492726 |success |
| Build, test, lint and APK verification |114664531145 |success |
| Room SQLCipher and Compose instrumentation |114666573531 |success |
| Android verification |114671366060 |success |

Build-Log: alle sieben Pflichtkommandos mit Exit0 (`help`, `build --dry-run`, `test`, `lint`, `assembleDebug`, `assembleInternal`, `:app:assembleDeviceTest`). Das bestehende Updatepaar40120→40121 wurde mit fester Testkennung/Signatur geprüft. Dieses CI-Paar ist **keine neue Auslieferung oder Gerätefreigabe** des geänderten PR65-Codes; der nächste tatsächlich ausgegebene Produktstand muss eine höhere Version und die bestätigte40121-Basis verwenden.

Emulatorlog: beide `:data:connectedDebugAndroidTest` und `:app:connectedDebugAndroidTest` ausgeführt, BUILD SUCCESSFUL; danach generierte Room-Exporte unverändert. Keine neue Schema-/UI-Speicherfunktion durch die alten SQLCipher-/Compose-Regressionen behaupten. Finale Gesamtkontrolle beendet05:07:49UTC /07:07:49Europe/Berlin.

Lokale tatsächliche Kotlin2.4.20-Kompilation aller betroffenen Reader-/Parserklassen und der beiden neuen Testsuites: Exit0, **OK(20Tests)**. Nur die bereits vorhandene synthetische DirectoryFixture wurde für den isolierten lokalen Lauf als Teststütze repliziert, keine Produktionsklasse ersetzt oder gestubbt. Lokale Projektgrenzen, zehn CI-Guard-Regressionsprüfungen, Shellsyntax und Diffprüfung bestanden.

## Schema, Version, Signatur und Grenzen

Schema18, sämtliche Entities/Migrationen/Exporte und APK-Version40121 bleiben unverändert. Export18-SHA256 `70340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b`. Stabile Testkennung `de.fahrerabrechnung.app.devicetest`, Zertifikat-SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`. Das ausgelieferte PR62-40121-Paket wurde nicht ersetzt. Keine neue Test-ZIP dieses internen Vorbereitungsstands zur Auslieferung erstellt oder ausgegeben.

Produktmanifest:294 Blob-Dateien in der rekursiven Git-Tree-Reihenfolge, Zeilen `<mode> blob <sha>\t<path>\n`, unter Ausschluss von `docs/`, `README.md`, `WORK_STATUS.md`; SHA256 `f95e87dfef86c6a82e2a95a42c300558b3dfd4ab2728f16db2b2db57d280cf13`. Die abschließende Docs-Branch muss exakt dieses Produktmanifest behalten und darf PR65 nicht vor eigener Freigabe als angeblich reines Docs-Update nach Main bringen.

Lokale Infrastruktur vorbereitet: echte Kotlin2.4.20-/JUnit4.13.2-Toolchain für Reader-Tests; Gradle9.6.1 mit genau dem Repository-SHA256 geprüft; offizielles Android-CLI-Paket15859902 sowie SDK37.0/BuildTools36.0.0 installiert. Ein lokaler KSP-Versuch löste zuerst das Android-Plugin ohne JVM-Proxy nicht auf; nach Konfiguration des bestehenden credentialfreien Proxys wurden die Projekte konfiguriert, der Lauf endete aber mit Exit1 ohne erfolgreichen KSP-/Exportabschluss. **Kein lokaler Room-Exporterfolg und kein neues Schema behaupten.** Bei Fortsetzung diesen lokalen Lauf diagnostizieren oder den echten Export über kontrollierte CI erzeugen; nichts handschreiben. Der vorhandeneSchema18-Export ist unabhängig davon durch die vollständig erfolgreiche CI verifiziert.

## Konkrete Fortsetzung

1. Aktuellen Main und unveränderten PR65-Kopf/CI erneut lesen; bestehende Fortsetzung übernehmen, nicht neu parallel beginnen. Lokale Snapshot-Commit-IDs niemals pushen. GitHub-Parent, vollständigen Tree und Ref-Lease genau prüfen.
2. Additive Batch-/Anwendungs-/MF-Relationen und echte compiler-generierte Migration umsetzen. Die physische Karte bleibt dieselbe. In einer SQLCipher-Transaktion speichern; Speicherfehler/Abbruch nach dem ersten Child muss die gesamte neue Gruppe zurückrollen. Erste/unklare Ersatzkarte frisch bestätigen; fremde/widersprüchliche/unverifizierte Karten ohne dauerhafte Mutation.
3. Stabile versionierte Parent-/Child-Requests, konkrete Generation je Anwendungssitzung, unveränderte alte Requests/Imports, keine Nachbefüllung und kein jüngerer Fallback. Normale `latest()`-/Verlaufswege müssen Gruppen und Primäranwendung ausdrücklich auswählen, nicht anhand zufälliger Child-ID-Sortierung. Zeiten beider DFs nicht addieren.
4. Eigenständige SQLCipher-/Compose-Fälle der neuen Funktion durchführen. Den endgültigen neuen Produktkopf vollständig prüfen. Steigendes signiertes Testupdate über bestätigtem40121/Schema18 vorbereiten; exakte Gen1-/Gen2-Ansichten, alte Daten/Requests und Wiederöffnung ohne Leser in der Anleitung. Bis eigener Hardwarebestätigung offen/Entwurf bleiben.
5. Nach dem Zwei-Anwendungs-Import: verifiziertes7F66-Primärformat beschaffen; echte Signatur-/Standarddownload-Bytebindung und passende Zertifikatskette separat implementieren. Der Sicherheitsabgleich enthält reale veröffentlichte Wurzelhashes und eine erfolgreiche lokale mathematische Gen2-Wurzel-Selbstsignatur; **kein App-Vertrauen oder Benutzerquellenbeweis**. Für0540 fehlt ein legitimer Authentisierungs-/Secure-Messaging-Pfad; kein blindes Lesen oder Schlüsselersatz.

Kein Browser, keine Unteragenten, keine Bereinigung/Löschung. Keine Fahrzeug-/Spesen-/Abrechnungsarbeit. Actions-Budget2USD/Stop-usage unverändert; genau ein neuer vollständiger CI-Lauf für diesen Produktentwurf, kein zusätzlicher Paketworkflow. Bestehende Webhook-Auslöser unverändert aktiv lassen. Der derzeitige Geräteumfang40121 gilt ausschließlich für den früheren getesteten Produktkopf, nicht für PR65 oder spätere Änderungen.
