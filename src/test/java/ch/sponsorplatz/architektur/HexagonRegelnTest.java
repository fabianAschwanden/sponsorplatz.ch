package ch.sponsorplatz.architektur;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spec 01, Req 1.2: Hexagon-Regeln für die neuen Fachbereiche aus structure.md. Die Regeln des Bestands
 * ({@link ArchitekturRegelnTest}) bleiben unverändert.
 * 
 * {@code engagement} ist ein Paket des Bestands; hexagonal sind dort nur die Unterpakete {@code domain},
 * {@code application} und {@code adapter}. Die fünf Bestandsklassen an der Wurzel prüft diese Klasse nicht.
 */
@AnalyzeClasses(packages = "ch.sponsorplatz", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonRegelnTest {

    /** Neue Fachbereiche ohne Bestand: Jede Klasse liegt in einer der drei Schichten. */
    static final List<String> NEU = List.of("ch.sponsorplatz.pilotanfrage", "ch.sponsorplatz.region",
            "ch.sponsorplatz.gegenleistung", "ch.sponsorplatz.vorteilsseite", "ch.sponsorplatz.lead");
    /** Dazu das Paket des Bestands, in dem nur die Unterpakete hexagonal sind. */
    static final List<String> HEXAGONAL = List.of("ch.sponsorplatz.pilotanfrage", "ch.sponsorplatz.region",
            "ch.sponsorplatz.gegenleistung", "ch.sponsorplatz.vorteilsseite", "ch.sponsorplatz.lead",
            "ch.sponsorplatz.engagement");

    private static final HexagonRegeln REGELN = new HexagonRegeln(HEXAGONAL);

    @ArchTest
    static final ArchRule domainOhneFramework = REGELN.domainOhneFramework();
    @ArchTest
    static final ArchRule applicationNurDomain = REGELN.applicationNurDomain();
    @ArchTest
    static final ArchRule adapterOutImplementiertPorts = REGELN.adapterOutImplementiertPorts();
    @ArchTest
    static final ArchRule betraegeNieGleitkomma = REGELN.betraegeNieGleitkomma();
    @ArchTest
    static final ArchRule betragsMethodenNieGleitkomma = REGELN.betragsMethodenNieGleitkomma();
    @ArchTest
    static final ArchRule keineUmgebungsvariablen = REGELN.keineUmgebungsvariablen();
    @ArchTest
    static final ArchRule nurDieDreiSchichten = REGELN.nurDieDreiSchichten(NEU);

    /** Die Liste hier und die Tabelle in structure.md nennen dieselben Fachbereiche. */
    @Test
    void fachbereicheStehenInStructureMd() throws IOException {
        String structure = Files.readString(ReqAbdeckungTest.REPO.resolve(".kiro/steering/structure.md"),
                StandardCharsets.UTF_8);
        String abschnitt = structure.substring(structure.indexOf("## Neue Fachbereiche"),
                structure.indexOf("## Templates"));
        for (String paket : HEXAGONAL) {
            assertThat(abschnitt).as(paket).contains("| `" + paket.substring("ch.sponsorplatz.".length()) + "`");
        }
        long zeilen = abschnitt.lines().filter(z -> z.startsWith("| `")).count();
        assertThat(zeilen).as("Zeilen der Tabelle «Neue Fachbereiche»").isEqualTo(HEXAGONAL.size());
    }

    /**
     * Die Regeln greifen: Probeklassen unter {@code hexagonprobe} verstossen je gegen genau eine Regel. Ohne diesen
     * Test wären die Regeln mit leeren Fachbereichen grün, auch wenn sie nichts prüften.
     */
    @Test
    void regelnGreifenAnProbeklassen() {
        String wurzel = "ch.sponsorplatz.architektur.hexagonprobe";
        JavaClasses probe = new ClassFileImporter().importPackages(wurzel);
        assertThat(probe).as("Probeklassen").isNotEmpty();
        HexagonRegeln regeln = new HexagonRegeln(List.of(wurzel));

        verstoesst(regeln.domainOhneFramework(), probe, "DomainMitSpring");
        verstoesst(regeln.applicationNurDomain(), probe, "AnwendungMitAdapter");
        verstoesst(regeln.adapterOutImplementiertPorts(), probe, "FremderAdapter");
        verstoesst(regeln.betraegeNieGleitkomma(), probe, "PreisAlsGleitkomma");
        verstoesst(regeln.betragsMethodenNieGleitkomma(), probe, "PreisAlsGleitkomma");
        verstoesst(regeln.keineUmgebungsvariablen(), probe, "LiestUmgebung");
        verstoesst(regeln.nurDieDreiSchichten(List.of(wurzel)), probe, "AusserhalbDerSchichten");
    }

    private static void verstoesst(ArchRule regel, JavaClasses klassen, String erwartet) {
        EvaluationResult ergebnis = regel.evaluate(klassen);
        assertThat(ergebnis.hasViolation()).as(regel.getDescription()).isTrue();
        List<String> meldungen = ergebnis.getFailureReport().getDetails();
        assertThat(meldungen).as(regel.getDescription()).isNotEmpty()
                .allSatisfy(m -> assertThat(m).contains(erwartet));
    }
}
