package ch.sponsorplatz.region.adapter.in.web;

import ch.sponsorplatz.region.application.RegionBenutzerPort;
import ch.sponsorplatz.region.application.RegionView;
import ch.sponsorplatz.region.application.RegionVerwaltung;
import ch.sponsorplatz.region.application.RegionZugriffVerweigertException;
import ch.sponsorplatz.region.domain.Stufe;
import ch.sponsorplatz.shared.config.ModelAttributeNames;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/regionen")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class RegionenController {
    private final RegionVerwaltung verwaltung;
    private final RegionBenutzerPort benutzer;
    private final MessageSource messageSource;

    public RegionenController(RegionVerwaltung verwaltung, RegionBenutzerPort benutzer,
                              MessageSource messageSource) {
        this.verwaltung = verwaltung;
        this.benutzer = benutzer;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String liste(Authentication authentication, Model model) {
        UUID benutzerId = benutzerId(authentication);
        List<RegionView> regionen = verwaltung.alleRegionen(benutzerId);
        model.addAttribute("regionen", regionen);
        model.addAttribute("stufen", Arrays.asList(Stufe.values()));
        model.addAttribute(ModelAttributeNames.AKTIVE_SEITE, "admin-regionen");
        return "region/regionen";
    }

    @PostMapping
    public String legeAn(@RequestParam String code, @RequestParam String name,
                         Authentication authentication, RedirectAttributes redirect) {
        return aendere(redirect, () -> verwaltung.legeRegionAn(code, name, benutzerId(authentication)));
    }

    @PostMapping("/{id}/name")
    public String benenneUm(@PathVariable UUID id, @RequestParam String name,
                            Authentication authentication, RedirectAttributes redirect) {
        return aendere(redirect, () -> verwaltung.benenneRegionUm(id, name, benutzerId(authentication)));
    }

    @PostMapping("/{id}/plz")
    public String setzePlzBereiche(@PathVariable UUID id, @RequestParam String bereiche,
                                   Authentication authentication, RedirectAttributes redirect) {
        return aendere(redirect, () -> verwaltung.setzePlzBereiche(id, bereiche, benutzerId(authentication)));
    }

    @PostMapping("/{id}/stufen/{nummer}")
    public String schalteStufe(@PathVariable UUID id, @PathVariable int nummer,
                               @RequestParam boolean freigeschaltet, Authentication authentication,
                               RedirectAttributes redirect) {
        return aendere(redirect, () -> verwaltung.schalteStufe(
                id, Stufe.vonNummer(nummer), freigeschaltet, benutzerId(authentication)));
    }

    @ExceptionHandler(RegionZugriffVerweigertException.class)
    public ResponseEntity<Void> keinZugriff() {
        return ResponseEntity.status(403).build();
    }

    private String aendere(RedirectAttributes redirect, Runnable aenderung) {
        try {
            aenderung.run();
            redirect.addFlashAttribute("erfolgsMeldung", meldung("admin.regionen.erfolg"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("fehlermeldung", meldung("admin.regionen.fehler"));
        }
        return "redirect:/admin/regionen";
    }

    private String meldung(String schluessel) {
        return messageSource.getMessage(schluessel, null, LocaleContextHolder.getLocale());
    }

    private UUID benutzerId(Authentication authentication) {
        return benutzer.findeIdNachAnmeldename(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Angemeldete Person nicht gefunden."));
    }
}
