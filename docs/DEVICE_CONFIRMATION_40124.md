# Gerätebestätigung 40124 - eigene Kernsignaturen

**BESTÄTIGT**, Tobi am 13.10.2026, 13:57 Europe/Berlin. [Produkt-PR #73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73), VersionCode 40124, Room-Schema 20. Die vorherige 40123-Bestätigung wurde nicht auf diesen Stand übertragen; es gab einen eigenen vollständigen CI-Lauf, ein exaktes Paket und eine eigene Checkliste.

## Exakter geprüfter Stand

| Beleg | Wert |
|---|---|
| Produktkopf | `a2e22d882bb707fe1a446c3a24f78387caa892b0` |
| Vollständiger Tree | `3540b26b775d17c9bdc6335549dafbfbd58bcd92` |
| Eigene vollständige CI | [38381962283](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38381962283), Versuch 1, sämtliche vier Jobs erfolgreich |
| Gerätepaket | [38388192092](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38388192092), erfolgreich, exakt gleicher Produktkopf |
| Paketinfrastruktur | `2a0b1face3fd8d08409415e36acb5e27bc28d583`, `include_baseline=false` |
| Produktmerge | `3298b707525a4f33162c835ce31deaf181d2e772`, exakt identischer vollständiger Tree |

Build, alle sieben vorgeschriebenen Gradle-Schritte, Unit-Tests, Lint, APK-/Signatur-/Updatepaar-/Room-Schemaexportprüfung, vollständige SQLCipher-/Compose-Suiten auf API 36 und Abschlussgate bestanden. Migration 19→20 bleibt vollständig geprüft; zusätzlich 18→20. Keine abgeschwächten Assertions, Testfilter oder Quarantäne. Paketlog bestätigt `SOURCE_SHA`, eigenen erfolgreichen CI-Lauf, Produktcheckout, Updatepaar und ZIP-Upload.

## Rückmeldung und sichtbarer Beleg

Tobi: „Hab alle Punkte abgearbeitet, und es funktioniert alles so wie es soll“.

Damit ist die [eigene Checkliste](DEVICE_TEST_40124.md) vollständig bestätigt: Update/Altbestand, plausibler bekannter Tag, neuer automatischer Import, vier Signaturanzeigen und Originaldateien, Wiederöffnung ohne Leser, historische Anwendungen/Sitzungen und Altarchiv soweit vorhanden, erneute Aufnahme ohne doppelte Tätigkeiten sowie Kartenentnahme während eines neuen Imports und vollständige erfolgreiche Wiederaufnahme.

Die zwei beigefügten Screenshots `1000017909.jpg` und `1000017907.jpg` zeigen 13:56 als Gerätezeit. Nur die nicht personenbezogenen Generationen/Größen/Statuswerte werden hier festgehalten:

| Anwendung | Datei | Original | Signatur | Status |
|---|---|---:|---:|---|
| GEN1 | Tätigkeiten / EF0504 | 13780 Bytes | 128 Bytes | aufgenommen · ungeprüft |
| GEN1 | Fahrzeuge / EF0505 | 6202 Bytes | 128 Bytes | aufgenommen · ungeprüft |
| GEN2V2 | Tätigkeiten / EF0504 | 13780 Bytes | 64 Bytes | aufgenommen · ungeprüft |
| GEN2V2 | Fahrzeuge / EF0505 | 9602 Bytes | 64 Bytes | aufgenommen · ungeprüft |

Geräteumfang: eigenes Android-Gerät, eigene Fahrerkarte mit GEN1- und GEN2V2-Anwendung, bisher verwendeter USB-Kartenleser. Genaue Modellbezeichnungen wurden in dieser Rückmeldung nicht erneut genannt; keine Modellannahme aus App-/Kontometadaten.

## Reichweite

Das Gerätegate gilt für diesen konkret getesteten Produktbaum und Umfang. „Aufgenommen“ bleibt „ungeprüft“; keine mathematische Gültigkeit, ERCA/MSCA-Vertrauenskette, vollständige Kartenabdeckung, DDD-/Standarddownload oder LastCardDownload-Schreibfreigabe. Andere Geräte/Leser, reine GEN2-Karten, sämtliche sechs Kurven oder jede seltene APDU-Abbruchphase wurden dadurch nicht hardwaregeprüft. Weitere relevante Produktänderungen brauchen eigene passende CI und erforderliche Gerätebestätigung.
