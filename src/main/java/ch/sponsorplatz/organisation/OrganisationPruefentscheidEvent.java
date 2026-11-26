package ch.sponsorplatz.organisation;

import java.util.UUID;

public record OrganisationPruefentscheidEvent(
        UUID organisationId,
        String organisationName,
        String empfaengerEmail,
        boolean angenommen,
        String ablehnungsgrund
) {
}
