CREATE TABLE region (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    erstellt_am TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE region_plz (
    id UUID PRIMARY KEY,
    region_id UUID NOT NULL REFERENCES region(id) ON DELETE CASCADE,
    plz_von INTEGER NOT NULL CHECK (plz_von BETWEEN 1000 AND 9999),
    plz_bis INTEGER NOT NULL CHECK (plz_bis BETWEEN plz_von AND 9999)
);

CREATE INDEX idx_region_plz_region ON region_plz(region_id);

CREATE TABLE region_freischaltung (
    region_id UUID NOT NULL REFERENCES region(id) ON DELETE CASCADE,
    stufe VARCHAR(40) NOT NULL CHECK (
        stufe IN ('ANGEBOT_UND_VORTEILE', 'FINDEN_UND_ZUSAGEN', 'WIRKUNG', 'UMFELD')
    ),
    freigeschaltet_am TIMESTAMP WITH TIME ZONE NOT NULL,
    freigeschaltet_von UUID REFERENCES app_user(id),
    PRIMARY KEY (region_id, stufe)
);

INSERT INTO region (id, code, name) VALUES
    ('00000000-0000-4000-8000-000000000001', 'pilot', 'Pilot');

INSERT INTO region_plz (id, region_id, plz_von, plz_bis) VALUES
    ('00000000-0000-4000-8000-000000000002', '00000000-0000-4000-8000-000000000001', 1000, 9999);

INSERT INTO region_freischaltung (region_id, stufe, freigeschaltet_am) VALUES
    ('00000000-0000-4000-8000-000000000001', 'ANGEBOT_UND_VORTEILE', CURRENT_TIMESTAMP);
