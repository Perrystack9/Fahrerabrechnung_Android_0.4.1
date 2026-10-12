> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

Aktuelle positive Gerätebestätigung12.10.2026 für den Wiederlese-/Anzeigefall: beide Dateien11/3Bytes, T=1 aufgezeichnet. [Exakter Umfang und Grenzen](DEVICE_CONFIRMATION_40121.md). Die folgenden Schritte dokumentieren die ausgegebene ursprüngliche Anleitung; nicht genannte Prüfungen nicht als bestanden behaupten.

---

# Gerätetest PR62 – Version40121

**Testupdate40121 ist bereit; die Gerätebestätigung steht noch aus.** Version40121, Schema 18, gleiches Testpaket und derselbe feste Testschlüssel. Bereits ausgegebenes Testupdate PR60/40120 bleibt unverändert; dessen [Gerätetest ist jetzt bestanden](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/blob/main/docs/DEVICE_CONFIRMATION_40120.md). Die neue Geräte-/Merge-Sperre gilt für40121 und spätere Änderungen.

Alle vier Prüfungen der vollständigen CI38172596871 am unveränderten Quellkopfefbf49c und der Paketbau38201601458 sind bestanden. APK-v2-Signatur, Inhaltsdigest, Paketkennung, Version40121 und Prüfsummen wurden zusätzlich lokal geprüft. Als Update über die bestehende FahrerTagebuch-Test-App installieren; nicht deinstallieren oder Daten löschen. Die bestätigte Gerätebasis ist jetzt PR60/40120. Das Updatepaar40120→40121 ist in CI geprüft;40121 benötigt anschließend seine eigene ausdrückliche Gerätebestätigung.

1. Nach dem Update eigenes Profil und bisherige Übernahmen prüfen; alte Übernahmen dürfen keine neuen Kommunikationsquellen erhalten.
2. Eigene Karte im bisherigen Galaxy-S23-/Android-16-/CCID-Kontext neu lesen. Bisherige Quellen müssen unabhängig weiter funktionieren. Die beiden neuen EFs werden nur bei eindeutiger ATR-Ankündigung versucht. Fehlende optionale Angaben sind kein Ersatz für einen realen Positivtest; angekündigte, aber fehlende/ungültige Daten müssen separat als fehlgeschlagen erscheinen.
3. Neue Übernahme → Fahrerkarten-Details → Kommunikationsangaben öffnen. Bei tatsächlich gelesenen Dateien müssen Dateigrößen und bekannte T=0/T=1-Angaben angezeigt werden. Leere Datei ist von fehlender historischer Quelle zu unterscheiden. Bei unbekanntem Protokollcode darf keine Unterstützung erfunden werden; ATR/INFO-Puffergrößen werden nicht interpretiert.
4. App vollständig schließen und ohne Leser wieder öffnen. Genau diese Übernahme erneut öffnen, dann eine ältere auswählen. Keine jüngere Quelle in der alten Übernahme. Kartenlesung wiederholen: unveränderte Dateien deduplizieren, getrennte Übernahmen erhalten; Erstbestätigung nicht erneut verlangen.

In der Rückmeldung Installationsversion und beobachteten Umfang nennen. Falls die Karte erweiterte Länge nicht eindeutig ankündigt, deckt der Test nur Update, bisherigen Import und fehlende optionale Angaben ab; kein realer ATR/INFO-/Extended_Length-Positivfall ist dann bestätigt. Keine anderen Karten/Leser, VU-Konfiguration, signierten Downloads oder Kryptoprüfungen aus dieser Rückmeldung ableiten.
