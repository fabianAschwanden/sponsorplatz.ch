# Design Document — 04 Leads und Cockpit

## Overview
Der Fachbereich `lead` entsteht früh und klein: Mit der Beratungsanfrage der Vorteilsseite (Spec 02) braucht es
den Kontakt mit Einwilligung, Quelle und Widerruf (Aufgabe 4.8b). Nachführen, Cockpit, Erinnerung, Anlass-Seite
und Reichweite folgen mit Stufe 3.

Die wichtigste Regel ist die Zugehörigkeit: Ein Kontakt gehört der Organisation, die ihn erzeugt. Der Verein des
Engagements sieht Zahlen, nie Personen. Das gilt ausdrücklich gegen das sonst «kollaborative» Rollenmodell des
Bestands.

## Components and Interfaces
| Fachbereich | Art | Inhalt |
| --- | --- | --- |
| `lead` | neu, hexagonal | Kontakt anlegen (nur mit Einwilligung), Liste und Status für die besitzende Organisation, Widerruf, Zählungen für Dritte |
| `vorteilsseite` | Spec 02, erweitert | Zählung der Reichweite, Teilen ohne Angaben zur Person |
| `projekt` | Bestand, angepasst | Anlass öffentlich schaltbar, Angebote am Stand, QR-Code |
| `engagement` | Spec 03, erweitert | Betrag und Laufzeit als Quelle der Kette und der Erinnerung |
| `dashboard`, `anfrage` | Bestand, angepasst | Cockpit des Sponsors, Sammelansichten über die Hierarchie |

### Zugriff auf Kontakte
| Wer | Sieht |
| --- | --- |
| Mitglieder der besitzenden Organisation (G3) | Personen, Status, Quelle |
| Verein des Engagements | Zahl je Quelle und Status |
| Marke der Agentur | Zahl je Agentur und Marktgebiet (offen: O3) |
| alle anderen | nichts |

Die Prüfung sitzt in der `application`-Schicht von `lead`; es gibt keinen Anwendungsfall, der Personen ohne
Angabe der besitzenden Organisation liefert.

### Widerruf und Aufbewahrung
Ein Widerruf löscht den Kontakt; im Protokoll bleibt ein Eintrag ohne Personendaten. Kontakte ohne Beratung
werden nach einer Frist gelöscht (offen: O4).

### Kette
Betrag aus dem Engagement; Kontakte, Beratungen und Kunden aus `lead` je Engagement. Summen kommen aus SQL.

### Erinnerung
Ein täglicher Lauf sucht Engagements, die in 90 Tagen enden und noch keine Erinnerung haben, und erzeugt eine
Benachrichtigung in der Plattform und eine E-Mail. Die Erinnerung wird am Engagement vermerkt, damit sie nur
einmal geht.

### Anlass-Seite
Öffentliche Seite je Anlass mit kurzem Link und QR-Code. Die Teilnahme am Wettbewerb braucht keine Angaben. Wer
seinen Kontakt hinterlässt, tut das über dasselbe Formular und dieselbe Einwilligung wie auf der Vorteilsseite.

### Reichweite
Ein Zähler je Vorteilsseite, Tag und Art (Aufruf, Aufruf über QR-Code, Beratungsanfrage). Der QR-Code trägt einen
Zusatz in der Adresse, der nur die Herkunft «QR» kennzeichnet. Gespeichert wird nur die Zahl.

## Data Models
| Tabelle | Stand | Spalten (Auszug) |
| --- | --- | --- |
| `lead` | neu | besitzende Organisation, Verein, Engagement, Quelle (`VORTEILSSEITE`, `ANLASS`, `EMPFEHLUNG`), Vorname, Nachname, Kontaktweg, Status (`NEU`, `BERATUNG`, `KUNDE`), Einwilligung (Verweis auf `einwilligung`), erstellt am |
| `reichweite_tag` | neu | Vorteilsseite, Tag, Art, Anzahl |
| `event` | angepasst | öffentlich, Kurzlink, Angebote am Stand, Bezug zum Engagement |
| `engagement` | erweitert | Erinnerung gesendet am |

## Error Handling
- Ein Kontakt ohne Einwilligung wird abgelehnt (Datenbank-Pflichtfeld und Prüfung in der Domain).
- Der Zugriff einer fremden Organisation auf einen Kontakt antwortet mit 404.
- Scheitert die E-Mail der Erinnerung, bleibt die Benachrichtigung in der Plattform; der Versand wird wiederholt.

## Testing Strategy
| Bereich | Nachweis |
| --- | --- |
| Kontakt, Einwilligung, Widerruf, Zugehörigkeit | Cucumber mit `@spec-04`, gegen PostgreSQL; je Szenario zwei Organisationen |
| Kette und Sammelansichten | Cucumber mit festen Zahlen |
| Reichweite ohne Personenbezug | Szenario prüft, dass weder Cookie noch Kennung entsteht; JUnit über das Tabellenschema |
| Erinnerung | Cucumber mit gestellter Uhr |

## Open Points
- O3, O4, E5b (Tabelle in `requirements.md`).
- Ob das bestehende CRM (Vereine als Leads des Sponsors) und `lead` (Personen) in einer Ansicht zusammenkommen.
