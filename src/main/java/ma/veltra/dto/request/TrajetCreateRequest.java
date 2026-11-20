package ma.veltra.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetCreateRequest(
        @NotNull @Positive Long conducteurId,
        @NotNull @Positive Long vehiculeId,
        @NotNull @Future LocalDateTime dateDepart,
        @Min(1) int placesProposees,
        @NotNull @Size(min = 2)
        List<@NotNull @Valid PointArretCreateRequest> pointsArret
) {}
