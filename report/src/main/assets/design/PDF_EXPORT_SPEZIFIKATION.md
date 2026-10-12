> **Produktentscheidung 12.10.2026 – überholte Produkt-/UI-/PDF-Planung.** Historische Referenz, kein aktueller Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung entfallen vollständig; neutraler Bericht nicht verbindlich geplant. Alte Tabanzahl, Navigationsplätze und sieben/acht Schritte sind keine Ziele. Ganze Oberfläche und Einrichtung später neu konzipieren, jetzt keine UI-Umsetzung aus diesem Paket. Historische Tests/Beobachtungen nicht umgeschrieben. Verbindlich ist die [neue Produktentscheidung](../../../../../docs/PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

> **Historische PDF-Designreferenz, kein implementierter PDF-Renderer.** Maßgeblich sind [aktuelle Produktregeln](../../../../../docs/CURRENT_RULES.md) und [Issue #3](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/issues/3); den heutigen Implementierungsstand beschreibt [WORK_STATUS](../../../../../WORK_STATUS.md). Bei Widersprüchen haben die aktuellen Quellen Vorrang. Historische Aussagen unten bleiben als Zeitdokument erhalten.

# FahrerTagebuch – PDF-Export-Spezifikation

## Allgemeines

- Format: **A4 Hochformat**
- Stil: seriös, klar, dokumentartig
- Weißer Hintergrund
- Navy für Überschriften und Tabellenköpfe
- Hellgrau für dezente Tabellenflächen
- Petrol nur als Akzent
- Seite/Monat im Footer
- Logo vorzugsweise oben links

Der Begriff **„Abrechnung“ wird im Export vollständig vermieden**.

---

# Seite 1 – Monatsübersicht

## Kopfbereich

- Fahrer
- Profil
- Berichtszeitraum
- Fahrerkarte / Kartennummer gekürzt
- Ausstellungsland
- Kartengeneration

Nicht aufnehmen:

- Datenstand
- GPS-Kilometer
- GPS-Abdeckung
- Verstöße
- offene Prüfungen
- Status

## Hauptkennzahlen

- Arbeitszeit
- Lenkzeit
- Nachtstunden
- Spesen
- Abwesenheitstage

Ergänzend:

- Samstagseinsatztage
- Sonntagseinsatztage
- Feiertagseinsatztage
- Karte/Tacho-Kilometer

---

# Seite 2 – Tagesübersicht

Spalten:

1. Tag
2. Datum
3. Arbeitszeit
4. Lenkzeit
5. Nachtzeit
6. Spesen
7. Abwesenheit
8. Fahrzeug
9. Kilometer

Regeln:

- Wochentag: `Mo`, `Di`, `Mi`, `Do`, `Fr`, `Sa`, `So`
- `Arbeitszeit`, nicht `Bezahlte Zeit`
- keine Statusspalte
- keine GPS-Spalten
- keine Verstöße
- keine Kurzform `9:24`; für die Tagesübersicht lesbar ausschreiben, z. B. `9 Stunden 24 Minuten`

---

# Seite 3 – Arbeitszeit & Zuschläge

Darzustellen:

- Gesamtarbeitszeit
- Zuschlagsarten
- Zeitraum/Bedingung
- Zeitmenge
- Prozentsatz
- Betrag
- Tagesaufteilung, sofern sinnvoll

## Nachtzuschläge

Mehrere konfigurierbare Fenster sind ausdrücklich vorgesehen.

Beispiel:

| Zuschlag | Zeitraum | Prozentsatz |
|---|---|---:|
| Nacht 1 | 00:00–02:00 | 40 % |
| Nacht 2 | 02:00–06:00 | 25 % |

Im Export getrennt ausweisen.

Verschiedene Zuschlagsarten dürfen sich überschneiden und addieren, z. B. Nacht + Sonntag.

---

# Seite 4 – Spesen & Abwesenheitstage

Darzustellen:

- Anreisetage
- volle Abwesenheitstage
- Abreisetage
- Anzahl
- Tages-/Konfigurationssatz
- Gesamtbeträge
- Tagesübersicht Spesen
- Reiseübersicht nach Zeitraum

Keine Abkürzung `ÜN` verwenden.

---

# Seite 5 – Kilometer & Fahrzeuge

Darzustellen:

- Karte/Tacho-Kilometer gesamt
- genutzte Fahrzeuge
- Kennzeichen
- Land, sofern vorhanden
- Einsatzzeiträume
- Kilometer je Fahrzeug, **nur wenn aus Quelldaten zuverlässig ableitbar**

Nicht darstellen:

- GPS-Kilometer
- GPS-Abdeckung
- künstlich errechnete Kilometeranteile, wenn die Quelldaten keine zuverlässige Aufteilung erlauben

---

# Separater Export – Verstöße & Hinweise

Dieser Export gehört **nicht** in die fünfseitige Monatsübersicht.

Geeignete Bereiche:

- erkannte Verstöße
- Hinweise / Auffälligkeiten
- Informationen
- Kategorien
- betroffene Tage
- Werte/Grenzwerte, wenn fachlich korrekt verfügbar

Visuell:

- Rot = Verstoß
- Orange = Hinweis/Prüfbedarf
- Grau/Blau = Information
- echte Kreise 1:1
- keine fehlerhaften Icons

Der Bericht ist informativ und darf nicht als endgültige rechtliche Prüfung ausgegeben werden.
