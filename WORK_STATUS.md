# FahrerTagebuch - aktueller technischer Zustand

**main: bestätigtes 40124 / Schema 20. Produktkandidat: 40125 / Schema 21 in offenem Draft-[PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76).** Kein 40125-Merge und keine neue Gerätebestätigung. Git/main/PR-Kopf und CI live prüfen. Regeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Übernommene Basis

40124 aus PR #73: Merge `3298b707525a4f33162c835ce31deaf181d2e772`, identischer Produktbaum `3540b26b775d17c9bdc6335549dafbfbd58bcd92` zum CI-/Gerätekopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`. Volle CI 38381962283 und zusätzliche Produkt-Main-CI 38394108412 erfolgreich. Exaktes Gerätepaket 38388192092; Tobi bestätigt am 13.10.2026 13:57 Europe/Berlin sämtliche eigenen Checklistenpunkte. GEN1-Originale 13780/6202, Signaturen 128/128; GEN2V2-Originale 13780/9602, Signaturen 64/64 Bytes, alle ungeprüft. [Gerätebeleg](docs/DEVICE_CONFIRMATION_40124.md).

Automatischer eigener Vordergrund-Import, Profil-/Kartenbindung, vollständige Core-Originale, vier neue Core-Signaturen, unabhängige Anwendungen, MF am Parent, exakte Historie/Fingerprints und atomare SQLCipher-/Keystore-Speicherung bleiben erhalten. Additive 19→20 und deren eigenständige vollständige Migrationsprüfung bleiben bestehen. Room-20-Export unverändert: SHA256 `34a9253cb546255ce9c12bef2d9808933e1af8a8dca1c27a783eb7322e6ab6e6`.

## Umsetzung im 40125-Kandidaten

- Neue eigene 0501-/0520-Signaturaufnahmen in GEN1 und GEN2(V2), 0525 nur auf V2: bei üblicher G1+G2V2-Karte fünf zusätzliche Belege neben den vier Core-Belegen.
- Zertifikate vor HASH eingefroren; deterministische SELECT/HASH/frische vollständige READ/strikte EOF/SIGN-Sequenzen. Card_Sign-Policy und echte Le wie 40124. Eigene Gruppen-Konsistenzverletzung bei abweichenden Bootstrap-Bytes; weder Teilgruppe noch Bestätigungs-Override.
- Unabhängig bewiesene Vorablesung bleibt unsigned Fallback. Neue Originale ersetzen sie nur aus passender vollständiger Capture. Lokale Hash-/Signierstörungen erhalten vollständige unsigned Bytes; fehlgeschlagene neue READs haben im Receipt keinen Original-FK. Unbewiesene Erstgrenze erlaubt keinen späteren positiven Beleg.
- Separate `card_identity_signature_receipts` mit exakten Child-/Karten-/Original-/Zertifikats-FKs und eigenem Fingerprint; Core-Enum, Core-Tabelle und alter Receipt-Digest unverändert. Kein Backfill oder jüngerer Quellenersatz; leere neue Map verändert alte Requests nicht.
- Additive 20→21 in verschlüsselter Produktion registriert. Alte Exporte 1–20 unverändert; neuer Export ausschließlich aus echtem Compiler. Historische vollständige 15-Byte-Quellen bleiben lesbar; positive 15-Byte-0501 nur reine GEN2, keine behauptete 17-Byte-V2-Aufnahme.
- Bestehende Fahrerkarten-Details zeigen Originalgröße, ungeprüfte Signaturlänge/Status/Grund. Alte Imports: Signatur in diesem Import nicht aufgenommen. GEN1/reine GEN2 erhalten keine erfundene V2-Quelle; Nummer nach Quellen-/Sitzungswechsel wieder verborgen. Keine neue Navigation oder technische Bedienknöpfe.

## Prüfung und nächster konkreter Schritt

Reader-/Format-/Policy-/Grenz-/Konsistenz-/Transport-/Freeze-Prüfungen sowie SQLCipher-Herkunfts-/Tamper-/Dedup-/Gruppenrollback-/20→21- und erhaltene 18-/19→21-Migrationsprüfungen, Request-Wiederholungen und Compose-Abnahme ergänzt. Lokale Projektgrenzen und zehn CI-Routing-/Schema-Guard-Regressionen bestehen. Lokaler Gradle-Download weiterhin netzbedingt blockiert; Android-Nachweis ausschließlich aus echter GitHub-CI.

**Maßgeblich sind die Checks des endgültigen PR-76-Kopfs:** alle sieben Gradle-Aufrufe, Unit/Lint/APK-/Signatur-/Updatepaar-/Compiler-Schemaprüfung, komplette SQLCipher-/Compose-Suiten API36 und Abschlussgate. Keine Filter/Quarantäne oder Workflow-/Upload-/Budgetänderung. Normale erfolgreiche CI ohne Artefakte.

Erst nach vollständiger grüner CI exaktes Paket über `device-test.yml` auf main mit vollem Produkt-`source_sha` aus PR #76 und `include_baseline=false` erzeugen. API/CLI-Dispatch fehlt im aktuellen Work-Zugang; Start über GitHub durch Tobi erforderlich, kein Browser-Ausweichen. Infrastruktur-/APK-Kopf getrennt prüfen. [40125-Checkliste](docs/DEVICE_TEST_40125.md) vorbereitet, noch nicht durchgeführt. Eigene ausdrückliche neue Gerätebestätigung vor Merge.

Offen: mathematische Prüfung/Trust, volle Kartenabdeckung, Export/DDD/Standarddownload und Schreibpfad. Keine gleichzeitige Folgephase; `7F66`/`0540` und Produktgrenzen unverändert. [Übergabe](docs/WORK_HANDOFF.md).
