# Implementation Plan — 01 Übernahme und Betrieb
Aufgaben und Nummern aus dem «Umsetzungsplan Plattform auf Infomaniak (v1)», Kapitel 4. Rollen: Infra, Backend,
Frontend und QA sind Rollen der Agenten; Aufgaben mit [Fabian] brauchen einen Entscheid, eine Freigabe oder
seinen Zugang. Eine Aufgabe ist ein Branch und ein PR (`CLAUDE.md`). Steht ein Wert auf «[offen: …]», wird er
nicht geraten; die Aufgabe rechnet mit der Annahme aus `requirements.md` oder wartet auf den Entscheid. Jede
Aufgabe liefert für ihre Anforderungen den Nachweis nach `tech.md` und hakt ihre Checkbox im selben PR ab.

## Welle 0 — Entscheide und Spike
- [ ] 0.1 Entscheide mit Frist «Welle 1» und «Gate 0» [Fabian]
  - E1, E2, E4, I1, U4, U5 (Tabelle in `requirements.md`).

- [x] 0.2 Plattform lokal als Container wie im Betrieb [Infra]
  - PostgreSQL 17, Profil `prod`, Secrets als Dateien, Dateisystem nur lesbar, nicht privilegierter Benutzer,
    ohne Capabilities. Alle Migrationen laufen durch, Login und ein Klickpfad gehen. Befunde als Liste.
  - _Anforderungen: 3.1, 5.2_

- [x] 0.3 Speicherbedarf messen, JVM-Grenzen festlegen [Infra]
  - Leerlauf und nach dem Klickpfad.
  - _Anforderungen: 5.3, 5.5_
  - _Abhängig von: 0.2_

- [x] 0.4 Verfügbaren Speicher und freie Platte des Servers ablesen [Fabian]

- [x] 0.5 Entwurf der Compose-Datei gegen die Policy des Servers prüfen [Infra]
  - Mit allen Routern und Middlewares.
  - _Anforderungen: 4.4_
  - _Abhängig von: 0.2_

- [x] 0.6 Profile, Demo-Seed und Mail klären [Backend]
  - Starten `prod` und `demo` zusammen? Ist der Demo-Seed wiederholbar? Geht Mail vom Container zum Postfach?
  - _Abhängig von: 0.2_

## Welle 1 — Fundament
### Methode im Repo
- [ ] 1.1 Leitplanken ausrollen [Infra] [Fabian]
  - Hooks, geschützte Pfade, Secret-Scan, `ci-fehler.yml`; `main` schützen; `CLAUDE.md` und `AGENTS.md`.
  - _Anforderungen: 1.4_

- [x] 1.2 Steering [Backend]
  - `product.md`, `tech.md`, `structure.md`.

- [x] 1.3 Specs 01 bis 06 anlegen [Backend] [Fabian]
  - `requirements.md` aus den 40 Anforderungen des Konzepts v5, `design.md`, `tasks.md` aus dem Umsetzungsplan;
    Dokumente des Bestands nach `docs/bestand/`.
  - _Abhängig von: 1.2_

- [x] 1.4 Rückverfolgung und Regeln [Backend]
  - `ReqAbdeckungTest`, `NurSpecTest`, `RechtematrixSpecTest`, Hexagon-Regeln für neue Pakete.
  - _Anforderungen: 1.1, 1.2, 2.1, 2.2, 2.3_
  - _Abhängig von: 1.3_

- [x] 1.5 CI teilen [Infra]
  - Nebeneinander laufende Jobs, Abkürzung «nur Spec», Entwürfe ohne CI; den roten Workflow
    «Architektur-Diagramme» reparieren oder entfernen. Liefert `.github/scripts/nur-spec.py`; danach läuft
    `NurSpecTest`.
  - _Anforderungen: 1.3, 1.5_
  - _Abhängig von: 1.1_

### Betrieb
- [x] 1.6 Anpassungen an der Plattform [Backend]
  - Anpassungen 1 bis 6 und 8 aus `design.md`, je mit Test.
  - _Anforderungen: 3.1, 3.3, 4.1, 4.2, 4.3, 5.1, 5.3, 7.1, 8.4, 10.2_
  - _Abhängig von: 0.2, 0.3_

- [x] 1.7 Compose-Datei und Datenbank-Image [Infra]
  - `deploy/infomaniak/compose.yml`; Test, der Secrets, Netze, Router, Healthcheck, Härtung und Limits prüft.
  - _Anforderungen: 3.2, 4.4, 4.5, 5.2, 8.1, 8.2_
  - _Abhängig von: 0.5_

- [x] 1.8 Deploy-Workflow [Infra]
  - CI-Nachweis, Secrets, Budget, Bundle, Prüfungen, Rollback.
  - _Anforderungen: 3.4, 5.4, 6.1, 6.2, 6.3, 6.4, 6.5, 8.3, 9.2_
  - _Abhängig von: 1.7_

- [x] 1.9 Deploy-Key, Variablen, Secrets, Postfach [Fabian]

- [ ] 1.10 Erster Deploy, Rollback-Probe, Messung [Infra] [Fabian]
  - Erster Deploy auf die Vorschau-Adresse, zweiter Deploy mit Rollback-Probe, Speicher im Betrieb messen,
    andere Apps prüfen.
  - _Anforderungen: 5.5, 6.6_
  - _Abhängig von: 1.6, 1.8, 1.9_

- [ ] 1.11 Sicherung und Wiederherstellungs-Probe [Infra] [Fabian]
  - _Anforderungen: 7.2, 7.3, 7.4_
  - _Abhängig von: 1.10_

- [ ] 1.12 Überwachung [Infra] [Fabian]
  - Container läuft, Seite von aussen erreichbar, Alter des jüngsten Dumps.
  - _Anforderungen: 9.1_
  - _Abhängig von: 1.10_

- [ ] 1.13 Betriebsdokumentation [Infra]
  - `docs/betrieb/`: Deploy, Rollback, Wiederherstellung, Geheimnisse rotieren; README.
  - _Anforderungen: 7.5, 9.3_
  - _Abhängig von: 1.11_

## Welle 2 — Webseite in die Plattform (Anteil dieser Spec)
- [ ] 2.1 Rückbau [Backend]
  - Je ein PR: Speicher- und Sicherungs-Adapter anderer Cloud-Anbieter, Infrastruktur-Definitionen,
    Cloud-Profile, ungenutzte Abhängigkeiten, eingebundene Skripte des externen Fehlermeldedienstes. Die lokale
    Schrift liefert Spec 03: 2.3.
  - _Anforderungen: 10.1, 10.3, 12.1, 12.2_
  - _Abhängig von: 1.10_

- [x] 2.2 Regionen und Schalter [Backend]
  - Tabellen `region`, `region_plz`, `region_freischaltung`, eine Region «Pilot»; Zahlung, Vertrag und Rechnung,
    Aufgaben-Engine und Matching abgeschaltet.
  - _Anforderungen: 11.1, 11.2, 11.3, 13.1, 13.2_

## Welle 3 — Domain-Umzug
- [ ] 3.1 Router für die Domain und `www`, Weiterleitung `/sca` [Infra] [Fabian]
  - _Anforderungen: 14.2_
  - _Abhängig von: 1.10_

- [ ] 3.2 Bisherige Pilot-Anmeldungen übernehmen [Fabian] [Backend]
  - _Anforderungen: 14.4_
  - _Abhängig von: Spec 03: 2.7_

- [ ] 3.3 Alte Anmeldung sperren, DNS umstellen, Live-Prüfung [Fabian] [Infra]
  - _Anforderungen: 14.1, 14.3_
  - _Abhängig von: 3.1, 3.2, Spec 03: 2.10_

- [ ] 3.4 Alten Betrieb abbauen [Fabian] [Infra]
  - Frühestens 14 Tage nach 3.3.
  - _Anforderungen: 14.5_
  - _Abhängig von: 3.3_
