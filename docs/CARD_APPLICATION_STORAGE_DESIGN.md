> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: DUPLIKAT.** Abgeschlossener Speicherentwurf; aktuelle Implementierung in CARD_MULTI_APPLICATION_IMPORT.md. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); verbindlich sind [CURRENT_RULES.md](CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

Aktueller Kandidat12.10.2026: Die folgende Modellentscheidung ist in PR65 als40122/Schema19 umgesetzt; neue Prüfungen/Hardwarefreigabe stehen noch aus. [Aktuelle Integration und genaue Grenzen](CARD_MULTI_APPLICATION_IMPORT.md). Die Modellentscheidung darunter beschreibt den ursprünglichen Plan.

---

# Gemeinsamer Kartenlesevorgang – additive Speicherintegration

Modellentscheidung12.10.2026 auf dem Reader-Fundament von PR65 (`f1f92d69fff37eeaec5740b399d7df452b436098`). **Dieser Text beschreibt die nächste Integration, keine bereits vorhandene Schema19-/Speicherfunktion.** Die normale App liest/speichert weiterhin eine ausgewählte Anwendung. [Reader-Umfang und Grenzen](CARD_MULTI_APPLICATION_IMPORT.md).

## Relationen und unveränderliche Herkunft

| Beleg | Schlüssel | Herkunft / verpflichtende Grenze |
|---|---|---|
| Gemeinsamer Lesevorgang | Parent-Request-ID | Genau eine bereits bestätigte physische DriverCard; versionierter vollständiger Request-Fingerprint |
| Anwendungsversuch | Parent-ID + festes AID | Eigene Generation, Status, Fehlercode; eigener Child-Run-/Sitzungsbeleg, falls identifiziert |
| Kartenweiter Originalbeleg | Karte + MF-EF + Originalbytes | Einmalige Erfassung vor AID-Auswahl; unabhängiger unveränderlicher Rohbeleg |
| MF-Beleg im Lesevorgang | Parent-ID + MF-EF | Konkreter Rohbeleg oder expliziter Fehl-/Nichtversuchszustand, kein jüngerer Fallback |
| Anwendungsquelle | bisheriger Child-ImportSession-/Quellenbeleg | Eigene0501-/0520-/0525-, Zertifikats- und Fachquellen; keine MF-/DF-Überlagerung |

Die physische Karten-ID und der bestehende Karteninstanzschlüssel bleiben unverändert. Anwendungsgeneration gehört in den Sitzungsbeleg, nicht in eine zweite künstliche DriverCard. Der bestehende `DriverCardIdentityEntity.generation` ist unveränderliche historische Identifikation der physischen Bindung und darf für neue Child-Runs nicht als Anwendungsbeleg benutzt werden.

Neue zusammengesetzte Foreign Keys müssen Parent-/Child-/Quellenkarte zusammen erzwingen. Dazu ggf. additive eindeutige Indizes auf bestehenden `(id,driverCardId)` beziehungsweise `(sessionId,driverCardId)`-Paaren ergänzen; echte Room-Exporte überprüfen. Eine isolierte FK auf eine Session-ID plus separate Karte-FK genügt nicht, da sie eine Sitzung einer anderen Karte nicht verhindert. Kartenweite Belege brauchen entsprechend eindeutige `(sourceId,driverCardId,fileId)`-Zuordnung. Historische Entities/Zeilen/Fingerprints nicht rückwirkend umschreiben.

## Ein Speicherabschluss, keine halbe erfolgreiche Gruppe

1. Unveränderlichen Batch prüfen: alle erwarteten AID-Versuche explizit, vollständige gültige0520-Belege je identifiziertem DF, genaue Übereinstimmung der Karten-/Inhaberfelder. Widerspruch/Nichtfahrer-Mischung/unbelegte Identität sperren die Gruppe vor Mutation. Ein fehlendes/unlesbares AID ist getrennt vom EF-Fehler zu erfassen.
2. Bestehende Profilregeln prüfen. Erste eigene und unklare Ersatzkarten benötigen weiterhin Bestätigung; Bestätigung liest beide Anwendungen frisch. Fremdkarten bleiben ausschließlich temporär. Profil-/Kartenbindung darf nicht schon vor der Batch-Identitätsprüfung schreiben.
3. Vorhandene Wiederanlauf-/Run-Initialisierung außerhalb der gemeinsamen Speichertransaktion abschließen. Sonst könnte deren prozessweiter Initialisierungsstatus bei einer äußeren Rückrolle schon wahr sein, obwohl die Wiederanlaufdaten zurückgerollt wurden.
4. Innerhalb **einer** SQLCipher-Transaktion: physische Bindung, Parent, Anwendungsversuche, sämtliche Child-Runs/-Sitzungen, unveränderliche Rohbelege/Quellenverweise, Normalisierung und Endstatus. Reader-IO ist davor abgeschlossen und findet nicht in der DB-Transaktion statt.
5. Ein unabhängiger EF-/AID-Lesefehler erlaubt einen expliziten Teilstatus für den ganzen eigenen Vorgang. `STORAGE_FAILED`/Abbruch eines Child-Prozessors darf dagegen nicht als gewöhnlicher Teilimport übernommen werden: gesamte neue Gruppe zurückrollen. Ein nach erfolgreichem ersten Child auftretender Speicherfehler darf dessen Quellen nicht allein stehen lassen.
6. Wiederholung desselben Parent-Requests: denselben vollständigen Fingerprint verlangen und nur den bestehenden exakten Gruppenbeleg öffnen. Andere Originals/Fehlerzustände/Generationen/AIDs/MF-Zustände müssen eine Wiederverwendung ablehnen, statt Quellen nachzufüllen. Alte Ein-Anwendungs-Requests behalten ihr altes Verhalten und ihre alten Fingerprints.

Child-Request-IDs deterministisch aus einer eigenen Versionsdomäne, Parent-ID und vollständigem AID ableiten; längenkodierte UTF8-Felder verwenden, keine abgeschnittenen IDs oder uneindeutigen NUL-Concatenations. Neue Anwendungs-Fingerprints ausdrücklich von der alten Ein-Anwendungs-Domäne abgrenzen. Kartenweite erwartete Dateiresultate gehören zum Parent; sie dürfen nicht in jedem Child als fehlende Quellen gemeldet werden.

## Auswahl und Zeitangaben

Normaler Einstieg bevorzugt den konkreten Gen2-Child desselben Vorgangs, wenn er identifiziert ist. Ein gescheiterter Gen2-Versuch bleibt dabei sichtbar. Exakte Gen1-/Gen2-Auswahl in den Karten-/Importdetails; keine Suche nach einer jüngeren passenden Quelle.

Die normale Übersicht/`latest()` muss Child-Sitzungen gruppieren und den festgelegten Primärbeleg verwenden. Die derzeitige Sortierung gleicher Zeitstempel nach Session-ID darf die Anwendung nicht zufällig bestimmen. Frühere ungruppierte Sitzungen behalten ihre damalige Ansicht. Eine Parent-MF-Sitzung darf nicht versehentlich die letzte Anwendungsübernahme werden.

Identische normalisierte Kartentage derselben physischen Karte dürfen bestehende Content-Deduplizierung teilen, **ihre getrennten Original-/Sitzungsbelege müssen erhalten bleiben**. Unterschiedliche Tagesquellen beider Anwendungen nicht addieren, überschreiben oder als vollständige kombinierte Zeitabdeckung ausgeben. Auswahl/Navigationszustand muss auch bei identischen Bytes auf den konkreten Parent/Child/Snapshot reagieren und sensible Angaben erneut verbergen.

## Pflichtnachweise vor Aktivierung

- Echter KSP-/Room-Export, ausschließlich additive Migration18→Folgeschema, validierter Updatepfad von40121/Schema18. Keine manuell erzeugte Schema-Datei und kein destruktiver Ersatz.
- Reader/JVM: stabile IDs/Fingerprints, neue/alte Request-Domänen, exakte Anwendungen, individuelle Kapazitäten, Abbruch/Transportverlust, eingefrorene Maps während Kartenprüfung und frische Bestätigung.
- SQLCipher: gleiche physische Karten-ID; unterschiedliche Original-/Zertifikatsbelege derselben Art; zusammengesetzte Karten-/AID-/Session-FKs; atomare Rückrolle nach dem ersten Child; Fremd-/unbestätigte/widersprüchliche Karte ohne neue Zeilen; Deduplizierung/Request-Wiederholung; unveränderte alte Sitzungen; verschlüsselte Wiederöffnung beider DFs und Parent-MF-Belege.
- Compose/AppRoot: normale Gruppenanzeige und alte Ein-Anwendungs-Historie; beide exakten Anwendungsdetails; fehlender/unlesbarer DF getrennt; kein jüngerer Fallback; gleicher Quellenfingerprint mit verschiedenen Sessions; Maskierung nach Parent-/Child-Wechsel; keine doppelte Zeitsumme.
- Endgültigen Produktkopf vollständig CI-prüfen. Erst danach steigende Testversion und signiertes Paket mit fester Testkennung/Zertifikat und konkreter Anleitung ausgeben. Kein Merge/Auto-Merge vor eigener ausdrücklicher Hardwarebestätigung.

Keine Browser/Unteragenten/Bereinigung, keine Fahrzeug-/Spesen-/Abrechnungsarbeit, Actions-Budget2USD/Stop-usage unverändert. Ein Docs-Checkpoint mit dem unbestätigten PR65-Code als Vorfahr ist **kein** reines nach Main mergebares Dokumentationsupdate.
