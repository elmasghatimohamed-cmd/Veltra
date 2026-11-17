package ma.veltra.repositories;

import ma.veltra.entities.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConducteurRepository
        extends JpaRepository<Conducteur, Long> {

    Optional<Conducteur> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);

    List<Conducteur> findByEmailContainingIgnoreCase(String email);
}