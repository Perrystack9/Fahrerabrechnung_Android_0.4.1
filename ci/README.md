# Dauerhaft aktualisierbare Gerätetest-App

`device-test.jks` ist eine **absichtlich öffentliche, ausschließlich für Entwicklung bestimmte Test-Signieridentität** (PKCS12, Alias `androiddebugkey`, Passwort `android`). Sie ist kein Geheimnis und darf niemals als Release-/Produktionsschlüssel verwendet werden. Die Ausnahme in `.gitignore` gilt nur für diese eine Testdatei. Alle echten Keystores/Signierzugänge bleiben außerhalb des Repositorys.

Nur der debuggable Buildtyp `deviceTest` verwendet sie: Paket `de.fahrerabrechnung.app.devicetest`, sichtbarer Name **FahrerTagebuch Test**. Debug, Internal und Release behalten ihre bisherigen Signierkonfigurationen. Testschlüssel und Paketkennung nicht mit jedem Build wechseln; dadurch bleiben zukünftige Gerätetests als Updates mit vorhandener Datenbank installierbar. Dieses Testpaket ist keine Produktionsauslieferung.

Die PR-28-/PR-30-Debug-APKs und die bisher ausgelieferte PR-30-Internal-APK besitzen andere Signaturen. Ihre vorhandenen Daten werden nicht automatisch in das neue Testpaket übertragen; dafür keine bestehende App deinstallieren oder ihre Daten löschen.

`scripts/build_device_test_update.sh` erzeugt ein überprüftes Paar für den echten Update-Test:

1. Schema-8-Produktcode aus dem festgehaltenen main-Commit `909dd6652d029cab842ee986c4756fac0fd2ff93` (bestandener PR-35-Gerätetest), mit demselben Testpaket/Schlüssel und kleinerem Test-Versionscode **40103**.
2. Aktueller `deviceTest`-Build mit automatischem Vordergrund-Import und Schema 8, Versionscode **40104** oder höher.

Für die Ausgangsversion werden ausschließlich Buildkonfiguration, Testlabel und Testschlüssel aus dem aktuellen Stand übernommen. Ihr Produktcode und Room-Schema stammen vollständig aus dem genannten Schema-8-Stand. Nach Installation des Updates die Ausgangsversion nicht erneut installieren. Mit der bereits eingerichteten PR-35-Test-App nur die aktuelle Update-APK verwenden; keine neue Ausgangsinstallation oder Löschung vorhandener Daten.

Das zuvor ausgelieferte PR-32-Paar enthielt Schema 5 aus `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit Versionscode 40100 und Schema 6 mit 40101. Sein echtes Update wurde am 02.10.2026 bestanden; Herkunft und Prüfsummen stehen in WORK_HANDOFF.

CI prüft beide APK-Signaturen gegen das öffentliche Zertifikat dieses Keystores sowie Paketkennung und aufsteigende Versionscodes. Die ZIP enthält APKs, Prüfsummen und die Gerätetest-Anleitung. Produktionssignierung bleibt ein eigener Vorgang mit privatem Schlüssel.

Das bestandene PR-34-Paar aktualisierte Schema 6 (40101) auf Schema 7 (40102). PR #35 aktualisierte Schema 7 (40102) auf Schema 8 (40103). Die aktuelle Korrektur der unabhängigen Dateilesung verwendet Schema 8 (40104) auf der vorhandenen PR-35-Test-App, ohne neue Migration; [Anleitung](../docs/INDEPENDENT_READ_DEVICE_TEST.md).
