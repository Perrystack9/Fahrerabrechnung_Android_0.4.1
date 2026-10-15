# Kontrollierter Abschluss M0/M1 –15.10.2026

Tobi hat am15.10.2026 15:36 Europe/Berlin M1 im bisher freigegebenen Umfang ausdrücklich abgenommen und die kontrollierte Integration PR88 zuerst, PR89 danach genehmigt. M2-Implementierung zusätzlich genehmigt; kein M2-Merge, M3–M7 oder Release.

| Schritt | Exakter Nachweis |
|---|---|
| Ausgangs-main | `df7061659b621d67ff39ac2f02f44e13d7ac4e86` |
| PR88-Endkopf | `a55d747513e79d5eb3a5961db35bd3d35d44cc78`,21 Markdown-Dateien, keine Produktänderung |
| PR88-Pflichtchecks | Docs-CI38684167403/Versuch1 erfolgreich; konservative bestehende Klassifikation android=false, keine behauptete Android-Abnahme |
| PR88-Merge zuerst | `9ff0a525d8394031148aada37593c9fed8f84148`; zusätzliche Docs-main-CI38744376819/Versuch1 erfolgreich |
| Ursprünglicher M1-Prüfkopf | `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`; volle CI38729242320/Versuch1 erfolgreich |
| Neue eigene M1-Geräteprüfung |40129/Room23, [identifiziertes Paket und tatsächliche Antwort](DEVICE_CONFIRMATION_40129.md), ausdrücklich als eigene Prüfung bestätigt; nicht auf spätere Produktänderungen übertragen |
| Abstimmung PR89 mit neuem main | `1f1655de023ae6463419be8be9278f0c1c05232e`, Baum `ac541c7c7e2d34dff67c2ff519030ea8b0d359a9` |
| Konfliktlösung | Nur WORK_START/WORK_STATUS/CURRENT_RULES/WORK_HANDOFF. Alte Freigabeformulierungen auf den neuen Auftrag abgestimmt; alle M0-/Planungsdateien und M1-Belege erhalten. Keine Produktdatei gegenüber dem Geräteprüfkopf verändert. |
| Neue volle Integrations-CI | [38744945937](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38744945937), Versuch1, exakter Kopf1f1655de: alle vier Jobs erfolgreich einschließlich sieben Gradle-Gates, kompletter ungekürzter API36-SQLCipher-/Compose-Instrumentierung und Abschlussgate |
| Letztes Mergegate | Live Kopf/Base, nicht Draft, mergefähig, alle vier Pflichtchecks erfolgreich, keine entgegenstehende Reviewanforderung; Produktgleichheit und geschützter Regelabschnitt erneut geprüft |
| PR89-Merge / neue Basis | **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**,40129/Room23 |
| Zusätzliche Produkt-main-CI |38749285258/Versuch1 für3c99b7db automatisch gestartet. Endstatus im aktuellen PR-Protokoll live nachweisen; beim Erstellen dieses Dokuments noch laufend. |

Lokal vor Integration zusätzlich Projektgrenzenprüfung, alle10 bestehenden Python-Routing-/Schema-Guard-Tests, Diff-Whitespaceprüfung und vollständiger Git-Baumvergleich bestanden. CURRENT_RULES ab Herkunftskapitel bytegleich zum bisherigen main; keine Qualitäts-/Workflow-/Sicherheitsänderung. Kein Branch oder Backup gelöscht, kein Reset/Clean.

Die verlorene lokale Settings-Sicherung bleibt ein nicht wiederhergestellter unvollständiger Entwicklungsentwurf mit unbekanntem Diff, SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`. Keine Rekonstruktion behauptet. Die erhaltene Settings-Datei ist zwischen altem main und bestätigtem M1 unverändert. Keine Kartendaten oder Geräteeinstellungen wurden dadurch verändert. [Erhaltene Verlustgrenze](WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung).
