# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien; Git/main/PR/CI immer live prüfen.

## Laufender Produkt-Meilenstein

**40124 / Schema 20 in offenem Draft-PR [#73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73), noch nicht freigegeben.** Umsetzung vorhanden, nicht erneut beginnen: echte 0504/0505-Sequenz mit Card_Sign-Policy, P2=00, vollständigem READ/defensiver Grenze/SIGN, unsigned Rohaufnahme bei lokalen Signaturfehlern, Gruppenabbruch bei Transportverlust und serialisierte powered Sessions. Sitzungsbelege mit gleicher Original-/Zertifikatsquelle, eigener Receipt-Fingerprint, additive 19→20 und historische Nullmap-Kompatibilität. Kleine gespeicherte Status-/Längen-/Ungeprüft-Anzeige, Quellen-/Altarchiv-Reset.

Planbasis main `2a0b1face3fd8d08409415e36acb5e27bc28d583` (PR #72, Docs-CI 38365176554 erfolgreich). Erster Produktkopf `dc45e0d6c4511b6875cf03fe66c02c5f5fd7f434`, Compilerlauf 38367776503 fehlgeschlagen am Compose-Aufruf; Fehler korrigiert. Echter Room-20-Export aus dessen unverändertem Artefakt 11797652151, SHA256 `34a9253cb546255ce9c12bef2d9808933e1af8a8dca1c27a783eb7322e6ab6e6`, übernommen; kein handgeschriebenes Schema. Lokaler SQL-Abgleich mit Compilerexport bestätigt FKs/Spalten/Indizes, statische Projektgrenze und 10 CI-Guards grün. Unit-/SQLCipher-/Migration-/Gruppenrollback-/Historien-/Compose-Tests ergänzt; endgültige volle CI **live prüfen**. Lokaler Gradle-Download netzbedingt blockiert, Android-Nachweis nur aus echter CI.

## Exakt nächste Arbeit

1. Endgültigen PR-Kopf und volle CI prüfen: alle sieben Gradle-Aufrufe, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexport, komplette API 36-SQLCipher-/Compose-Suiten und Abschlussjob. Tatsächliche Fehler beheben, keine Filter oder Gate-/Workflowänderungen.
2. Erst für den vollständig grünen unveränderten Produktkopf gezieltes Paket über bestehenden Workflow erzeugen: `source_sha` exakt, `include_baseline=false` für vorhandene 40123-Installation. Infrastrukturkopf/Produktkopf unterscheiden. Kein normales Erfolg-Artefaktupload nachrüsten.
3. [Eigene 40124-Geräteabnahme](DEVICE_TEST_40124.md): Update/Altbestand, vier Kernsignaturen in GEN1/GEN2V2 mit echter Länge/ungeprüft, Wiederöffnung ohne Leser, historische Auswahl, erneute Aufnahme und Kartenentnahme/Neustart. **Vor dieser Bestätigung kein Produktmerge.** Danach genauen getesteten Baum/CI/Paket/Umfang dokumentieren und Meilenstein sichern.

## Übernommene bestätigte Basis und Grenzen

40123 / Schema 19 aus PR #70: Produktkopf 659f31c, Merge e783ad3, voller CI-Lauf 38335263243 Versuch 2 und Paket 38358288875. Tobi bestätigt 13.10.2026 09:04 Europe/Berlin den Kernumfang; GEN1 13780/6202 und GEN2V2 13780/9602 Bytes. Optionale damalige Wiederholungs-/Abbruchfälle nicht separat bestätigt; neue 40124-Funktion nicht damit freigeben. [Alter konkreter Beleg](DEVICE_CONFIRMATION_40123.md) nur bei Bedarf.

Aufnahme ist ungeprüft. Keine Trust-Engine, mathematische Prüfung, weitere EF-Signaturen, DDD-/Export-/LastCardDownload-Schreib-/UI-Folgephase. 7F66 ohne ISO-Primärformat, 0540 ohne legitimen SC5-Weg gesperrt. Keine Spesen-/Lohn-/Heimkehr-/Heimatstandort-/Abrechnungsphase. Browser, Unteragenten und CI-/Budget-/Trigger-/Uploadänderungen ausgeschlossen.
