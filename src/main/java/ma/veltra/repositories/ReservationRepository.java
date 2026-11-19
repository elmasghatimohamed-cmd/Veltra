package ma.veltra.repositories;

import ma.veltra.entities.Reservation;
import ma.veltra.enums.StatutReservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPassagerId(Long passagerId);

    List<Reservation> findByTrajetId(Long trajetId);

    List<Reservation> findByPassagerIdAndStatut(
            Long passagerId,
            StatutReservation statut
    );

    boolean existsByPassagerIdAndTrajetIdAndStatut(
            Long passagerId,
            Long trajetId,
            StatutReservation statut
    );

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
}