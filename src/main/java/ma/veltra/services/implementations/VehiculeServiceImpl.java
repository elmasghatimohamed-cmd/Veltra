package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import ma.veltra.dto.request.VehiculeCreateRequest;
import ma.veltra.dto.response.VehiculeResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.entities.Vehicule;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.VehiculeMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.VehiculeRepository;
import ma.veltra.services.VehiculeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse create(VehiculeCreateRequest request) {

        Conducteur conducteur = conducteurRepository
                .findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant : "
                                + request.conducteurId()
                ));

        if (vehiculeRepository.existsByImmatriculation(
                request.immatriculation())) {

            throw new ResourceAlreadyExistsException(
                    "Un véhicule avec cette immatriculation existe déjà"
            );
        }

        Vehicule vehicule = new Vehicule(
                request.marque(),
                request.modele(),
                request.immatriculation(),
                request.couleur(),
                request.capacite(),
                conducteur
        );

        Vehicule savedVehicule = vehiculeRepository.save(vehicule);

        return vehiculeMapper.toResponse(savedVehicule);
    }
}
