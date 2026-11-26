package ch.sponsorplatz.backup;

import java.util.Map;

/**
 * Zugangsdaten für {@code pg_dump} und {@code psql}.
 *
 * <p>Die Werte kommen aus der Datenquelle der Anwendung, nicht aus der
 * Prozessumgebung: Im Betrieb liegen Geheimnisse als Dateien vor
 * ({@code /run/secrets}), eine Variable {@code DB_PASSWORD} gibt es dort nicht.
 */
final class PgZugang {

    private PgZugang() {
    }

    static void setze(Map<String, String> umgebung, String passwort, String benutzer, String host) {
        if (passwort != null && !passwort.isBlank()) {
            umgebung.put("PGPASSWORD", passwort);
        }
        if (benutzer != null && !benutzer.isBlank()) {
            umgebung.put("PGUSER", benutzer);
        }
        if (host != null && !host.isBlank()) {
            umgebung.put("PGHOST", host);
        }
    }
}
