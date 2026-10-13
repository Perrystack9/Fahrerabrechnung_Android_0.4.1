# Geplante Geräteabnahme 40125 - Identifikationssignaturen

**VORBEREITET, NICHT DURCHGEFÜHRT.** Produktkandidat **40125 / Schema 21** in offenem Draft-[PR #76](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/76): zusätzliche neue 0501-/0520-/V2-0525-Aufnahmen und additive Migration 20→21 nach dem [Plan](CARD_IDENTITY_SIGNATURE_CAPTURE_PLAN.md). main/ installierte bestätigte Basis bleibt 40124 / Schema20. Kein neues Paket oder Geräteergebnis aus der früheren 40124-Abnahme ableiten.

Vor Installation endgültigen vollständig grünen Produktkopf live prüfen und bestehendes Gerätepaket genau dafür erzeugen; `include_baseline=false` bei vorhandener 40124-Installation. Nach Update folgende Punkte eigenständig bestätigen:

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

Tatsächlich getestete Karte, Android-Gerät/USB-Leser, Schritte und Ergebnis dokumentieren; nicht getestete Punkte ausdrücklich benennen. Screenshots von Größen/Status/Längen genügen, keine privaten Dumps/APDUs/Schlüssel. Modellnamen nur aus tatsächlicher Rückmeldung, nicht aus Kontometadaten ableiten. 40124-Bestätigung ersetzt diese neue Abnahme nicht. Keine mathematische Prüfung/Vertrauens-/DDD-Freigabe. Erst eigene ausdrückliche Bestätigung des erforderlichen Umfangs erlaubt Merge des exakt geprüften Produktbaums.
