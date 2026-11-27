package ch.sponsorplatz.shared.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HmacSha256Test {

    @Test
    void berechnetHmacSha256() {
        String pruefwert = HmacSha256.berechne(
                "key".getBytes(StandardCharsets.UTF_8),
                "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));

        assertThat(pruefwert).isEqualTo("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8");
    }
}
