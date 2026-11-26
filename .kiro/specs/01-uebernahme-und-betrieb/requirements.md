# Requirements Document — 01 Übernahme und Betrieb

## Introduction
Diese Spec übernimmt die bestehende Plattform in die neue Arbeitsweise und bringt sie in den Betrieb: Leitplanken
und Rückverfolgung im Repo, die Rechte des Bestands als prüfbare Tabelle, der Betrieb als Container hinter einem
Reverse Proxy mit Deploy, Rollback und Sicherung, der Rückbau fremder Cloud-Anbindungen, Regionen und Schalter
sowie der Umzug der Domain.

Grundlage sind der «Umsetzungsplan Plattform auf Infomaniak (v1)» vom 26.11.2026, Kapitel 3 bis 5, und aus dem
«Konzept Sponsoring-Plattform (v5)» die Anforderungen P1, P2 und P6. Requirements ohne v5-Kennung stammen aus dem
Umsetzungsplan und tragen den Vermerk «ohne v5-Kennung».

Schreibweise der Akzeptanzkriterien: EARS («SOLL», «WENN … DANN SOLL», «SOLANGE … SOLL»). Nachweis ist ein
Cucumber-Szenario mit Tag `@req-<x.y>` in einer Feature-Datei mit `@spec-01`; Ausnahmen tragen den Vermerk
«Nachweis: manuell [Fabian]», «Nachweis: Dokument» oder «Nachweis: JUnit» (`tech.md`, «Tests und Qualität»).
«Spec 0n, x.y» bezeichnet ein Akzeptanzkriterium, «Spec 0n: x.y» eine Aufgabe.

Für den Bestand gilt Bestandsschutz (`tech.md`): Diese Spec beschreibt nur, was sich ändert oder neu dazukommt.

### Offene Entscheide
Entschieden sind E3 (Repo) und E8 (Umzug des SCA-Stands) sowie am 26.11.2026 durch Fabian E1, E2, E4 und E7 (je nach
Empfehlung des Umsetzungsplans). Die übrigen Entscheide sind offen; die Kriterien rechnen mit der Empfehlung des
Umsetzungsplans und sind mit `[offen: …]` gekennzeichnet, wo sie davon abhängen.

| Nr. | Frage | Stand |
| --- | --- | --- |
| E1 | Stack behalten oder portieren | **entschieden 26.11.2026:** behalten |
| E2 | Bauweise neuer Fachbereiche | **entschieden 26.11.2026:** hexagonal, per ArchUnit nur für neue Pakete |
| E4 | Anmeldung | **entschieden 26.11.2026:** die der Plattform, kein Keycloak |
| E7 | Zahlung, Vertrag und Rechnung, Aufgaben-Engine, Matching | **entschieden 26.11.2026:** per Schalter aus, nicht löschen |
| I1 | Sicherung bei zwei Volumes | die zentrale Sicherung holt das Volume `daten` |
| U1 | Domain-Umzug schon in der Vorschau | ja |
| U4 | Absender der Mails | ein Postfach der Domain |
| U5 | Gemeinsamer Server für die Vorschau | ja, wenn die Messung es zeigt |
| E5a | Pilot-Anfragen auf dem gemeinsamen Server | ja, als bewusster Entscheid |

## Glossary
- **Bestand** – Code, Tests und Verhalten der Plattform vor der Übernahme.
- **Prüfpfad** – Adresse ohne Anmeldung, mit der Deploy und Überwachung prüfen, ob die App lebt.
- **Modus** – Wert der Repo-Variable `SITE_MODE`: `vorschau` oder `oeffentlich`.
- **Vorschau-Zugang** – Benutzername und Passwort, die im Modus `vorschau` vor jeder Seite verlangt werden.
- **Speicherbudget** – Obergrenze für die Summe der Speicher- und CPU-Limits aller Dienste.
- **Bundle** – was ein Deploy an den Server schickt: Images, Compose-Datei, Konfiguration, Secret-Dateien.
- **Region** – Gebiet, für das Stufen der Freischaltung einzeln geschaltet werden.
- **Stufe** – eine der vier Stufen der Freischaltung aus `product.md`.
- **Grundrecht** – eine Zeile der Tabelle in Requirement 2.

---

### Requirement 1: Rückverfolgung und Leitplanken (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich, dass jede Anforderung bis zum Test verfolgbar ist und das Repo sich
gegen Fehlgriffe wehrt, damit zwei Agenten nebeneinander arbeiten können, ohne dass ich jede Zeile lese.

#### Acceptance Criteria
1. Das Repo SOLL für jedes Kriterium, das eine abgehakte Aufgabe ab Welle 1 unter `_Anforderungen:_` nennt, ein
   Szenario mit dem Tag `@req-<x.y>` in einer Feature-Datei derselben Spec oder einen Nachweis-Vermerk am
   Kriterium verlangen; fehlt beides, SOLL der Build rot sein. Nachweis: JUnit (`ReqAbdeckungTest`).
2. Das Repo SOLL für die neuen Fachbereiche aus `structure.md` erzwingen: `domain` hängt nur von `java` und
   `domain` ab, `application` nur von `domain` und `application`, Klassen unter `adapter.out` implementieren nur
   Ports aus `application`, Beträge sind nie Gleitkomma, kein Zugriff auf Umgebungsvariablen. Die Regeln des
   Bestands bleiben unverändert. Nachweis: JUnit (`HexagonRegelnTest`). (Entscheid E2, 26.11.2026)
3. WENN jede geänderte Datei eine `.md`-Datei unter `.kiro/` oder `docs/` ist, DANN SOLL die CI nur die
   Spec-Prüfungen ausführen; in jedem anderen Fall und im Zweifel den vollen Lauf. Nachweis: JUnit
   (`NurSpecTest`).
4. Das Repo SOLL einen Push auf `main` lokal verweigern, Geheimnisse vor dem Commit und in der CI suchen und die
   geschützten Pfade vor Änderungen durch Agenten bewahren. Nachweis: Dokument (Test der Leitplanken-Vorlage).
5. Die CI SOLL ihre Tests in nebeneinander laufenden Jobs ausführen, ohne eine Prüfung wegzulassen, und jede
   Testklasse genau einmal. Nachweis: Dokument (Lauf der CI im PR zu Aufgabe 1.5).

### Requirement 2: Rechte aus dem Bestand (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich die Grundrechte der Plattform an einer Stelle lesen und geprüft wissen,
damit neue Routen und Fachbereiche nicht unbemerkt daran vorbeigehen.

#### Acceptance Criteria
1. Die Plattform SOLL die Grundrechte nach dieser Tabelle gewähren; die Tabelle ist die einzige Quelle.
   Nachweis: JUnit (`RechtematrixSpecTest`).

   | Nr. | Grundrecht | nicht angemeldet | ohne Mitgliedschaft | `ORG_VIEWER` | `ORG_EDITOR` | `ORG_OWNER` | `PLATFORM_ADMIN` |
   | --- | --- | --- | --- | --- | --- | --- | --- |
   | G1 | Inhalte einer Organisation bearbeiten | verweigert | verweigert | verweigert | erlaubt | erlaubt | erlaubt |
   | G2 | Organisation verwalten | verweigert | verweigert | verweigert | verweigert | erlaubt | erlaubt |
   | G3 | Private Daten eines Sponsors sehen | verweigert | verweigert | erlaubt | erlaubt | erlaubt | erlaubt |

   Die Rollen `ORG_VIEWER`, `ORG_EDITOR` und `ORG_OWNER` gelten je Mitgliedschaft in der betroffenen
   Organisation; «ohne Mitgliedschaft» heisst angemeldet, aber nicht Mitglied dieser Organisation.
2. Eine Rolle in einer übergeordneten Organisation SOLL für alle ihr untergeordneten Organisationen gelten.
   Nachweis: JUnit (`AccessControlTest`, `MitgliedschaftHierarchieQueryTest`).
3. Jede Prüfmethode der zentralen Rechteprüfung (`AccessControl`) SOLL einer Zeile der Tabelle entsprechen; eine
   Methode ohne Zeile und eine Zeile ohne Methode SOLLEN den Build rot machen. Nachweis: JUnit
   (`RechtematrixSpecTest`).

### Requirement 3: Geheimnisse als Dateien (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich, dass kein Geheimnis in einer Umgebungsvariable steht, damit es auf dem
Server nicht über die Verwaltung der Container lesbar ist.

#### Acceptance Criteria
1. Die Plattform SOLL im Profil `prod` jedes Geheimnis (Passwort der Datenbank, Passwort des Postfachs, Passwort
   des ersten Verwaltungszugangs) aus einer Datei unter `/run/secrets/` lesen. Nachweis: JUnit.
2. Die Compose-Datei SOLL kein Geheimnis über `environment` oder `env_file` übergeben, und jeder Dienst SOLL nur
   die Secret-Dateien binden, die er braucht. Nachweis: JUnit.
3. Die Plattform SOLL für die nächtliche Sicherung das Passwort der Datenbank aus derselben Quelle nehmen wie die
   Datenverbindung und nicht aus einer Umgebungsvariable. Nachweis: JUnit.
4. WENN beim Deploy ein Pflicht-Secret fehlt oder nicht das erwartete Format hat, DANN SOLL der Deploy abbrechen,
   bevor das Bundle entsteht. Nachweis: JUnit.

### Requirement 4: Prüfpfad und Erreichbarkeit hinter dem Reverse Proxy (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich von aussen prüfen, ob die App lebt, ohne die Betriebsendpunkte zu
öffnen.

#### Acceptance Criteria
1. Die Plattform SOLL unter `/livez` auf dem App-Port ohne Anmeldung mit 200 antworten, solange sie lebt.
   Nachweis: JUnit.
2. Die Plattform SOLL die übrigen Betriebsendpunkte (Actuator) nur auf dem internen Verwaltungsport an der
   Loopback-Adresse anbieten. Nachweis: JUnit.
3. Die Plattform SOLL im Profil `prod` hinter dem Reverse Proxy das Protokoll und die Adresse des Besuchers aus
   den Weiterleitungs-Headern erkennen. Nachweis: JUnit.
4. Die Compose-Datei SOLL nur den Dienst `app` an das äussere Netz hängen, und die Regeln ihrer Router SOLLEN sich
   gegenseitig ausschliessen, ohne Priorität: App, Prüfpfad, Weiterleitung `/sca`, Weiterleitung von `www`.
   Nachweis: JUnit.
5. Die Compose-Datei SOLL den Zustand des Dienstes `app` über den Prüfpfad bestimmen. Nachweis: JUnit.

### Requirement 5: Laufzeit im Container (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich, dass die App mit festen Grenzen und ohne Schreibrechte ausserhalb ihrer
Daten läuft, damit sie andere Apps auf demselben Server nicht gefährdet.

#### Acceptance Criteria
1. Die Plattform SOLL im Profil `prod` Medien unter `/daten/medien` und Dumps unter `/daten/sicherung` ablegen;
   beide liegen im Volume `daten`. Nachweis: JUnit.
2. Der Dienst `app` SOLL als nicht privilegierter Benutzer mit nur lesbarem Dateisystem, ohne Capabilities und
   ohne Rechteausweitung laufen; schreibbar sind nur das Volume `daten` und ein flüchtiges `/tmp`.
   Nachweis: JUnit.
3. Die JVM SOLL feste Grenzen für Heap, Metaspace und Codecache tragen und keinen Prozentwert des Speichers.
   Nachweis: JUnit.
4. WENN die Summe der Speicherlimits aller Dienste über 1'024 MiB oder die Summe der CPU-Limits über 1.0 liegt,
   DANN SOLL der Deploy abbrechen, bevor das Bundle entsteht. Nachweis: JUnit.
5. Die Werte aus 5.3 und 5.4 SOLLEN auf einer Messung beruhen (Leerlauf und nach einem Klickpfad) und nur per PR
   mit Messung geändert werden. Nachweis: Dokument (Messprotokoll aus Aufgabe 0.3 und 1.10).

### Requirement 6: Deploy über die Pipeline (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich, dass ein Deploy sich selbst prüft und bei einem Fehler zurückgeht.

#### Acceptance Criteria
1. Der Deploy SOLL nur für einen Commit laufen, dessen CI grün ist. Nachweis: JUnit.
2. Das Bundle SOLL die Images aller Dienste enthalten, die die Compose-Datei nennt. Nachweis: JUnit.
3. Nach dem Deploy SOLL der Workflow prüfen: Der Prüfpfad antwortet mit 200; im Modus `vorschau` antwortet `/`
   ohne Vorschau-Zugang mit 401 und mit Vorschau-Zugang mit 200. Nachweis: JUnit.
4. WENN eine Prüfung nach dem Deploy fehlschlägt, DANN SOLL der Workflow auf den vorherigen Stand zurückgehen und
   erneut prüfen. Nachweis: JUnit.
5. Der Workflow SOLL keine Server-Adressen, keine Daten und kein Geheimnis in seine Logs schreiben und den Hash
   des Vorschau-Passworts maskieren. Nachweis: JUnit.
6. Ein Rollback auf das vorherige Image SOLL gegen das neuere Schema starten. Nachweis: Dokument (Rollback-Probe
   aus Aufgabe 1.10).

### Requirement 7: Sicherung und Wiederherstellung (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich jede Nacht eine Sicherung und einen geübten Weg zurück.

#### Acceptance Criteria
1. Die Plattform SOLL jede Nacht einen Dump der Datenbank nach `/daten/sicherung` schreiben und Dumps entfernen,
   die älter als 3 Tage sind. Nachweis: JUnit.
2. WENN der jüngste Dump älter als 26 Stunden ist, DANN SOLL die Überwachung das melden. Nachweis: Dokument.
3. Die Sicherung SOLL das Volume `daten` täglich abholen und verschlüsselt bei einem Schweizer Anbieter ablegen.
   Nachweis: Dokument. `[offen: I1]`
4. Die Wiederherstellung SOLL als Probe geübt sein: Dump in eine leere Datenbank, Medien zurück, App startet,
   das Schema ist gültig, die Zeilenzahlen der Kerntabellen stimmen mit dem Stand der Sicherung überein.
   Nachweis: Dokument (Protokoll der Probe).
5. Der Ablauf der Wiederherstellung SOLL unter `docs/betrieb/` beschrieben sein. Nachweis: Dokument.

### Requirement 8: Vorschau-Schutz und Demodaten (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich, dass vor der Freigabe niemand ohne Zugang die Seiten sieht und
Demo-Konten nie öffentlich erreichbar sind.

#### Acceptance Criteria
1. SOLANGE der Modus `vorschau` gilt, SOLL jede Adresse ausser dem Prüfpfad nur mit Vorschau-Zugang erreichbar
   sein. Nachweis: JUnit.
2. SOLANGE der Modus `vorschau` gilt, SOLL jede Antwort den Kopf `X-Robots-Tag: noindex` tragen. Nachweis: JUnit.
3. WENN der Modus `oeffentlich` und das Profil `demo` zusammenkommen, DANN SOLL der Deploy abbrechen.
   Nachweis: JUnit.
4. Die Plattform SOLL im Profil `prod` den ersten Verwaltungszugang aus dem dafür vorgesehenen Secret anlegen und
   ohne das Profil `demo` keine Demo-Konten. Nachweis: JUnit.

### Requirement 9: Überwachung und Betriebsdokumentation (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich merken, wenn die App steht, und nachlesen können, was dann zu tun ist.

#### Acceptance Criteria
1. Die Überwachung SOLL melden, wenn ein Container der App nicht läuft oder die Seite von aussen nicht erreichbar
   ist. Nachweis: Dokument.
2. Der Deploy SOLL nach jedem Lauf prüfen, dass die anderen Apps desselben Servers weiter antworten.
   Nachweis: JUnit.
3. Unter `docs/betrieb/` SOLLEN Deploy, Rollback, Wiederherstellung und das Rotieren der Geheimnisse beschrieben
   sein. Nachweis: Dokument.

### Requirement 10: Rückbau (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich nur noch den Betriebsweg im Repo haben, den es wirklich gibt.

#### Acceptance Criteria
1. Die Plattform SOLL keine Speicher- und Sicherungs-Adapter für andere Cloud-Anbieter, keine Profile dafür und
   keine Infrastruktur-Definitionen dafür enthalten. Nachweis: JUnit.
2. Die Plattform SOLL keine Konfiguration für einen externen Fehlermeldedienst enthalten. Nachweis: JUnit.
3. Der Rückbau SOLL das Verhalten für Benutzer nicht ändern; die Tests des Bestands bleiben grün, entfernt werden
   nur die Tests der entfernten Adapter. Nachweis: Dokument (CI-Lauf der Rückbau-PRs).

### Requirement 11: Regionen und schrittweise Freischaltung (P1)
**User Story:** Als Plattform-Verwaltung möchte ich Stufen Region für Region freischalten, damit der Pilot
kontrolliert wächst.

Stand der Plattform: neu. Der Kanton ergibt sich aus der PLZ; Schalter für Funktionen gibt es nicht.

#### Acceptance Criteria
1. Die Verwaltung SOLL Regionen anlegen und je Region die Stufen aus `product.md` freischalten können.
2. Eine nicht freigeschaltete Funktion SOLL in einer Region nicht erreichbar sein.
3. Eine Region SOLL einen eigenen Namen tragen können.

### Requirement 12: Daten in der Schweiz (P2)
**User Story:** Als Verein möchte ich wissen, dass unsere Daten in der Schweiz liegen.

Stand der Plattform: anpassen. Die Datenschutzerklärung nennt einen früheren Betrieb.

#### Acceptance Criteria
1. Die Plattform SOLL Anmelde- und Plattformdaten nur in der Schweiz speichern. Nachweis: Dokument.
2. Die Plattform SOLL keine Dienste aufrufen, die Besucherdaten ins Ausland senden: Schriften, Stylesheets und
   Skripte kommen von der eigenen Adresse. Nachweis: JUnit.

### Requirement 13: Für Vereine kostenlos (P6)
**User Story:** Als Verein möchte ich die Plattform ohne Kosten nutzen.

Stand der Plattform: vorhanden. Die Plattform erhebt keine Gebühren.

#### Acceptance Criteria
1. Die Plattform SOLL von Vereinen kein Geld verlangen.
2. Zahlung, Vertrag und Rechnung SOLLEN im Pilot abgeschaltet sein: Ihre Seiten sind nicht erreichbar, der Code
   bleibt. (Entscheid E7, 26.11.2026)

### Requirement 14: Domain-Umzug (ohne v5-Kennung)
**User Story:** Als Betreiber möchte ich die Domain umziehen, solange die Seite in der Vorschau ist, damit es
niemand ausser uns merkt.

#### Acceptance Criteria
1. Nach dem Umzug SOLLEN die bestehenden Adressen gültig bleiben: `/`, `/kunde/`, `?rolle=verein`,
   `?rolle=sponsor`, `#anmelden`, `/kunde/?kunde=ja`, `/kunde/?fuer=vorstand`. Nachweis: manuell [Fabian].
2. `/sca` und alles darunter SOLL auf die neue Adresse des SCA-Stands weiterleiten, `www` auf die Domain ohne
   `www`. Nachweis: manuell [Fabian].
3. Die Domain SOLL nach dem Umzug nur noch auf den neuen Server zeigen, auch über IPv6. Nachweis: manuell
   [Fabian].
4. Vor dem Umzug SOLLEN die bisherigen Pilot-Anmeldungen als Pilot-Anfragen übernommen sein, und die alte
   Anmeldung SOLL gesperrt sein. Nachweis: manuell [Fabian]. `[offen: E5a]`
5. Der alte Betrieb SOLL frühestens 14 Tage nach dem Umzug abgebaut werden. Nachweis: manuell [Fabian].
