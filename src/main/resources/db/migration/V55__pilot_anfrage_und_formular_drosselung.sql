CREATE TABLE einwilligung (
    id UUID PRIMARY KEY,
    bezug_art VARCHAR(40) NOT NULL,
    bezug_id UUID NOT NULL,
    zweck VARCHAR(50) NOT NULL,
    wortlaut TEXT NOT NULL,
    zeitpunkt TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE pilot_anfrage (
    id UUID PRIMARY KEY,
    rolle VARCHAR(20) NOT NULL,
    organisations_name VARCHAR(150) NOT NULL,
    bereich VARCHAR(100),
    sponsor_art VARCHAR(20),
    plz VARCHAR(4) NOT NULL,
    ort VARCHAR(100) NOT NULL,
    vorname VARCHAR(80) NOT NULL,
    nachname VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    paketname VARCHAR(120),
    gegenleistungen VARCHAR(1000),
    mitgliederzahl VARCHAR(20),
    naechster_anlass VARCHAR(200),
    interessen VARCHAR(1000),
    zielgruppe VARCHAR(200),
    marktgebiet VARCHAR(150),
    programme VARCHAR(300),
    uid VARCHAR(20),
    funktion VARCHAR(100),
    telefon VARCHAR(40),
    updates BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'NEU',
    eingereicht_am TIMESTAMP WITH TIME ZONE NOT NULL,
    aktualisiert_am TIMESTAMP WITH TIME ZONE NOT NULL,
    einwilligung_id UUID REFERENCES einwilligung (id)
);

CREATE INDEX idx_pilot_anfrage_duplikat
    ON pilot_anfrage (email, organisations_name, rolle, aktualisiert_am);

CREATE TABLE formular_drosselung (
    id UUID PRIMARY KEY,
    formular VARCHAR(20) NOT NULL,
    pruefwert VARCHAR(64) NOT NULL,
    erstellt_am TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_formular_drosselung_zeit
    ON formular_drosselung (formular, erstellt_am);

CREATE INDEX idx_formular_drosselung_pruefwert
    ON formular_drosselung (formular, pruefwert, erstellt_am);

CREATE TABLE formular_drosselung_sperre (
    formular VARCHAR(20) PRIMARY KEY
);

INSERT INTO formular_drosselung_sperre (formular) VALUES ('PILOT'), ('KONTAKT');
