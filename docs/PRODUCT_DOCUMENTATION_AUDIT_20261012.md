# Dokumentationsabgleich – neue Produktentscheidung 12.10.2026

## Umfang und Grundlage

Tobis Entscheidung12.10.2026 um 20:53 Europe/Berlin wurde gegen den exakten Main-Stand9d0c911d02ae39a29120224e813ba551b1a1e54f geprüft. Sämtliche **73 vorhandenen Markdown-Dateien** vollständig als Text erfasst und auf Spesen/Reisekosten, Lohn/Gehalt/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung, PDF, Navigation/Onboarding und GNSS-Verlauf durchsucht. Zusätzlich übrige Text-/JSON-/Konfigurationsdokumentation und vorhandene Logs eingeordnet; keine weiteren PDF-/Word-/HTML-/RST-/AsciiDoc-Dokumente im Ausgangsbaum vorhanden.

Der bisher ebenfalls als verbindlich verlinkte Berater-Handoff in **Issue #3** wurde vollständig gelesen und in eine aktuelle Fassung überführt. Originaltext als historisches Archiv erhalten. Kein neuer Kommentar, keine Nachricht an Dritte.

Verbindlicher Zielumfang: [PRODUCT_DECISION_20261012.md](PRODUCT_DECISION_20261012.md). Aktuelle Regeln, README, Status und Work-Handoff getrennt von alten Chronologien neu gefasst. Alte vollständige Fassungen erhalten, nur relative Archivlinks angepasst.

## Ergebnis

- Ganze bisherigen Handoff-Punkte 8/9 gestrichen: Spesen/Reisekosten, Reise-/Abwesenheitsberechnung, Heimkehr/Übernachtung, Lohn/Gehalt, Arbeitgeberzuschläge und lohn-/abrechnungsrelevante Arbeitszeit.
- Heimatstandort samt Einrichtung, Standortabfrage und Heimradius entfällt.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht nicht verbindlich geplant.
- Abrechnung kein Ziel-Hauptbereich. Gesamte Oberfläche, Navigation, Einrichtung und Onboarding später grundsätzlich neu konzipieren; konkreter Ersatz, Tabs und Schritte offen.
- Tätigkeiten und neutrale Dauern/Auswertung bleiben erhalten.
- GNSS-Karte später mit echten sichtbaren Punkten und klar approximiertem chronologischem Verlauf erlaubt; keine tatsächlich gefahrene Straßenroute, erfundenen Messpunkte oder Kilometer aus Linien.
- Import, bestätigte Daten, bestehende Oberfläche und CI-Regeln in diesem Schritt erhalten.

## Erhalt und Prüfung

Alle **297 Nicht-Markdown-Dateien** aus dem Ausgangsbaum sind anhand ihres Git-Blob-SHA bytegleich: funktionaler Code, Reader/Parser, Datenbank/Migrationen und sämtliche Schemaexporte, Tests, Build-/CI-Skripte/-Workflows, Konfiguration und funktionale Assets. Keine Dateien gelöscht. Keine neue Version, Migration oder Gerätefreigabe.

Historische Test-/Checkpoint-/Designbelege mit neuem Hinweis behalten ihren Originaltext vollständig. Die vier zentralen bisherigen Fassungen und Issue #3 wurden archiviert; relative Markdown-Links wurden auf ihre tatsächlichen Ziele umgerechnet. Ein historischer Markdown-Zeilenumbruch mit zwei Leerzeichen wurde als explizites HTML-br erhalten, damit die Diff-Prüfung sauber bleibt. Historische Aussagen sind weder neue aktuelle Anforderungen noch neu behauptete Tests.

Auch die alte Markdown-PDF-Spezifikation unter report/src/main/assets/design ist als überholt gekennzeichnet. Das ändert nur Dokumentation im gebündelten Asset. Nach der unveränderten konservativen CI-Klassifikation erfordert dieser Pfad trotzdem die **vollständige Android-CI**, keine leichte Docs-CI. Kein Scope-Bypass und kein ungeprüftes APK-/Hardware-Ergebnis.

Vor Veröffentlichung: Diff-/Linkprüfung, projektweite semantische Suchprüfung, Git-Blob-Vergleich und Erhalt der historischen Originaltexte. Bestehende Boundary-/CI-Guard-/Shellprüfungen laufen unverändert. Konkrete GitHub-CI-Ergebnisse sind dem PR-/Main-Lauf zu entnehmen; der vorherige erfolgreiche40122-Lauf ersetzt den Lauf dieser Dokumentationsänderung nicht.

## Geänderte Dateien

Die folgenden Gruppen enthalten sämtliche Änderungen dieses Abgleichs. Historische Belege erhalten nur einen klaren Vorrang-/Überholt-Hinweis; technische Folgepläne werden direkt auf den neuen Umfang angepasst.

### Aktuelle Entscheidung und zentrale Einstiegspunkte

- [README.md](../README.md)
- [WORK_STATUS.md](../WORK_STATUS.md)
- [docs/CURRENT_RULES.md](CURRENT_RULES.md)
- [docs/PRODUCT_DECISION_20261012.md](PRODUCT_DECISION_20261012.md)
- [docs/PRODUCT_DOCUMENTATION_AUDIT_20261012.md](PRODUCT_DOCUMENTATION_AUDIT_20261012.md)
- [docs/WORK_HANDOFF.md](WORK_HANDOFF.md)

### Erhaltene historische Gesamtfassungen

- [docs/history/CURRENT_RULES_BEFORE_PRODUCT_DECISION_20261012.md](history/CURRENT_RULES_BEFORE_PRODUCT_DECISION_20261012.md)
- [docs/history/HANDOFF_ISSUE3_BEFORE_PRODUCT_DECISION_20261012.md](history/HANDOFF_ISSUE3_BEFORE_PRODUCT_DECISION_20261012.md)
- [docs/history/README_BEFORE_PRODUCT_DECISION_20261012.md](history/README_BEFORE_PRODUCT_DECISION_20261012.md)
- [docs/history/WORK_HANDOFF_BEFORE_PRODUCT_DECISION_20261012.md](history/WORK_HANDOFF_BEFORE_PRODUCT_DECISION_20261012.md)
- [docs/history/WORK_STATUS_BEFORE_PRODUCT_DECISION_20261012.md](history/WORK_STATUS_BEFORE_PRODUCT_DECISION_20261012.md)

### Weitere abgeglichene Dokumentation

- [BUILD_STATUS.md](../BUILD_STATUS.md)
- [BUILD_VERIFICATION_REPORT_0.4.1.md](../BUILD_VERIFICATION_REPORT_0.4.1.md)
- [GITHUB_ACTIONS_HANDOFF.md](../GITHUB_ACTIONS_HANDOFF.md)
- [PROJECT_START_REPORT.md](../PROJECT_START_REPORT.md)
- [ci/README.md](../ci/README.md)
- [docs/AUTOMATIC_IMPORT_DEVICE_TEST.md](AUTOMATIC_IMPORT_DEVICE_TEST.md)
- [docs/BORDER_CROSSINGS_DEVICE_TEST.md](BORDER_CROSSINGS_DEVICE_TEST.md)
- [docs/BORDER_CROSSINGS_IMPORT.md](BORDER_CROSSINGS_IMPORT.md)
- [docs/CARD_APPLICATION_STORAGE_DESIGN.md](CARD_APPLICATION_STORAGE_DESIGN.md)
- [docs/CARD_CERTIFICATE_DEVICE_TEST.md](CARD_CERTIFICATE_DEVICE_TEST.md)
- [docs/CARD_COMMUNICATION_DEVICE_TEST.md](CARD_COMMUNICATION_DEVICE_TEST.md)
- [docs/CARD_IMPORT_CONTINUATION_CHECKPOINT_20261012.md](CARD_IMPORT_CONTINUATION_CHECKPOINT_20261012.md)
- [docs/CARD_IMPORT_FINALIZATION_HANDOFF.md](CARD_IMPORT_FINALIZATION_HANDOFF.md)
- [docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md](CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md)
- [docs/CARD_IMPORT_SECURITY_RESEARCH.md](CARD_IMPORT_SECURITY_RESEARCH.md)
- [docs/CARD_MASTER_FILE_DEVICE_TEST.md](CARD_MASTER_FILE_DEVICE_TEST.md)
- [docs/CARD_METADATA_DEVICE_TEST.md](CARD_METADATA_DEVICE_TEST.md)
- [docs/CARD_MULTI_APPLICATION_CHECKPOINT_40122.md](CARD_MULTI_APPLICATION_CHECKPOINT_40122.md)
- [docs/CARD_MULTI_APPLICATION_DEVICE_TEST.md](CARD_MULTI_APPLICATION_DEVICE_TEST.md)
- [docs/CARD_MULTI_APPLICATION_IMPORT.md](CARD_MULTI_APPLICATION_IMPORT.md)
- [docs/CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md)
- [docs/CARD_SOURCE_COVERAGE.md](CARD_SOURCE_COVERAGE.md)
- [docs/CI_ARTIFACT_POLICY.md](CI_ARTIFACT_POLICY.md)
- [docs/CONTROL_IMPORT_DEVICE_TEST.md](CONTROL_IMPORT_DEVICE_TEST.md)
- [docs/DEFERRED_TEST_HANDOFF.md](DEFERRED_TEST_HANDOFF.md)
- [docs/DEVICE_CONFIRMATION_40120.md](DEVICE_CONFIRMATION_40120.md)
- [docs/DEVICE_CONFIRMATION_40121.md](DEVICE_CONFIRMATION_40121.md)
- [docs/DEVICE_CONFIRMATION_40122.md](DEVICE_CONFIRMATION_40122.md)
- [docs/EVENT_FAULT_IMPORT_DEVICE_TEST.md](EVENT_FAULT_IMPORT_DEVICE_TEST.md)
- [docs/GNSS_ACCUMULATED_DRIVING_FORMAT.md](GNSS_ACCUMULATED_DRIVING_FORMAT.md)
- [docs/GNSS_IMPORT_DEVICE_TEST.md](GNSS_IMPORT_DEVICE_TEST.md)
- [docs/GNSS_IMPORT_IMPLEMENTATION.md](GNSS_IMPORT_IMPLEMENTATION.md)
- [docs/INDEPENDENT_READ_DEVICE_TEST.md](INDEPENDENT_READ_DEVICE_TEST.md)
- [docs/LOAD_TYPE_DEVICE_TEST.md](LOAD_TYPE_DEVICE_TEST.md)
- [docs/LOAD_UNLOAD_DEVICE_TEST.md](LOAD_UNLOAD_DEVICE_TEST.md)
- [docs/NORMALIZED_DAYS_DEVICE_TEST.md](NORMALIZED_DAYS_DEVICE_TEST.md)
- [docs/NORMALIZED_VEHICLES_DEVICE_TEST.md](NORMALIZED_VEHICLES_DEVICE_TEST.md)
- [docs/PLACES_IMPORT_DEVICE_TEST.md](PLACES_IMPORT_DEVICE_TEST.md)
- [docs/PLACES_IMPORT_IMPLEMENTATION.md](PLACES_IMPORT_IMPLEMENTATION.md)
- [docs/PR62_DEVICE_DELIVERY_40121.md](PR62_DEVICE_DELIVERY_40121.md)
- [docs/PR65_DEVICE_DELIVERY_40122.md](PR65_DEVICE_DELIVERY_40122.md)
- [docs/PROFILE_BINDING_DEVICE_TEST.md](PROFILE_BINDING_DEVICE_TEST.md)
- [docs/SPECIFIC_CONDITION_DEVICE_TEST.md](SPECIFIC_CONDITION_DEVICE_TEST.md)
- [docs/UI_REFINEMENT_0.4.1_V2.md](UI_REFINEMENT_0.4.1_V2.md)
- [docs/VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md)
- [docs/VEHICLE_UNITS_DEVICE_TEST.md](VEHICLE_UNITS_DEVICE_TEST.md)
- [docs/design-handoff-v2/00_WORK_START_HERE.md](design-handoff-v2/00_WORK_START_HERE.md)
- [docs/design-handoff-v2/04_Spezifikationen/ASSET_MANIFEST.md](design-handoff-v2/04_Spezifikationen/ASSET_MANIFEST.md)
- [docs/design-handoff-v2/04_Spezifikationen/BRAND_GUIDELINE.md](design-handoff-v2/04_Spezifikationen/BRAND_GUIDELINE.md)
- [docs/design-handoff-v2/04_Spezifikationen/ONBOARDING_SPEZIFIKATION.md](design-handoff-v2/04_Spezifikationen/ONBOARDING_SPEZIFIKATION.md)
- [docs/design-handoff-v2/04_Spezifikationen/PDF_EXPORT_SPEZIFIKATION.md](design-handoff-v2/04_Spezifikationen/PDF_EXPORT_SPEZIFIKATION.md)
- [docs/design-handoff-v2/06_Geraetetest/TESTFEEDBACK_S23_ANDROID16.md](design-handoff-v2/06_Geraetetest/TESTFEEDBACK_S23_ANDROID16.md)
- [docs/design-handoff-v2/WORK_UI_REFINEMENT_0.4.1.md](design-handoff-v2/WORK_UI_REFINEMENT_0.4.1.md)
- [report/src/main/assets/design/PDF_EXPORT_SPEZIFIKATION.md](../report/src/main/assets/design/PDF_EXPORT_SPEZIFIKATION.md)

## Unverändert geprüfte Markdown-Dateien

Kein widersprechender neuer Produktauftrag gefunden; negative Grenzen einzelner Import-/Decoderschritte und amtliche PDF-Primärquellen sind keine entfallene PDF-Abrechnungsfunktion. Technische Daten-/Quellen-/Testregeln bleiben bestehen.

- [docs/CARD_CERTIFICATE_IMPORT.md](CARD_CERTIFICATE_IMPORT.md)
- [docs/CARD_COMMUNICATION_IMPORT.md](CARD_COMMUNICATION_IMPORT.md)
- [docs/CARD_DIRECTORY_IMPORT.md](CARD_DIRECTORY_IMPORT.md)
- [docs/CARD_GEN1_PUBLIC_CERTIFICATE_AUDIT.md](CARD_GEN1_PUBLIC_CERTIFICATE_AUDIT.md)
- [docs/CARD_MASTER_FILE_IMPORT.md](CARD_MASTER_FILE_IMPORT.md)
- [docs/CARD_METADATA_IMPORT.md](CARD_METADATA_IMPORT.md)
- [docs/CARD_ORIGINAL_IDENTIFICATION_IMPORT.md](CARD_ORIGINAL_IDENTIFICATION_IMPORT.md)
- [docs/CONTROL_IMPORT_FORMAT.md](CONTROL_IMPORT_FORMAT.md)
- [docs/EVENT_FAULT_IMPORT_FORMAT.md](EVENT_FAULT_IMPORT_FORMAT.md)
- [docs/EVENT_FAULT_TIMELINE_CHECK.md](EVENT_FAULT_TIMELINE_CHECK.md)
- [docs/LOAD_TYPE_IMPLEMENTATION.md](LOAD_TYPE_IMPLEMENTATION.md)
- [docs/LOAD_UNLOAD_IMPORT.md](LOAD_UNLOAD_IMPORT.md)
- [docs/SPECIFIC_CONDITION_IMPORT_FORMAT.md](SPECIFIC_CONDITION_IMPORT_FORMAT.md)
- [docs/SPECIFIC_CONDITION_TIMELINE_CHECK.md](SPECIFIC_CONDITION_TIMELINE_CHECK.md)
- [docs/VEHICLE_UNITS_IMPORT.md](VEHICLE_UNITS_IMPORT.md)
