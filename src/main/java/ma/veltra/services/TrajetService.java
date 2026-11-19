package ma.veltra.services;

import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TrajetService {

    TrajetResponse create(TrajetCreateRequest request);

    TrajetResponse findById(Long id);

    List<TrajetResponse> findByConducteurId(Long conducteurId);

    TrajetResponse annuler(Long trajetId);

    TrajetResponse terminer(Long trajetId);

    Page<TrajetResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDateTime dateDepart,
            BigDecimal prixMax,
            Integer nombrePlaces,
            Pageable pageable
    );
}
