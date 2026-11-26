# Requirements Document — 04 Leads und Cockpit

## Introduction
Diese Spec beschreibt die Wirkung eines Engagements: Kontakte mit Einwilligung, die Kette vom eingesetzten Betrag
bis zum Kunden im Cockpit, die Erinnerung vor dem Ablauf, die Sicht der Agentur und der Marke, die Anlass-Seite
mit QR-Code sowie Weitersagen und Reichweite der Vorteilsseite.

Grundlage ist das «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026, Anforderungen S4 bis S7, M6, M9 und M10;
die Kennung steht in Klammern hinter jeder Anforderung.

Für den Bestand gilt Bestandsschutz (`tech.md`). Zu jeder Anforderung steht der Stand der Plattform.
Schreibweise, Nachweis und Verweise wie in Spec 01; Feature-Dateien tragen `@spec-04`.

### Offene Entscheide
| Nr. | Frage | Annahme dieser Spec |
| --- | --- | --- |
| O3 | Sieht die Marke die Kontakte ihrer Agenturen als Personen? | nein, nur die Zahl |
| O4 | Aufbewahrung von Kontakten ohne Beratung | `[offen: Frist; Vorschlag 12 Monate]` |
| E5b | Wo liegen Anfragen von Mitgliedern? | bis zum Entscheid nur erfundene Angaben in Tests |

## Glossary
- **Kontakt** (im Code `Lead`) – eine Person, die mit Einwilligung ihre Angaben hinterlassen hat.
- **Quelle** – woher ein Kontakt stammt: Vorteilsseite, Anlass oder Empfehlung.
- **Kette** – eingesetzter Betrag, Kontakte mit Einwilligung, Beratungen, Kunden.
- **Cockpit** – Ansicht des Sponsors über die Wirkung seiner Engagements.
- **Anlass-Seite** – öffentliche Seite zu einem Anlass eines Vereins, erreichbar über einen QR-Code.
- **Reichweite** – Zählung je Vorteilsseite und Tag, ohne Personenbezug.
- **Marke** – die Organisation, der Agenturen zugeordnet sind.

---

### Requirement 1: Kontakte mit Einwilligung (S4)
**User Story:** Als Agentur möchte ich Kontakte nur von Personen erhalten, die das wollen, und sie selbst
nachführen.

Stand der Plattform: neu. Das CRM führt Vereine als Leads des Sponsors, keine Personen aus Kampagnen.

#### Acceptance Criteria
1. Ein Kontakt SOLL nur entstehen, wenn die Person aktiv einwilligt; die Plattform SOLL Zeitpunkt und Wortlaut
   der Einwilligung speichern.
2. Jeder Kontakt SOLL der Organisation gehören, die ihn erzeugt. Andere Vereine und Sponsoren SOLLEN ihn nicht
   sehen, auch der Verein des Engagements nicht.
3. Der Sponsor SOLL nachführen können, wer zur Beratung kam und wer Kunde wurde.
4. Jeder Kontakt SOLL seine Quelle tragen: Vorteilsseite, Anlass oder Empfehlung.
5. WENN eine Person ihre Einwilligung widerruft, DANN SOLL die Plattform den Kontakt löschen.

### Requirement 2: Wirkung im Cockpit (S5)
**User Story:** Als Sponsor möchte ich sehen, was aus einem Engagement geworden ist.

Stand der Plattform: neu. Die Statistik zeigt Verträge, Volumen und die Quote angenommener Anfragen, aber keine
Kontakte und keine Wirkung.

#### Acceptance Criteria
1. Das Cockpit SOLL je Engagement die Kette zeigen: eingesetzter Betrag, Kontakte mit Einwilligung, Beratungen,
   Kunden.
2. Das Cockpit SOLL dieselbe Kette über alle Engagements des Sponsors zusammenzählen.
3. Die Kette SOLL ohne Vertrag und Rechnung funktionieren; der Betrag steht am Engagement.

### Requirement 3: Erinnerung vor dem Ablauf (S6)
**User Story:** Als Sponsor möchte ich rechtzeitig erfahren, dass ein Engagement ausläuft.

Stand der Plattform: anpassen. Es gibt eine Liste auslaufender Verträge im CRM, aber keine Erinnerung.

#### Acceptance Criteria
1. Die Plattform SOLL den Sponsor 90 Tage vor dem Ende eines Engagements erinnern, in der Plattform und per
   E-Mail.
2. Die Erinnerung SOLL am Engagement hängen, nicht am Vertrag.

### Requirement 4: Sicht der Agentur und der Marke (S7)
**User Story:** Als Agentur möchte ich alles an einem Ort sehen, und als Marke möchte ich die Zahlen meiner
Agenturen zusammengezählt sehen.

Stand der Plattform: anpassen. Rechte vererben sich an die übergeordnete Organisation; die Sammelansichten
rechnen aber nur über direkte Mitgliedschaften.

#### Acceptance Criteria
1. Eine Agentur SOLL alle ihre Anfragen, Engagements und Kontakte an einem Ort sehen.
2. Die Marke SOLL die Engagements und Kennzahlen ihrer Agenturen zusammengezählt sehen, je Marktgebiet.
3. Die Marke SOLL die Kontakte einer Agentur nur als Zahl sehen, nicht als Personen. `[offen: O3]`

### Requirement 5: Anlass-Seite (M6)
**User Story:** Als Besucher eines Anlasses möchte ich über einen QR-Code sehen, was es am Stand gibt, und
mitmachen können, ohne meine Daten zu hinterlassen.

Stand der Plattform: neu. Anlässe gibt es nur als interne Verwaltung.

#### Acceptance Criteria
1. Die Plattform SOLL je Anlass eine öffentliche Seite mit Name, Datum, Ort und den Angeboten am Stand liefern,
   erreichbar über einen QR-Code.
2. An einem Wettbewerb SOLL jede Person teilnehmen können, auch ohne Mitgliedschaft und ohne Kontaktangabe.
3. WENN eine Person ihren Kontakt hinterlässt, DANN SOLL das nur mit ausdrücklicher Einwilligung geschehen und
   einen Kontakt mit Quelle «Anlass» ergeben (Requirement 1).

### Requirement 6: Weitersagen (M9)
**User Story:** Als Mitglied möchte ich die Seite weitergeben können, ohne dass etwas über mich mitgeht.

Stand der Plattform: neu.

#### Acceptance Criteria
1. Die Seite SOLL sich teilen lassen, ohne dass der Link Angaben zur teilenden Person enthält.
2. Die Seite SOLL keine Belohnung fürs Teilen versprechen.

### Requirement 7: Reichweite (M10)
**User Story:** Als Verein und als Agentur möchte ich wissen, wie oft die Seite angesehen wurde.

Stand der Plattform: neu.

#### Acceptance Criteria
1. Die Plattform SOLL je Vorteilsseite und Tag zählen: Aufrufe, Aufrufe über QR-Code, Beratungsanfragen.
2. Die Zählung SOLL ohne Personenbezug auskommen: keine IP-Adresse, kein Cookie, keine Kennung.
3. Verein und Agentur SOLLEN dieselben Zahlen sehen.
