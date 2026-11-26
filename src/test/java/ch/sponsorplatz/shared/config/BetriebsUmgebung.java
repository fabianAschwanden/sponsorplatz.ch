package ch.sponsorplatz.shared.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Umgebung wie im Betrieb für Tests mit dem Profil {@code prod}: PostgreSQL 17
 * und Geheimnisse als Dateien.
 *
 * <p>Das Secret-Verzeichnis steht als System-Property bereit, bevor ein
 * Spring-Kontext lädt: {@code spring.config.import} wird aufgelöst, bevor
 * {@code @DynamicPropertySource} greift. Darum teilen sich alle Tests dieselben
 * Secret-Dateien und denselben Datenbank-Container.
 */
public final class BetriebsUmgebung {

    public static final String DB_PASSWORT = "nur-in-der-datei-db";
    public static final String ADMIN_PASSWORT = "nur-in-der-datei-admin";
    public static final String ADMIN_EMAIL = "betrieb@muster.test";

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine"))
            .withUsername("sponsorplatz")
            .withPassword(DB_PASSWORT);

    private static final Path DATEN;

    static {
        try {
            Path secrets = Files.createTempDirectory("sponsorplatz-secrets");
            DATEN = Files.createTempDirectory("sponsorplatz-daten");
            Files.writeString(secrets.resolve("db_password"), DB_PASSWORT);
            Files.writeString(secrets.resolve("admin_password"), ADMIN_PASSWORT);
            System.setProperty("SECRETS_VERZEICHNIS", secrets.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        POSTGRES.start();
    }

    private BetriebsUmgebung() {
    }

    /**
     * Erzwingt das Laden dieser Klasse. Jede Testklasse ruft das in einem
     * statischen Initialisierer auf: Das Secret-Verzeichnis muss gesetzt sein,
     * bevor Spring die Konfiguration lädt.
     */
    public static void bereit() {
        // Die Arbeit geschieht im statischen Initialisierer oben.
    }

    /**
     * Trägt die Werte ein, die im Betrieb aus {@code compose.yml} kommen.
     *
     * @param datenbank eigene Datenbank je Testklasse, damit sich die Klassen nicht beeinflussen
     */
    public static void eintragen(DynamicPropertyRegistry registry, String datenbank) {
        erzeugeDatenbank(datenbank);
        registry.add("DB_URL", () -> POSTGRES.getJdbcUrl().replaceFirst("/[^/?]+(\\?|$)", "/" + datenbank + "$1"));
        registry.add("DB_USER", POSTGRES::getUsername);
        registry.add("SPONSORPLATZ_ADMIN_EMAIL", () -> ADMIN_EMAIL);
        registry.add("STORAGE_PFAD", () -> DATEN.resolve(datenbank).resolve("medien").toString());
        registry.add("SICHERUNG_PFAD", () -> DATEN.resolve(datenbank).resolve("sicherung").toString());
        registry.add("management.server.port", () -> "0");
    }

    private static void erzeugeDatenbank(String datenbank) {
        if (!datenbank.matches("[a-z_]{1,30}")) {
            throw new IllegalArgumentException("Datenbankname nur aus Kleinbuchstaben und Unterstrich");
        }
        try {
            var ergebnis = POSTGRES.execInContainer("createdb", "-U", POSTGRES.getUsername(), datenbank);
            if (ergebnis.getExitCode() != 0 && !ergebnis.getStderr().contains("already exists")) {
                throw new IllegalStateException("createdb: " + ergebnis.getStderr());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
