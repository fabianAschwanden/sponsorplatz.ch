package ch.sponsorplatz.projekt;

import ch.sponsorplatz.organisation.Branche;
import ch.sponsorplatz.organisation.OrgStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Projekt-Repository.
 *
 * <p><b>Konvention:</b> Jede Methode, die {@link Projekt}-Entitäten zurückgibt,
 * hängt {@code JOIN FETCH p.org} dran — {@link ProjektView#von(Projekt)} und
 * {@code OrganisationKurzView.von(projekt.getOrg())} greifen auf
 * {@code projekt.getOrg().getName/getSlug/getId} zu, was mit
 * {@code spring.jpa.open-in-view=false} nach Service-Tx-Ende ohne JOIN FETCH
 * konsequent zu {@code LazyInitializationException} (500) führt.
 * Count-Queries brauchen es nicht (geben Long zurück).
 */
@Repository
public interface ProjektRepository extends JpaRepository<Projekt, UUID> {

    @Query("SELECT p FROM Projekt p JOIN FETCH p.org WHERE p.slug = :slug")
    Optional<Projekt> findBySlug(@Param("slug") String slug);

    @Query("""
            SELECT p FROM Projekt p JOIN FETCH p.org o
            WHERE p.slug = :slug
              AND p.sichtbarkeit = :sichtbarkeit
              AND o.status IN :stati
            """)
    Optional<Projekt> findeOeffentlichNachSlug(
            @Param("slug") String slug,
            @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit,
            @Param("stati") Collection<OrgStatus> stati);

    @Query("SELECT p FROM Projekt p JOIN FETCH p.org WHERE p.org.id = :orgId ORDER BY p.createdAt DESC")
    List<Projekt> findByOrgIdOrderByCreatedAtDesc(@Param("orgId") UUID orgId);

    /**
     * Alle Projekte über mehrere Orgs hinweg — fürs „Meine Projekte"-View
     * eines Users mit Mitgliedschaften in mehreren Orgs.
     */
    @Query("SELECT p FROM Projekt p JOIN FETCH p.org WHERE p.org.id IN :orgIds ORDER BY p.createdAt DESC")
    List<Projekt> findByOrgIdInOrderByCreatedAtDesc(@Param("orgIds") Collection<UUID> orgIds);

    @Query("""
            SELECT p FROM Projekt p JOIN FETCH p.org o
            WHERE p.sichtbarkeit = :sichtbarkeit AND o.status IN :stati
            ORDER BY p.veroeffentlichtAm DESC
            """)
    List<Projekt> findBySichtbarkeitOrderByVeroeffentlichtAmDesc(
            @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit, @Param("stati") Collection<OrgStatus> stati);

    @Query("""
            SELECT p FROM Projekt p JOIN FETCH p.org o
            WHERE p.org.id = :orgId
              AND p.sichtbarkeit = :sichtbarkeit
              AND o.status IN :stati
            ORDER BY p.veroeffentlichtAm DESC
            """)
    List<Projekt> findeOeffentlicheNachOrgId(
            @Param("orgId") UUID orgId,
            @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit,
            @Param("stati") Collection<OrgStatus> stati);

    boolean existsBySlug(String slug);

    long countByOrgIdInAndSichtbarkeit(Collection<UUID> orgIds, Sichtbarkeit sichtbarkeit);

    /**
     * Zählt direkt auf DB-Ebene — Marken-Landing braucht nur die Anzahl, nicht
     * die Liste. Spart das Laden + Mappen aller Projekte für eine Statistik.
     */
    @Query("""
            SELECT COUNT(p) FROM Projekt p
            WHERE p.sichtbarkeit = :sichtbarkeit
              AND p.org.status IN :stati
            """)
    long countOeffentlicheNachSichtbarkeit(
            @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit, @Param("stati") Collection<OrgStatus> stati);

    long countBySichtbarkeit(Sichtbarkeit sichtbarkeit);

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Projekt p
            WHERE p.id = :projektId
              AND p.sichtbarkeit = :sichtbarkeit
              AND p.org.status IN :stati
            """)
    boolean existsOeffentlichNachId(
            @Param("projektId") UUID projektId,
            @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit,
            @Param("stati") Collection<OrgStatus> stati);

    /**
     * Volltextsuche: durchsucht Name, Beschreibung, Kategorie, Ort und Org-Name.
     * Case-insensitive LIKE-Suche, funktioniert auf H2 und PostgreSQL.
     */
    @Query("""
            SELECT p FROM Projekt p
              JOIN FETCH p.org o
            WHERE p.sichtbarkeit = :sichtbarkeit
            AND o.status IN :stati
            AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :suchbegriff, '%'))
                 OR LOWER(p.beschreibung) LIKE LOWER(CONCAT('%', :suchbegriff, '%'))
                 OR LOWER(p.kategorie) LIKE LOWER(CONCAT('%', :suchbegriff, '%'))
                 OR LOWER(p.ort) LIKE LOWER(CONCAT('%', :suchbegriff, '%'))
                 OR LOWER(o.name) LIKE LOWER(CONCAT('%', :suchbegriff, '%')))
            ORDER BY p.veroeffentlichtAm DESC
            """)
    List<Projekt> sucheOeffentliche(@Param("suchbegriff") String suchbegriff,
                                     @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit,
                                     @Param("stati") Collection<OrgStatus> stati);

    /**
     * Matching-Query: Findet öffentliche Projekte, deren Org eine der gegebenen Branchen hat,
     * aber nicht zu den eigenen Orgs gehört. Limitiert auf die neuesten Einträge.
     */
    @Query("""
            SELECT p FROM Projekt p
              JOIN FETCH p.org o
            WHERE p.sichtbarkeit = :sichtbarkeit
            AND o.branche IN :branchen
            AND o.id NOT IN :eigeneOrgIds
            AND o.status IN :stati
            ORDER BY p.veroeffentlichtAm DESC
            """)
    List<Projekt> findePassende(@Param("branchen") Collection<Branche> branchen,
                                @Param("eigeneOrgIds") Collection<UUID> eigeneOrgIds,
                                @Param("sichtbarkeit") Sichtbarkeit sichtbarkeit,
                                @Param("stati") Collection<OrgStatus> stati);
}
