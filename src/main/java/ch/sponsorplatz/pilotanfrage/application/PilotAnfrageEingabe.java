package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;

public record PilotAnfrageEingabe(
        PilotAnfrageRolle rolle,
        String organisationsName,
        PilotAnfrageBereich bereich,
        PilotSponsorArt sponsorArt,
        String plz,
        String ort,
        String vorname,
        String nachname,
        String email,
        String paketname,
        String gegenleistungen,
        String mitgliederzahl,
        String naechsterAnlass,
        String interessen,
        String zielgruppe,
        String marktgebiet,
        String programme,
        String uid,
        String funktion,
        String telefon,
        boolean updates) {
}
