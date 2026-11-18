package ma.veltra.services;

import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;

import java.util.List;

public interface TrajetService {

    TrajetResponse create(TrajetCreateRequest request);

    TrajetResponse findById(Long id);

    List<TrajetResponse> findByConducteurId(Long conducteurId);
}
