---
inclusion: always
---

# Struktur und Namen

## Repository
```
sponsorplatz.ch/
├── CLAUDE.md                Einstieg für Agenten (bindet die Steering-Dateien ein)
├── AGENTS.md                Verweis für weitere Agenten auf CLAUDE.md
├── .kiro/
│   ├── steering/            product.md, tech.md, structure.md
│   └── specs/
│       ├── 01-uebernahme-und-betrieb/        requirements.md, design.md, tasks.md
│       ├── 02-vorteilsseite/
│       ├── 03-vereins-und-agenturwerkzeug/
│       ├── 04-leads-und-cockpit/
│       ├── 05-beitraege-und-partner/
│       └── 06-produktivschaltung/
├── src/main/java/ch/sponsorplatz/<paket>/    Bestand und neue Fachbereiche
├── src/main/resources/
│   ├── db/migration/                         Flyway, für alle Datenbanken
│   ├── db/migration_postgres/                Flyway, nur PostgreSQL
│   ├── templates/<paket>/                    Thymeleaf, Ordner je Paket (ARCH-14)
│   └── static/                               CSS, JavaScript, Schriften
├── src/test/java/ch/sponsorplatz/<paket>/    Tests spiegeln das Paket (ARCH-13)
├── src/test/resources/features/<fachbereich>/  Cucumber, Tags @spec-NN und @req-x.y
├── docker/                                   Dockerfile
├── deploy/infomaniak/                        compose.yml und Dienst-Images
└── docs/
    ├── betrieb/                              Deploy, Rollback, Wiederherstellung, Geheimnisse rotieren
    └── bestand/                              Dokumente des Bestands: specs/, adr/ (nur noch Hintergrund)
```

Bei einem Widerspruch gilt: Steering vor Spec vor `docs/bestand/`. Die Dokumente unter `docs/bestand/` beschreiben
den Stand vor der Übernahme und werden nicht mehr nachgeführt.

## Bestand
Diese Pakete sind Bestand. Für sie gelten die Regeln ARCH-01 bis ARCH-22 und der Bestandsschutz (`tech.md`):

| Paket | Inhalt |
| --- | --- |
| `organisation` | Organisationen, Mitgliedschaften, Hierarchie, Onboarding, `AccessControl` |
| `benutzer` | Konto, Registrierung, Login, TOTP, OIDC, Datenexport |
| `einladung` | Einladung in eine bestehende Organisation |
| `projekt` | Projekte, Sponsoring-Pakete, Anlässe, Medien, Marktplatz |
| `anfrage` | Sponsoring-Anfragen, Nachrichten, Verträge, Rechnungen, Statistik |
| `engagement` | Engagements, heute abgeleitet aus angenommenen Anfragen; Schaufenster |
| `crm` | CRM des Sponsors über Vereine |
| `aufgabe` | Aufgaben-Engine |
| `benachrichtigung` | Benachrichtigungen in der Plattform |
| `audit` | Protokoll |
| `admin` | Verwaltung: Prüfung, Benutzer, Einstellungen |
| `dashboard`, `home`, `kontakt` | Übersicht, Startseite und Rechtsseiten, Kontaktformular |
| `backup`, `ops`, `seed` | Sicherung, Betriebsübersicht, Demodaten |
| `shared` | Querschnitt: Konfiguration, Mail, Medien, PDF, Speicher; kennt keine Fachpakete (ARCH-07) |

## Neue Fachbereiche (hexagonal)
| Paket | Inhalt | Spec |
| --- | --- | --- |
| `pilotanfrage` | Pilot-Anfrage, Zusage und Absage | 03 |
| `region` | Regionen und Freischaltung der Stufen | 01 |
| `gegenleistung` | Katalog der Typen, Gegenleistungen eines Pakets, Aktivierbarkeit | 03 |
| `vorteilsseite` | Vorteilsseite je Verein, Bausteine, Freigabe, Teilen-Kit | 02 |
| `lead` | Kontakte mit Einwilligung, Quelle und Status | 04 |
| `engagement` (Unterpakete `domain`, `application`, `adapter`) | das Engagement als eigenes Objekt | 03 |

Das Engagement-Objekt entsteht im bestehenden Paket `engagement`, in den Unterpaketen `domain`, `application` und
`adapter`. Die fünf Bestandsklassen an der Wurzel des Pakets bleiben, bis die Aufgabe zum Engagement sie ablöst.
Das ist eine Auslegung; die Alternative wäre ein neues Paket `zusage`.

Jeder neue Fachbereich gliedert sich gleich:
```
<fachbereich>/
├── domain/          Records, Invarianten, Domain-Services – ohne Spring, JPA oder Jackson
├── application/     Anwendungsfälle und Ports (out: z. B. Ablage, Mail, Uhr), Rechteprüfung
└── adapter/
    ├── in/web/      Controller, Formulare, View-DTOs
    └── out/         persistence/, mail/, … – Implementierungen der Ports
```
ArchUnit prüft für diese Pakete (`HexagonRegelnTest`): `domain` hängt nur von `java` und `domain` ab,
`application` nur von `domain` und `application`, Klassen unter `adapter.out` implementieren nur Ports aus
`application`, Beträge sind nie Gleitkomma, kein Zugriff auf Umgebungsvariablen.

Ein neuer Fachbereich nutzt den Bestand über einen eigenen Port in seiner `application` und einen Adapter unter
`adapter.out`, nicht durch direkten Zugriff auf Services oder Entities des Bestands. Der Bestand ruft neue
Fachbereiche nur über deren `application` auf.

Wer das erste Mal Klassen in einen neuen Fachbereich legt, trägt das Paket in die Listen der Bestandsregeln
ARCH-04 und ARCH-07 ein; das ist eine Ergänzung der Liste, keine Änderung der Regel.

## Templates und statische Dateien
- Templates neuer Fachbereiche liegen unter `templates/<fachbereich>/`.
- Öffentliche Seiten teilen sich ein Fragment und ein Stylesheet; der Bereich hinter dem Login behält seinen
  eigenen Stil (offen, Entscheid E6).
- Schriften, Skripte und Stylesheets liefert die App selbst aus.

## Namen
- **Fachbegriffe auf Deutsch** in Klassen, Tabellen und Pfaden: `Organisation`, `Mitgliedschaft`, `Paket`,
  `Gegenleistung`, `Engagement`, `Vorteilsseite`, `Vorteil`, `Lead`, `Einwilligung`, `Region`, `PilotAnfrage`.
- Technische Begriffe englisch: `Repository`, `Controller`, `Service`, `Port`, `Adapter`, `Config`, `View`.
- Tabellen und Spalten `snake_case`, Singular: `pilot_anfrage`, `gegenleistung_typ`, `region_freischaltung`.
- Enums als Text speichern.
- Migrationen `V<n>__<stichwort>.sql`, fortlaufend; die Nummer vergibt der PR, der zuerst gemergt wird.
- Aufgaben tragen die Nummern des Umsetzungsplans (`<welle>.<nr>`), Kriterien `<requirement>.<kriterium>` je
  Spec. Verweise über Specs hinweg als «Spec 0n, x.y» (Kriterium) bzw. «Spec 0n: x.y» (Aufgabe).
- Branches `<agent>/<spec>-<aufgabe>-<stichwort>` (z. B. `claude/03-2.5-pilotanfrage`), PR-Titel
  «[<Agent>] <spec> <aufgabe> <stichwort>».
