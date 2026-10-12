Aktueller gesicherter PR65-Stand 12.10.2026: gemeinsamer Gen1-/Gen2-Import, atomare SQLCipher-Speicherung und exakte Verlaufswahl umgesetzt; Kandidat 40122 / Schema 19, Produktkopf 62fc0d2. Build, JVM-Tests, Signatur, Updatepaar und Schema bestanden. Aktuelle Emulator-/Gesamtprüfung wird von GitHub vor dem Runnerstart abgewiesen, auch nach gezieltem Retry. Keine volle CI- oder Hardwarefreigabe, keine Test-ZIP, kein Merge. [Verbindlicher Übergabestand mit konkretem CI-Blocker und Fortsetzung](CARD_MULTI_APPLICATION_CHECKPOINT_40122.md). Actions-Budget 2 USD / Stop-usage unverändert. Die Angaben darunter sind historische Checkpoints.

---

Aktueller Gerätecheckpoint12.10.2026: Tobi bestätigt den neuen40121-Wiederlese-/Anzeigefall nach erneutem Einstecken positiv. Beide neuen MF-Quellen real vorhanden: ATR/INFO11Bytes, Extended_Length3Bytes, T=1 aufgezeichnet/T=0 nicht aufgezeichnet. PR62-Produktkopfefbf49cdf92bd5e5ddb2e042b3e14e9372169756 unverändert und vollständige CI38172596871 sowie Paketlauf38201601458 erneut grün. [Genaue40121-Gerätebestätigung und Grenzen](DEVICE_CONFIRMATION_40121.md). Keine zusätzlichen40121-Neustart-/Altdaten-/Historienfälle behaupten. PR62 unverändert gemergt als26a9f14c34523fa4ba884d8dd9891c24e1736ab4, Tree11e79e492b381f9d515bee796ffea30da3099592 bytegleich zum Testkopf. Selbstständige Produktfortsetzung durch die eingerichtete Aufgabe;  spätere Änderungen weiterhin bis eigener Freigabe ungemergt. Keine Browser/Unteragenten/Bereinigung, Budget2USD unverändert. Die folgenden Angaben sind historische Checkpoints.

---

Aktueller Auslieferungsstand12.10.2026: Test-ZIP PR62/40121 aus unverändertem Produktkopfefbf49cdf92bd5e5ddb2e042b3e14e9372169756 bereitgestellt; vollständige CI38172596871 und Paketlauf38201601458 erfolgreich. APK-v2-Signatur/Inhaltsdigest, Zertifikat, Paketkennung, Version und alle Prüfsummen lokal bestanden. Anleitung/BUILD_INFO enthalten, Update über bestätigte40120, keine Neuinstallation.40121 noch hardwareunbestätigt; PR62 offener Entwurf. Tobi hat nach erfolgreicher ausdrücklicher Bestätigung den Abschluss von PR62 und selbstständige weitere Kartenimportarbeit beauftragt; automatische Fortsetzung nach PR62-Merge eingerichtet, mit zusätzlicher Prüfung der dokumentierten40121-Freigabe. [Verbindlicher Auslieferungs-/Fortsetzungscheckpoint](PR62_DEVICE_DELIVERY_40121.md). Keine neue Freigabe späterer Änderungen; kein Browser/Unteragenten/Bereinigung, Budget2USD unverändert. Die folgenden Angaben dokumentieren frühere Stände.

---

Aktueller bestätigter Stand11.10.2026: Gerätetest PR60/40120 ausdrücklich bestanden: Update und alte Daten, neue Kartenlesung, Zertifikate/Kartenangaben/Anwendungsverzeichnis, Bestand nach Kartenentnahme und App-Neustart sowie unveränderter alter Import. PR60 am unveränderten geprüften Kopf85d0614 gemergt als9e7670f05b7c35a585244aeca539be9d0fe7c6fb, Tree78155b21cb76f9c27231360bd78dad0aa5472a22 exakt gleich dem Teststand. PR59 ist enthalten und von GitHub ebenfalls als gemergt abgeschlossen. Letzte Gerätebasis jetzt40120/Schema18. PR62/40121 und spätere Änderungen sind ausdrücklich noch nicht hardwarebestätigt und bleiben ungemergt. [Verbindliche Gerätebestätigung und Merge-Nachweise](DEVICE_CONFIRMATION_40120.md). Selbstständige Importfortsetzung beauftragt; Projektregeln, kein Browser/Unteragenten/Bereinigung und Actions-Budget2USD bleiben bestehen. Ältere Checkpoints darunter sind historische Stände.

---

Aktueller geprüfter Importfolgeentwurf PR62/40121/Schema18: bedingte MF-Kommunikationsquellen, exakte eigene Karten-/Sitzungsspeicherung und Quellenmap-Snapshots; bis29 Gen2V2-Quellenarten bei eindeutiger ATR-Ankündigung. 38 zusätzliche Testmethoden. Integrierter Produktkopf efbf49cdf92bd5e5ddb2e042b3e14e9372169756, Tree11e79e492b381f9d515bee796ffea30da3099592; vollständige CI38172596871 mit allen vier Jobs erfolgreich, einschließlich Build/Unit/Lint/APKs, Updatepaar40120→40121, unveränderten Room-Exporten und SQLCipher/Compose. Dokumentierter Main f2984027643a27ea55dc44c10fae9cdf6d115938 als zweiter Parent enthalten; PR62 ohne Konflikt mergebar, aber ausdrücklich hardwareunbestätigt und ungemergt. Gerätefreigabe40120 gilt nur für abgeschlossene PR59/60. [Vollständiger Folgecheckpoint und offene Grenzen](CARD_IMPORT_FINALIZATION_HANDOFF.md). Abschließende Dokumentation liegt auf docs/card-import-finalization-checkpoint-20261011, Produktkopf unverändert. Ältere Checkpoints sind historische Stände.

---

Aktueller gemeinsamer Checkpoint11.10.2026: Tobi hat die Fortsetzung um14:41 und14:45 Europe/Prague erneut beauftragt. Der frühere Ausgabetermin11:10 ist überholt. PR59 ist vollständig grün; PR60 bündelt Originaldateien und zusätzlich das MF-Anwendungsverzeichnis. Version40120, Schema18, stabile Testkennung/-signatur, bestätigte UpdatebasisPR58/40117. 44 Originaldateien- und31 DIR-Testmethoden, zusammen mit PR59 insgesamt129 zusätzliche Methoden. CI38122321988 fand einen widersprüchlichen Fahrzeug-Testdatensatz; dieser ist an die tatsächlich gelieferten zwei Plätze angepasst, ohne Prüfungen zu entfernen oder Produktionsgrenzen zu lockern. Neue vollständige Android-/SQLCipher-/Compose-CI erforderlich; APK-Ausgabe danach. Kein neuer Hardwaretest bestätigt, kein Import-Merge vor ausdrücklicher Gerätebestätigung. Kein Browser, keine Subagenten, keine Bereinigung; Actions-Budget unverändert2USD. [DIR-Umfang und Primärformat](CARD_DIRECTORY_IMPORT.md), [gebündelte Geräteanleitung](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md).

Die älteren Checkpoints darunter bleiben als damalige Stände erhalten.

---

Aktueller gemeinsamer Checkpoint11.10.2026: Tobis erneuter Auftrag09:10:27 Europe/Berlin gilt für zwei Stunden bis11:10:27. PR59 ist vollständig grün, CI38119878793 am unveränderten Kopf c535c01. PR60 ist der gemeinsame kumulative Originaldateien-Entwurf40119/Schema18. Der parallel entstandene PR61 ergänzt dieselben Dateien und wurde deshalb ungemergt geschlossen; seine Dateien und Branch bleiben erhalten. Kein Browser, keine Subagenten, keine Bereinigung, Actions-Budget unverändert2USD. Kein neuer Import-Merge vor späterer ausdrücklicher Gerätebestätigung.

Abgleich ergänzt: Kartennummer und Zertifikatsreferenzen bei Wechsel der ausgewählten ImportSession auch dann erneut verbergen, wenn identische Originalbytes denselben Quellenfingerprint besitzen. Sämtliche in0501/0525 dekodierten Speicherkapazitäten werden in der Detailansicht angezeigt, einschließlich Ereignissen/Störungen/besonderen Bedingungen und VU-Konfigurationsgröße. Diese Größe ist kein Import von EF0540. Drei zusätzliche Compose-Prüfungen; insgesamt44 Originaldateien-Testmethoden bzw.98 zusammen mit PR59. Vollständige CI am endgültigen gemeinsamen Kopf erforderlich. Testpaket frühestens am Blockende11:10:27 ausgeben; keine neue Gerätebestätigung vorweggenommen.

Dieser gemeinsame Checkpoint ist maßgeblich; ältere Abschnitte dokumentieren ihre damaligen Stände.

---

Aktueller Checkpoint 11.10.2026: Tobi bestätigt um 08:36 Europe/Berlin allgemein „Funktioniert alles“ zum ausgegebenen PR58-Testupdate 40117/Schema 18 und beauftragt zwei Stunden selbstständige Weiterarbeit bis 10:36:13. Alle vier Jobs der exakten CI-Köpfe PR54–58 erneut erfolgreich geprüft. PR58 unverändert gemergt als 1b592d627d5d18db9fc909f5c8cf2c796fd8578b; Merge-Tree 2e314a47211f06696a6b05287e40b63575b921df bytegleich zum getesteten Kopf ba9656f0e22980af94883783986367d7bcdf545b. GitHub hat die enthaltenen PR54–57 ebenfalls abgeschlossen. Keine neuen Einzelmesswerte oder nichtleeren Quellenfälle behauptet.

Neuer begrenzter Zertifikatsentwurf PR59: vier unabhängige Zertifikatsarten der gewählten Kartenanwendung (Gen1 zwei), vollständige begrenzte EF-Lesung, struktureller Decoder, verschlüsselte exakte Karten-/Sitzungsquelle und Detailansicht mit Referenzen auf Antippen. Kopf c535c01a838fb9f565b49b7a9b50030d06e77bf7, 54 zusätzliche Testmethoden. Alle vier Jobs der vollständigen CI 38119878793 erfolgreich, einschließlich SQLCipher-/Room- und Compose-Instrumentierung. Der Zertifikatskopf bleibt unverändert. [Umfang und Primärformat](CARD_CERTIFICATE_IMPORT.md).

Separater Folgeentwurf ergänzt die vollständigen bereits gelesenen Originaldateien für Anwendung, Karten-/Inhaberidentifikation und V2-Anwendung, einschließlich unabhängiger Dateiendproben und der Ansicht „Kartenangaben“. 41 weitere Testmethoden vorbereitet. Die erste Folge-CI 38121099485 fand zwei falsch typisierte APDU-Testvergleiche; die Vergleiche sind auf kodierte Bytes korrigiert, neue vollständige CI am Korrekturkopf erforderlich. Aktuelle Testversion 40119, Schema 18 und stabile Testkennung/-signatur unverändert. Neue Gen2V2-Vorgänge können 26 unterstützte Quellen enthalten; ältere 19-/23-Quellen-Vorgänge behalten ihren exakten bisherigen Stand. Keine Vollständigkeits-/Kryptovertrauensbehauptung, keine neuen PSO-/Schreib-/Authentisierungsbefehle und keine Nachbefüllung. [Originalquellen und Prüfgrenzen](CARD_ORIGINAL_IDENTIFICATION_IMPORT.md), [gebündelter Gerätetest](CARD_ORIGINAL_IDENTIFICATION_DEVICE_TEST.md). Vollständige Android-/SQLCipher-/Compose-CI am endgültigen Kopf vor der Ausgabe erforderlich. Kein neuer Hardwaretest bestätigt; beide Importentwürfe bleiben bis zur Rückmeldung ungemergt.

Dieser Checkpoint ist maßgeblich; die älteren Abschnitte darunter dokumentieren ihre jeweiligen früheren Stände.

---

# Aktueller MF-Entwurf im laufenden zweiten Arbeitsblock

TCS43-Grenzfall nach amtlichem Abgleich korrigiert: Ein datenloses6700 beim Zwei-Byte-Rest nach15Byte kann15 oder16Byte bedeuten. Deshalb genau ein Byte an Offset15 nachprüfen; nur datenloses6700 an dieser Ein-Byte-Grenze als vollständiges Legacy-EOF akzeptieren.16Byte,6C01-Längenkorrektur mit abgeschnittenem Rest, Statusfehler mit Daten, leere9000-Antworten und Timeouts bleiben fehlerhaft. Vier weitere Lesertests; neue vollständige CI am geänderten Kopf erforderlich. Kein Hardwaretest/keine APK ausgegeben.

Neuer Branch feature/card-master-file-import auf PR57-Head48d7e0053b2eaef56f6ed9de54a7e06f70e26db1 (inklusive PR56-Korrektur79d79ab4bde565c7851e2291dcc7c0b43c188089). Zwei neue unabhängige MF-Quellen EF ICC0002/25Byte und EF IC0005/8Byte. Amtliche ASN.1-/Byte-/Reset-/SELECT-Tabellen visuell geprüft: beide nach ATR vor erster AID lesen, keine neue MF-Select-Variante, keine Kartenbindung aus Serien-/Chipwerten. Nummern/Kennungen verborgen bis Antippen. Schema18 unverändert, version40117,19Quellen möglich.37 neue synthetische Tests. Keine Nachbefüllung, keine Kartenschreibbefehle.

PR55 vollständig grün. PR56-CI38087221502 und PR57-CI38087497362 laufen. Authentischer Schema18-Export70340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b erhalten. Neuer MF-Entwurf braucht eigene vollständige CI einschließlich Room/SQLCipher/Compose und Updatepaar; noch kein Gerätebeleg, ungemergt lassen. Spätere kumulative Anleitung CARD_MASTER_FILE_DEVICE_TEST.md. Ausgangspunkt weiter PR53/40112/Schema14 und feste deviceTest-Signatur/Paketkennung.

Zweiter Arbeitsblock nach Nutzerwunsch endet22:34:51UTC /11.10.00:34:51Berlin. Hardware/APK-Ausgabe unverändert frühestens22:56:06UTC /00:56:06Berlin; Ausgabetask00:57Berlin darf ausschließlich neuestes vollständig grünes exaktes Head verwenden, keine parallele Implementierung starten.

---

# Echter Schema18-Export und korrigierte Dateigrenze

KSP-Compilerexport18 aus CI38086827094 Versuch1, Artifact11682788686 unverändert übernommen: ZIP322904Byte, SHA256626cdbf5b155cbf54791d7cc8f0a054fdb36ecebfc293537de535d833cd0593d; Schema99557Byte, SHA25670340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b. Alle36 alten Entitäten identisch zu17; Migration17→18 stimmt mit allen38 Tabellen einschließlich Spalten, Schlüssel und Indizes überein. Keine Nachbefüllung.

Dieser erste CI-Lauf kompilierte Produktcode und alle14 neuen Parser-/8 Lesertests; nur die zwei Tests zum alten15-Byte-Anwendungsstand scheiterten an der bekannten Reader-Grenze. PR56-Korrektur79d79ab4bde565c7851e2291dcc7c0b43c188089 samt vier zusätzlichen Grenztests jetzt übernommen. Finale vollständige CI am neuen kumulativen Kopf erforderlich; kein Hardwaretest/kein Paket ausgegeben. Zweiter Arbeitsblock bis22:34:51UTC, Ausgabe frühestens22:56:06UTC.

---

# Aktueller Arbeitsblock nach vorzeitig beendetem Pausenteil

10.10.2026 22:34:51 Europe/Berlin: Nutzer hat die Pause ausdrücklich beendet. Zweiter Arbeitsblock bis11.10.00:34:51 Europe/Berlin. Hardware/APK-Ausgabe weiterhin frühestens11.10.00:56:06. Keine neuen Karten-/Schemaentwürfe vor späterer ausdrücklicher Gerätebestätigung mergen.

PR55 Head ef7d44c29d8cb9520fcaf67306ef387263bf1ba9: Android-CI38084590529 Versuch1 vollständig grün (alle4 Jobs einschließlich SQLCipher/Room/Compose). PR56 Head1fcf5ce6f45afd2ad89d21bde597ea9731b19dd8: authentischer KSP-Schema17-Export eingecheckt, Unit-Testimport korrigiert; finale CI38085931554 läuft noch.

Aktuelle Weiterentwicklung: drei feste nur gelesene Kartenquellen im Branch feature/card-fixed-metadata-import, additive Schema18/version40116, kumulativ überPR54/55/56. Parser/Leser/verschlüsselte Sitzungsspeicherung/Status und genaue Detail-/Verlaufsauswahl angebunden. Führerscheinnummer zunächst verborgen; keine Kartenschreibbefehle. Echten Schema18-Compilerexport und vollständige finale CI erst abwarten. Noch keine dieser neuen Quellen am Gerät bestätigt. Später ausschließlich neuesten vollständig grünen exakten Kopf ausgeben; Ausgangspunkt PR53 Schema14/40112, gleiche feste deviceTest-Signatur und Paketkennung. Anleitung CARD_METADATA_DEVICE_TEST.md. Keine parallele Implementierung im späteren Ausgabetask.

---

Aktueller Compilercheckpoint10.10.: authentischer17.json-Export aus CI38085254090 Versuch1/Artefakt11682376429 unverändert übernommen;34 bisherige Entitäten vollständig identisch, zwei neue. Fehlender ByteBuffer-Import im neuen Reader-Test korrigiert; vollständige finale CI einschließlich Unit-/SQLCipher-/Room-/Compose-Prüfungen erforderlich. Siehe PR56 für exakten finalen Kopf/Status.

Aktueller Entwicklungsstand10.10.2026, zweiter vorzeitig gestarteter Arbeitsblock: eigene Fahrtenschreiber-Nutzungen EF0523 auf PR55-Korrekturkopf ef7d44c29d8cb9520fcaf67306ef387263bf1ba9. Amtliche ASN.1-/Byte-/Kapazitätstabellen vollständig visuell geprüft; Gen2 eigener Zähler84..200, Gen2V2 fest200, zehn Byte je Eintrag. Unabhängige exakte Karten-/Sitzungsquelle, additive Migration16→17, Testversion40115. [Umfang/Primärformat](VEHICLE_UNITS_IMPORT.md), [vorbereitete gemeinsame Geräteanleitung](VEHICLE_UNITS_DEVICE_TEST.md). Echter17.json-Compilerexport und vollständige finale CI noch erforderlich; neue Entwürfe ohne Gerätebestätigung ungemergt. Block22:34:51–11.10.00:34:51 Europe/Berlin, Ausgabe frühestens00:56:06. Ältere Abschnitte darunter bleiben historische Checkpoints.

Aktueller Fortsetzungscheckpoint10.10.2026 22:34:51 Europe/Berlin: Nutzer beendet die Pause vorzeitig; zweiter zweistündiger Arbeitsblock22:34:51–11.10.00:34:51. Geplanter Wiederbeginn22:56 deaktiviert, Abschlusslauf00:57 bleibt; Hardware-/Paketuntergrenze00:56:06 bleibt erhalten. PR54 vollständig grün im Lauf38075694297. PR55-Kopf3f5ec1f08f783bbf13eca96666b1afe1070e3e53: Build/Unit/Lint/APK/Paar/Schema erfolgreich, Instrumentation38076790377 scheitert beim Kompilieren an einem versehentlich doppelten Testhelfer conditions(Int), noch vor den Compose-Tests. Doppelte Definition korrigiert, bestehenden identischen Helfer und sämtliche Tests erhalten; vollständiger Folgelauf erforderlich. Keine neue Gerätebestätigung und kein Merge der Entwürfe. Ältere Checkpoints darunter dokumentieren die Entwicklung.

Aktueller Entwicklungsstand10.10.2026: eigener Ladungsart-Zweig auf PR54 `6d46ebd6fe77a4c424aee45dc700f0bd186f5d5c`. Gen2V2 EF0530 als unabhängige exakte Karten-/Sitzungsquelle, additive Migration15→16, Testversion40114. 39 neue Parser-/Reader-/SQLCipher-/Room-/Compose-Testmethoden; authentischer16.json-Compilerexport aus CI38075938370 übernommen, alle32 bisherigen Entitäten identisch; vollständige CI am finalen Kopf einschließlich Reader-Antwortgrenze/Manipulationsprüfungen noch erforderlich. [Implementierung](LOAD_TYPE_IMPLEMENTATION.md), [spätere gemeinsame Geräteanleitung](LOAD_TYPE_DEVICE_TEST.md). Hardwarepaket frühestens11.10.00:56 Europe/Berlin, neue Import-/Schema-PRs bis Gerätebestätigung ungemergt. Ältere Abschnitte darunter sind historische Checkpoints.

# Work-Übergabe – FahrerTagebuch

**Stand: 10.10.2026.** Diese Datei hält den technischen Fortschritt und den zuletzt vom Nutzer bestätigten Gerätetest fest. Sie ersetzt keine Fachregeln.

## Aktueller Stand nach PR53 und Zeitplan 10./11.10.2026

PR53 ist nach vollständiger grüner CI und Tobis allgemeiner Funktionsbestätigung vom10.10.19:56 Europe/Berlin am unveränderten Kopf `b2973034af43a281df4ab02c5c123b821dab0772` gemergt: main `22d41f64875de3aa5ac383d9d1ce80dc0dee1684`, Tree `7626c1d66fd711c4519a6f6f4e350682682bdbb0`, Schema14/Testversion40112. CI38065441534 Versuch1 einschließlich aller vier Jobs erneut erfolgreich geprüft; Paket38067285142 lieferte die zuvor ausgegebene ZIP. Nutzertext: „Funktioniert alles prima.“ Keine weiteren Einzelmesswerte/nichtleeren Grenzdatensätze oder manuellen Fehler-/Fremdkartentests behaupten.

**Neue konkrete Autonomie-/Testbündelungsfreigabe:** eine Stunde Arbeit bis10.10.20:56:06, zwei Stunden Pause bis22:56:06, dann zwei Stunden Arbeit bis11.10.00:56:06, jeweils Europe/Berlin. Neuer Hardwaretest frühestens danach. Einmalige Fortsetzung22:56 und Abschluss/Auslieferung00:57 sind eingerichtet; früherer abgeschlossener Zeitplan nicht erneut starten. Diese Freigabe erlaubt Folgeschritte ohne neue Routinebestätigung und bündelt Hardwaretests, erlaubt aber keinen Merge neuer Import-/Schema-PRs vor neuer ausdrücklicher Testbestätigung.

Erster neuer Abschnitt auf isolierter Branch `feature/card-load-unload-import`: EF0529 Load_Unload_Operations für Gen2V2, unabhängige Kapazität aus EF0525 Offset4..5, vollständige32482Bytes, zwei getrennte Quellzeiten und Originalwerte, verschlüsselte exakte Karte/Sitzung, additive Schema14→15 mit zwei Tabellen und neutraler Detailansicht. Version40113 mit unverändertem Testpaket/Schlüssel; Updatepaar auf dem bestätigten PR53-Produktcode40112/Schema14. Keine Timeline-/Abrechnungs-/Kilometer-/Ruhe-/Heimkehr-/Routenarbeit. [Format/Primärquelle](LOAD_UNLOAD_IMPORT.md), [erst später auszugebende kumulierte Anleitung](LOAD_UNLOAD_DEVICE_TEST.md).

18 Parser-, neun Reader-, sieben SQLCipher-, ein Room- und vier Compose-Testmethoden vorbereitet, einschließlich echter historischer Navigation zwischen altem, Nur-Vorgangs- und neuestem Import. SQLite-Migrationsprüfung:30 vorhandene Tabellen erhalten, genau zwei hinzugefügt, keine FK-Verletzung. Statische Projektgrenzen, Actionlint/Shellsyntax und zehn CI-Routing-/Schema-Guard-Prüfungen vor Veröffentlichung ausführen. Vollständige Android-CI und echter compiler-generierter15.json-Export sind am finalen Kopf erforderlich; erste CI erzeugt diesen Export, danach unverändert übernehmen und finalen Kopf vollständig prüfen. Prüfnachweise/PR-Kopf in der PR-Beschreibung, bei Pausenbeginn hier ergänzen. Neue Test-ZIP nicht vor Ende des zweiten Blocks ausgeben.

Fortsetzung: vorhandenen Folge-PR/Kopf/CI kontrollieren und weiterführen, nichts doppelt implementieren. Als nächste unabhängige Quelle ist EF Load_Type_Entries (`0530`, ausdrücklich nicht052A/052B) vorbereitet durch visuelle Primärformatprüfung:336×5Byte+2Byte-Zeiger=1682Bytes; Zeit4Byte, LoadType1Byte (00undefiniert/01Güter/02Personen/03..FFreserviert), Kapazität aus EF0525 Offset6..7. Erst separaten begrenzten Schritt festlegen; keine Zuordnung zu Be-/Entladevorgängen raten. Weitere noch nicht abgedeckte EFs anhand amtlicher Struktur inventarisieren, nicht vollständigen Kartenimport behaupten. Neue Quellen weiter exakt/atomar/persistent und fehlerunabhängig behandeln.

Keine Smartphone-Ortung, kein Card_Download-Schreiben, kein Backfill oder Quellenvermischung; fremde Karten temporär. Kein Browser, keine Subagenten, kein Cleanup/Löschen alter Dateien/Branches/Stashes/Releases. Budget2USD/Stop usageYes unverändert. Bei Zeitende Änderungen/Prüfnachweise im Repo sichern und den Block beenden. Alle folgenden älteren Angaben sind historische Herkunft und kein neuer Arbeitsauftrag.

## Einstieg und Ausgangsbasis

1. Zuerst [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) und [CURRENT_RULES.md](CURRENT_RULES.md) lesen. Beide sind der verbindliche Produkt-/Fachstand; historische Design-/GPS-Dokumente gelten nur ohne Widerspruch.
2. Danach diese Übergabe, [WORK_STATUS.md](../WORK_STATUS.md) und [README.md](../README.md) lesen.
3. Den aktuellen `main` aus GitHub verwenden und vor der nächsten Änderung HEAD, offene PRs und CI erneut prüfen. Alte Scratch-Verzeichnisse oder ZIPs können fehlen oder älter sein.

| Bezug | Stand |
|---|---|
| Letzter Import-/Schema-PR | [PR #44: Sonderbedingungsquelle sitzungsgenau übernehmen](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/44), nach grüner CI, erneuter Codeprüfung und ausdrücklichem Überspringen des Gerätetests gemergt |
| Letzte vollständig am Leser getestete Import-/Schema-Basis | [`e957834d2cfa5e1dcd4e54bb458a1e078f2a57f5`](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/commit/e957834d2cfa5e1dcd4e54bb458a1e078f2a57f5); PR #43 ergänzt UI-Marker; PR #44 ergänzt die neue Quelle und Schema 11, ohne bestätigten Gerätetest |
| Getesteter PR-41-Kopf | `69b3566638e6759b544b3470f95d83059a89ae56`; Merge-Tree `0de278c6e235823fc34c1e4f896bde2c800ee1d9` entspricht exakt diesem getesteten Stand |
| Abgeschlossener begrenzter Produktabschnitt | Automatische Vordergrund-Lesung eigener Aktivitäts-, Fahrzeug-, Kontroll-, Ereignis-, Störungs- und Sonderbedingungsquellen mit dauerhafter Importhistorie für diese unterstützten Dateien; vorhandene Profilbindung und Normalisierung weiterverwenden |
| Datenbank auf main | Room/SQLCipher, Schema **11**, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11; keine destruktive Migration oder automatische Zuordnung alter Quellen |
| Automatischer Nachweis der letzten Importerweiterung | [PR-44-Lauf 37121217858](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37121217858): vollständig grün, 135 Unit-, 99 SQLCipher- und 58 Compose-Tests, insgesamt 292 ohne Fehler/übersprungene Tests. Generierter Schema-11-Export byteidentisch bestätigt |
| Letzter vollständiger Karten-/Lesertest | **03.10.2026, 08:06 Uhr, PR #41: Update Schema 9→10, automatische Übernahme, exakte Historie und Wiederöffnung bestanden; Screenshot zeigt echten Ereignisbeleg und leere Störungsquelle**, Details und Grenzen unten |
| Aktueller Produkt-/UI-Stand | PR #45, Produktkopf `42fb5dea0fb8f18d1b2206bda5d09b59b4ebe8b1`, gemergt als `9f24a48845367502e585c87b1eb8e63f370b3dfb`; Schema 11, Testversion 40109; CI #46/#47 erhalten |
| Letzter kurzer Geräte-/Bestandstest | 10.10.2026, 05:52 Europe/Berlin, PR #45: vorhandene Daten nach Schließen/Wiederöffnen bestätigt; keine erneute Kartenlesung oder Prüfung realer Sonderbedingungsmarker |

## Wirklich implementiert

- Siebenstufiges Onboarding; Heimatpunkt ausdrücklich per Vordergrund-Standort oder Karte wählbar, fester 500-m-Radius.
- USB-CCID/APDU, USB-Freigabe, ATR-/Slot-Diagnose und Gen1-/Gen2-/Gen2V2-Erkennung.
- PR #28: Das Geburtsdatum in EF Identification wird als vier Byte BCD-Kalenderdatum (`LocalDate`) gelesen; alle Nullbytes bedeuten ein fehlendes Datum. TimeReal-Zeitstempel bleiben unverändert.
- Vollständiges Lesen der Aktivitäts- und Fahrzeugdatei, zyklische Tagesdatensätze/Tätigkeitswechsel und Fahrzeugnutzungen dekodieren.
- Aktivitätsintervalle auf lokale Kalendertage projizieren; fehlende Zeit und Einträge ohne Karte gesondert kennzeichnen.
- Kompakte Tagesübersicht, zoombare/verschiebbare Detailzeitleiste, Abschnittsauswahl, Zeitmarke und belegte Fahrzeug-Zeitfenster. Offene Fahrzeugenden werden nicht fortgeschrieben.
- Verschlüsselte, ausdrücklich bestätigte Einzeldateien; identische Quell-Snapshots werden dedupliziert und nach App-Neustart wieder geöffnet.
- Fahrzeughistorien-Vorschau über gespeicherte Dateien, VIN-/Kennzeichen-Suche, nachladbare Nutzungen und gezielte Navigation aus dem Kartentag. Tacho-Werte sind Quellwerte.
- Dateiübersicht mit Generation, Datensatzanzahl, maskierter Kartennummer und Quellzeitraum; ältere Aktivitäts-/Fahrzeugdateien gezielt als Vorschau öffnen.
- Startseite unterscheidet fehlende, vorhandene und nicht prüfbare Teilimporte.

**PR #26 ergänzt:** Ein tatsächlicher Lesevorgang mit beiden validierten Dateien kann über „Beide Dateien gemeinsam speichern“ ausdrücklich gesichert werden. Eine Room-Transaktion speichert beide Quellen und ihre Zuordnung. Die gemeinsame Vorschau öffnet genau diese beiden Dateien. Bestehende Einzeldateien werden nicht rückwirkend anhand von Datum oder Kartennummer gepaart.

Die Dateien und identische Dateipaare bleiben dedupliziert. Die Zeit des Dateipaars bezeichnet seine erste gemeinsame Speicherbestätigung; sie ist keine vollständige Historie jedes erneuten Leseversuchs. Neue Profil-Teilimporte protokollieren die ausdrücklich ausgelöste Speicherung eines Lesevorgangs separat. Beim gebundenen Profil öffnet die Standardvorschau die Quellen einer konkreten Teilimport-Sitzung; eine einzelne Quelle wird nicht mit einer anderen Sitzung ergänzt.

## Abgeschlossene Profilbindungs-Stufe (PR #30)

- `DriverCardImportRepository` und `DriverProfileDao`: ein ausdrücklich gebundenes lokales Profil; unabhängige Profil-/Karten-UUIDs, unveränderliche Kartenidentitäten und zusätzliche Folge-/Ersatzkarten. Die Kartennummer bezeichnet eine Quelle, nie die dauerhafte Fahreridentität.
- Match ausschließlich über exakt gleiche, vollständige Nachnamen, Vornamen und vorhandenes Geburtsdatum. Teil-/Präfix-/unscharfe Matches, fehlende Daten und möglicherweise abgeschnittene volle 35-Zeichen-Namensfelder verlangen Bestätigung. Unterschiedliche vollständige Namen und Geburtsdaten bei allen bestätigten Karten sperren eine Fremdkarte; nur ein abweichendes Merkmal bleibt unklar. Widersprüchliche Inhaberdaten zu einer bereits zugeordneten konkreten Karte werden gesperrt und überschreiben nichts.
- Erste eigene Karte und unklare neue/Ersatzkarte verlangen einen eigenen Bestätigungsdialog. Abbrechen und Lesetest speichern nichts. Zuordnung einer belastbar gleichen neuen Karte erfolgt erst beim ausdrücklich ausgelösten Speichern. Eine erkennbar fremde Karte bleibt temporäre Vorschau ohne Speicher-/Zuordnungsaktion und ohne Navigation ihrer Live-Nutzungen in die Profilhistorie.
- `driver_import_sources` verbindet jede neue `PARTIAL`-Sitzung mit ihrer konkreten Karte und ausgewählten Quellen. Quelle, Dateipaar, zusätzliche Karte und Sitzung werden gemeinsam atomar gespeichert; ein Fehler rollt die komplette neue Speicherung zurück. Derselbe Speicherauftrag ist idempotent, ein neuer Lesevorgang darf eine eigene Sitzung mit bereits vorhandenen Quelldateien erzeugen.
- Schema 5 ergänzt drei Tabellen ohne Backfill oder Umschreibung alter Karten/Snapshots/Sitzungen. Alte Dateien bleiben im getrennten Quellarchiv. Eine Archivauswahl bleibt read-only und erzeugt keine Profilbindung; bei Navigation bleibt die Fahrzeugvorschau auf diese ausgewählte Datei begrenzt.
- Die normale Fahrzeughistorie und Standard-Kartenvorschau nutzen bei gebundenem Profil ausschließlich dessen explizit verknüpfte Quellen. PR #30 enthielt noch keine normalisierten Tagesquellen; diese ergänzt der nachfolgend beschriebene PR #32. Vollständiger Kartenimport, dauerhafte Fahrzeugentität und fachliche Abrechnung bleiben offen.
- Automatische Regressionen: exakter/unklarer/fremder Match, Bestätigen/Abbrechen, Fremdkartensperre, unveränderte Folge-/Altkarten, Deduplizierung/Retry, atomarer Rollback, Einzelquellen, verschlüsselte Wiederöffnung, Migration 3 → 5 und 4 → 5 sowie Trennung von Archiv und Profilhistorie. Der geprüfte Profilbindungs-Gerätetest ist bestanden; Details und Grenzen unten.
- [Gerätetest-Anleitung](PROFILE_BINDING_DEVICE_TEST.md): Bestätigung/Abbruch, Speichern, Neustart ohne Leser, erneute eigene Karte und getrenntes Altarchiv. Identische ältere Quellen dürfen nach einem frischen Lesevorgang wiederverwendet werden; dadurch werden keine alten Sitzungen rückwirkend umgeschrieben.
- [Erster vollständig grüner PR-30-Lauf 36856005336](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36856005336), Kopf `99a29b6`: 57 Unit-Tests, 27 Daten-/Transporttests, 29 App-Tests, Lint und Debug-/Internal-APK-Prüfung bestanden; keine Fehler/übersprungenen Tests. Schemaexport und Kartenwechsel-Absicherung kamen anschließend hinzu und sind durch die oben genannte finale PR-30-CI abgedeckt. Bei Kartenwechsel werden der alte Dialog und laufende Prüfungen verworfen; verspätete Ergebnisse einer anderen Karte ändern keine aktuelle Speicherfreigabe.

## Abgeschlossene normalisierte Tagesquellen (PR #32)

- Drei additive Tabellen speichern unveränderliche UTC-Tagesinhalte, geordnete Tätigkeitswechsel und genaue Karten-/Quelldateiverweise einschließlich Ringposition. Die vorhandene Sitzung liefert die Importherkunft.
- Tagesinhalte derselben physischen Karte werden unabhängig von der Ringposition dedupliziert. Geänderte Fassungen desselben Datums bleiben zusätzlich erhalten; keine stille Auswahl einer fachlich maßgeblichen Fassung. Folge-/Ersatzkarten bleiben getrennte Karten, auch bei gleicher Nummer und gleichen Tagesinhalten.
- Normalisierung und bisherige Karten-/Datei-/Sitzungsspeicherung erfolgen atomar. Wiederholter Speicherauftrag bleibt idempotent. UTC, Kartenstatus, Fahrer-/Beifahrerslot, Teamstatus und Reihenfolge einschließlich gleicher Minuten bleiben erhalten. Entfernung der Karte und fehlende Tage werden nicht ergänzt.
- Die Profilvorschau verwendet gespeicherte normalisierte Tage. Quellen ohne Normalisierung bleiben über den bisherigen Parser lesbar. Lesen/Archivauswahl erzeugt keine Normalisierung oder Profilbindung; erst ein frischer, ausdrücklich gespeicherter eigener Lesevorgang darf passende vorhandene Quelldateien wiederverwenden.
- Noch kein vollständiger Import, konfliktbereinigter Tageskalender, berechnete Arbeitszeit, neue Fahrzeug-/GNSS-Datei oder automatische USB-Importbedienung. Letztere bleibt gemäß PR #31 verbindliches späteres Ziel.
- Vier neue Fingerprint-Unit-Tests und zehn neue SQLCipher-Instrumentierungstests prüfen Inhaltstreue, Ringverschiebung, Fassungen, Kartenherkunft, Deduplizierung, Verschlüsselung/Wiederöffnung, Fehler-Rollback und Migration 5 → 6. Die finale CI und der [Update-Gerätetest](NORMALIZED_DAYS_DEVICE_TEST.md) sind bestanden; keine erneute Implementierung oder Testwiederholung allein wegen eines Work-Wechsels.
- Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/6.json` stammt aus dem erfolgreichen Build von [Lauf 36876124448, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36876124448/attempts/2), Produktkopf `ad9e558`. Seine elf bisherigen Entities sind identisch zu Schema 5; ausschließlich die drei beschriebenen Tabellen kommen hinzu. SQLite-Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem Export; keine Fremdschlüsselverletzung. Schema-SHA256: `4e0a5e5f70db778d5f0426c3ab26839e9e1b47ce09a823178d2cc36a0b250ed0`.
- Die separate App **FahrerTagebuch Test** erhält eine gleichbleibende öffentliche Entwicklungssignatur. Für PR #32 baute/prüfte CI eine Schema-5-Ausgangs-APK und das Schema-6-Update mit derselben Paketkennung. Die bisherigen Debug-/Internal-Installationen bleiben getrennt; aktuelle Folgetests, Testschlüssel und Buildherkunft in [ci/README.md](../ci/README.md).

## Abgeschlossene normalisierte Fahrzeugnutzungen (PR #34)

[PR #34](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/34), Branch `feature/normalized-vehicle-sources`, baut auf dem bestandenen PR-32-Stand auf. Zwei additive Tabellen in Schema 7 speichern jeden dekodierten Fahrzeugnutzungsinhalt sowie dessen genaue Herkunft aus physischer Karte, Quelldatei, Datensatzreihenfolge und Ringslot. Identische Inhalte derselben Karte werden unabhängig vom Ringslot dedupliziert; geänderte Nutzungsfassungen und Ersatzkarten bleiben getrennt und schreiben frühere Imports nicht um.

Normalisierung, Kartentage, Rohquellen, Dateipaar, konkrete Karte und Sitzung werden gemeinsam atomar gespeichert. Profilvorschau und Fahrzeughistorie lesen die gespeicherten Nutzungen. Ältere unnormalisierte Quellen bleiben read-only über den bisherigen Parser verfügbar; weder Migration noch Öffnen oder Wiederholen eines alten Speicherauftrags erzeugt eine neue Zuordnung. Erst ein frischer eigener Lese-/Speichervorgang darf vorhandene identische Rohdateien wiederverwenden und zusätzlich normalisieren.

Keine aufgelöste dauerhafte Fahrzeugentität, neue Fahrzeugdarstellung, Kilometerberechnung, Tätigkeitszuordnung oder automatische USB-Bedienung in diesem Schritt. Fünf neue Fingerprint-Unit-Tests und zwölf SQLCipher-Instrumentierungstests decken exakte Quellwerte/offene Enden, Gen1, Ringverschiebung, Fassungen, Ersatz-/Fremdkarten, Wiederöffnung, Deduplizierung, Rollback und Migration 6 → 7 ab. Vollständigen automatischen Nachweis am veröffentlichten Kopf über die [PR-34-Prüfungen](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/34/checks) heranziehen. Der [Update-Gerätetest](NORMALIZED_VEHICLES_DEVICE_TEST.md) wurde am 02.10.2026 um 10:53 Uhr bestätigt; PR #34 danach mit unverändertem Kopf gemergt. Finale CI 36978402626: 66 Unit-, 49 Daten-/SQLCipher- und 30 App-/Compose-Tests, keine Fehler/übersprungenen Tests.

Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/7.json` stammt aus dem erfolgreichen Build von [Lauf 36976965131](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36976965131), Produktkopf `50327a1`. Alle 14 bisherigen Entities sind exakt identisch zu Schema 6; nur die beiden beschriebenen Tabellen kommen hinzu. Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem generierten Export; keine Fremdschlüsselverletzung. Schema-SHA256: `6d7a2837b83e9ccf549e45d0154abc4c3b17fdc94c86e82d90608e2bd8ff7cf0`. Der Build bestand 66 Unit-Tests, Lint und die APK-/Signatur-/Versionsprüfung; die vollständige Emulatorverifikation ist über den jeweils aktuellen PR-Kopf nachzuweisen.

Das neue Testpaar verwendet Schema 6 aus dem unveränderten PR-32-Merge `b0583a6ea8b15b7260b76c19e56f5166608bb898` als Ausgangscode (40101) und Schema 7 als Update (40102). Nutzer mit eingerichteter PR-32-Test-App installieren nur die Update-APK; kein erneutes Onboarding oder Löschen vorhandener Daten.

## Abgeschlossener automatischer Vordergrund-Import (PR #35)

`feature/card-import-lifecycle` baut auf dem unveränderten PR-34-Merge auf. Der normale Ablauf erkennt vorhandene/angeschlossene Leser und eingelegte Karten in der geöffneten App. Android-USB-Freigabe bleibt notwendig. Einmal pro beobachteter Einlage lesen; keine automatische Leseschleife bei unveränderter eingesteckter Karte. Karte herausnehmen und erneut einlegen löst einen frischen Lesevorgang aus. Nach Rückkehr in den Vordergrund wird die Karte frisch geprüft. Kein Hintergrunddienst, kein Lesen bei geschlossener App, keine Smartphone-Ortung.

Bekannte/reliabel eigene Karten werden ohne Speichern-Knopf übernommen. Erste eigene/unklare Ersatzkarte verlangt Bestätigung; davor wird die tatsächlich eingesteckte Karte nochmals vollständig gelesen. Stimmen Karte/Inhaber nicht mit dem Dialog überein, wird die alte Bestätigung verworfen und die aktuelle Karte erneut geprüft. Fremde/unbestätigte Karten erzeugen weder Profilbindung noch dauerhaften Importlauf oder Datensitzung. Der zuletzt eigene Import wird während fremder/noch ungeklärter Live-Karten nicht als deren Daten angezeigt.

Schema 8 ergänzt ausschließlich `card_import_runs` und `card_import_file_results`. Eigene akzeptierte Übernahmen erhalten exakte unterstützte Datenstatus (gespeichert/fehlend/Lesefehler/ungültig/leer/nicht gespeichert), Kartengeneration und Quellzählungen/Zeitraum. Rohquellen, normalisierte Inhalte, Sitzung und erfolgreicher Ergebnisbeleg werden gemeinsam atomar gespeichert. Ein Speicherfehler rollt alle neuen Datenschreibvorgänge zurück und bleibt als eigener Fehlversuch ohne Datensitzung sichtbar. Prozessabbruch wird beim nächsten Prozessstart einmal als unterbrochen beendet; es werden keine gelesenen Daten rekonstruiert. Neue requestId ist ein neuer Versuch, derselbe Auftrag bleibt inhaltlich geprüft idempotent. Unveränderte Daten werden weiterhin dedupliziert.

Importverlauf verwendet Metadaten und öffnet nur die ausgewählte Sitzung; defekte Rohdaten verhindern nicht die ganze Metadatenübersicht. Frühere explizite eigene Sitzungen bleiben sichtbar, ohne erfundene neue Fehler-/Zeitdetails. Unzugeordnete Dateien bleiben im getrennten read-only Archiv. Die normale gespeicherte Vorschau führt keine Leser-/Speicheraktion aus. Fünf Haupttabs, APDUs, Dateiparser, Tages-/Fahrzeugquellen, Fachlogik und Produktgrenzen bleiben erhalten. Weitere Kartenfiles, kanonischer Kalender, Fahrzeugentität, Kilometer-/Arbeitszeit-/Spesenberechnung und vollständiger Import fehlen.

Zwölf neue Unit-Tests des Koordinators, fünfzehn neue reale SQLCipher- und sechs neue Compose-Tests sind vorhanden. Der [USB-/Update-Gerätetest](AUTOMATIC_IMPORT_DEVICE_TEST.md) war erforderlich, weil Anschluss, Slotzustand, Freigabe und Lesen seit PR #35 automatisch zusammenarbeiten. Test-APK: Schema-7-Ausgangscode `7eb2c6fa8aaba38a09e89fa292a6bf9265248684`, 40102, Update 40103, gleicher öffentlicher Testschlüssel/Paketkennung. Nutzer mit PR-34-Test-App nur das Update installieren. [PR #35](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/35) ist nach dem am 02.10.2026 um 13:30 Uhr bestätigten Gerätetest am unveränderten Kopf gemergt.

Der unverändert übernommene Room-Export `data/schemas/de.fahrerabrechnung.data.db.AppDatabase/8.json` stammt aus dem erfolgreichen Build von [Lauf 36993377935](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36993377935), Produktkopf `31cb01eb2720ec2ed4af3df5ca370028ee280b46`. Schema-SHA256 `5614d0a4b4146e08397be0740dff5e6dc04822ff3dd53e332510d983b54d3c8d`, Room-Identität `3ccf9792f9859e1e03e16faa5afe016d`. Alle 16 bisherigen Entities exakt identisch zu Schema 7; nur die beiden Tabellen für Import-/Datenstatus kommen hinzu. Spalten, Indizes und Fremdschlüssel nach Migration entsprechen dem Export, keine FK-Verletzung.

[Finaler Lauf 36996161264](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36996161264) am unveränderten getesteten Kopf `47ea64cbea72c24c6231af26d451ee59d611b6dd`: 78 Unit-, 64 Daten-/SQLCipher- und 36 App-/Compose-Tests bestanden, keine Fehler oder übersprungenen Tests. Lint, Debug-/Internal-/DeviceTest-Builds sowie Signatur-/Paket-/Versionsprüfungen erfolgreich; generierter Schemaexport im finalen Build byte-identisch. Die Sitzungsauswahl erfolgt auf dem UI-Hauptthread; Navigation bei USB-Freigabe und leerem Leser ist regressionsgeprüft.

## Abgeschlossene unabhängige Dateilesung (PR #37)

Branch `fix/independent-card-source-reads` baut auf dem unveränderten bestandenen PR-35-Stand und dessen aktualisierter Übergabe auf. Nach gültiger Kartenidentifikation werden die unterstützten Aktivitäts- und Fahrzeugdateien unabhängig gelesen: ein Aktivitäts-Lesefehler verhindert eine vollständige Fahrzeuglesung nicht mehr. Unvollständige Bytes werden verworfen; der Status unterscheidet echten Lesefehler von fehlendem Eingang. Ungültige Identität bleibt gesperrt, Cancellation wird weitergereicht. Alte Daten werden nicht als Ersatz in die neue Sitzung gepaart. Wenn beide Dateien fehlschlagen, bleibt ein eigener Fehlversuch ohne neue Datensitzung; frühere Daten bleiben erhalten.

Sechs zusätzliche APDU-Unit-Tests für Gen1/Gen2/Gen2V2 und drei zusätzliche SQLCipher-Tests prüfen fehlende/abgeschnittene/ungültige Aktivitäten, unabhängige Fahrzeuge, Abbruch, zwei Fehler, Statusunterschied, Deduplizierung und exakte Wiederöffnung. Der bisherige Trunkierungstest erwartet jetzt vollständige verfügbare Fahrzeuge statt Verlust aller Daten. Schema 8, bestehende Parser, Profilregeln und Tabellen bleiben unverändert; vollständige Kartenabdeckung bleibt offen. [Finale CI 37003289077](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37003289077) am Kopf `af612b87f5266c79cf70201c4b37ef176ed69bdc`: 84 Unit-, 67 Daten-/SQLCipher- und 36 App-/Compose-Tests, keine Fehler oder übersprungenen Tests. Lint, APK-/Signatur-/Versionsprüfung und Schemaexport bestanden. Test-App 40103→40104, gleiche Testsignatur. Der [Gerätetest](INDEPENDENT_READ_DEVICE_TEST.md) ist seit 02.10.2026 um 14:24 Uhr bestanden, PR danach unverändert gemergt.

## Abgeschlossene Kontrollquellen (PR #39)

`feature/card-control-source` erweitert die bestehende Pipeline um EF Control_Activity_Data (0508h, 46 Bytes in Gen1/Gen2/Gen2V2). [Technischer Umfang, Feldbelegung und offizielle Quellen](CONTROL_IMPORT_FORMAT.md). Zwei additive Tabellen in Schema 9 erhalten vollständige verschlüsselte Quellen je physischer DriverCard und ihre exakte Sitzung; alle 18 bisherigen Entities bleiben unverändert, kein Backfill. Kontrolldaten werden unabhängig gelesen; fehlgeschlagene/ungültige Quellen verhindern keine Übernahme anderer gültiger Daten. Vollständig gelesene Dateien ohne Kontrollbeleg bleiben gespeicherte Quellen ohne erfundene Kontrolle. Ältere Läufe behalten ihre Zweiquellen-Abdeckung und ihren Request-Fingerprint. Profile, Karten, Normalisierung und atomare Speicherung weiterverwenden.

Kontrollen stehen ausschließlich in gespeicherte Fahrerkarte → Details → Kontrollen, mit standardmäßig maskierter Kontrollkartennummer. Exakt ausgewählte Quelle, keine spätere Kontrollangabe in ältere Imports übernehmen, keine Verstoßbewertung, kein weiterer Haupttab. Acht Parser-/Fingerprint-, vier Leser-, elf SQLCipher- und sechs Compose-Testmethoden ergänzt. Die automatische Navigation wartet beim App-Start auf den vorhandenen NavHost-Backstack; Zurückgehen erzwingt keine erneute Importnavigation.

Generierter Schema-9-Export aus dem erfolgreichen Build 37010358221 unverändert übernommen und im endgültigen Build byte-identisch bestätigt. SHA256 `0e32f360cdd214019c48a0e8fed7ac1813929a5f1c1462907deeb46fc4297f85`, Room-Identität `d0285c5a87bc3ffc66813ba4b57704cd`. Alle 18 alten Entities exakt identisch zu Schema 8; Migration entspricht den generierten Spalten/Indizes/Fremdschlüsseln. [Finale CI 37013517721](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37013517721) am Kopf `0e8a87df866b28203ee093ef9c87b06b0a7e3d7d` vollständig grün: 96 Unit-, 78 Daten-/SQLCipher- und 42 App-/Compose-Tests, keine Fehler oder übersprungenen Tests; Lint, APK-/Paket-/Signatur-/Versionsprüfungen bestanden. Der [Update-/Leser-Gerätetest](CONTROL_IMPORT_DEVICE_TEST.md) ist seit 02.10.2026 um 18:29 Uhr bestanden, einschließlich bestätigtem leerem Kontrollbeleg. Testpaar 40104→40105, Schema 8→9, gleiche Signatur; PR #39 am unveränderten getesteten Kopf gemergt.

## Letzte Hardware-/Gerätetests

### PR #41 – letzter bestätigter Leser-/Update-Test

**03.10.2026, 08:06 Uhr Europe/Berlin:** Nutzer bestätigte ausdrücklich **„Alle 4 Punkte funktionieren“** zur zuvor im Chat ausgegebenen vierteiligen [Anleitung](EVENT_FAULT_IMPORT_DEVICE_TEST.md): Update 40105→40106 (Schema 9→10) ohne Deinstallation/Datenlöschen mit Profil und Altdaten; eigene Karte automatisch ohne neue Erstbestätigung übernehmen; erneute Lesung/Deduplizierung und getrenntes Öffnen der exakten älteren/neuen Übernahmen; Wiederöffnung nach vollständigem Neustart ohne Leser. Der Screenshot zeigt einen echten Beleg **„Karte während der Fahrt eingelegt“** mit Beginn und Ende am 19.08.2026 um 18:40:15 Uhr sowie Fahrzeugangabe; ausdrücklich **keine Störungen aufgezeichnet**. Gleiche Beginn-/Endzeit bleibt als Quellangabe erhalten, keine daraus erfundene Dauer oder Verstoßbewertung.

Bestanden im vorhandenen Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext. Nichtleere Störungsdaten, Gen1/Gen2, weitere Leser, Fremd-/Ersatzkarten und gezielte Dateifehler sind nicht als zusätzliche manuelle Erfolge behauptet; diese Fälle sind synthetisch automatisch geprüft. Neue Byte-/Gesamtanzahlen wurden nicht nachgemeldet. Vollständige Kartenabdeckung bleibt offen.

**Herkunft:** `FahrerTagebuch-PR41-Ereignisse-Stoerungen-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11259429931`, [Lauf 37083192466](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37083192466), Kopf `69b3566638e6759b544b3470f95d83059a89ae56`. Schema-9-Ausgangs-APK 40105 byte-identisch zur bestandenen PR-39-Update-APK; Schema-10-Update 40106, gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und feste öffentliche Entwicklungssignatur.

- ZIP-SHA256: `b6c8f370b41d595044bce11c33cab090460b49f6790e6ed7dafa812239dd3abb` (50.333.809 Bytes).
- Ausgangs-APK-SHA256: `ab2da7280af44d74caa2e15c587911ac32a91c2720fc49688e9e949792596e8a`.
- Update-APK-SHA256: `369d5e0e2c9f7975679baf902421677a2bb9a23c25f30380ad8bb104c1984bca`.

PR #41 am unveränderten getesteten Kopf gemergt: `e957834d2cfa5e1dcd4e54bb458a1e078f2a57f5`, Tree `0de278c6e235823fc34c1e4f896bde2c800ee1d9`. Dieser bestandene Ablauf ist die aktuelle Hardwarebasis; die Freigabe zum gebündelten Weiterarbeiten besteht fort.

### PR #39 – vorheriger bestätigter Leser-/Update-Test

**02.10.2026, 18:29 Uhr Europe/Berlin:** Nutzer bestätigte **„Passt 👍“** zur unmittelbar zuvor im Chat bereitgestellten vierteiligen [Anleitung](CONTROL_IMPORT_DEVICE_TEST.md): Update 40104→40105 (Schema 8→9) ohne Deinstallation/Datenlöschen, Profil und bisherige Daten erhalten; eigene Karte automatisch ohne neue Erstbestätigung übernehmen; alte und neue Imports einzeln öffnen, identische Quellen deduplizieren; Wiederöffnung nach vollständigem Neustart ohne Leser. Der Screenshot zeigt ausdrücklich **„In dieser Kartendatei ist keine Kontrolle aufgezeichnet.“** und bestätigt den vorgesehenen Fall einer gelesenen Quelle ohne Kontrollbeleg.

Vorhandener Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext. Keine neuen Byte-/Datensatzwerte oder zusätzlichen Karten-/Leserkombinationen. Nichtleere Kontrollbelege und das Einblenden einer vorhandenen Kontrollkartennummer wurden mangels Beleg nicht separat am Gerät bestätigt; diese Fälle sowie Gen1/Gen2, Fremd-/Ersatzkarten und Fehlerfälle sind automatisch geprüft. Der Test ist keine vollständige Kartenabdeckung oder pauschale Hardwarefreigabe.

**Herkunft:** `FahrerTagebuch-PR39-Kontrollimport-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11230202438`, [Lauf 37013517721](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37013517721), Kopf `0e8a87df866b28203ee093ef9c87b06b0a7e3d7d`. Schema-8-Ausgangs-APK 40104 byte-identisch zur bestandenen PR-37-Update-APK; Schema-9-Update 40105, gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und öffentliche Entwicklungssignatur.

- ZIP-SHA256: `7374a6ef29fa07408f71b8aae0768941cbdfa3f0c81ff5f8d629e74df3066695` (50.275.143 Bytes).
- Ausgangs-APK-SHA256: `821eba8989256aa66026af7a8e56bde5d8ac505e00f5ceab58fb2c6b52b38220`.
- Update-APK-SHA256: `ab2da7280af44d74caa2e15c587911ac32a91c2720fc49688e9e949792596e8a`.

PR #39 am unveränderten getesteten Kopf gemergt: `13d3a31786a2d915bc365ae517c03713de179e51`, Tree `f20f108a63802456843ba8917f841b8d667a2592`. Dieser bestandene Ablauf ist die vorherige Hardwarebasis. Erfolgreiche Tests nicht allein wegen einer Work-Unterbrechung wiederholen.

### PR #37 – vorheriger bestätigter Leser-/Update-Test

**02.10.2026, 14:24 Uhr Europe/Berlin:** Nutzer bestätigte **„Passt alles.“** zur unmittelbar zuvor im Chat bereitgestellten [Anleitung](INDEPENDENT_READ_DEVICE_TEST.md). Update 40103→40104 ohne Deinstallation/Datenlöschen, Profil und ältere Daten erhalten; eigene Karte automatisch ohne Erstbestätigung übernehmen, Aktivitäten und Fahrzeuge verfügbar, keine Leseschleife; Wiederöffnung alter/neuer Imports nach vollständigem Neustart ohne Leser. Lesertrennung während der Übernahme war zusätzlich vorgesehen, soweit herstellbar; dazu liegt kein separates Einzelprotokoll vor. Vorhandener Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext, keine neuen Einzelmesswerte oder zusätzlichen Karten-/Leserkombinationen. Isolierte Fehler einer einzelnen Kartendatei sind durch die automatischen Tests belegt, nicht als manuell erzeugter Hardwarefall bestätigt.

**Herkunft:** `FahrerTagebuch-PR37-Lesefehler-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11225246786`, [Lauf 37003289077](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37003289077), Kopf `af612b87f5266c79cf70201c4b37ef176ed69bdc`. Schema 8 in beiden APKs; Ausgangs-APK 40103 byte-identisch zur bestandenen PR-35-Update-APK, Update 40104. Gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und öffentliche Entwicklungssignatur.

- ZIP-SHA256: `87e6b09a3d4f12386f11edb71dad3f301ce9e6e0e29a3a6d029a1dd196d50bb9` (50.246.717 Bytes).
- Ausgangs-APK-SHA256: `0444438a09aa3c9f495b8385fdcb19fb87c1a6cbf88d0f2e55d724160f3aef0e`.
- Update-APK-SHA256: `821eba8989256aa66026af7a8e56bde5d8ac505e00f5ceab58fb2c6b52b38220`.

PR #37 am unveränderten Kopf gemergt: `c4c4a7ef9a49ec003476731518cabadaa55ebe97`, Tree `f7fdd8bdd3c807ecb33b1e1941de330a5e2f83d5`, exakt der getestete Stand. Dieser bestandene Ablauf war bis PR #39 die Hardwarebasis und wird nicht allein wegen einer Work-Unterbrechung wiederholt.

### PR #35 – vorheriger bestätigter automatischer USB-/Update-Test

**02.10.2026, 13:30 Uhr Europe/Berlin:** Nutzer bestätigte **„Hab alles abgearbeitet, und es funktioniert alles wunderbar“** zur unmittelbar zuvor im Chat bereitgestellten [Anleitung](AUTOMATIC_IMPORT_DEVICE_TEST.md). Rückmeldung umfasst echtes Schema-7→8-Update ohne Deinstallation und Erhalt von Profil/älteren Daten; automatisches Öffnen/Lesen/Speichern bei Leseranschluss und Karteneinlage; genau ein Lesevorgang je beobachteter Einlage, keine Leseschleife/identischen Zusatznutzungen; erneutes Einlegen ohne Lesertrennung; Wiederöffnung alter/neuer Daten nach vollständigem Neustart ohne Leser sowie Erkennen des bereits angeschlossenen Lesers beim App-Start. Die Anleitung sah zusätzlich Freigabe-/Abbruchfälle vor, soweit am Gerät herstellbar; dazu keine getrennten Einzelprotokolle. Vorhandener Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext; keine neuen Einzelmesswerte, weiteren Karten-/Leserkombinationen oder manuell geprüften Fremd-/Ersatzkarten behaupten.

**Herkunft:** `FahrerTagebuch-PR35-Autoimport-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11222550163`, [Lauf 36996161264](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36996161264), Kopf `47ea64cbea72c24c6231af26d451ee59d611b6dd`. Schema-7-Ausgangs-APK (40102) byte-identisch zur bestandenen PR-34-Update-APK; Schema-8-Update (40103), gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und öffentliche Entwicklungssignatur.

- ZIP-SHA256: `0e6c514daab2417731e9467e7018669167edb639ae13cb099d18bbc00d30a845` (50.145.087 Bytes).
- Ausgangs-APK-SHA256: `592435fd14f5a1c238d3bb8b12162dcc727debe65caa3de3d957bde4fa7e44cd`.
- Update-APK-SHA256: `0444438a09aa3c9f495b8385fdcb19fb87c1a6cbf88d0f2e55d724160f3aef0e`.

PR #35 mit unverändertem Kopf gemergt: `909dd6652d029cab842ee986c4756fac0fd2ff93`, Tree `62541078580465bed35c79175594c8744e45b72b`, exakt derselbe Produktstand wie in der geprüften APK. Dieser bestandene Ablauf war bis PR #37 die Hardwarebasis und wird allein wegen eines Work-Wechsels nicht wiederholt. Die am 02.10.2026 erlaubte gebündelte Fortsetzung bleibt bestehen.

### PR #34 – vorheriger bestätigter Update-/Persistenztest

**02.10.2026, 10:53 Uhr Europe/Berlin:** Nutzer bestätigte **„Hat alles geklappt“** zu den unmittelbar zuvor im Chat bereitgestellten PR-34-Testschritten: echtes Update Schema 6→7 ohne Deinstallation, bestehendes Profil/ältere Kartentage und Fahrzeugnutzungen erhalten; eigene Karte erneut ohne Erstbestätigung lesen und gemeinsam speichern; alte und neue Sitzungen/Daten nach vollständigem Neustart ohne Leser öffnen; unveränderte Wiederholung erzeugt keine zusätzlichen identischen Fahrzeugnutzungen. Dies gilt im vorhandenen Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext. Keine neuen Einzelmesswerte, zusätzliche Karten-/Leserkombinationen oder Fremd-/Ersatzkarten manuell behauptet.

**Herkunft:** `FahrerTagebuch-PR34-Update-Test.zip`, finales CI-Artefakt `FahrerTagebuch-device-test-update`, ID `11214921515`, [Lauf 36978402626](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36978402626), getesteter Kopf `c4392761a5db904ec9ad5b4ea9a3d999be68053c`. Schema-6-Ausgangscode `b0583a6ea8b15b7260b76c19e56f5166608bb898`, Version 40101; Schema-7-Update 40102. Paket `de.fahrerabrechnung.app.devicetest` und Entwicklungssignatur gleich.

- ZIP-SHA256: `917bf5c61eabd221dfcb62b55fc62ef923701ebcc055375f53b4f703c6637c32`.
- Ausgangs-APK-SHA256: `87436270c797fc82f31c158caaa6bfa3659e71e93c16e0e5353e6a800ae82884`.
- Update-APK-SHA256: `592435fd14f5a1c238d3bb8b12162dcc727debe65caa3de3d957bde4fa7e44cd`.

PR #34 wurde mit unverändertem Kopf gemergt: `7eb2c6fa8aaba38a09e89fa292a6bf9265248684`, Tree `be841b982792b02b8ae07e1f3ad2b1f3f134242e`, exakt der getestete Kopf. Dieser Test ersetzte PR #32 als damalige Hardwarebasis; aktuell ist PR #39 maßgeblich.

In derselben Nachricht erlaubte der Nutzer: **„mach bitte erstmal ein gutes Stück alleine weiter, und melde dich bitte erst wenn ein Hardware test wirklich erforderlich ist“**. Das autorisiert gebündelte begrenzte Folgeschritte mit vollständiger automatischer Verifikation bis zum sachlich notwendigen Hardware-Prüfpunkt; kein erneutes Nachfragen nach jedem PR. Die inzwischen bestandene Leserprobe von PR #35 ist oben dokumentiert; bisherige erfolgreiche Tests werden nicht allein wegen einer Work-Unterbrechung wiederholt.

### PR #32 – vorheriger bestätigter Update-/Persistenztest

**02.10.2026, 08:25 Uhr Europe/Berlin:** Der Nutzer bestätigte mit **„Alle Punkte funktionieren perfekt“** sämtliche vier im Chat bereitgestellten Testschritte. Die Rückmeldung gilt im bisherigen Galaxy-S23-/Android-16-/USB-CCID-/Gen2V2-Kontext:

- Schema-5-Ausgangs-APK installieren, Onboarding durchführen, eigene Karte bestätigen und beide Dateien speichern.
- Leser entfernen, App vollständig beenden und Schema-6-APK als echtes Update ohne Deinstallation installieren.
- Ohne Leser: kein neues Onboarding; vorhandenes Profil, Kartentage und Fahrzeugvorschau bleiben verfügbar.
- Eigene Karte erneut ohne Erstbestätigung lesen und speichern; bei unverändertem Inhalt 0 neue Quelldateien. Alte und neue Vorschauen bleiben nach vollständigem Neustart ohne Leser verfügbar.

Damit ist der vorgesehene Schema-5→6-Gerätetest bestanden. Neue Byte-/Datensatzwerte oder genaue Sitzungsanzahlen wurden nicht nachgemeldet. Fremd-/Ersatzkarten, andere Generationen/Leser und Fehlerfälle sind keine zusätzlich behaupteten manuellen Erfolge. Der Test beweist weder eine Migration der früheren getrennten Debug-/Internal-Installationen noch ein Schema-4→5-Update oder den vollständigen Kartenimport.

**Herkunft:** `FahrerTagebuch-PR32-Update-Test.zip`, Artefakt `FahrerTagebuch-device-test-update`, ID `11187283572`, aus [vollständig grünem CI-Lauf 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2). Getesteter PR-32-Kopf `7194869bdc2a45ae79e1e88c823a2cdf2d7a6182`; Schema-5-Ausgangscode `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit ausschließlich Test-Buildkonfiguration gemäß [ci/README.md](../ci/README.md). Beide APKs verwenden `de.fahrerabrechnung.app.devicetest` und dieselbe Entwicklungssignatur; Versionscodes 40100 → 40101.

- ZIP-SHA256: `38a846b935eb4215a84351ffdb345f59a6a006b23242289fcf4bc233e65cb43f`.
- Ausgangs-APK-SHA256: `76fda42afb378ea97766647c397d5c08deddc8957bc27d7c5695681f0104adc5`.
- Update-APK-SHA256: `87436270c797fc82f31c158caaa6bfa3659e71e93c16e0e5353e6a800ae82884`.

PR #32 wurde nach dieser Bestätigung mit unverändertem Kopf gemergt. Dieser Test war bis zum bestandenen PR-34-Test der Ausgangspunkt für weitere import-/persistenzrelevante Änderungen.

### PR #30 – vorheriger bestätigter Profilbindungs-Ablauf

**01.10.2026, etwa 15:00–15:47 Uhr Europe/Berlin:** Galaxy S23 / Android 16, vorhandener USB-CCID-Leser und eigene Gen2V2-Karte. Video, Screenshots und ausdrückliche Rückmeldungen bestätigen:

- Aktivitätsdatei 13.776 Bytes / 74 Tagesdatensätze, Fahrzeugdatei 9.600 Bytes / 69 Nutzungen.
- Eigene Karte ausdrücklich bestätigt; gemeinsame Speicherung mit zwei neuen Quelldateien und sichtbarer Profil-Teilimport-Sitzung.
- Sitzung und beide Dateien nach vollständigem App-Neustart ohne Leser verfügbar.
- Wiederholtes Lesen erkennt die eigene Karte ohne Erstbestätigung; erneutes Speichern meldet Erfolg und **0 neue Quelldateien**.

Der geprüfte Ablauf ist bestanden. Die genaue Sitzungsanzahl nach Wiederholung wurde nicht separat gezeigt. Abbruch, Fremd-/Ersatzkarten, weitere Generationen/Leser und Fehlerfälle wurden nicht als zusätzliche manuelle Erfolge behauptet; synthetische automatische Tests sichern diese Fälle ab. Der vollständige Import bleibt offen.

**Herkunft:** finaler PR-30-Kopf `09ccfdf2f270164f4ee9968dc9e022218b53360f`, vollständig grüner [CI-Lauf 36859243839](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36859243839). Ausgeliefert wurde nach dem Debug-Installationsfehler die parallel installierbare Internal-ZIP `FahrerTagebuch-PR30-Paralleltest-Internal.zip`, Artefakt `11161857220`. ZIP-SHA256 `cb9bd0b7f3f7ddfaf9e794adff24c4a46f585e66e84e5275c58526e0e8ee31c0`; APK-SHA256 `5aaa301d7215694ed2dc9f3f5fa24dead4949ddccf2a9da094770d939500abe4`.

**Historische Installations-/Testgrenze:** Die PR-28- und PR-30-Debug-Zertifikate unterscheiden sich. Die parallel bereitgestellte Internal-App hat eine andere Paketkennung; dieser PR-30-Test wies keinen Datenerhalt bei einem echten Update nach. Vorhandene Daten nicht zur Umgehung löschen. Der separate PR-32-Test bestätigt inzwischen das echte Schema-5→6-Update innerhalb der konsistent signierten Test-App; die früheren Installationen bleiben getrennt.

### PR #28 – vorherige Nutzerrückmeldung

**01.10.2026, etwa 12:20 Uhr Europe/Berlin:** Nach Bereitstellung der PR-28-Debug-ZIP und einer Anleitung für Installation, „Kartendaten testen“, gemeinsame Speicherung, Lesertrennung, vollständiges Beenden und Wiederöffnen der App meldete der Nutzer: **„Funktioniert soweit“**. Diese Rückmeldung ist als bestandener Gerätetest im bisherigen Galaxy-S23-/Android-16-/CCID-/Gen2V2-Kontext übernommen.

Neue Einzelwerte, Screenshots und Angaben zum Installationsweg wurden nicht nachgemeldet. Die nachstehenden 74 Tagesdatensätze und 69 Fahrzeugnutzungen sind weiterhin die Messwerte aus PR #26. Das Geburtsdatum wird nicht in der Oberfläche angezeigt; seine genaue Dekodierung ist durch automatische Tests abgesichert. Diese Rückmeldung ist keine allgemeine Freigabe anderer Karten oder Leser.

- Ausgeliefert: `FahrerTagebuch-PR28-Datumsparser-Test.zip`, enthält `app-debug.apk`.
- Herkunft: [vollständig grüner PR-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966), Debug-Artefakt ID `11153391471`, PR-Kopf `969310b68446437e2214ea07e3e3155a2a72bb86`.
- ZIP-SHA256: `8d563f345abd90205daa12727a4d758e6170b44d3c4fe3b31462b96ed6362728`.
- APK-SHA256: `c729cc1a8f35bf78f0a080a28ba8e99546e9579094503193ad0f7207777a5b7e`.
- Der Zertifikatsvergleich ergab eine andere Debug-Signatur als bei der PR-26-CI-APK. Ein direktes Update über diese PR-26-APK ist damit blockiert; zur Umgehung nicht deinstallieren und gespeicherte Daten verlieren. Die Nutzerrückmeldung erklärt den Installationsweg nicht. PR #28 ändert keine Signierkonfiguration.

### PR #26 – vorheriger Test mit Einzelmesswerten

**Datum:** 01.10.2026, etwa 10:37–10:41 Uhr Europe/Berlin.  
**Gerät:** Galaxy S23, Android 16; vorhandener USB-CCID-Leser und echte Gen2V2-Fahrerkarte. Das genaue Lesermodell ist in dieser Rückmeldung nicht neu spezifiziert.

| Prüfung | Bestätigtes Ergebnis |
|---|---|
| Aktivitätsdatei | 13.776 Bytes gelesen; 74 Tagesdatensätze dekodiert |
| Fahrzeugdatei | 9.600 Bytes gelesen; 69 Nutzungen dekodiert |
| Gemeinsame Speicherung | Erfolgsmeldung „Beide Dateien gemeinsam verschlüsselt gespeichert.“ |
| Dateiübersicht | Ein gemeinsam gespeichertes Dateipaar, 74 Tagesdatensätze / 69 Fahrzeugnutzungen; Kartennummer maskiert |
| Leser entfernen und App neu starten | Nutzer bestätigte, dies bereits durchgeführt zu haben; letzter Screenshot zeigt die gespeicherte Übersicht ohne angeschlossenen Leser |
| Ergebnis | Gemeinsame Speicherung und Verfügbarkeit des Dateipaars nach Lesertrennung/App-Neustart am Gerät bestätigt |

Die Rückmeldung bestand aus drei Screenshots und der ausdrücklichen Nutzerbestätigung. Keine echten Rohdateien, VINs oder unmaskierten Kartennummern werden für diese Übergabe übernommen.

**Testgrenze:** Der Test bestätigt die vorhandene Geräte-/Leser-/Gen2V2-Kombination. Er ist keine Hardwarefreigabe für alle Generationen/Leser und kein Beweis für einen vollständigen Kartenimport. Er ersetzt auch nicht die automatischen Fehlerfalltests. Doppelt wirkende Einträge in früheren langen Handy-Screenshots waren laut Nutzer ein Screenshot-/Handyproblem; die tatsächliche App zeigte drei Fahrzeuge.

### Getestete PR-26-APK

- Ausgeliefert: **Debug-APK als ZIP**, `FahrerTagebuch-PR26-Kartenquellen-Test.zip`.
- Herkunft: [grüner CI-Lauf 36568389361](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361), Artefakt `Fahrerabrechnung-0.4.1-debug-apk`, ID `11032504544`; enthält `app-debug.apk`.
- APK-Quellstand: `7a112c09770e078e78b4bcd29ef43fbdf211723f`. Der nachfolgende Commit ergänzte ausschließlich den generierten Room-Schemaexport; der App-Code ist gleich.
- ZIP-SHA256: `1520ff9a7fa12b183a9784eed8383379a6f81e2a4b0183c057899d569fd3b0d3`.
- APK-SHA256: `2e20fc3e05c4cfd1fe12278cfafad731b21ccd334008a3a777dbf9ef6a15d6da`.

Bei Bedarf diese APK über das GitHub-Artefakt wiederherstellen. Eine ältere PR-APK nicht als neue Version ausgeben. Der Nutzer benötigt APKs **als ZIP**.

## Automatische Verifikation

- [Finaler PR-32-Lauf 36911258471, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36911258471/attempts/2) für `7194869`: **vollständig grün**, 61 Unit-Tests, 37 Daten-/SQLCipher- und 30 App-/Compose-Emulatortests; keine Fehler oder übersprungenen Tests. Alle zehn neuen Normalisierungs-/Migrationstests bestanden. Lint, Debug-/Internal-/Test-APKs, gleiche Testsignaturen und steigende Versionscodes geprüft. Der erste Emulatorversuch scheiterte vor Testbeginn an der Runner-Hardwarebeschleunigung; nur der fehlgeschlagene Job wurde erneut ausgeführt und bestand.
- [Vollständiger PR-28-Lauf 36845103966](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36845103966) für `969310b`: **grün**, einschließlich Unit-Tests, Lint, Debug-/Internal-APK-Verifikation und Room-/SQLCipher-/Compose-Instrumentierung. **13 Daten- und 22 App-Instrumentierungstests**, keine Fehler oder übersprungenen Tests. Zehn neue Regressionstestmethoden prüfen Datef-Dekodierung, Schaltjahre, Nullwert, ungültige BCD-/Kalenderdaten, Trunkierung und Leserablauf für Gen1/Gen2/Gen2V2.
- [Vollständiger main-Lauf 36743029047](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36743029047) für `fbac9310`: **grün**, einschließlich Build, Unit-Tests, Lint, Debug-/Internal-APK-Verifikation und Room-/SQLCipher-/Compose-Instrumentierung.
- [Lauf 36568389361](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36568389361) zur ausgelieferten APK: vollständig grün; **13 Daten- und 22 App-Instrumentierungstests**, keine Fehler.
- [Zusätzlicher PR-Lauf 36742530657](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/36742530657) nach dem Schemaexport: Build/Tests/Lint/APK grün, Emulatorjob scheiterte vor den App-Tests bei „Enable emulator hardware acceleration“. Der danach erfolgreiche main-Lauf liefert den vollständigen aktuellen Nachweis.

Gezielte Regressionstests sichern verschlüsselte Wiederöffnung, Deduplizierung, fehlende/defekte zweite Datei, Transaktions-Rollback mit Erhalt älterer Quellen, unveränderte Aktivität bei neuer Fahrzeugdatei und v3-Migration ohne erfundene Paarung. Der Compose-Test öffnet ein älteres Dateipaar mit dessen Fahrzeugen und wechselt zurück zu den neuesten Einzeldateien.

## Wichtige Code-Einstiege

- [DriverCard.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCard.kt): Kartenmodelle, Dateidekodierung und Leserablauf.
- [CardSourcePairRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardSourcePairRepository.kt): validieren, atomar speichern, exakt zugehörige Quellen lesen.
- [CardSourcePairDao.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/CardSourcePairDao.kt): Herkunftstabelle und Fremdschlüssel.
- [CardActivityImportRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardActivityImportRepository.kt) / [CardVehicleImportRepository.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardVehicleImportRepository.kt): Quell-Snapshots und inhaltliche Deduplizierung.
- [AppDatabase.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/AppDatabase.kt) / [DatabaseMigrations.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DatabaseMigrations.kt): main Schema 11, additive Migrationen 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11; keine destruktive Migration.
- [NormalizedCardVehicleStore.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/card/NormalizedCardVehicleStore.kt) / [DriverVehicleUseDao.kt](../data/src/main/kotlin/de/fahrerabrechnung/data/db/DriverVehicleUseDao.kt): Folgeschritt mit unveränderlichen Fahrzeugnutzungen und additiver Migration 6 → 7; bisherige Tabellen unverändert.
- [CardHardwareTestScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardHardwareTestScreen.kt), [CardSnapshotHistoryScreen.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/CardSnapshotHistoryScreen.kt), [AppRoot.kt](../app/src/main/kotlin/de/fahrerabrechnung/app/AppRoot.kt): lesen/speichern, Quellenübersicht und Auswahl der Vorschau.
- [EncryptedDatabaseInstrumentedTest.kt](../data/src/androidTest/kotlin/de/fahrerabrechnung/data/EncryptedDatabaseInstrumentedTest.kt) / [CardSourcePairNavigationTest.kt](../app/src/androidTest/kotlin/de/fahrerabrechnung/app/CardSourcePairNavigationTest.kt): Persistenz-/Migrations- und Navigationsregressionen.

## Noch offen und empfohlene Fortsetzung

Die normale Vordergrund-Lesung übernimmt unterstützte eigene Daten automatisch. Erste eigene/unklare Ersatzkarten verlangen weiterhin Bestätigung. Aktivitäts-, Fahrzeug-, Kontroll-, Ereignis-, Störungs- und Sonderbedingungsquellen sind **persistente, profilgebundene Quellen**, noch keine vollständige Kartenabdeckung oder fachliche Tageszusammenführung.

Offen sind vollständige Importhistorie und Status/Fehlerbehandlung über alle Kartenquellen, vollständiger quellenübergreifender Tagesbestand sowie weitere Dateien für GNSS und Grenzübertritte. Der letzte Kontrollbeleg ist seit PR #39, Ereignisse/Störungen samt Übersicht seit PR #41 im beschriebenen Umfang gespeichert und darstellbar; die gespeicherten Ereignis-/Störungszeitpunkte werden zusätzlich als Marker dargestellt. GNSS-/Grenzübertrittsmarker stehen noch aus. Die Profil-/Kartenbindung aus PR #30 und normalisierten Tagesquellen aus PR #32 sind gemergt und im beschriebenen Umfang am Gerät bestanden. Dauerhafte Fahrzeugzuordnung, Tageskilometer/Arbeitszeiten und fachliche Reise-/Spesen-/Zuschlagsauswertung fehlen. Grundscreens und vorbereitete Entities/Interfaces sind teilweise Preview.

Empfohlener nächster Arbeitsabschnitt: den zuverlässigen vollständigen Import und sein Datenmodell weiter ausbauen, ausgehend von den gespeicherten Quellen. Einen begrenzten Folgeschritt und seine Migration/Fehlerfälle vor Umsetzung festlegen. Die aktuelle Übergabe ist kein automatischer Auftrag, sämtliche Produktpunkte sofort zu implementieren.

**Profilbindung fachlich festgelegt am 01.10.2026:** Single-Driver-App; erste eigene Karte ausdrücklich bestätigen. Die dauerhafte Fahreridentität ist das lokale Profil, nicht die Kartennummer. Belastbar gleiche Inhaber-Identität erlaubt zusätzliche Folge-/Ersatzkarten; unklare Fälle verlangen Bestätigung. Erkennbar fremde Karten bleiben auf den temporären technischen Lesetest begrenzt. Alte Karten/Imports bleiben erhalten; ältere Teilimporte nicht anhand von Datum, Kartennummer oder Vermutung zuordnen. Zielmodell und vollständige Vorgabe stehen in [CURRENT_RULES.md](CURRENT_RULES.md).

Abgeschlossen: normalisierte Tagesquellen mit unveränderlicher Herkunft auf der vorhandenen Profil-/Kartenbindung, vollständige CI und echtes Update von Schema 5 auf 6. Normalisierte Fahrzeugnutzungsquellen aus PR #34 sind ebenfalls abgeschlossen. Die automatische Vordergrund-Übernahme mit nachvollziehbaren Import-/Quellenstatus aus PR #35 ist ebenfalls abgeschlossen. Weitere Kartenquellen in begrenzten Stufen auf dieser Basis ergänzen; keine Fachwerte oder Identitäten raten. Das Onboarding speichert bisher nur Heimatpunkt und Abschlussstatus; die übrigen Profil-/Fachfelder bleiben Vorschau.

### Verbindliche Grenzen bei der Fortsetzung

- Keine Smartphone-Routenaufzeichnung, Hintergrundortung oder Smartphone-Kilometer; Heimatpunkt ausdrücklich per Standort/Karte.
- Echte Tachograph-/Fahrerkarten-GNSS-Punkte später einzeln zeigen; keine erfundene Verbindungsstrecke.
- Kartenentnahme/fehlende Zeit beweist keine Ruhe oder Heimkehr. Heimkehr verlangt belastbaren Heimatstandort und mindestens 8:59 h zusammenhängende belegte Ruhe, rückwirkend auf deren Beginn.
- Übernachtung, Spesen und Arbeitgeberzuschläge gemäß Issue #3/CURRENT_RULES; keine Steuerlogik. Feiertagsgrundlage ist das Bundesland des Firmenstandorts.
- Für die spätere Fahrzeugdarstellung [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md) beachten; Modell/VIN-Zuordnung oder Fahrzeugbild nie raten.
- Rohkartendaten und vollständige APDUs nicht loggen; Testdaten synthetisch, gespeicherte Kartennummern in der Oberfläche maskieren.

### Test- und Arbeitsvereinbarungen

Manueller Gerätetest **spätestens bei jedem dritten PR**, früher sobald Hardware, Import, Parser, Persistenz oder Datenbank betroffen sind. Mehrere PRs bis zu einem späteren Test nur nach konkreter Nutzerfreigabe zusammenfassen. Der bestandene PR-41-Test ist der aktuelle Ausgangspunkt. Die ausdrücklich am 02.10.2026 erlaubte Bündelung ist oben festgehalten; kein schematischer Testaufruf nach jedem Folge-PR. Frühere Aufschübe sind keine allgemeine dauerhafte Ausnahme.

Auf dem bestandenen Test aufbauen; ihn nicht allein wegen des Work-Wechsels wiederholen. Nach neuen Änderungen die passenden automatischen Prüfungen und den nächsten erforderlichen Gerätetest planen. Der Nutzer kann Bildduplikate durch lange Handy-Screenshots haben; vor einer Datenkorrektur echte App-Einträge prüfen.

Bei Unterbrechung zuerst lokale unveröffentlichte Änderungen und den aktuellen GitHub-Stand prüfen und vorhandene Folgearbeit fortführen. PR #30/#31/#32 sind gemergt; Profilbindung und normalisierte Tagesquellen nicht erneut implementieren. PR #34 und PR #35 sind nach bestandenen Tests gemergt. Automatischen Import, normalisierte Quellen und die Kontrollquellen aus Schema 9 nicht nochmals implementieren. Lokale/unveröffentlichte Folgeänderungen zuerst prüfen und exakt dort fortsetzen. Vollständige Kartenabdeckung und Fachauswertung bleiben offen. Der bestandene PR-41-Gerätetest ist die Ausgangsbasis für weitere relevante Änderungen.

## Abgeschlossene Ereignis-/Störungsquellen (PR #41)

Der Pausenstand vom 02.10.2026 wurde am 03.10.2026 auf dem aktuellen PR-40-Merge `169e0c607537953ca9d24c02516a858393a83856` fortgesetzt; vorhandene Implementierung nicht wiederholt. Branch `feature/card-event-fault-sources` ergänzt unabhängig Events_Data (0502) und Faults_Data (0503), exakte verschlüsselte eigene Quellen/Sitzungszuordnung und eine gemeinsame read-only Übersicht in den Kartendetails. Keine Rückfüllung alter Zwei-/Drei-Datei-Imports, keine Verstoß-/Dauerberechnung. Schema 10 ergänzt zwei Tabellen ohne Änderung der bisherigen 20 Entities. [Format und Grenzen](EVENT_FAULT_IMPORT_FORMAT.md), [Gerätetest](EVENT_FAULT_IMPORT_DEVICE_TEST.md). Timeline-Marker waren kein Bestandteil dieses Import-PRs; der unten beschriebene UI-Folgeschritt verwendet diese abgeschlossenen Quellen.

Automatische Verifikation, generierter Schema-10-Export und Geräte-/Update-Test sind vollständig abgeschlossen. PR #41 ist nach Nutzerbestätigung am unveränderten Kopf gemergt; main verwendet Schema 10. Ausgangs-APK und Testpaket stammen unverändert aus dem zuvor bestandenen PR-39-Stand, Updateversion 40106.

Der generierte Schema-10-Export stammt unverändert aus dem erfolgreichen Build von [Lauf 37080786615](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37080786615), Produktkopf `bf9021023786099a5022a5ced3ac801bb6cb7a88`. SHA256 `50b4564e1676d2e84cda1d1b7f1e328c03d0a5b0868e2c9e0c668a686a46cce4`, Room-Identität `0cce5ad464a42f3f21ce0ac68159c1eb`. Alle bisherigen 20 Entities sind exakt unverändert; nur die zwei beschriebenen Tabellen kommen hinzu. SQLite-Spalten/Indizes/Fremdschlüssel nach Migration stimmen mit dem Export überein. [Finale CI 37083192466](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37083192466) zum getesteten Kopf `69b3566638e6759b544b3470f95d83059a89ae56` vollständig grün: 111 Unit-, 90 Daten-/SQLCipher- und 47 App-/Compose-Tests, insgesamt 248 ohne Fehler oder übersprungene Tests; alle sieben vorgeschriebenen Gradle-Prüfungen, Lint und APK-/Signatur-/Versionsprüfung bestanden. Schemaexport im letzten Build byte-identisch. Der oben dokumentierte Update-/Lesertest ist seit 03.10.2026 um 08:06 Uhr bestanden.

Der erste Emulatorjob dieses Laufs scheiterte vor den Tests an der Gradle-Wrapper-Prüfung der offiziellen Distributions-URL (Infrastruktur, kein Testresultat). Der finale Kopf enthält den exportierten Schema-10-Stand und muss einschließlich aller Emulatorprüfungen grün werden. Ein auf der Plattform nicht verfügbarer Kennzeichenzeichensatz wird als Fehler ausschließlich seiner Datei behandelt.

## UI-Folgeschritt: gespeicherte Ereignis-/Störungspunkte in der Tageszeitleiste

Die Tagesvorschau erhält ausschließlich die exakten Quellen und die physische DriverCard-ID ihrer ausgewählten eigenen Sitzung. Vorhandene Beginn-/Endzeitpunkte liegen als getrennte benannte Punkte auf derselben realen Zeitachse wie die Aktivitäten; gleiche Beginn-/Endzeit wird einmal dargestellt. Unbekannte Zeiten werden nicht ergänzt, invertierte Angaben bleiben erhalten. Mehrere Belege an derselben Position bleiben einzeln auswählbar. Tagesgrenzen, doppelte Ortsstunden, Sekunden und UTC-Offsets sowie Ausschnitte beim Zoomen sind berücksichtigt. Antippen/Tagesliste öffnet den bestehenden Originalbeleg; keine Dauer, Verstoßbewertung oder abgeleitete Tätigkeit/Fahrzeug-Zuordnung.

Acht zusätzliche Unit-Tests und sechs Compose-Tests prüfen Projektion, DST, fehlende/gleiche/invertierte Zeitpunkte, Überlagerung, Zoom, Tageswechsel und die Navigation zwischen exakten alten/neuen Übernahmen. Archiv oder eine andere physische Karte erhält keine Marker. Ohne Aktivitätsquelle entsteht kein erfundener Kartentag. [Umfang und optionale Softwareprüfung](EVENT_FAULT_TIMELINE_CHECK.md).

Leser, APDU, Kartenparser, Importpersistenz, alle Tabellen, Schema 10 und Migrationen bleiben unverändert. Test-App 40107 aktualisiert den bestandenen PR-41-Stand 40106 mit gleicher Testsignatur; kein neuer Hardwaretest für diesen reinen Projektions-/UI-Schritt erforderlich. Der zuletzt bestätigte Hardware-/Update-Stand bleibt PR #41 vom 03.10.2026 um 08:06 Uhr. Weitere Kartenquellen und kanonische Tages-/Fachauswertung bleiben gesonderte spätere Schritte.

## Fortsetzung nach PR #43: Sonderbedingungen (PR #44)

PR #43 ist am Kopf d13d57781a021b434f2b46b2b9736e91e02979b4 vollständig grün und gemergt: 119 Unit-, 90 SQLCipher- und 53 App-/Compose-Tests, zusammen 262 ohne Fehler/übersprungene Tests; [CI 37104004156](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37104004156). Merge/main-Ausgangsbasis `10eefb6d8b33f119948349d3ffea63f0c55c25cf`, Tree `592617416c0892902b3bcbfbc99a18a7dcaf68fc` exakt wie geprüft. Keine weitere Hardwareprobe für diesen reinen UI-Schritt.

PR #44 (`feature/card-specific-conditions`) ergänzt genau eine neue unabhängige EF-Quelle für Fähre/Zug und Out of Scope. [Format und technische Primärquellen](SPECIFIC_CONDITION_IMPORT_FORMAT.md). Schema 11 ist rein additiv mit zwei Tabellen und ohne Backfill; die übrigen 22 Entities und gespeicherte Altquellen bleiben erhalten. Eigene physische Karten-/Sitzungsherkunft, historische Request-Fingerprints und Fremdkartensperre bleiben maßgeblich. Read-only Detailansicht; keine Ruhe-/Verstoß-/Reise-/Abrechnungsentscheidung oder neue Marker.

16 Unit-, neun SQLCipher- und fünf Compose-Testmethoden ergänzt. Vor APK-Ausgabe vollständige CI am endgültigen Kopf und echten KSP-Schemaexport prüfen. Die Test-ZIP baut Schema-10-Ausgangsstand PR #43 mit Version 40107 und aktuelles Update 40108/Schema 11 mit demselben festen Testschlüssel. Auf einer vorhandenen PR-41- oder PR-43-Test-App nur die Update-APK darüber installieren. [Vier Prüfpunkte](SPECIFIC_CONDITION_DEVICE_TEST.md). Die Anleitung wurde mit der ZIP bereitgestellt; der Nutzer hat den Hardwaretest anschließend ausdrücklich übersprungen, siehe folgenden Status. Letzte bestätigte Gerätebasis bleibt PR #41.

Schema-11-Export aus CI 37120725391 übernommen und lokal mit Schema 10 sowie der tatsächlichen Migration verglichen: 24 Entities, 22 unverändert, keine Rückfüllung. Hash/Room-Identität in der Formatspezifikation. Der erste CI-Lauf baute Produktvarianten und Lint erfolgreich, meldete aber zwei NIO-Kompilierfehler in neuen Unit-Testhelfern; dieselbe Verkettung wurde auch in den neuen Instrumentierungstesthelfern korrigiert. Der nachfolgende endgültige Lauf 37121217858 am Kopf 6c0ac4ff6c28532c4dcbbc1f2d34377ad7730e3e ist vollständig grün: 292 Tests (135 Unit, 99 SQLCipher, 58 Compose), keine Fehler oder übersprungenen Tests; alle sieben Gradle-Prüfungen, APKs und stabile Test-Update-Paarung erfolgreich. Echter Schemaexport erneut byteidentisch.

**03.10.2026: Nutzer überspringt den PR-44-Gerätetest ausdrücklich**, weil seine Karte weder Out of Scope noch Fähre/Zug enthält, und beauftragt erneute Codeprüfung und Fortsetzung. Die Prüfung fand keinen weiteren Fehler. Die aus genau dem finalen CI-Kopf bereitgestellte Test-ZIP hat SHA-256 251072d4a00bcd63ce3b634cd0c61d3204d5c5bf0b473a6758b8efb153b89e56. Roh-APK 40107: 7a2d04db0e7ef3ea6de06ad474f932f705df527240ef708e872ef24630e25aa7; Update 40108: e57a18ea0c0b547005f1c0ca3df8d3dd870f9bb2c875bf79e72790de2700c8a7. Paket de.fahrerabrechnung.app.devicetest, fester Zertifikat-SHA-256 9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58. Die ursprünglich ausgelieferte Anleitung gehört unverändert zu diesem CI-Artefakt; ihr jetziger Dokumentationskopf ergänzt nur den tatsächlichen Teststatus.

Kein bestätigtes Update auf Schema 11, keine bestätigte Leerdaten-Lesung und keine Hardwareprüfung nichtleerer Sonderbedingungen. Letzte echte Hardwarebasis bleibt PR #41. Die Ausnahme gilt nur für diesen Schritt und setzt den Testzähler nicht auf einen bestandenen neuen Ausgangspunkt. PR #44 am unveränderten geprüften Kopf gemergt: 4b2c0b875d1912a91013bb23f0dbfe1596d0b3a2, Tree 50b953f4cc962daece55528ff743a82186c8a57f exakt wie CI.

## Fortsetzung: Sonderbedingungsmarker auf der Tageszeitleiste

Branch feature/card-specific-condition-timeline baut auf diesem Merge auf. Reiner Projektions-/UI-Schritt analog PR #43: exakte gespeicherte Quelle der ausgewählten eigenen Sitzung und physische DriverCard-ID; keine spätere Quelle in alten Imports/Archiven. Nur belegte Zeitpunkte auf realer Tages-/Zoomachse, 23-/25-Stunden-Tage, exakte Sekunden/Offset. Eigene Spur und Tagesliste halten zeitgleiche Meldungen einzeln auswählbar. Gen1-Fähre bleibt ohne erfundenen Beginn/Ende, reservierte Codes neutral, unbekannte Zeiten nur in der vorhandenen Quellenübersicht. Keine Intervalle, Ruhe-/Verstoß-/Fachberechnung oder Änderung der Tätigkeiten. Leser, Parser, Datenbank und Schema 11 bleiben unverändert. Acht Unit- und acht Compose-Tests ergänzen die bestehende vollständige CI; Test-App 40109. [Umfang und optionale Softwareprüfung](SPECIFIC_CONDITION_TIMELINE_CHECK.md). Eine nicht vorhandene reale Meldung wird nicht als am Gerät geprüft ausgegeben.

## Abgeschlossener PR #45: CI und kurzer Geräte-/Bestandstest

[CI 37137254348, Versuch 6](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/37137254348/attempts/6) am unveränderten Produktkopf `42fb5dea0fb8f18d1b2206bda5d09b59b4ebe8b1` vollständig grün: **143 Unit-, 99 Daten-/SQLCipher- und 66 App-/Compose-Tests, insgesamt 308 ohne Fehler oder übersprungene Tests**. Alle acht neuen Compose-Tests anhand der Instrumentierungsberichte als ausgeführt bestätigt. Alle sieben Gradle-Aufrufe, Lint, APK-/Signatur-/Versionsprüfung und bytegleicher Schema-11-Export bestanden.

Am 10.10.2026 um 05:52 Uhr Europe/Berlin bestätigt der Nutzer zur bereitgestellten Testversion 40109: „Daten sind nach dem schließen und wieder öffnen nach wie vor vorhanden.“ Die Bildschirmaufnahme zeigt gespeicherte Kartentage und Fahrzeughistorie ohne erneute Kartenlesung. Für die ausgewählte ältere Übernahme fehlen gespeicherte Sonderbedingungen. Das ist keine neue Leerdaten-Lesung oder Prüfung realer Fähre-/Zug-/Out-of-Scope-Marker. Installationsweg und Ausgangs-Versionscode sind nicht separat sichtbar; keine genauere Migrationsmessung behauptet. Letzte vollständige bestätigte Karten-/Leserprüfung bleibt PR #41 vom 03.10.2026, 08:06 Uhr.

Nach grüner Gesamt-CI und Nutzerbestätigung am unveränderten Kopf gemergt: `9f24a48845367502e585c87b1eb8e63f370b3dfb`, Tree `90b73aa63229055d5560d6e0f8ad63b855ab30d5`. CI-Infrastruktur aus PR #46/#47 erhalten.

Test-ZIP `FahrerTagebuch-PR45-Test-Update.zip`: nur Update-APK 40109, kurze Anleitung, Prüfsummen und Herkunft. Build-Job `114119290705`, Geräteartefakt `11658395401`, erfolgreicher Emulatorjob `114120535610`. ZIP-SHA-256 `a52f1b9a76d0b3e576aee3a3afbdf6c4e3da97bf2701a814a4ab3e1c82926736`. Paket `de.fahrerabrechnung.app.devicetest`, gleichbleibender öffentlicher Testschlüssel.

Die frühere externe Actions-Quota-Blockade ist erledigt: Nach mitgeteilter Budgeterhöhung auf 2,00 USD bei weiterhin aktivem Stop usage wurde nach kurzer Propagationszeit genau der damals aktuelle fehlgeschlagene Build-Job `113762376670` einmal wiederholt. Versuch 6 bestand einschließlich des abhängigen Emulatorjobs. Keine erneute PR-45-Wiederholung, keine zusätzlichen Löschungen. [Aktuelle CI-/Artefaktstrategie](CI_ARTIFACT_POLICY.md).

## Begrenzte GNSS-Decoder-Fortsetzung

Nach dem bestätigten PR-45-Bestandstest hat der Nutzer am 10.10.2026 um 06:05 Europe/Berlin das Weiterarbeiten bis 07:05 beauftragt. Dokumentationsabschluss PR #48 gemergt; danach isolierter Decoder für EF GNSS_Places auf diesem aktuellen main. [Geprüfte Primärquellen, Layout und Grenzen](GNSS_ACCUMULATED_DRIVING_FORMAT.md). Getrennte Drei-Stunden-/Positionszeiten, signed Grad-/Minutenkoordinaten, Ringreihenfolge/Slots und unveränderte unbekannte Quellfelder; 18 neue synthetische Unit-Testmethoden. Vollständige CI am finalen Kopf prüfen; kein neuer Hardwareerfolg behauptet.

Der Decoder ist noch nicht an Leser/APDU, Requests, Datenbank oder UI angeschlossen. Schema 11, Testversion 40109, bestehende Quellen/Profilbindung bleiben erhalten. Nächster begrenzter Schritt: vollständige Anwendungsidentifikation und unabhängige neue EF-Lesung mit exakter Karten-/Sitzungsherkunft, atomarer Persistenz und eigenem Testpunkt vorbereiten. Frühere Geräte-/Quota-Ausnahmen gelten nicht automatisch für diese neue Leser-/Schemaänderung.

Während PR #49 unverändert durch die vollständige CI läuft, separat auf dessen Decoderstand aufgebaut: EF GNSS_Places_Authentication (nur Gen2V2), 1682 Bytes mit eigenem Zeiger und 336 × 5-Byte-Datensätzen. Der Zeitstempel gehört zum kumulierten Drei-Stunden-Eintrag; Status 00/01 sind aufgezeichnete Werte, 02–FF bleiben reservierte Rohwerte. 12 neue Unit-Testmethoden. Kein Ringindex-Join, kein Trustentscheid, keine Leser-/Persistenz-/UI-Anbindung; der zweite PR enthält bis zum Merge seiner Basis auch den PR-49-Decoder. CI am endgültigen Kopf abwarten, keine weitere Gerätebestätigung behaupten.


## GNSS-Importschritt PR #51 – noch ohne Gerätefreigabe (10.10.2026)

Auf Nutzerfreigabe für weitere Umsetzung erstellt: [PR #51](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/51), Entwurf auf `feature/card-gnss-import`, auf den offenen CI-grünen Decoder-PRs #49/#50. Unabhängige Leser-EFs 0524/0527, volle 17-Byte-Basisidentifikation, exakte eigene Karten-/Sitzungsquellen, additive Schema-12-Migration, getrennte Detailanzeige. Version 40110, Testpaket und Signatur unverändert. Keine Punkt-/Statuszuordnung und keine Route.

Erster Kopf f9911fd05a4915fe054e460361e7117374ce4dd2, [Lauf 38027322333](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38027322333): alle sieben Gradle-Kommandos, Lint, APKs und stabiles Schema-11/40109→Schema-12/40110-Updatepaar bestanden; nur fehlender neuer KSP-Export im Schema-Guard. Emulator daher übersprungen. Diagnoseartefakt 11660872736 lieferte den echten 12.json-Export; kein handgeschriebenes Schema. Alle 24 bisherigen Entity-Exporte unverändert, zwei zusätzliche Tabellen. 15 neue Testmethoden ergänzt, einschließlich echter SQLCipher-Sitzungs-/Rollback-/Upgradeprüfung. Vollständige CI des Folgekopfs ist noch offen.

[Implementierung und Grenzen](GNSS_IMPORT_IMPLEMENTATION.md), [vorbereitete Vier-Punkte-Geräteanleitung](GNSS_IMPORT_DEVICE_TEST.md). Test-ZIP ausschließlich nach voller grüner CI des exakten Quellkopfs erzeugen, für vorhandene Test-App nur Update-APK. PR #51 erst nach ausdrücklicher Testbestätigung mergen. Der bestätigte PR-45-Bestandstest ersetzt diesen neuen Leser-/Migrationstest nicht. Kein Cleanup, keine Änderungen an main oder den Decoder-PR-Köpfen.
