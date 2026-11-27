package ch.sponsorplatz.region.adapter.out.persistenz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "region")
public class RegionEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private Instant erstelltAm;

    protected RegionEntity() {
    }

    RegionEntity(UUID id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.erstelltAm = Instant.now();
    }

    UUID getId() {
        return id;
    }

    String getCode() {
        return code;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}
