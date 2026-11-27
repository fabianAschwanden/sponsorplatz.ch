package ch.sponsorplatz.pilotanfrage.adapter.in.web;

import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageService;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotEinwilligungstext;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
@RequestMapping("/pilot")
public class PilotAnfrageController {

    private static final String FORMULAR = "pilotanfrage/formular";
    private final PilotAnfrageService pilotAnfrageService;

    public PilotAnfrageController(PilotAnfrageService pilotAnfrageService) {
        this.pilotAnfrageService = pilotAnfrageService;
    }

    @GetMapping
    public String formular(
            @RequestParam(required = false) String rolle,
            @RequestParam(required = false) String name,
            Model model) {
        erzwingeDeutsch();
        if (!model.containsAttribute("pilotForm")) {
            PilotAnfrageFormDto form = new PilotAnfrageFormDto();
            if ("sponsor".equalsIgnoreCase(rolle)) {
                form.setRolle(PilotAnfrageRolle.SPONSOR);
            }
            form.setOrganisationsName(name == null ? null : name.substring(0, Math.min(name.length(), 150)));
            model.addAttribute("pilotForm", form);
        }
        fuegeFormularModelHinzu((PilotAnfrageFormDto) model.getAttribute("pilotForm"), model);
        return FORMULAR;
    }

    @PostMapping
    public String absenden(
            @Valid @ModelAttribute("pilotForm") PilotAnfrageFormDto form,
            BindingResult binding,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response) {
        erzwingeDeutsch();
        if (binding.hasErrors()) {
            return formularMitEinwilligungstexten(form, model);
        }
        if (!pilotAnfrageService.speichere(form.zuEingabe(), request.getRemoteAddr())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return "shared/formular-drosselung";
        }
        return "redirect:/pilot/bestaetigung";
    }

    private void fuegeFormularModelHinzu(PilotAnfrageFormDto form, Model model) {
        if (form.getRolle() == null) {
            form.setRolle(PilotAnfrageRolle.VEREIN);
        }
        model.addAttribute("einwilligungBestaetigung", PilotEinwilligungstext.bestaetigung(form.getRolle()));
        model.addAttribute("einwilligungHinweis", PilotEinwilligungstext.hinweis());
        model.addAttribute("bereiche", PilotAnfrageBereich.values());
        model.addAttribute("sponsorArten", PilotSponsorArt.values());
    }

    private String formularMitEinwilligungstexten(PilotAnfrageFormDto form, Model model) {
        fuegeFormularModelHinzu(form, model);
        return FORMULAR;
    }

    @GetMapping("/bestaetigung")
    public String bestaetigung() {
        erzwingeDeutsch();
        return "pilotanfrage/bestaetigung";
    }

    private void erzwingeDeutsch() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("de-CH"));
    }
}
