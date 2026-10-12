# PR62 – Testupdate40121, Auslieferung12.10.2026

## Bereitgestelltes Testpaket

`FahrerTagebuch-PR62-Test-Update.zip`, Version40121, Schema18, ausschließlich Update-APK, Anleitung, BUILD_INFO.txt und SHA256SUMS.txt. Als Update über die bestätigte40120-Test-App installieren; nicht deinstallieren und keine Daten löschen. APK und Quellkopf unverändert gegenüber der vollständigen grünen PR62-CI.

| Nachweis | Exakter Wert |
|---|---|
| Produktcommit | `efbf49cdf92bd5e5ddb2e042b3e14e9372169756` |
| Produkttree | `11e79e492b381f9d515bee796ffea30da3099592` |
| Vollständige CI | [38172596871](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38172596871), alle vier Jobs erfolgreich |
| Paketlauf | [38201601458](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38201601458), Versuch1 erfolgreich |
| Paket-Workflowcommit | `0cabe92b5e0d9d06769c24811a14d97b33bbb7b4` |
| Paket-Branch | `build/pr62-device-update-40121`, separat, nicht in Main/PR62 aufgenommen |
| Paketkennung | `de.fahrerabrechnung.app.devicetest` |
| Version / bestätigte Basis |40121 /40120 |
| Zertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Gelieferte ZIP SHA256 | `a12529904cc08b0bf06f90fb7a453a6c6db4bd833fb83bbd700f2bb5b52a1302` |
| Gelieferte ZIP Größe |25398823Bytes |
| APK SHA256 | `d4dafa0a8860830bd553585c03d04426b3f1ccee89b3f42ccafc17af4a8d096a` |
| Anleitung SHA256 | `fd2771d15b97377cce940ba1cc70cfb728e8d955916ad721a6c27c188df578b0` |
| Originale CI-ZIP SHA256 | `353be25617a193f052e295eed9ab687491e8d6d04139760d438ebd0c380c1cbf` |
| Actions-Artifact |11726077253, SHA256 `08a4138df5176d0a665736417c58a2ee50ab847c011d1865256c209ebdc52bdb` |

Der separate Paketbranch ergänzt ausschließlich einen auf diesen Branch begrenzten Start des vorhandenen Paketworkflows mit festem geprüften Produktcommit. Der Produktcheckout bleibt separat und unverändert; der Workflow fordert eine vollständige grüne CI genau für diesen Commit. Budgeteinstellungen, Main und PR62-Kopf wurden nicht verändert. Kein neuer vollständiger Android-/Emulatorlauf für denselben Produktkopf ausgelöst.

CI bestätigt stabiles Testzertifikat, Paketkennung, steigendes Updatepaar40120→40121 und unveränderte generierte Room-Exporte. Lokal zusätzlich geprüft: Actions-Artifactdigest, originale CI-ZIP-Prüfsumme, enthaltene BUILD_INFO-Quelle/Workflow/Run/Versuch, keine Basis-APK, APK-v2-Signatur(RSA/SHA256), vollständiger APK-Inhaltsdigest, festes Zertifikat, Paketkennung und Version40121.

Die gelieferte ZIP aktualisiert ausschließlich die Anleitung auf den tatsächlichen Auslieferungsstand und ergänzt Prüfsummen für Anleitung/BUILD_INFO. Die signierte APK ist bytegleich zur CI-APK; deshalb unterschiedliche originale und gelieferte ZIP-Hashes. Sämtliche enthaltenen Prüfsummen und ZIP-CRC geprüft. Die ZIP ist dauerhaft bereitgestellt; lokale Prüfdetails bleiben im Auslieferungscheckpoint erhalten.

## Gerätefreigabe bleibt offen

**40121 ist noch nicht hardwarebestätigt. PR62 bleibt offener Entwurf, kein Merge/Auto-Merge.**40120 ist unverändert bestätigt und PR59/60 sind abgeschlossen. [Konkrete Geräteanleitung](CARD_COMMUNICATION_DEVICE_TEST.md).

Tobi soll Update/Altdaten, neue eigene Kartenlesung, Kommunikationsangaben, Wiederöffnung ohne Leser und unveränderten alten Import prüfen. Optionaldateien werden nur bei eindeutiger ATR-Ankündigung versucht; fehlende optionale Angaben sind kein belegter Positivfall und keine Vollständigkeitsbehauptung. Rückmeldung mit Version40121 und tatsächlich beobachtetem Umfang festhalten.

## Beauftragte Fortsetzung nach erfolgreicher Bestätigung

Tobi hat am12.10.2026 ausdrücklich beauftragt, nach erfolgreichem Hardwaretest die restlichen angekündigten Kartenimport-Punkte selbstständig abzuarbeiten. Zuerst die ausdrückliche40121-Gerätebestätigung im PR-/Repository-Checkpoint dokumentieren, aktuellen PR62-Kopf und CI erneut prüfen und genau den unveränderten freigegebenen Produktkopf abschließen. Niemals aus der alten40120-Freigabe oder einer bloßen CI-Freigabe auf40121 schließen.

Eine ereignisgesteuerte automatische Fortsetzung ist für den **Merge von PR62** eingerichtet. Sie prüft zusätzlich die dokumentierte erfolgreiche40121-Gerätebestätigung am oben bezeichneten Produktkopf und arbeitet ohne diese Bestätigung nicht. Kein Zeitplan/regelmäßiger Polling-Build, kein Start vor dem Merge; keine parallele doppelte Fortsetzung. Bei abweichendem Testkopf oder einer neuen Version die bestehende Fortsetzung entsprechend aktualisieren, keine zweite Aufgabe anlegen.

Reihenfolge: erst Anwendungs-/Sitzungsmodell und getrennten Import beider Anwendungen derselben Gen2-Karte; anschließend vollständige7F66-Inhaltsauswertung und belegte Verfahren für signierten Standarddownload/DDD, Zertifikats-/Signaturvertrauen und geschützte Kartenbereiche. [Bisherige Voranalyse und offene Grenzen](CARD_IMPORT_FINALIZATION_HANDOFF.md). Keine erfundenen Schlüssel/Authentisierung/Signaturen/Zeiten, keine blinde0540-Lesung und keine vorgezogene Fahrzeug-/Spesen-/Abrechnungsarbeit.

Neue Änderungen erhalten eigene vollständige CI und eigene ausdrückliche Gerätefreigaben; spätere Import-/Schemaentwürfe bis dahin ungemergt. Bei einem unverzichtbaren neuen Hardwareprüfpunkt konkretes geprüftes Paket und dokumentierten Stand hinterlassen. Regeln unverändert: kein Browser, keine Unteragenten, keine Bereinigung/Löschung; Actions-Budget2USD und Stop-usage-Regel nicht ändern.

Dieser Auslieferungscheckpoint enthält den unbestätigten PR62-Code als Vorfahren. Vor der40121-Gerätefreigabe nicht als angeblich reines Docs-Update nach Main mergen. Produktkopfefbf49c bleibt exakt am grünen CI-Stand; die Auslieferungsdokumentation wird auf `docs/pr62-delivery-40121-20261012` gesichert.
