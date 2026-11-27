package ch.sponsorplatz.region.adapter.out.persistenz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegionPlzJpaRepository extends JpaRepository<RegionPlzEntity, UUID> {
    List<RegionPlzEntity> findAllByRegionId(UUID regionId);

    void deleteAllByRegionId(UUID regionId);
}
