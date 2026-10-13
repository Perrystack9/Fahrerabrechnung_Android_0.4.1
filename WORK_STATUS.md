# FahrerTagebuch - aktueller technischer Zustand

**40123 / Room-Schema 19, eigener Geräteumfang bestätigt.** [PR #70](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/70) gemergt: `e783ad364b1692d8633c5e4b8846112adb256be6`. Identischer Produktbaum zum geprüften Kopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`. Aktuellen main, offene PRs und CI stets live prüfen; Produktregeln: [CURRENT_RULES.md](docs/CURRENT_RULES.md).

## Implementiert und übernommen

- Automatischer Vordergrund-Import, USB-Freigabe, eigene Profil-/Kartenbindung und Folge-/Ersatzkartenregeln; SQLCipher/Keystore, additive Migrationen und exakte Importhistorie.
- Gemeinsamer Gen1-/Gen2-Lesevorgang mit atomarem Parent/Child, MF am Parent und ausdrücklicher Anwendungswahl. V2 bleibt im Gen2-AID; beide Tätigkeitsbestände nicht addieren.
- Vollständige unabhängige 0504-/0505-READ-BINARY-Originale einschließlich Pointer und unbenutzter Bytes, defensive Kopien vor fachlichem Parser; gleiche Freeze-Funktion im Einzel-/Mehr-Anwendungs-Pfad.
- Rohaufnahme unabhängig von EMPTY/INVALID-Projektion. Fehlende Kapazität/nicht versucht, EF-Abwesenheit und tatsächlicher Readerfehler getrennt; keine Teilbytes als Original, keine Teilgruppe bei Transportabbruch.
- Unbenutzte 0504 nur Nullpointer plus ganzer Nullring; unklarer unbenutzter Fahrzeugpointer bleibt INVALID mit erhaltenem Original. Bestehende nichtleere Projektionen und Normalisierung erhalten.
- Bestehende verschlüsselte Schema-19-Tabellen mit separaten Quellarten/Fingerprints für physische Karte, EF, Generation und konkrete Child-Sitzung. Keine Migration, Rekonstruktion/Backfill oder neuerer Ersatz für historische Quellen. Alte Requests behalten Fingerprint/Abdeckung.
- Gespeicherte Vorschau zeigt Originalgrößen/Generation oder ausdrücklich fehlende Originaldatei. Bestehende neutrale Tätigkeits-/Fahrzeug-/GNSS-/Orts-/Ereignis-/Störungs- und übrige unterstützte Quellen erhalten; keine neue Navigation.

## Nachweise und Umfang

| Beleg | Ergebnis |
|---|---|
| Vollständige Produkt-CI | [38335263243, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2), erfolgreich für `659f31c510752e5a06bf5f45bf2c93f4aff37031` |
| Gerätepaket | [38358288875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875), erfolgreich für denselben Quellkopf |
| Gerätebestätigung | 13.10.2026 09:04 Europe/Berlin, Tobi: Kernprüfungen problemfrei |
| Originalgrößen | GEN1 13780/6202 Bytes; GEN2V2 13780/9602 Bytes |
| Produktmerge | `e783ad364b1692d8633c5e4b8846112adb256be6`, identischer Tree `328b6b344c9376cfda0d8ab77d896b53bf944014` |

Alle sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexportprüfung, komplette SQLCipher-/Compose-Suiten auf API 36 und Abschlussjob bestanden. Normale erfolgreiche CI ohne Artefaktupload, Paket separat gezielt erzeugt. Keine Filter/Quarantäne oder geänderten Budget-/Uploadregeln. Lokaler Gradle-Download weiter netzbedingt blockiert; Android-Nachweis stammt aus GitHub-CI.

Bestätigt sind Kernfälle Update/Altbestand, eigener Import, getrennte Anwendung/Originalgrößen, plausible fachliche Daten, historische Auswahl und Wiederöffnung ohne Leser. Optionale Wiederholungs-/Abbruchfälle nicht separat bestätigt; keine zusätzliche Abdeckung ableiten. [Genauer Gerätebeleg](docs/DEVICE_CONFIRMATION_40123.md). Frühere 40122-Basis bei konkretem Bedarf im zugehörigen Beleg.

Automatische Main-CI nach Produktmerge: [38360671532](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38360671532), bei Dokumentationsabschluss noch laufend; live prüfen, keinen späteren Erfolg vorwegnehmen. Gerätebestätigung gilt für den exakt unveränderten Produktbaum, nicht für zukünftige Änderungen.

## Weiter offen

Echte Downloadsignaturen, produktive ERCA/MSCA/Card_Sign-Vertrauenskette, vollständige Kartenabdeckung und DDD-/Standarddownload. `7F66`-Inhaltsdecoder braucht ISO-Primärformat, `0540` legitimen SC5-Weg. Keine Signatur-/Hash-/Schreib-/Vertrauensbefehle im Snapshot-Paket.

Als Nächstes eigenständigen Signaturaufnahme-PR planen; [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md). Neue UI/Einrichtung/Onboarding/GNSS-Karte später. Alte Abrechnungs-/Heimatpunkt-/Standortartefakte erzeugen keinen Auftrag. Aktuelle Übergabe: [WORK_HANDOFF.md](docs/WORK_HANDOFF.md).
