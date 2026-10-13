# Gerätebestätigung 40123 / vollständige 0504-/0505-Originale

**Bestätigung von Tobi am 13.10.2026, 09:04 Europe/Berlin.** Antwort auf die unmittelbar zuvor angekündigte Kernprüfliste: „Passt alles ohne Probleme 😅“, ergänzt durch zwei sichtbare Screenshots. Dies ist der konkrete Geräteumfang für PR #70, keine allgemeine Karten-/Kryptofreigabe.

## Exakte Standbindung

| Beleg | Stand |
|---|---|
| Testversion / Datenbank | 40123 / Room-Schema 19 |
| Geprüfter Produktkopf | `659f31c510752e5a06bf5f45bf2c93f4aff37031` |
| Vollständige Android-CI | [38335263243, Versuch 2](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38335263243/attempts/2), erfolgreich |
| Gezielt erzeugtes Gerätepaket | [38358288875](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38358288875), erfolgreich; Artefaktname enthält den Produktkopf |
| Installation | Nutzer meldete Paket-Workflow durchgeführt und APK installiert, vor der Kernprüfliste |
| Produktmerge PR #70 | `e783ad364b1692d8633c5e4b8846112adb256be6`, identischer vollständiger Git-Tree `328b6b344c9376cfda0d8ab77d896b53bf944014` |

Workflow-Infrastruktur lief auf damaligem main; das separat ausgecheckte Produkt und der Artefaktname binden Kopf `659f31c510752e5a06bf5f45bf2c93f4aff37031`. Nicht den Infrastruktur-HEAD mit dem APK-Quellkopf verwechseln. Paketprüfung enthält erfolgreiche Quell-CI-, Build-, stabile Signatur-/Paket-/Versionsprüfung; kein Code nach dieser Prüfung geändert.

## Bestätigter Kernumfang

Die Nutzerantwort bestätigt die unmittelbar zuvor angefragten Kernpunkte: Update/Erhalt alter Daten und Profil, eigener automatischer Import, separate vorhandene GEN1-/GEN2-Auswahl, plausible Kartentage/Fahrzeugnutzungen, erneute Öffnung ohne Leser und Wechsel zwischen altem/neuem Import ohne jüngeren Quellenersatz. Installation und Geräteergebnisse sind Nutzerangaben; die Screenshots stützen konkret die separate Anwendung und Originalgrößen.

| Sichtbare Anwendung | Tätigkeiten 0504 | Fahrzeuge 0505 |
|---|---:|---:|
| GEN1 | 13780 Bytes | 6202 Bytes |
| GEN2V2 im Gen2-AID | 13780 Bytes | 9602 Bytes |

Strukturell konsistent: 13776 Ringbytes + vier Pointerbytes; Fahrzeuge je 200 Slots × 31 Bytes (G1) bzw. 48 Bytes (G2/V2) + zwei Pointerbytes. Die Anzeige beweist keine Signatur/Vertrauenskette und ersetzt keine Byteprüfung der gespeicherten Quelle. Keine privaten Karten-/Fahrzeugdetails aus den Screenshots ins Repository übernommen.

## Nicht zusätzlich ableiten

Optionale erneute Einsteck-/Deduplizierungs-/Abbruchprüfungen nicht separat bestätigt; nicht als durchgeführt vermerken. Keine echte unbenutzte/defekte EF, reine GEN2-Karte ohne V2, Fremd-/Ersatzkarte, vollständige Kartenabdeckung, Dateisignatur, DDD-/Standarddownload oder produktive Vertrauenskette am Gerät bestätigt. Synthetische Unit-/SQLCipher-/Compose-Prüfungen bleiben getrennte CI-Belege.

Die eigene CI-/Geräte-Mergegrenze von PR #70 ist im beschriebenen Umfang erfüllt. Neue relevante Änderungen brauchen wieder ihre eigene passende CI und erforderliche Gerätebestätigung.
