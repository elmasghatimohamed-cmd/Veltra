package ma.veltra.services;

import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TrajetService {

    TrajetResponse create(TrajetCreateRequest request);

    TrajetResponse findById(Long id);

    List<TrajetResponse> findByConducteurId(Long conducteurId);

    TrajetResponse annuler(Long trajetId);

    TrajetResponse terminer(Long trajetId);

    List<TrajetResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDateTime dateDepart
    );
}
