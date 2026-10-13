# Eigene Geräteabnahme 40124 - Kernsignaturen

**ABNAHME OFFEN.** Produkt-PR [#73](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/73), neue EF0504-/EF0505-Signaturaufnahme und additive Schema 19→20-Migration. Keine 40123-Bestätigung auf diesen Stand übertragen. Endgültigen vollen CI-Lauf und dessen exakten 40-stelligen Produktkopf zuerst live prüfen.

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
