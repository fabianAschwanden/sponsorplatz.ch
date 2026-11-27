package ch.sponsorplatz.pilotanfrage.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PilotAnfrageTest {

    @Test
    void gleicheIdentitaetIgnoriertGrossschreibungUndLeerraum() {
        PilotAnfrage erste = anfrage("Beispielverein", "Kontakt@beispiel.test", PilotAnfrageRolle.VEREIN);
        PilotAnfrage zweite = anfrage(" beispielverein ", "kontakt@beispiel.test", PilotAnfrageRolle.VEREIN);

        assertThat(erste.gleicheIdentitaetWie(zweite)).isTrue();
    }

    @Test
    void andereRolleIstKeineGleicheAnfrage() {
        PilotAnfrage verein = anfrage("Beispielverein", "kontakt@beispiel.test", PilotAnfrageRolle.VEREIN);
        PilotAnfrage sponsor = anfrage("Beispielverein", "kontakt@beispiel.test", PilotAnfrageRolle.SPONSOR);

        assertThat(verein.gleicheIdentitaetWie(sponsor)).isFalse();
    }

    @Test
    void einwilligungswortlautUnterscheidetDieRolle() {
        assertThat(PilotEinwilligungstext.vollstaendig(PilotAnfrageRolle.VEREIN))
                .contains("unseren Verein anmelden")
                .contains(PilotEinwilligungstext.hinweis());
        assertThat(PilotEinwilligungstext.vollstaendig(PilotAnfrageRolle.SPONSOR))
                .contains("unsere Organisation anmelden");
    }

    private PilotAnfrage anfrage(String name, String email, PilotAnfrageRolle rolle) {
        Instant zeitpunkt = Instant.parse("2026-11-27T05:00:00Z");
        return new PilotAnfrage(rolle, name, null, null, "8000", "Beispielort", "Beispiel",
                "Kontakt", email, "", "", "", "", "", "", "", "", "", "", "", false, zeitpunkt, zeitpunkt);
    }
}
