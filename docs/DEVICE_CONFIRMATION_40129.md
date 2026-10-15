# Gerätebestätigung und ausdrückliche M1-Abnahme40129

Stand15.10.2026. Gehört zu [PR89](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/89), [Implementierung](VIOLATION_ENGINE_M1_IMPLEMENTATION.md) und [Abnahmematrix](VIOLATION_ENGINE_M1_ACCEPTANCE.md).

## Identifiziertes Testpaket

- Quellkopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`;40129/Room23.
- Vollständige CI [38729242320](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38729242320), Versuch1, erfolgreich.559 Unit-Tests und vollständige API36-SQLCipher-/Compose-Instrumentierung, alle sieben Gradle-/APK-/Signatur-/Updatepaar-/Schemagates. Keine Filter/Quarantäne.
- APK-SHA256 `f5f7873e88149515829c406ce04f20dc83cb56ac478497f05cd5c077c65bc996`.
- ZIP-SHA256 `42f428cc08c7377fda7369fbbbb79d484f4ff1376cd05f2288bd9d44b0a71b95`.
- Testzertifikat-SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`.
- Lokal isolierter Build des identischen geprüften Quellkopfes; kein Actions-Download und kein Downgrade-/Löschpaket. Die vollständige achtteilige Anleitung wurde direkt im Chat bereitgestellt.

## Tatsächliche Antwort und Umfang

Tobi antwortete am15.10.2026 um15:13 Europe/Berlin: **„Passt alles 👌🏻“**. Positive Gesamtbestätigung des angeleiteten Umfangs: Update der bestehenden Testinstallation, vorhandenes Profil/Importhistorie, ältere Original-/Signaturansichten und vorhandene Mathematik, regulärer eigener Import/Replay und Offline-Wiederöffnung. Keine gesonderten Einzelmesswerte, konkreten zusätzlichen Hardware-/Karten-/Generations-/EF-/Kurvennachweise daraus ableiten. Zertifikatsvertrauen bleibt NOT_EVALUATED. Das PR-Protokoll wurde vor der ausdrücklichen Abnahme bereits um genau diese Geräteantwort ergänzt.

Am15.10.2026 um15:36 Europe/Berlin hat Tobi zusätzlich ausdrücklich **M1 im bisher freigegebenen Funktionsumfang abgenommen** und kontrollierte Integration von PR88 zuerst und PR89 danach genehmigt, unter erneuter Prüfung aller Pflichtgates. Anschließend M2-Implementierung genehmigt; M2-Merge braucht gesonderte Freigabe, M3–M7/Release nicht genehmigt.

Die Integration von Planung/M0 ändert keinen Produktcode gegenüber diesem Paket. Produktgleichheit und erneute vollständige CI des abgestimmten PR89-Kopfes sind vor dessen Merge nachzuweisen. Neue Integration ist keine neue zusätzliche Gerätemessung. Der dokumentierte verlorene unvollständige Settings-Entwurf bleibt nicht wiederhergestellt.
