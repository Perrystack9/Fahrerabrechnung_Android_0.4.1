# M2 – Referenztests und Abnahme

**Fortschreibung15.10.2026:** M2 ist im tatsächlich nachgewiesenen technischen Umfang ausdrücklich abgenommen und PR90 kontrolliert integriert. [Eigener Gerätebeleg40130](DEVICE_CONFIRMATION_40130.md), [Merge und neue main-CI](M2_INTEGRATION_20261015.md). Historische offene Prüfvermerke unten bezeichnen ihren damaligen Entwicklungsstand; Rechtsfragen bleiben offen.
Stand15.10.2026. M0-Sollwerte [LG17–LG39](VIOLATION_ENGINE_M0_REFERENCE_CASES.md) bleiben unverändert. Diese Zuordnung benennt reale Testmethoden, keine behauptete zusätzliche Rechtsfreigabe. **Endgültige vollständige Projekt-CI, deren exakter Kopf und neue Geräteabnahme müssen im M2-PR-Protokoll nachgewiesen werden.** Ein isolierter Lauf ist keine Gradle-/Android-/SQLCipher-Abnahme.

## Fachliche Referenzzuordnung

Domainklasse `DrivingReferenceTest`; synthetische Sequenzen werden durch den bestehenden M1-Snapshot-/Semantik-/Intervallbuilder geführt. Vor belegtem Testbeginn ausdrücklich Unknown, jeder Tagesrecord hat echten synthetischen00:00-Marker. Keine echte Karte/APDU, kein unbewiesener Vorzähler oder erfundener Endpunkt. Vorzähler stammen aus tatsächlichen synthetischen Tagen mit ihrer Ruhechronologie.

| Referenz | Reale Methode / fester Erwartungswert |
|---|---|
| LG17 | `lg17`:540min, kein Vorgang, kein Tagesüberschuss. |
| LG18 | `lg18`:565min bei0/1 belegten vorherigen Vorgängen;600 Normalgrenze,35min bedingter Spielraum, kein Tagesüberschuss. Sicheres Budget unter L02 null. |
| LG19 | `lg19`:bei2 Vorgängen25min Überschuss;9h um15:45 erreicht, tatsächliche überschüssige Fahrt16:30–16:55. |
| LG20 | `lg20`:600/601min→0/1min; erster Überschuss11:30. |
| LG21 | `lg21`:dritte541min-Periode in derselben Woche→1min bei normaler9-h-Grenze. |
| LG22 | `lg22`:erste positive Zusatzfahrt Sonntag23:59/Montag00:00/00:01; UTC-Zuordnung und ungeteilter Tagesabschnitt, **L02 weiterhin ausdrücklich vorläufig**. |
| LG23 | `lg23`:3359/3360/3361 Wochenminuten→0/0/1min; letzter ÜberschussSamstag18:59. |
| LG24 | `lg24`:2700+2700=5400, folgendes überlappendes Paar2700+2701=5401; nur letzteres+1min. |
| LG25 | `lg25`:3000 Vorwochenminuten plus2399/2400/2401 laufend→5399/5400/5401; offene Paarfrist verhindert eindeutigen Zahlenüberschuss nicht. |
| LG26 | `lg26`:23:30–00:30 verteilt30/30min, Summe60, keine verlorene/doppelte Sekunde. |
| LG27 | `lg27`:Länderpunkt nach60 von120min setzt nichts zurück. AETR-/Scope-Regression separat. |
| LG28 | `lg28`:419min Ruhe→1200min Tageslenkprojektion;420/539→600+600 nach Q13, ausdrücklich keine erfüllte Tagesruhe. |
| LG29 | `lg29`:270/271min→0/1min, exakte erste Mehrfahrt04:30. |
| LG30 | `lg30`:44min allein kein Reset;45min Reset. |
| LG31 | `lg31`:15+30 reset, beide Quellen-/Zeitbelege erhalten; danach1min Fahrt und269min Pausenrest. |
| LG32 | `lg32`:Standard30+15 kein Abschluss,1min Überschuss. |
| LG33 | `lg33`:10+10+25 nicht zu45 addieren,1min Überschuss. |
| LG34 | `lg34`:Arbeit/gewöhnliche Bereitschaft60min verlängert verstrichene Zeit, Fahrt bleibt271min; erste Mehrfahrt05:30. |
| LG35/LG36 | `lg35`/`lg36`:Bus30+15 und20+25 vor/nach2024, jeweils≥15 und Summe≥45;15+15 genügt nicht. Am noch ungeklärten historischen Grenztag nur bedingte Projektion, keine juristische UTC-Inkrafttretensbehauptung. |
| LG37/LG100 | Drei`lg37`-Methoden:aufgezeichnete echte Crew-Beifahrer45min zählt; Arbeit/manuellesc1 nicht; Unknown ist weder belegter Reset noch bestätigter Pausenverstoß. Keine Zweitkarte/Bestätigung. |
| LG38 | `lg38`:frühere1min bleibt nach späterer45-min-Pause erhalten. |
| LG39 | `lg39`:D601→1min Tages- und331min Pausenüberschuss als zwei eigenständige Mengen, keine zusammengezählte Strafe. |
| LG99 | `lg99`:echter manueller45-min-Nachtrag versus unbekannte Entnahme; gleicher Rohzweck ersetzt keine semantische Kenntnis, unbekanntes Budget null. |
| LG111 | `lg111`:normale10h zuerst,0/1/60/61/120/121min darüber; passende besondere Hinweise nur bei positiver exakter Menge, immer offene Rechtsbewertung, keine Schuld. |
| LG112 | `lg112`:90-h-Paarüberschuss erhält keine spezielle1-/2-h-Heimkehrrechtfertigung. |
| LG113 | `lg113`:29/30/45min bei passender2-h-Zahlenkonstellation rechtfertigt nichts automatisch; normale45-min-Pflicht und30-min-Artikel12-Voraussetzung nicht vermischen. |

## Zusätzliche tatsächliche Regressionen

- Wochenruhe innerhalb derselben Rechtswochenprojektion setzt verbrauchte Verlängerungen nicht zurück. Montag teilt nur die Woche, niemals den laufenden Tageslenkabschnitt. Fehlende andere Wochenhistorie erschafft keinen dritten Slot nach zwei positiv belegten Vorgängen.
- Mehrfachimporte und beide Anwendungen: gleiche Fahrmengen/Ereignis-IDs, alle Ursprünge erhalten, andere vollständige Herkunfts-/Berechnungsrevision. Kein Originalersatz.
- Fehlende linke Historie: nur untere Mengenprojektion/spätestens belegter Beginn, kein exakter Rechtsbeginn und kein sicheres Budget. Spätere belegte Pause ermöglicht unabhängigen lokalen Nachweis; frühere Lücken bleiben sichtbar.
- Offenes Ende wird weder bis Mitternacht noch Import-/Geräteuhr verlängert. Winterzeit und DefaultzonenUTC/Berlin/NewYork liefern identische Instants/Mengen/IDs/Revisionen.
- AETR/ungeprüfter Rechtsraum/50-km-Linie:keine unberechtigten EU-Befunde oder Budgets; Rohfahrt bleibt unverändert. Cancellation propagiert.
- Fähre:nettozeit-/ankerabhängige Tagesbewertung bleibt für M3 gesperrt; späterer Marker löscht vorher belegte1min nicht. Crew-/Slotkonflikt erzeugt keinen erfundenen gesicherten Pausenverstoß.
- Historischer Artikel12-Hinweis vor2020 ausgeschlossen. Nach abgeschlossener45-min-Pause aktuelles270-min-Budget ohne erfundene Folgeminute; neueste Lücke blendet es aus und erhält frühere Befunde.
- `partialCrewAvailabilitySplitIsConditionalAndAProvenFullBreakRestoresCertainty`:15+30 mit kurzem Bereitschaftsteil an erster oder zweiter Stelle, zusätzlich Bus30+15; günstige Zuordnung, kein erfundener gesicherter Pausenverstoß, L09 offen, keine exakte Gesamtmenge oder gesichertes Budget nach bedingtem Reset. Spätere belegte45min stellen269min nach einer weiteren Fahrminute wieder her; ursprünglicher Hinweis bleibt.
- `actualRecordedRestSplitInCrewNeedsNoAdditionalLegalOrUserConfirmation`:echte aufgezeichnete15+30-Ruhe in Crew wird regulär angerechnet, danach269min belegtes Pausenrestbudget, kein zusätzlicher Bestätigungsdialog oder künstlicher L09-Vorbehalt.
- `shorterOccasionalBusFirstPartSurvivesALongerEarlierOrdinaryPart`:frühere30min im Linienverkehr verdrängen spätere20min im Gelegenheitsverkehr nicht; letztere+25min erfüllen den Buspfad, Herkunft beider Teile erhalten. Insgesamt50 M2-Domainmethoden, unverändert sechs neue Daten-Instrumentierungsmethoden.

## Echte SQLCipher-/Import-/Flow-Prüfungen

`data/src/androidTest/…/DrivingCheckInstrumentedTest.kt` ergänzt **sechs** Methoden:beide Anwendungen/Replay/Originalbelege; Ablehnung veralteter Kontext-/Importtokens; automatische Neubewertung nach vollständigem Import und Kontext; echter manueller Nachtrag; unbekannte Kartenentnahme; lesende Rechnung/Wiederöffnung/Room23-Integrität ohne neue Tabelle. Bestehender Parser, reale Importpipeline, echte verschlüsselte Room-Fabrik. Keine vorhandene Suite verkürzt oder gefiltert.

## Verbindliche technische und fachliche Gates

1. Alle sieben unveränderten Gradle-Aufrufe, alle Unit-/Lint-/APK-/Testsignatur-/Updatepaar-/echten Compilerexportprüfungen; komplette API36-App-/Compose-/Datensuiten einschließlich aller sechs neuen M2-Methoden, Abschlussjob.
2. Alle bestehenden SQLCipher-/Keystore-/Wrong-key-/Migration-/Herkunfts-/Signatur-/Rollback-/Reopen-Prüfungen erhalten. Exporte1–23 und geschützter CURRENT_RULES-Abschnitt bytegleich. Keine neue Migration/Schemaänderung notwendig; bestehende Kette bleibt auszuführen.
3. Neues identifiziertes Testupdate40130 mit stabilem Testzertifikat, eigener verständlicher Chat-Anleitung und erforderlicher neuer Geräteantwort.40129-Bestätigung gilt nicht für M2.
4. Fachliches Review der tatsächlichen Zahlen-/Zeit-/Herkunftsergebnisse, L02-Projektionen, L03/Historie und L09-Anrechnung kurzer Crew-Bereitschaftsteile; [konkrete offene Nachweise](VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md). Keine erfolgreiche CI als juristischen Nachweis ausgeben.
5. Tobi entscheidet danach gesondert über M2-Abnahme/Merge. M3–M7/Release unverändert gesperrt. Unabhängige qualifizierte Fachgesamtprüfung und Beta5–10 Fahrer bleiben Releasegates.

A03/A07/A08 und PausenanteilA18/A20 sind damit konkret M2 zugeordnet; vollständige spätere UI-/PDF-/Ruhe-/Releaseanteile bleiben an ihren ursprünglichen Paketen. Die Regeln zu unterschiedlichen Reduzierungs-/Verlängerungszählern werden nicht mit einer vorgezogenen M3-Ruheimplementierung verwechselt.

## Tatsächlich belegter Prüfstand bei Wiederaufnahme

PR90-Ausgangskopf `9a7911bde88347e82660cc1f7ba31806b38ceb6d`/Baum `a8dbce895d2214ccdedd33e3dcb0121cae04a240`: vollständige CI [38752173910](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38752173910), Versuch1, alle vier Jobs erfolgreich; sieben Gradle-Gates, APK-/Testsignatur-/Updatepaar-/Compilerexportprüfung und ungekürzte API36-App-/Dateninstrumentierung. Keine Filter oder CI-Änderung.

Die Folgekorrektur hat zusätzlich einen tatsächlich erfolgreichen isolierten Lauf mit84 Methoden (34 M1 und50 M2). Compiler2.3.21 aus der geprüften Gradle-Distribution, JUnit4.13.2; ausdrücklich kein Ersatz des projektkonfigurierten Kotlin2.4.20, der vollständigen Projekt-CI oder echter SQLCipher-/Gerätetests. Finaler Folgekopf und tatsächlich ausgeführte vollständige Ergebnisse werden im PR90-Abnahmeprotokoll ergänzt. Neue40130-Gerätebestätigung bleibt offen.
