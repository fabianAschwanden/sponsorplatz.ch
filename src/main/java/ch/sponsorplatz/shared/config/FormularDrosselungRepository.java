package ch.sponsorplatz.shared.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface FormularDrosselungRepository extends JpaRepository<FormularDrosselungEintrag, Long> {

    long countByFormularAndPruefwertAndErstelltAmAfter(String formular, String pruefwert, Instant zeitpunkt);

    long countByFormularAndErstelltAmAfter(String formular, Instant zeitpunkt);

    int deleteByErstelltAmBefore(Instant zeitpunkt);
}
