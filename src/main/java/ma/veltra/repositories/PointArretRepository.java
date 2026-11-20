package ma.veltra.repositories;

import ma.veltra.entities.PointArret;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointArretRepository
        extends JpaRepository<PointArret, Long> {}