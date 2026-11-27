package ch.sponsorplatz.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FormularDrosselungBereinigungTest {

    @Test
    void wirdStuendlichAusgefuehrtUndEntferntEintraegeAelterAlsEinenTag() throws Exception {
        FormularDrosselungService drosselung = mock(FormularDrosselungService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-27T05:30:00Z"), ZoneOffset.UTC);
        FormularDrosselungBereinigung bereinigung = new FormularDrosselungBereinigung(drosselung, clock);

        bereinigung.bereinige();

        verify(drosselung).loescheAeltereAls(clock.instant().minus(Duration.ofDays(1)));
        Scheduled schedule = FormularDrosselungBereinigung.class
                .getMethod("bereinige")
                .getAnnotation(Scheduled.class);
        assertThat(schedule.cron()).isEqualTo("0 0 * * * *");
    }
}
