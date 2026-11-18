package ma.veltra.services.implementations;

import ma.veltra.dto.request.ConducteurCreateRequest;
import ma.veltra.dto.response.ConducteurResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.exceptions.ResourceAlreadyExistsException;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.ConducteurMapper;
import ma.veltra.repositories.ConducteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConducteurServiceImplTest {

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private ConducteurMapper conducteurMapper;

    @InjectMocks
    private ConducteurServiceImpl conducteurService;

    private ConducteurCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new ConducteurCreateRequest(
                "El Masghati",
                "Mohamed",
                "mohamed@example.com",
                "0612345678",
                "PERMIS123"
        );
    }

    @Test
    void shouldCreateDriverSuccessfully() {
        Conducteur conducteur = new Conducteur(
                request.nom(),
                request.prenom(),
                request.email(),
                request.telephone(),
                request.numeroPermis()
        );

        ConducteurResponse response = new ConducteurResponse(
                1L,
                request.nom(),
                request.prenom(),
                request.email(),
                request.telephone(),
                request.numeroPermis()
        );

        when(conducteurRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(conducteurRepository.existsByNumeroPermis(request.numeroPermis()))
                .thenReturn(false);

        when(conducteurMapper.toEntity(request))
                .thenReturn(conducteur);

        when(conducteurRepository.save(conducteur))
                .thenReturn(conducteur);

        when(conducteurMapper.toResponse(conducteur))
                .thenReturn(response);

        ConducteurResponse result = conducteurService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(request.email(), result.email());

        verify(conducteurRepository).save(conducteur);
        verify(conducteurMapper).toResponse(conducteur);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(conducteurRepository.existsByEmail(request.email()))
                .thenReturn(true);

        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> conducteurService.create(request)
        );

        assertEquals(
                "Un conducteur avec cet email existe déjà",
                exception.getMessage()
        );

        verify(conducteurRepository, never())
                .save(any(Conducteur.class));

        verify(conducteurRepository, never())
                .existsByNumeroPermis(anyString());
    }

    @Test
    void shouldRejectDuplicateLicenseNumber() {
        when(conducteurRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(conducteurRepository.existsByNumeroPermis(request.numeroPermis()))
                .thenReturn(true);
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> conducteurService.create(request)
        );

        assertEquals(
                "Un conducteur avec ce numéro de permis existe déjà",
                exception.getMessage()
        );

        verify(conducteurRepository, never())
                .save(any(Conducteur.class));
    }


    @Test
    void shouldFindDriverById() {
        Long id = 1L;

        Conducteur conducteur = new Conducteur(
                "El Masghati",
                "Mohamed",
                "mohamed@example.com",
                "0611111111",
                "PERMIS123"
        );

        ConducteurResponse response = new ConducteurResponse(
                id,
                "El Masghati",
                "Mohamed",
                "mohamed@example.com",
                "0611111111",
                "PERMIS123"
        );

        when(conducteurRepository.findById(id))
                .thenReturn(Optional.of(conducteur));

        when(conducteurMapper.toResponse(conducteur))
                .thenReturn(response);

        ConducteurResponse result = conducteurService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("mohamed@example.com", result.email());

        verify(conducteurRepository).findById(id);
        verify(conducteurMapper).toResponse(conducteur);
    }

    @Test
    void shouldThrowExceptionWhenDriverNotFound() {
        Long id = 999L;

        when(conducteurRepository.findById(id))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> conducteurService.findById(id)
        );

        assertEquals(
                "Conducteur introuvable avec l'identifiant : 999",
                exception.getMessage()
        );

        verify(conducteurRepository).findById(id);
        verifyNoInteractions(conducteurMapper);
    }

}
