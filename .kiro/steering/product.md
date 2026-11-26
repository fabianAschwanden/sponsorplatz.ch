---
inclusion: always
---

# Produkt: Sponsorplatz

## Zweck
Sponsorplatz verbindet drei Seiten eines Sponsorings in Sport und Gesundheit:

- **Der Verein** beschreibt einmal, was er bietet, und erhält Anfragen, die dazu passen.
- **Der Sponsor** findet Pakete, die Begegnungen bringen, und sieht, was daraus wird.
- **Das Mitglied** erfährt in einer Minute, was die Partnerschaft seines Vereins ihm bringt, und kann eine
  Beratung anfragen.

Was die drei verbindet, ist das **Engagement**: die zugesagte Partnerschaft zwischen einem Verein und einem
Sponsor. Am Engagement hängen die Gegenleistungen, die Vorteilsseite für die Mitglieder, die Kontakte mit
Einwilligung und die Auswertung.

Grundlage: «Konzept Sponsoring-Plattform (v5)» vom 26.11.2026. Seine 40 Anforderungen tragen die Kennungen
V1–V9 (Verein), S1–S9 (Sponsor), M1–M12 (Mitglied) und P1–P10 (Plattform); sie stehen in jeder Spec in Klammern
hinter der Anforderung. Das Konzept liegt nicht in diesem Repo: Massgeblich sind die Steering-Dateien und die
freigegebenen Specs unter `.kiro/specs/`.

## Leitsatz
Jede Aussage der öffentlichen Seiten ist ein Versprechen, und die Plattform hält es prüfbar ein.

## Akteure
| Akteur | Wer | Zugang |
| --- | --- | --- |
| Verein | Vorstand, Sponsoring-Verantwortliche | mit Anmeldung, nach Prüfung |
| Sponsor: Agentur | regionale Vertretung einer Marke | mit Anmeldung, nach Prüfung |
| Sponsor: lokaler Partner | Sportgeschäft, Ausrüster, Physiotherapie, Fitnesscenter | mit Anmeldung, nach Prüfung |
| Sponsor: Marke | Gesundheitsmarke mit nationaler Ausrichtung, schrittweise | Interesse anmelden |
| Mitglied | Vereinsmitglied, bei der Versicherungspartnerin versichert oder anderswo | ohne Anmeldung |
| Umfeld | Eltern, Fans, Besucher eines Anlasses | ohne Anmeldung, über QR-Code |
| Multiplikator | Vorstand, Delegierte | ohne Anmeldung |
| Plattform-Verwaltung | prüft Organisationen, schaltet Regionen frei | mit Anmeldung |

Rollen im Code: Plattform-Rollen `PLATFORM_ADMIN`, `PLATFORM_MODERATOR`, `PLATFORM_SUPPORT`; Rollen je
Mitgliedschaft `ORG_OWNER`, `ORG_EDITOR`, `ORG_VIEWER`. Mitglieder, Umfeld und Multiplikatoren haben kein Konto.

## Fünf Klärungen
1. **Reihenfolge.** Zur ersten Stufe gehören Profil, Pakete **und** Vorteilsseite.
2. **Kein offenes Anfrageportal.** Die Suche gibt es nur für geprüfte Sponsoren und nur in freigeschalteten
   Regionen. Die Kontakt-Anfrage eines Vereins geht nur an Sponsoren, mit denen er schon verbunden ist
   (offen, Entscheid O5; Annahme der Specs).
3. **Erst nach der Prüfung öffentlich.** Von einer ungeprüften Organisation ist nichts öffentlich oder für andere
   Organisationen sichtbar, auch keine Projekte und Pakete.
4. **Engagement als eigenes Objekt.** Ein Engagement entsteht mit der Zusage des Vereins und trägt Vorteilsseite,
   Laufzeit, Erinnerung, Kontakte und gemeinsame Pakete.
5. **Marke und Programme sind Daten, nicht Code.** Der Code nennt keine Marke. Ob eine Vorteilsseite die
   Partnerin beim Namen nennt, entscheidet eine Einstellung je Seite; Standard ist die neutrale Anzeige
   «Versicherungspartnerin».

## Stufen der Freischaltung
Stufen werden je Region freigeschaltet (Spec 01, Requirement «Regionen»). Was nicht freigeschaltet ist, ist in
dieser Region nicht erreichbar.

| Stufe | Inhalt | Spec |
| --- | --- | --- |
| 1 · Angebot und Vorteile | Pilot-Anfrage, Prüfung, Zugang, Profil, Pakete mit Gegenleistungen, Vorteilsseite mit Weiche und Beratungsanfrage, Teilen-Kit | `02-vorteilsseite`, `03-vereins-und-agenturwerkzeug` |
| 2 · Finden und Zusagen | Suche, geführte Anfrage, Engagement, Vereine einladen | `03-vereins-und-agenturwerkzeug` |
| 3 · Wirkung | Kontakte nachführen, Anlass-Seiten, Cockpit, Erinnerung, Reichweite, Sicht der Agentur und der Marke | `04-leads-und-cockpit` |
| 4 · Umfeld | Beiträge, Partnerbetriebe, gemeinsame Pakete, Schaufenster der Region | `05-beitraege-und-partner` |

Betrieb, Regionen und Schalter stehen in `01-uebernahme-und-betrieb`, Export, Löschung, Rechtstexte und das
Öffentlichschalten in `06-produktivschaltung`.

## Wer was sieht
| Daten | Verein | Sponsor des Engagements | Marke der Agentur | andere Organisationen | öffentlich |
| --- | --- | --- | --- | --- | --- |
| Pakete geprüfter Vereine | eigene | in freigeschalteten Regionen | wie Agentur | Sponsoren: ja, Vereine: nein | nein |
| Engagement | ja | ja | ja | nein | nur nach Freigabe, ohne Betrag |
| Kontakte | nur Zahl | ja | nur Zahl (offen, Entscheid O3) | nein | nein |
| Vorteilsseite im Entwurf | ja | ja | ja | nein | nein |
| Reichweite | ja | ja | zusammengezählt | nein | nein |
| CRM des Sponsors | nein | ja | ja | nein | nein |

Der Bestand nennt sein Rollenmodell «kollaborativ»: Wer angemeldet ist, sieht fast alles. Für Kontakte,
Engagements im Entwurf und das CRM gilt das ausdrücklich nicht.

## Abgrenzung
- **Kein Matching.** Die Plattform schlägt nichts vor; der Sponsor sucht.
- **Keine Zahlung, kein Vertrag, keine Rechnung im Pilot.** Die Funktionen bleiben im Code, abgeschaltet.
- **Keine Anbindung von Programmen einer Marke.** Die Plattform zeigt und verlinkt, sie tauscht keine Daten mit
  einem Guthaben- oder Shop-System.
- **Keine Prüfung des Versicherungsstatus.** Die Weiche ist eine Selbstauskunft und wird weder gespeichert noch
  an den Server gesendet.
- **Kein persönlicher Empfehlungslink, keine Prämie.**
- **Keine Gesundheitsdaten.** Kein Formular erfragt Angaben zur Gesundheit; was an einem Stand gemessen wird,
  bleibt bei der Person.
- **Nicht mandantenfähig.** Eine Plattform, eine Datenbasis; Sichtbarkeit regeln Prüfung, Region und Rechte.

## Sprache und Ton
- Oberfläche, Fehlermeldungen, Dokumentation: Deutsch (Schweiz), **ss statt ß**, Guillemets «…».
- Organisationen werden mit «ihr» angesprochen, Mitglieder mit «du».
- Beträge `CHF 1'234.50`, Datum `26.11.2026`.
- Jeder fiktive Verein, Anlass, Betrieb und Beitrag ist als Beispiel gekennzeichnet.
- Fehlermeldungen sagen, was passiert ist und was die Person tun kann, ohne Stacktrace.
