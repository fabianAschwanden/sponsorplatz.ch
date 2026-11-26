package ch.sponsorplatz.architektur;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Spec 01, Req 1.3: Regel «nur Spec» ({@code .github/scripts/nur-spec.py}). Bei reinen Spec-Anpassungen läuft in der CI
 * nur der Job «Spec-Prüfungen». Nur .md-Dateien unter .kiro/ oder docs/ zählen, bei Umbenennungen beide Pfade; im
 * Zweifel voller Lauf.
 * 
 * Das Skript kommt mit der Aufgabe 1.5 (CI teilen). Solange es fehlt, werden die Tests übersprungen, nicht bestanden.
 * Die Verdrahtung in {@code ci.yml} prüft diese Klasse nicht.
 */
class NurSpecTest {

    private static final Path REPO = ReqAbdeckungTest.REPO;
    private static final Path SKRIPT = REPO.resolve(".github/scripts/nur-spec.py");
    private static final String NULL = "0".repeat(40);

    @TempDir
    Path tmp;

    @BeforeEach
    void skriptVorhanden() {
        assumeTrue(Files.isRegularFile(SKRIPT), "nur-spec.py fehlt noch (Spec 01: Aufgabe 1.5)");
    }

    @Test
    void specUndDocsAlsMarkdownSindNurSpec() throws Exception {
        assertThat(pfade(".kiro/specs/01-uebernahme-und-betrieb/design.md", ".kiro/steering/tech.md", "docs/bestand/adr/adr-001.md",
                "docs/betrieb/deploy.md")).isEqualTo("true");
        assertThat(pfade("docs/Grüne Übersicht mit Leerzeichen.md", ".kiro/specs/02 Abgleich/Ä ö ü – Notiz.md"))
                .isEqualTo("true");
        // Markdown direkt in der Repo-Wurzel zählt mit: Kein Build-Schritt übernimmt es in ein Artefakt
        assertThat(pfade("README.md", "AGENTS.md", "CLAUDE.md")).isEqualTo("true");
    }

    @Test
    void allesAndereVerlangtDenVollenLauf() throws Exception {
        for (String pfad : List.of("deploy/README.md", ".github/x.md",
                "tools/README.md", "docs/schluessel.pub.pem", "docs/adr/bild.png", ".kiro/settings/mcp.json",
                "docs/x.MD", "docs/x.md.txt", "docsx/a.md", "kiro/a.md", "x/docs/a.md", "docs/../src/a.md",
                "docs/./a.md", "docs//a.md", "docs/a\nb.md", ".github/workflows/ci.yml",
                "src/main/java/ch/sponsorplatz/A.java", "docs/Grün mit Leerzeichen.txt")) {
            assertThat(pfade("docs/a.md", pfad)).as(pfad).isEqualTo("false");
        }
    }

    @Test
    void leereListeIstNichtNurSpec() throws Exception {
        assertThat(pfade()).isEqualTo("false");
    }

    @Test
    void ohnePrUndPushNieNurSpec() throws Exception {
        assertThat(lauf(null, "workflow_dispatch").ausgabe()).isEqualTo("false");
        assertThat(lauf(null).ausgabe()).isEqualTo("false");
    }

    @Test
    void pushNurMitSpecAenderung() throws Exception {
        Path repo = repo();
        String vorher = commit(repo, "docs/a.md", "x", "src/A.java", "class A {}");
        String nachher = commit(repo, "docs/a.md", "y", ".kiro/specs/01 Kern/Grüne Übersicht.md", "neu");
        assertThat(regel(repo, "push", vorher, nachher)).isEqualTo("true");
    }

    @Test
    void umbenennungAusDemCodeOderInDenCodeIstNichtNurSpec() throws Exception {
        Path repo = repo();
        String basis = commit(repo, "src/Notiz.md", "gleicher Inhalt", "docs/b.md", "zweiter Inhalt");
        verschieben(repo, "src/Notiz.md", "docs/Notiz.md");
        String ausCode = commit(repo);
        assertThat(regel(repo, "push", basis, ausCode)).as("aus dem Code").isEqualTo("false");
        verschieben(repo, "docs/b.md", "src/b.md");
        String inCode = commit(repo);
        assertThat(regel(repo, "push", ausCode, inCode)).as("in den Code").isEqualTo("false");
        verschieben(repo, "docs/Notiz.md", "docs/adr/Notiz.md");
        assertThat(regel(repo, "push", inCode, commit(repo))).as("innerhalb docs/").isEqualTo("true");
    }

    @Test
    void loeschungZaehlt() throws Exception {
        Path repo = repo();
        String basis = commit(repo, "docs/a.md", "x", "src/A.java", "class A {}");
        Files.delete(repo.resolve("src/A.java"));
        String ohneCode = commit(repo);
        assertThat(regel(repo, "push", basis, ohneCode)).isEqualTo("false");
        Files.delete(repo.resolve("docs/a.md"));
        Files.writeString(repo.resolve("docs/b.md"), "y");
        assertThat(regel(repo, "push", ohneCode, commit(repo))).isEqualTo("true");
    }

    @Test
    void pemUnterDocsIstNichtNurSpec() throws Exception {
        Path repo = repo();
        String basis = commit(repo, "docs/a.md", "x");
        assertThat(regel(repo, "push", basis, commit(repo, "docs/schluessel.pub.pem", "schluessel")))
                .isEqualTo("false");
    }

    @Test
    void neuerBranchUnbekannterOderUeberschriebenerVorgaengerNieNurSpec() throws Exception {
        Path repo = repo();
        String a = commit(repo, "docs/a.md", "x");
        String b = commit(repo, "docs/a.md", "y");
        assertThat(regel(repo, "push", NULL, b)).as("neuer Branch").isEqualTo("false");
        assertThat(regel(repo, "push", "", b)).as("ohne before").isEqualTo("false");
        assertThat(regel(repo, "push", "f".repeat(40), b)).as("unbekannt").isEqualTo("false");
        assertThat(regel(repo, "push", b, a)).as("kein Vorfahr (überschrieben)").isEqualTo("false");
        assertThat(regel(repo, "push", a, a)).as("keine Änderung").isEqualTo("false");
        assertThat(regel(repo, "push", a, b)).isEqualTo("true");
    }

    @Test
    void prVergleichtAbDerAbzweigung() throws Exception {
        Path repo = repo();
        String basis = commit(repo, "docs/a.md", "x", "src/A.java", "class A {}");
        git(repo, "checkout", "-q", "-b", "pr");
        String kopf = commit(repo, "docs/a.md", "y");
        git(repo, "checkout", "-q", "main");
        // main bewegt sich mit Code weiter; der PR selbst ändert nur Spec
        String mainNeu = commit(repo, "src/A.java", "class A { int x; }");
        assertThat(regel(repo, "pr", mainNeu, kopf)).isEqualTo("true");
        assertThat(regel(repo, "pr", basis, kopf)).isEqualTo("true");
        git(repo, "checkout", "-q", "pr");
        String kopfMitCode = commit(repo, "src/B.java", "class B {}");
        assertThat(regel(repo, "pr", mainNeu, kopfMitCode)).isEqualTo("false");
    }

    /** Die Positivliste gilt nur, solange kein Build-Schritt eine .md-Datei aus .kiro/ oder docs/ übernimmt. */
    @Test
    void keinBuildUebernimmtMarkdownAusSpecOderDocs() throws IOException {
        String pom = Files.readString(REPO.resolve("pom.xml"), StandardCharsets.UTF_8);
        Matcher ressource = Pattern.compile("<resource>(.*?)</resource>", Pattern.DOTALL).matcher(pom);
        while (ressource.find()) {
            assertThat(ressource.group(1)).as("Ressource im pom.xml").doesNotContain("docs", ".kiro");
        }
        for (String datei : List.of("docker/Dockerfile", "src/main/resources/application.properties")) {
            assertThat(Files.readString(REPO.resolve(datei), StandardCharsets.UTF_8)).as(datei)
                    .doesNotContain(".kiro", "../docs", "COPY docs");
        }
    }

    private record Ergebnis(int code, String ausgabe) {
    }

    private String pfade(String... pfade) throws Exception {
        Ergebnis e = lauf(String.join("\0", pfade), "pfade");
        assertThat(e.code()).isZero();
        return e.ausgabe();
    }

    private Ergebnis lauf(String stdin, String... argumente) throws Exception {
        return ausfuehren(tmp, stdin, befehl(argumente));
    }

    private static String regel(Path repo, String art, String basis, String kopf) throws Exception {
        Ergebnis e = ausfuehren(repo, null, befehl(art, basis, kopf));
        assertThat(e.code()).isZero();
        return e.ausgabe();
    }

    private static List<String> befehl(String... argumente) {
        List<String> befehl = new ArrayList<>(List.of("python3", SKRIPT.toString()));
        befehl.addAll(Arrays.asList(argumente));
        return befehl;
    }

    private static Ergebnis ausfuehren(Path verzeichnis, String stdin, List<String> befehl) throws Exception {
        Process p = new ProcessBuilder(befehl).directory(verzeichnis.toFile())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        try (var ein = p.getOutputStream()) {
            if (stdin != null) {
                ein.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        }
        String ausgabe = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        assertThat(p.waitFor(30, TimeUnit.SECONDS)).isTrue();
        return new Ergebnis(p.exitValue(), ausgabe);
    }

    private Path repo() throws Exception {
        Path repo = Files.createDirectories(tmp.resolve("repo"));
        git(repo, "init", "-q", "-b", "main");
        return repo;
    }

    /** Schreibt Pfad/Inhalt-Paare, committet alles und liefert den SHA. */
    private static String commit(Path repo, String... pfadUndInhalt) throws Exception {
        for (int i = 0; i < pfadUndInhalt.length; i += 2) {
            Path datei = repo.resolve(pfadUndInhalt[i]);
            Files.createDirectories(datei.getParent());
            Files.writeString(datei, pfadUndInhalt[i + 1], StandardCharsets.UTF_8);
        }
        git(repo, "add", "-A");
        git(repo, "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "-c", "commit.gpgsign=false",
                "commit", "-q", "--allow-empty", "-m", "Test");
        return git(repo, "rev-parse", "HEAD");
    }

    private static void verschieben(Path repo, String von, String nach) throws Exception {
        Files.createDirectories(repo.resolve(nach).getParent());
        git(repo, "mv", von, nach);
    }

    private static String git(Path repo, String... argumente) throws Exception {
        List<String> befehl = new ArrayList<>(List.of("git"));
        befehl.addAll(Arrays.asList(argumente));
        Ergebnis e = ausfuehren(repo, null, befehl);
        assertThat(e.code()).as(String.join(" ", befehl)).isZero();
        return e.ausgabe();
    }
}
