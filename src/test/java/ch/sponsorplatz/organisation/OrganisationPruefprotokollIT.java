package ch.sponsorplatz.organisation;

import ch.sponsorplatz.audit.AuditLog;
import ch.sponsorplatz.audit.AuditLogRepository;
import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.benutzer.AppUserRepository;
import ch.sponsorplatz.shared.mail.MailVersand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "spring.datasource.url=jdbc:h2:mem:organisation-pruefprotokoll;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("dev")
class OrganisationPruefprotokollIT {

    @Autowired private OrganisationService organisationService;
    @Autowired private OrganisationRepository organisationRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private MitgliedschaftRepository mitgliedschaftRepository;

    @MockitoBean private AuditLogRepository auditLogRepository;
    @MockitoBean private MailVersand mailVersand;

    @Test
    void fehlgeschriebenesPruefprotokollRolltEntscheidZurueckUndSendetKeineMail() {
        UUID kennung = UUID.randomUUID();
        AppUser kontaktperson = new AppUser();
        kontaktperson.setEmail("kontakt-" + kennung + "@example.test");
        kontaktperson.setPasswortHash("nicht-verwendet");
        kontaktperson.setAnzeigename("Testkontakt");
        appUserRepository.saveAndFlush(kontaktperson);

        Organisation organisation = new Organisation();
        organisation.setName("Beispielorganisation");
        organisation.setSlug("pruefprotokoll-" + kennung);
        organisation.setTyp(OrgTyp.VEREIN);
        organisation.setBranche(Branche.SPORT);
        organisation.setStatus(OrgStatus.PENDING);
        organisationRepository.saveAndFlush(organisation);

        Mitgliedschaft mitgliedschaft = new Mitgliedschaft();
        mitgliedschaft.setUser(kontaktperson);
        mitgliedschaft.setOrg(organisation);
        mitgliedschaft.setRolle(Rolle.ORG_OWNER);
        mitgliedschaftRepository.saveAndFlush(mitgliedschaft);

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new DataIntegrityViolationException("Protokolleintrag kann nicht gespeichert werden."));

        assertThatThrownBy(() -> organisationService.verifiziere(
                organisation.getId(), new Pruefpunkte(true, true, true), UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(organisationRepository.findById(organisation.getId()))
                .get()
                .extracting(Organisation::getStatus)
                .isEqualTo(OrgStatus.PENDING);
        verifyNoInteractions(mailVersand);
    }
}
