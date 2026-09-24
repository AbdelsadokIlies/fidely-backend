package com.fidely.backend.api.controllers;

import com.fidely.backend.api.dtos.mappers.auth.AuthUserResponseMapper;
import com.fidely.backend.api.dtos.mappers.auth.LoginResponseMapper;
import com.fidely.backend.api.dtos.models.auth.*;
import com.fidely.backend.application.port.in.IAuthService;
import com.fidely.backend.application.port.in.IPasswordResetService;
import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.MerchantManager;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Contrôleur REST responsable des opérations d'authentification
 * et de création des comptes utilisateurs.
 *
 * <p>Ce contrôleur orchestre uniquement les échanges HTTP.
 * La logique métier est déléguée au port d'entrée
 * {@link IAuthService}.</p>
 *
 * <p>Les tokens d'authentification sont transmis au client
 * exclusivement via des cookies sécurisés et ne sont pas
 * inclus dans les réponses JSON.</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "fidely_access_token";
    private static final String REFRESH_TOKEN_COOKIE = "fidely_refresh_token";

    private static final Duration ACCESS_TOKEN_COOKIE_DURATION =
            Duration.ofMinutes(15);

    private static final Duration REFRESH_TOKEN_COOKIE_DURATION =
            Duration.ofDays(30);

    private final IAuthService authService;
    private final IPasswordResetService passwordResetService;
    private final AuthUserResponseMapper authUserResponseMapper;
    private final LoginResponseMapper loginResponseMapper;

    /**
     * Construit le contrôleur d'authentification.
     *
     * @param authService service applicatif d'authentification
     * @param passwordResetService service de réinitialisation du mot de passe
     * @param authUserResponseMapper mapper des réponses utilisateur
     * @param loginResponseMapper mapper de la réponse de connexion
     */
    public AuthController(
            IAuthService authService,
            IPasswordResetService passwordResetService,
            AuthUserResponseMapper authUserResponseMapper,
            LoginResponseMapper loginResponseMapper
    ) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.authUserResponseMapper = authUserResponseMapper;
        this.loginResponseMapper = loginResponseMapper;
    }

    /**
     * Crée un compte client.
     *
     * @param request données d'inscription du client
     * @return utilisateur créé
     */
    @PostMapping(
            path = "/register/customer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthUserResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request
    ) {
        Customer customer = authService.registerCustomer(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.birthDate(),
                request.password()
        );

        AuthUserResponse response =
                authUserResponseMapper.toResponse(customer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Crée un compte gestionnaire de marchand.
     *
     * @param request données d'inscription du gestionnaire
     * @return utilisateur créé
     */
    @PostMapping(
            path = "/register/merchant",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthUserResponse> registerMerchant(
            @Valid @RequestBody RegisterMerchantRequest request
    ) {
        MerchantManager merchantManager =
                authService.registerMerchantManager(
                        request.merchantId(),
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        request.password()
                );

        AuthUserResponse response =
                authUserResponseMapper.toResponse(merchantManager);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Vérifie l'adresse email d'un utilisateur à partir
     * du token contenu dans le lien de vérification.
     *
     * @param token token de vérification email
     * @return réponse HTTP indiquant que l'adresse email a été vérifiée
     */
    @GetMapping(
            path = "/verify-email",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping(
            path = "/resend-verification-email",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request
    ) {
        authService.resendVerificationEmail(
                request.email()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Authentifie un utilisateur et crée sa session.
     *
     * <p>L'access token et le refresh token sont transmis
     * exclusivement via des cookies HTTP sécurisés.</p>
     *
     * @param request données de connexion
     * @return informations publiques de l'utilisateur authentifié
     */
    @PostMapping(
            path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        IAuthService.AuthenticationResult authenticationResult =
                authService.login(
                        request.email(),
                        request.password()
                );

        ResponseCookie accessTokenCookie = createAccessTokenCookie(
                authenticationResult.accessToken()
        );

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
                authenticationResult.refreshToken()
        );

        LoginResponse response =
                loginResponseMapper.toResponse(
                        authenticationResult.user()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(response);
    }

    /**
     * Renouvelle la session d'authentification à partir
     * du refresh token présent dans le cookie sécurisé.
     *
     * <p>Le refresh token est soumis à une rotation :
     * l'ancien token est invalidé et un nouveau refresh token
     * ainsi qu'un nouvel access token sont générés.</p>
     *
     * @param refreshToken refresh token présent dans le cookie HTTP
     * @return informations publiques de l'utilisateur authentifié
     */
    @PostMapping(
            path = "/refresh",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(
                    name = REFRESH_TOKEN_COOKIE,
                    required = false
            ) String refreshToken
    ) {
        IAuthService.AuthenticationResult authenticationResult =
                authService.refresh(refreshToken);

        ResponseCookie accessTokenCookie = createAccessTokenCookie(
                authenticationResult.accessToken()
        );

        ResponseCookie refreshTokenCookie = createRefreshTokenCookie(
                authenticationResult.refreshToken()
        );

        LoginResponse response =
                loginResponseMapper.toResponse(
                        authenticationResult.user()
                );

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString()
                )
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.toString()
                )
                .body(response);
    }

    /**
     * Demande la réinitialisation du mot de passe d'un utilisateur.
     *
     * @param request données de la demande de réinitialisation
     * @return réponse HTTP confirmant la prise en compte de la demande
     */
    @PostMapping(
            path = "/forgot-password",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestPasswordReset(
                request.email()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur à partir
     * d'un token de réinitialisation valide.
     *
     * @param request données de réinitialisation du mot de passe
     * @return réponse HTTP confirmant la réinitialisation
     */
    @PostMapping(
            path = "/reset-password",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Construit le cookie contenant l'access token.
     *
     * @param accessToken access token JWT
     * @return cookie sécurisé contenant l'access token
     */
    private ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie
                .from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(ACCESS_TOKEN_COOKIE_DURATION)
                .build();
    }

    /**
     * Construit le cookie contenant le refresh token.
     *
     * @param refreshToken refresh token
     * @return cookie sécurisé contenant le refresh token
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(REFRESH_TOKEN_COOKIE_DURATION)
                .build();
    }
}