package ch.sponsorplatz.region.application;

import java.util.Optional;
import java.util.UUID;

public interface RegionBenutzerPort {
    Optional<UUID> findeIdNachAnmeldename(String anmeldename);
}
