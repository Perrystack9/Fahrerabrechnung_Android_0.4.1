# FahrerTagebuch · UI-Feinschliff 0.4.1 v2

Grundlage: `design-handoff-v2/WORK_UI_REFINEMENT_0.4.1.md`. Die schriftlichen Vorgaben des Handoffs sind im Projekt versioniert und haben Vorrang vor den Referenzbildern aus dem gelieferten ZIP.

## Umsetzung

- Fünf bestehende Haupttabs und ihre Navigation beibehalten. Alle NavHost-Bildschirmübergänge sind ohne Animation; während Navigation Compose einen ausgehenden Eintrag noch verwaltet, zeigt die App nur den aktuellen Inhalt. Die Auswahlmarkierung wechselt unmittelbar. Erneutes Tippen auf den gewählten Tab navigiert nicht erneut. Vorhandene Scrollzustände werden mit `saveState`/`restoreState` erhalten.
- Alle fünf Navigationslabels verwenden `maxLines = 1`, `softWrap = false` und dieselbe anhand sämtlicher Labels gemessene Schriftgröße (8–12 sp in 0,25-sp-Schritten). Die gemeinsame Messung berücksichtigt verfügbare Breite, Systemleisten, Dichte, Systemschriftgröße und Inter; sie wird über Tabwechsel hinweg gecacht. Alle Items erhalten dieselbe TextStyle-Instanz, unabhängig von Pixelrundungen.
- App-Name, Wortmarke und Claim: **FahrerTagebuch · Touren. Zeiten. Übersicht.** Navy `#0F2D4A`, Petrol `#008B8B`, Hellgrau `#E5E7EB`, Weiß `#FFFFFF`. Abgeleitete Töne unterstützen Lesbarkeit und Dunkelmodus. Karten mit geringer Schattierung, einheitliche Abstände und Inter Regular/Medium/SemiBold/Bold.
- Konzept C als saubere Android-Vektoren: Straßen-/F-Symbol mit Fahrerkarte; helle, dunkle und monochrome Varianten; Adaptive Icon mit getrenntem Hintergrund/Vordergrund und monochromer Android-Variante; Notification-Silhouette. Quadratische Symbolflächen und echte Kreisflächen. Keine Referenz-PNGs im App-Layout.
- Einrichtung als achtteilige, über Start/Einstellungen erreichbare UI-Vorschau. Session-Entwurf inklusive Zustandswiederherstellung, Zurück/Weiter und „Ändern“ aus der Zusammenfassung. Kein erzwungenes neues Onboarding für bestehende Installationen.
- Nur Benutzername; fester 500-m-Heimbereich; korrekte Spesenfeldnamen; freiwillige doppelte Spesen; mehrere Nachtfenster; Bereitschaft/Pause bezahlt standardmäßig aus; Erinnerungsentwurf mit Tag 21/25/27 und 18:00 Europe/Berlin. Formularvalidierung verhindert sich überschneidende Nachtfenster, auch über Mitternacht.

## Bewusste Phasengrenze

Die Einrichtung ist ausdrücklich eine Vorschau. Sie speichert keine fachlichen Profile und aktiviert weder Standorterfassung noch Berechtigungsdialoge, Kartenleser oder Erinnerungen. GPS-/Kartenaktionen sind als noch nicht verfügbar gekennzeichnet. Die Formularvalidierung berechnet keine Arbeitszeiten, Zuschläge oder Spesen. Domain, Datenbank und Fachschnittstellen bleiben unverändert.

Die fünfseitige Monatsübersicht und der separate Export „Verstöße & Hinweise“ bleiben Designvorgaben. `report/src/main/assets/design/` enthält die verbindlichen Exportregeln und Design-Tokens; ein Renderer wird noch nicht implementiert. Standardexport ohne Compliance/GPS-/Statusspalten, mit ausgeschriebenen Tageszeiten, Wochentagen und separat ausgewiesenen Nachtfenstern. „Abrechnung“ bleibt die bestehende Vergleichsseite der App und wird nicht als Exporttitel verwendet.

## Prüfungen und Grenzen

- CI führt unverändert `test`, `lint`, `assembleDebug`, `assembleInternal` mit JDK 17, Android SDK Platform 37 und offiziellem Gradle Wrapper 9.6.1 aus.
- Formularprüfungen decken benachbarte/überlappende Nachtfenster, Mitternacht, ungültige Eingaben und leere Entwürfe ab.
- Compose-Gerätetests prüfen 360/412 dp, Hell/Dunkel, gleiche einzeilige unbeschnittene Navigationslabels (vollständiger sichtbarer Text und Glyphengrenzen), Wechsel ohne Bildschirmüberlagerung nach 32 ms Compose-Zeit, Scrollzustand, acht Einrichtungsschritte und Rücksprünge aus der Zusammenfassung. Ein eigener Test startet die tatsächliche MainActivity.
- Ein veralteter SQLCipher-Exception-Import im bestehenden Instrumentierungstest wird auf `android.database.sqlite.SQLiteException` korrigiert; die Assertions und Datenbankimplementierung bleiben erhalten.
- Der bestehende Android-16-Emulatorjob erhält die vom Emulator-Runner dokumentierte KVM-Freigabe. Seine Datenpartition ist auf 2 GB begrenzt; ungenutzte .NET-/Haskell-Werkzeuge werden ausschließlich auf dem temporären Runner entfernt. Speicherplatz vor und nach der Bereinigung wird protokolliert. Android SDK, JDK, Gradle-Cache und Tests bleiben erhalten. Screenshots und Testergebnisse werden als CI-Artefakte gespeichert.
- CI-Ergebnisse sind dem zum Commit gehörenden Actions-Lauf zu entnehmen. Emulatorprüfungen ersetzen keinen erneuten Test auf dem physischen Galaxy S23 mit One UI 8.5; ein solcher Gerätetest wird hier nicht behauptet.

## Schriftquelle

Unveränderte `InterVariable.ttf` aus dem offiziellen Repository `rsms/inter`, Pfad `docs/font-files/InterVariable.ttf`, Git-Blob `4ab79e0102bbe0ffa1ed879b13e52ac8c6487833`. Die SIL Open Font License liegt unter `core/src/main/assets/licenses/Inter-OFL.txt` und wird mit der App ausgeliefert. Die Schrift funktioniert vollständig offline.
