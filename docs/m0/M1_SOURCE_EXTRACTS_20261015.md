# M0 – zusätzliche amtliche Primärstellen für den M1-Eingang

Stand15.10.2026. Nachweisarchiv, keine Implementierung und keine juristische Gesamtfreigabe. Vollquellen/Sprachen/Download- und Extraktionshashes im [Abrufmanifest](SOURCE_RETRIEVAL_MANIFEST.md); Verwendung und Schlussfolgerungen im [Nachweisprotokoll](../VIOLATION_ENGINE_M0_M1_EVIDENCE.md). Die Auszüge sind exakte Bereiche der gepinnten Textextraktion, einschließlich abschließendem LF. Konsolidierungen sind Dokumentationshilfen; Grund-/Änderungsakte sind separat nachgewiesen. Diese Quelle bestimmt nicht den Umfang einer bereits durchgeführten externen Fachprüfung.

## MS01 – Anhang IC Anlage1 §2.1: vollständige Karten-/VU-Unterscheidung und Entnahmenotiz

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `c0486ce8d2ee2367e12939661a1110ab857c4356366f143cce5caed25c114844`.

```text
2.1.
ActivityChangeInfo
This data type enables to code, within a two bytes word, a slot status at 00:00 and/or a driver status at 00:00 and/or changes of activity and/or changes of driving status and/or changes of card status for a driver or a co-driver. This data type is related to Annex 1C requirements 105, 266, 291, 320, 321, 343, and 344.
Value assignment — Octet Aligned: ‘scpaattttttttttt’B (16 bits)
For Data Memory recordings (or slot status):
‘s’B
Slot:
‘0’B: DRIVER,
‘1’B: CO-DRIVER,
‘c’B
Driving status:
‘0’B: SINGLE,
‘1’B: CREW,
‘p’B
Driver (or workshop) card status in the relevant slot:
‘0’B: INSERTED, a card is inserted,
‘1’B: NOT INSERTED, no card is inserted (or a card is withdrawn),
‘aa’B
Activity:
‘00’B: BREAK/REST,
‘01’B: AVAILABILITY,
‘10’B: WORK,
‘11’B: DRIVING,
‘ttttttttttt’B
Time of the change: Number of minutes since 00h00 on the given day.
For Driver (or Workshop) card recordings (and driver status):
‘s’B
Slot (not relevant when ‘p’=1 except note below):
‘0’B: DRIVER,
‘1’B: CO-DRIVER,
‘c’B
Driving status (case ‘p’=0) or
Following activity status (case ‘p’=1):
‘0’B: SINGLE,
‘0’B: UNKNOWN
‘1’B: CREW,
‘1’B: KNOWN (=manually entered)
‘p’B
Card status:
‘0’B: INSERTED, the card is inserted in a recording equipment,
‘1’B: NOT INSERTED, the card is not inserted (or the card is withdrawn),
‘aa’B
Activity (not relevant when ‘p’=1 and ‘c’=0 except note below):
‘00’B: BREAK/REST,
‘01’B: AVAILABILITY,
‘10’B: WORK,
‘11’B: DRIVING,
‘ttttttttttt’B
Time of the change: Number of minutes since 00h00 on the given day.
Note for the case ‘card withdrawal’:
When the card is withdrawn:
—
‘s’ is relevant and indicates the slot from which the card is withdrawn,
—
‘c’ must be set to 0,
—
‘p’ must be set to 1,
—
‘aa’ must code the current activity selected at that time,
As a result of a manual entry, the bits ‘c’ and ‘aa’ of the word (stored in a card) may be overwritten later to reflect the entry.
```

## MS02 – Anhang IB Anlage1 §2.1 im späteren G1-Text, Gegenprobe zum Original1360/2002

Quelle: Q14G1K / Q14G1KHTTPS. Auszug-SHA256 `30cc645759bd6c1b0dd65a5256b7e07aee205372122448865a4a68cfaea3e824`.

```text
2.1.   ActivityChangeInfo
This data type enables to code, within a two bytes word, a slot status at 00.00 and/or a driver status at 00.00 and/or changes of activity and/or changes of driving status and/or changes of card status for a driver or a co-driver. This data type is related to requirements 084, 109a, 199 and 219.
ActivityChangeInfo ::= OCTET STRING (SIZE(2))
Value assignment — Octet Aligned:
′scpaattttttttttt′B (16 bits)
For Data Memory recordings (or slot status):
′s′B
Slot:
′0′B: DRIVER,
′1′B: CO-DRIVER,
′c′B
Driving status:
′0′B: SINGLE,
′1′B: CREW,
′p′B
Driver (or workshop) card status in the relevant slot:
′0′B: INSERTED, a card is inserted,
1′B: NOT INSERTED, no card is inserted (or a card is withdrawn),
′aa′B
Activity:
′00′B: BREAK/REST,
′01′B: AVAILABILITY,
′10′B: WORK,
′11′B: DRIVING,
′ttttttttttt′B
Time of the change: Number of minutes since 00h00 on the given day.
For Driver (or Workshop) card recordings (and driver status):
′s′B
Slot (not relevant when ′p′ = 1 except note below):
′0′B: DRIVER,
′1′B: 2. CO-DRIVER,
′c′B
Driving status (case ′p′ = 0) or
Following activity status (case ′p′ = 1):
′0′B: SINGLE,
′0′B: UNKNOWN
′1′B: CREW,
′1′B: KNOWN (= manually entered)
′p′B
Card status:
′0′B: INSERTED, the card is inserted in a recording equipment,
′1′B: NOT INSERTED, the card is not inserted (or the card is withdrawn),
′aa′B
Activity (not relevant when ′p′ = 1 and ′c′ = 0 except note below):
′00′B: BREAK/REST,
′01′B: AVAILABILITY,
′10′B: WORK,
′11′B: DRIVING,
′ttttttttttt′B
Time of the change: Number of minutes since 00h00 on the given day.
Note for the case ‘card withdrawal’:
When the card is withdrawn:
— ′s′ is relevant and indicates the slot from which the card is withdrawn,
— ′c′ must be set to 0,
— ′p′ must be set to 1,
— ′aa′ must code the current activity selected at that time,
As a result of a manual entry, the bits ′c′ and ′aa′ of the word (stored in a card) may be overwritten later to reflect the entry.
▼M15
```

## MS03 – Anhang IC Anlage1 §2.9, Tagesdaten und 00:00-Zustand

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `a5acf8e23ce2280a9ec9bd5709a3b19b6b51b4814a83ec5ab871a53ab4799c06`.

```text
2.9.
CardActivityDailyRecord
Information, stored in a card, related to the driver activities for a particular calendar day. This data type is related to Annex 1C requirements 266, 291, 320 and 343.
activityPreviousRecordLength is the total length in bytes of the previous daily record. The maximum value is given by the length of the OCTET STRING containing these records (see CardActivityLengthRange Appendix 2 paragraph 4). When this record is the oldest daily record, the value of activityPreviousRecordLength must be set to 0.
activityRecordLength is the total length in bytes of this record. The maximum value is given by the length of the OCTET STRING containing these records.
activityRecordDate is the date of the record.
activityDailyPresenceCounter is the daily presence counter for the card this day.
activityDayDistance is the total distance travelled this day.
activityChangeInfo is the set of ActivityChangeInfo data for the driver this day. It may contain at maximum 1440 values (one activity change per minute). This set always includes the activityChangeInfo coding the driver status at 00:00.
```

## MS04 – Anhang IC Anlage1 §2.17, vollständige Tagesrecordkette/Ring

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `3fe85299f3f49cef4909f6cfebefe6c8b50bdd0c25c4915753327a4ac9452aa1`.

```text
2.17.
CardDriverActivity
Information, stored in a driver or a workshop card, related to the activities of the driver (Annex 1C requirements 267, 268, 292, 293, 321 and 344).
activityPointerOldestDayRecord is the specification of the begin of the storage place (number of bytes from the beginning of the string) of the oldest complete day record in the activityDailyRecords string. The maximum value is given by the length of the string.
activityPointerNewestRecord is the specification of the begin of the storage place (number of bytes from the beginning of the string) of the most recent day record in the activityDailyRecords string. The maximum value is given by the length of the string.
activityDailyRecords is the space available to store the driver activity data (data structure: CardActivityDailyRecord) for each calendar day where the card has been used.
Value assignment: this octet string is cyclically filled with records of CardActivityDailyRecord. At the first use storing is started at the first byte of the string. All new records are appended at the end of the previous one. When the string is full, storing continues at the first byte of the string independently of a break being inside a data element. Before placing new activity data in the string (enlarging current activityDailyRecord, or placing a new activityDailyRecord) that replaces older activity data, activityPointerOldestDayRecord must be updated to reflect the new location of the oldest complete day record, and activityPreviousRecordLength of this (new) oldest complete day record must be reset to 0.
```

## MS05 – Anhang IC Anlage1 §2.16, CURRENT_USE ist kein Aufzeichnungsende

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `8ae42dd581b4c9cc29aa1a13bcb6d686a241a85e6953531e50e3f4556e15865b`.

```text
2.16.
CardCurrentUse
Information about the actual usage of the card (Annex 1C requirement 273, 298, 326, and 349).
sessionOpenTime is the time when the card is inserted for the current usage. This element is set to zero at card removal.
sessionOpenVehicle is the identification of the currently used vehicle, set at card insertion. This element is set to zero at card removal.
```

## MS06 – Anhang IC Anlage1 §2.162 TimeReal

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `ab4ac89982fcc72d2ec7ad0dfc70baa902b51f9acfffcdba8ea9ca3e9ac4b14f`.

```text
2.162.
TimeReal
Code for a combined date and time field, where the date and time are expressed as seconds past 00h.00m.00s. on 1 January 1970 UTC.
Value assignment – Octet aligned: Number of seconds since midnight 1 January 1970 UTC.
The max. possible date/time is in the year 2106.
▼B
```

## MS07 – Anhang IC §3.6.2 Anforderung61: zulässige manuelle Eingaben/Grenzen

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `e6779afb7e406891f07719f068e4bb3cbcf3aefa31e7af19984099a97b89d450`.

```text
(61) Upon driver (or workshop) card insertion, and only at this time, the recording equipment shall allow manual entries of activities. Manual entries of activities shall be performed using local time and date values of the time zone (UTC offset) currently set for the vehicle unit.
At driver or workshop card insertion the cardholder shall be reminded of:
—
the date and time of his last card withdrawal;
—
optionally: the local time offset currently set for the vehicle unit.
At the first insertion of a given driver card or workshop card currently unknown to the vehicle unit, the cardholder shall be invited to express his consent for tachograph related personal data output through the ITS interface. For checking if a card has already been inserted, the recording equipment shall use the tachograph card data stored in its data memory, as set out in requirement 133.
At any moment, the driver (resp. workshop) consent can be enabled or disabled through commands in the menu, provided the driver (resp. workshop) card is inserted.
It shall be possible to input activities with the following restrictions:
—
Activity type shall be WORK, AVAILABILITY or BREAK/REST;
—
Start and end times for each activity shall be within the period of the last card withdrawal – current insertion only;
—
Activities shall not be allowed to overlap mutually in time.
It shall be possible to make manual entries, if required, at the first insertion of a previously unused driver (or workshop) card.
The procedure for manual entries of activities shall include as many consecutive steps as necessary to set a type, a start time and an end time for each activity. For any part of the time period between last card withdrawal and current card insertion, the cardholder shall have the option not to declare any activity.
During the manual entries associated with card insertion and if applicable, the card holder shall have the opportunity to input:
—
a place where a previous daily work period ended, associated to the relevant time (thus overwriting and validating the entry made at the last card withdrawal),
—
a place where the current daily work period begins, associated to the relevant time (thus validating a temporary entry made at last card withdrawal).
For the place where the current daily work period begins entered at the current card insertion, the recording equipment shall display the current place of the vehicle on the basis of the GNSS information, and of stored digital map(s) in accordance with point 3.12.19, and shall request the driver to confirm or to manually rectify the place.
If the card holder does not enter any place where the work period begins or ended, during the manual entries associated with card insertion, this shall be considered as a declaration that his work period has not changed since the last card withdrawal. The next entry of a place where a previous daily work period ends shall then overwrite the temporary entry made at the last card withdrawal.
If a place is entered, it shall be recorded in the relevant tachograph card.
Manual entries shall be interrupted if:
—
the card is withdrawn or,
—
the vehicle is moving and the card is in the driver slot.
Additional interruptions are allowed, e.g. a timeout after a certain period of user inactivity. If manual entries are interrupted, the recording equipment shall validate any complete place and activity entries (having either unambiguous place and time, or activity type, begin time and end time) already made.
If a second driver or workshop card is inserted while manual entries of activities are in progress for a previously inserted card, the manual entries for this previous card shall be allowed to be completed before manual entries start for the second card.
The cardholder shall have the option to insert manual entries according to the following minimum procedure:
—
Enter activities manually, in chronological order, for the period last card withdrawal – current insertion.
—
Begin time of the first activity shall be set to card withdrawal time. For each subsequent entry, the start time shall be preset to immediately follow the end time of the previous entry. Activity type and end time shall be selected for each activity.
The procedure shall end when the end time of a manually entered activity equals the card insertion time.
The recording equipment shall allow drivers and workshops to alternately upload manual entries that need to be entered during the procedure through the ITS interface specified in Appendix 13 and, optionally, through other interfaces.
The recording equipment shall allow the card holder to modify any activity manually entered, until validation by selection of a specific command. Thereafter, any such modification shall be forbidden.
▼B
```

## MS08 – VO1266/2009 Anhang3.4–3.6: Minutenregel und manuelle Eingaben

Quelle: QG1M15 / QG1M15HTTPS. Auszug-SHA256 `7bd81b8dc976f7f2b16483e7ac8e5a9a06ec424a8a694b68a4f97948322b1cfc`.

```text
3.4
Requirement 040 is deleted.
3.5
Requirement 038, Requirement 041 and Requirement 042 are replaced by the following:
‘Requirement 038
The first change of activity to REST or AVAILABILITY arising within 120 seconds of the automatic change to WORK due to the vehicle stop shall be assumed to have happened at the time of vehicle stop (therefore possibly cancelling the change to WORK).’
‘Requirement 041
Given a calendar minute, if DRIVING is registered as the activity of both the immediately preceding and immediately succeeding minute, the whole minute shall be regarded as DRIVING.’
‘Requirement 042
Given a calendar minute that is not regarded as DRIVING according to the previous requirement 041, the whole minute shall be regarded to be of the same type of activity as the longest continuous activity within the minute (or the latest of equally long activities).’
3.6
Requirements 050, 050a and 050b are replaced by the following:
‘Requirement 050
It shall be possible to input places where daily work periods begin and/or end through commands in the menus. If more than one such input is done within one calendar minute, only the last begin place input and the last end place input done within that time shall be kept recorded.’
‘Requirement 50a
Upon driver (or workshop) card insertion, and only at this time, the recording equipment shall allow manual entries of activities. It shall be possible to make manual entries, if required, at the first insertion of a previously unused driver (or workshop) card.
Manual entries of activities shall be performed using local time and date values of the time zone (UTC offset) currently set for the vehicle unit.
At driver or workshop card insertion the cardholder shall be reminded of:
—
The date and time of his last card withdrawal.
—
Optionally: the local time offset currently set for the vehicle unit
It shall be possible to input activities with the following restrictions:
—
Activity type shall be WORK, AVAILABILITY or BREAK/REST.
—
Start and end times for each activity shall be within the period of the last card withdrawal — current insertion only.
Activities shall not be allowed to overlap mutually in time.
The procedure for manual entries of activities shall include as many consecutive steps as necessary to set a type, a start time and an end time for each activity. For any part of the time period between last card withdrawal and current card insertion, the cardholder shall have the option not to declare any activity.
During the manual entries associated with card insertion and if applicable, the card holder shall have the opportunity to input:
—
a place where a previous daily work period ended, associated to the relevant time (if not already entered at the last card withdrawal),
—
a place where the current daily work period begins, associated to the relevant time,
If a location is entered, it shall be recorded in the relevant tachograph card.
Manual entries shall be interrupted if:
—
the card is withdrawn or,
—
the vehicle is moving and the card is in the driver slot.
Additional interruptions are allowed, e.g. a timeout after a certain period of user inactivity. If manual entries are interrupted, the recording equipment shall validate any complete place and activity entries (having either unambiguous place and time, or activity type, begin time and end time) already made.
If a second driver or workshop card is inserted while manual entries of activities are in progress for a previously inserted card, the manual entries for this previous card shall be allowed to be completed before manual entries start for the second card.
The cardholder shall have the option to insert manual entries according to the following minimum procedure:
Enter activities manually, in chronological order, for the period last card withdrawal – current insertion.
Begin time of the first activity shall be set to card withdrawal time. For each subsequent entry, the start time shall be preset to immediately follow the end time of the previous entry. Activity type and end time shall be selected for each activity.
The procedure shall end when the end time of a manually entered activity equals the card insertion time. The recording equipment may then optionally allow the card holder to modify any activity manually entered, until validation by selection of a specific command. Thereafter, any such modification shall be forbidden.’
‘Requirement 50b
The recording equipment shall allow the driver to enter, in real time, the following two specific conditions:
“OUT OF SCOPE” (begin, end)
“FERRY / TRAIN CROSSING”
A “FERRY / TRAIN CROSSING” may not occur if an “OUT OF SCOPE” condition is opened.
An opened “OUT OF SCOPE” condition shall be automatically closed, by the recording equipment, if a driver card is inserted or withdrawn.
An opened “OUT OF SCOPE” condition shall inhibit the following events and warnings:
—
Driving without an appropriate card,
—
Warnings associated with continuous driving time.’
```

## MS09 – Anhang IC §4.5.3.1.9 Anforderungen266–268: vollständiger Tages-/Statuswechselbestand

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `94ea6be589c91f051125ef2343e52d0f8896af0b76ba0f90fd6c86ae3471bba4`.

```text
(266) The driver card shall be able to store, for each calendar day where the card has been used or for which the driver has entered activities manually, the following data:
—
the date,
—
a daily presence counter (increased by one for each of these calendar days),
—
the total distance travelled by the driver during this day,
—
a driver status at 00:00,
—
whenever the driver has changed of activity, and/or has changed of driving status, and/or has inserted or withdrawn his card:
—
the driving status (CREW, SINGLE),
—
the slot (DRIVER, CO-DRIVER),
—
the card status (INSERTED, NOT INSERTED),
—
the activity (DRIVING, AVAILABILITY, WORK, BREAK/REST),
—
the time of the change.
(267) The driver card memory shall be able to hold driver activity data for at least 28 days (the average activity of a driver is defined as 93 activity changes per day).
(268) The data listed under requirements 261, 264 and 266 shall be stored in a way allowing the retrieval of activities in the order of their occurrence, even in case of a time overlap situation.
```

## MS10 – Anhang IC Kapitel1 Buchst.e: UTC-Kalendertag

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `f471fad530ed1b6db3ff6a65df65c2c45d2c5afce26320334ed9dd18d68a6c5b`.

```text
(e)
‘calendar day’ means:
a day ranging from 00:00 hours to 24:00 hours. All calendar days relate to UTC time (Universal Time Coordinated);
```

## MS11 – Anhang IC Kapitel1 Buchst.ww: ausdrücklich technische UTC-Woche

Quelle: Q04con / Q04HTTPS. Auszug-SHA256 `cd168d013cad4fd0e8db5f8a93ffb63bb1ad01f1c68ba6a86200a32d56d62716`.

```text
(ww)
for computing sake in the recording equipment ‘week’ means:
the period between 00:00 hours UTC on Monday and 24:00 UTC on Sunday;
```
