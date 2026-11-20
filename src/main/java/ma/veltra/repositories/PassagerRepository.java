package ma.veltra.repositories;

import ma.veltra.entities.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassagerRepository
        extends JpaRepository<Passager, Long> {

    boolean existsByEmail(String email);

    List<Passager> findByEmailContainingIgnoreCase(String email);
}