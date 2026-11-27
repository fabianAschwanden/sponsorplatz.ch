package ch.sponsorplatz.pilotanfrage.domain;

public final class PilotEinwilligungstext {

    private static final String HINWEIS = "Wir nutzen eure Angaben für die Pilot-Anmeldung, die Prüfung und "
            + "die Kontaktaufnahme zum Pilot. Sponsorplatz speichert sie in der Schweiz. Ihr könnt sie "
            + "exportieren und löschen lassen.";

    private PilotEinwilligungstext() {
    }

    public static String bestaetigung(PilotAnfrageRolle rolle) {
        return rolle == PilotAnfrageRolle.VEREIN
                ? "Ich darf unseren Verein anmelden und habe die"
                : "Ich darf unsere Organisation anmelden und habe die";
    }

    public static String hinweis() {
        return HINWEIS;
    }

    public static String vollstaendig(PilotAnfrageRolle rolle) {
        return bestaetigung(rolle) + " Datenschutzerklärung gelesen. " + HINWEIS;
    }
}
