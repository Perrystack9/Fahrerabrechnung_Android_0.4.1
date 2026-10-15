# M0 – reproduzierbare M1-Eingangs- und Bestandsproben

Stand15.10.2026. **Kein Engine-/Adapter-/Room-/SQLCipher-/UI-/APK-/Gerätetest.** Dieser Nachweis vergleicht vorhandene unveränderte Quellen gegen amtlich hergeleitete Eingaben. Eigene neue M1-Produktabnahmen können erst nach Programmierungsfreigabe und Implementierung stattfinden. Vollständige Einordnung im [Nachweisprotokoll](../VIOLATION_ENGINE_M0_M1_EVIDENCE.md).

## 1. Tatsächliche Ausführung und Quellenidentität

Produktquelle aus `192c0ea3750208cba6ec18560facbd183f28b515`, identisch zum geprüften main-Produktbaum. Java17, vorhandener separater Kotlin2.4.20-Compiler und JUnit4.13.2 aus einer bereits vorhandenen lokalen Prüfumgebung; keine Änderung der Projekttoolchain/Gradle-Abhängigkeiten. Alle Hilfsdateien und Klassen ausschließlich außerhalb des Repositorys. Ganze Parser-/Hilfs-/Testdateien bytegleich kopiert; nur die reine `CardActivityFile`-Deklaration und das reine Fingerprint-Objekt wortgleich aus ihren größeren Quelldateien entnommen. Keine Ersatzimplementierung des Parsers oder eines Room-Stores, keine Hardware und keine realen Karten-/APDU-Daten.

| Originaldatei / Auswahl | SHA256 der ganzen Originaldatei | SHA256 des unveränderten Ausschnitts, falls benutzt |
|---|---|---|
| [BinaryReader.kt](../../data/src/main/kotlin/de/fahrerabrechnung/data/card/BinaryReader.kt) · whole unchanged file | `033383144122ae206b667e948f1c6642753c22d60554f0035c1dd874672847da` | ganze Datei |
| [CardActivityParser.kt](../../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardActivityParser.kt) · whole unchanged file | `03aab023cc142e2b3995c6dce7aa9656a4721d4f9e92b0b6160a12746cf8484d` | ganze Datei |
| [CardDayTimeline.kt](../../data/src/main/kotlin/de/fahrerabrechnung/data/card/CardDayTimeline.kt) · whole unchanged file | `4666ff72aef106bccac26bf2b9053cefc501b26d91ecaa084f6f72650f958aa8` | ganze Datei |
| [CardActivityParserTest.kt](../../data/src/test/kotlin/de/fahrerabrechnung/data/card/CardActivityParserTest.kt) · whole unchanged file | `e6fc2ca63d618c580346428c19bb934944abf171a4812e5e8180f6d55e3f0167` | ganze Datei |
| [DriverCard.kt](../../data/src/main/kotlin/de/fahrerabrechnung/data/card/DriverCard.kt) · verbatim CardActivityFile declaration | `b7d55da00f5e7bbd4d20a156a1f6e83e315d8a46a3a074c4a1582a560076d3d6` | `aea79a1d32f3b04b7ac0d003204bed889f7b31c282bf6cf368291abcfcf615a9` |
| [NormalizedCardActivityStore.kt](../../data/src/main/kotlin/de/fahrerabrechnung/data/card/NormalizedCardActivityStore.kt) · verbatim pure CardActivityDayFingerprint object; no Room store executed | `af158f5b81a5df537108fc658383506d525b38aa5f4103ace8f53a8cc394cd82` | `f8d30883aad26ee4fefccb24c6924b3117770acfbf73b6d40f7395e064e33d70` |

Ergebnis: die drei unveränderten Methoden von `CardActivityParserTest` und die zwölf unten wiedergegebenen zusätzlichen Bestandsproben bestanden, **15 JUnit-Tests insgesamt**. Die Bitprobe umfasst alle32 s/c/p/aa-Kombinationen. Das tatsächliche JUnit-Ausgabeprotokoll:

```text
JUnit version 4.13.2
...............
Time: 0.058

OK (15 tests)
```

Fingerprinterwartung unabhängig in Python aus Big-Endian-/Längen-/UTF- und booleschen Referenzbytes für ausschließlich ASCII-Synthesewerte serialisiert: `c7633c161fe83b5b43fa04676bf627dd447214e79b28c49808c0dce5b88cb0c4`. Der echte unveränderte Kotlin-Fingerprinter liefert denselben Wert. Das beweist diesen deterministischen Vektor, keine neue Kryptofunktion, keine Sicherheitstests oder Android-Persistenzabnahme.

## 2. Tatsächlich ausgeführter Speicherstrukturabgleich

Der vorhandene Compilerexport Room22 wurde ausschließlich in einer neuen SQLite-`:memory:`-Datenbank geladen; Foreign Keys eingeschaltet. Original-DDL/Indizes und beide exakten `DriverActivityDao`-SELECTs benutzt. Synthetische notwendige Elternzeilen, keine Produktdatenbank geöffnet. Dies ist ein Struktur-/Querynachweis mit SQLite3.53.1, **kein Room-Lauf, kein SQLCipher-/Keystore-/Migration-/Reopen-/Rollback-/Gerätenachweis**.

Schema-SHA256 unverändert `e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f`. Tatsächlich alle12 Prüfungen bestanden:

- exact DAO SELECT statements found
- same-minute entries retained in changeIndex order
- same-minute cardInserted values remain distinct
- foreign-card source is not returned
- other import source is not returned
- source day preserves exact source position
- cross-card day reference rejected
- duplicate index rejected
- linked day deletion restricted
- linked raw import deletion restricted
- all inserted references valid
- Room schema unchanged

Die statisch zusätzlich gelesenen Bindungs-/Store-/Pipelinefunktionen und deren Grenzen stehen N13/N14 im Nachweisprotokoll. Statische Lektüre wird nicht als ausgeführter Datenbank-/Importtest ausgegeben.

## 3. Zusätzliche verbindliche M1-Abnahmevektoren MI01–MI24

Alle Zeitpunkte UTC, Basistag07.09.2026. Bei positivem Intervall jeweils gültige eigene Karte/Quelle und konkrete Endbelege vorausgesetzt. Unbekannt ist keine Aktivitätsergänzung. Die G1-Originalseiten55/56 wurden ergänzend gerendert und visuell auf c/p-Spalten und Entnahmenotiz geprüft; dies ist Quellenlektüre, kein neuer Testzählwert.

Die Spalte „jetzt belegt“ nennt nur wirklich ausgeführte Bestandsproben oder Quellenlektüre. **Alle neuen Adaptererwartungen bleiben nach M1-Implementierung auszuführen.** Die51 DT- und7 PF-IDs bleiben unverändert erhalten.

| ID / Bezug | Konkreter Eingang und Soll | Jetzt belegt / späterer M1-Test |
|---|---|---|
| MI01 / DT33, N01 | Alle32 s/c/p/aa-Rohwörter um06:00; aa/Slot/c/p und Minute bytegetreu | Echter Parser bestanden; Adapter muss die unterschiedliche Rechts-/Quellensemantik prüfen. |
| MI02 / DT30, N02 | Eingesetztes WORK bis08:00, Entnahme `31e0`, nächster eindeutiger Wechsel10:00: davor W,08:00–10:00 U; vier aa-Entnahmevarianten ändern U nicht | Parser erhält Rohdaten; U-/Zeitzuordnung späterer Adaptertest. |
| MI03 / DT31, N03 | `21e0` im alten Import und `61e0` mit späterem echten Nachtrag, beide bis10:00: alt U120, neu R120, keine Quelle mutiert | Rohkopien/Fingerprint bewiesen; Herkunfts-/Neubewertungs- und Badge-Vertrag später M1/M5. |
| MI04 / N04 | `7968` bis08:00: p=1/c=1/DRIVING bleibt Rohwort, zulässiger manueller Fahrtbeleg nicht nachgewiesen | Echter Parser erhält Rohmuster; Adapter muss Anomalie/ungeklärt tragen, kein Crew-/Fahrprivileg. |
| MI05 / DT33, N05 |08:00 `11e0`,`31e0`,10:00 eindeutiges WORK; Gegenprobe umgekehrte08:00-Reihenfolge: keine Dauer zwischen den08:00-Punkten,08:00–10:00 unklar, kein Indexgewinner | Beide Reihenfolgen in Parser und exakter SQL-Reihenfolge erhalten; Konflikt-/Abdeckungstest M1. |
| MI06 / N05 | Zwei semantisch identische WORK-Punkte08:00, Folge10:00: kompatibler W120, alle Ursprungsindizes erhalten, keine doppelte Minute | Quellengrenze belegt; Adapterdeterminismus/keine Scheinkonflikte M1. |
| MI07 / N06 | Tagesdatum00:00:01 statt00:00:00: betroffenen unzuverlässig verorteten Record nicht auf einen erfundenen Tag korrigieren | Parserakzeptanz tatsächlich gezeigt; Adapter erkennt Datumsanomalie. |
| MI08 / N06 | Gültige Recordkette mit08.09. vor07.09.: nicht sortierend als sichere Kalenderchronologie ausgeben, betroffene Abdeckung unklar | Parser akzeptiert Ringreihenfolge; Adapterchronologieprüfung M1. |
| MI09 / DT07/08, N08/N09 | Letzter WORK-Punkt10:00; Appimport14:00/24:00/20.09.,0507 vorhanden/leer: kein Schluss bis dahin | Parser/UI-Bestandsprobe zeigt künstlichen UI-Nachlauf840min; Adapter muss offenes Ende wahren. |
| MI10 / N07 | Vollständige gleiche Quelle:07.09.22:00 REST,08.09.00:00 REST,09:00 WORK, alle fünf Endbedingungen:660min zusammenhängende belegte Ruhe; neuer Tag ab09:00 offen | Echter Parser liefert beide Tages-/00:00-/Nachtragsbelege und660min Referenzabstand; rechtliche Endableitung/Verknüpfung im Adapter vor M1-Abnahme testen. |
| MI11 / DT06, N07 | MI10 mit fehlendem08.09., nur09.09.00:00 vorhanden: keine Brücke; außerdem Varianten unvollständiger Folgekopf/andere unbestätigte Quelle/doppeldeutiger00:00-Zustand | Echter Parser erhält die172800s-Datumslücke; End-/Negativ-Adaptertests M1, Kopf-/Quellenvarianten noch nicht ausgeführt. |
| MI12 / N07 | Wie MI10, aber letzter Zustand vor Mitternacht p=1/c=0: historisches Ende schließt nur Unknown, macht keine Ruhe daraus | Echter Parser erhält beide p=1/c=0-Zustände über Mitternacht; Unknown-Endverknüpfung im Adaptertest M1. |
| MI13 / N10 | Ein echter00:00-Wert `0000` versus vollständig genullter Ring: beobachteter REST-Punkt versus gültig leer; weder allein positive Ruhe bis24:00 | Echter Parser bestanden; unterschiedliche Abdeckung M1. |
| MI14 / DT01/02/14, N11–N14 | Gleiches Tagesmaterial anderer Ringplatz: gleiche Tages-ID; andere Karte/c/p/Reihenfolge: andere ID. Quellenabfrage falsche Karte/falsches Original liefert keine Zeile; beide Anwendungen nicht doppelt zählen | Fingerprint/SQL tatsächlich bestanden, Parent-/Child-Herkunft statisch geprüft; echter Snapshot-/Adapter-/Replaytest M1. |
| MI15 / L01/L05 | Gütereinsatz3,5t versus3,501t einschließlich Anhänger im regulären EU-Kontext: >3,5-Schwelle nicht ≥3,5. Fehlende Masse bleibt ungeklärt | Art.2Abs.1a nachgewiesen; M1 muss Fakten/Scopegrund tragen, noch keine Lenkzeitrechnung. |
| MI16 / L01/L05 |30.06./02.07.2026, international/Kabotage,2,5t/2,501t/3,5t, Variante nur national und Variante konkret belegtes Art.3ha: aktuelle Erweiterung nicht rückwirkend oder pauschal | Art.2Abs.1aa/3ha und Wirksamkeitsdatum nachgewiesen; unbestimmte Grenzzeitzone am01.07. bleibt ausdrücklicher Rechtszeitparameter, keine Displayannahme. |
| MI17 / L05/L09 | PASSENGERS0530, Linienkontext fehlt: gezielte wiederverwendbare Frage; Linienweg50km versus50,001km. Kurze Einzelfahrt auf längerer Linie ist keine Ausnahme | Art.2Abs.1b/3a,4n/na und0530-Typ nachgewiesen; Kontext-/Guardtest M1, Berechnung M2–M4. |
| MI18 / L05 | Karte im Fahrzeug allein; EF0522-OOS allein; gültiger Ausnahmebeleg nur für Art.7 in Gebiet/ZeitX; AETR-Mischreise | Keine pauschale Anwendung/Freistellung, Teilumfang respektieren, AETR nicht als EU-Teilreise rechnen; Primärstellen belegt, Guardtest M1. |
| MI19 / L05 | Nicht regulierte kommerzielle Fahrt, konkrete Überführung oder unbekannter Einsatzzweck | Rohfahrt erhalten; erforderliche Arbeitseinordnung getrennt, keine Ruhe aus Nichtanwendbarkeit; Art.6Abs.5/9Abs.2/3, Adapter-/Kontexttest M1. |
| MI20 / DT34, L09 | Zeitlich belegtes p=0/c=1 mit eigener Karte, dann p=1/c=1 und danach Lücke: nur erster Abschnitt Crewbeleg | Keine zweite Karte/pauschale Bestätigung; spätere30-h-/Beifahrerqualifikation nicht in M1 vortäuschen. Wahrheitstabelle belegt, Adaptertest M1. |
| MI21 / DT35, L09 | Fährmarker vorhanden, konkrete Schlafmöglichkeit bzw. reguläre-Wochenruhe-Fahrplan≥8h/Kabine fehlt; unveränderter Reimport | Ausnahmevoraussetzung unbekannt, gezielte Frage nur für betroffene Bewertung; keine allgemeine Unterkunftsfrage/Standardfrage. Art.9 belegt, Kontexttest M1, Anrechnung M3/M4. |
| MI22 / L09 | International zwei Reduzierungen mit bekanntem Arbeitgeber-/Wohnsitzland, aber fehlendem Beginnland einer Ruhe; frühere gültige Antwort im anderen Zeitraum | Fehlenden konkreten Zusammenhang tragen/gezielt klären, keine globale Länderübertragung/Adresse/Heimradius; Art.8Abs.6 belegt, Kontexttest M1. |
| MI23 / DT15/19/21, L01/L09 | Gleiche Quellen/Kontext, nur Displayzone UTC→Berlin→NewYork; danach echte rückwirkend begrenzte Kontextrevision | Nur echte Fakten-/Kontextrevision ändert fachlichen Snapshot; kein Frage-Replay/Originalwechsel aus Zone. Rechts-/Formatfassung nicht aus Importdatum. Vertrag/Primärstellen belegt, Revisionstest M1. |
| MI24 / DT14/23/43, L08/L09 | Ungebundenes Altarchiv; gebundene gültige Quelle ohne Normalisierung; neuere Quelle mit widersprüchlichem bekannten Inhalt; Art.12-Zahlenkandidat ohne weitere Tatsachen | Keine Altbindung/Backfill; vorhandenen Parser-Fallback lesend nutzen; echter Konflikt statt jüngster-gewinnt; keine automatische Art.12-Anerkennung/Schuld. Bestehende Funktionen/Vertragsgrenzen statisch geprüft, reale Adapter-/Auditfälle M1 und Ergebnisdarstellung M5/M6. |

MI01–MI24 ergänzen die bestehenden DT-/LG-/PF-Fälle, ersetzen keine frühere Erwartung und keine Sicherheits-/CI-/Geräteprüfung. Fachlich rechtfertigender Kontext wird nicht aus Bestandsproben erfunden. Datenfälle, die Rechtsfenster voraussetzen, bleiben zusätzlich von den betreffenden L-Nachweisen abhängig.

## 4. Reproduktionsprotokoll der tatsächlichen Hilfsprüfungen

Die folgenden Hilfsprogramme stehen **als Dokumentation der M0-Bestandsprüfung** im Text, nicht als neue Projekt-/Produkt-/CI-Dateien. Ausführung nur außerhalb des Arbeitsbaums. Für eine Wiederholung die drei Umgebungsverzeichnisse `repo`/`work`/`jardir` auf denselben geprüften Quellbaum und einen separaten Scratchbereich richten; keine Produktquellen bearbeiten. Die Java/Kotlin-Probe benötigt die benannten vorhandenen Compiler-/stdlib-/JUnit-/Hamcrest-/Compilerlaufzeit-Jars. Ihre Quellhashes müssen zur Tabelle passen. Ein anderer Compilerlauf ist als neuer Nachweis zu protokollieren.

### Unveränderter Parser/Fingerprint und zwölf zusätzliche synthetische Proben

Tatsächlich ausgeführtes Hilfsprogramm, SHA256 `930705de476ecf3fe1fd26866f1643431118f58479f1ba3b625687a4e990edc4`.

```python
from pathlib import Path
import hashlib,json,os,shutil,struct,subprocess
repo=Path('/workspace/scratch/8d9b62deaae5/FahrerTagebuch-v1-engine-plan')
work=Path('/workspace/scratch/8d9b62deaae5/m0-research/m1-parser-probe');src=work/'src';classes=work/'classes'
src.mkdir(parents=True,exist_ok=True);classes.mkdir(exist_ok=True)
base=repo/'data/src/main/kotlin/de/fahrerabrechnung/data/card';tests=repo/'data/src/test/kotlin/de/fahrerabrechnung/data/card'
inputs=[]
for name in ['BinaryReader.kt','CardActivityParser.kt','CardDayTimeline.kt']:
 b=(base/name).read_bytes();(src/name).write_bytes(b);inputs.append(dict(path=str((base/name).relative_to(repo)),sha256=hashlib.sha256(b).hexdigest(),selection='whole unchanged file'))
name='CardActivityParserTest.kt';b=(tests/name).read_bytes();(src/name).write_bytes(b);inputs.append(dict(path=str((tests/name).relative_to(repo)),sha256=hashlib.sha256(b).hexdigest(),selection='whole unchanged file'))
text=(base/'DriverCard.kt').read_text();fragment='class CardActivityFile'+text.split('class CardActivityFile',1)[1].split('\nclass CardVehiclesFile',1)[0]
(src/'CardActivityFileExtract.kt').write_text('package de.fahrerabrechnung.data.card\n'+fragment+'\n');inputs.append(dict(path=str((base/'DriverCard.kt').relative_to(repo)),file_sha256=hashlib.sha256(text.encode()).hexdigest(),selection='verbatim CardActivityFile declaration',fragment_sha256=hashlib.sha256(fragment.encode()).hexdigest()))
text=(base/'NormalizedCardActivityStore.kt').read_text();fragment='internal object CardActivityDayFingerprint'+text.split('internal object CardActivityDayFingerprint',1)[1].split('\n/** Called inside',1)[0]
(src/'FingerprintExtract.kt').write_text('package de.fahrerabrechnung.data.card\nimport java.io.ByteArrayOutputStream\nimport java.io.DataOutputStream\nimport java.security.MessageDigest\n'+fragment+'\n');inputs.append(dict(path=str((base/'NormalizedCardActivityStore.kt').relative_to(repo)),file_sha256=hashlib.sha256(text.encode()).hexdigest(),selection='verbatim pure CardActivityDayFingerprint object; no Room store executed',fragment_sha256=hashlib.sha256(fragment.encode()).hexdigest()))
# Independently serialised ASCII-only reference to Java DataOutputStream's specified wire format.
def utf(x):
 b=x.encode('ascii');return struct.pack('>H',len(b))+b
payload=utf('driver-activity-day-v1')+utf('C1')+struct.pack('>q',1788739200)+utf('0001')+struct.pack('>ii',0,3)
for minute,activity,c,p in [(0,'REST',False,True),(360,'WORK',False,True),(480,'WORK',False,False)]:
 payload+=struct.pack('>i',minute)+utf(activity)+utf('DRIVER')+bytes([c,p])
expected=hashlib.sha256(payload).hexdigest()
probe=r'''package de.fahrerabrechnung.data.card
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
class M0CardInputProbe {
 private val date=Instant.parse("2026-09-07T00:00:00Z").epochSecond
 private fun record(epoch:Long=date,previous:Int=0,vararg words:Int):ByteArray {
  val b=java.nio.ByteBuffer.allocate(12+2*words.size)
  b.putShort(previous.toShort());b.putShort(b.capacity().toShort());b.putInt(epoch.toInt());b.putShort(1);b.putShort(0)
  words.forEach { b.putShort(it.toShort()) };return b.array()
 }
 private fun parse(vararg words:Int)=CardActivityParser.parse(CardActivityFile(0,0,record(words=words))).single()
 @Test fun all32RawStateCombinationsMatchTheSourceBitPositions() {
  for(s in 0..1)for(c in 0..1)for(p in 0..1)for(a in 0..3){
   val word=s*32768+c*16384+p*8192+a*2048+360
   val got=parse(0,word).changes[1]
   assertEquals(360,got.minuteOfDay);assertEquals(DriverActivity.entries[a],got.activity)
   assertEquals(if(s==0) CardSlot.DRIVER else CardSlot.CO_DRIVER,got.slot)
   assertEquals(c==1,got.crewDriving);assertEquals(p==0,got.cardInserted)
  }
 }
 @Test fun withdrawalRetainsCurrentActivityWithoutPromisingFollowingRest() {
  for(a in 0..3){val got=parse(0,8192+a*2048+480).changes[1]
   assertFalse(got.cardInserted);assertFalse(got.crewDriving);assertEquals(DriverActivity.entries[a],got.activity)}
 }
 @Test fun laterManualEntryChangesRawCopyWithoutMutatingTheOldOne() {
  val old=parse(0,0x21e0,0x1258);val later=parse(0,0x61e0,0x1258)
  assertFalse(old.changes[1].crewDriving);assertTrue(later.changes[1].crewDriving)
  assertFalse(old.changes[1].cardInserted);assertFalse(later.changes[1].cardInserted)
  assertEquals(DriverActivity.REST,old.changes[1].activity);assertEquals(DriverActivity.REST,later.changes[1].activity)
 }
 @Test fun equalMinuteOrderAndCardStatusAreNotCollapsedByTheParser() {
  for(words in listOf(intArrayOf(0,0x11e0,0x31e0,0x1258),intArrayOf(0,0x31e0,0x11e0,0x1258))){
   val got=parse(*words);assertEquals(listOf(0,480,480,600),got.changes.map{it.minuteOfDay})
   assertEquals(words[1]and 0x2000==0,got.changes[1].cardInserted)
   assertEquals(words[2]and 0x2000==0,got.changes[2].cardInserted)
  }
 }
 @Test fun ringOrderIsNotAValidationOfCalendarChronology() {
  val nonMidnight=CardActivityParser.parse(CardActivityFile(0,0,record(date+1,words=intArrayOf(0)))).single()
  assertEquals(date+1,nonMidnight.recordDateUtc.epochSecond)
  val first=record(date+86400,words=intArrayOf(0));val second=record(date,first.size,0)
  val reversed=CardActivityParser.parse(CardActivityFile(0,first.size,first+second))
  assertEquals(listOf(date+86400,date),reversed.map{it.recordDateUtc.epochSecond})
 }
 @Test fun lastPointHasNoEndAndTheUiStillClosesItsOwnPreviewAtMidnight() {
  val day=parse(0,0x1258);assertEquals(listOf(0,600),day.changes.map{it.minuteOfDay})
  val preview=CardDayTimeline.build(listOf(day),ZoneOffset.UTC).single().segments.last()
  assertEquals(Instant.ofEpochSecond(date+86400),preview.end)
  assertEquals(840L,java.time.Duration.between(preview.start,preview.end).toMinutes())
 }
 @Test fun consecutiveCompleteRawDaysCarryTheActualMidnightAnd660MinuteEndpoints() {
  val first=record(date,words=intArrayOf(0x1000,0x6528))
  val next=record(date+86400,first.size,0x6000,0x121c).also{it[9]=2}
  val days=CardActivityParser.parse(CardActivityFile(0,first.size,first+next))
  assertEquals(listOf("0001","0002"),days.map{it.dailyPresenceCounter})
  assertEquals(86400L,days[1].recordDateUtc.epochSecond-days[0].recordDateUtc.epochSecond)
  assertEquals(0,days[1].changes.first().minuteOfDay)
  val start=days[0].recordDateUtc.plusSeconds(days[0].changes.last().minuteOfDay*60L)
  val end=days[1].recordDateUtc.plusSeconds(days[1].changes[1].minuteOfDay*60L)
  assertEquals(660L,java.time.Duration.between(start,end).toMinutes())
  assertFalse(days[0].changes.last().cardInserted);assertTrue(days[0].changes.last().crewDriving)
 }
 @Test fun validRingWithMissingCalendarDayIsNotEvidenceOfConsecutiveDays() {
  val first=record(date,words=intArrayOf(0,0x0528));val later=record(date+172800,first.size,0)
  val days=CardActivityParser.parse(CardActivityFile(0,first.size,first+later))
  assertEquals(172800L,days[1].recordDateUtc.epochSecond-days[0].recordDateUtc.epochSecond)
 }
 @Test fun midnightSuccessorCannotTurnAnUnknownRawWithdrawalIntoAKnownManualEntry() {
  val first=record(date,words=intArrayOf(0,0x2528));val next=record(date+86400,first.size,0x2000,0x121c)
  val days=CardActivityParser.parse(CardActivityFile(0,first.size,first+next))
  assertFalse(days[0].changes.last().cardInserted);assertFalse(days[0].changes.last().crewDriving)
  assertFalse(days[1].changes.first().cardInserted);assertFalse(days[1].changes.first().crewDriving)
 }
 @Test fun zeroWordIsARealMidnightObservationWhileTheWholeZeroRingIsEmpty() {
  assertEquals(DriverActivity.REST,parse(0).changes.single().activity)
  assertEquals(emptyList<CardActivityDay>(),CardActivityParser.parse(CardActivityFile(0,0,ByteArray(64))))
 }
 @Test fun actualFingerprintMatchesIndependentBytesAndRetainsCardStatusAndOrder() {
  val day=parse(0,0x1168,0x31e0);val h=CardActivityDayFingerprint.id("C1",day)
  assertEquals("EXPECTED_HASH",h)
  assertEquals(h,CardActivityDayFingerprint.id("C1",day.copy(ringOffset=48)))
  assertNotEquals(h,CardActivityDayFingerprint.id("C2",day))
  assertNotEquals(h,CardActivityDayFingerprint.id("C1",day.copy(changes=day.changes.mapIndexed{i,x->if(i==1)x.copy(crewDriving=true)else x})))
  assertNotEquals(h,CardActivityDayFingerprint.id("C1",day.copy(changes=day.changes.mapIndexed{i,x->if(i==2)x.copy(cardInserted=true)else x})))
  val a=parse(0,0x11e0,0x31e0,0x1258);val b=parse(0,0x31e0,0x11e0,0x1258)
  assertNotEquals(CardActivityDayFingerprint.id("C1",a),CardActivityDayFingerprint.id("C1",b))
 }
 @Test fun manualDrivingBitPatternRemainsRawRatherThanProofOfALegalManualEntry() {
  val got=parse(0,0x7968).changes[1];assertEquals(DriverActivity.DRIVING,got.activity)
  assertFalse(got.cardInserted);assertTrue(got.crewDriving)
 }
}
'''.replace('EXPECTED_HASH',expected)
(src/'M0CardInputProbe.kt').write_text(probe)
jardir=Path('/workspace/scratch/4c3cf966d792/40128-local-check/jars');jars=sorted(jardir.glob('*.jar'));cp=os.pathsep.join(map(str,jars))
cmd=['java','-cp',cp,'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-jvm-target','17','-no-stdlib','-no-reflect','-classpath',cp,'-d',str(classes)]+[str(p) for p in sorted(src.glob('*.kt'))]
r=subprocess.run(cmd,text=True,capture_output=True);(work/'compile.log').write_text(r.stdout+r.stderr)
if r.returncode:print(r.stderr);raise SystemExit(r.returncode)
r=subprocess.run(['java','-cp',str(classes)+os.pathsep+cp,'org.junit.runner.JUnitCore','de.fahrerabrechnung.data.card.CardActivityParserTest','de.fahrerabrechnung.data.card.M0CardInputProbe'],text=True,capture_output=True)
(work/'junit.log').write_text(r.stdout+r.stderr)
result=dict(kind='read-only existing source probe; not engine/adapter/Room/SQLCipher/device acceptance',source_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=repo,text=True).strip(),inputs=inputs,independent_fingerprint=h if False else expected,reference_payload_sha256=expected,junit_stdout=r.stdout,returncode=r.returncode,compiler='existing scratch Kotlin 2.4.20 embeddable / Java 17; product build unchanged',probe_sha256=hashlib.sha256(probe.encode()).hexdigest())
(work/'result.json').write_text(json.dumps(result,indent=2));print(json.dumps(result,indent=2))
raise SystemExit(r.returncode)
```

### Unveränderter Room-Compilerexport und genaue DAO-Abfragen

Tatsächlich ausgeführtes Hilfsprogramm, SHA256 `39b989915cd3a153ea35f5b910a90f2d0b211d60e8042d38dabc8742da47bd11`.

```python
from pathlib import Path
import json,sqlite3,re,hashlib
r=Path('/workspace/scratch/8d9b62deaae5/FahrerTagebuch-v1-engine-plan');p=r/'data/schemas/de.fahrerabrechnung.data.db.AppDatabase/22.json';database=json.loads(p.read_text())['database'];checks=[]
def ck(name,value):
 assert value,name
 checks.append(name)
con=sqlite3.connect(':memory:');con.execute('PRAGMA foreign_keys=ON')
for e in database['entities']:
 con.execute(e['createSql'].replace('${TABLE_NAME}',e['tableName']))
 for i in e.get('indices',[]):con.execute(i['createSql'].replace('${TABLE_NAME}',e['tableName']))
# Populate required ancestors with synthetic fields by exported types; no real database opened.
def row(table,**values):
 cols=con.execute('PRAGMA table_info(`'+table+'`)').fetchall();d={}
 for _,name,type_,notnull,default,pk in cols:
  if name in values:d[name]=values[name]
  elif notnull and default is None:d[name]=b'X' if type_=='BLOB' else 0 if type_ in ('INTEGER','REAL') else table+'-'+name
 sql='INSERT INTO `'+table+'` ('+','.join('`'+k+'`' for k in d)+') VALUES ('+','.join('?'for k in d)+')'
 con.execute(sql,list(d.values()))
row('profiles',id='P1');row('profiles',id='P2')
row('driver_cards',id='C1',profileId='P1');row('driver_cards',id='C2',profileId='P2')
row('card_activity_imports',id='O1');row('card_activity_imports',id='O2')
row('driver_activity_days',id='D1',driverCardId='C1',recordDateUtcEpochSeconds=1788739200,dailyPresenceCounter='0001',distanceKm=0,changeCount=4)
for index,minute,inserted in [(0,0,1),(1,480,1),(2,480,0),(3,600,1)]:
 row('driver_activity_changes',dayId='D1',changeIndex=index,minuteOfDay=minute,activity='WORK',slot='DRIVER',crewDriving=0,cardInserted=inserted)
row('driver_activity_day_sources',driverCardId='C1',activityId='O1',recordIndex=0,dayId='D1',ringOffset=48)
# Exact source DAO SQL, named parameters honoured by Python SQLite.
s=(r/'data/src/main/kotlin/de/fahrerabrechnung/data/db/DriverActivityDao.kt').read_text();queries=re.findall(r'@Query\("([^"]+)"\)',s)
ck('exact DAO SELECT statements found',len(queries)==2)
rows=con.execute(queries[1],dict(cardId='C1',activityId='O1')).fetchall()
ck('same-minute entries retained in changeIndex order',[a[1:3] for a in rows]==[(0,0),(1,480),(2,480),(3,600)])
ck('same-minute cardInserted values remain distinct',[a[-1]for a in rows]==[1,1,0,1])
ck('foreign-card source is not returned',con.execute(queries[1],dict(cardId='C2',activityId='O1')).fetchall()==[])
ck('other import source is not returned',con.execute(queries[1],dict(cardId='C1',activityId='O2')).fetchall()==[])
ck('source day preserves exact source position',con.execute(queries[0],dict(cardId='C1',activityId='O1')).fetchone()[-2:]==(0,48))
for name,sql,params in [
 ('cross-card day reference rejected','INSERT INTO driver_activity_day_sources VALUES (?,?,?,?,?)',('C2','O2',0,'D1',0)),
 ('duplicate index rejected','INSERT INTO driver_activity_changes VALUES (?,?,?,?,?,?,?)',('D1',1,480,'REST','DRIVER',0,0)),
 ('linked day deletion restricted','DELETE FROM driver_activity_days WHERE id=?',('D1',)),
 ('linked raw import deletion restricted','DELETE FROM card_activity_imports WHERE id=?',('O1',))]:
 try:con.execute(sql,params);raise AssertionError(name+' was not rejected')
 except sqlite3.IntegrityError:checks.append(name)
ck('all inserted references valid',con.execute('PRAGMA foreign_key_check').fetchall()==[])
ck('Room schema unchanged',hashlib.sha256(p.read_bytes()).hexdigest()=='e555aec9e6b1db51372db348c2afc57d6a38a0889df26642b7ef8cee3e4eef4f')
out=dict(kind='SQLite structural probe of compiler-exported Room22 SQL and exact DAO SELECTs; NOT Room/SQLCipher/migration acceptance',checks=len(checks),passed=checks,schema_sha256=hashlib.sha256(p.read_bytes()).hexdigest(),sqlite_version=sqlite3.sqlite_version)
(Path(__file__).parent/'m1-storage-probe.json').write_text(json.dumps(out,indent=2));print(json.dumps(out,indent=2))
```
