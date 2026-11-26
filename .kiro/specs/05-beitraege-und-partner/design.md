# Design Document — 05 Beiträge und Partner

## Overview
Vier kleine Erweiterungen auf dem, was die Specs 02 bis 04 gebaut haben. Keine davon braucht einen neuen
Zugangsweg: Beiträge und Partnerbetriebe erscheinen auf der Vorteilsseite, Beteiligungen am Engagement, das
Schaufenster ist eine weitere öffentliche Seite.

## Components and Interfaces
| Fachbereich | Art | Inhalt |
| --- | --- | --- |
| `vorteilsseite` | Spec 02, erweitert | Beiträge und Partnerbetriebe anzeigen und pflegen |
| `engagement` | Spec 03, erweitert | Beteiligungen mehrerer Sponsoren, Veröffentlichung fürs Schaufenster |
| `lead` | Spec 04, unverändert | Kontakte gehören weiter der Organisation, die sie erzeugt; bei mehreren Beteiligten je Sponsor getrennt |

### Beiträge
Titel, Text, Datum, ein Foto aus der Medienablage. Der Verein stellt Beiträge ein, die Agentur ebenfalls; jeder
ändert nur seine eigenen. Die Vorteilsseite zeigt die neuesten drei.

### Partnerbetriebe
Name, Ort, Art des Vorteils, optional ein Link. Ein Partnerbetrieb kann eine Organisation der Plattform sein
(lokaler Partner) oder nur ein Eintrag. Die Plattform übermittelt den Betrieben nichts über Mitglieder.

### Gemeinsames Paket
Ein Engagement bekommt Beteiligungen: je Sponsor eine Zeile mit eigenen Gegenleistungen. Die Zugehörigkeit der
Kontakte bleibt beim erzeugenden Sponsor (Spec 04, 1.2).

### Schaufenster
Öffentliche Seite je Region. Ein Engagement erscheint, wenn der Verein geprüft ist, die Region die Stufe 4
freigeschaltet hat und der Verein zugestimmt hat. Gezeigt wird der Dreiklang Verein, Sponsor, Mitglieder, nie
ein Betrag.

## Data Models
| Tabelle | Stand | Spalten (Auszug) |
| --- | --- | --- |
| `beitrag` | neu | Vorteilsseite, Autor-Organisation, Titel, Text, Datum, Foto |
| `partnerbetrieb` | neu | Vorteilsseite, Name, Ort, Vorteil, Link, Organisation (optional) |
| `engagement_beteiligung` | neu | Engagement, Sponsor, Gegenleistungen |
| `engagement` | erweitert | im Schaufenster (Zustimmung des Vereins), Texte des Dreiklangs |

## Error Handling
- Ein Beitrag ohne Titel oder Text wird abgelehnt.
- Das Schaufenster einer Region ohne Stufe 4 antwortet mit 404.

## Testing Strategy
Cucumber mit `@spec-05`, gegen PostgreSQL. Für Requirement 2.2 je Szenario zwei beteiligte Sponsoren. Für
Requirement 1.3 prüft ein Szenario, dass die Seite keinen Betrag enthält.

## Open Points
- Ob die Agentur Beiträge des Vereins freigeben muss, bevor sie erscheinen.
- Ob Partnerbetriebe aus Programmen einer Marke von Hand oder aus einer Liste kommen.
