package ch.sponsorplatz.pilotanfrage.adapter.out.persistenz;

import ch.sponsorplatz.pilotanfrage.application.PilotAnfrageAblage;
import ch.sponsorplatz.pilotanfrage.domain.Einwilligung;
import ch.sponsorplatz.pilotanfrage.domain.PilotAnfrage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public class PilotAnfrageJpaAdapter implements PilotAnfrageAblage {

    private final PilotAnfrageRepository anfragen;
    private final EinwilligungRepository einwilligungen;

    public PilotAnfrageJpaAdapter(PilotAnfrageRepository anfragen, EinwilligungRepository einwilligungen) {
        this.anfragen = anfragen;
        this.einwilligungen = einwilligungen;
    }

    @Override
    @Transactional
    public long speichereOderAktualisiere(PilotAnfrage anfrage, Einwilligung einwilligung, Instant jetzt) {
        PilotAnfrageEntity entity = anfragen
                .findFirstByEmailIgnoreCaseAndOrganisationsNameIgnoreCaseAndRolleAndAktualisiertAmAfterOrderByAktualisiertAmDesc(
                        anfrage.email(), anfrage.organisationsName(), anfrage.rolle(), jetzt.minusSeconds(600))
                .orElseGet(() -> anfragen.saveAndFlush(new PilotAnfrageEntity(anfrage)));
        entity.aktualisiere(anfrage);
        anfragen.saveAndFlush(entity);

        EinwilligungEntity gespeicherteEinwilligung = einwilligungen.saveAndFlush(
                new EinwilligungEntity(entity.getId(), einwilligung));
        entity.setEinwilligungId(gespeicherteEinwilligung.getId());
        anfragen.saveAndFlush(entity);
        return entity.getId();
    }
}
