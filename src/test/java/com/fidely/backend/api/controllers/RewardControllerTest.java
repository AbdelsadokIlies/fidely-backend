package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.loyalties.rewards.RewardResponseMapper;
import com.fidely.backend.api.dtos.mappers.loyalties.rewards.UpdateRewardRequestMapper;
import com.fidely.backend.api.dtos.mappers.rewards.CreateRewardRequestMapper;
import com.fidely.backend.api.dtos.models.loyalties.rewards.RewardResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdateRewardRequest;
import com.fidely.backend.api.dtos.models.rewards.CreateRewardRequest;
import com.fidely.backend.application.port.in.IRewardService;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du controller REST des récompenses de fidélité.
 */
@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private IRewardService rewardService;

    @Mock
    private CreateRewardRequestMapper createRewardRequestMapper;

    @Mock
    private UpdateRewardRequestMapper updateRewardRequestMapper;

    @Mock
    private RewardResponseMapper rewardResponseMapper;

    @Mock
    private Authentication authentication;

    private RewardController controller;

    private UUID userId;
    private UUID merchantId;

    @BeforeEach
    void setUp() {
        controller = new RewardController(
                rewardService,
                createRewardRequestMapper,
                updateRewardRequestMapper,
                rewardResponseMapper
        );

        merchantId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldGetMerchantRewards() {
        Reward reward = createReward();
        RewardResponse rewardResponse = createRewardResponse(reward);

        when(rewardService.getRewardsByMerchant(merchantId))
                .thenReturn(List.of(reward));

        when(rewardResponseMapper.toResponse(reward))
                .thenReturn(rewardResponse);

        var response = controller.getMerchantRewards(merchantId);

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isNotNull()
                .hasSize(1);

        assertThat(response.getBody().get(0))
                .isEqualTo(rewardResponse);

        verify(rewardService)
                .getRewardsByMerchant(merchantId);

        verify(rewardResponseMapper)
                .toResponse(reward);
    }

    @Test
    void shouldCreateReward() {
        CreateRewardRequest request = new CreateRewardRequest(
                "Café offert",
                "Un café offert",
                100,
                10,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true
        );

        Reward reward = createReward();

        Reward createdReward = new Reward(
                reward.getId(),
                reward.getMerchantId(),
                reward.getName(),
                reward.getDescription(),
                reward.getCostPoints(),
                reward.getQuantityAvailable(),
                reward.getValidFrom(),
                reward.getValidTo(),
                reward.isActive(),
                reward.getCreatedAt()
        );

        RewardResponse rewardResponse =
                createRewardResponse(createdReward);

        when(authentication.getPrincipal())
                .thenReturn(userId);

        when(createRewardRequestMapper.toDomain(
                request
        )).thenReturn(reward);

        when(rewardService.createReward(
                userId,
                reward
        )).thenReturn(createdReward);

        when(rewardResponseMapper.toResponse(createdReward))
                .thenReturn(rewardResponse);

        var response = controller.createReward(
                request,
                authentication
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(rewardResponse);

        verify(authentication)
                .getPrincipal();

        verify(createRewardRequestMapper)
                .toDomain(request);

        verify(rewardService)
                .createReward(
                        userId,
                        reward
                );

        verify(rewardResponseMapper)
                .toResponse(createdReward);
    }

    @Test
    void shouldUpdateReward() {
        UUID rewardId = UUID.randomUUID();

        UpdateRewardRequest request = new UpdateRewardRequest(
                "Café premium",
                "Un café premium offert",
                150,
                5,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true
        );

        Reward updatedReward = new Reward(
                rewardId,
                merchantId,
                request.name(),
                request.description(),
                request.costPoints(),
                request.quantityAvailable(),
                request.validFrom(),
                request.validTo(),
                request.active(),
                LocalDateTime.of(2026, 6, 1, 0, 0)
        );

        RewardResponse rewardResponse =
                createRewardResponse(updatedReward);

        when(updateRewardRequestMapper.toDomain(
                rewardId,
                request
        )).thenReturn(updatedReward);

        when(rewardService.updateReward(updatedReward))
                .thenReturn(updatedReward);

        when(rewardResponseMapper.toResponse(updatedReward))
                .thenReturn(rewardResponse);

        var response = controller.updateReward(
                rewardId,
                request
        );

        assertThat(response.getStatusCode().value())
                .isEqualTo(200);

        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(rewardResponse);

        verify(updateRewardRequestMapper)
                .toDomain(
                        rewardId,
                        request
                );

        verify(rewardService)
                .updateReward(updatedReward);

        verify(rewardResponseMapper)
                .toResponse(updatedReward);
    }

    @Test
    void shouldDeleteReward() {
        UUID rewardId = UUID.randomUUID();

        var response = controller.deleteReward(rewardId);

        assertThat(response.getStatusCode().value())
                .isEqualTo(204);

        assertThat(response.getBody())
                .isNull();

        verify(rewardService)
                .deleteReward(rewardId);
    }

    /**
     * Crée une récompense utilisée dans les tests.
     *
     * @return récompense de test
     */
    private Reward createReward() {
        return createReward(
                UUID.randomUUID(),
                merchantId
        );
    }

    /**
     * Crée une récompense utilisée dans les tests.
     *
     * @param rewardId identifiant de la récompense
     * @param rewardMerchantId identifiant du marchand
     * @return récompense de test
     */
    private Reward createReward(
            UUID rewardId,
            UUID rewardMerchantId
    ) {
        LocalDateTime createdAt =
                LocalDateTime.of(2026, 6, 1, 0, 0);

        return new Reward(
                rewardId,
                rewardMerchantId,
                "Café offert",
                "Un café offert",
                100,
                10,
                createdAt,
                null,
                true,
                createdAt
        );
    }

    /**
     * Crée une réponse API à partir d'une récompense.
     *
     * @param reward récompense utilisée dans le test
     * @return réponse API
     */
    private RewardResponse createRewardResponse(
            Reward reward
    ) {
        return new RewardResponse(
                reward.getId(),
                reward.getMerchantId(),
                reward.getName(),
                reward.getDescription(),
                reward.getCostPoints(),
                reward.getQuantityAvailable(),
                reward.getValidFrom(),
                reward.getValidTo(),
                reward.isActive(),
                reward.getCreatedAt()
        );
    }
}