# Sponsorplatz – Arbeitsweise für Agenten (Claude Code, GitHub Copilot)

Sponsoring-Plattform für Sport und Gesundheit: Der Verein beschreibt, was er bietet, der Sponsor
findet Pakete und sieht, was daraus wird, das Mitglied erfährt, was die Partnerschaft ihm bringt.
Entwickelt wird spec-driven im Kiro-Stil (`.kiro/`), umgesetzt mit Claude Code und GitHub Copilot.

## Immer geltende Vorgaben

@.kiro/steering/product.md
@.kiro/steering/tech.md
@.kiro/steering/structure.md

## Specs

- `.kiro/specs/<nr>-<name>/requirements.md` – User Stories und EARS-Kriterien (Nummern x.y)
- `design.md` – Architektur, Komponenten, Datenmodell, Fehler, Tests
- `tasks.md` – Aufgaben als Checkboxen mit `_Anforderungen: x.y_` und `_Abhängig von:_`; Wellen und
  Gates stehen dort
- Dokumente des Bestands (frühere Specs, ADRs): `docs/bestand/`

## Arbeitsweise

1. **Nur freigegebene Specs umsetzen.** Freigegeben ist, was gemergt auf `main` liegt. Keine Aufgabe
   aus einem offenen Spec-PR beginnen.
2. **Eine Aufgabe = ein Branch + ein PR.** Branch `<agent>/<spec>-<aufgabe>-<stichwort>`, z. B.
   `claude/02-4.5-vorteilsseite`. Aufgaben anderer Wellen nicht vorziehen.
3. **PR-Beschreibung** nennt die erfüllten Akzeptanzkriterien (`@req-x.y`) und die Szenarien bzw.
   Tests dazu; manuelle Nachweise werden verlinkt.
4. **Checkbox in `tasks.md`** im selben PR abhaken.
5. **Erkenntnisse** (Spikes, Abweichungen vom Design) im selben PR in `design.md` nachtragen.
6. **Requirements nie stillschweigend ändern.** Jede Änderung an `requirements.md` im PR
   ausdrücklich als Änderung ausweisen (Kriterium, alt → neu, Grund); bestehende Nummern nie
   umnummerieren, neue hinten anhängen.
7. **Offenes nicht raten.** Steht ein Wert als `[offen: …]` in der Spec, nachfragen statt erfinden.
8. **Nur über die Pipeline deployen.** Kein Zugriff auf Server oder Secrets aus der Sitzung; Deploy
   nur über den GitHub-Actions-Workflow.
9. **Das Repo ist öffentlich.** Keine Server-Adressen, keine Betriebsdetails und keine echten
   Personendaten in Dateien, PR-Texten, Kommentaren oder Commit-Messages. Marken und Programme
   einzelner Sponsoren sind Daten, nicht Code: Code, Tests, Demodaten und Dokumente nennen keine
   echte Marke.

## Bestandsschutz

Die Plattform ist ein bestehendes System. Für sie gilt die Methode mit einer Anpassung:

1. Das Verhalten des Bestands gilt als Ausgangslage. Belegt ist es durch die vorhandenen Tests; es
   wird nicht nachträglich spezifiziert.
2. Wer bestehendes Verhalten ändert, schreibt zuerst ein EARS-Kriterium in eine Spec und belegt es
   mit einem Test oder Szenario, das `@req-x.y` trägt.
3. Neue Fachbereiche folgen der Methode vollständig: Kriterium, Test oder Szenario mit `@req-x.y`,
   Pakete nach `structure.md`.
4. `ReqAbdeckungTest` zählt nur die Kriterien der Specs.

## Zwei Agenten parallel

Claude Code und GitHub Copilot (Agent-Modus, eigene VS-Code-Instanz) arbeiten gleichzeitig.

1. **Eigener Ordner je Agent.** Claude Code in `~/git/sponsorplatz.ch`, Copilot im Worktree
   `~/git/sponsorplatz.ch-copilot`. Nie im Ordner des anderen arbeiten. Weitere Worktrees heissen
   `~/git/sponsorplatz.ch-<spec>-<aufgabe>`.
2. **Aufgabe reservieren, bevor du anfängst.** `git fetch` und
   `gh pr list --state open --search "<aufgabe>"` prüfen; gibt es für die Aufgabe schon einen
   Branch oder PR, ist sie vergeben. Sonst Branch vom aktuellen `origin/main` anlegen, sofort
   pushen und einen **Draft-PR** «[<agent>] <spec> <aufgabe> <stichwort>» eröffnen – der Draft-PR
   ist die Reservierung.
3. **Keine Überschneidung.** Aufgaben, die dieselben Dateien ändern (ausser dem eigenen Haken in
   `tasks.md`), nicht gleichzeitig bearbeiten; `_Abhängig von_` beachten.
4. **Vor «Ready for review»** auf aktuellen `origin/main` rebasen und die Tests lokal laufen
   lassen. Entwürfe bekommen keine CI.

## Definition of Done

- Die Akzeptanzkriterien der Aufgabe sind erfüllt und mit `@req-x.y` belegt.
- `mvn verify` ist lokal grün, die CI auch (inklusive Secret-Scan).
- Schemaänderungen sind additiv (siehe «Migrationen»).
- Neue oder geänderte Seiten haben keinen neuen axe-Befund.
- Ergänzend gilt `tech.md`.

Gemergt wird nur mit «Squash and merge»; Branches werden nach dem Merge gelöscht. Wer merged:

- Umsetzungs-PRs merged Claude Code, wenn die CI grün ist (inklusive Secret-Scan), das Review von
  Claude Code keinen Muss-Befund hat und unter «Offen für Fabian» nichts steht. Technische
  Auslegungen innerhalb der Spec stehen im PR im Abschnitt «Auslegungen (Review entscheidet)»; das
  Review entscheidet sie und nennt sie im Bericht an Fabian.
- «Offen für Fabian» heisst nur echte Entscheide: was eine Anforderung, Rechte oder
  Produktverhalten ändert.
- Fabian merged selbst: Spec-PRs mit neuen Anforderungen oder Entscheiden (die Freigabe ist der
  Merge), PRs zu Gates und zu Aufgaben **[Fabian]** sowie jeden PR mit Punkten «Offen für Fabian».
- Ändert ein Umsetzungs-PR `requirements.md` oder `design.md` nur nach einem schon gefallenen und
  datierten Entscheid von Fabian (Entscheid mit Datum im Text), merged ihn ebenfalls Claude Code.
- Copilot erhält seine Aufträge von Claude Code; die Reihenfolge der Aufgaben bleibt die der
  `tasks.md`.

## Leitplanken

- `.claude/hooks/harness/`, `.githooks/`, `.gitleaks.toml`, `secret-scan.yml` und `ci-fehler.yml`
  kommen aus der Harness-Vorlage und werden von dort ausgerollt, nicht hier geändert. Dem Repo
  gehören `.claude/harness.yml` und die Liste der bewachten Workflows in `ci-fehler.yml`.
- Nach dem Klonen einmal `git config core.hooksPath .githooks` setzen. Der Pre-Commit-Hook braucht
  `gitleaks`, der Pre-Push-Hook sperrt direkte Pushes auf `main`.
- Geschützte Pfade (`.claude/harness.yml`) fragen vor jeder Änderung nach. Dazu gehören die
  Migrationen: Bestehende Dateien unter `src/main/resources/db/` werden nie geändert.

## Stack

| Schicht | Wahl |
| --- | --- |
| Sprache | Java 25 |
| Framework | Spring Boot 3.5.x |
| Oberfläche | Thymeleaf, serverseitig gerendert (kein SPA) |
| Datenbank | PostgreSQL 17 im Betrieb, H2 in der Entwicklung und in den Bestandstests |
| Schema | Flyway, versioniert und additiv |
| Build | Maven 3.9+ |
| Betrieb | Container, Deploy über GitHub Actions |

Sprache: Deutsch in Code, Logs und Specs (Schweizer Schreibweise, kein ß). Locale `de_CH`, Währung
CHF, Datum `dd.MM.yyyy`. Organisationen werden mit «ihr» angesprochen, Mitglieder mit «du».

## Code-Regeln des Bestands

Durchgesetzt von `ArchitekturRegelnTest` (ArchUnit) und `I18nLintTest`; vor jedem Umbau
`mvn test -Dtest=ArchitekturRegelnTest` laufen lassen. Eine Regel wird nie stillschweigend
gelockert.

- **Fachpakete.** Jedes Paket unter `ch.sponsorplatz.<paket>` hält Entities, Repositories, Services,
  Controller und DTOs zusammen. Querschnitt (Mail, PDF, Storage, Konfiguration) liegt in `shared/`,
  und `shared/` importiert nichts aus Fachpaketen. Templates liegen im Ordner ihres Pakets.
- **Entities verlassen die Service-Schicht nicht.** Ins Model kommen nur Records (`*View`,
  `*FormDto`) aus dem Fachpaket, nie eine JPA-Entity. Jeder neue View bekommt einen `<Name>ViewTest`.
- **Controller rufen Services auf**, nicht Repositories. Sie fangen keine Fachfehler; das
  übernimmt der `GlobalExceptionHandler`.
- **Services werfen** `NotFoundException` (404), `IllegalArgumentException` (400),
  `IllegalStateException` (409) und `AccessDeniedException` (403).
- **Jede schreibende Route prüft das Recht** (`AccessControl` oder `@PreAuthorize`). Datensätze
  werden über den Slug in der Adresse bestimmt, nicht über eine `id` im Formular.
- **Namen in deutscher Fachsprache**, ohne Abkürzungen: `speichere`, `findeNachSlug`,
  `istVerifiziert`. Eine Aufgabe pro Methode, Guard Clauses statt tiefer Verschachtelung.
- **Keine Magic Strings:** Schlüssel der Model-Attribute stehen in `ModelAttributeNames`.
- **Texte der Oberfläche** stehen in `messages_*.properties`, nicht im Template.
- **Tests:** `<Klasse>Test` für Unit-, Web- und Repository-Tests, `<Klasse>IT` für Integration;
  AssertJ und Mockito.

### Migrationen

- Additiv, nie rückwirkend: neue Spalte, dann Daten übernehmen, die alte Spalte erst in einem
  späteren Deploy entfernen. Ein Rollback setzt das Image zurück, nicht die Datenbank.
- SQL läuft auf H2 und PostgreSQL; was nur PostgreSQL kann, liegt in `db/migration_postgres`.
- `ddl-auto=validate` gilt in allen Profilen.

## Häufige Befehle

```sh
mvn spring-boot:run                              # lokal, http://localhost:8080
mvn test                                         # alle Tests ohne Browser
mvn test -Dtest=OrganisationServiceTest          # einzeln
mvn verify                                       # wie die CI, mit JaCoCo
mvn verify -P e2e -Dit.test='SmokeIT,A11ySmokeIT'  # Browser-Prüfungen (Playwright, axe)
docker compose -f docker/docker-compose.yml up -d postgres mailhog   # nur die Hilfsdienste
rm -rf data/                                     # H2 zurücksetzen
```

## Wo finde ich was

| Thema | Ort |
| --- | --- |
| Zweck, Rollen, Abgrenzung | `.kiro/steering/product.md` |
| Stack und nicht verhandelbare Regeln | `.kiro/steering/tech.md` |
| Pakete: Bestand und neue Fachbereiche | `.kiro/steering/structure.md` |
| Anforderungen, Entwurf, Aufgaben | `.kiro/specs/` |
| Datenmodell, Rollenkonzept, Teststrategie und ADRs des Bestands | `docs/bestand/` |
| Architektur-Diagramme (C4) | `docs/architektur/` |
| Mitarbeit, Checkliste für PRs | `.github/CONTRIBUTING.md`, `.github/PULL_REQUEST_TEMPLATE.md` |
