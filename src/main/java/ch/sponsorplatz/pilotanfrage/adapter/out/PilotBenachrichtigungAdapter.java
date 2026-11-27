package ch.sponsorplatz.pilotanfrage.adapter.out;

import ch.sponsorplatz.pilotanfrage.application.PilotBenachrichtigungPort;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;
import ch.sponsorplatz.shared.mail.MailVersand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class PilotBenachrichtigungAdapter implements PilotBenachrichtigungPort {

    private static final Logger log = LoggerFactory.getLogger(PilotBenachrichtigungAdapter.class);

    private final MailVersand mailVersand;
    private final MessageSource messageSource;
    private final String empfaenger;
    private final String basisUrl;

    public PilotBenachrichtigungAdapter(MailVersand mailVersand,
            MessageSource messageSource,
            @Value("${sponsorplatz.support.empfaenger:}") String empfaenger,
            @Value("${sponsorplatz.basis-url:http://localhost:8080}") String basisUrl) {
        this.mailVersand = mailVersand;
        this.messageSource = messageSource;
        this.empfaenger = empfaenger;
        this.basisUrl = basisUrl;
    }

    @Override
    public void meldeNeueAnfrage(UUID anfrageId, PilotAnfrageRolle rolle, PilotAnfrageBereich bereich,
            PilotSponsorArt sponsorArt, String plzKreis) {
        if (empfaenger.isBlank()) {
            log.warn("Keine Empfängeradresse für Pilot-Anfragen konfiguriert (Anfrage {}).", anfrageId);
            return;
        }

        String text = """
                Neue Pilot-Anfrage #%s

                Rolle: %s
                Bereich: %s
                PLZ-Kreis: %s

                Liste: %s/admin/pilotanfragen
                """.formatted(anfrageId, rolleText(rolle), bereichText(rolle, bereich, sponsorArt), plzKreis, basisUrl);
        try {
            mailVersand.sendePlain(empfaenger, "Neue Pilot-Anfrage #" + anfrageId, text);
        } catch (RuntimeException ex) {
            log.warn("Benachrichtigung für Pilot-Anfrage {} fehlgeschlagen ({}).",
                    anfrageId, ex.getClass().getSimpleName());
        }
    }

    private String rolleText(PilotAnfrageRolle rolle) {
        return rolle == PilotAnfrageRolle.VEREIN ? "Verein" : "Sponsor";
    }

    private String bereichText(PilotAnfrageRolle rolle, PilotAnfrageBereich bereich, PilotSponsorArt sponsorArt) {
        String schluessel = rolle == PilotAnfrageRolle.VEREIN
                ? "pilot.bereich." + bereich.name()
                : "pilot.sponsorArt." + sponsorArt.name();
        return messageSource.getMessage(schluessel, null, Locale.forLanguageTag("de-CH"));
    }
}
