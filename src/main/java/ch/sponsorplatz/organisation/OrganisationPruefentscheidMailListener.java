package ch.sponsorplatz.organisation;

import ch.sponsorplatz.shared.mail.MailVersand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrganisationPruefentscheidMailListener {

    private static final Logger log = LoggerFactory.getLogger(OrganisationPruefentscheidMailListener.class);

    private final MailVersand mailVersand;

    public OrganisationPruefentscheidMailListener(MailVersand mailVersand) {
        this.mailVersand = mailVersand;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aufPruefentscheid(OrganisationPruefentscheidEvent event) {
        try {
            mailVersand.sendePlain(event.empfaengerEmail(),
                    "Sponsorplatz — Ergebnis der Organisationsprüfung",
                    mailText(event));
        } catch (RuntimeException ex) {
            log.warn("Prüfentscheid-Mail für Organisation {} konnte nicht gesendet werden ({})",
                    event.organisationId(), ex.getClass().getSimpleName());
        }
    }

    private String mailText(OrganisationPruefentscheidEvent event) {
        if (event.angenommen()) {
            return "Die Prüfung eurer Organisation «" + event.organisationName()
                    + "» ist abgeschlossen. Eure Organisation wurde freigegeben.";
        }
        return "Die Prüfung eurer Organisation «" + event.organisationName()
                + "» ist abgeschlossen. Eure Organisation wurde abgelehnt.\n\nGrund: "
                + event.ablehnungsgrund();
    }
}
