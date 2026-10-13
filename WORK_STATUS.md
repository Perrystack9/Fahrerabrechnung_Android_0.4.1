# FahrerTagebuch - aktueller technischer Zustand

**Aktiver Auftrag 40126 / Schema 22: 0502/0503, Draft PR #79; noch keine neue Gerätefreigabe oder Übernahme.** Bestätigte Basis 40125 unten erhalten.

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

## Aktiver Folgeauftrag und offene Grenzen

Tobi hat die begrenzte 0502/0503-Aufnahme am 13.10.2026 freigegeben. [Plan](docs/CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md), Draft [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79), 40126 / Schema 22. Separate neue Raw-Metadatenkinds und Receipt-Tabelle; alte Core-/Identitätskinds, Digests, Requests ohne neue Map, Originale und Quellen unverändert. Vollständige Rohbytes auch bei EMPTY/INVALID; keine erfundenen Projektionen. Zusätzlich strikte EOF, echte Policy/Länge, exakte Child-/Original-/Zertifikats-FKs, additive 21→22 und neue Reader-/SQLCipher-/Historien-/Rollback-/Compose-Tests.

Implementierung zur vollständigen CI-Prüfung bereitgestellt. Room 22 stammt ausschließlich aus dem echten Compilerexport des ersten CI-Laufs 38442079970; SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. Alle 44 bisherigen Tabellen entsprechen Schema 21 exakt; Exporte 1–21 unverändert. Erfolgreiche vollständige CI und neue Gerätefreigabe erst nach eigenen Nachweisen behaupten. Eigene 19→20- und 20→21-Abnahmen erhalten; historische Zielstand-Upgrades gehen jetzt bis 22 und entfernen zusätzlich die neue leere Tabelle aus ihren Ausgangsdatenbanken. Die Timeout-Erwartung für die neue Ereignis-/Störungssignatursequenz prüft den stärkeren Gruppenabbruch; lokale IO-Fehler behalten ihre bisherigen vollständigen anderen Quellen.

Lokaler Gradle-Download weiterhin blockiert. Browser/Unteragenten ausgeschlossen. CI-, Upload-, Budget- und Workflowregeln unverändert. Neue [Checkliste 40126](docs/DEVICE_TEST_40126.md) im bestehenden Paketpfad eingebettet. Produktmerge erst nach eigenem vollständig grünen endgültigen Kopf und neuer positiver Geräteabnahme.

Ursprüngliche Settings-Änderung erhalten und separat gesichert; neuer isolierter Checkout in aktueller [Übergabe](docs/WORK_HANDOFF.md). Weitere EFs, mathematische Prüfung/Trust, Export/DDD/Standarddownload, Schreibpfad, 7F66/0540 und Oberflächenneugestaltung bleiben getrennt.
