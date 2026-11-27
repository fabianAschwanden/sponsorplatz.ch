package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {
    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final UUID REGION_ID = UUID.randomUUID();
    private static final Instant JETZT = Instant.parse("2026-11-27T06:00:00Z");

    @Mock
    private RegionSpeicherPort speicher;
    @Mock
    private RegionAdminRechtePort rechte;
    @Mock
    private RegionEreignisPort ereignisse;

    private RegionService service;

    @BeforeEach
    void initialisiere() {
        service = new RegionService(speicher, rechte, ereignisse,
                Clock.fixed(JETZT, ZoneOffset.UTC));
    }

    @Test
    void erlaubtNurFreigeschalteteStufenInnerhalbEinerRegion() {
        when(speicher.alleRegionen()).thenReturn(List.of(
                new RegionDaten(REGION_ID, "pilot", "Pilot", List.of(new PlzBereich(1000, 9999)),
                        Set.of(Stufe.ANGEBOT_UND_VORTEILE))));

        assertThat(service.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "8000")).isTrue();
        assertThat(service.istFreigeschaltet(Stufe.FINDEN_UND_ZUSAGEN, "8000")).isFalse();
        assertThat(service.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "0999")).isFalse();
        assertThat(service.istFreigeschaltet(Stufe.ANGEBOT_UND_VORTEILE, "9999")).isTrue();
    }

    @Test
    void sperrtBeispielfunktionWennStufeNichtFreigeschaltetIst() {
        when(speicher.alleRegionen()).thenReturn(List.of(
                new RegionDaten(REGION_ID, "pilot", "Pilot", List.of(new PlzBereich(1000, 9999)),
                        Set.of(Stufe.ANGEBOT_UND_VORTEILE))));

        int status = beispielFunktion(service, Stufe.FINDEN_UND_ZUSAGEN, "8000");

        assertThat(status).isEqualTo(404);
    }

    @Test
    void blockiertPlzBereichDerEineAndereRegionUeberschneidet() {
        when(rechte.istPlattformAdmin(ADMIN_ID)).thenReturn(true);
        when(speicher.findeRegion(REGION_ID)).thenReturn(java.util.Optional.of(
                new RegionDaten(REGION_ID, "pilot", "Pilot", List.of(), Set.of())));
        when(speicher.hatUeberschneidendePlzBereiche(REGION_ID, new PlzBereich(2000, 2099))).thenReturn(true);

        assertThatThrownBy(() -> service.setzePlzBereiche(REGION_ID, "2000-2099", ADMIN_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anderen Region");

        verify(speicher, never()).speicherePlzBereiche(any(), any());
        verify(ereignisse, never()).protokolliere(any());
    }

    @Test
    void schaltetStufeMitUtcZeitpunktUndAuditereignisEin() {
        when(rechte.istPlattformAdmin(ADMIN_ID)).thenReturn(true);
        when(speicher.findeRegion(REGION_ID)).thenReturn(java.util.Optional.of(
                new RegionDaten(REGION_ID, "pilot", "Pilot", List.of(), Set.of())));

        service.schalteStufe(REGION_ID, Stufe.WIRKUNG, true, ADMIN_ID);

        verify(speicher).schalteStufeEin(REGION_ID, Stufe.WIRKUNG, JETZT, ADMIN_ID);
        verify(ereignisse).protokolliere(new RegionAenderungsEreignis(
                REGION_ID, ADMIN_ID, RegionAenderungsart.STUFE_FREIGESCHALTET, "3"));
    }

    @Test
    void verweigertVerwaltungOhnePlattformAdmin() {
        when(rechte.istPlattformAdmin(ADMIN_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.alleRegionen(ADMIN_ID))
                .isInstanceOf(RegionZugriffVerweigertException.class);

        verify(speicher, never()).alleRegionen();
    }

    private int beispielFunktion(StufenPruefung pruefung, Stufe stufe, String plz) {
        return pruefung.istFreigeschaltet(stufe, plz) ? 200 : 404;
    }
}
