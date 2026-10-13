# Geräteabnahme: vollständige Originaldateien 0504/0505

**40123 / Schema 19, noch nicht am Gerät bestätigt.** Nur ein aus dem exakten endgültigen Produktkopf nach vollständiger grüner Android-CI gebautes Paket verwenden. Bestehendes `device-test.yml` mit diesem `source_sha` und `include_baseline=false` auslösen; stabile Testsignatur und Paket `de.fahrerabrechnung.app.devicetest` beibehalten. 40122 als Update installieren, nicht deinstallieren oder App-Daten löschen. CI-/Budget-/Artefaktregeln bleiben unverändert.

## Konkreter Umfang

- Tätigkeitsdatei 0504 und Fahrzeugdatei 0505 einschließlich originaler Pointer und unbenutzter Bytes einfrieren. Getrennte Gen1-/Gen2-Child-Sitzungen; V2 ist weiterhin das Gen2-AID. MF nur am Parent.
- Bestehende verschlüsselte Schema-19-Tabellen, neue `ORIGINAL_EF_0504`-/`ORIGINAL_EF_0505`-Quellarten. Keine Migration, keine Rekonstruktion älterer Originale.
- Rohaufnahme und Auswertung getrennt: gespeichert, nachgewiesen leer, ungültige Auswertung, EF nicht vorhanden, fehlende Kapazität/nicht versucht und fehlgeschlagener Leseversuch. Abbruch durch Entfernen/Trennen/Timeout im regulären Mehranwendungsimport liefert keine persistierbare Teilgruppe.
- Keine neuen Signatur-, Hash-, Schreib-, Authentisierungs- oder Vertrauensbefehle. Automatischer Import bleibt lesend.

## Prüfung durch Tobi

1. **Update:** 40123 über die bestätigte Test-App 40122 installieren. Alte Karte, Profil, Importhistorie und gespeicherte Daten müssen erhalten bleiben.
2. **Neuer Import:** Eigene Karte mit dem bekannten Leser im Vordergrund einstecken. Automatische Aufnahme abwarten; etwaige Android-USB-Freigabe erteilen. Tätigkeiten/Fahrzeuge müssen weiter plausibel sein. Im Importverlauf Originaldateien getrennt von Kartentagen/Fahrzeugnutzungen prüfen.
3. **Anwendungswahl:** Im selben neuen Lesevorgang vorhandene GEN1- und GEN2-Daten jeweils öffnen. Unter „Gespeicherte Originaldateien“ stehen getrennte Dateigrößen und die richtige Generation (GEN2 oder GEN2V2). Keine Addition beider Tätigkeitsbestände. Größen müssen zur Anwendungskapazität passen: 0504 Ringlänge + 4 Bytes, 0505 zwei Pointerbytes + 31×G1- bzw. 48×G2/V2-Recordanzahl. Keine privaten Rohbytes/Nummern teilen.
4. **Historie/Neustart:** Leser entfernen, App vollständig schließen und wieder öffnen. Den neuen Lesevorgang erneut öffnen, dann einen älteren Import auswählen. Die neuen Größen müssen am neuen Vorgang erhalten bleiben; ältere Vorgänge ohne Originalsnapshot zeigen „keine Originaldatei in dieser Übernahme“. Wieder zum neuen Vorgang wechseln. Keine neueren Quellen an ältere Auswahl hängen.
5. **Erneutes Lesen/Abbruch:** Wenn praktisch möglich, erneut einstecken und prüfen, dass eine neue Sitzung mit gleichen Originalquellen entsteht. Ein gezielt unterbrochener laufender Mehranwendungsimport darf keine halb gespeicherte Gruppe oder neu angehängte Originale hinterlassen. Nur tatsächlich ausgeführte Fälle bestätigen.

Gültig unbenutzte, ungültige und fehlende EFs werden automatisiert mit synthetischen Daten geprüft; keine solche Situation auf der echten Karte erzeugen oder aus einer normalen Karte ableiten. Falls eine Anwendung/Datei tatsächlich fehlt, nur die beobachtete Meldung berichten.

## Rückmeldung / Mergegrenze

Version, Installation/Datenerhalt, tatsächlich geprüfte Anwendungen, Originalgrößen, Historienwechsel und Wiederöffnung ohne Leser benennen; Fehler und nicht ausgeführte Zusatzfälle getrennt angeben. Keine pauschale Karten-/DDD-/Kryptofreigabe. **Vor Merge eigene vollständige CI und diese erforderliche Gerätebestätigung für den endgültigen Stand prüfen. 40122 bestätigt 40123 nicht.**
