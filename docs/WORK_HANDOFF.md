# FahrerTagebuch - aktuelle Übergabe

**M0/M1 kontrolliert integriert, M2-Implementierung genehmigt und in eigener Prüfung.** Kein M2-Merge, M3–M7 oder Release. Zuerst WORK_START und fünf Startdateien; keine pauschale Volllektüre/Chatrekonstruktion, Browser oder Unteragenten. [Status](../WORK_STATUS.md), [Regeln](CURRENT_RULES.md).

## Neue Basis und Nachweise

PR88-Merge `9ff0a525d8394031148aada37593c9fed8f84148` nach erfolgreicher Docs-CI38684167403; zusätzliche Docs-main-CI38744376819 ebenfalls erfolgreich. Anschließend PR89 auf Planung abgestimmt: vier reine Dokumentkonflikte aufgelöst, alle Produktdateien gegenüber dem eigenen Geräteprüfkopf212a140 unverändert und geschützter Regelabschnitt bytegleich. Neue volle CI38744945937/Versuch1 auf `1f1655de023ae6463419be8be9278f0c1c05232e` vollständig erfolgreich, alle vier Jobs/ungekürzte API36-Suiten. PR89 nach abschließender Live-Prüfung integriert: **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**,40129/Room23. Zusätzliche volle Produkt-main-CI38749285258 live prüfen. [Integrationsbeleg](M0_M1_INTEGRATION_20261015.md), [eigene Gerätebestätigung und ausdrückliche M1-Abnahme](DEVICE_CONFIRMATION_40129.md).

## Aktueller M2-Auftrag

Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m2`, Branch `feature/v1-m2-driving-breaks-20261015`, Basis3c99b7db. Stand40130/Room23; keine neue Migration/Schema-/Parser-/Krypto-/Hardware-/Workflowänderung. [M2-Bericht](VIOLATION_ENGINE_M2_IMPLEMENTATION.md), [L02/L03/L05/L06 und Quellen](VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md), [Abnahmematrix](VIOLATION_ENGINE_M2_ACCEPTANCE.md).

D01–D06/B01–B05 auf dem vorhandenen M1-Snapshot: normale Tagesfahrt/Verlängerungsvorgänge,56-/90-h-UTC-Projektionen, reguläre/geteilte Pause, datierte Bus-/Crewfälle und artikelbezogener Scope; genaue Fahrteilstücke und Revisions-/Herkunftsbezug. Automatischer read-only Flow während der App-Nutzung, letzte M1-Tokenprüfung vor Veröffentlichung. Kein UI-/PDF-/Ruhe-/Ausgleichsmotor.

L02 bleibt offen: technische UTC-Norm versus lokale TRACE-Woche nicht verbindlich aufgelöst. Wochen-/Verlängerungsbudgets nicht als gesichert ausgeben. Q13 nur Tageslenk-Konvention;7h ist keine legale Tagesruhe. Die zwei10-h-Verlängerungen zählen je Kalenderwoche ohne Wochenruhe-Reset, spätere Ruhereduzierungen zwischen qualifizierenden Wochenruhen bleiben separater M3-Vertrag. Fährenabhängige Tagesanker und12-Tage-Bus-Nachtqualifikation M3/M4, keine erfundene Entwarnung. Artikel12-Zahlenbefunde offen, keine Anerkennung/Schuld/Heimkehrüberwachung.

## Verbindliche nächsten Prüfschritte

1. Tatsächlichen M2-Remote-Kopf/Arbeitsbaum/CI prüfen und vorhandene Arbeit erhalten. Frühere M1-Checkouts/Branches nicht löschen oder neu implementieren.
2. Fachliche Referenzen und zusätzliche Regressionen aus der M2-Matrix tatsächlich ausführen; früher isolierter Compilerlauf ist keine konfigurierte Projekt-/Android-Abnahme.
3. Alle sieben unveränderten Gradle-Gates und vollständige API36-SQLCipher-/Compose-Suiten einschließlich neuer sechs M2-Androidmethoden, Signatur/Updatepaar/Compilerexporte; keine Filter/Quarantäne. Fehler beheben und finalen Kopf komplett prüfen.
4. Exaktes40130-Gerätepaket erst nach grünen Gates prüfen und mit verständlicher Anleitung direkt im Chat vorlegen;40129-Antwort nicht übertragen. Keine Datenlöschung oder neue Geräteabnahme behaupten.
5. M2 zur gesonderten fachlichen/technischen Abnahme und Mergeentscheidung vorlegen. L02-Entscheidungsvorlage und weitere konkrete Fachnachweise ausdrücklich offen; keine unbelegten Annahmen zum Schließen von Gates.

Anzeigeentscheidung15.10.2026 ersetzt die ausschließlich feste Zone als bevorzugte Darstellung: später belegbare historische Ortszeit einschließlich DST/Länder-/GNSS-/Grenzdaten, feste einstellbare Standardzone als Rückfall und Kennzeichnung von Unklarheit. M2 rechnet ausschließlich UTC, keine Standortüberwachung. Produktentscheidungen ansonsten unverändert; neue Entscheidung im historischen Addendum nachvollziehbar ergänzt.

40128-Kryptografie abgeschlossen; Zertifikatsvertrauen NOT_EVALUATED. Keine Trust-/DDD-/Kartenschreibarbeit. Unabhängige qualifizierte Fachgesamtprüfung/Beta5–10 Fahrer bleiben Releasegates.

## Lokale Arbeit und Workspace-Wiederherstellung

Die automatische Workspace-Wartung während der Pause am15.10.2026 hat die früheren Scratch-Checkouts einschließlich lokaler Buildumgebung und Sicherungspfade entfernt. Kein vom Agenten ausgeführter Reset/Clean oder Löschauftrag. M1 wurde aus dem unveränderten PR89-Kopf `68bee34b71ccd6e1489e1ae5e96ba43bf8781cb9` und der bereits zuvor auf GitHub vorbereiteten Testkorrektur `378a50dbaef4c836cb4c7d692f0928f42d5bab5c` wiederhergestellt. Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m1`, lokaler Fortsetzungsbranch `work/m1-resumed-20261015`; veröffentlichter Zielbranch bleibt `feature/v1-m1-input-foundation-20261015`. PR88/M0 ist weiterhin unter dem gepinnten GitHub-Kopf verfügbar. Veröffentlichung weiterhin mit Blob-/Tree-SHA-Abgleich, kein Rewrite oder Merge.

Die zuvor vorhandene uncommittete Settings-Arbeit `/workspace/scratch/4546aa92dbbb/FahrerTagebuch/feature/src/main/kotlin/de/fahrerabrechnung/feature/settings/SettingsScreen.kt` und Sicherung `/workspace/scratch/4c3cf966d792/session-backups/SettingsScreen.kt.before-0502-0503`, zuletzt belegter SHA256 `33d91e46cf441e1f3582f1f9c1f050e986bbb9777efbc27a7e009afc2c588ea0`, sind lokal nicht mehr vorhanden. Kein identischer Inhalt in den drei unterschiedlichen erreichbaren historischen Git-Blobs dieser Datei oder den gezielten Suchergebnissen gespeicherter Dateien gefunden. Wiederherstellung dieser separaten früheren Arbeit bleibt offen und benötigt eine tatsächlich passende externe Kopie; keine vermutete Neufassung als Original ausgeben. M1 und die Fahrerkartendaten auf Tobis Gerät werden dadurch nicht verändert.
