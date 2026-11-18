package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.response.PassagerResponse;
import ma.veltra.entities.Passager;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.mappers.PassagerMapper;
import ma.veltra.repositories.PassagerRepository;
import ma.veltra.services.PassagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;

    @Override
    @Transactional
    public PassagerResponse create(PassagerCreateRequest request) {

        if (passagerRepository.existsByEmail(request.email())){
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        Passager passager = passagerMapper.toEntity(request);
        Passager savedPassager = passagerRepository.save(passager);
        return passagerMapper.toResponse(savedPassager);
    }
}
