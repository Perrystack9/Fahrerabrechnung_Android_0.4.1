# Gerätebestätigung40121 – Wiederlesung und Kommunikationsangaben

Tobi meldete am **12.10.2026,06:15 Europe/Berlin** nach dem erneuten Einstecken der Karte: „So sieht es aus nach dem erneuten einstecken.. Ich denke das passt“. Die positive Rückmeldung bezieht sich im aktuellen Testablauf auf das unmittelbar zuvor bereitgestellte PR62-Testupdate40121. Im Zusammenhang mit dem vorherigen ausdrücklichen Auftrag, nach erfolgreicher Bestätigung PR62 abzuschließen und selbstständig weiterzuarbeiten, wird sie als Gerätefreigabe für40121 im konkret beobachteten Umfang dokumentiert.

## Tatsächlich beobachtet und bestätigt

| Prüfung | Belegter Umfang |
|---|---|
| Erneutes Einstecken / Wiederlesung | Positive Nutzerrückmeldung; Ansicht der ausgewählten gespeicherten Übernahme geöffnet |
| ATR/INFO-Pufferdatei |11Bytes; Pufferinformation als aufgezeichnet angezeigt |
| Extended_Length-Protokolldatei |3Bytes; erweiterte Länge fürT=1 aufgezeichnet, fürT=0 nicht aufgezeichnet |
| Auswertung | Originaldateien gespeichert und sichtbar; Puffergrößen ausdrücklich noch nicht ausgewertet |

Damit liegt ein realer positiver Fall für beide neuen bedingten MF-Quellen vor. Das Bild belegt keine tatsächliche Nutzung erweiterter APDUs, keine Puffergrößen, keine vollständige Kartenabdeckung und kein kryptografisches Vertrauen. Zusätzliche Prüfungen des Updates/Altdatenbestands, eines App-Neustarts ohne Leser, alter Imports oder weiterer Hardware wurden in dieser Rückmeldung nicht genannt und werden nicht als40121-Gerätetest behauptet. Der frühere40120-Test bleibt ein eigenständiger bestätigter Stand.

## Eindeutige Zuordnung und geprüfter Produktkopf

- Testpaket `FahrerTagebuch-PR62-Test-Update.zip`, Version40121/Schema18, Update über40120, Anwendung `de.fahrerabrechnung.app.devicetest`.
- Quellcommit `efbf49cdf92bd5e5ddb2e042b3e14e9372169756`, Tree `11e79e492b381f9d515bee796ffea30da3099592`.
- ZIP SHA256 `a12529904cc08b0bf06f90fb7a453a6c6db4bd833fb83bbd700f2bb5b52a1302`; APK SHA256 `d4dafa0a8860830bd553585c03d04426b3f1ccee89b3f42ccafc17af4a8d096a`.
- Zertifikat SHA256 `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58`; lokale APK-v2-Signatur/Inhaltsdigest-/Manifest-/Prüfsummenprüfung bestanden.
- Vollständige CI38172596871 und Paketlauf38201601458 erneut erfolgreich geprüft; PR62-Kopf unverändert, vorheriger Main f2984027643a27ea55dc44c10fae9cdf6d115938.

[Auslieferungsnachweis](PR62_DEVICE_DELIVERY_40121.md). Der Gerätefall wird textlich dokumentiert; kein privates Kartenabbild oder Screenshot in GitHub veröffentlicht.

## Abschluss und selbstständige Fortsetzung

PR62 wurde unverändert über Merge-Commit **26a9f14c34523fa4ba884d8dd9891c24e1736ab4** abgeschlossen. Eltern: bisheriger Main f2984027643a27ea55dc44c10fae9cdf6d115938 und geprüfter Produktkopfefbf49cdf92bd5e5ddb2e042b3e14e9372169756. Merge-Tree11e79e492b381f9d515bee796ffea30da3099592 bytegleich zum CI-/Gerätekopf. Die neue Gerätebasis ist40121/Schema18 mit dem oben begrenzten positiven Wiederlese-/Anzeigeumfang. Der Merge löst die bereits eingerichtete eigenständige Produktfortsetzung aus; Abschlussagent übernimmt ausschließlich Markdown-Dokumentation nach Main.

Die bereits eingerichtete automatische Fortsetzung reagiert auf den Merge von PR62 und prüft zusätzlich diese dokumentierte40121-Gerätefreigabe. Sie übernimmt die Produktfortsetzung: getrennte Anwendungen derselben Gen2-Karte, anschließend belegte Pufferinhalts-/Download-/Vertrauens-/geschützte Verfahren. Keine zweite parallele Produktimplementierung durch den abschließenden Agenten. Dieser Agent sichert ausschließlich Geräte-/Merge-Dokumentation; die Fortsetzung prüft vor Produktpatches den jeweils aktuellen Main und dokumentiert ihren eigenen Stand.

Spätere Änderungen sind dadurch nicht hardwarebestätigt. Neue Import-/Schemaentwürfe brauchen eigene vollständige CI und eigene ausdrückliche Gerätefreigaben. Keine erfundenen Quelldaten, keine Fremdkartenpersistenz, kein Backfill oder jüngerer Fallback. Kein Browser, keine Unteragenten, keine Bereinigung/Löschung; Actions-Budget2USD unverändert.
