package ma.veltra.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.entities.Passager;
import ma.veltra.entities.PointArret;
import ma.veltra.entities.Reservation;
import ma.veltra.entities.Trajet;
import ma.veltra.enums.StatutReservation;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ReservationMapper;
import ma.veltra.repositories.PassagerRepository;
import ma.veltra.repositories.PointArretRepository;
import ma.veltra.repositories.ReservationRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.services.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PassagerRepository passagerRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ReservationResponse creerReservation(ReservationCreateRequest request) {
        Trajet trajet = verrouillerTrajet(request.trajetId());
        validerTrajetReservable(trajet);
        if (request.nombrePlaces() <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif");
        }

        Passager passager = trouverPassager(request.passagerId());
        verifierAbsenceDoublon(passager, trajet);
        PointArret montee = trouverPoint(request.pointMonteeId(), "Point de montée introuvable");
        PointArret descente = trouverPoint(request.pointDescenteId(), "Point de descente introuvable");
        validerPoints(trajet, montee, descente);

        long placesRestantes = calculerPlacesRestantes(trajet);
        if (request.nombrePlaces() > placesRestantes) {
            log.warn("Réservation refusée : trajetId={}, places demandées={}, restantes={}",
                    trajet.getId(), request.nombrePlaces(), placesRestantes);
            throw new IllegalStateException("Nombre de places disponibles insuffisant");
        }

        BigDecimal montant = calculerMontant(montee, descente, request.nombrePlaces());
        Reservation reservation = new Reservation(passager, trajet, montee, descente,
                request.nombrePlaces(), montant, LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);

        if (request.nombrePlaces() == placesRestantes) {
            trajet.marquerComplet();
        }
        log.info("Réservation créée : id={}, trajetId={}, places={}",
                saved.getId(), trajet.getId(), request.nombrePlaces());
        return reservationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> historiquePassager(Long passagerId) {
        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException("Passager introuvable");
        }
        return reservationRepository.findByPassagerIdOrderByDateReservationDesc(passagerId)
                .stream().map(reservationMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ReservationResponse annulerReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));
        Trajet trajet = verrouillerTrajet(reservation.getTrajet().getId());
        entityManager.refresh(reservation);
        validerAnnulation(reservation, trajet);

        reservation.marquerAnnulee();
        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.marquerPublie();
        }
        log.info("Réservation annulée : id={}, trajetId={}", reservationId, trajet.getId());
        return reservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsDuTrajet(Long trajetId) {
        if (!trajetRepository.existsById(trajetId)) {
            throw new ResourceNotFoundException(
                    "Trajet introuvable : " + trajetId
            );
        }

        return reservationRepository
                .findByTrajetIdOrderByDateReservationDesc(trajetId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    //Helper Methods
    private Trajet verrouillerTrajet(Long trajetId) {
        return trajetRepository.findWithLockById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException("Trajet introuvable"));
    }

    private Passager trouverPassager(Long passagerId) {
        return passagerRepository.findById(passagerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passager introuvable"));
    }

    private PointArret trouverPoint(Long id, String message) {
        return pointArretRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message));
    }

    private void validerTrajetReservable(Trajet trajet) {

        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Ce trajet est complet. Aucune place disponible."
            );
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn(
                    "Réservation refusée : trajetId={}, statut={}",
                    trajet.getId(),
                    trajet.getStatut()
            );

            throw new IllegalStateException(
                    "Ce trajet n'accepte pas de réservations"
            );
        }

        if (!trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Impossible de réserver un trajet déjà parti"
            );
        }
    }

    private void verifierAbsenceDoublon(Passager passager, Trajet trajet) {
        if (reservationRepository.existsByPassagerIdAndTrajetIdAndStatut(
                passager.getId(), trajet.getId(), StatutReservation.CONFIRMEE)) {
            log.warn("Réservation refusée : doublon pour trajetId={} et passagerId={}",
                    trajet.getId(), passager.getId());
            throw new IllegalStateException("Le passager possède déjà une réservation confirmée");
        }
    }

    private void validerPoints(Trajet trajet, PointArret montee, PointArret descente) {
        if (!trajet.getId().equals(montee.getTrajet().getId())
                || !trajet.getId().equals(descente.getTrajet().getId())) {
            throw new IllegalArgumentException("Les points d'arrêt doivent appartenir au trajet");
        }
        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new IllegalArgumentException("Le point de montée doit précéder le point de descente");
        }
    }

    private long calculerPlacesRestantes(Trajet trajet) {
        long reservees = reservationRepository.sumReservedSeatsByTrajetAndStatut(
                trajet.getId(), StatutReservation.CONFIRMEE);
        return trajet.getPlacesProposees() - reservees;
    }

    private BigDecimal calculerMontant(PointArret montee, PointArret descente, int nombrePlaces) {
        BigDecimal prixUnitaire = descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart());
        if (prixUnitaire.signum() < 0) {
            throw new IllegalStateException("Tarification du trajet invalide");
        }
        return prixUnitaire.multiply(BigDecimal.valueOf(nombrePlaces))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validerAnnulation(Reservation reservation, Trajet trajet) {
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            log.warn("Annulation refusée : réservation déjà annulée, id={}", reservation.getId());
            throw new IllegalStateException("Cette réservation est déjà annulée");
        }
        if (!LocalDateTime.now().isBefore(trajet.getDateDepart().minusHours(24))) {
            log.warn("Annulation refusée : réservationId={}, délai de 24 heures dépassé", reservation.getId());
            throw new IllegalStateException(
                    "Une réservation doit être annulée au moins 24 heures avant le départ");
        }
        if (trajet.getStatut() != StatutTrajet.PUBLIE && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException("Le trajet ne permet plus l'annulation");
        }
    }
}
