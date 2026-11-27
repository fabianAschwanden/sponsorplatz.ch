package ch.sponsorplatz.region.application;

import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.benutzer.PlatformRolle;
import ch.sponsorplatz.region.domain.Stufe;
import ch.sponsorplatz.shared.config.BetriebsUmgebung;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@TestPropertySource(properties = {
        "sponsorplatz.funktion.zahlung=false",
        "sponsorplatz.funktion.vertrag=false",
        "sponsorplatz.funktion.rechnung=false",
        "sponsorplatz.funktion.aufgaben=false",
        "sponsorplatz.funktion.matching=false"
})
class RegionPostgresIT {
    private static final String UI_ADMIN_EMAIL = "region-ui-admin@example.test";

    static {
        BetriebsUmgebung.bereit();
    }

    @DynamicPropertySource
    static void betriebswerte(DynamicPropertyRegistry registry) {
        BetriebsUmgebung.eintragen(registry, "region_test");
    }

    @Autowired
    private RegionVerwaltung verwaltung;
    @Autowired
    private StufenPruefung stufenPruefung;
    @Autowired
    private AppUserRepository benutzer;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationContext applicationContext;

    private UUID adminId;

    @BeforeEach
    void legeAdminAn() {
        adminId = benutzer.findByEmail(UI_ADMIN_EMAIL)
                .map(AppUser::getId)
                .orElseGet(() -> {
                    AppUser admin = new AppUser();
                    admin.setEmail(UI_ADMIN_EMAIL);
                    admin.setPasswortHash("test-hash");
                    admin.setAnzeigename("Beispiel Admin");
                    admin.setPlatformRolle(PlatformRolle.PLATFORM_ADMIN);
                    admin.setEmailVerifiziert(true);
                    return benutzer.saveAndFlush(admin).getId();
                });
    }

    @Test
    void pilotRegionSchaltetStufeEinsFuerDenGesamtenVorgegebenenPlzBereichFrei() {
        assertThat(stufenPruefung.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "1000")).isTrue();
        assertThat(stufenPruefung.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "9999")).isTrue();
        assertThat(stufenPruefung.istFreigeschaltet(Stufe.FINDEN_UND_ZUSAGEN, "8000")).isFalse();
        assertThat(stufenPruefung.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "0000")).isFalse();
    }

    @Test
    void speichertRegionAenderungenUndProtokolliertSieInPostgres() {
        String code = "test-" + UUID.randomUUID().toString().substring(0, 8);
        UUID regionId = verwaltung.legeRegionAn(code, "Beispielregion", adminId);
        verwaltung.benenneRegionUm(regionId, "Umbenannte Beispielregion", adminId);
        verwaltung.schalteStufe(regionId, Stufe.WIRKUNG, true, adminId);

        RegionView region = verwaltung.alleRegionen(adminId).stream()
                .filter(eintrag -> eintrag.id().equals(regionId))
                .findFirst()
                .orElseThrow();
        Integer protokolliert = jdbc.queryForObject(
                "select count(*) from audit_log where bereich = 'REGION' and ziel_id = ? and benutzer_id = ?",
                Integer.class, regionId, adminId);

        assertThat(region.name()).isEqualTo("Umbenannte Beispielregion");
        assertThat(region.freigeschalteteStufen()).contains(Stufe.WIRKUNG);
        assertThat(protokolliert).isEqualTo(3);
    }

    @Test
    void lehntUeberschneidendePlzBereicheMitDatenAusPostgresAb() {
        String code = "plz-test-" + UUID.randomUUID().toString().substring(0, 8);
        UUID regionId = verwaltung.legeRegionAn(code, "Beispielregion", adminId);

        assertThatThrownBy(() -> verwaltung.setzePlzBereiche(regionId, "1000-1000", adminId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anderen Region");
    }

    @Test
    void abgeschalteteFunktionsroutenAntwortenVorDerAnmeldungMit404() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/payment/webhook/provider"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/aufgaben"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/vertraege-rechnungen"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = UI_ADMIN_EMAIL, roles = "PLATFORM_ADMIN")
    void regionsverwaltungZeigtKeineLinksZuAbgeschaltetenBereichen() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/admin/regionen"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("/admin/regionen")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
                                "/admin/payment-einstellungen"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
                                "/vertraege-rechnungen"))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(
                                "/admin/aufgaben-definitionen"))));
    }

    @Test
    void aufgabenBackfillIstBeiAbgeschalteterFunktionNichtAktiv() {
        assertThat(applicationContext.containsBean("aufgabenBackfillRunner")).isFalse();
    }
}
