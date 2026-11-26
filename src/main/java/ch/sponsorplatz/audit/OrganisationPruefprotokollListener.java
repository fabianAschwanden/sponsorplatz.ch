package ch.sponsorplatz.audit;

import ch.sponsorplatz.organisation.OrganisationPruefprotokollEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrganisationPruefprotokollListener {

    private final AuditService auditService;

    public OrganisationPruefprotokollListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void protokolliere(OrganisationPruefprotokollEvent event) {
        String aktion = event.freigegeben()
                ? AuditAktion.VERIFIZIERT
                : AuditAktion.PRUEFUNG_ABGELEHNT;
        auditService.protokolliereMitBenutzer(aktion, "ORGANISATION", event.benutzerId(), null,
                event.organisationId(), "Organisation", event.details());
    }
}
