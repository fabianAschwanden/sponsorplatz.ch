#!/usr/bin/env python3
"""Tests für schreibe_env.py: python3 -m unittest discover -s .github/scripts -p 'test_*.py'"""
import os
import sys
import unittest

sys.path.insert(0, os.path.dirname(__file__))
import schreibe_env as s  # noqa: E402

HASH = "$2y$05$" + "a" * 53
BASIS = {"HOST_APP": "sponsorplatz.apps.urinet.ch", "PREVIEW_USER": "vorschau",
         "PREVIEW_PASSWORD": "ein-langes-passwort"}


def hasher(benutzer, passwort):
    return HASH


class SchreibeEnvTest(unittest.TestCase):

    def test_vorschau_ist_der_normalfall(self):
        w = s.werte(dict(BASIS), hasher)
        self.assertEqual(w["VORSCHAU_BENUTZER"], "vorschau:" + HASH)
        self.assertEqual(w["SPONSORPLATZ_MIDDLEWARES"], s.MIDDLEWARES_VORSCHAU)
        self.assertEqual(w["SPONSORPLATZ_PROFILE"], "prod")
        self.assertEqual(w["BASIS_URL"], "https://sponsorplatz.apps.urinet.ch")

    def test_oeffentlich_ohne_vorschau_schutz(self):
        w = s.werte({"HOST_APP": "sponsorplatz.ch", "SITE_MODE": "oeffentlich"}, hasher)
        self.assertEqual(w["VORSCHAU_BENUTZER"], "")
        self.assertEqual(w["SPONSORPLATZ_MIDDLEWARES"], s.MIDDLEWARES_OEFFENTLICH)

    def test_oeffentlich_mit_demo_wird_abgelehnt(self):
        with self.assertRaises(s.Fehler):
            s.werte({"HOST_APP": "sponsorplatz.ch", "SITE_MODE": "oeffentlich",
                     "SPONSORPLATZ_PROFILE": "prod,demo"}, hasher)

    def test_vorschau_mit_demo_ist_erlaubt(self):
        w = s.werte(dict(BASIS, SPONSORPLATZ_PROFILE="prod,demo"), hasher)
        self.assertEqual(w["SPONSORPLATZ_PROFILE"], "prod,demo")

    def test_unbekannte_werte_werden_abgelehnt(self):
        for name, wert in [("SITE_MODE", "offen"), ("SPONSORPLATZ_PROFILE", "dev"),
                           ("HOST_APP", "kein host"), ("PREVIEW_USER", "Vor Schau"),
                           ("PREVIEW_PASSWORD", "kurz"), ("PREVIEW_PASSWORD", "zwei\nzeilen-lang-genug"),
                           ("SPONSORPLATZ_ADMIN_EMAIL", "a@b.ch, c@d.ch"), ("SMTP_HOST", "host mit leer"),
                           ("SMTP_USER", "ohne-at"), ("SMTP_PORT", "abc")]:
            with self.subTest(name=name), self.assertRaises(s.Fehler):
                s.werte(dict(BASIS, **{name: wert}), hasher)

    def test_fehlermeldung_nennt_den_wert_nicht(self):
        with self.assertRaises(s.Fehler) as kontext:
            s.werte(dict(BASIS, PREVIEW_PASSWORD="geheim"), hasher)
        self.assertNotIn("geheim", str(kontext.exception))

    def test_hash_in_unerwarteter_form_wird_abgelehnt(self):
        with self.assertRaises(s.Fehler):
            s.werte(dict(BASIS), lambda b, p: "klartext")

    def test_mail_nur_mit_server(self):
        ohne = s.werte(dict(BASIS), hasher)
        mit = s.werte(dict(BASIS, SMTP_HOST="mail.muster.example", SMTP_USER="post@muster.example"), hasher)
        self.assertEqual((ohne["SMTP_AUTH"], ohne["SMTP_STARTTLS"]), ("false", "false"))
        self.assertEqual((mit["SMTP_AUTH"], mit["SMTP_STARTTLS"]), ("true", "true"))

    def test_text_in_einfachen_anfuehrungszeichen(self):
        text = s.als_text({"VORSCHAU_BENUTZER": "vorschau:" + HASH, "LEER": ""})
        self.assertEqual(text, "VORSCHAU_BENUTZER='vorschau:" + HASH + "'\nLEER=''\n")
        with self.assertRaises(s.Fehler):
            s.als_text({"X": "mit'zeichen"})

    @unittest.skipUnless(any(os.access(os.path.join(p, "htpasswd"), os.X_OK)
                             for p in os.environ.get("PATH", "").split(os.pathsep)), "htpasswd fehlt")
    def test_echter_hash_hat_die_erwartete_form(self):
        self.assertRegex(s.bcrypt_hash("vorschau", "ein-langes-passwort"), s.BCRYPT)


if __name__ == "__main__":
    unittest.main()
