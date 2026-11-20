package ma.veltra.dto.request;

import jakarta.validation.constraints.*;

public record PassagerCreateRequest(
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotBlank @Email String email,
        @NotBlank String telephone
) {}
