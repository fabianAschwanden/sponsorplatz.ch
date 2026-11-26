# Requirements Document — 02 Vorteilsseite

## Introduction
Diese Spec beschreibt die Sicht der Mitglieder: je Verein eine öffentliche Seite, die auf dem Handy in einer
Minute zeigt, was die Partnerschaft bringt, mit Weiche, Anleitung zum Einlösen, Beratungsanfrage und einem
Teilen-Kit für Vorstand und Delegierte. Dazu kommen Sprache, Zugänglichkeit und die Kennzeichnung von Beispielen
für alle öffentlichen Seiten.

Grundlage ist das «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026, Anforderungen M1 bis M5, M8, M11, P7 und
P8; die Kennung steht in Klammern hinter jeder Anforderung. Requirement 10 stammt aus dem Umsetzungsplan (ohne
v5-Kennung).

Stand der Plattform: Alles in dieser Spec ist neu. Die Plattform hat keine öffentliche Vereinsseite und kennt
weder Vorteile noch Beratungsanfragen. Nutzbar sind die Tabelle für Anlässe, die Medienablage und die
PDF-Erzeugung.

Schreibweise, Nachweis und Verweise wie in Spec 01; Feature-Dateien tragen `@spec-02`. Der Kontakt, der aus einer
Beratungsanfrage entsteht, ist in Spec 04, Requirement 1 beschrieben.

### Offene Entscheide
| Nr. | Frage | Annahme dieser Spec |
| --- | --- | --- |
| E9 | Nennung der Versicherung auf Vorteilsseiten | neutral als Standard, Name je Seite nach Freigabe |
| O2 | Wer gibt eine Vorteilsseite frei? | Verein und Agentur gemeinsam |
| U3 | Öffentliche Seiten vorerst nur deutsch | ja |
| E5b | Wo liegen Anfragen von Mitgliedern? | bis zum Entscheid nur erfundene Angaben in Tests |
| O6 | Offene Fragen zu Rahmenvertrag, Guthaben und Prämie | betreffen nur die Texte der Bausteine |

## Glossary
- **Vorteilsseite** – öffentliche Seite eines Vereins zu einem Engagement, ohne Anmeldung.
- **Partnerin** – der Sponsor des Engagements; in der neutralen Anzeige «Versicherungspartnerin».
- **Baustein** – ein Vorteil auf der Vorteilsseite, mit Art, Titel, Kurztext, Zielgruppe und Hinweisen.
- **Weiche** – Frage «Wo bist du versichert?» mit den zwei Wegen «bei der Partnerin» und «anderswo».
- **Weg** – das Ergebnis der Weiche; nur im Browser, nie gespeichert.
- **Beratungsanfrage** – Formular der Vorteilsseite; ergibt mit Einwilligung einen Kontakt der Agentur.
- **Teilen-Kit** – Link, Chat-Text, Mail-Vorlage und A4-Plakat mit QR-Code für die Verteilung im Verein.
- **Agentur** – die Organisation des Sponsors, die das Engagement führt.

---

### Requirement 1: Vorteilsseite je Verein (M1)
**User Story:** Als Mitglied möchte ich auf dem Handy in einer Minute sehen, was die Partnerschaft meines Vereins
mir bringt, ohne mich anzumelden.

#### Acceptance Criteria
1. Die Plattform SOLL je Verein eine Seite ohne Login liefern, die auf dem Handy in einer Minute zeigt, was die
   Partnerschaft bringt.
2. Die Seite SOLL den Verein nennen und die Partnerin neutral oder mit Namen, je nach Einstellung der Seite;
   Standard ist neutral. `[offen: E9]`
3. Die Seite SOLL ohne JavaScript lesbar sein und dann beide Wege zeigen.
4. Die Seite SOLL einen kurzen, teilbaren Link haben.

### Requirement 2: Vorteils-Bausteine (M2)
**User Story:** Als Mitglied möchte ich die Vorteile als Übersicht sehen und bei Interesse das Detail lesen.

#### Acceptance Criteria
1. Eine Vorteilsseite SOLL aus Bausteinen bestehen. Jeder Baustein hat eine Art (Rahmenvertrag, Guthaben für den
   Beitrag, Partnerbetriebe, Aktion, Anlass), Titel, Kurztext, Zielgruppe («für alle» oder «für Versicherte der
   Partnerin»), je einen Hinweis für beide Wege und optional eine Gültigkeit.
2. Die Seite SOLL die Bausteine als Übersicht mit Sprung zum Detail zeigen und zählen, wie viele für alle gelten.
3. Konditionen SOLLEN nur im Text der Bausteine stehen; die Plattform rechnet nichts.
4. WENN die Gültigkeit eines Bausteins abgelaufen ist, DANN SOLL er nicht mehr erscheinen.

### Requirement 3: Weiche (M3)
**User Story:** Als Mitglied möchte ich nur sehen, was für mich gilt, ohne dass jemand erfährt, wo ich versichert
bin.

#### Acceptance Criteria
1. Die Seite SOLL fragen, ob das Mitglied bei der Partnerin oder anderswo versichert ist, und danach nur zeigen,
   was zum gewählten Weg passt.
2. Die Plattform SOLL die Wahl weder prüfen noch speichern noch an den Server senden.
3. WENN die Seite mit `?kunde=ja` oder `?kunde=nein` geöffnet wird, DANN SOLL sie den Weg vorwählen und den
   Zusatz aus der Adresse entfernen.

### Requirement 4: Einlösen (M4)
**User Story:** Als Versicherte der Partnerin möchte ich wissen, wie ich meine Vorteile bekomme.

#### Acceptance Criteria
1. Die Seite SOLL Versicherten der Partnerin in Schritten zeigen, wie sie ihre Vorteile einlösen; die Schritte
   pflegt die Agentur.
2. Die Seite SOLL sagen, dass nicht sie den Versicherungsstatus prüft, sondern die Versicherung.

### Requirement 5: Beratungsanfrage (M5)
**User Story:** Als Mitglied möchte ich unverbindlich fragen können, was für mich drinliegt.

#### Acceptance Criteria
1. Die Seite SOLL eine Anfrage mit Vorname, Nachname und einem Kontaktweg (Telefon oder E-Mail) annehmen, dazu
   eine ausdrückliche Einwilligung in die Weitergabe an die zuständige Agentur mit Vermerk des Vereins.
2. Die Plattform SOLL daraus einen Kontakt der Agentur mit Quelle «Vorteilsseite» und Bezug zum Verein anlegen
   (Spec 04, Requirement 1).
3. Die Seite SOLL keine Angaben zur Gesundheit erfragen.
4. Die Agentur SOLL eine Benachrichtigung ohne Personendaten erhalten; die Angaben stehen nur in der Plattform.
5. Die Seite SOLL sagen: unverbindlich, kein Newsletter, Widerruf jederzeit, für unter 18-Jährige fragen die
   Eltern an.

### Requirement 6: Teilen-Kit (M8)
**User Story:** Als Vorstandsmitglied möchte ich die Seite mit fertigen Texten und einem Plakat im Verein
verteilen.

#### Acceptance Criteria
1. Die Plattform SOLL je Vorteilsseite ein Kit liefern: Link, Text für den Vereins-Chat, Mail-Vorlage mit Betreff
   und ein A4-Plakat mit QR-Code.
2. Alle vier SOLLEN den Namen des Vereins und die Adresse seiner Vorteilsseite tragen.
3. SOLANGE die Vorteilsseite nicht freigegeben ist, SOLL das Kit sagen, dass es noch nicht zu verteilen ist.

### Requirement 7: Pflege und Freigabe (M11)
**User Story:** Als Agentur möchte ich die Bausteine pflegen, und als Verein möchte ich mitentscheiden, was unter
unserem Namen erscheint.

#### Acceptance Criteria
1. Die Agentur SOLL die Bausteine pflegen, der Verein die Beiträge.
2. Eine Vorteilsseite SOLL erst öffentlich sein, wenn Verein und Agentur sie freigegeben haben. `[offen: O2]`
3. Jede Änderung an einem Baustein SOLL im Protokoll stehen.

### Requirement 8: Sprache und Zugänglichkeit (P7)
**User Story:** Als Mitglied möchte ich die Seite auf jedem Gerät und mit Hilfsmitteln bedienen können.

Stand der Plattform: anpassen. Vier Sprachen und eine automatische Prüfung auf elf Seiten gibt es; die neuen
öffentlichen Seiten sind deutsch.

#### Acceptance Criteria
1. Öffentliche Seiten SOLLEN auf dem Handy bedienbar sein, hell und dunkel, und WCAG 2.1 AA erfüllen.
2. Die Plattform SOLL Organisationen mit «ihr» und Mitglieder mit «du» ansprechen. Nachweis: manuell [Fabian].

### Requirement 9: Beispiele sind als Beispiele erkennbar (P8)
**User Story:** Als Besucher möchte ich erkennen, was ein Beispiel ist und was echt.

Stand der Plattform: anpassen. Die Demodaten enthalten keine Anlässe, Gegenleistungen und Vorteilsseiten.

#### Acceptance Criteria
1. Jeder fiktive Verein, Anlass, Betrieb und Beitrag SOLL als Beispiel gekennzeichnet sein.
2. Die Demodaten SOLLEN den Turnverein Musterdorf mit seiner Vorteilsseite enthalten.

### Requirement 10: Vorteilsseite des Beispielvereins aus der Plattform (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich die bestehende Beispielseite unverändert aus der Plattform ausliefern,
bevor sie aus der Datenbank kommt.

#### Acceptance Criteria
1. Die Plattform SOLL die Vorteilsseite des Beispielvereins unter `/kunde` und `/kunde/` ohne Anmeldung
   ausliefern, mit dem Inhalt und Verhalten der bisherigen Seite.
2. Die Adressen `/kunde/?kunde=ja`, `/kunde/?kunde=nein` und `/kunde/?fuer=vorstand` SOLLEN weiter gelten.
3. SOLANGE Requirement 5 nicht umgesetzt ist, SOLL das Formular der Seite nichts übermitteln und das sagen.
4. Die Seite SOLL als Beispiel gekennzeichnet und von Suchmaschinen ausgenommen sein.
