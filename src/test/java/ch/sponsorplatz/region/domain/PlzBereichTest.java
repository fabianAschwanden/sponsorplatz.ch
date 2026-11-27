package ch.sponsorplatz.region.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlzBereichTest {
    @Test
    void umfasstGrenzenUndErkenntUeberschneidungen() {
        PlzBereich bereich = new PlzBereich(1000, 1999);

        assertThat(bereich.enthaelt(1000)).isTrue();
        assertThat(bereich.enthaelt(1999)).isTrue();
        assertThat(bereich.enthaelt(2000)).isFalse();
        assertThat(bereich.ueberschneidet(new PlzBereich(1999, 2500))).isTrue();
        assertThat(bereich.ueberschneidet(new PlzBereich(2000, 2500))).isFalse();
    }

    @Test
    void lehntUngueltigeGrenzenAb() {
        assertThatThrownBy(() -> new PlzBereich(999, 1000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PlzBereich(9000, 10000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PlzBereich(2000, 1000)).isInstanceOf(IllegalArgumentException.class);
    }
}
