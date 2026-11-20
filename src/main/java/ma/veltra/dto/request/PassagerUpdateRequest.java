package ma.veltra.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record PassagerUpdateRequest(

        @Pattern(regexp = ".*\\S.*",
                message = "Le nom ne peut pas être vide")
        String nom,

        @Pattern(regexp = ".*\\S.*",
                message = "Le prénom ne peut pas être vide")
        String prenom,

        @Email(message = "Format d'email invalide")
        @Pattern(regexp = ".*\\S.*",
                message = "L'email ne peut pas être vide")
        String email,

        @Pattern(regexp = ".*\\S.*",
                message = "Le téléphone ne peut pas être vide")
        String telephone

) {}
