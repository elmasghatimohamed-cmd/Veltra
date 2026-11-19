package ma.veltra.services.implementations;

import ma.veltra.dto.request.PointArretCreateRequest;
import ma.veltra.dto.request.TrajetCreateRequest;
import ma.veltra.dto.response.TrajetResponse;
import ma.veltra.entities.Conducteur;
import ma.veltra.entities.PointArret;
import ma.veltra.entities.Trajet;
import ma.veltra.entities.Vehicule;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.exceptions.ResourceNotFoundException;
import ma.veltra.mappers.TrajetMapper;
import ma.veltra.repositories.ConducteurRepository;
import ma.veltra.repositories.TrajetRepository;
import ma.veltra.repositories.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private TrajetMapper trajetMapper;

    @InjectMocks
    private TrajetServiceImpl trajetService;

    private LocalDateTime depart;
    private Conducteur conducteur;
    private Vehicule vehicule;

    @BeforeEach
    void setUp() {
        depart = LocalDateTime.now().plusDays(10);
        conducteur = mock(Conducteur.class);
        vehicule = mock(Vehicule.class);
    }

    private PointArretCreateRequest point(
            String ville,
            int ordre,
            LocalDateTime heure,
            String prix
    ) {
        return new PointArretCreateRequest(
                ville,
                ordre,
                heure,
                new BigDecimal(prix)
        );
    }

    private List<PointArretCreateRequest> parcoursValide() {
        return List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart.plusHours(3), "100.00"),
                point("Rabat", 2, depart.plusHours(6), "180.00")
        );
    }

    private TrajetCreateRequest request(
            List<PointArretCreateRequest> points
    ) {
        return new TrajetCreateRequest(
                1L, 2L, depart, 3, points
        );
    }

    private void mockConducteurEtVehicule() {
        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(2L))
                .thenReturn(Optional.of(vehicule));

        when(conducteur.getId()).thenReturn(1L);
        when(vehicule.getId()).thenReturn(2L);

        when(vehiculeRepository.existsByIdAndConducteurId(2L, 1L))
                .thenReturn(true);

        lenient().when(vehicule.getCapacite()).thenReturn(4);
    }


    private void verifierRefusAvecRessourcesValides(
            TrajetCreateRequest request,
            String message
    ) {
        mockConducteurEtVehicule();
        verifierRefus(request, message);
    }

    private void verifierRefus(
            TrajetCreateRequest request,
            String message
    ) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trajetService.create(request)
        );

        assertEquals(message, exception.getMessage());

        verify(trajetRepository, never())
                .saveAndFlush(any(Trajet.class));
    }

    @Test
    void create_shouldPublishValidTrip() {
        mockConducteurEtVehicule();

        Trajet saved = mock(Trajet.class);
        TrajetResponse response = mock(TrajetResponse.class);

        when(trajetRepository.saveAndFlush(any(Trajet.class)))
                .thenReturn(saved);

        when(trajetMapper.toResponse(saved))
                .thenReturn(response);

        TrajetResponse result = trajetService.create(
                request(parcoursValide())
        );

        assertSame(response, result);

        ArgumentCaptor<Trajet> captor =
                ArgumentCaptor.forClass(Trajet.class);

        verify(trajetRepository).saveAndFlush(captor.capture());
        verify(trajetMapper).toResponse(saved);

        Trajet created = captor.getValue();

        assertEquals(depart, created.getDateDepart());
        assertEquals(3, created.getPlacesProposees());
        assertEquals(3, created.getPointsArret().size());
        assertSame(conducteur, created.getConducteur());
        assertSame(vehicule, created.getVehicule());

        assertEquals(
                List.of("Nador", "Fes", "Rabat"),
                created.getPointsArret().stream()
                        .map(PointArret::getVille)
                        .toList()
        );

        assertTrue(created.getPointsArret().stream()
                .allMatch(p -> p.getTrajet() == created));
    }

    @Test
    void create_shouldSortStopsByOrder() {
        mockConducteurEtVehicule();

        Trajet saved = mock(Trajet.class);
        when(trajetRepository.saveAndFlush(any(Trajet.class)))
                .thenReturn(saved);

        List<PointArretCreateRequest> points = parcoursValide();

        TrajetCreateRequest request = request(List.of(
                points.get(2),
                points.get(0),
                points.get(1)
        ));

        trajetService.create(request);

        ArgumentCaptor<Trajet> captor =
                ArgumentCaptor.forClass(Trajet.class);

        verify(trajetRepository).saveAndFlush(captor.capture());

        assertEquals(
                List.of(0, 1, 2),
                captor.getValue().getPointsArret().stream()
                        .map(PointArret::getOrdre)
                        .toList()
        );
    }

    @Test
    void create_shouldRejectMissingConductor() {
        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trajetService.create(
                        request(parcoursValide())
                )
        );

        verify(trajetRepository, never())
                .saveAndFlush(any(Trajet.class));
    }

    @Test
    void create_shouldRejectMissingVehicle() {
        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trajetService.create(
                        request(parcoursValide())
                )
        );

        verify(trajetRepository, never())
                .saveAndFlush(any(Trajet.class));
    }

    @Test
    void create_shouldRejectVehicleFromAnotherDriver() {
        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(2L))
                .thenReturn(Optional.of(vehicule));

        when(conducteur.getId()).thenReturn(1L);
        when(vehicule.getId()).thenReturn(2L);

        verifierRefus(
                request(parcoursValide()),
                "Ce véhicule n'appartient pas au conducteur"
        );
    }

    @Test
    void create_shouldRejectPastDeparture() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        TrajetCreateRequest request = new TrajetCreateRequest(
                1L, 2L, past, 3, parcoursValide()
        );

        verifierRefusAvecRessourcesValides(
                request,
                "La date de départ doit être dans le futur"
        );
    }


    @Test
    void create_shouldRejectSeatsAboveCapacity() {

        verifierRefusAvecRessourcesValides(
                new TrajetCreateRequest(
                        1L, 2L, depart, 5, parcoursValide()
                ),
                "Le nombre de places proposées doit être compris entre 1 et 4"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    void create_shouldRejectInvalidOrders(
            List<Integer> orders,
            String expectedMessage
    ) {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", orders.get(0), depart, "0.00"),
                point("Fes", orders.get(1),
                        depart.plusHours(3), "100.00"),
                point("Rabat", orders.get(2),
                        depart.plusHours(6), "180.00")
        );

        verifierRefusAvecRessourcesValides(request(points), expectedMessage);
    }

    static Stream<Arguments> invalidOrders() {
        return Stream.of(
                Arguments.of(
                        List.of(0, 1, 1),
                        "L'ordre 1 est utilisé plusieurs fois"
                ),
                Arguments.of(
                        List.of(0, 2, 3),
                        "Ordre manquant des points d'arrêt. "
                ),
                Arguments.of(
                        List.of(1, 2, 3),
                        "Ordre manquant des points d'arrêt. "
                ),
                Arguments.of(
                        List.of(-1, 0, 1),
                        "L'ordre d'un point d'arrêt ne peut pas être négatif"
                )
        );
    }

    @Test
    void create_shouldRejectLessThanTwoStops() {
        verifierRefusAvecRessourcesValides(
                request(List.of(
                        point("Nador", 0, depart, "0.00")
                )),
                "Un trajet doit contenir au moins deux points d'arrêt"
        );
    }

    @Test
    void create_shouldRejectNullStop() {
        List<PointArretCreateRequest> points =
                new java.util.ArrayList<>(parcoursValide());

        points.set(1, null);

        verifierRefusAvecRessourcesValides(
                request(points),
                "Un point d'arrêt ne peut pas être null"
        );
    }

    @Test
    void create_shouldRejectDuplicateCity() {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart.plusHours(3), "100.00"),
                point(" fes ", 2, depart.plusHours(6), "180.00")
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "Une ville ne peut apparaître qu'une seule fois"
        );
    }

    @Test
    void create_shouldRejectWrongDepartureTime() {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart.plusMinutes(5), "0.00"),
                point("Fes", 1, depart.plusHours(3), "100.00")
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "L'heure du premier arrêt doit correspondre au départ"
        );
    }

    @Test
    void create_shouldRejectNonZeroStartingPrice() {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart, "10.00"),
                point("Fes", 1, depart.plusHours(3), "100.00")
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "Le prix au départ doit être égal à zéro"
        );
    }

    @Test
    void create_shouldRejectNonIncreasingTime() {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart, "100.00")
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "Les heures de passage doivent être strictement croissantes"
        );
    }

    @Test
    void create_shouldRejectNonIncreasingPrice() {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart.plusHours(3), "100.00"),
                point("Rabat", 2, depart.plusHours(6), "80.00")
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "Les prix doivent être strictement croissants"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPrices")
    void create_shouldRejectInvalidPrice(String price) {
        List<PointArretCreateRequest> points = List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart.plusHours(3), price)
        );

        verifierRefusAvecRessourcesValides(
                request(points),
                "Le prix doit être positif ou nul, avec au maximum 8 chiffres entiers et 2 décimales"
        );
    }

    static Stream<Arguments> invalidPrices() {
        return Stream.of(
                Arguments.of("-10.00"),
                Arguments.of("100.123"),
                Arguments.of("100000000.00")
        );
    }




    @Test
    void create_shouldRejectEqualPrices() {
        verifierRefusAvecRessourcesValides(request(List.of(
                point("Nador", 0, depart, "0.00"),
                point("Fes", 1, depart.plusHours(3), "100.00"),
                point("Rabat", 2, depart.plusHours(6), "100.00")
        )), "Les prix doivent être strictement croissants");
    }

    @Test
    void create_shouldAcceptSeatsAtVehicleCapacity() {
        mockConducteurEtVehicule();
        Trajet saved = mock(Trajet.class);
        when(trajetRepository.saveAndFlush(any(Trajet.class)))
                .thenReturn(saved);
        trajetService.create(new TrajetCreateRequest(
                1L, 2L, depart, 4, parcoursValide()));
        verify(trajetRepository).saveAndFlush(any(Trajet.class));
    }


    private Trajet preparerTrajetPourChangementStatut(StatutTrajet statut) {
        Trajet trajet = mock(Trajet.class);
        when(trajetRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(trajet.getStatut()).thenReturn(statut);
        return trajet;
    }

    @Test
    void annuler_devraitAnnulerTrajetPublie() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.PUBLIE);
        TrajetResponse response = mock(TrajetResponse.class);

        when(trajetMapper.toResponse(trajet)).thenReturn(response);

        assertSame(response, trajetService.annuler(1L));

        verify(trajet).marquerAnnule();
        verify(trajetMapper).toResponse(trajet);
    }

    @Test
    void annuler_devraitAnnulerTrajetComplet() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.COMPLET);

        trajetService.annuler(1L);

        verify(trajet).marquerAnnule();
    }

    @Test
    void annuler_devraitRefuserTrajetDejaAnnule() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.ANNULE);

        assertThrows(
                IllegalStateException.class,
                () -> trajetService.annuler(1L)
        );

        verify(trajet, never()).marquerAnnule();
        verifyNoInteractions(trajetMapper);
    }

    @Test
    void terminer_devraitTerminerTrajetPublieApresDepart() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.PUBLIE);
        TrajetResponse response = mock(TrajetResponse.class);

        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().minusHours(3));
        when(trajetMapper.toResponse(trajet)).thenReturn(response);

        assertSame(response, trajetService.terminer(1L));

        verify(trajet).marquerTermine();
    }

    @Test
    void terminer_devraitTerminerTrajetCompletApresDepart() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.COMPLET);
        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().minusDays(1));

        trajetService.terminer(1L);

        verify(trajet).marquerTermine();
    }

    @Test
    void terminer_devraitRefuserAvantDepart() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.PUBLIE);
        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().plusDays(1));

        assertThrows(
                IllegalStateException.class,
                () -> trajetService.terminer(1L)
        );

        verify(trajet, never()).marquerTermine();
    }

    @Test
    void terminer_devraitRefuserTrajetAnnule() {
        Trajet trajet = preparerTrajetPourChangementStatut(StatutTrajet.ANNULE);

        assertThrows(
                IllegalStateException.class,
                () -> trajetService.terminer(1L)
        );

        verify(trajet, never()).marquerTermine();
    }

    @Test
    void annuler_devraitRefuserTrajetIntrouvable() {
        when(trajetRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trajetService.annuler(1L)
        );
    }

    @Test
    void terminer_devraitRefuserTrajetIntrouvable() {
        when(trajetRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> trajetService.terminer(1L)
        );
    }

}
