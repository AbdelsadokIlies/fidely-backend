package com.fidely.backend.api.controllers.integrations;

import com.fidely.backend.api.controllers.RewardController;
import com.fidely.backend.api.dtos.mappers.loyalties.rewards.RewardResponseMapper;
import com.fidely.backend.api.dtos.mappers.loyalties.rewards.UpdateRewardRequestMapper;
import com.fidely.backend.api.dtos.mappers.rewards.CreateRewardRequestMapper;
import com.fidely.backend.api.dtos.models.loyalties.rewards.RewardResponse;
import com.fidely.backend.api.dtos.models.loyalties.rewards.UpdateRewardRequest;
import com.fidely.backend.api.dtos.models.rewards.CreateRewardRequest;
import com.fidely.backend.application.port.in.IRewardService;
import com.fidely.backend.domain.models.loyalties.Rewards.Reward;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests web du controller REST des récompenses de fidélité.
 */
@WebMvcTest(RewardController.class)
class RewardControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IRewardService rewardService;

    @MockitoBean
    private CreateRewardRequestMapper createRewardRequestMapper;

    @MockitoBean
    private UpdateRewardRequestMapper updateRewardRequestMapper;

    @MockitoBean
    private RewardResponseMapper rewardResponseMapper;

    @Test
    void shouldGetMerchantRewards() throws Exception {
        UUID merchantId = UUID.randomUUID();

        Reward reward = createReward(merchantId);

        RewardResponse response = createRewardResponse(reward);

        when(rewardService.getRewardsByMerchant(merchantId))
                .thenReturn(List.of(reward));

        when(rewardResponseMapper.toResponse(reward))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/merchants/{merchantId}/rewards",
                                merchantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.length()").value(1))
                        .andExpect(jsonPath("$[0].id")
                                .value(reward.getId().toString()))
                        .andExpect(jsonPath("$[0].merchantId")
                                .value(merchantId.toString()))
                        .andExpect(jsonPath("$[0].name")
                                .value("Café offert"))
                        .andExpect(jsonPath("$[0].description")
                                .value("Un café offert"))
                        .andExpect(jsonPath("$[0].costPoints")
                                .value(100))
                        .andExpect(jsonPath("$[0].quantityAvailable")
                                .value(10))
                        .andExpect(jsonPath("$[0].active")
                                .value(true));

        verify(rewardService)
                .getRewardsByMerchant(merchantId);

        verify(rewardResponseMapper)
                .toResponse(reward);
    }

    @Test
    void shouldCreateReward() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();

        CreateRewardRequest request = new CreateRewardRequest(
                "Café offert",
                "Un café offert",
                100,
                10,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true
        );

        Reward reward = new Reward(
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

        RewardResponse response = createRewardResponse(reward);

        when(createRewardRequestMapper.toDomain(
                any(CreateRewardRequest.class)
        )).thenReturn(reward);

        when(rewardService.createReward(
                userId,
                reward
        )).thenReturn(reward);

        when(rewardResponseMapper.toResponse(reward))
                .thenReturn(response);

        mockMvc.perform(
                        post("/merchants/me/rewards")
                                .principal(createAuthentication(userId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "Café offert",
                                        "description": "Un café offert",
                                        "costPoints": 100,
                                        "quantityAvailable": 10,
                                        "validFrom": "2026-06-01T00:00:00",
                                        "validTo": null,
                                        "active": true
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(rewardId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Café offert"))
                .andExpect(jsonPath("$.costPoints")
                        .value(100))
                .andExpect(jsonPath("$.quantityAvailable")
                        .value(10))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(createRewardRequestMapper)
                .toDomain(
                        any(CreateRewardRequest.class)
                );

        verify(rewardService)
                .createReward(
                        userId,
                        reward
                );

        verify(rewardResponseMapper)
                .toResponse(reward);
    }

    @Test
    void shouldUpdateReward() throws Exception {
        UUID rewardId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        Reward updatedReward = new Reward(
                rewardId,
                merchantId,
                "Café premium",
                "Un café premium offert",
                150,
                5,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true,
                LocalDateTime.of(2026, 6, 1, 0, 0)
        );

        RewardResponse response =
                createRewardResponse(updatedReward);

        UpdateRewardRequest request = new UpdateRewardRequest(
                "Café premium",
                "Un café premium offert",
                150,
                5,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true
        );

        when(updateRewardRequestMapper.toDomain(
                eq(rewardId),
                any(UpdateRewardRequest.class)
        )).thenReturn(updatedReward);

        when(rewardService.updateReward(updatedReward))
                .thenReturn(updatedReward);

        when(rewardResponseMapper.toResponse(updatedReward))
                .thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/merchants/me/rewards/{rewardId}",
                                rewardId
                        )
                                .principal(
                                        createAuthentication(
                                                UUID.randomUUID()
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "Café premium",
                                        "description": "Un café premium offert",
                                        "costPoints": 150,
                                        "quantityAvailable": 5,
                                        "validFrom": "2026-06-01T00:00:00",
                                        "validTo": null,
                                        "active": true
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON_VALUE
                ))
                .andExpect(jsonPath("$.id")
                        .value(rewardId.toString()))
                .andExpect(jsonPath("$.merchantId")
                        .value(merchantId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Café premium"))
                .andExpect(jsonPath("$.costPoints")
                        .value(150))
                .andExpect(jsonPath("$.quantityAvailable")
                        .value(5));

        verify(updateRewardRequestMapper)
                .toDomain(
                        eq(rewardId),
                        any(UpdateRewardRequest.class)
                );

        verify(rewardService)
                .updateReward(updatedReward);

        verify(rewardResponseMapper)
                .toResponse(updatedReward);
    }

    @Test
    void shouldDeleteReward() throws Exception {
        UUID rewardId = UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/merchants/me/rewards/{rewardId}",
                                rewardId
                        )
                                .principal(
                                        createAuthentication(
                                                UUID.randomUUID()
                                        )
                                )
                )
                .andExpect(status().isNoContent());

        verify(rewardService)
                .deleteReward(rewardId);
    }

    /**
     * Crée une authentification simulée contenant l'identifiant
     * utilisateur attendu par le controller.
     *
     * @param userId identifiant de l'utilisateur
     * @return authentification simulée
     */
    private Authentication createAuthentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                null
        );
    }

    /**
     * Crée une récompense utilisée dans les tests.
     *
     * @param merchantId identifiant du marchand
     * @return récompense de test
     */
    private Reward createReward(UUID merchantId) {
        return createReward(
                UUID.randomUUID(),
                merchantId
        );
    }

    /**
     * Crée une récompense utilisée dans les tests.
     *
     * @param rewardId identifiant de la récompense
     * @param merchantId identifiant du marchand
     * @return récompense de test
     */
    private Reward createReward(
            UUID rewardId,
            UUID merchantId
    ) {
        return new Reward(
                rewardId,
                merchantId,
                "Café offert",
                "Un café offert",
                100,
                10,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                null,
                true,
                LocalDateTime.of(2026, 6, 1, 0, 0)
        );
    }

    /**
     * Crée une réponse API à partir d'une récompense.
     *
     * @param reward récompense utilisée dans le test
     * @return réponse API
     */
    private RewardResponse createRewardResponse(Reward reward) {
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