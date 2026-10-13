# Eigene Geräteabnahme 40124 - Kernsignaturen

**ABNAHME BESTÄTIGT**, Tobi am 13.10.2026 13:57 Europe/Berlin; [genauer Beleg](DEVICE_CONFIRMATION_40124.md). Produkt-PR #73 gemergt, getesteter Kopf `a2e22d882bb707fe1a446c3a24f78387caa892b0`, eigene volle CI 38381962283 und exaktes Paket 38388192092 erfolgreich. Diese Checkliste dokumentiert den eigenen 40124-Umfang; keine Freigabe für spätere geänderte Produktstände. Neue EF0504-/EF0505-Signaturaufnahme und additive Schema 19→20-Migration wurden eigenständig geprüft.

## Paket und Installation

Vorhandenen Workflow **FahrerTagebuch targeted device-test package** auf main starten: `source_sha` gleich dem endgültigen vollständig grünen PR-Kopf, `include_baseline=false`, da 40123 bereits installiert ist. APK aus genau diesem Paket als Update installieren; App-Daten/Profil behalten. Infrastrukturkopf des Workflows ist nicht der Produktkopf des APK. Keine Paketfreigabe bei Docs-CI oder unvollständiger/skipped Android-CI.

## Erforderliche Rückmeldung

1. **Update/Altbestand:** Profil und alte Imports unverändert verfügbar. Bei alter Quelle „Signatur in diesem Import nicht aufgenommen“; keine nachträglich angehängten Signaturen.
2. **Eigene neue Aufnahme:** Reader/Karte verbinden, automatischen Import abwarten. Kartendaten öffnen, GEN1 und GEN2V2 separat wählen. Jeweils Tätigkeiten und Fahrzeuge mit „aufgenommen · ungeprüft“ und echter Länge. GEN1 je 128 Bytes; GEN2V2 nach tatsächlichem C101, mögliche Längen 64/96/128/132, keine pauschale 64-Byte-Annahme. Originalgrößen und fachliche Inhalte plausibel; 13780/6202 und 13780/9602 Bytes waren der vorherige konkrete Stand, neue Kapazitäten/Zeiten nicht aus diesen Beispielen erfinden.
3. **Wiederöffnung/Quellenwechsel:** Reader trennen, App schließen und neu öffnen. Neue gespeicherte Signaturen/Originale erhalten. Mehrfach zwischen Anwendungen, alter und neuer Sitzung sowie Altarchiv wechseln; Status/Länge nur für die ausgewählte Quelle. Alte Quellen bleiben ohne neuen Beleg.
4. **Erneute Aufnahme:** Eigene Karte nochmals lesen. Neue Sitzung und ihre eigenen Belege vorhanden; fachliche Daten nicht doppelt addiert. Deduplizierte Originale dürfen gleiche Größe/Bytes besitzen, die Signaturen bleiben an der neuen Sitzung.
5. **Abbruch/Neustart:** Einen neuen laufenden Leseimport durch Kartenentnahme abbrechen. Verständlicher Abbruch und keine neue Teilgruppe/Signaturquelle. Neu verbinden und vollständigen neuen Import abwarten; kein weiterverwendeter alter Hashzustand.

Bitte tatsächlichen Geräte-/Leserumfang, getestete Quelle und Ergebnis festhalten; Screenshots mit Signaturstatus/-längen genügen. Keine Roh-/APDU-/Schlüsseldumps. Bei Fehler genauen sichtbaren Zustand und Phase des Abbruchs nennen; das ist ein offenes Gate, keine automatisch akzeptierte Ausnahme.

## Grenzen

Aufgenommen heißt **ungeprüft**: keine mathematische Signaturprüfung, ERCA/MSCA-Vertrauenskette, vollständige Kartenabdeckung, DDD-/Standarddownload oder LastCardDownload-Schreibfreigabe. Alle sechs Kurven, leere/beschädigte Dateien und jede APDU-Abbruchphase brauchen automatisierte Tests; reale reine GEN2-Karte oder seltene Hardwarefehler nicht ohne konkreten Beleg als gerätegeprüft ausgeben. Erst eigene dokumentierte Bestätigung des genannten Kernumfangs erlaubt Merge des exakt getesteten Produktbaums.
