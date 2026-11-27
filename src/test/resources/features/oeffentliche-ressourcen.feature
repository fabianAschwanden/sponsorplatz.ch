# language: de
@spec-03
Funktionalität: Ressourcen der öffentlichen Seiten
  Das gemeinsame Design lädt seine Schrift, Stylesheets und Skripte von der Plattform.

  @req-16.3
  Szenario: Öffentliche Ressourcen werden lokal ausgeliefert
    Angenommen die öffentliche Testseite ist mit dem Fragment gerendert
    Wenn ich Stylesheet, Skript und Schriften ohne Anmeldung lade
    Dann enthalten die Ressourcen keine fremden Adressen
