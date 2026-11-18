package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.services.TrajetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
