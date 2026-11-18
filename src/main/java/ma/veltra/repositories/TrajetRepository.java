package ma.veltra.repositories;

import ma.veltra.entities.Trajet;
import ma.veltra.enums.StatutTrajet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrajetRepository
        extends JpaRepository<Trajet, Long> {

    List<Trajet> findByConducteurId(Long conducteurId);

    List<Trajet> findByVehiculeId(Long vehiculeId);

    boolean existsByVehiculeId(Long vehiculeId);

    Page<Trajet> findByStatutAndDateDepartAfter(
            StatutTrajet statut,
            LocalDateTime dateDepart,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Trajet> findWithLockById(Long id);

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
    """)
    List<Trajet> rechercherTrajets(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("dateDepart") LocalDateTime dateDepart,
            @Param("statut") StatutTrajet statut
    );
}