# language: de
Funktionalität: Vom Verein zur Sponsoring-Vereinbarung mit einem Sponsor

  Der vom User gewünschte E2E-Flow: ein Verein-Owner registriert sich,
  legt seinen Verein an, sendet proaktiv eine Sponsoring-Anfrage an den Sponsor.
  Der Sponsor nimmt die Anfrage an. Der Verein-Owner sieht den „Vertrag erstellen"-
  Button und lässt einen Vertrags-Entwurf entstehen — Snapshot der Betreff-
  und Org-Daten aus der Kontakt-Anfrage.

  Fachliche Spec: specs/KONTAKT_ANFRAGE_VERTRAG.md

  Hintergrund:
    Angenommen die Plattform läuft mit dem Test-Sponsor "Muster Versicherung"

  Szenario: Verein registriert sich, stellt Kontakt-Anfrage, Sponsor nimmt an, Vertrag entsteht
    Wenn ich mich als neuer Verein-Owner "Max Muster" registriere
    Und meine E-Mail-Adresse bestätigt wird
    Und ich mich einlogge
    Und ich im Onboarding den Verein "FC E2E" in der Branche "SPORT" anlege
    Und ich eine Kontakt-Anfrage an "Muster Versicherung" mit Betreff "Sommerfest-Sponsoring 2026" stelle

    Und sich der Sponsor "Muster Versicherung" einloggt
    Und der Sponsor die Kontakt-Anfrage von "FC E2E" annimmt

    Und ich mich wieder als Verein-Owner einlogge
    Und ich für die angenommene Kontakt-Anfrage einen Vertrag erstelle

    Dann existiert in der Datenbank ein Vertrag zwischen "FC E2E" und "Muster Versicherung"
    Und der Vertrag referenziert die Kontakt-Anfrage als Quelle
