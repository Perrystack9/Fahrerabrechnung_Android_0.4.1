# FahrerTagebuch - aktueller technischer Zustand

**Bestätigte Produktbasis 40122 / Room-Schema 19. Kandidat 40123 / Schema 19 noch ohne eigene Geräteabnahme.** Snapshot-Arbeit vom 13.10.2026 basiert auf main nach PR #69 (`5b1b9669af8614ec502d027a4c71d94ab81496ad`). Aktuellen main, offene PRs, endgültigen Kopf und dessen CI immer live prüfen. Produktregeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Übernommen und bestätigt

- Automatischer Vordergrund-Import, USB-Freigabe, eigene Profil-/Kartenbindung und Folge-/Ersatzkartenregeln.
- SQLCipher/Keystore, additive Migrationen und exakte Quellen-/Importhistorie ohne jüngere Ergänzungen.
- Gemeinsamer Gen1-/Gen2-Lesevorgang mit atomaren Parent-/Child-Belegen, MF am Parent und ausdrücklicher Anwendungswahl. Tätigkeitsbestände nicht addieren.
- Bestehende neutrale Tätigkeits-/Fahrzeug-/GNSS-/Orts-/Ereignis-/Störungs-/Sonderbedingungs- und weitere unterstützte Quellen samt Normalisierung erhalten.

| Nachweis | Bestätigter Umfang |
|---|---|
| Produktkopf PR #65 | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Unveränderter Produktmerge | `21edb80ce46c0202204bb74d83486c634b3c90c6` |
| Gerät | 12.10.2026, 18:57 Europe/Berlin, exakt 40122 |
| Vollständige Produkt-CI | [38243540172, Versuch 4](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38243540172/attempts/4), erfolgreich |
| Matrix-PR #69 | Kopf `04abc29047dd227265c9d8e3821740dc1b6e0efe`, [38332587203](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38332587203), erfolgreich; reine Dokumentationsprüfung |

Gerätebestätigung 40122 deckt Installation, Altbestand, Gen1-/Gen2-Zustände/Details und Wiederöffnung ohne Leser ab. Weitere Fälle nicht ableiten; [Originalnachweis](docs/DEVICE_CONFIRMATION_40122.md) nur bei Bedarf. Keine frühere Freigabe bestätigt den neuen Snapshot-Pfad.

## Neuer begrenzter Kandidat

Branch `feat/card-ef-snapshots-20261013`, Testversion **40123**. Noch nicht gemergt.

- `CardCoreSnapshot`: vollständige READ-BINARY-Originale von 0504/0505 einschließlich Pointer; defensive Kopien. Aufnahme unabhängig von Pointer-/Record-/Tageskettenparser.
- `CardCoreRead`: COMPLETE, ABSENT, READ_FAILED mit tatsächlichem Readerfehler oder NOT_ATTEMPTED wegen fehlender Kapazität. Originalaufnahme hat eigene Importbelege; Auswertung kann EMPTY oder INVALID bleiben. Keine Teilbytes als Original.
- Unbenutzte 0504 nur bei **beiden Nullpointern und vollständig nullgefülltem Ring** gültig leer (Appendix 2 TCS150/154, Konsolidierung S. 281/284). Bestehende 0505-Leererkennung erhalten; Original einschließlich Text-/Identifikationsfiller bleibt gespeichert.
- Speicherung in bestehenden verschlüsselten `card_metadata_imports`/`driver_import_metadata_sources`, getrennte Quellarten und Fingerprints mit physischer Karte, EF und Generation. FK-Bindung an die exakte Child-Sitzung. **Schema 19 unverändert; keine Migration und kein nachträglicher Backfill.**
- Bestehende nichtleere Projektionen und Normalisierung erhalten. Historische Requests ohne Rohaufnahme behalten Fingerprint/Abdeckung. Historische Quellen werden weder rekonstruiert noch durch jüngere ersetzt.
- Bestehende Vorschau zeigt die vollständigen Originalgrößen/Generation je Auswahl oder ausdrücklich fehlende Originaldatei. Kein Navigations-/Onboarding-Umbau.

## Prüfzustand und Sperre

Lokale Projektgrenzen, zehn CI-/Schema-Guard-Tests und Diff-Prüfung bestanden. Gradle lokal vor Build blockiert: Distribution nicht erreichbar (Netzwerk eingeschränkt); keine Android-Tests daraus als ausgeführt behaupten. Neue Unit-, SQLCipher-/Transaktions-/Historien-/Gen1-/Gen2V2- und Compose-Fälle ergänzt. **Volle unveränderte Android-CI für den endgültigen Kopf erforderlich; Live-Ergebnis und PR-Befunde im PR prüfen.**

40123 noch nicht am Gerät bestätigt oder als geprüfte APK ausgegeben. [Geräteprotokoll](docs/CARD_CORE_SNAPSHOT_DEVICE_TEST.md) vorbereitet. Vor Produktmerge eigener voller CI-Nachweis und erforderliche Gerätebestätigung; bloße Freigabe 40122 reicht nicht.

## Weiter offen

Vollständige Kartenabdeckung, echte Downloadsignaturen, produktive ERCA/MSCA/Card_Sign-Vertrauenskette und DDD-/Standarddownload. `7F66`-Inhaltsformat und legitimer SC5-Zugriff auf `0540` weiter ungeklärt. Keine neuen Signatur-/Hash-/Schreib-/Vertrauensbefehle im Snapshot-Paket.

Neue Navigation/Einrichtung/Onboarding/GNSS-Karte später. Alte Abrechnungs-/Heimatpunkt-/Standortartefakte erzeugen keinen Auftrag. Nächster Arbeitszustand: [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).
