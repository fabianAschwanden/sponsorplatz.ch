package ch.sponsorplatz.pilotanfrage.adapter.in.web;

import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageEingabe;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PilotAnfrageFormDto {

    private static final Set<String> GEGENLEISTUNGS_CODES = Set.of(
            "stand", "newsletter", "schnuppertraining", "qr-wettbewerb", "kurzauftritt",
            "mitgliedervorteil", "trikot", "bande", "website", "mitgliederversammlung");
    private static final Set<String> INTERESSEN_CODES = Set.of(
            "standaktionen", "qr-kampagnen", "newsletter", "sichtbarkeit", "vorteile", "gemeinsame-pakete");

    @NotNull(message = "{pilot.validierung.rolle}")
    private PilotAnfrageRolle rolle = PilotAnfrageRolle.VEREIN;

    @NotBlank(message = "{pilot.validierung.organisation}")
    @Size(max = 150, message = "{pilot.validierung.laenge}")
    private String organisationsName;

    private PilotAnfrageBereich bereich;
    private PilotSponsorArt sponsorArt;

    @NotBlank(message = "{pilot.validierung.plz}")
    @Pattern(regexp = "[1-9][0-9]{3}", message = "{pilot.validierung.plz}")
    private String plz;

    @NotBlank(message = "{pilot.validierung.ort}")
    @Size(max = 100, message = "{pilot.validierung.laenge}")
    private String ort;

    @NotBlank(message = "{pilot.validierung.vorname}")
    @Size(max = 80, message = "{pilot.validierung.laenge}")
    private String vorname;

    @NotBlank(message = "{pilot.validierung.nachname}")
    @Size(max = 80, message = "{pilot.validierung.laenge}")
    private String nachname;

    @NotBlank(message = "{pilot.validierung.email}")
    @Email(message = "{pilot.validierung.email}")
    @Size(max = 254, message = "{pilot.validierung.laenge}")
    private String email;

    @Size(max = 120, message = "{pilot.validierung.laenge}")
    private String paketname;

    @Size(max = 10, message = "{pilot.validierung.auswahl}")
    private List<String> gegenleistungen = new ArrayList<>();

    @Pattern(regexp = "|[1-9][0-9]{0,6}", message = "{pilot.validierung.mitgliederzahl}")
    private String mitgliederzahl;

    @Size(max = 200, message = "{pilot.validierung.laenge}")
    private String naechsterAnlass;

    @Size(max = 6, message = "{pilot.validierung.auswahl}")
    private List<String> interessen = new ArrayList<>();

    @Size(max = 200, message = "{pilot.validierung.laenge}")
    private String zielgruppe;

    @Size(max = 150, message = "{pilot.validierung.laenge}")
    private String marktgebiet;

    @Size(max = 300, message = "{pilot.validierung.laenge}")
    private String programme;

    @Size(max = 20, message = "{pilot.validierung.laenge}")
    private String uid;

    @Size(max = 100, message = "{pilot.validierung.laenge}")
    private String funktion;

    @Size(max = 40, message = "{pilot.validierung.laenge}")
    private String telefon;

    private boolean einwilligung;
    private boolean updates;

    @AssertTrue(message = "{pilot.validierung.bereich}")
    public boolean isBereichOderSponsorArtGueltig() {
        return rolle == PilotAnfrageRolle.VEREIN
                ? bereich != null && sponsorArt == null
                : rolle == PilotAnfrageRolle.SPONSOR && sponsorArt != null && bereich == null;
    }

    @AssertTrue(message = "{pilot.validierung.einwilligung}")
    public boolean isEinwilligungBestaetigt() {
        return einwilligung;
    }

    @AssertTrue(message = "{pilot.validierung.auswahl}")
    public boolean isAuswahlenGueltig() {
        return gegenleistungen != null && interessen != null
                && gegenleistungen.stream().allMatch(GEGENLEISTUNGS_CODES::contains)
                && interessen.stream().allMatch(INTERESSEN_CODES::contains);
    }

    public PilotAnfrageEingabe zuEingabe() {
        return new PilotAnfrageEingabe(
                rolle,
                organisationsName,
                bereich,
                sponsorArt,
                plz,
                ort,
                vorname,
                nachname,
                email,
                paketname,
                verbinde(gegenleistungen),
                mitgliederzahl,
                naechsterAnlass,
                verbinde(interessen),
                zielgruppe,
                marktgebiet,
                programme,
                uid,
                funktion,
                telefon,
                updates);
    }

    private String verbinde(List<String> werte) {
        return werte == null ? "" : werte.stream().collect(Collectors.joining(", "));
    }

    public PilotAnfrageRolle getRolle() { return rolle; }
    public void setRolle(PilotAnfrageRolle rolle) { this.rolle = rolle; }
    public String getOrganisationsName() { return organisationsName; }
    public void setOrganisationsName(String organisationsName) { this.organisationsName = organisationsName; }
    public PilotAnfrageBereich getBereich() { return bereich; }
    public void setBereich(PilotAnfrageBereich bereich) { this.bereich = bereich; }
    public PilotSponsorArt getSponsorArt() { return sponsorArt; }
    public void setSponsorArt(PilotSponsorArt sponsorArt) { this.sponsorArt = sponsorArt; }
    public String getPlz() { return plz; }
    public void setPlz(String plz) { this.plz = plz; }
    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }
    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }
    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPaketname() { return paketname; }
    public void setPaketname(String paketname) { this.paketname = paketname; }
    public List<String> getGegenleistungen() { return gegenleistungen; }
    public void setGegenleistungen(List<String> gegenleistungen) { this.gegenleistungen = gegenleistungen; }
    public String getMitgliederzahl() { return mitgliederzahl; }
    public void setMitgliederzahl(String mitgliederzahl) { this.mitgliederzahl = mitgliederzahl; }
    public String getNaechsterAnlass() { return naechsterAnlass; }
    public void setNaechsterAnlass(String naechsterAnlass) { this.naechsterAnlass = naechsterAnlass; }
    public List<String> getInteressen() { return interessen; }
    public void setInteressen(List<String> interessen) { this.interessen = interessen; }
    public String getZielgruppe() { return zielgruppe; }
    public void setZielgruppe(String zielgruppe) { this.zielgruppe = zielgruppe; }
    public String getMarktgebiet() { return marktgebiet; }
    public void setMarktgebiet(String marktgebiet) { this.marktgebiet = marktgebiet; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFunktion() { return funktion; }
    public void setFunktion(String funktion) { this.funktion = funktion; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public boolean isEinwilligung() { return einwilligung; }
    public void setEinwilligung(boolean einwilligung) { this.einwilligung = einwilligung; }
    public boolean isUpdates() { return updates; }
    public void setUpdates(boolean updates) { this.updates = updates; }
}
