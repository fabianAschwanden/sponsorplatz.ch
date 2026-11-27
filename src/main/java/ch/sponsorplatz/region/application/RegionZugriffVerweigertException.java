package ch.sponsorplatz.region.application;

public class RegionZugriffVerweigertException extends RuntimeException {
    public RegionZugriffVerweigertException() {
        super("Für diese Aktion ist die Rolle Plattform-Admin erforderlich.");
    }
}
