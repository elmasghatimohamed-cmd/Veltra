package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.PointArretCreateRequest;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.entities.PointArret;
import ma.veltra.entities.Trajet;
import ma.veltra.entities.Vehicule;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.TrajetMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.repositories.VehiculeRepository;
import ma.veltra.services.TrajetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper trajetMapper;

    @Override
    @Transactional
    public TrajetResponse create(TrajetCreateRequest request) {

        Objects.requireNonNull(
                request,
                "La demande de création est obligatoire"
        );

        Conducteur conducteur = conducteurRepository
                .findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable : " + request.conducteurId()
                ));

        Vehicule vehicule = vehiculeRepository
                .findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable : " + request.vehiculeId()
                ));

        if (!vehiculeRepository.existsByIdAndConducteurId(
                vehicule.getId(),
                conducteur.getId()
        )) {
            throw new IllegalArgumentException(
                    "Ce véhicule n'appartient pas au conducteur"
            );
        }

        if (request.dateDepart() == null
                || !request.dateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La date de départ doit être dans le futur"
            );
        }

        if (request.placesProposees() > vehicule.getCapacite()) {
            throw new IllegalArgumentException(
                    "Le nombre de places proposées doit être compris entre 1 et "
                            + vehicule.getCapacite()
            );
        }

        validerOrdrePointsArret(request.pointsArret());

        List<PointArretCreateRequest> points = request.pointsArret()
                .stream()
                .sorted(Comparator.comparingInt(
                        PointArretCreateRequest::ordre
                ))
                .toList();

        validerParcours(points, request.dateDepart());

        Trajet trajet = new Trajet(
                request.dateDepart(),
                request.placesProposees(),
                conducteur,
                vehicule
        );

        for (PointArretCreateRequest point : points) {
            trajet.ajouterPointArret(new PointArret(
                    point.ville().trim(),
                    point.ordre(),
                    point.heurePassage(),
                    point.prixDepuisDepart()
            ));
        }

        Trajet saved = trajetRepository.saveAndFlush(trajet);

        return trajetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrajetResponse findById(Long id) {

        Trajet trajet = trajetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable : " + id
                ));

        return trajetMapper.toResponse(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrajetResponse> findByConducteurId(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException(
                    "Conducteur introuvable : " + conducteurId
            );
        }

        return trajetRepository.findByConducteurId(conducteurId)
                .stream()
                .map(trajetMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TrajetResponse annuler(Long trajetId) {

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable : " + trajetId
                ));

        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Seul un trajet publié ou complet peut etre annulé"
            );
        }

        trajet.marquerAnnule();

        return trajetMapper.toResponse(trajet);
    }

    @Override
    @Transactional
    public TrajetResponse terminer(Long trajetId) {

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable : " + trajetId
                ));

        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Seul un trajet publié ou complet peut etre terminé"
            );
        }


        if (trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Impossible de terminer un trajet avant son départ"
            );
        }

        trajet.marquerTermine();

        return trajetMapper.toResponse(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrajetResponse> rechercherTrajets(
            String villeDepart,
            String villeArrivee,
            LocalDateTime dateDepart
    ) {
        if (villeDepart == null || villeDepart.isBlank()
                || villeArrivee == null || villeArrivee.isBlank()) {
            throw new IllegalArgumentException(
                    "Les villes de départ et d'arrivée sont obligatoires"
            );
        }

        if (villeDepart.trim().equalsIgnoreCase(villeArrivee.trim())) {
            throw new IllegalArgumentException(
                    "Les villes de départ et d'arrivée doivent être différentes"
            );
        }

        if (dateDepart == null) {
            throw new IllegalArgumentException(
                    "La date de départ est obligatoire"
            );
        }

        return trajetRepository.rechercherTrajets(
                        villeDepart.trim(),
                        villeArrivee.trim(),
                        dateDepart,
                        StatutTrajet.PUBLIE
                )
                .stream()
                .map(trajetMapper::toResponse)
                .toList();
    }

    //Helper methods
    private void validerOrdrePointsArret(
            List<PointArretCreateRequest> pointsArret
    ) {
        if (pointsArret == null || pointsArret.size() < 2) {
            throw new IllegalArgumentException(
                    "Un trajet doit contenir au moins deux points d'arrêt"
            );
        }

        if (pointsArret.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Un point d'arrêt ne peut pas être null"
            );
        }

        Set<Integer> ordres = new HashSet<>();

        for (PointArretCreateRequest point : pointsArret) {
            int ordre = point.ordre();

            if (ordre < 0) {
                throw new IllegalArgumentException(
                        "L'ordre d'un point d'arrêt ne peut pas être négatif"
                );
            }

            if (!ordres.add(ordre)) {
                throw new IllegalArgumentException(
                        "L'ordre " + ordre
                                + " est utilisé plusieurs fois"
                );
            }
        }

        for (int i = 0; i < pointsArret.size(); i++) {
            if (!ordres.contains(i)) {
                throw new IllegalArgumentException(
                        "Ordre manquant des points d'arrêt. "
                );
            }
        }
    }

    private void validerParcours(
            List<PointArretCreateRequest> points,
            LocalDateTime dateDepart
    ) {
        Set<String> villes = new HashSet<>();

        LocalDateTime heurePrecedente = null;
        BigDecimal prixPrecedent = null;

        for (PointArretCreateRequest point : points) {

            if (point.ville() == null
                    || point.ville().isBlank()
                    || point.heurePassage() == null
                    || point.prixDepuisDepart() == null) {
                throw new IllegalArgumentException(
                        "Les informations des points d'arrêt sont obligatoires"
                );
            }

            BigDecimal prix = point.prixDepuisDepart();

            if (prix.signum() < 0
                    || prix.scale() > 2
                    || prix.compareTo(new BigDecimal("99999999.99")) > 0) {
                throw new IllegalArgumentException(
                        "Le prix doit être positif ou nul, "
                                + "avec au maximum 8 chiffres entiers "
                                + "et 2 décimales"
                );
            }

            String ville = point.ville()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!villes.add(ville)) {
                throw new IllegalArgumentException(
                        "Une ville ne peut apparaître qu'une seule fois"
                );
            }

            if (point.ordre() == 0) {

                if (!point.heurePassage().equals(dateDepart)) {
                    throw new IllegalArgumentException(
                            "L'heure du premier arrêt doit correspondre au départ"
                    );
                }

                if (prix.compareTo(BigDecimal.ZERO) != 0) {
                    throw new IllegalArgumentException(
                            "Le prix au départ doit être égal à zéro"
                    );
                }

            } else {

                if (!point.heurePassage().isAfter(heurePrecedente)) {
                    throw new IllegalArgumentException(
                            "Les heures de passage doivent être strictement croissantes"
                    );
                }

                if (prix.compareTo(prixPrecedent) <= 0) {
                    throw new IllegalArgumentException(
                            "Les prix doivent être strictement croissants"
                    );
                }
            }

            heurePrecedente = point.heurePassage();
            prixPrecedent = prix;
        }
    }
}
