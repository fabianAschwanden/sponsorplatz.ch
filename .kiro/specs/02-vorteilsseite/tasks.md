# Implementation Plan — 02 Vorteilsseite
Aufgaben und Nummern aus dem «Umsetzungsplan Plattform auf Infomaniak (v1)», Kapitel 4. Rollen, Arbeitsweise und
Nachweis wie in Spec 01, `tasks.md`. Die Aufgabe 4.8 des Plans ist geteilt: 4.8a (Formular, diese Spec) und 4.8b
(Kontakt mit Einwilligung, Spec 04).

## Welle 2 — Webseite in die Plattform (Anteil dieser Spec)
- [ ] 2.9 Vorteilsseite als Template [Frontend]
  - Unter `/kunde` und `/kunde/`, Inhalt unverändert; Template im Ordner `vorteilsseite/`.
  - _Anforderungen: 10.1, 10.2, 10.3, 10.4_
  - _Abhängig von: Spec 03: 2.3_

## Welle 4 — Stufe 1: Angebot und Vorteile (Anteil dieser Spec)
- [ ] 4.5 Vorteilsseite je Verein [Backend] [Frontend]
  - Tabellen `vorteilsseite` und `vorteil`, öffentliche Seite mit kurzem Link, neutral oder mit Namen.
  - _Anforderungen: 1.1, 1.2, 1.4, 2.1, 2.2, 2.3, 2.4_
  - _Abhängig von: 2.9_

- [ ] 4.6 Weiche und Einlösen aus der Datenbank [Frontend]
  - Ohne JavaScript beide Wege.
  - _Anforderungen: 1.3, 3.1, 3.2, 3.3, 4.1, 4.2_
  - _Abhängig von: 4.5_

- [ ] 4.7 Pflege und Freigabe [Backend] [Frontend]
  - Pflege durch die Agentur, Freigabe, Protokoll jeder Änderung.
  - _Anforderungen: 7.1, 7.2, 7.3_
  - _Abhängig von: 4.5_

- [ ] 4.8a Beratungsanfrage: Formular [Backend] [Frontend]
  - Formular, Hinweise, Übergabe an den Fachbereich `lead`; Benachrichtigung ohne Personendaten.
  - _Anforderungen: 5.1, 5.2, 5.3, 5.4, 5.5_
  - _Abhängig von: 4.5, Spec 04: 4.8b, Spec 03: 2.5_

- [ ] 4.9 Teilen-Kit [Backend] [Frontend]
  - Link, Text für den Chat, Mail-Vorlage, A4-Plakat mit QR-Code als PDF.
  - _Anforderungen: 6.1, 6.2, 6.3_
  - _Abhängig von: 4.5_

- [ ] 4.10 Demodaten [Backend]
  - Turnverein Musterdorf mit Paketen, Anlass und Vorteilsseite; `/kunde/` zeigt ihn.
  - _Anforderungen: 9.1, 9.2_
  - _Abhängig von: 4.5, Spec 03: 4.3_

- [ ] 4.11 Zugänglichkeit [QA]
  - axe für alle neuen öffentlichen Seiten, Handy, hell und dunkel.
  - _Anforderungen: 8.1, 8.2_
  - _Abhängig von: 4.5, 4.6, 4.8a, 4.9_

- [ ] 4.12 Validierung mit Mitgliedern und Vorstand [Fabian]
  - 5 bis 8 Mitglieder, 2 bis 3 Vorstandsmitglieder. Bis zum Entscheid E5b senden Testpersonen die
    Beratungsanfrage mit erfundenen Angaben.
  - _Abhängig von: 4.5, 4.6, 4.7, 4.8a, 4.9, 4.10_
