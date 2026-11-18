package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.response.PassagerResponse;
import ma.veltra.services.PassagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/passagers")
public class PassagerController {

    private final PassagerService passagerService;

    @PostMapping
    public ResponseEntity<PassagerResponse> create(
            @Valid @RequestBody PassagerCreateRequest request
    ){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(passagerService.create(request));
    }
}
