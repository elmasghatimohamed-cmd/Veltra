package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.PointArretCreateRequest;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.entities.PointArret;
import ma.veltra.entities.Reservation;
import ma.veltra.entities.Trajet;
import ma.veltra.entities.Vehicule;
import ma.veltra.enums.StatutReservation;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.exceptions.ForbiddenOperationException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.TrajetMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.ReservationRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.repositories.VehiculeRepository;
import ma.veltra.services.TrajetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrajetServiceImpl implements TrajetService {

    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetMapper trajetMapper;

    @Override
    @Transactional
    public TrajetResponse create(TrajetCreateRequest request) {
        Objects.requireNonNull(request, "La demande de création est obligatoire");

        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable : " + request.conducteurId()));
        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable : " + request.vehiculeId()));

        validerPublication(request, conducteur, vehicule);
        List<PointArretCreateRequest> points = preparerPointsArret(request);

        Trajet trajet = new Trajet(request.dateDepart(), request.placesProposees(), conducteur, vehicule);
        for (PointArretCreateRequest point : points) {
            trajet.ajouterPointArret(new PointArret(
                    point.ville().trim(), point.ordre(), point.heurePassage(), point.prixDepuisDepart()));
        }

        Trajet saved = trajetRepository.saveAndFlush(trajet);
        log.info("Trajet créé : id={}, conducteurId={}, places={}",
                saved.getId(), conducteur.getId(), request.placesProposees());
        return trajetMapper.toResponse(saved, saved.getPlacesProposees());
    }

    @Override
    @Transactional(readOnly = true)
    public TrajetResponse findById(Long id) {
        Trajet trajet = trajetRepository.findDetailsById(id)
                .orElseThrow(() -> trajetIntrouvable(id));
        return mapperAvecDisponibilite(trajet);
    }

    @Override
    @Transactional
    public TrajetResponse annuler(Long trajetId) {
        Trajet trajet = trajetRepository.findWithLockById(trajetId)
                .orElseThrow(() -> trajetIntrouvable(trajetId));
        verifierStatutModifiable(trajet, "annulé");

        List<Reservation> reservations = reservationRepository.findByTrajetIdAndStatut(
                trajetId, StatutReservation.CONFIRMEE);
        reservations.forEach(Reservation::marquerAnnulee);
        trajet.marquerAnnule();

        log.info("Trajet annulé : id={}, réservations annulées={}", trajetId, reservations.size());
        return trajetMapper.toResponse(trajet, trajet.getPlacesProposees());
    }

    @Override
    @Transactional
    public TrajetResponse terminer(Long trajetId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> trajetIntrouvable(trajetId));
        verifierStatutModifiable(trajet, "terminé");
        if (trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible de terminer un trajet avant son départ");
        }

        log.info("Clôture trajet demandée : id={}", trajetId);
        trajet.marquerTermine();
        return mapperAvecDisponibilite(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrajetResponse> rechercherTrajets(
            String villeDepart, String villeArrivee, LocalDateTime dateDepart,
            BigDecimal prixMax, Integer nombrePlaces, Pageable pageable) {

        if (nombrePlaces != null && nombrePlaces < 1) {
            log.warn("Recherche trajets refusée : nombre de places invalide={}", nombrePlaces);
            throw new IllegalArgumentException("Le nombre de places demandé doit être positif");
        }

        Pageable pagination = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "dateDepart"));
        Page<Trajet> page = trajetRepository.rechercherTrajetsAvecPrix(
                villeDepart, villeArrivee, dateDepart, StatutTrajet.PUBLIE,
                prixMax, nombrePlaces, StatutReservation.CONFIRMEE, pagination);

        if (page.isEmpty()) {
            return Page.empty(pagination);
        }
        return mapperPageAvecDisponibilites(page);
    }

    private void validerPublication(TrajetCreateRequest request, Conducteur conducteur, Vehicule vehicule) {
        if (!vehiculeRepository.existsByIdAndConducteurId(vehicule.getId(), conducteur.getId())) {
            throw new ForbiddenOperationException("Ce véhicule n'appartient pas au conducteur");
        }
        if (request.dateDepart() == null || !request.dateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date de départ doit être dans le futur");
        }
        if (request.placesProposees() > vehicule.getCapacite()) {
            throw new IllegalArgumentException(
                    "Le nombre de places proposées doit être compris entre 1 et " + vehicule.getCapacite());
        }
    }

    private List<PointArretCreateRequest> preparerPointsArret(TrajetCreateRequest request) {
        List<PointArretCreateRequest> points = request.pointsArret();
        validerOrdrePointsArret(points);
        List<PointArretCreateRequest> tries = points.stream()
                .sorted(Comparator.comparingInt(PointArretCreateRequest::ordre)).toList();
        validerParcours(tries, request.dateDepart());
        return tries;
    }

    private void verifierStatutModifiable(Trajet trajet, String operation) {
        if (trajet.getStatut() != StatutTrajet.PUBLIE && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException("Seul un trajet publié ou complet peut être " + operation);
        }
    }

    private ResourceNotFoundException trajetIntrouvable(Long id) {
        return new ResourceNotFoundException("Trajet introuvable : " + id);
    }

    private TrajetResponse mapperAvecDisponibilite(Trajet trajet) {
        long reservees = reservationRepository.sumReservedSeatsByTrajetAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);
        return trajetMapper.toResponse(trajet, trajet.getPlacesProposees() - reservees);
    }

    private Page<TrajetResponse> mapperPageAvecDisponibilites(Page<Trajet> page) {
        List<Long> ids = page.getContent().stream().map(Trajet::getId).toList();
        Map<Long, Trajet> details = trajetRepository.findAllWithDetailsByIdIn(ids).stream()
                .collect(Collectors.toMap(Trajet::getId, Function.identity()));
        Map<Long, Long> reservees = reservationRepository
                .sumReservedSeatsByTrajetIdsAndStatut(ids, StatutReservation.CONFIRMEE)
                .stream()
                .collect(Collectors.toMap(
                        ReservationRepository.PlacesReserveesParTrajet::getTrajetId,
                        ReservationRepository.PlacesReserveesParTrajet::getPlacesReservees));

        return page.map(trajet -> {
            Trajet detail = details.get(trajet.getId());
            if (detail == null) {
                throw new IllegalStateException("Détails du trajet introuvables : " + trajet.getId());
            }
            return trajetMapper.toResponse(detail,
                    detail.getPlacesProposees() - reservees.getOrDefault(detail.getId(), 0L));
        });
    }

    private void validerOrdrePointsArret(List<PointArretCreateRequest> pointsArret) {
        if (pointsArret == null || pointsArret.size() < 2) {
            throw new IllegalArgumentException("Un trajet doit contenir au moins deux points d'arrêt");
        }
        if (pointsArret.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Un point d'arrêt ne peut pas être null");
        }

        Set<Integer> ordres = new HashSet<>();
        for (PointArretCreateRequest point : pointsArret) {
            int ordre = point.ordre();
            if (ordre < 0) {
                throw new IllegalArgumentException("L'ordre d'un point d'arrêt ne peut pas être négatif");
            }
            if (!ordres.add(ordre)) {
                throw new IllegalArgumentException("L'ordre " + ordre + " est utilisé plusieurs fois");
            }
        }
        for (int i = 0; i < pointsArret.size(); i++) {
            if (!ordres.contains(i)) {
                throw new IllegalArgumentException("Ordre manquant des points d'arrêt. ");
            }
        }
    }

    private void validerParcours(List<PointArretCreateRequest> points, LocalDateTime dateDepart) {
        Set<String> villes = new HashSet<>();
        LocalDateTime heurePrecedente = null;
        BigDecimal prixPrecedent = null;

        for (PointArretCreateRequest point : points) {
            if (point.ville() == null || point.ville().isBlank()
                    || point.heurePassage() == null || point.prixDepuisDepart() == null) {
                throw new IllegalArgumentException("Les informations des points d'arrêt sont obligatoires");
            }
            BigDecimal prix = point.prixDepuisDepart();
            if (prix.signum() < 0 || prix.scale() > 2
                    || prix.compareTo(new BigDecimal("99999999.99")) > 0) {
                throw new IllegalArgumentException(
                        "Le prix doit être positif ou nul, avec au maximum 8 chiffres entiers et 2 décimales");
            }
            String ville = point.ville().trim().toLowerCase(Locale.ROOT);
            if (!villes.add(ville)) {
                throw new IllegalArgumentException("Une ville ne peut apparaître qu'une seule fois");
            }
            if (point.ordre() == 0) {
                if (!point.heurePassage().equals(dateDepart)) {
                    throw new IllegalArgumentException("L'heure du premier arrêt doit correspondre au départ");
                }
                if (prix.compareTo(BigDecimal.ZERO) != 0) {
                    throw new IllegalArgumentException("Le prix au départ doit être égal à zéro");
                }
            } else {
                if (!point.heurePassage().isAfter(heurePrecedente)) {
                    throw new IllegalArgumentException("Les heures de passage doivent être strictement croissantes");
                }
                if (prix.compareTo(prixPrecedent) <= 0) {
                    throw new IllegalArgumentException("Les prix doivent être strictement croissants");
                }
            }
            heurePrecedente = point.heurePassage();
            prixPrecedent = prix;
        }
    }
}
