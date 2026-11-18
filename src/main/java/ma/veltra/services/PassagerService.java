package ma.veltra.services;

import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.response.PassagerResponse;

public interface PassagerService {

    PassagerResponse create(PassagerCreateRequest request);

    PassagerResponse findById(Long id);
}
