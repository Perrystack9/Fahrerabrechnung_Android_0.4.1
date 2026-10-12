Aktueller geprüfter Folgecheckpoint12.10.2026: vollständige CI38204376573 an Produktkopf f1f92d69fff37eeaec5740b399d7df452b436098 in allen vier Jobs erfolgreich; lokale Kotlin2.4.20-Reader-Prüfung mit20Tests ebenfalls bestanden. Die neue API bleibt intern; normale Batch-Speicherung/Migration/Verlaufsansichten noch offen. [Genauer Übergabestand](CARD_IMPORT_CONTINUATION_CHECKPOINT_20261012.md) · [Festgelegte Speicherintegration](CARD_APPLICATION_STORAGE_DESIGN.md) · [Sicherheitsabgleich](CARD_IMPORT_SECURITY_RESEARCH.md). Spätere Änderungen brauchen eigene Hardwarefreigabe; kein neues Testpaket ausgegeben.

---

# Beide Kartenanwendungen – Modell und Reader-Fundament

Stand 12.10.2026. Eigenständiger offener Folgeentwurf nach dem bestätigten PR62/40121, keine Gerätefreigabe dieses neuen Codes. Ausgangs-Main `ace6b7bca4fb1ccb105f32f6d5de5be30bc04c4b`, Tree `a0101ba452bcebd75eaf34b44cd9518a2ac4f1d9`; PR62-Merge `26a9f14c34523fa4ba884d8dd9891c24e1736ab4` hat exakt den geprüften Produktbaum `11e79e492b381f9d515bee796ffea30da3099592`. Gerätebestätigung40121 auf Main gelesen; Umfang bleibt Wiederlesung/Anzeige der beiden MF-Quellen11/3Bytes, keine zusätzliche Update-/Historien-/Neustartbehauptung.

## Implementierter abgegrenzter Schritt

Internes unveränderliches `CardApplicationReadBatch` für einen physischen Lesevorgang, mit genau einem Versuch je festem AID. `GEN2V2` ist eine Struktur des Gen2-DF und kein drittes AID. MF-Originale werden einmal vor AID-Auswahl erfasst und getrennt am Envelope gehalten. Weder MF-Werte noch Quellen anderer Anwendungen werden in die Anwendungsmaps eingefügt.

`DriverCardReader.readApplications()` liest beide DFs in einer eingeschalteten Sitzung. Jede Anwendung liest ihr eigenes0501,0520 und ggf.0525, ihre eigenen Kapazitäten, Zertifikate und Fachdaten. Ein fehlendes AID bleibt `ABSENT`; ein fehlerhaftes0501/0520 ist kein fehlendes AID und bekommt seinen eigenen Fehlerzustand. Ein einzelner EF-Fehler entfernt weder die übrigen Quellen noch den anderen Anwendungssatz. Transportverlust/Timeout/Kartenentnahme und Coroutine-Abbruch propagieren; kein erfolgreicher Batch nach Abbruch zwischen den Anwendungen. Nur vorhandene SELECT/READ-Kommandos, unverändert begrenzte GET RESPONSE/Le-Korrektur; kein0540, PSO, UPDATE, Authentisierung oder erweitertes APDU.

Originalbytes und Maps sind auf Eingabe und Ausgabe eingefroren. Identitätszustände unterscheiden keine Fahreridentität, fehlenden vollständigen0520-Originalbeleg, Übereinstimmung und Widerspruch. Übereinstimmung verlangt exakt gleiche Kartenfelder und Inhaberfelder beider lesbarer DFs sowie jeweils einen gültigen vollständigen0520-Beleg. Sprach-/Behördenabweichungen werden konservativ ebenfalls gesperrt; keine unsichere Zusammenführung. Ein gemischter Fahrer-/Nichtfahrer-Anwendungssatz ist widersprüchlich. `AGREED` bedeutet weder Eigentumsbestätigung noch kryptografisches Vertrauen. Erste/unklare Ersatzkarten benötigen unverändert Bestätigung; Fremdkarten sind nicht persistierbar.

**Die neue API ist intern und noch nicht am normalen Importkoordinator angeschlossen.** Der normale Ablauf bleibt beim bisherigen Gen2-bevorzugten Ein-Anwendungs-Pfad. Beide APIs teilen die unveränderte Dateiauswertung; der neue Pfad behandelt schwerwiegende Transportfehler strikt. Schema18, Entities, Migrationen, Exporte, APK-Version40121 und Paketsignatur bleiben unverändert. Kein Ersatz des ausgelieferten40121-Pakets und keine neue Test-ZIP dieses Zwischenschritts.

20 neue JVM-Testmethoden prüfen beide DFs mit unterschiedlichen Kapazitäten/Originaldateien/Zertifikaten, einen oder keinen DF, getrennte EF-/Identitätsfehler, Widerspruch, fehlende Endprobe, Abbruch zwischen DFs, Transportverlust, MF-Abgrenzung, keine0540-/Schreibbefehle, Defensive-Copies und die unveränderte Gen2-Präferenz des bisherigen Readers. Die bestehenden SQLCipher-/Compose-Regressionen bleiben erforderlich, weil der bisherige Reader dieselbe extrahierte Auswertung benutzt. Keine neue Schema-/UI-Funktion durch diese alten Regressionen behaupten.

## Festgelegte nächste Persistenzintegration

Noch nicht implementiert, vor Aktivierung der neuen Reader-API erforderlich:

1. Additive Batch-/Anwendungszuordnung: ein Parent-Lesevorgang für die bereits bestätigte physische DriverCard, zwei explizite AID-Versuche mit Anwendungsgeneration und eigenen Child-ImportSession-/Run-Verweisen; MF-Quellen am Parent. Keine Änderung des physischen Kartenschlüssels, keine zweite scheinbare Karte fürGen1.
2. Parent, sämtliche Child-Sitzungen, Quellen und Statusbelege in **einer** SQLCipher-Transaktion. Bindung/Identitätsprüfung vor jeder Mutation. Keine persistierte halbe Gruppe nach Abbruch oder zweitem Identitätswiderspruch. Ein unabhängiger Dateifehler darf als expliziter Teilstatus gespeichert werden, wenn die Kartenidentität ausreichend belegt ist.
3. Eigene versionierte Parent-Request-Fingerprints mit festen AID-/Status-/Generations-/MF-/Quellenfeldern. Child-Requests stabil aus Parent plus AID ableiten. Bestehende Ein-Anwendungs-Requests und Fingerprints bytegleich lassen. Alte Sitzungen nicht nachbefüllen oder an neue Batches hängen.
4. Anwendungs-Generation aus dem jeweiligen Sitzungsbeleg lesen, niemals aus dem aktuell gebundenen physischen Identity-Datensatz: der bestehende Run-Decoder verwendet derzeit dessen Generation und eignet sich deshalb noch nicht fürGen1-Child-Runs auf einerGen2-Karte.
5. Detail-/Verlaufsansicht öffnet exakt Parent und gewählten Child-Beleg. Kein jüngerer Fallback. Zeiten beider Anwendungen nicht addieren; Tagesdaten explizit aus einer ausgewählten Anwendungsquelle darstellen, bis eine getrennte fachliche Zusammenführung belegbar ist.
6. Migration ausschließlich additiv und mit echten KSP-/Room-Exporten. Keine handgeschriebeneSchema19-Datei. Erst dann höhere APK-Version und Updatepaar von bestätigter40121-Basis mit fester Testsignatur, neue vollständige CI und konkrete Anleitung/Paket für beide Anwendungsansichten, alte Imports sowie Wiederöffnung ohne Leser.

Pflichtfälle der Integration: beide DFs, ein DF/unabhängige Fehler, unterschiedliche Quellen derselben Art, gleiche physische DriverCard-ID, Widerspruch/Fremdkarte/unbestätigte Karte ohne Mutation, atomare Rückrolle, Abbruch, frische Bestätigungslesung, Snapshot während Bestätigungsdialog, gleiche Requests vor/nach Wiederöffnung, unveränderte alte Requests/Quellen, exakte SQLCipher-Wiederöffnung und Compose-Verlaufswechsel.

## Standards und weiterhin offene Grenzen

[Annex IC Appendix2 §4.2/TCS140–147, offizieller JRC-Spiegel](https://dtc.jrc.ec.europa.eu/iot_doc/EU%202016-799-EN.pdf) und [amtliches EUR-Lex-Format](https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02016R0799-20160526): feste Gen1-/Gen2-AIDs und getrennte DFs; DIR ist eine MF-Datei der zweiten Kartengeneration. Die bereits importierte DIR-Auflistung allein belegt keine erfolgreiche Lesung beider Anwendungen. Dieser Entwurf benutzt keine DIR-Werte für beliebige AID-Auswahl.

7F66-Inhaltsdecoder bleibt offen: der bisherige Import speichert Originale und äußere Struktur; für die vollständige Interpretation ist das konkret referenzierte ISO7816-4:2013-Primärformat nötig. Keine aus11Bytes geratenen Puffergrößen, keine automatische erweiterte APDU-Nutzung. Die aktuell auffindbare [ISO-Ausgabe2020](https://www.iso.org/standard/77180.html) ersetzt keinen Nachweis der konkret referenzierten2013-Felder.

Signierte Standarddownloads/DDD, ERCA-/MSCA-Vertrauen und geschützteVU_Configuration0540 bleiben separate nachgelagerte Schritte. Dieser Entwurf erzeugt keine Signaturen, Schlüssel oder Authentisierung und behauptet keine Download-/Zeit-/Routen-/Vertrauensvollständigkeit. Fahrzeug-/Spesen-/Abrechnungsarbeit bleibt zurückgestellt.

Keine Browser/Unteragenten/Bereinigung; Actions-Budget2USD und Stop-usage unverändert. Neue Produktarbeit bleibt offener Entwurf, kein Merge oder Auto-Merge vor eigener ausdrücklicher Gerätefreigabe. Der nächste Agent muss vor Ref-Updates Main, diesen Branch und die endgültigen CI-Belege erneut lesen, keine lokale Snapshot-Historie pushen und diese Fortsetzung nicht doppelt beginnen.

