# Requirements Document — 03 Vereins- und Agenturwerkzeug

## Introduction
Diese Spec beschreibt den Weg eines Vereins und eines Sponsors durch die Plattform: von der Pilot-Anfrage über
Prüfung und Zugang zu Profil, Paketen mit strukturierten Gegenleistungen, Suche, Anfrage und Engagement. Dazu
gehören die Startseite in der Plattform und die Regeln, die für alle öffentlichen Formulare gelten
(Einwilligung, Drosselung, Benachrichtigung).

Grundlage ist das «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026, Anforderungen V1 bis V8, S1 bis S3, S9, P4,
P5 und P9; die Kennung steht in Klammern hinter jeder Anforderung. Requirement 16 stammt aus dem Umsetzungsplan
(ohne v5-Kennung).

Für den Bestand gilt Bestandsschutz (`tech.md`). Zu jeder Anforderung steht der Stand der Plattform: «vorhanden»
heisst erfüllt, «anpassen» heisst Vorhandenes ändern, «neu» heisst es gibt nichts dazu.

Schreibweise, Nachweis und Verweise wie in Spec 01; Feature-Dateien tragen `@spec-03`.

### Offene Entscheide
| Nr. | Frage | Annahme dieser Spec |
| --- | --- | --- |
| U2 | Pilot-Anfrage mit Zusage statt direkter Registrierung | ja; `/registrieren` bleibt für Eingeladene |
| U3 | Öffentliche Seiten vorerst nur deutsch | ja |
| E6 | Gestaltung | «Hallenboden» öffentlich, bisheriger Stil hinter dem Login |
| O1 | Nennt die Startseite eine Marke und ihre Programme? | offen; die Plattform und `/kunde/` sind neutral |
| O5 | Kontakt-Anfrage des Vereins an Sponsoren | nur an verbundene Sponsoren |
| E5a | Pilot-Anfragen auf dem gemeinsamen Server | ja, als bewusster Entscheid |

## Glossary
- **Pilot-Anfrage** – Anfrage einer Organisation um einen Platz im Pilot; ersetzt die direkte Registrierung.
- **Prüfung** – Entscheid der Verwaltung, ob eine Organisation aufgenommen wird.
- **Paket** – Angebot eines Vereins an Sponsoren, bestehend aus Gegenleistungen.
- **Gegenleistung** – ein Bestandteil eines Pakets, mit Typ, Reichweite und Verfügbarkeit.
- **Typ** – Eintrag im Katalog der Gegenleistungen; fest als aktiv oder passiv eingestuft.
- **Aktivierbar** – ein Paket mit mindestens einer aktiven Gegenleistung.
- **Probepaket** – das Paket, das eine Organisation in der Pilot-Anfrage zusammengestellt hat.
- **Engagement** – die zugesagte Partnerschaft zwischen einem Verein und einem Sponsor.
- **Sponsor-Art** – Agentur einer Marke, lokaler Partner oder Marke.
- **Verbunden** – Verein und Sponsor haben ein Engagement oder eine angenommene Anfrage.

---

### Requirement 1: Pilot-Anfrage (V1)
**User Story:** Als Vorstandsmitglied möchte ich für meinen Verein einen Pilotplatz anfragen und wissen, wie es
weitergeht.

Stand der Plattform: neu. `/registrieren` legt direkt ein Konto an und kennt die Angaben der Seite nicht.

#### Acceptance Criteria
1. Die Plattform SOLL eine Anfrage mit diesen Pflichtangaben annehmen: Rolle (Verein oder Sponsor), Name der
   Organisation, Bereich bzw. Sponsor-Art, PLZ und Ort, Vor- und Nachname, E-Mail; dazu die Bestätigung, für die
   Organisation sprechen zu dürfen. `[offen: U2]`
2. Die Plattform SOLL die freiwilligen Angaben speichern: Paketname und gewählte Gegenleistungen, Mitgliederzahl,
   nächster Anlass, Interessen, Zielgruppe, Marktgebiet, Programme, UID, Funktion, Telefon, Wunsch nach Updates.
3. WENN dieselbe Anfrage (E-Mail, Organisation, Rolle) innert 10 Minuten erneut eintrifft, DANN SOLL die
   Plattform die bestehende aktualisieren statt eine zweite anzulegen.
4. Die Plattform SOLL nach dem Absenden sagen, was als Nächstes geschieht, und keine Organisation öffentlich
   zeigen.

### Requirement 2: Prüfung jeder Organisation (V2)
**User Story:** Als Verein möchte ich sicher sein, dass nur geprüfte Organisationen auf der Plattform sichtbar
sind.

Stand der Plattform: anpassen. Die Liste der Prüfungen in der Verwaltung gibt es; es fehlen Ablehnungsgrund,
Protokolleintrag und die Sperre für Ungeprüfte.

#### Acceptance Criteria
1. Die Plattform SOLL jede Organisation auf drei Punkte prüfen lassen: Bezug zu Sport oder Gesundheit, Bezug zur
   Schweiz, Plausibilität der Angaben.
2. SOLANGE eine Organisation nicht geprüft ist, SOLL nichts von ihr öffentlich oder für andere Organisationen
   sichtbar sein, auch keine Projekte und Pakete.
3. WENN die Verwaltung ablehnt, DANN SOLL sie einen Grund angeben, und die Kontaktperson SOLL ihn per E-Mail
   erhalten.
4. Die Plattform SOLL eine UID annehmen, aber nicht verlangen.
5. Jeder Prüfentscheid SOLL im Protokoll stehen, mit Person und Zeitpunkt.

### Requirement 3: Zugang nach der Zusage (V3)
**User Story:** Als Kontaktperson möchte ich nach der Zusage per E-Mail meinen Zugang erhalten.

Stand der Plattform: anpassen. Einladungen gibt es nur in eine bestehende Organisation.

#### Acceptance Criteria
1. WENN die Verwaltung eine Pilot-Anfrage zusagt, DANN SOLL die Plattform die Organisation mit den Angaben der
   Anfrage anlegen und die Kontaktperson als Eigentümerin einladen.
2. Die Einladung SOLL 7 Tage gelten und sich erneut senden lassen.
3. WENN die Region der Organisation noch nicht im Pilot ist, DANN SOLL die Plattform das mitteilen und die
   Anfrage für den späteren Start vormerken.

### Requirement 4: Vereinsprofil mit Reichweite (V4)
**User Story:** Als Verein möchte ich einmal beschreiben, wer wir sind und wen wir erreichen.

Stand der Plattform: anpassen. Die Organisation hat Stammdaten und Logo; Mitgliederzahl, Sportart und nächster
Anlass fehlen. Anlässe haben eine Tabelle, aber kein Formular.

#### Acceptance Criteria
1. Das Profil SOLL Sportart oder Gesundheitsbereich aus der Liste der Startseite führen (11 Werte von
   «Ballsport» bis «Anderes»), dazu Mitgliederzahl und Region.
2. Der Verein SOLL Anlässe mit Name, Datum und Ort erfassen und ändern können.
3. Die Region SOLL sich aus PLZ und Ort ergeben.

### Requirement 5: Pakete mit strukturierten Gegenleistungen (V5)
**User Story:** Als Verein möchte ich Pakete aus klaren Gegenleistungen zusammenstellen, damit Sponsoren sie
vergleichen können.

Stand der Plattform: neu. Ein Paket hat Name, Beschreibung und Preis; die Spalte `gegenleistungen` ist freier
Text ohne Formularfeld und ohne Anzeige.

#### Acceptance Criteria
1. Ein Paket SOLL aus einer oder mehreren Gegenleistungen bestehen.
2. Jede Gegenleistung SOLL einen Typ aus dem Katalog tragen, dazu Reichweite und Verfügbarkeit.
3. Der Katalog SOLL mit diesen zehn Typen starten. Aktiv: Standaktion am Turnier oder Anlass, Newsletter an die
   Mitglieder, Schnuppertraining mit dem Sponsor, Wettbewerb mit QR-Code, Kurzauftritt am Clubabend, Vorteil für
   Mitglieder beim Vereinsausrüster. Passiv: Logo auf dem Trikot, Hallenbande oder Banner am Platz, Logo auf
   Website und Social Media, Dank an der Mitgliederversammlung.
4. Jeder Typ SOLL fest als aktiv oder passiv gelten; der Verein ändert das nicht.
5. Der Verein SOLL ein Paket jederzeit ändern können; bestehende Engagements behalten den Stand der Zusage.

### Requirement 6: Aktivierbarkeit und Vorschau (V6)
**User Story:** Als Verein möchte ich sehen, wie ein Sponsor mein Paket sieht und wie aktivierbar es ist.

Stand der Plattform: neu.

#### Acceptance Criteria
1. Die Plattform SOLL zu jedem Paket die Zahl der aktiven und der passiven Gegenleistungen zeigen.
2. Ein Paket SOLL als aktivierbar gelten, sobald es mindestens eine aktive Gegenleistung enthält.
3. Der Verein SOLL beim Erfassen sehen, wie ein Sponsor das Paket sieht.

### Requirement 7: Probepaket übernehmen (V7)
**User Story:** Als Verein möchte ich das Paket, das ich bei der Anfrage zusammengestellt habe, nicht noch einmal
erfassen.

Stand der Plattform: neu.

#### Acceptance Criteria
1. WENN eine zugesagte Pilot-Anfrage ein Paket enthält, DANN SOLL es nach dem ersten Login als Entwurf
   bereitliegen.
2. Der Entwurf SOLL erst nach einer Bestätigung des Vereins sichtbar werden.

### Requirement 8: Anfragen und Zusage (V8)
**User Story:** Als Verein möchte ich Anfragen an einem Ort sehen und mit der Zusage ein Engagement festhalten.

Stand der Plattform: anpassen. Inbox, Annahme, Ablehnung und Nachrichten gibt es. Bei der Annahme entsteht
nichts; ein Vertrag wird von Hand erzeugt.

#### Acceptance Criteria
1. Jede Anfrage SOLL in der Inbox des Vereins stehen, zusammen mit dem Paket, um das es geht.
2. WENN der Verein zusagt, DANN SOLL die Plattform ein Engagement anlegen mit Verein, Sponsor, Paket,
   Gegenleistungen im Stand der Zusage und Laufzeit.
3. Das Engagement SOLL für beide Seiten sichtbar sein und für niemanden sonst, bis es veröffentlicht wird.
4. Ohne Zusage des Vereins SOLL kein Engagement entstehen.

### Requirement 9: Sponsor-Arten und Programme (S1)
**User Story:** Als Sponsor möchte ich angeben, was für ein Sponsor wir sind und woran wir teilnehmen.

Stand der Plattform: neu. Die Plattform kennt nur eine Branche des Sponsors.

#### Acceptance Criteria
1. Ein Sponsor SOLL eine Art tragen: Agentur einer Marke, lokaler Partner oder Marke.
2. Eine Agentur SOLL ihrer Marke zugeordnet sein und ein Marktgebiet haben.
3. Ein Sponsor SOLL angeben können, an welchen Programmen einer Marke er teilnimmt; Marken und Programme sind
   Daten, nicht Code.
4. WENN sich eine Marke ohne Pilot-Zugang meldet, DANN SOLL die Plattform das Interesse vormerken und es sagen.

### Requirement 10: Suche nach Paketen (S2)
**User Story:** Als Sponsor möchte ich Pakete finden, die Begegnungen bringen, statt auf Anfragen zu warten.

Stand der Plattform: anpassen. Der Marktplatz sucht im Volltext über Projekte und filtert nach Kategorie, Ort und
Branche. Region, Aktivierbarkeit und die Suche nach Vereinen fehlen.

#### Acceptance Criteria
1. Ein geprüfter Sponsor SOLL Pakete nach Region, Sportart oder Bereich und Aktivierbarkeit filtern können.
2. WENN der Sponsor einen Freitext wie «Volleyballclub in Baden» eingibt, DANN SOLL die Plattform Sportart und
   Ort erkennen und als Filter setzen.
3. Ein Treffer SOLL Verein, Region und die aktiven Gegenleistungen des Pakets zeigen.
4. Die Suche SOLL nur Pakete geprüfter Vereine aus freigeschalteten Regionen zeigen.

### Requirement 11: Geführte Anfrage (S3)
**User Story:** Als Sponsor möchte ich zu einem Paket eine Anfrage stellen und eine Antwort bekommen.

Stand der Plattform: vorhanden. Die Paket-Anfrage führt durch Nachricht und Kontakt.

#### Acceptance Criteria
1. Ein Sponsor SOLL zu einem Paket eine Anfrage stellen können, mit Nachricht und Kontaktperson.
2. Der Verein SOLL annehmen oder ablehnen; der Sponsor SOLL die Antwort erhalten.
3. Ein Verein SOLL eine Kontakt-Anfrage nur an Sponsoren richten können, mit denen er verbunden ist.
   `[offen: O5]`

### Requirement 12: Vereine einladen (S9)
**User Story:** Als Sponsor möchte ich Vereine aus unserem Netz auf die Plattform einladen.

Stand der Plattform: neu. Ein Sponsor kann im CRM einen Verein ohne Eigentümer anlegen, aber niemanden einladen.

#### Acceptance Criteria
1. Ein geprüfter Sponsor SOLL einen Verein einladen können; die Einladung führt in die Pilot-Anfrage mit
   vorbelegtem Bezug zum Sponsor.
2. Ein eingeladener Verein SOLL dieselbe Prüfung durchlaufen wie jeder andere.

### Requirement 13: Einwilligungen (P4)
**User Story:** Als Person möchte ich, dass nachvollziehbar ist, wozu ich wann eingewilligt habe.

Stand der Plattform: neu. Die Plattform speichert keine Einwilligung, auch nicht bei der Registrierung.

#### Acceptance Criteria
1. Jede Einwilligung SOLL mit Zeitpunkt und Wortlaut gespeichert sein.
2. Updates zum Pilot SOLL die Plattform nur senden, wenn die Person das angekreuzt hat.

### Requirement 14: Schutz vor Missbrauch (P5)
**User Story:** Als Betreiber möchte ich, dass öffentliche Formulare nicht geflutet werden können.

Stand der Plattform: anpassen. Login und Registrierung sind gedrosselt, das Kontaktformular nicht.

#### Acceptance Criteria
1. Jedes öffentliche Formular SOLL gedrosselt sein: höchstens 10 Eingaben pro Stunde je IPv4-Adresse bzw.
   IPv6-/64 und 60 insgesamt.
2. Die Plattform SOLL dafür nur einen Prüfwert der IP-Adresse speichern und ihn nach einem Tag entfernen.

### Requirement 15: Benachrichtigungen (P9)
**User Story:** Als Kontaktperson möchte ich erfahren, wie die Prüfung ausgegangen ist.

Stand der Plattform: anpassen. Mails gibt es für Registrierung, Anfrage und Einladung; das Ergebnis der Prüfung
wird nicht gemeldet.

#### Acceptance Criteria
1. Die Plattform SOLL das Ergebnis der Prüfung per E-Mail melden.
2. Benachrichtigungen über Kontakte und Anfragen von Mitgliedern SOLLEN keine Personendaten enthalten.

### Requirement 16: Startseite aus der Plattform (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich die bestehende Startseite aus der Plattform ausliefern, ohne dass sich
für Besucher etwas ändert.

#### Acceptance Criteria
1. Die Plattform SOLL die Startseite für Vereine und Sponsoren unter `/` ausliefern, mit dem Inhalt und Aussehen
   der bisherigen Seite. Nachweis: manuell [Fabian] (Bildvergleich bei 360 und 1280 px, hell und dunkel).
2. Die Adressen `/?rolle=verein`, `/?rolle=sponsor` und `/#anmelden` SOLLEN weiter gelten; der Schnellstart
   führt mit vorbelegter Rolle und vorbelegtem Namen in die Pilot-Anfrage.
3. Schriften, Stylesheets und Skripte der öffentlichen Seiten SOLLEN von der eigenen Adresse kommen.
4. Paket-Baukasten, Beispielsuche und Cockpit-Skizze der Startseite SOLLEN als Beispiel gekennzeichnet sein,
   solange Requirement 5, Requirement 10 und Spec 04, Requirement 2 nicht umgesetzt sind.
5. Die Startseite SOLL als Reihenfolge der Freischaltung nennen: zuerst Profil, Pakete und Vorteilsseite, dann
   Suche und Anfragen, danach Cockpit und Anlass-Seiten. Nachweis: manuell [Fabian].
6. Die automatische Prüfung der Zugänglichkeit SOLL für die neue Startseite keinen neuen Befund zeigen.
