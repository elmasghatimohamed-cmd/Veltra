package ma.veltra.services.implementations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.entities.*;
import ma.veltra.enums.StatutReservation;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ReservationMapper;
import ma.veltra.repositories.*;
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
    public ReservationResponse creerReservation(
            ReservationCreateRequest request
    ) {
        Trajet trajet = trajetRepository
                .findWithLockById(request.trajetId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trajet introuvable")
                );

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            log.warn("Réservation refusée : trajetId={}, statut={}", trajet.getId(), trajet.getStatut());
            throw new IllegalStateException(
                    "Ce trajet n'accepte pas de réservations"
            );
        }

        if (!trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Impossible de réserver un trajet déjà parti"
            );
        }

        if (request.nombrePlaces() <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de places doit être positif"
            );
        }

        Passager passager = passagerRepository
                .findById(request.passagerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Passager introuvable")
                );

        boolean dejaReserve = reservationRepository
                .existsByPassagerIdAndTrajetIdAndStatut(
                        passager.getId(),
                        trajet.getId(),
                        StatutReservation.CONFIRMEE
                );

        if (dejaReserve) {
            log.warn("Réservation refusée : doublon pour trajetId={} et passagerId={}", trajet.getId(), passager.getId());
            throw new IllegalStateException(
                    "Le passager possède déjà une réservation confirmée"
            );
        }

        PointArret montee = pointArretRepository
                .findById(request.pointMonteeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Point de montée introuvable")
                );

        PointArret descente = pointArretRepository
                .findById(request.pointDescenteId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Point de descente introuvable")
                );

        if (!trajet.getId().equals(montee.getTrajet().getId())
                || !trajet.getId().equals(descente.getTrajet().getId())) {
            throw new IllegalArgumentException(
                    "Les points d'arrêt doivent appartenir au trajet"
            );
        }

        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new IllegalArgumentException(
                    "Le point de montée doit précéder le point de descente"
            );
        }

        Long placesReservees = reservationRepository
                .sumReservedSeatsByTrajetAndStatut(
                        trajet.getId(),
                        StatutReservation.CONFIRMEE
                );

        long placesRestantes =
                (long) trajet.getPlacesProposees() - placesReservees;

        if (request.nombrePlaces() > placesRestantes) {
            log.warn("Réservation refusée : trajetId={}, places demandées={}, restantes={}", trajet.getId(), request.nombrePlaces(), placesRestantes);
            throw new IllegalStateException(
                    "Nombre de places disponibles insuffisant"
            );
        }

        BigDecimal prixUnitaire = descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart());

        if (prixUnitaire.signum() < 0) {
            throw new IllegalStateException(
                    "Tarification du trajet invalide"
            );
        }

        BigDecimal montantTotal = prixUnitaire
                .multiply(BigDecimal.valueOf(request.nombrePlaces()))
                .setScale(2, RoundingMode.HALF_UP);

        Reservation reservation = new Reservation(
                passager,
                trajet,
                montee,
                descente,
                request.nombrePlaces(),
                montantTotal,
                LocalDateTime.now()
        );

        Reservation saved = reservationRepository.save(reservation);

        if (request.nombrePlaces() == placesRestantes) {
            trajet.marquerComplet();
        }

        log.info("Réservation créée : id={}, trajetId={}, places={}", saved.getId(), trajet.getId(), request.nombrePlaces());
        return reservationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> historiquePassager(Long passagerId) {

        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException(
                    "Passager introuvable"
            );
        }

        return reservationRepository
                .findByPassagerIdOrderByDateReservationDesc(passagerId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public ReservationResponse annulerReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Réservation introuvable"
                ));

        Long trajetId = reservation.getTrajet().getId();

        Trajet trajet = trajetRepository.findWithLockById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trajet introuvable"
                ));

        entityManager.refresh(reservation);

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            log.warn("Annulation refusée : réservation déjà annulée, id={}", reservationId);
            throw new IllegalStateException(
                    "Cette réservation est déjà annulée"
            );
        }

        if (!trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Impossible d'annuler une réservation après le départ"
            );
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new IllegalStateException(
                    "Le trajet ne permet plus l'annulation"
            );
        }

        reservation.marquerAnnulee();

        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.marquerPublie();
        }

        log.info("Réservation annulée : id={}, trajetId={}", reservationId, trajetId);
        return reservationMapper.toResponse(reservation);
    }


}