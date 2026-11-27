package ch.sponsorplatz.region.domain;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public record Region(UUID id, String code, String name) {
    private static final Pattern CODE = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    public Region {
        Objects.requireNonNull(id, "id");
        if (code == null || !CODE.matcher(code).matches()) {
            throw new IllegalArgumentException("Der Regionscode darf nur Kleinbuchstaben, Zahlen und Bindestriche enthalten.");
        }
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new IllegalArgumentException("Der Regionsname muss zwischen 1 und 100 Zeichen lang sein.");
        }
        name = name.trim();
    }
}
