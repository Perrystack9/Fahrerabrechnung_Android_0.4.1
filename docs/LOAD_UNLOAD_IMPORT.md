# Be- und Entladevorgänge als eigene Kartenquelle

Stand 10.10.2026. Basis: am unveränderten getesteten Kopf gemergter PR53, main `22d41f64875de3aa5ac383d9d1ce80dc0dee1684`, Schema14/Testversion40112. Vollständige CI38065441534 Versuch1 und Paket38067285142 erfolgreich. Tobi bestätigt am10.10. um19:56 Europe/Berlin allgemein „Funktioniert alles prima.“ Keine zusätzlichen Einzelmesswerte oder bestimmten nichtleeren Grenzdatensätze behauptet.

## Amtliche Primärspezifikation

[EU2021/1228, amtliches JRC-PDF](https://dtc.jrc.ec.europa.eu/iot_doc/EU_2021-1228.pdf), Appendix1 2.24c/d (L273/29), 2.61a (31), 2.79c (34–35), 2.114a (36), Appendix2 TCS_152/TCS_154/TCS_155 (45,48,49). ASN.1-Strukturen, EF-Kennungen und Byte-/Kapazitätstabellen vor Implementierung visuell im amtlichen PDF geprüft. Öffnen derselben Datei per Webabruf scheiterte an der Dateigröße; die vorhandene amtliche PDF war lokal vollständig verfügbar. Keine Sekundärquelle als Formatbasis.

EF Load_Unload_Operations ist `0529`, ausschließlich für Generation2 Version2. Ältere Fahrer-/Werkstattkarten sind kein unterstützter Fall dieser Erweiterung. Die separate Load_Type_Entries-Datei `0530` ist ein eigener späterer Schritt.

| Feld | Bytes | Offset im Datensatz |
|---|---:|---:|
| Beginn des eingegebenen Vorgangs | 4 | 0 |
| Vorgangsart | 1 | 4 |
| GNSS-Positionszeit | 4 | 5 |
| HDOP | 1 | 9 |
| signierte Breite/Länge in Grad-Minuten | 3+3 | 10/13 |
| aufgezeichneter GNSS-Authentisierungsstatus | 1 | 16 |
| Kilometerstand der Karte | 3 | 17 |

Zwei-Byte-Ringzeiger plus1624×20Bytes ergibt32482Bytes. Die Kapazität steht unabhängig an Offset4..5 der zehn Byte großen Application_Identification_V2 (`0525`), deren Folgelänge8 sein muss. Die Border-Capacity an Offset2..3 wird nicht für diese Quelle validiert oder ersetzt. So blockiert eine unbrauchbare Grenzkapazität die gültige Be-/Entladequelle nicht und umgekehrt. Ohne vollständige V2-Erweiterung bleiben andere unabhängige EFs weiterhin lesbar.

Vorgangsart01=Beladen,02=Entladen,03=gleichzeitiges Be-/Entladen;00 und04..FF bleiben reservierte Rohwerte. Beginn und Positionsbestimmung werden getrennt erhalten, einschließlich fehlender Werte und unsigned Zeitangaben nach2038. Keine Ersatzzeit, Verbindung zu anderen Punkten, Zuordnung zu Arbeit/Pause oder Berechnung einer Dauer. Ringumbruch, physischer Slot, Duplikate und ungeordnete Zeiten bleiben erhalten. Nur vollständig00-/FF-gefüllte Slots entfallen; einzelne unbekannte Felder verwerfen andere Datensätze nicht. Ein aufgezeichneter GNSS-Status ist keine kryptografische Verifikation dieser App.

## Import und Herkunft

Unabhängige vollständige Lesung nach validierter Kartenidentifikation, inklusive letzter READ-BINARY-Portion an Offset32256 mit226Bytes. Fehler und Cancellation folgen den bisherigen Regeln; keine vollständigen APDUs/Rohdaten in Logs.

Verschlüsselte unveränderliche Quelle je physischer eigener Karte und exakte atomare Sitzungsverknüpfung. Fingerprint enthält Karten-ID, Generation, Kapazität und vollständige Datei. Wiederholungen deduplizieren Bytes, neue Fassungen/Ersatzkarten bleiben getrennt. Zusammengesetzte Fremdschlüssel verhindern eine fremde Kartenquelle in derselben Sitzung. Historische Requests ohne neue Quelle behalten Fingerprint und Quellenabdeckung; kein Backfill oder Ersatz durch spätere Imports. Fremde/unbestätigte Karten bleiben temporär.

Additive Migration14→15: ausschließlich `card_load_unload_imports` und `driver_import_load_unload_sources`. Alle30 bisherigen Tabellen bleiben erhalten. Der verbindliche15.json-Export wird unverändert aus KSP/CI übernommen, nicht handgeschrieben. Die Detailansicht zeigt nur die ausgewählte Sitzung und lädt weitere Vorgänge in20er-Portionen; Quellenwechsel setzt die Portion zurück. Fehlende und gültig leere Dateien haben verschiedene Meldungen.

## Verifikation und aufgeschobener Gerätetest

18 Parser-, neun Reader-, sieben SQLCipher-, ein Room- und vier Compose-Testmethoden ergänzen Ring-/Feld-/Zeit-/Koordinatenprüfung, vollständige Lesung nahe der Offsetgrenze, unabhängige Kapazitäten/Teilfehler/Cancellation, Deduplizierung/Kartenwechsel, Rollback, Upgrade14→15 und historische UI-Navigation. Lokale SQLite-Migrationsprüfung erhält alle30 bisherigen Tabellen, fügt zwei hinzu und hat keine FK-Verletzung. Vollständige Android-CI einschließlich SQLCipher/Room/Compose und bytegleichem echtem Schemaexport bleibt vor APK-Ausgabe erforderlich; Ergebnis am finalen PR-Kopf prüfen.

Testversion40113, gleiches Paket `de.fahrerabrechnung.app.devicetest` und feste Testsignatur. Updatepaar baut den unveränderten PR53-Produktstand mit40112/Schema14. Benutzerpaket enthält nur Update-APK, Anleitung, Buildinfo und Prüfsummen. Tobi hat am10.10.19:56 ausdrücklich weitere Hardwaretests gebündelt: erster Block bis20:56, Pause bis22:56, zweiter Block bis11.10.00:56 Europe/Berlin. [Geräteanleitung](LOAD_UNLOAD_DEVICE_TEST.md) erst danach ausgeben; neuer Import-/Schema-PR bleibt bis neuer Bestätigung ungemergt.
