package ch.sponsorplatz.shared.config;

import jakarta.servlet.http.HttpServletResponse;
import ch.sponsorplatz.admin.AdminPaymentEinstellungenController;
import ch.sponsorplatz.anfrage.RechnungController;
import ch.sponsorplatz.anfrage.VertragController;
import ch.sponsorplatz.anfrage.VertraegeRechnungenController;
import ch.sponsorplatz.anfrage.payment.PaymentRedirectController;
import ch.sponsorplatz.anfrage.payment.PaymentWebhookController;
import ch.sponsorplatz.aufgabe.AdminAufgabenDefinitionController;
import ch.sponsorplatz.aufgabe.AufgabenController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FunktionSchalterInterceptorTest {
    private static final List<Class<?>> CONTROLLER_KLASSEN = List.of(
            AdminPaymentEinstellungenController.class,
            PaymentRedirectController.class,
            PaymentWebhookController.class,
            VertragController.class,
            RechnungController.class,
            VertraegeRechnungenController.class,
            AufgabenController.class,
            AdminAufgabenDefinitionController.class);

    @Test
    void jederControllerDerAbgeschaltetenBereicheIstAnnotiert() {
        assertThat(CONTROLLER_KLASSEN)
                .allSatisfy(controller -> assertThat(
                        AnnotatedElementUtils.findMergedAnnotation(controller, Funktion.class))
                        .as(controller.getSimpleName())
                        .isNotNull());
    }

    @Test
    void bereicheAufDerVertraegeUndRechnungenUebersichtSindBeideAngegeben() {
        Funktion funktion = AnnotatedElementUtils.findMergedAnnotation(
                VertraegeRechnungenController.class, Funktion.class);

        assertThat(Set.of(funktion.value()))
                .containsExactlyInAnyOrder(
                        FunktionSchalter.Bereich.VERTRAG,
                        FunktionSchalter.Bereich.RECHNUNG);
    }

    @Test
    void checkoutHatEigenenZahlungsschalter() throws NoSuchMethodException {
        Funktion funktion = AnnotatedElementUtils.findMergedAnnotation(
                RechnungController.class.getMethod("checkout", String.class, java.util.UUID.class,
                        org.springframework.security.core.Authentication.class,
                        org.springframework.web.servlet.mvc.support.RedirectAttributes.class),
                Funktion.class);

        assertThat(funktion).isNotNull();
        assertThat(funktion.value()).containsExactly(
                FunktionSchalter.Bereich.ZAHLUNG,
                FunktionSchalter.Bereich.RECHNUNG);
    }

    @Test
    void rechnungserstellungHatVertragsUndRechnungsschalter() throws NoSuchMethodException {
        Funktion funktion = AnnotatedElementUtils.findMergedAnnotation(
                RechnungController.class.getMethod("erstellen", String.class, java.util.UUID.class,
                        org.springframework.security.core.Authentication.class,
                        org.springframework.web.servlet.mvc.support.RedirectAttributes.class),
                Funktion.class);

        assertThat(funktion).isNotNull();
        assertThat(funktion.value()).containsExactly(
                FunktionSchalter.Bereich.VERTRAG,
                FunktionSchalter.Bereich.RECHNUNG);
    }

    @Test
    void abweichendeRequestUrisAendernDenSchalterDesErkanntenControllersNicht() throws Exception {
        FunktionSchalterInterceptor interceptor =
                new FunktionSchalterInterceptor(new FunktionSchalter(false, false, false, false, false));
        TestHandler testHandler = new TestHandler();

        assertAbgeschaltet(interceptor, testHandler, "zahlung",
                "/p%61yment/erfolg", "//payment/erfolg", "/payment/erfolg/", "/PAYMENT/erfolg");
        assertAbgeschaltet(interceptor, testHandler, "vertrag",
                "/organisationen/fc-test/ver%74raege/1", "//payment/vertraege/1",
                "/organisationen/fc-test/vertraege/1/", "/ORGANISATIONEN/fc-test/vertraege/1");
        assertAbgeschaltet(interceptor, testHandler, "rechnung",
                "/organisationen/fc-test/rechn%75ngen/1", "//payment/rechnungen/1",
                "/organisationen/fc-test/rechnungen/1/", "/ORGANISATIONEN/fc-test/rechnungen/1");
        assertAbgeschaltet(interceptor, testHandler, "aufgaben",
                "/auf%67aben", "//payment/aufgaben", "/aufgaben/", "/AUFGABEN");
        assertAbgeschaltet(interceptor, testHandler, "matching",
                "/mat%63hing", "//payment/matching", "/matching/", "/MATCHING");
    }

    private void assertAbgeschaltet(
            FunktionSchalterInterceptor interceptor,
            TestHandler testHandler,
            String methodName,
            String... paths) throws Exception {
        Method method = TestHandler.class.getMethod(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(testHandler, method);

        for (String path : paths) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
            request.setRequestURI(path);
            MockHttpServletResponse response = new MockHttpServletResponse();

            assertThat(interceptor.preHandle(request, response, handlerMethod))
                    .as(path)
                    .isFalse();
            assertThat(response.getStatus())
                    .as(path)
                    .isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    static class TestHandler {
        @Funktion(FunktionSchalter.Bereich.ZAHLUNG)
        public void zahlung() {
        }

        @Funktion(FunktionSchalter.Bereich.VERTRAG)
        public void vertrag() {
        }

        @Funktion(FunktionSchalter.Bereich.RECHNUNG)
        public void rechnung() {
        }

        @Funktion(FunktionSchalter.Bereich.AUFGABEN)
        public void aufgaben() {
        }

        @Funktion(FunktionSchalter.Bereich.MATCHING)
        public void matching() {
        }
    }
}
