# FahrerTagebuch - aktuelle Übergabe

Zuerst WORK_START und dessen fünf Startdateien; aktuellen GitHub-main, offene PRs und CI live prüfen. Keine Chatrekonstruktion. Browser/Unteragenten ausgeschlossen.

**40127 / Room22 übernommen und neu im eigenen tatsächlich getesteten Umfang bestätigt.** [PR #82](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/82), Merge `96370d0ef291af1ac3893333cd128c2a3f735033`, identischer vollständiger Baum `f818dcb8b1da48cdad7cdc555bcc7638c478b819` zum CI-/Gerätekopf `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc`. Vollständige finale CI [38515830418](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38515830418) und exaktes Paket [38530132281](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38530132281), jeweils Versuch1, erfolgreich. Tobi 14.10.2026 10:12 Europe/Berlin: „Das passt alles 👍“ nach den erklärten abschließenden Chat-Testpunkten. [Genauer Beleg](DEVICE_CONFIRMATION_40127.md), [abgeschlossener Plan A1–A10](CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md).

## Bestätigter technischer Stand

0504/0505 ausschließlich aus exakten gespeicherten Child-Receipts offline als abgeleitete Leseprojektion prüfen. GEN1: strikt referenzierter öffentlicher CAR-Katalog, C108→C100-Recovery und getrennt PKCS1v1.5/SHA1-Dateiprüfung. GEN2/V2: C101/Card_Sign-Subject, sechs feste EC-Domänen, Punkt/Subgruppe/feste unsigned Plain-R/S, Hash einmal. Fehlende Voraussetzungen/Enginefehler nicht prüfbar; vollständige falsche Dateisignatur ungültig. Cancellation propagiert. Zertifikatsvertrauen immer NOT_EVALUATED.

Alle früheren Profil-/Karten-/Anwendungs-/Parent-/Child-/Original-/Receipt-Fingerprint-Ablehnungen erhalten. Konsistenter Read-Snapshot und defensive Bytes, Rechnen außerhalb der Transaktion auf Default-Dispatcher. Keine DB-Schreiboperation, Cache, Backfill oder neue Migration. Originaler CAPTURED_UNVERIFIED und sämtliche Fingerprintformate unverändert. Kleine getrennte Aufnahme-/Mathematik-/Vertrauenszeilen; Auswahl/Archiv/late-result geschützt, Maskierung nach Wechsel. Andere EF-Signaturen unverifiziert.

BC-Lightweight1.86 exakt gepinnt, ohne Providerwechsel. [Unabhängige Python/OpenSSL-Vektoren, Digests und Grenzen](CARD_CORE_SIGNATURE_MATH_EVIDENCE.md). Elf isolierte lokale JVM-Kryptotests sowie reale volle Android-CI bestanden. Alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemavergleich und ungekürzte API36-SQLCipher-/Compose-Suiten samt neuen Mathematik-/Zeilengleichheits-/Reopen-/Auswahltests und Abschlussgate. Kein Filter/Quarantäne oder CI-/Upload-/Budgetumbau.

Room22 unverändert: Compilerexport-SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. VersionCode40127. APK/ZIP bereitgestellt, BUILD_INFO/Prüfsummen/Manifest/festes Testzertifikat und APK-v2-Signatur samt Inhaltsdigest nach Download geprüft. Paket auf main-Infrastruktur `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0`, source_sha genau finaler Produktkopf, include_baseline=false. Zusätzlichen Produkt-Main-Lauf [38533736352](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38533736352) und Dokumentationsabschluss live prüfen.

## Tatsächliche Geräteabnahme und Grenzen

Screenshots 10:03: GEN1 Tätigkeiten13780/Signatur128, Fahrzeuge6202/Signatur128 Bytes; GEN2V2 Tätigkeiten13780/Signatur64, Fahrzeuge9602/Signatur64 Bytes. Alle vier mathematisch gültig, Zertifikatsvertrauen nicht geprüft. Rückmeldung10:08: alte Einträge ohne neue Anzeige, reguläres Einlesen ohne Probleme, Daten erhalten und Punkt9 (Unterbrechung/anschließender Import) funktioniert. Gesamtbestätigung10:12 nach Erklärung von Übernahme/Anwendung und vier Abschlussfällen: Auswahl alter/neuer Übernahme und GEN1/GEN2V2, Neustart ohne Leser, übrige Ansichten und installierte40127. Keine Auffälligkeit gemeldet.

Versionsanzeige und konkrete Importzeit nicht sichtbar; installierte40127 durch Gesamtantwort bestätigt. Kein zusätzliches Hardwaremodell, bestimmte EC-Kurve, GEN2-ohne-V2- oder allgemeine Altarchivabdeckung ableiten. Gilt nur für exakten Baum und tatsächlich getestete Kombination; keine Zertifikatsvertrauens-/DDD-/Schreibfreigabe.

## Vorgänger und lokale Arbeit erhalten

40126/Room22 aus PR #79: Merge `6334b4f67485feaee4de5d649d6788475af940ec`, eigener finaler CI-/Paket-/Gerätebeleg und Docs-Abschluss #80 erhalten. Vollständige eigene0502/0503-Originale/Child-Receipts, post-HASH-READ/EOF/SIGN, unabhängiger unsigned Fallback bei lokalen HASH/SIGN-Fehlern, Gruppenverwerfung bei Transportverlust/Cancellation und Historien-/Rollback-Abnahme abgeschlossen. Vier Core- und fünf Identifikationsbelege aus40124/40125 sowie sämtliche Original-/Request-/Certificate-Digests und Migrationen19→20/20→21/21→22 bleiben erhalten. Diese Arbeiten nicht erneut implementieren. [40126-Beleg](DEVICE_CONFIRMATION_40126.md) nur bei Bedarf.

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`; Abschlussbranch `docs/close-40127-session-20261014`, Produktbranch `feat/core-signature-math-40127`. Veröffentlichung über verbundenen GitHub-Connector, alle Blob-/Tree-SHAs gegen lokalen Commit vergleichen. CLI-fetch funktioniert; kein Reset/Rewrite oder Löschen alter Branches/Artefakte.

Ursprüngliche unvollständige Settings-Arbeit `/workspace/scratch/4546aa92dbbb/FahrerTagebuch/feature/src/main/kotlin/de/fahrerabrechnung/feature/settings/SettingsScreen.kt` unverändert; zusätzliche Datei/Patch `/workspace/scratch/4c3cf966d792/session-backups`. Sicherungs-SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`, bei Abschluss erneut geprüft. Nicht integrieren/verwerfen.

## Nächster Schritt

**Kein weiterer Produktmeilenstein freigegeben.** Separat den nächsten begrenzten Umfang festlegen. Weitere EF-Verifikation, Trustchain/Onlinekeys/DDD/Originalexport/Download-Schreiben und Oberflächenneugestaltung sind getrennte Pakete mit eigenen Voraussetzungen, keine automatische Fortsetzung. [Import-Folgeplan](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) bleibt einziger Fachtext im Startpfad.

Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitung ergänzt sie. Nach Dokumentationsabschluss Arbeit sichern und Sitzung beenden. Neuer Einstieg: „FahrerTagebuch am aktuellen Stand fortsetzen.“
