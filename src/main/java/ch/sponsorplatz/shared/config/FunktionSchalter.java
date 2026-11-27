package ch.sponsorplatz.shared.config;

public record FunktionSchalter(
        boolean zahlung,
        boolean vertrag,
        boolean rechnung,
        boolean aufgaben,
        boolean matching) {
}
