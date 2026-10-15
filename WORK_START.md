# FahrerTagebuch - Start für Work

Lokale Android-App: eigene Fahrerkartendaten zuverlässig importieren, ihre Herkunft erhalten und Tätigkeiten, Fahrzeuge und GNSS neutral darstellen.

## Startvertrag

1. Zuerst nur diese Datei und Git prüfen: Branch, HEAD, Arbeitsbaum, aktuellen GitHub-main, relevante offene PRs und deren CI.
2. Danach [aktuelle Regeln](docs/CURRENT_RULES.md), [technischen Status](WORK_STATUS.md) und [aktuelle Übergabe](docs/WORK_HANDOFF.md) lesen.
3. Für die M1-Integration zunächst den [M1-Implementierungsbericht](docs/VIOLATION_ENGINE_M1_IMPLEMENTATION.md) öffnen. MI-Matrix und gepinnte PR88-Fachbelege erst bei konkretem Bedarf.

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

**M1 ist am15.10.2026 ausdrücklich abgenommen; die kontrollierte Integration von PR88 und PR89 sowie anschließend M2 sind genehmigt.** PR88 wurde nach unveränderter erfolgreicher Docs-CI38684167403 auf Kopf `a55d747513e79d5eb3a5961db35bd3d35d44cc78` zuerst integriert: Merge `9ff0a525d8394031148aada37593c9fed8f84148`. Die freigegebenen80/A–H/E01–E12/P01–P05 und M0/N01–N19 stehen jetzt im Repository.

PR89 wird auf diesen main abgestimmt. Dabei werden ausschließlich vier überlappende Status-/Übergabetexte konsolidiert und M1-Abnahmebelege ergänzt; der gerätebestätigte Produktcode40129/Room23 muss vollständig identisch bleiben. Ursprünglicher geprüfter Kopf `212a1401d31b3ca95b535e59b0dffbfba90e7edb`, vollständige CI38729242320 erfolgreich; eigene Geräteantwort15.10.2026 15:13 Europe/Berlin: „Passt alles 👌🏻“. [M1-Abnahmebeleg](docs/DEVICE_CONFIRMATION_40129.md). Vor PR89-Merge neue vollständige Integrations-CI abwarten und exakten Kopf/Produktgleichheit erneut prüfen. Keine Qualitätsprüfung lockern.

**M2 ist anschließend zur Implementierung D01–D06/B01–B05 freigegeben**, mit seinen verbleibenden L01–L09-Gates. L02 bleibt ausdrücklich offen: UTC ist Speicher-/Dauerbasis und gewünschtes technisches Wochenraster, ersetzt aber keinen juristischen Nachweis der Wochen-/Verlängerungszuordnung. Die zwei10-h-Verlängerungen zählen je gesetzlicher Woche, ohne Reset durch Wochenruhe. Reduzierte Tagesruhen gehören zu M3 und einem getrennten Anker/Zähler. Sieben-Stunden-Konvention ist keine erfüllte Tagesruhe; Artikel12 weder automatisch gerechtfertigt noch abschließend rechtswidrig. M2 braucht vor eigenem Merge erneute ausdrückliche Zustimmung und die erforderliche Geräteprüfung. M3–M7/Release bleiben gesperrt.

Ab15.10.2026 ist historische belegte Ortszeit die bevorzugte spätere Anzeige (M5/M6), feste einstellbare Anzeigezone der Rückfall; Unklarheit kennzeichnen. M2 rechnet weiter ausschließlich mit Instants/Dauern und separater Rechtszeitkonvention, ohne Displayabhängigkeit.

Automatische Workspace-Wartung entfernte frühere lokale Checkouts und die uncommittete Settings-Sicherung. M1 wurde vollständig aus GitHub wiederhergestellt; der alte unvollständige Entwurf ist nicht wiederhergestellt und wird nicht erfunden. [Genaue Grenze](docs/WORK_HANDOFF.md#lokale-arbeit-und-workspace-wiederherstellung). Alle vorhandenen Dateien/Branches/Sicherungen erhalten; kein Reset/Clean.

## Sitzungsroutine

- Gezielt mit `rg` suchen, benötigte Abschnitte lesen; große Diffs, Logs und bekannte Fakten knapp zusammenfassen.
- Kernstatus und Übergabe ersetzen, keine Checkpoint-Chroniken anhängen; möglichst unter 100–200 Zeilen halten.
- Gerätetest-Anleitungen immer verständlich direkt im Chat; Repository-/ZIP-Anleitungen ergänzen sie.
- Lange Emulator-CI nach erwarteter Laufzeit prüfen, typischerweise etwa alle 10–15 Minuten oder ereignisbasiert. Unveränderte Logs nur einmal analysieren; keine künstlichen Tool-Aufrufe während Wartezeit. Alle erforderlichen Gates bleiben bestehen.
- Nach Meilenstein/PR Übergabe aktualisieren, Arbeit sichern und Sitzung beenden. Repository ist das Langzeitgedächtnis.
- Abschluss: **„Übergabe vorbereiten und Sitzung abschließen.“** Neuer Chat: **„FahrerTagebuch am aktuellen Stand fortsetzen.“**
