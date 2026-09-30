package com.fidely.backend.api.controllers.integrations;

import com.fidely.backend.IntegrationTest;
import com.fidely.backend.api.controllers.WheelController;
import com.fidely.backend.application.port.out.IMerchantRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.IWheelRepository;
import com.fidely.backend.application.port.out.security.IAccessTokenManagement;
import com.fidely.backend.domain.models.Merchants.Merchant;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration du {@link WheelController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WheelControllerWebMvcTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IMerchantRepository merchantRepository;

    @Autowired
    private IWheelRepository wheelRepository;

    @Autowired
    private IAccessTokenManagement accessTokenManagement;

    @Test
    void shouldGetMerchantWheel() throws Exception {
        UUID managerUserId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();

        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + managerUserId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                null,
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
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                60,
                true,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);
        wheelRepository.saveWheel(wheel);

        String accessToken = accessTokenManagement.generate(
                managerUserId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        get("/merchants/{merchantId}/wheel", merchantId)
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
                                .value(wheelId.toString())
                )
                .andExpect(
                        jsonPath("$.merchantId")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Roue Fidely")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.minIntervalMinutes")
                                .value(60)
                )
                .andExpect(
                        jsonPath("$.requiresValidatedPurchase")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.prizes")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.prizes")
                                .isEmpty()
                );
    }

    @Test
    void shouldGetCurrentMerchantWheel() throws Exception {
        UUID managerUserId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();

        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + managerUserId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                null,
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
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                30,
                false,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);
        wheelRepository.saveWheel(wheel);

        String accessToken = accessTokenManagement.generate(
                managerUserId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        get("/merchants/me/wheel")
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
                                .value(wheelId.toString())
                )
                .andExpect(
                        jsonPath("$.merchantId")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Roue Fidely")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.minIntervalMinutes")
                                .value(30)
                )
                .andExpect(
                        jsonPath("$.requiresValidatedPurchase")
                                .value(false)
                );
    }

    @Test
    void shouldUpdateCurrentMerchantWheel() throws Exception {
        UUID managerUserId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();

        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + managerUserId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                null,
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
                email,
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Ancienne roue",
                true,
                60,
                true,
                now
        );

        merchantRepository.save(merchant);
        userRepository.save(merchantManager);
        wheelRepository.saveWheel(wheel);

        String accessToken = accessTokenManagement.generate(
                managerUserId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        put("/merchants/me/wheel")
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "name": "Nouvelle roue",
                            "active": false,
                            "minIntervalMinutes": 120,
                            "requiresValidatedPurchase": false
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(wheelId.toString())
                )
                .andExpect(
                        jsonPath("$.merchantId")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Nouvelle roue")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.minIntervalMinutes")
                                .value(120)
                )
                .andExpect(
                        jsonPath("$.requiresValidatedPurchase")
                                .value(false)
                );
    }

    @Test
    void shouldCreateWheelWhenMerchantDoesNotHaveOne() throws Exception {
        UUID managerUserId = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();

        String slug = "fidely-shop-" + merchantId;
        String email = "manager-" + managerUserId + "@example.com";

        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                slug,
                null,
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
                managerUserId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        put("/merchants/me/wheel")
                                .cookie(
                                        new Cookie(
                                                "fidely_access_token",
                                                accessToken
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "name": "Nouvelle roue",
                            "active": true,
                            "minIntervalMinutes": 15,
                            "requiresValidatedPurchase": true
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.merchantId")
                                .value(merchantId.toString())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Nouvelle roue")
                )
                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.minIntervalMinutes")
                                .value(15)
                )
                .andExpect(
                        jsonPath("$.requiresValidatedPurchase")
                                .value(true)
                );
    }

    @Test
    void shouldSpinMerchantWheel() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                "fidely-shop-" + merchantId,
                null,
                "#000000",
                "#FFFFFF",
                "Description",
                "https://google.com",
                true,
                now,
                now
        );

        merchantRepository.save(merchant);

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        wheelRepository.saveWheel(wheel);

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

        wheelRepository.savePrize(firstPrize);
        wheelRepository.savePrize(secondPrize);

        mockMvc.perform(
                        post("/merchants/{merchantId}/wheel/spin", merchantId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isString())
                .andExpect(
                        jsonPath("$.result").value(
                                org.hamcrest.Matchers.anyOf(
                                        org.hamcrest.Matchers.equalTo(firstPrize.getLabel()),
                                        org.hamcrest.Matchers.equalTo(secondPrize.getLabel())
                                )
                        )
                )
                .andExpect(jsonPath("$.merchantId").doesNotExist())
                .andExpect(jsonPath("$.wheelId").doesNotExist())
                .andExpect(jsonPath("$.probabilityWeight").doesNotExist());
    }

    /**
     * Vérifie qu'un marchand authentifié peut créer un lot sur sa roue.
     */
    @Test
    void shouldCreateWheelPrize() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                "fidely-shop-" + merchantId,
                null,
                "#000000",
                "#FFFFFF",
                "Description",
                "https://google.com",
                true,
                now,
                now
        );

        merchantRepository.save(merchant);

        MerchantManager manager = new MerchantManager(
                userId,
                merchantId,
                "manager-" + merchantId + "@fidely.com",
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        userRepository.save(manager);

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        wheelRepository.saveWheel(wheel);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        post("/merchants/me/wheel/prizes")
                                .cookie(new Cookie("fidely_access_token", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                {
                    "label": "10% de réduction",
                    "probabilityWeight": 50
                }
                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("10% de réduction"))
                .andExpect(jsonPath("$.probabilityWeight").value(50))
                .andExpect(jsonPath("$.wheelId").value(wheelId.toString()));
    }

    /**
     * Vérifie qu'un marchand authentifié peut modifier un lot
     * appartenant à sa roue.
     */
    @Test
    void shouldUpdateWheelPrize() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();
        UUID prizeId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                "fidely-shop-" + merchantId,
                null,
                "#000000",
                "#FFFFFF",
                "Description",
                "https://google.com",
                true,
                now,
                now
        );

        merchantRepository.save(merchant);

        MerchantManager manager = new MerchantManager(
                userId,
                merchantId,
                "manager-" + merchantId + "@fidely.com",
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        userRepository.save(manager);

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        wheelRepository.saveWheel(wheel);

        WheelPrize prize = new WheelPrize(
                prizeId,
                wheelId,
                "Ancien lot",
                20,
                now
        );

        wheelRepository.savePrize(prize);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        patch("/merchants/me/wheel/prizes/{prizeId}", prizeId)
                                .cookie(new Cookie("fidely_access_token", accessToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                {
                    "label": "20% de réduction",
                    "probabilityWeight": 80
                }
                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(prizeId.toString()))
                .andExpect(jsonPath("$.label").value("20% de réduction"))
                .andExpect(jsonPath("$.probabilityWeight").value(80));
    }

    /**
     * Vérifie qu'un marchand authentifié peut supprimer un lot
     * appartenant à sa roue.
     */
    @Test
    void shouldDeleteWheelPrize() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID wheelId = UUID.randomUUID();
        UUID prizeId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Merchant merchant = new Merchant(
                merchantId,
                null,
                "Fidely Shop",
                "fidely-shop-" + merchantId,
                null,
                "#000000",
                "#FFFFFF",
                "Description",
                "https://google.com",
                true,
                now,
                now
        );

        merchantRepository.save(merchant);

        MerchantManager manager = new MerchantManager(
                userId,
                merchantId,
                "manager-" + merchantId + "@fidely.com",
                "John",
                "Doe",
                true,
                true,
                now,
                now
        );

        userRepository.save(manager);

        Wheel wheel = new Wheel(
                wheelId,
                merchantId,
                "Roue Fidely",
                true,
                0,
                false,
                now
        );

        wheelRepository.saveWheel(wheel);

        WheelPrize prize = new WheelPrize(
                prizeId,
                wheelId,
                "10% de réduction",
                50,
                now
        );

        wheelRepository.savePrize(prize);

        String accessToken = accessTokenManagement.generate(
                userId,
                "MERCHANT_MANAGER"
        );

        mockMvc.perform(
                        delete("/merchants/me/wheel/prizes/{prizeId}", prizeId)
                                .cookie(new Cookie("fidely_access_token", accessToken))
                )
                .andExpect(status().isNoContent());
    }
}