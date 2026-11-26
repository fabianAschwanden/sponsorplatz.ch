# Design Document — 06 Produktivschaltung

## Overview
Die Produktivschaltung ist kein Bauabschnitt, sondern eine Schwelle. Zwei Dinge werden gebaut (Export und
Löschung, die Sperre «ohne Betreiberangaben nicht öffentlich»), der Rest sind Entscheide, Umzüge und Nachweise.

## Components and Interfaces
| Bereich | Art | Inhalt |
| --- | --- | --- |
| `benutzer` | Bestand, angepasst | vollständiger Export; Konto löschen |
| `audit` | Bestand, angepasst | Pseudonymisieren der Einträge einer gelöschten Person |
| `organisation` | Bestand, angepasst | Löschung einer Organisation mit Hinweis auf laufende Engagements |
| `home` | Bestand, angepasst | Rechtsseiten mit den Texten der bisherigen Startseite |
| Deploy-Workflow | Spec 01, erweitert | verweigert `oeffentlich`, solange die Betreiberangaben fehlen |

### Export
Der Export enthält alles, was die Plattform zur Person hält: Konto, Mitgliedschaften, Merkliste, gestellte und
erhaltene Anfragen, Nachrichten, Einwilligungen. Kontakte (`lead`) gehören der Organisation, nicht der Person,
die sie bearbeitet; sie stehen im Export der Organisation.

### Löschung
- **Konto:** Die Person verliert den Zugang; Protokolleinträge bleiben mit einem Platzhalter statt der Person.
  Ist die Person die letzte Eigentümerin einer Organisation, sagt die Plattform das vorher.
- **Organisation:** Die Plattform nennt laufende Engagements; die Löschung ist ein Auftrag an die Verwaltung,
  kein Knopf.

### Betreiberangaben
Name, Sitz und Kontakt des Betreibers sind Konfiguration der Plattform. Fehlt eine Angabe, zeigen die
Rechtsseiten das nicht als Platzhalter, sondern der Deploy verweigert den Modus `oeffentlich`.

### Ort der echten Daten
Fällt E5b auf einen eigenen Server: gleiches Muster wie in Spec 01, Umzug per Dump und Wiederherstellung, danach
die Domain. Die Vorschau mit Demodaten kann auf dem bisherigen Server bleiben oder entfällt.

### Demodaten entfernen
Das Profil `demo` wird abgeschaltet. Die produktive Datenbank beginnt leer oder wird bereinigt; der erste
Verwaltungszugang entsteht aus dem Secret (Spec 01, 8.4).

## Data Models
Keine neuen Tabellen. `audit`-Einträge erhalten die Möglichkeit, den Bezug zur Person zu entfernen.

## Error Handling
- Der Export einer Person ohne Daten liefert eine leere, gültige Datei.
- Die Löschung eines Kontos mit offener Eigentümerschaft wird abgelehnt und sagt, was zuerst zu tun ist.

## Testing Strategy
| Bereich | Nachweis |
| --- | --- |
| Export, Löschung, Pseudonymisierung | Cucumber mit `@spec-06` |
| Sperre ohne Betreiberangaben | Cucumber oder JUnit über den Workflow |
| Rechtstexte, Ort der Daten, Abnahme | Protokoll der Abnahme unter `docs/betrieb/` |

## Open Points
- E5b, U4, O1 (Tabelle in `requirements.md`).
- Ob die Löschung einer Organisation Fristen aus laufenden Engagements beachten muss.
