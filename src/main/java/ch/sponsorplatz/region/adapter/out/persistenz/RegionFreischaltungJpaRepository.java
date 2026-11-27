package ch.sponsorplatz.region.adapter.out.persistenz;

import ch.sponsorplatz.region.domain.Stufe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegionFreischaltungJpaRepository
        extends JpaRepository<RegionFreischaltungEntity, RegionFreischaltungEntity.Schluessel> {
    List<RegionFreischaltungEntity> findAllByRegionId(UUID regionId);

    void deleteByRegionIdAndStufe(UUID regionId, Stufe stufe);
}
