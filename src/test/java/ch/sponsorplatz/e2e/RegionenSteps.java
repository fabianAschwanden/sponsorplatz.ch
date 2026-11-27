package ch.sponsorplatz.e2e;

import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.benutzer.PlatformRolle;
import ch.sponsorplatz.region.application.RegionDaten;
import ch.sponsorplatz.region.application.RegionVerwaltung;
import ch.sponsorplatz.region.application.StufenPruefung;
import ch.sponsorplatz.region.domain.Stufe;
import ch.sponsorplatz.shared.config.FunktionSchalter;
import ch.sponsorplatz.shared.config.FunktionSchalterInterceptor;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RegionenSteps {
    private static final List<String> ZAHLUNGSROUTEN = List.of(
            "/payment/webhook/provider",
            "/payment/erfolg",
            "/payment/abgebrochen",
            "/payment/fehler",
            "/admin/payment-einstellungen",
            "/organisationen/beispielverein/rechnungen/00000000-0000-4000-8000-000000000001/checkout",
            "/organisationen/beispielverein/vertraege/00000000-0000-4000-8000-000000000001/rechnung/erstellen");
    private static final List<String> FUNKTIONSROUTEN = List.of(
            "/payment/webhook/provider",
            "/organisationen/beispielverein/vertraege/abc",
            "/organisationen/beispielverein/rechnungen/abc",
            "/vertraege-rechnungen",
            "/aufgaben",
            "/admin/aufgaben-definitionen",
            "/matching");

    @Autowired
    private RegionVerwaltung verwaltung;
    @Autowired
    private StufenPruefung stufenPruefung;
    @Autowired
    private AppUserRepository benutzer;
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private UUID adminId;
    private UUID regionId;
    private String neuerName;
    private int funktionsStatus;
    private List<Integer> routenStatus;

    @Angenommen("ich bin als Plattform-Admin in der Regionenverwaltung")
    public void binPlattformAdmin() {
        AppUser admin = new AppUser();
        admin.setEmail("region-test-" + UUID.randomUUID() + "@example.test");
        admin.setPasswortHash("test-hash");
        admin.setAnzeigename("Beispiel Admin");
        admin.setPlatformRolle(PlatformRolle.PLATFORM_ADMIN);
        admin.setEmailVerifiziert(true);
        adminId = benutzer.saveAndFlush(admin).getId();
    }

    @Wenn("ich eine Region anlege und Stufe {int} freischalte")
    public void legeRegionAnUndSchalteStufeFrei(int stufenNummer) {
        String code = "bdd-" + UUID.randomUUID().toString().substring(0, 8);
        regionId = verwaltung.legeRegionAn(code, "Beispielregion", adminId);
        verwaltung.schalteStufe(regionId, Stufe.vonNummer(stufenNummer), true, adminId);
    }

    @Dann("ist Stufe {int} für diese Region freigeschaltet")
    public void stufeIstFreigeschaltet(int stufenNummer) {
        assertThat(verwaltung.alleRegionen(adminId).stream()
                .filter(region -> region.id().equals(regionId))
                .flatMap(region -> region.freigeschalteteStufen().stream())
                .map(Stufe::nummer))
                .contains(stufenNummer);
    }

    @Angenommen("die Pilotregion ist mit Stufe 1 freigeschaltet")
    public void pilotregionIstFreigeschaltet() {
        RegionDaten pilot = verwaltung.alleRegionen(erstelleAdmin()).stream()
                .filter(region -> region.code().equals("pilot"))
                .map(region -> new RegionDaten(region.id(), region.code(), region.name(),
                        region.plzBereiche(), region.freigeschalteteStufen()))
                .findFirst()
                .orElseThrow();
        assertThat(pilot.freigeschalteteStufen()).contains(Stufe.ANGEBOT_UND_VORTEILE);
    }

    @Wenn("eine Beispielfunktion Stufe {int} für PLZ {int} über den Prüfport anfragt")
    public void beispielFunktionFragtStufeAn(int stufenNummer, int plz) {
        funktionsStatus = stufenPruefung.istFreigeschaltet(
                Stufe.vonNummer(stufenNummer), Integer.toString(plz)) ? 200 : 404;
    }

    @Dann("antwortet die Beispielfunktion mit {int}")
    public void beispielFunktionAntwortet(int status) {
        assertThat(funktionsStatus).isEqualTo(status);
    }

    @Wenn("ich eine Region umbenenne")
    public void benenneRegionUm() {
        String code = "rename-" + UUID.randomUUID().toString().substring(0, 8);
        regionId = verwaltung.legeRegionAn(code, "Vorheriger Beispielname", adminId);
        neuerName = "Neuer Beispielname";
        verwaltung.benenneRegionUm(regionId, neuerName, adminId);
    }

    @Dann("wird der neue Regionsname angezeigt")
    public void neuerNameWirdAngezeigt() {
        assertThat(verwaltung.alleRegionen(adminId).stream()
                .filter(region -> region.id().equals(regionId))
                .map(region -> region.name()))
                .contains(neuerName);
    }

    @Wenn("ich alle Zahlungsrouten bei ausgeschalteter Zahlung prüfe")
    public void pruefeZahlungsrouten() throws Exception {
        routenStatus = pruefeRouten(ZAHLUNGSROUTEN);
    }

    @Wenn("ich Routen für Zahlung Vertrag Rechnung Aufgaben und Matching prüfe")
    public void pruefeFunktionsrouten() throws Exception {
        routenStatus = pruefeRouten(FUNKTIONSROUTEN);
    }

    @Dann("antworten alle Zahlungsrouten mit 404")
    public void zahlungsroutenSindAbgeschaltet() {
        assertThat(routenStatus).isNotEmpty().containsOnly(404);
    }

    @Dann("antworten alle abgeschalteten Funktionsrouten mit 404")
    public void funktionsroutenSindAbgeschaltet() {
        assertThat(routenStatus).isNotEmpty().containsOnly(404);
    }

    private UUID erstelleAdmin() {
        if (adminId == null) {
            binPlattformAdmin();
        }
        return adminId;
    }

    private List<Integer> pruefeRouten(List<String> routen) {
        FunktionSchalterInterceptor interceptor = new FunktionSchalterInterceptor(
                new FunktionSchalter(false, false, false, false, false));
        return routen.stream().map(pfad -> {
            try {
                String methode = pfad.contains("/payment/webhook/")
                        || pfad.contains("/rechnung/erstellen") ? "POST" : "GET";
                MockHttpServletRequest request = new MockHttpServletRequest(methode, pfad);
                var handler = handlerMapping.getHandler(request);
                if (handler == null) {
                    return 404;
                }
                MockHttpServletResponse response = new MockHttpServletResponse();
                return interceptor.preHandle(request, response, handler.getHandler())
                        ? 200
                        : response.getStatus();
            } catch (Exception ex) {
                throw new IllegalStateException("Funktionsroute konnte nicht geprüft werden.", ex);
            }
        }).toList();
    }
}
