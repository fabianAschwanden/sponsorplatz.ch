package ch.sponsorplatz.shared.config;

import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.benutzer.PlatformRolle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Das Profil {@code prod} so, wie es im Betrieb läuft: PostgreSQL 17,
 * Geheimnisse als Dateien, Actuator auf einem eigenen Port, Anwendung hinter
 * einem Reverse-Proxy.
 *
 * <p>Die übrigen Tests laufen mit dem Profil {@code dev}; ohne diesen Test
 * fiele ein Fehler in {@code application-prod.properties} oder in der
 * prod-Filterkette erst auf dem Server auf.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prod")
class ProdBetriebIT {

    static {
        BetriebsUmgebung.bereit();
    }

    @DynamicPropertySource
    static void betriebswerte(DynamicPropertyRegistry registry) {
        BetriebsUmgebung.eintragen(registry, "prod_betrieb");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AppUserRepository benutzer;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Test
    @DisplayName("Das Datenbank-Passwort kommt aus der Secret-Datei, keine Umgebungsvariable nötig")
    void passwortAusDatei() {
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo(BetriebsUmgebung.DB_PASSWORT);
        assertThat(System.getenv("DB_PASSWORD")).isNull();
    }

    @Test
    @DisplayName("Der Drosselungsschlüssel kommt aus der Secret-Datei")
    void drosselungsschluesselAusDatei() {
        assertThat(environment.getProperty("sponsorplatz.drosselung.schluessel"))
                .isEqualTo(BetriebsUmgebung.DROSSELUNG_SCHLUESSEL);
    }

    @Test
    @DisplayName("Alle Migrationen laufen gegen PostgreSQL 17 durch")
    void migrationenLaufenDurch() {
        Integer fehlgeschlagen = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where not success", Integer.class);
        Integer angewendet = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class);

        assertThat(fehlgeschlagen).isZero();
        assertThat(angewendet).isGreaterThanOrEqualTo(54);
    }

    @Test
    @DisplayName("Der erste Verwaltungszugang entsteht aus der Secret-Datei")
    void verwaltungszugangAusDatei() {
        Optional<AppUser> admin = benutzer.findByEmail(BetriebsUmgebung.ADMIN_EMAIL);

        assertThat(admin).isPresent();
        assertThat(admin.get().getPlatformRolle()).isEqualTo(PlatformRolle.PLATFORM_ADMIN);
    }

    @Test
    @DisplayName("/livez antwortet ohne Anmeldung mit 200 und ohne Details")
    void livezOhneAnmeldung() throws Exception {
        HttpResponse<String> antwort = hole("/livez");

        assertThat(antwort.statusCode()).isEqualTo(200);
        assertThat(antwort.body()).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    @DisplayName("Actuator ist auf dem Anwendungs-Port nicht erreichbar")
    void actuatorNichtAufAnwendungsPort() throws Exception {
        assertThat(hole("/actuator/prometheus").statusCode()).isNotEqualTo(200);
        assertThat(hole("/actuator/health").statusCode()).isNotEqualTo(200);
        assertThat(hole("/readyz").statusCode()).isNotEqualTo(200);
    }

    @Test
    @DisplayName("Hinter dem Reverse-Proxy leitet die Anwendung mit https und dem öffentlichen Host weiter")
    void weiterleitungMitHttps() throws Exception {
        HttpRequest anfrage = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/dashboard"))
                .header("X-Forwarded-Proto", "https")
                .header("X-Forwarded-Host", "plattform.muster.example")
                .header("X-Forwarded-Port", "443")
                .GET().build();

        HttpResponse<String> antwort = client.send(anfrage, HttpResponse.BodyHandlers.ofString());

        assertThat(antwort.statusCode()).isEqualTo(302);
        assertThat(antwort.headers().firstValue("Location"))
                .hasValue("https://plattform.muster.example/login");
    }

    private HttpResponse<String> hole(String pfad) throws IOException, InterruptedException {
        HttpRequest anfrage = HttpRequest.newBuilder(URI.create("http://localhost:" + port + pfad)).GET().build();
        return client.send(anfrage, HttpResponse.BodyHandlers.ofString());
    }
}
