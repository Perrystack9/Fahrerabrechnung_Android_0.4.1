# 0502/0503 — begrenzte neue Signaturaufnahme

**Abgeschlossen:** Tobi hat am 13.10.2026 den vorgeschlagenen 0502/0503-Meilenstein freigegeben und anschließend alle neun Chat-Gerätefälle als getestet zurückgemeldet. [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79) übernommen, Merge `6334b4f67485feaee4de5d649d6788475af940ec`, identischer Baum zum exakten CI-/Gerätekopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`. Volle CI 38444430958 und Paket 38448369846 erfolgreich; [eigener Gerätebeleg](DEVICE_CONFIRMATION_40126.md). Umsetzung nicht wiederholen. Ursprüngliche Basis main `3c90e97cd598afb7f8b66344dc049b195898cd31`, bestätigtes 40125/Schema 21. [Regeln](CURRENT_RULES.md) und [Primärmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) gelten weiterhin.

## Umfang

Neue eigene Aufnahme von Events_Data 0502 und Faults_Data 0503 in GEN1 und GEN2/GEN2V2. V2 ist weiterhin dieselbe GEN2-Anwendung. Erwartete Größen ausschließlich aus den eingefrorenen, bereits konsistent geprüften 0501-Kapazitäten und dem vorhandenen Parser: Events 6 Gruppen in G1 bzw. 11 in G2/V2, Faults 2 Gruppen, jeweils 24-Byte-Slots; die bisherigen gültigen Kapazitäten bleiben unverändert. Keine dritte Anwendung, neue fachliche Interpretation, Navigationsphase, Trust-, DDD-, Export- oder Schreibfunktion.

## Aufnahme und Herkunft

Je EF zusammenhängend SELECT → HASH mit P2=00 → frisches vollständiges READ ab Offset 0 → strikte EOF-Prüfung → SIGN mit der bereits belegten Policy. Nur datenloses 6700/6B00 beweist das Ende; keine Le-Retries für HASH/EOF/SIGN. Zertifikatsbytes derselben Anwendung vorher einfrieren; G1 RSA/SHA1/128, G2/V2 nur echte C101/Card_Sign-Policy, keine Aussteller-/Card_MA-Ersatzpolicy. Core-/Identitätssequenzen und deren Receipts unverändert.

Vollständigkeit und fachliche Interpretation trennen: vollständige neue 0502/0503-Rohbytes unabhängig von EMPTY/INVALID-Projektion erhalten. Eigene Rohkinds `EVENTS_ORIGINAL`/`FAULTS_ORIGINAL` in vorhandenen verschlüsselten Metadatenquellen mit eigenem Fingerprint; keine neue Zuordnung historischer Ereignisquellen. Gültig leere Dateien behalten vollständige Slots und null fachliche Einträge. Ungültige Kapazität: nicht versucht; fehlende EF, fehlgeschlagenes READ, unbewiesenes Ende und nicht interpretierbares vollständiges Format getrennt. Kein positiver Signaturbeleg bei unbewiesener Grenze/Format. Zusätzliche Bytes oder fehlgeschlagenes READ liefern keinen vollständigen Original-FK.

Lokaler HASH-/SIGN-Fehler darf das unabhängig vollständig bewiesene unsigned Original erhalten; kein SIGN nach fehlgeschlagenem HASH. Transportverlust und Cancellation verwerfen die gesamte laufende Gruppe in beiden Reader-APIs. Keine Wiederaufnahme mit altem Hash. Alle Eingaben und veröffentlichten Signatur-/Originalbytes defensiv kopieren.

## Persistenz und Historie

Eigene `eventFaultSignatures`-Map und eigene Tabelle `card_event_fault_signature_receipts`, kein Erweitern alter Core-/Identitätskinds oder Digests. Exakte Karten-/Child-/Original-/Zertifikats-FKs durch die bestehenden kombinierten Metadatenquellenindizes. Rohquelle und Receipt in derselben Importtransaktion. Original-/Receipt-/Policy-/Status-/Anwendungs-/Bytebindung vor insert und beim historischen load validieren; kein jüngerer Quellenersatz. Leere neue Map ändert alte Requests und Abdeckungsberichte nicht; alte Request-/Batch-IDs dürfen keine neuen Belege aufnehmen.

Migration 21→22 ausschließlich additiv: eine leere Receipt-Tabelle samt eigenen Indizes. Alle bisherigen Tabellen und Compilerexporte 1–21 unverändert, kein Backfill. Produktions-SQLCipher/Keystore erhalten; Schema 22 ausschließlich vom echten Room-Compiler. Historische 19→20- und20→21-Abnahmen unverändert erhalten; zusätzlich21→22 und verschlüsselte historische Upgradepfade prüfen.

## Anzeige und Abnahme

Bestehende Ereignis-/Störungsansicht lediglich um Originalgröße, aufgezeichneten ungeprüften Signaturstatus, Länge/Grund ergänzen. Exakte ausgewählte Sitzung/Anwendung/Altarchiv, kein globaler neuester Beleg. Alte Imports ausdrücklich ohne neu aufgenommene Signaturen. Keine technischen Bedienknöpfe oder Rohdatenlogs.

Automatisiert: beide EFs/alle Generationen; gültige Kapazitäten, vollständige leere Slots und unbekannte Felder; Reihenfolge, Chunkgrenzen, fehlende EF, kurze READs, Zusatzbyte, EOF-SWs, alle vorhandenen Card_Sign-Kurven, falsche Zertifikatsrolle/Generation, HASH-/SIGN-SWs und Fehler ohne SW; Transport/Cancellation, defensive Maps; gleiche Child-FKs, Tamper/Dedup/alte Request-ID, Gruppenrollback nach zweitem Child; additive echte verschlüsselte Migration/Reopen/Historie; Compose-Auswahlwechsel und Altimport. Unveränderte vollständige Android-CI und eigene Geräteabnahme für den endgültigen Kopf vor Merge.

Version 40126, Geräteupdate über vorhandenes 40125, exaktes Paket über den bestehenden Workflow. Checkliste hält tatsächliche Originalgrößen/Signaturlängen und den eigenen getesteten Umfang fest. Bis zur neuen positiven Gerätebestätigung kein Produktmerge und keine Trust-/Hardware-Vollständigkeitsbehauptung.
