package ma.veltra.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.services.ConducteurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
@RequiredArgsConstructor
public class ConducteurController {

    private final ConducteurService conducteurService;

    @PostMapping
    public ResponseEntity<ConducteurResponse> create(
            @Valid @RequestBody ConducteurCreateRequest request
            ){
        ConducteurResponse response = conducteurService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConducteurResponse> findById(
            @PathVariable Long id
    ){
        ConducteurResponse response = conducteurService.findById(id);
        return  ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConducteurResponse>> findAll(){
        return ResponseEntity.ok(conducteurService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ConducteurResponse>> search(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String email
    ){
        return ResponseEntity.ok(conducteurService.search(nom, email));
    }
}
