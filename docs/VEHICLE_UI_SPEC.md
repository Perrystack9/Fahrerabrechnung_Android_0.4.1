# FahrerTagebuch · verbindliche Fahrzeugdarstellung

**Stand: 28.09.2026.** Diese Spezifikation konkretisiert die Fahrzeugdarstellung aus [CURRENT_RULES.md](CURRENT_RULES.md). Sie ist für die spätere Platzierung der Fahrzeugdaten in Tagesübersicht, Monatsübersicht und Fahrzeughistorie als **verbindliche Designvorgabe** zu behandeln. Sie ist kein Auftrag, die Funktion vor der dafür vorgesehenen Entwicklungsphase vorzuziehen.

## Grundsatz

Die Fahrzeugdarstellung soll sich **1:1 an der bestehenden FahrerTagebuch-Designsprache** orientieren:

- vorhandenes Material-3-Layout weiterverwenden;
- helle Standardoberfläche wie im aktuellen Projekt;
- Navy `#0F2D4A` als Primärfarbe;
- Petrol `#008B8B` als Akzent;
- Weiß/Hellgrau als Flächen;
- vorhandene `SectionCard`-/Typography-/Spacing-Komponenten verwenden;
- keine neue Hauptnavigation und keine zusätzlichen Haupttabs nur für Fahrzeuge;
- Tages-/Monatsdarstellung gehört in den bestehenden Bereich **Kalender**;
- Fahrzeughistorie gehört später unter **Auswertung**;
- Fahrerkarte bleibt Datenquelle/Import und wird nicht zur dauerhaften Fahrzeug-Hauptansicht.

## Daten, die an einem erkannten Fahrzeug sichtbar sein sollen

Wenn belastbar vorhanden:

1. **Marke**
2. **Modell**
3. **Antriebsart**
4. **Baureihe**
5. Kennzeichen
6. Zulassungsstaat
7. VIN
8. Nutzungszeitraum bzw. konkrete Nutzung
9. Tacho-Start-/Endstand aus Quelldaten

Die ersten vier Punkte sind die primären Fahrzeugdetails und sollen optisch zusammen als Block erscheinen.

## Beispiel für die aktuell getestete VIN

Für die aktuelle Beispielzuordnung:

- VIN: `W1T98340410834416`
- Marke: **Mercedes-Benz**
- Modell: **eActros 600**
- Antriebsart: **Elektro**
- Baureihe: **983.404**
- Visual: passendes echtes eActros-600-Modellbild
- Zusatz: kleines **Elektro-/Blitz-Badge**

Die App darf diese Werte nur dann genauso anzeigen, wenn die spätere Fahrzeugzuordnung sie belastbar bestätigt. Das Beispiel definiert das gewünschte Layout, nicht die Quelle der Fahrzeugdekodierung.

## Tagesübersicht · Fahrzeugdetails

Im ausgewählten Kalendertag soll oberhalb bzw. unmittelbar bei der Tageschronik eine Fahrzeugkarte erscheinen.

### Aufbau

**Kartenkopf**

`<Kennzeichen> · Staat <Code>`

Beispiel:

`NÖ NR 6600 · Staat 13`

**Darunter zweispaltig:**

Links:
- freigestelltes bzw. ruhig dargestelltes LKW-Modellbild;
- Bild vollständig sichtbar, Seitenverhältnis erhalten;
- keine dekorativen Hintergründe oder künstlichen Straßen-/Szenenbilder;
- Elektro-Badge oben rechts am/nahe dem Fahrzeugbild.

Rechts:
- `Marke: Mercedes-Benz`
- `Modell: eActros 600`
- `Antriebsart: Elektro`
- `Baureihe: 983.404`
- `Kennzeichen: NÖ NR 6600`
- `VIN: W1T98340410834416`

**Darunter als eigene kompakte Nutzungszeile:**

Primär:
`02:00 – 06:38 · NÖ NR 6600`

Sekundär:
`Tacho 135521 → 135662 km`

Rechts ein Chevron/Öffnen-Symbol, wenn die Nutzung antippbar ist.

Bei mehreren Fahrzeugen an einem Tag werden die Nutzungen zeitlich korrekt getrennt; das aktuell ausgewählte bzw. für einen Timeline-Abschnitt relevante Fahrzeug kann hervorgehoben werden.

## Monatsübersicht · Fahrzeuge im Monat

Unter der Monatsansicht soll ein Bereich **„Fahrzeuge im Monat“** erscheinen.

### Erweiterte Fahrzeugkarte

Kartenkopf:

`<Kennzeichen> · Staat <Code>`

Links:
- dasselbe Modellbild wie in der Tagesansicht;
- keine abweichende Visualisierung desselben Fahrzeugs;
- ggf. Elektro-Badge.

Rechts:
- Marke
- Modell
- Antriebsart
- Baureihe
- Anzahl der Nutzungen
- erste Nutzung
- letzte belegte Zeit
- VIN

Unterhalb des Detailblocks eine kompakte Kennzahlenzeile, beispielsweise:

- **Nutzungstage**
- **km aus Quelldaten**

Nur tatsächlich belastbar berechenbare Werte anzeigen. Kilometerstände aus der Karte sind Quellwerte und dürfen nicht automatisch als Tages-/Monatsstrecke ausgegeben werden, solange dafür keine freigegebene Berechnungslogik existiert.

### Eingeklappte weitere Fahrzeuge

Weitere Fahrzeuge dürfen platzsparend als kompakte Karten dargestellt werden:

- kleines Modellbild oder Fallback;
- Kennzeichen + Staat;
- Marke;
- Modell;
- Antriebsart;
- Anzahl Nutzungen;
- Chevron zum Aufklappen.

Beim Aufklappen gilt dieselbe Detailstruktur wie oben.

## Fahrzeughistorie

Unter **Auswertung → Fahrzeuge/Fahrzeughistorie** soll dieselbe visuelle Identität weiterverwendet werden.

Der Nutzer soll ein Fahrzeug eindeutig wiedererkennen durch:

- immer dasselbe Fahrzeugbild/Logo-Fallback;
- Kennzeichen + Staat;
- Marke + Modell;
- Antriebsart + Baureihe;
- VIN, falls belastbar vorhanden.

Darunter chronologische Nutzungen mit:

- Start/Ende;
- Kennzeichen;
- Tacho-Start/Tacho-Ende;
- später ggf. Dauer und weitere belastbare Kennzahlen.

## Visual- und Fallbacklogik

Die Bildwahl ist deterministisch:

1. **Exaktes Modell sicher erkannt** → passendes echtes Modellbild.
2. **Hersteller sicher, Modell nicht sicher** → Herstellerlogo.
3. **Hersteller ebenfalls unklar** → neutrales LKW-Standardsymbol.

Ein konkretes Modellbild darf niemals geraten werden.

### Produktionsassets

- **Keine KI-generierten LKW-Bilder** in der produktiven App.
- Reale/produktbezogene Bilder nur aus kuratierten und rechtlich nutzbaren Quellen.
- Keine Google-Bildersuche als Laufzeitquelle.
- Kein Hotlinking.
- Bilder lokal bündeln oder kontrolliert laden/cachen.
- Assets für kleine Darstellung optimieren, bevorzugt WebP/AVIF oder geeignetes Android-Ressourcenformat.
- Modellbilder in möglichst einheitlicher Perspektive und Größe aufbereiten.
- Logos und Bilder dürfen nicht verzerrt, gestreckt oder unproportional beschnitten werden.
- Rechte/Lizenzen vor Auslieferung klären.

## Antriebs-Badge

Bei **Elektro**:

- kleines Blitzsymbol;
- Petrol-Akzent;
- kompakt, nicht dominanter als Marke/Modell;
- direkt am Fahrzeugbild oder unmittelbar daneben;
- zusätzlich bleibt der Text `Antriebsart: Elektro` sichtbar; das Badge ersetzt den Text nicht.

Später können analog eigene Badges für LNG/CNG/H₂ ergänzt werden, aber nur bei belastbarer Zuordnung.

## Größen- und Layout-Richtung

Keine Pixel-1:1-Hardcodes außerhalb des vorhandenen Designsystems. Work soll das Mockup innerhalb der bestehenden responsiven Compose-Komponenten nachbilden.

Zielverhältnis auf typischer Smartphonebreite:

- Fahrzeugvisual ungefähr **30–35 %** der inneren Kartenbreite;
- Textblock ungefähr **65–70 %**;
- Modellbild groß genug, dass die Baureihe visuell erkennbar bleibt;
- Fahrzeugkarte über volle verfügbare Inhaltsbreite;
- kompakte, ruhige Abstände entsprechend den bestehenden FahrerTagebuch-Spacing-Tokens;
- kein horizontaler Scrollzwang für die Fahrzeugdetails.

## Nicht tun

- keine zusätzliche Hauptnavigation „Fahrzeuge“;
- keine eigenständige Karten-/Routenansicht nur wegen der Fahrzeugdarstellung;
- keine erfundenen Kilometer;
- kein geratenes Modell;
- keine KI-Modellbilder;
- keine unterschiedlichen Bilder für dasselbe erkannte Fahrzeug in Tages-/Monats-/Historienansicht;
- keine ungeklärten Webbilder ungeprüft in die App übernehmen;
- keine Abweichung vom bestehenden FahrerTagebuch-Branding zugunsten eines generischen Dashboard-Stils.

## Abnahmekriterium

Wenn die Fahrzeuginformationen später an ihren endgültigen Platz verschoben werden, soll ein Nutzer bei derselben Fahrzeug-VIN in **Tagesübersicht**, **Monatsübersicht** und **Fahrzeughistorie** dieselbe visuelle Identität sehen.

Für die Beispiel-VIN bedeutet das bei bestätigter Zuordnung:

**Mercedes-Benz · eActros 600 · Elektro · Baureihe 983.404**

mit echtem eActros-600-Modellbild und kleinem Elektro-Badge.
