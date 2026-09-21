package com.fidely.backend.application;

import com.fidely.backend.application.port.in.ILoyaltyService;
import com.fidely.backend.application.port.out.ILoyaltyRepository;
import com.fidely.backend.domain.models.loyalties.Loyalty;
import com.fidely.backend.domain.models.loyalties.LoyaltyTransaction;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service applicatif responsable de la gestion des fidélités.
 */
@Service
public class LoyaltyService implements ILoyaltyService {

    private static final int MAX_RETRIES = 3;

    private final ILoyaltyRepository loyaltyRepository;
    private final LoyaltyTransactionService loyaltyTransactionService;

    public LoyaltyService(
            ILoyaltyRepository loyaltyRepository,
            LoyaltyTransactionService loyaltyTransactionService
    ) {
        this.loyaltyRepository = loyaltyRepository;
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Override
    public Loyalty createLoyalty(
            UUID customerId,
            UUID merchantId
    ) {
        LocalDateTime now = LocalDateTime.now();

        Loyalty loyalty = new Loyalty(
                UUID.randomUUID(),
                customerId,
                merchantId,
                0,
                now,
                now
        );

        return loyaltyRepository.save(loyalty);
    }

    @Override
    public Optional<Loyalty> getLoyaltyById(UUID loyaltyId) {
        return loyaltyRepository.findById(loyaltyId);
    }

    @Override
    public Optional<Loyalty> getLoyaltyByCustomerAndMerchant(
            UUID customerId,
            UUID merchantId
    ) {
        return loyaltyRepository.findByCustomerIdAndMerchantId(
                customerId,
                merchantId
        );
    }

    @Override
    public List<Loyalty> getLoyaltiesByCustomer(UUID customerId) {
        return loyaltyRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Loyalty> getLoyaltiesByMerchant(UUID merchantId) {
        return loyaltyRepository.findByMerchantId(merchantId);
    }

    @Override
    public void addPoints(UUID loyaltyId, int points) {
        Loyalty loyalty = getLoyaltyOrThrow(loyaltyId);

        loyalty.addPoints(points);

        loyaltyRepository.save(loyalty);
    }

    @Override
    public void removePoints(UUID loyaltyId, int points) {
        Loyalty loyalty = getLoyaltyOrThrow(loyaltyId);

        loyalty.removePoints(points);

        loyaltyRepository.save(loyalty);
    }

    /**
     * Ajoute les points correspondant à un ticket.
     *
     * <p>En cas de conflit d'optimistic locking, toute la tentative
     * transactionnelle est rejouée depuis le début.</p>
     *
     * @param ticketId identifiant du ticket
     */
    @Override
    public void addPointsFromTicket(UUID ticketId) {

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                loyaltyTransactionService
                        .addPointsFromTicket(ticketId);

                return;

            } catch (OptimisticLockingFailureException exception) {

                if (attempt == MAX_RETRIES) {
                    throw exception;
                }
            }
        }
    }

    @Override
    public List<LoyaltyTransaction> getTransactionsByLoyalty(
            UUID loyaltyId
    ) {
        return loyaltyRepository.findTransactionsByLoyaltyId(loyaltyId);
    }

    @Override
    public List<LoyaltyTransaction> getTransactionsByTicket(
            UUID ticketId
    ) {
        return loyaltyRepository.findTransactionsByTicketId(ticketId);
    }

    @Override
    public void deleteLoyalty(UUID loyaltyId) {
        loyaltyRepository.deleteById(loyaltyId);
    }

    private Loyalty getLoyaltyOrThrow(UUID loyaltyId) {
        return loyaltyRepository.findById(loyaltyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Loyalty introuvable : " + loyaltyId
                        )
                );
    }
}