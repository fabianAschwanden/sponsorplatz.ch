package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.shared.config.BetriebsUmgebung;
import ch.sponsorplatz.shared.config.Formular;
import ch.sponsorplatz.shared.config.FormularDrosselungService;
import ch.sponsorplatz.shared.mail.MailVersand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("prod")
@Import(PilotAnfragePostgresIT.TestClockConfig.class)
class PilotAnfragePostgresIT {

    private static final Instant START = Instant.parse("2026-11-27T05:00:00Z");
    private static final String COMMIT_CONSTRAINT = "pilot_anfrage_commit_test_fk";
    private static final String COMMIT_MARKER_TABLE = "pilot_anfrage_commit_marker";

    static {
        BetriebsUmgebung.bereit();
    }

    @DynamicPropertySource
    static void betriebswerte(DynamicPropertyRegistry registry) {
        BetriebsUmgebung.eintragen(registry, "pilot_anfrage_postgres");
    }

    @Autowired private PilotAnfrageService pilotAnfrageService;
    @Autowired private FormularDrosselungService drosselung;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private MutableClock clock;
    @MockitoBean private MailVersand mailVersand;

    @BeforeEach
    void setzeDatenbankUndUhrZurueck() {
        jdbc.update("delete from pilot_anfrage");
        jdbc.update("delete from einwilligung");
        jdbc.update("delete from formular_drosselung");
        clock.setInstant(START);
        reset(mailVersand);
    }

    @Test
    void speichertAktualisiertInnerhalbVonZehnMinutenUndLegtDanachEineNeueAnfrageAn() {
        assertThat(pilotAnfrageService.speichere(
                eingabe("pilot-postgres@example.test", "Beispielverein"), "198.51.100.10"))
                .isTrue();
        UUID ersteId = jdbc.queryForObject(
                "select id from pilot_anfrage where email = ?", UUID.class, "pilot-postgres@example.test");

        clock.setInstant(START.plusSeconds(9 * 60));
        assertThat(pilotAnfrageService.speichere(eingabe("PILOT-POSTGRES@example.test", "Beispielverein"),
                "198.51.100.10")).isTrue();
        UUID aktualisierteId = jdbc.queryForObject(
                "select id from pilot_anfrage where email = ?", UUID.class, "pilot-postgres@example.test");

        clock.setInstant(START.plusSeconds(20 * 60));
        assertThat(pilotAnfrageService.speichere(eingabe("pilot-postgres@example.test", "Beispielverein"),
                "198.51.100.10")).isTrue();

        assertThat(aktualisierteId).isEqualTo(ersteId);
        assertThat(jdbc.queryForObject(
                "select count(*) from pilot_anfrage where email = ?", Integer.class, "pilot-postgres@example.test"))
                .isEqualTo(2);
        assertThat(jdbc.queryForObject(
                "select count(*) from einwilligung e join pilot_anfrage p on e.bezug_id = p.id "
                        + "where e.bezug_art = 'PILOT_ANFRAGE' and p.email = ?",
                Integer.class, "pilot-postgres@example.test"))
                .isEqualTo(3);
    }

    @Test
    void erlaubtZehnEingabenJeAdresseUndSechzigJeFormular() {
        for (int nummer = 0; nummer < 10; nummer++) {
            assertThat(drosselung.erfasseEingabe(Formular.PILOT, "203.0.113.8")).isTrue();
        }
        assertThat(drosselung.erfasseEingabe(Formular.PILOT, "203.0.113.8")).isFalse();

        jdbc.update("delete from formular_drosselung");
        for (int nummer = 1; nummer <= 60; nummer++) {
            assertThat(drosselung.erfasseEingabe(Formular.PILOT, "198.51.100." + nummer)).isTrue();
        }
        assertThat(drosselung.erfasseEingabe(Formular.PILOT, "198.51.100.61")).isFalse();
    }

    @Test
    void fehlgeschlagenerCommitVersendetKeineBenachrichtigung() {
        jdbc.execute("drop table if exists " + COMMIT_MARKER_TABLE);
        jdbc.execute("create table " + COMMIT_MARKER_TABLE + " (id UUID primary key)");
        jdbc.execute("alter table pilot_anfrage add constraint " + COMMIT_CONSTRAINT
                + " foreign key (id) references " + COMMIT_MARKER_TABLE
                + " (id) deferrable initially deferred not valid");
        try {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);

            assertThatThrownBy(() -> transaction.executeWithoutResult(status ->
                    assertThat(pilotAnfrageService.speichere(
                            eingabe("commit-fehler@example.test", "Commit-Fehler"), "198.51.100.20")).isTrue()))
                    .isInstanceOf(RuntimeException.class);

            verifyNoInteractions(mailVersand);
            assertThat(jdbc.queryForObject(
                    "select count(*) from pilot_anfrage where email = ?", Integer.class, "commit-fehler@example.test"))
                    .isZero();
        } finally {
            jdbc.execute("alter table pilot_anfrage drop constraint if exists " + COMMIT_CONSTRAINT);
            jdbc.execute("drop table if exists " + COMMIT_MARKER_TABLE);
        }
    }

    private PilotAnfrageEingabe eingabe(String email, String organisationsName) {
        return new PilotAnfrageEingabe(
                PilotAnfrageRolle.VEREIN, organisationsName, PilotAnfrageBereich.BALLSPORT, null,
                "8000", "Beispielort", "Beispiel", "Kontakt", email,
                "", "", "", "", "", "", "", "", "", "", "", false);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestClockConfig {

        @Bean
        @Primary
        MutableClock pilotAnfrageTestClock() {
            return new MutableClock(START);
        }
    }

    static class MutableClock extends Clock {

        private final AtomicReference<Instant> instant;
        private final ZoneId zone;

        MutableClock(Instant initial) {
            this(new AtomicReference<>(initial), ZoneOffset.UTC);
        }

        private MutableClock(AtomicReference<Instant> instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void setInstant(Instant neuerZeitpunkt) {
            instant.set(neuerZeitpunkt);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId neueZone) {
            return new MutableClock(instant, neueZone);
        }

        @Override
        public Instant instant() {
            return instant.get();
        }
    }
}
