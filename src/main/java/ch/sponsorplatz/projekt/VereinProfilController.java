package ch.sponsorplatz.projekt;

import ch.sponsorplatz.organisation.OrganisationService;
import ch.sponsorplatz.organisation.AccessControl;
import ch.sponsorplatz.organisation.OrgTyp;
import ch.sponsorplatz.organisation.OrganisationView;
import ch.sponsorplatz.shared.config.ModelAttributeNames;
import ch.sponsorplatz.shared.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Öffentliche Vereinsprofile — ohne Login erreichbar.
 */
@Controller
@RequestMapping("/vereine")
public class VereinProfilController {

    private final OrganisationService orgService;
    private final ProjektService projektService;
    private final AccessControl accessControl;

    public VereinProfilController(OrganisationService orgService, ProjektService projektService,
                                  AccessControl accessControl) {
        this.orgService = orgService;
        this.projektService = projektService;
        this.accessControl = accessControl;
    }

    @GetMapping("/{slug}")
    public String profil(@PathVariable String slug, Authentication auth, Model model) {
        if (!accessControl.kannOrgSehenNachSlug(slug, auth)) {
            throw new NotFoundException("Verein nicht gefunden: " + slug);
        }
        OrganisationView org = orgService.findeViewNachSlug(slug)
                .orElseThrow(() -> new NotFoundException("Verein nicht gefunden: " + slug));
        if (org.typ() != OrgTyp.VEREIN) {
            throw new NotFoundException("Verein nicht gefunden: " + slug);
        }

        model.addAttribute(ModelAttributeNames.AKTIVE_SEITE, "vereine");
        model.addAttribute("org", org);
        model.addAttribute("projekte", projektService.findeOeffentlicheViewsNachOrg(org.id()));
        return "projekt/verein-profil";
    }
}
