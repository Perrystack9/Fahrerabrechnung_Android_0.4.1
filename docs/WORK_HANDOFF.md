# FahrerTagebuch - aktuelle Übergabe

Einstieg über [WORK_START.md](../WORK_START.md), aktuellen Git/GitHub-Stand und die fünf Startdateien. Keine alten Chats/Handoffs rekonstruieren.

## Abgeschlossener Meilenstein

**Begrenzter Signaturaufnahmeplan fertig; Produkt unverändert 40123 / Schema 19.** [Konkreter Plan](CARD_CORE_SIGNATURE_CAPTURE_PLAN.md) für neue 0504-/0505-Signaturen in GEN1 und GEN2/V2: primärgeprüfte Befehle/Hash-/Längenwahl, defensive Vollständigkeit, Zustände/Fallback/Abbruch, neue sitzungseigene Belege mit geplanter additiver Migration 19→20, genaue Codefläche, vollständige automatische Prüfungen und neues eigenes Gerätegate. Nur Dokumentation geändert; Signaturbefehle, Migration, 40124 und Anzeige noch nicht implementiert.

Geprüfte Ausgangsbasis main `632e0dfa020adb1d190d76b9591b046c0332f58c` nach Docs-PR #71. Produkt-Main-CI [38360671532](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38360671532) vollständig erfolgreich; Docs-Main-CI [38361629250](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38361629250) erfolgreich mit korrekt übersprungenem Android. Endgültigen aktuellen Plan-PR/-Merge/-CI live prüfen; keinen gespeicherten Ausgangskopf als dauerhaft aktuell verwenden.

Übernommene Produktbasis: PR #70, Merge `e783ad364b1692d8633c5e4b8846112adb256be6`, Produktkopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`, identischer Tree `328b6b344c9376cfda0d8ab77d896b53bf944014`. [Volle CI 38335263243 Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2), [Paket 38358288875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875), Tobis Gerätebestätigung 13.10.2026 09:04 Europe/Berlin. GEN1 13780/6202 und GEN2V2 13780/9602 Bytes; [konkreter Geräteumfang](DEVICE_CONFIRMATION_40123.md), optionale Wiederholungs-/Abbruchfälle nicht separat bestätigt.

## Exakt nächste Arbeit

1. Aktuellen main, relevante PRs und CI live prüfen; offene echte Fehler zuerst klären. Plan-Meilenstein nicht nochmals erstellen oder abgeschlossene Originalaufnahme umbauen.
2. **Getrennten Produkt-PR für echte 0504-/0505-Signaturaufnahme beginnen.** Nach dem Startpfad nur den konkreten Plan öffnen; Primärmatrix/Code gezielt bei Bedarf. Zuerst Signaturmodelle/Command-Factories/Sequenztests, danach Reader-Fallback, Sitzungsbindung/Migration und kleine Abnahmeanzeige. 40124/Schema 20 erst tatsächlich mit dieser Implementierung erzeugen.
3. C101 vor der GEN2-Kernsequenz einmal neu lesen; ausschließlich dessen Card_Sign-OID/CHA, P2=00 und tatsächliche Länge. Kein späteres Signieren alter Bytes; eigene Receipt-FKs zur gleichen Original-/Zertifikatssitzung, Nullmap-Historie/Fingerprints erhalten.
4. Vollständige neue Byte-/Negativ-/Phasenabbruch-/SQLCipher-/Migration-/Historien-/Compose-Prüfungen sowie volle unveränderte CI. Exaktes Gerätepaket und neue bestätigte Kern-/Wiederholungs-/Abbruchfälle **vor Produktmerge**. Keine Freigabe aus 40123 übernehmen.

## Andere Grenzen

Mathematische Signaturprüfung, ERCA/MSCA/Card_Sign-Vertrauen, weitere EF-Signaturen, Original-/DDD-Export und bewusst ausgelöster Download-Datum-Schreibpfad separat. Aufnahme heißt „ungeprüft“. Automatischer Import verändert keine Karten-EFs. `7F66` ohne ISO-Primärformat und `0540` ohne legitimen SC5-Weg gesperrt. UI/Einrichtung/GNSS später; keine Spesen-/Lohn-/Heimkehr-/Heimatstandort-/Abrechnungsphase. Browser, Unteragenten und CI-/Budget-/Upload-/Triggeränderungen bleiben ausgeschlossen.

Nach Sicherung dieses Plan-Meilensteins Sitzung abschließen. Neue Sitzung: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
