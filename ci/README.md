# Dauerhaft aktualisierbare Gerätetest-App

`device-test.jks` ist eine **absichtlich öffentliche, ausschließlich für Entwicklung bestimmte Test-Signieridentität** (PKCS12, Alias `androiddebugkey`, Passwort `android`). Sie ist kein Geheimnis und darf niemals als Release-/Produktionsschlüssel verwendet werden. Die Ausnahme in `.gitignore` gilt nur für diese eine Testdatei. Alle echten Keystores/Signierzugänge bleiben außerhalb des Repositorys.

Nur der debuggable Buildtyp `deviceTest` verwendet sie: Paket `de.fahrerabrechnung.app.devicetest`, sichtbarer Name **FahrerTagebuch Test**. Debug, Internal und Release behalten ihre bisherigen Signierkonfigurationen. Testschlüssel und Paketkennung nicht mit jedem Build wechseln; dadurch bleiben zukünftige Gerätetests als Updates mit vorhandener Datenbank installierbar. Dieses Testpaket ist keine Produktionsauslieferung.

Die PR-28-/PR-30-Debug-APKs und die bisher ausgelieferte PR-30-Internal-APK besitzen andere Signaturen. Ihre vorhandenen Daten werden nicht automatisch in das neue Testpaket übertragen; dafür keine bestehende App deinstallieren oder ihre Daten löschen.

`scripts/build_device_test_update.sh` erzeugt ein überprüftes Paar für den echten Update-Test:

1. Schema-8-Produktcode aus dem festgehaltenen main-Commit `c4c4a7ef9a49ec003476731518cabadaa55ebe97` (bestandener PR-37-Gerätetest), mit demselben Testpaket/Schlüssel und kleinerem Test-Versionscode **40104**.
2. Aktueller `deviceTest`-Build mit automatischem Vordergrund-Import einschließlich Kontrolldaten und Schema 9, Versionscode **40105** oder höher.

Für die Ausgangsversion werden ausschließlich Buildkonfiguration, Testlabel und Testschlüssel aus dem aktuellen Stand übernommen. Ihr Produktcode und Room-Schema stammen vollständig aus dem genannten Schema-8-Stand. Nach Installation des Updates die Ausgangsversion nicht erneut installieren. Mit der bereits eingerichteten PR-37-Test-App nur die aktuelle Update-APK verwenden; keine neue Ausgangsinstallation oder Löschung vorhandener Daten.

Das zuvor ausgelieferte PR-32-Paar enthielt Schema 5 aus `6587515d4afa5d20c6c5dc6a227eb863695b6519` mit Versionscode 40100 und Schema 6 mit 40101. Sein echtes Update wurde am 02.10.2026 bestanden; Herkunft und Prüfsummen stehen in WORK_HANDOFF.

CI prüft beide APK-Signaturen gegen das öffentliche Zertifikat dieses Keystores sowie Paketkennung und aufsteigende Versionscodes. Die ZIP enthält APKs, Prüfsummen und die Gerätetest-Anleitung. Produktionssignierung bleibt ein eigener Vorgang mit privatem Schlüssel.

Das bestandene PR-34-Paar aktualisierte Schema 6 (40101) auf Schema 7 (40102). PR #35 aktualisierte Schema 7 (40102) auf Schema 8 (40103). PR #37 ergänzte die unabhängige Dateilesung innerhalb Schema 8 (40104). PR #39 aktualisierte diese App auf Schema 9 (40105) und ist seit 02.10.2026 um 18:29 Uhr am Gerät bestanden; der Screenshot zeigt eine gelesene Kontrollquelle ohne aufgezeichnete Kontrolle. [Anleitung](../docs/CONTROL_IMPORT_DEVICE_TEST.md), genaue Herkunft und Testgrenzen in WORK_HANDOFF. Nächste Testupdates auf dieser vorhandenen App aufbauen; die Ausgangs-APK nicht erneut installieren.
