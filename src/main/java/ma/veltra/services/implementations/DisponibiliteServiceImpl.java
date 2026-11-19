package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import ma.veltra.entities.Trajet;
import ma.veltra.enums.StatutReservation;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.repositories.ReservationRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.services.DisponibiliteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisponibiliteServiceImpl implements DisponibiliteService {

    private final TrajetRepository trajetRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public long calculerPlacesRestantes(Long trajetId) {

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trajet introuvable")
                );

        long placesReservees = reservationRepository
                .sumReservedSeatsByTrajetAndStatut(
                        trajetId,
                        StatutReservation.CONFIRMEE
                );

        return (long) trajet.getPlacesProposees() - placesReservees;
    }
}