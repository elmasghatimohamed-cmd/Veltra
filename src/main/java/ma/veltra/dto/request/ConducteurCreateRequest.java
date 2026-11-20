package ma.veltra.dto.request;

import jakarta.validation.constraints.*;

public record ConducteurCreateRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Email String email,
        @NotBlank String telephone,
        @NotBlank String numeroPermis
) {}
