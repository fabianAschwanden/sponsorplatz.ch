package ch.sponsorplatz.organisation;

import ch.sponsorplatz.shared.mail.MailVersand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrganisationPruefentscheidMailListenerTest {

    @Mock
    private MailVersand mailVersand;

    @Test
    void ablehnungsmailEnthaeltGrund() {
        OrganisationPruefentscheidMailListener listener = new OrganisationPruefentscheidMailListener(mailVersand);
        OrganisationPruefentscheidEvent event = new OrganisationPruefentscheidEvent(
                UUID.randomUUID(), "Beispielverein", "kontakt@example.ch", false, "Angaben nicht plausibel");

        listener.aufPruefentscheid(event);

        ArgumentCaptor<String> inhalt = ArgumentCaptor.forClass(String.class);
        verify(mailVersand).sendePlain(
                org.mockito.ArgumentMatchers.eq("kontakt@example.ch"), anyString(), inhalt.capture());
        assertThat(inhalt.getValue()).contains("Beispielverein", "Angaben nicht plausibel");
    }

    @Test
    void freigabemailNenntErgebnis() {
        OrganisationPruefentscheidMailListener listener = new OrganisationPruefentscheidMailListener(mailVersand);
        OrganisationPruefentscheidEvent event = new OrganisationPruefentscheidEvent(
                UUID.randomUUID(), "Beispielverein", "kontakt@example.ch", true, null);

        listener.aufPruefentscheid(event);

        ArgumentCaptor<String> inhalt = ArgumentCaptor.forClass(String.class);
        verify(mailVersand).sendePlain(
                org.mockito.ArgumentMatchers.eq("kontakt@example.ch"), anyString(), inhalt.capture());
        assertThat(inhalt.getValue()).contains("freigegeben").doesNotContain("Angaben");
    }

    @Test
    void versandfehlerVerhindertKeinenAbgeschlossenenEntscheid() {
        doThrow(new MailSendException("SMTP down"))
                .when(mailVersand).sendePlain(anyString(), anyString(), anyString());
        OrganisationPruefentscheidMailListener listener = new OrganisationPruefentscheidMailListener(mailVersand);

        assertThatCode(() -> listener.aufPruefentscheid(new OrganisationPruefentscheidEvent(
                UUID.randomUUID(), "Beispielverein", "kontakt@example.ch", true, null)))
                .doesNotThrowAnyException();
    }
}
