# FahrerTagebuch - aktueller technischer Zustand

**M0/M1 integriert; M2/40130/Room23 wird auf eigenem Branch geprüft.** M1/40129/Room23 ist ausdrücklich abgenommen. Tobi bestätigte die eigene Geräteprüfung am15.10.2026 15:13 Europe/Berlin („Passt alles 👌🏻“) und erteilte danach ausdrücklich M1-Abnahme, kontrollierte Mergefreigabe für PR88/PR89 sowie anschließende M2-Implementierungsfreigabe. Keine M2-Merge-/M3–M7-/Releasefreigabe. [Regeln](docs/CURRENT_RULES.md), [M1-Bericht](docs/VIOLATION_ENGINE_M1_IMPLEMENTATION.md), [Abnahmematrix](docs/VIOLATION_ENGINE_M1_ACCEPTANCE.md), [Gerätebeleg](docs/DEVICE_CONFIRMATION_40129.md).

## Integration und nachgewiesene Basis

- Planungs-/M0-PR88: geprüfter Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78`, erfolgreiche Docs-CI38684167403/Versuch1; zuerst integriert durch Merge `9ff0a525d8394031148aada37593c9fed8f84148`. Alle80/A–H/E01–E12/P01–P05, N01–N19 und MI01–MI24 erhalten.
- M1-PR89: ursprünglicher Produktprüfkopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`, vollständige CI38729242320/Versuch1 erfolgreich.559 Unit-Tests; vollständige API36-SQLCipher-/Compose-Suiten (151 App-/297 Datenmethoden), sieben Gradle-Gates, APK/Signatur/Updatepaar/Schemaexport. Keine Filter/Quarantäne.
- Eigene Gerätebestätigung für exakt40129/Room23 und APK-SHA256 `f5f7873e88149515829c406ce04f20dc83cb56ac478497f05cd5c077c65bc996`; keine zusätzlichen Einzelmesswerte oder weitere Hardware-/Kartenabdeckung daraus ableiten.
- PR89 nach erfolgreicher neuer vollständiger CI38744945937/Versuch1 integriert: **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**. Produktdateien40129 und geschützter Regelabschnitt bytegleich zum bestätigten Kopf. Zusätzliche Produkt-main-CI38749285258 live prüfen; [Integrationsnachweis](docs/M0_M1_INTEGRATION_20261015.md).

## Implementiert und erhalten

M1: konsistenter lesender Snapshot des eigenen Profils/physischer Karten/aller G1-/G2-Imports, vollständige Original-/Receipt-/Signaturherkunft; reale c/p-Semantik, manuelle bekannte Nachträge versus Unknown; UTC-Intervalle nur mit echten Endbelegen, gleiche Minuten ohne Indexgewinner; Deduplizierung mit allen Quellen, Konflikte und regelbezogene Abdeckung; zeitlich versionierter isolierter Kontext/minimaler Scope-Guard; revisionsgebundener Cold Flow.34 neue Domain- und24 Androidmethoden mit MI01–MI24-Zuordnung.

Room23 ergänzt ausschließlich `violation_context_revisions`, additive Migration22→23. Compilerexport23-SHA256 `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`; alle45 bisherigen Entities und Exporte1–22 unverändert. Verschlüsselte18/22→23-Upgrades, komplette1→23-Kette, Inhalte/FKs/Reopen/CAS/Historie/Rollback/Cancellation/No-backfill geprüft. Keine Änderungen an Parsern, Hardware, Kryptografie oder CI/Sicherheitsregeln.

40128-Kryptografie bleibt abgeschlossen: [PR85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Merge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`, volle CI38547729369 und Produkt-main-CI38559255131 erfolgreich; [eigene Gerätebestätigung](docs/DEVICE_CONFIRMATION_40128.md). Gespeicherte0504/0505,0501/0520,0502/0503 sowie bedingtesV2-0525 mathematisch geprüft. Zertifikatsvertrauen immer NOT_EVALUATED. Keine neue Trust-/DDD-/Signaturaufnahme-/Kartenschreibphase. Frühere40124–40128 nicht erneut implementieren.

## Aktueller M2-Umfang und Gates

[Implementierungsbericht](docs/VIOLATION_ENGINE_M2_IMPLEMENTATION.md), [fachliche Nachweise](docs/VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md), [Abnahme](docs/VIOLATION_ENGINE_M2_ACCEPTANCE.md). M2-Branch `feature/v1-m2-driving-breaks-20261015`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m2`, Basis3c99b7db.40130/Room23; keine neue DB-Tabelle, Migration, Parser-/Krypto-/Hardware-/CI-Änderung.

Implementiert: reiner M1-Konsument für9-/10-h-Tagesfahrt, zwei Verlängerungsvorgänge je technischer UTC-Woche,56-/90-h-UTC-Projektionen,270-/45-/15+30-Pausen, datierte Gelegenheitsbus-/eigene Crewpause; genaue Fahrteilstücke/Herkunft, lokale Lücken-/Scopegrenzen, offener Artikel12-Zahlenvertrag und revisionsgeprüfter automatischer Flow während der App-Nutzung. Keine Verstoßoberfläche oder PDF. Ursprüngliche Rohdaten bleiben unverändert.

L02 ist **nicht geschlossen**; gesicherte Wochen-/Verlängerungsbudgets bleiben gesperrt. Konkrete Entscheidungsvorlage zu technischer UTC-Norm versus lokaler TRACE-Auslegung, Grenzübertritt>9h und historischen Tatbeständen vorhanden. Q13-Empfehlung getrennt von M3-Ruhequalifikation; Fähranker und12-Tage-Bus-Nachtqualifikation bleiben M3/M4. Zwei10-h-Vorgänge werden nicht durch eine Wochenruhe zurückgesetzt; späterer Tagesruhereduzierungszähler ist anderer Vertrag.

Frühe isolierte Domainprüfungen und projektkonfigurierte Domain-Gradleprüfung sind Entwicklungsnachweise; endgültige vollständige CI, alle bestehenden API36-SQLCipher-/Compose-Suiten und neue40130-Geräteprüfung müssen am veröffentlichten Kopf gesondert nachgewiesen werden. Aktueller tatsächlicher Endstatus im M2-PR-Protokoll, keine offene Prüfung als erfolgreich behandeln.

M2-Merge benötigt neue ausdrückliche Zustimmung. M3–M7/Release gesperrt; qualifizierte unabhängige Fachgesamtprüfung und Beta5–10 Fahrer bleiben Releasegates. Historische Ortszeit ist neue bevorzugte spätere M5/M6-Anzeige, ohne Auswirkung auf Rechtswerte.

Automatische Workspace-Wartung entfernte frühere Checkouts und die separate unvollständige Settings-Arbeitskopie. M1 ist aus GitHub wiederhergestellt; der alte Entwurf nicht. Keine Rekonstruktion erfinden. [Nachweisgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Browser/Unteragenten ausgeschlossen; alle vorhandenen Arbeiten erhalten.
