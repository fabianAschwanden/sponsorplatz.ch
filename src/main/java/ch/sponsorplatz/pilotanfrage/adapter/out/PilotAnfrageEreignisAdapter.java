package ch.sponsorplatz.pilotanfrage.adapter.out;

import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageBenachrichtigungEvent;
import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageEreignisPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PilotAnfrageEreignisAdapter implements PilotAnfrageEreignisPort {

    private final ApplicationEventPublisher eventPublisher;

    public PilotAnfrageEreignisAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publiziere(PilotAnfrageBenachrichtigungEvent ereignis) {
        eventPublisher.publishEvent(ereignis);
    }
}
