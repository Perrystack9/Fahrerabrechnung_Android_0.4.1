> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Aktueller MF-Entwurf im laufenden zweiten Arbeitsblock

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

Aktuelle Nutzerkorrektur10.10.2026 22:34:51 Europe/Berlin: Pause beendet, zweiter Block22:34:51–11.10.00:34:51. Doppelter Fortsetzungslauf22:56 deaktiviert; Ausgabe00:57 und früheste Hardware-/Paketzeit00:56:06 bleiben. Zuerst konkrete PR55-Instrumentationsursache beheben: versehentlich doppelte conditions(Int)-Testdefinition; kein Produkt-/Schemafehler im erfolgreichen Build behauptet. Neueste PR-Köpfe/CI prüfen. Der ursprüngliche Zeitplan darunter ist historisch.

# Entwicklungspause und späteres gemeinsames Testupdate

Tobi hat am10.10.2026 um19:56:06 Europe/Berlin angewiesen: eine Stunde weiterentwickeln, zwei Stunden Pause, noch zwei Stunden entwickeln, erst danach frühestens Hardwaretest. Erster Block endet20:56:06; Wiederaufnahme22:56:06; zweiter Block endet11.10.00:56:06. Die Fortsetzung ist für22:56:06 und die spätere Ausgabe für00:57 geplant. Keine aktive Entwicklung oder neue Gerätetestanforderung während der Pause. Laufende GitHub-CI muss nicht erneut angestoßen werden.

## Autoritativer Ausgangspunkt

- Main `22d41f64875de3aa5ac383d9d1ce80dc0dee1684` enthält den unverändert gemergten PR53-Kopf `b2973034af43a281df4ab02c5c123b821dab0772`, Schema14/Testversion40112. Die allgemeine Rückmeldung „Funktioniert alles prima“ bezog sich auf dessen zuvor ausgegebenes Update. Keine ungenannten Einzeltests/nichtleeren Datensätze behaupten.
- PR54 Be-/Entladequelle: `6d46ebd6fe77a4c424aee45dc700f0bd186f5d5c`, Schema15/Testversion40113; vollständiger CI-Folgelauf38075694297. Den tatsächlichen finalen Status aller vier Jobs erneut lesen.
- PR55 Ladungsart-Quelle baut auf diesem PR54-Kopf auf, Schema16/Testversion40114. Neuer finaler PR-Kopf und CI stehen im PR selbst. Frühere Abschnitte und zunächst fehlende Exporte sind historische Entwicklungsstände.
- Die beiden neuen Import-/Schema-PRs bleiben bis späterer ausdrücklicher Gerätebestätigung als Entwürfe offen. Das Aufschieben des Tests ist keine Bestätigung neuer Hardwarefunktion.

## Zweiter Arbeitsblock

Zuerst echte Schemaexporte und vollständige CI am neuesten Kopf sicherstellen, Fehler gezielt beheben und den Stand dokumentieren. Danach relevante noch offene Kartendateien gemäß [Abdeckung/Recherchecheckpoint](CARD_SOURCE_COVERAGE.md), aktueller Produktregel und Issue3 weiterentwickeln. Für VehicleUnits_Used vor Codeänderung die noch offenen amtlichen ASN.1-/Kapazitätsdetails prüfen; Fahrtenschreiber-Hersteller nicht als LKW-Hersteller interpretieren.

Eigene physische Karte und exakte Sitzung, unabhängige Teilfehler, unbekannte Rohwerte, kein historischer Backfill/Fallback, fremde/unbestätigte Karten temporär, vollständiger atomarer Import und defensive Quellenprüfung beibehalten. Kein Smartphone-GPS-Tracking, keine Card_Download-Schreiboperation. Keine Subagenten, kein Browser, keine Lösch-/Aufräumaktionen und keine Änderung des Actions-Budgets von2USD/Stop usage Yes.

## Spätere Paketbereitstellung

Nur einen vollständig geprüften, vollständig angebundenen Produktkopf verwenden. Aktuelle APK-Version muss über40112 liegen, gleiche Paketkennung `de.fahrerabrechnung.app.devicetest` und feste Zertifikat-SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`. Das vorhandene Skript prüft das Updatepaar vom unveränderten getesteten PR53-Stand Schema14/40112. Benutzer-ZIP enthält nur Update-APK, gemeinsame Anleitung, Buildinfo und Prüfsummen; keine Baselineinstallation oder Datenlöschung verlangen.

Die bestehende `device-test.yml` prüft vor dem Bauen erfolgreiche vollständige Android-CI des exakten source_sha. Wenn kein Dispatch-Tool angeboten wird, wurde zuletzt eine eigene einmalige `ci/device-test-pr53-b2973034`-Branch mit zusätzlichem `create`-Workflow verwendet: exakten Branchnamen prüfen, exakten CI-Lauf/source_sha und alle vier erfolgreichen Jobs prüfen und anschließend `createWorkflowDispatch` des bestehenden `device-test.yml` mit passender Produktbranch und `include_baseline:false` aufrufen. Referenz liegt im Repository auf dieser Branch; nur auf den finalen aktuellen Kandidaten anpassen. Die Dispatch-Datei gehört nicht in Main oder Produkt-PRs. Erst nach Ende des zweiten Blocks anfordern; keine wiederkehrende CI-/Paketserie daraus machen.

Erfolgreiches Paketartefakt herunterladen, Paketkennung/Version/Signatur und Prüfsummen prüfen, gemeinsame Anleitung an tatsächlich enthaltene Quellen anpassen und neues ZIP verfügbar machen. Bei laufender oder fehlgeschlagener CI den Stand ehrlich melden; keine alte APK als neue ausgeben. Keine neue parallel laufende Umsetzung aus der Ausgabe-Aufgabe beginnen und keine offenen Änderungen ohne Gerätebestätigung mergen.
