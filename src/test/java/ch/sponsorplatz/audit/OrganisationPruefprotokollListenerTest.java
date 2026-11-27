package ch.sponsorplatz.audit;

import ch.sponsorplatz.organisation.OrganisationPruefprotokollEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganisationPruefprotokollListenerTest {

    @Mock private AuditService auditService;

    @Test
    void pruefentscheidWirdMitPersonUndDetailsProtokolliert() {
        UUID userId = UUID.randomUUID();
        UUID organisationId = UUID.randomUUID();
        var listener = new OrganisationPruefprotokollListener(auditService);

        listener.protokolliere(new OrganisationPruefprotokollEvent(
                userId, organisationId, false, "Prüfpunkte; Ablehnungsgrund: Beispielgrund"));

        verify(auditService).protokolliereInLaufenderTransaktion(AuditAktion.PRUEFUNG_ABGELEHNT,
                "ORGANISATION", userId, organisationId, "Organisation",
                "Prüfpunkte; Ablehnungsgrund: Beispielgrund");
    }
}
