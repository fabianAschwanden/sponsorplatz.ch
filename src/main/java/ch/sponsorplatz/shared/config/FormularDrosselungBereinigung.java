package ch.sponsorplatz.shared.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;

@Service
public class FormularDrosselungBereinigung {

    private final FormularDrosselungService drosselung;
    private final Clock clock;

    public FormularDrosselungBereinigung(FormularDrosselungService drosselung) {
        this.drosselung = drosselung;
        this.clock = Clock.systemUTC();
    }

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void bereinige() {
        drosselung.loescheAeltereAls(clock.instant().minus(Duration.ofDays(1)));
    }
}
