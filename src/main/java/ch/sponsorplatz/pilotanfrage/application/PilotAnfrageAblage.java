package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.Einwilligung;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;

import java.time.Instant;
import java.util.UUID;

public interface PilotAnfrageAblage {

    UUID speichereOderAktualisiere(PilotAnfrage anfrage, Einwilligung einwilligung, Instant jetzt);
}
