package ch.sponsorplatz.shared.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "formular_drosselung_sperre")
public class FormularDrosselungSperre {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "formular", length = 20)
    private Formular formular;

    protected FormularDrosselungSperre() {
    }
}
