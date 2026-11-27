package ch.sponsorplatz.shared.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "formular_drosselung")
public class FormularDrosselungEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "formular", nullable = false, length = 20)
    private Formular formular;

    @Column(name = "pruefwert", nullable = false, length = 64)
    private String pruefwert;

    @Column(name = "erstellt_am", nullable = false)
    private Instant erstelltAm;

    protected FormularDrosselungEintrag() {
    }

    public FormularDrosselungEintrag(Formular formular, String pruefwert, Instant erstelltAm) {
        this.formular = formular;
        this.pruefwert = pruefwert;
        this.erstelltAm = erstelltAm;
    }
}
