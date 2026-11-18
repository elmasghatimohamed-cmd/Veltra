package ma.veltra.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record VehiculeUpdateRequest(

        @Pattern(regexp = ".*\\S.*",
                message = "La marque ne peut pas être vide")
        String marque,

        @Pattern(regexp = ".*\\S.*",
                message = "Le modèle ne peut pas être vide")
        String modele,

        @Pattern(regexp = ".*\\S.*",
                message = "La couleur ne peut pas être vide")
        String couleur,

        @Min(value = 1, message = "La capacité minimale est 1")
        @Max(value = 8, message = "La capacité maximale est 8")
        Integer capacite

) {}
