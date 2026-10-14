# FahrerTagebuch - aktuelle Übergabe

Zuerst WORK_START und dessen fünf Startdateien; aktuellen GitHub-main, Produkt-PR, finalen Kopf und CI live prüfen. Keine Chatrekonstruktion. Browser/Unteragenten ausgeschlossen.

**40127 mathematische Core-Verifikation freigegeben und implementiert, noch nicht gerätebestätigt oder übernommen.** Tobi am14.10.2026, 06:13 Europe/Berlin. [Verbindlicher Plan A1–A10](CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md). Plan-PR #81 normal übernommen, Basis main `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0`, reine Docs-CI38510387252 erfolgreich. Produktbasis bleibt bestätigtes40126/Room22.

## Aktuelle Umsetzung und Nachweise

0504/0505 aus exakten gespeicherten Child-Receipts offline als abgeleitete Leseprojektion prüfen. Gen1: strikt referenzierter öffentlicher Katalog/C108→C100-Recovery und getrennt PKCS1v1.5/SHA1-Dateiprüfung. Gen2/V2: nur C101/Card_Sign, alle sechs festen EC-Domänen, Punkt/Subgruppe/feste unsigned Plain-R/S, Hash einmal. Fehlende Voraussetzungen/Enginefehler nicht prüfbar, vollständige falsche Dateisignatur ungültig; Cancellation propagiert. Zertifikatsvertrauen immer NOT_EVALUATED.

Alle früheren Profil-/Karten-/Anwendungs-/Parent-/Child-/Original-/Receipt-Fingerprint-Ablehnungen erhalten. Konsistenter Read-Snapshot, Rechnen außerhalb der Transaktion auf Default-Dispatcher. Keine DB-Schreiboperation/Cache/Backfill/neue Migration. Originaler CAPTURED_UNVERIFIED und alle Fingerprintformate unverändert. UI kleine getrennte Aufnahme-/Mathematik-/Vertrauenszeilen; Auswahl/Archiv/late-result geschützt. Andere EFs unverifiziert.

BC-Lightweight1.86 exakt gepinnt, ohne Providerwechsel. [Artefaktdigests, unabhängige Python/OpenSSL-Vektoren und Abnahmegrenzen](CARD_CORE_SIGNATURE_MATH_EVIDENCE.md). Elf isolierte lokale JVM-Kryptotests grün; lokaler vollständiger Android-Toolchain-Nachweis fehlt. Eigene vollständige echte Android-CI sowie neues exaktes Paket und Gerätegate am endgültigen Kopf erforderlich. Aktuellen Produkt-PR und dessen CI/Paketstatus live prüfen.

Keine Änderung an Workflows/Upload/Budget, Tests/Migrationsprüfungen/Compilerexporten/Keystore/SQLCipher. Vollständige sieben Gradle-Aufrufe und ungekürzte API36-SQLCipher-/Compose-Suiten samt Abschlussgate. Room22 unverändert: Schema-SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. VersionCode40127.

## Bestätigte Vorgänger erhalten

40126 Produkt-PR #79: Merge `6334b4f67485feaee4de5d649d6788475af940ec`, vollständiger identischer Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e` zum CI-/Gerätekopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`. Volle CI38444430958, Paket38448369846 und zusätzlicher Main-Lauf38451502155 erfolgreich. Tobi13.10.2026,20:59 Europe/Berlin: „Hab alles getestet.“ Screenshot Events3168/Faults1152, beide64Bytes aufgenommen·ungeprüft, keine sichtbare Anwendung. [Genauer Beleg](DEVICE_CONFIRMATION_40126.md); keine weitere Hardware/Kurve/Versionsanzeige daraus ableiten. Docs-Abschluss #80 erhalten.

Eigene0502/0503-Originale/Child-Receipts, post-HASH-READ/EOF/SIGN, getrennt vollständige unsigned Originale bei lokalen HASH/SIGN-Fehlern, ganze Gruppenverwerfung bei Transportverlust/Cancellation, FK-/Fingerprint-/Rollback-/Reopen-/UI-Abnahme abgeschlossen. Nicht erneut implementieren. Vier Core- und fünf Identifikationsbelege aus40124/40125, alle alten Request-/Original-/Certificate-Digests und Migrationen19→20/20→21/21→22 vollständig erhalten.

## Lokaler Stand und nächster Schritt

Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`; Implementierungsbranch `feat/core-signature-math-40127`. Veröffentlichung über verbundenen GitHub-Connector (CLI-Push ohne Schreibcredential); Blob-/Tree-SHAs gegen lokalen Commit prüfen, danach Remote fetch auf neuen lokalen Branch. Kein Reset/Rewrite oder Löschung alter Branches/Artefakte.

Alte Produkt-/Docs-/Sicherungsbranches erhalten. Ursprüngliche unvollständige Settings-Arbeit `/workspace/scratch/4546aa92dbbb/FahrerTagebuch/feature/src/main/kotlin/de/fahrerabrechnung/feature/settings/SettingsScreen.kt` unverändert, zusätzliche Datei/Patch `/workspace/scratch/4c3cf966d792/session-backups`; Sicherungs-SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`. Nicht integrieren/verwerfen.

Nächster Schritt: finalen40127-Draft-Produktkopf vollständig grün verifizieren; unveränderten targeted device-test-Workflow auf main mit genau diesem source_sha und include_baseline=false starten. Connector hat derzeit keine Dispatch-Funktion; nötigenfalls Tobi den konkreten Start erklären. Paket/BUILD_INFO/ZIP/APK-Digests/VersionCode/Testzertifikat nach Download prüfen, ZIP und identische APK bereitstellen. [40127-Testpunkte](DEVICE_TEST_40127.md) **immer direkt verständlich im Chat**. Neue eigene positive Gerätebestätigung vor Produktmerge, unerwartete INVALID-Urteile vorher aufklären. Keine frühere Geräteabnahme dafür übertragen.

Weitere EF-Verifikation, Trustchain/Onlinekeys/DDD/Originalexport/Download-Schreiben und Oberflächenneugestaltung sind nicht freigegeben. Keine neue Produktfunktion außerhalb dieses Plans. Sitzung nach Übergabe/Meilenstein abschließen; neuer Einstieg: „FahrerTagebuch am aktuellen Stand fortsetzen.“
