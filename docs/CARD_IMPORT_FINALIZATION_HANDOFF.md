Aktueller Auslieferungsstand12.10.2026: Test-ZIP PR62/40121 aus unverändertem Produktkopfefbf49cdf92bd5e5ddb2e042b3e14e9372169756 bereitgestellt; vollständige CI38172596871 und Paketlauf38201601458 erfolgreich. APK-v2-Signatur/Inhaltsdigest, Zertifikat, Paketkennung, Version und alle Prüfsummen lokal bestanden. Anleitung/BUILD_INFO enthalten, Update über bestätigte40120, keine Neuinstallation.40121 noch hardwareunbestätigt; PR62 offener Entwurf. Tobi hat nach erfolgreicher ausdrücklicher Bestätigung den Abschluss von PR62 und selbstständige weitere Kartenimportarbeit beauftragt; automatische Fortsetzung nach PR62-Merge eingerichtet, mit zusätzlicher Prüfung der dokumentierten40121-Freigabe. [Verbindlicher Auslieferungs-/Fortsetzungscheckpoint](PR62_DEVICE_DELIVERY_40121.md). Keine neue Freigabe späterer Änderungen; kein Browser/Unteragenten/Bereinigung, Budget2USD unverändert. Die folgenden Angaben dokumentieren frühere Stände.

---

# Kartenimport – Fortsetzung und Übergabe am 11.10.2026

## Verbindlicher bestätigter Stand

Repository `Perrystack9/Fahrerabrechnung_Android_0.4.1`. **Gerätetest40120 ist ausdrücklich bestanden.** Tobi bestätigte am11.10.2026,22:22 Europe/Berlin: Update installiert und alte Daten vorhanden; neue Kartenlesung erfolgreich; Zertifikate, Kartenangaben und Anwendungsverzeichnis sichtbar; nach Kartenentnahme/App-Neustart weiterhin vorhanden; alter Import unverändert. Keine weiteren Einzelmesswerte, Fremdkarten-/Fehlerfälle oder Kryptovertrauensnachweise daraus ableiten. [Genaue Gerätebestätigung](DEVICE_CONFIRMATION_40120.md).

PR60 wurde am unveränderten Quellkopf `85d0614235e4d9b5384d96d9ee124aab1f9fbc3a` gemergt als `9e7670f05b7c35a585244aeca539be9d0fe7c6fb`. Main-Tree `78155b21cb76f9c27231360bd78dad0aa5472a22` bytegleich zum geprüften und getesteten40120-Stand. Vorheriger Main `1b592d627d5d18db9fc909f5c8cf2c796fd8578b` ist der erste Elterncommit, PR60 der zweite. PR59-Kopf `c535c01a838fb9f565b49b7a9b50030d06e77bf7` ist enthalten; GitHub hat PR59 automatisch ebenfalls als gemergt abgeschlossen. Vollständige CI38119878793/38145473572 und Paketlauf38148088141 erneut erfolgreich geprüft. Auch die nach dem Merge automatisch gestartete vollständige Main-CI38172090773 am Mergekopf9e7670f ist in allen vier Jobs erfolgreich (114566626588,114566666934,114568345231,114573403361). Der anschließende Docs-Push38172378527 am dokumentierten Main f2984027 ist erfolgreich; Android dort planmäßig übersprungen.

Der reine Dokumentationsabschluss [PR63](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/63) ist nach erfolgreicher Docs-CI38172276322 gemergt als `f2984027643a27ea55dc44c10fae9cdf6d115938`, Tree `7b9bd9e96e2b123748514e508f9ddbbd18d76028`. Sechs Markdown-Dateien, kein Produkt-/Schema-/Versionswechsel; Android-Build und Instrumentierung dabei korrekt übersprungen. Dauerhafter Geräte-Checkpoint und Einstiegshinweise auf Main vorhanden.

Bereitgestelltes Paket bleibt `FahrerTagebuch-PR60-Test-Update.zip`, Version40120/Schema18. ZIP SHA256 `77a66af59fb994892e852084f44bdedd857ba981aa4beef24c65765352e2b23a`; APK SHA256 `e287466c84f4bc38ecc1dc88971920eab30f11ff761cf8a9d9d179b10be81d82`. Feste Testkennung `de.fahrerabrechnung.app.devicetest`, Zertifikat-SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`. Paket und Originalquellkopf nicht ersetzt. **Letzte Gerätebasis ist jetzt PR60/40120, nicht mehr PR58/40117.**

Tobi hat selbstständige Weiterarbeit mit dem verbleibenden Kontingent erneut beauftragt. Kein neuer fester Stundenauftrag daraus abgeleitet. Keine Browsernutzung, Unteragenten, Bereinigung oder Löschung. GitHub-Actions-Budget unverändert2USD; keine Budgeteinstellungen geändert.

## Gesicherter unbestätigter Folgeentwurf

[PR62](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/pull/62), offen und Entwurf, Branch `feature/card-import-finalization`, Base `main`. **PR62 und spätere Änderungen gelten durch den Test40120 ausdrücklich nicht als hardwarebestätigt. Kein Merge/Auto-Merge vor eigener ausdrücklicher Gerätefreigabe.** Entwurfsversion40121, Schema18 unverändert.

Aktueller integrierter Produktkopf **`efbf49cdf92bd5e5ddb2e042b3e14e9372169756`**, Tree **`11e79e492b381f9d515bee796ffea30da3099592`**. Eltern: Kommunikationskopf `93d1eca6b14c2b0bb0edc23d24648f49e7d7b081` und dokumentierter Main `f2984027643a27ea55dc44c10fae9cdf6d115938`. Der Main ist vollständig enthalten; PR62 ist ohne Konflikt mergebar und bleibt trotzdem bis eigener Hardwarefreigabe Entwurf. Die vollständige [CI38172596871](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38172596871) am integrierten Kopf ist in allen vier Jobs erfolgreich. Diese neue CI belegt das aktualisierte40120→40121-Updatepaar. Der vorherige Kommunikationskopf93d1eca hat alle vier Jobs38165084869 bestanden.

Die lokale Arbeitskopie stammt aus einem vorhandenen PR60-Snapshot; lokale Commit-IDs und GitHub-Commit-IDs unterscheiden sich wegen der Snapshot-Historie. Alle hochgeladenen Blobs und vollständigen Trees wurden bytegenau abgeglichen. Für Remote-Fortsetzungen den bezeichneten **GitHub-Kopf** als Parent verwenden und vor jedem Ref-Update erneut prüfen. Der Integrationscommit hat zusätzlich den dokumentierten Main als zweiten Parent. Keine lokale Snapshot-Historie auf den Remote-Branch drücken.

## Erledigt im Folgeentwurf

- Bedingte MF-Dateien ATR/INFO `2F01` und Extended_Length `0006`: unabhängige vollständige begrenzte SELECT/READ-Lesung vor AID, eindeutige ATR-Ankündigung, Gen2/Gen2V2; keine neuen Transportbefehle.
- Exakte verschlüsselte Speicherung je eigener physischer Karte und ausgewählter ImportSession in bestehenden Schema18-Tabellen; Deduplizierung, Request-Vereinigung, Art-/Karten-FKs, Rückrollen und kein historischer Fallback.
- Kommunikationsangaben in Fahrerkarten-Details, normaler Importübersicht und Verlauf; gespeicherte leere und fehlende Quellen bleiben unterscheidbar.
- Koordinator friert alle neuen Quellenmaps vor wartender Kartenprüfung ein; ausdrückliche Bestätigung verwendet weiter frische Lesung.
- 38 neue Testmethoden:20 JVM, elf SQLCipher, sieben Compose/AppRoot. Alte Tests nicht entfernt oder gelockert. Amtlicher MF-/Fahrerkarten-Abgleich und genaue Grenzen in [CARD_COMMUNICATION_IMPORT.md](CARD_COMMUNICATION_IMPORT.md).
- Nach bestätigtem40120 aktualisierte Build-Basis aus dem bytegleichen PR60-Merge; gleiches Testpaket und Testzertifikat, steigendes Updatepaar40120→40121. Kein erneuter manueller Test von40120 nötig.

## Prüfung des integrierten Produktkopfs

Die neue vollständige [CI38172596871](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38172596871) gehört exakt zu efbf49cdf92bd5e5ddb2e042b3e14e9372169756 und ist vollständig erfolgreich:

| Job | ID | Ergebnis |
|---|---|---|
| Project boundaries and CI scope | `114568150966` | erfolgreich |
| Build, test, lint and APK verification | `114568180648` | erfolgreich |
| Room SQLCipher and Compose instrumentation | `114569924279` | erfolgreich |
| Android verification | `114574672562` | erfolgreich |

Das Build-Log bestätigt alle sieben Pflichtkommandos mit Exit0 und das feste Testzertifikat/Paket mit steigendem Updatepaar. Das bytegenau geprüfte Skript verwendet ausdrücklich die bestätigte Basis40120 aus9e7670f. Beide connectedDebugAndroidTest-Tasks und BUILD SUCCESSFUL sind im Emulatorlog belegt; danach generierte Room-Exporte unverändert. PR62-Kopf/Base/Draft-Zustand nach Abschluss erneut geprüft. Keine neue Hardwarefreigabe daraus ableiten.

## Bereits belegte Prüfung und aktuelle Integration

Lokale Projektgrenzen, zehn CI-Scope-/Schema-Guard-Regressionsprüfungen, Shellsyntax, eindeutige neue Testmethoden, Navigations-/Reset-Abgleich und Diffprüfung bestanden. Datenbank, Entities, Migrationen und Schema18-Exporte unverändert; SHA256 `70340ea11ae74941615f56e0297b5c711fb7d3acf44a63bc981daccccfb8ac3b`. Keine lokale Android-/Gradle-Toolchain verfügbar; neue Android-Nachweise aus CI, keine lokale APK-Signaturprüfung40121 behaupten.

Vorheriger vollständiger [CI-Lauf38165084869](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38165084869) gehört exakt zum oben bezeichneten93d1eca-Kopf:

| Job | ID | Ergebnis |
|---|---|---|
| Project boundaries and CI scope | `114545865219` | erfolgreich |
| Build, test, lint and APK verification | `114545906490` | erfolgreich |
| Room SQLCipher and Compose instrumentation | `114548025669` | erfolgreich |
| Android verification | `114554241414` | erfolgreich |

Build-Log bestätigt sieben Pflichtkommandos mit Exit0:help,build --dry-run,test,lint,assembleDebug,assembleInternal,:app:assembleDeviceTest; gültiges Testzertifikat/Paket mit steigenden Versionen und unveränderte generierte Room-Exporte. Emulatorlauf: beide connectedDebugAndroidTest-Tasks und BUILD SUCCESSFUL, danach Schemaexport geprüft. Dieser damalige Lauf verwendete noch die ältere bestätigte Basis40117; er ist kein Nachweis für das anschließend aktualisierte40120-Updatepaar.

Vorläufe38161429985/38161802833 wurden durch Folgecommits abgelöst, nicht als Gesamterfolg gewertet. Sechs synthetische Reader-Tests hatten zunächst den Pflichtparameter der Identifikationsfixture nicht übergeben; explizite gültige Fixture ergänzt. Die ATR-Maske wurde im Primärabgleich für dekodierte CCID-Zeichen auf0x40 korrigiert, beide Konventionen/Statuslayouts vollständig getestet.

Lauf38161893490 bestand Build/Unit/Lint/APK/Schema und alle136 App-/Compose-Tests; ein neuer SQLCipher-Test brach vor dem Import wegen zwei über16Byte langer synthetischer Kartennummern ab. 218/219 Datenbanktests erfolgreich. Nur diese Fixture-Werte korrigiert, keine Ablehnungsprüfung gelockert; vollständiger Folgelauf38165084869 danach erfolgreich. Diagnoseartifact11711014776, ZIP-SHA256 `6724d82a5a87adbd8c2363d14bd8a9f2f694000c4e31d3c063d8448371002d55`.

Keine neue Test-ZIP ausgegeben, kein separater Paketworkflow ausgelöst. Eine spätere Ausgabe braucht den genau bezeichneten vollständig geprüften integrierten Produktkopf; Geräteanleitung in [CARD_COMMUNICATION_DEVICE_TEST.md](CARD_COMMUNICATION_DEVICE_TEST.md). Keine Gerätefreigabe40121 vorweggenommen.

## Präzise verbleibende Grenzen

| Thema | Aktueller Stand | Nächster konkreter Schritt |
|---|---|---|
| Geräte-/Merge-Freigabe |40120 bestätigt;40121 unbestätigt | Eigene ausdrückliche Gerätebestätigung mit genauer Version für PR62/spätere Änderungen |
| MF ATR/INFO | Originalbytes und äußere7F66-Struktur vorhanden | Vollständigen ISO7816-4:2013-Inhaltsdecoder anhand überprüfbaren Primärformats ergänzen, keine ungesicherten Pufferannahmen |
| Unbekannte ATR-Historie | Optionaldateien nicht versucht | Keine Vollständigkeitsbehauptung; ggf. gesonderte belegte ATR-Formatklärung |
| DIR auf reinen Gen1-Karten | Keine fehlende Standardquelle: Annex IC§4.5.2.2 sieht sie nur auf Karten der zweiten Generation vor | Keine erfundene Gen1-DIR-Lücke; Herstellerabweichungen nur mit belegter eigener Formatspezifikation |
| Beide Anwendungen auf Gen2-Karte | Ausgewählte erkannte Anwendung importiert | Generationsgetrennter Import beider DFs und eigener Quellen-/Sitzungsbezug |
| VU_Configuration0540 | Nicht gelesen, SC5-Zugriff | Autorisiertes Authentisierungs-/Secure-Messaging-Konzept und geprüfte Zugriffsschritte |
| Signierter Standarddownload/DDD | Kein vollständiger Standarddownload | Amtliches Download-/Signaturprotokoll, Originalsignaturen und interoperablen Export separat implementieren |
| Zertifikats-/Signaturvertrauen | Nur strukturell dekodiert | Überprüfbare ERCA-/MSCA-Kette und RSA-/EC-Verifikation mit eigenen Fehlerzuständen |

Die ungesichert lesbaren Quellenarten der **ausgewählten** Anwendung:Gen1 16,Gen2 21 beziehungsweise23 mit beiden angekündigten Kommunikationsquellen,Gen2V2 27 beziehungsweise29. Das zählt Quellenarten, keine Zeitabdeckung oder Datensätze. Es ist keine Behauptung, alle Anwendungen, Signaturen und geschützten Kartenbereiche zu importieren. Fahrzeug-/Spesen-/Abrechnungslogik bleibt außerhalb dieses Schritts.

[Primärabgleich Annex IC§4.5.2.2–4.5.2.4 und Appendix2 TCS140–147](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02016R0799-20160526):DIR und beide Kommunikationsdateien nur auf Karten der zweiten Generation. Die zweite Anwendung **auf derselben Gen2-Karte** ist davon getrennt ein echter offener Importpunkt.

## Nächster abgegrenzter Importschritt: beide Anwendungen

Dies ist eine technische Voranalyse, keine bereits umgesetzte Funktion. Der vorhandene physische Kartenschlüssel in DriverCardImportRepository.key enthält Ausstellerstaat, Nummer und Ausstellungs-/Gültigkeitszeiten, keine Anwendungsgeneration. Diesen stabilen Schlüssel nicht durch zwei scheinbar verschiedene physische Karten ersetzen.

Der aktuelle Reader wählt Gen2 bevorzugt und Gen1 nur als Fallback; CardReadResult und SavedDriverImport tragen jeweils einen einzigen Generation-/Quellensatz. Die bestehenden Quellenverweise erlauben je Sitzung und Art einen konkreten Beleg. Die zweite Anwendung darf deshalb nicht einfach in dieselben Maps eingefügt oder über vorhandene Generationsfelder gelegt werden.

Vor dem ersten Produktpatch ein eigenes Anwendungs-/Sitzungsmodell festlegen: Anwendung/AID und erkannte Generation explizit am jeweiligen Importzweig führen; Identifikation und Kapazitäten je DF unabhängig lesen und validieren; beide DFs an dieselbe bereits bestätigte physische Karte binden. Kartenweite MF-Dateien und anwendungsspezifische Original-/Zertifikats-/Fachdaten bewusst unterscheiden. Ältere Ein-Anwendungs-Sitzungen behalten ihr Modell und bekommen keinen Backfill. Eine ggf. nötige additive Migration muss mit echtem KSP-Export und Update über40120 geprüft werden.

Mindestens prüfen: beide DFs erfolgreich; nur einer erfolgreich bei unabhängigen Dateifehlern; unterschiedliche Original-/Zertifikatsdateien derselben Art; keine Überschreibung/Quellenvermischung; dieselbe physische Karten-ID; Abbruch zwischen DFs; widersprüchliche zweite Fahreridentität; fremde/unbestätigte Karte ohne Persistenz; Snapshot während Kartenprüfung und frische Bestätigungslesung; Deduplizierung mit unveränderten historischen Requests; exakte Wiederöffnung beider Anwendungsbelege nach Neustart und Verlaufswechsel. Belegte Tagesdaten verschiedener Anwendungen nicht automatisch zu doppelter Zeit-/Abrechnungsabdeckung addieren.

## Abschließender Dokumentationscheckpoint

Der abschließende Checkpoint ist auf `docs/card-import-finalization-checkpoint-20261011` gesichert, ausgehend vom exakt geprüften integrierten PR62-Produktkopf. Änderungen dort nur an docs/ und WORK_STATUS.md; Quellcode, Tests, Buildskripte, Schema und Version bleiben bytegleich. Manifest über git-ls-tree-Zeilen in Git-Sortierung mit LF, ohne docs/,README.md,WORK_STATUS.md:292 Dateien, SHA256 `f5750896a878e8c84749c7d6251145822b33b8fcf35b87c65f6516a4502d4fe0`.

PR62 bleibt am genau CI-geprüften Produktkopf stehen; seine Beschreibung verweist auf den vollständigen Checkpoint. Dieser Checkpoint enthält als Vorfahren auch den noch hardwareunbestätigten PR62-Code: **nicht als angeblich reines Docs-Update nach Main mergen**. Bei späterer ausdrücklicher40121-Freigabe den unveränderten freigegebenen Produktkopf behandeln und Dokumentation anschließend separat übernehmen. Bei weiteren Produktänderungen den GitHub-Checkpoint als Ausgang verwenden und erneut vollständig prüfen.

## Weiterarbeiten

Zuerst diesen Checkpoint und aktuellen GitHub-Kopf/CI-Stand prüfen.40120 ist bestätigt und abgeschlossen, nicht erneut als wartenden Gerätetest behandeln. PR62 bleibt ohne eigene Freigabe ungemergt. Bei Fortsetzung an klar abgegrenzten offenen Importpunkten arbeiten; [Produktregeln](CURRENT_RULES.md), [Abdeckung](CARD_SOURCE_COVERAGE.md) und spätere Geräteanleitung beachten. Keine vorhandenen Pakete ersetzen oder spätere Änderungen als hardwarebestätigt markieren.
