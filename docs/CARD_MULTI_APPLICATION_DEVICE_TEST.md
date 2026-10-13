# Aktuelle Paket-Anleitung 40125 - Identifikationssignaturen

**Neue Geräteabnahme erforderlich; vor deren ausdrücklicher Bestätigung kein Merge von PR #76.** Dieses Paket ist ein Update auf die bestätigte 40124-Installation, additive SQLCipher-Migration 20→21. Die folgenden Schritte gelten für die neue APK; die frühere Anleitung steht zur Dokumentation darunter.

Vor Installation `BUILD_INFO.txt` und `SHA256SUMS.txt` aus derselben ZIP prüfen: `product_commit` muss dem vollständig grünen endgültigen PR-76-Quellkopf entsprechen, `baseline_included=false`. `02-FahrerTagebuch-Test-Update.apk` über die bestehende Test-App installieren. **Nicht deinstallieren, keine App-Daten löschen.** Die feste Testkennung `de.fahrerabrechnung.app.devicetest` und das bisherige Testzertifikat bleiben erhalten. Installierte Version muss 40125 sein. Eine 40124-Gerätebestätigung gibt diesen neuen Stand nicht frei.

1. **Altbestand:** Profil, Karten, Imports und bisherige vier Core-Signaturen erhalten. Alte Imports haben keine nachträglich aufgenommenen Identitätssignaturen.
2. **Neuer eigener Import:** automatischen vollständigen Import durchführen, GEN1/GEN2V2 getrennt öffnen; bisherige Tätigkeits-/Fahrzeugoriginale, Signaturen und bekannte fachliche Daten plausibel.
3. **Neue Details:** im neuesten Import „Fahrerkarten-Details“ öffnen. Je Anwendung „Kartenanwendung“ und „Karteninhaber und Gültigkeit“, zusätzlich auf GEN2V2 „Kartenanwendung V2“. Bei üblicher eigener G1+G2V2-Karte fünf neue Anzeigen „aufgenommen · ungeprüft“ plus Originalgröße und echte Signaturlänge. Werte/Identität stimmen weiterhin zur Karte; keine Änderung der Kartennummer/Struktur/Kapazitäten durch die zweite Leseaufnahme.

| Quelle | Erwartete Originalgröße bei aktuellem Format | Signaturlänge |
|---|---:|---|
| GEN1 Kartenanwendung | 10 Bytes | 128 Bytes |
| GEN1 Karteninhaber/Gültigkeit | 143 Bytes | 128 Bytes |
| GEN2V2 Kartenanwendung | 17 Bytes | Aus tatsächlichem C101: 64/96/128/132 Bytes |
| GEN2V2 Karteninhaber/Gültigkeit | 143 Bytes | Aus tatsächlichem C101 |
| GEN2V2 Kartenanwendung V2, wenn vorhanden | 10 Bytes | Aus tatsächlichem C101 |

4. **Wiederöffnung/Quellenwechsel:** ohne Reader neu öffnen; neue Originale/Signaturen erhalten. Zwischen EFs, Anwendungen, alten/neuen Imports und Altarchiv soweit vorhanden wechseln. Alte Quelle nie mit neuen Belegen; Kartennummer nach Wechsel wieder verborgen. GEN1 bekommt keine erfundene V2-Quelle.
5. **Wiederholung:** nochmals vollständig einlesen; neue eigene Sitzung und Signaturbelege, Tätigkeiten/Fahrzeuge nicht doppelt addiert. Gleiche Rohblob-IDs oder Größen sind kein Ersatz für eigene neue Receipts.
6. **Kartenentnahme/Neustart:** laufenden neuen Import durch Entnahme abbrechen; verständliches Ergebnis, keine neuen teilweise gespeicherten Kartendaten/Gruppe. Neu verbinden und vollständigen Import erfolgreich beenden; kein alter Hashzustand.


Alle tatsächlich abgearbeiteten Punkte und das Ergebnis ausdrücklich bestätigen; nicht geprüfte Punkte benennen. Bitte sichtbare Originalgrößen und Signaturlängen festhalten, gern mit Screenshots. Keine privaten Kartennummern, APDUs, Dumps oder Schlüssel teilen. Aufnahme bleibt ungeprüft, keine mathematische Vertrauens- oder DDD-Freigabe. Getestete Karte/Android-Gerät/USB-Leser nur aus tatsächlicher Rückmeldung dokumentieren.

---

# Frühere Paket-Anleitung - ausschließlich historischer Beleg

> **HISTORISCH - nicht als aktuelle Anforderung verwenden. Einordnung: HISTORISCH.** Zeitgebundener Nachweis; ursprüngliche Aussagen und Testgrenzen bleiben erhalten. Aktueller Einstieg: [WORK_START.md](../WORK_START.md); verbindlich sind [CURRENT_RULES.md](CURRENT_RULES.md). Nicht pauschal lesen.

> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Gerätetest40122 – beide Anwendungen derselben Fahrerkarte

Vorbereitung für PR65, Schema19. **Noch keine Gerätefreigabe und kein ausgeliefertes Paket.** Erst nach vollständiger grüner CI und eigener Signatur-/Prüfsummenprüfung ausgeben. Updatebasis ist das bereits installierte40121/Schema18, feste Testkennung `de.fahrerabrechnung.app.devicetest` und unverändertes Testzertifikat.

1. Update40122 über40121 installieren. Nicht deinstallieren, App-Daten nicht löschen. Vorhandene alte Übernahmen müssen erhalten bleiben.
2. Dieselbe eigene physische Fahrerkarte anschließen. Bekannte Karte übernimmt automatisch; erste/unklare Ersatzkarte braucht weiterhin ausdrückliche eigene Bestätigung mit anschließender frischer Lesung.
3. Im Importverlauf den neuen gemeinsamen Lesevorgang öffnen. Gen1 und Gen2/Gen2V2 müssen getrennt erscheinen, soweit diese Karte beide Anwendungen anbietet. „Nicht vorhanden“ oder ein eigener Lesefehler ist ein eigener Status, kein erfundener zweiter Datensatz.
4. Beide Anwendungsschaltflächen einzeln öffnen. Die Vorschau benennt die ausgewählte Generation; originale Kartenangaben, Kapazitäten und Zertifikate gehören jeweils zu genau dieser Anwendung. Zeitangaben beider Anwendungen werden nicht zusammengerechnet.
5. Gemeinsame Kartenkennung, Chipkennung, Anwendungsverzeichnis und bedingte Kommunikationsdateien kontrollieren. Vorhandene11-/3Byte-Kommunikationsangaben bleiben sichtbar. Fehlende bedingte Dateien sind kein Vollständigkeitsbeweis. Es werden weiterhin keine Puffergrößen geraten oder erweiterten APDUs eingesetzt.
6. Karte und Leser entfernen, App schließen und neu öffnen. Den konkreten gemeinsamen Vorgang im Verlauf erneut öffnen; beide gespeicherten Anwendungen und die MF-Angaben müssen ohne Leser erhalten bleiben.
7. Einen alten40121- oder früheren Ein-Anwendungs-Import öffnen. Seine ursprünglichen Quellen bleiben unverändert; keine nachträglich ergänzte Gen1-Anwendung und keine MF-Daten aus jüngeren Vorgängen.
8. Dieselbe Karte erneut einstecken. Ein weiterer gemeinsamer Lesevorgang darf entstehen; gespeicherte Rohquellen werden dedupliziert. Beide älteren Anwendungsbelege bleiben exakt wiederöffnbar.

Bitte Rückmeldung mit installierter Version, sichtbaren Gen1-/Gen2-Zuständen, Erhalt alter Daten und Wiederöffnung ohne Leser. Keine private Kartennummer, Zertifikatsreferenz oder Kartenrohdaten veröffentlichen. Test verlangt keine Fremdkarte und keine erzwungene Authentisierung.

40121-Bestätigung gilt nicht für diesen neuen Import-/Schema-/UI-Stand. PR65 bleibt bis eigener ausdrücklicher Freigabe offen, ohne Merge oder Auto-Merge. Vollständiger signierter Standarddownload/DDD, Kryptovertrauen und geschützteVU_Configuration0540 sind weiterhin nicht implementiert.
