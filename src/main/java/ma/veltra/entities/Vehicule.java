package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "vehicules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vehicule_immatriculation",
                columnNames = "immatriculation"
        )
)
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false, length = 30)
    private String immatriculation;

    private String couleur;

    @Column(nullable = false)
    private int capacite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conducteur_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_vehicule_conducteur"
            )
    )
    private Conducteur conducteur;

    public Vehicule(
            String marque,
            String modele,
            String immatriculation,
            String couleur,
            int capacite,
            Conducteur conducteur
    ) {
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.couleur = couleur;
        this.capacite = capacite;
        this.conducteur = conducteur;
    }

    public void modifierCaracteristiques(
            String marque,
            String modele,
            String couleur,
            int capacite
    ) {
        this.marque = marque;
        this.modele = modele;
        this.couleur = couleur;
        this.capacite = capacite;
    }
}
