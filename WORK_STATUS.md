# FahrerTagebuch – aktueller Arbeitsstand

**12.10.2026, nach Tobis Produktentscheidung um 20:53 Europe/Berlin.**

Zielprodukt: [Produktentscheidung](docs/PRODUCT_DECISION_20261012.md) und [aktuelle Regeln](docs/CURRENT_RULES.md). Neue Planung ist kein Nachweis einer bereits umgebauten Oberfläche.

## Bestätigte Basis

| Bezug | Stand |
|---|---|
| Testversion / Room | **40122 / Schema 19** |
| PR65 bestätigter Produktkopf | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Unveränderter Produktmerge | `21edb80ce46c0202204bb74d83486c634b3c90c6` |
| Bytegleicher bestätigter Produkt-Tree | `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` |
| Main vor dieser Docs-Entscheidung | `9d0c911d02ae39a29120224e813ba551b1a1e54f`, reiner Docs-Folgeabschluss PR66 |
| Vollständige Produkt-Main-CI | [38281475535](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38281475535), erfolgreich einschließlich SQLCipher/Compose |
| Docs-Main-CI PR66 | [38284000060](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38284000060), erfolgreich |
| Gerätebestätigung | 12.10.2026, 18:57 Europe/Berlin, exakt 40122; [Beleg/Grenzen](docs/DEVICE_CONFIRMATION_40122.md) |

Tobi bestätigt exakt: „Version 40122 installiert, alte Daten erhalten, sichtbare Gen1-/Gen2-Zustände, passende Details und Wiederöffnung ohne Leser erfolgreich“.

Keine zusätzlichen manuellen Einsteck-/Deduplizierungs-/Neustart-/Fremdkarten- oder Einzelmessfälle behaupten. Relevante Folgeänderungen sind dadurch nicht freigegeben.

## Erhaltene Implementierung

Automatische Vordergrund-Lesung eigener Daten, USB-Freigabe, eigene Profil-/Kartenbindung, unabhängige Quellen, additive SQLCipher-Persistenz und exakte Historie sind vorhanden. PR65: gemeinsame atomare Gruppen für Gen1/Gen2 derselben Karte, MF-Belege einmal am Parent, getrennte Child-Sitzungen. Keine Addition beider Zeitsätze, Nachbefüllung oder jüngere Ersatzquelle.

Tätigkeitsparser, Tagesprojektion, Quellzeit-Zusammenfassungen, Kartentag-/Fahrzeugvorschau, vorhandene Marker und unterstützte GNSS-/Orts-/Grenz-/Lade-/VU-/Metadaten-/Zertifikats-/MF-Quellen bleiben erhalten. [Quellenabdeckung](docs/CARD_SOURCE_COVERAGE.md). Kartensignaturen, Standarddownload und produktives Kartenvertrauen offen.

Öffentliche Zertifikatsmathematik begrenzt untersucht: [Gen1-Beleg](docs/CARD_GEN1_PUBLIC_CERTIFICATE_AUDIT.md). Keine App-Vertrauensfunktion oder Benutzerkartenprüfung behaupten.

## Vollständig gestrichene Ziele

Spesen/Reisekosten, Reise-/Abwesenheitsberechnung, Heimkehr/Übernachtung, Heimatstandort/Heimradius samt Einrichtung/Standortlogik, Lohn/Gehalt/Arbeitgeberzuschläge, lohn-/abrechnungsrelevante Arbeitszeit und gesamte Abrechnungslogik entfallen. Handoff-Punkte 8/9 aufgehoben, keine spätere Phase.

PDF-Abrechnungs-/Lohnexport entfällt. Neutraler Tätigkeits-/Fahrtenbericht nicht verbindlich geplant. Vorhandene Entities, Schnittstellen, Platzhalter, Mockups und PDF-Spezifikationen sind kein Fertigstellungsauftrag.

## Aktuelle App gegenüber zukünftigem Konzept

Bestehende Navigation mit Abrechnung, Einrichtung und Heimatpunkt-/Standort-/Konfigurationsartefakte sind in diesem Dokumentationsschritt **nicht entfernt**. Tests, CI-Guards, Profile und Room-Schemata bleiben unverändert.

Später gesamte Oberfläche, Navigation, Einrichtung und Onboarding grundsätzlich neu konzipieren. Abrechnung entfernen/ersetzen; Tabstruktur/Schrittzahl offen. Keine Heimat-/Spesen-/Gehalts-/Zuschlagsfelder. Späterer gezielter Code-/Berechtigungs-/Datenmodellabgleich muss Importdaten erhalten; keine destruktive Migration aus der Entscheidung ableiten.

Tätigkeiten, neutrale Dauern/Auswertung bleiben erlaubt. Spätere GNSS-Karte mit sichtbaren echten Punkten und klar approximiertem chronologischem Verlauf zulässig, noch nicht umgesetzt. Keine gefahrene Straßenroute oder Smartphone-Ortung.

## Fortsetzung und Prüfgrenze

1. [Import-Folgeplan](docs/CARD_IMPORT_NEXT_STEPS_AFTER_PR65.md): Pflichtdateien-/Signaturmatrix, echte Signatur-/Bytebindung, Zertifikats-/Vertrauensverfahren und begrenzter Quellenexport.
2. Danach neutrale Tätigkeit-/Fahrzeugauswertung und spätere UI-/Einrichtung-/GNSS-Kartenkonzeption.
3. Relevante Produktänderungen brauchen eigene passende CI und erforderlichen Gerätetest vor Merge.

Bisherige getrennte Komfort-/Backup-/Cloud-/Sync-Planungen bleiben spätere Punkte; durch die Umfangsentscheidung weder gestrichen noch jetzt implementiert.

Jetzt ausschließlich Dokumentation: Code, Build/CI, Tests, funktionale Assets und alle Room-Schemata unverändert. Nur die gebündelte historische Markdown-PDF-Spezifikation erhält ebenfalls einen Überholt-Hinweis. Nach bestehender konservativer CI ist dieser Report-Asset-Pfad trotzdem vollständig auf Android zu prüfen; kein Scope-Bypass.

[Historischer vollständiger Status](docs/history/WORK_STATUS_BEFORE_PRODUCT_DECISION_20261012.md) mit damaligen Test-/Checkpointbelegen erhalten; alte Spesen-/Lohn-/Heimat-/PDF-Planungen überholt.
