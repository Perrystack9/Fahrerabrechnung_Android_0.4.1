# Aktualisierbare FahrerTagebuch-Test-App

**DETAIL-AKTIV - nur für Build-/Geräteausgabe lesen.** Aktueller Produktstand und Geräteumfang: [WORK_STATUS.md](../WORK_STATUS.md). Hier stehen stabile Signier-/Updatevorgaben, keine Checkpoint-Chronik.

`device-test.jks` ist eine absichtlich öffentliche Test-Signieridentität (PKCS12, Alias `androiddebugkey`, Passwort `android`). Ausschließlich Entwicklung; niemals Release-/Produktionsschlüssel. Echte Keystores und Signierzugänge bleiben außerhalb des Repositorys.

Nur der debuggable Buildtyp `deviceTest` verwendet diesen Schlüssel: Paket **de.fahrerabrechnung.app.devicetest**, Name **FahrerTagebuch Test**. Paketkennung und Testschlüssel beibehalten, damit neue Tests als Updates mit vorhandener DB installierbar bleiben. Debug, Internal und Release haben ihre eigenen Signierkonfigurationen. Frühere Debug-/Internal-Daten werden nicht automatisch in das Testpaket übertragen; vorhandene Installationen/Daten nicht löschen.

Testzertifikat SHA256: `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`.

## Ausgabe und Prüfung

- `scripts/build_device_test_update.sh` bestimmt die Ausgangsbasis aus dem tatsächlichen Quellstand. Keine hier historisch aufgeführten Versionspaare als neue Baseline benutzen.
- CI prüft APK-Paketkennung, feste Signatur, steigende Versionscodes, echtes Updatepaar und Schemaexporte. Benötigte neue Produktbasis vollständig Android-/SQLCipher-/Compose-prüfen.
- Bereits eingerichtete Test-App mit der Update-APK aktualisieren. Eine Ausgangs-APK nur bei tatsächlich erforderlichem frischem Migrationstest; nach Update keine ältere Ausgangsversion erneut installieren.
- Normaler erfolgreicher PR-/main-Lauf lädt keine APK-/Reportartefakte hoch. Benötigte ZIP gezielt mit `device-test.yml`: exakter geprüfter `source_sha`, `include_baseline=false` als Standard; ZIP sieben Tage, Fehlerdiagnostik drei Tage.
- ZIP enthält benötigte APK, Anleitung aus demselben Quellstand, Prüfsummen und Buildherkunft. Eine neue Ausgabe ist keine Gerätebestätigung.

Details zu Startparametern und Gates: [CI_ARTIFACT_POLICY.md](../docs/CI_ARTIFACT_POLICY.md). Historische Update-/Gerätebelege nur bei konkretem Bedarf über den [Dokumentenindex](../docs/DOCUMENTATION_INDEX.md).
