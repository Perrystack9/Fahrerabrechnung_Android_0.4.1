# FahrerTagebuch - aktuelle Übergabe

Einstieg über [WORK_START.md](../WORK_START.md), aktuellen Git/GitHub-Stand und die fünf Startdateien. Keine alten Chats/Handoffs rekonstruieren.

## Abgeschlossener Meilenstein

**40123 / Schema 19: vollständige 0504-/0505-Originalsnapshots CI- und gerätegeprüft; PR #70 gemergt.** Produktmerge `e783ad364b1692d8633c5e4b8846112adb256be6`, identischer Tree `328b6b344c9376cfda0d8ab77d896b53bf944014` zum geprüften Kopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`. Die Kernstatus-/Gerätenachweise werden mit einem reinen Dokumentations-PR auf main verankert; finale PR-/CI-Belege live prüfen.

[Volle CI 38335263243, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2) erfolgreich, alle sieben Gradle-Schritte und komplette API-36-SQLCipher-/Compose-Suiten. [Gerätepaket 38358288875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875) erfolgreich, gleicher Produktkopf. Tobi bestätigt 13.10.2026 09:04 Europe/Berlin die angefragten Kernprüfungen problemfrei. Screenshots: GEN1 13780/6202 Bytes, GEN2V2 13780/9602 Bytes. [Konkreter Geräteumfang](DEVICE_CONFIRMATION_40123.md); optionale Wiederholungs-/Abbruchfälle nicht separat bestätigt.

Implementiert: Rohaufnahme vor Parser mit originalen Pointer-/unbenutzten Bytes, unabhängige EMPTY/INVALID-/Nicht-versucht-/Abwesenheits-/Lesezustände, exakte Karten-/Generations-/Child-Sitzungsbindung, MF am Parent, defensive Freeze-Funktion in beiden Importpfaden. Schema19 unverändert, keine Migration/Altquellenrekonstruktion/Backfill. Normalisierung, Requests und historische Auswahl erhalten. Keine neuen Signatur-/Schreib-/Vertrauensbefehle oder Navigation.

## Exakt nächste Arbeit

1. Aktuellen main, relevante PRs und CI live prüfen. Automatische Main-CI nach PR70: [38360671532](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38360671532), bei dieser Übergabe noch laufend; nicht als grün behaupten. Konkrete Fehler gegebenenfalls zuerst beheben; keine Retryserie oder Budgetänderung.
2. **Eigenständigen begrenzten Signaturaufnahme-PR planen**, auf den akzeptierten Originalen. Nur hierfür die vorhandene [Primärmatrix](CARD_DOWNLOAD_REQUIREMENTS_MATRIX.md) öffnen: DDP038/TCS118-131/Appendix11, P2=00, implizites Hashverfahren aus Card_Sign und echte Signaturlänge prüfen. Vollständige neue Byte-/Abbruch-/Negativ-/Transaktions-/Historien- und Geräteabnahme vorher festlegen.
3. Signaturen nur an tatsächlich gemeinsam neu aufgenommene EF-Originale/Anwendung/Sitzung binden. Keine nachträgliche Signatur an ältere Originale, keine rekonstruierte Quelle und keine Benutzer-Verifikation aus öffentlicher Forschung ableiten.
4. Erst im getrennten Folgepaket implementieren. Eigene volle CI und erforderliche Gerätebestätigung; 40123 erlaubt keine spätere produktive Signatur-/Vertrauens-/DDD-Freigabe.

## Andere Grenzen

ERCA/MSCA/Card_Sign-Vertrauen, Standardexport und bewusst ausgelöster Download-Datum-Schreibpfad separat. Automatischer Import bleibt lesend. `7F66`-Inhaltsdecoder ohne ISO-Primärformat und `0540` ohne legitimen SC5-Weg weiterhin gesperrt. UI/Einrichtung/GNSS erst später; keine Spesen-/Lohn-/Heimkehr-/Heimatstandort-/Abrechnungsphase.

Nach diesem Meilenstein Arbeit sichern und Sitzung abschließen. Kein größeres Folgepaket in dieser Sitzung beginnen; aktuellen Kernstatus ersetzen, keine Chronik anhängen.
