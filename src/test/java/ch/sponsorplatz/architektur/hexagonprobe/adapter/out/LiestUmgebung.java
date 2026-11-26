package ch.sponsorplatz.architektur.hexagonprobe.adapter.out;

/** Verstoss: liest eine Umgebungsvariable. */
public class LiestUmgebung {
    public String lies() {
        return System.getenv("PROBE");
    }
}
