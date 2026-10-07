package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.models.wheels.UpdateWheelPrizeRequest;
import com.fidely.backend.api.dtos.models.wheels.UpdateWheelRequest;
import com.fidely.backend.api.dtos.models.wheels.WheelPrizeResponse;
import com.fidely.backend.api.dtos.models.wheels.WheelResponse;
import com.fidely.backend.application.port.in.IWheelService;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du {@link WheelController}.
 */
@ExtendWith(MockitoExtension.class)
class WheelControllerTest {

    @Mock
    private IWheelService wheelService;

    @Mock
    private Authentication authentication;

    private WheelController wheelController;

    private UUID userId;
    private UUID merchantId;
    private UUID wheelId;
    private UUID prizeId;

    private Wheel wheel;
    private WheelPrize prize;

    @BeforeEach
    void setUp() {
        wheelController = new WheelController(wheelService);

        userId = UUID.randomUUID();
        merchantId = UUID.randomUUID();
        wheelId = UUID.randomUUID();
        prizeId = UUID.randomUUID();

        wheel = new Wheel(
                wheelId,
                merchantId,
                "Ma roue",
                true,
                60,
                true,
                LocalDateTime.now()
        );

        prize = new WheelPrize(
                prizeId,
                wheelId,
                "10 points",
                5,
                LocalDateTime.now()
        );
    }

    @Test
    void shouldGetMerchantWheel() {
        when(wheelService.getWheelByMerchantId(merchantId))
                .thenReturn(wheel);

        when(wheelService.getPrizes(wheelId))
                .thenReturn(List.of(prize));

        ResponseEntity<WheelResponse> response =
                wheelController.getMerchantWheel(merchantId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        WheelResponse body = response.getBody();

        assertEquals(wheelId, body.id());
        assertEquals(merchantId, body.merchantId());
        assertEquals("Ma roue", body.name());
        assertEquals(true, body.active());
        assertEquals(60, body.minIntervalMinutes());
        assertEquals(true, body.requiresValidatedPurchase());

        assertNotNull(body.prizes());
        assertEquals(1, body.prizes().size());

        assertEquals(prizeId, body.prizes().get(0).id());
        assertEquals(wheelId, body.prizes().get(0).wheelId());
        assertEquals("10 points", body.prizes().get(0).label());
        assertEquals(5, body.prizes().get(0).probabilityWeight());

        verify(wheelService).getWheelByMerchantId(merchantId);
        verify(wheelService).getPrizes(wheelId);
    }

    @Test
    void shouldGetCurrentMerchantWheel() {
        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(wheelService.getWheelForManager(userId))
                .thenReturn(wheel);

        when(wheelService.getPrizes(wheelId))
                .thenReturn(List.of(prize));

        ResponseEntity<WheelResponse> response =
                wheelController.getCurrentMerchantWheel(authentication);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        WheelResponse body = response.getBody();

        assertEquals(wheelId, body.id());
        assertEquals(merchantId, body.merchantId());
        assertEquals("Ma roue", body.name());
        assertEquals(true, body.active());
        assertEquals(60, body.minIntervalMinutes());
        assertEquals(true, body.requiresValidatedPurchase());

        assertEquals(1, body.prizes().size());
        assertEquals(prizeId, body.prizes().get(0).id());

        verify(authentication).getPrincipal();
        verify(wheelService).getWheelForManager(userId);
        verify(wheelService).getPrizes(wheelId);
    }

    @Test
    void shouldUpdateCurrentMerchantWheel() {
        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(wheelService.updateWheelForManager(
                userId,
                "Nouvelle roue",
                false,
                120,
                false
        )).thenReturn(wheel);

        when(wheelService.getPrizes(wheelId))
                .thenReturn(List.of(prize));

        UpdateWheelRequest request = new UpdateWheelRequest(
                "Nouvelle roue",
                false,
                120,
                false
        );

        ResponseEntity<WheelResponse> response =
                wheelController.updateCurrentMerchantWheel(
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        WheelResponse body = response.getBody();

        assertEquals(wheelId, body.id());
        assertEquals(merchantId, body.merchantId());

        verify(authentication).getPrincipal();

        verify(wheelService).updateWheelForManager(
                userId,
                "Nouvelle roue",
                false,
                120,
                false
        );

        verify(wheelService).getPrizes(wheelId);
    }

    @Test
    void shouldReturnWheelWithoutPrizesWhenThereAreNone() {
        when(wheelService.getWheelByMerchantId(merchantId))
                .thenReturn(wheel);

        when(wheelService.getPrizes(wheelId))
                .thenReturn(List.of());

        ResponseEntity<WheelResponse> response =
                wheelController.getMerchantWheel(merchantId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        assertNotNull(response.getBody().prizes());
        assertEquals(0, response.getBody().prizes().size());

        verify(wheelService).getWheelByMerchantId(merchantId);
        verify(wheelService).getPrizes(wheelId);
    }

    @Test
    void shouldCreatePrize() {
        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(wheelService.savePrizeForManager(
                userId,
                "10% de réduction",
                50
        )).thenReturn(prize);

        ResponseEntity<WheelPrizeResponse> response =
                wheelController.createPrize(
                        new UpdateWheelPrizeRequest(
                                "10% de réduction",
                                50
                        ),
                        authentication
                );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(prizeId, response.getBody().id());
        assertEquals("10 points", response.getBody().label());
        assertEquals(5, response.getBody().probabilityWeight());

        verify(authentication).getPrincipal();

        verify(wheelService).savePrizeForManager(
                userId,
                "10% de réduction",
                50
        );
    }

    @Test
    void shouldUpdatePrize() {
        WheelPrize updatedPrize = new WheelPrize(
                prizeId,
                wheelId,
                "10% de réduction",
                80,
                prize.getCreatedAt()
        );

        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(wheelService.updatePrizeForManager(
                userId,
                prizeId,
                "10% de réduction",
                80
        )).thenReturn(updatedPrize);

        ResponseEntity<WheelPrizeResponse> response =
                wheelController.updatePrize(
                        prizeId,
                        new UpdateWheelPrizeRequest(
                                "10% de réduction",
                                80
                        ),
                        authentication
                );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(prizeId, response.getBody().id());
        assertEquals("10% de réduction", response.getBody().label());
        assertEquals(80, response.getBody().probabilityWeight());

        verify(authentication).getPrincipal();

        verify(wheelService).updatePrizeForManager(
                userId,
                prizeId,
                "10% de réduction",
                80
        );
    }

    @Test
    void shouldDeletePrize() {
        when(authentication.getPrincipal())
                .thenReturn(userId);

        ResponseEntity<Void> response =
                wheelController.deletePrize(
                        prizeId,
                        authentication
                );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(authentication).getPrincipal();
        verify(wheelService).deletePrizeForManager(userId, prizeId);
    }
}