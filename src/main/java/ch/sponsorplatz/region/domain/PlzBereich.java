package ch.sponsorplatz.region.domain;

public record PlzBereich(int von, int bis) {
    public PlzBereich {
        if (von < 1000 || bis > 9999 || von > bis) {
            throw new IllegalArgumentException("PLZ-Bereiche müssen zwischen 1000 und 9999 liegen.");
        }
    }

    public boolean enthaelt(int plz) {
        return von <= plz && plz <= bis;
    }

    public boolean ueberschneidet(PlzBereich anderer) {
        return von <= anderer.bis && anderer.von <= bis;
    }
}
