package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.VehiculeCreateRequest;
import ma.veltra.dto.response.VehiculeResponse;
import ma.veltra.services.VehiculeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponse> create(
            @Valid @RequestBody VehiculeCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehiculeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(vehiculeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehiculeResponse>> findByConducteurId(
            @RequestParam Long conducteurId
    ) {
        return ResponseEntity.ok(
                vehiculeService.findByConducteurId(conducteurId)
        );
    }
}
