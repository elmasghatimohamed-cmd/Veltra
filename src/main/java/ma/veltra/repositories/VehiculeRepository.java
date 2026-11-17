package ma.veltra.repositories;

import ma.veltra.entities.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculeRepository
        extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    List<Vehicule> findByConducteurId(Long conducteurId);

    boolean existsByIdAndConducteurId(
            Long vehiculeId,
            Long conducteurId
    );
}