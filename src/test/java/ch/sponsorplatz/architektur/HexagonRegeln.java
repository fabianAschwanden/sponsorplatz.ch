package ch.sponsorplatz.architektur;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Spec 01, Req 1.2: Regeln für neue, hexagonale Fachbereiche (structure.md, «Neue Fachbereiche»; tech.md, Regel 13).
 * Geprüft werden nur Klassen unter {@code <fachbereich>.(domain|application|adapter)}; der Bestand bleibt unter
 * ARCH-01 bis ARCH-22. Jede Regel ist mit einem leeren Fachbereich grün ({@code allowEmptyShould}).
 * 
 * Die Regeln stammen aus dem UriNet Archiv ({@code domainOhneFramework}, {@code applicationNurDomain},
 * {@code adapterOutImplementiertPorts}, {@code betraegeNieGleitkomma}, {@code keineUmgebungsvariablen}). Die Regeln
 * nehmen die Wurzelpakete als Parameter, damit {@link HexagonRegelnTest} sie an Probeklassen belegen kann.
 */
final class HexagonRegeln {

    /** Betragsnamen wie im UriNet Archiv. */
    static final String BETRAG = "(?i).*(betrag|summe|preis|total|mwst|saldo).*";
    private static final Set<String> GLEITKOMMA = Set.of("double", "float", "java.lang.Double", "java.lang.Float");

    private final List<String> wurzeln;

    /** @param wurzeln Wurzelpakete der Fachbereiche, z. B. {@code ch.sponsorplatz.pilotanfrage} */
    HexagonRegeln(List<String> wurzeln) {
        this.wurzeln = List.copyOf(wurzeln);
    }

    private String[] schicht(String name) {
        return wurzeln.stream().map(w -> w + "." + name + "..").toArray(String[]::new);
    }

    private String[] schichten(String... namen) {
        List<String> pakete = new ArrayList<>();
        for (String name : namen) {
            pakete.addAll(List.of(schicht(name)));
        }
        return pakete.toArray(String[]::new);
    }

    private String[] mit(String[] pakete, String... weitere) {
        List<String> alle = new ArrayList<>(List.of(pakete));
        alle.addAll(List.of(weitere));
        return alle.toArray(String[]::new);
    }

    /** Die Domain hängt nur von {@code java} und der Domain neuer Fachbereiche ab. */
    ArchRule domainOhneFramework() {
        return classes().that().resideInAnyPackage(schicht("domain"))
                .should().onlyDependOnClassesThat().resideInAnyPackage(mit(schicht("domain"), "java.."))
                .allowEmptyShould(true)
                .because("die Domain neuer Fachbereiche ist framework-frei (tech.md Regel 13, structure.md)");
    }

    /**
     * {@code application} hängt nur von {@code domain} und {@code application} ab. Erlaubt sind zusätzlich die
     * Stereotypen und die Transaktions-Annotation von Spring, {@code jakarta.annotation} und SLF4J, entsprechend den
     * im UriNet Archiv erlaubten Gegenstücken von Quarkus.
     */
    ArchRule applicationNurDomain() {
        return classes().that().resideInAnyPackage(schicht("application"))
                .should().onlyDependOnClassesThat().resideInAnyPackage(mit(schichten("domain", "application"), "java..",
                        "org.springframework.stereotype..", "org.springframework.transaction.annotation..",
                        "jakarta.annotation..", "org.slf4j.."))
                .allowEmptyShould(true)
                .because("application hängt nur von domain ab (tech.md Regel 13, structure.md)");
    }

    /** Eine Klasse unter {@code adapter.out} implementiert aus dem eigenen Code nur Ports aus {@code application}. */
    ArchRule adapterOutImplementiertPorts() {
        List<String> application = wurzeln.stream().map(w -> w + ".application").toList();
        return classes().that().resideInAnyPackage(schicht("adapter.out")).and().areNotInterfaces()
                .should(new ArchCondition<>("nur Ports aus application implementieren") {
                    @Override
                    public void check(JavaClass klasse, ConditionEvents ereignisse) {
                        klasse.getRawInterfaces().stream()
                                .filter(s -> s.getPackageName().startsWith("ch.sponsorplatz."))
                                .filter(s -> application.stream().noneMatch(a -> s.getPackageName().equals(a)
                                        || s.getPackageName().startsWith(a + ".")))
                                .forEach(s -> ereignisse.add(SimpleConditionEvent.violated(klasse, klasse.getName()
                                        + " implementiert " + s.getName() + ", das nicht in application liegt")));
                    }
                })
                .allowEmptyShould(true)
                .because("Implementierungen der Ports liegen unter adapter/out (structure.md)");
    }

    /** Beträge sind nie {@code double} oder {@code float} (tech.md Regel 9). */
    ArchRule betraegeNieGleitkomma() {
        return fields().that().haveNameMatching(BETRAG)
                .and().areDeclaredInClassesThat().resideInAnyPackage(schichten("domain", "application", "adapter"))
                .should().notHaveRawType(double.class).andShould().notHaveRawType(Double.class)
                .andShould().notHaveRawType(float.class).andShould().notHaveRawType(Float.class)
                .allowEmptyShould(true)
                .because("Beträge sind BigDecimal (tech.md Regel 9)");
    }

    /** Auch Methoden mit Betragsnamen liefern keine Gleitkommazahl. */
    ArchRule betragsMethodenNieGleitkomma() {
        return methods().that().haveNameMatching(BETRAG)
                .and().areDeclaredInClassesThat().resideInAnyPackage(schichten("domain", "application", "adapter"))
                .should(new ArchCondition<>("keine Gleitkommazahl liefern") {
                    @Override
                    public void check(JavaMethod methode, ConditionEvents ereignisse) {
                        if (GLEITKOMMA.contains(methode.getRawReturnType().getName())) {
                            ereignisse.add(SimpleConditionEvent.violated(methode, methode.getFullName()
                                    + " liefert " + methode.getRawReturnType().getName()));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("Beträge sind BigDecimal (tech.md Regel 9)");
    }

    /** Kein Zugriff auf Umgebungsvariablen; erfasst Aufrufe und Methodenreferenzen ({@code System::getenv}). */
    ArchRule keineUmgebungsvariablen() {
        return noClasses().that().resideInAnyPackage(schichten("domain", "application", "adapter"))
                .should().accessTargetWhere(DescribedPredicate.describe("System.getenv",
                        zugriff -> zugriff.getTarget().getOwner().isEquivalentTo(System.class)
                                && zugriff.getTarget().getName().equals("getenv")))
                .allowEmptyShould(true)
                .because("Secrets liegen nur als Dateien unter /run/secrets/ (tech.md Regel 2)");
    }

    /** Ein neuer Fachbereich hat keine Klassen ausserhalb der drei Schichten; {@code package-info} ist ausgenommen. */
    ArchRule nurDieDreiSchichten(List<String> fachbereicheOhneBestand) {
        String[] ganze = fachbereicheOhneBestand.stream().map(w -> w + "..").toArray(String[]::new);
        String[] erlaubt = fachbereicheOhneBestand.stream()
                .flatMap(w -> List.of(w + ".domain..", w + ".application..", w + ".adapter..").stream())
                .toArray(String[]::new);
        return classes().that().resideInAnyPackage(ganze).and().doNotHaveSimpleName("package-info")
                .should().resideInAnyPackage(erlaubt)
                .allowEmptyShould(true)
                .because("neue Fachbereiche gliedern sich in domain, application und adapter (structure.md)");
    }
}
