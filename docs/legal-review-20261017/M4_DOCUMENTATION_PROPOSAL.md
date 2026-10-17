# Vorschlag für spätere Übernahme in M2-/M3-/M4-Dokumente

**Nur Anhang im getrennten Docs-Branch.** Keine der hier genannten vorhandenen Dateien wurde geändert. Insbesondere bleiben PR92, dessen Head, bestehende Test Evidence und main unverändert. Die historische Entscheidungs-/Prüfgrundlage soll bei einer später ausdrücklich genehmigten Übernahme mit Datum erhalten bleiben. Keine rückwirkenden neuen Testresultate eintragen.

## Vorschlag1 – genaue M2-Quellenfundstellen

Ziel: docs/VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md, TRACE-Zeile und2020/1054-Zeile; entsprechender Normverweis auch in docs/VIOLATION_ENGINE_M0_SOURCE_REGISTER.md, L06-Zeile.

Einzusetzende datierte Präzisierung:

> Nachprüfung17.10.2026: TRACE-II nennt die feste Woche auf PDF-Seite34/gedruckter Seite28 ausdrücklich lokal. Der Hinweis zur ersten Überschreitung von9h vor Mitternacht steht auf PDF-Seite37/gedruckter Seite31; die frühere AngabeS.33 ist als Seitenverweis überholt. Der Leitfaden entscheidet nicht verbindlich, welcher geografische Uhrbezug und Ereignisinstant für alle grenzüberschreitenden Fälle gilt. Die technische Woche bleibt, am17.10.2026 erneut verbindlich bestätigt, Montag00:00UTC bis Montag00:00UTC. Keine neue Produktentscheidung hierzu.

> Für die Art.12-Rückkehrergänzung ist2020/1054 Art.1 Nr.11 maßgeblich. Nr.3 ergänzt Art.7-Crewpause, Nr.8 betrifft Art.9-Fähre/Zug. Der bisherige zusammengefasste Verweis Nr.3/8 genügt für Art.12 nicht.

Kein inhaltlicher Widerruf der tatsächlich vorhandenen TRACE-Ortszeitstelle. Keine Änderung des UTC-Codes oder der bisherigen L02-Kennzeichnung.

## Vorschlag2 – offene Crewfrage fachlich genauer benennen

Ziel: M2 Legal Evidence, L09-Zeile; korrespondierender M3-L09-Text nur als datierte Ergänzung.

> Q13 undQ14 der aktuellen Kommissions-Q&A behandeln unterschiedliche Fragen: Belegwirkung der zusammenhängenden45min-Automatik gegenüber grundsätzlicher Pausenteilung. Die offene Fachfrage ist die sichere Anerkennung kürzerer automatisch gespeicherter Beifahrer-Bereitschaften als konkrete Teilpause. Der DVSA-Leitfaden vom04.12.2025 enthält zusätzlich eine konkrete UK-Zuordnung9h-Crewruhe zum Dreierlimit; dessen EU-/deutsche Übertragbarkeit ist nicht belegt. L09 bleibt insoweit offen, ohne neue Routinebestätigung oder Zweitkarte.

Keine neue Produktfunktion, kein unbedingtes9h-Budget, kein Wegfall bereits aufgezeichneter verlässlicher15+30-Ruhepausen.

## Vorschlag3 – M4-Zusatzruhe/Alternativen konkretisieren

Ziel: ungemergtes docs/VIOLATION_ENGINE_M4_LEGAL_EVIDENCE.md, MW04/L04-1 und L04-2. Als Ergänzung nach bestehendem Text:

> Die Nachprüfung17.10.2026 fixiert LR09 als Gegenprobe für den konkreten internationalen Zusatzruhefall. Der dortige amtliche Hinweis zur Fristnähe muss bei der fachlichen Bewertung des zulässigen Suchraums ausdrücklich berücksichtigt werden. Daraus wird weder ein allgemeiner FIFO für Schulden noch eine uneingeschränkte Gleichwertigkeit sämtlicher technischer Rollenpfade abgeleitet. Ein unabhängiges fallgenaues Gesamt-Orakel bleibt erforderlich.

> LR07 unterscheidet rechnerisch späten Suffix und rechtzeitigen Präfix innerhalb desselben32h-Belegs. Der Präfix ist ohne geklärten Tagesruhe-/Grundkredit noch kein bestätigter Gesamtzeuge. LR08 trennt zwei gewöhnliche, nicht internationale aufeinanderfolgende Reduzierungen. LR11 hält21+20 vor45 und den konkreten2460min-Spätpfad fest. LR12 trennt Fristursprung am Ende der Bus-Ausnahme von der noch nicht bestätigten internen Langruhenteilung.

Keine Änderung an vorhandenen Testbelegen LG65/LG73, Testzahlen oder Laufresultaten. Neue LR-Fälle sind dokumentierte fachliche Gegen-/Grenzfälle.

## Vorschlag4 – gerichtliche Vergleichsfälle und Normgrenzen

Ziel: Quellenregister sowie M4 L04/L09 als Verweis, nicht als neue Freigabe.

> EuGH C-541/20 u.a., Urteil04.10.2024, Rn.163–174, betrifft u.a. die Arbeitgeberpflicht zur Rückkehrorganisation und die freie Ruheortwahl des Fahrers; die Aufhebung einer Fahrzeugrückkehrnorm ist davon getrennt. Keine automatische Heimkehrüberwachung ableiten. C-477/21 MÁV-START betrifft die tägliche/wöchentliche Ruhe nach RL2003/88 im dortigen Regime; kein automatisches Orakel für die Wochen-/Ausgleichszerlegung nach561 Art.8(3)/(7). Die spezifische Übertragung bedarf qualifizierter Prüfung.

Keine Erweiterung um nationale Arbeitszeit-/Unterkunftsprüfung, kein zusätzliches pauschales11h-Paket aus einer Analogie.

## Vorschlag5 – eindeutiger Status und weitere Gate-Reihenfolge

Datierte Ergänzung für spätere gemeinsame Planfortschreibung:

1. Technisches UTC-Wochenraster bleibt endgültig beschlossen; rechtlicher Quellenstatus gesondert L02, keine erneute Produktfrage.
2. Qualifizierte externe Bearbeitung zuerst der konkreten L04-/L09-Gegenfälle und des L02-Rechtsnachweises. Benötigte Fragestellungen/Orakel in LEGAL_MATRIX/LR01–LR16.
3. Nationale/historische Nachweise zu L03/L05/L07 und datierte Übergänge L01 nur im tatsächlich aktiven Produktpfad verifizieren; keine seltenen Spezialbetriebe neu ausbauen.
4. Falls daraus Änderungsbedarf entsteht: eigener ausdrücklich freizugebender Implementierungs-/Regressionstestauftrag; Sicherheits-, CI-, Herkunfts- und Gerätegates erhalten.
5. M4-Merge ausschließlich nach eigener ausdrücklicher Freigabe; M5–M7/Release ebenfalls gesondert. Dokumentationsfreigabe wäre kein Ersatz.

Die frühere technische CI und positive Geräteabnahme40132/Room23 bleiben technische Nachweise des damaligen Kandidaten; die offene Rechtsprüfung wird davon nicht nachträglich als abgeschlossen abgeleitet.

**Nicht Bestandteil dieses Vorschlags:** Änderung von PR92-Metadaten, bestehenden Test Evidence, Code, Tests, Schema, Kryptografie, Parsern, Build/CI, APK oder Produktversion. Nach Vorlage dieses Berichts ist der ausdrücklich angeordnete Stopppunkt erreicht.
