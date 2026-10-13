> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); verbindlich sind [CURRENT_RULES.md](CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Gerätebestätigung 40120 und Abschluss PR59/PR60

Tobi hat am **11.10.2026, 22:22 Europe/Berlin** den bereitgestellten gemeinsamen Gerätetest40120 ausdrücklich bestanden gemeldet und den Abschluss von PR59/PR60 beauftragt.

## Tatsächlich bestätigter Umfang

| Prüfung | Nutzerrückmeldung |
|---|---|
| Update | Update installiert, alte Daten vorhanden |
| Neue Kartenlesung | Erfolgreich |
| Neue Ansichten | Zertifikate, Kartenangaben und Anwendungsverzeichnis sichtbar |
| Persistenz | Nach Kartenentnahme und App-Neustart weiterhin vorhanden |
| Historischer Import | Alter Import unverändert |

Diese Bestätigung betrifft die gelieferten Funktionen im gemeinsamen Stand40120. Sie enthält keine zusätzlichen Einzelmesswerte, keine ungenannten Fremdkarten-/Fehlerfälle, keinen Kryptovertrauensnachweis und keine Freigabe anderer Hardware.

## Eindeutige Zuordnung

- Repository `Perrystack9/Fahrerabrechnung_Android_0.4.1`.
- Paket `FahrerTagebuch-PR60-Test-Update.zip`, Version40120, Schema18, Anwendung `de.fahrerabrechnung.app.devicetest`.
- Quellcommit `85d0614235e4d9b5384d96d9ee124aab1f9fbc3a`, Tree `78155b21cb76f9c27231360bd78dad0aa5472a22`.
- ZIP SHA256 `77a66af59fb994892e852084f44bdedd857ba981aa4beef24c65765352e2b23a`; APK SHA256 `e287466c84f4bc38ecc1dc88971920eab30f11ff761cf8a9d9d179b10be81d82`.
- Feste Testsignatur, Zertifikat-SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`.
- Vollständige [Android-CI38145473572](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38145473572) am exakten Quellkopf: alle vier Jobs erneut erfolgreich geprüft.
- [Paketlauf38148088141](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38148088141) am selben Kopf erfolgreich. Bestehende lokale APK-v2-Signatur-/Inhaltsdigest-/Prüfsummenprüfung gehört zu diesem gelieferten Paket.

## Durchgeführter Abschluss

[PR60](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/60) wurde unverändert mit erwartetem Kopf85d0614 über einen Merge-Commit übernommen: **`9e7670f05b7c35a585244aeca539be9d0fe7c6fb`**. Dessen Eltern sind der vorherige Main `1b592d627d5d18db9fc909f5c8cf2c796fd8578b` und der bestätigte Quellkopf85d0614. Der Merge-Tree ist bytegleich zum oben bezeichneten geprüften und getesteten Produktbaum.

[PR59](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/59), Zertifikatskopf `c535c01a838fb9f565b49b7a9b50030d06e77bf7`, ist nachweislich Vorfahr von PR60 und des neuen Main; GitHub hat ihn nach dem kumulativen Merge automatisch ebenfalls als gemergt abgeschlossen. Kein zweiter Produktmerge nötig. PR59-CI38119878793 war ebenfalls vollständig erfolgreich. Branches, alte Dateien und ausgegebenes Paket wurden erhalten.

Die nachfolgende Dokumentation ändert ausschließlich Markdown-Dateien; der bestätigte Produktcode, Schema18 und Testversion40120 bleiben unverändert. Die letzte Gerätebasis ist jetzt **PR60/40120**, nicht mehr PR58/40117.

## Grenze für die Fortsetzung

**PR62 und spätere Änderungen gelten durch diesen Test ausdrücklich nicht als hardwarebestätigt.** PR62 enthält zusätzliche MF-Kommunikationsquellen und Koordinator-Snapshots, Entwurfsversion40121/Schema18. Es bleibt ein offener Entwurf ohne Merge/Auto-Merge bis zu einer eigenen ausdrücklichen Gerätebestätigung. Grüne CI ersetzt diese Rückmeldung nicht.

Selbstständige Weiterarbeit am Kartenimport ist beauftragt. Produktregeln gelten weiter; kein Browser, keine Unteragenten und keine Bereinigung/Löschung. GitHub-Actions-Budget unverändert2USD; keine Budgeteinstellungen geändert.
