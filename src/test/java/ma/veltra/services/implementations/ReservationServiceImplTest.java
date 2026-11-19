package ma.veltra.services.implementations;

import ma.veltra.dto.request.ReservationCreateRequest;
import ma.veltra.dto.response.ReservationResponse;
import ma.veltra.entities.*;
import ma.veltra.enums.StatutReservation;
import ma.veltra.enums.StatutTrajet;
import ma.veltra.mappers.ReservationMapper;
import ma.veltra.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private PointArretRepository pointArretRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Trajet trajet;
    private Passager passager;
    private PointArret montee;
    private PointArret descente;
    private ReservationCreateRequest request;

    @BeforeEach
    void setUp() {
        trajet = mock(Trajet.class);
        passager = mock(Passager.class);
        montee = mock(PointArret.class);
        descente = mock(PointArret.class);

        request = new ReservationCreateRequest(
                1L, 2L, 4L, 5L, 2
        );
    }

    private void preparerTrajetValide() {
        when(trajetRepository.findWithLockById(2L))
                .thenReturn(Optional.of(trajet));

        when(trajet.getStatut())
                .thenReturn(StatutTrajet.PUBLIE);

        when(trajet.getDateDepart())
                .thenReturn(LocalDateTime.now().plusDays(6));

        when(trajet.getId()).thenReturn(2L);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(passager));

        when(passager.getId()).thenReturn(1L);
    }

    private void preparerSegmentValide() {
        when(pointArretRepository.findById(4L))
                .thenReturn(Optional.of(montee));

        when(pointArretRepository.findById(5L))
                .thenReturn(Optional.of(descente));

        when(montee.getTrajet()).thenReturn(trajet);
        when(descente.getTrajet()).thenReturn(trajet);

        when(montee.getOrdre()).thenReturn(0);
        when(descente.getOrdre()).thenReturn(1);
    }

    @Test
    void creerReservation_devraitCalculerMontantCorrectement() {
        preparerTrajetValide();
        preparerSegmentValide();

        when(reservationRepository
                .existsByPassagerIdAndTrajetIdAndStatut(
                        1L, 2L, StatutReservation.CONFIRMEE
                )).thenReturn(false);

        when(reservationRepository
                .sumReservedSeatsByTrajetAndStatut(
                        2L, StatutReservation.CONFIRMEE
                )).thenReturn(0L);

        when(trajet.getPlacesProposees()).thenReturn(3);

        when(montee.getPrixDepuisDepart())
                .thenReturn(new BigDecimal("0.00"));

        when(descente.getPrixDepuisDepart())
                .thenReturn(new BigDecimal("100.00"));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);

                    return new ReservationResponse(
                            1L, 1L, 2L, 4L, 5L,
                            reservation.getNombrePlaces(),
                            reservation.getMontantTotal(),
                            reservation.getDateReservation(),
                            reservation.getStatut()
                    );
                });

        ReservationResponse response =
                reservationService.creerReservation(request);

        assertThat(response.montantTotal())
                .isEqualByComparingTo("200.00");

        assertThat(response.nombrePlaces()).isEqualTo(2);
        assertThat(response.statut())
                .isEqualTo(StatutReservation.CONFIRMEE);

        verify(reservationRepository).save(any(Reservation.class));
        verify(trajet, never()).marquerComplet();
    }

    @Test
    void creerReservation_devraitRefuserDoublon() {
        preparerTrajetValide();

        when(reservationRepository
                .existsByPassagerIdAndTrajetIdAndStatut(
                        1L, 2L, StatutReservation.CONFIRMEE
                )).thenReturn(true);

        assertThatThrownBy(() ->
                reservationService.creerReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà une réservation");

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void creerReservation_devraitRefuserPlacesInsuffisantes() {
        preparerTrajetValide();
        preparerSegmentValide();

        when(reservationRepository
                .sumReservedSeatsByTrajetAndStatut(
                        2L, StatutReservation.CONFIRMEE
                )).thenReturn(2L);

        when(trajet.getPlacesProposees()).thenReturn(3);

        assertThatThrownBy(() ->
                reservationService.creerReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("places disponibles insuffisant");

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void creerReservation_devraitMarquerTrajetComplet() {
        preparerTrajetValide();
        preparerSegmentValide();

        when(reservationRepository
                .sumReservedSeatsByTrajetAndStatut(
                        2L, StatutReservation.CONFIRMEE
                )).thenReturn(1L);

        when(trajet.getPlacesProposees()).thenReturn(3);

        when(montee.getPrixDepuisDepart())
                .thenReturn(new BigDecimal("0.00"));

        when(descente.getPrixDepuisDepart())
                .thenReturn(new BigDecimal("100.00"));

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        reservationService.creerReservation(request);

        verify(trajet).marquerComplet();
        verify(reservationRepository).save(any(Reservation.class));
    }
}
