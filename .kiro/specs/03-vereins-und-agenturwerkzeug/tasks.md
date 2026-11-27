# Implementation Plan — 03 Vereins- und Agenturwerkzeug
Aufgaben und Nummern aus dem «Umsetzungsplan Plattform auf Infomaniak (v1)», Kapitel 4. Rollen, Arbeitsweise und
Nachweis wie in Spec 01, `tasks.md`.

## Welle 2 — Webseite in die Plattform (Anteil dieser Spec)
- [x] 2.3 Stylesheet, Schrift und Fragment der öffentlichen Seiten [Frontend]
  - Gemeinsames Stylesheet, Schrift lokal, Fragment mit Kopf, Fuss und Farbschema.
  - _Anforderungen: 16.3_

- [ ] 2.4 Startseite als Template [Frontend] [Fabian]
  - Texte zur Reihenfolge der Freischaltung angepasst; Tests des Bestands zur Startseite nachgezogen.
  - _Anforderungen: 16.1, 16.2, 16.4, 16.5_
  - _Abhängig von: 2.3_

- [x] 2.5 Pilot-Anfrage [Backend]
  - Formular unter `/pilot`, Speicherung, Einwilligung, Drosselung (auch für das Kontaktformular),
    Benachrichtigung der Verwaltung ohne Personendaten.
  - _Anforderungen: 1.1, 1.2, 1.3, 1.4, 13.1, 13.2, 14.1, 14.2, 15.2_
  - _Abhängig von: 2.3_

- [x] 2.6 Prüfung [Backend]
  - Ablehnungsgrund, Protokoll, nichts Ungeprüftes ist sichtbar; Mail mit dem Ergebnis.
  - _Anforderungen: 2.1, 2.2, 2.3, 2.4, 2.5, 15.1_

- [ ] 2.7 Zugang nach der Zusage [Backend] [Frontend]
  - Zusage legt die Organisation an und lädt die Kontaktperson ein; Vormerken, wenn die Region nicht im Pilot
    ist.
  - _Anforderungen: 3.1, 3.2, 3.3_
  - _Abhängig von: 2.5, 2.6, Spec 01: 2.2_

- [ ] 2.10 Bildvergleich und Zugänglichkeit [QA]
  - Bisherige Seiten gegen die Plattform bei 360 und 1280 px, hell und dunkel; alle bestehenden Adressen; axe
    ohne neuen Befund.
  - _Anforderungen: 16.6_
  - _Abhängig von: 2.4, Spec 02: 2.9, Spec 06: 2.8_

## Welle 4 — Stufe 1: Angebot und Vorteile (Anteil dieser Spec)
- [ ] 4.1 Vereinsprofil [Backend] [Frontend]
  - Sportart, Mitgliederzahl, Region aus PLZ; Formular für Anlässe.
  - _Anforderungen: 4.1, 4.2, 4.3_
  - _Abhängig von: Spec 01: 2.2_

- [ ] 4.2 Sponsor-Arten und Programme [Backend] [Frontend]
  - Agentur, lokaler Partner, Marke; Marktgebiet; Programme.
  - _Anforderungen: 9.1, 9.2, 9.3, 9.4_

- [ ] 4.3 Gegenleistungen [Backend] [Frontend]
  - Katalog mit zehn Typen, Paket aus Gegenleistungen mit Reichweite und Verfügbarkeit.
  - _Anforderungen: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ] 4.4 Aktivierbarkeit, Vorschau und Probepaket [Backend] [Frontend]
  - _Anforderungen: 6.1, 6.2, 6.3, 7.1, 7.2_
  - _Abhängig von: 4.3, 2.7_

## Welle 5 — Stufe 2: Finden und Zusagen
- [ ] 5.1 Engagement als eigenes Objekt [Backend]
  - Entsteht bei der Zusage; bestehende angenommene Anfragen werden überführt.
  - _Anforderungen: 8.2, 8.4_
  - _Abhängig von: 4.3_

- [ ] 5.2 Ansicht des Engagements für beide Seiten [Frontend]
  - Mit Laufzeit und Betrag.
  - _Anforderungen: 8.1, 8.3_
  - _Abhängig von: 5.1_

- [ ] 5.3 Suche [Backend] [Frontend]
  - Region, Sportart, Aktivierbarkeit, Freitext; nur geprüfte Vereine in freigeschalteten Regionen.
  - _Anforderungen: 10.1, 10.2, 10.3, 10.4_
  - _Abhängig von: 4.1, 4.3, Spec 01: 2.2_

- [ ] 5.4 Geführte Anfrage auf Pakete mit Gegenleistungen [Backend]
  - Kontakt-Anfrage des Vereins nur an verbundene Sponsoren.
  - _Anforderungen: 11.1, 11.2, 11.3_
  - _Abhängig von: 5.1_

- [ ] 5.5 Vereine einladen [Backend] [Frontend]
  - Die Einladung führt in die Pilot-Anfrage.
  - _Anforderungen: 12.1, 12.2_
  - _Abhängig von: 2.5_
