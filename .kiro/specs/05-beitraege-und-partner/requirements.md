# Requirements Document — 05 Beiträge und Partner

## Introduction
Diese Spec beschreibt das Umfeld eines Engagements: kurze Beiträge von Verein und Agentur, Partnerbetriebe der
Region auf der Vorteilsseite, gemeinsame Pakete mehrerer Sponsoren und das öffentliche Schaufenster der Region.

Grundlage ist das «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026, Anforderungen V9, S8, M7 und M12; die
Kennung steht in Klammern hinter jeder Anforderung.

Für den Bestand gilt Bestandsschutz (`tech.md`). Zu jeder Anforderung steht der Stand der Plattform.
Schreibweise, Nachweis und Verweise wie in Spec 01; Feature-Dateien tragen `@spec-05`.

Diese Spec folgt nach dem Gate «echte Daten». Offene Entscheide hat sie keine eigenen; es gelten E9 (Nennung der
Partnerin, Spec 02) und O6 (Texte der Bausteine).

## Glossary
- **Beitrag** – kurzer Text mit Titel, Datum und Foto, eingestellt von Verein oder Agentur.
- **Partnerbetrieb** – Betrieb der Region, bei dem Mitglieder einen Vorteil einlösen.
- **Gemeinsames Paket** – ein Engagement mit mehreren beteiligten Sponsoren.
- **Beteiligung** – der Anteil eines Sponsors an einem Engagement, mit eigenen Gegenleistungen.
- **Schaufenster** – öffentliche Seite einer Region mit den Engagements der Vereine, die das wollen.

---

### Requirement 1: Schaufenster der Region (V9)
**User Story:** Als Verein möchte ich in meiner Region sichtbar sein, wenn ich das will.

Stand der Plattform: anpassen. Ein Schaufenster je Marke gibt es hinter dem Login, einen Ausschnitt auf der
Startseite. Eine Seite je Region fehlt.

#### Acceptance Criteria
1. Geprüfte Vereine SOLLEN wählen können, ob sie im öffentlichen Schaufenster ihrer Region erscheinen.
2. Das Schaufenster SOLL je Engagement zeigen, was es dem Verein, dem Sponsor und den Mitgliedern bringt.
3. Beträge SOLLEN im Schaufenster nicht erscheinen.

### Requirement 2: Gemeinsames Paket (S8)
**User Story:** Als lokaler Partner möchte ich zusammen mit einer Agentur in einem Paket auftreten.

Stand der Plattform: neu.

#### Acceptance Criteria
1. An einem Engagement SOLLEN mehrere Sponsoren beteiligt sein können, jeder mit eigenen Gegenleistungen.
2. Jeder beteiligte Sponsor SOLL nur seine eigenen Kontakte sehen.

### Requirement 3: Beiträge (M7)
**User Story:** Als Mitglied möchte ich sehen, dass die Partnerschaft im Verein angekommen ist.

Stand der Plattform: neu. Die Medienablage ist nutzbar.

#### Acceptance Criteria
1. Verein und Agentur SOLLEN kurze Beiträge mit Titel, Text, Datum und Foto einstellen können.
2. Die Vorteilsseite SOLL die neuesten Beiträge zeigen.

### Requirement 4: Partnerbetriebe (M12)
**User Story:** Als Mitglied möchte ich wissen, wo ich einen Vorteil einlösen kann.

Stand der Plattform: neu.

#### Acceptance Criteria
1. Eine Vorteilsseite SOLL Betriebe der Region nennen können, bei denen Mitglieder einen Vorteil einlösen.
2. Die Plattform SOLL keine Personendaten von Mitgliedern an diese Betriebe geben.
