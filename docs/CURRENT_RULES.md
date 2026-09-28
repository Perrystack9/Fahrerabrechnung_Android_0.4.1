# FahrerTagebuch · aktuelle Produktregeln

**Stand: 28.09.2026.** Dieses Dokument und [Issue #3 „Berater-Handoff“](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3) bilden zusammen den maßgeblichen Fach-/Produktstand. Bei Widersprüchen haben sie Vorrang vor älteren Design-Handoffs, GPS-Konzepten und Tests. Issue #3 ist eine Produktvorgabe, **kein Auftrag, alle Punkte sofort zu implementieren**. Der tatsächliche Code-Stand steht in [WORK_STATUS.md](../WORK_STATUS.md).

## Standort, Karte und Datenquellen

- Smartphone-Standort nur nach ausdrücklicher Aktion zur Heimatpunktwahl im Onboarding: aktuelle Position im Vordergrund bestimmen oder den Punkt auf der Karte manuell setzen/korrigieren. Keine Smartphone-Routenaufzeichnung, Hintergrundortung, Smartphone-Kilometer, Routen-Matching oder GPS-/Tacho-Vergleiche.
- Der Heimatpunkt ist eine Koordinate mit festem **500-m-Radius**. Die Heimatpunkt-Karte bleibt zulässig.
- Spätere GNSS-Darstellung darf nur **echte einzelne Tachograph-/Fahrerkartenpunkte** zeigen: Zeitpunkt, Position und Punktart; bei Bedarf kleine Detailkarte mit Marker. Niemals Punkte zu einer erfundenen Strecke verbinden. Eine allgemeine Smartphone-Fahrtkarte ist nicht vorgesehen.
- Kein aus einer Kartenentnahme erfundener Tätigkeits-, Ruhe- oder Ortsnachweis. Sensible Kartendaten, vollständige APDUs und Rohdumps gehören nicht in Logs/Test-Fixtures.

## Onboarding und Tageszeitleiste

- Siebenstufiges Onboarding; Heimatpunkt in Schritt 2. Ältere Acht-Schritte- und „App-GPS während der Fahrt“-Vorgaben sind überholt.
- Tageszeitleiste als Kernfeature: kompakte horizontale Tagesübersicht, zoombares und verschiebbares Detail bis zur sinnvollen Minutenebene, antippbare Abschnitte mit exaktem Start/Ende/Dauer sowie verstellbare Zeitmarke. Jede Tätigkeit hat **Farbe und Symbol**: Lenken, andere Arbeit, Bereitschaft, Pause/Ruhe.
- Geplant sind belastbare Marker für Fahrzeugwechsel, echte GNSS-Punkte, Grenzübertritt, Ereignis/Störung, Fähre/Zug, Out of Scope und Kartenstatus; dazu Fahrzeugbezug und kompakte Tageskennzahlen. Nur aus Quelldaten belegte Merkmale anzeigen. Die Kartentag-Zeitleiste bildet Aktivitätsdaten als Vorschau ab; gespeicherte Fahrzeug-Quelldaten sind noch nicht damit verknüpft.
- Technische Ereignisse nicht automatisch als Verstoß oder Fahrerverschulden darstellen. Kontrollen gehören in Fahrerkarte → Details → Kontrollen; sensible Nummern dort standardmäßig maskieren.

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

Die technische Fahrerkarte-/Parser-Phase hat bereits echte Teilfunktionen; Aktivitäts- und Fahrzeugdatei werden zunächst im Arbeitsspeicher geprüft und nach gesonderter Bestätigung als verschlüsselte Teilimporte gespeichert. Ein vollständiger Import, weitere Kartendateien, Fahrzeughistorie, fachliche Berechnungen, PDF und Cloud folgen nur in gesonderten Umsetzungen. Keine ältere Spezifikation darf einen noch nicht implementierten Punkt als vorhanden ausgeben.
