package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.services.TrajetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

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
    public ResponseEntity<List<TrajetResponse>> rechercherTrajets(
            @RequestParam String villeDepart,
            @RequestParam String villeArrivee,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateDepart
    ) {
        return ResponseEntity.ok(
                trajetService.rechercherTrajets(
                        villeDepart,
                        villeArrivee,
                        dateDepart
                )
        );
    }
}
