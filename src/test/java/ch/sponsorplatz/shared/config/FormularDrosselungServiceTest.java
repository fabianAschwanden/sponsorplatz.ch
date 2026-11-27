package ch.sponsorplatz.shared.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

class FormularDrosselungServiceTest {

    private static final String SCHLUESSEL = "fester-testwert-fuer-hmac-drosselung-2026";

    @Test
    void ipv6AdressenDesselben64NetzesErgebenDenselbenPruefwert() {
        FormularDrosselungService service = service(SCHLUESSEL);

        assertThat(service.pruefwert("2001:db8:1:2::1"))
                .isEqualTo(service.pruefwert("2001:db8:1:2::ffff"));
        assertThat(service.pruefwert("2001:db8:1:3::1"))
                .isNotEqualTo(service.pruefwert("2001:db8:1:2::1"));
    }

    @Test
    void pruefwertIstEinHmacUndSpeichertKeineAdresse() {
        String adresse = "203.0.113.8";
        String pruefwert = service(SCHLUESSEL).pruefwert(adresse);

        assertThat(pruefwert).matches("[0-9a-f]{64}").doesNotContain(adresse);
    }

    @Test
    void lehntUngueltigeAdressenUndFehlendenSchluesselAb() {
        FormularDrosselungService service = service(SCHLUESSEL);

        assertThatIllegalArgumentException().isThrownBy(() -> service.pruefwert("kein-ip"));
        assertThatIllegalStateException()
                .isThrownBy(() -> service(null))
                .withMessageContaining("Secret-Datei drossel_schluessel");
    }

    private FormularDrosselungService service(String schluessel) {
        return new FormularDrosselungService(
                mock(FormularDrosselungRepository.class),
                mock(FormularDrosselungSperreRepository.class),
                schluessel,
                Clock.fixed(Instant.parse("2026-11-27T05:00:00Z"), ZoneOffset.UTC));
    }
}
