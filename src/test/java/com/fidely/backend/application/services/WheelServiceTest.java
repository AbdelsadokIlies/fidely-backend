package com.fidely.backend.application.services;

import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.IWheelRepository;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de gestion des roues.
 *
 * <p>Ces tests utilisent uniquement Mockito et ne nécessitent
 * aucune connexion à la base de données.</p>
 */
@ExtendWith(MockitoExtension.class)
class WheelServiceTest {

    @Mock
    private IWheelRepository wheelRepository;

    @Mock
    private IUserRepository userRepository;

    private WheelService wheelService;

    private UUID userId;
    private UUID merchantId;
    private UUID wheelId;

    private MerchantManager merchantManager;

    /**
     * Initialise les données communes aux tests.
     */
    @BeforeEach
    void setUp() {
        wheelService = new WheelService(
                wheelRepository,
                userRepository
        );

        userId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        wheelId = UUID.randomUUID();

        merchantManager = new MerchantManager(
                userId,
                merchantId,
                "merchant@test.com",
                "John",
                "Doe",
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    /**
     * Vérifie que le service récupère la roue d'un marchand.
     */
    @Test
    void shouldGetWheelByMerchantId() {
        Wheel wheel = createWheel();

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        Wheel result = wheelService.getWheelByMerchantId(merchantId);

        assertSame(wheel, result);

        verify(wheelRepository).findByMerchantId(merchantId);
    }

    /**
     * Vérifie qu'une erreur est levée lorsqu'aucune roue n'existe
     * pour le marchand.
     */
    @Test
    void shouldThrowWhenWheelDoesNotExist() {
        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.getWheelByMerchantId(merchantId)
        );

        assertEquals("Wheel not found", exception.getMessage());

        verify(wheelRepository).findByMerchantId(merchantId);
    }

    /**
     * Vérifie que le service récupère la roue du marchand
     * associé au manager connecté.
     */
    @Test
    void shouldGetWheelForManager() {
        Wheel wheel = createWheel();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        Wheel result = wheelService.getWheelForManager(userId);

        assertSame(wheel, result);

        verify(userRepository).findById(userId);
        verify(wheelRepository).findByMerchantId(merchantId);
    }

    /**
     * Vérifie que le service crée une nouvelle roue lorsqu'aucune
     * roue n'existe encore pour le marchand.
     */
    @Test
    void shouldCreateWheelForManager() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.empty());

        when(wheelRepository.saveWheel(any(Wheel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wheel result = wheelService.updateWheelForManager(
                userId,
                "Ma roue",
                true,
                30,
                true
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(merchantId, result.getMerchantId());
        assertEquals("Ma roue", result.getName());
        assertTrue(result.isActive());
        assertEquals(30, result.getMinIntervalMinutes());
        assertTrue(result.isRequiresValidatedPurchase());
        assertNotNull(result.getCreatedAt());

        verify(userRepository).findById(userId);
        verify(wheelRepository).findByMerchantId(merchantId);
        verify(wheelRepository).saveWheel(any(Wheel.class));
    }

    /**
     * Vérifie que le service met à jour une roue existante
     * sans modifier son identifiant ni sa date de création.
     */
    @Test
    void shouldUpdateExistingWheelForManager() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);

        Wheel existingWheel = new Wheel(
                wheelId,
                merchantId,
                "Ancienne roue",
                false,
                10,
                false,
                createdAt
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(existingWheel));

        when(wheelRepository.saveWheel(any(Wheel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wheel result = wheelService.updateWheelForManager(
                userId,
                "Nouvelle roue",
                true,
                60,
                true
        );

        assertEquals(wheelId, result.getId());
        assertEquals(merchantId, result.getMerchantId());
        assertEquals("Nouvelle roue", result.getName());
        assertTrue(result.isActive());
        assertEquals(60, result.getMinIntervalMinutes());
        assertTrue(result.isRequiresValidatedPurchase());
        assertEquals(createdAt, result.getCreatedAt());

        verify(userRepository).findById(userId);
        verify(wheelRepository).findByMerchantId(merchantId);
        verify(wheelRepository).saveWheel(any(Wheel.class));
    }

    /**
     * Vérifie qu'un nom vide est refusé lors de la création
     * ou de la modification d'une roue.
     */
    @Test
    void shouldRejectBlankWheelName() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.updateWheelForManager(
                        userId,
                        " ",
                        true,
                        30,
                        true
                )
        );

        assertEquals("name must not be blank", exception.getMessage());

        verify(wheelRepository, never()).saveWheel(any(Wheel.class));
    }

    /**
     * Vérifie qu'un intervalle négatif est refusé.
     */
    @Test
    void shouldRejectNegativeMinInterval() {
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.updateWheelForManager(
                        userId,
                        "Ma roue",
                        true,
                        -1,
                        true
                )
        );

        assertEquals(
                "minIntervalMinutes must not be negative",
                exception.getMessage()
        );

        verify(wheelRepository, never()).saveWheel(any(Wheel.class));
    }

    /**
     * Vérifie que le service récupère les lots d'une roue.
     */
    @Test
    void shouldGetPrizes() {
        WheelPrize prize = createPrize();

        when(wheelRepository.findPrizesByWheelId(wheelId))
                .thenReturn(List.of(prize));

        List<WheelPrize> result = wheelService.getPrizes(wheelId);

        assertEquals(1, result.size());
        assertSame(prize, result.get(0));

        verify(wheelRepository).findPrizesByWheelId(wheelId);
    }

    /**
     * Vérifie qu'un manager peut enregistrer un lot appartenant
     * à sa propre roue.
     */
    @Test
    void shouldSavePrizeForManager() {
        Wheel wheel = createWheel();
        WheelPrize prize = createPrize();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.savePrize(prize))
                .thenReturn(prize);

        WheelPrize result = wheelService.savePrizeForManager(
                userId,
                prize
        );

        assertSame(prize, result);

        verify(userRepository).findById(userId);
        verify(wheelRepository).findByMerchantId(merchantId);
        verify(wheelRepository).savePrize(prize);
    }

    /**
     * Vérifie qu'un manager ne peut pas enregistrer un lot
     * appartenant à une autre roue.
     */
    @Test
    void shouldRejectPrizeFromAnotherWheel() {
        Wheel wheel = createWheel();

        WheelPrize prize = new WheelPrize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Lot externe",
                10,
                LocalDateTime.now()
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.savePrizeForManager(
                        userId,
                        prize
                )
        );

        assertEquals(
                "Prize does not belong to the manager's wheel",
                exception.getMessage()
        );

        verify(wheelRepository, never()).savePrize(any(WheelPrize.class));
    }

    /**
     * Vérifie qu'un manager peut supprimer un lot appartenant
     * à sa propre roue.
     */
    @Test
    void shouldDeletePrizeForManager() {
        Wheel wheel = createWheel();
        WheelPrize prize = createPrize();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.findPrizeById(prize.getId()))
                .thenReturn(Optional.of(prize));

        wheelService.deletePrizeForManager(
                userId,
                prize.getId()
        );

        verify(wheelRepository).findByMerchantId(merchantId);
        verify(wheelRepository).findPrizeById(prize.getId());
        verify(wheelRepository).deletePrizeById(prize.getId());
    }

    /**
     * Vérifie qu'un manager ne peut pas supprimer un lot
     * appartenant à une autre roue.
     */
    @Test
    void shouldRejectDeletingPrizeFromAnotherWheel() {
        Wheel wheel = createWheel();

        WheelPrize prize = new WheelPrize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Lot externe",
                10,
                LocalDateTime.now()
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(merchantManager));

        when(wheelRepository.findByMerchantId(merchantId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.findPrizeById(prize.getId()))
                .thenReturn(Optional.of(prize));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.deletePrizeForManager(
                        userId,
                        prize.getId()
                )
        );

        assertEquals(
                "Prize does not belong to the manager's wheel",
                exception.getMessage()
        );

        verify(wheelRepository, never())
                .deletePrizeById(prize.getId());
    }

    /**
     * Vérifie qu'un utilisateur qui n'est pas un manager de marchand
     * ne peut pas accéder aux opérations de gestion de roue.
     */
    @Test
    void shouldRejectNonMerchantManager() {
        User customer = mock(User.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(customer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.getWheelForManager(userId)
        );

        assertEquals(
                "User is not a merchant manager",
                exception.getMessage()
        );

        verify(wheelRepository, never()).findByMerchantId(any(UUID.class));
    }

    @Test
    void shouldSpinWheelAndReturnPrize() {
        UUID wheelId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Wheel wheel = new Wheel(
                wheelId,
                UUID.randomUUID(),
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        WheelPrize firstPrize = new WheelPrize(
                UUID.randomUUID(),
                wheelId,
                "10% de réduction",
                50,
                now
        );

        WheelPrize secondPrize = new WheelPrize(
                UUID.randomUUID(),
                wheelId,
                "5€ offerts",
                50,
                now
        );

        when(wheelRepository.findById(wheelId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.findPrizesByWheelId(wheelId))
                .thenReturn(List.of(firstPrize, secondPrize));

        WheelPrize result = wheelService.spin(wheelId);

        assertNotNull(result);
        assertTrue(
                result.getId().equals(firstPrize.getId())
                        || result.getId().equals(secondPrize.getId())
        );

        verify(wheelRepository).findById(wheelId);
        verify(wheelRepository).findPrizesByWheelId(wheelId);
    }

    @Test
    void shouldRejectSpinWhenWheelDoesNotExist() {
        UUID wheelId = UUID.randomUUID();

        when(wheelRepository.findById(wheelId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.spin(wheelId)
        );

        assertEquals("Wheel not found", exception.getMessage());

        verify(wheelRepository).findById(wheelId);
        verify(wheelRepository, never()).findPrizesByWheelId(wheelId);
    }

    @Test
    void shouldRejectSpinWhenWheelIsInactive() {
        UUID wheelId = UUID.randomUUID();

        Wheel wheel = new Wheel(
                wheelId,
                UUID.randomUUID(),
                "Roue Fidely",
                false,
                0,
                false,
                LocalDateTime.now()
        );

        when(wheelRepository.findById(wheelId))
                .thenReturn(Optional.of(wheel));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.spin(wheelId)
        );

        assertEquals("Wheel is not active", exception.getMessage());

        verify(wheelRepository).findById(wheelId);
        verify(wheelRepository, never()).findPrizesByWheelId(wheelId);
    }

    @Test
    void shouldRejectSpinWhenWheelHasNoPrizes() {
        UUID wheelId = UUID.randomUUID();

        Wheel wheel = new Wheel(
                wheelId,
                UUID.randomUUID(),
                "Roue Fidely",
                true,
                0,
                false,
                LocalDateTime.now()
        );

        when(wheelRepository.findById(wheelId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.findPrizesByWheelId(wheelId))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.spin(wheelId)
        );

        assertEquals("Wheel has no prizes", exception.getMessage());
    }

    @Test
    void shouldRejectSpinWhenPrizeWeightsAreZero() {
        UUID wheelId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Wheel wheel = new Wheel(
                wheelId,
                UUID.randomUUID(),
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        WheelPrize firstPrize = new WheelPrize(
                UUID.randomUUID(),
                wheelId,
                "10% de réduction",
                0,
                now
        );

        WheelPrize secondPrize = new WheelPrize(
                UUID.randomUUID(),
                wheelId,
                "5€ offerts",
                0,
                now
        );

        when(wheelRepository.findById(wheelId))
                .thenReturn(Optional.of(wheel));

        when(wheelRepository.findPrizesByWheelId(wheelId))
                .thenReturn(List.of(firstPrize, secondPrize));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wheelService.spin(wheelId)
        );

        assertEquals(
                "Wheel prizes must have a positive total probability weight",
                exception.getMessage()
        );
    }

    /**
     * Crée une roue de test.
     *
     * @return roue de test
     */
    private Wheel createWheel() {
        return new Wheel(
                wheelId,
                merchantId,
                "Ma roue",
                true,
                30,
                true,
                LocalDateTime.now()
        );
    }

    /**
     * Crée un lot de test appartenant à la roue du marchand.
     *
     * @return lot de test
     */
    private WheelPrize createPrize() {
        return new WheelPrize(
                UUID.randomUUID(),
                wheelId,
                "10 points",
                10,
                LocalDateTime.now()
        );
    }
}