# FahrerTagebuch - aktueller technischer Zustand

**Testversion 40122 / Room-Schema 19.** Matrix-Basis am 13.10.2026: main nach gemergtem PR #68 (`6190efa38762749a165922820b75ef61b998135f`), sauberer Arbeitsbaum, keine offenen PRs beim Start. Aktuellen main, PRs und CI beim Start live prüfen; diese Basis ist kein dauerhaft aktueller HEAD. Produktregeln stehen in [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert

- Automatischer Vordergrund-Import mit USB-Freigabe, eigener Profil-/Kartenbindung und Folge-/Ersatzkartenregeln.
- Unabhängige Originalquellen, SQLCipher/Keystore, additive Migrationen und exakte Quellen-/Importhistorie ohne jüngere Ergänzungen.
- Gemeinsamer Gen1-/Gen2-Lesevorgang mit atomaren Parent-/Child-Belegen, gemeinsamen MF-Quellen und ausdrücklicher Anwendungswahl. Keine Addition der Tätigkeitsbestände.
- Tätigkeitsparser, Tagesprojektion und neutrale Quellzeit-Zusammenfassungen, Fahrzeugvorschau sowie unterstützte Kontroll-/Ereignis-/Störungs-/Sonderbedingungs-, GNSS-/Orts-/Grenz-/Lade-/VU-/Metadaten-/Zertifikats-/MF-Quellen.
- Öffentliche Zertifikatsmathematik begrenzt untersucht; daraus entsteht keine App-Verifikation einer Benutzerquelle.

## Nachweis und Grenze

| Beleg | Bestätigter Stand |
|---|---|
| Geprüfter Produktkopf PR #65 | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Unveränderter Produktmerge | `21edb80ce46c0202204bb74d83486c634b3c90c6` |
| Gerätebestätigung | 12.10.2026, 18:57 Europe/Berlin, exakt 40122 |
| Vollständige Produkt-CI | [38243540172, Versuch 4](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38243540172/attempts/4), erfolgreich |
| Vollständige PR-67-CI | [38296221786](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38296221786), erfolgreich |
| Geprüfte Main-CI nach PR #68 | [38330641632](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38330641632), erfolgreich; Dokumentationspfad, keine neue Produktprüfung |

Tobi bestätigte Installation, Erhalt alter Daten, sichtbare Gen1-/Gen2-Zustände mit passenden Details und Wiederöffnung ohne Leser. Keine zusätzlichen Einsteck-/Deduplizierungs-/Neustart-/Fremdkarten- oder Einzelmessfälle ableiten. Originalnachweis bei konkretem Bedarf: [DEVICE_CONFIRMATION_40122.md](docs/DEVICE_CONFIRMATION_40122.md).

## Noch nicht implementiert

Vollständige Kartenabdeckung, echte Downloadsignaturen, produktive Zertifikats-/Vertrauenskette und vollständiger DDD-/Standarddownload sind offen. `7F66`-Inhaltsdecoder benötigt belegtes Primärformat; für geschützte `0540` fehlt ein legitimer SC5-Zugriffsweg.

**Neu belegte Speichergrenze:** 0504/0505 speichern Pointerwerte und Ring-/Recordbytes statt eines unabhängigen vollständigen EF-Blobs. Gültig leere Fahrzeugbestände werden als EMPTY ohne Originalsnapshot behandelt; unbenutzte Aktivitätsdateien sind nicht als gültig leerer Originalbeleg abgesichert. Einige Readerfehler/fehlende Kapazitäten sind noch nicht vollständig von „nicht versucht“ getrennt. Bestehende erfolgreiche nichtleere Quellen bleiben erhalten; keine rückwirkende Ergänzung.

Neue Navigation/Einrichtung/Onboarding und GNSS-Karte sind noch nicht umgesetzt. Aktuelle App enthält weiterhin alte Abrechnungs-/Heimatpunkt-/Standort-/Konfigurationsartefakte. Diese begründen keine Produktziele; spätere Änderungen müssen Profile und Importdaten erhalten.

## Erreichter Dokumentationsmeilenstein

Die [Pflichtdateien-/Ausnahme-/Signaturmatrix](docs/CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) verknüpft 5 MF-, 14 G1- und 25 G2-/V2-EF-Zeilen mit Primärstellen, Reader-/Speicherpfaden und Herkunft. Geprüft: Änderungen 2018/502 einschließlich P2=00/implizitem Card_Sign-Hash, V2-Pflichten, Signaturlängen aus TR-03111 v2.00 und getrennte Schreib-/Exportgrenzen. Kein Hardwaretest und keine Benutzer-Kryptoprüfung daraus ableiten.

Dieser Meilenstein verändert ausschließlich Dokumentation; Produktcode, Tests, Reader, Parser, SQLCipher, Migrationen, Schemaexporte, Build/CI und funktionale Assets bleiben unverändert. Arbeitsbranch `docs/card-download-matrix-20261013`; endgültige CI-/Mergebelege stehen im zugehörigen PR. Offene Arbeit und exakt nächster Schritt: [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).
