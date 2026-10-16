# Eigene Gerätebestätigung40130 / Room23

Tobi antwortete am **15.10.2026 19:38 Europe/Berlin** unmittelbar auf das exakt zugeordnete M2-Gerätepaket und die direkt im Chat gegebene Prüfanleitung:

> Funktioniert alles wie es soll 👌🏻

Mit dem anschließenden ausdrücklichen Auftrag bestätigte Tobi die **technische Abnahme des tatsächlich implementierten und nachgewiesenen M2-Umfangs**, genehmigte den kontrollierten PR90-Merge und M3 nach erfolgreicher Integration. Keine uneingeschränkte juristische Bestätigung und keine M3-Geräte-/Mergeabnahme.

## Paketbindung

| Merkmal | Nachweis |
|---|---|
| Produkt |40130 / unverändertes Room23 |
| Exakter Quellkopf | `8fc3fd4d64876450373989952edcbc31feaed0f0` |
| Vollständiger Quellbaum | `8fe032142f9987e8c46b818d802bdf97f25c9ca7` |
| Finale volle PR-CI | [38773986395](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38773986395), Versuch1, alle vier Jobs erfolgreich |
| Paket | `FahrerTagebuch-M2-Test-40130.zip`,28379842 Bytes |
| ZIP-SHA256 | `877d6a9e10fb55a4d5dae8ec7a4661d8fc6ac1633f1aae3e36cf3a2e9881a871` |
| Enthaltene Update-APK-SHA256 | `42d5b9bfd29c4fc48329ceb7ce951bbd0538394ec6b4bf81f7d0846b53a1f698` |
| Paket-ID | `de.fahrerabrechnung.app.devicetest` |
| Fester Testsignierer-SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Erstellung | Lokal isolierter Build aus genau diesem Git-Kopf, deaktivierter Buildcache; keine Behauptung eines Actions-Artefaktdownloads. APK-Version, tatsächliche v2-Signatur, Testsignierer, ZIP-CRC und Prüfsummen vor Bereitstellung kontrolliert. |

Das Paket enthielt ausschließlich das Update und die ergänzenden Nachweise, keine zur Neuinstallation auffordernde Schema18-Baseline. Die Chat-Anleitung verlangte ein Update ohne Deinstallation/Datenlöschung sowie Prüfung bestehender Daten, Import-/Signatur-/Bedienfunktionen im tatsächlich eigenen Umfang. Die Antwort ist eine positive Gesamtbestätigung; sie enthält keine separaten Zeit-, Kurven-, Reader-, Kartengenerations- oder Hashmesswerte auf dem Telefon. Solche Details werden daraus nicht abgeleitet.

PR90 wurde später mit identischem vollständigem Baum integriert. [Merge und neue vollständige main-CI](M2_INTEGRATION_20261015.md). Eine spätere Produktänderung benötigt ihre eigenen Prüfungen; diese Bestätigung wird nicht auf M3 übertragen.
