package ch.sponsorplatz.admin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.i18n.LocaleContextHolder;

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
    private final AppUserService appUserService;
    private final MessageSource messageSource;

    public AdminVerifizierungController(OrganisationService organisationService,
                                        AppUserService appUserService,
                                        MessageSource messageSource) {
        this.organisationService = organisationService;
        this.appUserService = appUserService;
        this.messageSource = messageSource;
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
                             Authentication auth,
                             RedirectAttributes redirect) {
        if (!"VERIFIZIEREN".equals(entscheidung) && !"ABLEHNEN".equals(entscheidung)) {
            throw new IllegalArgumentException("Ungültiger Prüfentscheid.");
        }
        UUID benutzerId = appUserService.findeOptionalIdNachEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Angemeldete Person nicht gefunden."));
        Pruefpunkte pruefpunkte = new Pruefpunkte(
                sportOderGesundheit, bezugZurSchweiz, angabenPlausibel);
        if ("VERIFIZIEREN".equals(entscheidung)) {
            if (!pruefpunkte.sindAlleBestaetigt()) {
                redirect.addFlashAttribute(ModelAttributeNames.FEHLERMELDUNG,
                        meldung("admin.verifizierungen.fehler.freigabe"));
                return "redirect:/admin/verifizierungen";
            }
        }
        try {
            if ("VERIFIZIEREN".equals(entscheidung)) {
                organisationService.verifiziere(id, pruefpunkte, benutzerId);
            } else {
                organisationService.lehnePruefungAb(id, grund, pruefpunkte, benutzerId);
            }
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute(ModelAttributeNames.FEHLERMELDUNG,
                    meldung("admin.verifizierungen.fehler.ablehnungsgrund"));
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute(ModelAttributeNames.FEHLERMELDUNG,
                    meldung("admin.verifizierungen.fehler.entscheid"));
        }
        return "redirect:/admin/verifizierungen";
    }

    private String meldung(String schluessel) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(schluessel, null, locale);
    }
}
