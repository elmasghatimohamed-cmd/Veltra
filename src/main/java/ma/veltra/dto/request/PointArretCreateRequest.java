package ma.veltra.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretCreateRequest(
        @NotBlank String ville,
        @Min(0) int ordre,
        @NotNull LocalDateTime heurePassage,
        @NotNull @DecimalMin("0.00") BigDecimal prixDepuisDepart
) {}