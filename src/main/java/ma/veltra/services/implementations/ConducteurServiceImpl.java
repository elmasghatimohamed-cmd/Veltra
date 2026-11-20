package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.request.ConducteurUpdateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ConducteurMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.services.ConducteurService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        verifierEmailDisponible(request.email(), null);
        verifierPermisDisponible(request.numeroPermis(), null);

        Conducteur saved = conducteurRepository.save(conducteurMapper.toEntity(request));
        log.info("Conducteur créé : id={}", saved.getId());
        return conducteurMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponse findById(Long id) {
        return conducteurMapper.toResponse(trouverConducteur(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> findAll() {
        return conducteurRepository.findAll().stream()
                .map(conducteurMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> search(String nom, String email) {
        boolean hasNom = nom != null && !nom.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        if (hasNom == hasEmail) {
            throw new IllegalArgumentException(
                    "Veuillez fournir exactement un critère de recherche : nom ou email");
        }

        List<Conducteur> resultats = hasNom
                ? conducteurRepository.findByNomContainingIgnoreCase(nom.trim())
                : conducteurRepository.findByEmailContainingIgnoreCase(email.trim());
        return resultats.stream().map(conducteurMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ConducteurResponse update(Long id, ConducteurUpdateRequest request) {
        Conducteur conducteur = trouverConducteur(id);
        verifierEmailDisponible(request.email(), conducteur.getEmail());
        verifierPermisDisponible(request.numeroPermis(), conducteur.getNumeroPermis());

        conducteur.modifierInformations(request.nom(), request.prenom(), request.email(),
                request.telephone(), request.numeroPermis());
        log.info("Mise à jour conducteur demandée : id={}", id);
        return conducteurMapper.toResponse(conducteur);
    }

    private Conducteur trouverConducteur(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant : " + id));
    }

    private void verifierEmailDisponible(String email, String emailActuel) {
        if (email != null && !email.equals(emailActuel)
                && conducteurRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Un conducteur avec cet email existe déjà");
        }
    }

    private void verifierPermisDisponible(String permis, String permisActuel) {
        if (permis != null && !permis.equals(permisActuel)
                && conducteurRepository.existsByNumeroPermis(permis)) {
            throw new ResourceAlreadyExistsException("Un conducteur avec ce numéro de permis existe déjà");
        }
    }
}
