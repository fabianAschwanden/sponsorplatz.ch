package ch.sponsorplatz.pilotanfrage.adapter.out;

import ch.sponsorplatz.pilotanfrage.application.DrosselungPort;
import ch.sponsorplatz.shared.config.FormularDrosselungService;
import org.springframework.stereotype.Component;

@Component
public class DrosselungAdapter implements DrosselungPort {

    private final FormularDrosselungService drosselung;

    public DrosselungAdapter(FormularDrosselungService drosselung) {
        this.drosselung = drosselung;
    }

    @Override
    public boolean erfasseEingabe(String formular, String clientAdresse) {
        return drosselung.erfasseEingabe(formular, clientAdresse);
    }
}
