# Implementation Plan — 06 Produktivschaltung
Aufgaben und Nummern aus dem «Umsetzungsplan Plattform auf Infomaniak (v1)», Kapitel 4. Rollen, Arbeitsweise und
Nachweis wie in Spec 01, `tasks.md`.

## Welle 2 — Webseite in die Plattform (Anteil dieser Spec)
- [ ] 2.8 Rechtsseiten [Frontend] [Fabian]
  - Texte der bisherigen Startseite, Hoster und Schriften nachgeführt; Betreiberangaben.
  - _Anforderungen: 2.1, 2.3_
  - _Abhängig von: Spec 03: 2.3_

## Welle 6 — Produktivschaltung
- [ ] 6.1 Ort der echten Daten umsetzen [Fabian] [Infra]
  - Bei eigenem Server: einrichten, Deploy-Key, Umzug per Dump und Wiederherstellung, DNS.
  - _Anforderungen: 3.1_

- [ ] 6.2 Export und Löschung [Backend]
  - Vollständiger Export; Konto löschen mit Pseudonymisierung im Protokoll; Löschung einer Organisation.
  - _Anforderungen: 1.1, 1.2, 1.3_

- [ ] 6.3 Rechtstexte und Betreiber abschliessen [Fabian]
  - Auftragsbearbeitung mit den Hostern geklärt.
  - _Anforderungen: 2.2, 3.5_
  - _Abhängig von: 2.8_

- [ ] 6.4 Demodaten entfernen [Infra] [Fabian]
  - Profil `demo` aus, Verwaltungszugang aus dem Secret, Datenbank ohne Demo-Konten.
  - _Anforderungen: 3.2_
  - _Abhängig von: 6.1_

- [ ] 6.5 Abnahme [QA] [Fabian]
  - Kriterien der Stufen 1 und 2 als Checkliste, Wiederherstellungs-Probe, Sicherheitsprüfung von aussen.
  - _Anforderungen: 3.3_
  - _Abhängig von: 6.2, 6.4_

- [ ] 6.6 Öffentlich schalten [Fabian]
  - _Anforderungen: 3.4_
  - _Abhängig von: 6.5_
