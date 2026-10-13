> **DETAIL-AKTIV - nur bei konkreter Facharbeit lesen.** Aktueller Umfang: [CURRENT_RULES.md](CURRENT_RULES.md); aktueller Versions-/Geräte-/PR-Stand: [WORK_STATUS.md](../WORK_STATUS.md). Format- und Teilschrittgrenzen unten sind keine feste künftige Navigation.

# Begrenzte Kontrolldaten-Quelle

Diese Stufe ergänzt ausschließlich `EF Control_Activity_Data` der bereits identifizierten eigenen Fahrerkarte. Keine vollständige Kartenabdeckung, VU-Kontrollkarten-Historie, Verstoßbewertung oder Zuordnung des Kontrollkarteninhabers zum Fahrerprofil.

## Primärquellen

- [EU 2016/799, JRC-Originalfassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf): Annex IC, Appendix 1, 2.15 (`CardControlActivityDataRecord`), 2.53 (`ControlType`) und 2.73 (`FullCardNumber`); Appendix 2, TCS_148/150 (Gen1), TCS_151/152 (Gen2).
- [EU 2021/1228, JRC-Fassung](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf): Änderung von Appendix 2, TCS_152, Amtsblatt L 273/47. Auch Gen2V2 verwendet für diese Datei unverändert 46 Bytes und `FullCardNumber`, ohne zusätzliches Generation-Byte.
- [Aktuelle JRC-Dokumentenübersicht](https://dtc.jrc.ec.europa.eu/dtc_smart_tachograph.php.html) verweist auch auf die Änderungen 2018/502 und 2023/980. Diese Stufe betrifft ausschließlich die obige Kontrollstruktur, keine GNSS-Authentifizierung/OSNMA.

Die Feldreihenfolge und Größen wurden direkt an den Originaltabellen geprüft. Dateikennung `0508h` innerhalb der ausgewählten Fahrer-Anwendung; nur SELECT/READ BINARY, keine Kartenschreiboperation.

| Offset | Bytes | Quellfeld |
|---|---:|---|
| 0 | 1 | ControlType, vollständiges Bitfeld behalten |
| 1 | 4 | controlTime, unsigned TimeReal in UTC |
| 5 | 1 | Kontrollkarten-Typ |
| 6 | 1 | Kontrollkarten-Ausstellungsstaat |
| 7 | 16 | Kontrollkartennummer |
| 23 | 1 | Fahrzeug-Zulassungsstaat |
| 24 | 1 | Kennzeichen-Codepage |
| 25 | 13 | Kennzeichen |
| 38 | 4 | Beginn des heruntergeladenen Zeitraums |
| 42 | 4 | Ende des heruntergeladenen Zeitraums |

Ein Nullwert bei controlTime bedeutet einen unbenutzten Kontrollbeleg; eine vollständige leere Datei bleibt als Quelle gespeichert, ohne ein Kontrollereignis zu erfinden. Andere Null-Zeitwerte bleiben unabhängig fehlend. Widersprüchliche Download-Zeitangaben bleiben unverändert und werden in der Detailansicht kenntlich gemacht. Unbekannte Kontrollbits bleiben erhalten, ohne ihnen eine geratene Bedeutung zu geben. Gen1 kennt c/v/p/d (Bits 7 bis 4), Gen2/Gen2V2 zusätzlich e (Bit 3, Kalibrierungsparameter).

## Herkunft, Speicherung und historische Abdeckung

Nach gültiger Fahreridentifikation werden alle drei unterstützten Dateien unabhängig gelesen. Ausfälle/ungültige Daten der Kontrolldatei verhindern keine Speicherung gültiger Aktivitäten/Fahrzeuge. Nur vollständige gültige Kontrolldateien werden gespeichert. Cancellation wird weitergereicht; fremde/unbestätigte Fahrer bleiben durch die unveränderte Profilprüfung gesperrt.

Schema 9 ergänzt zwei Tabellen: unveränderliche vollständige Kontrollquellen je physischer DriverCard sowie exakte Verweise aus der tatsächlichen ImportSession. Identische Bytes derselben Karte werden wiederverwendet, neue Fassungen und Ersatzkarten bleiben getrennt. Quelldatei, Sitzung, vorhandene Tages-/Fahrzeugnormalisierung und erfolgreicher Importbeleg sind gemeinsam atomar. Die bisherige Roh-Quellenzählung umfasst neu gespeicherte Kontrollquellen; eine leere gültige Kontrollquelle zählt als Quelldatei, aber nicht als Kontrollereignis.

Alle 18 bestehenden Tabellen bleiben unverändert. Additive Migration 8→9 ohne Backfill: kein Kontrollstatus oder Quellverweis für frühere Imports. Historische Importläufe behalten ihre zwei damaligen Quellenstatus und ihren unveränderten Request-Fingerprint. Neue echte Lesergebnisse versuchen drei Quellen. Die Detailansicht öffnet ausschließlich die Kontrollquelle der gewählten Sitzung; keine Ergänzung aus einer älteren/neueren Übernahme.

Platzierung gemäß Issue #3: gespeicherte Fahrerkarte → Details → Kontrollen; fünf Haupttabs unverändert. Kontrollkartennummer standardmäßig maskiert, auf ausdrücklichen Wunsch einblendbar. Nur aufgezeichnete Kontrollarten und verfügbare Quellwerte anzeigen, keine Bewertung von Fahrerverschulden oder Verstößen.
