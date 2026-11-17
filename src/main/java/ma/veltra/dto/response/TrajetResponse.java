package ma.veltra.dto.response;

import ma.veltra.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        Long conducteurId,
        Long vehiculeId,
        LocalDateTime dateDepart,
        int placesProposees,
        StatutTrajet statut,
        List<PointArretResponse> pointsArret
) {}
