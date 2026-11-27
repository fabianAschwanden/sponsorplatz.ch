package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.Stufe;

import java.util.List;
import java.util.UUID;

public interface RegionVerwaltung {
    List<RegionView> alleRegionen(UUID benutzerId);

    UUID legeRegionAn(String code, String name, UUID benutzerId);

    void benenneRegionUm(UUID regionId, String name, UUID benutzerId);

    void setzePlzBereiche(UUID regionId, String bereiche, UUID benutzerId);

    void schalteStufe(UUID regionId, Stufe stufe, boolean freigeschaltet, UUID benutzerId);
}
