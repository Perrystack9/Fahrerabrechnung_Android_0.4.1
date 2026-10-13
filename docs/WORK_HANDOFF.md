# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien. Branch/HEAD, Arbeitsbaum, aktuellen GitHub-main, offene PRs und relevante CI live prüfen.

## Abgeschlossener Produkt-Meilenstein

**40125 / Schema 21 übernommen, eigene neue Gerätebestätigung erhalten.** [PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76), Merge `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, vollständiger Produktbaum `36e014af6cbffa9add766bdd917c6d94313f87ec` identisch zum geprüften Kopf `20d34a0fc7ce370fb4c1d22002905a500574ee06`. Umsetzung nicht wiederholen.

[Volle Produkt-CI 38408152663](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38408152663) erfolgreich: alle sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaexport, ungekürzte API36-SQLCipher-/Compose-Suiten und Abschlussgate. Eigene 19→20-Abnahme vollständig erhalten, zusätzlich verschlüsselte 18-/19-/20→21 mit korrekt rekonstruierten Alt-Schemas. Keine Filter/Quarantäne oder CI-/Workflow-/Upload-/Budgetänderung. Normale erfolgreiche CI ohne Artefakte. Lokaler Gradle-Download blockiert.

[Gerätepaket 38414059875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38414059875), Versuch 1, erfolgreich für denselben Produktkopf. Infrastruktur `07db91f33957a45598cae6e4772787d1aa7ddbf4`, `include_baseline=false`; Manifest VersionCode 40125. Archiv/BUILD_INFO/Manifest/Testsignatur/ZIP-/APK-Prüfsummen tatsächlich geprüft, aktuelle Checkliste im Paket. [Detaillierter Beleg](DEVICE_CONFIRMATION_40125.md).

Tobi bestätigt am 13.10.2026 17:32 Europe/Berlin: „Wurde getestet, und funktioniert 1a“. Positive Gesamtrückmeldung zum bereitgestellten neuen Paket; Einzelmesswerte, Screenshots, einzelne Testschritte oder Modellnamen wurden diesmal nicht separat wiederholt. Gerätegate für diesen konkret bestätigten Produktbaum und eigenen Umfang erfüllt; keine pauschale Hardware-/Kurven-/Sonderfallabdeckung daraus ableiten. Frühere 40124-Bestätigung dient nur als erhaltene Basis.

Neu übernommen: 0501/0520 beide Anwendungen, 0525 nur V2; frische post-HASH-READs, strikte 6700/6B00-Grenze, C101-Policy/Le und nicht tolerierter Gruppenabbruch bei Bootstrap-Widerspruch. Separate Child-Receipts mit genauen Original-/Zertifikats-FKs; null Original-FK nach fehlgeschlagenem neuen READ und unabhängiger unsigned Fallback. Keine nachträglichen Signaturen/Quellenersatz; leere neue Map lässt alte Fingerprints exakt. Core-Belege unverändert, additive 20→21, kleine maskierte Quellenanzeige. 15-Byte-G2-Kompatibilität erhalten, V2-15 unsigned. Room21 echt, SHA256 `8cd199f6012938fda80f8f8f265b49224b7fd8c127d2ad913b7926784eafa8c9`; Exporte 1–20 unverändert, alle 43 bisherigen Tabellen gleich.

## Exakt nächster Einstieg

1. Aktuellen main und offene PRs/Checks prüfen. Zusätzlicher Produkt-Main-Lauf [38423233063](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38423233063) gehört zum identischen Produktmerge; seinen aktuellen Status und die reine Dokumentations-CI live abfragen, keine alten Pending-/Grün-Angaben übernehmen.
2. Vorhandene lokale Arbeit erhalten: ursprünglicher Checkout `/workspace/scratch/4546aa92dbbb/FahrerTagebuch`, Branch `verified/40125-pr76-select-fixture-final`, angefangene uncommitted Änderung an `feature/src/main/kotlin/de/fahrerabrechnung/feature/settings/SettingsScreen.kt`. Beim Einstieg war der Text unvollständig. Nicht verwerfen, automatisch integrieren oder den geprüften Stand daraus neu bauen. Abnahmedokumentation wurde im isolierten Worktree `FahrerTagebuch-40125-acceptance` erstellt. Tatsächliche lokale Zustände erneut prüfen.
3. Nach Sicherung dieses Meilensteins Sitzung abschließen. Nächsten begrenzten Auftrag ausdrücklich auswählen/planen; kein bereits freigegebener Folge-Produktumfang aus offenen Optionen ableiten.

## Grenzen

Aufnahme bleibt ungeprüft. Weitere EFs, mathematische Prüfung/ERCA/MSCA-Vertrauen, vollständige Kartenabdeckung, Originalexport/DDD/Standarddownload und Card_Download-Schreiben getrennt. `7F66` ohne ISO-Primärformat, `0540` ohne legitimen SC5-Weg gesperrt. Import/Profile/Historie/SQLCipher/Keystore erhalten, keine destructive fallback migration. Keine Spesen-/Lohn-/Abrechnungs-/Heimatstandortphase; spätere neue Oberfläche nicht gleichzeitig beginnen. Browser und Unteragenten ausgeschlossen.
