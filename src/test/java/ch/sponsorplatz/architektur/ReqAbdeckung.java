package ch.sponsorplatz.architektur;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Spec 01, Req 1.1: Jedes Kriterium, das eine abgehakte Aufgabe ab Welle 1 unter {@code _Anforderungen_} nennt, hat ein
 * Szenario mit Tag {@code @req-x.y} oder einen Nachweis-Vermerk («manuell», «Dokument», «JUnit»), jeweils innerhalb
 * derselben Spec: Jede Feature-Datei mit {@code @req}-Tags trägt auf Feature-Ebene genau einen Tag {@code @spec-NN}.
 * Welle 0 bleibt aussen vor. Optionale Aufgaben ({@code - [x]* O.n}) zählen nicht.
 * Feature-Dateien mit {@code @attrappe} (nur auf Feature-Ebene) prüfen gegen Attrappen statt gegen die echte Anwendung
 * und sind nur Teilnachweis: Für Kriterien abgehakter {@code [Backend]}-Aufgaben zählen sie nicht.
 * 
 * Bestandsschutz (tech.md, Punkt 4): Feature-Dateien des Bestands ohne {@code @spec}- und ohne {@code @req}-Tag
 * zählen nicht und brechen die Prüfung nicht ab. Übernommen aus dem UriNet Archiv, angepasst an die Pfade dieses
 * Repos.
 */
public final class ReqAbdeckung {

    private static final Pattern AUFGABE = Pattern.compile("^\\s*- \\[([ xX])](\\*?) (\\S+)");
    private static final Pattern CHECKBOX = Pattern.compile("^\\s*[-*] \\[[ xX]]");
    private static final Pattern LABEL = Pattern.compile("^\\s*-?\\s*[*_]*Anforderungen[*_]*\\s*:");
    private static final Pattern WELLE = Pattern.compile("^(\\d+)\\.\\d+[a-z]?$");
    private static final Pattern OPTIONAL = Pattern.compile("^O\\.\\d+$");
    private static final Pattern ANFORDERUNGEN = Pattern.compile("^\\s*(?:- )?_Anforderungen: ([^_]*)_\\s*$");
    private static final Pattern KRITERIEN = Pattern.compile("^(\\d+)\\.(\\d+)(\\s*–\\s*(?:(\\d+)\\.)?(\\d+))?$");
    private static final Pattern SPEC_TAG = Pattern.compile("@spec-(\\d{2})\\b");
    private static final Pattern FEATURE = Pattern.compile("^\\s*(Funktionalität|Funktion|Feature):");
    private static final Pattern REQUIREMENT = Pattern.compile("^#{2,4} Requirement (\\d+)\\b");
    private static final Pattern KRITERIUM = Pattern.compile("^(\\d+)\\. ");
    private static final Pattern NACHWEIS = Pattern.compile("Nachweis: (manuell|Dokument|JUnit)");
    private static final Pattern TAG = Pattern.compile("@req-(\\d+\\.\\d+)\\b");
    private static final Pattern ATTRAPPE = Pattern.compile("@attrappe\\b");
    private static final String BACKEND = "[Backend]";

    /** {@code @req}-Tags einer Spec: aus allen Feature-Dateien und nur aus denen ohne {@code @attrappe}. */
    record Szenarien(Set<String> alle, Set<String> ohneAttrappe) {
        static final Szenarien KEINE = new Szenarien(Set.of(), Set.of());
    }

    private ReqAbdeckung() {
    }

    /** Fehlende Abdeckung als «spec: x.y», sortiert; leer, wenn alles abgedeckt ist. Nur IDs, keine Inhalte. */
    public static List<String> luecken(Path repo) {
        Map<String, Szenarien> tags = tags(repo.resolve("src/test/resources/features"));
        List<String> luecken = new ArrayList<>();
        try (Stream<Path> specs = Files.list(repo.resolve(".kiro/specs"))) {
            for (Path spec : specs.sorted().toList()) {
                Path tasks = spec.resolve("tasks.md");
                Path requirements = spec.resolve("requirements.md");
                if (!Files.isRegularFile(tasks) || !Files.isRegularFile(requirements)) {
                    continue;
                }
                Set<String> nachweis = mitNachweis(lesen(requirements));
                Szenarien szenarien = tags.getOrDefault(spec.getFileName().toString().split("-", 2)[0], Szenarien.KEINE);
                List<String> aufgaben = lesen(tasks);
                Set<String> backend = gefordert(aufgaben, true);
                for (String kriterium : gefordert(aufgaben)) {
                    if (nachweis.contains(kriterium) || szenarien.ohneAttrappe().contains(kriterium)) {
                        continue;
                    }
                    if (!szenarien.alle().contains(kriterium)) {
                        luecken.add(spec.getFileName() + ": " + kriterium);
                    } else if (backend.contains(kriterium)) {
                        luecken.add(spec.getFileName() + ": " + kriterium + " (nur @attrappe)");
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return luecken;
    }

    /** Kriterien aus {@code _Anforderungen_} abgehakter Aufgaben ab Welle 1. */
    static Set<String> gefordert(List<String> tasks) {
        return gefordert(tasks, false);
    }

    /** Wie {@link #gefordert(List)}, mit {@code nurBackend} nur aus Aufgaben mit {@code [Backend]} im Titel. */
    static Set<String> gefordert(List<String> tasks, boolean nurBackend) {
        Set<String> kriterien = new TreeSet<>();
        boolean zaehlt = false;
        for (String zeile : tasks) {
            if (zeile.startsWith("#")) {
                zaehlt = false;
                continue;
            }
            Matcher aufgabe = AUFGABE.matcher(zeile);
            if (aufgabe.find()) {
                String kennung = aufgabe.group(3);
                Matcher welle = WELLE.matcher(kennung);
                boolean optional = !aufgabe.group(2).isEmpty();
                if (!optional && !welle.matches() && !OPTIONAL.matcher(kennung).matches()) {
                    throw new IllegalArgumentException("Ungültige Aufgabenkennung: " + kennung);
                }
                zaehlt = !aufgabe.group(1).isBlank() && !optional
                        && welle.matches() && Integer.parseInt(welle.group(1)) >= 1
                        && (!nurBackend || zeile.contains(BACKEND));
                continue;
            }
            if (CHECKBOX.matcher(zeile).find()) {
                throw new IllegalArgumentException("Checkbox nicht im Format «- [x] x.y Titel»: " + zeile.strip());
            }
            if (!zeile.contains("_Anforderungen") && !LABEL.matcher(zeile).find()) {
                continue;
            }
            Matcher anforderungen = ANFORDERUNGEN.matcher(zeile);
            if (!anforderungen.matches()) {
                throw new IllegalArgumentException("Anforderungszeile nicht im Format «_Anforderungen: x.y, …_»: "
                        + zeile.strip());
            }
            List<String> liste = kriterien(anforderungen.group(1));
            if (zaehlt) {
                kriterien.addAll(liste);
            }
        }
        return kriterien;
    }

    /** «1.7, 16.20–16.22» → 1.7, 16.20, 16.21, 16.22; jedes Teil muss ganz passen, sonst Abbruch. */
    static List<String> kriterien(String liste) {
        List<String> ergebnis = new ArrayList<>();
        for (String teil : liste.split(",", -1)) {
            Matcher m = KRITERIEN.matcher(teil.strip());
            if (!m.matches()) {
                throw new IllegalArgumentException("Ungültige Anforderung: " + teil.strip());
            }
            int req = Integer.parseInt(m.group(1));
            int von = Integer.parseInt(m.group(2));
            int bis = m.group(5) == null ? von : Integer.parseInt(m.group(5));
            if (m.group(4) != null && Integer.parseInt(m.group(4)) != req) {
                throw new IllegalArgumentException("Bereich über mehrere Requirements: " + teil.strip());
            }
            if (von > bis) {
                throw new IllegalArgumentException("Bereich absteigend: " + teil.strip());
            }
            for (int k = von; k <= bis; k++) {
                ergebnis.add(req + "." + k);
            }
        }
        return ergebnis;
    }

    /** Kriterien, deren Text einen Nachweis-Vermerk trägt (tech.md, Tests und Qualität, Ausnahmen). */
    static Set<String> mitNachweis(List<String> requirements) {
        Set<String> ergebnis = new TreeSet<>();
        String requirement = null;
        String kriterium = null;
        for (String zeile : requirements) {
            Matcher kopf = REQUIREMENT.matcher(zeile);
            if (kopf.find()) {
                requirement = kopf.group(1);
                kriterium = null;
                continue;
            }
            if (zeile.startsWith("#")) {
                kriterium = null;
                continue;
            }
            Matcher nummer = KRITERIUM.matcher(zeile);
            if (requirement != null && nummer.find()) {
                kriterium = requirement + "." + nummer.group(1);
            }
            if (kriterium != null && NACHWEIS.matcher(zeile).find()) {
                ergebnis.add(kriterium);
            }
        }
        return ergebnis;
    }

    /**
     * {@code @req}-Tags je Spec-Nummer («01»); bricht ab, wenn eine Feature-Datei mit {@code @req}-Tags nicht genau einen
     * {@code @spec-NN} trägt oder {@code @attrappe} unterhalb der Feature-Ebene steht. Dateien ohne beide Tags sind Bestand.
     */
    static Map<String, Szenarien> tags(Path features) {
        Map<String, Szenarien> ergebnis = new TreeMap<>();
        if (!Files.isDirectory(features)) {
            return ergebnis;
        }
        try (Stream<Path> dateien = Files.walk(features)) {
            for (Path datei : dateien.filter(p -> p.toString().endsWith(".feature")).sorted().toList()) {
                List<String> specs = new ArrayList<>();
                Set<String> reqs = new TreeSet<>();
                boolean kopf = true;
                boolean attrappe = false;
                for (String zeile : lesen(datei)) {
                    if (FEATURE.matcher(zeile).find()) {
                        kopf = false;
                    }
                    if (!zeile.strip().startsWith("@")) {
                        continue;
                    }
                    if (ATTRAPPE.matcher(zeile).find()) {
                        if (!kopf) {
                            throw new IllegalStateException("@attrappe nur auf Feature-Ebene: " + features.relativize(datei));
                        }
                        attrappe = true;
                    }
                    Matcher spec = SPEC_TAG.matcher(zeile);
                    while (spec.find()) {
                        specs.add(kopf ? spec.group(1) : "Szenario");
                    }
                    Matcher req = TAG.matcher(zeile);
                    while (req.find()) {
                        reqs.add(req.group(1));
                    }
                }
                if (specs.isEmpty() && reqs.isEmpty() && !attrappe) {
                    continue; // Bestand: weder @spec noch @req, zählt nicht (tech.md, Bestandsschutz)
                }
                if (specs.size() != 1 || specs.getFirst().equals("Szenario")) {
                    throw new IllegalStateException("Feature-Datei braucht auf Feature-Ebene genau einen Tag @spec-NN: "
                            + features.relativize(datei));
                }
                Szenarien szenarien = ergebnis.computeIfAbsent(specs.getFirst(),
                        s -> new Szenarien(new TreeSet<>(), new TreeSet<>()));
                szenarien.alle().addAll(reqs);
                if (!attrappe) {
                    szenarien.ohneAttrappe().addAll(reqs);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ergebnis;
    }

    /**
     * Unlesbare Nachweis-Vermerke als «spec: Zeile n»: Auf «Nachweis:» folgt in derselben Zeile «manuell», «Dokument»
     * oder «JUnit». Ein Zeilenumbruch dazwischen liesse den Vermerk still verfallen.
     */
    public static List<String> unlesbareVermerke(Path repo) {
        List<String> fehler = new ArrayList<>();
        try (Stream<Path> specs = Files.list(repo.resolve(".kiro/specs"))) {
            for (Path spec : specs.sorted().toList()) {
                Path requirements = spec.resolve("requirements.md");
                if (!Files.isRegularFile(requirements)) {
                    continue;
                }
                List<String> zeilen = lesen(requirements);
                boolean inKriterien = false;
                for (int i = 0; i < zeilen.size(); i++) {
                    String zeile = zeilen.get(i);
                    if (REQUIREMENT.matcher(zeile).find()) {
                        inKriterien = true;
                    }
                    if (inKriterien && zeile.contains("Nachweis:") && !NACHWEIS.matcher(zeile).find()) {
                        fehler.add(spec.getFileName() + ": Zeile " + (i + 1));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return fehler;
    }

    private static List<String> lesen(Path datei) {
        try {
            return Files.readAllLines(datei, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
