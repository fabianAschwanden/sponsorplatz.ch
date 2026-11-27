package ch.sponsorplatz.shared.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface FormularDrosselungRepository extends JpaRepository<FormularDrosselungEintrag, UUID> {

    long countByFormularAndPruefwertAndErstelltAmAfter(Formular formular, String pruefwert, Instant zeitpunkt);

    long countByFormularAndErstelltAmAfter(Formular formular, Instant zeitpunkt);

    int deleteByErstelltAmBefore(Instant zeitpunkt);
}
