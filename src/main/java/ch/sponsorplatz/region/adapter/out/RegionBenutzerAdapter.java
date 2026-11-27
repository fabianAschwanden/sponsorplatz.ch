package ch.sponsorplatz.region.adapter.out;

import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.region.application.RegionBenutzerPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RegionBenutzerAdapter implements RegionBenutzerPort {
    private final AppUserRepository benutzer;

    public RegionBenutzerAdapter(AppUserRepository benutzer) {
        this.benutzer = benutzer;
    }

    @Override
    public Optional<UUID> findeIdNachAnmeldename(String anmeldename) {
        return benutzer.findByEmail(anmeldename).map(appUser -> appUser.getId());
    }
}
