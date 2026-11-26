package ch.sponsorplatz.shared.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prüft {@code deploy/infomaniak/compose.yml} gegen die Regeln des Betriebs,
 * bevor ein Deploy sie dem Server vorlegt.
 *
 * <p>Der Server prüft die Datei selbst gegen seine Policy und lehnt Verstösse
 * ab. Dieser Test fängt sie früher ab und prüft zusätzlich, was die Policy nicht
 * kennt: das Speicherbudget, die Zuordnung der Router und dass der
 * Vorschau-Schutz der Normalfall ist.
 */
class DeployHaertungTest {

    private static final Path COMPOSE = Path.of("deploy", "infomaniak", "compose.yml");
    private static final String HOST = "sponsorplatz.apps.urinet.ch";
    private static final long BUDGET_MIB = 1024;
    private static final BigDecimal BUDGET_CPUS = new BigDecimal("1.0");

    private static Map<String, Object> compose;
    private static Map<String, Map<String, Object>> dienste;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void lade() throws IOException {
        try (Reader reader = Files.newBufferedReader(COMPOSE)) {
            compose = new Yaml().load(reader);
        }
        dienste = (Map<String, Map<String, Object>>) compose.get("services");
    }

    @Test
    @DisplayName("Kein Dienst öffnet Ports oder hängt Verzeichnisse des Hosts ein")
    void keinePortsKeineBindMounts() {
        dienste.forEach((name, dienst) -> {
            assertThat(dienst).as(name).doesNotContainKeys("ports", "privileged", "network_mode", "cap_add", "build");
            for (Object volume : liste(dienst.get("volumes"))) {
                assertThat(volume).as("%s: Volume in Langform", name).isInstanceOf(Map.class);
                assertThat(((Map<?, ?>) volume).get("type")).as("%s: nur benannte Volumes", name).isEqualTo("volume");
            }
        });
    }

    @Test
    @DisplayName("Jeder Dienst läuft ohne root, nur lesbar und ohne zusätzliche Rechte")
    void gehaertet() {
        dienste.forEach((name, dienst) -> {
            assertThat(String.valueOf(dienst.get("user"))).as(name).matches("[1-9][0-9]*:[1-9][0-9]*");
            assertThat(dienst.get("read_only")).as(name).isEqualTo(true);
            assertThat(liste(dienst.get("cap_drop"))).as(name).containsExactly("ALL");
            assertThat(liste(dienst.get("security_opt"))).as(name).hasSize(1)
                    .allSatisfy(o -> assertThat(String.valueOf(o)).startsWith("no-new-privileges"));
        });
    }

    @Test
    @DisplayName("Die Limits aller Dienste zusammen bleiben im Speicherbudget")
    void speicherbudget() {
        long summeMib = 0;
        BigDecimal summeCpus = BigDecimal.ZERO;
        for (Map.Entry<String, Map<String, Object>> eintrag : dienste.entrySet()) {
            Map<String, Object> dienst = eintrag.getValue();
            assertThat(dienst).as(eintrag.getKey()).containsKeys("mem_limit", "cpus");
            summeMib += mib(String.valueOf(dienst.get("mem_limit")));
            summeCpus = summeCpus.add(new BigDecimal(String.valueOf(dienst.get("cpus"))));
        }

        assertThat(summeMib).isLessThanOrEqualTo(BUDGET_MIB);
        assertThat(summeCpus).isLessThanOrEqualTo(BUDGET_CPUS);
    }

    @Test
    @DisplayName("Die JVM-Grenzen der Anwendung passen unter ihr Limit")
    void jvmGrenzenUnterLimit() {
        String optionen = String.valueOf(umgebung("app").get("JAVA_OPTS"));
        long heap = zahl(optionen, "-Xmx(\\d+)m");
        long metaspace = zahl(optionen, "-XX:MaxMetaspaceSize=(\\d+)m");
        long codecache = zahl(optionen, "-XX:ReservedCodeCacheSize=(\\d+)m");
        long tmpfs = liste(dienste.get("app").get("tmpfs")).stream()
                .mapToLong(t -> zahl(String.valueOf(t), "size=(\\d+)m")).sum();
        long limit = mib(String.valueOf(dienste.get("app").get("mem_limit")));

        assertThat(optionen).doesNotContain("MaxRAMPercentage").contains("-XX:+ExitOnOutOfMemoryError");
        // Rest für Threads, direkte Puffer und die JVM selbst
        assertThat(heap + metaspace + codecache + tmpfs).isLessThanOrEqualTo(limit - 96);
    }

    @Test
    @DisplayName("Geheimnisse kommen nur als Dateien, jeder Dienst sieht nur die eigenen")
    void geheimnisseNurAlsDateien() {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> secrets = (Map<String, Map<String, Object>>) compose.get("secrets");

        secrets.forEach((name, secret) ->
                assertThat(secret.get("file")).as(name).isEqualTo("./secrets/" + name));
        assertThat(liste(dienste.get("db").get("secrets"))).containsExactly("db_password");
        dienste.forEach((name, dienst) -> umgebung(name).forEach((schluessel, wert) -> {
            if (schluessel.matches(".*(PASSWORD|PASSWORT|SECRET|TOKEN|API_KEY).*")) {
                assertThat(String.valueOf(wert)).as("%s: %s verweist auf eine Datei", name, schluessel)
                        .startsWith("/run/secrets/");
                assertThat(schluessel).as("%s: Geheimnis nur über _FILE", name).endsWith("_FILE");
            }
        }));
    }

    @Test
    @DisplayName("Nur die Anwendung hängt am Netz des Reverse-Proxys")
    void nurAnwendungAmProxyNetz() {
        assertThat(netze("app")).contains("web", "intern");
        assertThat(netze("db")).containsExactly("intern");
        assertThat(dienste.get("db")).doesNotContainKey("labels");
    }

    @Test
    @DisplayName("Die Router schliessen sich aus: /livez geht an den Prüfpfad, alles andere an die Anwendung")
    void routerSchliessenSichAus() {
        Map<String, Object> labels = labels();
        String anwendung = String.valueOf(labels.get("traefik.http.routers.sponsorplatz.rule"));
        String pruefpfad = String.valueOf(labels.get("traefik.http.routers.sponsorplatz-health.rule"));

        assertThat(anwendung).isEqualTo("Host(`" + HOST + "`) && !Path(`/livez`)");
        assertThat(pruefpfad).isEqualTo("Host(`" + HOST + "`) && Path(`/livez`)");
        assertThat(labels.keySet()).noneMatch(k -> k.endsWith(".priority"));
        List<String> regeln = labels.keySet().stream().filter(k -> k.endsWith(".rule")).collect(Collectors.toList());
        assertThat(regeln).hasSize(2);
    }

    @Test
    @DisplayName("Ohne Angabe im Bundle gilt der Vorschau-Schutz; nur der Prüfpfad ist offen")
    void vorschauSchutzIstNormalfall() {
        Map<String, Object> labels = labels();
        String anwendung = String.valueOf(labels.get("traefik.http.routers.sponsorplatz.middlewares"));
        String pruefpfad = String.valueOf(labels.get("traefik.http.routers.sponsorplatz-health.middlewares"));

        assertThat(anwendung).isEqualTo(
                "${SPONSORPLATZ_MIDDLEWARES:-sponsorplatz-drossel,sponsorplatz-vorschau,sponsorplatz-noindex}");
        assertThat(pruefpfad).isEqualTo("sponsorplatz-drossel-health");
        assertThat(String.valueOf(labels.get("traefik.http.middlewares.sponsorplatz-vorschau.basicauth.users")))
                .as("Passwort-Hash kommt aus dem Bundle, nicht aus dem Repo")
                .isEqualTo("${VORSCHAU_BENUTZER:-}");
        assertThat(labels.keySet()).noneMatch(k -> k.contains("usersfile"));
    }

    @Test
    @DisplayName("Ohne Angabe im Bundle läuft nur das Profil prod; Daten liegen auf dem Volume")
    void profilUndPfade() {
        Map<String, Object> umgebung = umgebung("app");

        assertThat(umgebung.get("SPRING_PROFILES_ACTIVE")).isEqualTo("${SPONSORPLATZ_PROFILE:-prod}");
        assertThat(umgebung.get("STORAGE_PFAD")).isEqualTo("/daten/medien");
        assertThat(umgebung.get("SICHERUNG_PFAD")).isEqualTo("/daten/sicherung");
        assertThat(liste(dienste.get("app").get("volumes")))
                .anySatisfy(v -> assertThat(((Map<?, ?>) v).get("target")).isEqualTo("/daten"));
    }

    private static Map<String, Object> labels() {
        return abbildung(dienste.get("app").get("labels"));
    }

    private static Map<String, Object> umgebung(String dienst) {
        return abbildung(dienste.get(dienst).get("environment"));
    }

    private static List<String> netze(String dienst) {
        return List.copyOf(abbildung(dienste.get(dienst).get("networks")).keySet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> abbildung(Object wert) {
        return wert == null ? Map.of() : (Map<String, Object>) wert;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> liste(Object wert) {
        return wert == null ? List.of() : (List<Object>) wert;
    }

    private static long mib(String wert) {
        return zahl(wert, "(\\d+)m");
    }

    private static long zahl(String text, String muster) {
        Matcher treffer = Pattern.compile(muster).matcher(text);
        assertThat(treffer.find()).as("%s in «%s»", muster, text).isTrue();
        return Long.parseLong(treffer.group(1));
    }
}
