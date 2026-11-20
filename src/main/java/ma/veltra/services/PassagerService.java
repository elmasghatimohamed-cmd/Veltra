package ma.veltra.services;

import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.request.PassagerUpdateRequest;
import ma.veltra.dto.response.PassagerResponse;

import java.util.List;

public interface PassagerService {

    PassagerResponse create(PassagerCreateRequest request);

    PassagerResponse findById(Long id);

    List<PassagerResponse> findAll();

    List<PassagerResponse> search(String email);

    PassagerResponse update(Long id, PassagerUpdateRequest request);
}
