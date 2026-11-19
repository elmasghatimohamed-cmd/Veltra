package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.services.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> creerReservation(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResponse response =
                reservationService.creerReservation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}