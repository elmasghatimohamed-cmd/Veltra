package ma.veltra.services;

import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;

public interface TrajetService {

    TrajetResponse create(TrajetCreateRequest request);
}
