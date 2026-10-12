> **Produktentscheidung 12.10.2026 – historische Test-/Checkpointgrenzen erhalten.** Damalige Testschritte, Messwerte, Rückmeldungen und Freigabegrenzen bleiben erhalten. Erwähnte Einrichtung/Navigation/frühere Fachpläne sind kein zukünftiger Auftrag. Spesen, Lohn/Zuschläge, Abrechnung, Heimatstandort, Heimkehr/Übernachtung und PDF-Abrechnung vollständig gestrichen. Ganze Oberfläche/Einrichtung erst später neu konzipieren; bestehender Import und bestätigte Daten unverändert. Verbindlich ist die [neue Produktentscheidung](PRODUCT_DECISION_20261012.md); sie hat Vorrang vor früheren Fach-, Design- und Navigationsvorgaben.

# Zertifikatsdateien – gebündeltes Testupdate

Nur die neue Update-APK über die vorhandene **FahrerTagebuch Test** installieren. Nicht deinstallieren und keine Daten löschen. Basis ist das von dir bestätigte PR58-Testupdate40117/Schema18; der neue Schritt hat40118, dasselbe Testpaket, dieselbe feste Signatur und unverändert Schema18. Paket erst nach dem beauftragten Arbeitsblock11.10.2026 08:36:13–10:36:13 Europe/Berlin und vollständig grüner CI ausgeben.

1. Vorhandenes Profil und alte Übernahmen öffnen: bisherige Daten müssen erhalten bleiben. Eine vor dem Zertifikatsupdate gespeicherte Übernahme darf keine Zertifikate aus späteren Lesevorgängen anzeigen.
2. Eigene Karte am bisherigen USB-Leser automatisch einlesen lassen. In **Fahrerkarte → Details → Zertifikate** die einzelnen Arten öffnen: Kartenzertifikat, Signaturzertifikat, CA-Zertifikat und Link-Zertifikat. Vorhandene Angaben bzw. leere/fehlende Dateistände prüfen. Gen1 unterstützt nur Karten-/CA-Zertifikat; die bisherige Karte ist Gen2V2. Eine fehlende Quelle oder leere Datei bestätigt keinen nichtleeren Datensatz.
3. Referenzen zunächst verborgen, bewusst anzeigen und wieder verbergen. Zertifikatsart wechseln und zurückgehen: Referenzen erneut verborgen. Keine vollständigen Referenzen, Kennungen oder Rohdaten in die Rückmeldung kopieren.
4. Karte entnehmen, App vollständig schließen und ohne Leser wieder öffnen. Dieselbe Übernahme auswählen: Zertifikatsangaben bzw. Leer-/Fehlstatus bleiben erhalten, Referenzen wieder verborgen. Eine ältere Übernahme zeigt weiterhin ausschließlich ihren damaligen Stand.
5. Karte erneut einlesen und beide Lesevorgänge getrennt im Importverlauf öffnen. Frühere Übernahme unverändert; Lesen bleibt automatisch, ohne extra Speichern oder erneute Bestätigung einer bereits gebundenen eigenen Karte.

Die Anzeige beschreibt aufgezeichnete Zertifikatsangaben. **Signatur und Vertrauenskette sind noch nicht geprüft.** Zeitangaben sind der Zeitraum laut Datei, kein bestätigter Gültigkeitsstatus. Generation1 benötigt zur weiteren Dekodierung eine gesonderte RSA-Nachrichtenrückgewinnung. Der Schritt schreibt nichts auf die Fahrerkarte.

Diese Anleitung ist vorbereitet und keine Gerätebestätigung. Erst deine spätere Rückmeldung dokumentiert den tatsächlich geprüften Ablauf. Neue Importänderung vorher nicht mergen.
