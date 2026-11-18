package ma.veltra.services;

import ma.veltra.dto.request.VehiculeCreateRequest;
import ma.veltra.dto.request.VehiculeUpdateRequest;
import ma.veltra.dto.response.VehiculeResponse;

import java.util.List;

public interface VehiculeService {

    VehiculeResponse create(VehiculeCreateRequest request);

    VehiculeResponse findById(Long id);

    List<VehiculeResponse> findByConducteurId(Long conducteurId);

    VehiculeResponse update(Long id, VehiculeUpdateRequest request);

    void delete(Long id);
}
