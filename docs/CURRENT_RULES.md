# FahrerTagebuch - aktuelle verbindliche Regeln

Aktuelle Regeln ohne Chronologie. Einstieg: [WORK_START.md](../WORK_START.md). Dieser Text entscheidet über Produkt- und Arbeitsregeln; technische Implementierung und offene Arbeit stehen getrennt in Status und Übergabe. Historische Dateien begründen keine aktuellen Anforderungen.

## Produkt

- Lokale Single-Driver-App auf Grundlage des bestehenden Fahrerkarte-Imports. Eigene Quellen, Tätigkeiten, Kalender/Tageschronik, belegte Fahrzeuge, GNSS und Importhistorie bleiben erhalten.
- Vollständig gestrichen: Spesen/Reisekosten, Reise-/Abwesenheitsberechnung, Heimkehr/Übernachtung, Heimatstandort/Heimradius samt Standort- und Einrichtungslogik, Lohn/Gehalt, Arbeitgeberzuschläge, bezahlte oder lohnrelevante Arbeitszeit sowie gesamte Abrechnung. Keine spätere Umsetzungsphase daraus ableiten.
- PDF-Abrechnungs-/Lohnexport entfällt. Neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant. Alte Entities, Schnittstellen, Mockups und Formulare erzeugen keinen Auftrag.
- Oberfläche, Navigation, Einrichtung und Onboarding später grundsätzlich neu konzipieren. Zielrichtung: etwa vier Hauptbereiche Start, Kalender, Auswertung, Einstellungen; konkrete Struktur und Schrittzahl noch offen. Abrechnung ist kein Zielbereich. Jetzt kein großer UI-Umbau.
- Getrennte spätere Komfort-/Backup-/Cloud-/Sync-Planungen, optionale Google-Anmeldung, Sprache und Heimatland bleiben außerhalb des aktuellen Auftrags. Heimatland als Profil-/Sprachangabe ist kein Heimatstandort; lokale Kernnutzung nicht unnötig blockieren.

## Kartenidentität und unveränderliche Herkunft

- Erste eigene Karte ausdrücklich bestätigen. Dauerhafte Identität ist das lokale Fahrerprofil: DriverProfile -> DriverCard(s) -> ImportSession(s), mit Gruppen-/Anwendungsbelegen.
- Belastbar gleiche Inhaberidentität erlaubt Folge-/Ersatzkarten; unklare Fälle brauchen Bestätigung. Fremde Karten nicht dauerhaft mit eigenem Profil/Datenbestand vermischen.
- Gen1 und Gen2 derselben physischen Karte sind eigene Anwendungsquellen; Gen2V2 ist kein dritter AID-Zweig. MF-Belege einmal am Parent. Beide Zeitsätze nicht addieren.
- Alte Karten, Imports, Request-Fingerprints und getrenntes Altarchiv erhalten. Kein Backfill, keine vermutete Zuordnung und kein jüngerer Quellenersatz für ausgewählte historische Sitzungen, auch bei identischen Bytes.
- Originalbytes und konkrete Karten-/Anwendungs-/Sitzungsbindung erhalten. Fehlend, unlesbar, nicht versucht, unbekannt und gültig leer unterscheiden; unvollständige Daten nicht als vollständig darstellen.
- Sensible Nummern maskieren und nach Auswahlwechsel erneut verbergen. Keine sensiblen Rohdumps oder vollständigen APDUs in Logs/Fixtures. Room/SQLCipher und Keystore-Schutz erhalten; kein Klartext-Fallback oder destruktive Migration. Schemaexporte ausschließlich aus dem echten Compiler.

## Regulärer Import und Sicherheitsgrenzen

Kompatiblen Leser und Karte im Vordergrund erkennen, zulässige eigene unterstützte Daten automatisch lesen und atomar speichern. Android-USB-Freigabe sowie erste/unklare Kartenbestätigung bleiben erforderlich. Fortschritt/Ergebnis verständlich zeigen; technische Test-/Speichern-Knöpfe sind keine Zielbedienung. Funktionierenden Reader, Parser, Persistenz, Migrationen und Tests erhalten.

Signaturaufnahme, Originalquellenexport, Standarddownload und Vertrauenskette separat belegen. Ohne erforderliche Originalsignaturen/Standardvorgänge keinen vollständigen DDD-/Standarddownload behaupten. Automatischer Import bleibt lesend; Card_Download-Schreiben erst im getrennten geprüften Downloadpfad. `7F66`-Inhalte nur mit belegtem Primärformat dekodieren. Geschützte `0540` nur mit legitimen SC5-Voraussetzungen; keine erfundenen Schlüssel, Authentisierung, Signaturen oder Vertrauensbehauptungen.

## Neutrale Darstellung

- Fahren, andere Arbeit, Bereitschaft und Pause/Ruhe behalten ihre Quellbedeutung. Anzeige, Dauern und zeitliche Zusammenfassungen sind erlaubt; keine bezahlten oder abrechnungsrelevanten Kennzahlen daraus machen.
- Tageszeitleiste mit Farbe/Symbol je Tätigkeit, Übersicht, Zoom, Abschnittsauswahl und exakten Zeiten bleibt fachliche Grundlage. Fehlende/unbekannte Zeiten und offene Fahrzeugenden nicht ergänzen. Kartenentnahme beweist keine Ruhe.
- Ereignisse, Störungen, Sonderbedingungen, Grenz-/Ländereinträge und Marker nur aus belegten Quellen. Technische Meldungen nicht automatisch als Verstoß/Verschulden behandeln. Navigationsplätze später festlegen.
- GNSS: echte Kartenpunkte mit Zeit, Position, Punktart und Quelle erhalten. Spätere Karte darf sie chronologisch zu einem klar approximierten groben Verlauf verbinden. Punkte und Linien unterscheidbar; keine tatsächlich gefahrene Straßenroute, erfundenen Zwischenmesspunkte oder gefahrenen Kilometer aus Linien. Lücken/Unbekanntwerte sichtbar lassen.
- Keine Smartphone-Routenaufzeichnung/Hintergrundortung. Smartphone-Standort ist keine Voraussetzung für Karten-GNSS. Bestehender HomePointPicker und zugehörige Berechtigungen sind technischer Altbestand bis zum späteren gezielten Codeabgleich.
- Fahrzeuge: nur belegte Identität, Kennzeichen, Staat, VIN, Nutzungszeiten und Tacho-Quellwerte; Hersteller/Modell/Antrieb/Baureihe nur belastbar zuordnen. Echtes rechtlich nutzbares Modellbild bei sicherem Modell, sonst Herstellerlogo oder neutrales Symbol. Keine KI-Modellbilder oder Laufzeit-Hotlinks; gleiche Identität konsistent darstellen.

## Git, CI und Merge

- Vor Änderungen tatsächlichen Branch/HEAD, Diff, aktuellen main, relevante offene PRs und CI prüfen. Vorhandene Arbeit sichern; kein Reset, Verwerfen, Löschen von Daten/Branches/Artefakten oder Neuaufbau bestätigter Arbeit ohne ausdrückliche Freigabe.
- Der Signaturplan-Meilenstein ändert nur Dokumentation und erzeugt keine Produktfunktion oder Hardwarefreigabe. Die getrennte Folgeimplementierung richtet sich nach dem dokumentierten Plan und den eigenen Produktgates. Browser und Unteragenten nicht einsetzen. Budget-/Stop-usage-Einstellungen und bestehende Auslöser unverändert lassen; Actions-Nutzung ist freigegeben, aktuelle Budgethöhe nicht behaupten.
- Leichte CI nur für bestehende explizit erlaubte Dokumentationspfade. Eingebettete App-Assets, Build/CI, Schemata, Skripte und gemischte Änderungen erfordern unverändert volle Android-CI. Keine Klassifikation oder Prüfgrenze für schnellere Ergebnisse lockern.
- Volle CI: alle sieben vorgeschriebenen Gradle-Aufrufe, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexportprüfung, vollständige SQLCipher-/Compose-Suiten auf API 36 und Abschlussjob; keine Filter/Quarantäne. Erfolgreiche normale CI ohne Artefaktuploads; Fehlerdiagnostik drei Tage. Details nur bei Bedarf in [CI_ARTIFACT_POLICY.md](CI_ARTIFACT_POLICY.md).
- Relevante Hardware-/Import-/Parser-/Persistenz-/DB-/Schema-/Sicherheitsänderungen benötigen eigene passende CI und erforderliche Gerätebestätigung vor Merge. Gerätetest spätestens jeder dritte Produkt-PR, früher bei diesen Änderungen. Frühere konkrete Ausnahmen/Bündelungen nicht pauschal übertragen; reine Docs-Merges umgehen keine Produkt-Sperre.
- Gerätetest-Anleitungen immer verständlich direkt im Chat geben; Repository-/ZIP-Anleitungen sind nur Ergänzungen. Rückmeldungen und sichtbare Einzelmesswerte getrennt und ohne zusätzliche Hardware- oder Kurvenannahmen dokumentieren.
- Vor Merge exakten endgültigen Kopf, benötigte grüne Checks, Mergefähigkeit und Freigabegrenzen prüfen. Gerätebestätigungen für 40124/40125 gelten jeweils nur für ihren konkret bestätigten Produktbaum und tatsächlichen Umfang, nicht für spätere Änderungen oder pauschal für weitere Hardware/Kurven.

Die begrenzte Lese-, CI-Warte- und Sitzungsroutine aus WORK_START.md gilt dauerhaft. Aktuelle Kerntexte ersetzen frühere Checkpoints; Historie liegt in Git und gekennzeichneten Belegen.
