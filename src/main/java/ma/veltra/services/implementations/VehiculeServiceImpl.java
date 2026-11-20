package ma.veltra.services.implementations;

import lombok.RequiredArgsConstructor;
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
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final TrajetRepository trajetRepository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse create(VehiculeCreateRequest request) {
        Conducteur conducteur = conducteurRepository.findById(request.conducteurId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conducteur introuvable avec l'identifiant : " + request.conducteurId()));
        if (vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            throw new ResourceAlreadyExistsException("Un véhicule avec cette immatriculation existe déjà");
        }
        Vehicule vehicule = new Vehicule(request.marque(), request.modele(), request.immatriculation(),
                request.couleur(), request.capacite(), conducteur);
        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse findById(Long id) {
        return vehiculeMapper.toResponse(trouverVehicule(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> findByConducteurId(Long conducteurId) {
        if (!conducteurRepository.existsById(conducteurId)) {
            throw new ResourceNotFoundException(
                    "Conducteur introuvable avec l'identifiant : " + conducteurId);
        }
        return vehiculeRepository.findByConducteurId(conducteurId).stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VehiculeResponse update(Long id, VehiculeUpdateRequest request) {
        Vehicule vehicule = trouverVehicule(id);
        vehicule.modifierCaracteristiques(request.marque(), request.modele(),
                request.couleur(), request.capacite());
        return vehiculeMapper.toResponse(vehicule);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Vehicule vehicule = trouverVehicule(id);
        if (trajetRepository.existsByVehiculeId(id)) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce vehicule: il est associe a un ou plusieurs trajets");
        }
        vehiculeRepository.delete(vehicule);
        vehiculeRepository.flush();
    }

    private Vehicule trouverVehicule(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule introuvable avec l'identifiant : " + id));
    }
}
