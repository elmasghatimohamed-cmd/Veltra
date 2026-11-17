package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ma.veltra.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trajets")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_depart", nullable = false)
    private LocalDateTime dateDepart;

    @Column(name = "places_proposees", nullable = false)
    private int placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTrajet statut = StatutTrajet.PUBLIE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conducteur_id",
            nullable = false
    )
    private Conducteur conducteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vehicule_id",
            nullable = false
    )
    private Vehicule vehicule;

    @Getter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "trajet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordre ASC")
    private List<PointArret> pointsArret = new ArrayList<>();


    public Trajet(
            LocalDateTime dateDepart,
            int placesProposees,
            Conducteur conducteur,
            Vehicule vehicule
    ) {
        this.dateDepart = dateDepart;
        this.placesProposees = placesProposees;
        this.conducteur = conducteur;
        this.vehicule = vehicule;
    }

    public List<PointArret> getPointsArret() {
        return List.copyOf(pointsArret);
    }

    public void ajouterPointArret(PointArret pointArret) {
        pointsArret.add(pointArret);
        pointArret.rattacherAuTrajet(this);
    }


    public void marquerComplet() {
        this.statut = StatutTrajet.COMPLET;
    }

    public void marquerTermine() {
        this.statut = StatutTrajet.TERMINE;
    }

    public void marquerAnnule() {
        this.statut = StatutTrajet.ANNULE;
    }

    public void marquerPublie() {
        this.statut = StatutTrajet.PUBLIE;
    }

}