#!/usr/bin/env python3
"""Schreibt die .env des Deploy-Bundles (Aufruf aus infomaniak-deploy.yml).

Compose setzt die Werte in deploy/infomaniak/compose.yml ein. Die Datei enthält
keine Geheimnisse ausser dem Hash des Vorschau-Passworts; Geheimnisse gehen als
Dateien unter secrets/. Jeder Wert wird geprüft, bevor er geschrieben wird:
Ein Zeilenumbruch oder ein $ würde weitere Zeilen erzeugen oder Compose weiter
einsetzen lassen. Bei einem Fehler nennt das Skript die Variable, nie den Wert.

Regeln:
- SITE_MODE ist «vorschau» (Standard) oder «oeffentlich».
- In «vorschau» schützen Benutzer und Passwort die Seite; der Hash entsteht hier.
- In «oeffentlich» darf das Profil «demo» nicht aktiv sein: Der Demo-Seed legt
  Konten mit bekannten Passwörtern an, darunter einen Plattform-Admin.
"""
import os
import re
import subprocess
import sys

MODI = ("vorschau", "oeffentlich")
PROFILE = ("prod", "prod,demo")
HOSTNAME = re.compile(r"^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
EMAIL = re.compile(r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")
BENUTZER = re.compile(r"^[a-z0-9_-]{1,32}$")
BCRYPT = re.compile(r"^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$")
MIDDLEWARES_VORSCHAU = "sponsorplatz-drossel,sponsorplatz-vorschau,sponsorplatz-noindex"
MIDDLEWARES_OEFFENTLICH = "sponsorplatz-drossel"
PASSWORT_MIN = 12


class Fehler(Exception):
    pass


def bcrypt_hash(benutzer: str, passwort: str) -> str:
    """Hash über htpasswd; das Passwort geht über stdin, nicht als Argument."""
    ergebnis = subprocess.run(["htpasswd", "-niB", benutzer], input=passwort + "\n",
                              capture_output=True, text=True, check=False)
    if ergebnis.returncode != 0:
        raise Fehler("htpasswd konnte den Hash nicht erzeugen")
    zeile = ergebnis.stdout.strip()
    return zeile.split(":", 1)[1] if ":" in zeile else ""


def werte(umgebung: dict, hasher=bcrypt_hash) -> dict:
    """Geprüfte Zeilen der .env als Abbildung Name → Wert."""
    modus = umgebung.get("SITE_MODE") or "vorschau"
    if modus not in MODI:
        raise Fehler("SITE_MODE muss «vorschau» oder «oeffentlich» sein")
    profil = umgebung.get("SPONSORPLATZ_PROFILE") or "prod"
    if profil not in PROFILE:
        raise Fehler("SPONSORPLATZ_PROFILE muss «prod» oder «prod,demo» sein")
    if modus == "oeffentlich" and "demo" in profil.split(","):
        raise Fehler("SITE_MODE «oeffentlich» und Profil «demo» schliessen sich aus: "
                     "Der Demo-Seed legt Konten mit bekannten Passwörtern an")

    host = umgebung.get("HOST_APP") or ""
    if not HOSTNAME.match(host):
        raise Fehler("HOST_APP fehlt oder ist kein Hostname")

    aus = {"SPONSORPLATZ_PROFILE": profil, "BASIS_URL": f"https://{host}"}

    if modus == "vorschau":
        benutzer = umgebung.get("PREVIEW_USER") or ""
        passwort = umgebung.get("PREVIEW_PASSWORD") or ""
        if not BENUTZER.match(benutzer):
            raise Fehler("PREVIEW_USER fehlt oder enthält andere Zeichen als a–z, 0–9, _ und -")
        if len(passwort) < PASSWORT_MIN or "\n" in passwort:
            raise Fehler(f"PREVIEW_PASSWORD fehlt, ist kürzer als {PASSWORT_MIN} Zeichen oder mehrzeilig")
        gehasht = hasher(benutzer, passwort)
        if not BCRYPT.match(gehasht):
            raise Fehler("Der Hash des Vorschau-Passworts hat nicht die erwartete Form")
        aus["VORSCHAU_BENUTZER"] = f"{benutzer}:{gehasht}"
        aus["SPONSORPLATZ_MIDDLEWARES"] = MIDDLEWARES_VORSCHAU
    else:
        aus["VORSCHAU_BENUTZER"] = ""
        aus["SPONSORPLATZ_MIDDLEWARES"] = MIDDLEWARES_OEFFENTLICH

    admin = umgebung.get("SPONSORPLATZ_ADMIN_EMAIL") or ""
    if admin and not EMAIL.match(admin):
        raise Fehler("SPONSORPLATZ_ADMIN_EMAIL ist keine einzelne E-Mail-Adresse")
    aus["SPONSORPLATZ_ADMIN_EMAIL"] = admin

    smtp_host = umgebung.get("SMTP_HOST") or ""
    smtp_user = umgebung.get("SMTP_USER") or ""
    smtp_port = umgebung.get("SMTP_PORT") or "587"
    if smtp_host and not HOSTNAME.match(smtp_host):
        raise Fehler("SMTP_HOST ist kein Hostname")
    if smtp_user and not EMAIL.match(smtp_user):
        raise Fehler("SMTP_USER ist keine einzelne E-Mail-Adresse")
    if not re.fullmatch(r"[0-9]{2,5}", smtp_port):
        raise Fehler("SMTP_PORT ist keine Portnummer")
    aus["SMTP_HOST"] = smtp_host
    aus["SMTP_PORT"] = smtp_port
    aus["SMTP_USER"] = smtp_user
    # Ohne Mail-Server bleibt der Versand aus; mit Server immer angemeldet und verschlüsselt
    aus["SMTP_AUTH"] = "true" if smtp_host else "false"
    aus["SMTP_STARTTLS"] = "true" if smtp_host else "false"
    return aus


def als_text(eintraege: dict) -> str:
    """Einfache Anführungszeichen: Compose setzt in solchen Werten nichts mehr ein ($ im Hash bleibt $)."""
    zeilen = []
    for name, wert in eintraege.items():
        if "'" in wert or "\n" in wert or "\r" in wert:
            raise Fehler(f"{name} enthält ein Anführungszeichen oder einen Zeilenumbruch")
        zeilen.append(f"{name}='{wert}'")
    return "\n".join(zeilen) + "\n"


def main(argv: list) -> int:
    if len(argv) != 2:
        print("Aufruf: schreibe_env.py <ziel>", file=sys.stderr)
        return 2
    try:
        text = als_text(werte(dict(os.environ)))
    except Fehler as e:
        print(f"::error::{e}; kein Deploy.")
        return 1
    alt = os.umask(0o077)
    try:
        with open(argv[1], "w", encoding="utf-8") as datei:
            datei.write(text)
    finally:
        os.umask(alt)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
