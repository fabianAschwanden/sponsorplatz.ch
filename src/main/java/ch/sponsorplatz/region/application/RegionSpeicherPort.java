package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionSpeicherPort {
    List<RegionDaten> alleRegionen();

    Optional<RegionDaten> findeRegion(UUID id);

    boolean existiertCode(String code);

    RegionDaten speichereRegion(RegionDaten region);

    void speicherePlzBereiche(UUID regionId, List<PlzBereich> bereiche);

    boolean hatUeberschneidendePlzBereiche(UUID regionId, PlzBereich bereich);

    void schalteStufeEin(UUID regionId, Stufe stufe, Instant zeitpunkt, UUID benutzerId);

    void schalteStufeAus(UUID regionId, Stufe stufe);
}
