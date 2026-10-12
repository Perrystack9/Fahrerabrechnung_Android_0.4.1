# Gerätetest40122 – beide Anwendungen derselben Fahrerkarte

Vorbereitung für PR65, Schema19. **Noch keine Gerätefreigabe und kein ausgeliefertes Paket.** Erst nach vollständiger grüner CI und eigener Signatur-/Prüfsummenprüfung ausgeben. Updatebasis ist das bereits installierte40121/Schema18, feste Testkennung `de.fahrerabrechnung.app.devicetest` und unverändertes Testzertifikat.

1. Update40122 über40121 installieren. Nicht deinstallieren, App-Daten nicht löschen. Vorhandene alte Übernahmen müssen erhalten bleiben.
2. Dieselbe eigene physische Fahrerkarte anschließen. Bekannte Karte übernimmt automatisch; erste/unklare Ersatzkarte braucht weiterhin ausdrückliche eigene Bestätigung mit anschließender frischer Lesung.
3. Im Importverlauf den neuen gemeinsamen Lesevorgang öffnen. Gen1 und Gen2/Gen2V2 müssen getrennt erscheinen, soweit diese Karte beide Anwendungen anbietet. „Nicht vorhanden“ oder ein eigener Lesefehler ist ein eigener Status, kein erfundener zweiter Datensatz.
4. Beide Anwendungsschaltflächen einzeln öffnen. Die Vorschau benennt die ausgewählte Generation; originale Kartenangaben, Kapazitäten und Zertifikate gehören jeweils zu genau dieser Anwendung. Zeitangaben beider Anwendungen werden nicht zusammengerechnet.
5. Gemeinsame Kartenkennung, Chipkennung, Anwendungsverzeichnis und bedingte Kommunikationsdateien kontrollieren. Vorhandene11-/3Byte-Kommunikationsangaben bleiben sichtbar. Fehlende bedingte Dateien sind kein Vollständigkeitsbeweis. Es werden weiterhin keine Puffergrößen geraten oder erweiterten APDUs eingesetzt.
6. Karte und Leser entfernen, App schließen und neu öffnen. Den konkreten gemeinsamen Vorgang im Verlauf erneut öffnen; beide gespeicherten Anwendungen und die MF-Angaben müssen ohne Leser erhalten bleiben.
7. Einen alten40121- oder früheren Ein-Anwendungs-Import öffnen. Seine ursprünglichen Quellen bleiben unverändert; keine nachträglich ergänzte Gen1-Anwendung und keine MF-Daten aus jüngeren Vorgängen.
8. Dieselbe Karte erneut einstecken. Ein weiterer gemeinsamer Lesevorgang darf entstehen; gespeicherte Rohquellen werden dedupliziert. Beide älteren Anwendungsbelege bleiben exakt wiederöffnbar.

Bitte Rückmeldung mit installierter Version, sichtbaren Gen1-/Gen2-Zuständen, Erhalt alter Daten und Wiederöffnung ohne Leser. Keine private Kartennummer, Zertifikatsreferenz oder Kartenrohdaten veröffentlichen. Test verlangt keine Fremdkarte und keine erzwungene Authentisierung.

40121-Bestätigung gilt nicht für diesen neuen Import-/Schema-/UI-Stand. PR65 bleibt bis eigener ausdrücklicher Freigabe offen, ohne Merge oder Auto-Merge. Vollständiger signierter Standarddownload/DDD, Kryptovertrauen und geschützteVU_Configuration0540 sind weiterhin nicht implementiert.
