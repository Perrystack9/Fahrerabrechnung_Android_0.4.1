# M3 – Technische Abnahme und kontrollierte Integration

Stand: 16.10.2026. Technische Abnahme im tatsächlich implementierten und nachgewiesenen Umfang; keine unabhängige juristische Gesamtfreigabe.

## Freigabe und Gerätebindung

Tobi bestätigt das bereitgestellte eigene Gerätepaket40131/Room23 am16.10.2026,09:55 Europe/Berlin: „Ne das passt alles 👌🏻“. Der anschließende vierseitige PDF-Auftrag „M3 abschließen. M4 kontrolliert starten.“ vom16.10.2026 erteilt ausdrücklich die technische M3-Abnahme, den kontrollierten PR91-Merge und die bedingte anschließende M4-Entwicklung. PDF-SHA256: `3d582e399156a9d41db3ca02cafbe384c397674670357642c8957570a0a76b35`.

- Geprüfter Kopf: `7545eaef2ce078c6c96ead18a45e0e5fd499e7f7`.
- Vollständiger CI-/Gerätebaum: `32f6da8ae5a8afcac58a2246d2ec8bdf02d7499c`.
- APK-SHA256: `30c8373773083e5805a09bf7bf845ce3a72de1866451493900655fbf1923e6a9`.
- ZIP-SHA256: `bfa0f5319b766e1ea31041f69a79ad3da7957be22499f3b3c6928525b56ac219`.
- [PR91-Abnahmeprotokoll](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/91), [vollständige CI311/Versuch1](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38856596778): alle vier Jobs erfolgreich.

691 konfigurierte Projekt-Unitprüfungen am Endkopf,0 Fehler/0 übersprungen;82 neue M3-Methoden. Zusätzlich166 isolierte Domainmethoden und elf lokale Abschlussprüfschritte erfolgreich. Die vollständigen API36-SQLCipher-/Compose-Aufgaben bestehen. Die Quellinventur und Laufzeit-Testinstanzzahlen werden weiterhin unterschieden; erfolgreiche Einzel-XML-Artefakte werden nach unveränderter Richtlinie nicht hochgeladen. Die eigene Bestätigung belegt nur den tatsächlich geprüften Geräteumfang, keine zusätzliche Hardware-/Kartenabdeckung oder juristische Prüfung.

## Tatsächlicher Merge und Main-CI

Vor Integration: erwarteter Kopf identisch, main unverändert, mergeable/clean, alle vier Checks erfolgreich. Der reguläre Merge wurde mit erwarteter Kopf-SHA ausgeführt.

- Tatsächlicher Merge-Commit: **`ba0cf1933f8d01590d8fdef32526f1715ec8d571`**.
- Eltern: `c542b4c9cd371a6e3a9489678e752e07ca7e7360` und `7545eaef2ce078c6c96ead18a45e0e5fd499e7f7`.
- Tatsächlicher Mergebaum: **`32f6da8ae5a8afcac58a2246d2ec8bdf02d7499c`**, exakt derselbe vollständige Baum wie Endprüfung und Gerätepaket.
- Neue vollständige [Produkt-main-CI312/Versuch1](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38888523631), Push für diesen Merge: **alle vier Jobs erfolgreich**.

| Main-CI-Job | ID | Ergebnis |
|---|---|---|
| Projektgrenzen/CI-Umfang |116932080997|success|
| Build/Test/Lint/APK |116932133541|success|
| Vollständige API36-SQLCipher-/Compose-Instrumentierung |116934582270|success|
| Abschlussgate |116941060134|success|

Erst nach dem vollständigen Erfolg wurde der M4-Branch angelegt. Während des Integrationsgates erfolgte ausschließlich die Wiederherstellung der lokalen Arbeits-/Buildumgebung und deren Konfigurationsprüfung, keine M4-Facharbeit oder Produktimplementierung.

## Unveränderte Grenzen

L02-Wochenzeit/Verlängerungszuordnung, L03-Konventionen, L04-Wochen-/Ausgleichsreferenzen und L09-Sonderanrechnungen bleiben nachweisabhängig offen. M3-Rechts-/Budgetguards werden durch die technische Abnahme nicht aufgehoben. Parser, Hardware, Kryptografie, Original-/Receipt-Herkunft, Datenbankschema/Migrationen, Exporte1–23 und CI-Vorgaben sind gegenüber der zuvor bestätigten Basis unverändert. Schema23-SHA256: `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`.

Freigegeben ist nun M4 gemäß [Arbeitsvertrag](VIOLATION_ENGINE_M4_IMPLEMENTATION.md). **Kein M4-Merge, M5–M7 oder Release ohne neue ausdrückliche Freigabe.** Die unabhängige qualifizierte Fachgesamtprüfung und Beta5–10 Fahrer bleiben Releasegates.
