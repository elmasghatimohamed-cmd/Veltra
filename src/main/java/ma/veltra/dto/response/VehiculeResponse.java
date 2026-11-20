package ma.veltra.dto.response;

public record VehiculeResponse(
        Long id,
        Long conducteurId,
        String marque,
        String modele,
        String immatriculation,
        String couleur,
        int capacite
) {}
