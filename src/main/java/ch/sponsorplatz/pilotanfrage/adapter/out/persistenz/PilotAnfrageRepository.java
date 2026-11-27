package ch.sponsorplatz.pilotanfrage.adapter.out.persistenz;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PilotAnfrageRepository extends JpaRepository<PilotAnfrageEntity, Long> {

    Optional<PilotAnfrageEntity> findFirstByEmailIgnoreCaseAndOrganisationsNameIgnoreCaseAndRolleAndAktualisiertAmAfterOrderByAktualisiertAmDesc(
            String email, String organisationsName, PilotAnfrageRolle rolle, Instant seit);
}
