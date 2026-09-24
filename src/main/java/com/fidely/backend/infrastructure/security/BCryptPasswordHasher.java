package com.fidely.backend.infrastructure.security;

import com.fidely.backend.application.port.out.security.IPasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implémentation du hachage des mots de passe utilisant BCrypt.
 *
 * <p>Cette classe appartient à l'infrastructure et implémente
 * le port {@link IPasswordHasher} attendu par la couche application.</p>
 *
 * <p>BCrypt génère automatiquement un sel aléatoire lors du hachage.
 * Le résultat peut donc être différent pour un même mot de passe,
 * tout en permettant sa vérification ultérieure.</p>
 */
@Component
public class BCryptPasswordHasher implements IPasswordHasher {

    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Construit un hasher utilisant la configuration par défaut
     * de {@link BCryptPasswordEncoder}.
     */
    public BCryptPasswordHasher() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be null or blank"
            );
        }

        return passwordEncoder.encode(rawPassword);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(
            String rawPassword,
            String hashedPassword
    ) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return false;
        }

        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        return passwordEncoder.matches(
                rawPassword,
                hashedPassword
        );
    }
}