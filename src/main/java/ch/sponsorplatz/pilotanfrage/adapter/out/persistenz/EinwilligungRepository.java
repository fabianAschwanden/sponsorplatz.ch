package ch.sponsorplatz.pilotanfrage.adapter.out.persistenz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EinwilligungRepository extends JpaRepository<EinwilligungEntity, UUID> {
}
