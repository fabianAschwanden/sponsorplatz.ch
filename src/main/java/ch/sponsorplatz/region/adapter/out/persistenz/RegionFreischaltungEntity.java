package ch.sponsorplatz.region.adapter.out.persistenz;

import ch.sponsorplatz.region.domain.Stufe;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "region_freischaltung")
@IdClass(RegionFreischaltungEntity.Schluessel.class)
public class RegionFreischaltungEntity {
    @Id
    @Column(name = "region_id", nullable = false, updatable = false)
    private UUID regionId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "stufe", nullable = false, updatable = false, length = 40)
    private Stufe stufe;

    @Column(name = "freigeschaltet_am", nullable = false, updatable = false)
    private Instant freigeschaltetAm;

    @Column(name = "freigeschaltet_von")
    private UUID freigeschaltetVon;

    protected RegionFreischaltungEntity() {
    }

    RegionFreischaltungEntity(UUID regionId, Stufe stufe, Instant freigeschaltetAm, UUID freigeschaltetVon) {
        this.regionId = regionId;
        this.stufe = stufe;
        this.freigeschaltetAm = freigeschaltetAm;
        this.freigeschaltetVon = freigeschaltetVon;
    }

    Stufe getStufe() {
        return stufe;
    }

    public static class Schluessel implements Serializable {
        private UUID regionId;
        private Stufe stufe;

        public Schluessel() {
        }

        Schluessel(UUID regionId, Stufe stufe) {
            this.regionId = regionId;
            this.stufe = stufe;
        }

        @Override
        public boolean equals(Object anderer) {
            if (this == anderer) {
                return true;
            }
            if (!(anderer instanceof Schluessel schluessel)) {
                return false;
            }
            return Objects.equals(regionId, schluessel.regionId) && stufe == schluessel.stufe;
        }

        @Override
        public int hashCode() {
            return Objects.hash(regionId, stufe);
        }
    }
}
