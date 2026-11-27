# language: de
@spec-03
Funktionalität: Pilot-Anfrage
  Organisationen können einen Pilotplatz anfragen, ohne dass bereits eine Organisation entsteht.

  @req-1.1
  Szenario: Ein Verein sendet die Pflichtangaben
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine gültige Anfrage absende
    Dann ist die Anfrage gespeichert und die Bestätigung sichtbar

  @req-1.1
  Szenario: Ein Sponsor wählt seine Art und sendet die Pflichtangaben
    Angenommen ich öffne die Pilot-Anfrage als Sponsor
    Wenn ich eine gültige Anfrage absende
    Dann ist die Anfrage gespeichert und die Bestätigung sichtbar

  @req-1.2
  Szenario: Freiwillige Angaben und abgewählte Updates werden gespeichert
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine Anfrage mit freiwilligen Angaben absende
    Dann sind die freiwilligen Angaben gespeichert und Updates sind abgewählt

  @req-1.3
  Szenario: Eine gleiche Anfrage innert zehn Minuten wird aktualisiert
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich dieselbe Anfrage zweimal mit geänderten Angaben absende
    Dann gibt es eine aktualisierte Anfrage mit beiden Einwilligungsnachweisen

  @req-1.4
  Szenario: Die Bestätigung erklärt Prüfung und Zugang
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine gültige Anfrage absende
    Dann erklärt die Bestätigung die nächsten Schritte ohne etwas öffentlich zu zeigen

  @req-13.1
  Szenario: Einwilligung wird mit Wortlaut und UTC-Zeitpunkt festgehalten
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine gültige Anfrage absende
    Dann ist der Wortlaut der Einwilligung mit Zeitpunkt gespeichert

  @req-13.2
  Szenario: Updates werden nach ausdrücklicher Auswahl gespeichert
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich die Updates ankreuze und die Anfrage absende
    Dann ist der Wunsch nach Updates gespeichert

  @req-13.2
  Szenario: Updates werden ohne Auswahl nicht gespeichert
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine gültige Anfrage absende
    Dann sind Updates abgewählt

  @req-14.1
  Szenario: Das Pilot-Formular und jedes Formular haben Stundenlimiten
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich zehn Anfragen von derselben Adresse und die elfte absende
    Dann wird die elfte Anfrage mit einer verständlichen 429-Seite abgewiesen
    Und das globale Limit gilt ebenfalls pro Formular

  @req-14.2
  Szenario: Nur HMAC-Prüfwerte bleiben höchstens einen Tag gespeichert
    Wenn ich Drosselungseinträge für IPv4 und IPv6 erfasse
    Dann ist nur der HMAC-Prüfwert gespeichert
    Und die IPv6-Adressen aus demselben Netz teilen sich ein Limit
    Und Einträge älter als ein Tag werden entfernt

  @req-15.2
  Szenario: Die Verwaltungsmeldung enthält keine Personendaten
    Angenommen ich öffne die Pilot-Anfrage als Verein
    Wenn ich eine gültige Anfrage absende
    Dann enthält die Verwaltungsmeldung nur Rolle Bereich PLZ-Kreis und Listenlink
