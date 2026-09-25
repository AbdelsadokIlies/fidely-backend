package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.auth.AuthUserResponseMapper;
import com.fidely.backend.api.dtos.mappers.auth.LoginResponseMapper;
import com.fidely.backend.api.dtos.models.auth.AuthUserResponse;
import com.fidely.backend.api.dtos.models.auth.LoginResponse;
import com.fidely.backend.application.port.in.IAuthService;
import com.fidely.backend.application.port.in.IPasswordResetService;
import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.MerchantManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests web du contrôleur d'authentification.
 */
@WebMvcTest(AuthController.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private AuthUserResponseMapper authUserResponseMapper;

    @MockitoBean
    private LoginResponseMapper loginResponseMapper;

    @MockitoBean
    private IPasswordResetService passwordResetService;

    @Test
    void shouldRegisterCustomer() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                false,
                true,
                LocalDate.of(1998, 5, 20),
                now,
                now
        );

        AuthUserResponse response = new AuthUserResponse(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                false
        );

        when(authService.registerCustomer(
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                LocalDate.of(1998, 5, 20),
                "Password123!"
        )).thenReturn(customer);

        when(authUserResponseMapper.toResponse(customer))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/register/customer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "customer@example.com",
                                          "firstName": "John",
                                          "lastName": "Doe",
                                          "phone": "0612345678",
                                          "birthDate": "1998-05-20",
                                          "password": "Password123!"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("customer@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        verify(authService).registerCustomer(
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                LocalDate.of(1998, 5, 20),
                "Password123!"
        );

        verify(authUserResponseMapper).toResponse(customer);
    }

    @Test
    void shouldRegisterMerchant() throws Exception {
        UUID merchantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        MerchantManager merchantManager = new MerchantManager(
                userId,
                merchantId,
                "manager@example.com",
                "Jane",
                "Smith",
                false,
                true,
                now,
                now
        );

        AuthUserResponse response = new AuthUserResponse(
                userId,
                "manager@example.com",
                "Jane",
                "Smith",
                false
        );

        when(authService.registerMerchantManager(
                merchantId,
                "manager@example.com",
                "Jane",
                "Smith",
                "Password123!"
        )).thenReturn(merchantManager);

        when(authUserResponseMapper.toResponse(merchantManager))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/register/merchant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "merchantId": "%s",
                                          "email": "manager@example.com",
                                          "firstName": "Jane",
                                          "lastName": "Smith",
                                          "password": "Password123!"
                                        }
                                        """.formatted(merchantId))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("manager@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        verify(authService).registerMerchantManager(
                merchantId,
                "manager@example.com",
                "Jane",
                "Smith",
                "Password123!"
        );

        verify(authUserResponseMapper).toResponse(merchantManager);
    }

    @Test
    void shouldLoginCustomer() throws Exception {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                false,
                true,
                LocalDate.of(1998, 5, 20),
                now,
                now
        );

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        IAuthService.AuthenticationResult authenticationResult =
                new IAuthService.AuthenticationResult(
                        customer,
                        accessToken,
                        refreshToken
                );

        LoginResponse response = new LoginResponse(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                false
        );

        when(authService.login(
                "customer@example.com",
                "Password123!"
        )).thenReturn(authenticationResult);

        when(loginResponseMapper.toResponse(customer))
                .thenReturn(response);

        var result = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "customer@example.com",
                                          "password": "Password123!"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("customer@example.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("John"))
                .andExpect(jsonPath("$.lastName")
                        .value("Doe"))
                .andExpect(jsonPath("$.emailVerified")
                        .value(false))
                .andReturn();

        List<String> cookies =
                result.getResponse().getHeaders(
                        HttpHeaders.SET_COOKIE
                );

        assertEquals(2, cookies.size());

        String accessCookie = cookies.stream()
                .filter(cookie ->
                        cookie.startsWith("fidely_access_token="))
                .findFirst()
                .orElseThrow();

        String refreshCookie = cookies.stream()
                .filter(cookie ->
                        cookie.startsWith("fidely_refresh_token="))
                .findFirst()
                .orElseThrow();

        assertTrue(accessCookie.contains("HttpOnly"));
        assertTrue(accessCookie.contains("Secure"));
        assertTrue(accessCookie.contains("SameSite=Lax"));
        assertTrue(accessCookie.contains("Path=/"));
        assertTrue(accessCookie.contains("Max-Age=900"));

        assertTrue(refreshCookie.contains("HttpOnly"));
        assertTrue(refreshCookie.contains("Secure"));
        assertTrue(refreshCookie.contains("SameSite=Lax"));
        assertTrue(refreshCookie.contains("Path=/auth/refresh"));
        assertTrue(refreshCookie.contains("Max-Age=2592000"));

        assertFalse(result.getResponse()
                .getContentAsString()
                .contains(accessToken));

        assertFalse(result.getResponse()
                .getContentAsString()
                .contains(refreshToken));

        verify(authService).login(
                "customer@example.com",
                "Password123!"
        );

        verify(loginResponseMapper).toResponse(customer);
    }

    @Test
    void shouldRejectInvalidCustomerRegistrationRequest()
            throws Exception {

        mockMvc.perform(
                        post("/auth/register/customer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "invalid-email",
                                          "firstName": "",
                                          "lastName": "Doe",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerCustomer(
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldRejectInvalidMerchantRegistrationRequest()
            throws Exception {

        mockMvc.perform(
                        post("/auth/register/merchant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "invalid-email",
                                          "firstName": "",
                                          "lastName": "Smith",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registerMerchantManager(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldRejectInvalidLoginRequest()
            throws Exception {

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "invalid-email",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCustomerRegistrationFails()
            throws Exception {

        when(authService.registerCustomer(
                eq("customer@example.com"),
                eq("John"),
                eq("Doe"),
                eq("0612345678"),
                eq(LocalDate.of(1998, 5, 20)),
                eq("Password123!")
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid registration data"
                )
        );

        mockMvc.perform(
                        post("/auth/register/customer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "customer@example.com",
                                          "firstName": "John",
                                          "lastName": "Doe",
                                          "phone": "0612345678",
                                          "birthDate": "1998-05-20",
                                          "password": "Password123!"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenCustomerRegistrationFails()
            throws Exception {

        when(authService.registerCustomer(
                eq("customer@example.com"),
                eq("John"),
                eq("Doe"),
                eq("0612345678"),
                eq(LocalDate.of(1998, 5, 20)),
                eq("Password123!")
        )).thenThrow(
                new IllegalStateException(
                        "An account already exists with this email"
                )
        );

        mockMvc.perform(
                        post("/auth/register/customer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "customer@example.com",
                                          "firstName": "John",
                                          "lastName": "Doe",
                                          "phone": "0612345678",
                                          "birthDate": "1998-05-20",
                                          "password": "Password123!"
                                        }
                                        """)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenMerchantRegistrationFails()
            throws Exception {

        UUID merchantId = UUID.randomUUID();

        when(authService.registerMerchantManager(
                eq(merchantId),
                eq("manager@example.com"),
                eq("Jane"),
                eq("Smith"),
                eq("Password123!")
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid registration data"
                )
        );

        mockMvc.perform(
                        post("/auth/register/merchant")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "merchantId": "%s",
                                          "email": "manager@example.com",
                                          "firstName": "Jane",
                                          "lastName": "Smith",
                                          "password": "Password123!"
                                        }
                                        """.formatted(merchantId))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenLoginFails()
            throws Exception {

        when(authService.login(
                "customer@example.com",
                "WrongPassword!"
        )).thenThrow(
                new IllegalArgumentException(
                        "Invalid email or password"
                )
        );

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "customer@example.com",
                                          "password": "WrongPassword!"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verify(authService).login(
                "customer@example.com",
                "WrongPassword!"
        );
    }

    @Test
    void shouldRefreshAuthenticationSession() throws Exception {
        UUID userId = UUID.randomUUID();

        Customer customer = new Customer(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                "0612345678",
                false,
                true,
                LocalDate.of(1998, 5, 20),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        String newAccessToken = "new-access-token";

        IAuthService.AuthenticationResult authenticationResult =
                new IAuthService.AuthenticationResult(
                        customer,
                        newAccessToken,
                        newRefreshToken
                );

        LoginResponse response = new LoginResponse(
                userId,
                "customer@example.com",
                "John",
                "Doe",
                false
        );

        when(authService.refresh(oldRefreshToken))
                .thenReturn(authenticationResult);

        when(loginResponseMapper.toResponse(customer))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(
                                        new jakarta.servlet.http.Cookie(
                                                "fidely_refresh_token",
                                                oldRefreshToken
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(header().stringValues(
                        "Set-Cookie",
                        org.hamcrest.Matchers.hasItems(
                                org.hamcrest.Matchers.containsString(
                                        "fidely_access_token=new-access-token"
                                ),
                                org.hamcrest.Matchers.containsString(
                                        "fidely_refresh_token=new-refresh-token"
                                )
                        )
                ))
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email")
                        .value("customer@example.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("John"))
                .andExpect(jsonPath("$.lastName")
                        .value("Doe"))
                .andExpect(jsonPath("$.emailVerified")
                        .value(false));

        verify(authService).refresh(oldRefreshToken);
        verify(loginResponseMapper).toResponse(customer);
    }

    @Test
    void shouldRejectRefreshWhenRefreshCookieIsMissing() throws Exception {
        when(authService.refresh(null))
                .thenThrow(
                        new IllegalArgumentException(
                                "Refresh token cannot be null or blank"
                        )
                );

        mockMvc.perform(
                        post("/auth/refresh")
                )
                .andExpect(status().isBadRequest());

        verify(authService).refresh(null);
        verify(loginResponseMapper, never())
                .toResponse(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectRefreshWhenRefreshTokenIsInvalid() throws Exception {
        String refreshToken = "invalid-refresh-token";

        when(authService.refresh(refreshToken))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );

        mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(
                                        new jakarta.servlet.http.Cookie(
                                                "fidely_refresh_token",
                                                refreshToken
                                        )
                                )
                )
                .andExpect(status().isBadRequest());

        verify(authService).refresh(refreshToken);
        verify(loginResponseMapper, never())
                .toResponse(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectRefreshWhenUserIsInactive() throws Exception {
        String refreshToken = "refresh-token";

        when(authService.refresh(refreshToken))
                .thenThrow(
                        new IllegalStateException(
                                "User account is inactive"
                        )
                );

        mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(
                                        new jakarta.servlet.http.Cookie(
                                                "fidely_refresh_token",
                                                refreshToken
                                        )
                                )
                )
                .andExpect(status().isConflict());

        verify(authService).refresh(refreshToken);
        verify(loginResponseMapper, never())
                .toResponse(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldVerifyEmail() throws Exception {
        String verificationToken = "verification-token";

        mockMvc.perform(
                        get("/auth/verify-email")
                                .param("token", verificationToken)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(authService).verifyEmail(verificationToken);
    }

    @Test
    void shouldReturnBadRequestWhenEmailVerificationTokenIsInvalid()
            throws Exception {

        String verificationToken = "invalid-token";


        doThrow(
                new IllegalArgumentException(
                        "Invalid email verification token"
                )
        ).when(authService).verifyEmail(verificationToken);

        mockMvc.perform(
                        get("/auth/verify-email")
                                .param("token", verificationToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email verification token"));

        verify(authService).verifyEmail(verificationToken);
    }

    @Test
    void shouldReturnConflictWhenUserAssociatedWithVerificationTokenDoesNotExist()
            throws Exception {

        String verificationToken = "verification-token";

        doThrow(
                new IllegalStateException(
                        "User associated with email verification token was not found"
                )
        ).when(authService).verifyEmail(verificationToken);

        mockMvc.perform(
                        get("/auth/verify-email")
                                .param("token", verificationToken)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value(
                                "User associated with email verification token was not found"
                        ));

        verify(authService).verifyEmail(verificationToken);
    }

    @Test
    void shouldResendVerificationEmail() throws Exception {
        String email = "user@example.com";

        mockMvc.perform(
                        post("/auth/resend-verification-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "email": "user@example.com"
                            }
                            """)
                )
                .andExpect(status().isOk());

        verify(authService)
                .resendVerificationEmail(email);
    }

    @Test
    void shouldRejectInvalidEmailForResendVerificationEmail()
            throws Exception {

        mockMvc.perform(
                        post("/auth/resend-verification-email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "email": "invalid-email"
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRequestPasswordReset() throws Exception {
        String email = "user@example.com";

        mockMvc.perform(
                        post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "email": "user@example.com"
                            }
                            """)
                )
                .andExpect(status().isOk());

        verify(passwordResetService)
                .requestPasswordReset(email);
    }

    @Test
    void shouldRejectInvalidEmailForPasswordReset() throws Exception {
        mockMvc.perform(
                        post("/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "email": "invalid-email"
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void shouldResetPassword() throws Exception {
        String token = "valid-reset-token";
        String newPassword = "NewPassword123!";

        mockMvc.perform(
                        post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "token": "valid-reset-token",
                                "newPassword": "NewPassword123!"
                            }
                            """)
                )
                .andExpect(status().isOk());

        verify(passwordResetService)
                .resetPassword(
                        token,
                        newPassword
                );
    }

    @Test
    void shouldRejectInvalidPasswordForPasswordReset() throws Exception {
        mockMvc.perform(
                        post("/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "token": "valid-reset-token",
                                "newPassword": "short"
                            }
                            """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(passwordResetService);
    }

    @Test
    void shouldLogout() throws Exception {
        String refreshToken = "refresh-token";

        mockMvc.perform(
                        post("/auth/logout")
                                .cookie(
                                        new Cookie(
                                                "fidely_refresh_token",
                                                refreshToken
                                        ),
                                        new Cookie(
                                                "fidely_access_token",
                                                "access-token"
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().stringValues(
                                HttpHeaders.SET_COOKIE,
                                org.hamcrest.Matchers.hasItems(
                                        org.hamcrest.Matchers.containsString(
                                                "fidely_access_token="
                                        ),
                                        org.hamcrest.Matchers.containsString(
                                                "fidely_refresh_token="
                                        )
                                )
                        )
                );

        verify(authService).logout(refreshToken);
    }
}