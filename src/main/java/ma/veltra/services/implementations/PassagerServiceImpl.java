package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.response.PassagerResponse;
import ma.veltra.entities.Passager;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.PassagerMapper;
import ma.veltra.repositories.PassagerRepository;
import ma.veltra.services.PassagerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse findById(Long id) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'identifiant : " + id
                ));

        return passagerMapper.toResponse(passager);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> findAll() {
        return passagerRepository.findAll()
                .stream()
                .map(passagerMapper::toResponse)
                .toList();
    }
}
