package ma.veltra.dto.request;

import jakarta.validation.constraints.*;

public record ReservationCreateRequest(
        @NotNull Long passagerId,
        @NotNull Long trajetId,
        @NotNull Long pointMonteeId,
        @NotNull Long pointDescenteId,
        @Min(1) int nombrePlaces
) {}
