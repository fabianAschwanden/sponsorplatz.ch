package ch.sponsorplatz.organisation;

import java.util.Set;

/**
 * Status einer Organisation im Plattform-Lebenszyklus.
 *
 * Übergänge:
 *   PENDING ── verifiziert ──> VERIFIED ── aktiviert ──> ACTIVE
 *      \                                                  /
 *       \__ suspendiert ___________________________ SUSPENDED
 */
public enum OrgStatus {
    /** Frisch registriert, wartet auf Verifizierung. */
    PENDING,
    /** Plattform-Admin oder Auto-Verifizierung (Zefix) hat geprüft. */
    VERIFIED,
    /** Aktiv genutzt, im Marktplatz sichtbar. */
    ACTIVE,
    /** Temporär gesperrt durch Plattform-Admin / Moderator. */
    SUSPENDED;

    public static final Set<OrgStatus> SICHTBAR = Set.of(VERIFIED, ACTIVE);

    public boolean istSichtbar() {
        return SICHTBAR.contains(this);
    }
}
