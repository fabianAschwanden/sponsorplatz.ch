package ch.sponsorplatz.organisation;

public record Pruefpunkte(boolean sportOderGesundheit, boolean bezugZurSchweiz, boolean angabenPlausibel) {

    public boolean sindAlleBestaetigt() {
        return sportOderGesundheit && bezugZurSchweiz && angabenPlausibel;
    }

    public String protokollText() {
        return "Sport/Gesundheit: " + jaNein(sportOderGesundheit)
                + "; Bezug zur Schweiz: " + jaNein(bezugZurSchweiz)
                + "; Angaben plausibel: " + jaNein(angabenPlausibel);
    }

    private static String jaNein(boolean wert) {
        return wert ? "Ja" : "Nein";
    }
}
