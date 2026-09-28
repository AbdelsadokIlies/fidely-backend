package com.fidely.backend.api.controllers;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.application.port.out.IMerchantRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import com.fidely.backend.domain.models.Merchants.Merchant;
import com.fidely.backend.domain.models.users.MerchantManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantControllerTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IMerchantRepository merchantRepository;

    @Autowired
    private IAccessTokenManagement accessTokenManagement;

    @Test
    void shouldGetCurrentMerchant() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + userId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        MerchantManager merchantManager = new MerchantManager(
                userId,
                merchantId,
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                "https://example.com/logo.png",
                "#000000",
                "#FFFFFF",
                "Mon commerce",
                "https://google.com/review",
                true,
                now,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        get("/merchants/me")
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Fidely Shop")
                )
                .andExpect(
                        jsonPath("$.slug")
                                .value(slug)
                )
                .andExpect(
                        jsonPath("$.logoUrl")
                                .value("https://example.com/logo.png")
                )
                .andExpect(
                        jsonPath("$.primaryColor")
                                .value("#000000")
                )
                .andExpect(
                        jsonPath("$.secondaryColor")
                                .value("#FFFFFF")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Mon commerce")
                )
                .andExpect(
                        jsonPath("$.googleReviewUrl")
                                .value("https://google.com/review")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                );
    }

    @Test
    void shouldUpdateCurrentMerchant() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + userId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Old Name",
                slug,
                "https://example.com/logo.png",
                "#000000",
                "#FFFFFF",
                "Old description",
                "https://google.com/old",
                true,
                now,
                now
        );

        MerchantManager merchantManager = new MerchantManager(
                userId,
                merchantId,
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        patch("/merchants/me")
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "name": "New Name",
                            "slug": "new-slug-%s",
                            "description": "New description",
                            "googleReviewUrl": "https://google.com/new"
                        }
                        """.formatted(merchantId))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("New Name")
                )
                .andExpect(
                        jsonPath("$.slug")
                                .value("new-slug-" + merchantId)
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("New description")
                )
                .andExpect(
                        jsonPath("$.googleReviewUrl")
                                .value("https://google.com/new")
                )
                .andExpect(
                        jsonPath("$.logoUrl")
                                .value("https://example.com/logo.png")
                )
                .andExpect(
                        jsonPath("$.primaryColor")
                                .value("#000000")
                )
                .andExpect(
                        jsonPath("$.secondaryColor")
                                .value("#FFFFFF")
                );
    }

    @Test
    void shouldUpdateCurrentMerchantBranding() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + userId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                "https://example.com/old-logo.png",
                "#000000",
                "#FFFFFF",
                "Mon commerce",
                "https://google.com/review",
                true,
                now,
                now
        );

        MerchantManager merchantManager = new MerchantManager(
                userId,
                merchantId,
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        patch("/merchants/me/branding")
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "logoUrl": "https://example.com/new-logo.png",
                            "primaryColor": "#FF0000",
                            "secondaryColor": "#00FF00"
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.logoUrl")
                                .value("https://example.com/new-logo.png")
                )
                .andExpect(
                        jsonPath("$.primaryColor")
                                .value("#FF0000")
                )
                .andExpect(
                        jsonPath("$.secondaryColor")
                                .value("#00FF00")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Fidely Shop")
                )
                .andExpect(
                        jsonPath("$.slug")
                                .value(slug)
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Mon commerce")
                );
    }

    @Test
    void shouldGetPublicMerchantWithAuthenticatedUser() throws Exception {
        UUID managerUserId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        String slug = "fidely-shop-" + merchantId;
        String managerEmail = "manager-" + managerUserId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                "https://example.com/logo.png",
                "#000000",
                "#FFFFFF",
                "Mon commerce",
                "https://google.com/review",
                true,
                now,
                now
        );

        MerchantManager merchantManager = new MerchantManager(
                managerUserId,
                merchantId,
                managerEmail,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);

        String accessToken = accessTokenManagement.generate(
                authenticatedUserId,
                "CUSTOMER"
        );

        mockMvc.perform(
                        get("/merchants/{id}/public", merchantId)
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Fidely Shop")
                )
                .andExpect(
                        jsonPath("$.slug")
                                .value(slug)
                )
                .andExpect(
                        jsonPath("$.logoUrl")
                                .value("https://example.com/logo.png")
                )
                .andExpect(
                        jsonPath("$.primaryColor")
                                .value("#000000")
                )
                .andExpect(
                        jsonPath("$.secondaryColor")
                                .value("#FFFFFF")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Mon commerce")
                )
                .andExpect(
                        jsonPath("$.googleReviewUrl")
                                .value("https://google.com/review")
                );
    }
}