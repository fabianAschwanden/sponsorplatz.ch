#!/usr/bin/env python3
"""Regeln für infomaniak-deploy.yml, die sich am Text prüfen lassen.

Das Repo ist öffentlich, die Logs des Workflows sind es auch. Darum: Geheimnisse
nur über env, nie in einer Befehlszeile; Ausgaben des Servers nie ungefiltert ins Log.
"""
import os
import re
import unittest

WORKFLOW = os.path.join(os.path.dirname(__file__), "..", "workflows", "infomaniak-deploy.yml")


class DeployWorkflowTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        with open(WORKFLOW, encoding="utf-8") as datei:
            cls.zeilen = datei.read().splitlines()
        cls.text = "\n".join(cls.zeilen)

    def test_geheimnisse_nur_ueber_env(self):
        for zeile in self.zeilen:
            if "secrets." in zeile and not zeile.lstrip().startswith("#"):
                self.assertRegex(zeile, r"^\s+[A-Z_]+: \$\{\{ secrets\.[A-Z_]+ \}\}$")

    def test_ausloeser_nur_von_hand_oder_release(self):
        kopf = self.text.split("permissions:")[0]
        self.assertIn("workflow_dispatch:", kopf)
        self.assertIn("tags: ['v*']", kopf)
        self.assertNotIn("branches:", kopf)

    def test_serverausgaben_nie_ungefiltert_im_log(self):
        for datei in ("status.txt", "deploy.log", "rollback.log", "bundle/.env"):
            self.assertIsNone(re.search(r"\b(cat|tee|tail|head)\b[^|\n]*" + re.escape(datei), self.text), datei)

    def test_kein_deploy_ohne_ci_budget_und_modus(self):
        for schritt in ("Commit liegt auf main", "Speicherbudget", "CI für diesen Stand grün",
                        "Geheimnisse und Variablen vorhanden", "Schutz von aussen", "Rollback"):
            self.assertIn("- name: " + schritt, self.text)
        self.assertIn("schreibe_env.py bundle/.env", self.text)
        self.assertLess(self.text.index("CI für diesen Stand grün"), self.text.index("- name: Deployen"))

    def test_hash_wird_maskiert(self):
        self.assertIn("::add-mask::", self.text)


if __name__ == "__main__":
    unittest.main()
