package ch.sponsorplatz.e2e;

import ch.sponsorplatz.shared.mail.MailAnhang;
import ch.sponsorplatz.shared.mail.MailVersand;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TestConfiguration
public class E2EMailCaptureConfig {

    @Bean
    @Primary
    CapturingMailVersand e2eMailVersand() {
        return new CapturingMailVersand();
    }

    public static class CapturingMailVersand implements MailVersand {
        private final List<Nachricht> nachrichten = new CopyOnWriteArrayList<>();

        @Override
        public void sendePlain(String to, String subject, String body) {
            nachrichten.add(new Nachricht(to, subject, body));
        }

        @Override
        public void sendeHtml(String to, String subject, String htmlBody) {
            nachrichten.add(new Nachricht(to, subject, htmlBody));
        }

        @Override
        public void sendeHtmlMitAnhang(String to, String subject, String htmlBody, MailAnhang anhang) {
            nachrichten.add(new Nachricht(to, subject, htmlBody));
        }

        @Override
        public boolean istKonfiguriert() {
            return false;
        }

        @Override
        public boolean istLiveMode() {
            return false;
        }

        @Override
        public String effektiverHost() {
            return "";
        }

        @Override
        public String effektiverTestEmpfaenger() {
            return "";
        }

        @Override
        public String effektiverAbsender() {
            return "";
        }

        @Override
        public void sendeTestMail() {
        }

        public void leere() {
            nachrichten.clear();
        }

        public List<Nachricht> nachrichten() {
            return List.copyOf(nachrichten);
        }
    }

    public record Nachricht(String empfaenger, String betreff, String inhalt) {
    }
}
