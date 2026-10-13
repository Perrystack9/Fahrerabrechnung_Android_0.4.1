# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien; Git/main/PR/CI immer live prüfen.

## Abgeschlossener Produkt-Meilenstein

**40124 / Schema 20 übernommen und im eigenen Geräteumfang bestätigt.** [PR #73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73), Merge `3298b707525a4f33162c835ce31deaf181d2e772`, vollständiger Tree `3540b26b775d17c9bdc6335549dafbfbd58bcd92` identisch zum geprüften Produktkopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`. Keine Umsetzung wiederholen.

Eigene volle [CI 38381962283](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38381962283) erfolgreich: sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexport, vollständige API36-SQLCipher-/Compose-Suiten inklusive additiver Migration 19→20 und Abschlussgate. [Gerätepaket 38388192092](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38388192092) erfolgreich für exakt denselben Quellkopf; Infrastruktur `2a0b1face3fd8d08409415e36acb5e27bc28d583`, `include_baseline=false`.

Tobi bestätigt am 13.10.2026 13:57 Europe/Berlin ausdrücklich sämtliche Checklistenpunkte: Update/Altbestand, eigener Import, vier Signaturanzeigen, plausible fachliche Daten, Wiederöffnung ohne Leser, historische Quellen und Altarchiv soweit vorhanden, erneute Aufnahme ohne doppelte Tätigkeiten sowie Kartenentnahme/Wiederaufnahme. Screenshots zeigen GEN1-Originale 13780/6202 und Signaturen 128/128 Bytes; GEN2V2-Originale 13780/9602 und Signaturen 64/64 Bytes. Alle vier „aufgenommen · ungeprüft“. Eigenes bisheriges Android-/USB-Setup; Modellnamen nicht erneut genannt. [Genauer Gerätebeleg](DEVICE_CONFIRMATION_40124.md).

Erhalten: vollständige Originale, eigene Profil-/Karten-/Parent-/Child-Bindung, MF am Parent, unveränderliche Historie/Fingerprints, unsigned Rohaufnahme bei lokalen Signaturfehlern und Gruppenabbruch bei Transportverlust. SELECT/HASH/READ/Grenze/SIGN ununterbrochen, C101-Policy, defensive Kopien und serialisierte Sessions. Neue verschlüsselte sitzungseigene Receipts/FKs, additive 19→20, keine Alt-Signaturen oder jüngeren Quellen. Room-20-Export echt, SHA256 `34a9253cb546255ce9c12bef2d9808933e1af8a8dca1c27a783eb7322e6ab6e6`; Schema 19 unverändert. Keine Filter/Quarantäne oder CI-/Upload-/Budgetänderung. Lokaler Gradle-Download blockiert, Android-Nachweis aus echter CI.

## Abgeschlossener nächster Plan-Meilenstein

[Identifikations-Signaturplan](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md) für **40125 / Schema 21** ausgearbeitet, noch keine Produktfunktion implementiert. Genau 0501/0520 in GEN1 und GEN2(V2), 0525 nur auf V2 wenn vorhanden; fünf neue Belege auf üblicher G1+G2V2-Karte. Neue vollständige post-HASH-READ-Aufnahmen, bytegenauer Bootstrap-Vergleich, nicht verschluckter Konsistenzabbruch der gesamten Gruppe, unsigned Erhalt bei lokalen Hash-/Signierstörungen, eigene sitzungseigene Identitäts-Receipts neben unveränderten Core-Receipts, additive 20→21. Historische 15-Byte-Originale/Fingerprints nicht ändern; keine unbewiesene 17-Byte-V2-Signatur aus einem kürzeren Präfix. [Neue Gerätecheckliste](DEVICE_TEST_40125.md) vorbereitet, nicht durchgeführt.

Planbasis main `71eac7163e53148952b09639c1fcd203a2a7b8a4`, bestätigtes 40124 / Schema 20. Zusätzliche volle Produkt-Main-CI 38394108412 und Docs-Main-CI 38395123764 am Einstieg erfolgreich geprüft. Primärmatrix um aktuellen 40124-Core-Signaturstand und den nächsten begrenzten Umfang berichtigt. Nur dokumentarische Markdown-Dateien geändert; aktuelle App/DB/Workflows unverändert.

## Exakt nächste Arbeit

1. Aktuellen main, offene PRs und sämtliche relevanten Checks live prüfen; keine alten Pending-/Grün-Angaben als aktuellen HEAD behandeln.
2. Den Identifikationsplan gezielt öffnen. **40125 als eigenen Produkt-Meilenstein** umsetzen: neue Modelle und Befehls-/Konsistenz-/Fehler-/Freeze-Tests zuerst; dann Reader-Reihenfolge, konkrete Quellenbindung, atomare Persistenz/additive 20→21 und kleine Anzeige in vorhandenen Kartenangaben. Core-Enum/Fingerprints/Receipts und komplette bestehende Tests erhalten.
3. Room-21-Export nur aus echtem Compiler, alte Exporte unverändert. Vollständige unveränderte Android-/API36-CI für endgültigen Kopf, exaktes Paket und eigene erforderliche 40125-Gerätebestätigung **vor Merge**. 40124-Geräteumfang nicht übertragen. Nach Sicherung dieses Produkt-Meilensteins Sitzung beenden.

## Grenzen

Aufnahme ist ungeprüft. Keine Trust-Engine, mathematische Prüfung, vollständige Kartenabdeckung, DDD-/Export-/LastCardDownload-Schreib-/UI-Folgephase gleichzeitig beginnen. Reine GEN2-Karten, andere Hardware oder jede seltene APDU-Abbruchphase nicht aus dem konkreten Gerätebeleg ableiten. `7F66` ohne ISO-Primärformat und `0540` ohne legitimen SC5-Weg bleiben gesperrt. Keine Spesen-/Lohn-/Heimkehr-/Heimatstandort-/Abrechnungsphase. Browser, Unteragenten und CI-/Budget-/Trigger-/Uploadänderungen ausgeschlossen.
