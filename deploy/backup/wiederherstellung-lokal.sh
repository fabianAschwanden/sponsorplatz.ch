#!/usr/bin/env bash
# Lokale Wiederherstellungs-Probe (Spec 01, Requirement 7): beweist, dass der Dump,
# den die Anwendung jede Nacht schreibt, in eine leere Datenbank zurückgespielt
# werden kann und die Anwendung darauf startet.
#
#   deploy/backup/wiederherstellung-lokal.sh
#
# Ablauf: Quelle mit Demodaten starten → Dump der Anwendung abwarten → Zeilen
# zählen → Dump in eine zweite, leere Installation einspielen → Zeilen vergleichen
# → Anwendung auf dem zurückgespielten Stand starten. Berührt nur zwei eigene
# Compose-Projekte und räumt sie am Ende weg. Braucht Docker; kein Server-Zugriff.
set -euo pipefail

repo="$(cd "$(dirname "$0")/../.." && pwd)"
arbeit="$repo/target/wiederherstellung"
quelle=sp-probe-quelle
ziel=sp-probe-ziel
tabellen=(app_user organisation mitgliedschaft projekt sponsoring_paket sponsoring_anfrage flyway_schema_history)
netz_angelegt=""

c() { local projekt=$1; shift; docker compose -p "$projekt" -f "$arbeit/compose.yml" -f "$arbeit/lokal.yml" --project-directory "$arbeit" "$@"; }
aufraeumen() {
  c "$quelle" down -v >/dev/null 2>&1 || true
  c "$ziel" down -v >/dev/null 2>&1 || true
  [[ -z $netz_angelegt ]] || docker network rm web >/dev/null 2>&1 || true
  rm -rf "$arbeit"
}
trap aufraeumen EXIT

rm -rf "$arbeit"; mkdir -p "$arbeit/secrets"
cp "$repo/deploy/infomaniak/compose.yml" "$arbeit/compose.yml"
# Wegwerf-Werte nur für diese Probe
printf '%s' "probe-$(date +%s)-$RANDOM-$RANDOM-db" > "$arbeit/secrets/db_password"
printf '%s' "probe-$(date +%s)-$RANDOM-$RANDOM-admin" > "$arbeit/secrets/admin_password"
: > "$arbeit/secrets/smtp_password"
chmod 644 "$arbeit"/secrets/*
# Der Dump kommt vom geplanten Lauf der Anwendung, hier alle 20 Sekunden statt nachts
cat > "$arbeit/lokal.yml" <<'YAML'
services:
  app:
    environment:
      SPONSORPLATZ_BACKUP_CRON: "*/20 * * * * *"
YAML

docker network inspect web >/dev/null 2>&1 || { docker network create web >/dev/null; netz_angelegt=1; }
echo "── Images"
docker build -q -f "$repo/docker/Dockerfile" -t sponsorplatz:current "$repo" >/dev/null
docker build -q -t sponsorplatz:db-current "$repo/deploy/infomaniak/db" >/dev/null

zaehle() { # zaehle <projekt> → «tabelle=zahl» je Zeile
  local t
  for t in "${tabellen[@]}"; do
    printf '%s=%s\n' "$t" "$(c "$1" exec -T db psql -U sponsorplatz -d sponsorplatz -Atc "select count(*) from $t")"
  done
}
warte_gesund() { # warte_gesund <projekt> <dienst>
  for _ in $(seq 1 60); do
    if [[ "$(c "$1" ps --format '{{.Health}}' "$2" 2>/dev/null)" == healthy ]]; then return 0; fi
    sleep 5
  done
  echo "FEHLER: $2 in $1 wird nicht gesund" >&2; return 1
}

echo "── Quelle mit Demodaten"
printf "SPONSORPLATZ_PROFILE='prod,demo'\n" > "$arbeit/.env"
c "$quelle" up -d >/dev/null 2>&1
warte_gesund "$quelle" app
dump=""
for _ in $(seq 1 30); do
  dump=$(c "$quelle" exec -T app sh -c 'ls -t /daten/sicherung/sponsorplatz_backup_*.sql 2>/dev/null | head -n1' || true)
  [[ -n $dump ]] && break
  sleep 5
done
[[ -n $dump ]] || { echo "FEHLER: Die Anwendung hat keinen Dump geschrieben" >&2; exit 1; }
# Nach dem Dump zählen: Der Seed ist dann sicher fertig, und die Quelle ändert sich nicht mehr
sleep 25
dump=$(c "$quelle" exec -T app sh -c 'ls -t /daten/sicherung/sponsorplatz_backup_*.sql | head -n1')
c "$quelle" exec -T app cat "$dump" > "$arbeit/dump.sql"
zaehle "$quelle" > "$arbeit/vorher.txt"
echo "Dump: $(wc -c < "$arbeit/dump.sql") Bytes"
c "$quelle" down -v >/dev/null 2>&1

echo "── Ziel: leere Datenbank, Dump einspielen"
printf "SPONSORPLATZ_PROFILE='prod'\n" > "$arbeit/.env"
c "$ziel" up -d db >/dev/null 2>&1
warte_gesund "$ziel" db
c "$ziel" exec -T db psql -q -U sponsorplatz -d sponsorplatz -v ON_ERROR_STOP=1 -f - < "$arbeit/dump.sql" >/dev/null
zaehle "$ziel" > "$arbeit/nachher.txt"

echo "── Vergleich"
paste -d' ' "$arbeit/vorher.txt" "$arbeit/nachher.txt" | sed 's/^/  /'
if ! cmp -s "$arbeit/vorher.txt" "$arbeit/nachher.txt"; then echo "FEHLER: Zeilenzahlen weichen ab" >&2; exit 1; fi
if grep -qE '=0$' <(grep -v sponsoring_paket "$arbeit/vorher.txt"); then echo "FEHLER: Eine Kerntabelle ist leer, die Probe sagt nichts aus" >&2; exit 1; fi

echo "── Anwendung auf dem zurückgespielten Stand"
c "$ziel" up -d app >/dev/null 2>&1
warte_gesund "$ziel" app
echo "ok: Dump zurückgespielt, Zeilenzahlen gleich, Anwendung gesund"
