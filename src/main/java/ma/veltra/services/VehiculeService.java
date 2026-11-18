package ma.veltra.services;

import ma.veltra.dto.request.VehiculeCreateRequest;
import ma.veltra.dto.response.VehiculeResponse;

public interface VehiculeService {

    VehiculeResponse create(VehiculeCreateRequest request);
}
