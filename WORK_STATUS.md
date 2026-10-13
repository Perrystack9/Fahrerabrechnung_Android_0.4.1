# FahrerTagebuch - aktueller technischer Zustand

**Testversion 40122 / Room-Schema 19.** Dokumentationsbasis: main nach gemergtem PR #67 (`11301b4078db7094a7b6d234cf9f58e612e6b203`). Aktuellen main, PRs und CI beim Start live prüfen; diese Basis ist kein dauerhaft aktueller HEAD. Produktregeln stehen in [CURRENT_RULES.md](docs/CURRENT_RULES.md).

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

Tobi bestätigte Installation, Erhalt alter Daten, sichtbare Gen1-/Gen2-Zustände mit passenden Details und Wiederöffnung ohne Leser. Keine zusätzlichen Einsteck-/Deduplizierungs-/Neustart-/Fremdkarten- oder Einzelmessfälle ableiten. Originalnachweis bei konkretem Bedarf: [DEVICE_CONFIRMATION_40122.md](docs/DEVICE_CONFIRMATION_40122.md).

## Noch nicht implementiert

Vollständige Kartenabdeckung, echte Downloadsignaturen, produktive Zertifikats-/Vertrauenskette und vollständiger DDD-/Standarddownload sind offen. `7F66`-Inhaltsdecoder benötigt belegtes Primärformat; für geschützte `0540` fehlt ein legitimer SC5-Zugriffsweg.

Neue Navigation/Einrichtung/Onboarding und GNSS-Karte sind noch nicht umgesetzt. Aktuelle App enthält weiterhin alte Abrechnungs-/Heimatpunkt-/Standort-/Konfigurationsartefakte. Diese begründen keine Produktziele; spätere Änderungen müssen Profile und Importdaten erhalten.

Dieser Cleanup verändert ausschließlich Dokumentation: Produktcode, Tests, Reader, Parser, SQLCipher, Migrationen, Schemaexporte, Build/CI und funktionale Assets bleiben unverändert. Offene Arbeit und exakt nächster Schritt: [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).
