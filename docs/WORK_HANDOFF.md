# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien. Branch/HEAD, Arbeitsbaum, aktuellen GitHub-main, offene PRs und relevante CI live prüfen.

**40126-Produktmeilenstein abgeschlossen; neuer begrenzter Planauftrag.** Tobi beauftragt am 13.10.2026 zunächst einen konkreten Plan zur mathematischen Prüfung vorhandener 0504/0505-Signaturen für Gen1/Gen2/Gen2V2. **Implementierung ausdrücklich erst nach Freigabe.** [Plan mit verbindlichen Abnahmekriterien](CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md). Bestätigte Produktbasis main `7bbc66ed37dc1129a1e5286319e73e0860a2f474`, aus Produkt-PR #79 und Docs-Abschluss #80; aktuelle CI vollständig grün und beim neuen Planeinstieg keine offenen PRs.

## Bestätigter Produktstand

**40126 / Schema 22**, CI-/Gerätekopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`, identischer vollständiger Produktmerge-Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e`. Keine Produktänderung nach dem Test.

[Volle Produkt-CI 38444430958](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38444430958), Versuch 1, vollständig erfolgreich: alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaexport, ungekürzte API 36-SQLCipher-/Compose-Suiten und Abschlussgate. Eigenständige 19→20-/20→21-Prüfungen erhalten, zusätzlich additive 21→22 und verschlüsselte historische Upgrades bis 22. Keine Filter/Quarantäne oder CI-/Workflow-/Upload-/Budgetänderung; normale erfolgreiche Produkt-CI ohne Artefakte. Lokaler Gradle-Download blockiert.

[Gerätepaket 38448369846](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38448369846), Versuch 1, erfolgreich für denselben Produktkopf. Infrastruktur `3c90e97cd598afb7f8b66344dc049b195898cd31`, include_baseline=false. Archiv/ZIP/APK-Prüfsummen, BUILD_INFO, APK-Manifest VersionCode 40126 und festes Testzertifikat nach Download geprüft. ZIP und identische einzelne APK bereitgestellt. [Genauer Geräte-/Paketbeleg](DEVICE_CONFIRMATION_40126.md).

Tobi meldet am 13.10.2026 20:59 Europe/Berlin nach der vollständigen neunteiligen Chat-Testanleitung: „Hab alles getestet.“ Keine Auffälligkeit gemeldet. Die Sammelbestätigung umfasst Update/Historie/beide Anwendungen/Status/Auswahl/Maskierung/Reopen/erneuten Import und zusätzlichen Abbruchtest. Screenshot zeigt Events 3168/Faults 1152 Originalbytes und jeweils 64-Byte-Signatur, aufgenommen · ungeprüft; keine Störungen. Die ausgewählte Anwendung und Versionsanzeige sind nicht im Bildausschnitt enthalten. Keine zusätzlichen GEN1-Werte, C101-Kurven, Hardwaremodelle oder Fehlerfallmessungen behaupten. Gerätegate nur für den exakten eigenen getesteten Produktbaum/Umfang.

Neu übernommen: eigene 0502/0503-Rohquellen und separate Child-Receipts; vollständige Bytes auch bei EMPTY/INVALID, frische post-HASH-READs und strikter EOF-Beweis, echte C101/Card_Sign-Policy. Lokale HASH-/SIGN-Fehler behalten unabhängig vollständige unsigned Quellen; Transportverlust/Cancellation verwerfen die Gruppe. Exakte Child-/Original-/Zertifikats-FKs, defensive Maps, Tamper-/Request-/Dedup-/Historien-/Rollback-Prüfung einschließlich erstmaliger Kartenbindung. Alte Originale/Receipts/Fingerprints erhalten; kein Backfill oder jüngerer Ersatz. Kleine bestehende Statusanzeige; sensible Nummern nach Auswahlwechsel wieder maskiert.

Room22 echt: SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`; alle 44 bisherigen Tabellen und Exporte 1–21 unverändert. Vier Core-Belege aus 40124, fünf mögliche Identifikationsbelege aus40125 und deren unabhängige Migrationsabnahme vollständig erhalten. Vorgängerbelege nur bei Bedarf öffnen; Umsetzung nicht wiederholen.

Zusätzlicher [Produkt-Main-Lauf 38451502155](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38451502155), Versuch 1, vollständig erfolgreich für Produktmerge: alle vier Jobs einschließlich ungekürzter SQLCipher-/Compose-Suiten. Keine laufende Produktabnahme mehr offen. Docs-PR #80 separat nach unverändertem Dokumentationsrouting prüfen; exaktes finales CI-/Mergeergebnis steht am PR. Der Produktmerge besitzt nachgewiesen denselben vollständigen Baum wie der bereits geprüfte/gerätebestätigte Kopf.

## Lokaler Arbeitsstand

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`. Produktbranch `work/40126-event-fault-remote` mit Remote `feat/event-fault-card-signatures-40126`; Docs-Abschlussbranch `docs/close-40126-session-20261013`, lokal geprüfter Remote-Stand auf `work/40126-docs-remote`. Zum Sitzungsende auf einen neuen sauberen Branch vom tatsächlichen finalen main wechseln; bestehende Produkt-/Docs-/Sicherungsbranches erhalten. Git-Push hier ohne Schreibcredential; Veröffentlichung über verbundenen GitHub-Connector, danach Commit/Tree fetch und exakt vergleichen. Kein Reset oder Rewrite.

Frühere lokale Implementierungsstände bleiben in `preserve/40126-local-tree-before-remote`, `preserve/40126-compiler-export-local-before-remote` und `preserve/40126-rollback-test-local-before-remote` erhalten. Keine Branch-/Artefaktlöschung.

Ursprüngliche unvollständige Settings-Arbeit in `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` unverändert; Datei und Patch zusätzlich in `/workspace/scratch/4c3cf966d792/session-backups`, Dateisicherung SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`. Nicht integrieren oder verwerfen. Lokale Diagnose-/Paketdateien sind kein neuer Produktauftrag.

## Nächster Schritt und Grenzen

Nächster Schritt: Tobis Freigabe für den [0504/0505-Verifikationsplan](CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md) abwarten. Vorgesehen: reiner exakter Read-Assembler und abgeleitete RSA-/ECDSA-Prüfung, keine Persistenz/Migration; Room22 bleibt, Aufnahme-Receipts/Fingerprints unverändert. Gen1 benötigt mathematische C108/C100-Rückgewinnung mit exakt referenziertem öffentlichem Rechenschlüssel; Gen2 verwendet ausschließlich C101 und sechs feste Domänen. Mathematische Gültigkeit ist keine Zertifikatsvertrauensentscheidung. Alle bisherigen Prüfungen plus eigene neue vollständige Produkt-CI und passende neue Geräteabnahme erforderlich. Produkt, Paket und Geräteabnahme 40126 nicht erneut durchführen. Planbranch `docs/core-signature-math-verification-plan-20261013`, gleicher isolierter Checkout; nur Markdown geändert.

Aufnahme bleibt UNVERIFIED; vorgeschlagene mathematische Dateiprüfung als separate Leseaussage, Zertifikatsvertrauen NOT_EVALUATED. Weitere EFs, vollständiges ERCA/MSCA-Vertrauen, Kartenabdeckung, Originalexport/DDD/Standarddownload und Card_Download-Schreiben getrennt. 7F66 ohne ISO-Primärformat, 0540 ohne legitimen SC5-Weg gesperrt. Import/Profile/Historie/SQLCipher/Keystore erhalten; keine destruktive Migration. Keine Spesen-/Lohn-/Abrechnungs-/Heimatstandortphase und kein gleichzeitiger Oberflächenumbau. Browser und Unteragenten ausgeschlossen.

Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen nur ergänzend. Nach diesem Meilenstein Übergabe abschließen und Sitzung beenden. Neuer Einstieg: „FahrerTagebuch am aktuellen Stand fortsetzen.“
