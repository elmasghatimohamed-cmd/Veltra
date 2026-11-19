package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PassagerRepository passagerRepository;
    private final PointArretRepository pointArretRepository;
    private final ReservationMapper reservationMapper;

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

        return reservationMapper.toResponse(saved);
    }
}