package com.fidely.backend.infrastructure.security;

import com.fidely.backend.application.port.out.security.IRefreshTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Génère des refresh tokens cryptographiquement aléatoires.
 */
@Component
public class SecureRandomRefreshTokenGenerator
        implements IRefreshTokenGenerator {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom;

    public SecureRandomRefreshTokenGenerator() {
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String generate() {
        byte[] randomBytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}