"""Tests für testgruppen.py: Gruppen passen zum Repo, «rest» schliesst genau die Gruppen und e2e/ aus."""
import importlib.util
import pathlib
import unittest

_SPEC = importlib.util.spec_from_file_location("testgruppen", pathlib.Path(__file__).with_name("testgruppen.py"))
modul = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(modul)
QUELLEN = pathlib.Path(__file__).resolve().parents[2] / "src" / "test" / "java"


class TestgruppenTest(unittest.TestCase):
    def test_gruppen_passen_zum_repo(self):
        self.assertEqual(modul.pruefe(QUELLEN), [])

    def test_rest_schliesst_jede_gruppe_und_e2e_aus(self):
        rest = modul.zeilen(modul.REST)
        for gruppe in modul.GRUPPEN:
            for muster in modul.zeilen(gruppe):
                self.assertIn(muster, rest)
        self.assertIn("ch/sponsorplatz/e2e/**/*.java", rest)

    def test_matrix_in_ci_yml_nennt_dieselben_jobs(self):
        ci = (pathlib.Path(__file__).resolve().parents[1] / "workflows" / "ci.yml").read_text(encoding="utf-8")
        for name in [*modul.GRUPPEN, modul.REST]:
            self.assertIn(f"{name}=berichte/surefire-{name}", ci, "Job «testzahl» kennt die Gruppe nicht")
            self.assertRegex(ci, rf"gruppe: \[[^\]]*\b{name}\b[^\]]*\]", "Matrix des Jobs «tests» kennt die Gruppe nicht")

    def test_doppeltes_und_fehlendes_paket_faellt_auf(self):
        alt = dict(modul.GRUPPEN)
        try:
            modul.GRUPPEN.clear()
            modul.GRUPPEN.update({"a": ["benutzer"], "b": ["benutzer", "gibt-es-nicht"]})
            fehler = modul.pruefe(QUELLEN)
        finally:
            modul.GRUPPEN.clear()
            modul.GRUPPEN.update(alt)
        self.assertEqual(len(fehler), 2)


if __name__ == "__main__":
    unittest.main()
