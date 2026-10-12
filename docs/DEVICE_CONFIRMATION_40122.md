# Gerätebestätigung 40122 / PR65

Tobi bestätigt am **12.10.2026 um 18:57 Europe/Berlin**:

> Version 40122 installiert, alte Daten erhalten, sichtbare Gen1-/Gen2-Zustände, passende Details und Wiederöffnung ohne Leser erfolgreich

Damit ist der ausgegebene PR65-Teststand40122/Schema19 für den bestätigten Umfang freigegeben. Bestätigt sind Installation des Updates, Erhalt alter Daten, sichtbare Gen1-/Gen2-Zustände mit passenden Details und erfolgreiche Wiederöffnung ohne angeschlossenen Leser. Die Rückmeldung enthält keine separaten Bytezahlen, Zertifikats-/Signaturprüfungen, besondere Fehlerfälle oder ausdrücklich berichtete erneute Einsteck-/Deduplizierungsprüfung. Die allgemeine Wiederöffnung wird nicht um zusätzliche ungenannte Einzeltests erweitert.

## Exakt zugehöriger geprüfter Stand

| Beleg | Wert |
|---|---|
| Produktkopf | `62fc0d299f87b26109a98ff11e94c9202a27ae01` |
| Produktbaum / getesteter Merge-Baum | `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` |
| Vollständige CI | [38243540172, Versuch4](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38243540172/attempts/4), alle vier Pflichtjobs erfolgreich |
| Testversion / Schema | 40122 / 19 |
| Paket | `de.fahrerabrechnung.app.devicetest` |
| Testzertifikat SHA256 | `9ffc4c280b390a1699567f5cc381dfd7a0e221ef9766af6fe937f6500ee9ad58` |
| Tatsächlich ausgegebene APK SHA256 | `c6ccf05b04583e9e80ac84aa8c4743be8b47be383bd7b222e13638702305542c` |
| Ausgegebene ZIP SHA256 | `d3f1750b41029ed9aaeac7e075a0ef648402aa4bc7adfa889a911b45c7169193` |
| Produktmanifest | 302 Blobs, SHA256 `72f9a96d655651389ef993efd80c49e86e07bc57bc4e32a01b126d979560afbb` |

[Auslieferung und technische Prüfungen](PR65_DEVICE_DELIVERY_40122.md) · [Sicherungs-/CI-Verlauf](CARD_MULTI_APPLICATION_CHECKPOINT_40122.md).

PR65 wurde nach erneutem Abgleich von Kopf, allen vier grünen Pflichtjobs und Test-Merge-Baum unverändert abgeschlossen: **Mergecommit `21edb80ce46c0202204bb74d83486c634b3c90c6`**, Parents `ace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b` und `62fc0d299f87b26109a98ff11e94c9202a27ae01`. Der Mergebaum `6f71f4ecc0f4b7a8976891d3ebeb726ad9281d94` ist exakt gleich dem bestätigten Teststand. Main jetzt40122/Schema19. Der automatisch gestartete autoritative Main-Lauf [38281475535](https://github.com/Perrystack9/Fahrerabrechnung_Android_0.4.1/actions/runs/38281475535) wird separat bis zu seinem tatsächlichen Abschluss geprüft; die Mergefreigabe stützt sich auf die bereits vollständig grüne PR-CI und Tobis eigene Gerätebestätigung. Diese Bestätigung autorisiert keine ungetesteten späteren Import-, Schema-, Signatur-, Schreib- oder Sicherheitsänderungen. Nachfolgende Hardware-/Merge-Sperren bleiben für ihren eigenen Stand bestehen.
