package ma.veltra.services;

import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;

import java.util.List;

public interface ConducteurService {

    ConducteurResponse create(ConducteurCreateRequest request);

    ConducteurResponse findById(Long id);

    List<ConducteurResponse> findAll();

    List<ConducteurResponse> search(String nom, String email);
}
