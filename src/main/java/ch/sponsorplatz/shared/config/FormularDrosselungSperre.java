package ch.sponsorplatz.shared.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "formular_drosselung_sperre")
public class FormularDrosselungSperre {

    @Id
    @Column(name = "formular", length = 20)
    private String formular;

    protected FormularDrosselungSperre() {
    }
}
