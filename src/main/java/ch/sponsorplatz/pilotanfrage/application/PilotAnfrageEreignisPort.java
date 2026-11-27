package ch.sponsorplatz.pilotanfrage.application;

public interface PilotAnfrageEreignisPort {

    void publiziere(PilotAnfrageBenachrichtigungEvent ereignis);
}
