# Requirements Document — 06 Produktivschaltung

## Introduction
Diese Spec beschreibt, was erfüllt sein muss, bevor die Plattform echte Daten hält und öffentlich ist: die Rechte
der Personen an ihren Daten (Export und Löschung), Rechtstexte mit Betreiber und Hoster, der Ort der echten
Daten, eine Datenbank ohne Demo-Konten und die Abnahme.

Grundlage ist das «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026, Anforderungen P3 und P10; die Kennung steht
in Klammern hinter jeder Anforderung. Requirement 3 stammt aus dem Umsetzungsplan, Gate 3 (ohne v5-Kennung).

Für den Bestand gilt Bestandsschutz (`tech.md`). Schreibweise, Nachweis und Verweise wie in Spec 01;
Feature-Dateien tragen `@spec-06`.

### Offene Entscheide
| Nr. | Frage | Annahme dieser Spec |
| --- | --- | --- |
| E5b | Wo liegen Anfragen von Mitgliedern und Daten echter Vereine? | eigener Server vor dem ersten echten Kontakt |
| U4 | Absender der Mails | ein Postfach der Domain |
| O1 | Nennt die Startseite eine Marke und ihre Programme? | offen; betrifft die Rechtstexte, wenn Programme genannt werden |

## Glossary
- **Betreiber** – die Firma, die die Plattform verantwortet, mit Sitz und Kontakt.
- **Hoster** – der Anbieter, bei dem App, Datenbank und Sicherung liegen.
- **Empfänger** – jede Stelle, an die Personendaten gehen (z. B. die Agentur eines Engagements).
- **Echte Daten** – Personendaten wirklicher Personen, im Unterschied zu Demo- und Testdaten.
- **Pseudonymisieren** – den Bezug zur Person entfernen, den Eintrag aber behalten.
- **Abnahme** – Prüfung aller Kriterien der Stufen 1 und 2 vor dem Öffentlichschalten.

---

### Requirement 1: Export und Löschung (P3)
**User Story:** Als Person möchte ich meine Daten mitnehmen und löschen lassen können.

Stand der Plattform: anpassen. Der Export liefert Konto, Mitgliedschaften und Merkliste. Eine Löschung fehlt.

#### Acceptance Criteria
1. Eine Person SOLL ihre Daten vollständig exportieren können, auch Anfragen und Nachrichten.
2. Eine Person SOLL ihr Konto löschen können; Einträge im Protokoll werden dabei pseudonymisiert.
3. Eine Organisation SOLL die Löschung ihrer Daten verlangen können; laufende Engagements nennt die Plattform
   vorher.

### Requirement 2: Rechtstexte und Betreiber (P10)
**User Story:** Als Besucher möchte ich wissen, wer hinter der Plattform steht und wohin meine Daten gehen.

Stand der Plattform: anpassen. Die Texte sind auf dem Stand vom 1.1.2026 und nennen den früheren Betrieb.

#### Acceptance Criteria
1. Datenschutzerklärung und Impressum SOLLEN den Betreiber, den Hoster und alle Empfänger von Daten nennen.
   Nachweis: manuell [Fabian].
2. SOLANGE Betreiberangaben fehlen, SOLL die Plattform nicht öffentlich schaltbar sein.
3. Die Rechtsseiten SOLLEN unter `/datenschutz` und `/impressum` ohne Anmeldung erreichbar sein; die bisherigen
   Sprungmarken `/#datenschutzerklaerung` und `/#impressum` SOLLEN dorthin führen.

### Requirement 3: Echte Daten und Öffentlichschalten (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich erst öffentlich schalten, wenn nichts Vorläufiges mehr im Weg steht.

#### Acceptance Criteria
1. SOLANGE der Ort für echte Daten nicht entschieden und umgesetzt ist, SOLL die Plattform keine echten Anfragen
   von Mitgliedern und keine Daten echter Vereine halten. Nachweis: manuell [Fabian]. `[offen: E5b]`
2. Vor dem Öffentlichschalten SOLL die Datenbank keine Demo-Konten und keine Demodaten enthalten.
   Nachweis: Dokument (Protokoll der Abnahme).
3. Vor dem Öffentlichschalten SOLLEN alle Kriterien der Stufen 1 und 2 als Checkliste abgenommen sein, dazu eine
   Wiederherstellungs-Probe und eine Sicherheitsprüfung von aussen. Nachweis: Dokument (Protokoll der Abnahme).
4. Der Modus `oeffentlich` SOLL erst nach der Abnahme gesetzt werden. Nachweis: manuell [Fabian].
5. Die Auftragsbearbeitung mit dem Hoster und dem Anbieter des Postfachs SOLL geklärt sein. Nachweis: manuell
   [Fabian].
