package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;

public interface PilotBenachrichtigungPort {

    void meldeNeueAnfrage(long anfrageId, PilotAnfrageRolle rolle, PilotAnfrageBereich bereich,
            PilotSponsorArt sponsorArt, String plzKreis);
}
