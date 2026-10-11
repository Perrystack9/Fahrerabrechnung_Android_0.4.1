Aktueller Checkpoint11.10.2026: Tobi bestätigt um08:36 Europe/Berlin allgemein „Funktioniert alles“ zum ausgegebenen PR58-Testupdate40117/Schema18 und beauftragt zwei Stunden selbstständige Weiterarbeit bis10:36:13. Alle vier Jobs der exakten CI-KöpfePR54–58 erneut erfolgreich geprüft. PR58 unverändert gemergt als1b592d627d5d18db9fc909f5c8cf2c796fd8578b; Merge-Tree2e314a47211f06696a6b05287e40b63575b921df bytegleich zum getesteten Kopfba9656f0e22980af94883783986367d7bcdf545b. GitHub hat die enthaltenen PR54–57 ebenfalls abgeschlossen, keine alten PRs offen. Keine neuen Einzelmesswerte oder nichtleeren Quellenfälle behauptet.

Neuer begrenzter Entwurf: vier unabhängige Zertifikatsarten der gewählten Kartenanwendung (Gen1 zwei); vollständige begrenzte EF-Lesung, struktureller Decoder, verschlüsselte exakte Karten-/Sitzungsquelle und Detailansicht mit Referenzen auf Antippen. Testversion40118, Schema18 unverändert, stabile Testkennung/-signatur. Alte19-Quellen-Historie bleibt unverändert; neue Gen2V2-Vorgänge können23 Quellen enthalten. Keine Kryptovertrauens-/Echtheits-/Vollständigkeitsbehauptung, keine neuen PSO-/Schreib-/Authentisierungsbefehle, keine Nachbefüllung. [Umfang und Primärformat](CARD_CERTIFICATE_IMPORT.md), [erst nach dem Arbeitsblock auszugebender Gerätetest](CARD_CERTIFICATE_DEVICE_TEST.md). 54 zusätzliche Testmethoden vorbereitet, vollständige Android-/SQLCipher-/Compose-CI am endgültigen Kopf noch erforderlich. Kein neuer Hardwaretest bestätigt; Entwurf bis späterer Rückmeldung ungemergt.

Dieser Checkpoint ist maßgeblich; die älteren Abschnitte darunter dokumentieren ihre jeweiligen früheren Stände.

---

# FahrerTagebuch · aktuelle Produktregeln

**Stand: 01.10.2026.** Dieses Dokument und [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) bilden zusammen den maßgeblichen Fach-/Produktstand. Die Profilbindung und die spätere vereinfachte Import-Bedienung wurden am 01.10.2026 ausdrücklich als Nutzervorgaben ergänzt. Bei Widersprüchen haben die aktuellen Regeln Vorrang vor älteren Design-Handoffs, GPS-Konzepten und Tests. Issue #3 ist eine Produktvorgabe, **kein Auftrag, alle Punkte sofort zu implementieren**. Der tatsächliche Code-Stand steht in [WORK_STATUS.md](../WORK_STATUS.md).

## Fahrerprofil, Fahrerkarte und Importzuordnung

- FahrerTagebuch ist zunächst eine **Single-Driver-App**. Die erste eigene Fahrerkarte wird nach **ausdrücklicher Nutzerbestätigung** an das lokale Fahrerprofil gebunden. Die Kartennummer ist **nicht** die dauerhafte Fahreridentität.
- Zielmodell: **DriverProfile → DriverCard(s) → ImportSession(s)**. Folge-/Ersatzkarten mit belastbar gleicher Karteninhaber-Identität werden als zusätzliche DriverCard demselben DriverProfile zugeordnet.
- Alte Karten und deren Imports bleiben unverändert erhalten. Keine rückwirkende Umschreibung auf die neue Kartennummer.
- Bei nicht eindeutigem Identitäts-Match muss der Nutzer bestätigen, dass es seine neue/Ersatzkarte ist. Keine automatische Zusammenführung auf Basis einer teilweisen oder unsicheren Übereinstimmung.
- Eine erkennbar fremde Fahrerkarte darf niemals mit dem gebundenen Profil, Kalender, der Fahrzeughistorie oder Abrechnung vermischt werden. Für V1 höchstens technischer Lesetest/temporäre Vorschau, ohne dauerhafte Profilbindung.
- Bereits gespeicherte ältere Teilimporte werden nicht nachträglich anhand von Datum, Kartennummer oder Vermutung einem Profil zugeordnet. Dafür ist eine belastbare Zuordnung oder ausdrückliche Nutzerbestätigung erforderlich.

## Spätere reguläre Kartenimport-Bedienung

**Nutzervorgabe vom 01.10.2026:** Die aktuelle technische Ansicht ist für Entwicklung und Gerätetests akzeptiert, aber keine Vorlage für die spätere normale Bedienung. Die folgende Vereinfachung ist ein verbindliches Ziel der vollständigen Import-Pipeline. PR #35 setzt seit 02.10.2026 den automatischen Vordergrund-Ablauf für Aktivitäten/Fahrzeugnutzungen auf main um; CI und USB-/Update-Gerätetest sind bestanden. Vollständige Kartenabdeckung bleibt offen. Den tatsächlichen Stand in WORK_STATUS prüfen.

- Die App soll einen angeschlossenen Kartenleser erkennen und eine verfügbare Fahrerkarte selbständig lesen. Zulässige Daten der eigenen, bestätigten bzw. zuverlässig zugeordneten Karte sollen automatisch übernommen und gespeichert werden; kein gesonderter Speichern-Schritt im normalen Ablauf.
- Manuelle Bedienknöpfe wie **„Kartenleser prüfen“**, **„Kartendaten testen“** sowie gemeinsames oder getrenntes Speichern gehören nicht in den späteren normalen Importablauf.
- Die Übersicht soll einen verständlichen, zusammenhängenden Kartenimport zeigen. Technische Begriffe wie **„Teilimporte“**, Quelldateien oder eine getrennte Aktivitäts-/Fahrzeugdateiverwaltung sind keine Bedienaufgaben für den Nutzer.
- Die verbindliche Profilregel bleibt erhalten: Die erste eigene Karte erfordert eine ausdrückliche Bestätigung; eine nicht eindeutig zuordenbare Folge-/Ersatzkarte ebenfalls. Automatisches Lesen hebt diese Bestätigung vor Profilbindung und dauerhafter Übernahme nicht auf. Erkennbar fremde Karten dürfen weiterhin nicht dauerhaft mit dem eigenen Profil oder dessen Daten vermischt werden.
- Technisch erforderliche Android-USB-Freigaben bleiben zu beachten. Fortschritt, Ergebnis und ein gegebenenfalls erforderlicher Eingriff bei Fehlern sollen verständlich angezeigt werden.
- Die vereinfachte Oberfläche darf fehlende oder fehlerhafte Daten nicht als vollständigen Import ausgeben. Die interne Herkunft und Trennung der Quellen sowie die Regeln für ältere unzugeordnete Daten bleiben erhalten.

## Standort, Karte und Datenquellen

- Smartphone-Standort nur nach ausdrücklicher Aktion zur Heimatpunktwahl im Onboarding: aktuelle Position im Vordergrund bestimmen oder den Punkt auf der Karte manuell setzen/korrigieren. Keine Smartphone-Routenaufzeichnung, Hintergrundortung, Smartphone-Kilometer, Routen-Matching oder GPS-/Tacho-Vergleiche.
- Der Heimatpunkt ist eine Koordinate mit festem **500-m-Radius**. Die Heimatpunkt-Karte bleibt zulässig.
- Spätere GNSS-Darstellung darf nur **echte einzelne Tachograph-/Fahrerkartenpunkte** zeigen: Zeitpunkt, Position und Punktart; bei Bedarf kleine Detailkarte mit Marker. Niemals Punkte zu einer erfundenen Strecke verbinden. Eine allgemeine Smartphone-Fahrtkarte ist nicht vorgesehen.
- Kein aus einer Kartenentnahme erfundener Tätigkeits-, Ruhe- oder Ortsnachweis. Sensible Kartendaten, vollständige APDUs und Rohdumps gehören nicht in Logs/Test-Fixtures.

## Onboarding und Tageszeitleiste

- Siebenstufiges Onboarding; Heimatpunkt in Schritt 2. Ältere Acht-Schritte- und „App-GPS während der Fahrt“-Vorgaben sind überholt.
- Tageszeitleiste als Kernfeature: kompakte horizontale Tagesübersicht, zoombares und verschiebbares Detail bis zur sinnvollen Minutenebene, antippbare Abschnitte mit exaktem Start/Ende/Dauer sowie verstellbare Zeitmarke. Jede Tätigkeit hat **Farbe und Symbol**: Lenken, andere Arbeit, Bereitschaft, Pause/Ruhe.
- Geplant sind belastbare Marker für Fahrzeugwechsel, echte GNSS-Punkte, Grenzübertritt, Ereignis/Störung, Fähre/Zug, Out of Scope und Kartenstatus; dazu Fahrzeugbezug und kompakte Tageskennzahlen. Nur aus Quelldaten belegte Merkmale anzeigen. Die Kartentag-Zeitleiste bildet Aktivitätsdaten als Vorschau ab und zeigt in PR #11 passende Fahrzeugnutzungen als Quell-Zeitfenster und Startpunkte, ohne offene Enden fortzuschreiben. PR #12 zeigt zusätzlich eine read-only Fahrzeughistorien-Vorschau aus gespeicherten Snapshots; dauerhafte Fahrzeugzuordnung, Arbeitstage und Kilometeraggregation folgen später.
- Technische Ereignisse nicht automatisch als Verstoß oder Fahrerverschulden darstellen. Kontrollen gehören in Fahrerkarte → Details → Kontrollen; sensible Nummern dort standardmäßig maskieren.

## Fahrzeugdarstellung und visuelle Zuordnung

> **Verbindliche Detail-Spezifikation:** [VEHICLE_UI_SPEC.md](VEHICLE_UI_SPEC.md). Bei der späteren finalen Platzierung der Fahrzeugdaten soll Work diese Darstellung innerhalb des bestehenden FahrerTagebuch-Designsystems möglichst 1:1 umsetzen.

- Fahrzeugdaten sollen später in **Tagesübersicht**, **Monatsübersicht** und **Fahrzeughistorie** kompakt und visuell verständlich dargestellt werden.
- Wenn aus belastbaren Quelldaten bzw. einer verlässlichen Fahrzeugzuordnung verfügbar, anzeigen:
  - **Marke**
  - **Modell**
  - **Antriebsart**
  - **Baureihe**
- Für ein **eindeutig zugeordnetes Modell** soll ein kleines, passendes LKW-Modellbild verwendet werden. Produktionsassets dafür **nicht KI-generiert** verwenden, sondern aus einer kuratierten, rechtlich nutzbaren Bildquelle übernehmen und für die App optimieren.
- Keine beliebigen Bilder zur Laufzeit aus Suchmaschinen laden oder hotlinken. Bevorzugt werden lokal mitgelieferte oder kontrolliert gecachte/verwaltete, optimierte Assets.
- Wenn der **Hersteller sicher erkannt**, das konkrete Modell aber nicht zuverlässig zugeordnet werden kann, **Herstellerlogo als Fallback** anzeigen.
- Wenn auch der Hersteller nicht zuverlässig erkannt werden kann, ein **neutrales LKW-Standardsymbol** anzeigen.
- Ein konkretes Modellbild darf **niemals nur geraten** werden. Unklare VIN-/Baureihen-Zuordnungen bleiben allgemein; die visuelle Darstellung darf keine höhere Sicherheit suggerieren als die Datenquelle hergibt.
- Bei Elektro-LKW zusätzlich ein kleines, klar erkennbares **Elektro-/Blitz-Badge** am Fahrzeugbild bzw. an der Fahrzeugkarte anzeigen. Weitere Antriebs-Badges (z. B. LNG/CNG/H₂) können später ergänzt werden, wenn die Antriebsart belastbar bestimmt ist.
- Modellbilder und Herstellerlogos sollen optisch einheitlich aufbereitet sein: bevorzugt freigestellt bzw. mit ruhigem Hintergrund, konsistenter Blickwinkel/Größe und ohne dekorative Effekte, die nicht zum FahrerTagebuch-Design passen.
- Rechte/Lizenzen der verwendeten Bilder und Logos müssen vor produktiver Auslieferung geklärt sein.

## Reise, Heimkehr und Spesen

- **Karte ziehen allein ist niemals Heimkehr.** Heimkehr erst bei belastbar erkanntem Heimatstandort und anschließenden mindestens **8:59 h zusammenhängender, belegter Pause/Ruhe**. Heimkehrzeit rückwirkend auf den Beginn dieser Phase setzen. Karte darf dabei eingesteckt oder entnommen sein; fehlende Zeit nach Entnahme ist keine automatisch belegte Ruhe. 8:59 h ist eine Produkt-/Toleranzschwelle, keine Aussage über gesetzliche Ruhezeiten.
- Auswärtige Übernachtung: laufende Reise, auswärts, zusammenhängende qualifizierende Pause/Ruhe ab 9 h mit technischer Toleranz **8:59 h**. Tagschlaf zählt ebenso; mehrtägige Wochenruhe kann mehrere Übernachtungen ergeben. Produktannahme: LKW-Fahrer auswärts = Übernachtung im LKW. Unklarer Aufenthaltsort bleibt **UNKLAR**.
- Eintägige Reise **über 8 h**: kleine Spesenpauschale; exakt 8 h oder weniger: keine. Mehrtägig: Anreise-/Abreisetag klein, voller Abwesenheitstag groß. Zwei Reisen an einem Tag ohne Doppelzählung zusammenführen; doppelte Spesen verdoppeln nur die Spesenposition, nicht die Übernachtungspauschale. Unklare Heimkehr lässt abhängige Spesen **UNKLAR**.
- Über-Mitternacht-Fahrt ohne echte Übernachtung vor der Implementierung als eigenen aktuellen Testfall fachlich präzisieren; keine alte GPS-Ableitung übernehmen.
- **Keine Steuerlogik** (Steuerfreiheit, Mahlzeitenkürzung oder besondere Berufskraftfahrer-Steuerpauschale).

## Arbeitgeberzuschläge und Historie

- Bereitschaft/Pause können als bezahlt konfiguriert werden, werden dadurch aber nicht automatisch zuschlagsfähig. Zuschlagsbasis grundsätzlich Lenken plus andere Arbeit. Mehrere Nachtfenster ohne gegenseitige Überschneidung; verschiedene Zuschlagsarten können gleichzeitig gelten. Prozente addieren, nicht multiplizieren; Rundung auf Monats-/Zuschlagsartenebene gemäß Testkatalog.
- Samstag, Sonntag und Feiertag bleiben konfigurierbare **Arbeitgeberzuschläge**; je Art Prozent oder feste Pauschale. Die Pauschale grundsätzlich einmal je betroffenem Kalendertag mit zuschlagsfähiger Tätigkeit. Feiertage richten sich nach dem im Profil gewählten **Bundesland des Firmen-/Arbeitgeberstandorts**, nicht nach wechselnden LKW-Positionen.
- Konfigurationsänderungen erhalten Gültig-ab-Datum und nachvollziehbare Revisionen; frühere Ergebnisse und PDF-Snapshots nicht still rückwirkend überschreiben.

## Aktueller Umsetzungsrahmen

Die technische Fahrerkarte-/Parser-Phase hat bereits echte Teilfunktionen. PR #35 liest und übernimmt Aktivitäten/Fahrzeugnutzungen im normalen Vordergrund-Ablauf automatisch, sobald die Karte bestätigt bzw. zuverlässig dem eigenen Profil zugeordnet ist. Erste eigene/unklare Ersatzkarten verlangen weiter ausdrückliche Bestätigung. Die früheren manuellen Test-/Speichern-Knöpfe sind historische Entwicklungsstufen, keine normale Bedienung auf main. Ein vollständiger Import, weitere Kartendateien, dauerhafte Fahrzeugzuordnung und fachliche Fahrzeugkennzahlen, Abrechnungsberechnungen, PDF und Cloud folgen nur in gesonderten Umsetzungen. Kartentag-Zeiten sind lediglich Summen belegter Quellenabschnitte, keine berechnete Arbeitszeit oder Ruhefeststellung. Keine ältere Spezifikation darf einen noch nicht implementierten Punkt als vorhanden ausgeben.

Technischer Stand 03.10.2026: Die Import-/Datenbankbasis ist der am Gerät bestandene PR-41-Stand mit Schema 10. Automatische Vordergrund-Lesung/Übernahme, Importstatus und unabhängige Dateilesung bleiben erhalten. Zusätzlich zu Aktivitäten, Fahrzeugen und Kontrollquelle werden Events_Data und Faults_Data als unabhängige verschlüsselte Quellen exakt zur eigenen Sitzung gespeichert; gültige leere Dateien erfinden keine Meldung. Die gemeinsame Ereignis-/Störungsübersicht zeigt vorhandene Quellangaben ohne Verstoßbewertung. Finale CI mit 248 Tests und Leser-/Update-Test am 03.10.2026 um 08:06 Uhr bestätigt; getesteter Kopf unverändert gemergt. Dies ändert keine der obigen Profil-/Fachregeln; nicht bestätigte und fremde Karten bleiben temporär, ältere Daten werden nicht rückwirkend zugeordnet. Der reine UI-Folgeschritt projiziert die gespeicherten Ereignis-/Störungszeitpunkte zusätzlich als antippbare Marker auf die Tageszeitleiste, ohne neue Lesung oder Persistenz. Vollständige Kartenabdeckung und GNSS-/Grenzübertrittsmarker bleiben offen. Herkunft und Grenzen stehen in WORK_HANDOFF.

Technischer Stand nach PR #44: Zusätzlich wird die eigene Quelle Specific_Conditions für Fähre/Zug und Out of Scope unabhängig gelesen und exakt zur ausgewählten Sitzung gespeichert. Schema 11 ergänzt zwei Tabellen ohne Backfill; ruheneutrale Detailansicht, keine Zeit-/Verstoß-/Abrechnungsableitung. Finale CI mit 292 Tests grün; unveränderter Kopf nach erneuter Codeprüfung gemergt. Der Nutzer hat den PR-44-Gerätetest am 03.10.2026 ausdrücklich übersprungen. Das ist keine Hardwarefreigabe für Schema 11; letzter bestätigter Leser-/Update-Test bleibt PR #41. [Umfang](SPECIFIC_CONDITION_IMPORT_FORMAT.md), [ursprüngliche Testanleitung und Status](SPECIFIC_CONDITION_DEVICE_TEST.md). Der reine UI-Folgeschritt ergänzt nur belegte Einzelzeitpunkte dieser exakten Quelle auf der Tageszeitleiste; keine neue Lesung oder Persistenz. [Grenzen](SPECIFIC_CONDITION_TIMELINE_CHECK.md). Die obigen Produktregeln bleiben unverändert.

Technischer Abschluss 10.10.2026, PR #45: reine Einzelpunktprojektion der exakten gespeicherten Sonderbedingungsquelle, vollständige CI mit 308 Tests und kurzer Bestandstest nach App-Schließen/Wiederöffnen bestätigt; unverändert gemergt. Schema 11, keine neuen Produkt-/Fachregeln. Der kurze Test umfasst keine erneute Kartenlesung oder realen Sonderbedingungsmarker; letzter vollständiger Karten-/Lesertest bleibt PR #41. [Herkunft und Grenzen](WORK_HANDOFF.md).


Technischer Abschluss 10.10.2026, PR #51: GNSS-Punkte und unabhängiger aufgezeichneter Authentisierungsstatus werden exakt je eigener Karte und Sitzung gelesen, gespeichert und separat angezeigt. Schema 12/Testversion 40110, vollständige CI 38046259050 Versuch 2 grün. Nutzer bestätigt passende GNSS-Anzeige und Erhalt nach Wiederöffnung (14:06 Europe/Berlin); keine ungenannten Gerätetests behaupten. Unverändert gemergt als 1c73cb4. Aktueller Folgeschritt: unabhängige Beginn-/Ende-Ortsquelle, Schema 13/Testversion 40111 im Feature-PR, CI und neuer notwendiger Gerätetest noch offen. Keine Änderung der obigen Fachregeln.

Technischer Stand 10.10.2026 nach PR52: Beginn-/Ende-Orte und getrennte Orts-Authentisierungsquelle sind nach vollständig grüner CI und ausdrücklichem Gerätetest unverändert auf main, Schema 13/Testversion 40111. Bestätigt sind Anzeige und Sichtbarkeit nach Kartenentnahme. Der begrenzte Folge-Entwurf ergänzt ausschließlich Gen2V2-Grenz-/Ländereinträge (EF0528), Schema 14/Testversion 40112; vollständige CI und neuer Gerätetest vor Merge erforderlich. Ein Eintrag kann von einer Karteneinlage stammen und beweist nicht automatisch einen Grenzübertritt. Keine neue Fachregel, Berechnung oder Timeline-Projektion. [Aktueller Checkpoint](WORK_HANDOFF.md).


Technischer Stand10.10.2026 nach PR53: Grenz-/Ländereinträge nach vollständiger grüner CI und allgemeiner Nutzerbestätigung „Funktioniert alles prima“ unverändert gemergt; Schema14/Testversion40112. Begrenzter Folge-Entwurf für eigenständige Be-/Entladeeinträge, Schema15/40113. Neuer Hardwaretest konkret bis nach11.10.00:56 Europe/Berlin aufgeschoben und gebündelt; neue Import-/Schema-PRs bis neuer Bestätigung offen. Keine Änderung der obigen Fachregeln, keine Dauer-/Arbeitszeit-/Fahrstreckenableitung. Aktueller Stand in WORK_HANDOFF.

Aktuelle Ergänzung10.10.2026: eigener Ladungsart-Zweig EF0530 mit unveränderten Rohcodes/Eintragszeiten, unabhängiger Quelle und additiver Migration15→16. Be-/Entlade- und Ladungsarteinträge nicht zu Transportintervallen, Arbeit/Pause oder Fahrzeugklassen umdeuten. Kein historischer Fallback. Geräteausgabe nach dem zweiten Block frühestens11.10.00:56 Europe/Berlin; bis konkreter neuer Gerätebestätigung ungemergt.
