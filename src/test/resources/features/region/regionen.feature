# language: de
@spec-01
Funktionalität: Regionen und Freischaltungsstufen

  @req-11.1
  Szenario: Eine Plattform-Admin-Person schaltet eine Stufe für eine neue Region frei
    Angenommen ich bin als Plattform-Admin in der Regionenverwaltung
    Wenn ich eine Region anlege und Stufe 2 freischalte
    Dann ist Stufe 2 für diese Region freigeschaltet

  @req-11.2
  Szenario: Eine Funktion ohne Freischaltung bleibt für eine PLZ gesperrt
    Angenommen die Pilotregion ist mit Stufe 1 freigeschaltet
    Wenn eine Beispielfunktion Stufe 2 für PLZ 8000 über den Prüfport anfragt
    Dann antwortet die Beispielfunktion mit 404

  @req-11.3
  Szenario: Der Name einer Region kann geändert werden
    Angenommen ich bin als Plattform-Admin in der Regionenverwaltung
    Wenn ich eine Region umbenenne
    Dann wird der neue Regionsname angezeigt

  @req-13.1
  Szenario: Zahlungsrouten fordern bei abgeschalteter Zahlung keinen Betrag
    Wenn ich alle Zahlungsrouten bei ausgeschalteter Zahlung prüfe
    Dann antworten alle Zahlungsrouten mit 404

  @req-13.2
  Szenario: Abgeschaltete Funktionsbereiche sind nicht erreichbar
    Wenn ich Routen für Zahlung Vertrag Rechnung Aufgaben und Matching prüfe
    Dann antworten alle abgeschalteten Funktionsrouten mit 404
