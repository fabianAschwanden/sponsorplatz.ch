package ch.sponsorplatz.region.adapter.out.persistenz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionJpaRepository extends JpaRepository<RegionEntity, UUID> {
    boolean existsByCode(String code);
}
