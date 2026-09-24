package com.fidely.backend.infrastructure.entities.mappers.users;

import com.fidely.backend.domain.models.users.Customer;
import com.fidely.backend.infrastructure.entities.models.users.CustomerEntity;
import com.fidely.backend.infrastructure.entities.models.users.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper permettant de transformer un client entre le domaine
 * et les entités de persistance.
 *
 * <p>Les informations communes du compte utilisateur sont stockées
 * dans {@link UserEntity}, tandis que les informations spécifiques
 * au client sont stockées dans {@link CustomerEntity}.</p>
 *
 * <p>Ce mapper ne réalise aucun accès à la persistance.</p>
 */
@Component
public class CustomerMapper {

    /**
     * Transforme un client du domaine en entités JPA.
     *
     * <p>La création des deux entités est nécessaire car les données
     * du client sont réparties entre {@link UserEntity} et
     * {@link CustomerEntity}.</p>
     *
     * @param customer client du domaine
     * @return tableau contenant l'entité utilisateur puis le profil client
     * @throws IllegalArgumentException si le client est null
     */
    public UserEntity toUserEntity(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        return new UserEntity(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.isEmailVerified(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    /**
     * Transforme les informations spécifiques d'un client
     * du domaine vers son entité JPA.
     *
     * @param customer client du domaine
     * @return entité du profil client
     * @throws IllegalArgumentException si le client est null
     */
    public CustomerEntity toEntity(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        return new CustomerEntity(
                customer.getId(),
                customer.getPhone(),
                customer.getBirthDate()
        );
    }

    /**
     * Reconstruit un client du domaine à partir de ses entités JPA.
     *
     * @param userEntity entité contenant les informations communes
     * @param customerEntity entité contenant les informations spécifiques
     * @return client du domaine
     * @throws IllegalArgumentException si l'une des entités est null
     */
    public Customer toDomain(
            UserEntity userEntity,
            CustomerEntity customerEntity
    ) {
        if (userEntity == null) {
            throw new IllegalArgumentException(
                    "User entity cannot be null"
            );
        }

        if (customerEntity == null) {
            throw new IllegalArgumentException(
                    "Customer entity cannot be null"
            );
        }

        return new Customer(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                customerEntity.getPhone(),
                userEntity.isEmailVerified(),
                userEntity.isActive(),
                customerEntity.getBirthDate(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }
}