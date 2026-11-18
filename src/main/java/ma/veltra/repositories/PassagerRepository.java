package ma.veltra.repositories;

import ma.veltra.entities.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PassagerRepository
        extends JpaRepository<Passager, Long> {

    Optional<Passager> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Passager> findByNomContainingIgnoreCase(String nom);

    List<Passager> findByEmailContainingIgnoreCase(String email);
}