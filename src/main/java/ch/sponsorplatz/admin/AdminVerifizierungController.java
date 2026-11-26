package ch.sponsorplatz.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ch.sponsorplatz.audit.AuditAktion;
import ch.sponsorplatz.audit.AuditService;
import ch.sponsorplatz.benutzer.AppUserService;
import ch.sponsorplatz.organisation.Organisation;
import ch.sponsorplatz.organisation.OrganisationService;
import ch.sponsorplatz.organisation.OrganisationView;
import ch.sponsorplatz.organisation.Pruefpunkte;
import ch.sponsorplatz.shared.config.ModelAttributeNames;

/**
 * Admin-Bereich: Verifizierungs-Queue für PENDING-Organisationen.
 * Schutz deklarativ via {@code @PreAuthorize("hasRole('PLATFORM_ADMIN')")} —
 * die Rolle wird vom {@code SponsorplatzUserDetailsService} aus
 * {@code AppUser.platformRolle}
 * als {@code ROLE_PLATFORM_ADMIN}-GrantedAuthority gesetzt; kein DB-Roundtrip
 * pro Request.
 */
@Controller
@RequestMapping("/admin/verifizierungen")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class AdminVerifizierungController {

    private final OrganisationService organisationService;
    private final AuditService auditService;
    private final AppUserService appUserService;

    public AdminVerifizierungController(OrganisationService organisationService,
                                        AuditService auditService,
                                        AppUserService appUserService) {
        this.organisationService = organisationService;
        this.auditService = auditService;
        this.appUserService = appUserService;
    }

    @GetMapping
    public String liste(Model model) {
        List<Organisation> pendingOrgs = organisationService.findePending();
        model.addAttribute(ModelAttributeNames.AKTIVE_SEITE, "admin");
        model.addAttribute("pendingOrgs", OrganisationView.von(pendingOrgs));
        return "admin/verifizierungen";
    }

    @PostMapping("/{id}/entscheid")
    public String entscheide(@PathVariable UUID id,
                             @RequestParam String entscheidung,
                             @RequestParam Boolean sportOderGesundheit,
                             @RequestParam Boolean bezugZurSchweiz,
                             @RequestParam Boolean angabenPlausibel,
                             @RequestParam(required = false) String grund,
                             Authentication auth) {
        UUID benutzerId = appUserService.findeOptionalIdNachEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Angemeldete Person nicht gefunden."));
        Pruefpunkte pruefpunkte = new Pruefpunkte(
                sportOderGesundheit, bezugZurSchweiz, angabenPlausibel);
        Organisation org;
        String aktion;
        if ("VERIFIZIEREN".equals(entscheidung)) {
            org = organisationService.verifiziere(id, pruefpunkte);
            aktion = AuditAktion.VERIFIZIERT;
        } else if ("ABLEHNEN".equals(entscheidung)) {
            org = organisationService.lehnePruefungAb(id, grund, pruefpunkte);
            aktion = AuditAktion.PRUEFUNG_ABGELEHNT;
        } else {
            throw new IllegalArgumentException("Ungültiger Prüfentscheid.");
        }
        auditService.protokolliereMitBenutzer(aktion, "ORGANISATION", benutzerId, null,
                id, "Organisation", pruefpunkte.protokollText());
        return "redirect:/admin/verifizierungen";
    }
}
