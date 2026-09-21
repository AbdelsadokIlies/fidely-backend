package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataLoyaltyTransactionRepository;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyEntity;
import com.fidely.backend.infrastructure.entities.loyalties.LoyaltyTransactionEntity;
import com.fidely.backend.infrastructure.mappers.loyalties.LoyaltyMapper;
import com.fidely.backend.infrastructure.mappers.loyalties.LoyaltyTransactionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant d'utiliser les repositories Spring Data
 * à travers le port de sortie de gestion de la fidélité.
 */
@Repository
public class LoyaltyRepositoryAdapter implements ILoyaltyRepository {

    private final SpringDataLoyaltyRepository loyaltyRepository;
    private final SpringDataLoyaltyTransactionRepository transactionRepository;

    private final LoyaltyMapper loyaltyMapper;
    private final LoyaltyTransactionMapper transactionMapper;

    /**
     * Crée un nouvel adaptateur de repository de fidélité.
     *
     * @param loyaltyRepository repository Spring Data des fidélités
     * @param transactionRepository repository Spring Data des transactions
     * @param loyaltyMapper mapper des fidélités
     * @param transactionMapper mapper des transactions de fidélité
     */
    public LoyaltyRepositoryAdapter(
            SpringDataLoyaltyRepository loyaltyRepository,
            SpringDataLoyaltyTransactionRepository transactionRepository,
            LoyaltyMapper loyaltyMapper,
            LoyaltyTransactionMapper transactionMapper
    ) {
        this.loyaltyRepository = loyaltyRepository;
        this.transactionRepository = transactionRepository;
        this.loyaltyMapper = loyaltyMapper;
        this.transactionMapper = transactionMapper;
    }

    // Loyalty

    @Override
    public Optional<Loyalty> findById(UUID id) {
        return loyaltyRepository.findById(id)
                .map(loyaltyMapper::toDomain);
    }

    @Override
    public Optional<Loyalty> findByCustomerIdAndMerchantId(
            UUID customerId,
            UUID merchantId
    ) {
        return loyaltyRepository
                .findByCustomerIdAndMerchantId(customerId, merchantId)
                .map(loyaltyMapper::toDomain);
    }

    @Override
    public List<Loyalty> findByCustomerId(UUID customerId) {
        return loyaltyRepository.findByCustomerId(customerId)
                .stream()
                .map(loyaltyMapper::toDomain)
                .toList();
    }

    @Override
    public List<Loyalty> findByMerchantId(UUID merchantId) {
        return loyaltyRepository.findByMerchantId(merchantId)
                .stream()
                .map(loyaltyMapper::toDomain)
                .toList();
    }

    @Override
    public Loyalty save(Loyalty loyalty) {
        LoyaltyEntity entity = loyaltyRepository.findById(loyalty.getId())
                .orElseGet(() -> loyaltyMapper.toEntity(loyalty));

        entity.setPointsBalance(loyalty.getPointsBalance());
        entity.setUpdatedAt(loyalty.getUpdatedAt());

        LoyaltyEntity savedEntity =
                loyaltyRepository.save(entity);

        return loyaltyMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteById(UUID id) {
        loyaltyRepository.deleteById(id);
    }

    // Loyalty transactions

    @Override
    public LoyaltyTransaction saveTransaction(
            LoyaltyTransaction transaction
    ) {
        LoyaltyTransactionEntity entity =
                transactionMapper.toEntity(transaction);

        LoyaltyTransactionEntity savedEntity =
                transactionRepository.save(entity);

        return transactionMapper.toDomain(savedEntity);
    }

    @Override
    public List<LoyaltyTransaction> findTransactionsByLoyaltyId(
            UUID loyaltyId
    ) {
        return transactionRepository.findByLoyaltyId(loyaltyId)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<LoyaltyTransaction> findTransactionsByTicketId(
            UUID ticketId
    ) {
        return transactionRepository.findByTicketId(ticketId)
                .stream()
                .map(transactionMapper::toDomain)
                .toList();
    }
}