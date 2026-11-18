package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.services.TrajetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
