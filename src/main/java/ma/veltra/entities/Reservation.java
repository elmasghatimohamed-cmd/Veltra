package ma.veltra.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ma.veltra.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_places", nullable = false)
    private int nombrePlaces;

    @Column(
            name = "montant_total",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal montantTotal;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut = StatutReservation.CONFIRMEE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "passager_id", nullable = false)
    private Passager passager;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_montee_id", nullable = false)
    private PointArret pointMontee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "point_descente_id", nullable = false)
    private PointArret pointDescente;


    public Reservation(
            Passager passager,
            Trajet trajet,
            PointArret pointMontee,
            PointArret pointDescente,
            int nombrePlaces,
            BigDecimal montantTotal,
            LocalDateTime dateReservation
    ) {
        this.passager = passager;
        this.trajet = trajet;
        this.pointMontee = pointMontee;
        this.pointDescente = pointDescente;
        this.nombrePlaces = nombrePlaces;
        this.montantTotal = montantTotal;
        this.dateReservation = dateReservation;
    }

    public void marquerAnnulee() {
        this.statut = StatutReservation.ANNULEE;
    }
}
