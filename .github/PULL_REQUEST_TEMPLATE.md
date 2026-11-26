## Aufgabe

<!-- Spec und Aufgabe, z. B. «02 4.5 Vorteilsseite je Verein». Ein PR schliesst eine Aufgabe. -->

## Akzeptanzkriterien

<!-- Je Kriterium: @req-x.y und der Test oder das Szenario, das es belegt. Manuelle Nachweise verlinken. -->

| Kriterium | Beleg |
| --- | --- |
| `@req-` | |

## Was sich ändert

<!-- Kurz. Bei Änderungen an requirements.md: Kriterium, alt → neu, Grund. -->

## Auslegungen (Review entscheidet)

<!-- Technische Auslegungen innerhalb der Spec. «Keine», wenn es keine gibt. -->

## Offen für Fabian

<!-- Nur echte Entscheide: was eine Anforderung, Rechte oder Produktverhalten ändert. «Nichts», wenn nichts offen ist. -->

## Prüfliste

- [ ] Checkbox in `tasks.md` abgehakt
- [ ] `mvn verify` lokal grün (inklusive `ArchitekturRegelnTest`)
- [ ] Bestandsschutz: bestehendes Verhalten nur geändert, wo ein Kriterium es verlangt
- [ ] Jede schreibende Route prüft das Recht; Admin-Routen tragen `@PreAuthorize`
- [ ] Keine Entity im Model, Templates nutzen nur View-Records
- [ ] Migration additiv, bestehende Migrationen unverändert, SQL läuft auf H2 und PostgreSQL
- [ ] Neue oder geänderte Seiten: axe ohne neuen Befund, am Handy geprüft
- [ ] Keine Geheimnisse, keine Server-Adressen, keine echten Personendaten, keine echte Marke
