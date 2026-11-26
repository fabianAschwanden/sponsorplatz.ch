"""Tests für pruefe-testaufteilung.py: je Job zählt nur der neueste Versuch (Re-run failed jobs)."""
import importlib.util
import pathlib
import tempfile
import unittest

_SPEC = importlib.util.spec_from_file_location(
    "pruefe_testaufteilung", pathlib.Path(__file__).with_name("pruefe-testaufteilung.py"))
modul = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(modul)


def bericht(verzeichnis, klasse, fehler):
    verzeichnis.mkdir(parents=True, exist_ok=True)
    (verzeichnis / f"TEST-{klasse}.xml").write_text(
        f'<testsuite name="{klasse}" tests="1" skipped="0" failures="{fehler}" errors="0"/>', encoding="utf-8")


class NeuesterVersuchTest(unittest.TestCase):
    def test_nimmt_den_hoechsten_versuch(self):
        with tempfile.TemporaryDirectory() as tmp:
            basis = pathlib.Path(tmp)
            for versuch in (1, 2, 10):
                (basis / f"surefire-gruppe-1-{versuch}").mkdir()
            (basis / "surefire-gruppe-10-1").mkdir()
            self.assertEqual(modul.neuester(basis / "surefire-gruppe-1").name, "surefire-gruppe-1-10")

    def test_ohne_versuch_bleibt_der_pfad(self):
        with tempfile.TemporaryDirectory() as tmp:
            pfad = pathlib.Path(tmp) / "surefire-backend"
            self.assertEqual(modul.neuester(pfad), pfad)

    def test_alter_fehlbericht_zaehlt_nicht(self):
        with tempfile.TemporaryDirectory() as tmp:
            basis = pathlib.Path(tmp)
            quellen = basis / "src" / "a"
            quellen.mkdir(parents=True)
            (quellen / "ATest.java").write_text("class ATest { @Test void a() {} }", encoding="utf-8")
            bericht(basis / "b" / "surefire-gruppe-1-1", "a.ATest", 1)
            bericht(basis / "b" / "surefire-gruppe-1-2", "a.ATest", 0)
            rc = modul.main(["x", str(basis / "src"), f"gruppe-1={basis / 'b' / 'surefire-gruppe-1'}"])
            self.assertEqual(rc, 0)

    def test_verschachtelte_klasse_zaehlt_zur_aeusseren(self):
        with tempfile.TemporaryDirectory() as tmp:
            basis = pathlib.Path(tmp)
            bericht(basis / "b", "a.ATest$Innen", 0)
            bericht(basis / "b", "a.ATest", 0)
            self.assertEqual(modul.berichte(basis / "b"), {"a.ATest": (2, 0, 0)})

    def test_e2e_und_klassen_ohne_test_zaehlen_nicht(self):
        with tempfile.TemporaryDirectory() as tmp:
            quellen = pathlib.Path(tmp)
            for pfad, inhalt in (("a/ATest.java", "class ATest { @Test void a() {} }"),
                                 ("a/BIT.java", "class BIT { @Test void b() {} }"),
                                 ("a/HelferTest.java", "class HelferTest { }"),
                                 ("a/BasisTest.java", "abstract class BasisTest { @Test void c() {} }"),
                                 ("e2e/SmokeIT.java", "class SmokeIT { @Test void s() {} }")):
                (quellen / pfad).parent.mkdir(parents=True, exist_ok=True)
                (quellen / pfad).write_text(inhalt, encoding="utf-8")
            self.assertEqual(modul.erwartete_klassen(quellen), {"a.ATest", "a.BIT"})

    def test_klasse_in_zwei_jobs_ist_ein_fehler(self):
        with tempfile.TemporaryDirectory() as tmp:
            basis = pathlib.Path(tmp)
            (basis / "src" / "a").mkdir(parents=True)
            (basis / "src" / "a" / "ATest.java").write_text("class ATest { @Test void a() {} }", encoding="utf-8")
            bericht(basis / "eins", "a.ATest", 0)
            bericht(basis / "zwei", "a.ATest", 0)
            rc = modul.main(["x", str(basis / "src"), f"eins={basis / 'eins'}", f"zwei={basis / 'zwei'}"])
            self.assertEqual(rc, 1)

    def test_klasse_in_keinem_job_ist_ein_fehler(self):
        with tempfile.TemporaryDirectory() as tmp:
            basis = pathlib.Path(tmp)
            (basis / "src" / "a").mkdir(parents=True)
            for name in ("ATest", "BTest"):
                (basis / "src" / "a" / f"{name}.java").write_text(f"class {name} {{ @Test void a() {{}} }}", encoding="utf-8")
            bericht(basis / "eins", "a.ATest", 0)
            self.assertEqual(modul.main(["x", str(basis / "src"), f"eins={basis / 'eins'}"]), 1)


if __name__ == "__main__":
    unittest.main()
