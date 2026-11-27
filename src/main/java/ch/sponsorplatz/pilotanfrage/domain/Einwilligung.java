package ch.sponsorplatz.pilotanfrage.domain;

import java.time.Instant;
import java.util.Objects;

public record Einwilligung(String zweck, String wortlaut, Instant zeitpunkt) {

    public Einwilligung {
        Objects.requireNonNull(zweck);
        Objects.requireNonNull(wortlaut);
        Objects.requireNonNull(zeitpunkt);
        if (zweck.isBlank() || wortlaut.isBlank()) {
            throw new IllegalArgumentException("Zweck und Wortlaut der Einwilligung sind erforderlich.");
        }
    }
}
