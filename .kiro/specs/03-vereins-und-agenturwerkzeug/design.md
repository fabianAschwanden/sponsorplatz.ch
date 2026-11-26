# Design Document — 03 Vereins- und Agenturwerkzeug

## Overview
Der Weg einer Organisation hat fünf Stationen: Pilot-Anfrage, Prüfung, Zugang, Angebot (Profil und Pakete),
Zusage (Anfrage und Engagement). Die ersten drei ersetzen die direkte Registrierung für neue Organisationen;
`/registrieren` bleibt für Eingeladene. Vorhandenes wird angepasst (Prüfung, Einladung, Marktplatz, Anfrage),
Neues entsteht in eigenen Fachbereichen (`pilotanfrage`, `gegenleistung`, Engagement).

Die Startseite der Vorschau wird das Template der Startseite der Plattform; ihre Demos bleiben Demos, bis die
echten Funktionen stehen.

## Components and Interfaces
| Fachbereich | Art | Inhalt |
| --- | --- | --- |
| `pilotanfrage` | neu, hexagonal | Anfrage annehmen, Liste für die Verwaltung, Zusage und Absage, Vormerken |
| `gegenleistung` | neu, hexagonal | Katalog der Typen, Gegenleistungen eines Pakets, Aktivierbarkeit |
| `engagement` (`domain`, `application`, `adapter`) | neu, hexagonal, im bestehenden Paket | Engagement als Objekt, entsteht bei der Zusage |
| `organisation` | Bestand, angepasst | Profil (Sportart, Mitgliederzahl, Region), Sponsor-Art, Marke, Marktgebiet, Programme |
| `admin` | Bestand, angepasst | Prüfung mit Grund und Protokoll |
| `einladung` | Bestand, angepasst | Einladung der Eigentümerin einer neuen Organisation |
| `projekt` | Bestand, angepasst | Paket verweist auf Gegenleistungen; Formular für Anlässe; Marktplatz mit neuen Filtern |
| `anfrage` | Bestand, angepasst | Zusage ruft den Anwendungsfall «Engagement anlegen» auf |
| `home` | Bestand, angepasst | Startseite als neues Template |

### Adressen
| Adresse | Inhalt | Zugang |
| --- | --- | --- |
| `/` | Startseite | öffentlich |
| `/pilot` | Pilot-Anfrage absenden | öffentlich, gedrosselt |
| Liste der Pilot-Anfragen | Zusage, Absage mit Grund | `PLATFORM_ADMIN` |
| Suche | Pakete nach Region, Sportart, Aktivierbarkeit, Freitext | geprüfte Sponsoren, Stufe 2 der Region |

### Pilot-Anfrage und Zugang
1. Das Formular speichert die Anfrage mit Einwilligung (Zeitpunkt und Wortlaut).
2. Die Verwaltung sagt zu oder ab. Eine Absage braucht einen Grund.
3. Die Zusage legt die Organisation mit den Angaben der Anfrage an (Status geprüft), lädt die Kontaktperson als
   Eigentümerin ein und legt ein enthaltenes Probepaket als Entwurf ab.
4. Ist die Region nicht im Pilot, wird die Anfrage vorgemerkt und die Kontaktperson informiert.

Doppelte Anfragen: Schlüssel ist (E-Mail, Organisation, Rolle) innert 10 Minuten; die bestehende wird
aktualisiert.

### Prüfung und Sichtbarkeit
«Nichts Ungeprüftes ist sichtbar» wird an einer Stelle durchgesetzt: Abfragen für öffentliche Seiten und für
andere Organisationen filtern auf geprüfte Organisationen. Das betrifft Marktplatz, Projekte, Pakete,
Schaufenster und Suche. Eigene Inhalte sieht eine ungeprüfte Organisation weiter.

### Gegenleistungen
Der Katalog ist eine Tabelle mit festem Start (zehn Typen). `aktiv` gehört zum Typ. Aktivierbarkeit ist
abgeleitet, nicht gespeichert. Die Freitext-Spalte `gegenleistungen` am Paket bleibt in der Datenbank, bis eine
spätere Migration sie entfernt (`tech.md`, Regel 1).

### Engagement
Die Zusage einer Anfrage legt ein Engagement an und kopiert die Gegenleistungen des Pakets (Stand der Zusage).
Spätere Änderungen am Paket ändern das Engagement nicht. Bestehende angenommene Anfragen werden einmalig in
Engagements überführt.

### Suche
Filter Region, Sportart oder Bereich, Aktivierbarkeit. Der Freitext wird in Wörter zerlegt und gegen die Liste
der Sportarten und das PLZ-Verzeichnis geprüft; Erkanntes wird Filter, der Rest Volltext. Kein Sprachmodell.

### Drosselung
Ein Zähler je Formular und Prüfwert der IP-Adresse (IPv4 ganz, IPv6 auf /64 gekürzt, mit Salz gehasht), dazu ein
Gesamtzähler je Formular. Einträge älter als ein Tag werden entfernt. Die Drosselung am Reverse Proxy
(Spec 01) bleibt die äussere Schicht.

## Data Models
| Tabelle | Stand | Spalten (Auszug) |
| --- | --- | --- |
| `pilot_anfrage` | neu | Rolle, Organisation, Bereich bzw. Sponsor-Art, PLZ, Ort, Kontaktperson, E-Mail, freiwillige Angaben, Probepaket, Status (`NEU`, `ZUGESAGT`, `ABGELEHNT`, `VORGEMERKT`), Grund, eingeladen von (Sponsor) |
| `einwilligung` | neu | Bezug (Art und ID), Zweck, Wortlaut, Zeitpunkt |
| `formular_drosselung` | neu | Formular, Prüfwert, Zeitpunkt |
| `gegenleistung_typ` | neu | Code, Name, `aktiv` |
| `gegenleistung` | neu | Paket, Typ, Reichweite, Verfügbarkeit, Position |
| `engagement` | neu | Verein, Sponsor, Paket, Laufzeit von und bis, Betrag, Status, veröffentlicht |
| `engagement_gegenleistung` | neu | Engagement, Typ, Reichweite, Verfügbarkeit (Kopie zum Stand der Zusage) |
| `organisation` | angepasst | Sportart, Mitgliederzahl, Region, Sponsor-Art, Marke, Marktgebiet, Programme |

## Error Handling
- Formulare sagen, welche Pflichtangabe fehlt; Eingaben bleiben stehen.
- Bei erreichter Drosselung sagt die Seite, dass es später wieder geht.
- Eine Absage ohne Grund wird abgelehnt.
- Die Suche antwortet in einer Region ohne Stufe 2 mit 404 (Spec 01, 11.2).

## Testing Strategy
| Bereich | Nachweis |
| --- | --- |
| Pilot-Anfrage, Prüfung, Zugang, Gegenleistungen, Engagement, Suche | Cucumber mit `@spec-03`, gegen PostgreSQL |
| Sichtbarkeit Ungeprüfter | Szenarien je betroffene Ansicht: Marktplatz, Projekt, Paket, Suche |
| Startseite | Bildvergleich (manuell), angepasste Tests des Bestands (`HomeControllerTest`, Smoke, axe) |
| Domain-Regeln | JUnit ohne Spring |

Die erste Aufgabe mit neuem Fachbereich ist 2.5. Sie setzt das Muster für alle weiteren.

## Open Points
- U2, U3, E6, O1, O5, E5a (Tabelle in `requirements.md`).
- Ob die Liste der Sportarten der Startseite und die Branchen des Bestands zusammengeführt oder nebeneinander
  geführt werden (Requirement 4).
- Wie Regionen aus PLZ entstehen, legt Spec 01, Requirement 11 fest.
