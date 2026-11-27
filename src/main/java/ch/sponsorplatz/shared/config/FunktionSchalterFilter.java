package ch.sponsorplatz.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FunktionSchalterFilter extends OncePerRequestFilter {
    private final FunktionSchalter schalter;

    public FunktionSchalterFilter(FunktionSchalter schalter) {
        this.schalter = schalter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (istAbgeschaltet(request.getRequestURI())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }

    boolean istAbgeschaltet(String pfad) {
        if (pfad.startsWith("/payment/") || pfad.equals("/payment")) {
            return !schalter.zahlung();
        }
        if (pfad.startsWith("/admin/payment-einstellungen")) {
            return !schalter.zahlung();
        }
        if (pfad.equals("/vertraege-rechnungen")) {
            return !schalter.vertrag() || !schalter.rechnung();
        }
        if (pfad.startsWith("/admin/aufgaben-definitionen") || pfad.equals("/aufgaben")
                || pfad.startsWith("/aufgaben/")) {
            return !schalter.aufgaben();
        }
        if (pfad.equals("/matching") || pfad.startsWith("/matching/")) {
            return !schalter.matching();
        }
        if (pfad.startsWith("/organisationen/")) {
            if (enthaeltPfadSegment(pfad, "vertraege") || pfad.contains("/vertrag/")) {
                if (!schalter.vertrag()) {
                    return true;
                }
            }
            if (enthaeltPfadSegment(pfad, "rechnungen") || pfad.contains("/rechnung/")) {
                if (!schalter.rechnung()) {
                    return true;
                }
            }
            if (pfad.contains("/checkout")) {
                return !schalter.zahlung();
            }
        }
        return false;
    }

    private boolean enthaeltPfadSegment(String pfad, String segment) {
        return pfad.matches(".*/" + segment + "(/.*)?");
    }
}
