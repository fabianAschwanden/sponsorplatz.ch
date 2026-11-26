# Design Document — 02 Vorteilsseite

## Overview
Die Vorteilsseite entsteht in zwei Schritten. Zuerst wird die bestehende Beispielseite ein Template der
Plattform (Requirement 10), unverändert in Inhalt und Verhalten. Danach kommt je Verein eine Seite aus der
Datenbank (Requirements 1 bis 7); `/kunde/` zeigt dann den Beispielverein aus den Demodaten.

Öffentliche Seiten werden serverseitig gerendert und sind ohne JavaScript lesbar. Die Weiche ist eine Anzeige im
Browser: Ohne JavaScript zeigt die Seite beide Wege, mit JavaScript blendet sie den nicht gewählten aus. Der
Server erfährt den Weg nie.

## Components and Interfaces
Neuer Fachbereich `vorteilsseite` (hexagonal, `structure.md`).

| Schicht | Inhalt |
| --- | --- |
| `domain` | `Vorteilsseite` (Status, Anzeige der Partnerin), `Vorteil` (Art, Zielgruppe, Gültigkeit), Regel «öffentlich erst nach beiden Freigaben» |
| `application` | Anwendungsfälle: Seite anzeigen, Baustein pflegen, freigeben, Teilen-Kit erzeugen; Ports für Ablage, Verein und Engagement, Protokoll, PDF |
| `adapter.in.web` | öffentliche Seite, Pflege für Agentur und Verein, Teilen-Kit |
| `adapter.out` | Persistenz, Zugriff auf den Bestand (Organisation, Protokoll, PDF-Dienst) |

### Adressen
| Adresse | Inhalt | Zugang |
| --- | --- | --- |
| `/kunde`, `/kunde/` | Vorteilsseite des Beispielvereins | öffentlich, `noindex` |
| `/v/{kurzlink}` | Vorteilsseite eines Vereins | öffentlich, sobald freigegeben |
| `/v/{kurzlink}/beratung` | Beratungsanfrage absenden | öffentlich, gedrosselt (Spec 03, Requirement 14) |
| Pflege und Teilen-Kit | hinter dem Login, im Bereich der Organisation | Agentur (G1 der Agentur), Verein (G1 des Vereins) |

Der Kurzlink ist ein Vorschlag; er enthält keinen Namen einer Person und keine Angabe zur teilenden Person.

### Weiche
`?kunde=ja` und `?kunde=nein` wählen den Weg vor; das Skript entfernt den Zusatz danach aus der Adresse
(`history.replaceState`), damit er beim Weiterteilen nicht mitgeht. Die Wahl liegt höchstens im Speicher der
Seite, nicht in einem Cookie und nicht in `localStorage`.

### Beratungsanfrage
Das Formular nimmt Vorname, Nachname, einen Kontaktweg und die Einwilligung an. Die Anwendung übergibt die
Angaben mit Zeitpunkt und Wortlaut der Einwilligung an den Fachbereich `lead` (Spec 04, Requirement 1); dort
entsteht der Kontakt. Die Agentur erhält eine Benachrichtigung ohne Personendaten.

### Teilen-Kit
Link, Chat-Text und Mail-Vorlage sind Texte mit dem Namen des Vereins und der Adresse der Seite. Das Plakat
entsteht über den vorhandenen PDF-Dienst, der QR-Code serverseitig.

## Data Models
| Tabelle | Spalten (Auszug) |
| --- | --- |
| `vorteilsseite` | `id`, `verein_id`, `partner_id`, `kurzlink` (eindeutig), `partnerin_mit_namen` (Standard falsch), `status` (`ENTWURF`, `FREIGEGEBEN`), `freigabe_verein_am`, `freigabe_partner_am`, `einloesen_schritte` |
| `vorteil` | `id`, `vorteilsseite_id`, `art`, `titel`, `kurztext`, `zielgruppe` (`ALLE`, `VERSICHERTE`), `hinweis_partnerin`, `hinweis_anderswo`, `gueltig_bis`, `position` |

Sobald es das Engagement als eigenes Objekt gibt (Spec 03, Requirement 8), verweist die Vorteilsseite darauf;
bis dahin auf Verein und Partner.

## Error Handling
- Eine nicht freigegebene oder unbekannte Vorteilsseite antwortet mit 404.
- Ein Formular ohne Einwilligung wird abgelehnt und sagt, was fehlt; die Eingaben bleiben stehen.
- Wird die Drosselung erreicht, sagt die Seite, dass es später wieder geht; sie nennt keine Zahlen.

## Testing Strategy
| Bereich | Nachweis |
| --- | --- |
| Seite, Bausteine, Freigabe, Teilen-Kit | Cucumber mit `@spec-02`, gegen PostgreSQL |
| Weiche, Verhalten ohne JavaScript | wenige Browser-Szenarien (Playwright) |
| Zugänglichkeit | axe auf jeder neuen öffentlichen Seite, Handy und Desktop, hell und dunkel |
| Domain-Regeln | JUnit ohne Spring |

## Open Points
- E9, O2, U3, E5b, O6 (Tabelle in `requirements.md`).
- Form des Kurzlinks.
- Ob die Übersetzung der öffentlichen Seiten eine eigene Aufgabe wird (U3).
