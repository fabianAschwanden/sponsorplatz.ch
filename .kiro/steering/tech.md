---
inclusion: always
---

# Technik und verbindliche Regeln

## Stack
| Bereich | Wahl | Hinweis |
| --- | --- | --- |
| Backend | Java 25, Spring Boot 3.5, Maven | ein Modul, ein Deployable |
| Oberfläche | Thymeleaf, serverseitig gerendert, wenig CSS und JavaScript | kein SPA; öffentliche Seiten sind ohne JavaScript lesbar |
| Persistenz | PostgreSQL 17, Spring Data JPA, Flyway | Hibernate nur `validate`; der Bestand entwickelt und testet zusätzlich gegen H2 |
| Dateien | lokaler Speicher auf einem benannten Volume | Logos, Fotos, Dokumente; kein Objektspeicher |
| Anmeldung | Formular-Login der Plattform, TOTP, OIDC | kein eigener Identitätsdienst; Mitglieder melden sich nie an |
| Mail | SMTP über ein Postfach in der Schweiz | Versand asynchron; ein Ausfall des Mail-Servers macht die App nicht krank |
| Betrieb | Docker Compose (`deploy/infomaniak/compose.yml`) hinter einem Reverse Proxy, Dienste `app` und `db` | Details unter «Plattform und Betrieb» |
| Sicherung | nächtlicher `pg_dump` und Medien in einem Volume, abgeholt von der zentralen Sicherung | Wiederherstellung als Probe geprüft |
| Tests | JUnit 5, Cucumber (Features mit Tag `@req-<x.y>`), Playwright und axe für Browser-Prüfungen, Testcontainers (PostgreSQL), ArchUnit | Details unter «Tests und Qualität» |

Offen ist der Entscheid E1 (Stack behalten oder portieren); die Specs rechnen mit «behalten».

## Nicht verhandelbare Regeln
1. **Schema nur über Flyway, nie rückwirkend, nur additiv.** Bestehende Migrationen werden nie geändert, nur
   neue angehängt. Ein Rollback setzt die Images zurück, nicht die Datenbank: Jede Migration muss mit dem
   Vorgänger-Release zusammen laufen. Eine Spalte kommt in einem Deploy dazu und fällt frühestens in einem
   späteren weg. Ein Upgrade von PostgreSQL ist ein eigener Deploy nach einer frischen Sicherung.
2. **Keine Geheimnisse im Code und nicht in Umgebungsvariablen.** Secrets liegen nur als Dateien unter
   `/run/secrets/`, Quelle sind die GitHub-Secrets des Repos. Nie als `environment` oder `env_file` in Compose.
   Neuer Code liest keine Umgebungsvariablen (`System.getenv`).
3. **Daten bleiben in der Schweiz.** App, Datenbank, Medien und Sicherung liegen bei Schweizer Anbietern. Die
   Plattform ruft keine Dienste auf, die Besucherdaten ins Ausland senden: keine externen Schriften, kein
   externer Fehlermeldedienst, keine eingebetteten Skripte fremder Herkunft.
4. **Jede Einwilligung ist gespeichert, mit Zeitpunkt und Wortlaut.** Ein Kontakt ohne Einwilligung kann nicht
   entstehen; die Einwilligung ist Pflichtfeld.
5. **Öffentliche Seiten speichern keine Personendaten ohne Einwilligung.** Keine IP-Adresse, kein Cookie, keine
   Kennung für Zählungen. Für die Drosselung gilt nur ein Prüfwert der IP-Adresse, der nach einem Tag entfernt
   wird.
6. **Kontakte gehören der Organisation, die sie erzeugt.** Andere Organisationen sehen sie nicht, auch der Verein
   des Engagements nicht; er sieht nur Zahlen.
7. **Erst nach der Prüfung sichtbar.** Solange eine Organisation nicht geprüft ist, ist nichts von ihr öffentlich
   oder für andere Organisationen sichtbar.
8. **Zeit in UTC speichern**, in der Oberfläche `Europe/Zurich` anzeigen.
9. **Beträge nie als Gleitkomma.** `NUMERIC` in der Datenbank, `BigDecimal` im Code, Währung `CHF`.
10. **Logs nur mit IDs.** Keine Namen, E-Mail-Adressen, Telefonnummern oder Formularinhalte in Logs und in
    Benachrichtigungen an Betreiber. Benachrichtigungen über Kontakte und Anfragen von Mitgliedern enthalten
    keine Personendaten.
11. **Deploy nur über die Pipeline.** Niemand ändert auf dem Server von Hand.
12. **Der Code nennt keine Marke.** Marken, Programme und Partner sind Daten. Dieses Repo ist öffentlich: keine
    Server-Adressen und keine internen Betriebsdetails in Code, Specs, Workflows oder Logs.
13. **Neue Fachbereiche sind hexagonal.** Abhängigkeiten nur `adapter → application → domain`; die Domain ist
    framework-frei. ArchUnit erzwingt das für die neuen Pakete (`structure.md`); der Bestand bleibt unter seinen
    bisherigen Regeln (offen, Entscheid E2; Annahme der Specs).

## Bestandsschutz
Die Plattform ist ein bestehendes System. Für sie gilt:

1. Das Verhalten des Bestands gilt als Ausgangslage. Belegt ist es durch die vorhandenen Tests; es wird nicht
   nachträglich spezifiziert.
2. Wer bestehendes Verhalten ändert, schreibt zuerst ein EARS-Kriterium in eine Spec und belegt es mit einem
   `@req`-Szenario oder -Test.
3. Neue Fachbereiche folgen der Methode vollständig: Kriterium, Cucumber-Szenario mit `@req-x.y`, hexagonale
   Pakete.
4. Die Abdeckungsprüfung (`ReqAbdeckungTest`) zählt nur die Kriterien der Specs.

## Plattform und Betrieb (nicht verhandelbar)
- **Dienste:** `app` (läuft als nicht privilegierter Benutzer, Dateisystem nur lesbar, alle Capabilities entzogen)
  und `db`. Nur `app` ist von aussen erreichbar, die Datenbank nur im internen Netz.
- **Speicherbudget:** Startwert 1'024 MiB und 1.0 CPU über beide Dienste (`app` 768 MiB, `db` 256 MiB). Der
  Deploy bricht ab, wenn die Summe der Limits über dem Budget liegt. Die JVM hat feste Grenzen für Heap,
  Metaspace und Codecache statt eines Prozentwerts. Geändert wird das Budget nur per PR mit Messung.
- **Prüfpfad:** `/livez` auf dem App-Port, ohne Login. Actuator bleibt intern.
- **Vorschau und Demodaten:** Im Modus `vorschau` sind alle Seiten nur mit Vorschau-Zugang erreichbar und tragen
  `noindex`. Das Profil `demo` (Konten mit bekannten Passwörtern) läuft nie zusammen mit dem Modus
  `oeffentlich`; der Deploy verweigert die Kombination.
- **Stufen:** Vorschau mit Demo- und Testdaten zuerst. Wo echte Personendaten liegen dürfen, ist offen
  (Entscheide E5a und E5b); bis dahin gilt: keine echten Anfragen von Mitgliedern.
- **Sicherung:** Der nächtliche Dump und die Medien liegen im Volume `daten`. Ein fehlgeschlagener Dump muss
  auffallen: Die Wiederherstellungs-Probe prüft auch das Alter des jüngsten Dumps.

## Konventionen
- Bestand: Feature-Pakete unter `ch.sponsorplatz.<paket>`, Controller geben View-DTOs an Templates, keine
  JPA-Entities; die Regeln ARCH-01 bis ARCH-22 gelten unverändert (`docs/bestand/specs/TESTSTRATEGIE.md`).
- Templates liegen im Ordner ihres Pakets (ARCH-14). Deutscher Text in HTML-Attributen braucht eine
  Thymeleaf-Bindung, ausser das Template steht auf der Ausnahmeliste (ARCH-15). Öffentliche Seiten sind vorerst
  nur deutsch (offen, Entscheid U3).
- Rechte: Der Bestand prüft in den Controllern über `AccessControl`. Neue Fachbereiche prüfen in der
  `application`-Schicht. Die Grundrechte stehen als Tabelle in Spec 01 und sind dort die einzige Quelle
  (`RechtematrixSpecTest`).
- Fehler: Benutzer sehen eine verständliche Meldung, nie Stacktrace oder Ausnahmetext.

## Tests und Qualität
- Jedes Akzeptanzkriterium einer Spec hat mindestens ein Cucumber-Szenario mit dem Tag `@req-<x.y>` (z. B.
  `@req-3.4` für Requirement 3, Kriterium 4). Feature-Dateien liegen unter
  `src/test/resources/features/<fachbereich>/`.
- Jede Feature-Datei mit `@req`-Tags trägt auf Feature-Ebene genau einen Spec-Tag (`@spec-01` bis `@spec-06`).
  Die Abdeckungsprüfung vergleicht `@req`-Tags nur innerhalb derselben Spec. Feature-Dateien des Bestands ohne
  `@spec`- und ohne `@req`-Tag zählen nicht.
- **Ausnahmen vom Szenario:** Kriterien mit dem Vermerk «Nachweis: manuell [Fabian]» oder «Nachweis: Dokument»
  brauchen kein automatisiertes Szenario, Kriterien mit «Nachweis: JUnit» kein Cucumber-Szenario. Der Nachweis
  wird im PR genannt.
- Die Abdeckungsprüfung verlangt den Nachweis für jedes Kriterium, das eine abgehakte Aufgabe ab Welle 1 unter
  `_Anforderungen:_` nennt. Wer eine Aufgabe abhakt, liefert die Szenarien im selben PR.
- Domain-Logik neuer Fachbereiche zusätzlich mit JUnit-Unit-Tests ohne Spring.
- Neue Fachbereiche testen gegen PostgreSQL (Testcontainers), nicht gegen H2.
- Wenige Browser-Szenarien; ein instabiles Szenario kommt mit `@flaky` in Quarantäne und wird einmal
  wiederholt, nicht gelöscht.
- **Definition of Done pro Aufgabe:** Szenarien und Tests grün, `mvn verify` grün (inkl. ArchUnit), bestehende
  Migrationen unverändert, erfüllte Akzeptanzkriterien mit `@req`-Tags im PR genannt, Checkbox in `tasks.md` im
  selben PR abgehakt. Bei Aufgaben mit Oberfläche zusätzlich: Handy und Desktop, hell und dunkel, axe ohne neuen
  Befund.
- **Reine Spec-Anpassungen ohne vollständigen CI-Lauf:** Ist jede geänderte Datei eine `.md`-Datei unter `.kiro/`
  oder `docs/`, läuft nur der Job «Spec-Prüfungen» (`.github/scripts/nur-spec.py`, `NurSpecTest`). Im Zweifel
  voller Lauf.
- **`main` nur über PRs mit grüner CI.** Eine Aufgabe ist ein Branch und ein PR; gemergt wird mit «Squash and
  merge».
