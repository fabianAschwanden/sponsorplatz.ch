package ch.sponsorplatz.e2e;

import ch.sponsorplatz.pilotanfrage.domain.PilotEinwilligungstext;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.shared.config.Formular;
import ch.sponsorplatz.shared.config.FormularDrosselungService;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PilotAnfrageSteps {

    @Autowired private E2EContext ctx;
    @Autowired private E2EMailCaptureConfig.CapturingMailVersand mailVersand;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private FormularDrosselungService formularDrosselung;

    @Angenommen("ich öffne die Pilot-Anfrage als Verein")
    public void oeffnePilotAnfrageAlsVerein() {
        oeffnePilotAnfrage("verein", "Beispielverein");
        assertThat(ctx.getPage().locator("#organisationsName").inputValue()).isEqualTo("Beispielverein");
        assertThat(ctx.getPage().locator("input[name='rolle'][value='VEREIN']").isChecked()).isTrue();
    }

    @Angenommen("ich öffne die Pilot-Anfrage als Sponsor")
    public void oeffnePilotAnfrageAlsSponsor() {
        oeffnePilotAnfrage("sponsor", "Beispielpartner");
        assertThat(ctx.getPage().locator("#organisationsName").inputValue()).isEqualTo("Beispielpartner");
        assertThat(ctx.getPage().locator("input[name='rolle'][value='SPONSOR']").isChecked()).isTrue();
    }

    @Wenn("ich eine gültige Anfrage absende")
    public void sendeGueltigeAnfrage() {
        sendeGueltigeAnfrage(false, false);
    }

    @Wenn("ich das Anfrageformular mit ausgefülltem Honeypot absende")
    public void sendeAnfrageMitHoneypot() {
        String email = ctx.eindeutigeEmail("pilot");
        fuelleFormular("Beispielverein", email, false, false);
        ctx.getDaten().put("pilotEmail", email);
        ctx.getPage().locator("input[name='homepage']").evaluate("(element) => { element.value = 'bot'; }");
        sendeFormular();
    }

    @Wenn("ich eine Anfrage mit freiwilligen Angaben absende")
    public void sendeAnfrageMitFreiwilligenAngaben() {
        String email = ctx.eindeutigeEmail("pilot");
        fuelleFormular("Beispielverein", email, false, true);
        ctx.getDaten().put("pilotEmail", email);
        sendeFormular();
    }

    @Wenn("ich dieselbe Anfrage zweimal mit geänderten Angaben absende")
    public void sendeDoppelteAnfrage() {
        String email = ctx.eindeutigeEmail("pilot");
        fuelleFormular("Beispielverein", email, false, false);
        sendeFormular();
        oeffnePilotAnfrage("verein", "Beispielverein");
        fuelleFormular("beispielverein", email.toUpperCase(Locale.ROOT), true, true);
        ctx.getDaten().put("pilotEmail", email);
        ctx.getDaten().put("pilotOrganisation", "beispielverein");
        sendeFormular();
    }

    @Wenn("ich die Updates ankreuze und die Anfrage absende")
    public void sendeAnfrageMitUpdates() {
        sendeGueltigeAnfrage(true, false);
    }

    @Wenn("ich zehn Anfragen von derselben Adresse und die elfte absende")
    public void sendeElfAnfragenVonGleicherAdresse() {
        for (int nummer = 1; nummer <= 10; nummer++) {
            oeffnePilotAnfrage("verein", "Beispielverein " + nummer);
            fuelleFormular("Beispielverein " + nummer, ctx.eindeutigeEmail("pilot"), false, false);
            sendeFormular();
        }

        oeffnePilotAnfrage("verein", "Beispielverein 11");
        fuelleFormular("Beispielverein 11", ctx.eindeutigeEmail("pilot"), false, false);
        Page page = ctx.getPage();
        Response response = page.waitForResponse(
                r -> "POST".equals(r.request().method()) && r.url().endsWith("/pilot"),
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Pilotplatz anfragen")).click());
        page.waitForLoadState(LoadState.LOAD);
        ctx.getDaten().put("pilotRateStatus", response.status());
    }

    @Wenn("ich Drosselungseinträge für IPv4 und IPv6 erfasse")
    public void erfasseDrosselungseintraege() {
        assertThat(formularDrosselung.erfasseEingabe(Formular.PILOT, "203.0.113.8")).isTrue();
        String pruefwert = jdbc.queryForObject(
                "select pruefwert from formular_drosselung where formular = 'PILOT'",
                String.class);
        ctx.getDaten().put("pilotPruefwert", pruefwert);

        for (int nummer = 1; nummer <= 10; nummer++) {
            String adresse = "2001:db8:0:1::" + Integer.toHexString(nummer);
            assertThat(formularDrosselung.erfasseEingabe(Formular.PILOT, adresse)).isTrue();
        }
        ctx.getDaten().put("pilotIpv6Abgewiesen",
                !formularDrosselung.erfasseEingabe(Formular.PILOT, "2001:db8:0:1::b"));

        jdbc.update("update formular_drosselung set erstellt_am = ?",
                Timestamp.from(Instant.now().minus(Duration.ofDays(2))));
        ctx.getDaten().put("pilotGeloescht",
                formularDrosselung.loescheAeltereAls(Instant.now().minus(Duration.ofDays(1))));
    }

    @Dann("ist die Anfrage gespeichert und die Bestätigung sichtbar")
    public void anfrageGespeichertUndBestaetigt() {
        assertThat(ctx.getPage().url()).endsWith("/pilot/bestaetigung");
        Integer anfragen = jdbc.queryForObject(
                "select count(*) from pilot_anfrage where email = ?",
                Integer.class, ctx.<String>daten("pilotEmail"));
        assertThat(anfragen).isEqualTo(1);
    }

    @Dann("wird die Anfrage bestätigt, aber nicht gespeichert")
    public void botAnfrageWirdNichtGespeichert() {
        assertThat(ctx.getPage().url()).endsWith("/pilot/bestaetigung");
        Integer anfragen = jdbc.queryForObject(
                "select count(*) from pilot_anfrage where email = ?",
                Integer.class, ctx.<String>daten("pilotEmail"));
        assertThat(anfragen).isZero();
    }

    @Dann("sind die freiwilligen Angaben gespeichert und Updates sind abgewählt")
    public void freiwilligeAngabenGespeichert() {
        Map<String, Object> anfrage = ladeAnfrage();
        assertThat(anfrage.get("paketname")).isEqualTo("Beispielpaket");
        assertThat(anfrage.get("gegenleistungen")).asString().contains("stand", "newsletter");
        assertThat(anfrage.get("mitgliederzahl")).isEqualTo("240");
        assertThat(anfrage.get("naechster_anlass")).isEqualTo("Beispielturnier");
        assertThat(anfrage.get("interessen")).asString().contains("standaktionen");
        assertThat(anfrage.get("zielgruppe")).isEqualTo("Familien in der Region");
        assertThat(anfrage.get("marktgebiet")).isEqualTo("Beispielregion");
        assertThat(anfrage.get("programme")).isEqualTo("Beispielprogramm");
        assertThat(anfrage.get("uid")).isEqualTo("CHE-123.456.789");
        assertThat(anfrage.get("funktion")).isEqualTo("Vorstand");
        assertThat(anfrage.get("telefon")).isEqualTo("+41 00 000 00 00");
        assertThat(anfrage.get("updates")).isEqualTo(false);
    }

    @Dann("gibt es eine aktualisierte Anfrage mit beiden Einwilligungsnachweisen")
    public void doppelteAnfrageWirdAktualisiert() {
        Integer anfragen = jdbc.queryForObject(
                "select count(*) from pilot_anfrage where email = ?",
                Integer.class, ctx.<String>daten("pilotEmail"));
        assertThat(anfragen).isEqualTo(1);
        assertThat(ladeAnfrage().get("paketname")).isEqualTo("Beispielpaket");
        Integer einwilligungen = jdbc.queryForObject(
                "select count(*) from einwilligung where bezug_art = 'PILOT_ANFRAGE' and bezug_id = "
                        + "(select id from pilot_anfrage where email = ?)",
                Integer.class, ctx.<String>daten("pilotEmail"));
        assertThat(einwilligungen).isEqualTo(2);
    }

    @Dann("erklärt die Bestätigung die nächsten Schritte ohne etwas öffentlich zu zeigen")
    public void bestaetigungErklaertNaechsteSchritte() {
        String inhalt = ctx.getPage().content();
        assertThat(inhalt).contains("prüfen", "per E-Mail", "keine Organisation", "nichts wird öffentlich sichtbar");
        Integer organisationen = jdbc.queryForObject(
                "select count(*) from organisation where name = ?",
                Integer.class, ctx.<String>daten("pilotOrganisation"));
        assertThat(organisationen).isZero();
    }

    @Dann("ist der Wortlaut der Einwilligung mit Zeitpunkt gespeichert")
    public void einwilligungIstGespeichert() {
        Map<String, Object> anfrage = ladeAnfrage();
        Map<String, Object> einwilligung = jdbc.queryForMap(
                "select zweck, wortlaut, zeitpunkt from einwilligung where id = ?",
                anfrage.get("einwilligung_id"));
        assertThat(einwilligung.get("zweck")).isEqualTo("PILOT_ANFRAGE");
        assertThat(einwilligung.get("wortlaut"))
                .isEqualTo(PilotEinwilligungstext.vollstaendig(PilotAnfrageRolle.VEREIN));
        assertThat(einwilligung.get("zeitpunkt")).isNotNull();
    }

    @Dann("ist der Wunsch nach Updates gespeichert")
    public void updatesSindGespeichert() {
        assertThat(ladeAnfrage().get("updates")).isEqualTo(true);
    }

    @Dann("sind Updates abgewählt")
    public void updatesSindNichtGespeichert() {
        assertThat(ladeAnfrage().get("updates")).isEqualTo(false);
    }

    @Dann("wird die elfte Anfrage mit einer verständlichen 429-Seite abgewiesen")
    public void elfteAnfrageWirdAbgewiesen() {
        assertThat(ctx.<Integer>daten("pilotRateStatus")).isEqualTo(429);
        assertThat(ctx.getPage().content()).contains("zu viele Eingaben", "versucht es erneut");
    }

    @Dann("das globale Limit gilt ebenfalls pro Formular")
    public void globalesLimitGiltProFormular() {
        for (int nummer = 0; nummer < 60; nummer++) {
            assertThat(formularDrosselung.erfasseEingabe(Formular.KONTAKT, "192.0.2." + nummer)).isTrue();
        }
        assertThat(formularDrosselung.erfasseEingabe(Formular.KONTAKT, "192.0.2.100")).isFalse();
    }

    @Dann("ist nur der HMAC-Prüfwert gespeichert")
    public void nurHmacWertGespeichert() {
        String pruefwert = ctx.daten("pilotPruefwert");
        assertThat(pruefwert).matches("[0-9a-f]{64}").doesNotContain("203.0.113.8");
    }

    @Dann("die IPv6-Adressen aus demselben Netz teilen sich ein Limit")
    public void ipv6AdressenTeilenLimit() {
        assertThat(ctx.<Boolean>daten("pilotIpv6Abgewiesen")).isTrue();
    }

    @Dann("Einträge älter als ein Tag werden entfernt")
    public void alteEintraegeWerdenEntfernt() {
        assertThat(ctx.<Integer>daten("pilotGeloescht")).isEqualTo(11);
        Integer anzahl = jdbc.queryForObject("select count(*) from formular_drosselung", Integer.class);
        assertThat(anzahl).isZero();
    }

    @Dann("enthält die Verwaltungsmeldung nur Rolle Bereich PLZ-Kreis und Listenlink")
    public void verwaltungsMeldungEnthaeltNurNoetigeAngaben() {
        assertThat(mailVersand.nachrichten()).hasSize(1);
        String mail = mailVersand.nachrichten().getFirst().inhalt();
        assertThat(mail).contains("Rolle: Verein", "Bereich: Ballsport", "PLZ-Kreis: 80", "/admin/pilotanfragen");
        assertThat(mail).doesNotContain(
                "Beispielverein", "Beispielperson", ctx.<String>daten("pilotEmail"), "kontakt@beispiel.test");
    }

    private void sendeGueltigeAnfrage(boolean updates, boolean freiwilligeAngaben) {
        String orgName = ctx.<String>daten("pilotOrganisation");
        String email = ctx.eindeutigeEmail("pilot");
        fuelleFormular(orgName, email, updates, freiwilligeAngaben);
        ctx.getDaten().put("pilotEmail", email);
        sendeFormular();
    }

    private void oeffnePilotAnfrage(String rolle, String organisationsName) {
        mailVersand.leere();
        String name = java.net.URLEncoder.encode(organisationsName, java.nio.charset.StandardCharsets.UTF_8);
        ctx.getPage().navigate(ctx.getBaseUrl() + "/pilot?rolle=" + rolle + "&name=" + name);
        ctx.getDaten().put("pilotRolle", rolle.toUpperCase(Locale.ROOT));
        ctx.getDaten().put("pilotOrganisation", organisationsName);
    }

    private void fuelleFormular(String organisationsName, String email, boolean updates, boolean freiwilligeAngaben) {
        Page page = ctx.getPage();
        page.locator("#organisationsName").fill(organisationsName);
        if ("SPONSOR".equals(ctx.<String>daten("pilotRolle"))) {
            page.locator("#sponsorArt").selectOption("AGENTUR");
        } else {
            page.locator("#bereich").selectOption("BALLSPORT");
        }
        page.locator("#plz").fill("8000");
        page.locator("#ort").fill("Beispielort");
        page.locator("#vorname").fill("Beispielperson");
        page.locator("#nachname").fill("Beispielkontakt");
        page.locator("#email").fill(email);
        if (freiwilligeAngaben) {
            page.locator("#paketname").fill("Beispielpaket");
            page.locator("input[name='gegenleistungen'][value='stand']").check();
            page.locator("input[name='gegenleistungen'][value='newsletter']").check();
            page.locator("#mitgliederzahl").fill("240");
            page.locator("#naechsterAnlass").fill("Beispielturnier");
            page.locator("input[name='interessen'][value='standaktionen']").check();
            page.locator("#zielgruppe").fill("Familien in der Region");
            page.locator("#marktgebiet").fill("Beispielregion");
            page.locator("#programme").fill("Beispielprogramm");
            page.locator("#uid").fill("CHE-123.456.789");
            page.locator("#funktion").fill("Vorstand");
            page.locator("#telefon").fill("+41 00 000 00 00");
        }
        page.locator("#einwilligung").check();
        if (updates) {
            page.locator("#updates").check();
        }
    }

    private void sendeFormular() {
        Page page = ctx.getPage();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Pilotplatz anfragen")).click();
        page.waitForURL("**/pilot/bestaetigung");
    }

    private Map<String, Object> ladeAnfrage() {
        return jdbc.queryForMap("select * from pilot_anfrage where email = ?",
                ctx.<String>daten("pilotEmail"));
    }
}
