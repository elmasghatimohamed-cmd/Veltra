package ma.veltra.repositories;

import ma.veltra.entities.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PassagerRepository
        extends JpaRepository<Passager, Long> {

    Optional<Passager> findByEmail(String email);

    boolean existsByEmail(String email);
}