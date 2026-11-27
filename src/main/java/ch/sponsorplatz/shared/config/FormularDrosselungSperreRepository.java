package ch.sponsorplatz.shared.config;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormularDrosselungSperreRepository extends JpaRepository<FormularDrosselungSperre, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sperre from FormularDrosselungSperre sperre where sperre.formular = :formular")
    Optional<FormularDrosselungSperre> findeUndSperre(@Param("formular") String formular);
}
