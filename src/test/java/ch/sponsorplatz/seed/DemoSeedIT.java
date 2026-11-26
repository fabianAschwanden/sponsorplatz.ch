package ch.sponsorplatz.seed;

import ch.sponsorplatz.organisation.OrgTyp;
import ch.sponsorplatz.organisation.Organisation;
import ch.sponsorplatz.organisation.OrganisationRepository;
import ch.sponsorplatz.shared.config.BetriebsUmgebung;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Der Demo-Seed gegen eine echte PostgreSQL-Datenbank mit allen Migrationen.
 *
 * <p>{@link DemoSeedRunnerTest} prüft den Seed nur gegen Mocks. Die Regeln der
 * Datenbank (etwa {@code chk_branche_pro_typ}) greifen dort nicht; so blieb
 * unbemerkt, dass der Seed an ihnen scheiterte und die Anwendung mit den
 * Profilen {@code prod} und {@code demo} nicht startete.
 */
@SpringBootTest
@ActiveProfiles({"prod", "demo"})
class DemoSeedIT {

    static {
        BetriebsUmgebung.bereit();
    }

    @DynamicPropertySource
    static void betriebswerte(DynamicPropertyRegistry registry) {
        BetriebsUmgebung.eintragen(registry, "demo_seed");
    }

    @Autowired
    private OrganisationRepository organisationen;

    @Autowired
    private DemoSeedRunner seed;

    @Test
    @DisplayName("Der Seed läuft beim Start durch: Vereine und Sponsoren sind in der Datenbank")
    void seedLaeuftDurch() {
        List<Organisation> alle = organisationen.findAll();

        assertThat(alle).filteredOn(o -> o.getTyp() == OrgTyp.VEREIN).hasSizeGreaterThanOrEqualTo(5);
        assertThat(alle).filteredOn(o -> o.getTyp() == OrgTyp.UNTERNEHMEN)
                .hasSizeGreaterThanOrEqualTo(3)
                .allSatisfy(o -> assertThat(o.getSponsorBranche()).isNotNull());
    }

    @Test
    @DisplayName("Ein zweiter Lauf legt nichts doppelt an")
    void seedIstWiederholbar() {
        long vorher = organisationen.count();

        seed.run();

        assertThat(organisationen.count()).isEqualTo(vorher);
    }
}
