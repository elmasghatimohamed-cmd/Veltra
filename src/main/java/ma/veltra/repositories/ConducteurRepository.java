package ma.veltra.repositories;

import ma.veltra.entities.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConducteurRepository
        extends JpaRepository<Conducteur, Long> {

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);

    List<Conducteur> findByEmailContainingIgnoreCase(String email);
}