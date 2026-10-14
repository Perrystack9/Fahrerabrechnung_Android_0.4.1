# Abschluss der mathematischen Prüfung gespeicherter Kartensignaturen

**Plan beauftragt14.10.2026 10:48 und vollständig zur Umsetzung freigegeben14.10.2026 11:20 Europe/Berlin.** Tobi bestätigt PR #84 und A1–A8. Ziel: alle bereits gespeicherten Dateisignaturen abdecken und danach diesen mathematischen Teil abschließen; anschließend Oberfläche und Funktionen für Version 1.0 planen. Regeln: [CURRENT_RULES.md](CURRENT_RULES.md).

**Abgeschlossen nach A1–A8:** neue eigene positive Geräteabnahme14.10.2026 13:47 Europe/Berlin, „Funktioniert alles wie es soll 👍“. [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85) übernommen; endgültige volle CI und exaktes Paket erfolgreich. [Technische Nachweise](CARD_STORED_SIGNATURE_MATH_EVIDENCE.md), [Gerätebeleg und tatsächliche Grenzen](DEVICE_CONFIRMATION_40128.md). Die nachstehenden Kriterien bleiben unverändert; keine weitere mathematische Implementierungsphase ableiten.

## Bestätigte Basis und Umfang

Main `cba811635a934c72c357aad23fe1c4e47a82b824`, 40127 / Room22. PR #82 und Dokumentationsabschluss #83 übernommen; endgültige volle Produkt-CI38515830418, Gerätepaket38530132281 und eigene Geräteabnahme14.10.2026 10:12 bestätigt. Zusätzlicher Produkt-Main-Lauf38533736352 jetzt vollständig erfolgreich, alle vier Jobs; Docs-main38534984439 ebenfalls grün. Keine offenen PRs beim Planstart. [Genauer 40127-Beleg](DEVICE_CONFIRMATION_40127.md).

| EF | Vorhandene Belegfamilie / Bedeutung | Generationen |
|---|---|---|
| 0504 / 0505 | Core: Tätigkeiten / Fahrzeuge, bereits mathematisch geprüft | GEN1, GEN2, GEN2V2; erhalten |
| 0501 | Identifikation: Kartenanwendung | GEN1, GEN2, GEN2V2 |
| 0520 | Identifikation: Karteninhaber und Gültigkeit | GEN1, GEN2, GEN2V2 |
| 0502 | Ereignisse | GEN1, GEN2, GEN2V2 |
| 0503 | Störungen | GEN1, GEN2, GEN2V2 |
| 0525 | Kartenanwendung V2 | ausschließlich GEN2V2, soweit tatsächlich vorhanden/aufgenommen |

Es geht um die vorhandenen Core-, Identity- und Event/Fault-Receipts, nicht neue Signaturaufnahmen. Maximal13 Belege auf einer Karte mit GEN1 und GEN2V2. Fehlende, fehlgeschlagene oder nicht versuchte Aufnahme bleibt sichtbar; alte unsigned Imports erhalten keinen nachträglichen Beleg. 0525 ist auf GEN1/reiner GEN2 nicht vorgesehen und wird dort nicht als Fehler ausgegeben.

## Technische Umsetzung nach Freigabe

1. **Gemeinsamen Rechenkern nutzen.** Die geprüfte 40127-Engine für Gen1-C108→C100-Recovery plus striktes PKCS1-v1.5/SHA1 und für sechs feste Gen2/V2-ECDSA-Domänen wiederverwenden. Ein gemeinsamer eingefrorener Prüfinput und Ergebnis mit explizitem EF/Belegfamilie; keine Identitäts-/Ereignisbytes als Core-Snapshot tarnen. Bestehende Core-API und Ergebnisse über kompatiblen Adapter erhalten. Policy ausschließlich aus dem zugehörigen gespeicherten Kartenzertifikat; kein Card_MA-/Ausstellerkurvenersatz, kein Providerwechsel oder neuer Schlüsselkatalog.
2. **Vorhandene Stores lesend anbinden.** Ein konsistenter Read-Snapshot der ausgewählten eigenen Karte/Child-Sitzung mit Core-, Identity- und Event/Fault-Receipts. Die bisherigen load/decode-, Fingerprint-, FK-, Profil-/Parent-/Child-/Generations-/Bootstrap-Prüfungen bleiben bestehen. Für jeden EF ausschließlich sein tatsächlich referenziertes vollständiges Original, seine Signatur und sein Kartenzertifikat; Gen1-C108 nur aus demselben Child. Ergebnis bindet Karte, Sitzung/Batch, Anwendung/Generation, EF/Familie, Receipt-Fingerprint, Quellen-IDs und Original-/Signatur-/Zertifikats-/Ausstellerdigests. Defensive Kopien, Rechnen außerhalb der Transaktion auf Default-Dispatcher, Cancellation weiterreichen.
3. **Vorhandene Anzeigen ergänzen.** In Kartendaten, Fahrerkarten-Details/Identifikationsquellen und Ereignisse/Störungen je Datei denselben getrennten Aufnahme-, Mathematik- und Vertrauensstatus darstellen. Endergebnis gültig/ungültig/nicht prüfbar mit Grund; Vertrauen stets NOT_EVALUATED bzw. „nicht geprüft“. Quellen-/EF-/Anwendungswechsel, Maskierung, Altarchiv und späte Ergebnisse absichern. Keine Gesamtanzeige „alles vertrauenswürdig“; keine neue Navigation.
4. **Als 40128 abnehmen.** Room22, Tabellen, persistierte Kinds, alte Receipt-/Request-/Original-/Certificate-Fingerprints und CAPTURED_UNVERIFIED unverändert lassen; keine Migration, DB-Schreiboperation, Cache oder Backfill. Vollständige unveränderte Produkt-CI, exaktes signiertes Updatepaket über40127 und neue eigene Geräteabnahme vor Merge. Anschließend Abdeckung und tatsächliche Nachweisgrenzen dokumentieren und den mathematischen Umfang schließen.

0501 bleibt bytegenau: GEN1 10 Bytes, GEN2 aktuell17 oder tatsächlich unterstützte vollständige15-Byte-Quelle; GEN2V2-Capture17 Bytes. Historische unsigned15-Byte-V2-Quelle nicht ergänzen/aufwerten. 0520 bleibt143 Bytes, 0525 bleibt tatsächlich gespeicherte vollständige10-Byte-Quelle. 0502/0503 einschließlich unbenutzter/leerer Slots aus dem eigenen Original prüfen, nie aus fachlichen Listen rekonstruieren. Die vorhandenen Aufnahme-/Parserregeln bleiben unverändert; mathematische Gültigkeit bewertet keine fachliche Richtigkeit der Einträge.

## Abnahmekriterien

| ID | Nachweis |
|---|---|
| A1 — vollständige Zuordnung | Jeder neue EF in jeder unterstützten Generation mit eigener gültiger Signatur geprüft; 0525 nurV2. Alle tatsächlich gespeicherten positiven Receipts der drei Familien erreichen denselben Rechenkern. 0504/0505-Regressionsabnahme aus40127 bleibt grün. |
| A2 — korrekte Mathematik | Unabhängig mit Python/OpenSSL erzeugte feste Positiv-/Negativvektoren je neuem EF; Bitänderung im signierten Original, falscher Schlüssel und vertauschte Signatur werden abgewiesen. Vorhandene strikten RSA-/Recovery- und alle sechs EC-Domänenprüfungen einschließlich R/S-Grenzen, Doppelhash, High-S und Plain-30-Präfix unverändert erhalten. Keine DER-/Padding-/Modulo-/Quellenrettung. |
| A3 — exakte Herkunft | Karten-/Sitzungs-/Anwendungs-/EF-/Familien-/Original-/Zertifikatstausch, identische deduplizierte Rohbytes mit unterschiedlichen Receipts sowie beschädigte Fingerprints geprüft. Bestehende Integritätsablehnungen erhalten; keine neuere Quelle oder unsigned Fallback für einen signierten Beleg. |
| A4 — alte und bedingte Daten | Fehlende/fehlgeschlagene/nicht versuchte Aufnahme, fehlendes Zertifikat oder unbekannter Gen1-Einstieg verständlich nicht prüfbar. Vollständige gültig leere Ereignis-/Störungsdateien und echte unterstützte Gen2-Layouts geprüft. Alte unsigned Quellen und nicht vorgesehene0525 unverändert, ohne falsches positives Ergebnis. |
| A5 — getrenntes Vertrauen | Für jedes gültige, ungültige und nicht prüfbare mathematische Ergebnis bleibt Zertifikatsvertrauen NOT_EVALUATED. Abgelaufene/unauthentifizierte Zertifikate dürfen die vorhandene Trennung nicht aufheben; keine neue Vertrauenskette oder implizite Zertifikatsfreigabe. |
| A6 — Daten und Oberfläche | Tatsächliche SQLCipher-Zeilen vor/nach Prüfung, Wiederholung und Reopen identisch; alte Fingerprints/Originale/Receipts/Room22 erhalten. Compose-Prüfungen für EF-/Sitzungs-/Anwendungswechsel, späte Ergebnisse, Maskierung und Altarchiv; Aussagen gehören ausschließlich zur aktuellen Auswahl. |
| A7 — vollständige CI | Alle bisherigen sieben Gradle-Aufrufe, Unit/Lint/APK-/Testsignatur-/Updatepaar-/Compiler-Schemaprüfungen, vollständige API36-SQLCipher-/Compose-Suiten und Abschlussgate grün. Keine Testfilter/Quarantäne, Sicherheits-/Migrations-/Workflow-/Upload-/Budgetänderung. |
| A8 — neue eigene Geräteabnahme | Exaktes40128-Update über40127 ohne Datenlöschung; zuerst gespeicherte signierte Übernahme ohne Leser mit allen angebotenen Dateien/Anwendungen, alte unsigned Einträge, Wechsel/Reopen/übrige Ansichten, regulärer Import mit erhaltener Historie und Kartenentnahme-/Neuimportfall. Eindeutige positive Rückmeldung vor Merge; unerwartete INVALID vorher aufklären. Anleitung immer verständlich direkt im Chat, tatsächliche Größen/Ergebnisse und Grenzen festhalten. |

Nach A1–A8 ist **die mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen abgeschlossen**. Zulässige fehlende Voraussetzungen bleiben begründet nicht prüfbar; Abschluss bedeutet keine universelle Hardware-/Kurven- oder Vertrauensfreigabe. Der anschließend zu planende Produktschwerpunkt ist die Benutzeroberfläche und der vereinbarte Funktionsumfang für1.0. Zertifikatsvertrauen, DDD/Export, zusätzliche Signaturaufnahme und Karten-Schreibvorgänge sind nicht Bestandteil dieses Abschlussmeilensteins.

## Geprüfte Codebasis

- [40127-Verifikator](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureVerifier.kt) und [Quellen-/Ergebnisvertrag](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureVerification.kt): bestehenden mathematischen Kern und Trennung erhalten.
- [Core-Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardCoreSignatureStore.kt), [Identity-Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardIdentitySignatureStore.kt), [Event/Fault-Store](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardEventFaultSignatureStore.kt) und [Import-Repository](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCardImportRepository.kt): vorhandene eigene Referenzen und Integritätsprüfungen wiederverwenden.
- [Identifikationsaufnahme](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md), [Ereignis-/Störungsaufnahme](CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md) und [40127-Mathematikbelege](CARD_CORE_SIGNATURE_MATH_EVIDENCE.md) sind abgeschlossen; nicht erneut implementieren. Die vorhandenen belegten Formate/Policies bleiben Basis, keine neue Kryptospezifikation in diesem Plan.
