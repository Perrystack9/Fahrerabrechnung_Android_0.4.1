# Gerätebestätigung 40127 — mathematische 0504/0505-Verifikation

## Exakter Produkt- und Paketstand

| Beleg | Tatsächlich geprüftes Ergebnis |
|---|---|
| Produkt | 40127 / unverändertes Room-Schema22, [PR #82](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/82) |
| CI-/Gerätequellkopf | `f1fd5ac0e7e488c71daa48f69757bfd5904e0ecc` |
| Vollständige Produkt-CI | [38515830418](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38515830418), Versuch1; alle vier Jobs erfolgreich |
| Gerätepaket | [38530132281](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38530132281), Versuch1; erfolgreich für denselben Produktkopf |
| Paketinfrastruktur | `8cadcdc0ff1ad8e18496bf2c07fd54bcd5a61da0` auf main; include_baseline=false |
| Installierbare APK | `de.fahrerabrechnung.app.devicetest`, Manifest-VersionCode40127 |
| Testzertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Produktmerge | `96370d0ef291af1ac3893333cd128c2a3f735033`; Eltern: damaliger main und geprüfter Quellkopf |
| Vollständiger identischer Baum | `f818dcb8b1da48cdad7cdc555bcc7638c478b819` für Produktmerge und CI-/Gerätekopf |

Alle sieben vorgeschriebenen Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Compiler-Schemavergleich, ungekürzte SQLCipher-/Compose-Suiten auf API36 und Abschlussgate erfolgreich. Neue mathematische Positiv-/Negativ-/Provenienz-/Zeilengleichheits-/Reopen-/Auswahltests in realem Android-Kontext bestanden. Alle früheren Sicherheits-/Reader-/SQLCipher-/Migrations-/Maskierungs-/Gruppenprüfungen erhalten; keine Filter/Quarantäne, Workflow-/Upload-/Budgetänderung. Erfolgreiche normale CI ohne Artefakte. [Technische Belege und unabhängige Testvektoren](CARD_CORE_SIGNATURE_MATH_EVIDENCE.md).

Room22 und echte Compilerexporte unverändert, SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. Keine neue Migration, DB-Schreiboperation, Cache oder Backfill; alte Original-/Request-/Receipt-/Certificate-Fingerprints und CAPTURED_UNVERIFIED unverändert.

## Paketintegrität

Nach Download tatsächlich geprüft: GitHub-Archivdigest, innerer ZIP-Hash, APK-Hash, BUILD_INFO, APK-Manifest und festes Testzertifikat. APK-v2-Signatur samt Inhaltsdigest zusätzlich kryptografisch verifiziert. Die bestehende CI hat die beiden Updatepaar-APKs mit apksigner/aapt2 geprüft.

| Datei | SHA256 |
|---|---|
| GitHub-Artefaktarchiv, ID11878101353 | `4d8580b53dba6944b0d5af8c213bf9a7f5c574606b6ca8cea9471edf12e11a4d` |
| Inneres Test-Update-ZIP | `b5f130115a8825ef4f48fa5d22c0d43fe296c3edfb0cdf73a4f8fe969348443f` |
| Test-Update-APK | `7a23ae2beeeb143bebe7c0067500f95fbe966d4a45a6bfe5f2e343d9d01e466b` |

BUILD_INFO: product_commit wie oben, workflow_commit wie oben, run_id38530132281, run_attempt1, baseline_included=false. Inneres ZIP enthält ausschließlich Update-APK, ANLEITUNG.md, BUILD_INFO.txt und SHA256SUMS.txt. Die ausgegebene einzelne APK entspricht exakt der APK im ZIP. Nach der Gerätebestätigung keine neue Produktausgabe.

## Neue eigene Rückmeldung

Tobi berichtet am **14.10.2026 10:08 Europe/Berlin**: „Bei alten Einträgen steht, sowie du sagtest, nichts dran“, „Einlesen funktioniert ohne probleme, alle daten bleiben bestehen“ und „Punkt 9 funktioniert ebenfalls“. Keine Auffälligkeit oder mathematisch ungültige Signatur gemeldet. Seine Screenshots zeigen beide angebotenen Anwendungen.

Nach Erklärung von „Übernahme“ (einzelner gespeicherter Einlesevorgang) und „Anwendung“ (GEN1/GEN2V2 derselben Karte) sowie den abschließenden vier Chat-Testpunkten bestätigt er am **14.10.2026 10:12 Europe/Berlin**: **„Das passt alles 👍“**. Als neue positive Gesamtantwort zum bereitgestellten40127-Paket und zur tatsächlich eigenen Testkombination übernommen. Versionsbestätigung ist Teil dieser Gesamtantwort; eine sichtbare Versionsanzeige wurde nicht geliefert.

Die verständliche neunteilige Anleitung wurde direkt im Chat gegeben: Update über40126 ohne Datenlöschung, zuerst gespeicherte signierte Übernahme ohne Leser, beide angebotenen Anwendungen und beide EFs getrennt, fehlende alte Signaturen, Wechsel/Maskierung, Reopen ohne Leser, erhaltene übrige Ansichten, normaler neuer Import mit unveränderter Historie und zusätzlicher Unterbrechungs-/Neuimportfall. Die abschließende vierteilige Erklärung konkretisierte Auswahl alter/neuer Übernahme und GEN1/GEN2V2, App-Neustart ohne Leser, übrige Ansichten und installierte40127. Keine separate Zeit-/Sitzungs-/Fehlerfallmessung genannt; vorhandenes Altarchiv war ein bedingter Fall und wurde nicht separat bestätigt.

Die Screenshots im Chat (`1000018268.jpg` GEN1 und `1000018270.jpg` GEN2V2, Anzeigezeit10:03) zeigen:

| Anwendung | Datei | Originalgröße | Signaturlänge | Mathematische Prüfung | Zertifikatsvertrauen |
|---|---|---:|---:|---|---|
| GEN1 | Tätigkeiten0504 | 13780 Bytes | 128 Bytes | mathematisch gültig | nicht geprüft |
| GEN1 | Fahrzeuge0505 | 6202 Bytes | 128 Bytes | mathematisch gültig | nicht geprüft |
| GEN2V2 | Tätigkeiten0504 | 13780 Bytes | 64 Bytes | mathematisch gültig | nicht geprüft |
| GEN2V2 | Fahrzeuge0505 | 9602 Bytes | 64 Bytes | mathematisch gültig | nicht geprüft |

Die konkrete Importzeit und installierte Versionsanzeige sind auf den Screenshots nicht sichtbar. Kein zusätzliches Karten-/Leser-/Telefonmodell, keine bestimmte C101-Kurve, GEN2 ohne V2, allgemeine Kurven-/Hardwareabdeckung oder vollständige Altarchivabnahme ableiten. CI deckt alle sechs festen EC-Domänen ab; Hardwarebeleg bestätigt ausschließlich die tatsächlich getestete Kombination und den exakten40127-Baum.

Mathematische Gültigkeit und Zertifikatsvertrauen bleiben getrennt. Originaler Aufnahmezustand bleibt CAPTURED_UNVERIFIED. Andere EF-Signaturen bleiben unverifiziert. Keine ERCA/MSCA-Vertrauens-, vollständige Karten-/DDD-/Export- oder Schreibfreigabe. Bestätigungen für40124/40125/40126 bleiben in ihrem eigenen Umfang erhalten.

## Abschluss

PR #82 nach Prüfung des unveränderten endgültigen Kopfes, aller vier grünen CI-Jobs, erfolgreichem exaktem Paket, Mergefähigkeit und neuer eigener positiver Geräterückmeldung normal gemergt. Produktmerge und CI-/Gerätekopf haben den vollständigen identischen Baum. Zusätzlicher Produkt-Main-Lauf [38533736352](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38533736352), Versuch1, für `96370d0ef291af1ac3893333cd128c2a3f735033` jetzt vollständig erfolgreich, alle vier Jobs. Reiner Dokumentationsabschluss [PR #83](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/83) übernommen; Docs-main38534984439 grün. Tatsächlich live beim nächsten Planstart14.10.2026 geprüft.

Geräteanleitungen immer verständlich direkt im Chat; [Repository-Checkliste](DEVICE_TEST_40127.md) und ZIP-Anleitung ergänzen sie. Kein weiterer Produktmeilenstein automatisch freigegeben.
