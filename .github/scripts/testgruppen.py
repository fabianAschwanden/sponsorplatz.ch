#!/usr/bin/env python3
"""Teilt die Tests (Surefire) auf die parallelen CI-Jobs auf (ci.yml, Job «tests»).

Eine Gruppe ist eine Liste von Fachpaketen unter src/test/java/ch/sponsorplatz/. Alles, was in keiner Gruppe steht,
läuft im Job «rest»: Ein neues Paket braucht darum keinen Eintrag und läuft trotzdem. Die Browser-Prüfungen unter e2e/
gehören zu keiner Gruppe; sie laufen über Failsafe im Job «e2e».

Aufruf:
  testgruppen.py <gruppe>   Muster der Gruppe in einer Zeile, durch Komma getrennt, für -Dtest=…
  testgruppen.py rest       Muster aller Gruppen und von e2e/, eines pro Zeile, für -Dsurefire.excludesFile=…
  testgruppen.py --namen    die Namen aller Jobs, einer pro Zeile (Gruppen und «rest»)

Warum zwei Wege: -Dsurefire.includesFile ergänzt die <includes> des pom.xml nur, die Gruppe liefe dann mit allen
anderen Tests zusammen (lokal geprüft am 26.11.2026). -Dtest ersetzt sie. Die Ausschlussdatei für «rest» wirkt wie
erwartet.

Stand der Aufteilung: gemessen am 26.11.2026, 172 Klassen, 903 Tests, zusammen 27 s reine Testzeit (gruppe-1 19 s,
rest 9 s). Die Aufteilung lohnt sich erst mit langsameren Tests (PostgreSQL in neuen Fachbereichen); dann hier
umverteilen. Der Job «testzahl» prüft, dass jede Testklasse in genau einem Job lief.
"""
import pathlib
import sys

WURZEL = "ch/sponsorplatz"
# Nicht Teil der Surefire-Läufe (pom.xml schliesst e2e/ aus); steht hier, damit «rest» es auch dann ausschliesst, wenn
# eine Ausschlussdatei die Ausschlüsse des pom ersetzt.
OHNE = ["e2e"]
GRUPPEN = {
    "gruppe-1": ["benutzer", "aufgabe", "home", "shared"],
}
REST = "rest"
# wie <includes> im pom.xml
ENDUNGEN = ("Test.java", "Tests.java", "IT.java")


def muster(pakete):
    return [f"{WURZEL}/{paket}/**/*{endung}" for paket in pakete for endung in ENDUNGEN]


def zeilen(job):
    if job == REST:
        belegt = [paket for pakete in GRUPPEN.values() for paket in pakete]
        return muster(belegt) + [f"{WURZEL}/{paket}/**/*.java" for paket in OHNE]
    return muster(GRUPPEN[job])


def pruefe(testquellen):
    """Fehlerliste: Pakete doppelt, in keiner Gruppe vorhanden oder gleich einem reservierten Namen."""
    fehler = []
    gesehen = {}
    for gruppe, pakete in GRUPPEN.items():
        if gruppe == REST:
            fehler.append(f"Gruppe darf nicht «{REST}» heissen")
        for paket in pakete:
            if paket in gesehen:
                fehler.append(f"Paket {paket} steht in {gesehen[paket]} und {gruppe}")
            gesehen[paket] = gruppe
            if paket in OHNE:
                fehler.append(f"Paket {paket} läuft nicht über Surefire")
            if not (testquellen / WURZEL / paket).is_dir():
                fehler.append(f"Paket {paket} ({gruppe}) gibt es nicht unter {testquellen / WURZEL}")
    return fehler


def main(argv):
    if len(argv) != 2:
        print(__doc__, file=sys.stderr)
        return 2
    if argv[1] == "--namen":
        print("\n".join([*GRUPPEN, REST]))
        return 0
    if argv[1] != REST and argv[1] not in GRUPPEN:
        print(f"Unbekannter Job «{argv[1]}»; bekannt: {', '.join([*GRUPPEN, REST])}", file=sys.stderr)
        return 2
    fehler = pruefe(pathlib.Path(__file__).resolve().parents[2] / "src" / "test" / "java")
    if fehler:
        print("\n".join(fehler), file=sys.stderr)
        return 1
    print(("\n" if argv[1] == REST else ",").join(zeilen(argv[1])))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
