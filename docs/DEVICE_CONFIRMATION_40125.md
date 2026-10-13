# Gerätebestätigung 40125 - neue Identifikationssignaturen

**BESTÄTIGT im tatsächlich getesteten eigenen Umfang.** Tobi am 13.10.2026 um 17:32 Europe/Berlin zum bereitgestellten 40125-Paket: „Wurde getestet, und funktioniert 1a“. [Produkt-PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76) nach dieser neuen positiven Gerätebestätigung übernommen. 40124 wurde nur als Updatebasis verwendet.

## Exakter geprüfter Stand

| Beleg | Wert |
|---|---|
| Produktkopf | `20d34a0fc7ce370fb4c1d22002905a500574ee06` |
| Vollständiger Produktbaum | `36e014af6cbffa9add766bdd917c6d94313f87ec` |
| Vollständige Produkt-CI | [38408152663](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38408152663), alle vier Jobs erfolgreich |
| Gerätepaket | [38414059875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38414059875), Versuch 1, erfolgreich für genau diesen Produktkopf |
| Paketinfrastruktur | `07db91f33957a45598cae6e4772787d1aa7ddbf4`, `include_baseline=false` |
| Manifest der geprüften APK | `de.fahrerabrechnung.app.devicetest`, VersionCode 40125, VersionName `0.4.1-ui-v2-device-test` |
| Produktmerge | `ae00f9699fe7ef7ab3082d9a1164b8cca185561d`, identischer vollständiger Produktbaum |
| Zusätzlicher Produkt-Main-Lauf | [38423233063](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38423233063), Status stets live prüfen |

Alle sieben Gradle-Aufrufe, Unit/Lint/APK-/Testsignatur-/Updatepaar-/Compiler-Schemaabgleich, vollständige SQLCipher-/Compose-Suiten auf API36 und Abschlussgate bestanden. Eigenständige vollständige 19→20-Abnahme erhalten; verschlüsselte 18-/19-/20→21-Pfade ebenfalls geprüft. Keine abgeschwächten Assertions, Filter, Quarantäne oder Workflowänderung. Normale erfolgreiche CI ohne Artefakte.

Das bereitgestellte Archiv wurde tatsächlich heruntergeladen und geprüft: GitHub-Artefakt-Digest, ZIP-/APK-Prüfsummen, BUILD_INFO, Manifest und aktuelle 40125-ANLEITUNG stimmen. Nur Update-APK und Belege enthalten, keine Baseline-APK.

| Prüfsumme | SHA256 |
|---|---|
| Paket-ZIP | `f03a0f71194102b7a2c306aebb29e4cad5b0b957649de252c6e4a98b527446ad` |
| Update-APK | `a3564a7db33dc7a8f2a559eb3171a0555a3b0ec37200d38f0ac8e8b68e0baa0f` |
| GitHub-Artefakt | `d45862e64243ea8b702f081a8581852239ebd788cf80f697bff238d33bc61bc3` |
| Compiler-Room21 | `8cd199f6012938fda80f8f8f265b49224b7fd8c127d2ad913b7926784eafa8c9` |

## Rückmeldung und Reichweite

Die positive Rückmeldung bezieht sich im Gespräch auf das bereitgestellte neue Paket und die [40125-Abnahmecheckliste](DEVICE_TEST_40125.md). Der Plan umfasst Update/Altbestand, fünf mögliche neue Identitätssignaturen neben vier Core-Belegen, Identitätskonsistenz, Wiederöffnung/Quellenwechsel, Wiederholung und Kartenentnahme/Neustart. Tobi berichtet das erfolgreiche Gesamtergebnis; einzelne Schritte, Größen/Signaturlängen, Screenshots oder konkrete Hardwaremodellnamen wurden diesmal nicht separat protokolliert. Keine neuen Messwerte oder pauschale Bestätigung jeder Einzel-/Sonderkonstellation daraus erfinden.

Das Gerätegate ist für diesen konkret bestätigten Produktbaum im tatsächlich getesteten eigenen Umfang erfüllt. Aufnahme bleibt ungeprüft: keine mathematische Signaturgültigkeit, ERCA/MSCA-Vertrauenskette, vollständige Kartenabdeckung, DDD-/Standarddownload- oder Schreibfreigabe. Reine GEN2-Karten, alle Kurven, weitere Hardware oder jede seltene APDU-Abbruchphase nicht aus der Gesamtrückmeldung ableiten. Neue relevante Produktänderungen brauchen eigene passende CI und erforderliche Geräteabnahme.
