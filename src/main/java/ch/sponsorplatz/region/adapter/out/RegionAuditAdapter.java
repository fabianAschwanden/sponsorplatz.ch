package ch.sponsorplatz.region.adapter.out;

import ch.sponsorplatz.audit.AuditAktion;
import ch.sponsorplatz.audit.AuditService;
import ch.sponsorplatz.region.application.RegionAenderungsEreignis;
import ch.sponsorplatz.region.application.RegionAenderungsart;
import ch.sponsorplatz.region.application.RegionEreignisPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegionAuditAdapter implements RegionEreignisPort {
    private final AuditService auditService;

    public RegionAuditAdapter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void protokolliere(RegionAenderungsEreignis ereignis) {
        auditService.protokolliereInLaufenderTransaktion(
                aktion(ereignis.aenderungsart()), "REGION", ereignis.benutzerId(),
                ereignis.regionId(), "Region", ereignis.details());
    }

    private String aktion(RegionAenderungsart art) {
        return switch (art) {
            case ANGELEGT -> AuditAktion.REGION_ANGELEGT;
            case UMBENANNT -> AuditAktion.REGION_UMBENANNT;
            case PLZ_BEREICHE_GEAENDERT -> AuditAktion.REGION_PLZ_GEAENDERT;
            case STUFE_FREIGESCHALTET -> AuditAktion.REGION_STUFE_FREIGESCHALTET;
            case STUFE_ABGESCHALTET -> AuditAktion.REGION_STUFE_ABGESCHALTET;
        };
    }
}
