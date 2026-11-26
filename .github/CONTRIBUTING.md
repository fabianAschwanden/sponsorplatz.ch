# Mitarbeit

Die Arbeitsweise steht in [`CLAUDE.md`](../CLAUDE.md). Sie gilt für Menschen und Agenten gleich.
Hier nur der Einstieg.

## Einrichten

1. Java 25 und Maven 3.9+ installieren, dazu `gitleaks` und `jq` (`brew install gitleaks jq`).
2. Nach dem Klonen die Git-Hooks einschalten: `git config core.hooksPath .githooks`.
3. App starten: `mvn spring-boot:run`, dann http://localhost:8080.

## Ablauf einer Aufgabe

1. Aufgabe in `.kiro/specs/<nr>-<name>/tasks.md` wählen; nur Specs umsetzen, die auf `main` liegen.
2. Reservieren: Branch `<agent>/<spec>-<aufgabe>-<stichwort>` vom aktuellen `origin/main`, pushen,
   Draft-PR «[<agent>] <spec> <aufgabe> <stichwort>» eröffnen.
3. Zuerst der Test oder das Szenario mit `@req-x.y`, dann der Code.
4. `mvn verify` lokal grün, auf `origin/main` rebasen, dann «Ready for review». Entwürfe bekommen
   keine CI.
5. Gemergt wird mit «Squash and merge». Wer merged, steht in `CLAUDE.md`.

Direkte Pushes auf `main` sperrt der Pre-Push-Hook.

## Vor dem PR

- [ ] Akzeptanzkriterien im PR genannt, je mit Test oder Szenario
- [ ] Checkbox in `tasks.md` abgehakt
- [ ] `mvn verify` grün, `ArchitekturRegelnTest` grün
- [ ] Migration additiv, bestehende Migrationen unverändert
- [ ] Nichts aus `target/` oder `data/` im Commit, keine Geheimnisse, keine echten Personendaten
