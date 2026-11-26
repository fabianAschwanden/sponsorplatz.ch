# Implementation Plan — 04 Leads und Cockpit
Aufgaben und Nummern aus dem «Umsetzungsplan Plattform auf Infomaniak (v1)», Kapitel 4. Rollen, Arbeitsweise und
Nachweis wie in Spec 01, `tasks.md`. Die Aufgabe 4.8 des Plans ist geteilt: 4.8a (Formular, Spec 02) und 4.8b
(Kontakt mit Einwilligung, diese Spec).

## Welle 4 — Stufe 1: Angebot und Vorteile (Anteil dieser Spec)
- [ ] 4.8b Kontakt mit Einwilligung [Backend] [Frontend]
  - Tabelle `lead` mit Einwilligung, Quelle und Widerruf; Liste für die besitzende Organisation.
  - _Anforderungen: 1.1, 1.2, 1.4, 1.5_
  - _Abhängig von: Spec 03: 2.5_

## Welle 7 — Stufe 3: Wirkung
- [ ] 7.1 Kontakte nachführen [Backend] [Frontend]
  - Status Beratung und Kunde; Aufbewahrungsfrist.
  - _Anforderungen: 1.3_
  - _Abhängig von: 4.8b_

- [ ] 7.2 Cockpit [Backend] [Frontend]
  - Betrag, Kontakte, Beratungen, Kunden je Engagement und gesamt.
  - _Anforderungen: 2.1, 2.2, 2.3_
  - _Abhängig von: 7.1, Spec 03: 5.1_

- [ ] 7.3 Erinnerung vor dem Ablauf [Backend]
  - 90 Tage vor dem Ende eines Engagements.
  - _Anforderungen: 3.1, 3.2_
  - _Abhängig von: Spec 03: 5.1_

- [ ] 7.4 Sicht der Agentur und der Marke [Backend] [Frontend]
  - Je Marktgebiet zusammengezählt.
  - _Anforderungen: 4.1, 4.2, 4.3_
  - _Abhängig von: Spec 03: 5.1_

- [ ] 7.5 Anlass-Seite [Backend] [Frontend]
  - Mit QR-Code und Wettbewerb.
  - _Anforderungen: 5.1, 5.2, 5.3_
  - _Abhängig von: 4.8b, Spec 03: 4.1_

- [ ] 7.6 Weitersagen und Reichweite [Backend] [Frontend]
  - Reichweite je Seite und Tag ohne Personenbezug.
  - _Anforderungen: 6.1, 6.2, 7.1, 7.2, 7.3_
  - _Abhängig von: Spec 02: 4.5_
