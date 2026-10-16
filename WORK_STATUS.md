# FahrerTagebuch - aktueller technischer Zustand

**M2/40130/Room23 technisch abgenommen und integriert; M3 ausdrücklich genehmigt.** Basis `c542b4c9cd371a6e3a9489678e752e07ca7e7360`, exakt derselbe Baum wie der abschließende CI-/Gerätestand. [M2-Integrationsbeleg](docs/M2_INTEGRATION_20261015.md), [aktuelle Regeln](docs/CURRENT_RULES.md). Keine juristische Gesamtbestätigung, kein M3-Merge/M4–M7/Release.

## Integration und nachgewiesene Basis

- Planungs-/M0-PR88: geprüfter Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78`, erfolgreiche Docs-CI38684167403/Versuch1; zuerst integriert durch Merge `9ff0a525d8394031148aada37593c9fed8f84148`. Alle80/A–H/E01–E12/P01–P05, N01–N19 und MI01–MI24 erhalten.
- M1-PR89: ursprünglicher Produktprüfkopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`, vollständige CI38729242320/Versuch1 erfolgreich.559 Unit-Tests; vollständige API36-SQLCipher-/Compose-Suiten (151 App-/297 Datenmethoden), sieben Gradle-Gates, APK/Signatur/Updatepaar/Schemaexport. Keine Filter/Quarantäne.
- Eigene Gerätebestätigung für exakt40129/Room23 und APK-SHA256 `f5f7873e88149515829c406ce04f20dc83cb56ac478497f05cd5c077c65bc996`; keine zusätzlichen Einzelmesswerte oder weitere Hardware-/Kartenabdeckung daraus ableiten.
- PR89 nach erfolgreicher neuer vollständiger CI38744945937/Versuch1 integriert: **`3c99b7db397f307ccbe91b33fa9862e2b410a144`**. Produktdateien40129 und geschützter Regelabschnitt bytegleich zum bestätigten Kopf. Zusätzliche Produkt-main-CI38749285258/Versuch1 ebenfalls vollständig erfolgreich; [Integrationsnachweis](docs/M0_M1_INTEGRATION_20261015.md).

## Implementiert und erhalten

M1: konsistenter lesender Snapshot des eigenen Profils/physischer Karten/aller G1-/G2-Imports, vollständige Original-/Receipt-/Signaturherkunft; reale c/p-Semantik, manuelle bekannte Nachträge versus Unknown; UTC-Intervalle nur mit echten Endbelegen, gleiche Minuten ohne Indexgewinner; Deduplizierung mit allen Quellen, Konflikte und regelbezogene Abdeckung; zeitlich versionierter isolierter Kontext/minimaler Scope-Guard; revisionsgebundener Cold Flow.34 neue Domain- und24 Androidmethoden mit MI01–MI24-Zuordnung.

Room23 ergänzt ausschließlich `violation_context_revisions`, additive Migration22→23. Compilerexport23-SHA256 `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`; alle45 bisherigen Entities und Exporte1–22 unverändert. Verschlüsselte18/22→23-Upgrades, komplette1→23-Kette, Inhalte/FKs/Reopen/CAS/Historie/Rollback/Cancellation/No-backfill geprüft. Keine Änderungen an Parsern, Hardware, Kryptografie oder CI/Sicherheitsregeln.

40128-Kryptografie bleibt abgeschlossen: [PR85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Merge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`, volle CI38547729369 und Produkt-main-CI38559255131 erfolgreich; [eigene Gerätebestätigung](docs/DEVICE_CONFIRMATION_40128.md). Gespeicherte0504/0505,0501/0520,0502/0503 sowie bedingtesV2-0525 mathematisch geprüft. Zertifikatsvertrauen immer NOT_EVALUATED. Keine neue Trust-/DDD-/Signaturaufnahme-/Kartenschreibphase. Frühere40124–40128 nicht erneut implementieren.

## Aktuelle M3-Arbeit und fortbestehende Grenzen

M2 implementiert9-/10-h-Tagesfahrt, zwei Vorgänge je technischer UTC-Woche,56-/90-h-Projektionen,270/45/15+30 sowie datierte Bus-/Crewpausen auf M1. Finale CI38773986395 erfolgreich,609 Unit-Tests ohne Fehler/Skip, vollständige API36-/SQLCipher-/Compose-Prüfung. Eigene40130-Geräteantwort15.10.2026 19:38 Europe/Berlin positiv. Merge und neue volle main-CI38780974996 im [Integrationsbeleg](docs/M2_INTEGRATION_20261015.md); alle vier Jobs erfolgreich. Keine Schema-/Parser-/Krypto-/Hardware-/CI-Änderung in M2.

M3-Branch `feature/v1-m3-daily-rest-20261015`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m3`. [Implementierungsvertrag](docs/VIOLATION_ENGINE_M3_IMPLEMENTATION.md), [Quellenprüfung](docs/VIOLATION_ENGINE_M3_LEGAL_EVIDENCE.md), [Abnahmesollwerte](docs/VIOLATION_ENGINE_M3_ACCEPTANCE.md). M3 in [Draft-PR91](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/91):tatsächliche Ruhe-/Fährkandidaten,11/9/3+9,24/30h, getrennte Tagesreduzierungen, konkrete25-h-Busdienste, chronologische alternative Anker und gemeinsamer revisionsgebundener M2/M3-Flow implementiert.166 isolierte Methoden erfolgreich (84 M1/M2+82 M3);zehn neue echte SQLCipher-Androidmethoden vorbereitet. Ziel40131, Room23 unverändert. [Referenzzuordnung](docs/VIOLATION_ENGINE_M3_REFERENCE_RESULTS.md). Vollständige technische CI, Paketbindung und eigene Geräteabnahme werden im PR91-Abnahmeprotokoll nachgewiesen; eine grüne CI ersetzt keine Geräte- oder juristische Abnahme.

L02/L03/L09 und sonstige betroffene Fachgrenzen bleiben erhalten. Tagesruhe, Tageslenkkonvention und Wochen-Verlängerungszähler sind verschiedene Verträge. Kein Wochen-/Ausgleichsmotor M4, keine UI M5/PDF M6; unabhängige Fachgesamtprüfung/Beta M7 weiter verbindlich. Historische Ortszeit ist spätere bevorzugte Anzeige, UTC bleibt Berechnungsbasis.

[Wiederaufnahme16.10.2026](docs/M3_RECOVERY_20261016.md) trennt zusätzlich den GitHub-M3-Checkpoint von fehlenden lokalen Folgeänderungen und ihrer neu geprüften Bearbeitung. Automatische Workspace-Wartung entfernte frühere Checkouts und die separate unvollständige Settings-Arbeitskopie. M1 ist aus GitHub wiederhergestellt; der alte Entwurf nicht. Keine Rekonstruktion erfinden. [Nachweisgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Browser/Unteragenten ausgeschlossen; alle vorhandenen Arbeiten erhalten.
