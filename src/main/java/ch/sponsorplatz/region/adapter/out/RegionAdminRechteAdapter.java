package ch.sponsorplatz.region.adapter.out;

import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.benutzer.PlatformRolle;
import ch.sponsorplatz.region.application.RegionAdminRechtePort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegionAdminRechteAdapter implements RegionAdminRechtePort {
    private final AppUserRepository benutzer;

    public RegionAdminRechteAdapter(AppUserRepository benutzer) {
        this.benutzer = benutzer;
    }

    @Override
    public boolean istPlattformAdmin(UUID benutzerId) {
        return benutzer.findById(benutzerId)
                .filter(appUser -> appUser.getPlatformRolle() == PlatformRolle.PLATFORM_ADMIN)
                .isPresent();
    }
}
