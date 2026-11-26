# language: de
@spec-03
Funktionalität: Prüfung von Organisationen
  Die Verwaltung hält Entscheide fest und ungeprüfte Organisationen bleiben verborgen.

  @req-2.1
  Szenario: Drei Prüfpunkte werden beim Entscheid festgehalten
    Angenommen eine ausstehende Organisation für die Prüfung
    Wenn die Verwaltung alle drei Prüfpunkte bestätigt
    Dann enthält das Protokoll alle drei Prüfpunkte

  @req-2.2
  Szenario: Eine ungeprüfte Organisation und ihr Projekt bleiben verborgen
    Angenommen eine ausstehende Organisation mit einem öffentlichen Projekt
    Wenn ich die öffentlichen Listen und Projektdetails öffne
    Dann sind die Organisation und das Projekt nicht sichtbar

  @req-2.3
  Szenario: Eine Ablehnung benötigt einen Grund und teilt ihn der Kontaktperson mit
    Angenommen eine ausstehende Organisation für die Prüfung
    Wenn die Verwaltung sie mit einem Ablehnungsgrund ablehnt
    Dann erhält die Kontaktperson den Ablehnungsgrund per E-Mail

  @req-2.4
  Szenario: Eine Organisation kann ohne UID gespeichert werden
    Angenommen eine ausstehende Organisation ohne UID
    Dann ist die Organisation ohne UID gespeichert

  @req-2.5
  Szenario: Das Prüfprotokoll nennt entscheidende Person und Zeitpunkt
    Angenommen eine ausstehende Organisation für die Prüfung
    Wenn die Verwaltung alle drei Prüfpunkte bestätigt
    Dann enthält das Protokoll die entscheidende Person und den Zeitpunkt

  @req-15.1
  Szenario: Eine Freigabe wird der Kontaktperson per E-Mail mitgeteilt
    Angenommen eine ausstehende Organisation für die Prüfung
    Wenn die Verwaltung alle drei Prüfpunkte bestätigt
    Dann erhält die Kontaktperson das Ergebnis per E-Mail
