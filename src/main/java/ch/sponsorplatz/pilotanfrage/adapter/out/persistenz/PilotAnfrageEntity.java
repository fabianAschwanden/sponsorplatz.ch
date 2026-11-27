package ch.sponsorplatz.pilotanfrage.adapter.out.persistenz;

import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageBereich;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrageRolle;
import ch.sponsorplatz.pilotanfrage.domain.PilotSponsorArt;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pilot_anfrage")
public class PilotAnfrageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolle", nullable = false, length = 20)
    private PilotAnfrageRolle rolle;

    @Column(name = "organisations_name", nullable = false, length = 150)
    private String organisationsName;

    @Enumerated(EnumType.STRING)
    @Column(name = "bereich", length = 100)
    private PilotAnfrageBereich bereich;

    @Enumerated(EnumType.STRING)
    @Column(name = "sponsor_art", length = 20)
    private PilotSponsorArt sponsorArt;

    @Column(name = "plz", nullable = false, length = 4)
    private String plz;

    @Column(name = "ort", nullable = false, length = 100)
    private String ort;

    @Column(name = "vorname", nullable = false, length = 80)
    private String vorname;

    @Column(name = "nachname", nullable = false, length = 80)
    private String nachname;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "paketname", length = 120)
    private String paketname;

    @Column(name = "gegenleistungen", length = 1000)
    private String gegenleistungen;

    @Column(name = "mitgliederzahl", length = 20)
    private String mitgliederzahl;

    @Column(name = "naechster_anlass", length = 200)
    private String naechsterAnlass;

    @Column(name = "interessen", length = 1000)
    private String interessen;

    @Column(name = "zielgruppe", length = 200)
    private String zielgruppe;

    @Column(name = "marktgebiet", length = 150)
    private String marktgebiet;

    @Column(name = "programme", length = 300)
    private String programme;

    @Column(name = "uid", length = 20)
    private String uid;

    @Column(name = "funktion", length = 100)
    private String funktion;

    @Column(name = "telefon", length = 40)
    private String telefon;

    @Column(name = "updates", nullable = false)
    private boolean updates;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "NEU";

    @Column(name = "eingereicht_am", nullable = false)
    private Instant eingereichtAm;

    @Column(name = "aktualisiert_am", nullable = false)
    private Instant aktualisiertAm;

    @Column(name = "einwilligung_id")
    private UUID einwilligungId;

    protected PilotAnfrageEntity() {
    }

    public PilotAnfrageEntity(PilotAnfrage anfrage) {
        aktualisiere(anfrage);
        eingereichtAm = anfrage.eingereichtAm();
        aktualisiertAm = anfrage.aktualisiertAm();
    }

    public void aktualisiere(PilotAnfrage anfrage) {
        rolle = anfrage.rolle();
        organisationsName = anfrage.organisationsName();
        bereich = anfrage.bereich();
        sponsorArt = anfrage.sponsorArt();
        plz = anfrage.plz();
        ort = anfrage.ort();
        vorname = anfrage.vorname();
        nachname = anfrage.nachname();
        email = anfrage.email();
        paketname = leerZuNull(anfrage.paketname());
        gegenleistungen = leerZuNull(anfrage.gegenleistungen());
        mitgliederzahl = leerZuNull(anfrage.mitgliederzahl());
        naechsterAnlass = leerZuNull(anfrage.naechsterAnlass());
        interessen = leerZuNull(anfrage.interessen());
        zielgruppe = leerZuNull(anfrage.zielgruppe());
        marktgebiet = leerZuNull(anfrage.marktgebiet());
        programme = leerZuNull(anfrage.programme());
        uid = leerZuNull(anfrage.uid());
        funktion = leerZuNull(anfrage.funktion());
        telefon = leerZuNull(anfrage.telefon());
        updates = anfrage.updates();
        aktualisiertAm = anfrage.aktualisiertAm();
    }

    public UUID getId() { return id; }
    public void setEinwilligungId(UUID einwilligungId) { this.einwilligungId = einwilligungId; }

    private String leerZuNull(String wert) {
        return wert == null || wert.isBlank() ? null : wert;
    }
}
