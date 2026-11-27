package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.Stufe;

public interface StufenPruefung {
    boolean istFreigeschaltet(Stufe stufe, String plz);
}
