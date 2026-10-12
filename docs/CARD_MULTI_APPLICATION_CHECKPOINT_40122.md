# PR65 – gemeinsamer Kartenimport 40122

Stand 12.10.2026. **Emulatorprüfung noch offen; keine Hardwarefreigabe für 40122.** Dieser Checkpoint setzt die bestehende PR65-Fortsetzung fort. Main bleibt auf der bestätigten Basis 40121 / Schema 18.

## Gesicherte Quelle

Der vorhandene Arbeitsstand wurde zuerst vollständig auf der lokalen Sicherungsbranch `backup/pr65-storage-local-20261012` erhalten: Implementierung `7e906ec`, ergänzte lokale Prüfungsdokumentation `ef886d8`. Keine implementierte Datei verworfen, keine Bereinigung oder Neuimplementierung. Diese lokale Snapshot-Abstammung wurde nicht veröffentlicht.

Der vollständige geprüfte Dateibaum wurde anschließend mit dem tatsächlichen GitHub-Parent veröffentlicht. [PR65](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/65) bleibt ein offener Entwurf auf `feature/card-dual-application-reader-20261012`:

| Beleg | Wert |
|---|---|
| Produktcommit | `faf47521529b8d3c42bec56ab8b96daa73ff4938` |
| Vollständiger Quellbaum | `b52c40dd6655c9771212a1f4972ffff0ee75d011` |
| Einziger GitHub-Parent | `134464f95d0520415cbd5b48c522d69cc03ed8db` |
| Produktmanifest | 302 Blobs; SHA256 `099df3a19ce0cad2b66e11d0db6cf07cf36a6d74325df4846654b8b27415aaab` |
| Testversion / Schema | 40122 / 19 |
| Unveränderte Updatebasis | 40121 / 18 |

Manifestformat: rekursive Git-Tree-Reihenfolge, Zeilen `<mode> blob <sha>\t<path>\n`, ohne `docs/`, `README.md` und `WORK_STATUS.md`. GitHub-Tree exakt gleich dem gesicherten lokalen Quellbaum; keine Dateilöschungen. Ref-Update mit erwarteter alter SHA und ohne Force. Die lokale Arbeitskopie besitzt eine ausdrücklich markierte GitHub-Historiengrenze am Reader-Parent; die lokalen Sicherungsbranches bleiben erhalten.

## Implementierter Umfang

Normaler Vordergrund-Import beider fester Kartenanwendungen in einem gemeinsamen Lesevorgang. Gen1 und Gen2/Gen2V2 behalten eigene Anwendungs-, Identitäts-, Kapazitäts-, Zertifikats-, Run- und Quellenbelege; MF-Dateien gehören zum gemeinsamen Parent. Beide Anwendungsschaltflächen öffnen die konkrete gespeicherte Sitzung. Normale Übersicht und Quellenlisten wählen die Primäranwendung ausdrücklich. Keine Addition beider Zeitsätze, kein jüngerer Quellenfallback oder Backfill alter Übernahmen.

Migration 18→19 ist additiv. Eine äußere SQLCipher-Transaktion umfasst Bindung, Parent, beide Child-Sitzungen und sämtliche Quellen. Widerspruch, fremde/unbestätigte Karten und Kollision mit alten manuellen Requests werden vor dauerhafter Übernahme abgewiesen. Ein Speicherfehler nach dem ersten Child rollt die ganze neue Gruppe zurück. Zusammengesetzte Fremdschlüssel binden Run, Sitzung und MF-Quelle an die physische Karte. Alte Requests bleiben unverändert.

[Integration und Grenzen](CARD_MULTI_APPLICATION_IMPORT.md) · [additive Modellentscheidung](CARD_APPLICATION_STORAGE_DESIGN.md) · [Geräteanleitung](CARD_MULTI_APPLICATION_DEVICE_TEST.md).

## Tatsächliche Prüfungen

Lokal: 452 Daten-Unit-Tests, keine Fehler oder übersprungenen Tests; App-Kompilierung, beide Instrumentierungssuiten und DeviceTest-APK erfolgreich. Gegenüber Main sind 27 JVM-, neun SQLCipher- und drei Compose-Testmethoden hinzugekommen. Projektgrenzen, zehn CI-Guard-Tests, Shellsyntax und Diff-/Schema-Guards bestehen.

Schema 19 stammt vom echten Room/KSP-Compiler. Migration gegen alle 42 erzeugten Tabellen einschließlich Spalten, PK, Indizes und FKs abgeglichen. Schema-19-SHA256 `01cdfdeee311e13efdcebd74d66783103f5bd0a8423ad8fedc8b9c12f897892e`, Room-Identität `055b3aad563fd07d68ec0e56bf533124`. Schema 18 unverändert: SHA256 `70340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b`.

[Abschließende CI 38238378646](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38238378646), Versuch 1, Ereignis `pull_request`, exakt am korrigierten Produktcommit `faf4752`: **Emulator und Gesamtergebnis noch offen**.

| Prüfung | Job-ID | Stand |
|---|---|---|
| Project boundaries and CI scope | 114774852310 | success |
| Build, test, lint and APK verification | 114774923323 | success |
| Room SQLCipher and Compose instrumentation | 114777460444 | noch offen |
| Android verification | noch nicht gestartet | noch offen |

Der direkt gelesene Build-Log des korrigierten Kopfs belegt erneut alle sieben Pflichtkommandos mit Exit 0, das signierte Updatepaar und den unveränderten Room-Schemaexport.

Der vorherige Lauf 38235270100 am Kopf 134464f bestand Projektgrenzen und Build-Job: alle sieben Pflichtkommandos Exit 0 (`help`, `build --dry-run`, `test`, `lint`, `assembleDebug`, `assembleInternal`, `:app:assembleDeviceTest`), signiertes Updatepaar 40121→40122 und Room-Schema-Guard. Beim Nachabgleich wurden zwei ältere Assertions entdeckt, die noch fest Datenbankversion 18 erwarteten. Sie verwenden jetzt `AppDatabase.SCHEMA_VERSION`. Sämtliche Prüfungen historischer Quellen und Requests bleiben erhalten. Der erste Lauf ist durch die neue PR-Prüfung automatisch abgebrochen worden (`cancelled`) und ist kein vollständiger Freigabenachweis. Sein Log zeigt einen fehlgeschlagenen Daten-Test-Task, aber keine vollständige Testdiagnose; es wird daraus keine Behauptung abgeleitet, dass ausschließlich die zwei alten Assertions betroffen waren.

Der Korrekturcommit faf4752 hat genau einen geänderten Instrumentierungstest-Blob mit zwei Zeilenänderungen gegenüber 134464f. App-Code, Export 19 und APK sind unverändert. Der lokale Snapshot 78be454 ist auf der Sicherungsbranch `backup/pr65-schema-assertions-local-20261012` erhalten; der veröffentlichte Korrekturcommit hat den tatsächlichen GitHub-Parent 134464f. Lokale Instrumentierungskompilierung und DeviceTest-Assemble nach der Korrektur erfolgreich; APK-SHA256 weiterhin unverändert. Es wird keine fehlgeschlagene alte Assertion als Produktfehler oder zusätzlicher Hardwarebeweis bezeichnet.

## Vorbereitetes Testupdate und Hardwaregrenze

APK lokal aus denselben unveränderten Produktblobs erstellt, unabhängig von der vollständigen CI am genannten Quellcommit. Normale erfolgreiche CI lädt gemäß bestehender Speicherpolitik keine APK-Artefakte hoch; kein weiterer Paketworkflow gestartet. APK mit offiziellem `apksigner` einschließlich Inhaltssignatur und Manifest geprüft; `arm64-v8a` enthalten.

| APK-Beleg | Wert |
|---|---|
| Paket | `de.fahrerabrechnung.app.devicetest` |
| Version | 40122 |
| Stabiles Zertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| APK SHA256 | `1ee96d453becc036026de3cf9acee1d6787ac69e06877e6e8620eca6e13cba29` |

**Noch keine Test-ZIP ausgegeben.** Erst nach vollständig grüner CI einschließlich der neuen SQLCipher-/Compose-Fälle ausgeben. Update über die vorhandene 40121 installieren, ohne Deinstallation oder Datenlöschung. Der nächste reale Test muss den Erhalt alter Imports, Gen1-/Gen2-Auswahl desselben Vorgangs, gemeinsame MF-Details, erneutes Einstecken und genaue Wiederöffnung ohne Leser nach App-Neustart abdecken.

Die positive 40121-Rückmeldung galt nur für den beobachteten Wiederlese-/Anzeigeumfang der 11-/3-Byte-Kommunikationsdateien. Sie bestätigt diesen neuen Import-/Schema-/UI-Stand nicht. Kein Merge oder Auto-Merge von PR65 vor eigener ausdrücklicher Gerätefreigabe des unveränderten getesteten Produktkopfs.

## Nächste Importpunkte

Nach eigener Gerätefreigabe von 40122 den exakt getesteten PR65-Kopf technisch sauber abschließen. Danach verifiziertes 7F66-Primärformat, echte Byte-/Signaturbindung beim Standarddownload/DDD sowie ERCA-/MSCA-/Kartenvertrauen in getrennten Schritten weiterführen. Für geschützte VU_Configuration 0540 fehlt weiterhin ein belegter legitimer Authentisierungs-/Secure-Messaging-Weg. Keine behauptete vollständige Kartenabdeckung oder kryptografisches Vertrauen. [Vorhandener Primärabgleich](CARD_IMPORT_SECURITY_RESEARCH.md).

Keine Browser, Unteragenten, Bereinigung oder Fahrzeug-/Spesen-/Abrechnungsarbeit. Actions-Budget 2 USD und Stop-usage unverändert. Ein abschließender vollständiger CI-Lauf am korrigierten Kopf; der ältere Lauf wird ersetzt, kein zusätzlicher Paketworkflow. Bestehende Webhook-Auslöser unverändert. Ein späterer reiner Dokumentationscheckpoint enthält den unbestätigten PR65-Code als Vorfahren und darf deshalb nicht separat als angebliches Docs-Update nach Main gemergt werden.
