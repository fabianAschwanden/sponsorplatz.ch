package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RegionDaten(
        UUID id,
        String code,
        String name,
        List<PlzBereich> plzBereiche,
        Set<Stufe> freigeschalteteStufen) {
    public RegionDaten {
        plzBereiche = List.copyOf(plzBereiche);
        freigeschalteteStufen = Set.copyOf(freigeschalteteStufen);
    }
}
