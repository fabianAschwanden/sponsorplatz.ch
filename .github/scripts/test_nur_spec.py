"""Tests für nur-spec.py: strenge Positivliste, im Zweifel voller Lauf."""
import importlib.util
import io
import pathlib
import unittest

_SPEC = importlib.util.spec_from_file_location("nur_spec", pathlib.Path(__file__).with_name("nur-spec.py"))
modul = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(modul)


def pfade(*namen):
    return modul.bestimmen(["x", "pfade"], io.BytesIO(b"\0".join(n.encode() for n in namen)))[0]


class NurSpecTest(unittest.TestCase):
    def test_markdown_unter_kiro_docs_und_wurzel(self):
        self.assertTrue(pfade(".kiro/specs/01-betrieb/tasks.md", "docs/bestand/adr/0001.md", "README.md", "CLAUDE.md"))

    def test_alles_andere_verlangt_den_vollen_lauf(self):
        for pfad in ("src/main/resources/hilfe.md", "src/test/java/ch/sponsorplatz/XTest.java", "pom.xml",
                     ".github/CONTRIBUTING.md", ".github/workflows/ci.yml", ".github/scripts/nur-spec.py",
                     "docs/architektur/workspace.dsl", "docs/logo.html", "docker/Dockerfile", "deploy/readme.md",
                     ".kiro/steering/tech.txt", "docs/../src/x.md", "docs//x.md"):
            with self.subTest(pfad=pfad):
                self.assertFalse(pfade("README.md", pfad))

    def test_ohne_datei_kein_nur_spec(self):
        self.assertFalse(pfade())

    def test_unbekannte_art_und_unbekannter_commit(self):
        self.assertFalse(modul.bestimmen(["x", "workflow_dispatch"])[0])
        self.assertFalse(modul.bestimmen(["x", "pr", "0" * 40, "0" * 40])[0])


if __name__ == "__main__":
    unittest.main()
