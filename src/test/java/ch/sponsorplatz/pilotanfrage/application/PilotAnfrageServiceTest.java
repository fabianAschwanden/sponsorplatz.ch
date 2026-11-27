package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PilotAnfrageServiceTest {

    @Mock private PilotAnfrageAblage ablage;
    @Mock private DrosselungPort drosselung;
    @Mock private PilotAnfrageEreignisPort ereignisPort;

    private PilotAnfrageService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-27T05:00:00Z"), ZoneOffset.UTC);
        service = new PilotAnfrageService(ablage, drosselung, ereignisPort, clock);
    }

    @Test
    void speichertEinwilligungMitUtcWortlautUndBenachrichtigtOhnePersonendaten() {
        when(drosselung.erfasseEingabe("203.0.113.8")).thenReturn(true);
        UUID anfrageId = UUID.fromString("6d3c85ba-8637-4bdb-b7a1-6a4ed39079a0");
        when(ablage.speichereOderAktualisiere(any(), any(), any())).thenReturn(anfrageId);

        boolean gespeichert = service.speichere(eingabe(true), "203.0.113.8");

        assertThat(gespeichert).isTrue();
        ArgumentCaptor<PilotAnfrage> anfrage = ArgumentCaptor.forClass(PilotAnfrage.class);
        ArgumentCaptor<ch.sponsorplatz.pilotanfrage.domain.Einwilligung> einwilligung =
                ArgumentCaptor.forClass(ch.sponsorplatz.pilotanfrage.domain.Einwilligung.class);
        verify(ablage).speichereOderAktualisiere(anfrage.capture(), einwilligung.capture(), any());
        assertThat(anfrage.getValue().email()).isEqualTo("kontakt@beispiel.test");
        assertThat(anfrage.getValue().updates()).isTrue();
        assertThat(einwilligung.getValue().zeitpunkt()).isEqualTo(Instant.parse("2026-11-27T05:00:00Z"));
        assertThat(einwilligung.getValue().wortlaut()).contains("unseren Verein anmelden");
        ArgumentCaptor<PilotAnfrageBenachrichtigungEvent> event =
                ArgumentCaptor.forClass(PilotAnfrageBenachrichtigungEvent.class);
        verify(ereignisPort).publiziere(event.capture());
        assertThat(event.getValue()).isEqualTo(new PilotAnfrageBenachrichtigungEvent(
                anfrageId, PilotAnfrageRolle.VEREIN, PilotAnfrageBereich.BALLSPORT, null, "80"));
    }

    @Test
    void beiUeberschrittenemLimitWirdNichtsGespeichertOderGemeldet() {
        when(drosselung.erfasseEingabe("203.0.113.8")).thenReturn(false);

        boolean gespeichert = service.speichere(eingabe(false), "203.0.113.8");

        assertThat(gespeichert).isFalse();
        verify(ablage, never()).speichereOderAktualisiere(any(), any(), any());
        verify(ereignisPort, never()).publiziere(any());
    }

    private PilotAnfrageEingabe eingabe(boolean updates) {
        return new PilotAnfrageEingabe(
                PilotAnfrageRolle.VEREIN, "Beispielverein", PilotAnfrageBereich.BALLSPORT, null,
                "8000", "Beispielort", "Beispiel", "Kontakt", "Kontakt@Beispiel.test",
                "Beispielpaket", "", "", "", "", "", "", "", "", "", "", updates);
    }
}
