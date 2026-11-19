package ma.veltra.services;

import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.entities.Reservation;

import java.util.List;

public interface ReservationService {

    ReservationResponse creerReservation(ReservationCreateRequest request);

    List<ReservationResponse> historiquePassager(Long passagerId);
}
