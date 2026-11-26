package ch.sponsorplatz.organisation;

import ch.sponsorplatz.benutzer.AppUser;
import ch.sponsorplatz.benutzer.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Spec 01, Req 2.1 und 2.3: Die Grundrechte der zentralen Rechteprüfung ({@link AccessControl}) stimmen Zelle für
 * Zelle mit der Tabelle in {@code requirements.md} überein; die Tabelle ist die einzige Quelle. Unbekannter
 * Zellentext, eine fehlende oder zusätzliche Zeile, eine Zeile ohne Nummer {@code G<n>}, eine abweichende Bezeichnung
 * und eine Prüfmethode ohne Zeile machen den Test rot.
 *
 * Vorbild ist {@code RechtematrixSpecTest} des UriNet Archivs. Dort liegt die Matrix als Typ in der Domain; hier
 * prüft der Bestand über die Methoden von {@code AccessControl}, darum ordnet {@link Grundrecht} jede Zeile einer
 * Methode zu.
 */
class RechtematrixSpecTest {

    private static final Path REQUIREMENTS = Path.of(".kiro/specs/01-uebernahme-und-betrieb/requirements.md");
    private static final Pattern ZEILE = Pattern.compile("^\\| (G\\d+) \\|.*");
    private static final Pattern TRENNZEILE = Pattern.compile("^\\|( --- \\|)+$");
    private static final List<String> KOPF = List.of("Nr.", "Grundrecht", "nicht angemeldet", "ohne Mitgliedschaft",
            "`ORG_VIEWER`", "`ORG_EDITOR`", "`ORG_OWNER`", "`PLATFORM_ADMIN`");

    /** Zeilen der Tabelle und die Prüfmethode von {@link AccessControl}, die sie umsetzt. */
    enum Grundrecht {
        G1("Inhalte einer Organisation bearbeiten", "kannOrgEditieren"),
        G2("Organisation verwalten", "kannOrgVerwalten"),
        G3("Private Daten eines Sponsors sehen", "kannSponsorDatenSehen");

        final String bezeichnung;
        final String methode;

        Grundrecht(String bezeichnung, String methode) {
            this.bezeichnung = bezeichnung;
            this.methode = methode;
        }
    }

    /** Spalten der Tabelle in der Reihenfolge von {@link #KOPF}. */
    enum Wer {
        NICHT_ANGEMELDET, OHNE_MITGLIEDSCHAFT, ORG_VIEWER, ORG_EDITOR, ORG_OWNER, PLATFORM_ADMIN
    }

    @Test
    void accessControlEntsprichtDerSpec() throws Exception {
        Map<Grundrecht, Map<Wer, Boolean>> spec = lesen(Files.readAllLines(REQUIREMENTS, StandardCharsets.UTF_8));
        assertThat(spec.keySet()).containsExactlyInAnyOrder(Grundrecht.values());
        for (Map.Entry<Grundrecht, Map<Wer, Boolean>> zeile : spec.entrySet()) {
            for (Map.Entry<Wer, Boolean> zelle : zeile.getValue().entrySet()) {
                assertThat(erlaubt(zeile.getKey(), zelle.getKey()))
                        .as(zeile.getKey() + " " + zelle.getKey()).isEqualTo(zelle.getValue());
            }
        }
    }

    /** Req 2.3: Jede Prüfmethode hat eine Zeile, jede Zeile eine Methode; die Varianten «NachSlug» leiten nur weiter. */
    @Test
    void jedePruefmethodeHatEineZeile() {
        Set<String> methoden = Arrays.stream(AccessControl.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .filter(n -> !n.endsWith("NachSlug"))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> zeilen = Arrays.stream(Grundrecht.values()).map(g -> g.methode)
                .collect(Collectors.toCollection(TreeSet::new));
        assertThat(methoden).as("öffentliche Prüfmethoden von AccessControl").isEqualTo(zeilen);
    }

    @Test
    void unbekannterZellentextIstRot() {
        assertThatThrownBy(() -> lesen(List.of(kopf(),
                "| G1 | Inhalte einer Organisation bearbeiten | verweigert | verweigert | vielleicht | erlaubt | erlaubt | erlaubt |")))
                .hasMessageContaining("vielleicht");
    }

    @Test
    void zusaetzlicheZeileIstRot() {
        assertThatThrownBy(() -> lesen(List.of(kopf(),
                "| G99 | Neu | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt |")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeileOhneNummerIstRot() {
        for (String zeile : List.of("| B1 | Neu | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt |",
                "|G4| Neu | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt | erlaubt |")) {
            assertThatThrownBy(() -> lesen(List.of(kopf(), "| --- | --- | --- | --- | --- | --- | --- | --- |", zeile)))
                    .as(zeile).hasMessageContaining("ohne Nummer");
        }
    }

    @Test
    void abweichendeBezeichnungIstRot() {
        assertThatThrownBy(() -> lesen(List.of(kopf(),
                "| G2 | Organisation löschen | verweigert | verweigert | verweigert | verweigert | erlaubt | erlaubt |")))
                .hasMessageContaining("Organisation verwalten");
    }

    @Test
    void tabelleDarfEingeruecktSein() {
        Map<Grundrecht, Map<Wer, Boolean>> matrix = lesen(List.of("   " + kopf(),
                "   | --- | --- | --- | --- | --- | --- | --- | --- |",
                "   | G2 | Organisation verwalten | verweigert | verweigert | verweigert | verweigert | erlaubt | erlaubt |"));
        assertThat(matrix.get(Grundrecht.G2)).containsEntry(Wer.ORG_EDITOR, false).containsEntry(Wer.ORG_OWNER, true);
    }

    private static String kopf() {
        return "| " + String.join(" | ", KOPF) + " |";
    }

    /**
     * Liest die Zeilen G1 … der ersten Tabelle mit dem Kopf von Req 2.1 («Nr.», «Grundrecht», …); andere Tabellen mit
     * einer Spalte «Nr.» (z. B. die offenen Entscheide) bleiben aussen vor. Die Tabelle darf eingerückt sein.
     */
    static Map<Grundrecht, Map<Wer, Boolean>> lesen(List<String> zeilen) {
        Map<Grundrecht, Map<Wer, Boolean>> matrix = new LinkedHashMap<>();
        boolean inTabelle = false;
        for (String roh : zeilen) {
            String zeile = roh.strip();
            if (zeile.startsWith("| Nr. | Grundrecht |")) {
                assertThat(zellen(zeile)).containsExactlyElementsOf(KOPF);
                inTabelle = matrix.isEmpty();
                continue;
            }
            if (!zeile.startsWith("|")) {
                inTabelle = false;
                continue;
            }
            if (!inTabelle || TRENNZEILE.matcher(zeile).matches()) {
                continue;
            }
            if (!ZEILE.matcher(zeile).matches()) {
                throw new IllegalArgumentException("Zeile der Rechtematrix ohne Nummer G<n>: " + zeile);
            }
            List<String> z = zellen(zeile);
            assertThat(z).as(zeile).hasSize(KOPF.size());
            Grundrecht recht = Grundrecht.valueOf(z.getFirst());
            assertThat(z.get(1)).as("Grundrecht " + recht).isEqualTo(recht.bezeichnung);
            Map<Wer, Boolean> ergebnisse = new EnumMap<>(Wer.class);
            Wer[] wer = Wer.values();
            for (int i = 0; i < wer.length; i++) {
                ergebnisse.put(wer[i], zelle(z.get(2 + i)));
            }
            assertThat(matrix.put(recht, ergebnisse)).as("doppelte Zeile " + recht).isNull();
        }
        return matrix;
    }

    private static boolean zelle(String text) {
        return switch (text) {
            case "erlaubt" -> true;
            case "verweigert" -> false;
            default -> throw new IllegalArgumentException("Unbekannter Zellentext der Rechtematrix: " + text);
        };
    }

    private static List<String> zellen(String zeile) {
        String innen = zeile.substring(1, zeile.length() - 1);
        return Arrays.stream(innen.split("\\|")).map(String::strip).toList();
    }

    /** Ruft die Prüfmethode der Zeile für die Person der Spalte auf, mit Attrappen für die Repositories. */
    private static boolean erlaubt(Grundrecht recht, Wer wer) throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MitgliedschaftRepository mitgliedschaften = mock(MitgliedschaftRepository.class);
        AppUserRepository benutzer = mock(AppUserRepository.class);
        AccessControl accessControl = new AccessControl(mitgliedschaften, benutzer, mock(OrganisationRepository.class));

        Authentication auth = null;
        if (wer != Wer.NICHT_ANGEMELDET) {
            auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("person@muster.test");
            List<SimpleGrantedAuthority> rechte = wer == Wer.PLATFORM_ADMIN
                    ? List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")) : List.of();
            when(auth.getAuthorities()).thenAnswer(inv -> rechte);
            AppUser person = new AppUser();
            person.setId(userId);
            when(benutzer.findByEmail("person@muster.test")).thenReturn(Optional.of(person));
            // Die Datenbank zählt Mitgliedschaften der Person mit einer der gefragten Rollen (auch über Eltern-Organisationen)
            when(mitgliedschaften.zaehleMitgliedschaftenInHierarchie(eq(userId), eq(orgId), any()))
                    .thenAnswer(inv -> {
                        Collection<String> gefragt = inv.getArgument(2);
                        return gefragt.contains(wer.name()) ? 1L : 0L;
                    });
        }
        Method methode = AccessControl.class.getMethod(recht.methode, UUID.class, Authentication.class);
        return (boolean) methode.invoke(accessControl, orgId, auth);
    }
}
