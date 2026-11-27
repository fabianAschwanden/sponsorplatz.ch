package ch.sponsorplatz.region.domain;

import java.util.Arrays;

public enum Stufe {
    ANGEBOT_UND_VORTEILE(1),
    FINDEN_UND_ZUSAGEN(2),
    WIRKUNG(3),
    UMFELD(4);

    private final int nummer;

    Stufe(int nummer) {
        this.nummer = nummer;
    }

    public int nummer() {
        return nummer;
    }

    public static Stufe vonNummer(int nummer) {
        return Arrays.stream(values())
                .filter(stufe -> stufe.nummer == nummer)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unbekannte Freischaltungsstufe."));
    }
}
