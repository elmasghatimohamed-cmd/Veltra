package ma.veltra.services;

import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;

public interface ConducteurService {

    ConducteurResponse create(ConducteurCreateRequest request);

    ConducteurResponse findById(Long id);
}
