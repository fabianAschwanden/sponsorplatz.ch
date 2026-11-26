# ADR-0004: Branche-Enum strikt auf Health-Fokus

## Status
Akzeptiert

## Datum
2026-05-05

## Kontext

In der initialen Version (Konzept v3.0) war `Organisation.branche` ein freies
String-Feld mit beispielhafter Verwendung wie `SPORT`, `KULTUR`, `SOZIALES`,
`BILDUNG`, `UMWELT`. Die Plattform war damit thematisch offen für alle
Vereinstypen.

Strategische Reflexion ergab: **generische Sponsoring-Plattformen funktionieren
schlecht**, weil sie keinen klaren Vertrauensvorteil für Sponsoren mit
spezifischer Mission bieten. Eine Plattform für "alle Vereine" ist eine
Streuwiese — für Health-Marken (Krankenkassen, Apotheken, Lebensmittel,
Fitness) ein Albtraum, weil 80% der Vereine thematisch nicht passen.

Gleichzeitig: zu enger Fokus (nur klassischer Sport) verkleinert den Markt
zu stark — Schätzung ~15-20'000 Vereine, vs. ~30-40'000 wenn das Health-
Spektrum breit gefasst wird.

Hintergrund-Treiber: die Kickbox-Idee „CSS Sponsoring-Hub" verlangt eine
kuratierte Health-Plattform — die Sponsorplatz-Codebasis muss dem genügen.

## Entscheidung

**Strikte Nische, breiter Themen-Umfang.**

- **Strikt:** Nur Vereine im Sport- und Gesundheitsbereich. Andere Themen
  werden im Verifizierungs-Workflow durch den `PLATFORM_ADMIN` abgelehnt.
- **Breit innerhalb der Nische:** Sport, Bewegung, Reha, Behindertensport,
  Seniorensport, Prävention, Mental Health, Ernährung, Wellness, Selbsthilfe,
  Patientenorganisation — elf Werte.

Technische Umsetzung:

- `Branche` wird zum **Java-Enum** mit elf Werten und je einer deutschen
  Anzeige (`getAnzeige()`) plus Beschreibung (`getBeschreibung()`).
- `Organisation.branche` ist `@NotNull`, gespeichert als String via
  `@Enumerated(EnumType.STRING)`.
- Flyway-Migration **V12** macht `branche` NOT NULL und ergänzt einen
  CHECK-Constraint auf die elf Werte. Vorher freie String-Werte werden auf
  `SPORT` gemappt (defensiver Default — Plattform war Pre-Launch).
- `OrganisationFormDto.branche` ist `@NotNull` Enum, Service wirft
  `IllegalArgumentException` bei null.
- Verifizierungs-Queue zeigt Branche prominent mit Hinweis "Health-Fokus prüfen".

## Konsequenzen

**Positiv:**

- Klares Vertrauens-Versprechen an Health-Marken — keine Streuverluste.
- Compile-Time-Sicherheit: Branche-Werte können nicht versehentlich vertippt werden.
- DB-Constraint plus Enum-Typ + DTO-Annotation + Service-Validierung = vierfache Defense.
- Marketing-Story (Vereine + Health-Marken) wird scharf erzählbar.
- Skalierungs-Pfad offen: weitere Health-Themen können später per Migration ergänzt werden, ohne andere Themen aufzunehmen.

**Negativ:**

- **Verkleinert den adressierbaren Markt** — Kulturvereine, Umweltvereine, Bildungs-Initiativen sind nicht willkommen. Bewusst akzeptiert für klares Brand-Versprechen.
- **PlatformAdmin-Aufwand bei Verifizierung** — manuelle Health-Fokus-Prüfung pro Anmeldung. Akzeptabel im Pilot-Volumen, langfristig ggf. Auto-Klassifikation.
- **Enum-Erweiterung verlangt Migration** — bewusster Reibungswiderstand, damit Schärfung nicht versehentlich aufgeweicht wird.

## Alternativen

- **Free-String-Feld behalten** verworfen — keine Compile-Time-Sicherheit, keine UI-Filter-Konsistenz, kein Marketing-USP.
- **Nur klassischer Sport (`SPORT`-Enum-Singleton)** verworfen — zu engmaschiger Markt, kein Raum für Reha-/Mental-Health-/Ernährungs-Vereine, die genau die spannenden Health-Stories liefern.
- **Branche als separate `branche`-Tabelle (Master-Daten)** verworfen — overkill für elf statische Werte. Würde bei beliebig vielen Branchen Sinn ergeben, aber Sponsorplatz strebt strikt elf an.
- **Tagging-System (mehrere Branchen pro Verein)** verworfen — Plattform-Admin-Verifizierung wäre nicht eindeutig. Eine Verein-Mission soll klar einer Branche zugeordnet sein.

## Referenzen

- [`docs/bestand/specs/PROJEKT_INFO.md`](../specs/PROJEKT_INFO.md) §Positionierung
- [`docs/bestand/specs/DATENMODELL.md`](../specs/DATENMODELL.md) — `organisation.branche` mit V12-CHECK
- `00_Konzept_v3_Kollaborative-Plattform.md` Schlüssel-Entscheidung 2
- `07_Marketing_Konzept.md` v1.1 — Marketing-Konsequenzen der Schärfung
- Test-IDs ORG-22/23 — Branche-Pflicht + alle elf akzeptiert
- Migration `V12__branche_health_fokus.sql`
- Konzept-Dokument: warum eng + breit
