package com.fidely.backend.application.port.in;

import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Port d'entrée définissant les opérations métier liées
 * à l'authentification des utilisateurs.
 *
 * <p>Ce port expose les opérations nécessaires à la création
 * des comptes, à l'authentification par e-mail et mot de passe
 * et au renouvellement des tokens d'authentification.</p>
 *
 * <p>La gestion HTTP des cookies et de Spring Security reste
 * en dehors de ce port. La génération et la rotation des tokens
 * sont réalisées par les composants applicatifs et techniques dédiés.</p>
 */
public interface IAuthService {

    /**
     * Crée un compte client.
     *
     * @param email     adresse e-mail du client
     * @param firstName prénom du client
     * @param lastName  nom du client
     * @param phone     numéro de téléphone du client
     * @param birthDate date de naissance du client
     * @param password  mot de passe du client
     * @return client créé
     */
    Customer registerCustomer(
            String email,
            String firstName,
            String lastName,
            String phone,
            LocalDate birthDate,
            String password
    );

    /**
     * Crée un compte gestionnaire de marchand.
     *
     * @param merchantId identifiant du marchand
     * @param email      adresse e-mail du gestionnaire
     * @param firstName  prénom du gestionnaire
     * @param lastName   nom du gestionnaire
     * @param password   mot de passe du gestionnaire
     * @return gestionnaire créé
     */
    MerchantManager registerMerchantManager(
            UUID merchantId,
            String email,
            String firstName,
            String lastName,
            String password
    );

    /**
     * Vérifie l'adresse email d'un utilisateur à partir
     * d'un token de vérification valide.
     *
     * @param token token de vérification email
     */
    void verifyEmail(String token);

    /**
     * Renvoie un email de vérification à un utilisateur
     * dont l'adresse email n'est pas encore vérifiée.
     *
     * @param email adresse e-mail de l'utilisateur
     */
    void resendVerificationEmail(String email);

    /**
     * Authentifie un utilisateur à partir de son adresse e-mail
     * et de son mot de passe.
     *
     * @param email    adresse e-mail
     * @param password mot de passe
     * @return utilisateur authentifié
     */
    User authenticate(String email, String password);

    /**
     * Authentifie un utilisateur et crée une nouvelle session
     * d'authentification.
     *
     * @param email    adresse e-mail
     * @param password mot de passe
     * @return résultat contenant l'utilisateur et les tokens générés
     */
    AuthenticationResult login(String email, String password);

    /**
     * Renouvelle une session d'authentification à partir
     * d'un refresh token valide.
     *
     * <p>Le refresh token présenté est soumis à une rotation :
     * l'ancien token est révoqué et un nouveau refresh token
     * est généré dans la même famille de sessions.</p>
     *
     * @param refreshToken refresh token présenté par le client
     * @return résultat contenant l'utilisateur et les nouveaux tokens
     */
    AuthenticationResult refresh(String refreshToken);

    /**
     * Résultat d'une authentification ou d'un renouvellement
     * de session.
     *
     * @param user         utilisateur authentifié
     * @param accessToken  access token JWT
     * @param refreshToken refresh token en clair destiné au client
     */
    record AuthenticationResult(
            User user,
            String accessToken,
            String refreshToken
    ) {
    }

}