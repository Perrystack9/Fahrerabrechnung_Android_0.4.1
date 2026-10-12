# Work-Handoff – FahrerTagebuch

**Verbindlicher Einstieg ab 12.10.2026, Produktentscheidung um 20:53 Europe/Berlin.**

## Vor jeder Fortsetzung lesen

1. [Neue Produktentscheidung](PRODUCT_DECISION_20261012.md), Vorrang vor früheren Vorgaben.
2. [Aktuelle Regeln](CURRENT_RULES.md).
3. [WORK_STATUS](../WORK_STATUS.md), [README](../README.md), tatsächlichen GitHub-/lokalen Stand.
4. [Nächste Importpunkte](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) und konkrete Implementierungs-/Testbelege.

[Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) ergänzend lesen. Frühere Punkte 8/9 vollständig aufgehoben. Alte Design-/Archivdateien dürfen gestrichene Ziele nicht wieder einführen.

## Neuer Umfang

Vollständig entfallen: Spesen/Reisekosten, Reise-/Abwesenheitsberechnung, Heimkehr/Übernachtung, Heimatstandort samt Standort-/Einrichtungslogik/Heimradius, Lohn/Gehalt/Arbeitgeberzuschläge, bezahlte/lohnrelevante Arbeitszeit und gesamte Abrechnungslogik. PDF-Abrechnungs-/Lohnexport ebenfalls gestrichen. Neutraler Tätigkeits-/Fahrtenbericht nicht verbindlich geplant.

Fahren, andere Arbeit, Bereitschaft und Pause/Ruhe erhalten, anzeigen, zeitlich zusammenfassen und neutral auswerten. Keine bezahlten, lohnrelevanten oder abrechnungsrelevanten Stunden.

GNSS aus der Fahrerkarte erhalten. Spätere Karte darf echte sichtbare Punkte chronologisch mit einem klar **approximierten Verlauf** verbinden. Keine gefahrene Straßenroute, Zwischenmesspunkte oder Kilometer aus Linien erfinden; kein Smartphone-Tracking.

## Bestätigte Ausgangsbasis

| Bezug | Wert |
|---|---|
| Test-App / DB | **40122 / Schema 19** |
| PR65 geprüfter Produktkopf | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| PR65 Produktmerge | `21edb80ce46c0202204bb74d83486c634b3c90c6` |
| Bytegleicher bestätigter Tree | `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` |
| Main vor dieser Docs-Entscheidung | `9d0c911d02ae39a29120224e813ba551b1a1e54f`, Docs-PR66 |
| Vollständige Main-CI | [38281475535](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38281475535), erfolgreich einschließlich SQLCipher/Compose |
| Gerätebestätigung | [DEVICE_CONFIRMATION_40122.md](DEVICE_CONFIRMATION_40122.md), 12.10.2026 um 18:57 Europe/Berlin |

Exakte Rückmeldung: „Version 40122 installiert, alte Daten erhalten, sichtbare Gen1-/Gen2-Zustände, passende Details und Wiederöffnung ohne Leser erfolgreich“. Keine weiteren manuellen Fälle daraus behaupten.

PR65 abgeschlossen: beide Anwendungen, gemeinsame MF-Belege, atomare Gruppenspeicherung, genaue Anwendungs-/Historienwahl. Profilbindung, Quellenmodelle, Reader, Parser und Migrationen nicht erneut bauen. Keine Nachbefüllung/Addition beider Tätigkeitsbestände.

## Jetzt Dokumentation, später grundlegende Oberfläche

**Jetzt keine großflächige UI-Neuentwicklung, kein Rückbau des Imports, keine Datenmigration.**

Aktuelle App kann noch Abrechnung, sieben Einrichtungsschritte, Heimatpunktwahl, Standortberechtigungen und alte Gehalts-/Spesenvorschauen enthalten. Technischer Altbestand, kein Zielprodukt. Bestehende Tests/CI-Boundary prüfen unveränderten Code; daraus keine künftige Heimatpunktpflicht ableiten.

Später gesamte Oberfläche, Navigation, Einrichtung und Onboarding grundsätzlich neu konzipieren. Abrechnung entfernen/ersetzen, konkrete Struktur offen. Fünf Tabs, sieben/acht Schritte, alte Kalender-/Auswertungsplätze und 1:1-Layouts sind keine feste Vorgabe. Profile und Importdaten bei späteren Änderungen erhalten.

## Nächste Priorität

Kartenimport anhand des [Folgeplans](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md) vervollständigen: Pflichtdateien/-ausnahmen, Originalquellenexport, echte Signaturaufnahme, Zertifikats-/Vertrauensverfahren. Öffentliche Forschungsbelege sind keine App-Kartenverifikation. 7F66 nur mit belegtem Primärformat, geschützte 0540 nur mit legitimen SC5-Voraussetzungen. Keine Schlüssel, Signaturen, Authentisierung oder Vollständigkeit erfinden.

Fahrzeug-/Kilometer-/weitere neutrale Timeline-Auswertung später aus belastbaren Quellen. Keine spätere Spesen-/Lohn-/Heimkehr-/Übernachtungs-/PDF-Abrechnungsphase.

## Git, CI, Hardware und Merge

- Vor Änderungen HEAD, Branch, lokalen Diff, offene PRs und tatsächliche CI prüfen. Vorhandene Arbeit sichern, exakt fortsetzen; kein Reset, Löschen, Verwerfen oder Neuaufbau.
- Kein Browser/Unteragenten. Budget-/Stop-usage-Einstellungen/Auslöser unverändert. Tobi hat Actions-Nutzung freigegeben; neuer Betrag nicht dokumentiert.
- Leichte CI nur für explizit erlaubte Dokumentationspfade. Markdown-Asset im Report-Modul fällt nach bestehender konservativer Klassifikation unter volle Android-CI; keinen Bypass einbauen.
- Normale CI: alle sieben Gradle-Aufrufe, Build/Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexportprüfung, vollständige SQLCipher/Compose-Suiten auf API 36, Abschlussjob. Keine Filter/Quarantäne.
- Erfolgreiche normale CI ohne APK-/Reportuploads; Fehlerdiagnostik drei Tage. [CI-Regeln](CI_ARTIFACT_POLICY.md).
- Neue relevante Hardware-/Import-/Parser-/Persistenz-/Schema-/Sicherheitsänderungen: eigene passende automatische Prüfung und erforderliche Gerätebestätigung vor Merge. Freigabe 40122 gilt nur für den unveränderten Testkopf.
- Vereinbarung „Gerätetest spätestens jeder dritte PR, früher bei relevanten Hardware-/Import-/Parser-/Persistenz-/DB-Änderungen“ und konkrete Ausnahmen beibehalten. Alte Bündelungen nicht pauschal übertragen.
- Diese rein dokumentarische Entscheidung erzeugt keine neue Test-App oder Hardwarebestätigung. Hardware-Sperren nicht mit Docs-Merges umgehen.

[Historischer vollständiger Handoff](history/WORK_HANDOFF_BEFORE_PRODUCT_DECISION_20261012.md) bewahrt damalige Tests, Checkpoints, Code-Einstiege und Pläne; kein aktueller Fachauftrag.
