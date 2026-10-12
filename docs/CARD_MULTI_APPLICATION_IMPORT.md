# Gemeinsamer Import beider Kartenanwendungen

Fortsetzung am12.10.2026 im offenen PR65. Neuer Kandidat40122/Schema19, noch keine eigene Gerätefreigabe. Ausgang: vollständig grüne Reader-CI38204376573 am Kopf f1f92d69fff37eeaec5740b399d7df452b436098 und dokumentierter Checkpointd99660271726dcee413369b8fde9b61f3d5f7553. Mainace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b/40121 bleibt unverändert.

Der Reader erfasst die festen Gen1-/Gen2-AIDs in einer eingeschalteten Sitzung. Gen2V2 ist ein Format des Gen2-DF, kein drittes AID. Identität, Kapazitäten, Originaldateien, Zertifikate und Dateifehler gehören jeweils zum eigenen Anwendungssatz. MF-Dateien werden einmal vorher erfasst. Transportverlust oder Abbruch ergibt keinen erfolgreichen Batch. Keine0540-, PSO-, Schreib-, Authentisierungs- oder erweiterten APDU-Befehle.

## Neue Speicher-/Bedienintegration im Kandidaten

- Additive Migration18→19: vier neue Parent-/Anwendungs-/MF-Tabellen und zusätzliche eindeutige Indizes auf bestehenden Karten-/Run-/Sitzungspaaren. Keine historische Zeile, Quellenbindung oder Request-Fingerprint wird nachbefüllt oder geändert. Schemaexport ausschließlich durch echten Room/KSP-Compiler.
- Batch-Identität vor allen neuen Bindungs-/Quellenmutationen prüfen: genau dieselben Karten-/Inhaberfelder und vollständige gültige0520-Belege. Widerspruch, unbestätigte und fremde Karte werden nicht persistiert. Die eigene Bestätigung liest beide Anwendungen frisch; neue Bindung und gesamte Gruppe werden gemeinsam gespeichert.
- Eine äußere SQLCipher-Transaktion umfasst Parent, Bindung, beide Child-Runs/-Sitzungen, normalisierte Daten und Quellen. Initialisierung der Wiederanlauferkennung erfolgt vorher. STORAGE_FAILED oder Abbruch eines Child-Prozessors führt zur ganzen Gruppenrückrolle; unabhängige EF-/AID-Lesefehler bleiben getrennte Teilzustände.
- Neue versionierte, längenkodierte Parent-/Child-Request-Domänen. Parent-Fingerprint umfasst beide AIDs, Generationen, Status/Fehler, Child-Fingerprints und alle genauen MF-Zustände/Bytes. Wiederverwendete Requests mit geänderten Daten werden abgewiesen. Bestehende Ein-Anwendungs-Requests bleiben unverändert.
- Zusammengesetzte Foreign Keys verknüpfen Parentkarte, Child-Run, konkrete Child-Sitzung und MF-Datei/-Quelle. Eine andere Karte kann weder durch eine getrennte Session-ID noch durch eine andere Quelle eingeschleust werden.
- Generation neuer Runs kommt aus dem jeweiligen Anwendungssitzungsbeleg. Der physische Kartenschlüssel und die unveränderliche historische Kartenidentität bleiben unverändert.
- Normale Übersicht und Verlauf zeigen pro Gruppe ausdrücklich den Gen2-Child, falls dieser gelesen/identifiziert wurde; sonst den Gen1-Child. Beide Child-Belege sind im selben Vorgang einzeln auswählbar. Kein jüngerer Fallback. Tageszeiten und Fahrzeugquellen beider DFs werden nicht addiert.
- MF-Dateien bleiben am Parent. Für die Detailansicht wird ausschließlich die Quelle genau dieses Parents gelesen und ihr Hash geprüft. Alte Sitzungen bekommen keine neue MF-Projektion. Defekte Gruppen lassen andere Verlaufseinträge sichtbar. Sensible Angaben werden nach Sitzungs-/Anwendungswechsel erneut verborgen.

## Nachweise und Gerätepunkt

Die Codeintegration ist noch kein CI-/Hardwarebeweis. Neue JVM-Tests prüfen Requests, Snapshots und den Koordinator einschließlich frischer Bestätigung/Kartentausch. Neue SQLCipher-Fälle prüfen beide Anwendungen, genaue Generation und Quellen, Deduplizierung, Widerspruch/Fremd-/unbestätigte Karten, ganze Rückrolle nach dem ersten Child, getrennte Fehler, zusammengesetzte FKs und den verschlüsselten Updatepfad18→19. Neue Compose-Fälle prüfen beide tatsächlichen Verlaufsschaltflächen, fehlenden jüngeren Fallback und erneute Maskierung gemeinsamer MF-Quellen.

Vor Ausgabe: echter aktueller Compilerexport, lokale Kompilierung/Unit-Tests, komplette CI am endgültigen Produktkopf einschließlich SQLCipher/Compose, feste Testsignatur/Paketkennung und40121→40122. [Konkrete Geräteanleitung](CARD_MULTI_APPLICATION_DEVICE_TEST.md). Das ausgegebene40121-Paket wird nicht ersetzt. PR65 bleibt offen/Entwurf, kein Merge/Auto-Merge vor eigener ausdrücklicher Freigabe.

## Verbleibende Importgrenzen

7F66-Pufferinhaltsdecoder, signierte Standarddownloads/DDD, ERCA-/MSCA-Vertrauen und geschützteVU_Configuration0540 bleiben gesonderte nächste Arbeiten. [Primär-/Vertrauensabgleich](CARD_IMPORT_SECURITY_RESEARCH.md). Keine geratenen Puffergrößen, Authentisierung, Schlüssel oder Originalsignaturen. Fahrzeug-/Spesen-/Abrechnungsarbeit bleibt zurückgestellt.

Keine Browser, Unteragenten oder Bereinigung/Löschung. Actions-Budget2USD/Stop-usage unverändert. GitHub-Parent/Tree/Ref-Lease vor Updates prüfen; lokale Snapshot-Historie niemals pushen. Diese Fortsetzung übernimmt den bestehenden PR65, keine zweite Produktbranch.
