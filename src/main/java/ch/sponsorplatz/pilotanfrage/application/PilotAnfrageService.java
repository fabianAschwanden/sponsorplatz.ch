package ch.sponsorplatz.pilotanfrage.application;

import ch.sponsorplatz.pilotanfrage.domain.Einwilligung;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;
import ch.sponsorplatz.pilotanfrage.domain.PilotEinwilligungstext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Service
public class PilotAnfrageService {

    private static final String ZWECK = "PILOT_ANFRAGE";
    private final PilotAnfrageAblage ablage;
    private final DrosselungPort drosselung;
    private final PilotAnfrageEreignisPort ereignisPort;
    private final Clock clock;

    public PilotAnfrageService(PilotAnfrageAblage ablage, DrosselungPort drosselung,
            PilotAnfrageEreignisPort ereignisPort, Clock clock) {
        this.ablage = ablage;
        this.drosselung = drosselung;
        this.ereignisPort = ereignisPort;
        this.clock = clock;
    }

    @Transactional
    public boolean speichere(PilotAnfrageEingabe eingabe, String clientAdresse) {
        if (!drossleEingabe(clientAdresse)) {
            return false;
        }

        Instant jetzt = clock.instant();
        PilotAnfrage anfrage = new PilotAnfrage(
                eingabe.rolle(),
                sauber(eingabe.organisationsName()),
                eingabe.bereich(),
                eingabe.sponsorArt(),
                sauber(eingabe.plz()),
                sauber(eingabe.ort()),
                sauber(eingabe.vorname()),
                sauber(eingabe.nachname()),
                sauber(eingabe.email()).toLowerCase(Locale.ROOT),
                sauber(eingabe.paketname()),
                sauber(eingabe.gegenleistungen()),
                sauber(eingabe.mitgliederzahl()),
                sauber(eingabe.naechsterAnlass()),
                sauber(eingabe.interessen()),
                sauber(eingabe.zielgruppe()),
                sauber(eingabe.marktgebiet()),
                sauber(eingabe.programme()),
                sauber(eingabe.uid()),
                sauber(eingabe.funktion()),
                sauber(eingabe.telefon()),
                eingabe.updates(),
                jetzt,
                jetzt);
        Einwilligung einwilligung = new Einwilligung(
                ZWECK, PilotEinwilligungstext.vollstaendig(eingabe.rolle()), jetzt);

        var id = ablage.speichereOderAktualisiere(anfrage, einwilligung, jetzt);
        ereignisPort.publiziere(new PilotAnfrageBenachrichtigungEvent(
                id, anfrage.rolle(), anfrage.bereich(), anfrage.sponsorArt(), anfrage.plz().substring(0, 2)));
        return true;
    }

    public boolean drossleEingabe(String clientAdresse) {
        return drosselung.erfasseEingabe(clientAdresse);
    }

    private String sauber(String wert) {
        return wert == null ? "" : wert.trim();
    }
}
