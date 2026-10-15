# FahrerTagebuch - aktueller technischer Zustand

**M1/40129/Room23 ist ausdrücklich abgenommen.** Tobi bestätigte die eigene Geräteprüfung am15.10.2026 15:13 Europe/Berlin („Passt alles 👌🏻“) und erteilte danach ausdrücklich M1-Abnahme, kontrollierte Mergefreigabe für PR88/PR89 sowie anschließende M2-Implementierungsfreigabe. Keine M2-Merge-/M3–M7-/Releasefreigabe. [Regeln](docs/CURRENT_RULES.md), [M1-Bericht](docs/VIOLATION_ENGINE_M1_IMPLEMENTATION.md), [Abnahmematrix](docs/VIOLATION_ENGINE_M1_ACCEPTANCE.md), [Gerätebeleg](docs/DEVICE_CONFIRMATION_40129.md).

## Integration und nachgewiesene Basis

- Planungs-/M0-PR88: geprüfter Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78`, erfolgreiche Docs-CI38684167403/Versuch1; zuerst integriert durch Merge `9ff0a525d8394031148aada37593c9fed8f84148`. Alle80/A–H/E01–E12/P01–P05, N01–N19 und MI01–MI24 erhalten.
- M1-PR89: ursprünglicher Produktprüfkopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, Baum `64003129407730cd1ce01822dcc07d6c777a1020`, vollständige CI38729242320/Versuch1 erfolgreich.559 Unit-Tests; vollständige API36-SQLCipher-/Compose-Suiten (151 App-/297 Datenmethoden), sieben Gradle-Gates, APK/Signatur/Updatepaar/Schemaexport. Keine Filter/Quarantäne.
- Eigene Gerätebestätigung für exakt40129/Room23 und APK-SHA256 `f5f7873e88149515829c406ce04f20dc83cb56ac478497f05cd5c077c65bc996`; keine zusätzlichen Einzelmesswerte oder weitere Hardware-/Kartenabdeckung daraus ableiten.
- Aktuell PR89 auf integrierten PR88-main abstimmen: ausschließlich Status-/Übergabedokumente konsolidieren und Nachweise ergänzen. Produktgleichheit zu bestätigtem Kopf und vollständige neue Integrations-CI sind vor PR89-Merge zu prüfen. Endgültige Integrationslauf-/Merge-IDs commitgebunden im PR-Protokoll festhalten; nicht als bereits erfolgreich voraussetzen.

## Implementiert und erhalten

M1: konsistenter lesender Snapshot des eigenen Profils/physischer Karten/aller G1-/G2-Imports, vollständige Original-/Receipt-/Signaturherkunft; reale c/p-Semantik, manuelle bekannte Nachträge versus Unknown; UTC-Intervalle nur mit echten Endbelegen, gleiche Minuten ohne Indexgewinner; Deduplizierung mit allen Quellen, Konflikte und regelbezogene Abdeckung; zeitlich versionierter isolierter Kontext/minimaler Scope-Guard; revisionsgebundener Cold Flow.34 neue Domain- und24 Androidmethoden mit MI01–MI24-Zuordnung.

Room23 ergänzt ausschließlich `violation_context_revisions`, additive Migration22→23. Compilerexport23-SHA256 `f55ad0fbe684753dfc47d4067e7002072064f8d95d70134bf4095ffbf14e5dc8`; alle45 bisherigen Entities und Exporte1–22 unverändert. Verschlüsselte18/22→23-Upgrades, komplette1→23-Kette, Inhalte/FKs/Reopen/CAS/Historie/Rollback/Cancellation/No-backfill geprüft. Keine Änderungen an Parsern, Hardware, Kryptografie oder CI/Sicherheitsregeln.

40128-Kryptografie bleibt abgeschlossen: [PR85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Merge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`, volle CI38547729369 und Produkt-main-CI38559255131 erfolgreich; [eigene Gerätebestätigung](docs/DEVICE_CONFIRMATION_40128.md). Gespeicherte0504/0505,0501/0520,0502/0503 sowie bedingtesV2-0525 mathematisch geprüft. Zertifikatsvertrauen immer NOT_EVALUATED. Keine neue Trust-/DDD-/Signaturaufnahme-/Kartenschreibphase. Frühere40124–40128 nicht erneut implementieren.

## Nächster Umfang und Grenzen

M2 nach kontrolliertem M1-Merge: D01–D06/B01–B05, tägliche/wochentliche/Doppelwochenlenkzeit, Pausen, belegte Crew-/Busregeln, historische Rechtsstände und minimaler Scope. M1-Adapter wiederverwenden; keine zweite Parserarchitektur und kein unbekanntes Intervall als Ruhe. Wochen-/Verlängerungszuordnung L02 und7-h-Konvention L03 tatsächlich nachweisen oder betroffene Resultate als bedingt/offen tragen. Nicht betroffene belegte Teile getrennt implementierbar. Keine erfundene Artikel12-Rechtfertigung oder abschließende Rechtswidrigkeit. Zwei10-h-Verlängerungen zählen je gesetzlicher Woche ohne Wochenruhe-Reset; Tagesruhereduzierungen bleiben separater M3-Vertrag.

Historische Ortszeit ist neu bevorzugte spätere M5/M6-Anzeige; M2-Resultate bleiben unabhängig. M2 wird in eigenem PR zur separaten fachlichen/technischen Abnahme, Geräteprüfung und Mergeentscheidung vorgelegt. Unabhängige qualifizierte Fachgesamtprüfung und Beta5–10 Fahrer bleiben Releasegates.

Die automatische Workspace-Bereinigung entfernte frühere Checkouts und die separate unvollständige Settings-Arbeitskopie. M1 ist aus GitHub wiederhergestellt; der alte Entwurf nicht. Keine Rekonstruktion erfinden. [Nachweisgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Browser/Unteragenten ausgeschlossen. Alle vorhandenen Arbeiten/Originale/Herkunft erhalten.
