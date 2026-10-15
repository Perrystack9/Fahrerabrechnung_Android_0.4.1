# M3 – Quellenprüfung und offene Fachnachweise

Stand15.10.2026. Autorenseitiger Quellen-/Implementierungsabgleich, **keine unabhängige juristische Abnahme**. [M0-Register](VIOLATION_ENGINE_M0_SOURCE_REGISTER.md), [M2-Nachweise](VIOLATION_ENGINE_M2_LEGAL_EVIDENCE.md) und deren offene L-Punkte bleiben erhalten.

## Nachweisgrundlage

| Quelle | Konkrete Prüfung / Ergebnis | Grenze |
|---|---|---|
| Q01-B Original561/2006, Art.4(f/g/h/o),8(1–5),9,29; Q01-K Dokumentationsfassung31.12.2024 |11/9/3+9 h,24/30 h, drei Reduzierungen zwischen Wochenruhen und tatsächliche Ruheendanker am Original gegen aktuelle Artikel kontrolliert. Bestehende vollständige Originalbytes und Pins aus M2 wiederverwendet. | Konsolidierung hat keine eigene Rechtswirkung. Allgemeine Art.12-Abweichung, Anwendbarkeit und historische Auslegung bleiben getrennt. |
| Q02 Original2020/1054 Art.1 ÄnderungArt.9 und Inkrafttreten | Unterbrechungsmöglichkeit seit20.08.2020 zusätzlich für reduzierte Wochenruhe; regelmäßige Wochenruhe zusätzlich Fahrplan≥8h/Kabine. Keine unterbrochene reduzierte Tagesruhe aus9h Nettosumme. | Gemischter Rechtsstandswechsel und rechtliche Ortszeit des Stichtags bleiben L01/L02. |
| Q03 Original2024/1258 Art.1 Nr.3(a),Art.2 |25h ab22.05.2024; derselbe einzelne Dienst≥6/8×24h, einmal/zweimal; Tagesfahrt≤7h, Straßenverkehrssicherheit/Arbeitsbedingungen und geltende Höchstarbeitszeit bleiben Voraussetzungen. | Keine nationale Arbeitszeitprüfung. EF0530 allein belegt weder Gelegenheitsdienst noch diese Voraussetzungen. |
| Q07 [Guidance Note7](https://transport.ec.europa.eu/system/files/2016-09/guidance_7_en.pdf), S.1 und Beispiele1–5 | Vollständig erneut geladen:273533 Bytes, SHA256 `2c0def3698a6e30e4efb48bbd90fe445c64972ca6d7c401106a4569fb84c784d`. Regelgerechte Ruhe verankert am tatsächlichen Ende; bei Fehlruhe empfiehlt die Leitlinie fortlaufende24-/30-h-Fenster, mit ausdrücklich behandelter später vollendeter Ruhe. | Kommissionsdienststellen-Leitlinie, keine verbindliche Gesetzesänderung. Unabhängige/historische Bestätigung L03 bleibt offen; nicht mit der7-h-Lenkzeitkonvention verwechseln. |
| M3-GN6 [Guidance Note6,2017-01](https://transport.ec.europa.eu/system/files/2017-01/guidance_6_en.pdf), vollständige Seite |8939 Bytes, SHA256 `4102816b73dd10ac0fa2d4b72cea6fa17b64759b3a70c5956c9ee14cd004f08b`. Unterbrechungen≤2/gesamt≤60min; Nettoruhe bleibt erforderlich. Bei3+9 gilt die Unterbrechungsgrenze für den gesamten Ruheanspruch. Tatsächliche volle Ruhe darf über24h hinausreichen; Mindestmenge muss rechtzeitig erfüllt sein. | Historische Leitlinie schließt Wochenruhe noch aus; dieser Satz ist seit2020 für die neu geregelten Fälle überholt. Keine unveränderte Anwendung der ganzen alten Seite. Rechtliche Erstteil-/Gesamtzuordnung zusätzlich L09 offen. |

Zusätzlicher historischer Nachweis **M3-BLRB-ARCHIVE**: [Bund-Länder-Hinweise über das amtliche KomNet-Archiv](https://www.komnet.nrw.de/KomNet/files/28325/requ/Leitfaden_Rechtsvor_Sozialvor-Stra_e.pdf), Stand01.12.2011, Abschnitt8.5 teilweise12.07.2013;1733864 Bytes, SHA256 `c6c196ea9e891b6bd56f2ce89a8c1137767790b3e5d0302de60e78a60a859f48`. Volltext tatsächlich abgerufen; S.13–14 trennen Tagesreduzierungen, Fährunterbrechungen und9/30-h-Crewregel. Historische Vollzugshilfe, **kein Ersatz für den fehlenden aktuellen BALM-v7-Volltext**, keine Schließung L02/L03/L09. Die damaligen Wochen-Fährausschlüsse nicht über2020 hinaus übertragen.

Der alte2016-09-Link für Leitlinie6 und der erneut versuchte BALM-Leitfaden v7 lieferten404; sie werden nicht als gelesener Volltextnachweis verwendet. Suchauszüge ersetzen keine Quelle. Q06 TRACEII bleibt eine Vollzugshilfe, kein verbindlicher Rechtssatz; Q08-FAQ ist nach eigenem Disclaimer keine verbindliche Gerichtsauslegung.

## Fortzuführende Prüfungen

- L01/L02: historische Aktivierungsgrenzen und gemischte Übergänge; keine Anzeigezone als Rechtszeit. M2-UTC-Wochenprojektionen bleiben eingeschränkt.
- L03: Leitlinie7/Folgefenster separat und sichtbar behandeln; sie rechtfertigt keine erfüllte Ruhe aus der7-h-Lenkempfehlung.
- L04: zusätzliche/reduzierte Wochenruhekandidaten gegenüber späterer M4-Pflicht-/Ausgleichszuordnung. Keine vorgezogene Schuld-/Wochenbewertung.
- L05: bestehenden artikelbezogenen Scope-Guard unverändert verwenden; nicht unterstützte Länder-/AETR-/Spezialpfade erzeugen kein EU-Urteil.
- L06: Zahlenfehlmengen sind keine abschließende Art.12-/Rechtswidrigkeitsanerkennung.
- L09: Fährunterbrechungen/3+9-Zuordnung, Crew-Tagesruhe und Bus-Vorbehalte am konkreten Fall absichern. Kurze Crew-Bereitschaft aus M2 bleibt offen und wird nicht als Tagesruhe behandelt.

Konkrete Implementierung, unabhängige Rechenreferenzen und Restblocker werden vor M3-Abnahme ergänzt. Keine externe Fachprüfung wird durch selbst geschriebene Tests ersetzt.
