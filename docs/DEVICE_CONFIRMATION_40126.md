# Gerätebestätigung 40126 — Ereignis-/Störungssignaturen

## Exakter Produkt- und Paketstand

| Beleg | Tatsächlich geprüftes Ergebnis |
|---|---|
| Produkt | 40126 / Room-Schema 22, PR #79 |
| CI-/Gerätequellkopf | `e4b70aec0b555c340e45df193c4f3b93bccc8770` |
| Vollständige Produkt-CI | [38444430958](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38444430958), Versuch 1; alle vier Jobs erfolgreich |
| Gerätepaket | [38448369846](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38448369846), Versuch 1; erfolgreich für denselben Produktkopf |
| Paketinfrastruktur | `3c90e97cd598afb7f8b66344dc049b195898cd31` auf main; `include_baseline=false` |
| Installierbare APK | `de.fahrerabrechnung.app.devicetest`, VersionCode 40126, `0.4.1-ui-v2-device-test` |
| Testzertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Produktmerge | `6334b4f67485feaee4de5d649d6788475af940ec`; Eltern sind damaliger main und der geprüfte Quellkopf |
| Vollständiger identischer Baum | `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e` für Produktmerge und CI-/Gerätekopf |

Alle sieben vorgeschriebenen Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemaexport, vollständige SQLCipher-/Compose-Suiten auf API 36 und Abschlussgate erfolgreich. Eigene 19→20- und 20→21-Migrationsabnahmen vollständig erhalten, zusätzlich additive 21→22 und historische verschlüsselte Upgrades bis 22. Keine Filter, Quarantäne oder CI-/Upload-/Budgetänderung; erfolgreicher normaler Lauf ohne Artefakte.

Room22 stammt ausschließlich aus echtem Compilerexport, SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`; alle 44 bisherigen Tabellen und Exporte 1–21 unverändert.

## Paketintegrität

Nach Download tatsächlich verglichen: GitHub-Archivdigest, innerer ZIP-Hash, APK-Hash, BUILD_INFO, APK-Manifest und enthaltenes Testzertifikat. Die unveränderte CI hat beide APK-Signaturen und das Installations-Updatepaar mit apksigner/aapt2 verifiziert.

| Datei | SHA256 |
|---|---|
| GitHub-Artefaktarchiv, ID 11839402477 | `70fc4284b44bd6c18579474a445bf980bcdacfb3702d31b04091f2d1cff15378` |
| Inneres Test-Update-ZIP | `2b943f49cefabc9fb8bd138582dc7af6659746e7f7db04e642fa4cc140403917` |
| Test-Update-APK | `49b2424a16af3fb062640e59f0ef533aa2cc55e5ea19c4c4ae580d0f7d5dde24` |

BUILD_INFO: Produktkopf wie oben, Infrastrukturkopf wie oben, run_id 38448369846, run_attempt 1, baseline_included=false. Inneres ZIP enthält ausschließlich Update-APK, ANLEITUNG.md, BUILD_INFO.txt und SHA256SUMS.txt. Die ausgegebene einzelne APK entspricht exakt der APK im ZIP. Keine neue Produktausgabe nach der Gerätebestätigung.

## Neue eigene Rückmeldung

Tobi meldet am **13.10.2026 20:59 Europe/Berlin** nach der verständlichen neunteiligen Anleitung direkt im Chat: **„Hab alles getestet.“** Keine Auffälligkeit gemeldet. Als positive Gesamtrückmeldung zum bereitgestellten 40126-Paket und zu den tatsächlich eigenen Testpunkten übernommen.

Die Chat-Anleitung umfasste Update über 40125 ohne Datenlöschung, alte Übernahme, neuen Import mit beiden angebotenen Anwendungen, Ereignis-/Störungsstatus, erhaltene andere Daten, Auswahlwechsel/Maskierung, Reopen ohne Leser, erneuten regulären Import mit unveränderter Historie und zusätzlichen Kartenentnahme-/Abbruchtest. Tobi bestätigt diese Punkte gesammelt; kein separates Ablaufprotokoll oder Fehlerfall-Messwert wurde genannt.

Der Screenshot im Chat (`1000018013.jpg`, Anzeigezeit 20:58) zeigt:

| Bereich | Originalgröße | Signaturlänge | Status / fachliche Darstellung |
|---|---:|---:|---|
| Ereignisse | 3168 Bytes | 64 Bytes | aufgenommen · ungeprüft; ein Ereignis sichtbar |
| Störungen | 1152 Bytes | 64 Bytes | aufgenommen · ungeprüft; keine Störungen aufgezeichnet |

Die ausgewählte Anwendungsbezeichnung und die installierte Versionsanzeige sind im sichtbaren Screenshot nicht enthalten. Den Screenshot deshalb keiner zusätzlich behaupteten Generation zuordnen. Weitere einzelne Größen/Längen, GEN1-Werte, C101-Kurven, Leser-/Karten-/Telefonmodellnamen oder Abbruchdetails wurden nicht separat geliefert. Keine alten 40124-/40125-Werte als neue 40126-Messung übernehmen.

Das Gerätegate gilt für diesen exakten bestätigten Produktbaum und Tobis tatsächlich getestete Karte-/Leser-Kombination. Aufnahme bleibt UNVERIFIED; keine mathematische Signatur-, ERCA/MSCA-Vertrauens-, universelle Hardware-/Kurven-, vollständige Karten-/DDD- oder Schreibfreigabe. Die bisherigen Bestätigungen für 40124 und 40125 bleiben in ihrem eigenen Umfang erhalten.

## Abschluss

PR #79 nach Prüfung des exakten unveränderten Kopfes, aller grünen Checks, Mergefähigkeit und neuer Geräterückmeldung normal gemergt. Zusätzlicher [Produkt-Main-Lauf 38451502155](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38451502155), Versuch 1, vollständig erfolgreich für Produktmerge: alle vier Jobs einschließlich vollständiger SQLCipher-/Compose-Suiten und Abschlussgate. Live vor Sitzungsabschluss geprüft. Reine Abnahmedokumentation in [PR #80](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/80).

Gerätetest-Anleitungen künftig immer verständlich direkt im Chat. Die [Repository-Checkliste](DEVICE_TEST_40126.md) und ZIP-Anleitung ergänzen diese Kommunikation.
