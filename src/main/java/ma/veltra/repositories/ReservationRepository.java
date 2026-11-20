package ma.veltra.repositories;

import ma.veltra.entities.Reservation;
import ma.veltra.enums.StatutReservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
    boolean existsByPassagerIdAndTrajetIdAndStatut(
            Long passagerId,
            Long trajetId,
            StatutReservation statut
    );

    interface PlacesReserveesParTrajet {
        Long getTrajetId();
        Long getPlacesReservees();
    }

    @Query("""
    SELECT COALESCE(SUM(r.nombrePlaces), 0)
    FROM Reservation r
    WHERE r.trajet.id = :trajetId
      AND r.statut = :statut
    """)
    Long sumReservedSeatsByTrajetAndStatut(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut
    );

    @Query("""
    SELECT
        r.trajet.id AS trajetId,
        SUM(r.nombrePlaces) AS placesReservees
    FROM Reservation r
    WHERE r.trajet.id IN :trajetIds
      AND r.statut = :statut
    GROUP BY r.trajet.id
    """)
    List<PlacesReserveesParTrajet> sumReservedSeatsByTrajetIdsAndStatut(
            @Param("trajetIds") List<Long> trajetIds,
            @Param("statut") StatutReservation statut
    );

    List<Reservation> findByPassagerIdOrderByDateReservationDesc(
            Long passagerId
    );

    List<Reservation> findByTrajetIdAndStatut(
            Long trajetId,
            StatutReservation statut
    );

    List<Reservation> findByTrajetIdOrderByDateReservationDesc(
            Long trajetId
    );
}