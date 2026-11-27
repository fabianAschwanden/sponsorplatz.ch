package ch.sponsorplatz.pilotanfrage.adapter.in.event;

import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageBenachrichtigungEvent;
import ch.sponsorplatz.pilotanfrage.application.PilotBenachrichtigungPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PilotBenachrichtigungListener {

    private final PilotBenachrichtigungPort benachrichtigung;

    public PilotBenachrichtigungListener(PilotBenachrichtigungPort benachrichtigung) {
        this.benachrichtigung = benachrichtigung;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void nachCommit(PilotAnfrageBenachrichtigungEvent event) {
        benachrichtigung.meldeNeueAnfrage(
                event.anfrageId(), event.rolle(), event.bereich(), event.sponsorArt(), event.plzKreis());
    }
}
