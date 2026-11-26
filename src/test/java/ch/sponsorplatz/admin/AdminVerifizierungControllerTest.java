package ch.sponsorplatz.admin;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ch.sponsorplatz.benutzer.SponsorplatzUserDetailsService;
import ch.sponsorplatz.benutzer.AppUserService;
import ch.sponsorplatz.audit.AuditService;
import ch.sponsorplatz.organisation.OrgStatus;
import ch.sponsorplatz.organisation.OrgTyp;
import ch.sponsorplatz.organisation.Organisation;
import ch.sponsorplatz.organisation.OrganisationService;
import ch.sponsorplatz.shared.config.SecurityConfig;

@WebMvcTest(controllers = AdminVerifizierungController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
class AdminVerifizierungControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SponsorplatzUserDetailsService userDetailsService;

    @MockitoBean
    private OrganisationService organisationService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private AppUserService appUserService;

    /** ADM-01: GET /admin/verifizierungen ohne PLATFORM_ADMIN-Rolle → 403. */
    @Test
    @WithMockUser(username = "user@example.ch")
    void listeOhneAdminGibt403() throws Exception {
        mockMvc.perform(get("/admin/verifizierungen"))
                .andExpect(status().isForbidden());
    }

    /** ADM-02: GET /admin/verifizierungen als PLATFORM_ADMIN → 200 + Liste. */
    @Test
    @WithMockUser(username = "admin@sponsorplatz.ch", roles = "PLATFORM_ADMIN")
    void listeAlsAdminGibt200() throws Exception {
        Organisation pending = new Organisation();
        pending.setId(UUID.randomUUID());
        pending.setName("Pending Verein");
        pending.setSlug("pending-verein");
        pending.setTyp(OrgTyp.VEREIN);
        pending.setStatus(OrgStatus.PENDING);
        when(organisationService.findePending()).thenReturn(List.of(pending));

        mockMvc.perform(get("/admin/verifizierungen"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/verifizierungen"))
                .andExpect(model().attributeExists("pendingOrgs"));
    }

    /** ADM-03: POST verifizieren als Admin → delegiert an Service. */
    @Test
    @WithMockUser(username = "admin@sponsorplatz.ch", roles = "PLATFORM_ADMIN")
    void verifizierenDelegiertAnService() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID benutzerId = UUID.randomUUID();
        when(appUserService.findeOptionalIdNachEmail("admin@sponsorplatz.ch"))
                .thenReturn(java.util.Optional.of(benutzerId));

        mockMvc.perform(post("/admin/verifizierungen/{id}/entscheid", orgId)
                        .param("entscheidung", "VERIFIZIEREN")
                        .param("sportOderGesundheit", "true")
                        .param("bezugZurSchweiz", "true")
                        .param("angabenPlausibel", "false")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/verifizierungen"));

        verify(organisationService).verifiziere(
                orgId, new ch.sponsorplatz.organisation.Pruefpunkte(true, true, false));
        verify(auditService).protokolliereMitBenutzer(
                ch.sponsorplatz.audit.AuditAktion.VERIFIZIERT, "ORGANISATION", benutzerId, null,
                orgId, "Organisation", "Sport/Gesundheit: Ja; Bezug zur Schweiz: Ja; Angaben plausibel: Nein");
    }

    /** ADM-04: POST ablehnen als Admin → delegiert an Service. */
    @Test
    @WithMockUser(username = "admin@sponsorplatz.ch", roles = "PLATFORM_ADMIN")
    void ablehnenDelegiertAnService() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(appUserService.findeOptionalIdNachEmail("admin@sponsorplatz.ch"))
                .thenReturn(java.util.Optional.of(UUID.randomUUID()));

        mockMvc.perform(post("/admin/verifizierungen/{id}/entscheid", orgId)
                        .param("entscheidung", "ABLEHNEN")
                        .param("sportOderGesundheit", "false")
                        .param("bezugZurSchweiz", "true")
                        .param("angabenPlausibel", "false")
                        .param("grund", "Angaben nicht plausibel")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/verifizierungen"));

        verify(organisationService).lehnePruefungAb(
                orgId, "Angaben nicht plausibel",
                new ch.sponsorplatz.organisation.Pruefpunkte(false, true, false));
    }

    /** ADM-09: POST verifizieren ohne PLATFORM_ADMIN-Rolle → 403. */
    @Test
    @WithMockUser(username = "user@example.ch")
    void verifizierenOhneAdminGibt403() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(post("/admin/verifizierungen/{id}/entscheid", orgId)
                        .param("entscheidung", "VERIFIZIEREN")
                        .param("sportOderGesundheit", "true")
                        .param("bezugZurSchweiz", "true")
                        .param("angabenPlausibel", "true")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
