#!/usr/bin/env python3
"""Tests über die CI-Jobs vollständig (ci.yml, Job «testzahl»).

Die Tests laufen in mehreren Jobs: je Gruppe einer und «rest» für alles Übrige (testgruppen.py). Aus den
Surefire-Berichten (TEST-*.xml) der Jobs wird geprüft: Jede Testklasse lief in genau einem Job, kein Job ist leer, kein
Test ist fehlgeschlagen. Testklasse heisst: Datei unter <testquellen> nach den Mustern des pom.xml (*Test, *Tests, *IT),
nicht unter e2e/ (die Browser-Prüfungen laufen über Failsafe im Job «e2e»), nicht abstrakt, mit mindestens einem Test
(JUnit oder ArchUnit). Verschachtelte Klassen (@Nested) zählen zu ihrer äusseren Klasse. Die Zahlen je Job gehen auch
in die Zusammenfassung des Laufs (GITHUB_STEP_SUMMARY).

Aufruf: pruefe-testaufteilung.py <testquellen> <job>=<berichtsverzeichnis> ...
Ausgabe nur Klassennamen und Zahlen.
"""
import os
import pathlib
import re
import sys
import xml.etree.ElementTree as ET

MUSTER = re.compile(r"^(.*Test|.*Tests|.*IT)$")
# Verzeichnisse, die Surefire nicht ausführt (pom.xml, <excludes>)
OHNE = ("e2e",)
TEST = re.compile(r"@(Test|ParameterizedTest|TestFactory|RepeatedTest|TestTemplate|ArchTest)\b")


def erwartete_klassen(quellen):
    klassen = set()
    for datei in quellen.rglob("*.java"):
        if any(teil in OHNE for teil in datei.relative_to(quellen).parts[:-1]):
            continue
        name = datei.stem
        text = datei.read_text(encoding="utf-8")
        if MUSTER.match(name) and TEST.search(text) and not re.search(rf"abstract\s+class\s+{name}\b", text):
            klassen.add(".".join(datei.relative_to(quellen).with_suffix("").parts))
    return klassen


def berichte(verzeichnis):
    """{Klasse: (Tests, übersprungen, Fehler)} aus den TEST-*.xml eines Jobs."""
    erg = {}
    for datei in sorted(pathlib.Path(verzeichnis).rglob("TEST-*.xml")):
        suite = ET.parse(datei).getroot()
        zahl = lambda a: int(suite.get(a, "0"))
        # @Nested: «Aussen$Innen» zählt zur äusseren Klasse
        klasse = suite.get("name").split("$", 1)[0]
        bisher = erg.get(klasse, (0, 0, 0))
        erg[klasse] = (bisher[0] + zahl("tests"), bisher[1] + zahl("skipped"), bisher[2] + zahl("failures") + zahl("errors"))
    return erg


def neuester(pfad):
    """Verzeichnis des neuesten Versuchs: Artefakte heissen `<name>-<versuch>` (github.run_attempt); nach
    «Re-run failed jobs» liegen auch die Berichte früherer Versuche im Lauf und dürfen nicht mitzählen."""
    basis = pathlib.Path(pfad)
    versuche = [p for p in basis.parent.glob(basis.name + "-*") if p.name[len(basis.name) + 1:].isdigit()]
    return max(versuche, key=lambda p: int(p.name[len(basis.name) + 1:])) if versuche else basis

def main(argv):
    quellen = pathlib.Path(argv[1])
    jobs = dict(arg.split("=", 1) for arg in argv[2:])
    erwartet = erwartete_klassen(quellen)
    gelaufen = {job: berichte(neuester(pfad)) for job, pfad in jobs.items()}
    fehler = []
    zeilen = ["| Job | Klassen | Tests | übersprungen |", "| --- | ---: | ---: | ---: |"]
    for job, klassen in gelaufen.items():
        tests = sum(t for t, _, _ in klassen.values())
        zeilen.append(f"| {job} | {len(klassen)} | {tests} | {sum(s for _, s, _ in klassen.values())} |")
        if tests == 0:
            fehler.append(f"Job {job}: keine Tests")
        fehler += [f"Job {job}: {k} mit {f} Fehlern" for k, (_, _, f) in sorted(klassen.items()) if f]
    alle = [k for klassen in gelaufen.values() for k in klassen]
    zeilen.append(f"| Summe | {len(alle)} | {sum(t for kl in gelaufen.values() for t, _, _ in kl.values())} | "
                  f"{sum(s for kl in gelaufen.values() for _, s, _ in kl.values())} |")
    for k in sorted(erwartet):
        jobs_k = [job for job, klassen in gelaufen.items() if k in klassen]
        if len(jobs_k) != 1:
            fehler.append(f"{k}: lief in {len(jobs_k)} Jobs {jobs_k}, erwartet genau einer")
    for k in sorted(set(alle) - erwartet):
        if alle.count(k) > 1:
            fehler.append(f"{k}: lief in mehreren Jobs")
    print("\n".join(zeilen))
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as f:
            f.write("### Tests je Job\n\n" + "\n".join(zeilen) + f"\n\n{len(erwartet)} Testklassen im Modul\n")
    for f in fehler:
        print(f"::error::{f}")
    if fehler:
        return 1
    print(f"Alle {len(erwartet)} Testklassen liefen in genau einem Job.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
