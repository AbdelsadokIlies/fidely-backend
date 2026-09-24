package com.fidely.backend.infrastructure.adapters;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private SpringDataUserRepository userRepository;

    @Mock
    private SpringDataCustomerRepository customerRepository;

    @Mock
    private SpringDataMerchantManagerRepository merchantManagerRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private MerchantManagerMapper merchantManagerMapper;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(
                userRepository,
                customerRepository,
                merchantManagerRepository,
                userMapper,
                customerMapper,
                merchantManagerMapper
        );
    }

    // ---------------------------------------------------------
    // Find by ID
    // ---------------------------------------------------------

    @Test
    void shouldFindCustomerById() {
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = createUserEntity(userId);
        CustomerEntity customerEntity = createCustomerEntity(userId);
        Customer customer = createCustomer(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.of(customerEntity));

        when(customerMapper.toDomain(
                userEntity,
                customerEntity
        )).thenReturn(customer);

        Optional<User> result =
                adapter.findById(userId);

        assertThat(result)
                .isPresent()
                .contains(customer);

        verify(userRepository)
                .findById(userId);

        verify(customerRepository)
                .findByUserId(userId);

        verify(customerMapper)
                .toDomain(
                        userEntity,
                        customerEntity
                );

        verifyNoInteractions(merchantManagerRepository);
    }

    @Test
    void shouldFindMerchantManagerById() {
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = createUserEntity(userId);
        MerchantManagerEntity merchantManagerEntity =
                createMerchantManagerEntity(userId);
        MerchantManager merchantManager =
                createMerchantManager(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(userEntity));

        when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(merchantManagerRepository.findByUserId(userId))
                .thenReturn(Optional.of(merchantManagerEntity));

        when(merchantManagerMapper.toDomain(
                userEntity,
                merchantManagerEntity
        )).thenReturn(merchantManager);

        Optional<User> result =
                adapter.findById(userId);

        assertThat(result)
                .isPresent()
                .contains(merchantManager);

        verify(userRepository)
                .findById(userId);

        verify(customerRepository)
                .findByUserId(userId);

        verify(merchantManagerRepository)
                .findByUserId(userId);

        verify(merchantManagerMapper)
                .toDomain(
                        userEntity,
                        merchantManagerEntity
                );
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        Optional<User> result =
                adapter.findById(userId);

        assertThat(result).isEmpty();

        verify(userRepository)
                .findById(userId);

        verifyNoInteractions(
                customerRepository,
                merchantManagerRepository,
                userMapper,
                customerMapper,
                merchantManagerMapper
        );
    }

    // ---------------------------------------------------------
    // Find by email
    // ---------------------------------------------------------

    @Test
    void shouldFindUserByEmail() {
        UUID userId = UUID.randomUUID();
        String email = "customer@test.com";

        UserEntity userEntity = createUserEntity(userId);
        CustomerEntity customerEntity = createCustomerEntity(userId);
        Customer customer = createCustomer(userId);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(userEntity));

        when(customerRepository.findByUserId(userId))
                .thenReturn(Optional.of(customerEntity));

        when(customerMapper.toDomain(
                userEntity,
                customerEntity
        )).thenReturn(customer);

        Optional<User> result =
                adapter.findByEmail(email);

        assertThat(result)
                .isPresent()
                .contains(customer);

        verify(userRepository)
                .findByEmail(email);

        verify(customerRepository)
                .findByUserId(userId);

        verify(customerMapper)
                .toDomain(
                        userEntity,
                        customerEntity
                );
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Optional<User> result =
                adapter.findByEmail(email);

        assertThat(result).isEmpty();

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(
                customerRepository,
                merchantManagerRepository,
                userMapper,
                customerMapper,
                merchantManagerMapper
        );
    }

    // ---------------------------------------------------------
    // Save customer
    // ---------------------------------------------------------

    @Test
    void shouldSaveCustomer() {
        UUID userId = UUID.randomUUID();

        Customer customer = createCustomer(userId);

        UserEntity userEntity = createUserEntity(userId);
        UserEntity savedUserEntity = createUserEntity(userId);

        CustomerEntity customerEntity =
                createCustomerEntity(userId);

        when(userMapper.toEntity(customer))
                .thenReturn(userEntity);

        when(userRepository.save(userEntity))
                .thenReturn(savedUserEntity);

        when(customerMapper.toEntity(customer))
                .thenReturn(customerEntity);

        when(customerRepository.save(customerEntity))
                .thenReturn(customerEntity);

        when(customerMapper.toDomain(
                savedUserEntity,
                customerEntity
        )).thenReturn(customer);

        User result =
                adapter.save(customer);

        assertThat(result)
                .isSameAs(customer);

        verify(userMapper)
                .toEntity(customer);

        verify(userRepository)
                .save(userEntity);

        verify(customerMapper)
                .toEntity(customer);

        verify(customerRepository)
                .save(customerEntity);

        verify(customerMapper)
                .toDomain(
                        savedUserEntity,
                        customerEntity
                );

        verifyNoInteractions(merchantManagerMapper);
    }

    // ---------------------------------------------------------
    // Save merchant manager
    // ---------------------------------------------------------

    @Test
    void shouldSaveMerchantManager() {
        UUID userId = UUID.randomUUID();

        MerchantManager merchantManager =
                createMerchantManager(userId);

        UserEntity userEntity = createUserEntity(userId);
        UserEntity savedUserEntity = createUserEntity(userId);

        MerchantManagerEntity merchantManagerEntity =
                createMerchantManagerEntity(userId);

        when(userMapper.toEntity(merchantManager))
                .thenReturn(userEntity);

        when(userRepository.save(userEntity))
                .thenReturn(savedUserEntity);

        when(merchantManagerMapper.toEntity(merchantManager))
                .thenReturn(merchantManagerEntity);

        when(merchantManagerRepository.save(merchantManagerEntity))
                .thenReturn(merchantManagerEntity);

        when(merchantManagerMapper.toDomain(
                savedUserEntity,
                merchantManagerEntity
        )).thenReturn(merchantManager);

        User result =
                adapter.save(merchantManager);

        assertThat(result)
                .isSameAs(merchantManager);

        verify(userMapper)
                .toEntity(merchantManager);

        verify(userRepository)
                .save(userEntity);

        verify(merchantManagerMapper)
                .toEntity(merchantManager);

        verify(merchantManagerRepository)
                .save(merchantManagerEntity);

        verify(merchantManagerMapper)
                .toDomain(
                        savedUserEntity,
                        merchantManagerEntity
                );

        verifyNoInteractions(customerMapper);
    }

    // ---------------------------------------------------------
    // Delete
    // ---------------------------------------------------------

    @Test
    void shouldDeleteUserById() {
        UUID userId = UUID.randomUUID();

        adapter.deleteById(userId);

        verify(customerRepository)
                .deleteById(userId);

        verify(merchantManagerRepository)
                .deleteById(userId);

        verify(userRepository)
                .deleteById(userId);
    }

    // ---------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------

    private UserEntity createUserEntity(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        return new UserEntity(
                id,
                "test@test.com",
                "Test",
                "User",
                true,
                true,
                now,
                now
        );
    }

    private CustomerEntity createCustomerEntity(UUID userId) {
        return new CustomerEntity(
                userId,
                "0600000000",
                LocalDate.of(2000, 1, 1)
        );
    }

    private MerchantManagerEntity createMerchantManagerEntity(
            UUID userId
    ) {
        return new MerchantManagerEntity(
                userId,
                UUID.randomUUID()
        );
    }

    private Customer createCustomer(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        return new Customer(
                id,
                "customer@test.com",
                "Test",
                "Customer",
                "0600000000",
                true,
                true,
                LocalDate.of(2000, 1, 1),
                now,
                now
        );
    }

    private MerchantManager createMerchantManager(UUID id) {
        LocalDateTime now = LocalDateTime.now();

        return new MerchantManager(
                id,
                UUID.randomUUID(),
                "merchant@test.com",
                "Test",
                "Manager",
                true,
                true,
                now,
                now
        );
    }
}