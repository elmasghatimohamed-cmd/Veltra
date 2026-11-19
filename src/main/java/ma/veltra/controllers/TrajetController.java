package ma.veltra.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.DisponibiliteResponse;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.services.DisponibiliteService;
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
    private final DisponibiliteService disponibiliteService;

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

    @GetMapping("/conducteur/{conducteurId}")
    public ResponseEntity<List<TrajetResponse>> findByConducteurId(
            @PathVariable Long conducteurId
    ) {
        return ResponseEntity.ok(
                trajetService.findByConducteurId(conducteurId)
        );
    }

    @PatchMapping("/{id}/annulation")
    public ResponseEntity<TrajetResponse> annuler(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(trajetService.annuler(id));
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


    @GetMapping("/{trajetId}/disponibilite")
    public ResponseEntity<DisponibiliteResponse> consulterDisponibilite(
            @PathVariable Long trajetId
    ) {
        long placesRestantes = disponibiliteService.calculerPlacesRestantes(trajetId);

        return ResponseEntity.ok(new DisponibiliteResponse(trajetId, placesRestantes));
    }

}
