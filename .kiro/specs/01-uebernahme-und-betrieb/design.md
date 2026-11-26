# Design Document — 01 Übernahme und Betrieb

## Overview
Die bestehende Spring-Boot-Plattform bleibt der Code-Kern (offen: E1). Sie läuft als ein Compose-Projekt mit den
Diensten `app` und `db` hinter einem Reverse Proxy (Traefik). Deployt wird nur aus GitHub Actions: Der Workflow
baut die Images, schnürt ein Bundle und schickt es an den Server; der Server prüft die Compose-Datei gegen seine
eigene Policy, bevor sich etwas ändert.

Die Reihenfolge ist: zuerst läuft die Plattform unverändert im neuen Betrieb, dann zieht die Webseite ein
(Spec 03, Spec 02), dann die Domain (Requirement 14). Neues entsteht erst danach.

## Arbeitsweise im Repo (Requirement 1)
- Leitplanken aus der gemeinsamen Vorlage: Hooks für Agenten (geschützte Pfade, verbotene Befehle, Secret-Scan),
  Git-Hooks (`pre-commit`, `pre-push` sperrt `main`), Workflows `secret-scan.yml` und `ci-fehler.yml`.
- `ReqAbdeckung` liest `tasks.md` und `requirements.md` jeder Spec und die Feature-Dateien unter
  `src/test/resources/features/`. Feature-Dateien des Bestands ohne `@spec`- und ohne `@req`-Tag bleiben aussen
  vor (Bestandsschutz).
- `HexagonRegelnTest` prüft nur Klassen unter `ch.sponsorplatz.<fachbereich>.(domain|application|adapter)` der
  Fachbereiche aus `structure.md`. Solange ein Fachbereich leer ist, prüft die Regel nichts und ist grün.
- `NurSpecTest` prüft das Skript `.github/scripts/nur-spec.py`, das die CI als ersten Job aufruft.

## Rechte (Requirement 2)
Der Bestand prüft Rechte in den Controllern über `AccessControl` mit drei Prüfmethoden; sie entsprechen den
Grundrechten G1 bis G3. `RechtematrixSpecTest` liest die Tabelle aus `requirements.md` und ruft für jede Zelle die
zugehörige Methode auf. Die vollständige Liste der Routen und ihrer Prüfung steht im Bestand
(`docs/bestand/specs/ROLLENKONZEPT.md`) und wird nicht nachgeführt; massgeblich für Neues ist die Tabelle.

Neue Fachbereiche prüfen in ihrer `application`-Schicht und fragen die Grundrechte über einen Port ab.

## Zielaufbau (Requirements 3 bis 5)
| Baustein | Festlegung |
| --- | --- |
| Dienst `app` | Image aus `docker/Dockerfile`, nicht privilegierter Benutzer, Port 8080, Dateisystem nur lesbar, alle Capabilities entzogen, `/tmp` flüchtig |
| Dienst `db` | PostgreSQL 17, nur im internen Netz |
| Netze | ein äusseres (nur `app`) und ein internes |
| Volume `db` | Datenverzeichnis von PostgreSQL |
| Volume `daten` | in `app` unter `/daten`: `medien/` und `sicherung/` |
| Secrets | je eine Datei: `db_password`, `smtp_password`, `admin_password`; jeder Dienst bindet nur seine eigenen |
| Konfiguration ohne Geheimnis | in `compose.yml`: Profil, Adresse der Datenbank, Basis-Adresse, Mail-Server, Speicherpfade, JVM-Grenzen |
| Limits (Startwert) | `app` 768 MiB und 0.7 CPU, `db` 256 MiB und 0.3 CPU |
| Prüfpfad | `/livez` auf dem App-Port; Actuator bleibt auf dem Verwaltungsport an der Loopback-Adresse |
| Router | App, Prüfpfad, Weiterleitung `/sca`, Weiterleitung `www`; die Regeln schliessen sich aus |
| Middlewares | Drosselung; im Modus `vorschau` zusätzlich Basic-Auth und `X-Robots-Tag: noindex` |

### Anpassungen an der Plattform
| Nr. | Was | Kriterium |
| --- | --- | --- |
| 1 | `spring.config.import=optional:configtree:/run/secrets/` und Zuordnung der bisherigen Variablen auf die Dateinamen in `application-prod.properties` | 3.1 |
| 2 | Zusätzlicher Pfad der Liveness-Probe auf dem App-Port, in der `SecurityConfig` freigegeben | 4.1, 4.2 |
| 3 | `server.forward-headers-strategy=framework` im Profil `prod` | 4.3 |
| 4 | Speicherpfad der Medien und Verzeichnis der Sicherung auf das Volume; Aufbewahrung der Dumps 3 Tage | 5.1, 7.1 |
| 5 | Feste JVM-Grenzen statt Prozentwert | 5.3 |
| 6 | Konfiguration des externen Fehlermeldedienstes entfernen | 10.2 |
| 7 | Healthcheck in der Compose-Datei über den Prüfpfad | 4.5 |
| 8 | `BackupService` nimmt das Passwort für `pg_dump` aus der Datenquelle | 3.3 |

Keine dieser Anpassungen ändert Verhalten für Benutzer.

## Deploy (Requirement 6)
Auslöser ist ein Push auf `main` in Code, Dockerfile, Compose-Datei oder Workflow, dazu der Start von Hand.

1. CI-Nachweis für den Commit.
2. Secrets prüfen: vorhanden und im erwarteten Format.
3. Speicherbudget prüfen.
4. Images bauen bzw. mit fester Version beziehen.
5. Bundle schnüren: Images, `compose.yml`, nicht geheime Konfiguration, Ordner mit den Secret-Dateien.
6. Bundle an den Server; lehnt dessen Policy ab, bleibt der alte Stand.
7. Prüfen: Prüfpfad, Vorschau-Schutz, andere Apps.
8. Bei Fehler Rollback und erneut prüfen.

Der Rollback setzt die Images zurück, nicht die Datenbank. Darum sind Migrationen nur additiv (`tech.md`,
Regel 1).

## Sicherung (Requirement 7)
`BackupService` schreibt nachts einen Dump nach `/daten/sicherung`. Damit liegt alles, was zu sichern ist, in
einem Volume. Die zentrale Sicherung holt dieses Volume täglich, legt es verschlüsselt ab und prüft monatlich
eine Wiederherstellung. Der Dump liegt unverschlüsselt im Volume, also neben der Datenbank, aus der er stammt.

Offen (I1): Die zentrale Sicherung muss für eine App mit zwei Volumes das richtige benennen können. Das ist eine
Änderung ausserhalb dieses Repos.

## Vorschau und Demodaten (Requirement 8)
Die Repo-Variable `SITE_MODE` steuert den Modus. In `vorschau` setzt der Workflow Basic-Auth und `noindex` am
Reverse Proxy; der Hash entsteht im Workflow aus einem Secret. Die Vorschau läuft mit den Profilen `prod` und
`demo`. Der Demo-Seed legt Konten mit bekannten Passwörtern an; das ist nur hinter dem Vorschau-Schutz
vertretbar, darum verweigert der Workflow `oeffentlich` zusammen mit `demo`.

## Regionen und Schalter (Requirements 11, 13)
Neuer Fachbereich `region` (hexagonal).

### Data Models
| Tabelle | Spalten (Auszug) |
| --- | --- |
| `region` | `id`, `code` (eindeutig), `name`, `erstellt_am` |
| `region_plz` | `region_id`, `plz_von`, `plz_bis` – Zuordnung einer Organisation über ihre PLZ |
| `region_freischaltung` | `region_id`, `stufe` (1–4), `freigeschaltet_am`, `freigeschaltet_von` |

Zum Start gibt es eine Region «Pilot». Die Schalter für Zahlung, Vertrag und Rechnung, Aufgaben-Engine und
Matching sind Konfiguration der Plattform, nicht der Region; abgeschaltet heisst: Die Routen antworten mit 404
und die Einträge der Navigation fehlen.

## Error Handling
- Fehlt ein Pflicht-Secret, bricht der Deploy ab; die App selbst startet ohne Datenbank-Passwort nicht.
- Eine nicht freigeschaltete Funktion antwortet mit 404, nicht mit 403: Ihre Existenz ist keine Auskunft.
- Scheitert der nächtliche Dump, steht das im Log; auffallen muss es über das Alter des jüngsten Dumps (7.2).

## Testing Strategy
| Bereich | Nachweis |
| --- | --- |
| Rückverfolgung, Hexagon, nur Spec | `ReqAbdeckungTest`, `HexagonRegelnTest`, `NurSpecTest` |
| Rechte | `RechtematrixSpecTest` |
| Konfiguration `prod` | JUnit über die geladene Konfiguration und die Dateien unter `src/main/resources` |
| Compose-Datei und Workflow | JUnit, das beide Dateien liest: Secrets, Netze, Router, Limits, Budget, Schritte |
| Deploy, Rollback, Wiederherstellung | Protokoll des ersten und zweiten Deploys und der Probe unter `docs/betrieb/` |
| Regionen | Cucumber mit `@spec-01`, gegen PostgreSQL |

## Open Points
- E1, E2, E4, E7, I1, U1, U4, U5, E5a (Tabelle in `requirements.md`).
- Die Werte der JVM-Grenzen und Limits sind Startwerte bis zur Messung (5.5).
- Wie die Überwachung das Alter des Dumps erfährt (7.2), klärt Aufgabe 1.12.
