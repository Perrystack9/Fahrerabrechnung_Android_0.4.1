# Dauerhaft aktualisierbare Gerätetest-App

`device-test.jks` ist eine **absichtlich öffentliche, ausschließlich für Entwicklung bestimmte Test-Signieridentität** (PKCS12, Alias `androiddebugkey`, Passwort `android`). Sie ist kein Geheimnis und darf niemals als Release-/Produktionsschlüssel verwendet werden. Die Ausnahme in `.gitignore` gilt nur für diese eine Testdatei. Alle echten Keystores/Signierzugänge bleiben außerhalb des Repositorys.

Nur der debuggable Buildtyp `deviceTest` verwendet sie: Paket `de.fahrerabrechnung.app.devicetest`, sichtbarer Name **FahrerTagebuch Test**. Debug, Internal und Release behalten ihre bisherigen Signierkonfigurationen. Testschlüssel und Paketkennung nicht mit jedem Build wechseln; dadurch bleiben zukünftige Gerätetests als Updates mit vorhandener Datenbank installierbar. Dieses Testpaket ist keine Produktionsauslieferung.

Die PR-28-/PR-30-Debug-APKs und die bisher ausgelieferte PR-30-Internal-APK besitzen andere Signaturen. Ihre vorhandenen Daten werden nicht automatisch in das neue Testpaket übertragen; dafür keine bestehende App deinstallieren oder ihre Daten löschen.

`scripts/build_device_test_update.sh` erzeugt ein überprüftes Paar für den echten Update-Test:

1. Schema-10-Produktcode aus dem festgehaltenen main-Commit `e957834d2cfa5e1dcd4e54bb458a1e078f2a57f5` (bestandener PR-41-Gerätetest), mit demselben Testpaket/Schlüssel und kleinerem Test-Versionscode **40106**.
2. Aktueller `deviceTest`-Build mit Ereignis-/Störungsmarkern aus den gespeicherten Quellen, unverändert Schema 10, Versionscode **40107** oder höher.

Für die Ausgangsversion werden ausschließlich Buildkonfiguration, Testlabel und Testschlüssel aus dem aktuellen Stand übernommen. Ihr Produktcode und Room-Schema stammen vollständig aus dem genannten Schema-10-Stand. Nach Installation des Updates die Ausgangsversion nicht erneut installieren. Mit der bereits eingerichteten PR-41-Test-App nur die aktuelle Update-APK verwenden; keine neue Ausgangsinstallation oder Löschung vorhandener Daten.

Das zuvor ausgelieferte PR-32-Paar enthielt Schema 5 aus `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit Versionscode 40100 und Schema 6 mit 40101. Sein echtes Update wurde am 02.10.2026 bestanden; Herkunft und Prüfsummen stehen in WORK_HANDOFF.

CI prüft beide APK-Signaturen gegen das öffentliche Zertifikat dieses Keystores sowie Paketkennung und aufsteigende Versionscodes. Die ZIP enthält APKs, Prüfsummen und die Gerätetest-Anleitung. Produktionssignierung bleibt ein eigener Vorgang mit privatem Schlüssel.

Das bestandene PR-34-Paar aktualisierte Schema 6 (40101) auf Schema 7 (40102). PR #35 aktualisierte Schema 7 (40102) auf Schema 8 (40103). PR #37 ergänzte die unabhängige Dateilesung innerhalb Schema 8 (40104). PR #39 aktualisierte diese App auf Schema 9 (40105) und ist seit 02.10.2026 um 18:29 Uhr am Gerät bestanden; der Screenshot zeigt eine gelesene Kontrollquelle ohne aufgezeichnete Kontrolle. [Anleitung](../docs/CONTROL_IMPORT_DEVICE_TEST.md), genaue Herkunft und Testgrenzen in WORK_HANDOFF. Nächste Testupdates auf dieser vorhandenen App aufbauen; die Ausgangs-APK nicht erneut installieren.

PR #41 aktualisiert 40105→40106 mit Schema 9→10 und ist seit 03.10.2026 um 08:06 Uhr am Gerät bestanden: alle vier Anleitungspunkte bestätigt, Screenshot mit echtem Ereignisbeleg und gelesener Störungsquelle ohne Störung. [Anleitung](../docs/EVENT_FAULT_IMPORT_DEVICE_TEST.md), genaue Herkunft und Grenzen in WORK_HANDOFF. CI zum getesteten Kopf vollständig grün (248 Tests); PR unverändert gemergt. Diese vorhandene Test-App ist die Grundlage künftiger Updates; bestandene Tests nicht wiederholen.

Der Marker-Folgeschritt ist reine Quellenprojektion/UI: keine neue Lesung, Speicherung oder Migration. Acht neue Unit- und sechs neue Compose-Tests ergänzen die vollständige CI. [Darstellung, Grenzen und optionale Prüfung ohne Leser](../docs/EVENT_FAULT_TIMELINE_CHECK.md). Der bestandene PR-41-Hardwaretest bleibt die aktuelle Grundlage.
