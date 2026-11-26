package ch.sponsorplatz.architektur.hexagonprobe.domain;

/** Verstoss: Betrag als Gleitkommazahl, als Feld und als Methode. */
public class PreisAlsGleitkomma {

    private final double preis;

    public PreisAlsGleitkomma(double wert) {
        this.preis = wert;
    }

    public double preis() {
        return preis;
    }
}
