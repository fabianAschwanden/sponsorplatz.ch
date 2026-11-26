package ch.sponsorplatz.architektur.hexagonprobe.application;

import ch.sponsorplatz.architektur.hexagonprobe.adapter.out.GuterAdapter;

/** Verstoss: application kennt einen Adapter. */
public class AnwendungMitAdapter {

    private final GuterAdapter adapter = new GuterAdapter();

    public String lies() {
        return adapter.lies();
    }
}
