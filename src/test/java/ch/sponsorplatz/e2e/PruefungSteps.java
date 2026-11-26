package ch.sponsorplatz.e2e;

import ch.sponsorplatz.admin.AdminVerifizierungController;
import ch.sponsorplatz.audit.AuditAktion;
import ch.sponsorplatz.audit.AuditLog;
import ch.sponsorplatz.audit.AuditLogRepository;
import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.organisation.Branche;
import ch.sponsorplatz.organisation.MitgliedschaftService;
import ch.sponsorplatz.organisation.OrgStatus;
import ch.sponsorplatz.organisation.OrgTyp;
import ch.sponsorplatz.organisation.Organisation;
import ch.sponsorplatz.organisation.OrganisationRepository;
import ch.sponsorplatz.organisation.OrganisationService;
import ch.sponsorplatz.organisation.Pruefpunkte;
import ch.sponsorplatz.organisation.Rolle;
import ch.sponsorplatz.projekt.Projekt;
import ch.sponsorplatz.projekt.ProjektRepository;
import ch.sponsorplatz.projekt.SponsoringPaket;
import ch.sponsorplatz.projekt.SponsoringPaketRepository;
import ch.sponsorplatz.projekt.Sichtbarkeit;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PruefungSteps {

    private static final String ABLEHNUNGSGRUND = "Angaben nicht plausibel";

    @Autowired private E2EContext ctx;
    @Autowired private E2EFixtures fixtures;
    @Autowired private OrganisationRepository organisationRepository;
    @Autowired private OrganisationService organisationService;
    @Autowired private MitgliedschaftService mitgliedschaftService;
    @Autowired private ProjektRepository projektRepository;
    @Autowired private SponsoringPaketRepository paketRepository;
    @Autowired private AdminVerifizierungController adminVerifizierungController;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private E2EMailCaptureConfig.CapturingMailVersand mailVersand;

    @Angenommen("eine ausstehende Organisation für die Prüfung")
    @Transactional
    public void ausstehendeOrganisationErstellen() {
        AppUser kontaktperson = fixtures.appUserRepository()
                .findByEmail(E2EFixtures.SPONSOR_EMAIL)
                .orElseThrow();
        Organisation org = neueOrganisation("Prüfverein");
        organisationRepository.saveAndFlush(org);
        mitgliedschaftService.fuegeHinzu(org.getId(), kontaktperson.getId(), Rolle.ORG_OWNER, null);
        ctx.getDaten().put("pruefOrgId", org.getId());
        ctx.getDaten().put("pruefOrgSlug", org.getSlug());
        ctx.getDaten().put("pruefKontaktId", kontaktperson.getId());
    }

    @Angenommen("eine ausstehende Organisation mit einem öffentlichen Projekt")
    @Transactional
    public void ausstehendeOrganisationMitProjektErstellen() {
        ausstehendeOrganisationErstellen();
        Organisation org = organisationRepository.findById(ctx.daten("pruefOrgId")).orElseThrow();
        Projekt projekt = new Projekt();
        projekt.setName("Beispielprojekt in Prüfung");
        projekt.setSlug("pruefprojekt-" + UUID.randomUUID().toString().substring(0, 8));
        projekt.setOrg(org);
        projekt.setSichtbarkeit(Sichtbarkeit.OEFFENTLICH);
        projekt.setVeroeffentlichtAm(java.time.Instant.now());
        projektRepository.saveAndFlush(projekt);
        SponsoringPaket paket = new SponsoringPaket();
        paket.setProjekt(projekt);
        paket.setName("Beispielpaket in Prüfung");
        paketRepository.saveAndFlush(paket);
        ctx.getDaten().put("pruefProjektSlug", projekt.getSlug());
        ctx.getDaten().put("pruefPaketName", paket.getName());
    }

    @Angenommen("eine ausstehende Organisation ohne UID")
    @Transactional
    public void ausstehendeOrganisationOhneUidErstellen() {
        ausstehendeOrganisationErstellen();
    }

    @Wenn("die Verwaltung alle drei Prüfpunkte bestätigt")
    public void pruefentscheidErteilen() {
        entscheide("VERIFIZIEREN", null);
    }

    @Wenn("die Verwaltung sie mit einem Ablehnungsgrund ablehnt")
    public void ablehnungsentscheidErteilen() {
        mailVersand.leere();
        entscheide("ABLEHNEN", ABLEHNUNGSGRUND);
    }

    @Wenn("ich die öffentlichen Listen und Projektdetails öffne")
    public void oeffentlicheAusgabenOeffnen() {
        Page page = ctx.getPage();
        Response liste = page.navigate(ctx.getBaseUrl() + "/organisationen");
        assertThat(liste.status()).isEqualTo(200);
        assertThat(page.content()).doesNotContain("Prüfverein");

        Response projekt = page.navigate(ctx.getBaseUrl() + "/marktplatz/"
                + ctx.<String>daten("pruefProjektSlug"));
        assertThat(projekt.status()).isIn(200, 404);
        assertThat(page.content())
                .doesNotContain("Beispielprojekt in Prüfung", ctx.<String>daten("pruefPaketName"));

        Response sitemap = page.navigate(ctx.getBaseUrl() + "/sitemap.xml");
        assertThat(sitemap.status()).isEqualTo(200);
        assertThat(page.content())
                .doesNotContain(ctx.<String>daten("pruefOrgSlug"))
                .doesNotContain(ctx.<String>daten("pruefProjektSlug"));
    }

    @Dann("enthält das Protokoll alle drei Prüfpunkte")
    public void protokollEnthaeltPruefpunkte() {
        AuditLog log = warteAufPruefprotokoll();
        assertThat(log.getDetails())
                .contains("Sport/Gesundheit: Ja", "Bezug zur Schweiz: Ja", "Angaben plausibel: Ja");
    }

    @Dann("sind die Organisation und das Projekt nicht sichtbar")
    public void ausgabenBleibenVerborgen() {
        Response ogBild = ctx.getPage().navigate(ctx.getBaseUrl() + "/og/verein/"
                + ctx.<String>daten("pruefOrgSlug") + ".png");
        assertThat(ogBild.status()).isEqualTo(404);
        Response projektBild = ctx.getPage().navigate(ctx.getBaseUrl() + "/og/projekt/"
                + ctx.<String>daten("pruefProjektSlug") + ".png");
        assertThat(projektBild.status()).isEqualTo(404);
    }

    @Dann("erhält die Kontaktperson den Ablehnungsgrund per E-Mail")
    public void ablehnungsmailEnthaeltGrund() {
        assertThat(organisationService.findeNachId(ctx.daten("pruefOrgId")))
                .get().extracting(Organisation::getStatus).isEqualTo(OrgStatus.SUSPENDED);
        assertThat(mailVersand.nachrichten())
                .anySatisfy(nachricht -> {
                    assertThat(nachricht.empfaenger()).isEqualTo("sponsor@muster.test");
                    assertThat(nachricht.inhalt()).contains(ABLEHNUNGSGRUND);
                });
    }

    @Dann("ist die Organisation ohne UID gespeichert")
    public void uidIstOptional() {
        assertThat(organisationRepository.findById(ctx.daten("pruefOrgId")))
                .get().extracting(Organisation::getZefixUid).isNull();
    }

    @Dann("enthält das Protokoll die entscheidende Person und den Zeitpunkt")
    public void protokollEnthaeltPersonUndZeitpunkt() {
        AuditLog log = warteAufPruefprotokoll();
        assertThat(log.getBenutzerId()).isEqualTo(ctx.<UUID>daten("pruefKontaktId"));
        assertThat(log.getZeitpunkt()).isNotNull();
        assertThat(log.getAktion()).isEqualTo(AuditAktion.VERIFIZIERT);
    }

    @Dann("erhält die Kontaktperson das Ergebnis per E-Mail")
    public void freigabemailEnthaeltErgebnis() {
        assertThat(mailVersand.nachrichten())
                .anySatisfy(nachricht -> {
                    assertThat(nachricht.empfaenger()).isEqualTo("sponsor@muster.test");
                    assertThat(nachricht.inhalt()).contains("freigegeben");
                });
    }

    private Organisation neueOrganisation(String name) {
        Organisation org = new Organisation();
        org.setName(name);
        org.setSlug("pruefverein-" + UUID.randomUUID().toString().substring(0, 8));
        org.setTyp(OrgTyp.VEREIN);
        org.setBranche(Branche.SPORT);
        org.setStatus(OrgStatus.PENDING);
        return org;
    }

    private void entscheide(String entscheid, String grund) {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                E2EFixtures.SPONSOR_EMAIL, "test", "ROLE_PLATFORM_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            adminVerifizierungController.entscheide(
                    ctx.daten("pruefOrgId"), entscheid, true, true, true, grund, auth);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private AuditLog warteAufPruefprotokoll() {
        UUID orgId = ctx.daten("pruefOrgId");
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(auditLogRepository.findByBereichOrderByZeitpunktDesc("ORGANISATION"))
                    .anySatisfy(log -> {
                        assertThat(log.getZielId()).isEqualTo(orgId);
                        assertThat(log.getAktion()).isEqualTo(AuditAktion.VERIFIZIERT);
                    });
        });
        return auditLogRepository.findByBereichOrderByZeitpunktDesc("ORGANISATION").stream()
                .filter(log -> orgId.equals(log.getZielId()))
                .findFirst()
                .orElseThrow();
    }
}
