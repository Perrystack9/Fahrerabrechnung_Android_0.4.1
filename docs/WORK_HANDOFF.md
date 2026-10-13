# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien. Branch/HEAD, Arbeitsbaum, aktuellen GitHub-main, offene PRs und relevante CI live prüfen.

**Vorherige 40125-Sitzung abgeschlossen; neuer 0502/0503-Auftrag aktiv.** Geprüfte Basis vor dem abschließenden Dokumentationsschritt: main `1007ee37ab693f5b89277b76daa91b2f5836cf53` aus PR #77, keine offenen PRs. Produkt-Main-CI [38423233063](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38423233063) vollständig erfolgreich; Dokumentations-Main-CI [38424648944](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38424648944) erfolgreich. Ergebnisse live erneut geprüft; keine laufende Prüfung aus dieser Produktabnahme übrig. Der anschließende Abschluss ändert nur WORK_START, WORK_STATUS und diese Übergabe.

## Abgeschlossener Produkt-Meilenstein

**40125 / Schema 21 übernommen, eigene neue Gerätebestätigung erhalten.** [PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76), Merge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, vollständiger Produktbaum `36e014af6cbffa9add766bdd917c6d94313f87ec` identisch zum geprüften Kopf `20d34a0fc7ce370fb4c1d22002905a500574ee06`. Umsetzung nicht wiederholen.

[Volle Produkt-CI 38408152663](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38408152663) erfolgreich: alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaexport, ungekürzte API36-SQLCipher-/Compose-Suiten und Abschlussgate. Eigene 19→20-Abnahme vollständig erhalten, zusätzlich verschlüsselte 18-/19-/20→21 mit korrekt rekonstruierten Alt-Schemas. Keine Filter/Quarantäne oder CI-/Workflow-/Upload-/Budgetänderung. Normale erfolgreiche CI ohne Artefakte. Lokaler Gradle-Download blockiert.

[Gerätepaket 38414059875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38414059875), Versuch 1, erfolgreich für denselben Produktkopf. Infrastruktur `07db91f33957a45598cae6e4772787d1aa7ddbf4`, `include_baseline=false`; Manifest VersionCode 40125. Archiv/BUILD_INFO/Manifest/Testsignatur/ZIP-/APK-Prüfsummen tatsächlich geprüft, aktuelle Checkliste im Paket. [Detaillierter Beleg](DEVICE_CONFIRMATION_40125.md).

Tobi bestätigt am 13.10.2026 17:32 Europe/Berlin: „Wurde getestet, und funktioniert 1a“. Positive Gesamtrückmeldung zum bereitgestellten neuen Paket; Einzelmesswerte, Screenshots, einzelne Testschritte oder Modellnamen wurden diesmal nicht separat wiederholt. Gerätegate für diesen konkret bestätigten Produktbaum und eigenen Umfang erfüllt; keine pauschale Hardware-/Kurven-/Sonderfallabdeckung daraus ableiten. Frühere 40124-Bestätigung dient nur als erhaltene Basis.

Neu übernommen: 0501/0520 beide Anwendungen, 0525 nur V2; frische post-HASH-READs, strikte 6700/6B00-Grenze, C101-Policy/Le und nicht tolerierter Gruppenabbruch bei Bootstrap-Widerspruch. Separate Child-Receipts mit genauen Original-/Zertifikats-FKs; null Original-FK nach fehlgeschlagenem neuen READ und unabhängiger unsigned Fallback. Keine nachträglichen Signaturen/Quellenersatz; leere neue Map lässt alte Fingerprints exakt. Core-Belege unverändert, additive 20→21, kleine maskierte Quellenanzeige. 15-Byte-G2-Kompatibilität erhalten, V2-15 unsigned. Room21 echt, SHA256 `8cd199f6012938fda80f8f8f265b49224b7fd8c127d2ad913b7926784eafa8c9`; Exporte 1–20 unverändert, alle 43 bisherigen Tabellen gleich.

## Aktiver Auftrag 40126

Tobi hat am 13.10.2026 die vorgeschlagene 0502/0503-Signaturaufnahme freigegeben. Neuer [Plan](CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md), Draft [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79), 40126 / Schema 22. Produktbasis 40125 oben bleibt übernommen; dieser neue Umfang noch nicht gerätebestätigt und nicht gemergt. Die CI des tatsächlichen endgültigen PR-Kopfes live prüfen; keine Checkpoint-Chronik.

Separate Roh-Metadatenquellen und Signaturreceipts, neue Reader-/Herkunfts-/Rollback-/Historien-/Migrations-/Compose-Tests. Room 22 aus dem echten ersten CI-Compilerexport übernommen, SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`; alte Exporte 1–21 und alle 44 bisherigen Tabellen unverändert. Vor Merge volle CI, exaktes Paket und eigene neue positive Gerätebestätigung. [Checkliste 40126](DEVICE_TEST_40126.md) über den bestehenden Paketpfad eingebettet. Keine CI-, Budget- oder Workflowänderung.

Neuer Checkout `/workspace/scratch/4c3cf966d792/FahrerTagebuch-event-fault-signatures`, lokaler Branch `work/40126-event-fault-remote`, Remote-Branch `feat/event-fault-card-signatures-40126`. Git-Push aus dieser Umgebung ohne Schreibcredential; Veröffentlichung über verbundenen GitHub-Connector, danach identischen Tree fetch/sicher prüfen. Lokaler erster Implementierungscommit bleibt in `preserve/40126-local-tree-before-remote` erhalten. Keine Resets oder Rewrites.

Alte unvollständige Settings-Arbeit im ursprünglichen Checkout unverändert; zusätzlich Datei und Patch mit SHA256 in `/workspace/scratch/4c3cf966d792/session-backups`. Nicht integrieren oder verwerfen. Keine abgeschlossene 40125-Umsetzung, Paket- oder Geräteabnahme erneut durchführen. Diesen Abschnitt nach eigenen tatsächlichen CI-/Geräteergebnissen ersetzen.

## Grenzen

Aufnahme bleibt ungeprüft. Weitere EFs, mathematische Prüfung/ERCA/MSCA-Vertrauen, vollständige Kartenabdeckung, Originalexport/DDD/Standarddownload und Card_Download-Schreiben getrennt. `7F66` ohne ISO-Primärformat, `0540` ohne legitimen SC5-Weg gesperrt. Import/Profile/Historie/SQLCipher/Keystore erhalten, keine destructive fallback migration. Keine Spesen-/Lohn-/Abrechnungs-/Heimatstandortphase; spätere neue Oberfläche nicht gleichzeitig beginnen. Browser und Unteragenten ausgeschlossen.
