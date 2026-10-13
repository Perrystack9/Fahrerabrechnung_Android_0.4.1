# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien. Branch/HEAD, Arbeitsbaum, aktuellen GitHub-main, offene PRs und relevante CI live prüfen.

**40126-Produktmeilenstein abgeschlossen.** Keine weitere Umsetzung freigegeben. [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79) nach neuer eigener Geräteabnahme normal gemergt; Produkt-main `6334b4f67485feaee4de5d649d6788475af940ec`. Anschließend ausschließlich Gerätebeleg, Kernstatus, Folgeplan und diese Übergabe aktualisieren.

## Bestätigter Produktstand

**40126 / Schema 22**, CI-/Gerätekopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`, identischer vollständiger Produktmerge-Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e`. Keine Produktänderung nach dem Test.

[Volle Produkt-CI 38444430958](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38444430958), Versuch 1, vollständig erfolgreich: alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaexport, ungekürzte API 36-SQLCipher-/Compose-Suiten und Abschlussgate. Eigenständige 19→20-/20→21-Prüfungen erhalten, zusätzlich additive 21→22 und verschlüsselte historische Upgrades bis 22. Keine Filter/Quarantäne oder CI-/Workflow-/Upload-/Budgetänderung; normale erfolgreiche Produkt-CI ohne Artefakte. Lokaler Gradle-Download blockiert.

[Gerätepaket 38448369846](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38448369846), Versuch 1, erfolgreich für denselben Produktkopf. Infrastruktur `3c90e97cd598afb7f8b66344dc049b195898cd31`, include_baseline=false. Archiv/ZIP/APK-Prüfsummen, BUILD_INFO, APK-Manifest VersionCode 40126 und festes Testzertifikat nach Download geprüft. ZIP und identische einzelne APK bereitgestellt. [Genauer Geräte-/Paketbeleg](DEVICE_CONFIRMATION_40126.md).

Tobi meldet am 13.10.2026 20:59 Europe/Berlin nach der vollständigen neunteiligen Chat-Testanleitung: „Hab alles getestet.“ Keine Auffälligkeit gemeldet. Die Sammelbestätigung umfasst Update/Historie/beide Anwendungen/Status/Auswahl/Maskierung/Reopen/erneuten Import und zusätzlichen Abbruchtest. Screenshot zeigt Events 3168/Faults 1152 Originalbytes und jeweils 64-Byte-Signatur, aufgenommen · ungeprüft; keine Störungen. Die ausgewählte Anwendung und Versionsanzeige sind nicht im Bildausschnitt enthalten. Keine zusätzlichen GEN1-Werte, C101-Kurven, Hardwaremodelle oder Fehlerfallmessungen behaupten. Gerätegate nur für den exakten eigenen getesteten Produktbaum/Umfang.

Neu übernommen: eigene 0502/0503-Rohquellen und separate Child-Receipts; vollständige Bytes auch bei EMPTY/INVALID, frische post-HASH-READs und strikter EOF-Beweis, echte C101/Card_Sign-Policy. Lokale HASH-/SIGN-Fehler behalten unabhängig vollständige unsigned Quellen; Transportverlust/Cancellation verwerfen die Gruppe. Exakte Child-/Original-/Zertifikats-FKs, defensive Maps, Tamper-/Request-/Dedup-/Historien-/Rollback-Prüfung einschließlich erstmaliger Kartenbindung. Alte Originale/Receipts/Fingerprints erhalten; kein Backfill oder jüngerer Ersatz. Kleine bestehende Statusanzeige; sensible Nummern nach Auswahlwechsel wieder maskiert.

Room22 echt: SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`; alle 44 bisherigen Tabellen und Exporte 1–21 unverändert. Vier Core-Belege aus 40124, fünf mögliche Identifikationsbelege aus40125 und deren unabhängige Migrationsabnahme vollständig erhalten. Vorgängerbelege nur bei Bedarf öffnen; Umsetzung nicht wiederholen.

Zusätzlichen [Produkt-Main-Lauf 38451502155](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38451502155) und Docs-Abschluss-CI vor endgültigem Sitzungsabschluss live prüfen. Der Produktmerge besitzt nachgewiesen denselben vollständigen Baum wie der bereits geprüfte/gerätebestätigte Kopf.

## Lokaler Arbeitsstand

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`. Produktbranch `work/40126-event-fault-remote` mit Remote `feat/event-fault-card-signatures-40126`; Docs-Abschlussbranch `docs/close-40126-session-20261013`. Git-Push hier ohne Schreibcredential; Veröffentlichung über verbundenen GitHub-Connector, danach Commit/Tree fetch und exakt vergleichen. Kein Reset oder Rewrite.

Frühere lokale Implementierungsstände bleiben in `preserve/40126-local-tree-before-remote`, `preserve/40126-compiler-export-local-before-remote` und `preserve/40126-rollback-test-local-before-remote` erhalten. Keine Branch-/Artefaktlöschung.

Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` unverändert; Datei und Patch zusätzlich in `/workspace/scratch/4c3cf966d792/session-backups`, Dateisicherung SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`. Nicht integrieren oder verwerfen. Lokale Diagnose-/Paketdateien sind kein neuer Produktauftrag.

## Nächster Schritt und Grenzen

Kein aktiver neuer Produktauftrag. Für die nächste Entscheidung vorhandenen [Import-Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) und bereits abgeschlossene Matrix verwenden, ein weiteres Paket separat begrenzen; keine automatischen Folgefunktionen. Produkt, Paket und Geräteabnahme 40126 nicht erneut durchführen.

Aufnahme bleibt UNVERIFIED. Weitere EFs, mathematische Prüfung/ERCA/MSCA-Vertrauen, vollständige Kartenabdeckung, Originalexport/DDD/Standarddownload und Card_Download-Schreiben getrennt. 7F66 ohne ISO-Primärformat, 0540 ohne legitimen SC5-Weg gesperrt. Import/Profile/Historie/SQLCipher/Keystore erhalten; keine destruktive Migration. Keine Spesen-/Lohn-/Abrechnungs-/Heimatstandortphase und kein gleichzeitiger Oberflächenumbau. Browser und Unteragenten ausgeschlossen.

Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen nur ergänzend. Nach diesem Meilenstein Übergabe abschließen und Sitzung beenden. Neuer Einstieg: „FahrerTagebuch am aktuellen Stand fortsetzen.“
