package ch.sponsorplatz.region.application;

import ch.sponsorplatz.region.domain.PlzBereich;
import ch.sponsorplatz.region.domain.Region;
import ch.sponsorplatz.region.domain.Stufe;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RegionService implements RegionVerwaltung, StufenPruefung {
    private final RegionSpeicherPort speicher;
    private final RegionAdminRechtePort rechte;
    private final RegionEreignisPort ereignisse;
    private final Clock clock;

    public RegionService(RegionSpeicherPort speicher, RegionAdminRechtePort rechte,
                         RegionEreignisPort ereignisse, Clock clock) {
        this.speicher = speicher;
        this.rechte = rechte;
        this.ereignisse = ereignisse;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionView> alleRegionen(UUID benutzerId) {
        pruefeAdmin(benutzerId);
        return speicher.alleRegionen().stream().map(RegionView::von).toList();
    }

    @Override
    @Transactional
    public UUID legeRegionAn(String code, String name, UUID benutzerId) {
        pruefeAdmin(benutzerId);
        Region region = new Region(UUID.randomUUID(), code, name);
        if (speicher.existiertCode(region.code())) {
            throw new IllegalArgumentException("Der Regionscode ist bereits vergeben.");
        }
        speicher.speichereRegion(new RegionDaten(region.id(), region.code(), region.name(), List.of(), Set.of()));
        ereignisse.protokolliere(new RegionAenderungsEreignis(
                region.id(), benutzerId, RegionAenderungsart.ANGELEGT, region.code()));
        return region.id();
    }

    @Override
    @Transactional
    public void benenneRegionUm(UUID regionId, String name, UUID benutzerId) {
        pruefeAdmin(benutzerId);
        RegionDaten vorhanden = findeRegion(regionId);
        Region region = new Region(vorhanden.id(), vorhanden.code(), name);
        speicher.speichereRegion(new RegionDaten(region.id(), region.code(), region.name(),
                vorhanden.plzBereiche(), vorhanden.freigeschalteteStufen()));
        ereignisse.protokolliere(new RegionAenderungsEreignis(
                region.id(), benutzerId, RegionAenderungsart.UMBENANNT, region.name()));
    }

    @Override
    @Transactional
    public void setzePlzBereiche(UUID regionId, String bereiche, UUID benutzerId) {
        pruefeAdmin(benutzerId);
        RegionDaten region = findeRegion(regionId);
        List<PlzBereich> plzBereiche = parseBereiche(bereiche);
        for (PlzBereich bereich : plzBereiche) {
            if (speicher.hatUeberschneidendePlzBereiche(regionId, bereich)) {
                throw new IllegalArgumentException("Der PLZ-Bereich ist bereits einer anderen Region zugeordnet.");
            }
        }
        speicher.speicherePlzBereiche(regionId, plzBereiche);
        ereignisse.protokolliere(new RegionAenderungsEreignis(
                region.id(), benutzerId, RegionAenderungsart.PLZ_BEREICHE_GEAENDERT,
                bereiche == null ? "" : bereiche.strip()));
    }

    @Override
    @Transactional
    public void schalteStufe(UUID regionId, Stufe stufe, boolean freigeschaltet, UUID benutzerId) {
        pruefeAdmin(benutzerId);
        RegionDaten region = findeRegion(regionId);
        if (freigeschaltet) {
            speicher.schalteStufeEin(regionId, stufe, clock.instant(), benutzerId);
        } else {
            speicher.schalteStufeAus(regionId, stufe);
        }
        RegionAenderungsart art = freigeschaltet
                ? RegionAenderungsart.STUFE_FREIGESCHALTET
                : RegionAenderungsart.STUFE_ABGESCHALTET;
        ereignisse.protokolliere(new RegionAenderungsEreignis(
                region.id(), benutzerId, art, Integer.toString(stufe.nummer())));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean istFreigeschaltet(Stufe stufe, String plz) {
        if (stufe == null || plz == null || !plz.matches("[1-9][0-9]{3}")) {
            return false;
        }
        int postleitzahl = Integer.parseInt(plz);
        return speicher.alleRegionen().stream()
                .filter(region -> region.plzBereiche().stream().anyMatch(bereich -> bereich.enthaelt(postleitzahl)))
                .anyMatch(region -> region.freigeschalteteStufen().contains(stufe));
    }

    private RegionDaten findeRegion(UUID regionId) {
        return speicher.findeRegion(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Die Region wurde nicht gefunden."));
    }

    private void pruefeAdmin(UUID benutzerId) {
        if (benutzerId == null || !rechte.istPlattformAdmin(benutzerId)) {
            throw new RegionZugriffVerweigertException();
        }
    }

    private List<PlzBereich> parseBereiche(String eingabe) {
        if (eingabe == null || eingabe.isBlank()) {
            return List.of();
        }
        List<PlzBereich> bereiche = new ArrayList<>();
        for (String zeile : eingabe.split("[,\\n]")) {
            String[] grenzen = zeile.strip().split("-", -1);
            if (grenzen.length != 2 || !grenzen[0].matches("[0-9]{4}") || !grenzen[1].matches("[0-9]{4}")) {
                throw new IllegalArgumentException("PLZ-Bereiche bitte im Format 1000-1999 eingeben.");
            }
            bereiche.add(new PlzBereich(Integer.parseInt(grenzen[0]), Integer.parseInt(grenzen[1])));
        }
        return List.copyOf(bereiche);
    }
}
