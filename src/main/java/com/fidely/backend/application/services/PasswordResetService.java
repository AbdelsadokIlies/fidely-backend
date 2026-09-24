package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IPasswordResetService;
import com.fidely.backend.application.port.out.IAuthIdentityRepository;
import com.fidely.backend.application.port.out.IEmailSender;
import com.fidely.backend.application.port.out.IPasswordResetTokenRepository;
import com.fidely.backend.application.port.out.IPasswordResetTokenRepository.PasswordResetTokenData;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.security.IPasswordHasher;
import com.fidely.backend.application.port.out.security.IRefreshTokenGenerator;
import com.fidely.backend.application.port.out.security.ITokenHasher;
import com.fidely.backend.domain.models.auth.AuthIdentity;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.infrastructure.config.ApiUrlProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service applicatif chargé de la réinitialisation
 * des mots de passe.
 */
@Service
public class PasswordResetService
        implements IPasswordResetService {

    private final IUserRepository userRepository;
    private final IAuthIdentityRepository authIdentityRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IPasswordHasher passwordHasher;
    private final ITokenHasher tokenHasher;
    private final IRefreshTokenGenerator tokenGenerator;
    private final IEmailSender emailSender;
    private final ApiUrlProperties apiUrlProperties;

    /**
     * Crée le service de réinitialisation des mots de passe.
     *
     * @param userRepository repository des utilisateurs
     * @param authIdentityRepository repository des identités d'authentification
     * @param passwordResetTokenRepository repository des tokens de réinitialisation
     * @param passwordHasher service de hachage des mots de passe
     * @param tokenHasher service de hachage des tokens
     * @param tokenGenerator générateur de tokens de réinitialisation
     * @param emailSender service d'envoi des emails
     * @param apiUrlProperties configuration de l'URL de l'API
     */
    public PasswordResetService(
            IUserRepository userRepository,
            IAuthIdentityRepository authIdentityRepository,
            IPasswordResetTokenRepository passwordResetTokenRepository,
            IPasswordHasher passwordHasher,
            ITokenHasher tokenHasher,
            IRefreshTokenGenerator tokenGenerator,
            IEmailSender emailSender,
            ApiUrlProperties apiUrlProperties
    ) {
        this.userRepository = userRepository;
        this.authIdentityRepository = authIdentityRepository;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.passwordHasher = passwordHasher;
        this.tokenHasher = tokenHasher;
        this.tokenGenerator = tokenGenerator;
        this.emailSender = emailSender;
        this.apiUrlProperties = apiUrlProperties;
    }

    /**
     * Crée un token de réinitialisation et envoie le lien
     * de réinitialisation à l'utilisateur.
     *
     * @param email adresse email de l'utilisateur
     * @return token de réinitialisation généré
     */
    @Override
    public String requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be null or blank"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"
                ));

        LocalDateTime now = LocalDateTime.now();

        passwordResetTokenRepository.invalidateByUserId(
                user.getId(),
                now
        );

        String token = tokenGenerator.generate();

        String tokenHash = tokenHasher.hash(token);

        PasswordResetTokenData resetToken =
                new PasswordResetTokenData(
                        UUID.randomUUID(),
                        user.getId(),
                        tokenHash,
                        now.plusMinutes(15),
                        null,
                        now
                );

        passwordResetTokenRepository.save(resetToken);

        String resetUrl =
                apiUrlProperties.getUrl()
                        + "auth/reset-password?token="
                        + token;

        emailSender.send(
                email,
                "Réinitialisation de votre mot de passe",
                """
                Bonjour,

                Une demande de réinitialisation de votre mot de passe
                a été effectuée pour votre compte Fidely.

                Pour choisir un nouveau mot de passe, cliquez sur le lien suivant :

                %s

                Ce lien est valable pendant 15 minutes.

                Si vous n'êtes pas à l'origine de cette demande,
                vous pouvez ignorer cet email.

                L'équipe Fidely
                """.formatted(resetUrl)
        );

        return token;
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur à partir
     * d'un token de réinitialisation valide.
     *
     * @param token token de réinitialisation du mot de passe
     * @param newPassword nouveau mot de passe
     */
    @Override
    public void resetPassword(
            String token,
            String newPassword
    ) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid password reset token"
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or blank"
            );
        }

        String tokenHash = tokenHasher.hash(token);

        PasswordResetTokenData resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid password reset token"
                        ));

        LocalDateTime now = LocalDateTime.now();

        if (resetToken.usedAt() != null) {
            throw new IllegalArgumentException(
                    "Password reset token has already been used"
            );
        }

        if (resetToken.expiresAt().isBefore(now)) {
            throw new IllegalArgumentException(
                    "Password reset token has expired"
            );
        }

        AuthIdentity identity =
                authIdentityRepository
                        .findByUserIdAndProvider(
                                resetToken.userId(),
                                "password"
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Password authentication identity not found"
                        ));

        String newPasswordHash =
                passwordHasher.hash(newPassword);

        AuthIdentity updatedIdentity =
                identity.withPasswordHash(newPasswordHash);

        authIdentityRepository.save(updatedIdentity);

        passwordResetTokenRepository.markAsUsed(
                resetToken.id(),
                now
        );
    }
}