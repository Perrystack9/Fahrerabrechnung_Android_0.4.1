# FahrerTagebuch - aktuelle Übergabe

Einstieg über [WORK_START.md](../WORK_START.md), aktuellen Git/GitHub-Stand und die fünf Startdateien. Keine alten Chats/Handoffs rekonstruieren.

## Erreichter Meilenstein

**13.10.2026: 40123 / Schema 19 vollständig CI-geprüft, eigene Geräteabnahme noch offen.** Draft-[PR #70](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/70), Produktbranch `feat/card-ef-snapshots-20261013`, exakter grüner Kopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`. main beim Wiedereinstieg unverändert nach PR #69 (`5b1b9669af8614ec502d027a4c71d94ab81496ad`); PR #70 offen und mergefähig.

Die Pause ist durch ausdrückliche Nutzeranweisung vom 13.10.2026, 07:54 Europe/Berlin aufgehoben. CI-Versuch 1 war abgebrochen. [38335263243, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2) vollständig erfolgreich, Abschluss 08:18:33 Europe/Berlin: Projektgrenzen, alle sieben Gradle-Schritte, Unit/Lint/APK-/Signatur-/Updatepaar-/Schemaexportprüfung, vollständige SQLCipher-/Compose-Suiten auf API 36 und Abschlussjob. Keine fehlgeschlagene oder ausgenommene Suite. Normale erfolgreiche CI wie vorgesehen ohne Artefaktupload.

## Übernommene Implementierung

Vollständige 0504-/0505-Originale vor Parser einfrieren, einschließlich Pointer/unbenutzter Bytes. Rohaufnahme unabhängig von EMPTY/INVALID-Auswertung. Not attempted bei fehlender Kapazität, EF-Abwesenheit und tatsächlicher Lesefehler getrennt; keine Teilbytes und keine Teilgruppe bei Transportabbruch. Unbenutzte Aktivitätsdatei nur Nullpointer plus vollständiger Nullring; unklarer unbenutzter Fahrzeugpointer ist INVALID mit erhaltenem Original.

Bestehende verschlüsselte Schema-19-Tabellen, separate Quellarten/Fingerprints, exakte Karte/Generation/Child-Sitzung. MF am Parent. Keine Migration, Rekonstruktion oder Backfill; alte Requests/Quellenwahl und Normalisierung erhalten. Auch Einzelanwendungs-Coordinator nutzt dieselbe vollständige Freeze-Funktion; Mutationsprüfung während suspendierter Kartenprüfung bestanden. Vorschau zeigt Originalgrößen/Generation je Auswahl, keine neue Navigation.

## Exakt nächste Arbeit

1. Aktuellen PR-Kopf und vollständigen grünen CI-Nachweis live prüfen; Produktkopf für Paket `659f31c510752e5a06bf5f45bf2c93f4aff37031`. Diese Dokumentationssicherung verändert den grünen Produktbranch nicht.
2. Gezielten bestehenden [Geräte-Workflow](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/workflows/device-test.yml) mit Workflow-Branch `main`, `source_sha=659f31c510752e5a06bf5f45bf2c93f4aff37031`, `include_baseline=false` auslösen. **Aktueller GitHub-Zugriff bietet keinen Dispatch; bisher kein Paketlauf ausgelöst.** Keine Auslöser-/Upload-/Budgetänderung und kein Browserersatz. Nach Auslösung Lauf/Quellbindung/Signatur/Version prüfen und 40123 bereitstellen.
3. [Geräteabnahme 40123](CARD_CORE_SNAPSHOT_DEVICE_TEST.md): Update über 40122/Altbestand, vorhandene G1-/G2-Originalgrößen, exakte historische Auswahl und Wiederöffnung ohne Leser; zusätzliche tatsächlich geprüfte Fälle getrennt bestätigen.
4. Erst danach exakten grünen Kopf, Gerätebestätigung, erforderliche Checks und Mergefähigkeit erneut prüfen. PR bleibt bis dahin Draft; **kein Produktmerge mit bloßer 40122-Freigabe.**

## Weitere Grenzen

Echte Dateisignaturen, ERCA/MSCA/Card_Sign-Vertrauen, Standardexport und Download-Datum-Schreibpfad getrennte spätere Pakete. Keine Signatur an Altquellen hängen; automatischer Import bleibt lesend. `7F66`-Inhaltsdecoder ohne ISO-Primärformat und `0540` ohne legitimen SC5-Weg weiterhin gesperrt. Kein neuer Signatur-/Schreib-/Vertrauens- oder UI-Auftrag begonnen.

Status/Übergabe ersetzen, keine Chronik anhängen. Nach diesem CI-Meilenstein Arbeit sichern und Sitzung abschließen. Spätere Produktarbeit ausschließlich nach CURRENT_RULES.md, keine Spesen-/Lohn-/Heimkehr-/Heimatstandort-/Abrechnungsphase.
