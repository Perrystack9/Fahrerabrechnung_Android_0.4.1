# M3 – Prüfvertrag und tatsächliche Abnahme

Arbeitsstand16.10.2026.165 isolierte Domainmethoden erfolgreich; [konkrete Zuordnung](VIOLATION_ENGINE_M3_REFERENCE_RESULTS.md). **Noch keine abgeschlossene technische Gesamtprüfung oder Geräteabnahme.** Die nachfolgenden Sollwerte stammen aus den vor der Implementierung hergeleiteten [M0-Referenzen](VIOLATION_ENGINE_M0_REFERENCE_CASES.md); sie sind kein durch den Algorithmus erzeugtes Testorakel. M2 ist separat technisch abgenommen, [Integration](M2_INTEGRATION_20261015.md).

## Fachliche Vektoren

| Referenzen | Unabhängige Erwartung / notwendige Unterscheidung |
|---|---|
| LG40/41 |780+660=1440:11h rechtzeitig.781+660=1441: bei24h nur659min; freie Reduzierung möglich, nach drei belegten Reduzierungen1min Fehlmenge. Späteres tatsächliches Ende heilt diesen alten Fensterbefund nicht. |
| LG42/43 |Viermal900+540: nur drei zulässige Reduzierungen; vierter9-h-Block120min unter11h. Ein Montag setzt diesen Zähler nicht zurück. Der getrennte M2-Wochenzähler bleibt erhalten. |
| LG44/45 |240+180+480+540=1440:3+9 regelmäßig.179 als Erstteil oder539 als Zweitteil erfüllen diese Aufteilung nicht; eine zuvor eigenständig gültige9-h-Ruhe wird nicht durch die bloße Reihenfolge9+3 entwertet. Legale Alternativen erhalten. |
| LG46/47 |960+660: im ersten24-h-Fenster480 statt540,60min Fehlmenge; tatsächliches Ruheende bei+27h. Drei1080+360-Fenster: je180min; Folgefenstermethode ausdrücklich Q07/L03, kein7-h-Ruhebeweis. |
| LG48/49/100 |1260+540=1800: nachgewiesene Crew erfüllt9h in30h;1261 lässt539min. Zweitkarte/Bestätigung nicht erforderlich. Solo nach der optionalen Anfangsphase oder unbekannte Crewzuordnung ist ein anderer Fall. Manuelles c-Bit ist kein Crewbeweis. |
| LG50–53/98 |420 Fahrt+45 Pause+375 Arbeit+660 Ruhe=1500. Derselbe Dienst6×24h erlaubt einmal,8×24h zweimal bei allen Voraussetzungen.421 Fahrt, zu kurzer Dienst oder erschöpfter Dienstzähler gewähren keine weitere25-h-Ausnahme. Fehlende Voraussetzungen bleiben bedingt; zulässige24-h-Reduzierung als Alternative prüfen. |
| LG54/101–103, Tagesanteil |45h benötigen keine zusätzliche vorangestellte11h-Ruhe.23h und1439min können Tagesanker sein, sind keine24-h-Wochenruhe.1440/2699/2700 unterscheiden nur zeitliche Wochenkandidaten; Wochenpflicht/Schuldzuordnung bleibt M4. |
| LG55/56 |Datenstand12:00: letzter11-h-Beginn13:00, noch60min; keine Systemuhrfortschreibung. Belegte Arbeit bis13:01 kann bei erschöpften Reduzierungen und ohne andere legale Alternative1min unumkehrbar fehlen lassen. Bei freier9-h-Alternative noch kein solcher Befund. Keine neue feste Warnschwelle. |
| LG57/99 |Unbekannte660min sind weder Ruhebeweis noch bestätigte Fehlruhe. Ordnungsgemäß manuell nachgetragene540/660min zählen mit ihrer belegten Semantik; keine Änderung der Originale. |
| LG81/82/109 |Nettoruhe660 bei zwei Unterbrechungen30+30: regelmäßig möglich bei weiteren Voraussetzungen.61min oder dritte Unterbrechung unzulässig. Netto659/660,1439/1440,2699/2700 je Ruheart und historischer Rechtsfassung getrennt. |
| LG83/84 |G1-Fährbeginn erzeugt kein fiktives Fährende; andere konkrete Belege können Voraussetzungen schließen. Zwei unterbrochene4,5-h-Teile ergeben keine reduzierte tägliche9-h-Ruhe. |
| LG85–87/110 |Wochen-Fährerweiterung erst ab20.08.2020; regelmäßige Wochenruhe zusätzlich Kabine und geplanter Weg≥480min.479/480 sowie unbekannt→belegte Kontextrevision prüfen. Kein Fahrplan aus Ruhesumme, keine allgemeine Hotel-/Lkw-Abfrage. |
| LG88 |180 erster Teil und240+300 zweiter Teil, Unterbrechung30:3+9 netto,1230min verstrichen. Unterbrechungen über beide Teile gemeinsam begrenzen; nicht beliebige zwölf Stunden addieren. Erstteil-/historische Auslegungsgrenzen als solche testen. |

Vorbelastungen werden in Tests durch konkrete vorausgehende Quellenintervalle aufgebaut. Keine künstliche Behauptung eines vollständigen Zählerstands, um einen erwarteten Testwert zu erzwingen. Der erste unvollständige Historienrand bleibt unbekannt.

## Technische Gates

Herkunftsbindung und Deduplizierung beider Anwendungen; manueller Nachtrag/Unknown/Quellenkonflikt; gleiche Minute; offener Endpunkt/fortlaufende Ruhe ohne Endanker; Stornierung und deterministische Revision; Kontextwechsel/Reimport/Reopen und finaler M1-CAS; UTC/Sonntag–Montag/DST unabhängig von Anzeigezone; unzureichende Daten ohne sicheres Restbudget; keine Änderung der Original-/Receipt-/Signaturdaten. Neue Kontextschlüssel müssen den vorhandenen SQLCipher-Kontextcodec ohne Klartext- oder Schemaausweichpfad durchlaufen.

Alle sieben bestehenden Gradle-Aufrufe, APK/Signatur/Updatepaar, Compiler-Schemaexporte und vollständige API36-SQLCipher-/Compose-Suiten sind am endgültigen veröffentlichten Kopf auszuführen. Kein Filtern, keine Quarantäne und keine Lockerung der CI. M3-Gerätepaket und verständliche Anleitung direkt im Chat nach grünen technischen Gates; danach eigene Geräteantwort und gesonderte Mergeentscheidung durch Tobi.

## Fachliche Grenzen

Kein M3-Zahlenbefund ist eine unabhängige rechtliche Gesamtabnahme. L01–L09 bleiben je konkreter Abhängigkeit nachvollziehbar. Eine bedingte Fähr-/Crew-/Buszuordnung oder alternative Ruhezuordnung begründet kein unbedingtes Budget und keine pauschale Entwarnung. Wochen-/Ausgleichsmotor M4, UI M5, PDF M6, Release/Beta M7 sind nicht Bestandteil dieser Umsetzung.
