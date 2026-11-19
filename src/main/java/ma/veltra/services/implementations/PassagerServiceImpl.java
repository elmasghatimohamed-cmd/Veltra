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

        if (passagerRepository.existsByEmail(request.email())){
            log.warn("Création passager refusée : email déjà utilisé");
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        Passager passager = passagerMapper.toEntity(request);
        Passager savedPassager = passagerRepository.save(passager);
        log.info("Passager créé : id={}", savedPassager.getId());
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

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> search(String nom, String email) {

        boolean hasNom = nom != null && !nom.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (hasNom == hasEmail) {
            throw new IllegalArgumentException(
                    "Veuillez fournir uniquement un critère : nom ou email"
            );
        }

        List<Passager> passagers = hasNom
                ? passagerRepository.findByNomContainingIgnoreCase(nom.trim())
                : passagerRepository.findByEmailContainingIgnoreCase(email.trim());

        return passagers.stream()
                .map(passagerMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public PassagerResponse update(
            Long id,
            PassagerUpdateRequest request
    ) {
        Passager passager = passagerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passager introuvable avec l'identifiant : " + id
                ));

        if (request.email() != null
                && !request.email().equals(passager.getEmail())
                && passagerRepository.existsByEmail(request.email())) {

            throw new ResourceAlreadyExistsException(
                    "Un passager avec cet email existe déjà"
            );
        }

        log.info("Mise à jour passager demandée : id={}", id);
        passager.modifierInformations(
                request.nom(),
                request.prenom(),
                request.email(),
                request.telephone()
        );

        return passagerMapper.toResponse(passager);
    }
}
