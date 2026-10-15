# FahrerTagebuch - aktueller technischer Zustand

**M1-Entwicklungsstand40129/Room23; noch kein Merge und keine neue Geräteabnahme.** Tobi hat ausschließlich M1 freigegeben. [Implementierung](docs/VIOLATION_ENGINE_M1_IMPLEMENTATION.md), [MI-Abnahme und Prüfgates](docs/VIOLATION_ENGINE_M1_ACCEPTANCE.md). Branch `feature/v1-m1-input-foundation-20261015`, main-Basis `df7061659b621d67ff39ac2f02f44e13d7ac4e86`. Die nachfolgende40128-Bestätigung ist der erhaltene Vorgängerbeleg.

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

Dokumentationsabschluss [PR #86](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/86) übernommen, Merge `b61ed8aedbab53847c5361f05b61ef280dded613`; Docs-PR-CI38559979155 und Docs-main-CI38560196780 erfolgreich. Bei der Übergabeprüfung keine offenen PRs; aktuelle Übergabeänderungen auf eigenem Docs-Branch. Aktuellen main und späteren Übergabe-PR/-Merge weiterhin live prüfen.

## Aktueller M1-Umfang und verbleibende Gates

M1 implementiert den konsistenten eigenen G1-/G2-Lesesnapshot, tatsächliche c/p-Semantik, UTC-Intervalle mit belegten Enden, konservative Konflikte/Lücken, deduplizierte Minuten mit vollständigen Herkunftsbezügen, regelbezogene Eingangsabdeckung sowie getrennten zeitlich versionierten Kontext und minimalen Scope-Guard. Kalter Commit-/Revisions-Flow als Eingang für M2; kein gesetzlicher Motor, keine neue Oberfläche/PDF/Beta-Funktion.34 neue Domain- und24 neue Androidmethoden mit Zuordnung MI01–MI24. Echtes Compiler-Schema23 ergänzt nur `violation_context_revisions`; alle45 bisherigen Entities und alle bisherigen Exportdateien bleiben unverändert.

Lokale vollständige Unit-Suite559 Tests/0 Fehler und Androidtest-Kompilierung ausgeführt; die vollständigen finalen sieben Gradle-Aufrufe, CI einschließlich echter API36-SQLCipher-/Compose-Suiten, Updatepaar/Signatur sowie neue eigene Geräteprüfung sind commitgebunden im M1-PR/Abschlussbericht nachzuweisen. Kompilierte Instrumentierung ist kein ausgeführter Gerätetest. Keines dieser neuen Gates wird durch40128 ersetzt.

[Planungs-PR88](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/88), Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78`, bleibt Draft/unmerged. PD01–PD80/A–H/E01–E12/P01–P05 und M0/N01–N19 sind dokumentiert und übernommen. Keine offenen Produktfragen erfinden. M1 ist ausdrücklich implementierungsfreigegeben; **M2–M7 und Produktmerges benötigen neue ausdrückliche Freigaben**. Die späteren gesetzlichen Nachweise, unabhängige qualifizierte Fachprüfung und Beta5–10 Fahrer bleiben an ihren Gates.

Bestehende Kryptografie mit40128 abgeschlossen. Zertifikatsvertrauen weiter NOT_EVALUATED; keine automatische Trust-/Export-/Kartenschreibphase. Kalender, Auswertungen und neue Oberfläche folgen nach dem Engine-Plan. Regeln unverändert respektieren. Nach automatischer Workspace-Bereinigung ist M1 aus GitHub wiederhergestellt; die getrennte frühere Settings-Sicherung ist derzeit nicht wiederhergestellt, siehe [Übergabe](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Browser/Unteragenten ausgeschlossen.
