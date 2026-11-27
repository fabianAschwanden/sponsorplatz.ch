package ch.sponsorplatz.shared.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Value;

@ControllerAdvice
public class FunktionSchalterModelAdvice {
    private final boolean zahlung;
    private final boolean vertrag;
    private final boolean rechnung;
    private final boolean aufgaben;
    private final boolean matching;

    public FunktionSchalterModelAdvice(
            @Value("${sponsorplatz.funktion.zahlung:false}") boolean zahlung,
            @Value("${sponsorplatz.funktion.vertrag:false}") boolean vertrag,
            @Value("${sponsorplatz.funktion.rechnung:false}") boolean rechnung,
            @Value("${sponsorplatz.funktion.aufgaben:false}") boolean aufgaben,
            @Value("${sponsorplatz.funktion.matching:false}") boolean matching) {
        this.zahlung = zahlung;
        this.vertrag = vertrag;
        this.rechnung = rechnung;
        this.aufgaben = aufgaben;
        this.matching = matching;
    }

    @ModelAttribute("zahlungAktiv")
    public boolean zahlungAktiv() {
        return zahlung;
    }

    @ModelAttribute("vertragAktiv")
    public boolean vertragAktiv() {
        return vertrag;
    }

    @ModelAttribute("rechnungAktiv")
    public boolean rechnungAktiv() {
        return rechnung;
    }

    @ModelAttribute("aufgabenAktiv")
    public boolean aufgabenAktiv() {
        return aufgaben;
    }

    @ModelAttribute("matchingAktiv")
    public boolean matchingAktiv() {
        return matching;
    }
}
