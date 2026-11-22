package ma.veltra.repositories;

import jakarta.persistence.LockModeType;
import ma.veltra.entities.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConducteurRepository
        extends JpaRepository<Conducteur, Long> {

    boolean existsByEmail(String email);

    boolean existsByNumeroPermis(String numeroPermis);

    List<Conducteur> findByNomContainingIgnoreCase(String nom);

    List<Conducteur> findByEmailContainingIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conducteur c WHERE c.id = :id")
    Optional<Conducteur> findWithLockById(@Param("id") Long id);
}