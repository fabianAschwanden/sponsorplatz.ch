package ch.sponsorplatz.organisation;

import java.util.UUID;

public record OrganisationPruefprotokollEvent(
        UUID benutzerId, UUID organisationId, boolean freigegeben, String details) {
}
