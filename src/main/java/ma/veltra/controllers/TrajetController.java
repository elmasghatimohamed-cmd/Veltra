package ma.veltra.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.services.ReservationService;
import ma.veltra.services.TrajetService;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<TrajetResponse> create(
            @Valid @RequestBody TrajetCreateRequest request
    ) {
        TrajetResponse response = trajetService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrajetResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(trajetService.findById(id));
    }

    @PatchMapping("/{id}/annulation")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {

        trajetService.annuler(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/terminaison")
    public ResponseEntity<TrajetResponse> terminer(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(trajetService.terminer(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TrajetResponse>> rechercherTrajets(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateDepart,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false)
            @Min(1)
            Integer nombrePlaces,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                trajetService.rechercherTrajets(
                        villeDepart,
                        villeArrivee,
                        dateDepart,
                        prixMax,
                        nombrePlaces,
                        pageable)
        );
    }

    @GetMapping("/{trajetId}/reservations")
    public ResponseEntity<List<ReservationResponse>> reservationsDuTrajet(
            @PathVariable Long trajetId
    ) {
        return ResponseEntity.ok(
                reservationService.reservationsDuTrajet(trajetId)
        );
    }
}
