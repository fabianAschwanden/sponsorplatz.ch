# Datenbank-Image der Plattform auf der VPS: PostgreSQL 17, unverändert.
# Ein eigenes Image, weil der Server nur Images mit dem Namen der App annimmt
# (sponsorplatz:db-<version>-<sha>) und sie als Bundle erhält, nicht aus einer Registry.
# Ein Upgrade der Version läuft als eigener Deploy nach einer frischen Sicherung:
# Ein Rollback setzt die Images zurück, nicht die Daten.
FROM postgres:17.12-trixie@sha256:87c8b42f423b45d5247e6aa8fdb2e7762fec164c68a50fe3eb07e7bfcaf4dbcf
# Eigene Kennzeichnung: macht aus dem gezogenen ein selbst gebautes Image (eine Plattform, alle Blobs im Archiv)
LABEL org.opencontainers.image.title="sponsorplatz-db"
