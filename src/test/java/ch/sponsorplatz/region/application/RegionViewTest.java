package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegionViewTest {
    @Test
    void bildetAnzeigedatenOhnePersistenzobjekteAb() {
        RegionView view = RegionView.von(new RegionDaten(
                UUID.randomUUID(), "pilot", "Pilot", List.of(new PlzBereich(1000, 9999)),
                Set.of(Stufe.ANGEBOT_UND_VORTEILE)));

        assertThat(view.code()).isEqualTo("pilot");
        assertThat(view.plzBereicheText()).isEqualTo("1000-9999");
        assertThat(view.freigeschalteteStufen()).containsExactly(Stufe.ANGEBOT_UND_VORTEILE);
    }
}
