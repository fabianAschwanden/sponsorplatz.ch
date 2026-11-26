package ch.sponsorplatz.backup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zugangsdaten für pg_dump und psql kommen aus der Datenquelle, nicht aus der
 * Prozessumgebung: Im Betrieb liegen Geheimnisse als Dateien vor.
 */
class PgZugangTest {

    @Test
    @DisplayName("Passwort, Benutzer und Host der Datenquelle landen in der Umgebung des Prozesses")
    void setztZugangsdaten() {
        Map<String, String> umgebung = new HashMap<>();

        PgZugang.setze(umgebung, "geheim", "sponsorplatz", "db");

        assertThat(umgebung)
                .containsEntry("PGPASSWORD", "geheim")
                .containsEntry("PGUSER", "sponsorplatz")
                .containsEntry("PGHOST", "db");
    }

    @Test
    @DisplayName("Fehlende Werte werden ausgelassen statt als null gesetzt (ProcessBuilder verträgt kein null)")
    void laesstLeereWerteAus() {
        Map<String, String> umgebung = new HashMap<>();

        PgZugang.setze(umgebung, null, " ", null);

        assertThat(umgebung).isEmpty();
    }
}
