package ch.sponsorplatz.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FunktionSchalterFilterTest {
    private final FunktionSchalter aus = new FunktionSchalter(false, false, false, false, false);

    @Test
    void zahlungsroutenFragenBeiAbgeschalteterZahlungKeinenBetragAb() throws Exception {
        List<String> zahlungsRouten = List.of(
                "/payment/webhook/provider",
                "/payment/erfolg",
                "/payment/abgebrochen",
                "/payment/fehler",
                "/admin/payment-einstellungen",
                "/organisationen/beispielverein/rechnungen/00000000-0000-4000-8000-000000000001/checkout",
                "/organisationen/beispielverein/vertraege/00000000-0000-4000-8000-000000000001/rechnung/erstellen");

        for (String pfad : zahlungsRouten) {
            assertThat(status(new FunktionSchalterFilter(aus), pfad))
                    .as(pfad)
                    .isEqualTo(404);
        }
    }

    @Test
    void vertraegeRechnungenUndAufgabenSindBeiAbgeschaltetenSchalternNichtErreichbar() throws Exception {
        List<String> pfade = List.of(
                "/vertraege-rechnungen",
                "/organisationen/beispielverein/vertraege/abc",
                "/organisationen/beispielverein/rechnungen/abc",
                "/aufgaben",
                "/admin/aufgaben-definitionen");

        for (String pfad : pfade) {
            assertThat(status(new FunktionSchalterFilter(aus), pfad))
                    .as(pfad)
                    .isEqualTo(404);
        }
    }

    @Test
    void aktivierteRouteWirdAnDieAnwendungWeitergegeben() throws Exception {
        FunktionSchalterFilter filter = new FunktionSchalterFilter(
                new FunktionSchalter(true, true, true, true, true));
        MockFilterChain chain = new MockFilterChain();

        int status = status(filter, "/payment/webhook/provider", chain);

        assertThat(status).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
    }

    private int status(FunktionSchalterFilter filter, String pfad) throws Exception {
        return status(filter, pfad, new MockFilterChain());
    }

    private int status(FunktionSchalterFilter filter, String pfad, MockFilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", pfad);
        request.setRequestURI(pfad);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response.getStatus();
    }
}
