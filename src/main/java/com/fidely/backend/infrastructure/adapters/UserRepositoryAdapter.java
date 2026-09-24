package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataCustomerRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataMerchantManagerRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataUserRepository;
import com.fidely.backend.infrastructure.entities.models.users.CustomerEntity;
import com.fidely.backend.infrastructure.entities.models.users.MerchantManagerEntity;
import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import com.fidely.backend.infrastructure.entities.mappers.users.CustomerMapper;
import com.fidely.backend.infrastructure.entities.mappers.users.MerchantManagerMapper;
import com.fidely.backend.infrastructure.entities.mappers.users.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant d'utiliser les repositories Spring Data
 * à travers le port de sortie de gestion des utilisateurs.
 *
 * <p>Cet adaptateur orchestre les différentes tables nécessaires
 * à la persistance d'un utilisateur. Les informations communes sont
 * stockées dans {@code users}, tandis que les informations spécifiques
 * sont stockées dans {@code customer_profiles} ou
 * {@code merchant_managers}.</p>
 */
@Repository
public class UserRepositoryAdapter implements IUserRepository {

    private final SpringDataUserRepository userRepository;
    private final SpringDataCustomerRepository customerRepository;
    private final SpringDataMerchantManagerRepository merchantManagerRepository;

    private final UserMapper userMapper;
    private final CustomerMapper customerMapper;
    private final MerchantManagerMapper merchantManagerMapper;

    /**
     * Crée un nouvel adaptateur de repository utilisateur.
     *
     * @param userRepository repository Spring Data des utilisateurs
     * @param customerRepository repository Spring Data des profils clients
     * @param merchantManagerRepository repository Spring Data des profils
     *                                    gestionnaires
     * @param userMapper mapper des utilisateurs
     * @param customerMapper mapper des clients
     * @param merchantManagerMapper mapper des gestionnaires de marchands
     */
    public UserRepositoryAdapter(
            SpringDataUserRepository userRepository,
            SpringDataCustomerRepository customerRepository,
            SpringDataMerchantManagerRepository merchantManagerRepository,
            UserMapper userMapper,
            CustomerMapper customerMapper,
            MerchantManagerMapper merchantManagerMapper
    ) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.merchantManagerRepository = merchantManagerRepository;
        this.userMapper = userMapper;
        this.customerMapper = customerMapper;
        this.merchantManagerMapper = merchantManagerMapper;
    }

    /**
     * Recherche un utilisateur à partir de son identifiant.
     *
     * <p>Le type concret du domaine est déterminé par la présence
     * d'un profil client ou d'un profil gestionnaire.</p>
     *
     * @param userId identifiant de l'utilisateur
     * @return utilisateur correspondant s'il existe
     */
    @Override
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toDomain);
    }

    /**
     * Recherche un utilisateur à partir de son adresse e-mail.
     *
     * @param email adresse e-mail de l'utilisateur
     * @return utilisateur correspondant s'il existe
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDomain);
    }

    /**
     * Enregistre un utilisateur.
     *
     * <p>Les données communes sont enregistrées dans {@code users}
     * et les données spécifiques sont enregistrées dans la table
     * de profil correspondante.</p>
     *
     * @param user utilisateur à enregistrer
     * @return utilisateur enregistré
     */
    @Override
    public User save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);

        UserEntity savedUserEntity = userRepository.save(userEntity);

        if (user instanceof Customer customer) {
            CustomerEntity customerEntity =
                    customerMapper.toEntity(customer);

            customerRepository.save(customerEntity);

            return customerMapper.toDomain(
                    savedUserEntity,
                    customerEntity
            );
        }

        if (user instanceof MerchantManager merchantManager) {
            MerchantManagerEntity merchantManagerEntity =
                    merchantManagerMapper.toEntity(merchantManager);

            merchantManagerRepository.save(merchantManagerEntity);

            return merchantManagerMapper.toDomain(
                    savedUserEntity,
                    merchantManagerEntity
            );
        }

        throw new IllegalArgumentException(
                "Unsupported user type: " + user.getClass().getName()
        );
    }

    /**
     * Supprime un utilisateur à partir de son identifiant.
     *
     * <p>La suppression du profil spécifique est effectuée avant
     * la suppression du compte utilisateur afin de respecter
     * les contraintes de clé étrangère.</p>
     *
     * @param userId identifiant de l'utilisateur
     */
    @Override
    public void deleteById(UUID userId) {
        customerRepository.deleteById(userId);
        merchantManagerRepository.deleteById(userId);
        userRepository.deleteById(userId);
    }

    /**
     * Transforme une entité utilisateur et son profil associé
     * en objet du domaine.
     *
     * @param userEntity entité utilisateur
     * @return utilisateur du domaine
     */
    private User toDomain(UserEntity userEntity) {
        UUID userId = userEntity.getId();

        Optional<CustomerEntity> customerEntity =
                customerRepository.findByUserId(userId);

        if (customerEntity.isPresent()) {
            return customerMapper.toDomain(
                    userEntity,
                    customerEntity.get()
            );
        }

        Optional<MerchantManagerEntity> merchantManagerEntity =
                merchantManagerRepository.findByUserId(userId);

        if (merchantManagerEntity.isPresent()) {
            return merchantManagerMapper.toDomain(
                    userEntity,
                    merchantManagerEntity.get()
            );
        }

        throw new IllegalStateException(
                "No user profile found for user: " + userId
        );
    }
}