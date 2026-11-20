package ma.veltra.repositories;

import ma.veltra.entities.Trajet;
import ma.veltra.enums.StatutReservation;
import ma.veltra.enums.StatutTrajet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrajetRepository
        extends JpaRepository<Trajet, Long> {

    boolean existsByVehiculeId(Long vehiculeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Trajet t WHERE t.id = :id")
    Optional<Trajet> findWithLockById(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN t.pointsArret montee
    JOIN t.pointsArret descente
    WHERE LOWER(montee.ville) = LOWER(:villeDepart)
      AND LOWER(descente.ville) = LOWER(:villeArrivee)
      AND montee.ordre < descente.ordre
      AND t.dateDepart >= :dateDepart
      AND t.statut = :statut
      AND (
          :prixMax IS NULL
          OR (descente.prixDepuisDepart - montee.prixDepuisDepart) <= :prixMax
      )
      AND (
          :nombrePlaces IS NULL
          OR (
              t.placesProposees - (
                  SELECT COALESCE(SUM(r.nombrePlaces), 0)
                  FROM Reservation r
                  WHERE r.trajet = t
                    AND r.statut = :statutReservation
              )
          ) >= :nombrePlaces
          )
    """)
    Page<Trajet> rechercherTrajetsAvecPrix(
            String villeDepart,
            String villeArrivee,
            LocalDateTime dateDepart,
            StatutTrajet statut,
            BigDecimal prixMax,
            @Param("nombrePlaces") Integer nombrePlaces,
            @Param("statutReservation") StatutReservation statutReservation,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN FETCH t.conducteur
    JOIN FETCH t.vehicule
    LEFT JOIN FETCH t.pointsArret
    WHERE t.id = :id
    """)
    Optional<Trajet> findDetailsById(@Param("id") Long id);

    @Query("""
    SELECT DISTINCT t
    FROM Trajet t
    JOIN FETCH t.conducteur
    JOIN FETCH t.vehicule
    LEFT JOIN FETCH t.pointsArret
    WHERE t.id IN :ids
    """)
    List<Trajet> findAllWithDetailsByIdIn(
            @Param("ids") List<Long> ids
    );
}