package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.Einwilligung;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;

import java.time.Instant;

public interface PilotAnfrageAblage {

    long speichereOderAktualisiere(PilotAnfrage anfrage, Einwilligung einwilligung, Instant jetzt);
}
