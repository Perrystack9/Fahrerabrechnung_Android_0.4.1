# FahrerTagebuch - aktueller technischer Zustand

**40127 / Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #82](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/82), Produktmerge `96370d0ef291af1ac3893333cd128c2a3f735033`, identischer vollständiger Baum `f818dcb8b1da48cdad7cdc555bcc7638c478b819` zum geprüften Kopf `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc`. Aktuellen main, offene PRs und CI live prüfen. Regeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und erhalten

- Automatischer eigener Vordergrund-Import, Profil-/Kartenbindung, unabhängige GEN1-/GEN2-Anwendungen, MF am Parent, atomare SQLCipher-Speicherung und Keystore-Schutz erhalten. Gen2V2 bleibt eine Ausprägung der Gen2-Anwendung; beide Zeitsätze nicht addieren.
- Vollständige eigene Originale einschließlich Pointer/unbenutzter Bytes, konkrete Parent-/Child-Sitzungen, historische Quellen, Request-/Original-/Certificate-/Receipt-Fingerprints und getrenntes Altarchiv erhalten. Fehlend, nicht versucht, unbekannt, Lesefehler und gültig leer bleiben getrennt. Keine Rekonstruktion, Backfill oder jüngerer Quellenersatz.
- Core-Aufnahme 40124: 0504/0505 sowie 0501/0520 in beiden Anwendungen. Identifikationsaufnahme 40125: frische 0501/0520 und V2-0525. Ereignis-/Störungsaufnahme 40126: eigene 0502/0503-Rohquellen und separate Child-Receipts. Alle ursprünglichen Aufnahmekinds, Tabellen und Digests unverändert.
- SELECT/HASH(P2=00)/frisches vollständiges READ/strikte EOF/SIGN und reale C101/Card_Sign-Policy erhalten. Lokale HASH-/SIGN-Fehler erhalten unabhängig vollständig bewiesene unsigned Originale; Transportverlust/Cancellation verwerfen die gesamte laufende Gruppe. Keine teilweise gespeicherte Gruppe, alten Request-/Batch-IDs keine neuen Belege hinzufügen.
- Neue mathematische 0504/0505-Prüfung als reine abgeleitete Leseprojektion: `verifySavedCoreSignatures(sessionId)` übernimmt bestehende Profil-/Karten-/Child-/Parent-/Fingerprint-Ablehnungen in einer Lesetransaktion; gefrorener Snapshot und Berechnung außerhalb auf Default-Dispatcher. Keine DB-Schreiboperation und kein Cache.
- GEN1: exakt referenzierter öffentlicher CAR-Katalog als Rechenmaterial, C108→C100-Recovery mit vollständigem CSM-Rahmen/Hash/Referenz/Rolle/RSA-Domäne; separat striktes PKCS1-v1.5/SHA1 für die vollständigen Dateibytes. Fehlender/unbekannter Einstieg nicht prüfbar.
- GEN2/V2: nur C101/Card_Sign-Subject, sechs feste EC-Domänen, Feld-/Kurven-/Untergruppenprüfung, feste unsigned R/S-Breiten/Range und genau einmaliges Hashen. High-S und Plain-30-Präfix zulässig; keine DER-Rettung/Kürzung/Modulo-Reparatur.
- Ergebnis an exakte Karte/Sitzung/EF/Original/Receipt/Zertifikat gebunden: VALID/INVALID/NOT_CHECKABLE mit Grund. Enginefehler nicht als mathematisch ungültig ausgeben; Cancellation propagiert. Zertifikatsvertrauen immer NOT_EVALUATED. Ursprüngliches CAPTURED_UNVERIFIED bleibt unverändert; andere EFs weiterhin unverifiziert.
- Kleine getrennte Aufnahme-/Mathematik-/Vertrauenszeilen, Quellen-/Anwendungs-/Archivwechsel und späte Coroutine-Ergebnisse abgesichert; sensible Nummern nach Wechsel erneut verborgen. Keine DB-Änderung oder UI-Neugestaltung.
- BC-Lightweight `bcprov-jdk15to18:1.86` exakt gepinnt, kein globaler Providerwechsel. Android-Keystore/SQLCipher/TLS unverändert. Room22 ohne Migration; Compilerexporte1–22 und eigenständige 19→20-/20→21-/21→22-Abnahmen erhalten.

## Nachweise für 40127

| Beleg | Ergebnis |
|---|---|
| Freigabe / Plan | Tobi 14.10.2026 06:13 Europe/Berlin; Plan-PR #81, [A1–A10](docs/CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md) |
| Vollständige Produkt-CI | [38515830418](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38515830418), Versuch 1, alle vier Jobs erfolgreich für `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc` |
| Exaktes Gerätepaket | [38530132281](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38530132281), Versuch 1, gleicher Produktkopf; Infrastruktur main `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0`, include_baseline=false |
| Neue eigene Gerätebestätigung | Tobi 14.10.2026 10:12 Europe/Berlin: „Das passt alles 👍“ nach erklärten abschließenden Chat-Testpunkten; keine Auffälligkeit |
| Sichtbare GEN1-Ergebnisse | Tätigkeiten 13780/128 Bytes, Fahrzeuge 6202/128 Bytes; beide mathematisch gültig, Zertifikatsvertrauen nicht geprüft |
| Sichtbare GEN2V2-Ergebnisse | Tätigkeiten 13780/64 Bytes, Fahrzeuge 9602/64 Bytes; beide mathematisch gültig, Zertifikatsvertrauen nicht geprüft |
| Produktmerge | `96370d0ef291af1ac3893333cd128c2a3f735033`, vollständiger identischer CI-/Gerätebaum `f818dcb8b1da48cdad7cdc555bcc7638c478b819` |
| Zusätzlicher Produkt-Main-Lauf | [38533736352](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38533736352), Versuch1, vollständig erfolgreich für Produktmerge; alle vier Jobs |

Alle sieben Gradle-Schritte, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaabgleich, vollständige API36-SQLCipher-/Compose-Suiten und Abschlussgate im endgültigen Produkt-PR bestanden. Neue Prüfungen: alle sechs EC-Domänen und RSA-Known-Answer, unabhängig in Python/OpenSSL erzeugte feste Positiv-/Negativvektoren, mathematisch gültige fachlich ungültige Zeiger, unterschiedliche Karten-/Ausstellerkurven, SQLCipher-Zeilengleichheit/Replay/Reopen/Provenienz/Tamper und Compose-Auswahl/Trust. Alle bestehenden Tests und Gates unverändert; keine Filter/Quarantäne, Workflow-/Upload-/Budgetänderung. Erfolgreiche normale CI ohne Artefakte.

Room22 aus echtem Compilerexport unverändert: SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. ZIP/APK/BUILD_INFO/Manifest VersionCode40127 und feste Testsignatur nach Download tatsächlich geprüft; APK-v2-Signatur samt Inhaltsdigest zusätzlich verifiziert. [Genauer Geräte-/Paketbeleg und Grenzen](docs/DEVICE_CONFIRMATION_40127.md), [technische Belege](docs/CARD_CORE_SIGNATURE_MATH_EVIDENCE.md).

## Vorgänger und offene Grenzen

40126/Room22 aus PR #79 (`6334b4f67485feaee4de5d649d6788475af940ec`), 40125/Room21 aus PR #76 und 40124/Room20 aus PR #73 bleiben in ihrem eigenen bestätigten Umfang erhalten. [40126-Beleg](docs/DEVICE_CONFIRMATION_40126.md), [40125-Beleg](docs/DEVICE_CONFIRMATION_40125.md), [40124-Beleg](docs/DEVICE_CONFIRMATION_40124.md) nur bei Bedarf. Diese Arbeiten nicht erneut durchführen.

Die neue Abnahme gilt für Tobis tatsächlich getestete Kombination und den exakten40127-Baum. Screenshots zeigen keine Versionsanzeige oder Importzeit; Version40127 durch abschließende Gesamtantwort bestätigt. Kein zusätzliches Telefon-/Leser-/Kartenmodell, keine bestimmte EC-Kurve, GEN2 ohne V2 oder vollständige Altarchivabnahme behaupten. Mathematische Gültigkeit bedeutet kein Zertifikatsvertrauen oder vollständigen Standarddownload.

**Nächster Planungsauftrag:** alle bereits gespeicherten Kartendateisignaturen mathematisch abdecken; [Plan A1–A8](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), beauftragt14.10.2026 10:48 Europe/Berlin. Neue EFs0501/0520/0502/0503 sowie0525 nurV2, bestehende0504/0505 erhalten. **Vollständige Umsetzung nach A1–A8 freigegeben14.10.2026 11:20 Europe/Berlin.** Gemeinsame Engine, alle gespeicherten Belegfamilien und Detailanzeigen auf40128-Produktbranch implementiert; eigene finale volle CI/Paket/neue Geräteabnahme noch am endgültigen Kopf zu bestätigen. [Belege](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md), [Gerätetest](docs/DEVICE_TEST_40128.md). Ziel40128/Room22 als reine Leseprojektion, gemeinsame geprüfte Engine, exakte eigene Belegbindung und kleine vorhandene Anzeigen; keine erneute Signaturaufnahme. Nach eigener positiver Abnahme diesen mathematischen Teil schließen und den Produktschwerpunkt auf Benutzeroberfläche/Funktionen für1.0 legen. ERCA/MSCA/Kartenvertrauen, Originalexport/DDD und Schreibvorgänge bleiben außerhalb dieses Plans.

[Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) und Regeln/Leseroutine aus WORK_START gelten weiterhin. Geräteanleitungen immer verständlich direkt im Chat. Browser/Unteragenten ausgeschlossen. Ursprüngliche unvollständige Settings-Arbeit unverändert erhalten; Planbranch/Sicherungen in der [Übergabe](docs/WORK_HANDOFF.md). 40128 ist noch nicht neu gerätebestätigt/übernommen; bestätigte Produktbasis40127 erhalten.
