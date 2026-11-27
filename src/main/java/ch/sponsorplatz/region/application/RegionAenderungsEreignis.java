package ch.sponsorplatz.region.application;

import java.util.UUID;

public record RegionAenderungsEreignis(
        UUID regionId,
        UUID benutzerId,
        RegionAenderungsart aenderungsart,
        String details) {
}
