# M1 – Abnahmematrix und Prüfprotokoll

Stand15.10.2026. Zuordnung zum [Implementierungsbericht](VIOLATION_ENGINE_M1_IMPLEMENTATION.md) und dessen unverändert gepinntem M0-/PR88-Vertrag. Nur Eingangsdaten-/Kontextabnahme; keine M2–M7-Freigabe.

## Reproduzierbare MI-Zuordnung

Testdateien: Domain **AI**=`ActivityInputAcceptanceTest`, **SC**=`ScopeContextAcceptanceTest`, **COV**=`InputCoverageTest`; Android **DB**=`ViolationInputInstrumentedTest`, **CTX**=`ViolationContextInstrumentedTest`, **MIG**=`M1MigrationInstrumentedTest`. Alle neuen Androidtests verwenden synthetische Daten und die echte verschlüsselte Room-Fabrik; kein Mock-SQLCipher und keine echten APDUs.

| Vektor | Implementierte Prüfung / unabhängig festgelegtes Soll |
|---|---|
| MI01 | AI/DB `mi01`:32 s/c/p/aa-Kombinationen; DB führt echte Rohwörter durch bestehenden Parser, Import, Normalisierung und Adapter. Karte/c gemeinsam; keine Crew aus manuellem c1. |
| MI02 | AI/DB `mi02`: Entnahme nach WORK08:00, nächster10:00-Punkt:120min Unknown für alle vier aa; Originalaktivität erhalten. |
| MI03 | AI/DB `mi03`: alter Unknown und neuer echter manueller REST-Nachtrag:120min REST, kein positiver Quellenkonflikt, alle Ursprünge, alte Bytes unverändert; manuelle Herkunft vorhanden. Sichtbarer Badge erst M5. |
| MI04 | AI `mi04`: p1/c1/DRIVING06:00–08:00 trägt120min Anomalie/Unknown; keine gültige Fahrt und kein Crewprivileg; Rohkombination erhalten. DB-mi01 enthält dieses Rohmuster ebenfalls. |
| MI05 | AI `mi05` plus DB `mi05_mi06`:08:00 unterschiedliches Kartenstatusmaterial, beide Reihenfolgen; keine positive Zwischendauer/kein Indexgewinner,120min Unknown. Exakte normalisierte Indizes und Quellenauswahl. |
| MI06 | AI `mi06`: zwei gleiche WORK08:00-Punkte bis10:00 ergeben120min, beide Indizes, kein Scheinkonflikt. |
| MI07 | AI `mi07`:00:00:01-Record wird nicht gerundet; seine Anomalie bleibt erhalten. Andere gültige Quelle/Recordteile bleiben lokal nutzbar. |
| MI08 | AI `mi08`: rückläufige Recorddaten werden nicht zu einer erfundenen verlässlichen Chronologie sortiert. |
| MI09 | AI `mi09`/DB `mi09`: letzter WORK10:00 bleibt offen trotz Importzeit, Metadata und echtem0507-Sitzungsbeginn14:00; kein Work-Nachlauf bis24:00. |
| MI10 | AI/DB `mi10`: vollständige gleiche Quelle22:00→00:00→09:00:660min REST, beide Record-/00:00-Endbezüge; ab09:00 offen. |
| MI11 | AI `mi11`: kein Folgetag, Datumslücke, unvollständiger Kopf, fremde unbestätigte Quelle oder mehrdeutiger00:00-Punkt erlauben keine historische Brücke. |
| MI12 | AI `mi12`: derselbe Mitternachts-Endbeleg schließt660min Unknown; daraus werden0min REST. |
| MI13 | AI/DB `mi13`: wirklicher0000-Wechsel versus vollständig genulltes Original: Punkt versus gültig leer mit unterschiedlicher Faktenrevision; beide ohne erfundenen Tagesrest. Fehlende Aktivitätsprojektions-ID bleibt null. |
| MI14 | AI/DB `mi14` und DB `mi05_mi06`: Ringplatz neutral, andere Karte/c/p/Reihenfolge nicht neutral; falsche Karten-/Originalabfrage leer; zwei Anwendungen/replay/zusätzliche kompatible Herkunft ohne doppelte Minuten; verschlüsselte Wiederöffnung. |
| MI15 | SC `mi15`: >3500kg strikt, inklusive konkret angegebener Gesamtmasse.3500kg nicht pauschal reguliert/freigestellt; fehlende entscheidende Fakten Unknown. |
| MI16 | SC `mi16`:30.06./02.07.2026, internationale/Kabotage-2500/2501/3500kg, national, konkrete Art.3ha-Angaben; kein Rückdatieren. Unbelegte Rechtszeitkonvention bleibt Unknown. |
| MI17 | SC `mi17`, DB-Punkteprüfung: tatsächlicher0530-PASSENGERS-Punkt, fehlender Dienstbedarf, Antwortwiederverwendung; gesamter Linienweg50000/50001m. Keine kurze Einzelfahrt als Linienausnahme. |
| MI18 | SC `mi18`, DB-Punkteprüfung: Karte/OOS allein beweisen nichts; geprüfter synthetischer Teilmanifestpunkt wirkt nur im Artikel-/Gebiets-/Zeitbereich, AETR ganze Reise unsupported. Keine tatsächliche Manifestfreigabe aus Tests ableiten. |
| MI19 | SC `mi19`: separate Other-work-Einordnung bei nicht regulierter kommerzieller Fahrt/Überführung; unbekannter Zweck bleibt unbestimmt, keine Rohfahrtkorrektur oder Ruheannahme. |
| MI20 | AI `mi20`, AI/DB-mi01: Crew nur bei belegtem gestecktem c1-Abschnitt; manuelles c1 und Lücke erben kein Crewprivileg. Keine Zweitkarten-/Anwesenheitsfrage.30h-/Beifahrerqualifikation späteres Regelpaket. |
| MI21 | SC `mi21`, DB-Punkteprüfung: Fährenbedarf nur für konkret angefragtes Ruhesubjekt; Schlaf-/Fahrplandaten, unveränderte Antwort wiederverwenden, keine allgemeine Unterkunftsfrage. Ausnahmeanrechnung nicht Teil M1. |
| MI22 | SC `mi22`: Arbeitgeber-/Wohnsitzland und Beginnland jeder konkreten Ruhe getrennt zeitlich/subjektbezogen; Antwort für Ruhe1 heilt nicht die fehlende Angabe für Ruhe2. |
| MI23 | SC `mi23`, CTX, DB `mi23`: UTC/Berlin/NewYork unveränderte Revision; begrenzter echter Kontextwechsel wirkt nur in seiner Zeitspanne; identische Antwort oder geänderte Abgabeuhr keine neue Kontextversion; CAS/Historie/Reopen. |
| MI24 | AI/DB `mi24`: ungebundenes Archiv ausgeschlossen; gebundener Legacy-Rawfallback ohne Backfill; neuer bekannter Widerspruch bleibt Konflikt, auch gegenüber bekannten Alternativen einer gleichminütigen Gruppe; kein Artikel12-Anerkennungs-/Schuldmodell. Spätere Ergebnis-/PDF-Behandlung nicht als M1 implementiert ausgeben. |

Zusätzlich: COV prüft lokale Fakten vor/nach Lücken, fehlende Scope-/Punktvoraussetzungen und unmittelbar anschließende Kontextgültigkeiten. AI prüft Herkunft eines kompatibel geliehenen Endes, widersprechenden offenen Punkt ohne erfundene positive Dauer, Crew-/Slotkonflikte getrennt von der Tätigkeit, defensive Kopien, Cancellation und echte UTC-Dauer über Winterzeit. DB prüft fehlgeschlagene zweite Anwendung, beschädigte konkrete Quelle ohne jüngeren Ersatz, exakte Signatur-/Originalbelege ohne neue Mathematik/Trustbehauptung, stale Veröffentlichung und Revisionswechsel. CTX prüft konkurrierende Schreiber, Rollback/Cancellation, Fremdprofil und beschädigte Kontextpayloads. MIG prüft die vollständige Kette1→23,22→23 sowie echte SQLCipher18→23/22→23 einschließlich zeilenweiser Digestgleichheit aller alten Tabellen und Reopen.

## Tatsächliche lokale Ausführung

Die vollständige lokale Gradle-Unit-Suite wurde ausgeführt:559 Tests,0 Fehler (domain40, core1, data513, feature5; keine App-/Report-Unitmethoden). Darin34 neue Domainmethoden. Vorher bereits ein echter Gradle-Lauf mit Room-KSP/Compilerexport23 erfolgreich; separat unveränderte Primärmodell-Proben mit JUnit, danach Gradle als maßgeblicher lokaler Testlauf. Androidtest-Kompilierung erfolgreich; deren Ausführung ist das separate API36-CI-Gate.

Projektgrenzenprüfung und alle10 bestehenden Skriptregressionstests bestanden; `git diff --check` ohne Fehler. Die Originalparser,40128-Kryptografie-Engine, Keystore, CI-Workflows/Skripte und22 alte Exportdateien werden nicht geändert. Frühere lokale Gradle-Ergebnisdateien aus einem überlappenden lokalen Prüfanlauf wurden gesichert; der anschließende serielle Lauf ohne Buildcache bestand. Keine Testfilter, Quarantäne oder CI-Änderung als Umgehung.

Historische Android-Migrationsfixtures sind für die additive M1-Tabelle nachgeführt:19 Rückstufungsaufbauten entfernen deren leere Relation, vier Zielversionsprüfungen verwenden die aktuelle Anwendungsversion des Schemas. Alle bisherigen Inhalts-, Quellen-, Request-, Signatur- und No-backfill-Assertions bleiben erhalten. Der zusätzliche Androidtest-Kompilierungslauf nach dieser Anpassung bestand. Die tatsächliche vollständige Instrumentierung bleibt das CI-Gate.

Vollständiger CI-Vorlauf [38702476211](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38702476211), Versuch1, Kopf `68bee34b71ccd6e1489e1ae5e96ba43bf8781cb9`: Build-/Unit-/Lint-/APK-/Updatepaar-/Schemagates bestanden. API36 führte151 App-/Compose- und297 Datentests ohne übersprungene Fälle aus. Alle App-Tests und292 Datentests bestanden. Vier Fehler waren die veralteten Zielversionsassertions22 statt23; ein weiterer lag ausschließlich im neuen verschlüsselten Alt-DDL-Testaufbau (`JSONException: No value for indices`). Compilerexporte lassen `indices` bei Entities ohne deklarierte Indizes weg. Der Fixtureaufbau akzeptiert dieses belegte Fehlen; ein vorhandener fehlerhafter Wert wird weiterhin streng verworfen. Alle23 übrigen neuen Androidmethoden bestanden bereits. Das Fehlerartefakt wurde per SHA256 `9190c405d1b3c6f98c4fd3a25f45544f8dea3e30496eb9c57718b5bde73aeef1` geprüft und die beiden vollständigen JUnit-XML-Berichte ausgewertet. Die korrigierte Gesamtsuite muss am endgültigen Kopf erneut vollständig bestehen; dieser Vorlauf ist keine grüne Gesamtabnahme.

Die gesamten sieben vorgeschriebenen Gradle-Aufrufe und zusätzlichen Androidtest-Builds werden protokolliert. Ihr endgültiges Ergebnis, exakte CI-Läufe, Versuch und Quellkopf stehen im zugehörigen M1-PR-Abnahmeprotokoll/Abschlussbericht. Eine reine Kompilierung ist kein ausgeführter Geräte-/Migrationstest. Keine alte40128-Geräteantwort gilt als M1-Bestätigung.

## Unveränderte verbindliche Abnahmegates

1. Vollständige Android-CI am endgültigen Kopf:help, build--dry-run, test, lint, assembleDebug, assembleInternal, assembleDeviceTest; APK-/Signatur-/Updatepaar-/Compilerexportprüfungen, alle bestehenden und neuen API36-SQLCipher-/Compose-Tests ohne Kürzung ihrer Prüfinhalte sowie Abschlussjob.
2. Neue verschlüsselte Migration18/22→23 und sämtliche bestehenden Migrationen, Keystore-/Wrong-key-/Reopen-/Provenienz-/Rollback-/Signaturregressionen; kein destruktiver Fallback.
3. Exaktes geprüftes Testupdate40129 mit unverändertem Testpaket/Testzertifikat und passender eigener Gerätebestätigung. Bestehende Installation/Daten nicht löschen; direkte verständliche Anleitung im Chat ist Pflicht.
4. Review der MI-Zuordnung und konservativen Unknown-/Konflikt-/Scopegrenzen. Freigabesperrend: erfundene positive Intervalle, doppelte Minuten, falsche Crewsemantik, verlorene Herkunft, fälschlich vollständige Abdeckung, überschriebene Originale/Kontexte oder Veröffentlichung veralteter/halber Snapshots.
5. Gesonderte Produktmergefreigabe. PR88 bleibt Draft/unmerged. M2 nur mit neuer ausdrücklicher Freigabe und seinen eigenen rechtlichen Nachweisen. Unabhängige qualifizierte Fachgesamtprüfung und Beta5–10 Fahrer bleiben spätere Releasegates.
