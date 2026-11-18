package ma.veltra.services.implementations;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ConducteurMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.services.ConducteurService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConducteurServiceImpl implements ConducteurService {

    private final ConducteurRepository conducteurRepository;
    private final ConducteurMapper conducteurMapper;

    @Override
    @Transactional
    public ConducteurResponse create(ConducteurCreateRequest request) {

        if (conducteurRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec cet email existe déjà"
            );
        }

        if (conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            throw new ResourceAlreadyExistsException(
                    "Un conducteur avec ce numéro de permis existe déjà"
            );
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        Conducteur savedConducteur = conducteurRepository.save(conducteur);
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
}
