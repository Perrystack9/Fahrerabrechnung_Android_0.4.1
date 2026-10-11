# Originale Anwendungs- und Identifikationsdateien

Dieser kumulative Entwurf ergänzt die unveränderten EFs `Application_Identification` (0501), `Application_Identification_V2` (0525) und `Identification` (0520) als eigene verschlüsselte Quellen je bestätigter physischer Fahrerkarte und Import. Er baut auf dem Zertifikatsentwurf PR59 auf. Testversion40119, Schema18 unverändert, Updatebasis der bestätigte PR58/40117.

## Amtliche Formate

Primärquellen: EU2016/799 AnhangIC, Anlage1 Abschnitte2.61/2.62 und Anlage2 TCS149/153; Korrektur EU2018/502; EU2021/1228 Anlage1 Abschnitt2.61a und Gen2V2-Dateitabelle. Die bereits im Projekt recherchierten amtlichen ASN.1-/Byte-/SELECT-/READ-Tabellen wurden visuell abgeglichen.

| Original-EF | Gen1 | Gen2 | Gen2V2 |
|---|---|---|---|
| Application_Identification 0501 | 10Byte | 15Byte Legacy oder17Byte | 15Byte Legacy oder17Byte |
| Application_Identification_V2 0525 | nicht vorhanden | nicht vorhanden | 10Byte, FollowingDataLength8 |
| Identification 0520 | 143Byte | 143Byte | 143Byte |

Die Bytes der bereits ausgewählten Anwendung werden aus derselben Lesesitzung übernommen: keine erneute Identifikationslesung, keine andere AID, keine Vermischung von Gen1-/Gen2-Verzeichnissen. Ein Ein-Byte-READ an der jeweiligen Dateigrenze muss ein datenloses6700/6B00/6282 ergeben. Zusatzbytes, leeres9000, Statusfehler mit Daten oder6C-Längenkorrekturen belegen keine vollständige Originaldatei. Ein Fehler dieser zusätzlichen Prüfung betrifft nur die Archivquelle; die bisherige Kartenerkennung und unabhängige Quellen bleiben erhalten. Cancellation propagiert und der Leser wird geschlossen. Keine neuen PSO-, Schreib- oder Authentisierungsbefehle.

## Dekodierung und Herkunft

0501 enthält Kartentyp, ursprüngliche zwei Versionsbytes und unsigned Speicherkapazitäten. Gen1 hat einen einbytigen Ortszähler, Gen2 einen zweiByte-Zähler. Eine15Byte-Datei besitzt keinen Fahrtenschreiberzähler; dafür wird kein Wert angenommen. Kapazitäten0 oder65535 bleiben als Rohwerte erhalten und erweitern keine Grenzen vorhandener Aktivitäts-/Ereignisleser.

0525 liefert die vier unsigned Kapazitätsfelder einschließlich VU-Konfigurationsspeicher. Die deklarierte Folgedatenlänge muss8 sein. Die Angabe des Konfigurationsspeichers ist keine Übernahme von EF0540.

0520 verwendet denselben bestehenden Identitätsdecoder wie die Profilbindung; zusätzlich bleiben die drei Zeichensatzcodes und alle143Originalbytes erhalten. Die dekodierte Identität muss exakt dem berichteten DriverCard-Objekt entsprechen. Ein Original einer anderen Karte wird als ungültige Quelle behandelt, im direkten Speichern vor jeder Mutation abgewiesen. Kein neues Profilmatching, keine Identitätsbindung aus Anwendungskapazitäten. Kartenlaufzeiten werden nicht zu Aktivitäten oder Importzeitabdeckung.

Die bestehenden Schema18-Metadatentabellen tragen drei neue bekannte `ORIGINAL_`-Arten. Metadaten-, Zertifikats- und Originalquellen werden getrennt dekodiert; unbekannte gespeicherte Arten werden weiterhin abgewiesen. Fingerprint umfasst physische Karten-ID, Art, Generation und vollständige Bytes. Atomare Rückrolle, unabhängige Deduplizierung, exakte Wiederholung derselben Anforderung und Laden nur der ausgewählten Sitzung. Ältere19-/23-Quellen-Übernahmen behalten ihre ursprünglichen Dateien und Fingerprints; keine Nachbefüllung.

Neue Gen2V2-Lesevorgänge können26 unterstützte Quellen enthalten. Fehlende/ungültige EFs bleiben sichtbar; keine Behauptung vollständiger Kartenabdeckung oder eines signierten Standarddownloads.

## Oberfläche und Prüfung

Fahrerkarte → Details → Anwendungs- und Identifikationsdateien. Kapazitäten sind als Speicherplätze beschriftet, keine tatsächlichen Eintragszahlen. Fehlende ältere Quellen bleiben fehlend. Kartennummer anfangs maskiert, auf Antippen sichtbar und bei Art-/Quellen-/Sitzungs-/Ansichtswechsel wieder verborgen. Keine Rohdaten in Logs oder Rückmeldungen.

Neue Tests:13 Parser/Dateigrenze,6 tatsächlicher DriverCardReader,11 SQLCipher-Importtests und9 Compose/AppRoot-Tests. Projektgrenzen, zehn CI-Guard-Tests und Diffprüfung lokal bestanden. Vollständige Android-CI mit Build, Unit, Lint, APK/Updatepaar, KSP-Schema18 und Room/SQLCipher/Compose am endgültigen Produktkopf erforderlich. Hardwarebestätigung bleibt separat; kein neuer Import-Merge vor Tobis späterer ausdrücklicher Testbestätigung.
