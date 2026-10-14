# FahrerTagebuch - aktueller technischer Zustand

**40128 / unverändertes Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`; vollständiger identischer Baum `066dc0532fd2de486def695323b74f8e9d1150bd` zum endgültigen CI-/Gerätekopf `7f5f770aa71ec70bb8f02d51cae3403a6f904637`. Volle CI [38547729369](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38547729369) und exaktes Gerätepaket [38553890752](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38553890752), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach der vollständigen neunteiligen Chat-Testanleitung am **14.10.2026 13:47 Europe/Berlin: „Funktioniert alles wie es soll 👍“**. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40128.md).

Aktuellen main, offene PRs und CI live prüfen. Regeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und erhalten

- Automatischer eigener Vordergrund-Import, Profil-/Kartenbindung, unabhängige GEN1-/GEN2-Anwendungen, MF am Parent, atomare SQLCipher-Speicherung und Keystore-Schutz erhalten. Gen2V2 bleibt Ausprägung der Gen2-Anwendung; beide Zeitsätze nicht addieren.
- Vollständige Originale einschließlich Pointer/unbenutzter Bytes, konkrete Parent-/Child-Sitzungen, historische Quellen und sämtliche Request-/Original-/Certificate-/Receipt-Fingerprints erhalten. Altarchiv bleibt getrennt; keine Rekonstruktion oder jüngerer Ersatz.
- Bestätigte Aufnahme 40124–40126 unverändert: Core 0504/0505, frische Identifikationsbelege 0501/0520/V2-0525 und Ereignis-/Störungsbelege 0502/0503. SELECT/HASH(P2=00)/frisches READ/strikte EOF/SIGN und C101/Card_Sign-Policy erhalten.
- Lokale HASH-/SIGN-Fehler erhalten unabhängig bewiesene unsigned Originale; Transportverlust/Cancellation verwerfen die laufende Gruppe. Kein Hinzufügen neuer Belege zu alten Request-/Batch-IDs.
- **Mathematische Prüfung aller vorhandenen Belegfamilien abgeschlossen:** `verifySavedSignatures` übernimmt Core-, Identity- und Event/Fault-Receipts aus einem validierten eingefrorenen Read-Snapshot. Bestehende `verifySavedCoreSignatures` und Core-Ergebnis-API bleiben kompatibel. Berechnung außerhalb der Lesetransaktion auf Default-Dispatcher; defensive Bytes und Cancellation.
- GEN1: exakt referenzierter unveränderter öffentlicher CAR-Katalog, strikte C108→C100-Recovery und getrennt striktes PKCS1-v1.5/SHA1 für vollständige Originalbytes. Fehlender/unbekannter Einstieg nicht prüfbar.
- GEN2/V2: eigenes C101/Card_Sign-Subject, sechs feste EC-Domänen, Feld-/Kurven-/Untergruppenprüfung, feste unsigned R/S-Breiten/Range und Hash einmal. High-S/Plain-30 zulässig; keine DER-/Kürzungs-/Modulo-Reparatur.
- Jeder Befund bindet Karte, Sitzung/Batch, Anwendung/Generation, EF/Familie, Original-/Receipt-/Zertifikats-/Ausstellerdigests und Quellen-IDs. Kein unsigned oder jüngerer Ersatz für einen signierten Receipt. VALID/INVALID/NOT_CHECKABLE mit Grund; Enginefehler kein INVALID.
- Kleine bestehende Detailanzeigen mit getrenntem Aufnahme-, Mathematik- und Vertrauensstatus. Zertifikatsvertrauen stets NOT_EVALUATED; persistiertes CAPTURED_UNVERIFIED unverändert. EF-/Karten-/Sitzungs-/Archivwechsel, späte Ergebnisse und erneute Maskierung abgesichert.
- Room22 ohne Migration, Tabellen und Compilerexporte1–22 unverändert. Keine DB-Schreiboperation, Cache, Backfill oder neue Reader-Befehle. BC-Lightweight1.86 exakt gepinnt, kein Providerwechsel; Android-Keystore/SQLCipher/TLS erhalten.

## Abgeschlossene Abdeckung

| Dateien | Generationen |
|---|---|
| 0504 Tätigkeiten / 0505 Fahrzeuge | GEN1, GEN2, GEN2V2; bestätigtes40127 erhalten |
| 0501 Kartenanwendung / 0520 Identifikation | GEN1, GEN2, GEN2V2 |
| 0502 Ereignisse / 0503 Störungen | GEN1, GEN2, GEN2V2 |
| 0525 Kartenanwendung V2 | ausschließlich GEN2V2 bei vorhandenem Beleg |

Gültig leere vollständige Quellen, unterstütztes15-Byte-Gen2-0501 und Blank-V2-0525 geprüft. Fehlende/fehlgeschlagene/nicht versuchte Aufnahme und fehlende Voraussetzungen bleiben getrennt nicht prüfbar; alte unsigned Einträge erhalten keine Signatur oder fremde gültige Ergebnisse. GEN1/reineGEN2:0525 nicht vorgesehen.

## Nachweise und Grenzen

A1–A8 erfüllt; [Plan](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), [technische Nachweise](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md), [Gerätebeleg](docs/DEVICE_CONFIRMATION_40128.md). 18 isolierte lokale JVM-Kryptotests, neue unabhängige Python/OpenSSL-Vektoren und finale volle reale Android-CI erfolgreich. Alle sieben Gradle-Aufrufe, Unit/Lint/APK-/Testsignatur-/Updatepaar-/Compiler-Schemavergleich, ungekürzte API36-SQLCipher-/Compose-Suiten und Abschlussjob bestanden. Alte Tests/Gates unverändert; keine Filter/Quarantäne, Sicherheits-/Migrations-/Workflow-/Upload-/Budgetänderung.

Room22-Compilerexport-SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. Paket nach Download tatsächlich geprüft: BUILD_INFO, Archiv-/ZIP-/APK-Hashes, Manifest40128, festes Testzertifikat sowie APK-v2-Signatur und Inhaltsdigest. APK/ZIP und verständliche neunteilige Anleitung im Chat bereitgestellt. Eigene positive Gesamtantwort13:47; keine zusätzlichen Einzelmesswerte oder weitere Hardware-/Kurven-/GEN2-ohne-V2-Abdeckung aus dieser Gesamtantwort behaupten. Späterer Screenshot13:59 zeigt zusätzlich GEN2V2-Core0504/0505 jeweils mathematisch gültig, Vertrauen nicht geprüft; genaue sichtbare Werte im Gerätebeleg, kein neuer Einzelbeleg für die fünf weiteren EFs.

Zusätzliche volle Produkt-Main-CI [38559255131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38559255131), Versuch1, für Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405` jetzt vollständig erfolgreich; alle vier Jobs einschließlich ungekürzter API36-SQLCipher-/Compose-Instrumentierung und Abschlussgate. Am14.10.2026 nach Übergabeauftrag live bestätigt. Keine offenen40128-Prüfungen; Produktmerge besitzt exakt den endgültigen CI-/Gerätebaum.

## Abgeschlossene PRs

| PR | Bestätigter Umfang |
|---|---|
| #73 /40124 | Core-Kartensignaturaufnahme; übernommen und eigene Abnahme erhalten |
| #76 /40125 | Identifikationssignaturaufnahme; übernommen und eigene Abnahme erhalten |
| #79 /40126 | Ereignis-/Störungssignaturaufnahme, Room22; übernommen und eigene Abnahme erhalten |
| #82 /40127 | Mathematische0504/0505-Prüfung; übernommen und eigene Abnahme erhalten |
| #84 | Freigegebener40128-Plan A1–A8; übernommen |
| #85 /40128 | Mathematische Prüfung aller gespeicherten Dateisignaturen; übernommen und neu abgenommen |
| #86 |40128-Gerätebeleg, technische Nachweise und Abschlussdokumentation; übernommen, Docs-CI grün |
| #87 | Vollständige Übergabe und Priorität Verstoß-Engine1.0; übernommen, Docs-CI grün |

Übergabe-PR [#87](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/87) übernommen, main `df7061659b621d67ff39ac2f02f44e13d7ac4e86`; Docs-PR-CI38562392675 und Docs-main-CI38562576031 erfolgreich. Vor Veröffentlichung des jetzigen Planungsentwurfs keine offenen PRs/laufenden Workflows gefunden. Bestätigter Produktcode40128 unverändert; ausschließlich spätere Dokumentation.

## Nächster Produktschwerpunkt

**Mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen abgeschlossen.** Keine weitere automatische Kryptografiephase. Zertifikatsvertrauen bleibt ungeprüft; ERCA/MSCA-Vertrauen, DDD/Originalexport, neue Signaturaufnahmen und Kartenschreiben außerhalb dieses Meilensteins.

**Vollständige Anforderungsübergabe erfolgt:** Tobis80Entscheidungen und GrundsätzeA–H sind [verbindlich dokumentiert](docs/VIOLATION_ENGINE_PRODUCT_DECISIONS_80.md). Anlage unverändert; maßgeblich die vollständige neue Nachricht. [Planungsentwurf](docs/VIOLATION_ENGINE_V1_PLAN.md), [gesetzliche Matrix](docs/VIOLATION_ENGINE_LEGAL_MATRIX.md) und [Testmatrix](docs/VIOLATION_ENGINE_TEST_MATRIX.md) enthalten Architektur/Datenbedarf/Arbeitspakete,57Regelzeilen,98fachliche Fälle,31Daten-/Technikfälle,80PD-Zuordnungen und16Abnahmekriterien. **Das sind geplante Nachweise, keine neu bestandenen Tests und keine implementierte Engine.**

Fachlich/technisch geprüft: vorhandene Module und EF0504/0522/GEN2V2-0530-Pfade; beide getrennten Anwendungsquellen, Primärfilter von `all/latest`, Import-Commitgrenze, Zeitdarstellung und App-Speicherstichtage. EF0504-c-Bit bei entnommener Karte bedeutet nicht Crew; semantischer Adapter und entsprechende Tests sind nur geplant. Reuse statt neuer Reader/Parser/Imports.

Offen: Tobis Planprüfung und ProduktpräzisierungenP01–P05; vollständige amtliche Quellen-/Wirksamkeits-/Katalogprüfung sowieL01–L09, anschließend konkrete Umsetzungsfreigabe. EUR-Lex direkt teilweise durch JavaScript-Prüfseite nicht lesbar; amtlich indexierte Artikelabschnitte und geöffnete Kommissionsleitlinien wurden ausgewertet, kein vollständiger Gesetzesvolltextpin/keine unabhängige Fachabnahme behauptet. Betatest und sämtliche neuen Engine-CI-/Geräteabnahmen liegen erst nach Implementierung an.

Neue Priorität unverändert561/2006-Engine, danach Kalender/Auswertungen/neue Oberfläche. Lokaler PDF-Verstoßbericht nach59/71–75/78 in1.0; Lohn/Spesen/Abrechnung/DDD/Sync/Smartphone-Ortung bleiben außerhalb. Kein Produktcode, Schema, Workflow, Testfilter oder Paket geändert. Isolierter Docs-Branch `docs/v1-violation-engine-plan-20261014`; Draft-PR/CI vor weiterem Start live prüfen. Browser/Unteragenten ausgeschlossen. Offene Settings-Datei und hashgleiche Sicherung unverändert erhalten.
