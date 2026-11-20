package ma.veltra.dto.request;

import jakarta.validation.constraints.*;

public record ReservationCreateRequest(
        @NotNull @Positive Long passagerId,
        @NotNull @Positive Long trajetId,
        @NotNull @Positive Long pointMonteeId,
        @NotNull @Positive Long pointDescenteId,
        @NotNull
        @Min(1)
        Integer nombrePlaces
) {}
