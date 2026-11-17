package ma.veltra.dto.request;

import jakarta.validation.constraints.*;

public record VehiculeCreateRequest(
        @NotNull Long conducteurId,
        @NotBlank String marque,
        @NotBlank String modele,
        @NotBlank String immatriculation,
        @NotBlank String couleur,
        @Min(1) @Max(8) int capacite
) {}
