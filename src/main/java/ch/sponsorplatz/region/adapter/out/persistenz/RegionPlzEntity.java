package ch.sponsorplatz.region.adapter.out.persistenz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "region_plz")
public class RegionPlzEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "region_id", nullable = false, updatable = false)
    private UUID regionId;

    @Column(name = "plz_von", nullable = false)
    private int plzVon;

    @Column(name = "plz_bis", nullable = false)
    private int plzBis;

    protected RegionPlzEntity() {
    }

    RegionPlzEntity(UUID id, UUID regionId, int plzVon, int plzBis) {
        this.id = id;
        this.regionId = regionId;
        this.plzVon = plzVon;
        this.plzBis = plzBis;
    }

    UUID getRegionId() {
        return regionId;
    }

    int getPlzVon() {
        return plzVon;
    }

    int getPlzBis() {
        return plzBis;
    }
}
