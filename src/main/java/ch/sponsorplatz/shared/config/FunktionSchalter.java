package ch.sponsorplatz.shared.config;

public record FunktionSchalter(
        boolean zahlung,
        boolean vertrag,
        boolean rechnung,
        boolean aufgaben,
        boolean matching) {

    public boolean istAktiv(Bereich bereich) {
        return switch (bereich) {
            case ZAHLUNG -> zahlung;
            case VERTRAG -> vertrag;
            case RECHNUNG -> rechnung;
            case AUFGABEN -> aufgaben;
            case MATCHING -> matching;
        };
    }

    public enum Bereich {
        ZAHLUNG,
        VERTRAG,
        RECHNUNG,
        AUFGABEN,
        MATCHING
    }
}
