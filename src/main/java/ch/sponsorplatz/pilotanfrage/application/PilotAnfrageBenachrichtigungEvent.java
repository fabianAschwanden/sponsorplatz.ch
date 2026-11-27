package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;

import java.util.UUID;

public record PilotAnfrageBenachrichtigungEvent(
        UUID anfrageId,
        PilotAnfrageRolle rolle,
        PilotAnfrageBereich bereich,
        PilotSponsorArt sponsorArt,
        String plzKreis) {
}
