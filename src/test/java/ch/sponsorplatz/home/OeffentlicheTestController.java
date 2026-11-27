package ch.sponsorplatz.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OeffentlicheTestController {

    @GetMapping("/__test/oeffentlich")
    public String oeffentlicheTestseite() {
        return "fragments/oeffentlich-test";
    }
}
