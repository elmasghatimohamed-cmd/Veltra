package ma.veltra.services.implementations;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.entities.Conducteur;
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
            throw new IllegalArgumentException(
                    "Un conducteur avec cet email existe déjà"
            );
        }

        if (conducteurRepository.existsByNumeroPermis(request.numeroPermis())) {
            throw new IllegalArgumentException(
                    "Un conducteur avec ce numéro de permis existe déjà"
            );
        }

        Conducteur conducteur = conducteurMapper.toEntity(request);
        Conducteur savedConducteur = conducteurRepository.save(conducteur);
        return conducteurMapper.toResponse(savedConducteur);
    }
}
