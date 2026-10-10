# Dauerhaft aktualisierbare Gerätetest-App

**CI-Speicherstrategie ab 08.10.2026:** Normale erfolgreiche PR-/main-Prüfungen bauen und prüfen die APKs einschließlich des stabilen Update-Paars, laden aber keine Artefakte hoch. Benötigte Test-ZIPs werden gezielt über den manuellen `device-test.yml`-Workflow erzeugt (7 Tage; Fehlerdiagnosen 3 Tage). Vollständige Prüfungen, feste Signatur und Versionskontinuität bleiben erhalten. [Workflow, Startparameter und PR-45-Grenzen](../docs/CI_ARTIFACT_POLICY.md). Die nachstehenden älteren Produkt-/Gerätestände sind historische Herkunft; maßgeblich sind der tatsächliche Git-Stand und die aktuelle Übergabe.

`device-test.jks` ist eine **absichtlich öffentliche, ausschließlich für Entwicklung bestimmte Test-Signieridentität** (PKCS12, Alias `androiddebugkey`, Passwort `android`). Sie ist kein Geheimnis und darf niemals als Release-/Produktionsschlüssel verwendet werden. Die Ausnahme in `.gitignore` gilt nur für diese eine Testdatei. Alle echten Keystores/Signierzugänge bleiben außerhalb des Repositorys.

Nur der debuggable Buildtyp `deviceTest` verwendet sie: Paket `de.fahrerabrechnung.app.devicetest`, sichtbarer Name **FahrerTagebuch Test**. Debug, Internal und Release behalten ihre bisherigen Signierkonfigurationen. Testschlüssel und Paketkennung nicht mit jedem Build wechseln; dadurch bleiben zukünftige Gerätetests als Updates mit vorhandener Datenbank installierbar. Dieses Testpaket ist keine Produktionsauslieferung.

Die PR-28-/PR-30-Debug-APKs und die bisher ausgelieferte PR-30-Internal-APK besitzen andere Signaturen. Ihre vorhandenen Daten werden nicht automatisch in das neue Testpaket übertragen; dafür keine bestehende App deinstallieren oder ihre Daten löschen.

`scripts/build_device_test_update.sh` erzeugt ein überprüftes Paar für den echten Update-Test:

1. Schema-11-Produktcode aus dem gemergten PR-45-Commit `9f24a48845367502e585c87b1eb8e63f370b3dfb`, mit demselben Testpaket/Schlüssel und kleinerem Test-Versionscode **40109**.
2. Aktueller `deviceTest`-Build mit zusätzlichen GNSS-Quellen, Schema 12, Versionscode **40110**.

Für die Ausgangsversion werden ausschließlich Buildkonfiguration, Testlabel und Testschlüssel aus dem aktuellen Stand übernommen. Ihr Produktcode und Room-Schema stammen vollständig aus dem genannten Schema-11-Stand. Nach Installation des Updates die Ausgangsversion nicht erneut installieren. Mit der bereits eingerichteten PR-41-Test-App nur die aktuelle Update-APK verwenden; keine neue Ausgangsinstallation oder Löschung vorhandener Daten.

Das zuvor ausgelieferte PR-32-Paar enthielt Schema 5 aus `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit Versionscode 40100 und Schema 6 mit 40101. Sein echtes Update wurde am 02.10.2026 bestanden; Herkunft und Prüfsummen stehen in WORK_HANDOFF.

CI prüft beide APK-Signaturen gegen das öffentliche Zertifikat dieses Keystores sowie Paketkennung und aufsteigende Versionscodes. Die ZIP enthält APKs, Prüfsummen und die Gerätetest-Anleitung. Produktionssignierung bleibt ein eigener Vorgang mit privatem Schlüssel.

Das bestandene PR-34-Paar aktualisierte Schema 6 (40101) auf Schema 7 (40102). PR #35 aktualisierte Schema 7 (40102) auf Schema 8 (40103). PR #37 ergänzte die unabhängige Dateilesung innerhalb Schema 8 (40104). PR #39 aktualisierte diese App auf Schema 9 (40105) und ist seit 02.10.2026 um 18:29 Uhr am Gerät bestanden; der Screenshot zeigt eine gelesene Kontrollquelle ohne aufgezeichnete Kontrolle. [Anleitung](../docs/CONTROL_IMPORT_DEVICE_TEST.md), genaue Herkunft und Testgrenzen in WORK_HANDOFF. Nächste Testupdates auf dieser vorhandenen App aufbauen; die Ausgangs-APK nicht erneut installieren.

PR #41 aktualisiert 40105→40106 mit Schema 9→10 und ist seit 03.10.2026 um 08:06 Uhr am Gerät bestanden: alle vier Anleitungspunkte bestätigt, Screenshot mit echtem Ereignisbeleg und gelesener Störungsquelle ohne Störung. [Anleitung](../docs/EVENT_FAULT_IMPORT_DEVICE_TEST.md), genaue Herkunft und Grenzen in WORK_HANDOFF. CI zum getesteten Kopf vollständig grün (248 Tests); PR unverändert gemergt. Diese vorhandene Test-App ist die Grundlage künftiger Updates; bestandene Tests nicht wiederholen.

Der Marker-Folgeschritt ist reine Quellenprojektion/UI: keine neue Lesung, Speicherung oder Migration. Acht neue Unit- und sechs neue Compose-Tests ergänzen die vollständige CI. [Darstellung, Grenzen und optionale Prüfung ohne Leser](../docs/EVENT_FAULT_TIMELINE_CHECK.md). Der bestandene PR-41-Hardwaretest bleibt die aktuelle Grundlage.

Der Sonderbedingungs-Importschritt (PR #44) verwendet Schema 11 auf main; finale CI samt KSP-Schemaexport grün, 292 Tests. Test-ZIP 40107→40108 bereitgestellt. Der Nutzer hat den vorgesehenen Hardwaretest ausdrücklich übersprungen; PR nach erneuter Codeprüfung unverändert gemergt. Kein bestätigtes Schema-11-Update oder Leerdaten-Lesen. [Ursprüngliche Anleitung und Status](../docs/SPECIFIC_CONDITION_DEVICE_TEST.md). Der reine Marker-Folgeschritt hat Version 40109, unverändertes Schema 11 und zusätzliche Projektions-/UI-Tests. Die CI-Paarung behält die Schema-10-Ausgangs-APK 40107; aktuelle Anleitung beschreibt die Markerprüfung und die fehlende Hardwarebestätigung ausdrücklich. [Darstellung und optionale Softwareprüfung](../docs/SPECIFIC_CONDITION_TIMELINE_CHECK.md).

PR #45 ist seit 10.10.2026 nach grüner vollständiger CI (308 Tests) und bestätigtem kurzen Bestandstest zur Updateversion **40109** gemergt. Daten nach Schließen/Wiederöffnen erhalten; kein erneutes Lesen, keine separat beobachtete Ausgangsversion und keine realen Sonderbedingungsmarker bestätigt. Die ausgegebene ZIP enthält nur die Update-APK. Letzter vollständiger Leser-/Kartentest bleibt PR #41. [Abschluss, Prüfsummen und genaue Grenzen](../docs/WORK_HANDOFF.md).

Der neue GNSS-Importschritt ist noch unbestätigt; [Anbindung und CI-Status](../docs/GNSS_IMPORT_IMPLEMENTATION.md), [vorgesehener Gerätetest](../docs/GNSS_IMPORT_DEVICE_TEST.md). Die obigen früheren Updatepaar-Stände dokumentieren historische Tests.
