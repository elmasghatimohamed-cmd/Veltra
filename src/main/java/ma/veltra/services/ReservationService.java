package ma.veltra.services;

import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.entities.Reservation;

public interface ReservationService {

    ReservationResponse creerReservation(ReservationCreateRequest request);
}
