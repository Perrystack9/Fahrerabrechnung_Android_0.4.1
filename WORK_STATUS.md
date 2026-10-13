# FahrerTagebuch - aktueller technischer Zustand

**40125 / Room-Schema 21 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76), Merge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, identischer vollständiger Produktbaum `36e014af6cbffa9add766bdd917c6d94313f87ec` zum CI-/Gerätekopf `20d34a0fc7ce370fb4c1d22002905a500574ee06`. Aktuellen main, offene PRs und CI live prüfen. Regeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und erhalten

- Automatischer eigener Vordergrund-Import, Profil-/Kartenbindung, unabhängige Anwendungen, MF am Parent, vollständige Core-Originale und vier vorhandene Core-Signaturbelege. Exakte Historie/Fingerprints, atomare SQLCipher-Speicherung und Keystore-Schutz bleiben erhalten.
- Neue eigene 0501-/0520-Aufnahmen in GEN1 und GEN2(V2), 0525 nur V2: fünf zusätzliche mögliche Belege auf üblicher G1+G2V2-Karte. C101 vor HASH eingefroren, konkrete Card_Sign-Policy/Le; G1 RSA/SHA1 128 Bytes wie vorher.
- Ununterbrochene SELECT/HASH/frische vollständige READ/strikte EOF/SIGN-Sequenzen. Neue Bytes müssen zum Bootstrap passen; INCONSISTENT_DATA verwirft die ganze Gruppe, auch nach erstem erfolgreichen Child. Unbewiesene Erstgrenze erlaubt keinen positiven Beleg.
- Passende neue Originale sind echte Capture-Quellen. Unabhängig vollständig bewiesener unsigned Bootstrap bleibt nur Fallback; kein späteres Überschreiben. Lokale Hash-/Signierfehler erhalten vollständige unsigned Originale, fehlgeschlagene neue READs behalten null Original-FK. Transportverlust/Cancellation verwerfen die laufende Gruppe.
- Separate `card_identity_signature_receipts`, exakte Child-/Karten-/Original-/Zertifikats-FKs, eigener Digest und defensive Maps. Core-Enum, Core-Tabelle und alter Digest unverändert. Kein Backfill oder jüngerer Historienersatz; leere neue Map verändert alte Requests nicht.
- Rein additive 20→21 in verschlüsselter Produktion. Historische vollständige 15-Byte-Quellen bleiben lesbar; positive 15-Byte-0501 nur reine GEN2, V2-15-Byte-Quelle unsigned. Unbekannte Layouts/ungültige Kapazitäten erzeugen keine positive Vollständigkeitsbehauptung oder erfundene abhängige Daten.
- Bestehende Fahrerkarten-Details zeigen Größe, ungeprüften Status, echte Signaturlänge/Grund; alte Imports ohne neue Belege. Quellen-/Sitzungs-/Anwendungs-/Archivwechsel maskieren die Nummer erneut. Kein UI-Umbau oder technische Bedienknöpfe.

## Nachweise

| Beleg | Ergebnis |
|---|---|
| Vollständige Produkt-CI | [38408152663](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38408152663), alle vier Jobs erfolgreich für `20d34a0fc7ce370fb4c1d22002905a500574ee06` |
| Gerätepaket | [38414059875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38414059875), gleicher Quellkopf; Infrastruktur `07db91f33957a45598cae6e4772787d1aa7ddbf4`, `include_baseline=false` |
| Gerätebestätigung | Tobi, 13.10.2026 17:32 Europe/Berlin: „Wurde getestet, und funktioniert 1a“ |
| Produktmerge | `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, identischer vollständiger Produktbaum |
| Zusätzlicher Produkt-Main-Lauf | [38423233063](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38423233063), vollständig erfolgreich für Produktmerge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d` |
| Abnahmedokumentation | PR #77, Merge `1007ee37ab693f5b89277b76daa91b2f5836cf53`; [Dokumentations-Main-CI 38424648944](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38424648944) erfolgreich mit unverändertem Dokumentationsrouting |

Alle sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Compiler-Schemaabgleich, ungekürzte SQLCipher-/Compose-Suiten API36 und Abschlussgate bestanden. Reader-/Format-/Policy-/Grenz-/Konsistenz-/Transport-/Freeze-, Herkunfts-/Tamper-/Dedup-/Rollback-/Request-/Historien- und UI-Abnahmeprüfungen erhalten. Eigenständige vollständige 19→20-Abnahme plus echte verschlüsselte 18-/19-/20→21-Pfade bestehen. Keine Filter/Quarantäne oder Workflow-/Upload-/Budgetänderung; normale erfolgreiche CI ohne Artefakte. Lokaler Gradle-Download blockiert, Android-Nachweis aus echter CI.

Room21 ausschließlich aus Compiler: SHA256 `8cd199f6012938fda80f8f8f265b49224b7fd8c127d2ad913b7926784eafa8c9`. Alle 43 bisherigen Tabellen entsprechen Schema 20 exakt; Exporte 1–20 unverändert. APK-Manifest, BUILD_INFO, feste Testsignatur und alle Archiv-/APK-Prüfsummen tatsächlich geprüft. [Genauer Geräte-/Paketbeleg](docs/DEVICE_CONFIRMATION_40125.md).

Die neue Geräte-Rückmeldung ist eine positive Gesamtabnahme zum bereitgestellten Paket; einzelne Schritte/Messwerte/Screenshots und Hardwaremodellnamen wurden diesmal nicht separat genannt. Keine 40124-Messwerte oder pauschale Hardware-/Kurvenabdeckung auf 40125 übertragen. Die bestätigte Basis 40124 aus PR #73 bleibt erhalten; [damaliger Gerätebeleg](docs/DEVICE_CONFIRMATION_40124.md).

## Nächster Einstieg und offene Grenzen

Dieser Produkt-Meilenstein ist gesichert; Sitzung am 13.10.2026 abgeschlossen. Vor diesem abschließenden reinen Dokumentationsschritt waren main `1007ee37ab693f5b89277b76daa91b2f5836cf53`, beide Main-Läufe erfolgreich und keine PRs offen; diese Angaben wurden live geprüft. Aktuellen main/PR/CI beim nächsten Einstieg erneut prüfen, dann nur einen ausdrücklich ausgewählten Folgeumfang planen. Im ursprünglichen Checkout `/workspace/scratch/4546aa92dbbb/FahrerTagebuch` liegt eine lokale angefangene Änderung an `feature/src/main/kotlin/de/fahrerabrechnung/feature/settings/SettingsScreen.kt`; sie wurde erhalten und nicht in den bestätigten Produktmerge aufgenommen. Dokumentationsabschluss im isolierten Worktree. Vor weiterer Integration vorhandene Arbeit prüfen und sichern.

Offen bleiben weitere EF-Signaturen, mathematische Prüfung/Trust, volle Kartenabdeckung, Export/DDD/Standarddownload und Schreibpfad. Keine automatische Folgephase; `7F66`/`0540`, Browser-/Unteragentenverbot und Produktgrenzen unverändert. [Übergabe](docs/WORK_HANDOFF.md).
