package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "points_arret",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_arret_trajet_ordre",
                        columnNames = {"trajet_id", "ordre"}
                ),
                @UniqueConstraint(
                        name = "uk_arret_trajet_ville",
                        columnNames = {"trajet_id", "ville"}
                )
        }
)
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private int ordre;

    @Column(name = "heure_passage", nullable = false)
    private LocalDateTime heurePassage;

    @Column(
            name = "prix_depuis_depart",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal prixDepuisDepart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "trajet_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_arret_trajet"
            )
    )
    private Trajet trajet;

    public PointArret(
            String ville,
            int ordre,
            LocalDateTime heurePassage,
            BigDecimal prixDepuisDepart
    ) {
        this.ville = ville;
        this.ordre = ordre;
        this.heurePassage = heurePassage;
        this.prixDepuisDepart = prixDepuisDepart;
    }

    void rattacherAuTrajet(Trajet trajet) {
        this.trajet = trajet;
    }
}
