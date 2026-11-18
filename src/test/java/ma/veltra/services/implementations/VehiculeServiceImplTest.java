package ma.veltra.services.implementations;

import ma.veltra.entities.Vehicule;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.VehiculeMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.repositories.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @InjectMocks
    private VehiculeServiceImpl vehiculeService;

    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        vehicule = new Vehicule(
                "Dacia",
                "Logan",
                "12345-A-6",
                "Blanc",
                4,
                null
        );
    }

    @Test
    void delete_shouldDeleteVehicle_whenNoTripExists() {

        Long vehiculeId = 1L;

        when(vehiculeRepository.findById(vehiculeId))
                .thenReturn(Optional.of(vehicule));

        when(trajetRepository.existsByVehiculeId(vehiculeId))
                .thenReturn(false);

        vehiculeService.delete(vehiculeId);

        verify(vehiculeRepository).delete(vehicule);
        verify(vehiculeRepository).flush();
    }

    @Test
    void delete_shouldThrowNotFound_whenVehicleDoesNotExist() {

        Long vehiculeId = 999L;

        when(vehiculeRepository.findById(vehiculeId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> vehiculeService.delete(vehiculeId)
        );

        verify(vehiculeRepository, never())
                .delete(any(Vehicule.class));

        verifyNoInteractions(trajetRepository);
    }


    @Test
    void delete_shouldRejectDeletion_whenVehicleHasTrips() {

        Long vehiculeId = 1L;

        when(vehiculeRepository.findById(vehiculeId))
                .thenReturn(Optional.of(vehicule));

        when(trajetRepository.existsByVehiculeId(vehiculeId))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> vehiculeService.delete(vehiculeId)
        );

        verify(vehiculeRepository).findById(vehiculeId);
        verify(trajetRepository).existsByVehiculeId(vehiculeId);

        verify(vehiculeRepository, never())
                .delete(any(Vehicule.class));

        verify(vehiculeRepository, never()).flush();
    }

}
