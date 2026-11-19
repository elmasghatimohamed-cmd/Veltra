package ma.veltra.services.implementations;

import ma.veltra.dto.request.ConducteurUpdateRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ConducteurMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.services.ConducteurService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    @Transactional
    public ConducteurResponse create(ConducteurCreateRequest request) {

        if (conducteurRepository.existsByEmail(request.email())) {
            log.warn("Création conducteur refusée : email déjà utilisé");
            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec cet email existe déjà"
            );
        }

        if (conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            log.warn("Création conducteur refusée : permis déjà utilisé");
            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec ce numéro de permis existe déjà"
            );
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        log.info("Création conducteur demandée");
        Conducteur savedConducteur = conducteurRepository.save(conducteur);
        log.info("Conducteur créé : id={}", savedConducteur.getId());
        return conducteurMapper.toResponse(savedConducteur);
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponse findById(Long id) {

        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant : " + id
                ));
        return conducteurMapper.toResponse(conducteur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> findAll() {
        return conducteurRepository.findAll()
                .stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> search(String nom, String email) {

        boolean hasNom = nom != null && !nom.isEmpty();
        boolean hasEmail = email != null && !email.isEmpty();

        if (hasNom == hasEmail) {
            throw new IllegalArgumentException(
                    "Veuillez fournir uniquement un critère de recherche: nom ou email"
            );
        }

        List<Conducteur> conducteurs = hasNom
                ? conducteurRepository.findByNomContainingIgnoreCase(nom.trim())
                : conducteurRepository.findByEmailContainingIgnoreCase(email.trim());

        return conducteurs.stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConducteurResponse update(
            Long id,
            ConducteurUpdateRequest request
    ){
        Conducteur conducteur = conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant : " + id
                ));

        if (request.email() != null
                && !request.email().equals(conducteur.getEmail())
                && conducteurRepository.existsByEmail(request.email())) {

            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec cet email existe déjà"
            );
        }

        if (request.numeroPermis() != null
                && !request.numeroPermis().equals(conducteur.getNumeroPermis())
                && conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {

            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec ce numéro de permis existe déjà"
            );
        }

        log.info("Mise à jour conducteur demandée : id={}", id);
        conducteur.modifierInformations(
                request.nom(),
                request.prenom(),
                request.email(),
                request.telephone(),
                request.numeroPermis()
        );

        return conducteurMapper.toResponse(conducteur);
    }
}
