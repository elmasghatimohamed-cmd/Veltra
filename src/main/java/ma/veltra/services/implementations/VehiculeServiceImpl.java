package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.veltra.dto.request.VehiculeCreateRequest;
import ma.veltra.dto.request.VehiculeUpdateRequest;
import ma.veltra.dto.response.VehiculeResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.entities.Vehicule;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.VehiculeMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.repositories.VehiculeRepository;
import ma.veltra.services.VehiculeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final TrajetRepository trajetRepository;
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

        log.info("Véhicule créé : id={}, conducteurId={}", savedVehicule.getId(), request.conducteurId());
        return vehiculeMapper.toResponse(savedVehicule);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse findById(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'identifiant : " + id
                ));

        return vehiculeMapper.toResponse(vehicule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByConducteurId(Long conducteurId) {

        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException(
                    "Conducteur introuvable avec l'identifiant : " + conducteurId
            );
        }

        return vehiculeRepository.findByConducteurId(conducteurId)
                .stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public VehiculeResponse update(
            Long id,
            VehiculeUpdateRequest request
    ) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'identifiant : " + id
                ));

        log.info("Mise à jour véhicule demandée : id={}", id);
        vehicule.modifierCaracteristiques(
                request.marque(),
                request.modele(),
                request.couleur(),
                request.capacite()
        );

        return vehiculeMapper.toResponse(vehicule);
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'identifiant : " + id
                ));

        if (trajetRepository.existsByVehiculeId(id)) {
            log.warn("Suppression véhicule refusée : id={} associé à un trajet", id);
            throw new IllegalStateException(
                    "Impossible de supprimer ce vehicule: il est associe a un ou plusieurs trajets"
            );
        }

        vehiculeRepository.delete(vehicule);
        log.info("Véhicule supprimé : id={}", id);
        vehiculeRepository.flush();
    }

}
