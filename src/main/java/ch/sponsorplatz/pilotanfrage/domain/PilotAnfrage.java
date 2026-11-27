package ch.sponsorplatz.pilotanfrage.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public record PilotAnfrage(
        PilotAnfrageRolle rolle,
        String organisationsName,
        PilotAnfrageBereich bereich,
        PilotSponsorArt sponsorArt,
        String plz,
        String ort,
        String vorname,
        String nachname,
        String email,
        String paketname,
        String gegenleistungen,
        String mitgliederzahl,
        String naechsterAnlass,
        String interessen,
        String zielgruppe,
        String marktgebiet,
        String programme,
        String uid,
        String funktion,
        String telefon,
        boolean updates,
        Instant eingereichtAm,
        Instant aktualisiertAm) {

    public PilotAnfrage {
        Objects.requireNonNull(rolle);
        Objects.requireNonNull(eingereichtAm);
        Objects.requireNonNull(aktualisiertAm);
    }

    public boolean gleicheIdentitaetWie(PilotAnfrage andere) {
        return rolle == andere.rolle
                && normalisiere(organisationsName).equals(normalisiere(andere.organisationsName))
                && normalisiere(email).equals(normalisiere(andere.email));
    }

    private static String normalisiere(String wert) {
        return Objects.requireNonNullElse(wert, "").trim().toLowerCase(Locale.ROOT);
    }
}
