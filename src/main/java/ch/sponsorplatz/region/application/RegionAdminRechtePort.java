package ch.sponsorplatz.region.application;

import java.util.UUID;

public interface RegionAdminRechtePort {
    boolean istPlattformAdmin(UUID benutzerId);
}
