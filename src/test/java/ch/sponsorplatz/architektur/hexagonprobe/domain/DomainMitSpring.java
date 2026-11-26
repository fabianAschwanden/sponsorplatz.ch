package ch.sponsorplatz.architektur.hexagonprobe.domain;

import org.springframework.util.StringUtils;

/** Verstoss: Die Domain hängt an einem Framework. Bewusst ohne Stereotyp, damit kein Test-Kontext die Klasse lädt. */
public class DomainMitSpring {

    public boolean hatText(String wert) {
        return StringUtils.hasText(wert);
    }
}
