package ch.sponsorplatz.pilotanfrage.adapter.out.persistenz;

import ch.sponsorplatz.pilotanfrage.domain.Einwilligung;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "einwilligung")
public class EinwilligungEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bezug_art", nullable = false, length = 40)
    private String bezugArt;

    @Column(name = "bezug_id", nullable = false)
    private UUID bezugId;

    @Column(name = "zweck", nullable = false, length = 50)
    private String zweck;

    @Column(name = "wortlaut", nullable = false, columnDefinition = "TEXT")
    private String wortlaut;

    @Column(name = "zeitpunkt", nullable = false)
    private java.time.Instant zeitpunkt;

    protected EinwilligungEntity() {
    }

    public EinwilligungEntity(UUID anfrageId, Einwilligung einwilligung) {
        bezugArt = "PILOT_ANFRAGE";
        bezugId = anfrageId;
        zweck = einwilligung.zweck();
        wortlaut = einwilligung.wortlaut();
        zeitpunkt = einwilligung.zeitpunkt();
    }

    public UUID getId() { return id; }
}
