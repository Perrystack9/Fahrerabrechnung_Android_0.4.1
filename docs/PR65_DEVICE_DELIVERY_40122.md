Aktueller Abschluss12.10.2026: [Tobis ausdrückliche40122-Bestätigung](DEVICE_CONFIRMATION_40122.md) liegt vor; PR65 unverändert gemergt als21edb80, Tree exakt gleich dem Teststand. Die folgende Auslieferungsdokumentation behält ihre damaligen Paketbelege und ursprüngliche Hardware-Sperre als Historie.

---

# PR65: ausgelieferter Gerätecheckpoint 40122

Stand 12.10.2026. **Vollständige CI und aktuelle APK-/Paketprüfung bestanden; Test-ZIP bereitgestellt. Hardwarebestätigung ausstehend. PR65 bleibt offen/Entwurf, kein Merge oder Auto-Merge.**

## Unveränderter Produktstand und tatsächlich grüne CI

| Beleg | Wert |
|---|---|
| PR | [PR65](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/65) |
| Produktcommit | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Produktbaum / getesteter Merge-Baum | `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` |
| Test-Merge-Commit | `5b96c098f8459efbe4cfcb1e2eafee0217c5c664` |
| Produktmanifest | 302 Blobs, SHA256 `72f9a96d655651389ef993efd80c49e86e07bc57bc4e32a01b126d979560afbb` |
| Vollständige CI | [38243540172, Versuch 4](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38243540172/attempts/4), completed / success |
| CI-Abschluss | 12.10.2026, 16:32:47 UTC / 18:32:47 Europe/Berlin |
| Gerätetestkandidat | 40122 / Schema 19 |
| Letzte bestätigte Basis | Main `ace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b`, 40121 / Schema 18 |

Tobi meldete um 18:12 Europe/Berlin die Budgetfreigabe. Ausschließlich die zuvor fehlgeschlagenen Jobs wurden am bestehenden Lauf erneut gestartet. Sie bekamen diesmal tatsächlich Runner und führten ihre Schritte aus. Der Agent änderte keine Budget-/Stop-usage-Einstellungen; ein neuer Betrag wurde nicht mitgeteilt. Keine weitere Paket-CI gestartet.

| Pflichtjob | Job-ID | Ergebnis | Tatsächlicher Start / Abschluss UTC |
|---|---|---|---|
| Room SQLCipher and Compose instrumentation | 114900718817 | success | 2026-10-12T16:12:29Z / 2026-10-12T16:32:38Z |
| Project boundaries and CI scope | 114900719470 | success | 2026-10-12T11:34:43Z / 2026-10-12T11:34:51Z |
| Build, test, lint and APK verification | 114900749956 | success | 2026-10-12T11:34:54Z / 2026-10-12T11:43:50Z |
| Android verification | 114908499092 | success | 2026-10-12T16:32:42Z / 2026-10-12T16:32:46Z |

Grenz- und Build-Prüfungen aus Versuch 1 sind erhalten, einschließlich aller sieben Gradle-Pflichtkommandos, Lint, APK-/Signatur-/Updatepaar40121→40122 und Room-Exporten. Instrumentierung wurde jetzt vollständig auf dem normalen API36-Emulator ausgeführt: `:app:connectedDebugAndroidTest` und `:data:connectedDebugAndroidTest`, ohne Testfilter oder Quarantäne. Beide Tasks melden reale Geräteausführung; Gesamtbuild erfolgreich nach 16m15s. Der vorherige bestehende Compose-Dispose-Fehler hat diesen vollständigen Lauf nicht erneut scheitern lassen; keine ungeprüfte Produkt-/Lifecycle-Korrektur vorgenommen. Erfolgreiche Instrumentierungsberichte werden nach Projektregel nicht als Actions-Artefakte hochgeladen; konkrete Fallzahlen werden aus dem erfolgreichen Konsolenlog nicht erfunden.

## Geprüftes Testpaket

| Paketbeleg | Wert |
|---|---|
| ZIP | `FahrerTagebuch-PR65-Test-Update.zip` |
| ZIP-Größe | 25476225 Bytes |
| ZIP SHA256 | `d3f1750b41029ed9aaeac7e075a0ef648402aa4bc7adfa889a911b45c7169193` |
| APK | `02-FahrerTagebuch-Test-Update.apk` |
| APK-Größe | 30083677 Bytes |
| APK SHA256 | `c6ccf05b04583e9e80ac84aa8c4743be8b47be383bd7b222e13638702305542c` |
| Paketkennung | `de.fahrerabrechnung.app.devicetest` |
| VersionCode / Schema | 40122 / 19 |
| Stabiles Testzertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Schema19 SHA256 | `01cdfdeee311e13efdcebd74d66783103f5bd0a8423ad8fedc8b9c12f897892e` |
| Room-Identität im Schema und APK | `055b3aad563fd07d68ec0e56bf533124` |

Die aktuelle APK wurde aus einem separaten Checkout exakt dieses Produktkopfs lokal erstellt. Offizielle Gradle9.6.1-Distribution per SHA256 geprüft, vollständiges TemurinJDK17.0.20+8 per veröffentlichter SHA256 geprüft, Android37.0_r02 / Build Tools36.0.0 per offiziellen Repository-Prüfsummen geprüft. Die vorhandene lokale Java-Laufzeit enthielt zunächst keinen Compiler; danach waren einzelne Google-Bibliotheksdownloads aus Gradle mit403 blockiert. Die 18 betroffenen Dateien wurden aus exakt den offiziellen URLs derselben festgelegten Versionen mit ihren veröffentlichten SHA1-Prüfsummen geladen. Keine Repository-, Versions- oder Produktänderung. Alte Diagnose-/Buildlogs bleiben erhalten.

Offizielles `apksigner` prüft Signatur und Inhalte; zusätzlich unabhängige APK-v2-Signatur-/Digestprüfung bestanden. Manifest/Version/Testkennung, ARM64 und die tatsächlich im Dex enthaltene Room-Identität geprüft. Compilerexport19 bleibt bytegleich; kein neuer oder geänderter Produktblob. Die vorher dokumentierte lokale Kandidaten-APK-SHA ist ein historischer Buildbeleg. Maßgeblich für die jetzt ausgegebene, neu erzeugte APK ist die oben genannte aktuelle SHA.

ZIP enthält genau APK, `ANLEITUNG.md`, `BUILD_INFO.txt`, `APK_VERIFICATION.json`, `CI_VERIFICATION.json`, `PRODUCT_BLOBS.txt` und `SHA256SUMS.txt`. CRC, genaue Dateiliste, unveränderte APK-Bytes innerhalb der ZIP und alle Prüfsummen geprüft. ZIP erst nach erfolgreicher Gesamt-CI erzeugt und gespeichert.

## Kurzer Hardwaretest

1. ZIP entpacken und die APK als Update über vorhandene40121 installieren. Nicht deinstallieren oder App-Daten löschen; alte Imports müssen erhalten bleiben.
2. Dieselbe eigene Karte lesen. Neuen gemeinsamen Vorgang öffnen, Gen1 und Gen2/Gen2V2 einzeln wählen und passende Kartenangaben/Kapazitäten/Zertifikate sowie gemeinsame MF-Details prüfen. Vorhandene11-/3Byte-Kommunikationsangaben erhalten; keine Addition beider Zeitsätze.
3. Karte und Leser entfernen, App schließen und erneut öffnen. Denselben Vorgang mit beiden gespeicherten Anwendungen und MF-Details ohne Leser erneut öffnen.
4. Alten Ein-Anwendungs-Import unverändert öffnen und dieselbe Karte nochmals einstecken; frühere Anwendungsbelege bleiben exakt wiederöffnbar.

[Ausführliche Geräteanleitung](CARD_MULTI_APPLICATION_DEVICE_TEST.md). Keine Fremdkarte, erzwungene Authentisierung oder privaten Kartendaten erforderlich. Rückmeldung mit installierter40122, beiden Anwendungszuständen, alten Daten und Wiederöffnung ohne Leser genügt.

**Bis Tobis ausdrücklicher Bestätigung dieses unveränderten Teststands kein Merge/Auto-Merge.** PR65 und Dokumentationsbranches mit dessen unbestätigtem Code als Vorfahr bleiben offen.40121-Freigabe gilt nicht für40122. Main bleibt unverändert. [Verbleibende vorbereitete Importpunkte](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md): verifiziertes7F66-Primärformat, echte Signatur-/Bytebindung beim Standarddownload, ERCA/MSCA/Kartenvertrauen und legitimer SC5-Pfad für0540. Keine neue Produktfunktion nach dem Testkopf, keine Browser/Unteragenten/Bereinigung oder Fahrzeug-/Spesen-/Abrechnungsarbeit.
