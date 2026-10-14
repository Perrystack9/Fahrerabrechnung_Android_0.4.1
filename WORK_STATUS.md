# FahrerTagebuch - aktueller technischer Zustand

**40126 / Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #79](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/79), Produktmerge `6334b4f67485feaee4de5d649d6788475af940ec`, identischer vollständiger Baum `88ce8cdc6f62a71bcf0bc37ca38c447eb765ea6e` zum geprüften Kopf `e4b70aec0b555c340e45df193c4f3b93bccc8770`. Aktuellen main, offene PRs und CI live prüfen. Regeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und erhalten

- Automatischer eigener Vordergrund-Import, Profil-/Kartenbindung, unabhängige Anwendungen, MF am Parent, exakte Originale/Historie/Fingerprints, atomare SQLCipher-Speicherung und Keystore-Schutz erhalten. Beide Zeitsätze nicht addieren.
- Vier Core-Belege aus 40124 und fünf mögliche Identifikationsbelege aus 40125 erhalten: 0504/0505 sowie 0501/0520 in beiden Anwendungen, 0525 nur V2. Alte Core-/Identitätskinds, Tabellen und Digests unverändert.
- Neu: Events 0502 und Faults 0503 in GEN1 und GEN2/GEN2V2, eigene Rohkinds EVENTS_ORIGINAL/FAULTS_ORIGINAL und separate card_event_fault_signature_receipts. Vier zusätzliche mögliche Belege auf üblicher G1+G2V2-Karte; keine dritte AID.
- Zusammenhängend SELECT/HASH(P2=00)/frisches vollständiges READ/strikte EOF/SIGN. Nur datenloses 6700/6B00 beweist das Ende; keine Le-Retries für HASH/EOF/SIGN. Reale C101/Card_Sign-Policy und Länge, keine Ersatzpolicy oder pauschale 64-Byte-Annahme.
- Vollständige Rohbytes unabhängig von EMPTY/INVALID-Projektion erhalten, fachliche Ansicht nur bei interpretierbaren Daten. Fehlend, nicht versucht, Lesefehler und gültig leer unterscheiden. Bootstrap-Kapazitäten bei Aufnahme und historischem Load prüfen; keine erfundenen Einträge.
- Lokale HASH-/SIGN-Fehler erhalten unabhängig vollständig bewiesene unsigned Originale; unbewiesene/fehlgeschlagene neue READs erlauben keinen vollständigen Original-FK. Transportverlust/Cancellation verwerfen die gesamte laufende Gruppe, auch über die eigenständige Reader-API.
- Konkrete Child-/Karten-/Original-/Zertifikats-FKs, eigener Digest, defensive Maps/Bytekopien und Transaktion. Exakte alte Sitzungen/Originale erhalten; keine nachträglichen Signaturen, Backfill oder jüngerer Quellenersatz. Leere neue Map verändert alte Requests nicht; alte Request-/Batch-IDs dürfen keine neuen Belege erhalten.
- Additive verschlüsselte 21→22: eine neue leere Receipt-Tabelle mit eigenen Indizes. Alle 44 Tabellen von Schema 21 und Compilerexporte1–21 unverändert. Eigenständige vollständige 19→20-/20→21-Abnahmen erhalten, zusätzlich 21→22 und echte verschlüsselte historische Upgrades bis 22.
- Bestehende Ereignis-/Störungsansicht zeigt Originalgröße, ungeprüften Status und reale Signaturlänge/Grund. Altimporte ohne neue Belege. Quellen-/Sitzungs-/Anwendungs-/Archivwechsel maskieren sensible Nummern erneut und leeren die neue Auswahl. Kein UI-Umbau.

## Nachweise für 40126

| Beleg | Ergebnis |
|---|---|
| Vollständige Produkt-CI | [38444430958](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38444430958), Versuch 1, alle vier Jobs erfolgreich für `e4b70aec0b555c340e45df193c4f3b93bccc8770` |
| Exaktes Gerätepaket | [38448369846](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38448369846), Versuch 1, gleicher Produktkopf; Infrastruktur `3c90e97cd598afb7f8b66344dc049b195898cd31`, include_baseline=false |
| Neue eigene Gerätebestätigung | Tobi, 13.10.2026 20:59 Europe/Berlin: „Hab alles getestet.“ nach sämtlichen neun Chat-Testpunkten; keine Auffälligkeit gemeldet |
| Sichtbarer Screenshot | Events 3168 / Faults 1152 Originalbytes; jeweils 64-Byte-Signatur, aufgenommen · ungeprüft; Störungsliste gültig leer, Anwendungsbezeichnung nicht sichtbar |
| Produktmerge | `6334b4f67485feaee4de5d649d6788475af940ec`; vollständiger identischer CI-/Gerätebaum |
| Zusätzlicher Produkt-Main-Lauf | [38451502155](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38451502155), Versuch 1, vollständig erfolgreich für Produktmerge; alle vier Jobs |

Alle sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Compiler-Schemaabgleich, ungekürzte SQLCipher-/Compose-Suiten API 36 und Abschlussgate bestanden. Reader-/Format-/Policy-/Grenz-/Transport-/Freeze-, Herkunfts-/Tamper-/Dedup-/Request-/Historien-, erster Kartenbindungs-/Gruppenrollback- und UI-Abnahme. Keine Filter/Quarantäne oder Workflow-/Upload-/Budgetänderung; erfolgreiche normale Produkt-CI ohne Artefakte. Lokaler Gradle-Download blockiert; Android-Nachweis aus echter CI.

Room22 ausschließlich aus echtem CI-Compilerexport: SHA256 `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. Archiv/ZIP/APK-Prüfsummen, BUILD_INFO, Manifest VersionCode 40126 und feste Testsignatur nach Download tatsächlich geprüft. [Genauer Geräte-/Paketbeleg](docs/DEVICE_CONFIRMATION_40126.md); [umgesetzter Plan](docs/CARD_EVENT_FAULT_SIGNATURE_CAPTURE_PLAN.md).

Die neue Gesamtabnahme deckt Tobis tatsächlich getestete Karte-/Leser-Kombination ab. Einzelmesswerte wurden nur für den sichtbaren Screenshot geliefert; keine zusätzliche Generation, installierte Versionsanzeige, Hardwaremodelle oder allgemeine Kurvenabdeckung behaupten. Aufnahme bleibt UNVERIFIED; keine mathematische Signatur-/Trust-/DDD-/Schreibfreigabe.

## Erhaltene Vorgänger und offene Arbeit

40125/Schema 21 aus PR #76 (`ae00f9699fe7ef7ab3082d9a1164b8cca185561d`) und 40124/Schema20 aus PR #73 bleiben in ihrem jeweils bestätigten Umfang erhalten. Die bestehenden Quellen, Parser, Kartenbindung, Historie und Sicherheitsprüfungen nicht neu aufbauen. [40125-Beleg](docs/DEVICE_CONFIRMATION_40125.md), [40124-Beleg](docs/DEVICE_CONFIRMATION_40124.md) nur bei konkretem Bedarf.

**Neuer Planauftrag, noch keine Implementierungsfreigabe:** mathematische Verifikation vorhandener 0504/0505-Core-Signaturen, Gen1 und Gen2/Gen2V2. [Technischer Plan und Abnahmekriterien](docs/CARD_CORE_SIGNATURE_VERIFICATION_PLAN.md). Reine abgeleitete Prüfung der exakten gespeicherten Original-/Receipt-/Zertifikatsquellen; Gen1-Schlüsselrückgewinnung und Datensignatur getrennt, alle sechs Gen2-Policies. Zertifikatsvertrauen bleibt NOT_EVALUATED. Keine neue Aufnahme, Migration oder veränderte Fingerprints; Produktbasis 40126/Room22 bis zur Freigabe unverändert. 0502/0503-Arbeit/Paket/Geräteabnahme abgeschlossen. Weitere EFs, vollständige ERCA/MSCA-Vertrauensprüfung, Originalexport/DDD/Standarddownload, Schreibpfad, 7F66/0540 und Oberflächenneugestaltung getrennt. Keine Spesen-/Lohn-/Abrechnungs-/Heimatstandortphase.

Gerätetest-Anleitungen immer verständlich direkt im Chat, Dokumente/ZIP als Ergänzung. Browser/Unteragenten ausgeschlossen. Ursprüngliche unvollständige Settings-Arbeit erhalten und separat gesichert; lokale Branches/Checkout in der [Übergabe](docs/WORK_HANDOFF.md). Reiner Docs-Abschluss [PR #80](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/80) verändert keinen Produktcode, Test, Schema oder eingebetteten App-Asset; unverändertes Dokumentationsrouting.
