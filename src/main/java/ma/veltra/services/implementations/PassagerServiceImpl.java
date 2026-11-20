package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.PassagerCreateRequest;
import ma.veltra.dto.request.PassagerUpdateRequest;
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
@Slf4j
public class PassagerServiceImpl implements PassagerService {

    private final PassagerRepository passagerRepository;
    private final PassagerMapper passagerMapper;

    @Override
    @Transactional
    public PassagerResponse create(PassagerCreateRequest request) {
        verifierEmailDisponible(request.email(), null);
        Passager saved = passagerRepository.save(passagerMapper.toEntity(request));
        log.info("Passager créé : id={}", saved.getId());
        return passagerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse findById(Long id) {
        return passagerMapper.toResponse(trouverPassager(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> findAll() {
        return passagerRepository.findAll().stream()
                .map(passagerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> search(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email de recherche est obligatoire");
        }
        return passagerRepository.findByEmailContainingIgnoreCase(email.trim()).stream()
                .map(passagerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PassagerResponse update(Long id, PassagerUpdateRequest request) {
        Passager passager = trouverPassager(id);
        verifierEmailDisponible(request.email(), passager.getEmail());
        passager.modifierInformations(request.nom(), request.prenom(), request.email(), request.telephone());
        log.info("Mise à jour passager demandée : id={}", id);
        return passagerMapper.toResponse(passager);
    }

    private Passager trouverPassager(Long id) {
        return passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'identifiant : " + id));
    }

    private void verifierEmailDisponible(String email, String emailActuel) {
        if (email != null && !email.equals(emailActuel) && passagerRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Un passager avec cet email existe déjà");
        }
    }
}
