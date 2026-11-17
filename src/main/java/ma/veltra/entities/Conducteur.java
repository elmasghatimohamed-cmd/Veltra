package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "conducteurs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_conducteur_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_conducteur_permis",
                        columnNames = "numero_permis"
                )
        }
)
public class Conducteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @Column(name = "numero_permis", nullable = false)
    private String numeroPermis;

    public Conducteur(
            String nom,
            String prenom,
            String email,
            String telephone,
            String numeroPermis
    ) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.numeroPermis = numeroPermis;
    }

    public void modifierCoordonnees(
            String nom,
            String prenom,
            String email,
            String telephone
    ) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
    }
}