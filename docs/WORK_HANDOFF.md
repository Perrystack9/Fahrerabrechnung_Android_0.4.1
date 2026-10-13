# FahrerTagebuch - aktuelle Übergabe

Einstieg: WORK_START und dessen fünf Startdateien. Branch/HEAD, Arbeitsbaum, GitHub-main, offene PRs und maßgebliche CI stets live prüfen.

## Aktueller Produktkandidat

**40125 / Schema 21 in offenem Draft-[PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76), kein Merge und keine 40125-Gerätefreigabe.** Basis main `07db91f33957a45598cae6e4772787d1aa7ddbf4`, bestätigtes 40124 aus PR #73. Die bereits übernommene Umsetzung nicht neu aufbauen. [Technischer Status](../WORK_STATUS.md), [ursprünglicher Identifikationsplan](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md).

Neu: 0501/0520 in beiden Anwendungen, 0525 nur V2; eigene Capture-Map und Receipt-Tabelle neben unveränderten Core-Belegen. Frische post-HASH-READs, strikte datenlose 6700/6B00-Grenze, echte Card_Sign-Policy/Le. Byteabweichung zum Bootstrap bricht die ganze laufende Gruppe über INCONSISTENT_DATA ab, auch nach erfolgreichem ersten Child. Keine Bestätigungsoption für widersprüchliche Daten. Unbewiesene Erstgrenze kann keinen positiven Beleg erzeugen.

Passende neue vollständige Originale atomar mit konkreten Child-Receipts; lokale Hash-/Signierfehler erhalten unabhängig bewiesene unsigned Quellen. Fehlgeschlagene neue READs behalten null Original-FK, auch wenn ein alter unsigned Bootstrap gespeichert bleibt. Kein späteres Überschreiben mit Discovery-Bytes, keine jüngeren Historienquellen oder Alt-Signatur-Ergänzung. G2-15-Byte-Kompatibilität erhalten; V2-15-Byte-Quelle bleibt unsigned. Separate neue Fingerprints, leere neue Map lässt alte Request-/Batch-Verträge unverändert.

Additive verschlüsselte 20→21, Produktionsregistrierung und echte SQLCipher-Migration/Reopen mit alten Core-/Identitätsquellen ergänzt. Historische 18-/19→21-Schemas korrekt rekonstruiert; eigenständige 19→20-Abnahme bleibt vollständig. Room21 ausschließlich aus Compiler, 1–20 unverändert. Kleine Anzeige in bestehenden Fahrerkarten-Details; alle fünf möglichen Quellen, Länge/Status/Grund und Maskierungswechsel geprüft. Bestehende vier Core-Anzeigen bleiben erhalten.

## Gates und exakt nächste Arbeit

1. Endgültigen PR-76-Kopf und **vollständige Android-CI** prüfen: sieben Gradle-Aufrufe, Unit/Lint/APKs/Testsignatur/Updatepaar/Schemaexport, ungekürzte API36-SQLCipher-/Compose-Suiten und Abschlussjob. Lokaler Gradle-Download ist blockiert und liefert keinen Android-Nachweis. Lokale statische Projektgrenzen und zehn CI-Regressionsprüfungen bestehen; keine Workflow-/Trigger-/Budget-/Upload- oder Testfilteränderung.
2. Für exakt diesen vollständig grünen Produktkopf gezieltes `device-test.yml` auf main starten: voller `source_sha`, `include_baseline=false` für installierte 40124. Im aktuellen Work-Zugang fehlt eine API-/CLI-Dispatch-Funktion; Tobi muss den konkreten Paketlauf starten, Browser nicht verwenden. Neuen Lauf/alle Ergebnisse anschließend prüfen; Infrastrukturkopf und APK-Quellkopf getrennt dokumentieren. Normale erfolgreiche CI erzeugt kein APK-Artefakt.
3. [40125-Gerätecheckliste](DEVICE_TEST_40125.md): Update/Altbestand, fünf neue Identitätssignaturen plus vier Core-Belege, Identitäts-/Kapazitätskonsistenz, Wiederöffnung, EF-/Anwendungs-/Historien-/Altarchivwechsel, Wiederholung und Kartenentnahme/Neustart. Noch nicht durchgeführt. **Kein Merge vor eigener ausdrücklicher Gerätebestätigung für den exakten endgültigen Produktbaum.**
4. Nach Sicherung dieses Meilensteins Sitzung abschließen; keine Trust-/DDD-/Schreib-/UI-Folgephase parallel beginnen.

## Bestätigte Basis und Grenzen

40124 / Schema20: PR #73 Merge `3298b707525a4f33162c835ce31deaf181d2e772`, vollständiger Tree `3540b26b775d17c9bdc6335549dafbfbd58bcd92` identisch zum CI-/Gerätekopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`. Volle Produkt-CI 38381962283, zusätzliche Main-CI 38394108412 und exaktes Gerätepaket 38388192092 erfolgreich. Tobi bestätigt alle eigenen Punkte am 13.10.2026 13:57 Europe/Berlin, [Gerätebeleg](DEVICE_CONFIRMATION_40124.md). Umfang nicht auf 40125 oder andere Karten/Hardware/Kurven übertragen.

Import, Profile, Originale, Herkunft, atomare Gruppen, unveränderliche Historie und SQLCipher/Keystore erhalten. Aufnahme bleibt ungeprüft. Kein Abrechnungs-/Spesen-/Lohn-/Heimatstandortauftrag, kein destruktiver Fallback. Browser und Unteragenten ausgeschlossen; Gerätebestätigung ist das offene Produktgate.
