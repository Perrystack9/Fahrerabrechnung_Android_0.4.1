> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Gemeinsamer Import beider Kartenanwendungen

Implementiert und abgeschlossen mit PR #65: 40122 / Schema 19. Der [aktuelle Status](../WORK_STATUS.md) nennt geprüften Kopf und bestätigten Geräteumfang.

Der Reader erfasst die festen Gen1-/Gen2-AIDs in einer eingeschalteten Sitzung. Gen2V2 ist ein Format des Gen2-DF, kein drittes AID. Identität, Kapazitäten, Originaldateien, Zertifikate und Dateifehler gehören jeweils zum eigenen Anwendungssatz. MF-Dateien werden einmal vorher erfasst. Transportverlust oder Abbruch ergibt keinen erfolgreichen Batch. Keine0540-, PSO-, Schreib-, Authentisierungs- oder erweiterten APDU-Befehle.

## Implementierte Speicher-/Bedienintegration

- Additive Migration18→19: vier neue Parent-/Anwendungs-/MF-Tabellen und zusätzliche eindeutige Indizes auf bestehenden Karten-/Run-/Sitzungspaaren. Keine historische Zeile, Quellenbindung oder Request-Fingerprint wird nachbefüllt oder geändert. Schemaexport ausschließlich durch echten Room/KSP-Compiler.
- Batch-Identität vor allen neuen Bindungs-/Quellenmutationen prüfen: genau dieselben Karten-/Inhaberfelder und vollständige gültige0520-Belege. Widerspruch, unbestätigte und fremde Karte werden nicht persistiert. Die eigene Bestätigung liest beide Anwendungen frisch; neue Bindung und gesamte Gruppe werden gemeinsam gespeichert.
- Eine äußere SQLCipher-Transaktion umfasst Parent, Bindung, beide Child-Runs/-Sitzungen, normalisierte Daten und Quellen. Initialisierung der Wiederanlauferkennung erfolgt vorher. STORAGE_FAILED oder Abbruch eines Child-Prozessors führt zur ganzen Gruppenrückrolle; unabhängige EF-/AID-Lesefehler bleiben getrennte Teilzustände.
- Neue versionierte, längenkodierte Parent-/Child-Request-Domänen. Parent-Fingerprint umfasst beide AIDs, Generationen, Status/Fehler, Child-Fingerprints und alle genauen MF-Zustände/Bytes. Wiederverwendete Requests mit geänderten Daten werden abgewiesen. Bestehende Ein-Anwendungs-Requests bleiben unverändert.
- Zusammengesetzte Foreign Keys verknüpfen Parentkarte, Child-Run, konkrete Child-Sitzung und MF-Datei/-Quelle. Eine andere Karte kann weder durch eine getrennte Session-ID noch durch eine andere Quelle eingeschleust werden.
- Generation neuer Runs kommt aus dem jeweiligen Anwendungssitzungsbeleg. Der physische Kartenschlüssel und die unveränderliche historische Kartenidentität bleiben unverändert.
- Normale Übersicht und Verlauf zeigen pro Gruppe ausdrücklich den Gen2-Child, falls dieser gelesen/identifiziert wurde; sonst den Gen1-Child. Beide Child-Belege sind im selben Vorgang einzeln auswählbar. Kein jüngerer Fallback. Tageszeiten und Fahrzeugquellen beider DFs werden nicht addiert.
- MF-Dateien bleiben am Parent. Für die Detailansicht wird ausschließlich die Quelle genau dieses Parents gelesen und ihr Hash geprüft. Alte Sitzungen bekommen keine neue MF-Projektion. Defekte Gruppen lassen andere Verlaufseinträge sichtbar. Sensible Angaben werden nach Sitzungs-/Anwendungswechsel erneut verborgen.

## Prüfbelege bei Bedarf

Reader/JVM-, SQLCipher-/Room- und Compose-Prüfungen des endgültigen Produktkopfs sind vollständig grün; der explizite Geräteumfang ist in [DEVICE_CONFIRMATION_40122.md](DEVICE_CONFIRMATION_40122.md) dokumentiert. Historische Kandidaten-/Retry-/Paketzustände bleiben in [CARD_MULTI_APPLICATION_CHECKPOINT_40122.md](CARD_MULTI_APPLICATION_CHECKPOINT_40122.md); keine neuen Einzeltests daraus ableiten.

## Verbleibende Importgrenzen

7F66-Pufferinhaltsdecoder, signierte Standarddownloads/DDD, ERCA-/MSCA-Vertrauen und geschützteVU_Configuration0540 bleiben gesonderte nächste Arbeiten. [Primär-/Vertrauensabgleich](CARD_IMPORT_SECURITY_RESEARCH.md). Keine geratenen Puffergrößen, Authentisierung, Schlüssel oder Originalsignaturen. Fahrzeugauswertung bleibt eine spätere Arbeit; Spesen, Lohn/Zuschläge und Abrechnung entfallen vollständig als Produktziele.

Für Folgeänderungen gelten die aktuellen CI-/Hardware-/Git-Regeln. Der abgeschlossene PR #65 wird nicht neu aufgebaut.
