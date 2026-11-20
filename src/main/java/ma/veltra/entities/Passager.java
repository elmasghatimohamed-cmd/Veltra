package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "passagers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_passager_email",
                columnNames = "email"
        )
)
public class Passager {

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

    public Passager(
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

    public void modifierInformations(
            String nom,
            String prenom,
            String email,
            String telephone
    ) {
        if (nom != null) {
            this.nom = nom;
        }

        if (prenom != null) {
            this.prenom = prenom;
        }

        if (email != null) {
            this.email = email;
        }

        if (telephone != null) {
            this.telephone = telephone;
        }
    }

}