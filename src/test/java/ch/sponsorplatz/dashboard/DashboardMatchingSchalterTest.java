package ch.sponsorplatz.dashboard;

import ch.sponsorplatz.aufgabe.AufgabenService;
import ch.sponsorplatz.benutzer.AppUserService;
import ch.sponsorplatz.benutzer.AppUserService.OnboardingSnapshot;
import ch.sponsorplatz.benutzer.SponsorplatzUserDetailsService;
import ch.sponsorplatz.einladung.EinladungsService;
import ch.sponsorplatz.organisation.MitgliedschaftService;
import ch.sponsorplatz.projekt.MatchingService;
import ch.sponsorplatz.projekt.ProjektService;
import ch.sponsorplatz.shared.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
@TestPropertySource(properties = "sponsorplatz.funktion.matching=false")
class DashboardMatchingSchalterTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SponsorplatzUserDetailsService userDetailsService;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private DashboardChartService dashboardChartService;

    @MockitoBean
    private MatchingService matchingService;

    @MockitoBean
    private ProjektService projektService;

    @MockitoBean
    private AppUserService appUserService;

    @MockitoBean
    private MitgliedschaftService mitgliedschaftService;

    @MockitoBean
    private EinladungsService einladungsService;

    @MockitoBean
    private AufgabenService aufgabenService;

    @Test
    @org.springframework.security.test.context.support.WithMockUser("matching-off@test.ch")
    void matchingAusLaesstServiceUnaufgerufenUndEmpfehlungsblockWeg() throws Exception {
        UUID userId = UUID.randomUUID();
        when(appUserService.findeOnboardingSnapshotNachEmail("matching-off@test.ch"))
                .thenReturn(Optional.of(new OnboardingSnapshot(userId, false, true)));
        when(mitgliedschaftService.findeOrgIdsVonUser(userId)).thenReturn(List.of());
        when(dashboardService.ladeDashboardDaten(anyString())).thenReturn(DashboardDaten.leer());
        when(dashboardChartService.ladeCharts(anyString(), anyString()))
                .thenReturn(new DashboardChartDaten(
                        DashboardChartDaten.entwicklungAus(List.of(), List.of()),
                        DashboardChartDaten.finanzierungAus(
                                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO)));
        when(einladungsService.findeOffeneFuerEmail(anyString())).thenReturn(List.of());
        when(aufgabenService.meineOffenenAlsViews(eq("matching-off@test.ch"), eq(6))).thenReturn(List.of());

        var result = mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Empfohlene Projekte"))))
                .andReturn();

        assertThat(result.getModelAndView().getModel().get("empfehlungen")).isEqualTo(List.of());
        verifyNoInteractions(matchingService);
    }
}
