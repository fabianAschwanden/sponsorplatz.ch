# Betrieb

Wie die Plattform auf der VPS läuft, wie sie dorthin kommt und was bei einer Störung zu tun ist. Anforderungen dazu: Spec 01, Requirements 3 bis 9.

Stand: vor dem ersten Deploy. Abschnitte mit dem Vermerk «noch offen» beschreiben den geplanten Weg, nicht einen erprobten.

## Aufbau

| Baustein | Festlegung |
| --- | --- |
| Dienste | `app` (Spring Boot, uid 1001, nur lesbar) und `db` (PostgreSQL 17, nur im internen Netz) |
| Volumes | `db` (Daten von PostgreSQL), `daten` (unter `/daten`: `medien/` und `sicherung/`) |
| Geheimnisse | je eine Datei unter `/run/secrets/`: `db_password`, `smtp_password`, `admin_password`, `drossel_schluessel` |
| Prüfpfad | `/livez` auf dem Anwendungs-Port, ohne Anmeldung. Actuator liegt auf Port 9090 und ist von aussen nicht erreichbar |
| Speicherbudget | `app` 768 MiB, `db` 256 MiB, zusammen 1.0 CPU |
| Vorschau | Basic-Auth und `noindex` am Reverse-Proxy, solange `SITE_MODE` nicht `oeffentlich` ist |

Alles steht in `deploy/infomaniak/compose.yml`. `DeployHaertungTest` prüft die Datei bei jedem Testlauf.

## Deploy

Ein Deploy geht nur über den Workflow «Infomaniak Deploy». Niemand ändert auf dem Server von Hand: Sonst weicht der Server vom Repo ab, und niemand weiss, was läuft.

1. Der Stand liegt auf `main`, und die CI für genau diesen Commit ist grün.
2. Workflow von Hand starten (`gh workflow run infomaniak-deploy.yml --ref main`) oder ein Tag `v…` auf einen Commit von `main` setzen.
3. Der Workflow prüft Budget, CI und Geheimnisse, baut beide Images, schickt das Bundle und prüft danach von aussen.

Der Lauf ist rot, wenn eine Prüfung scheitert. Was die Meldung bedeutet:

| Meldung | Bedeutung | Was tun |
| --- | --- | --- |
| «Kein grüner CI-Lauf» | Die CI auf `main` läuft noch oder ist rot | abwarten oder `gh workflow run ci.yml --ref main`, dann neu auslösen |
| «Summe über dem Speicherbudget» | Limits in `compose.yml` liegen über dem Budget | Limits senken, oder das Budget bewusst anheben (Workflow, `tech.md`, Test) |
| «hat nicht die erwartete Form» | ein Secret ist zu kurz oder enthält unerlaubte Zeichen | Secret neu setzen |
| «abgelehnt … Compose-Policy» | Der Server lehnt `compose.yml` ab; auf dem Server ist nichts geändert | `compose.yml` anpassen, nie die Policy |
| «/livez liefert … nicht 200» | Die Anwendung startet nicht | der Workflow hat schon zurückgerollt; Ursache im Log der Anwendung suchen (Zugang zum Server) |
| «App N ist nicht gesund» | Eine andere App der VPS antwortet nicht | kein weiterer Deploy, die VPS prüfen |

## Rollback

Der Workflow rollt selbst zurück, wenn nach dem Umschalten eine Prüfung scheitert.

**Ein Rollback setzt die Images zurück, nicht die Datenbank.** Darum gilt für jede Migration: nur additiv. Eine Spalte kommt in einem Deploy dazu; die alte fällt frühestens im nächsten weg, wenn kein Rollback mehr auf sie angewiesen ist. Ein Upgrade von PostgreSQL läuft als eigener Deploy nach einer frischen Sicherung.

Beim allerersten Deploy gibt es keinen Vorgänger und damit keinen Rollback.

## Geheimnisse setzen und wechseln

Geheimnisse stehen nur als GitHub-Secrets im Repo und als Dateien auf dem Server. Sie gehören nie in eine Datei des Repos, in einen Chat oder in eine Befehlszeile.

```sh
gh secret set DB_PASSWORD        # fragt verdeckt
gh secret set PREVIEW_PASSWORD
```

Der Workflow prüft die Form jedes Werts, bevor er etwas an den Server schickt (`.github/scripts/schreibe_env.py`, Schritt «Geheimnisse und Variablen vorhanden»).

| Art | Name | Bemerkung |
| --- | --- | --- |
| Variable | `INFOMANIAK_HOST`, `INFOMANIAK_KNOWN_HOSTS` | Server und sein Host-Schlüssel; ohne `INFOMANIAK_HOST` läuft der Workflow nicht |
| Variable | `SITE_MODE` | `vorschau` (Standard) oder `oeffentlich` |
| Variable | `SPONSORPLATZ_PROFILE` | `prod` (Standard) oder `prod,demo` |
| Variable | `PREVIEW_USER` | Benutzer der Vorschau |
| Variable | `SPONSORPLATZ_ADMIN_EMAIL` | optional: erster Verwaltungszugang |
| Variable | `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER` | optional: ohne sie bleibt der Mailversand aus |
| Variable | `INFOMANIAK_ANDERE_APPS_HEALTH` | optional: Prüfadressen der anderen Apps, durch Leerzeichen getrennt |
| Secret | `INFOMANIAK_SSH_KEY` | Deploy-Key der App |
| Secret | `DB_PASSWORD` | 24 bis 128 Zeichen aus Buchstaben, Ziffern, Unterstrich, Punkt und Bindestrich |
| Secret | `PREVIEW_PASSWORD` | mindestens 12 Zeichen, ohne Anführungszeichen und Rückstrich |
| Secret | `ADMIN_PASSWORD` | mindestens 16 Zeichen; nötig, sobald `SPONSORPLATZ_ADMIN_EMAIL` gesetzt ist |
| Secret | `DROSSEL_SCHLUESSEL` | Pflicht, 32 bis 128 Zeichen aus Buchstaben und Ziffern (`openssl rand -hex 32`); Schlüssel für den Prüfwert der Client-Adresse in der Drosselung der öffentlichen Formulare |
| Secret | `SMTP_PASSWORD` | nötig, sobald `SMTP_HOST` gesetzt ist |

| Geheimnis | Wechsel |
| --- | --- |
| `PREVIEW_PASSWORD` | Secret neu setzen, Deploy auslösen. Wirkt sofort |
| `SMTP_PASSWORD` | zuerst im Postfach ändern, dann Secret setzen und deployen |
| `ADMIN_PASSWORD` | wirkt nur beim ersten Start ohne Verwaltungszugang. Später ändert die Person ihr Passwort in der Anwendung |
| `DROSSEL_SCHLUESSEL` | jederzeit: Secret setzen, deployen. Die Zähler der laufenden Stunde beginnen dann neu |
| `DB_PASSWORD` | **nicht allein über das Secret.** PostgreSQL übernimmt das Passwort nur beim Anlegen der Datenbank. Wechsel: Passwort in der Datenbank ändern (`ALTER ROLE`), dann Secret setzen und deployen. Noch offen: als erprobter Ablauf beschreiben |
| Deploy-Key | neuen Key im Infra-Repo eintragen und ausrollen, dann `INFOMANIAK_SSH_KEY` setzen |

## Sicherung

- Die Anwendung schreibt jede Nacht um 02:00 einen Dump nach `/daten/sicherung/` und behält drei Tage.
- Die zentrale Sicherung holt das Volume `daten` täglich ab und legt es verschlüsselt ab. **Noch offen:** Der Sicherungszugang der VPS gibt heute nur Apps mit genau einem Volume heraus; diese App hat zwei. Bis das im Infra-Repo entschieden und ausgerollt ist, verlässt kein Dump den Server.
- Ein fehlgeschlagener Dump steht nur im Log der Anwendung. **Noch offen:** Meldung, wenn der jüngste Dump älter als 26 Stunden ist.

## Wiederherstellung

Lokal erprobt mit `deploy/backup/wiederherstellung-lokal.sh`: Das Skript startet die Plattform mit Demodaten, wartet auf den Dump der Anwendung, spielt ihn in eine zweite, leere Installation ein, vergleicht die Zeilenzahlen der Kerntabellen und startet die Anwendung auf dem zurückgespielten Stand. Es braucht nur Docker.

Auf dem Server (noch offen, erst nach der ersten Sicherung zu erproben):

1. Jüngsten Stand des Volumes `daten` aus der Sicherung holen.
2. Anwendung stoppen, Datenbank laufen lassen.
3. Dump aus `sicherung/` mit `psql` und `ON_ERROR_STOP` einspielen. Der Dump räumt bestehende Tabellen selbst ab.
4. `medien/` zurückkopieren.
5. Anwendung starten. Sie prüft beim Start das Schema gegen die Migrationen und startet nicht, wenn es nicht passt.
6. Zeilenzahlen der Kerntabellen mit dem Stand vor dem Ausfall vergleichen, soweit bekannt.

Ein Dump passt zu dem Stand der Anwendung, der ihn geschrieben hat, und zu jedem späteren. Zu einem älteren Stand passt er nur, solange die Migrationen dazwischen additiv waren.

## Vorschau und Öffentlichschalten

- `SITE_MODE=vorschau` (Standard): Die Seite verlangt den Vorschau-Zugang, Suchmaschinen sehen `noindex`. Der Deploy prüft beides.
- Das Profil `demo` legt Konten mit bekannten Passwörtern an, darunter eine Plattform-Verwaltung. Es darf nur hinter dem Vorschau-Schutz laufen. Der Deploy bricht ab, wenn `SITE_MODE=oeffentlich` und `demo` zusammenkommen.
- Vor dem Öffentlichschalten: Profil `demo` aus, Datenbank ohne Demo-Konten, Verwaltungszugang aus `ADMIN_PASSWORD` (Spec 06).
