# FahrerTagebuch - aktueller technischer Zustand

**40124 / Room-Schema 20, eigener Geräteumfang bestätigt.** [PR #73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73) gemergt: `3298b707525a4f33162c835ce31deaf181d2e772`. Identischer Tree `3540b26b775d17c9bdc6335549dafbfbd58bcd92` zum vollständig CI- und gerätegeprüften Produktkopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`. Aktuellen main, offene PRs und CI stets live prüfen; Produktregeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und übernommen

- Automatischer Vordergrund-Import, USB-Freigabe, eigene Profil-/Kartenbindung und Folge-/Ersatzkartenregeln; SQLCipher/Keystore, additive Migrationen und exakte Importhistorie.
- Gemeinsamer Gen1-/Gen2-Lesevorgang mit atomarem Parent/Child, MF am Parent und ausdrücklicher Anwendungswahl. V2 bleibt im Gen2-AID; beide Tätigkeitsbestände nicht addieren.
- Vollständige unabhängige 0504-/0505-READ-BINARY-Originale einschließlich Pointer/unbenutzter Bytes, defensive Kopien vor Parser und vor wartender Kartenbestätigung; auch bei EMPTY/INVALID erhalten. Fehlend/nicht versucht/Lesefehler getrennt, keine Teilbytes oder Teilgruppe bei Transportverlust.
- Echte neue 0504-/0505-Kartensignaturen in beiden Anwendungen: SELECT→HASH(P2=00)→vollständiges READ→defensive Dateigrenze→SIGN, ohne fremde APDU dazwischen. Card_Sign-Policy und tatsächliche Länge, serialisierte powered Reader-Sessions. Lokaler Signaturfehler erhält unsigned Rohdaten; Transportverlust verwirft die laufende Gruppe.
- Unveränderliche sitzungseigene Receipts mit FKs zur gleichen Original-/Zertifikatsquelle, eigener Fingerprint und additive Migration 19→20. Alte Imports, Quellen und Request-Fingerprints ohne Signatur-Backfill oder jüngeren Ersatz.
- Gespeicherte Vorschau zeigt Originalgrößen, Signaturstatus/-länge/Grund und ausdrücklich „aufgenommen · ungeprüft“. Quellenwahl und Altarchiv setzen die Anzeige passend zurück. Bestehende neutrale Tätigkeits-/Fahrzeug-/GNSS-/Orts-/Ereignis-/Störungsquellen erhalten; keine neue Navigation.

## Nachweise und Umfang

| Beleg | Ergebnis |
|---|---|
| Vollständige Produkt-CI | [38381962283](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38381962283), erfolgreich für `a2e22d882bb707fe1a446c3a24f78387caa892b0` |
| Gerätepaket | [38388192092](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38388192092), erfolgreich für denselben Quellkopf, `include_baseline=false` |
| Gerätebestätigung | 13.10.2026 13:57 Europe/Berlin, Tobi: sämtliche Checklistenpunkte problemfrei |
| Originalgrößen | GEN1 13780/6202 Bytes; GEN2V2 13780/9602 Bytes |
| Aufgenommene Signaturen | GEN1 Tätigkeiten/Fahrzeuge jeweils 128 Bytes; GEN2V2 jeweils 64 Bytes; alle vier ungeprüft |
| Produktmerge | `3298b707525a4f33162c835ce31deaf181d2e772`, identischer vollständiger Tree zum geprüften Produktkopf |
| Zusätzliche Produkt-Main-CI | [38394108412](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38394108412), vollständig erfolgreich einschließlich API36 und Abschlussgate; am Einstieg live geprüft |

Alle sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexportprüfung, komplette SQLCipher-/Compose-Suiten auf API 36 und Abschlussjob bestanden. Eigene verschlüsselte 19→20- und 18→20-Migrationsprüfungen, Gruppenrollback, exakte Historie, negative Signatur-/Readerfälle und Inhaltsvergleiche ohne Objektidentitätsfehler erhalten. Keine Assertions abgeschwächt, Tests gefiltert/quarantänisiert oder Workflow-/Upload-/Budgetregeln geändert. Normale erfolgreiche CI ohne Artefaktupload; Paket separat gezielt erzeugt.

Room-20-Export ausschließlich aus echtem Compilerlauf übernommen, SHA256 `34a9253cb546255ce9c12bef2d9808933e1af8a8dca1c27a783eb7322e6ab6e6`; endgültiger Build- und Emulatorjob bestätigen ihn. Schema 19 unverändert. Lokaler Gradle-Download netzbedingt blockiert; Android-Nachweis aus GitHub-CI.

Tobi bestätigt Update/Altbestand, eigenen Import, vier Signaturanzeigen, plausible fachliche Daten, Wiederöffnung ohne Leser, historische Anwendungen/Sitzungen und Altarchiv soweit vorhanden, erneute Aufnahme ohne doppelte Tätigkeiten und Kartenentnahme mit erfolgreicher Wiederaufnahme. Eigenes Android-Gerät und bisheriger USB-Leser; genaue Modelle nicht erneut genannt. [Genauer Gerätebeleg](docs/DEVICE_CONFIRMATION_40124.md). Reine GEN2-Karten, andere Hardware, alle seltenen APDU-Abbruchphasen und alle Kurven nicht aus diesem Geräteumfang ableiten.

Die vorherige 40123-/Schema-19-Basis aus PR #70 bleibt erhalten; ihr eigener damaliger Umfang steht in [DEVICE_CONFIRMATION_40123.md](docs/DEVICE_CONFIRMATION_40123.md). Wiederholungs-/Abbruchfälle waren damals nicht separat bestätigt und sind erst für den jetzigen 40124-Umfang ausdrücklich bestätigt.

## Exakt nächste Arbeit und offene Grenzen

Zusätzliche volle Produkt-Main-CI 38394108412 und Docs-Main-CI 38395123764 am Einstieg erfolgreich geprüft. Der nächste begrenzte [Identifikations-Signaturplan](docs/CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md) ist ausgearbeitet: 0501/0520 in beiden Anwendungen, 0525 bedingt auf V2, neue post-HASH-READs mit bytegenauem Bootstrap-Vergleich, eigene sitzungseigene Receipts und additive 20→21. [40125-Gerätecheckliste](docs/DEVICE_TEST_40125.md) vorbereitet, nicht durchgeführt. Planbasis main 71eac71; Primärmatrix um aktuellen 40124-Signaturstand berichtigt. Keine App-/DB-/Schema-/Workflowänderung in diesem Docs-Meilenstein.

**Nächste Arbeit:** aktuellen main/PR/CI live prüfen, Plan gezielt öffnen und **40125 / Schema 21** als eigenen Produkt-Meilenstein umsetzen; Modelle/Befehls-/Konsistenztests zuerst, dann Aufnahme/atomare Speicherung/Migration/kleine Anzeige. Eigene vollständige CI, exaktes Paket und erforderliche neue Gerätebestätigung vor Merge. Die App bleibt bis zur Umsetzung 40124 / Schema 20.

Offen bleiben mathematische Signaturprüfung, produktive ERCA/MSCA/Card_Sign-Vertrauenskette, vollständige Kartenabdeckung, Originalquellenexport und DDD-/Standarddownload. `7F66` braucht ISO-Primärformat, `0540` legitimen SC5-Weg. Automatischer Import bleibt ohne LastCardDownload-Schreiben. Neue Oberfläche/Einrichtung/Onboarding/GNSS-Karte später; keine Abrechnungs-/Heimatstandortphase. [Aktuelle Übergabe](docs/WORK_HANDOFF.md).
