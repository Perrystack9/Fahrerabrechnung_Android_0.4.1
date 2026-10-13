# FahrerTagebuch – verbindliche Produktentscheidung vom 12.10.2026

Tobi hat diese Entscheidung am **12.10.2026 um 20:53 Europe/Berlin** ausdrücklich beauftragt. Sie ersetzt alle widersprechenden früheren Anforderungen, insbesondere **Punkt 8 und Punkt 9 des bisherigen Berater-Handoffs in Issue #3 vollständig**. Die frühere Einschränkung auf „keine Steuerlogik“ reicht nicht mehr: Die gesamte Spesen-, Lohn- und Abrechnungsfunktion entfällt.

Maßgeblich für neue Sitzungen: diese Entscheidung, [CURRENT_RULES.md](CURRENT_RULES.md), [WORK_HANDOFF.md](WORK_HANDOFF.md) und [WORK_STATUS.md](../WORK_STATUS.md). Historische Designpakete, Testkataloge, Checkpoints und frühere Issue-Fassungen dürfen keine gestrichenen Produktziele wieder einführen.

## Technische Grundlage bleibt erhalten

Der bestehende Fahrerkarte-Import bleibt vollständig erhalten: Leser/APDU, Parser, beide Kartenanwendungen, gemeinsame MF-Belege, eigene Profil-/Kartenbindung, unveränderliche Originalquellen, Sitzungen, SQLCipher-Persistenz, exakte Historienwahl und bestätigte Importdaten. Kein Rückbau, Neuaufbau oder Datenverlust durch diese Entscheidung.

Bestätigte Basis: **40122 / Schema 19**, geprüfter PR65-Kopf `62fc0d299f87b26109a98ff11e94c9202a27ae01`, unverändert gemergt als `21edb80ce46c0202204bb74d83486c634b3c90c6`. [Gerätebestätigung und Grenzen](DEVICE_CONFIRMATION_40122.md). Offene Signatur-/Vertrauens-/Standarddownloadarbeit bleibt sinnvoll; vollständige Kartenabdeckung wird noch nicht behauptet.

## Vollständig entfallene Anforderungen

| Bisheriger Bereich | Verbindlicher neuer Status |
|---|---|
| Handoff Punkt 8: Spesen / Reise / Arbeitgeberabrechnung | Vollständig gestrichen: keine Spesen, Reisekosten, Pauschalen, Reise-/Abwesenheitsberechnung, Heimkehr-/Übernachtungslogik oder davon abhängigen Berechnungen. Auch Auslands-/doppelte Spesen entfallen. |
| Handoff Punkt 9: Arbeitszeit & Zuschläge | Vollständig gestrichen: keine Lohn-/Gehaltsberechnung, Arbeitgeberzuschläge, Stundenlohn, Zuschlagsfenster/-beträge, bezahlte Bereitschaft/Pause oder lohnrelevante Arbeitszeit. |
| Heimatstandort / Zuhause | Entfällt einschließlich Einrichtung, Heimatpunktwahl, Standortabfrage für Zuhause, Heimradius und darauf beruhender Erkennung. |
| PDF-Abrechnungs-/Lohnexport | Vollständig gestrichen. Alte Monats-/Tagesexportlayouts mit Lohn, Zuschlägen oder Spesen sind keine offene Umsetzung mehr. |
| Neutraler Tätigkeits-/Fahrtenbericht | Aktuell **nicht verbindlich geplant**; kein Renderer-/Exportauftrag aus alten PDF-Unterlagen. |
| Hauptbereich „Abrechnung“ | Gehört nicht mehr zum zukünftigen Produkt. Später entfernen oder ersetzen; ein konkreter Ersatz ist noch offen. |

Diese Funktionen sind **entfallen**, nicht bis nach dem Kartenimport zurückgestellt. Gehalts-/Spesenkonfiguration, Firmen-Bundesland für Zuschläge, bezahlte Stunden und PDF-Abrechnung gehören auch nicht in die künftige Einrichtung oder Auswertung. Bestehende Platzhalter, Datenfelder, Schnittstellen und Tests begründen keine Produktanforderung.

## Tätigkeiten bleiben fachlich nutzbar

| Kartenquelle | Weiter erlaubt | Nicht daraus ableiten |
|---|---|---|
| Fahren / Lenken | Anzeige, belegte Zeitabschnitte, Dauern und neutrale Auswertung | Bezahlte oder abrechnungsrelevante Stunden |
| Andere Arbeit | Anzeige, zeitliche Zusammenfassung und neutrale Auswertung | Lohnrelevante Arbeitszeit |
| Bereitschaft | Anzeige, Dauer und neutrale Statistik | Bezahlung oder Zuschlagsfähigkeit |
| Pause / Ruhe | Anzeige, Dauer und neutrale Auswertung | Heimkehr, Übernachtung, Spesen oder Gehaltsposition |

Dauern dürfen weiterhin zusammengefasst werden. Herkunft, Sitzung, Kartengeneration und Lücken bleiben erkennbar; die beiden Anwendungen derselben Karte nicht addieren. Fehlende Zeit nicht als belegte Tätigkeit oder Ruhe ergänzen. Begriffe/Kennzahlen dürfen keine bezahlte, lohnrelevante oder abrechnungsrelevante Zeit suggerieren.

## GNSS und spätere Kartenansicht

GNSS aus der Fahrerkarte bleibt Bestandteil der App. Eine spätere Karte darf **echte gelesene GNSS-Punkte** und einen **groben chronologischen, approximierten Verlauf** zwischen ihnen zeigen.

- Echte Punkte bleiben sichtbar und unterscheidbar, mit nachvollziehbarer Zeit, Punktart und Quelle.
- Verbindungslinien ausdrücklich als **approximierter Verlauf** kennzeichnen. Sie beweisen keine tatsächlich gefahrene Straßenroute.
- Keine erfundenen Zwischenmesspunkte, keine Straßenroutenbehauptung und keine gefahrenen Kilometer allein aus Verbindungslinien.
- Fehlende/ungültige Positionen, Zeitpunkte und Datenlücken nicht als vollständigen Verlauf ausgeben.
- Exakte ausgewählte Kartendaten verwenden; historische Sitzungen nicht aus jüngeren ergänzen.
- Keine Smartphone-Routenaufzeichnung oder Hintergrundortung. Die bisherige Smartphone-Standortausnahme für Heimatpunktwahl entfällt als Produktziel.

Frühere pauschale Verbote jeglicher Punktverbindungen gelten nicht mehr als zukünftige Produktvorgabe. Bestehende Decoder-/Detailansichten erzeugen weiterhin keine Route; eine neue Kartenansicht wurde noch nicht implementiert.

## Spätere Neukonzeption, jetzt Dokumentation

Die **gesamte Oberfläche, Navigation, Einrichtung und das Onboarding** werden später grundsätzlich neu konzipiert. Weder fünf feste Haupttabs noch sieben/acht Schritte oder bisherige Bildschirmplätze sind verbindliche Ziele. Abrechnung, Zuhause, Spesen und Gehaltsfelder entfallen; Ersatz und neue Struktur bleiben offen.

Dieser Auftrag ändert jetzt ausschließlich Dokumentation. Die vorhandene App kann noch alte Tabs, Heimatpunkt-/Standortfelder und Konfigurationsvorschauen enthalten. Das ist technischer Altbestand, keine künftige Anforderung und kein Auftrag zum sofortigen UI-Umbau. Bestehende Importdaten und Profile bei späteren Änderungen erhalten.

Merge-/Hardware-Sperren und CI-Vorgaben bleiben bestehen. Neue relevante Produkt-/Import-/Schema-/Sicherheitsänderungen benötigen eigene Prüfungen und erforderliche Gerätebestätigung. Die Bestätigung 40122 gilt nur für ihren geprüften Stand.
