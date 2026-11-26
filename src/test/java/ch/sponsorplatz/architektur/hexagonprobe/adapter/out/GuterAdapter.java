package ch.sponsorplatz.architektur.hexagonprobe.adapter.out;

import ch.sponsorplatz.architektur.hexagonprobe.application.EinPort;

/** Kein Verstoss: implementiert einen Port aus application. */
public class GuterAdapter implements EinPort {
    @Override
    public String lies() {
        return "x";
    }
}
