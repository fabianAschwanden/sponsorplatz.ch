package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record RegionView(
        UUID id,
        String code,
        String name,
        List<PlzBereich> plzBereiche,
        Set<Stufe> freigeschalteteStufen) {
    public RegionView {
        plzBereiche = List.copyOf(plzBereiche);
        freigeschalteteStufen = Set.copyOf(freigeschalteteStufen);
    }

    public static RegionView von(RegionDaten region) {
        return new RegionView(region.id(), region.code(), region.name(),
                region.plzBereiche(), region.freigeschalteteStufen());
    }

    public String plzBereicheText() {
        return plzBereiche.stream()
                .map(bereich -> "%04d-%04d".formatted(bereich.von(), bereich.bis()))
                .collect(Collectors.joining(", "));
    }
}
