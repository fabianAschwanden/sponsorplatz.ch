package ch.sponsorplatz.region.adapter.out.persistenz;

import ch.sponsorplatz.region.application.RegionDaten;
import ch.sponsorplatz.region.application.RegionSpeicherPort;
import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Stufe;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class RegionJpaAdapter implements RegionSpeicherPort {
    private final RegionJpaRepository regionen;
    private final RegionPlzJpaRepository plzBereiche;
    private final RegionFreischaltungJpaRepository freischaltungen;

    public RegionJpaAdapter(RegionJpaRepository regionen, RegionPlzJpaRepository plzBereiche,
                            RegionFreischaltungJpaRepository freischaltungen) {
        this.regionen = regionen;
        this.plzBereiche = plzBereiche;
        this.freischaltungen = freischaltungen;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDaten> alleRegionen() {
        return regionen.findAll().stream().map(this::abbilden).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<RegionDaten> findeRegion(UUID id) {
        return regionen.findById(id).map(this::abbilden);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existiertCode(String code) {
        return regionen.existsByCode(code);
    }

    @Override
    public RegionDaten speichereRegion(RegionDaten daten) {
        RegionEntity region = regionen.findById(daten.id())
                .orElseGet(() -> new RegionEntity(daten.id(), daten.code(), daten.name()));
        region.setName(daten.name());
        RegionEntity gespeichert = regionen.save(region);
        return abbilden(gespeichert);
    }

    @Override
    @Transactional
    public void speicherePlzBereiche(UUID regionId, List<PlzBereich> bereiche) {
        plzBereiche.deleteAllByRegionId(regionId);
        plzBereiche.saveAll(bereiche.stream()
                .map(bereich -> new RegionPlzEntity(UUID.randomUUID(), regionId, bereich.von(), bereich.bis()))
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hatUeberschneidendePlzBereiche(UUID regionId, PlzBereich bereich) {
        return plzBereiche.findAll().stream()
                .filter(zuordnung -> !zuordnung.getRegionId().equals(regionId))
                .anyMatch(zuordnung -> bereich.ueberschneidet(
                        new PlzBereich(zuordnung.getPlzVon(), zuordnung.getPlzBis())));
    }

    @Override
    @Transactional
    public void schalteStufeEin(UUID regionId, Stufe stufe, Instant zeitpunkt, UUID benutzerId) {
        RegionFreischaltungEntity.Schluessel schluessel =
                new RegionFreischaltungEntity.Schluessel(regionId, stufe);
        if (!freischaltungen.existsById(schluessel)) {
            freischaltungen.save(new RegionFreischaltungEntity(regionId, stufe, zeitpunkt, benutzerId));
        }
    }

    @Override
    @Transactional
    public void schalteStufeAus(UUID regionId, Stufe stufe) {
        freischaltungen.deleteByRegionIdAndStufe(regionId, stufe);
    }

    private RegionDaten abbilden(RegionEntity region) {
        List<PlzBereich> bereiche = plzBereiche.findAllByRegionId(region.getId()).stream()
                .map(zuordnung -> new PlzBereich(zuordnung.getPlzVon(), zuordnung.getPlzBis()))
                .toList();
        Set<Stufe> stufen = freischaltungen.findAllByRegionId(region.getId()).stream()
                .map(RegionFreischaltungEntity::getStufe)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new RegionDaten(region.getId(), region.getCode(), region.getName(), bereiche, stufen);
    }

}
