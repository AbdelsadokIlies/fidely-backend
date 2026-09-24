package com.fidely.backend.infrastructure.security;

import com.fidely.backend.application.port.out.security.ITokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**

 * Implémentation du hachage des tokens avec SHA-256.
 *
 * <p>SHA-256 est utilisé ici car les tokens sont générés
 * aléatoirement et doivent produire un hash déterministe
 * permettant leur recherche en base de données.</p>
 */
@Component
public class Sha256TokenHasher implements ITokenHasher {

    @Override
    public String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token cannot be null or blank"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder result = new StringBuilder();

            for (byte value : hash) {
                result.append(
                        String.format("%02x", value)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }

    }

    @Override
    public boolean matches(
            String token,
            String hash
    ) {
        if (token == null || token.isBlank()) {
            return false;
        }

        if (hash == null || hash.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.UTF_8),
                hash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
