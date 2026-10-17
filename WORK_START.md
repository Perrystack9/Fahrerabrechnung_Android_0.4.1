# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für den aktuellen M4-Schritt den [M4-Arbeitsvertrag](docs/VIOLATION_ENGINE_M4_IMPLEMENTATION.md) öffnen. M3-Integrations-/Quellenbelege und ältere M0–M2-Dokumente nur bei konkretem Bedarf.

Das sind **fünf Startdateien einschließlich WORK_START.md**. Keine pauschale Markdown-Volltextlektüre oder Rekonstruktion alter Chats, wenn das Repository ausreicht.

## Aktuelle Produktgrenzen

- Bestehenden Fahrerkarte-Import vollständig erhalten; Fahr-, Arbeits-, Bereitschafts- und Ruhezeiten dürfen neutral angezeigt und ausgewertet werden.
- Spesen/Reisekosten, Heimkehr/Übernachtung, Heimatstandort samt Einrichtung, Lohn/Arbeitgeberzuschläge, lohnrelevante Arbeitszeit und Abrechnung sind vollständig gestrichen.
- PDF-Abrechnungs-/Lohnexport entfällt; neutraler Tätigkeits-/Fahrtenbericht ist nicht verbindlich geplant.
- GNSS bleibt: echte Punkte und später gekennzeichneter grober chronologischer Verlauf; keine behauptete gefahrene Straßenroute oder Kilometer aus Verbindungslinien.
- Priorität für1.0: vollständige zeitbezogene Verstoß-Engine nach Verordnung (EG) Nr.561/2006; danach Kalender, Auswertungen und neue Oberfläche/Navigation/Onboarding. Zielrichtung etwa vier Bereiche **Start, Kalender, Auswertung, Einstellungen** bleibt offen; konkrete Anforderungen/Umsetzung separat planen.

## Nachweislich fertig und offene Grenzen

**40128 / unverändertes Room-Schema 22 übernommen und im tatsächlich getesteten eigenen Umfang bestätigt.** [PR #85](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/85), Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405`; vollständiger identischer Baum `066dc0532fd2de486def695323b74f8e9d1150bd` zum endgültigen CI-/Gerätekopf `7f5f770aa71ec70bb8f02d51cae3403a6f904637`. Volle CI [38547729369](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38547729369) und exaktes Gerätepaket [38553890752](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38553890752), jeweils Versuch 1, erfolgreich. Tobi bestätigt nach der vollständigen neunteiligen Chat-Testanleitung am **14.10.2026 13:47 Europe/Berlin: „Funktioniert alles wie es soll 👍“**. [Genauer Gerätebeleg und Grenzen](docs/DEVICE_CONFIRMATION_40128.md).

**Die mathematische Verifikation sämtlicher derzeit gespeicherter Kartendateisignaturen ist nach A1–A8 abgeschlossen.** 0504/0505 und 0501/0520/0502/0503 für GEN1/GEN2/GEN2V2; 0525 ausschließlich GEN2V2 bei vorhandenem Beleg. Gemeinsame geprüfte RSA-/ECDSA-Engine, konsistenter gefrorener Read-Snapshot, exakte Karte/Child/EF/Original/Receipt/Zertifikatsbindung und getrennte Mathematik-/Vertrauensanzeige. Fehlende Voraussetzungen bleiben begründet nicht prüfbar; Zertifikatsvertrauen stets NOT_EVALUATED.

Keine DB-Schreiboperation, Migration, Cache, Backfill, neue Signaturaufnahme oder jüngerer Quellenersatz. Alte Aufnahmestatus/Fingerprints, Room22 und sämtliche Sicherheits-/CI-/Migrations-/Geräteprüfungen erhalten. Bestätigte Vorgänger 40124–40127 nicht erneut bearbeiten. [Abgeschlossener Plan](docs/CARD_STORED_SIGNATURE_COMPLETION_PLAN.md), [technische Nachweise](docs/CARD_STORED_SIGNATURE_MATH_EVIDENCE.md).

Zusätzliche volle Produkt-Main-CI [38559255131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38559255131), Versuch1, für Produktmerge `c4057b3810d1ad0f5ef8e27cf69b260acd9c8405` jetzt vollständig erfolgreich; alle vier Jobs einschließlich ungekürzter API36-SQLCipher-/Compose-Instrumentierung und Abschlussgate. Am14.10.2026 nach Übergabeauftrag live bestätigt. Keine offenen40128-Prüfungen; Produktmerge besitzt exakt den endgültigen CI-/Gerätebaum.

## Nächste Arbeit und Übergabe

**M3/40131/Room23 technisch abgenommen und PR91 kontrolliert integriert; M4 ausdrücklich genehmigt.** Neue Basis **`ba0cf1933f8d01590d8fdef32526f1715ec8d571`**, exakt identischer vollständiger Baum zum endgültigen CI-/Gerätepaket40131. Eigene Geräteantwort16.10.2026,09:55 Europe/Berlin positiv. [M3-Integration und Freigabe](docs/M3_INTEGRATION_20261016.md). Neue vollständige Main-CI312/Versuch1 mit allen vier Jobs erfolgreich.

M4-Branch `feature/v1-m4-weekly-rest-20261016`, Checkout `/workspace/scratch/8d9b62deaae5/FahrerTagebuch-m4`. [Arbeitsvertrag](docs/VIOLATION_ENGINE_M4_IMPLEMENTATION.md): W01–W14, Wochenruhe und global zugeordneter Ausgleich auf M1–M3. M4-Prüfkandidat mit801 erfolgreichen lokalen Unit-Tests, darunter282 Domain-/110 neue M4-Referenzmethoden, ohne Fehler/Skip. Alle sieben lokalen Gradle-Gates, Signatur/Updatepaar/Schema und vollständige Android-Testkompilierung erfolgreich. Exakter endgültiger Kopf und volle GitHub-CI werden im M4-PR dokumentiert; deren tatsächlichen Status zuerst prüfen. Eigene40132-Gerätebestätigung und M4-Abnahme stehen aus. Zwischenstände regelmäßig auf GitHub sichern. Finalen Prüfkandidaten erst nach abgeschlossener lokaler Arbeit veröffentlichen und während der vollständigen CI einfrieren.

L02-Wochen-/Nachtzeit, L03-Konventionen, L04-Wochen-/Ausgleichszuordnung, L09-Anrechnungen und übrige fallbezogene Fachgrenzen bleiben offen. Keine gesicherten Budgets aus bedingten Tatsachen/Zuordnungen. Drei Tagesruhereduzierungen zwischen qualifizierenden Wochenruhen und zwei Tageslenkverlängerungen je Woche bleiben getrennt;23h sind kein Wochenreset. UTC rechnet; spätere historische Ortszeitanzeige M5/M6 ändert keine Rechtswerte.

**Kein M4-Merge, M5–M7 oder Release ohne neue ausdrückliche Freigabe.** Alle80/A–H/E01–E12/P01–P05 sowie Sicherheits-/Herkunfts-/CI-/Gerätegates gelten weiter. Unabhängige qualifizierte Fachprüfung und Beta5–10 Fahrer bleiben Releasegates.

Frühere automatische Workspace-Wartung entfernte die alte uncommittete Settings-Arbeit. M1 ist aus GitHub wiederhergestellt; der alte unvollständige Entwurf nicht. [Verlustgrenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Nicht rekonstruieren oder als vorhanden melden. Alle vorhandenen Dateien/Branches/Backups erhalten, kein Reset/Clean.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
