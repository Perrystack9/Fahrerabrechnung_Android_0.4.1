# FahrerTagebuch - aktueller technischer Zustand

**M3/40131/Room23 technisch abgenommen und integriert; M4 ausdrücklich genehmigt.** Basis `ba0cf1933f8d01590d8fdef32526f1715ec8d571`, identischer endgültiger CI-/Gerätebaum. [M3-Integration](docs/M3_INTEGRATION_20261016.md), [aktuelle Regeln](docs/CURRENT_RULES.md). Main-CI312 vollständig erfolgreich. Keine juristische Gesamtfreigabe, kein M4-Merge/M5–M7/Release.

## Integration und nachgewiesene Basis

- Planungs-/M0-PR88: geprüfter Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78`, erfolgreiche Docs-CI38684167403/Versuch1; zuerst integriert durch Merge `9ff0a525d8394031148aada37593c9fed8f84148`. Alle80/A–H/E01–E12/P01–P05, N01–N19 und MI01–MI24 erhalten.
- M1-PR89: ursprünglicher Produktprüfkopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`, vollständige CI38729242320/Versuch1 erfolgreich.559 Unit-Tests; vollständige API36-SQLCipher-/Compose-Suiten (151 App-/297 Datenmethoden), sieben Gradle-Gates, APK/Signatur/Updatepaar/Schemaexport. Keine Filter/Quarantäne.
- Eigene Gerätebestätigung für exakt40129/Room23 und APK-SHA256 `f5f7873e88149515829c406ce04f20dc83cb56ac478497f05cd5c077c65bc996`; keine zusätzlichen Einzelmesswerte oder weitere Hardware-/Kartenabdeckung daraus ableiten.
- PR89 nach erfolgreicher neuer vollständiger CI38744945937/Versuch1 integriert: **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**. Produktdateien40129 und geschützter Regelabschnitt bytegleich zum bestätigten Kopf. Zusätzliche Produkt-main-CI38749285258/Versuch1 ebenfalls vollständig erfolgreich; [Integrationsnachweis](docs/M0_M1_INTEGRATION_20261015.md).

## Implementiert und erhalten

M1: konsistenter lesender Snapshot des eigenen Profils/physischer Karten/aller G1-/G2-Imports, vollständige Original-/Receipt-/Signaturherkunft; reale c/p-Semantik, manuelle bekannte Nachträge versus Unknown; UTC-Intervalle nur mit echten Endbelegen, gleiche Minuten ohne Indexgewinner; Deduplizierung mit allen Quellen, Konflikte und regelbezogene Abdeckung; zeitlich versionierter isolierter Kontext/minimaler Scope-Guard; revisionsgebundener Cold Flow.34 neue Domain- und24 Androidmethoden mit MI01–MI24-Zuordnung.

Room23 ergänzt ausschließlich `violation_context_revisions`, additive Migration22→23. Compilerexport23-SHA256 `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`; alle45 bisherigen Entities und Exporte1–22 unverändert. Verschlüsselte18/22→23-Upgrades, komplette1→23-Kette, Inhalte/FKs/Reopen/CAS/Historie/Rollback/Cancellation/No-backfill geprüft. Keine Änderungen an Parsern, Hardware, Kryptografie oder CI/Sicherheitsregeln.

40128-Kryptografie bleibt abgeschlossen: [PR85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Merge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`, volle CI38547729369 und Produkt-main-CI38559255131 erfolgreich; [eigene Gerätebestätigung](docs/DEVICE_CONFIRMATION_40128.md). Gespeicherte0504/0505,0501/0520,0502/0503 sowie bedingtesV2-0525 mathematisch geprüft. Zertifikatsvertrauen immer NOT_EVALUATED. Keine neue Trust-/DDD-/Signaturaufnahme-/Kartenschreibphase. Frühere40124–40128 nicht erneut implementieren.

## Aktuelle M4-Arbeit und Grenzen

M2/40130/Room23 ist technisch abgenommen; PR90 `c542b4c9cd371a6e3a9489678e752e07ca7e7360`, eigene Geräteantwort15.10.2026 positiv und volle Main-CI38780974996 erfolgreich. M2-Grenzen bleiben erhalten.

M3/40131 ergänzt tatsächliche Ruhe-/Fährkandidaten,11/9/3+9,24/30h, getrennte Reduzierungen, konkrete25-h-Busdienste und den gemeinsamen revisionsgeprüften M2/M3-Flow. Endkopf7545eae, volle CI311 erfolgreich,691 Unit-Tests ohne Fehler/Skip;82 neue M3-Domainmethoden und zehn neue SQLCipher-Androidmethoden. Eigene40131-Geräteantwort16.10.2026 positiv. PR91-Merge `ba0cf1933f8d01590d8fdef32526f1715ec8d571`; identischer vollständiger Prüf-/Gerätebaum. Neue volle Main-CI312 mit allen vier Jobs erfolgreich. [Integrationsbeleg](docs/M3_INTEGRATION_20261016.md).

M4-Branch `feature/v1-m4-weekly-rest-20261016`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m4`. [Arbeitsvertrag](docs/VIOLATION_ENGINE_M4_IMPLEMENTATION.md): W01–W14, globale Wochenruhe-/Ausgleichszuordnung, internationale und Bus-Sonderregeln. Quellenabgleich und erste Domainimplementierung mit270 zuletzt erfolgreichen Domain-Tests; M4 noch unvollständig und nicht abgenommen. Eigener PR und abschließende vollständige CI erst nach der lokalen Entwicklungsphase; regelmäßige GitHub-Sicherungen bleiben erforderlich.

L02/L03/L04/L09 und sonstige betroffene Fachgrenzen bleiben offen. Keine gesicherten Budgets aus unbestätigter Zuordnung, keine gemeinsame Resetfunktion für beide gesetzlichen Zähler. Keine UI M5/PDF M6 oder Release/Beta M7 implementieren. Unabhängige Fachgesamtprüfung/Beta bleiben verbindlich.

[Wiederaufnahme16.10.2026](docs/M3_RECOVERY_20261016.md) trennt zusätzlich den GitHub-M3-Checkpoint von fehlenden lokalen Folgeänderungen und ihrer neu geprüften Bearbeitung. Automatische Workspace-Wartung entfernte frühere Checkouts und die separate unvollständige Settings-Arbeitskopie. M1 ist aus GitHub wiederhergestellt; der alte Entwurf nicht. Keine Rekonstruktion erfinden. [Nachweisgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Browser/Unteragenten ausgeschlossen; alle vorhandenen Arbeiten erhalten.
